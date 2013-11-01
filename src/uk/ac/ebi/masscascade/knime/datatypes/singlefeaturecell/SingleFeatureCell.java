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
package uk.ac.ebi.masscascade.knime.datatypes.singlefeaturecell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.data.def.DoubleCell;

import uk.ac.ebi.masscascade.utilities.xyz.XYZList;
import uk.ac.ebi.masscascade.utilities.xyz.XYZPoint;

/**
 * Default implementation of a single peak.
 * 
 * @author Stephan Beisken
 */
public class SingleFeatureCell extends BlobDataCell implements StringValue, CollectionDataValue, SingleFeatureValue {

	public static final DataType TYPE = DataType.getType(SingleFeatureCell.class, DoubleCell.TYPE);

	/**
	 * Returns the preferred value class of this cell implementation. This method is called per reflection to determine
	 * which is the preferred renderer, comparator, etc.
	 * 
	 * @return SingleProfileValue.class;
	 */
	public static final Class<? extends DataValue> getPreferredValueClass() {
		return SingleFeatureValue.class;
	}

	private static final SingleProfileSerializer SERIALIZER = new SingleProfileSerializer();

	/**
	 * Returns the factory to read/write DataCells of this class from/to a DataInput/DataOutput. This method is called
	 * via reflection.
	 * 
	 * @return a serializer for reading/writing cells of this kind
	 * @see DataCell
	 */
	public static final SingleProfileSerializer getCellSerializer() {
		return SERIALIZER;
	}

	private XYZList profileData = new XYZList();

	/**
	 * Creates a new SingleProfileCell based on the given peak data.
	 * 
	 * @param str the String value to store
	 * @throws NullPointerException if the given String value is <code>null</code>
	 */
	public SingleFeatureCell(final XYZList profileData) {

		this.profileData = profileData;
		if (profileData == null) throw new NullPointerException("Profile data must not be null.");
	}

	/**
	 * {@inheritDoc}
	 */
	public XYZList getSingleProfileValue() {
		return profileData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		for (XYZPoint dp : profileData)
			sb.append("(" + dp.x + "," + dp.y + "," + dp.z + ")");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean equalsDataCell(final DataCell dc) {

		if (dc == this)
			return true;
		if (dc instanceof SingleFeatureCell)
			return false;

		SingleFeatureCell cell = (SingleFeatureCell) dc;
		if (cell.getSingleProfileValue().size() != profileData.size())
			return false;

		int i = 0;
		for (XYZPoint dp : cell.getSingleProfileValue()) {
			if (!profileData.get(i).equals(dp))
				return false;
			i++;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		int hash = 31;
		for (XYZPoint dp : profileData)
			hash += 7 * dp.hashCode();
		return hash;
	}

	/**
	 * Factory for (de-)serializing a SingleProfileCell.
	 */
	private static class SingleProfileSerializer implements DataCellSerializer<SingleFeatureCell> {

		/**
		 * {@inheritDoc}
		 */
		public void serialize(final SingleFeatureCell cell, final DataCellDataOutput output) throws IOException {

			output.writeInt(cell.getSingleProfileValue().size());

			for (XYZPoint dp : cell.getSingleProfileValue()) {
				output.writeDouble(dp.x);
				output.writeDouble(dp.y);
				output.writeDouble(dp.z);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public SingleFeatureCell deserialize(final DataCellDataInput input) throws IOException {

			XYZList data = new XYZList();

			int n = input.readInt();
			for (int i = 0; i < n; i++)
				data.add(new XYZPoint(input.readDouble(), input.readDouble(), input.readDouble()));

			return new SingleFeatureCell(data);
		}
	}

	@Override
	public boolean containsBlobWrapperCells() {
		return false;
	}

	@Override
	public Iterator<DataCell> iterator() {
		
		List<DataCell> cells = new ArrayList<DataCell>();
		for (XYZPoint dp : profileData) cells.add(new DoubleCell(dp.z));
		return cells.iterator();
	}

	@Override
	public DataType getElementType() {
		return DoubleCell.TYPE;
	}

	@Override
	public int size() {
		return profileData.size();
	}

	@Override
	public String getStringValue() {
		return toString();
	}
}
