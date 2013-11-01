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
package uk.ac.ebi.masscascade.knime.datatypes.featurecell;

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

import uk.ac.ebi.masscascade.core.container.file.feature.FileFeatureContainer;
import uk.ac.ebi.masscascade.interfaces.container.FeatureContainer;
import uk.ac.ebi.masscascade.io.cml.FeatureDeserializer;
import uk.ac.ebi.masscascade.io.cml.FeatureSerializer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataInputStream;
import uk.ac.ebi.masscascade.utilities.buffer.BufferedDataOutputStream;

/**
 * Default implementation of a peak collection
 * 
 * @author Stephan Beisken
 */
public class FeatureCell extends BlobDataCell implements StringValue, FeatureValue {

	public static final DataType TYPE = DataType.getType(FeatureCell.class);

	/**
	 * Returns the preferred value class of this cell implementation. This
	 * method is called per reflection to determine which is the preferred
	 * renderer, comparator, etc.
	 * 
	 * @return ProfileValue.class;
	 */
	public static final Class<? extends DataValue> getPreferredValueClass() {
		return FeatureValue.class;
	}

	private static final FeatureBlobSerializer SERIALIZER = new FeatureBlobSerializer();

	/**
	 * Returns the factory to read/write DataCells of this class from/to a
	 * DataInput/DataOutput. This method is called via reflection.
	 * 
	 * @return a serializer for reading/writing cells of this kind
	 * @see DataCell
	 */
	public static final FeatureBlobSerializer getCellSerializer() {
		return SERIALIZER;
	}

	private FeatureContainer featureContainer;

	/**
	 * Creates a new PeakDataCell based on the given peak object.
	 * 
	 * @param str the String value to store
	 * @throws NullPointerException if the given String value is
	 *         <code>null</code>
	 */
	public FeatureCell(final FeatureContainer featureContainer) {

		this.featureContainer = featureContainer;
		if (featureContainer == null) {
			throw new NullPointerException("Feature collection must not be null.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringValue() {
		return featureContainer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public FeatureContainer getPeakDataValue() {
		return featureContainer;
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
		return getStringValue().equals(((FeatureCell) dc).getStringValue());
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
	private static class FeatureBlobSerializer implements DataCellSerializer<FeatureCell> {

		/**
		 * {@inheritDoc}
		 */
		public void serialize(final FeatureCell cell, final DataCellDataOutput output) throws IOException {

			FeatureSerializer cmlSerializer = new FeatureSerializer((FileFeatureContainer) cell.getPeakDataValue());
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
		public FeatureCell deserialize(final DataCellDataInput input) throws IOException {

			BufferedDataInputStream stream = new BufferedDataInputStream((InputStream) input);
			FeatureContainer featureContainer = null;

			try {
				FeatureDeserializer cmlDeserializer = new FeatureDeserializer(stream, NodePlugin.getProjectDirectory());
				featureContainer = (FeatureContainer) cmlDeserializer.getFile();
				cmlDeserializer = null;
			} catch (Exception exception) {
				throw new IOException("XMLStream failure: " + exception.getMessage());
			}
			return new FeatureCell(featureContainer);
		}
	}
}
