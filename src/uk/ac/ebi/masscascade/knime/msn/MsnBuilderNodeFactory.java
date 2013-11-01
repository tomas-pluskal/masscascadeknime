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
package uk.ac.ebi.masscascade.knime.msn;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "MSnBuilder" Node.
 * 
 * @author Stephan Beisken
 */
public class MsnBuilderNodeFactory extends NodeFactory<MsnBuilderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MsnBuilderNodeModel createNodeModel() {
		return new MsnBuilderNodeModel();
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
	public NodeView<MsnBuilderNodeModel> createNodeView(final int viewIndex, final MsnBuilderNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {

		DefaultDialog dialog = new DefaultDialog();

		dialog.addColumnSelection(Parameter.DATA_COLUMN, MsValue.class);
		dialog.addColumnSelection(Parameter.FEATURE_SET_COLUMN, FeatureSetValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 8);
		dialog.addTextOption(Parameter.MIN_FEATURE_INTENSITY, 8);
		
		return dialog.build();
	}
}
