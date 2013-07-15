/*
 * Copyright (C) 2013 EMBL - European Bioinformatics Institute
 * 
 * All rights reserved. This file is part of the MassCascade feature for KNIME.
 * 
 * The feature is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * The feature is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with the feature. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * Contributors: Stephan Beisken - initial API and implementation
 */
package uk.ac.ebi.masscascade.knime.alignment.obiwarp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.data.replace.ReplacedColumnsDataRow;
import org.knime.base.node.parallel.builder.ThreadedTableBuilderNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.RowAppender;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.alignment.Obiwarp;
import uk.ac.ebi.masscascade.alignment.ObiwarpHelper;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileCell;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;
import uk.ac.ebi.masscascade.utilities.TextUtils;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;

/**
 * This is the model implementation of MsFastDtw. Fast dynamic time warp for mass spectrometry sample alignment.
 * 
 * @author Stephan Beisken
 */
public class ObiwarpNodeModel extends ThreadedTableBuilderNodeModel {

	private final List<File> ids = new ArrayList<File>();
	private final Settings settings = new DefaultSettings();
	private final ParameterMap parameterMap;

	private AtomicInteger rowId;
	private int colIndex;

	/**
	 * Constructor for the node model.
	 */
	protected ObiwarpNodeModel() {

		super(2, 2);
		parameterMap = new ParameterMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		DataTableSpec inSpec = data[1].getDataTableSpec();
		rowId = new AtomicInteger(1);
		colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.REFERENCE_PROFILE_COLUMN));
		ProfileContainer refContainer = null;
		for (DataRow row : data[1]) {
			refContainer = ((ProfileValue) row.getCell(colIndex)).getPeakDataValue();
			break;
		}

		Range mzRange = new ExtendableRange(refContainer.iterator().next().getMz());
		Range timeRange = new ExtendableRange(refContainer.getTimes().keySet().first(), refContainer.getTimes()
				.keySet().last());
		for (Profile profile : refContainer)
			mzRange.extendRange(profile.getMzRange());

		for (DataRow row : data[0]) {
			ProfileContainer inContainer = ((ProfileValue) row.getCell(colIndex)).getPeakDataValue();
			timeRange.extendRange(inContainer.getTimes().keySet().first());
			timeRange.extendRange(inContainer.getTimes().keySet().last());
			for (Profile profile : inContainer)
				mzRange.extendRange(profile.getMzRange());
		}

		// take boundaries into account
		timeRange.extendRange(timeRange.getLowerBounds() - 5);
		timeRange.extendRange(timeRange.getUpperBounds() + 5);

		double mzBinWidth = settings.getDoubleOption(Parameter.BIN_WIDTH_MZ);
		double timeBinWidth = settings.getDoubleOption(Parameter.BIN_WIDTH_RT);
		ObiwarpHelper obiHelper = new ObiwarpHelper(mzBinWidth, mzRange, timeBinWidth, timeRange);
		File refFile = obiHelper.buildLmataFile(refContainer);
		refFile.deleteOnExit();

		parameterMap.put(Parameter.REFERENCE_FILE, refFile);
		parameterMap.put(Parameter.BIN_WIDTH_MZ, mzBinWidth);
		parameterMap.put(Parameter.BIN_WIDTH_RT, timeBinWidth);
		parameterMap.put(Parameter.MZ_RANGE, mzRange);
		parameterMap.put(Parameter.TIME_RANGE, timeRange);

		parameterMap.put(Parameter.EXECUTABLE, settings.getTextOption(Parameter.EXECUTABLE));
		parameterMap.put(Parameter.GAP_INIT, settings.getDoubleOption(Parameter.GAP_INIT));
		parameterMap.put(Parameter.GAP_EXTEND, settings.getDoubleOption(Parameter.GAP_EXTEND));
		parameterMap.put(Parameter.RESPONSE, settings.getDoubleOption(Parameter.RESPONSE));

		return new DataTableSpec[] { inSpec, new DataTableSpec(createOutputTableSpecification()) };
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		createColumnSpec(dataColumnSpecs, "container id", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "profile id", IntCell.TYPE);
		createColumnSpec(dataColumnSpecs, "time shift", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "retention time", DoubleCell.TYPE);

		return dataColumnSpecs.toArray(new DataColumnSpec[] {});
	}

	/**
	 * Creates a single column specification.
	 */
	private void createColumnSpec(List<DataColumnSpec> dataColumnSpecs, String colName, DataType cellType) {

		DataColumnSpec colSpec = new DataColumnSpecCreator(colName, cellType).createSpec();
		dataColumnSpecs.add(colSpec);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRow(final DataRow inRow, final BufferedDataTable[] additionalData,
			final RowAppender[] outputTables) throws Exception {

		try {

			ParameterMap taskParms = parameterMap.clone();

			DataCell cell = inRow.getCell(colIndex);
			if (cell.isMissing()) {
				setWarningMessage("Missing cell: " + inRow.getKey() + " -- skipped");
				outputTables[0].addRowToTable(new ReplacedColumnsDataRow(inRow, DataType.getMissingCell(), colIndex));
				return;
			}

			Container file = ((ProfileValue) cell).getPeakDataValue();
			taskParms.put(Parameter.PROFILE_CONTAINER, file);

			Obiwarp obiwarp = new Obiwarp(taskParms);
			Container container = obiwarp.call();

			if (container == null) {
				setWarningMessage("Process failed: " + inRow.getKey() + " -- skipped");
				outputTables[0].addRowToTable(new ReplacedColumnsDataRow(inRow, DataType.getMissingCell(), colIndex));
				return;
			}

			ids.add(container.getDataFile());
			DataCell outCell = new ProfileCell((ProfileContainer) container);
			outputTables[0].addRowToTable(new ReplacedColumnsDataRow(inRow, outCell, colIndex));
			
			DataCell[] cells = new DataCell[4];
			for (Entry<Integer, Double[]> entry : obiwarp.getTimeDiffMap().entrySet()) {
				cells[0] = new StringCell(TextUtils.cleanId(file.getId()));
				cells[1] = new IntCell(entry.getKey());
				cells[2] = new DoubleCell(entry.getValue()[0]);
				cells[3] = new DoubleCell(entry.getValue()[1]); 
				outputTables[1].addRowToTable(new DefaultRow(RowKey.createRowKey(rowId.incrementAndGet()), cells));
			}	


		} catch (Exception exception) {
			setWarningMessage("Node execution failed for \"" + Obiwarp.class.getSimpleName() + "\". Details below.");
			throw exception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.EXECUTABLE, "" + Parameter.EXECUTABLE.getDefaultValue());
			settings.setTextOption(Parameter.GAP_INIT, "" + Parameter.GAP_INIT.getDefaultValue());
			settings.setTextOption(Parameter.GAP_EXTEND, "" + Parameter.GAP_EXTEND.getDefaultValue());
			settings.setTextOption(Parameter.RESPONSE, "" + Parameter.RESPONSE.getDefaultValue());
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.TIME_WINDOW, "" + Parameter.TIME_WINDOW.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[1], settings, Parameter.PEAK_COLUMN);
		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.PEAK_COLUMN);

		return new DataTableSpec[] { inSpecs[0], new DataTableSpec(createOutputTableSpecification()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);

		String exe = tmpSettings.getTextOption(Parameter.EXECUTABLE);
		File exeFile = new File(exe);
		if (!exeFile.exists() || !exeFile.isFile())
			throw new InvalidSettingsException("Executable not found.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileReader fileReader = new FileReader(nodeInternDir + File.separator + "pointers");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			File file = new File(line);
			if (!file.exists())
				throw new IOException("Serialized data file missing: " + line);
			ids.add(file);
		}
		bufferedReader.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileWriter fileWriter = new FileWriter(nodeInternDir + File.separator + "pointers");
		for (File id : ids)
			fileWriter.write(id.getAbsolutePath() + "\n");
		fileWriter.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

		if (settings.getBooleanOption(DefaultDialog.TERMINUS))
			return;

		for (File id : ids)
			id.delete();
		ids.clear();
	}
}
