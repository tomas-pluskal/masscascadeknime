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
package uk.ac.ebi.masscascade.knime.datatypes.featuresetcell;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.utilities.TextUtils;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

public class FeatureSetRenderer extends AbstractPainterDataValueRenderer {

	private final String description;
	private FeatureSetContainer featureSetContainer;
	private Font currentFont;

	public FeatureSetRenderer(final String description) {
		this.description = description == null ? "FeatureSetDataCell" : description;
	}

	public void setValue(final Object value) {

		if (value instanceof FeatureSetValue) {
			setFeatureSetContainer(((FeatureSetValue) value).getFeatureSetDataValue());
			return;
		} else {
			setFeatureSetContainer(null);
		}
	}

	/**
	 * Sets a new object to be rendered.
	 * 
	 * @param sample the new spectra collection to be rendered (<code>null</code> is ok)
	 */
	protected void setFeatureSetContainer(final FeatureSetContainer featureSetContainer) {
		this.featureSetContainer = featureSetContainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);

		if (featureSetContainer == null) {
			g.drawString("Object missing", 3, getHeight() - 3);
			return;
		}

		int height = getHeight();
		int width = getWidth();
		
		double maxMz = 0;
		double maxRt = 0;
		List<XYPoint> basePeaks = featureSetContainer.getBasePeaks();
		for (XYPoint basePeak : basePeaks) {
			if (maxRt < basePeak.x) maxRt = basePeak.x;
			if (maxMz < basePeak.y) maxMz = basePeak.y;
		}
		
		for (int i = 0; i < basePeaks.size(); i++) {
			int y = height - 5 - (int) (basePeaks.get(i).y * height / maxMz);
			if (y < 9) {
				y = 9;
			}
			g.drawString(".", (int) (basePeaks.get(i).x * width / maxRt), y);
		}
		String[] idParts = TextUtils.cleanId(featureSetContainer.getId());
		g.drawString(idParts[1], 3, 8);
		g.drawString(idParts[0] + " (" + featureSetContainer.size() + ")", 3, getHeight() - 3);
	}

	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFont(final Font font) {

		// DefaultTableCellRenderer sets the font upon each paint to the
		// font of the JTable; we do not want this here so we overwrite it
		if (font == null) {
			super.setFont(currentFont);
		} else if (font.equals(currentFont)) {
			return;
		} else {
			currentFont = new Font("Monospaced", font.getStyle(), font.getSize());
			super.setFont(currentFont);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(180, 100);
	}
}
