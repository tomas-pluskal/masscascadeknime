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
package uk.ac.ebi.masscascade.knime.msn;

import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.library.LibraryParameter;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.SearchTasks;

/**
 * This is the model implementation of the MSnEnumerator node.
 * 
 * @author Stephan Beisken
 */
public class MsnEnumeratorNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected MsnEnumeratorNodeModel() {
		super(1, 1, SearchTasks.MSN_ENUMERATOR.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		parameterMap.put(Parameter.MZ_WINDOW_AMU, settings.getDoubleOption(Parameter.MZ_WINDOW_AMU));
		parameterMap.put(LibraryParameter.DEPTH, settings.getIntOption(LibraryParameter.DEPTH));
		parameterMap.put(Parameter.EXECUTABLE, settings.getTextOption(Parameter.EXECUTABLE));

		return getDataTableSpec(data, Parameter.FEATURE_SET_COLUMN, Parameter.FEATURE_SET_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(LibraryParameter.DEPTH, "" + LibraryParameter.DEPTH.getDefaultValue());
			settings.setTextOption(Parameter.MZ_WINDOW_AMU, "" + Parameter.MZ_WINDOW_AMU.getDefaultValue());
			settings.setTextOption(Parameter.EXECUTABLE, "" + Parameter.EXECUTABLE.getDefaultValue());
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
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_AMU);
		NodeUtils.validateIntGreaterZero(tmpSettings, LibraryParameter.DEPTH);
		NodeUtils.validateTextNotEmpty(tmpSettings, Parameter.EXECUTABLE);
	}
}
