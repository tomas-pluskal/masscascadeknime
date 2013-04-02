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
package uk.ac.ebi.masscascade.knime.profileperception.correlation;

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
 * This is the model implementation of CosineCompiler. Node to generate pseudospectra based on pariwise cosine
 * similarity between extracted mass traces in the time domain.
 * 
 * @author Stephan Beisken
 */
public class CosineCompilerNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected CosineCompilerNodeModel() {
		super(1, 1, CoreTasks.COSINE_SIMILARITY.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		parameterMap.put(Parameter.CORRELATION_THRESHOLD, settings.getDoubleOption(Parameter.CORRELATION_THRESHOLD));
		parameterMap.put(Parameter.BINS, settings.getIntOption(Parameter.BINS));

		return getDataTableSpec(data, Parameter.PEAK_COLUMN, Parameter.SPECTRUM_COLUMN, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.CORRELATION_THRESHOLD,
					"" + Parameter.CORRELATION_THRESHOLD.getDefaultValue());
			settings.setTextOption(Parameter.BINS, "" + Parameter.BINS.getDefaultValue());
		}

		return getDataTableSpec(inSpecs, Parameter.PEAK_COLUMN, Parameter.SPECTRUM_COLUMN, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.CORRELATION_THRESHOLD);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.BINS);
	}
}
