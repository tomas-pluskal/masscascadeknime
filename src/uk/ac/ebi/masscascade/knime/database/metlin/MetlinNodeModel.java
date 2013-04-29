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
package uk.ac.ebi.masscascade.knime.database.metlin;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.WebTasks;

/**
 * This is the model implementation of the Metlin node.
 * 
 * @author Stephan Beisken
 */
public class MetlinNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected MetlinNodeModel() {
		super(1, 1, WebTasks.METLIN.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		Constants.ION_MODE ionMode;
		if (settings.getTextOption(Parameter.POSITIVE_MODE).equals("true"))
			ionMode = Constants.ION_MODE.POSITIVE;
		else
			ionMode = Constants.ION_MODE.NEGATIVE;

		parameterMap.put(Parameter.MZ_WINDOW_PPM, settings.getDoubleOption(Parameter.MZ_WINDOW_PPM));
		parameterMap.put(Parameter.MZ_WINDOW_AMU, settings.getDoubleOption(Parameter.MZ_WINDOW_AMU));
		parameterMap.put(Parameter.SECURITY_TOKEN, settings.getTextOption(Parameter.SECURITY_TOKEN));
		parameterMap.put(Parameter.COLLISION_ENERGY, settings.getIntOption(Parameter.COLLISION_ENERGY));
		parameterMap.put(Parameter.SCORE_METLIN, settings.getIntOption(Parameter.SCORE_METLIN));
		parameterMap.put(Parameter.ION_MODE, ionMode);

		return getDataTableSpec(data, Parameter.SPECTRUM_COLUMN, Parameter.SPECTRUM_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.MZ_WINDOW_AMU, "" + Parameter.MZ_WINDOW_AMU.getDefaultValue());
			settings.setTextOption(Parameter.SECURITY_TOKEN, "" + Parameter.SECURITY_TOKEN.getDefaultValue());
			settings.setTextOption(Parameter.POSITIVE_MODE, "" + Parameter.POSITIVE_MODE.getDefaultValue());
			settings.setTextOption(Parameter.COLLISION_ENERGY, "" + Parameter.COLLISION_ENERGY.getDefaultValue());
			settings.setTextOption(Parameter.SCORE_METLIN, "" + Parameter.SCORE_METLIN.getDefaultValue());
		}

		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.SPECTRUM_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.SPECTRUM_COLUMN);
		NodeUtils.validateTextNotEmpty(tmpSettings, Parameter.SECURITY_TOKEN);
		NodeUtils.validateIntervalInt(tmpSettings, Parameter.MZ_WINDOW_PPM, 1, 100);
		NodeUtils.validateIntervalDouble(tmpSettings, Parameter.MZ_WINDOW_AMU, 0.001, 0.5);
		NodeUtils.validateIntGreaterZero(tmpSettings, Parameter.COLLISION_ENERGY);
		NodeUtils.validateIntervalInt(tmpSettings, Parameter.SCORE_METLIN, 0, 100);
	}
}
