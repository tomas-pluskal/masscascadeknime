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
package uk.ac.ebi.masscascade.knime.datatypes.profilecell;

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

import uk.ac.ebi.masscascade.core.container.file.profile.FileProfileContainer;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.io.cml.ProfileDeserializer;
import uk.ac.ebi.masscascade.io.cml.ProfileSerializer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataInputStream;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataOutputStream;

/**
 * Default implementation of a peak collection
 * 
 * @author Stephan Beisken
 */
public class ProfileCell extends BlobDataCell implements StringValue, ProfileValue {

	public static final DataType TYPE = DataType.getType(ProfileCell.class);

	/**
	 * Returns the preferred value class of this cell implementation. This method is called per reflection to determine
	 * which is the preferred renderer, comparator, etc.
	 * 
	 * @return ProfileValue.class;
	 */
	public static final Class<? extends DataValue> getPreferredValueClass() {
		return ProfileValue.class;
	}

	private static final ProfileBlobSerializer SERIALIZER = new ProfileBlobSerializer();

	/**
	 * Returns the factory to read/write DataCells of this class from/to a DataInput/DataOutput. This method is called
	 * via reflection.
	 * 
	 * @return a serializer for reading/writing cells of this kind
	 * @see DataCell
	 */
	public static final ProfileBlobSerializer getCellSerializer() {
		return SERIALIZER;
	}

	private ProfileContainer profileContainer;

	/**
	 * Creates a new PeakDataCell based on the given peak object.
	 * 
	 * @param str the String value to store
	 * @throws NullPointerException if the given String value is <code>null</code>
	 */
	public ProfileCell(final ProfileContainer profileContainer) {

		this.profileContainer = profileContainer;
		if (profileContainer == null) {
			throw new NullPointerException("Profile collection must not be null.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringValue() {
		return profileContainer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public ProfileContainer getPeakDataValue() {
		return profileContainer;
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
		return getStringValue().equals(((ProfileCell) dc).getStringValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		return getStringValue().hashCode();
	}

	/**
	 * Factory for (de-)serializing a PeakDataBlobCell.
	 */
	private static class ProfileBlobSerializer implements DataCellSerializer<ProfileCell> {

		/**
		 * {@inheritDoc}
		 */
		public void serialize(final ProfileCell cell, final DataCellDataOutput output) throws IOException {

			ProfileSerializer cmlSerializer = new ProfileSerializer((FileProfileContainer) cell.getPeakDataValue());
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
		public ProfileCell deserialize(final DataCellDataInput input) throws IOException {

			BufferedDataInputStream stream = new BufferedDataInputStream((InputStream) input);
			ProfileContainer profileContainer = null;

			try {
				ProfileDeserializer cmlDeserializer = new ProfileDeserializer(stream, NodePlugin.getProjectDirectory());
				profileContainer = (ProfileContainer) cmlDeserializer.getFile();
				cmlDeserializer = null;
			} catch (Exception exception) {
				throw new IOException("XMLStream failure: " + exception.getMessage());
			}
			return new ProfileCell(profileContainer);
		}
	}
}
