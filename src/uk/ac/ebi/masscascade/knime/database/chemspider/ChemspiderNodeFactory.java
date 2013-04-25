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
package uk.ac.ebi.masscascade.knime.database.chemspider;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultDialog;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeFactory</code> for the "Chemspider" Node. Peak-based Chemspider database search.
 * 
 * @author Stephan Beisken
 */
public class ChemspiderNodeFactory extends NodeFactory<ChemspiderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChemspiderNodeModel createNodeModel() {
		return new ChemspiderNodeModel();
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
	public NodeView<ChemspiderNodeModel> createNodeView(final int viewIndex, final ChemspiderNodeModel nodeModel) {
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
		dialog.addTextOption(Parameter.MZ_WINDOW_PPM, 5);

		JRadioButton positiveMode = new JRadioButton("", true);
		JRadioButton negativeMode = new JRadioButton("");

		ButtonGroup ionModeGroup = new ButtonGroup();
		ionModeGroup.add(positiveMode);
		ionModeGroup.add(negativeMode);

		dialog.addCustomOption(Parameter.POSITIVE_MODE, positiveMode);
		dialog.addCustomOption(Parameter.NEGATIVE_MODE, negativeMode);

		dialog.addTextOption(Parameter.SECURITY_TOKEN, 20);
		
		Map<String, Boolean> databases = new LinkedHashMap<>();
		databases.put("ChEBI", true);
		databases.put("NIST", true);
		databases.put("ZINC", false);
		databases.put("ChEMBL", false);
		databases.put("PubChem", false);
		databases.put("NMRShiftDB", false);
		databases.put("WikiPathways", false);
		databases.put("Human Metabolome Database", false);
		databases.put("SMPDB Small Molecule Pathway Database", false);

		dialog.addBooleanTable(Parameter.DATABASES, databases);

		return dialog.build();
	}
}
