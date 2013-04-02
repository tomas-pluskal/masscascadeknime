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
package uk.ac.ebi.masscascade.knime.visualization.profiletable.profileinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import uk.ac.ebi.masscascade.interfaces.Property;
import uk.ac.ebi.masscascade.properties.Adduct;

/**
 * Class implementing a table model for the adduct table.
 * 
 * @author Stephan Beisken
 */
public class AdductTableModel extends AbstractTableModel {

	private final String[] headers = new String[] { "Id", "Parent", "Child", "Remove" };
	private List<Property> adducts = new ArrayList<Property>();
	private Map<Integer, Boolean> remMap = new HashMap<Integer, Boolean>();

	/**
	 * Sets the data list.
	 * 
	 * @param adducts the set of adduct properties
	 */
	public void setDataList(Set<Property> adducts) {

		if (adducts != null && adducts.size() > 0)
			this.adducts = new ArrayList<Property>(adducts);
	}

	/**
	 * Sets the data list.
	 * 
	 * @param adducts the list of adduct properties
	 */
	public void setDataList(List<Property> adducts) {

		if (adducts != null && adducts.size() > 0)
			this.adducts = adducts;
	}

	@Override
	public void setValueAt(Object aValue, int row, int col) {

		if (col == 3) {
			remMap.put(row, (Boolean) aValue);
			this.fireTableCellUpdated(row, col);
		}
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
		return adducts.size();
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

		Object value;

		switch (col) {
		case 0:
			value = adducts.get(row).getName();
			break;
		case 1:
			value = ((Adduct) adducts.get(row)).getParentId();
			break;
		case 2:
			value = ((Adduct) adducts.get(row)).getChildId();
			break;
		case 3:
			value = remMap.containsKey(row) ? remMap.get(row) : false;
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
			return String.class;
		case 1:
			return Integer.class;
		case 2:
			return Integer.class;
		case 3:
			return Boolean.class;
		default:
			return String.class;
		}
	}
}
