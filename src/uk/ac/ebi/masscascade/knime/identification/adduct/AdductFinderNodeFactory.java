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
package uk.ac.ebi.masscascade.knime.identification.adduct;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "AdductFinder" Node. Detects adducts within the peaks grouped by retention time.
 * 
 * @author Stephan Beisken
 */
public class AdductFinderNodeFactory extends NodeFactory<AdductFinderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AdductFinderNodeModel createNodeModel() {
		return new AdductFinderNodeModel();
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
	public NodeView<AdductFinderNodeModel> createNodeView(final int viewIndex, final AdductFinderNodeModel nodeModel) {
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
		dialog.addColumnSelection(Parameter.LABEL_COLUMN, 1, StringValue.class);
		dialog.addColumnSelection(Parameter.VALUE_COLUMN, 1, DoubleValue.class);

		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 5);

		JRadioButton posMode = new JRadioButton("", true);
		JRadioButton negMode = new JRadioButton("", false);

		ButtonGroup bg = new ButtonGroup();
		bg.add(posMode);
		bg.add(negMode);

		dialog.addCustomOption(Parameter.POSITIVE_MODE, posMode);
		dialog.addCustomOption(Parameter.NEGATIVE_MODE, negMode);
		
		dialog.addCustomOption(Parameter.NEUTRAL_LOSS, new JCheckBox());

		return dialog.build();
	}
}
