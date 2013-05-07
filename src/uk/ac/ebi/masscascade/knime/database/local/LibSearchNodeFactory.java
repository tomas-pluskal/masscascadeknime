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
package uk.ac.ebi.masscascade.knime.database.local;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.librarycell.LibraryValue;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "LibSearch" Node.
 * 
 * @author Stephan Beisken
 */
public class LibSearchNodeFactory extends NodeFactory<LibSearchNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LibSearchNodeModel createNodeModel() {
		return new LibSearchNodeModel();
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
	public NodeView<LibSearchNodeModel> createNodeView(final int viewIndex, final LibSearchNodeModel nodeModel) {
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
		dialog.addColumnSelection(Parameter.REFERENCE_COLUMN, 1, LibraryValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 8);
		dialog.addTextOption(Parameter.MZ_WINDOW_AMU, 8);
		dialog.addTextOption(Parameter.COLLISION_ENERGY, 8);
		dialog.addTextOption(Parameter.MS_LEVEL, 8);
		dialog.addTextOption(Parameter.SCORE, 8);
		
		JRadioButton positiveMode = new JRadioButton("", true);
		JRadioButton negativeMode = new JRadioButton("");

		ButtonGroup ionModeGroup = new ButtonGroup();
		ionModeGroup.add(positiveMode);
		ionModeGroup.add(negativeMode);

		dialog.addCustomOption(Parameter.POSITIVE_MODE, positiveMode);
		dialog.addCustomOption(Parameter.NEGATIVE_MODE, negativeMode);
		
		return dialog.build();
	}
}
