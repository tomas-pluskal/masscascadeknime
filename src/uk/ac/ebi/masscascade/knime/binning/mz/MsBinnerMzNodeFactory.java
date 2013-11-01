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
package uk.ac.ebi.masscascade.knime.binning.mz;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "MsBinner" Node. Bins the scans of the mass spectrometry dataset in the m/z domain.
 * 
 * @author Stephan Beisken
 */
@Deprecated
public class MsBinnerMzNodeFactory extends NodeFactory<MsBinnerMzNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MsBinnerMzNodeModel createNodeModel() {
		return new MsBinnerMzNodeModel();
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
	public NodeView<MsBinnerMzNodeModel> createNodeView(final int viewIndex, final MsBinnerMzNodeModel nodeModel) {
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
		dialog.addTextOption(Parameter.MZ_WINDOW_AMU, 6);

		JRadioButton maxButton = new JRadioButton("", true);
		JRadioButton minButton = new JRadioButton();
		JRadioButton sumButton = new JRadioButton();
		JRadioButton avgButton = new JRadioButton();

		ButtonGroup group = new ButtonGroup();
		group.add(maxButton);
		group.add(minButton);
		group.add(sumButton);
		group.add(avgButton);

		dialog.addCustomOption("Max", maxButton);
		dialog.addCustomOption("Min", minButton);
		dialog.addCustomOption("Sum", sumButton);
		dialog.addCustomOption("Avg", avgButton);

		return dialog.build();
	}
}
