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
package uk.ac.ebi.masscascade.knime.identification.ion;

import java.util.TreeMap;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
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
 * This is the model implementation of IonSearch. Searches for the input masses and their adducts/fragments in all peak
 * collections.
 * 
 * @author Stephan Beisken
 */
public class IonSearchNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected IonSearchNodeModel() {
		super(2, 1, CoreTasks.ION_FINDER.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		final int labelIndex = data[1].getDataTableSpec().findColumnIndex(
				settings.getColumnName(Parameter.LABEL_COLUMN));
		final int massIndex = data[1].getDataTableSpec()
				.findColumnIndex(settings.getColumnName(Parameter.VALUE_COLUMN));

		parameterMap.put(Parameter.MZ_WINDOW_PPM, settings.getDoubleOption(Parameter.MZ_WINDOW_PPM));

		double delta = 0;
		if (settings.getBooleanOption(Parameter.POSITIVE_MODE))
			delta = Constants.PARTICLES.PROTON.getMass();
		else if (settings.getBooleanOption(Parameter.NEGATIVE_MODE))
			delta = Constants.PARTICLES.PROTON.getMass() * -1;

		TreeMap<Double, String> adductMap = new TreeMap<Double, String>();
		for (DataRow row : data[1]) {
			if (row.getCell(labelIndex).isMissing() || row.getCell(massIndex).isMissing()) continue;
			String name = ((StringValue) row.getCell(labelIndex)).getStringValue();
			double mass = ((DoubleValue) row.getCell(massIndex)).getDoubleValue() + delta;
			
			adductMap.put(mass, name);
		}
		parameterMap.put(Parameter.ION_LIST, adductMap);

		return getDataTableSpec(data, Parameter.FEATURE_SET_COLUMN, Parameter.FEATURE_SET_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[1], settings, Parameter.LABEL_COLUMN);
		NodeUtils.getDataTableSpec(inSpecs[1], settings, Parameter.VALUE_COLUMN);
		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.FEATURE_SET_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_SET_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.LABEL_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.VALUE_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
	}
}
