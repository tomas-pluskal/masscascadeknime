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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

import uk.ac.ebi.masscascade.interfaces.container.RawContainer;

/**
 * Factory class to create cell objects representing mass spectrometry formats.
 * 
 * @author Stephan Beisken
 */
public final class MsFactory {

	/**
	 * Type for mass spectrometry data cells.
	 */
	public static final DataType TYPE = MsCell.TYPE;

	/**
	 * Don't instantiate this class.
	 */
	private MsFactory() {

	}

	/**
	 * Factory method to create {@link DataCell} representing mass spectrometry formats.
	 * 
	 * @param File File pointing to the mass spectrometry data file.
	 * @return DataCell representing mass spectrometry data content.
	 */
	public static DataCell create(final RawContainer rawContainer) {

		return new MsCell(rawContainer);
	}
}
