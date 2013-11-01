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

import javax.swing.Icon;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.data.renderer.DefaultDataValueRendererFamily;
import org.knime.core.data.renderer.MultiLineStringValueRenderer;

import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;

/**
 * Mass spectrometry data value interface.
 * 
 * @author Stephan Beisken
 */
public interface MsValue extends DataValue {

	/**
	 * Meta information to this value type.
	 * 
	 * @see DataValue#UTILITY
	 */
	public static final MsDataUtilityFactory UTILITY = new MsDataUtilityFactory();

	/**
	 * Returns the mass spectrometry file.
	 * 
	 * @return a string value
	 */
	public ScanContainer getMsDataValue();

	/** Implementations of the meta information of this value class. */
	public static class MsDataUtilityFactory extends UtilityFactory {

		private static final Icon ICON = loadIcon(MsValue.class, "ms_data.png");

		/** Only subclasses are allowed to instantiate this class. */
		protected MsDataUtilityFactory() {

		}

		public Icon getIcon() {
			return ICON;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DataValueRendererFamily getRendererFamily(final DataColumnSpec spec) {
			return new DefaultDataValueRendererFamily(new MsRenderer(), new MultiLineStringValueRenderer("String"));
		}
	}
}
