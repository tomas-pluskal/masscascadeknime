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
package uk.ac.ebi.masscascade.knime.database.massbank;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.parameters.Constants;

/**
 * Settings for the Massbank Webservice node.
 * 
 * @author Stephan Beisken
 */
public class MassbankSettings {

	private static final String[] DEF_INST = new String[] { "CE-ESI-TOF", "EI-EBEB", "ESI-IT-MS/MS", "ESI-QqTOF-MS/MS",
			"LC-ESI-IT", "LC-ESI-ITFT", "LC-ESI-ITTOF", "LC-ESI-Q", "LC-ESI-QIT", "LC-ESI-QQ", "LC-ESI-QTOF" };

	public static final String[] ALL_INST = new String[] { "CE-ESI-TOF", "CI-B", "EI-B", "EI-EBEB", "ESI-IT-MS/MS",
			"ESI-QqIT-MS/MS", "ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "FAB-B", "FAB-EB", "FAB-EBEB", "FD-B", "FI-B",
			"GC-EI-TOF", "LC-APPI-QQ", "LC-ESI-IT", "LC-ESI-ITFT", "LC-ESI-ITTOF", "LC-ESI-Q", "LC-ESI-QIT",
			"LC-ESI-QQ", "LC-ESI-QTOF", "MALDI-TOF", "MALDI-TOFTOF" };

	private String spectrumColumn;
	private int cutoff = 50;
	private double tolerance = 10d;
	private Constants.ION_MODE ionMode = Constants.ION_MODE.POSITIVE;
	private int maxNumOfResults = 50;
	private String[] instruments = DEF_INST;

	/**
	 * @return the spectrumColumn
	 */
	public final String getSpectrumColumn() {

		return spectrumColumn;
	}

	/**
	 * @param spectrumColumn the spectrumColumn to set
	 */
	public final void setSpectrumColumn(String spectrumColumn) {

		this.spectrumColumn = spectrumColumn;
	}

	/**
	 * @return the tolerance
	 */
	public final double getTolerance() {

		return tolerance;
	}

	/**
	 * @param tolerance the tolerance to set
	 */
	public final void setTolerance(double tolerance) {

		this.tolerance = tolerance;
	}

	/**
	 * @return the ionMode
	 */
	public final Constants.ION_MODE getIonMode() {

		return ionMode;
	}

	/**
	 * @param ionMode the ionMode to set
	 */
	public final void setIonMode(Constants.ION_MODE ionMode) {

		this.ionMode = ionMode;
	}

	/**
	 * @return the maxNumOfResults
	 */
	public final int getMaxNumOfResults() {

		return maxNumOfResults;
	}

	/**
	 * @param maxNumOfResults the maxNumOfResults to set
	 */
	public final void setMaxNumOfResults(int maxNumOfResults) {

		this.maxNumOfResults = maxNumOfResults;
	}

	/**
	 * @return the instruments
	 */
	public final String[] getInstruments() {

		return instruments;
	}

	/**
	 * @param instruments the instruments to set
	 */
	public final void setInstruments(String[] instruments) {

		this.instruments = instruments;
	}

	/**
	 * @return the cutoff
	 */
	public final int getCutoff() {

		return cutoff;
	}

	/**
	 * @param cutoff the cutoff to set
	 */
	public final void setCutoff(int cutoff) {

		this.cutoff = cutoff;
	}

	/**
	 * Saves the settings into the given node settings object.
	 * 
	 * @param settings a node settings object
	 */
	public void saveSettings(final NodeSettingsWO settings) {

		settings.addString("spectrumColumn", spectrumColumn);

		settings.addDouble("tolerance", tolerance);
		settings.addString("mode", ionMode.name());
		settings.addStringArray("instruments", instruments);
		settings.addInt("results", maxNumOfResults);
		settings.addInt("cutoff", cutoff);
	}

	/**
	 * Loads the settings from the given node settings object.
	 * 
	 * @param settings node settings
	 */
	public void loadSettingsForDialog(final NodeSettingsRO settings) {

		spectrumColumn = settings.getString("spectrumColumn", null);

		tolerance = settings.getDouble("tolerance", 10d);
		ionMode = Constants.ION_MODE.valueOf(settings.getString("mode", Constants.ION_MODE.POSITIVE.name()));
		instruments = settings.getStringArray("instruments", DEF_INST);
		maxNumOfResults = settings.getInt("results", 50);
		cutoff = settings.getInt("cutoff", 50);
	}

	/**
	 * Loads the settings from the given node settings object.
	 * 
	 * @param settings a node settings object
	 * @throws InvalidSettingsException if not all required settings are available
	 */
	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		spectrumColumn = settings.getString("spectrumColumn");

		tolerance = settings.getDouble("tolerance");
		ionMode = Constants.ION_MODE.valueOf(settings.getString("mode"));
		instruments = settings.getStringArray("instruments");
		maxNumOfResults = settings.getInt("results");
		cutoff = settings.getInt("cutoff");
	}
}
