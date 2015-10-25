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
package uk.ac.ebi.masscascade.knime.utilities.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.core.scan.ScanLevel;
import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.TextUtils;

/**
 * This is the model implementation of PropertyExtractor. Extracts meta
 * information from mass spec data files.
 * 
 * @author Stephan Beisken
 */
public class PropertyExtractorNodeModel extends NodeModel {

	private final List<File> ids = new ArrayList<File>();
	private final Settings settings = new DefaultSettings();
	private DataColumnSpec[] dataColumnSpecs;

	/**
	 * Constructor for the node model.
	 */
	protected PropertyExtractorNodeModel() {

		super(1, 1);
	}

	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();
		ColumnRearranger rearranger = createColumnRearranger(inSpec, false);
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0], rearranger, exec);

		return new BufferedDataTable[] { outTable };
	}

	/**
	 * {@inheritDoc}
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, boolean append)
			throws InvalidSettingsException {

		NodeUtils.getDataTableSpec(inSpec, settings, Parameter.DATA_COLUMN);
		final int colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.DATA_COLUMN));

		dataColumnSpecs = new DataColumnSpec[4];
		dataColumnSpecs[0] = new DataColumnSpecCreator("Id", StringCell.TYPE).createSpec();
		dataColumnSpecs[1] = new DataColumnSpecCreator("Date", StringCell.TYPE).createSpec();
		dataColumnSpecs[2] = new DataColumnSpecCreator("Authors", StringCell.TYPE).createSpec();
		dataColumnSpecs[3] = new DataColumnSpecCreator("MSn levels", ListCell.getCollectionType(DoubleCell.TYPE))
				.createSpec();

		ColumnRearranger result = new ColumnRearranger(inSpec);

		result.append(new CellFactory() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public DataCell[] getCells(DataRow row) {

				DataCell dataCell = row.getCell(colIndex);
				DataCell[] newCells = new DataCell[dataColumnSpecs.length];
				if (dataCell.isMissing()) {
					Arrays.fill(newCells, DataType.getMissingCell());
					return newCells;
				}

				if (!(dataCell instanceof MsValue)) {
					throw new IllegalArgumentException("No data cell at " + colIndex + ": "
							+ dataCell.getClass().getName());
				}

				ScanContainer rawFile = ((MsValue) dataCell).getMsDataValue();

				newCells[0] = new StringCell(TextUtils.cleanId(rawFile.getScanInfo().getId())[0]);
				newCells[1] = new StringCell(rawFile.getScanInfo().getDate());
				newCells[2] = new StringCell(rawFile.getScanInfo().getAuthors());

				List<DoubleCell> msnCells = new ArrayList<DoubleCell>();
				for (ScanLevel level : rawFile.getScanLevels()) {

					msnCells.add(new DoubleCell(level.getMsn().getLvl()));
					msnCells.add(new DoubleCell(level.getScanRange().getLowerBounds()));
					msnCells.add(new DoubleCell(level.getScanRange().getUpperBounds()));
					msnCells.add(new DoubleCell(level.getMzRange().getLowerBounds()));
					msnCells.add(new DoubleCell(level.getMzRange().getUpperBounds()));
					msnCells.add(new DoubleCell(rawFile.size(level.getMsn())));
				}

				newCells[3] = CollectionCellFactory.createListCell(msnCells);

				return newCells;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public DataColumnSpec[] getColumnSpecs() {
				return dataColumnSpecs;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setProgress(int curRowNr, int rowCount, RowKey lastKey, ExecutionMonitor exec) {

				exec.setProgress(curRowNr / (double) rowCount, "Retrieved conversions for row " + curRowNr + " (\""
						+ lastKey + "\")");
			}
		});
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		DataTableSpec outSpec = createColumnRearranger(inSpecs[0], false).createSpec();
		return new DataTableSpec[] { outSpec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);
		if ((tmpSettings.getColumnName(Parameter.DATA_COLUMN) == null)
				|| (tmpSettings.getColumnName(Parameter.DATA_COLUMN).length() == 0)) {
			throw new InvalidSettingsException("No valid column.");
		}
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		FileReader fileReader = new FileReader(nodeInternDir + File.separator + "pointers");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			File file = new File(line);
			if (!file.exists()) {
				bufferedReader.close();
				throw new IOException("Serialized data file missing: " + line);
			}
			ids.add(file);
		}
		bufferedReader.close();
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

//		if (settings.getBooleanOption(DefaultDialog.TERMINUS))
//			return;

		for (File id : ids)
			id.delete();
		ids.clear();
	}
}
