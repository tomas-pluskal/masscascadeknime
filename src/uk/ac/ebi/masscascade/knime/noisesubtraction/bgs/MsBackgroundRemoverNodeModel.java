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
package uk.ac.ebi.masscascade.knime.noisesubtraction.bgs;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.background.BackgroundSubtraction;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.Trace;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;

import com.google.common.collect.TreeMultimap;

/**
 * This is the model implementation of MsBackgroundRemover. Removes the reference background from all mass spectrometry
 * runs in the target table.
 * 
 * @author Stephan Beisken
 */
public class MsBackgroundRemoverNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected MsBackgroundRemoverNodeModel() {

		super(2, 1, CoreTasks.BACKGROUND_SUBTRACTION.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		RawContainer backgroundFile = null;
		final int backgroundIndex = data[1].getDataTableSpec().findColumnIndex(
				settings.getColumnName(Parameter.REFERENCE_COLUMN));

		for (DataRow row : data[1]) {
			backgroundFile = ((MsValue) row.getCell(backgroundIndex)).getMsDataValue();
			break;
		}
		
		TreeMultimap<Range, Trace> refMap = new BackgroundSubtraction().getReference(backgroundFile);
		
		parameterMap.put(Parameter.TIME_WINDOW, settings.getDoubleOption(Parameter.TIME_WINDOW));
		parameterMap.put(Parameter.MZ_WINDOW_PPM, settings.getDoubleOption(Parameter.MZ_WINDOW_PPM));
		parameterMap.put(Parameter.SCALE_FACTOR, settings.getDoubleOption(Parameter.SCALE_FACTOR));
		parameterMap.put(Parameter.REFERENCE_RAW_MAP, refMap);

		return getDataTableSpec(data, Parameter.DATA_COLUMN, Parameter.DATA_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.TIME_WINDOW, "" + Parameter.TIME_WINDOW.getDefaultValue());
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.SCALE_FACTOR, "" + Parameter.SCALE_FACTOR.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[1], settings, Parameter.REFERENCE_COLUMN);
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
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.REFERENCE_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.TIME_WINDOW);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.SCALE_FACTOR);
	}
}
