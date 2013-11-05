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
package uk.ac.ebi.masscascade.knime.visualization.totalionfeature;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

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
import uk.ac.ebi.masscascade.tables.GroupFeatureTable;
import uk.ac.ebi.masscascade.tables.model.ATableModel;
import uk.ac.ebi.masscascade.tables.renderer.NumberCellRenderer;
import uk.ac.ebi.masscascade.tables.renderer.ScientificCellRenderer;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;
import uk.ac.ebi.masscascade.utilities.xyz.XYZPoint;

/**
 * <code>NodeView</code> for the "SpectrumViewer" Node. Displays the spectrums from the selected run.
 * 
 * @author Stephan Beisken
 */
public class TicFeatureViewerNodeView extends DefaultView {

	private Map<Integer, Feature> features;
	private DataSet ticDataSet;

	private GraphColor graphColor;
	private int selectedRun;
	private int[] selectedRows;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link SpectrumViewerNodeModel})
	 */
	protected TicFeatureViewerNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.PEAK_COLUMN, new GroupFeatureTable());

		selectedRun = 1;
		graphColor = new GraphColor();

		tableView.getContentTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.SPLINE, true);
		chart.setDefaultTracePainter(tracePainter);
	}

	/**
	 * Updates the spectrum list.
	 */
	protected void loadDataFromTable(int... rowNumber) {

		selectedRun = rowNumber.length != 0 ? rowNumber[0] : selectedRun;
		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], selectedRun);
		Container featureContainer = null;
		if (row.getCell(column).getType() == FeatureCell.TYPE)
			featureContainer = ((FeatureValue) row.getCell(column)).getPeakDataValue();
		else if (row.getCell(column).getType() == FeatureSetCell.TYPE)
			featureContainer = ((FeatureSetValue) row.getCell(column)).getFeatureSetDataValue();

		List<Object[]> tableDataList = new ArrayList<>();
		Map<Double, Double> ticMap = new HashMap<Double, Double>();
		features = new HashMap<>();
		for (Feature feature : featureContainer.featureIterator()) {

			for (XYZPoint dp : feature.getData()) {
				if (ticMap.containsKey(dp.x))
					ticMap.put(dp.x, ticMap.get(dp.x) + dp.z);
				else
					ticMap.put(dp.x, dp.z);
			}

			tableDataList.add(new Object[] { feature.getId(), feature.getMz(), feature.getRetentionTime(),
					feature.getIntensity() });
			features.put(feature.getId(), feature);
		}

		Object[][] tableData = new Object[tableDataList.size()][4];
		int i = 0;
		for (Object[] o : tableDataList) {
			tableData[i][0] = o[0];
			tableData[i][1] = o[1];
			tableData[i][2] = o[2];
			tableData[i][3] = o[3];
			i++;
		}

		XYList data = new XYList();
		for (Double rt : ticMap.keySet())
			data.add(new XYPoint(rt, ticMap.get(rt)));
		Collections.sort(data);
		ticDataSet = new DataSet.Builder(data, "TIC").color(Color.BLACK).build();

		ProfileTableModel tableModel = new ProfileTableModel();
		tableModel.setData(tableData);
		listTable.setModel(tableModel);
		listTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer());
		listTable.getColumnModel().getColumn(2).setCellRenderer(new NumberCellRenderer(new DecimalFormat("0.00")));
		listTable.getColumnModel().getColumn(3).setCellRenderer(new ScientificCellRenderer());

		TableRowSorter<ProfileTableModel> sorter = new TableRowSorter<ProfileTableModel>(tableModel);

		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		listTable.setRowSorter(sorter);

		loadDataFromList();
		chart.zoomAll();
	}

	/**
	 * Updates the features.
	 */
	protected void loadDataFromList(int... selectedRows) {

		this.selectedRows = selectedRows;

		chart.clearData();
		graphColor.reset();

		chart.addData(ticDataSet);

		for (int selectedRow : selectedRows)
			chart.addData(getDataSet(selectedRow));

		chartsPanel.revalidate();
	}

	/**
	 * Returns the compiled data set for the selected sample.
	 * 
	 * @param selectedRow selected sample row
	 * @return the data set
	 */
	private DataSet getDataSet(int selectedRow) {

		int profileIndex = Integer.parseInt(""
				+ listTable.getModel().getValueAt(listTable.convertRowIndexToModel(selectedRow), 0));

		XYList data = features.get(profileIndex).getTrace().getData();

		Color color = graphColor.nextColor();
		return new DataSet.Builder(data, "" + profileIndex).color(color).build();
	}

	/**
	 * Returns the scan indices of the selected scans in the table.
	 * 
	 * @return the scan indices
	 */
	protected String[] getSelectedIndices() {

		String[] res = new String[selectedRows.length + 1];
		int i = 0;

		for (int selectedRow : selectedRows) {
			res[i] = "" + listTable.getModel().getValueAt(listTable.convertRowIndexToModel(selectedRow), 0);
			i++;
		}

		res[i] = "TIC";

		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		chart.setAxisTitle(LABELS.RT.getLabel(), LABELS.INTENSITY.getLabel());
	}

	class ProfileTableModel extends ATableModel {

		public ProfileTableModel() {
			super(new String[] { "id", "m/z", "rt [s]", "intensity" });
		}
	}
}
