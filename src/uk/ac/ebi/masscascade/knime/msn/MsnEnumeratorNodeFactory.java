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
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.library.LibraryParameter;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "MSnEnumerator" Node.
 * 
 * @author Stephan Beisken
 */
public class MsnEnumeratorNodeFactory extends NodeFactory<MsnEnumeratorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MsnEnumeratorNodeModel createNodeModel() {
		return new MsnEnumeratorNodeModel();
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
	public NodeView<MsnEnumeratorNodeModel> createNodeView(final int viewIndex, final MsnEnumeratorNodeModel nodeModel) {
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

		dialog.addColumnSelection(Parameter.FEATURE_SET_COLUMN, FeatureSetValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_AMU, 8);
		dialog.addTextOption(LibraryParameter.DEPTH, 8);
		dialog.addTextOption(Parameter.EXECUTABLE, 20);
		
		return dialog.build();
	}
}
