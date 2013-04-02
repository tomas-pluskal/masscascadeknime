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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.ebi.masscascade.knime.NodePlugin;

/**
 * Class to create a Eclipse preferences page.
 * 
 * @author Stephan Beisken
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Creates a new preference page.
	 */
	public PreferencePage() {

		super(GRID);

		// we use the pref store of the UI plugin
		setPreferenceStore(NodePlugin.getDefault().getPreferenceStore());
		setDescription("MassCascade Preferences");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFieldEditors() {

		Composite parent = getFieldEditorParent();
		DirectoryFieldEditor projectDirectory = new DirectoryFieldEditor(
				PreferenceInitializer.PROJECT_DIRECTORY, "Project Directory", parent);
		addField(projectDirectory);
		IntegerFieldEditor threadPool = new IntegerFieldEditor(PreferenceInitializer.THREADS,
				"Number of Threads (requires restart)", parent);
		addField(threadPool);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final IWorkbench workbench) {

		// nothing to do
	}
}
