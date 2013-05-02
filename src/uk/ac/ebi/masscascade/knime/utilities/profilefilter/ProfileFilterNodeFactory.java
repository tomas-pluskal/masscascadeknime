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
package uk.ac.ebi.masscascade.knime.utilities.profilefilter;

import javax.swing.JCheckBox;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "PeakFilter" Node. Filters and trims a collection of peak by various criteria.
 * 
 * @author Stephan Beisken
 */
public class ProfileFilterNodeFactory extends NodeFactory<ProfileFilterNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProfileFilterNodeModel createNodeModel() {
		return new ProfileFilterNodeModel();
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
	public NodeView<ProfileFilterNodeModel> createNodeView(final int viewIndex, final ProfileFilterNodeModel nodeModel) {
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
		dialog.addTextOption(Parameter.TIME_RANGE, 8);
		dialog.addTextOption(Parameter.MZ_RANGE, 8);
		dialog.addTextOption(Parameter.PROFILE_RANGE, 8);
		dialog.addTextOption(Parameter.MIN_PROFILE_INTENSITY, 8);
		dialog.addCustomOption("Keep isotopes", new JCheckBox());
		
		return dialog.build();
	}
}
