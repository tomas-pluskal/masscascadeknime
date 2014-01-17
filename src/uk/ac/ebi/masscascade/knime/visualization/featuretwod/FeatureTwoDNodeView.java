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
package uk.ac.ebi.masscascade.knime.visualization.featuretwod;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataRow;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.interfaces.Feature;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureCell;
import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureValue;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetCell;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultView;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;

/**
 * <code>NodeView</code> for the "TicViewer" Node. Visualises the total ion chromatogram (TIC) of the selected mass
 * spectrometry run.
 * 
 * @author Stephan Beisken
 */
public class FeatureTwoDNodeView extends DefaultView {

	private int[] selectedRows;
	private final GraphColor graphColor;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link ProfileTwoDNodeModel})
	 */
	protected FeatureTwoDNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.FEATURE_COLUMN, null);

		selectedRows = new int[] { 1 };
		graphColor = new GraphColor();

		getJMenuBar().remove(spectrumMenu);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.DISC_ONLY, false);
		chart.setDefaultTracePainter(tracePainter);

		chart.getAxisY().setPaintGrid(false);
		chart.setUseAntialiasing(false);
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
		Container profileContainer;
		if (row.getCell(column).getType() == FeatureCell.TYPE)
			profileContainer = ((FeatureCell) row.getCell(column)).getPeakDataValue();
		else
			profileContainer = ((FeatureSetCell) row.getCell(column)).getFeatureSetDataValue();
		
		XYList trace = new XYList();
		for (Feature profile : profileContainer.featureIterator()) trace.addAll(profile.getData().getXYSlice());
		chart.addData(new DataSet.Builder(trace, profileContainer.getId()).color(graphColor.nextColor()).build());

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
					if (row.getCell(column).getType() == FeatureCell.TYPE)
						titles[titleIndex] =(((FeatureValue) row.getCell(column)).getPeakDataValue().getId());
					else
						titles[titleIndex] =(((FeatureSetValue) row.getCell(column)).getFeatureSetDataValue().getId());
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
