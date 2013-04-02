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
package uk.ac.ebi.masscascade.knime.fragmenter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.openscience.cdk.knime.type.CDKValue;

/**
 * <code>NodeDialog</code> for the "MzFragmenter" Node.
 * 
 * @author Stephan Beisken
 */
public class MzFragmenterNodeDialog extends NodeDialogPane {

	private final JTextField inTreeDepth = new JTextField(3);
	private final JTextField inNoThreads = new JTextField(3);
	private final JTextField inMassThreshold = new JTextField(5);
	private final JTextField inPeakIntensity = new JTextField(5);

	private final JCheckBox checkBreakAromaticRings = new JCheckBox();
	private final JCheckBox checkFormulaRedundancy = new JCheckBox();
	private final JCheckBox checkCalculateEnergies = new JCheckBox();
	private final JCheckBox checkBreakLikelyBonds = new JCheckBox();

	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox selColumn = new ColumnSelectionComboxBox((Border) null, CDKValue.class);

	private final MzFragmenterSettings fragSettings = new MzFragmenterSettings();

	/**
	 * Pane for configuring MzFragmenter node dialog.
	 */
	protected MzFragmenterNodeDialog() {

		JPanel mainPanel = new JPanel(new GridLayout(2, 1));

		JPanel subPanelUpper = new JPanel(new GridBagLayout());
		subPanelUpper.setBorder(new TitledBorder("Algorithm Parameters"));

		GridBagConstraints subPanelUC = new GridBagConstraints();
		subPanelUC.gridx = 0;
		subPanelUC.gridy = 0;
		subPanelUC.anchor = GridBagConstraints.NORTHWEST;

		subPanelUpper.add(new JLabel("Tree depth  "), subPanelUC);
		subPanelUC.gridx++;
		subPanelUpper.add(inTreeDepth, subPanelUC);
		subPanelUC.gridy++;

		subPanelUC.gridx = 0;
		subPanelUpper.add(new JLabel("No. of threads  "), subPanelUC);
		subPanelUC.gridx++;
		subPanelUpper.add(inNoThreads, subPanelUC);
		subPanelUC.gridy++;

		subPanelUC.gridx = 0;
		subPanelUpper.add(new JLabel("Lower mass threshold  "), subPanelUC);
		subPanelUC.gridx++;
		subPanelUpper.add(inMassThreshold, subPanelUC);
		subPanelUC.gridy++;

		subPanelUC.gridx = 0;
		subPanelUpper.add(new JLabel("Peak intensity  "), subPanelUC);
		subPanelUC.gridx++;
		subPanelUpper.add(inPeakIntensity, subPanelUC);
		subPanelUC.gridy++;

		subPanelUC.gridx--;
		subPanelUpper.add(new JLabel("Column with molecules  "), subPanelUC);
		subPanelUC.gridx++;
		subPanelUpper.add(selColumn, subPanelUC);
		subPanelUC.gridy++;

		JPanel subPanelLower = new JPanel(new GridBagLayout());
		subPanelLower.setBorder(new TitledBorder("Fragmentation Parameters  "));

		GridBagConstraints subPanelLC = new GridBagConstraints();
		subPanelLC.gridx = 0;
		subPanelLC.gridy = 0;
		subPanelLC.anchor = GridBagConstraints.NORTHWEST;

		subPanelLower.add(new JLabel("Calculate bond energies  "), subPanelLC);
		subPanelLC.gridx++;
		subPanelLower.add(checkCalculateEnergies, subPanelLC);
		subPanelLC.gridy++;

		subPanelLC.gridx = 0;
		subPanelLower.add(new JLabel("Break aromatic rings  "), subPanelLC);
		subPanelLC.gridx++;
		subPanelLower.add(checkBreakAromaticRings, subPanelLC);
		subPanelLC.gridy++;

		subPanelLC.gridx = 0;
		subPanelLower.add(new JLabel("Break only likely bonds  "), subPanelLC);
		subPanelLC.gridx++;
		subPanelLower.add(checkBreakLikelyBonds, subPanelLC);
		subPanelLC.gridy++;

		subPanelLC.gridx = 0;
		subPanelLower.add(new JLabel("Check formula-based redundancy  "), subPanelLC);
		subPanelLC.gridx++;
		subPanelLower.add(checkFormulaRedundancy, subPanelLC);
		subPanelLC.gridy++;

		mainPanel.add(subPanelUpper);
		mainPanel.add(subPanelLower);

		addTab("Settings", mainPanel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {

		try {
			fragSettings.loadSettings(settings);
		} catch (InvalidSettingsException ex) {
			// ignore it
		}

		inTreeDepth.setText("" + fragSettings.getTreeDepth());
		inNoThreads.setText("" + fragSettings.getNoThreads());
		inMassThreshold.setText("" + fragSettings.getMassThreshold());
		inPeakIntensity.setText("" + fragSettings.getPeakIntensity());

		selColumn.update(specs[0], fragSettings.getMolColumnName());

		checkCalculateEnergies.setSelected(fragSettings.isCalculateEnergies());
		checkBreakAromaticRings.setSelected(fragSettings.isBreakAromaticRings());
		checkBreakLikelyBonds.setSelected(fragSettings.isBreakLikelyBonds());
		checkFormulaRedundancy.setSelected(fragSettings.isFormulaRedundancy());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		try {
			fragSettings.setTreeDepth(Integer.parseInt(inTreeDepth.getText()));
			fragSettings.setNoThreads(Integer.parseInt(inNoThreads.getText()));
			fragSettings.setMassThreshold(Double.parseDouble(inMassThreshold.getText()));
			fragSettings.setPeakIntensity(Double.parseDouble(inPeakIntensity.getText()));

			fragSettings.setMolColumnName(selColumn.getSelectedColumn());

			fragSettings.setCalculateEnergies(checkCalculateEnergies.isSelected());
			fragSettings.setBreakAromaticRings(checkBreakAromaticRings.isSelected());
			fragSettings.setBreakLikelyBonds(checkBreakLikelyBonds.isSelected());
			fragSettings.setFormulaRedundancy(checkFormulaRedundancy.isSelected());
		} catch (Exception exception) {
			// ignore it
		}

		fragSettings.saveSettings(settings);
	}
}
