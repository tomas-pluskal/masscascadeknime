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
package uk.ac.ebi.masscascade.knime.visualization.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
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

import uk.ac.ebi.masscascade.alignment.ProfileBinTableModel;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of MzFileReader. File reader node for Thermo RAW and PSI mzML mass spectrometry
 * files.
 * 
 * @author Stephan Beisken
 */
public class ProfileViewerNodeModel extends NodeModel {
	
	private final Settings settings = new DefaultSettings();
	
	private ProfileBinTableModel model;

	/**
	 * Constructor for the node model.
	 */
	protected ProfileViewerNodeModel() {
		super(1, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		List<Container> profileContainers = new ArrayList<Container>();
		
		int colIndex = inData[0].getDataTableSpec().findColumnIndex(settings.getColumnName(Parameter.PEAK_COLUMN));
		for (DataRow row : inData[0]) {
			DataCell cell = row.getCell(colIndex);
			if (cell.isMissing()) continue;
			profileContainers.add(((ProfileValue) cell).getPeakDataValue());
		}
		
		double ppm = settings.getDoubleOption(Parameter.MZ_WINDOW_PPM);
		double sec = settings.getDoubleOption(Parameter.TIME_WINDOW);
		
		model = new ProfileBinTableModel(profileContainers, ppm, sec, 0);
		
		return null;
	}
	
	public ProfileBinTableModel getModel() {
		return model;
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
		
		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.PEAK_COLUMN);
		return new DataTableSpec[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.TIME_WINDOW);
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
	protected void reset() {
		// nothing to do
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to do
	}
}
