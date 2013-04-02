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
package uk.ac.ebi.masscascade.knime.alignment.obiwarp;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "MsFastDtw" Node. Fast dynamic time warp for
 * mass spectrometry sample alignment.
 * 
 * @author Stephan Beisken
 */
public class ObiwarpNodeFactory extends NodeFactory<ObiwarpNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObiwarpNodeModel createNodeModel() {
		return new ObiwarpNodeModel();
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
	public NodeView<ObiwarpNodeModel> createNodeView(final int viewIndex, final ObiwarpNodeModel nodeModel) {
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
		dialog.addColumnSelection(Parameter.REFERENCE_PROFILE_COLUMN, 1, ProfileValue.class);
		dialog.addTextOption(Parameter.EXECUTABLE, 20);
		dialog.addTextOption(Parameter.GAP_INIT, 5);
		dialog.addTextOption(Parameter.GAP_EXTEND, 5);
		dialog.addTextOption(Parameter.RESPONSE, 5);
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 5);
		dialog.addTextOption(Parameter.TIME_WINDOW, 5);

		return dialog.build();
	}
}
