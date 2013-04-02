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
package uk.ac.ebi.masscascade.knime.visualization.threed;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.tableview.TableContentModel;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of MsViewer. Visualises the data (scans and meta information) of the selected mass
 * spectrometry run.
 * 
 * @author Stephan Beisken
 */
public class Spectrum3DNodeModel extends NodeModel implements BufferedDataTableHolder {

	private final TableContentModel contentModel;
	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected Spectrum3DNodeModel() {

		super(1, 0);
		contentModel = new TableContentModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		setInternalTables(inData);
		return new BufferedDataTable[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MASS_RESOLUTION, "" + Parameter.MASS_RESOLUTION.getDefaultValue());
			settings.setTextOption(Parameter.TIME_RESOLUTION, "" + Parameter.TIME_RESOLUTION.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
		return null;
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

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getColumnName(Parameter.DATA_COLUMN) == null
				|| tmpSettings.getColumnName(Parameter.DATA_COLUMN).length() == 0) {
			throw new InvalidSettingsException("No MsDataCell column chosen");
		}
	}

	/** {@inheritDoc} */
	@Override
	public BufferedDataTable[] getInternalTables() {

		return new BufferedDataTable[] { (BufferedDataTable) contentModel.getDataTable() };
	}

	/** {@inheritDoc} */
	@Override
	public void setInternalTables(final BufferedDataTable[] tables) {

		contentModel.setDataTable(tables[0]);
		contentModel.setHiLiteHandler(getInHiLiteHandler(0));
	}

	public TableContentModel getContentModel() {

		return contentModel;
	}

	public Settings getSettings() {

		return settings;
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
