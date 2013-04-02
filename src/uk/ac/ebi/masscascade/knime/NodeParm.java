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
package uk.ac.ebi.masscascade.knime;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;

import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * Parameter class for the MassCascade feature.
 * 
 * @author Stephan Beisken
 */
public class NodeParm {

	/**
	 * Option-Class associations used throughout all mass spectrometry feature nodes.
	 */
	public final static Map<Parameter, Class<? extends DataValue>> columnClass = 
		new HashMap<Parameter, Class<? extends DataValue>>();

	static {
		columnClass.put(Parameter.DATA_COLUMN, MsValue.class);
		columnClass.put(Parameter.REFERENCE_COLUMN, MsValue.class);
		columnClass.put(Parameter.REFERENCE_PROFILE_COLUMN, ProfileValue.class);
		columnClass.put(Parameter.SPECTRUM_COLUMN, SpectrumValue.class);
		columnClass.put(Parameter.PEAK_COLUMN, ProfileValue.class);
		columnClass.put(Parameter.ION_COLUMN, StringValue.class);
		columnClass.put(Parameter.LABEL_COLUMN, StringValue.class);
		columnClass.put(Parameter.VALUE_COLUMN, DoubleValue.class);
	};
}
