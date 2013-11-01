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
package uk.ac.ebi.masscascade.knime.visualization.featuretable.featureinfo;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

import uk.ac.ebi.masscascade.properties.Identity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Class implementing a table model for the fragment table.
 * 
 * @author Stephan Beisken
 */
public class FragmentTableModel extends AbstractTableModel {

	private Multimap<Double, Identity> massToidentity = HashMultimap.create();
	private final String[] headers = new String[] { "m/z", "Notation", "Evidence" };

	/**
	 * Sets the list of identity properties.
	 * 
	 * @param identities the list of properties
	 */
	public void setDataList(Multimap<Double, Identity> massToidentity) {

		if (massToidentity != null && massToidentity.size() > 0)
			this.massToidentity = massToidentity;
	}

	/**
	 * Returns the number of column headers.
	 */
	public int getColumnCount() {
		return headers.length;
	}

	/**
	 * Returns the number of rows.
	 */
	public int getRowCount() {
		return massToidentity.values().size();
	}

	/**
	 * Returns the name of column x.
	 * 
	 * @param col a column index
	 */
	public String getColumnName(int col) {
		return headers[col];
	}

	/**
	 * Returns the specified row value.
	 * 
	 * @param row a row index
	 * @param col a col index
	 */
	public Object getValueAt(int row, int col) {

		Map.Entry<Double, Identity> tmpEntry = null;
		int i = 0;
		for (Map.Entry<Double, Identity> entry : massToidentity.entries()) {
			if (i++ == row) {
				tmpEntry = entry;
				break;
			}
		}

		Object value;

		switch (col) {
		case 0:
			value = tmpEntry.getKey();
			break;
		case 1:
			value = tmpEntry.getValue().getNotation();
			break;
		case 2:
			value = tmpEntry.getValue().getEvidence();
			break;
		default:
			value = false;
			break;
		}

		return value;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getColumnClass(int c) {

		switch (c) {
		case 0:
			return Double.class;
		default:
			return String.class;
		}
	}

	/**
	 * Returns the line notation. Either SMILES or InChI.
	 * 
	 * @param row the index
	 * @return the line notation
	 */
	public String getNotationAt(int row) {
		int i = 0;
		for (Identity identity : massToidentity.values()) {
			if (i++ == row)
				return identity.getNotation();
		}
		return "";
	}
}
