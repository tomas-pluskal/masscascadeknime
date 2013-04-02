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
package uk.ac.ebi.masscascade.knime;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.ebi.masscascade.knime.preferences.PreferenceInitializer;

/**
 * This is the eclipse bundle activator.
 * 
 * @author KNIME Team
 */
public class NodePlugin extends AbstractUIPlugin {
	// The shared instance.
	private static NodePlugin plugin;

	private static String projectDirectory = "";
	private static int threads = 5;

	/**
	 * The constructor.
	 */
	public NodePlugin() {

		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context The OSGI bundle context
	 * @throws Exception If this plugin could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {

		super.start(context);

		final IPreferenceStore pStore = getDefault().getPreferenceStore();
		pStore.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {

				if (event.getProperty().equals(PreferenceInitializer.PROJECT_DIRECTORY)) {
					projectDirectory = pStore.getString(PreferenceInitializer.PROJECT_DIRECTORY);
					threads = pStore.getInt(PreferenceInitializer.THREADS);
				}
			}
		});
		projectDirectory = pStore.getString(PreferenceInitializer.PROJECT_DIRECTORY);
		threads = pStore.getInt(PreferenceInitializer.THREADS);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context The OSGI bundle context
	 * @throws Exception If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {

		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static NodePlugin getDefault() {

		return plugin;
	}

	/**
	 * Returns the project directory.
	 * 
	 * @return the project directory
	 */
	public static String getProjectDirectory() {

		return projectDirectory;
	}
	
	/**
	 * Returns the number of threads.
	 * 
	 * @return the number of threads
	 */
	public static int getNumberOfThreads() {
		
		return threads;
	}
}
