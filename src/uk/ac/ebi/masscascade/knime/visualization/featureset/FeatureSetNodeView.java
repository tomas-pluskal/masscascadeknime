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
package uk.ac.ebi.masscascade.knime.visualization.featureset;

import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.core.data.DataRow;
import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableContentModel;
import org.knime.core.node.tableview.TableView;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.interfaces.Chromatogram;
import uk.ac.ebi.masscascade.interfaces.Feature;
import uk.ac.ebi.masscascade.interfaces.FeatureSet;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.knime.visualization.InfiniteProgressPanel;
import uk.ac.ebi.masscascade.knime.visualization.featureset.spectrumquery.JOptionPaneComboBox;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featureinfo.FeatureFrame;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featurequery.JOptionPaneOneInput;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featurequery.JOptionPaneTwoInput;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.tables.GroupFeatureTable;
import uk.ac.ebi.masscascade.tables.SimpleFeatureTable;
import uk.ac.ebi.masscascade.tables.model.GroupFeatureTableModel;
import uk.ac.ebi.masscascade.tables.model.SimpleFeatureTableModel;
import uk.ac.ebi.masscascade.utilities.AnnotationUtils;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.math.MathUtils;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

import com.google.common.collect.Lists;

/**
 * <code>NodeView</code> for the "PeakSpectrum" Node. Visualises grouped peak
 * clusters including their annotations as pseudo spectrum.
 * 
 * @author Stephan Beisken
 */
public class FeatureSetNodeView extends NodeView<ViewerModel> {

	private final static Dimension PREF_DISPLAY_SIZE = new Dimension(900, 600);

	private int column;
	private TableView tableView;

	private JTable spectraTable;
	private JTable traceTable;

	private JMenuItem filterId;
	private JMenuItem filterMass;

	private SimpleSpectrum spectraChart;
	private SimpleSpectrum traceChart;

	private JPanel chartsPanel;
	private JPanel listPanel;
	private JSplitPane mainPane;
	private JScrollPane spectraPane;
	private JScrollPane tracePane;

	private GraphColor graphColorSpectra;
	private GraphColor graphColorTraces;

	private FeatureSetContainer featureSetContainer;
	private FeatureSet currentSpectrum;

