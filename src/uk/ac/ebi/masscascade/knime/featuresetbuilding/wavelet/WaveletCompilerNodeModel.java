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
package uk.ac.ebi.masscascade.knime.featuresetbuilding.wavelet;

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
 * This is the model implementation of WaveletCompiler. Compiles spectra using a wavelet transform.
 * 
 * @author Stephan Beisken
 */
public class WaveletCompilerNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model template.
	 */
	protected WaveletCompilerNodeModel() {
		super(1, 1, CoreTasks.WAVELET.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		parameterMap.put(Parameter.MIN_FEATURE_INTENSITY, settings.getDoubleOption(Parameter.MIN_FEATURE_INTENSITY));
		parameterMap.put(Parameter.SCALE_FACTOR, settings.getIntOption(Parameter.SCALE_FACTOR));
		parameterMap.put(Parameter.WAVELET_WIDTH, settings.getDoubleOption(Parameter.WAVELET_WIDTH));

		return getDataTableSpec(data, Parameter.DATA_COLUMN, Parameter.DATA_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MIN_FEATURE_INTENSITY, "" + Parameter.MIN_FEATURE_INTENSITY.getDefaultValue());
			settings.setTextOption(Parameter.SCALE_FACTOR, "" + Parameter.SCALE_FACTOR.getDefaultValue());
			settings.setTextOption(Parameter.WAVELET_WIDTH, "" + Parameter.WAVELET_WIDTH.getDefaultValue());
		}

		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MIN_FEATURE_INTENSITY);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.SCALE_FACTOR);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.WAVELET_WIDTH);
	}
}
