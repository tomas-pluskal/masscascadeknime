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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;

/**
 * <code>NodeDialog</code> for the "Metlin" Node.
 * 
 * @author Stephan Beisken
 */
public class MetlinNodeDialog extends NodeDialogPane {

	private final JTextField mzTol = new JTextField(5);
	private final JTextField noRes = new JTextField(5);

	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox selMzCol = new ColumnSelectionComboxBox((Border) null, DoubleValue.class);

	MetlinNodeSettings searchSettings = new MetlinNodeSettings();

	protected MetlinNodeDialog() {

		JPanel panel = new JPanel(new GridLayout(2, 1));
		JPanel i = new JPanel(new GridBagLayout());
		i.setBorder(BorderFactory.createTitledBorder("Source"));
		JPanel j = new JPanel(new GridBagLayout());
		j.setBorder(BorderFactory.createTitledBorder("Parameters"));

		GridBagConstraints panelC = new GridBagConstraints();
		panelC.gridx = 0;
		panelC.gridy = 0;
		panelC.anchor = GridBagConstraints.NORTHWEST;

		j.add(new JLabel("mz tolerance   "), panelC);
		panelC.gridx++;
		j.add(mzTol, panelC);
		panelC.gridy++;

		panelC.gridx = 0;
		j.add(new JLabel("no of results   "), panelC);
		panelC.gridx++;
		j.add(noRes, panelC);
		panelC.gridy++;

		panelC.gridx = 0;
		i.add(new JLabel("mz column   "), panelC);
		panelC.gridx++;
		i.add(selMzCol, panelC);

		panel.add(i);
		panel.add(j);
		addTab("Search Settings", panel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {

		try {
			searchSettings.loadSettings(settings);
		} catch (InvalidSettingsException ex) {
			// ignore it
		}

		selMzCol.update(specs[0], searchSettings.getMzColName());

		mzTol.setText("" + searchSettings.getMzTolerance());
		noRes.setText("" + searchSettings.getNoResults());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		try {
			searchSettings.setMzColName(selMzCol.getSelectedColumn());
			searchSettings.setMzTolerance(Double.parseDouble(mzTol.getText()));
			searchSettings.setNoResults(Integer.parseInt(noRes.getText()));
		} catch (Exception exception) {
			// ignore it
		}

		searchSettings.saveSettings(settings);
	}
}
