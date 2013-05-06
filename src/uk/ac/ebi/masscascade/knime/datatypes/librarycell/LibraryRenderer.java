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
package uk.ac.ebi.masscascade.knime.datatypes.librarycell;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

import uk.ac.ebi.masscascade.reference.ReferenceContainer;

public class LibraryRenderer extends AbstractPainterDataValueRenderer {

	private final String description;
	private ReferenceContainer libraryContainer;
	private Font currentFont;

	public LibraryRenderer(final String description) {
		this.description = description == null ? "LibraryDataCell" : description;
	}

	public void setValue(final Object value) {

		if (value instanceof LibraryValue) {
			setLibraryContainer(((LibraryValue) value).getLibraryValue());
			return;
		} else {
			setLibraryContainer(null);
		}
	}

	/**
	 * Sets a new object to be rendered.
	 * 
	 * @param sample the new reference spectra collection to be rendered (<code>null</code> is ok)
	 */
	protected void setLibraryContainer(final ReferenceContainer libraryContainer) {
		this.libraryContainer = libraryContainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);

		if (libraryContainer == null) {
			g.drawString("Object missing", 3, getHeight() - 3);
			return;
		}

		String id = libraryContainer.getId();
		String source = libraryContainer.getSource();
		String msn = libraryContainer.getMsn().name();
		int size = libraryContainer.size();
		
		int height = getHeight();
		int width = getWidth();
		
		g.drawString("Spectra: " + size, (int) (width / 4), (int) (height - height / 2));
		g.drawString(id + ": " + msn + " - " + source, 3, getHeight() - 3);
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
