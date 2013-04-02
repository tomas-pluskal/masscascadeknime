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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * Class implementing a spinning wheel for task loading.
 * 
 * @author Stephan Beisken
 */
public class InfiniteProgressPanel extends JComponent implements MouseListener {

	protected Area[] ticker = null;
	protected Thread animation = null;
	protected boolean started = false;
	protected int alphaLevel = 0;
	protected int rampDelay = 300;
	protected float shield = 0.70f;
	protected String text = "";
	protected int barsCount = 14;
	protected float fps = 15.0f;

	protected RenderingHints hints = null;

	/**
	 * Constructs the spinning wheel.
	 */
	public InfiniteProgressPanel() {
		this("");
	}

	/**
	 * Constructs a titled spinning wheel.
	 * 
	 * @param text the title
	 */
	public InfiniteProgressPanel(String text) {
		this(text, 14);
	}

	/**
	 * Constructs a titled custom spinning wheel.
	 * 
	 * @param text the title
	 * @param barsCount the number of bars for the wheel
	 */
	public InfiniteProgressPanel(String text, int barsCount) {
		this(text, barsCount, 0.70f);
	}

	/**
	 * Constructs a titled custom spinning wheel.
	 * 
	 * @param text the title
	 * @param barsCount the number of bars for the wheel
	 * @param shield the alpha value
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield) {
		this(text, barsCount, shield, 15.0f);
	}

	/**
	 * Constructs a titled custom spinning wheel.
	 * 
	 * @param text the title
	 * @param barsCount the number of bars for the wheel
	 * @param shield the alpha value
	 * @param fps the granularity (frames per second)
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield, float fps) {
		this(text, barsCount, shield, fps, 300);
	}

	/**
	 * Constructs a titled custom spinning wheel.
	 * 
	 * @param text the title
	 * @param barsCount the number of bars for the wheel
	 * @param shield the alpha value
	 * @param fps the granularity (frames per second)
	 * @param rampDelay the delay between updates
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield, float fps, int rampDelay) {

		this.text = text;
		this.rampDelay = rampDelay >= 0 ? rampDelay : 0;
		this.shield = shield >= 0.0f ? shield : 0.0f;
		this.fps = fps > 0.0f ? fps : 15.0f;
		this.barsCount = barsCount > 0 ? barsCount : 14;

		this.hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		this.hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	/**
	 * Sets the title.
	 * 
	 * @param text the title
	 */
	public void setText(String text) {

		repaint();
		this.text = text;
	}

	/**
	 * Returns the title.
	 * 
	 * @return the title
	 */
	public String getText() {
		return text;
	}

	/**
	 * Starts the spinning wheel.
	 */
	public void start() {

		addMouseListener(this);
		setVisible(true);
		ticker = buildTicker();
		animation = new Thread(new Animator(true));
		animation.start();
	}

	/**
	 * Stops the spinning wheel.
	 */
	public void stop() {

		if (animation != null) {
			animation.interrupt();
			animation = null;
			animation = new Thread(new Animator(false));
			animation.start();
		}
	}

	/**
	 * Interrupts the spinning wheel.
	 */
	public void interrupt() {

		if (animation != null) {
			animation.interrupt();
			animation = null;

			removeMouseListener(this);
			setVisible(false);
		}
	}

