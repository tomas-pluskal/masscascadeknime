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
package uk.ac.ebi.masscascade.knime.io.read;

import java.io.File;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class MsFileReaderSettings {

	private File[] files;
	private String filePath;
	private boolean retainData;

	/**
	 * Returns the file.
	 * 
	 * @return the file
	 */
	public File[] files() {

		return files;
	}

	/**
	 * Sets the file.
	 * 
	 * @param name the file
	 */
	public void files(final File[] files) {

		this.files = files;
	}

	/**
	 * Returns the file path.
	 * 
	 * @return the file path
	 */
	public String filePath() {

		return filePath;
	}

	/**
	 * Sets the file path.
	 * 
	 * @param path the file path
	 */
	public void filePath(final String path) {

		this.filePath = path;
	}

	/**
	 * Returns if data should be retained after reset.
	 * 
	 * @return boolean
	 */
	public boolean retainData() {

		return retainData;
	}

	/**
	 * If data should be retained after reset.
	 * 
	 * @param retainData boolean
	 */
	public void retainData(boolean retainData) {

		this.retainData = retainData;
	}

	/**
	 * Saves all settings into the given node settings object.
	 * 
	 * @param settings the node settings
	 */
	public void saveSettings(final NodeSettingsWO settings) {

		settings.addStringArray("files", getFileStrings());
		settings.addString("path", filePath);
		settings.addBoolean("retain", retainData);
	}

	/**
	 * Loads all settings from the given node settings object.
	 * 
	 * @param settings the node settings
	 * @throws InvalidSettingsException if a setting is missing
	 */
	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		getFiles(settings.getStringArray("files"));
		filePath = settings.getString("path");
		retainData = settings.getBoolean("retain");
	}

	private String[] getFileStrings() {

		int i = 0;
		String[] names = new String[files.length];
		for (File file : files) {
			names[i] = file.getAbsolutePath();
			i++;
		}
		return names;
	}

	private void getFiles(String[] names) {

		files = new File[names.length];
		int i = 0;
		for (String name : names) {
			files[i] = new File(name);
			i++;
		}
	}
}
