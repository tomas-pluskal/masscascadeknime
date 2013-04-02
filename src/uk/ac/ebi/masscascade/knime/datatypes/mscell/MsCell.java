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
package uk.ac.ebi.masscascade.knime.datatypes.mscell;

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
import org.xmlcml.cml.element.CMLCml;

import uk.ac.ebi.masscascade.core.container.file.raw.FileRawContainer;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.io.cml.RawDeserializer;
import uk.ac.ebi.masscascade.io.cml.RawSerializer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataInputStream;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataOutputStream;

/**
 * Default implementation of a mass spectrometry data cell.
 * 
 * @author Stephan Beisken
 */
public class MsCell extends BlobDataCell implements StringValue, MsValue {

	private static final long serialVersionUID = 395409814576845171L;

	public static final DataType TYPE = DataType.getType(MsCell.class);

	/**
	 * Returns the preferred value class of this cell implementation. This method is called per reflection to determine
	 * which is the preferred renderer, comparator, etc.
	 * 
	 * @return MsDataValue.class;
	 */
	public static final Class<? extends DataValue> getPreferredValueClass() {

		return MsValue.class;
	}

	private static final MsDataBlobSerializer SERIALIZER = new MsDataBlobSerializer();

	/**
	 * Returns the factory to read/write DataCells of this class from/to a DataInput/DataOutput. This method is called
	 * via reflection.
	 * 
	 * @return a serializer for reading/writing cells of this kind
	 * @see DataCell
	 */
	public static final MsDataBlobSerializer getCellSerializer() {

		return SERIALIZER;
	}

	private RawContainer rawContainer;

	/**
	 * Creates a new MsDataCell based on the given MassSpectrometryFile.
	 * 
	 * @param str the String value to store
	 * @throws NullPointerException if the given String value is <code>null</code>
	 */
	public MsCell(final RawContainer rawContainer) {

		this.rawContainer = rawContainer;
		if (rawContainer == null) {
			throw new NullPointerException("MassSpecFile object must not be null.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringValue() {

		String result = rawContainer.getId();

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public RawContainer getMsDataValue() {

		return rawContainer;
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

		return getStringValue().equals(((MsCell) dc).getStringValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		return getStringValue().hashCode();
	}

	/**
	 * Factory for (de-)serializing a MsDataBlobCell.
	 */
	private static class MsDataBlobSerializer implements DataCellSerializer<MsCell> {

		/**
		 * {@inheritDoc}
		 */
		public void serialize(final MsCell cell, final DataCellDataOutput output) throws IOException {

			RawSerializer cmlSerializer = new RawSerializer((FileRawContainer) cell.getMsDataValue());
			CMLCml cml = cmlSerializer.getCml();

			BufferedDataOutputStream stream = new BufferedDataOutputStream((OutputStream) output);
			cml.serialize(stream, 1);

			cml = null;

			stream.flush();
			stream.close();
		}

		/**
		 * {@inheritDoc}
		 */
		public MsCell deserialize(final DataCellDataInput input) throws IOException {

			BufferedDataInputStream stream = new BufferedDataInputStream((InputStream) input);
			RawContainer rawRun = null;

			try {
				RawDeserializer cmlDeserializer = new RawDeserializer(stream, NodePlugin.getProjectDirectory());
				rawRun = (RawContainer) cmlDeserializer.getFile();

				stream.close();

				cmlDeserializer = null;
			} catch (Exception exception) {
				throw new IOException("XMLStream failure: " + exception.getMessage());
			}
			return new MsCell(rawRun);
		}
	}
}
