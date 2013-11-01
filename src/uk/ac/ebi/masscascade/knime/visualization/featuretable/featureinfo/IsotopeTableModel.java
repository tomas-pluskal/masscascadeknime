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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import uk.ac.ebi.masscascade.interfaces.Property;
import uk.ac.ebi.masscascade.properties.Isotope;

/**
 * Class implementing a table model for the isotope table.
 * 
 * @author Stephan Beisken
 */
public class IsotopeTableModel extends AbstractTableModel {

	private List<Property> isotopes = new ArrayList<Property>();
	private final String[] headers = new String[] { "Id", "Parent", "Child" };

	/**
	 * Sets the isotope property list.
	 * 
	 * @param isotopes the property set
	 */
	public void setDataList(Set<? extends Property> isotopes) {

		if (isotopes != null && isotopes.size() > 0)
			this.isotopes = new ArrayList<Property>(isotopes);
	}

	/**
	 * Sets the isotope property list.
	 * 
	 * @param isotopes the property list
	 */
	public void setDataList(List<Property> isotopes) {

		if (isotopes != null && isotopes.size() > 0)
			this.isotopes = isotopes;
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
		return isotopes.size();
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
			value = isotopes.get(row).getName();
			break;
		case 1:
			value = ((Isotope) isotopes.get(row)).getParentId();
			break;
		case 2:
			value = ((Isotope) isotopes.get(row)).getChildId();
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
		case 1:
			return Integer.class;
		case 2:
			return Integer.class;
		default:
			return String.class;
		}
	}
}
