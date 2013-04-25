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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Iterables;

/**
 * Table model for a two column boolean table.
 * 
 * @author Stephan Beisken
 */
public class BooleanTableModel extends AbstractTableModel {

	private final Map<String, Boolean> objects = new LinkedHashMap<String, Boolean>();

	/**
	 * Constructs a boolean table model with the given set of objects.
	 * 
	 * @param objects the objects and their state
	 */
	public BooleanTableModel(Map<String, Boolean> objects) {
		this.objects.putAll(objects);
	}

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
		return objects.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {

		switch (columnIndex) {
		case 0:
			return Iterables.get(objects.values(), rowIndex);
		case 1:
			return Iterables.get(objects.keySet(), rowIndex);
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
			objects.put(Iterables.get(objects.keySet(), rowIndex), (Boolean) value);
			break;
		}
	}

	/**
	 * Updates the table model with the found instruments.
	 * 
	 * @param selected the list of selected objects
	 */
	public void update(final List<String> selected) {

		if (selected.isEmpty()) return;
		
		Iterator<Entry<String, Boolean>> iter = objects.entrySet().iterator();
		while (iter.hasNext())
			iter.next().setValue(false);

		for (String select : selected)
			objects.put(select, true);

		fireTableDataChanged();
	}

	/**
	 * Returns the selected objects shown in the table.
	 * 
	 * @return a list of the selected instruments
	 */
	public List<String> getSelected() {

		List<String> selected = new ArrayList<String>();
		for (Map.Entry<String, Boolean> entry : objects.entrySet())
			if (entry.getValue())
				selected.add(entry.getKey());

		return selected;
	}
}
