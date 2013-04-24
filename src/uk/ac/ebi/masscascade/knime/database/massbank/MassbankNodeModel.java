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
package uk.ac.ebi.masscascade.knime.database.massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.ThreadPool;

import uk.ac.ebi.masscascade.interfaces.CallableWebservice;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.SpectrumContainer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumCell;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;
import uk.ac.ebi.masscascade.ws.massbank.MassBankBatchSearch;

/**
 * This is the model implementation of Massbank. Spectrum-based Massbank database search.
 * 
 * @author Stephan Beisken
 */
public class MassbankNodeModel extends NodeModel {

	private NodeLogger LOGGER = NodeLogger.getLogger(this.getClass());

	private final MassbankSettings settings = new MassbankSettings();
	private final List<File> ids = new ArrayList<File>();
	private List<Future<Container>> tasks;

	/**
	 * Constructor for the node model.
	 */
	protected MassbankNodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();

		final int colIndex = findDataColumn(inSpec);

		int threadNumber = NodePlugin.getNumberOfThreads();
		ThreadPool threadPool = new ThreadPool(threadNumber);

		tasks = new ArrayList<Future<Container>>();

		SpectrumContainer container = null;
		double currentRow = 1;
		double threadCounter = 1;
		double rowCount = inData[0].getRowCount();

		try {
			for (DataRow row : inData[0]) {

				exec.checkCanceled();
				exec.setProgress((double) currentRow / rowCount, "processing rows " + (currentRow - threadCounter)
						+ " - " + currentRow);

				container = ((SpectrumValue) row.getCell(colIndex)).getSpectrumDataValue();
				ParameterMap params = new ParameterMap();
				params.put(Parameter.SCORE, settings.getScore());
				params.put(Parameter.ION_MODE, settings.getIonMode());

				List<String> instruments = new ArrayList<String>(Arrays.asList(settings.getInstruments()));

				params.put(Parameter.INSTRUMENTS, instruments);
				params.put(Parameter.RESULTS, settings.getMaxNumOfResults());
				params.put(Parameter.MIN_PROFILES, settings.getMinNumOfProfiles());
				params.put(Parameter.SPECTRUM_CONTAINER, container);

				CallableWebservice task = new MassBankBatchSearch(params);
				tasks.add(threadPool.enqueue(task));

				if (threadPool.getRunningThreads() == threadNumber) {
					threadPool.waitForTermination();
					threadCounter = 0;
				}

				currentRow++;
				threadCounter++;
			}

			threadPool.waitForTermination();

		} catch (Exception exception) {
			LOGGER.error(this, exception);
			threadPool.interruptAll();
		} finally {
			threadPool.shutdown();
		}

		ColumnRearranger rearranger = createColumnRearranger(inSpec);
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0], rearranger, exec);

		tasks = null;

		return new BufferedDataTable[] { outTable };
	}

	/**
	 * Creates a custom column rearranger.
	 * 
	 * @param inSpec input data specification
	 * @return the custom column rearranger object
	 * @throws InvalidSettingsException unexpected behaviour
	 */
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec) throws InvalidSettingsException {

		final int colIndex = inSpec.findColumnIndex(settings.getSpectrumColumn());

		ColumnRearranger result = new ColumnRearranger(inSpec);

		result.replace(new SingleCellFactory(inSpec.getColumnSpec(colIndex)) {

			private int fileIndex = 0;

			@Override
			public DataCell getCell(final DataRow row) {

				DataCell cell = row.getCell(colIndex);
				if (cell.isMissing()) {
					setWarningMessage("Missing cell: " + row.getKey() + " -- skipped");
					return DataType.getMissingCell();
				}

				try {
					Container file = tasks.get(fileIndex++).get();

					ids.add(file.getDataFile());
					return new SpectrumCell((SpectrumContainer) file);

				} catch (Exception exception) {
					LOGGER.error(this, exception);
					setWarningMessage("Erroneous cell: " + row.getKey() + " -- skipped");
					return DataType.getMissingCell();
				}
			}
		}, colIndex);
		return result;
	}

	/**
	 * Find the index of the requested data column in the data column specification.
	 * 
	 * @param inSpec the data column specification
	 * @return the index of the requested data column
	 */
	private int findDataColumn(DataTableSpec inSpec) {

		int dataCol = inSpec.findColumnIndex(settings.getSpectrumColumn());

		if (dataCol == -1) {
			int i = 0;
			for (DataColumnSpec dcs : inSpec) {
				if (dcs.getType().isCompatible(SpectrumValue.class)) {
					dataCol = i;
				}
				i++;
			}

			if (dataCol != -1) {
				String name = inSpec.getColumnSpec(dataCol).getName();
				settings.setSpectrumColumn(name);
			}
		}

		return dataCol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

		for (File id : ids) {
			id.delete();
		}
		ids.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		String spectrumColumn = "";
		int isomerColumn = inSpecs[0].findColumnIndex(settings.getSpectrumColumn());
		if (isomerColumn == -1) {
			int i = 0;
			for (DataColumnSpec spec : inSpecs[0]) {
				if (spec.getType().isCompatible(SpectrumValue.class)) {
					isomerColumn = i;
					spectrumColumn = spec.getName();
				}
				i++;
			}

			if (isomerColumn == -1) {
				throw new InvalidSettingsException("Column '" + settings.getSpectrumColumn() + "' does not exist");
			}

			settings.setSpectrumColumn(spectrumColumn);
		}

		return new DataTableSpec[] { inSpecs[0] };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		this.settings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		this.settings.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		MassbankSettings tmpSettings = new MassbankSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getSpectrumColumn() == null || tmpSettings.getSpectrumColumn().length() == 0) {
			throw new InvalidSettingsException("No valid spectrum column.");
		}

		if (tmpSettings.getScore() < 0 || tmpSettings.getScore() > 1) {
			throw new InvalidSettingsException("Score must be between 0 and 1.");
		}

		if (tmpSettings.getMaxNumOfResults() <= 0) {
			throw new InvalidSettingsException("Maximum results must be positive.");
		}

		if (tmpSettings.getMinNumOfProfiles() <= 0) {
			throw new InvalidSettingsException("Min. no. of profiles must be positive.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileReader fileReader = new FileReader(internDir + File.separator + "pointers");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			File file = new File(line);
			if (!file.exists())
				throw new IOException("Serialized data file missing: " + line);
			ids.add(file);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileWriter fileWriter = new FileWriter(internDir + File.separator + "pointers");
		for (File id : ids) {
			fileWriter.write(id.getAbsolutePath() + "\n");
		}
		fileWriter.close();
	}

}
