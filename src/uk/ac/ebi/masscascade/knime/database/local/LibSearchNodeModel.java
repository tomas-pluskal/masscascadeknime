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
package uk.ac.ebi.masscascade.knime.database.local;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.librarycell.LibraryValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.library.LibraryParameter;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.SearchTasks;
import uk.ac.ebi.masscascade.reference.ReferenceContainer;

/**
 * This is the model implementation of the Metlin node.
 * 
 * @author Stephan Beisken
 */
public class LibSearchNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected LibSearchNodeModel() {
		super(2, 1, SearchTasks.LIBRARY_BATCH_SEARCH.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		DataTableSpec inSpec = data[1].getDataTableSpec();
		final int colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.REFERENCE_COLUMN));
		List<ReferenceContainer> refs = new ArrayList<ReferenceContainer>();
		for (DataRow row : data[1])
			refs.add(((LibraryValue) row.getCell(colIndex)).getLibraryValue());

		Constants.ION_MODE ionMode;
		if (settings.getTextOption(Parameter.POSITIVE_MODE).equals("true"))
			ionMode = Constants.ION_MODE.POSITIVE;
		else
			ionMode = Constants.ION_MODE.NEGATIVE;

		parameterMap.put(LibraryParameter.REFERENCE_LIBRARY_LIST, refs);
		parameterMap.put(Parameter.MS_LEVEL, Constants.MSN.get(settings.getIntOption(Parameter.MS_LEVEL)));
		parameterMap.put(Parameter.MZ_WINDOW_PPM, settings.getDoubleOption(Parameter.MZ_WINDOW_PPM));
		parameterMap.put(Parameter.MZ_WINDOW_AMU, settings.getDoubleOption(Parameter.MZ_WINDOW_AMU));
		parameterMap.put(Parameter.COLLISION_ENERGY, settings.getIntOption(Parameter.COLLISION_ENERGY));
		parameterMap.put(Parameter.SCORE, settings.getIntOption(Parameter.SCORE));
		parameterMap.put(Parameter.ION_MODE, ionMode);

		return getDataTableSpec(data, Parameter.FEATURE_SET_COLUMN, Parameter.FEATURE_SET_COLUMN, false);
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
			settings.setTextOption(Parameter.MS_LEVEL, "1");
			settings.setTextOption(Parameter.COLLISION_ENERGY, "" + Parameter.COLLISION_ENERGY.getDefaultValue());
			settings.setTextOption(Parameter.SCORE, "" + Parameter.SCORE.getDefaultValue());
		}

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
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.REFERENCE_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_AMU);
		NodeUtils.validateIntGreaterOrEqualZero(tmpSettings, Parameter.COLLISION_ENERGY);
		NodeUtils.validateIntGreaterZero(tmpSettings, Parameter.MS_LEVEL);
		NodeUtils.validateIntervalInt(tmpSettings, Parameter.SCORE, 0, 1000);
	}
}