	/**
	 * Draws the spinning wheel.
	 */
	public void paintComponent(Graphics g) {

		if (started) {
			int width = getWidth();

			double maxY = 0.0;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(hints);

			g2.setColor(new Color(255, 255, 255, (int) (alphaLevel * shield)));
			g2.fillRect(0, 0, getWidth(), getHeight());

			for (int i = 0; i < ticker.length; i++) {
				int channel = 224 - 128 / (i + 1);
				g2.setColor(new Color(channel, channel, channel, alphaLevel));
				g2.fill(ticker[i]);

				Rectangle2D bounds = ticker[i].getBounds2D();
				if (bounds.getMaxY() > maxY)
					maxY = bounds.getMaxY();
			}

			if (text != null && text.length() > 0) {
				FontRenderContext context = g2.getFontRenderContext();
				TextLayout layout = new TextLayout(text, getFont(), context);
				Rectangle2D bounds = layout.getBounds();
				g2.setColor(getForeground());
				layout.draw(g2, (float) (width - bounds.getWidth()) / 2,
						(float) (maxY + layout.getLeading() + 2 * layout.getAscent()));
			}

			// Image img = getToolkit().getImage(this.getClass().getResource("./professor.png"));
			// g2.drawImage(img, getWidth() / 2 - 40, getHeight() / 2 - 42, this);
		}
	}

	/**
	 * Builds the actual spinning wheel representation.
	 * 
	 * @return the bar elements
	 */
	private Area[] buildTicker() {

		Area[] ticker = new Area[barsCount];
		Point2D.Double center = new Point2D.Double((double) getWidth() / 2, (double) getHeight() / 2);
		double fixedAngle = 2.0 * Math.PI / ((double) barsCount);

		for (double i = 0.0; i < (double) barsCount; i++) {
			Area primitive = buildPrimitive();

			AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
			AffineTransform toBorder = AffineTransform.getTranslateInstance(45.0, -6.0);
			AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

			AffineTransform toWheel = new AffineTransform();
			toWheel.concatenate(toCenter);
			toWheel.concatenate(toBorder);

			primitive.transform(toWheel);
			primitive.transform(toCircle);

			ticker[(int) i] = primitive;
		}

		return ticker;
	}

	/**
	 * Builds a bar element.
	 * 
	 * @return the bar element
	 */
	private Area buildPrimitive() {

		Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
		Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
		Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);

		Area tick = new Area(body);
		tick.add(new Area(head));
		tick.add(new Area(tail));

		return tick;
	}

	/**
	 * Class creating the animation of the spinning wheel.
	 * 
	 * @author Stephan Beisken
	 */
	protected class Animator implements Runnable {

		private boolean rampUp = true;

		protected Animator(boolean rampUp) {
			this.rampUp = rampUp;
		}

		public void run() {

			Point2D.Double center = new Point2D.Double((double) getWidth() / 2, (double) getHeight() / 2);
			double fixedIncrement = 2.0 * Math.PI / ((double) barsCount);
			AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement, center.getX(), center.getY());

			long start = System.currentTimeMillis();
			if (rampDelay == 0)
				alphaLevel = rampUp ? 255 : 0;

			started = true;
			boolean inRamp = rampUp;

			while (!Thread.interrupted()) {
				if (!inRamp) {
					for (int i = 0; i < ticker.length; i++)
						ticker[i].transform(toCircle);
				}

				repaint();

				if (rampUp) {
					if (alphaLevel < 255) {
						alphaLevel = (int) (255 * (System.currentTimeMillis() - start) / rampDelay);
						if (alphaLevel >= 255) {
							alphaLevel = 255;
							inRamp = false;
						}
					}
				} else if (alphaLevel > 0) {
					alphaLevel = (int) (255 - (255 * (System.currentTimeMillis() - start) / rampDelay));
					if (alphaLevel <= 0) {
						alphaLevel = 0;
						break;
					}
				}

				try {
					Thread.sleep(inRamp ? 10 : (int) (1000 / fps));
				} catch (InterruptedException ie) {
					break;
				}
				Thread.yield();
			}

			if (!rampUp) {
				started = false;
				repaint();

				setVisible(false);
				removeMouseListener(InfiniteProgressPanel.this);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		// do nothing
	}

	public void mousePressed(MouseEvent e) {
		// do nothing
	}

	public void mouseReleased(MouseEvent e) {
		// do nothing
	}

	public void mouseEntered(MouseEvent e) {
		// do nothing
	}

	public void mouseExited(MouseEvent e) {
		// do nothing
	}
}
