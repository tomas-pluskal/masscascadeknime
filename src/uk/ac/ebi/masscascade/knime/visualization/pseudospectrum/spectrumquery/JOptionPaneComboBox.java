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
package uk.ac.ebi.masscascade.knime.visualization.pseudospectrum.spectrumquery;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import uk.ac.ebi.masscascade.knime.visualization.profiletable.profilequery.RequestFocusListener;

public class JOptionPaneComboBox {

	private int index;
	private int status;

	/**
	 * Constructs the JOptionPane.
	 */
	public JOptionPaneComboBox(String[] comboBoxStrings) {

		final JComboBox<String> spectrumBox = new JComboBox<String>(comboBoxStrings);
		spectrumBox.addAncestorListener(new RequestFocusListener());

		JPanel inputPanel = new JPanel();
		inputPanel.add(new JLabel("Select Spetrum:"));
		inputPanel.add(spectrumBox);

		status = JOptionPane.showConfirmDialog(null, inputPanel, "Multiple hits, please select.",
				JOptionPane.OK_CANCEL_OPTION);

		if (status == JOptionPane.OK_OPTION) {

			try {
				index = spectrumBox.getSelectedIndex();
			} catch (Exception exception) {
				status = JOptionPane.CANCEL_OPTION;
			}
		}
	}

	/**
	 * Returns the return status of the pane.
	 * 
	 * @return OK_OPTION for valid values, CANCEL_OPTION for invalid values
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Returns the index of the selected value.
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
}