	private InfiniteProgressPanel progressPanel;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link PeakSpectrumNodeModel})
	 */
	protected FeatureSetNodeView(final ViewerModel nodeModel) {

		super(nodeModel);

		progressPanel = new InfiniteProgressPanel();
		// hack: add panel to glass pane of 'not visible' JFrame
		((JFrame) this.getComponent().getParent().getParent().getParent().getParent()).setGlassPane(progressPanel);

		graphColorSpectra = new GraphColor();
		graphColorTraces = new GraphColor();

		getDataColumn(nodeModel);

		JMenu menu = new JMenu("Filter...");
		menu.setMnemonic(KeyEvent.VK_F);

		filterId = new JMenuItem("Filter by id");
		filterId.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		filterId.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JOptionPaneOneInput inputPane = new JOptionPaneOneInput();
				if (inputPane.getStatus() != JOptionPane.OK_OPTION)
					return;

				final int id = inputPane.getId();
				SwingUtilities.invokeLater(new RunIdSearch(id));
			}
		});

		filterMass = new JMenuItem("Filter by mass");
		filterMass.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		filterMass.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JOptionPaneTwoInput inputPane = new JOptionPaneTwoInput();
				if (inputPane.getStatus() != JOptionPane.OK_OPTION)
					return;

				final double mass = inputPane.getMz();
				final double ppm = inputPane.getPpm();

				SwingUtilities.invokeLater(new RunMassSearch(mass, ppm));
			}
		});

		menu.add(filterId);
		menu.add(filterMass);
		this.getJMenuBar().add(menu);

		getLayoutManagers();

		layoutTableView(nodeModel);
		layoutListView();
		layoutChartView();

		setComponent(mainPane);
	}

	/**
	 * Finds the column index of the data column.
	 */
	private void getDataColumn(ViewerModel nodeModel) {

		TableContentModel tableContentModel = nodeModel.getContentModel();

		String columnName = nodeModel.getSettings().getColumnName(Parameter.FEATURE_SET_COLUMN);
		column = tableContentModel.getDataTable().getDataTableSpec().findColumnIndex(columnName);
	}

	/**
	 * Configures the parent containers for the GUI.
	 */
	private void getLayoutManagers() {

		listPanel = new JPanel(new GridLayout(3, 1));
		chartsPanel = new JPanel(new GridLayout(2, 1));

		mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartsPanel, listPanel);
		mainPane.setPreferredSize(PREF_DISPLAY_SIZE);
		mainPane.setResizeWeight(1);
		mainPane.setDividerLocation(600);
	}

	/**
	 * Configures the data summary view.
	 */
	private void layoutTableView(ViewerModel nodeModel) {

		tableView = new TableView(nodeModel.getContentModel());
		tableView.getContentTable().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {

						progressPanel = new InfiniteProgressPanel();
						progressPanel.start();
						final GroupFeatureTableModel tableModel = new GroupFeatureTableModel();
						Thread tableLoader = new Thread(new Runnable() {

							public void run() {

								int rowNumber = tableView.getContentTable().getSelectedRows()[0];
								DataRow dataRow = NodeUtils
										.getDataRow(getNodeModel().getInternalTables()[0], rowNumber);
								featureSetContainer = ((FeatureSetValue) dataRow.getCell(column))
										.getFeatureSetDataValue();
								getSpectrumTableModel(tableModel);

								progressPanel.stop();
							}
						}, "TableLoader");
						tableLoader.start();
						while (tableLoader.isAlive()) {
						}
						;

						spectraTable.clearSelection();
						spectraTable.setModel(tableModel);
						spectraTable.revalidate();

						spectraChart.clearData();
						traceChart.clearData();
					}
				});
			}
		});

		listPanel.add(tableView);
	}

	/**
	 * Configures the chart view
	 */
	private void layoutChartView() {

		traceChart = new SimpleSpectrum();
		ChartPanel chartTrace = new ChartPanel(traceChart);
		chartsPanel.add(chartTrace);

		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.SPLINE, false);
		tracePainter.put(PAINTERS.DISC, false);
		traceChart.setDefaultTracePainter(tracePainter);

		spectraChart = new SimpleSpectrum();
		ChartPanel chartSpectra = new ChartPanel(spectraChart);

		Map<SimpleSpectrum.PAINTERS, Boolean> spectrumpainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		spectrumpainter.put(PAINTERS.BAR, false);
		spectrumpainter.put(PAINTERS.LABEL, false);
		spectrumpainter.put(PAINTERS.ANNO, false);
		spectraChart.setDefaultTracePainter(spectrumpainter);

		chartsPanel.add(chartSpectra);
	}

	/**
	 * Configures the data list view.
	 */
	private void layoutListView() {

		spectraTable = new GroupFeatureTable();
		spectraTable.setFillsViewportHeight(true);
		spectraTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spectraTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				try {
					loadDataFromSpectra(spectraTable.getSelectedRow());
				} catch (Exception exception) {
					// fail silently
				}
			}
		});
		spectraPane = new JScrollPane(spectraTable);
		spectraPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		traceTable = new SimpleFeatureTable();
		traceTable.setFillsViewportHeight(true);
		traceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				try {
					loadDataFromTrace(traceTable.getSelectedRows());
				} catch (Exception exception) {
					// fail silently
				}
			}
		});

		final Component parent = this.getComponent();
		traceTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() != 2)
					return;

				int row = traceTable.rowAtPoint(e.getPoint());
				int profileId = (Integer) traceTable.getValueAt(row, 0);

				FeatureFrame infoFrame = new FeatureFrame("Profile Summary", featureSetContainer);
				infoFrame.setProfile(currentSpectrum.getFeature(profileId), currentSpectrum.getIndex() + "");

				infoFrame.setLocationRelativeTo(parent);
				infoFrame.setSize(640, 480);
				infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				infoFrame.setVisible(true);
			}
		});

		tracePane = new JScrollPane(traceTable);
		tracePane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		listPanel.add(spectraPane);
		listPanel.add(tracePane);
	}

	/**
	 * Loads the selected spectrum from the list.
	 * 
	 * @param selectedRow the index of the selected row
	 */
	protected void loadDataFromSpectra(int selectedRow) {

		spectraChart.clearData();
		graphColorSpectra.reset();

		int id = Integer.parseInt(""
				+ spectraTable.getModel().getValueAt(spectraTable.convertRowIndexToModel(selectedRow), 0));
		currentSpectrum = featureSetContainer.getFeatureSet(id);

		SimpleFeatureTableModel tableModel = getTraceTableModel(currentSpectrum.getFeaturesMap());
		traceTable.setModel(tableModel);
		traceTable.revalidate();

		DataSet dataSet = getDataSet(currentSpectrum);
		spectraChart.addData(dataSet);
		spectraChart.updateTraceRanges(true);
		chartsPanel.repaint();
	}

	/**
	 * Returns the compiled data set for the sample.
	 * 
	 * @param spectrum the spectrum
	 * @return the data set
	 */
	private DataSet getDataSet(FeatureSet spectrum) {

		XYList data = spectrum.getData();
		Map<XYPoint, String> annotations = AnnotationUtils.getAnnotations(spectrum.getFeaturesMap().values());
		Collections.sort(data);

		Color color = graphColorSpectra.nextColor();
		return new DataSet.Builder(data, "" + MathUtils.roundToThreeDecimals(spectrum.getRetentionTime())).color(color)
				.annotations(annotations).build();
	}

	/**
	 * Loads all selected traces in the viewer.
	 * 
	 * @param selectedRows the selected traces
	 */
	private void loadDataFromTrace(int... selectedRows) {

		traceChart.clearData();
		graphColorTraces.reset();

		for (int selectedRow : selectedRows) {
			int id = Integer.parseInt(""
					+ traceTable.getModel().getValueAt(traceTable.convertRowIndexToModel(selectedRow), 0));
			Chromatogram trace = currentSpectrum.getFeature(id).getTrace(Constants.PADDING);
			Color color = graphColorTraces.nextColor();
			DataSet dataSet = new DataSet.Builder(trace.getData(), "" + currentSpectrum.getFeature(id).getId()).color(
					color).build();
			traceChart.addData(dataSet);
		}

		chartsPanel.repaint();
	}

	/**
	 * Prepares the table model for the list view.
	 * 
	 * @param groupedPeakMap the peak lists grouped by retention time
	 * @return the table model
	 */
	private void getSpectrumTableModel(GroupFeatureTableModel peakTableModel) {

		Object[][] tableData = new Object[featureSetContainer.size()][3];

		int i = 0;
		for (FeatureSet spectrum : featureSetContainer) {

			tableData[i][0] = spectrum.getIndex();
			tableData[i][1] = spectrum.getRetentionTime();

			i++;
		}

		peakTableModel.setData(tableData);
	}

	/**
	 * Prepares the table model for the list view.
	 * 
	 * @param groupedPeakMap the peak lists grouped by retention time
	 * @return the table model
	 */
	private SimpleFeatureTableModel getTraceTableModel(Map<Integer, Feature> peakMap) {

		Object[][] tableData = new Object[peakMap.size()][3];

		int i = 0;
		for (int id : peakMap.keySet()) {

			tableData[i][0] = id;
			tableData[i][1] = peakMap.get(id).getRetentionTime();
			tableData[i][2] = peakMap.get(id).getMzIntDp().x;

			i++;
		}

		SimpleFeatureTableModel tracekTableModel = new SimpleFeatureTableModel();
		tracekTableModel.setData(tableData);

		return tracekTableModel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		spectraChart.setAxisTitle(LABELS.MZ_FULL.getLabel(), LABELS.INTENSITY.getLabel());
		traceChart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.INTENSITY.getLabel());
	}

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

	class RunMassSearch implements Runnable {

		private double mass;
		private double ppm;

		public RunMassSearch(double mass, double ppm) {

			this.mass = mass;
			this.ppm = ppm;
		}

		public void run() {

			progressPanel = new InfiniteProgressPanel();
			progressPanel.start();
			final List<Integer> resultList = new ArrayList<Integer>();
			final Thread tableLoader = new Thread(new Runnable() {

				public void run() {

					Range range = MathUtils.getRangeFromPPM(mass, ppm);

					if (featureSetContainer != null) {
						for (FeatureSet spectrum : featureSetContainer) {
							for (XYPoint dp : spectrum.getData()) {
								if (range.contains(dp.x))
									resultList.add(spectrum.getIndex());
							}
						}
					}

					progressPanel.stop();
				}
			}, "TableLoader");
			tableLoader.start();
			while (tableLoader.isAlive()) {
			}

			int resultIndex = -1;
			if (resultList.size() == 1) {
				resultIndex = resultList.get(0);
			} else {
				String[] comboBoxStrings = new String[resultList.size()];
				int k = 0;
				for (int i : resultList) {
					double rt = MathUtils.roundToThreeDecimals(featureSetContainer.getFeatureSet(i).getRetentionTime());
					comboBoxStrings[k++] = "id: " + i + " -  rt:" + rt;
				}
				JOptionPaneComboBox inputPane = new JOptionPaneComboBox(comboBoxStrings);
				if (inputPane.getStatus() != JOptionPane.OK_OPTION)
					return;
				resultIndex = resultList.get(inputPane.getIndex());
			}

			if (resultIndex == -1)
				return;

			for (int row = 0; row < spectraTable.getRowCount(); row++) {
				if (resultIndex == (Integer) spectraTable.getModel().getValueAt(row, 0)) {
					spectraTable.getSelectionModel().setSelectionInterval(row, row);
					spectraTable.scrollRectToVisible(new Rectangle(spectraTable.getCellRect(resultIndex, 0, false)));
				}
			}
		}
	}

	class RunIdSearch implements Runnable {

		private int id;

		public RunIdSearch(int id) {
			this.id = id;
		}

		public void run() {

			progressPanel = new InfiniteProgressPanel();
			progressPanel.start();
			final List<Integer> specIndex = Lists.newArrayList();
			Thread tableLoader = new Thread(new Runnable() {

				public void run() {

					if (featureSetContainer != null) {
						for (FeatureSet spectrum : featureSetContainer) {
							if (spectrum.getFeature(id) != null) {
								specIndex.add(spectrum.getIndex());
								break;
							}
						}
					}

					// the inifite progress panel 'freezes' the JFrame if not
					// set to null after termination, why!?
					progressPanel.stop();
					progressPanel = null;

				}
			}, "TableLoader");

			tableLoader.start();
			while (tableLoader.isAlive()) {
			}

			if (!specIndex.isEmpty()) {
				for (int row = 0; row < spectraTable.getRowCount(); row++) {
					if (specIndex.get(0) == (Integer) spectraTable.getModel().getValueAt(row, 0)) {
						spectraTable.getSelectionModel().setSelectionInterval(row, row);
						spectraTable.scrollRectToVisible(new Rectangle(spectraTable.getCellRect(specIndex.get(0), 0,
								false)));
					}
				}
			}
		}
	}
}
