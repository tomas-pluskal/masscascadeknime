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
package uk.ac.ebi.masscascade.knime.identification.isotope.remove;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "IsotopeRemover" Node. Removes peaks from the peak collections that are annotated as
 * isotopes.
 * 
 * @author Stephan Beisken
 */
public class IsotopeRemoverNodeFactory extends NodeFactory<IsotopeRemoverNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IsotopeRemoverNodeModel createNodeModel() {
		return new IsotopeRemoverNodeModel();
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
	public NodeView<IsotopeRemoverNodeModel> createNodeView(final int viewIndex, final IsotopeRemoverNodeModel nodeModel) {
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

		dialog.addColumnSelection(Parameter.SPECTRUM_COLUMN, SpectrumValue.class);

		JRadioButton extractButton = new JRadioButton("", true);
		JRadioButton removeButton = new JRadioButton();

		ButtonGroup bg = new ButtonGroup();
		bg.add(extractButton);
		bg.add(removeButton);

		dialog.addCustomOption(Parameter.KEEP_ISOTOPES, extractButton);
		dialog.addCustomOption(Parameter.REMOVE_ISOTOPES, removeButton);

		return dialog.build();
	}
}
