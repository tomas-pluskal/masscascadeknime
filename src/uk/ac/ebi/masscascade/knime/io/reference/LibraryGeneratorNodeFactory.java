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
package uk.ac.ebi.masscascade.knime.io.reference;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;

/**
 * <code>NodeFactory</code> for the "ProfileConverter" Node. Node to extract peak information from the peak colletions.
 * All peak details are converted into a table matrix, where all peak collections are shown in succession.
 * 
 * @author Stephan Beisken
 */
public class LibraryGeneratorNodeFactory extends NodeFactory<LibraryGeneratorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LibraryGeneratorNodeModel createNodeModel() {
		return new LibraryGeneratorNodeModel();
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
	public NodeView<LibraryGeneratorNodeModel> createNodeView(final int viewIndex,
			final LibraryGeneratorNodeModel nodeModel) {
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

		dialog.addOptionalColumnPanel("id", StringValue.class);
		dialog.addOptionalColumnPanel("title", StringValue.class);
		dialog.addOptionalColumnPanel("source", StringValue.class);
		dialog.addOptionalColumnPanel("name", StringValue.class);
		dialog.addOptionalColumnPanel("notation", StringValue.class);
		dialog.addOptionalColumnPanel("mass", DoubleValue.class);
		dialog.addOptionalColumnPanel("formula", StringValue.class);
		dialog.addOptionalColumnPanel("instrument", StringValue.class);
		dialog.addOptionalColumnPanel("ion mode", StringValue.class);
		dialog.addOptionalColumnPanel("collision energy", IntValue.class);
		dialog.addOptionalColumnPanel("precursor type", StringValue.class);
		dialog.addOptionalColumnPanel("precursor mass", DoubleValue.class);
		dialog.addOptionalColumnPanel("mz list", CollectionDataValue.class);
		dialog.addOptionalColumnPanel("intensity list", CollectionDataValue.class);
		
		dialog.addTextOption("Library Name", 10);
		dialog.addTextOption("Library Source", 10);
		dialog.addTextOption("Library MSn", 10);

		return dialog.build();
	}
}
