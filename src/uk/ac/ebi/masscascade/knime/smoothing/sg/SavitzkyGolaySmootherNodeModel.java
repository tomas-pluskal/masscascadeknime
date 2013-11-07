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
package uk.ac.ebi.masscascade.knime.smoothing.sg;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of SavitzkyGolaySmoother. Applies
 * Savitzky-Golay smoothing to the mass spectrometry data set.
 * 
 * @author Stephan Beisken
 */
public class SavitzkyGolaySmootherNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected SavitzkyGolaySmootherNodeModel() {

		super(1, 1, CoreTasks.SAVITZGY_GOLAY.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		parameterMap.put(Parameter.MS_LEVEL, Constants.MSN.MS1);
		parameterMap.put(Parameter.POLYNOMIAL_ORDER, settings.getIntOption(Parameter.POLYNOMIAL_ORDER));
		parameterMap.put(Parameter.SCAN_WINDOW, settings.getIntOption(Parameter.SCAN_WINDOW));

		return getDataTableSpec(data, Parameter.FEATURE_COLUMN, Parameter.FEATURE_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.POLYNOMIAL_ORDER, "" + Parameter.POLYNOMIAL_ORDER.getDefaultValue());
			settings.setTextOption(Parameter.SCAN_WINDOW, "" + 6);
		}

		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.FEATURE_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.POLYNOMIAL_ORDER);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.SCAN_WINDOW);
	}
}
