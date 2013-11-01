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
package uk.ac.ebi.masscascade.knime.visualization.spectrum;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.knime.core.data.DataRow;

import uk.ac.ebi.masscascade.core.container.file.scan.FileScanContainer;
import uk.ac.ebi.masscascade.interfaces.Scan;
import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultView;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Constants.MSN;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.tables.GroupFeatureTable;
import uk.ac.ebi.masscascade.tables.model.GroupFeatureTableModel;
import uk.ac.ebi.masscascade.tables.renderer.NumberCellRenderer;
import uk.ac.ebi.masscascade.utilities.ChartUtils;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * <code>NodeView</code> for the "SpectrumViewer" Node. Displays the spectrums from the selected run.
 * 
 * @author Stephan Beisken
 */
public class SpectrumViewerNodeView extends DefaultView {

	private List<JMenuItem> msLevelItems;

	private ScanContainer msFile;
	private GraphColor graphColor;
	private Constants.MSN globalLevel;
	private int selectedRun;
	private int[] selectedRows;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link SpectrumViewerNodeModel})
	 */
	protected SpectrumViewerNodeView(final ViewerModel nodeModel) {

		super(nodeModel, Parameter.DATA_COLUMN, new GroupFeatureTable());

		msLevelItems = new ArrayList<JMenuItem>();

		globalLevel = MSN.MS1;
		selectedRun = 1;
		graphColor = new GraphColor();

		tableView.getContentTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Updates the spectrum list.
	 */
	protected void loadDataFromTable(int... rowNumber) {

		selectedRun = rowNumber.length != 0 ? rowNumber[0] : selectedRun;
		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], selectedRun);
		msFile = ((MsValue) row.getCell(column)).getMsDataValue();

		Map<Integer, Long> scanList = ((FileScanContainer) msFile).getScanNumbers(globalLevel);
		List<XYPoint> xyPoints = msFile.getTicChromatogram(globalLevel).getData();

		int j = msFile.getScanLevels().size();

		for (JMenuItem item : msLevelItems) {
			spectrumMenu.remove(item);
		}

		for (int i = 1; i <= j; i++) {
			JMenuItem tmpItem = new JMenuItem("MS Level " + i);
			tmpItem.setAccelerator(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
			tmpItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					chart.updateTraceRanges(true);
					chart.revalidate();
					updateLevel(((JMenuItem) e.getSource()).getText());
				}
			});
			spectrumMenu.add(tmpItem);
			msLevelItems.add(tmpItem);
		}

		Object[][] tableData = new Object[scanList.size()][2];
		int i = 0;
		for (int scanIndex : scanList.keySet()) {
			tableData[i][0] = scanIndex;
			tableData[i][1] = xyPoints.get(i).x;
			i++;
		}

		Object[][] tableDataCut = new Object[i][2];
		System.arraycopy(tableData, 0, tableDataCut, 0, i);

		GroupFeatureTableModel tableModel = new GroupFeatureTableModel();
		tableModel.setData(tableDataCut);
		listTable.setModel(tableModel);
		listTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(new DecimalFormat("0.00")));

		chart.zoomAll();
	}

	/**
	 * Updates the MSn level.
	 * 
	 * @param desc a MSn description
	 */
	private void updateLevel(String desc) {

		globalLevel = Constants.MSN.get(Integer.parseInt(desc.substring(9)));
		loadDataFromTable();
	}

	/**
	 * Updates the spectra.
	 */
	protected void loadDataFromList(int... selectedRows) {

		this.selectedRows = selectedRows;

		chart.clearData();
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

		int scanIndex = Integer.parseInt(""
				+ listTable.getModel().getValueAt(listTable.convertRowIndexToModel(selectedRow), 0));

		Scan scan = msFile.getScan(scanIndex);
		XYList data = scan.getData();
		
		Color color = graphColor.nextColor();
		return new DataSet.Builder(data, "(" + scanIndex + ":" + ChartUtils.format(scan.getRetentionTime()) + ")")
				.color(color).build();
	}

	/**
	 * Returns the scan indices of the selected scans in the table.
	 * 
	 * @return the scan indices
	 */
	protected String[] getSelectedIndices() {

		String[] res = new String[selectedRows.length];
		int i = 0;

		for (int selectedRow : selectedRows) {
			res[i] = "(" + listTable.getModel().getValueAt(selectedRow, 0) + ":"
					+ ChartUtils.format((double) listTable.getModel().getValueAt(selectedRow, 1)) + ")";
			i++;
		}

		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		chart.setAxisTitle(LABELS.MZ.getLabel(), LABELS.INTENSITY.getLabel());
	}
}
