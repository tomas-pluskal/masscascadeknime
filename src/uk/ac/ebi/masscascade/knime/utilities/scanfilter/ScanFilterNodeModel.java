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
package uk.ac.ebi.masscascade.knime.utilities.scanfilter;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;

/**
 * This is the model implementation of ScanFilter. Filters scans of a scan collection by retention time and/or m/z.
 * 
 * @author Stephan Beisken
 */
public class ScanFilterNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected ScanFilterNodeModel() {

		super(1, 1, CoreTasks.SCAN_FILTER.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		String[] elementsM = settings.getTextOption(Parameter.MZ_RANGE).split("-");
		String[] elementsT = settings.getTextOption(Parameter.TIME_RANGE).split("-");

		Range rangeM = new ExtendableRange(Double.parseDouble(elementsM[0]), Double.parseDouble(elementsM[1]));
		Range rangeT = new ExtendableRange(Double.parseDouble(elementsT[0]), Double.parseDouble(elementsT[1]));

		parameterMap.put(Parameter.MZ_RANGE, rangeM);
		parameterMap.put(Parameter.TIME_RANGE, rangeT);

		return getDataTableSpec(data, Parameter.DATA_COLUMN, Parameter.DATA_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_RANGE, "" + Parameter.MZ_RANGE.getDefaultValue().toString());
			settings.setTextOption(Parameter.TIME_RANGE, "" + Parameter.TIME_RANGE.getDefaultValue().toString());
		}

		return getDataTableSpec(inSpecs, Parameter.DATA_COLUMN, Parameter.DATA_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
		NodeUtils.validateDoubleRange(tmpSettings, Parameter.MZ_RANGE);
		NodeUtils.validateDoubleRange(tmpSettings, Parameter.TIME_RANGE);
	}
}
