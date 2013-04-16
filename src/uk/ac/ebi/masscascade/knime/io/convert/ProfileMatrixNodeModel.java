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
package uk.ac.ebi.masscascade.knime.io.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.alignment.ProfileBin;
import uk.ac.ebi.masscascade.alignment.ProfileBinTableModel;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of the "ProfileMatrix" node to build the m/z to sample matrix.
 * 
 * @author Stephan Beisken
 */
public class ProfileMatrixNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected ProfileMatrixNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		List<Container> profileContainers = new ArrayList<Container>();

		String colName = settings.getColumnName(Parameter.PEAK_COLUMN) == null ? settings
				.getColumnName(Parameter.SPECTRUM_COLUMN) : settings.getColumnName(Parameter.PEAK_COLUMN);
		int colIndex = inData[0].getDataTableSpec().findColumnIndex(colName);
		
		for (DataRow row : inData[0]) {
			DataCell cell = row.getCell(colIndex);
			if (cell.isMissing())
				continue;
			
			if (cell instanceof ProfileValue) profileContainers.add(((ProfileValue) cell).getPeakDataValue());
			else profileContainers.add(((SpectrumValue) cell).getSpectrumDataValue());
		}

		double ppm = settings.getDoubleOption(Parameter.MZ_WINDOW_PPM);
		double sec = settings.getDoubleOption(Parameter.TIME_WINDOW);

		ProfileBinTableModel model = new ProfileBinTableModel(profileContainers, ppm, sec);
		BufferedDataContainer dataContainer = exec.createDataContainer(new DataTableSpec(
				createOutputTableSpecification(profileContainers, model.getRows())));

		int id = 1;
		DataCell[] cellRow;
		List<ProfileBin> rows = model.getRows();

		if (settings.getBooleanOption(ProfileMatrixNodeFactory.CLASSIC_MATRIX)) {
			cellRow = new DataCell[rows.size() + 1];
			int mzIndex = 0;
			cellRow[mzIndex++] = new StringCell("Sample");
			for (ProfileBin row : rows)
				cellRow[mzIndex++] = new DoubleCell(row.getMz());
			dataContainer.addRowToTable(new DefaultRow(new RowKey(id++ + ""), cellRow));
			for (int i = 0; i < profileContainers.size(); i++) {
				mzIndex = 0;
				cellRow[mzIndex++] = new StringCell(profileContainers.get(i).getId());
				for (ProfileBin row : rows) {
					double intensity = row.isPresent(i);
					cellRow[mzIndex++] = intensity > 0 ? new DoubleCell(intensity) : DataType.getMissingCell();
				}
				dataContainer.addRowToTable(new DefaultRow(new RowKey(id++ + ""), cellRow));
			}
		} else {
			for (ProfileBin row : rows) {
				cellRow = new DataCell[model.getColumnCount() - 1];
				cellRow[0] = new DoubleCell(row.getMz());
				cellRow[1] = new DoubleCell(row.getRt());
				cellRow[2] = new DoubleCell(row.getArea());
				cellRow[3] = new StringCell(row.getLabel());
				cellRow[4] = new DoubleCell(row.getMzDev());
				for (int i = 6; i < model.getColumnCount(); i++) {
					double intensity = row.isPresent(i - ProfileBin.COLUMNS);
					cellRow[i - 1] = intensity > 0 ? new DoubleCell(intensity) : DataType.getMissingCell();
				}
				dataContainer.addRowToTable(new DefaultRow(new RowKey(id++ + ""), cellRow));
			}
		}
		dataContainer.close();

		return new BufferedDataTable[] { dataContainer.getTable() };
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification(List<Container> container, List<ProfileBin> rows) {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		if (settings.getBooleanOption(ProfileMatrixNodeFactory.CLASSIC_MATRIX)) {
			createColumnSpec(dataColumnSpecs, "Sample", StringCell.TYPE);
			for (int i = 1; i <= rows.size(); i++)
				createColumnSpec(dataColumnSpecs, i + "", DoubleCell.TYPE);
		} else {
			createColumnSpec(dataColumnSpecs, "m/z", DoubleCell.TYPE);
			createColumnSpec(dataColumnSpecs, "rt", DoubleCell.TYPE);
			createColumnSpec(dataColumnSpecs, "area", DoubleCell.TYPE);
			createColumnSpec(dataColumnSpecs, "label", StringCell.TYPE);
			createColumnSpec(dataColumnSpecs, "m/z dev", DoubleCell.TYPE);
			for (int i = 0; i < container.size(); i++)
				createColumnSpec(dataColumnSpecs, container.get(i).getId(), DoubleCell.TYPE);
		}
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.TIME_WINDOW, "" + Parameter.TIME_WINDOW.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.PEAK_COLUMN, Parameter.SPECTRUM_COLUMN);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getColumnName(Parameter.PEAK_COLUMN) == null)
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.SPECTRUM_COLUMN);
		else 
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.TIME_WINDOW);
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
