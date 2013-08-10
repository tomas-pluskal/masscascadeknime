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
package uk.ac.ebi.masscascade.knime.curation.brush;

import javax.swing.JCheckBox;

import org.knime.core.data.IntValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "BlessTable" Node.
 * 
 * @author Stephan Beisken
 */
public class BrushNodeFactory extends NodeFactory<BrushNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BrushNodeModel createNodeModel() {
		return new BrushNodeModel();
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
	public NodeView<BrushNodeModel> createNodeView(final int viewIndex,
			final BrushNodeModel nodeModel) {
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
		dialog.addColumnSelection(Parameter.LABEL_COLUMN, IntValue.class);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 8);
		dialog.addTextOption(Parameter.TIME_WINDOW, 8);
		dialog.addTextOption(Parameter.MISSINGNESS, 8);
		dialog.addCustomOption(Parameter.ELEMENT_FILTER, new JCheckBox("", true));
		dialog.addCustomOption(Parameter.ISOTOPE_FILTER, new JCheckBox("", true));
		dialog.addCustomOption(Parameter.FRAGMENTATION_FILTER, new JCheckBox("", true));
		dialog.addCustomOption(Parameter.RELATION_FILTER, new JCheckBox("", true));

		return dialog.build();
	}
}
