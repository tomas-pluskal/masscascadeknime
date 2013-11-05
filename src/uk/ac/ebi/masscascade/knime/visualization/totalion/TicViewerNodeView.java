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
package uk.ac.ebi.masscascade.knime.visualization.totalion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.knime.core.data.DataRow;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.interfaces.Chromatogram;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultView;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.math.MathUtils;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * <code>NodeView</code> for the "TicViewer" Node. Visualises the total ion chromatogram (TIC) of the selected mass
 * spectrometry run.
 * 
 * @author Stephan Beisken
 */
public class TicViewerNodeView extends DefaultView {

	private Chromatogram tic;
	private List<JMenuItem> msLevelItems;
	private JMenuItem sumTicMenuItem;

	private Constants.MSN globalLevel;
	private int[] selectedRows;
	private final GraphColor graphColor;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link TicViewerNodeModel})
	 */
	protected TicViewerNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.DATA_COLUMN, null);

		msLevelItems = new ArrayList<JMenuItem>();

		tic = null;
		globalLevel = Constants.MSN.MS1;
		selectedRows = new int[] { 1 };
		graphColor = new GraphColor();

		sumTicMenuItem = new JMenuItem("Total Ion Intensity");
		sumTicMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				showTicIntensities();
			}
		});
		spectrumMenu.add(sumTicMenuItem);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.SPLINE, true);
		chart.setDefaultTracePainter(tracePainter);
	}

	/**
	 * Updates the chart.
	 * 
	 * @param selectedRows selected sample rows
	 */
	protected void loadDataFromTable(int... selectedRows) {

		this.selectedRows = selectedRows;

		chart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.INTENSITY.getLabel());

		chart.clearData();
		graphColor.reset();

		for (int selectedRow : selectedRows) chart.addData(getDataSet(selectedRow, globalLevel));

		chart.updateTraceRanges(false);
		chartsPanel.repaint();
	}

	/**
	 * Changes the MSn level displayed.
	 * 
	 * @param desc a MSn description
	 */
	private void updateLevel(String desc) {

		globalLevel = Constants.MSN.get(Integer.parseInt(desc.substring(9)));
		graphColor.reset();
		chart.clearData();

		for (int selectedRow : selectedRows) {
			chart.addData(getDataSet(selectedRow, globalLevel));
		}

		chart.zoomAll();
		chartsPanel.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		chart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.INTENSITY.getLabel());
	}

	/**
	 * Returns the compiled data set for the chart.
	 * 
	 * @param rowIndex a row index
	 * @param level a MSn level
	 * @return the data set
	 */
	private DataSet getDataSet(int rowIndex, Constants.MSN level) {

		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], rowIndex);

		tic = ((MsValue) row.getCell(column)).getMsDataValue().getTicChromatogram(level);
		int j = ((MsValue) row.getCell(column)).getMsDataValue().getScanLevels().size();

		for (JMenuItem item : msLevelItems) spectrumMenu.remove(item);

		for (int i = 1; i <= j; i++) {
			JMenuItem tmpItem = new JMenuItem("MS Level " + i);
			tmpItem.setAccelerator(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
			tmpItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					updateLevel(((JMenuItem) e.getSource()).getText());
				}
			});
			spectrumMenu.add(tmpItem);
			msLevelItems.add(tmpItem);
		}

		return new DataSet.Builder(tic.getData(), tic.toString()).color(graphColor.nextColor()).build();
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
					titles[titleIndex] = (((MsValue) row.getCell(column)).getMsDataValue().getId());
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

	/**
	 * Shows a spectrum with the total ion chromatogram intensities across all data containers.
	 */
	private void showTicIntensities() {

		int counter = 1;
		XYList data = new XYList();
		for (DataRow row : this.getNodeModel().getInternalTables()[0]) {

			double totalIntensity = 0;
			tic = ((MsValue) row.getCell(column)).getMsDataValue().getTicChromatogram(globalLevel);
			for (XYPoint ticDp : tic.getData()) {
				totalIntensity += ticDp.y;
			}
			data.add(new XYPoint(counter++, totalIntensity));
			System.out.println(totalIntensity);
		}

		XYList meanData = new XYList();
		for (int i = 1; i <= data.size(); i++) {
			meanData.add(new XYPoint(i, 0));
		}

		DataSet meanSet = new DataSet.Builder(meanData, "").color(graphColor.nextColor()).build();

		XYList mcData = MathUtils.getMeanCenteredData(data);
		DataSet dataSet = new DataSet.Builder(mcData, "Total Ion Chromatogram Intensities")
				.color(graphColor.nextColor()).xLabel(LABELS.SAMPLE.getLabel()).yLabel(LABELS.INTENSITY.getLabel())
				.build();

		chart.clearData();
		graphColor.reset();

		chart.addData(dataSet);
		chart.addData(meanSet);

		chart.setAxisTitle("Sample #", "Total Intensity");

		chartsPanel.repaint();
	}
}
