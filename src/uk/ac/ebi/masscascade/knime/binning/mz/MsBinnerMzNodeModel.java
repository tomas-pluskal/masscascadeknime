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
package uk.ac.ebi.masscascade.knime.binning.mz;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.binning.MzBinning;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * This is the model implementation of MsBinner. Bins the scans of the mass spectrometry dataset in the m/z domain.
 * 
 * @author Stephan Beisken
 */
public class MsBinnerMzNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected MsBinnerMzNodeModel() {

		super(1, 1, CoreTasks.MASS_DOMAIN.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		MzBinning.BinningType selectedButton = null;
		if (settings.getBooleanOption("Max"))
			selectedButton = MzBinning.BinningType.MAX;
		else if (settings.getBooleanOption("Min"))
			selectedButton = MzBinning.BinningType.MIN;
		else if (settings.getBooleanOption("Sum"))
			selectedButton = MzBinning.BinningType.SUM;
		else if (settings.getBooleanOption("Avg"))
			selectedButton = MzBinning.BinningType.AVG;

		parameterMap.put(Parameter.MZ_WINDOW_AMU, settings.getDoubleOption(Parameter.MZ_WINDOW_AMU));

		if (selectedButton == null)
			selectedButton = MzBinning.BinningType.MAX;
		parameterMap.put(Parameter.AGGREGATION, selectedButton);

		return getDataTableSpec(data, Parameter.DATA_COLUMN, Parameter.DATA_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_WINDOW_AMU, "" + Parameter.MZ_WINDOW_AMU.getDefaultValue());
			settings.setTextOption(Parameter.AGGREGATION, "Avg");
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
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_AMU);
	}
}
