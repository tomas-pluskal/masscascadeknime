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
package uk.ac.ebi.masscascade.knime.visualization;

import java.awt.Color;

/**
 * Class managing the available chart colors.
 * 
 * @author Stephan Beisken
 */
public class GraphColor {

	private int colorIndex;

	private Color[] colors = new Color[] { new Color(0, 0, 255, 128), new Color(255, 0, 0, 128),
			new Color(0, 255, 0, 128), new Color(255, 0, 255, 128), new Color(0, 255, 255, 128) };

	/**
	 * Constructs a color manager.
	 */
	public GraphColor() {

		colorIndex = 0;
	}

	/**
	 * Returns the next color in the list.
	 * 
	 * @return a color
	 */
	public Color nextColor() {

		Color color = colors[colorIndex];
		colorIndex++;
		if (colorIndex == colors.length)
			colorIndex = 0;

		return color;
	}

	/**
	 * Resets the list iterator.
	 */
	public void reset() {

		colorIndex = 0;
	}
}
