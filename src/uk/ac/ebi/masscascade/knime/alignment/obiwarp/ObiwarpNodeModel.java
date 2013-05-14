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
package uk.ac.ebi.masscascade.knime.alignment.obiwarp;

import java.io.File;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import uk.ac.ebi.masscascade.alignment.ObiwarpHelper;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultModel;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.CoreTasks;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;

/**
 * This is the model implementation of MsFastDtw. Fast dynamic time warp for mass spectrometry sample alignment.
 * 
 * @author Stephan Beisken
 */
public class ObiwarpNodeModel extends DefaultModel {

	/**
	 * Constructor for the node model.
	 */
	protected ObiwarpNodeModel() {

		super(2, 1, CoreTasks.OBIWARP.getCallableClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(final DataTable[] data) throws Exception {

		DataTableSpec inSpec = data[1].getDataTableSpec();
		final int colIndex = inSpec.findColumnIndex(settings.getColumnName(Parameter.REFERENCE_PROFILE_COLUMN));
		ProfileContainer refContainer = null;
		for (DataRow row : data[1]) {
			refContainer = ((ProfileValue) row.getCell(colIndex)).getPeakDataValue();
			break;
		}

		Range mzRange = new ExtendableRange(refContainer.iterator().next().getMz());
		Range timeRange = new ExtendableRange(refContainer.getTimes().keySet().first(), refContainer.getTimes()
				.keySet().last());
		for (Profile profile : refContainer)
			mzRange.extendRange(profile.getMzRange());

		for (DataRow row : data[0]) {
			ProfileContainer inContainer = ((ProfileValue) row.getCell(colIndex)).getPeakDataValue();
			timeRange.extendRange(inContainer.getTimes().keySet().first());
			timeRange.extendRange(inContainer.getTimes().keySet().last());
			for (Profile profile : inContainer)
				mzRange.extendRange(profile.getMzRange());
		}
		
		// take boundaries into account
		timeRange.extendRange(timeRange.getLowerBounds() - 5);
		timeRange.extendRange(timeRange.getUpperBounds() + 5);

		double mzBinWidth = settings.getDoubleOption(Parameter.BIN_WIDTH_MZ);
		double timeBinWidth = settings.getDoubleOption(Parameter.BIN_WIDTH_RT);
		ObiwarpHelper obiHelper = new ObiwarpHelper(mzBinWidth, mzRange, timeBinWidth, timeRange);
		File refFile = obiHelper.buildLmataFile(refContainer);
		refFile.deleteOnExit();

		parameterMap.put(Parameter.REFERENCE_FILE, refFile);
		parameterMap.put(Parameter.BIN_WIDTH_MZ, mzBinWidth);
		parameterMap.put(Parameter.BIN_WIDTH_RT, timeBinWidth);
		parameterMap.put(Parameter.MZ_RANGE, mzRange);
		parameterMap.put(Parameter.TIME_RANGE, timeRange);

		parameterMap.put(Parameter.EXECUTABLE, settings.getTextOption(Parameter.EXECUTABLE));
		parameterMap.put(Parameter.GAP_INIT, settings.getDoubleOption(Parameter.GAP_INIT));
		parameterMap.put(Parameter.GAP_EXTEND, settings.getDoubleOption(Parameter.GAP_EXTEND));
		parameterMap.put(Parameter.RESPONSE, settings.getDoubleOption(Parameter.RESPONSE));

		return getDataTableSpec(data, Parameter.PEAK_COLUMN, Parameter.PEAK_COLUMN, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.EXECUTABLE, "" + Parameter.EXECUTABLE.getDefaultValue());
			settings.setTextOption(Parameter.GAP_INIT, "" + Parameter.GAP_INIT.getDefaultValue());
			settings.setTextOption(Parameter.GAP_EXTEND, "" + Parameter.GAP_EXTEND.getDefaultValue());
			settings.setTextOption(Parameter.RESPONSE, "" + Parameter.RESPONSE.getDefaultValue());
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.TIME_WINDOW, "" + Parameter.TIME_WINDOW.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[1], settings, Parameter.PEAK_COLUMN);
		return NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.PEAK_COLUMN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);

		String exe = tmpSettings.getTextOption(Parameter.EXECUTABLE);
		File exeFile = new File(exe);
		if (!exeFile.exists() || !exeFile.isFile())
			throw new InvalidSettingsException("Executable not found.");
	}
}
