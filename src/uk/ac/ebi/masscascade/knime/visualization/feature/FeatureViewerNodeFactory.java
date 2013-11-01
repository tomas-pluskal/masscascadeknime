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
package uk.ac.ebi.masscascade.knime.visualization.feature;

import org.knime.core.data.IntValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "ProfileAligner" Node. Shows all grouped profiles within the specified m/z and rt
 * tolerance.
 * 
 * @author Stephan Beisken
 */
public class FeatureViewerNodeFactory extends NodeFactory<FeatureViewerNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureViewerNodeModel createNodeModel() {
		return new FeatureViewerNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<FeatureViewerNodeModel> createNodeView(final int viewIndex, final FeatureViewerNodeModel nodeModel) {
		return new FeatureViewerNodeView(nodeModel);
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

		dialog.addColumnSelection(Parameter.PEAK_COLUMN, FeatureValue.class);
		dialog.addColumnSelection(Parameter.LABEL_COLUMN, IntValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 8);
		dialog.addTextOption(Parameter.TIME_WINDOW, 8);
		dialog.addTextOption(Parameter.MISSINGNESS, 8);

		return dialog.build();
	}
}
