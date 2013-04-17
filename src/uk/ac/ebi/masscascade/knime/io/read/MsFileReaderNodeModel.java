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
package uk.ac.ebi.masscascade.knime.io.read;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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

import uk.ac.ebi.masscascade.core.container.file.FileContainerBuilder;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.io.PsiMzmlReader;
import uk.ac.ebi.masscascade.io.XCaliburReader;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsCell;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;
import uk.ac.ebi.masscascade.utilities.PackUtils;

/**
 * This is the model implementation of MzFileReader. File reader node for Thermo RAW and PSI mzML mass spectrometry
 * files.
 * 
 * @author Stephan Beisken
 */
public class MsFileReaderNodeModel extends NodeModel {

	private final NodeLogger logger = NodeLogger.getLogger(MsFileReaderNodeModel.class);

	private final MsFileReaderSettings settings = new MsFileReaderSettings();
	private List<File> scanFileIds = new ArrayList<File>();

	private static final String RAWLOCATION = "uk/ac/ebi/masscascade/thermo/RAWdumpProfile.exe";
	private static final String DLLLOCATION = "uk/ac/ebi/masscascade/thermo/XRawfile2.dll";

	/**
	 * Constructor for the node model.
	 */
	protected MsFileReaderNodeModel() {
		super(0, 2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		BufferedDataContainer msFileCont = exec.createDataContainer(createOutSpec()[0]);
		BufferedDataContainer brokenCont = exec.createDataContainer(createOutSpec()[1]);

		Map<Future<Container>, String> fTasks = new HashMap<Future<Container>, String>();

		int threadNumber = NodePlugin.getNumberOfThreads();
		ThreadPool threadPool = new ThreadPool(threadNumber);

		double currentRow = 1;
		double threadCounter = 1;

		File[] files = settings.files();
		if (files.length == 1 && files[0].isDirectory())
			files = files[0].listFiles(MsFileReaderNodeDialog.FILTER);

		try {
			for (File file : files) {

				if (!file.exists())
					continue;

				exec.checkCanceled();
				exec.setProgress((double) currentRow / files.length, "processing files " + (currentRow - threadCounter)
						+ " - " + currentRow);

				fTasks.put(threadPool.enqueue(getReader(file.getName(), file)), file.getName());

				if (threadPool.getRunningThreads() == threadNumber) {
					threadPool.waitForTermination();
					threadCounter = 0;
				}

				currentRow++;
				threadCounter++;
			}

			threadPool.waitForTermination();

		} catch (Exception exception) {
			logger.error("File / Directory could not be read: " + exception.getMessage());
			threadPool.interruptAll();
		} finally {
			threadPool.shutdown();
		}

		int rowId = 0;
		RawContainer fTaskResult = null;
		for (Future<Container> fTask : fTasks.keySet()) {
			try {
				fTaskResult = (RawContainer) fTask.get();
				scanFileIds.add(fTaskResult.getDataFile());
				msFileCont.addRowToTable(new DefaultRow(new RowKey("" + rowId), new MsCell(fTaskResult)));
			} catch (Exception exception) {
				logger.error("File could not be read: " + exception.getMessage());
				exception.printStackTrace();
				brokenCont.addRowToTable(new DefaultRow(new RowKey("" + rowId), new StringCell(fTasks.get(fTask))));
			}
			rowId++;
		}
		fTasks = null;

		msFileCont.close();
		brokenCont.close();

		return new BufferedDataTable[] { msFileCont.getTable(), brokenCont.getTable() };
	}

	private Callable<Container> getReader(String fileName, File file) throws IOException {

		if (fileName.lastIndexOf(".") <= 0)
			throw new IOException("File prefix missing: " + fileName);

		// "~" as global delimiter: file name ~ task1 - task2 - ...
		String name = fileName.substring(0, fileName.lastIndexOf(".")) + "~";
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

		ParameterMap params = new ParameterMap();
		params.put(Parameter.DATA_FILE, file);
		params.put(Parameter.RAW_CONTAINER,
				FileContainerBuilder.getInstance().newInstance(RawContainer.class, name,
						NodePlugin.getProjectDirectory()));

		try {
			if (suffix.equalsIgnoreCase(Constants.FILE_FORMATS.RAW.name())) {
				return new XCaliburReader(params);
			} else if (suffix.equalsIgnoreCase(Constants.FILE_FORMATS.MZML.name())) {
				return new PsiMzmlReader(params);
			} 
		} catch (Exception exception) {
			throw new IOException(fileName + ". " + exception.getMessage());
		}
		throw new IOException("File not found: " + fileName + ".");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

		if (this.settings.retainData())
			return;

		NodeUtils.deleteScanFiles(scanFileIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.filePath() == null)
			settings.filePath(System.getProperty("user.dir"));
		if (settings.files() == null)
			settings.files(new File[] { new File(System.getProperty("user.dir")) });

		return createOutSpec();
	}

	private DataTableSpec[] createOutSpec() {

		DataColumnSpec msData = new DataColumnSpecCreator("MS Data", MsCell.TYPE).createSpec();
		DataTableSpec outSpec = new DataTableSpec(msData);

		DataColumnSpec filePath = new DataColumnSpecCreator("File Path", StringCell.TYPE).createSpec();
		DataTableSpec outBrokenSpec = new DataTableSpec(filePath);

		return new DataTableSpec[] { outSpec, outBrokenSpec };
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

		MsFileReaderSettings tmpSettings = new MsFileReaderSettings();
		tmpSettings.loadSettings(settings);
		if (tmpSettings.files() == null) {
			throw new InvalidSettingsException("No files or directory selected.");
		}

		File[] files = tmpSettings.files();
		for (File file : files) {

			if (!file.exists()) {
				throw new InvalidSettingsException("File or directory does not exist: " + file.getAbsolutePath());
			} else if (file.isFile()) {
				if (file.getName().substring(file.getName().indexOf(".") + 1)
						.equalsIgnoreCase(Constants.FILE_FORMATS.RAW.toString())) {
					checkAndPreparePlatform();
				}
			} else if (file.isDirectory()) {
				for (String s : file.list(MsFileReaderNodeDialog.FILTER)) {
					if (s.substring(s.indexOf(".") + 1).equalsIgnoreCase(Constants.FILE_FORMATS.RAW.toString())) {
						checkAndPreparePlatform();
					}
				}
			}
		}
	}

	private void checkAndPreparePlatform() throws InvalidSettingsException {

		String osName = System.getProperty("os.name").toUpperCase();
		if (!osName.toUpperCase().contains("WINDOWS"))
			setWarningMessage("Platform not supported for Thermo RAW files. Extracting archived RAW reader. ");
		if (!new File(System.getProperty("java.io.tmpdir") + File.separator + "RAWdumpProfile.exe").exists()) {
			try {
				URI uri = PackUtils.getJarURI(RawContainer.class);
				PackUtils.getFile(uri, RAWLOCATION);
				PackUtils.getFile(uri, DLLLOCATION);
			} catch (Exception exception) {
				setWarningMessage("Failed to load RAWdump.");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		NodeUtils.loadInternals(internDir, exec, scanFileIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		NodeUtils.saveInternals(internDir, exec, scanFileIds);
	}
}
