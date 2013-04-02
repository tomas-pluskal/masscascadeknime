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
package uk.ac.ebi.masscascade.knime.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.ebi.masscascade.knime.NodePlugin;

/**
 * Class to initialise the feature preferences page.
 * 
 * @author Stephan Beisken
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Preference key for the "project directory" setting.
	 */
	public static final String PROJECT_DIRECTORY = "knime.spectrometry.directory";
	
	/**
	 * Preference key for the "number of threads" setting.
	 */
	public static final String THREADS = "knime.spectrometry.threads";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		// get the preference store for the UI plugin
		IPreferenceStore store = NodePlugin.getDefault().getPreferenceStore();

		String tmpDir = System.getProperty("java.io.tmpdir");
		
		// set default values
		store.setDefault(PROJECT_DIRECTORY, tmpDir);
		store.setDefault(THREADS, 5);
	}
}
