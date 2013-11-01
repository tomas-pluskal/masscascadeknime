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
package uk.ac.ebi.masscascade.knime.visualization.featuretable.featureinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class MoleculePanel extends JPanel {

	private final AtomContainerRenderer renderer;
	private IAtomContainer molecule;

	public MoleculePanel() {

		this.setBackground(Color.WHITE);

		List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
		generators.add(new BasicSceneGenerator());
		generators.add(new RingGenerator());
		generators.add(new ExtendedAtomGenerator());
		generators.add(new AtomNumberGenerator());
		renderer = new AtomContainerRenderer(generators, new AWTFontManager());

		RendererModel renderer2dModel = renderer.getRenderer2DModel();
		renderer2dModel.set(RingGenerator.ShowAromaticity.class, false);
		renderer2dModel.set(RingGenerator.MaxDrawableAromaticRing.class, 9);
		renderer2dModel.set(BasicSceneGenerator.UseAntiAliasing.class, true);
		renderer2dModel.set(BasicAtomGenerator.ShowExplicitHydrogens.class, true);
		renderer2dModel.set(BasicAtomGenerator.ShowEndCarbons.class, true);
		renderer2dModel.set(ExtendedAtomGenerator.ShowImplicitHydrogens.class, true);
		renderer2dModel.set(AtomNumberGenerator.WillDrawAtomNumbers.class, false);
	}

	public void drawMolecule(String notation) {

		molecule = null;
		try {
			if (notation.startsWith("InChI=")) {
				InChIToStructure iTs = InChIGeneratorFactory.getInstance().getInChIToStructure(notation,
						SilentChemObjectBuilder.getInstance());
				molecule = iTs.getAtomContainer();
			} else {
				SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
				molecule = parser.parseSmiles(notation);
			}

			StructureDiagramGenerator sdg = new StructureDiagramGenerator(molecule);
			sdg.generateCoordinates();
			molecule = sdg.getMolecule();

		} catch (Exception exception) {
			System.out.println(notation);
			exception.printStackTrace();
		}
		
		paint(this.getGraphics());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (molecule == null)
			g.drawString("???", this.getWidth() / 2, this.getHeight() / 2);
		else {
			Rectangle bounds = new Rectangle(15, 15, this.getWidth() - 25, this.getHeight() - 25);
			renderer.paint(molecule, new AWTDrawVisitor((Graphics2D) g), bounds, true);
		}
	}
}
