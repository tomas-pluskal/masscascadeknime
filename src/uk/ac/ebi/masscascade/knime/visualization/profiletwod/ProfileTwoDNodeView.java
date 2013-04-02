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
package uk.ac.ebi.masscascade.knime.visualization.profiletwod;

import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.knime.core.data.DataRow;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultView;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;
import uk.ac.ebi.masscascade.utilities.xyz.XYZList;

/**
 * <code>NodeView</code> for the "TicViewer" Node. Visualises the total ion chromatogram (TIC) of the selected mass
 * spectrometry run.
 * 
 * @author Stephan Beisken
 */
public class ProfileTwoDNodeView extends DefaultView {

	private int[] selectedRows;
	private final GraphColor graphColor;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link ProfileTwoDNodeModel})
	 */
	protected ProfileTwoDNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.PEAK_COLUMN, null);

		selectedRows = new int[] { 1 };
		graphColor = new GraphColor();

		getJMenuBar().remove(spectrumMenu);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.DISC_ONLY, false);
		chart.setDefaultTracePainter(tracePainter);

		chart.getAxisY().setPaintGrid(false);
	}

	/**
	 * Updates the chart.
	 * 
	 * @param selectedRows selected sample rows
	 */
	protected void loadDataFromTable(int... selectedRows) {

		this.selectedRows = selectedRows;

		chart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.MZ_FULL.getLabel());

		chart.clearData();
		graphColor.reset();

		for (int selectedRow : selectedRows) {
			getDataSet(selectedRow);
		}

		chartsPanel.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		chart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.MZ_FULL.getLabel());
	}

	/**
	 * Returns the compiled data set for the chart.
	 * 
	 * @param rowIndex a row index
	 * @param level a MSn level
	 * @return the data set
	 */
	private DataSet getDataSet(int rowIndex) {

		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], rowIndex);
		ProfileContainer profileContainer = ((ProfileValue) row.getCell(column)).getPeakDataValue();

		XYList trace = new XYList();
		for (Profile profile : profileContainer) {
			XYZList data = profile.getData();
			
			for (int i = 1; i < data.size() - 1; i++) trace.add(new XYPoint(data.get(i).x, data.get(i).y));
			
		}
		chart.addData(new DataSet.Builder(trace, profileContainer.getId()).color(graphColor.nextColor()).build());
		
		ZoomableChart s = new ZoomableChart();
		ChartPanel cp = new ChartPanel(s);
		
		JFrame frame = new JFrame();
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(cp);
		
		return null;
	}
	
	/**
	 * Returns a list of file identifiers.
	 * 
	 * @return the list of file identifiers
	 */
	protected String[] getSelectedIndices() {

		String[] titles = new String[selectedRows.length];
		int titleIndex = 0;
		for (int selectedRowIndex : selectedRows) {

			int rowIndex = 0;
			for (DataRow row : getNodeModel().getInternalTables()[0]) {

				if (rowIndex == selectedRowIndex) {
					titles[titleIndex] = (((ProfileValue) row.getCell(column)).getPeakDataValue().getId());
					titleIndex++;
				}
				rowIndex++;
			}
		}
		return titles;
	}

	@Override
	protected void loadDataFromList(int... rowNumber) {

		// nothing to do
	}
}
