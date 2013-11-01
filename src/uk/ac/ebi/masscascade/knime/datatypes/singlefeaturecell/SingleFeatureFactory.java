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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

import uk.ac.ebi.masscascade.utilities.xyz.XYZList;

/**
 * Factory class to create cell objects representing a single peak.
 * 
 * @author Stephan Beisken
 */
public class SingleFeatureFactory {

	/**
	 * Type for peak data cells.
	 */
	public static final DataType TYPE = SingleFeatureCell.TYPE;

	/**
	 * Don't instantiate this class.
	 */
	private SingleFeatureFactory() {

	}

	/**
	 * Factory method to create {@link DataCell} representing a single peak.
	 * 
	 * @return DataCell representing a single peak
	 * @throws NullPointerException If argument is null
	 */
	public static DataCell create(final XYZList profileData) {
		return new SingleFeatureCell(profileData);
	}
}
