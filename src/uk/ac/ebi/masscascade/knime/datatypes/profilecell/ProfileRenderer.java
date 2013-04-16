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
package uk.ac.ebi.masscascade.knime.datatypes.profilecell;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.SortedSet;

import org.apache.commons.math3.util.FastMath;
import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;

import com.google.common.collect.TreeMultimap;

public class ProfileRenderer extends AbstractPainterDataValueRenderer {

	private final String description;
	private ProfileContainer profileContainer;
	private Font currentFont;

	public ProfileRenderer(final String description) {

		this.description = description == null ? "PeakDataCell" : description;
	}

	public void setValue(final Object value) {

		if (value instanceof ProfileValue) {
			setProfileContainer(((ProfileValue) value).getPeakDataValue());
			return;
		} else {
			setProfileContainer(null);
		}
	}

	/**
	 * Sets a new object to be rendered.
	 * 
	 * @param sample the new peak collection to be rendered (<code>null</code> is ok)
	 */
	protected void setProfileContainer(final ProfileContainer profileContainer) {

		this.profileContainer = profileContainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);

		if (profileContainer == null) {
			g.drawString("Object missing", 3, getHeight() - 3);
			return;
		}

		TreeMultimap<Double, Integer> peakTimes = profileContainer.getTimes();
		if (peakTimes.isEmpty()) {
			g.drawString(profileContainer.getId(), 3, getHeight() - 3);
			return;
		}

		int[] bins = new int[51];
		SortedSet<Double> times = peakTimes.keySet();
		double maxRt = times.last();
		
		int maxCount = 0;
		for (double rt : times) {
			int binIndex = (int) FastMath.round(rt * 50 / maxRt);
			bins[binIndex]++;
			if (bins[binIndex] > maxCount) maxCount = bins[binIndex];
		}

		for (double rt : times) {
			int x = (int) (rt * getWidth() / maxRt);
			int y = getHeight() - 15 - (bins[(int) FastMath.round(rt * 50 / maxRt)] * (getHeight() - 15) / maxCount);
			g.drawLine(x, y, x, getHeight() - 15);
		}

		g.drawString(profileContainer.getId(), 3, getHeight() - 3);
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

		return new Dimension(100, 30);
	}
}
