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
package uk.ac.ebi.masscascade.knime.defaults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.knime.base.data.append.column.AppendedColumnRow;
import org.knime.base.data.replace.ReplacedColumnsDataRow;
import org.knime.base.node.parallel.builder.ThreadedTableBuilderNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.RowAppender;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.interfaces.CallableTask;
import uk.ac.ebi.masscascade.interfaces.Task;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.interfaces.container.SpectrumContainer;
import uk.ac.ebi.masscascade.knime.NodeParm;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsCell;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileCell;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumCell;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;

/**
 * This is the model template implementation for MassCascade nodes.
 * 
 * @author Stephan Beisken
 */
public abstract class DefaultModel extends ThreadedTableBuilderNodeModel {

	protected final List<File> ids = new ArrayList<File>();
	protected final Settings settings = new DefaultSettings();
	protected final ParameterMap parameterMap;

	// the first index indicates the column to be replaced (if set)
	// all subsequent values are additional input container (one per type, max 3)
	private int[] colIndex;
	// all values are input container (one per type, max 3)
	private Parameter dataColumnIn[];
	
	private Class<? extends Task> taskClass;
	private boolean replace;

	/**
	 * Constructor for the default node model.
	 */
	protected DefaultModel(int inPorts, int outPorts, Class<? extends Task> taskClass) {

		super(inPorts, outPorts);

		this.taskClass = taskClass;

		parameterMap = new ParameterMap();
	}

	/**
	 * Allows the post-construction task definition.
	 * 
	 * @param taskClass the task class
	 */
	public void setTaskClass(Class<? extends CallableTask> taskClass) {

		this.taskClass = taskClass;
	}

	public DataTableSpec[] getDataTableSpec(final DataTable[] data, Parameter dataColumnIn, Parameter dataColumnOut,
			boolean append) {
		return getDataTableSpec(new DataTableSpec[] { data[0].getDataTableSpec() }, new Parameter[] { dataColumnIn },
				dataColumnOut, append);
	}

	public DataTableSpec[] getDataTableSpec(final DataTable[] data, Parameter[] dataColumnIn, Parameter dataColumnOut,
			boolean append) {
		return getDataTableSpec(new DataTableSpec[] { data[0].getDataTableSpec() }, dataColumnIn, dataColumnOut, append);
	}

	public DataTableSpec[] getDataTableSpec(final DataTableSpec[] inSpecs, final Parameter dataColumnIn,
			final Parameter dataColumnOut, final boolean append) {
		return getDataTableSpec(inSpecs, new Parameter[] { dataColumnIn }, dataColumnOut, append);
	}

	public DataTableSpec[] getDataTableSpec(final DataTableSpec[] inSpecs, final Parameter dataColumnIn[],
			final Parameter dataColumnOut, final boolean append) {

		this.replace = !append;
		this.dataColumnIn = dataColumnIn;

		DataTableSpec inSpec = inSpecs[0];
		colIndex = findDataColumn(inSpec);

		if (replace)
			return new DataTableSpec[] { inSpec };

		DataColumnSpecCreator specCreator = null;
		if (dataColumnOut == Parameter.PEAK_COLUMN)
			specCreator = new DataColumnSpecCreator(dataColumnOut.getDescription(), ProfileCell.TYPE);
		else if (dataColumnOut == Parameter.DATA_COLUMN)
			specCreator = new DataColumnSpecCreator(dataColumnOut.getDescription(), MsCell.TYPE);
		else if (dataColumnOut == Parameter.SPECTRUM_COLUMN)
			specCreator = new DataColumnSpecCreator(dataColumnOut.getDescription(), SpectrumCell.TYPE);

		return new DataTableSpec[] { new DataTableSpec(inSpec, new DataTableSpec(specCreator.createSpec())) };
	}

	/**
	 * Find the index of the requested data column in the data column specification.
	 * 
	 * @param inSpec the data column specification
	 * @return the index of the requested data column
	 */
	private int[] findDataColumn(DataTableSpec inSpec) {

		int[] dataCol = new int[dataColumnIn.length];
		int j = 0;
		for (Parameter columnIn : dataColumnIn) {
			dataCol[j] = inSpec.findColumnIndex(settings.getColumnName(columnIn));

			if (dataCol[j] == -1) {
				int i = 0;
				for (DataColumnSpec dcs : inSpec) {
					if (dcs.getType().isCompatible(NodeParm.columnClass.get(columnIn)))
						dataCol[j] = i;
					i++;
				}

				if (dataCol[j] != -1) {
					String name = inSpec.getColumnSpec(dataCol[j]).getName();
					settings.setColumnName(columnIn, name);
				}
			}
			j++;
		}

		return dataCol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRow(final DataRow inRow, final BufferedDataTable[] additionalData,
			final RowAppender[] outputTables) throws Exception {

		try {
			
			ParameterMap taskParms = parameterMap.clone();
	
			for (int i = 0; i < colIndex.length; i++) {
				DataCell cell = inRow.getCell(colIndex[i]);
				if (cell.isMissing()) {
					setWarningMessage("Missing cell: " + inRow.getKey() + " -- skipped");
					skipRow(outputTables, inRow);
					return;
				}
	
				Parameter columnIn = dataColumnIn[i];
	
				Container file = null;
				if (columnIn == Parameter.DATA_COLUMN) {
					file = ((MsValue) cell).getMsDataValue();
					taskParms.put(Parameter.RAW_CONTAINER, file);
				} else if (columnIn.equals(Parameter.PEAK_COLUMN)) {
					file = ((ProfileValue) cell).getPeakDataValue();
					taskParms.put(Parameter.PROFILE_CONTAINER, file);
				} else if (columnIn.equals(Parameter.SPECTRUM_COLUMN)) {
					file = ((SpectrumValue) cell).getSpectrumDataValue();
					taskParms.put(Parameter.SPECTRUM_CONTAINER, file);
				}
			}
	
			Constructor<?> cstr = taskClass.getConstructor(ParameterMap.class);
			Task task = (Task) cstr.newInstance(taskParms);
			Container container = task.call();
	
			if (container == null) {
				setWarningMessage("Process failed: " + inRow.getKey() + " -- skipped");
				skipRow(outputTables, inRow);
				return;
			}
	
			ids.add(container.getDataFile());
			DataCell outCell;
			if (container instanceof RawContainer)
				outCell = new MsCell((RawContainer) container);
			else if (container instanceof ProfileContainer)
				outCell = new ProfileCell((ProfileContainer) container);
			else if (container instanceof SpectrumContainer)
				outCell = new SpectrumCell((SpectrumContainer) container);
			else
				outCell = DataType.getMissingCell();
	
			if (replace)
				outputTables[0].addRowToTable(new ReplacedColumnsDataRow(inRow, outCell, colIndex[0]));
			else
				outputTables[0].addRowToTable(new AppendedColumnRow(inRow, outCell));
			
		} catch (Exception exception) {
			setWarningMessage("Node execution failed for \"" + taskClass.getSimpleName() + "\". Details below.");
			throw exception;
		}
	}

	private void skipRow(final RowAppender[] outputTables, DataRow inRow) {

		if (replace)
			outputTables[0].addRowToTable(new ReplacedColumnsDataRow(inRow, DataType.getMissingCell(), colIndex[0]));
		else
			outputTables[0].addRowToTable(new AppendedColumnRow(inRow, DataType.getMissingCell()));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException;
}
