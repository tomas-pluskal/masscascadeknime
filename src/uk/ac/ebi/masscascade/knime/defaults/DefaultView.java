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
package uk.ac.ebi.masscascade.knime.defaults;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableContentModel;
import org.knime.core.node.tableview.TableView;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * Default view for MassCascade nodes.
 * 
 * @author Stephan Beisken
 */
public abstract class DefaultView extends NodeView<ViewerModel> {

	private final static Dimension PREF_DISPLAY_SIZE = new Dimension(900, 600);

	protected int column;
	protected JTable listTable;
	protected TableView tableView;
	protected SimpleSpectrum chart;
	protected JPanel chartsPanel;
	protected JMenu spectrumMenu;

	protected JPanel listPanel;
	protected JSplitPane mainPane;
	protected JScrollPane listPane;

	protected JMenuItem discItem;
	protected JMenuItem lineItem;
	protected JMenuItem polyItem;
	protected JMenuItem labelItem;
	protected JMenuItem scaleItem;
	protected JMenuItem zoomItem;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link PeakSpectrumNodeModel})
	 */
	protected DefaultView(final ViewerModel nodeModel, final Parameter columnType, final JTable listTable) {

		super(nodeModel);
		this.listTable = listTable;

		getDataColumn(nodeModel, columnType);

		getLayoutManagers();

		layoutTableView(nodeModel);
		if (this.listTable != null)
			layoutListView(listTable);
		layoutChartView();
		layoutMenuBar();

		setComponent(mainPane);
	}

	/**
	 * Finds the column index of the data column.
	 */
	private void getDataColumn(ViewerModel nodeModel, Parameter columnType) {

		TableContentModel tableContentModel = nodeModel.getContentModel();

		String columnName = nodeModel.getSettings().getColumnName(columnType);

		// hack to avoid "null" value in auto-configured data column
		if (columnName == null) {
			try {
				NodeUtils.getDataTableSpec(nodeModel.getContentModel().getDataTableSpec(), nodeModel.getSettings(),
						columnType);
				columnName = nodeModel.getSettings().getColumnName(columnType);
			} catch (InvalidSettingsException e) {
				e.printStackTrace();
			}
		}

		column = tableContentModel.getDataTable().getDataTableSpec().findColumnIndex(columnName);
	}

	/**
	 * Configures the parent containers for the GUI.
	 */
	private void getLayoutManagers() {

		listPanel = listTable == null ? new JPanel(new GridLayout(1, 1)) : new JPanel(new GridLayout(2, 1));

		chartsPanel = new JPanel(new GridLayout(1, 1));

		mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartsPanel, listPanel);
		mainPane.setPreferredSize(PREF_DISPLAY_SIZE);
		mainPane.setResizeWeight(1);
		mainPane.setDividerLocation(500);
	}

	/**
	 * Configures the data summary view.
	 */
	private void layoutTableView(ViewerModel nodeModel) {

		tableView = new TableView(nodeModel.getContentModel());
		tableView.getContentTable().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				loadDataFromTable(tableView.getContentTable().getSelectedRows());
			}
		});

		listPanel.add(tableView);
	}

	/**
	 * Configures the chart view
	 */
	private void layoutChartView() {

		chart = new SimpleSpectrum();
		ChartPanel chartP = new ChartPanel(chart);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.BAR, false);
		chart.setDefaultTracePainter(tracePainter);
		chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chartsPanel.add(chartP);
	}

	/**
	 * Configures the data list view.
	 */
	private void layoutListView(final JTable listTable) {

		listTable.setFillsViewportHeight(true);

		listTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				loadDataFromList(listTable.getSelectedRows());

			}
		});

		listPane = new JScrollPane(listTable);
		listPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		listPanel.add(listPane);
	}

	/**
	 * Creates the menu bar.
	 */
	private void layoutMenuBar() {

		JMenuBar menuBar = getJMenuBar();

		discItem = new JMenuItem("Toogle disc trace");
		discItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				chart.toogleDisc(getSelectedIndices());
			}
		});
		discItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));

		lineItem = new JMenuItem("Toogle vertical bars");
		lineItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				chart.toogleVerticalBar(getSelectedIndices());
			}
		});
		lineItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));

		polyItem = new JMenuItem("Toogle polyline");
		polyItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				chart.tooglePolyline(getSelectedIndices());
			}
		});
		polyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));

		labelItem = new JMenuItem("Toogle labels");
		labelItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				chart.toogleLabels(getSelectedIndices());
			}
		});
		labelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));

		scaleItem = new JMenuItem("Scale traces");
		scaleItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				chart.scaleTraces(getSelectedIndices());
				chart.zoomAll();
			}
		});
		scaleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
		zoomItem = new JMenuItem("Reset Zoom");
		zoomItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				chart.zoomAll();
			}
		});
		zoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		spectrumMenu = new JMenu("Spectrum");
		spectrumMenu.add(discItem);
		spectrumMenu.add(lineItem);
		spectrumMenu.add(labelItem);
		spectrumMenu.add(polyItem);
		spectrumMenu.addSeparator();
		spectrumMenu.add(scaleItem);
		spectrumMenu.addSeparator();
		spectrumMenu.add(zoomItem);
		spectrumMenu.addSeparator();
		menuBar.add(spectrumMenu);
	}

	/**
	 * Defines the standard load behaviour for the table data.
	 */
	protected abstract void loadDataFromTable(int... rowNumber);

	/**
	 * Defines the standard load behaviour for the list data.
	 */
	protected abstract void loadDataFromList(int... rowNumber);

	/**
	 * Returns the selected table indices.
	 */
	protected abstract String[] getSelectedIndices();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {

		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		// nothing to do
	}
}
