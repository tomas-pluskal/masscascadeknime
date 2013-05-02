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
package uk.ac.ebi.masscascade.knime.deconvolution.savitzkygolay;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of SavitzkyGolayDeconvolution. Performs Savitzky Golay deconvolution on a collection
 * of peak profiles.
 * 
 * @author Stephan Beisken
 */
public class SavitzkyGolayDeconvolutionNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected SavitzkyGolayDeconvolutionNodeModel() {
		super(1, 1, CoreTasks.SG_DECONVOLUTION.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		parameterMap.put(Parameter.MIN_PROFILE_INTENSITY, settings.getDoubleOption(Parameter.MIN_PROFILE_INTENSITY));
		parameterMap.put(Parameter.DERIVATIVE_THRESHOLD, settings.getDoubleOption(Parameter.DERIVATIVE_THRESHOLD));
		parameterMap.put(Parameter.SCAN_WINDOW, settings.getIntOption(Parameter.SCAN_WINDOW));
		parameterMap.put(Parameter.SG_LEVEL, settings.getIntOption(Parameter.SG_LEVEL));

		return getDataTableSpec(data, new Parameter[] { Parameter.PEAK_COLUMN, Parameter.DATA_COLUMN },
				Parameter.PEAK_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.SCAN_WINDOW, "" + Parameter.SCAN_WINDOW.getDefaultValue());
			settings.setTextOption(Parameter.MIN_PROFILE_INTENSITY,
					"" + Parameter.MIN_PROFILE_INTENSITY.getDefaultValue());
			settings.setTextOption(Parameter.DERIVATIVE_THRESHOLD,
					"" + Parameter.DERIVATIVE_THRESHOLD.getDefaultValue());
			settings.setTextOption(Parameter.SG_LEVEL, "" + Parameter.SG_LEVEL.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.PEAK_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.SCAN_WINDOW);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MIN_PROFILE_INTENSITY);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.DERIVATIVE_THRESHOLD);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.SG_LEVEL);
	}
}
