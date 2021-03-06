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
package uk.ac.ebi.masscascade.knime.io.convert;

import javax.swing.JCheckBox;

import org.knime.core.data.IntValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureValue;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "ProfileMatrix" node to build the m/z to sample matrix.
 * 
 * @author Stephan Beisken
 */
public class ResultMatrixNodeFactory extends NodeFactory<ResultMatrixNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultMatrixNodeModel createNodeModel() {
		return new ResultMatrixNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<ResultMatrixNodeModel> createNodeView(final int viewIndex,
			final ResultMatrixNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

//	public static final String CLASSIC_MATRIX = "Sample to m/z matrix only";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {

		DefaultDialog dialog = new DefaultDialog();
		dialog.addColumnSelection(Parameter.FEATURE_COLUMN, FeatureValue.class, FeatureSetValue.class);
		dialog.addColumnSelection(Parameter.DATA_COLUMN, MsValue.class);
		dialog.addColumnSelection(Parameter.LABEL_COLUMN, IntValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 8);
		dialog.addTextOption(Parameter.TIME_WINDOW, 8);
		dialog.addTextOption(Parameter.MISSINGNESS, 8);
		dialog.addCustomOption(Parameter.GAP_FILL, new JCheckBox());
		dialog.addTextOption(Parameter.DEFAULT, 8);
		
//		dialog.addCustomOption(CLASSIC_MATRIX, new JCheckBox());
		
		return dialog.build();
	}
}
