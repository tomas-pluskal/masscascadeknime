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
package uk.ac.ebi.masscascade.knime.datatypes.mscell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

import uk.ac.ebi.masscascade.interfaces.Chromatogram;
import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels;
import uk.ac.ebi.masscascade.utilities.TextUtils;
import uk.ac.ebi.masscascade.utilities.math.LinearEquation;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

public final class MsRenderer extends AbstractPainterDataValueRenderer {

	private static final long serialVersionUID = 4834950347364477370L;

	private ScanContainer scanContainer;
	private Font currentFont;

	public void setValue(final Object value) {

		if (value instanceof MsValue) {
			setScanFile(((MsValue) value).getMsDataValue());
			return;
		} else {
			setScanFile(null);
		}
	}

	/**
	 * Sets a new object to be rendered.
	 * 
	 * @param sample the new mass spec sample to be rendered (<code>null</code>
	 *        is ok)
	 */
	protected void setScanFile(final ScanContainer sample) {
		scanContainer = sample;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);

		if (scanContainer == null) {
			g.drawString("Object missing", 3, getHeight() - 3);
			return;
		}

		Chromatogram chromatogram = scanContainer.getTicChromatogram(Constants.MSN.MS1);
		if (chromatogram == null || chromatogram.getData().size() == 0) {
			g.drawString("Container empty", 3, getHeight() - 15);
			String[] idParts = TextUtils.cleanId(scanContainer.getId());
			g.drawString(idParts[1], 3, 8);
			g.drawString(idParts[0] + " (" + scanContainer.size() + ")", 3, getHeight() - 3);
			return;
		}

		DataSet dataSet = new DataSet.Builder(chromatogram.getData(), chromatogram.toString()).color(Color.BLUE)
				.xLabel(Labels.LABELS.RT.toString()).yLabel(Labels.LABELS.INTENSITY.toString()).build();

		double maxMz = dataSet.getDataSet().get(dataSet.getDataSet().size() - 1).x;
		double minMz = dataSet.getDataSet().get(0).x;

		double maxInt = 0;
		double minInt = Double.MAX_VALUE;
		for (XYPoint point : dataSet.getDataSet()) {
			if (point.y > maxInt)
				maxInt = point.y;
			if (point.y < minInt)
				minInt = point.y;
		}

		LinearEquation eqX = new LinearEquation(new XYPoint(minMz, 0), new XYPoint(maxMz, getWidth()));
		LinearEquation eqY = new LinearEquation(new XYPoint(minInt, getHeight() - 15), new XYPoint(maxInt, 0));

		for (XYPoint point : dataSet.getDataSet()) {
			int y = (int) eqY.getY(point.y);
			if (y < 9) {
				y = 9;
			}
			g.drawString(".", (int) eqX.getY(point.x), y);
		}

		String[] idParts = TextUtils.cleanId(scanContainer.getId());
		g.drawString(idParts[1], 3, 8);
		g.drawString(idParts[0] + " (" + scanContainer.size() + ")", 3, getHeight() - 3);
	}

	public String getDescription() {
		return "Mass Spectrometry Sample";
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
