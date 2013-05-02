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
	private int minNumOfProfiles = 3;
	private double score = 0.8;
	private Constants.ION_MODE ionMode = Constants.ION_MODE.POSITIVE;
	private int maxNumOfResults = 50;
	private String[] instruments = DEF_INST;
	private int msnLevel = 2;
	private double ppm;

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
	 * @return the score
	 */
	public final double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public final void setScore(double score) {
		this.score = score;
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
	 * @return the minimum no. of profiles
	 */
	public final int getMinNumOfProfiles() {
		return minNumOfProfiles;
	}

	/**
	 * @param minNumOfProfiles the minimum no. of profiles
	 */
	public final void setMinNumOfProfiles(int minNumOfProfiles) {
		this.minNumOfProfiles = minNumOfProfiles;
	}

	/**
	 * @param msnLevel the MSn level to query
	 */
	public final void setMSnLevel(int msnLevel) {
		this.msnLevel = msnLevel;
	}

	/**
	 * @return the MSn level to query
	 */
	public final int getMSnLevel() {
		return msnLevel;
	}
	
	/**
	 * @return the ppm
	 */
	public final double getPpm() {
		return ppm;
	}

	
	/**
	 * @param ppm the ppm to set
	 */
	public final void setPpm(double ppm) {
		this.ppm = ppm;
	}

	/**
	 * Saves the settings into the given node settings object.
	 * 
	 * @param settings a node settings object
	 */
	public void saveSettings(final NodeSettingsWO settings) {

		settings.addString("spectrumColumn", spectrumColumn);

		settings.addDouble("score", score);
		settings.addString("mode", ionMode.name());
		settings.addStringArray("instruments", instruments);
		settings.addInt("results", maxNumOfResults);
		settings.addInt("minProfiles", minNumOfProfiles);
		settings.addInt("msnLevel", msnLevel);
		settings.addDouble("ppm", ppm);
	}

	/**
	 * Loads the settings from the given node settings object.
	 * 
	 * @param settings node settings
	 */
	public void loadSettingsForDialog(final NodeSettingsRO settings) {

		spectrumColumn = settings.getString("spectrumColumn", null);

		score = settings.getDouble("score", 0.9);
		ionMode = Constants.ION_MODE.valueOf(settings.getString("mode", Constants.ION_MODE.POSITIVE.name()));
		instruments = settings.getStringArray("instruments", DEF_INST);
		maxNumOfResults = settings.getInt("results", 50);
		minNumOfProfiles = settings.getInt("minProfiles", 3);
		msnLevel = settings.getInt("msnLevel", 2);
		ppm = settings.getDouble("ppm", 10.0);
	}

	/**
	 * Loads the settings from the given node settings object.
	 * 
	 * @param settings a node settings object
	 * @throws InvalidSettingsException if not all required settings are available
	 */
	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		spectrumColumn = settings.getString("spectrumColumn");

		score = settings.getDouble("score");
		ionMode = Constants.ION_MODE.valueOf(settings.getString("mode"));
		instruments = settings.getStringArray("instruments");
		maxNumOfResults = settings.getInt("results");
		minNumOfProfiles = settings.getInt("minProfiles");
		msnLevel = settings.getInt("msnLevel");
		ppm = settings.getDouble("ppm");
	}
}
