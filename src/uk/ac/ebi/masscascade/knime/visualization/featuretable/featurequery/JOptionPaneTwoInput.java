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
package uk.ac.ebi.masscascade.knime.visualization.featuretable.featurequery;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Custom JOptionPane to display two input fields for mz and ppm values.
 * 
 * @author Stephan Beisken
 */
public class JOptionPaneTwoInput {

	private double mz;
	private double ppm;

	private int status;

	/**
	 * Constructs the JOptionPane.
	 */
	public JOptionPaneTwoInput() {

		final JTextField massField = new JTextField(7);
		massField.addAncestorListener(new RequestFocusListener());

		final JTextField ppmField = new JTextField(7);

		JPanel inputPanel = new JPanel();
		inputPanel.add(new JLabel("m/z:"));
		inputPanel.add(massField);
		inputPanel.add(Box.createHorizontalStrut(10));
		inputPanel.add(new JLabel("ppm:"));
		inputPanel.add(ppmField);

		status = JOptionPane.showConfirmDialog(null, inputPanel, "Please enter mz and ppm values.",
				JOptionPane.OK_CANCEL_OPTION);

		if (status == JOptionPane.OK_OPTION) {

			try {
				mz = Double.parseDouble(massField.getText());
				ppm = Double.parseDouble(ppmField.getText());
			} catch (Exception exception) {
				status = JOptionPane.CANCEL_OPTION;
				JOptionPane.showMessageDialog(inputPanel, "The values entered are not valid.", "Error",
						JOptionPane.ERROR_MESSAGE);
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
	 * Returns the mz value.
	 * 
	 * @return the mz value
	 */
	public double getMz() {

		return mz;
	}

	/**
	 * Returns the ppm value.
	 * 
	 * @return the ppm value
	 */
	public double getPpm() {

		return ppm;
	}
}
