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
package uk.ac.ebi.masscascade.knime.datatypes.librarycell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.BlobDataCell;

import uk.ac.ebi.masscascade.reference.ReferenceContainer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Default implementation of a reference library.
 * 
 * @author Stephan Beisken
 */
public class LibraryCell extends BlobDataCell implements StringValue, LibraryValue {

	public static final DataType TYPE = DataType.getType(LibraryCell.class);

	/**
	 * Returns the preferred value class of this cell implementation. This method is called per reflection to determine
	 * which is the preferred renderer, comparator, etc.
	 * 
	 * @return LibraryValue.class;
	 */
	public static final Class<? extends DataValue> getPreferredValueClass() {
		return LibraryValue.class;
	}

	private static final LibraryBlobSerializer SERIALIZER = new LibraryBlobSerializer();

	/**
	 * Returns the factory to read/write DataCells of this class from/to a DataInput/DataOutput. This method is called
	 * via reflection.
	 * 
	 * @return a serializer for reading/writing cells of this kind
	 * @see DataCell
	 */
	public static final LibraryBlobSerializer getCellSerializer() {
		return SERIALIZER;
	}

	private ReferenceContainer referenceContainer;

	/**
	 * Creates a new LibraryCell based on the given reference library object.
	 * 
	 * @param str the String value to store
	 * @throws NullPointerException if the given String value is <code>null</code>
	 */
	public LibraryCell(final ReferenceContainer referenceContainer) {

		this.referenceContainer = referenceContainer;
		if (referenceContainer == null) {
			throw new NullPointerException("Reference library must not be null.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringValue() {
		return referenceContainer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public ReferenceContainer getLibraryValue() {
		return referenceContainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getStringValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean equalsDataCell(final DataCell dc) {
		return getStringValue().equals(((LibraryCell) dc).getStringValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getStringValue().hashCode();
	}

	/**
	 * Factory for (de-)serializing a LibraryCell.
	 */
	private static class LibraryBlobSerializer implements DataCellSerializer<LibraryCell> {

		/**
		 * {@inheritDoc}
		 */
		public void serialize(final LibraryCell cell, final DataCellDataOutput output) throws IOException {

			Kryo kryo = new Kryo();
			Output kryoOutput = new Output((OutputStream) output);
			kryo.writeObject(kryoOutput, cell.getLibraryValue());
		}

		/**
		 * {@inheritDoc}
		 */
		public LibraryCell deserialize(final DataCellDataInput input) throws IOException {

			Kryo kryo = new Kryo();
			Input kryoInput = new Input((InputStream) input);
			ReferenceContainer libraryContainer = kryo.readObject(kryoInput, ReferenceContainer.class);

			return new LibraryCell(libraryContainer);
		}
	}
}
