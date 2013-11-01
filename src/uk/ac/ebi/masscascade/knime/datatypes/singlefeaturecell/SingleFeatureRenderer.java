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
package uk.ac.ebi.masscascade.knime.datatypes.singlefeaturecell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

import uk.ac.ebi.masscascade.utilities.xyz.XYZList;
import uk.ac.ebi.masscascade.utilities.xyz.XYZPoint;

public class SingleFeatureRenderer extends AbstractPainterDataValueRenderer {

	private final String description;
	private XYZList profileData;

	public SingleFeatureRenderer(final String description) {
		this.description = description == null ? "SingleProfileDataCell" : description;
	}

	public void setValue(final Object value) {

		if (value instanceof SingleFeatureValue)
			setData(((SingleFeatureValue) value).getSingleProfileValue());
		else
			setData(null);
	}

	/**
	 * Sets a new object to be rendered.
	 * 
	 * @param sample the new peak data to be rendered (<code>null</code> is ok)
	 */
	protected void setData(final XYZList profileData) {
		this.profileData = profileData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);

		if (profileData == null) {
			g.drawString("Object missing", 3, getHeight() - 3);
			return;
		}

		g.setColor(Color.RED);

        double zMax = 0;
        double zMin = Double.MAX_VALUE;

        double xMin = Double.MAX_VALUE;
        double xMax = 0;

        if (profileData == null) return;

        for (XYZPoint xyzPoint : profileData) {

            if (zMax < xyzPoint.z) zMax = xyzPoint.z;
            if (zMin > xyzPoint.z) zMin = xyzPoint.z;

            if (xMax < xyzPoint.x) xMax = xyzPoint.x;
            if (xMin > xyzPoint.x) xMin = xyzPoint.x;
        }

        xMin -= 5;
        xMax += 5;

        double mx = getWidth() / (xMax - xMin);
        double bx = getWidth() - (mx * xMax);

        double mz = getHeight() / (zMax - zMin);
        double bz = getHeight() - (mz * zMax);

        int z1 = getHeight();
        int z2 = z1;

        int x1 = (int) (mx * profileData.get(0).x + bx);
        int x2 = x1;

        for (int i = 0; i < profileData.size(); i++) {

            x2 = (int) (mx * profileData.get(i).x + bx);
            z2 = (int) (getHeight() - (mz * profileData.get(i).z + bz));

            g.drawLine(x1, z1, x2, z2);

            x1 = x2;
            z1 = z2;
        }

        g.drawLine(x1, z1, x1, getHeight());
	}

	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(50, 30);
	}
}
