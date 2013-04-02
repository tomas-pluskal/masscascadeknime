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
package uk.ac.ebi.masscascade.knime.database.metlin;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class MetlinNodeSettings {

	private String mzColName;
	private int noResults;
	private double mzTolerance;

	public String getMzColName() {

		return mzColName;
	}

	public int getNoResults() {

		return noResults;
	}

	public double getMzTolerance() {

		return mzTolerance;
	}

	public void setMzColName(String mzColName) {

		this.mzColName = mzColName;
	}

	public void setNoResults(int noResults) {

		this.noResults = noResults;
	}

	public void setMzTolerance(double mzTolerance) {

		this.mzTolerance = mzTolerance;
	}

	/**
	 * Saves the settings into the given node settings object.
	 * 
	 * @param settings a node settings object
	 */
	public void saveSettings(final NodeSettingsWO settings) {

		settings.addString("mzColName", mzColName);
		settings.addDouble("mzTolerance", mzTolerance);
		settings.addInt("noResults", noResults);
	}

	/**
	 * Loads the settings from the given node settings object.
	 * 
	 * @param settings a node settings object
	 * @throws InvalidSettingsException if not all required settings are available
	 */
	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		mzColName = settings.getString("mzColName");
		mzTolerance = settings.getDouble("mzTolerance");
		noResults = settings.getInt("noResults");
	}
}
