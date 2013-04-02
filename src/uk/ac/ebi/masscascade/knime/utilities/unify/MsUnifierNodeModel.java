/*
 * Copyright (C) 2013 EMBL - European Bioinformatics Institute
 * 
 * All rights reserved. This file is part of the MassCascade feature for KNIME.
 * 
 * The feature is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 * 
 * The feature is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * the feature. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *    Stephan Beisken - initial API and implementation
 */
package uk.ac.ebi.masscascade.knime.utilities.unify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.core.container.file.FileContainerBuilder;
import uk.ac.ebi.masscascade.core.raw.ScanImpl;
import uk.ac.ebi.masscascade.interfaces.Scan;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsCell;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * This is the model implementation of MsUnifier. Joins all runs in the input table of cell type MsDataCell.
 * 
 * @author Stephan Beisken
 */
public class MsUnifierNodeModel extends NodeModel {

	private final List<File> ids = new ArrayList<File>();
	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected MsUnifierNodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();
		final int colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.DATA_COLUMN));

		String fileKeys = "";
		Map<Double, Integer> retentionTimeMap = new HashMap<Double, Integer>();
		Map<Integer, Integer> scanIndexMap = new HashMap<Integer, Integer>();
		Map<Integer, RawContainer> fileIndexMap = new HashMap<Integer, RawContainer>();
		int j = 0;
		RawContainer file = null;
		double rt;
		for (DataRow row : inData[0]) {
			file = ((MsValue) row.getCell(colIndex)).getMsDataValue();
			XYList ticData = file.getTicChromatogram(Constants.MSN.MS1).getData();
			int i = 0;
			for (XYPoint xyPoint : ticData) {
				rt = xyPoint.x;
				retentionTimeMap.put(rt, j);
				scanIndexMap.put(j, i);
				fileIndexMap.put(j, file);
				i++;
				j++;
			}
			fileKeys = fileKeys + " " + row.getKey().toString();
		}

		Map<Double, Integer> sortedRetentionTimes = new TreeMap<Double, Integer>(retentionTimeMap);

		Scan dbScan = null;
		RawContainer rawContainer = FileContainerBuilder.getInstance().newInstance(RawContainer.class,
				"Background-" + System.currentTimeMillis(), NodePlugin.getProjectDirectory());
		ids.add(rawContainer.getDataFile());

		int scanIndex = 0;
		int oldScanIndex = 0;
		for (double sRt : sortedRetentionTimes.keySet()) {
			j = sortedRetentionTimes.get(sRt);
			oldScanIndex = scanIndexMap.get(j);
			dbScan = fileIndexMap.get(j).getScanByIndex(oldScanIndex);
			rawContainer.addScan(new ScanImpl(scanIndex, dbScan.getMsn(), dbScan.getIonMode(), dbScan.getData(), dbScan
					.getMzRange(), dbScan.getBasePeak(), dbScan.getRetentionTime(), dbScan.getTotalIonCurrent(), dbScan
					.getParentScan(), dbScan.getParentCharge(), dbScan.getParentMz()));
			scanIndex++;
		}

		fileIndexMap = null;
		scanIndexMap = null;
		retentionTimeMap = null;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		rawContainer.finaliseFile(dateFormat.format(date));

		DataColumnSpec msData = new DataColumnSpecCreator("MS Data", MsCell.TYPE).createSpec();
		DataTableSpec outSpec = new DataTableSpec(msData);
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		DataCell[] combTableCells = new DataCell[] { new MsCell(rawContainer) };
		container.addRowToTable(new DefaultRow(new RowKey("0"), combTableCells));
		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
	}

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
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileWriter fileWriter = new FileWriter(nodeInternDir + File.separator + "pointers");
		for (File id : ids)
			fileWriter.write(id.getAbsolutePath() + "\n");
		fileWriter.close();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {

		this.settings.saveSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {

		this.settings.loadSettings(settings);
	}

	@Override
	protected void reset() {

		if (settings.getBooleanOption(DefaultDialog.TERMINUS))
			return;

		for (File id : ids)
			id.delete();
		ids.clear();
	}
}
