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
package uk.ac.ebi.masscascade.knime.io.write;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.ThreadPool;

import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.io.CmlWriter;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;

/**
 * This is the model implementation of MsFileWriter. Writes mass spectrometry files to disk in PSI MzML format.
 * 
 * @author Stephan Beisken
 */
public class MsFileWriterNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected MsFileWriterNodeModel() {

		super(1, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();
		final int colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.DATA_COLUMN));

		final List<Future<Container>> tasks = new ArrayList<Future<Container>>();

		int threadNumber = NodePlugin.getNumberOfThreads();
		ThreadPool threadPool = new ThreadPool(threadNumber);

		String path = settings.getTextOption(Parameter.OUTPUT_DIRECTORY);
		double currentRow = 1;
		double threadCounter = 1;
		double rowCount = inData[0].getRowCount();

		try {
			for (DataRow row : inData[0]) {

				exec.checkCanceled();
				exec.setProgress((double) currentRow / rowCount, "processing rows " + (currentRow - threadCounter)
						+ " - " + currentRow);

				RawContainer rawFile = ((MsValue) row.getCell(colIndex)).getMsDataValue();

				ParameterMap params = new ParameterMap();
				params.put(Parameter.RAW_CONTAINER, rawFile);
				params.put(Parameter.OUTPUT_DIRECTORY, path);
				CmlWriter task = new CmlWriter(params);
				tasks.add(threadPool.enqueue(task));
			}
			if (threadPool.getRunningThreads() == threadNumber) {
				threadPool.waitForTermination();
				threadCounter = 0;
			}

			currentRow++;
			threadCounter++;

			threadPool.waitForTermination();
		} finally {
			threadPool.interruptAll();
			threadPool.shutdown();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.OUTPUT_DIRECTORY, "" + Parameter.OUTPUT_DIRECTORY.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
		return new DataTableSpec[] {};
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
		// nothing to do
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to do
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
		// nothing to do
	}
}
