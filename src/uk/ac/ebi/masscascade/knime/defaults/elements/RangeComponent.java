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

package uk.ac.ebi.masscascade.knime.defaults.elements;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;

public class RangeComponent extends JPanel {

	private JTextField fromField;
	private JTextField toField;

	public RangeComponent(int width) {

		fromField = new JTextField(width);
		toField = new JTextField(width);

		init();
	}

	private void init() {

		this.setLayout(new GridLayout(1, 3));
		this.add(fromField);
		this.add(new JLabel("-->", JLabel.CENTER));
		this.add(toField);
	}

	public Range getRange() {

		Range fromToRange = new ExtendableRange();
		try {
			fromToRange = new ExtendableRange(Double.parseDouble(fromField.getText()), Double.parseDouble(toField
					.getText()));
		} catch (Exception exception) {
			// fall through
		}
		return fromToRange;
	}
	
	public void setRange(String fromToRange) {
		
		String[] fromToRangeArray = fromToRange.split("-");
		fromField.setText(fromToRangeArray[0]);
		toField.setText(fromToRangeArray[1]);
	}
}
