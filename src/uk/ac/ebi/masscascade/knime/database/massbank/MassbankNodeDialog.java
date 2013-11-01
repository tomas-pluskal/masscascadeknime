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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;

import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.parameters.Constants;

/**
 * <code>NodeDialog</code> for the "MassBank" Node.
 * 
 * @author Stephan Beisken
 */
public class MassbankNodeDialog extends NodeDialogPane {

	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox spectrumColumn = new ColumnSelectionComboxBox((Border) null,
			FeatureSetValue.class);

	private JTextField minProfiles = new JTextField(6);
	private JTextField score = new JTextField(6);
	private JTextField maxNumOfResults = new JTextField(6);
	private JTextField msnLevel = new JTextField(6);
	private JTextField ppm = new JTextField(6);
	private JRadioButton positiveButton = new JRadioButton("positive");
	private JRadioButton negativeButton = new JRadioButton("negative");

	private JButton selectButton = new JButton("(De)Select All");

	private InstrumentTableModel instrumentModel = new InstrumentTableModel();
	private JTable instrumentTable = new JTable(instrumentModel);

	private MassbankSettings settings = new MassbankSettings();

	/**
	 * New pane for configuring the MassBank node.
	 */
	protected MassbankNodeDialog() {

		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Query Settings"));

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 10, 0, 3);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;

		panel.add(new JLabel("Spectrum column"), c);
		c.gridx = 1;
		panel.add(spectrumColumn, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("m/z window [ppm]"), c);
		c.gridx++;
		panel.add(ppm, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Min. number of profiles"), c);
		c.gridx++;
		panel.add(minProfiles, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Query score (0-1)"), c);
		c.gridx = 1;
		panel.add(score, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Max results"), c);
		c.gridx = 1;
		panel.add(maxNumOfResults, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("MSn level"), c);
		c.gridx = 1;
		panel.add(msnLevel, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Mode"), c);
		c.gridx = 1;
		panel.add(positiveButton, c);
		c.gridy++;
		panel.add(negativeButton, c);
		c.gridy++;
		c.gridx = 0;

		c.gridwidth = 4;
		c.insets = new Insets(0, 0, 0, 0);

		JScrollPane sp = new JScrollPane(instrumentTable);
		instrumentTable.setFillsViewportHeight(true);

		ButtonGroup bg = new ButtonGroup();
		bg.add(positiveButton);
		bg.add(negativeButton);

		panel.add(sp, c);
		c.gridy++;
		panel.add(selectButton, c);
		selectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean value = !(Boolean) instrumentModel.getValueAt(0, 0);

				for (int i = 0; i < instrumentModel.getRowCount(); i++) {
					instrumentModel.setValueAt(value, i, 0);
				}
				instrumentModel.fireTableDataChanged();
			}
		});

		this.addTab("Settings", panel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {

		this.settings.loadSettingsForDialog(settings);

		spectrumColumn.update(specs[0], this.settings.getSpectrumColumn());

		score.setText("" + this.settings.getScore());
		minProfiles.setText("" + this.settings.getMinNumOfProfiles());
		maxNumOfResults.setText("" + this.settings.getMaxNumOfResults());
		msnLevel.setText("" + this.settings.getMSnLevel());
		ppm.setText("" + this.settings.getPpm());

		if (this.settings.getIonMode() == Constants.ION_MODE.POSITIVE) {
			positiveButton.setSelected(true);
		} else if (this.settings.getIonMode() == Constants.ION_MODE.NEGATIVE) {
			negativeButton.setSelected(true);
		}

		Map<String, Boolean> instrumentMap = new HashMap<String, Boolean>();
		for (String instrument : MassbankSettings.ALL_INST)
			instrumentMap.put(instrument, false);
		for (String instrument : this.settings.getInstruments())
			instrumentMap.put(instrument, true);
		instrumentModel.update(instrumentMap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		this.settings.setSpectrumColumn(spectrumColumn.getSelectedColumn());

		this.settings.setScore(Double.parseDouble(score.getText()));
		this.settings.setMinNumOfProfiles(Integer.parseInt(minProfiles.getText()));
		this.settings.setMaxNumOfResults(Integer.parseInt(maxNumOfResults.getText()));
		this.settings.setMSnLevel(Integer.parseInt(msnLevel.getText()));
		this.settings.setPpm(Double.parseDouble(ppm.getText()));

		if (positiveButton.isSelected()) {
			this.settings.setIonMode(Constants.ION_MODE.POSITIVE);
		} else if (negativeButton.isSelected()) {
			this.settings.setIonMode(Constants.ION_MODE.NEGATIVE);
		}

		this.settings.setInstruments(instrumentModel.getSelected().toArray(new String[] {}));

		this.settings.saveSettings(settings);
	}
}
