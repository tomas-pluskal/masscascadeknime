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
package uk.ac.ebi.masscascade.knime.deconvolution.biehman;

import javax.swing.JCheckBox;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "BiehmanDeconvolution" Node. Deconvolutes mass traces using a modified Biller
 * Biehman algorithm.
 * 
 * @author Stephan Beisken
 */
public class BiehmanDeconvolutionNodeFactory extends NodeFactory<BiehmanDeconvolutionNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BiehmanDeconvolutionNodeModel createNodeModel() {

		return new BiehmanDeconvolutionNodeModel();
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
	public NodeView<BiehmanDeconvolutionNodeModel> createNodeView(final int viewIndex,
			final BiehmanDeconvolutionNodeModel nodeModel) {

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

		dialog.addColumnSelection(Parameter.PEAK_COLUMN, ProfileValue.class);
		dialog.addTextOption(Parameter.SCAN_WINDOW, 5);
		dialog.addTextOption(Parameter.NOISE_FACTOR, 5);
		dialog.addCustomOption(Parameter.CENTER, new JCheckBox("", true));

		return dialog.build();
	}

}
