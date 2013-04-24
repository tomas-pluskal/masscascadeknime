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
package uk.ac.ebi.masscascade.knime.database.massbank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for the instruments property table.
 * 
 * @author Stephan Beisken
 */
public class InstrumentTableModel extends AbstractTableModel {

	private final List<Boolean> extract = new ArrayList<Boolean>();
	private final List<String> instruments = new ArrayList<String>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getColumnName(final int column) {

		switch (column) {
		case 0:
			return "Select";
		case 1:
			return "Instrument";
		default:
			return "???";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getColumnClass(final int columnIndex) {

		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		default:
			return Object.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {

		switch (columnIndex) {
		case 0:
			return true;
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRowCount() {
		return instruments.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {

		switch (columnIndex) {
		case 0:
			return extract.get(rowIndex);
		case 1:
			return instruments.get(rowIndex);
		default:
			return "???";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {

		switch (columnIndex) {
		case 0:
			extract.set(rowIndex, (Boolean) value);
			break;
		}
	}

	/**
	 * Updates the table model with the found instruments.
	 * 
	 * @param groups a map of instruments with the instruments' names as keys and their boolean as values.
	 */
	public void update(final Map<String, Boolean> groups) {

		extract.clear();
		instruments.clear();

		synchronized (groups) {
			for (Map.Entry<String, Boolean> e : groups.entrySet()) {
				instruments.add(e.getKey());
				extract.add(e.getValue());
			}
		}

		fireTableDataChanged();
	}

	/**
	 * Returns the selected instruments shown in the table.
	 * 
	 * @return a list of the selected instruments
	 */
	public List<String> getSelected() {

		List<String> selected = new ArrayList<String>();
		for (int i = 0; i < instruments.size(); i++) {
			if (extract.get(i))
				selected.add(instruments.get(i));
		}
		return selected;
	}
}
