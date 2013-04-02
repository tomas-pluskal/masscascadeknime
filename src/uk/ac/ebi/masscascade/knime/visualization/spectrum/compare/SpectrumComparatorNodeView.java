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
package uk.ac.ebi.masscascade.knime.visualization.spectrum.compare;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.knime.core.data.DataRow;

import uk.ac.ebi.masscascade.core.container.file.raw.FileRawContainer;
import uk.ac.ebi.masscascade.interfaces.Scan;
import uk.ac.ebi.masscascade.interfaces.container.RawContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultView;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.tables.CompareSpectrumTable;
import uk.ac.ebi.masscascade.tables.model.CompareSpectrumTableModel;
import uk.ac.ebi.masscascade.tables.renderer.NumberCellRenderer;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * <code>NodeView</code> for the "SpectrumComparator" Node. Displays the spectrums from the selected run.
 * 
 * @author Stephan Beisken
 */
public class SpectrumComparatorNodeView extends DefaultView {

	private GraphColor graphColor;
	private final Map<String, RawContainer> rawFileMap;
	private final List<String> addedIds;

	private final Settings settings;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link SpectrumComparatorNodeModel})
	 */
	protected SpectrumComparatorNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.DATA_COLUMN, new CompareSpectrumTable());

		settings = nodeModel.getSettings();
		graphColor = new GraphColor();

		rawFileMap = new HashMap<String, RawContainer>();
		addedIds = new ArrayList<String>();
	}

	/**
	 * Updates the displayed spectrum list.
	 */
	protected void loadDataFromTable(int... selectedRows) {

		Constants.MSN msLevel = Constants.MSN.get(settings.getIntOption(Parameter.MS_LEVEL));

		int totalSize = 0;
		for (int selectedRun : selectedRows) {

			DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], selectedRun);
			RawContainer msFile = ((MsValue) row.getCell(0)).getMsDataValue();

			totalSize += msFile.size(msLevel);
		}

		int i = 0;
		RawContainer msFile = null;
		CompareSpectrumTableModel tableModel = new CompareSpectrumTableModel();
		Object[][] tableData = new Object[totalSize][tableModel.getColumnCount()];
		for (int selectedRun : selectedRows) {

			DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], selectedRun);
			msFile = ((MsValue) row.getCell(column)).getMsDataValue();

			rawFileMap.put(msFile.getId(), msFile);

			Map<Integer, Long> scanList = ((FileRawContainer) msFile).getScanNumbers(msLevel);
			List<XYPoint> xyPoints = msFile.getTicChromatogram(msLevel).getData();

			int j = 0;
			for (int scanIndex : scanList.keySet()) {
				tableData[i][0] = msFile.getId();
				tableData[i][1] = scanIndex;
				tableData[i][2] = xyPoints.get(j).x;
				i++;
				j++;
			}
		}

		tableModel.setData(tableData);
		listTable.setModel(tableModel);
		listTable.getColumnModel().getColumn(2).setCellRenderer(new NumberCellRenderer(new DecimalFormat("0.00")));

		TableRowSorter<CompareSpectrumTableModel> sorter = new TableRowSorter<CompareSpectrumTableModel>(tableModel);

		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		listTable.setRowSorter(sorter);

		listTable.revalidate();

		chart.zoomAll();
	}

	/**
	 * Updates the spectrum chart.
	 */
	protected void loadDataFromList(int... selectedRows) {

		chart.clearData();
		addedIds.clear();
		graphColor.reset();

		for (int selectedRow : selectedRows) {
			chart.addData(getDataSet(selectedRow));
		}

		chartsPanel.revalidate();
	}

	/**
	 * Returns the compiled data set for the selected sample.
	 * 
	 * @param selectedRow selected sample row
	 * @return the data set
	 */
	private DataSet getDataSet(int selectedRow) {

		String id = (String) listTable.getModel().getValueAt(listTable.convertRowIndexToModel(selectedRow), 0);
		int scanIndex = Integer.parseInt(""
				+ listTable.getModel().getValueAt(listTable.convertRowIndexToModel(selectedRow), 1));

		Scan scan = rawFileMap.get(id).getScan(scanIndex);
		String cid = "(" + id + ":" + scan.getIndex() + ")";
		addedIds.add(cid);
		XYList data = scan.getData();

		Color color = graphColor.nextColor();
		return new DataSet.Builder(data, cid).color(color).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		chart.setAxisTitle(LABELS.MZ_FULL.getLabel(), LABELS.INTENSITY.getLabel());
	}

	@Override
	protected String[] getSelectedIndices() {

		return addedIds.toArray(new String[] {});
	}
}
