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
package uk.ac.ebi.masscascade.knime.visualization.featuretable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataRow;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableContentModel;
import org.knime.core.node.tableview.TableView;

import uk.ac.ebi.masscascade.core.chromatogram.MassChromatogram;
import uk.ac.ebi.masscascade.interfaces.Feature;
import uk.ac.ebi.masscascade.interfaces.FeatureSet;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.container.FeatureContainer;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureCell;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetCell;
import uk.ac.ebi.masscascade.knime.defaults.ViewerModel;
import uk.ac.ebi.masscascade.knime.visualization.InfiniteProgressPanel;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featureinfo.FeatureFrame;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featurequery.JOptionPaneOneInput;
import uk.ac.ebi.masscascade.knime.visualization.featuretable.featurequery.JOptionPaneTwoInput;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.tables.lazytable.util.LazyList;
import uk.ac.ebi.masscascade.tables.lazytable.util.SimpleLazyList;
import uk.ac.ebi.masscascade.tables.model.DetailFeatureTableModel;
import uk.ac.ebi.masscascade.tables.model.lazy.LazyProfileListPeer;
import uk.ac.ebi.masscascade.tables.model.lazy.LazyProfileTableModel;
import uk.ac.ebi.masscascade.tables.renderer.ScientificCellRenderer;
import uk.ac.ebi.masscascade.tables.renderer.XicCellRenderer;
import uk.ac.ebi.masscascade.utilities.FeatureUtils;
import uk.ac.ebi.masscascade.utilities.math.MathUtils;

/**
 * <code>NodeView</code> for the "PeakTable" Node. Visualizes a peak collection
 * as table.
 * 
 * @author Stephan Beisken
 */
public class FeatureTableNodeView extends NodeView<ViewerModel> {

	private final static Dimension PREF_DISPLAY_SIZE = new Dimension(800, 400);
	private final static int CELL_WIDTH = 280;

	private final TableView tableView;
	private final JTable profileTable;
	private final JSplitPane display;
	private final JScrollPane peakPane;

	private JMenuItem filterId;
	private JMenuItem filterMass;
	private JMenuItem filterReset;

	private InfiniteProgressPanel progressPanel;
	private Component parent;

	private int column;
	private int lastSelected;
	private FeatureContainer profileContainer;
	private FeatureSetContainer featureSetContainer;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link PeakTableNodeModel})
	 */
	protected FeatureTableNodeView(final ViewerModel nodeModel) {

		super(nodeModel);

		progressPanel = new InfiniteProgressPanel();
		// hack: add panel to glass pane of 'not visible' JFrame
		((JFrame) this.getComponent().getParent().getParent().getParent().getParent()).setGlassPane(progressPanel);
		parent = this.getComponent();

		getDataColumn(nodeModel);

		JMenu menu = new JMenu("Filter...");
		menu.setMnemonic(KeyEvent.VK_F);

		filterId = new JMenuItem("Filter by id");
		filterId.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		filterId.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JOptionPaneOneInput inputPane = new JOptionPaneOneInput();
				if (inputPane.getStatus() != JOptionPane.OK_OPTION) {
					return;
				}

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
				if (inputPane.getStatus() != JOptionPane.OK_OPTION) {
					return;
				}

				final double mass = inputPane.getMz();
				final double ppm = inputPane.getPpm();

				SwingUtilities.invokeLater(new RunMassSearch(mass, ppm));
			}
		});

		filterReset = new JMenuItem("Reset");
		filterReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		filterReset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				updatePeakList(lastSelected);
			}
		});

		menu.add(filterId);
		menu.add(filterMass);
		menu.add(filterReset);
		this.getJMenuBar().add(menu);

		tableView = new TableView(nodeModel.getContentModel());
		tableView.setPreferredSize(new Dimension(CELL_WIDTH, PREF_DISPLAY_SIZE.height / 2));
		tableView.getContentTable().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				lastSelected = tableView.getContentTable().getSelectedRow();
				updatePeakList(lastSelected);
			}
		});

		profileTable = new JTable();

		profileTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() != 2)
					return;

				int row = profileTable.rowAtPoint(e.getPoint());
				int profileId = (Integer) profileTable.getValueAt(row, 0);

				FeatureFrame infoFrame = new FeatureFrame("Feature Summary", featureSetContainer);
				if (profileContainer != null)
					infoFrame.setProfile(profileContainer.getFeature(profileId), null);
				else {
					for (FeatureSet spectrum : featureSetContainer) {
						if (spectrum.getFeature(profileId) != null)
							infoFrame.setProfile(spectrum.getFeature(profileId), spectrum.getIndex() + "");
					}
				}

				infoFrame.setLocationRelativeTo(parent);
				infoFrame.setSize(640, 480);
				infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				infoFrame.setVisible(true);
			}
		});

		peakPane = new JScrollPane(profileTable);
		peakPane.setBorder(BorderFactory.createTitledBorder("Peak List"));
		peakPane.setPreferredSize(new Dimension(CELL_WIDTH, PREF_DISPLAY_SIZE.height / 2));

		display = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, peakPane, tableView);
		display.setPreferredSize(PREF_DISPLAY_SIZE);
		display.setResizeWeight(1);
		display.setDividerLocation(0.7);

		setComponent(display);
	}

	/**
	 * Finds the column index of the data column.
	 */
	private void getDataColumn(ViewerModel nodeModel) {

		TableContentModel tableContentModel = nodeModel.getContentModel();

		String columnName = nodeModel.getSettings().getColumnName(Parameter.FEATURE_COLUMN);

		// hack to avoid "null" value in auto-configured data column
		if (columnName == null) {
			try {
				NodeUtils.getDataTableSpec(nodeModel.getContentModel().getDataTableSpec(), nodeModel.getSettings(),
						Parameter.FEATURE_COLUMN);
				columnName = nodeModel.getSettings().getColumnName(Parameter.FEATURE_COLUMN);
			} catch (InvalidSettingsException e) {
				e.printStackTrace();
			}
		}

		column = tableContentModel.getDataTable().getDataTableSpec().findColumnIndex(columnName);
	}

	/**
	 * Updates the peak list.
	 * 
	 * @param index row index of the selected file
	 */
	private void updatePeakList(int index) {

		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], index);
		LazyList<Object[]> lazyList;
		if (row.getCell(column).getType() == FeatureCell.TYPE)
			profileContainer = ((FeatureCell) row.getCell(column)).getPeakDataValue();
		else
			featureSetContainer = ((FeatureSetCell) row.getCell(column)).getFeatureSetDataValue();

		if (profileContainer != null && profileContainer.size() == 0)
			return;
		lazyList = new SimpleLazyList<Object[]>(10, new LazyProfileListPeer(
				featureSetContainer == null ? profileContainer : featureSetContainer));

		LazyProfileTableModel peakTable = new LazyProfileTableModel(lazyList, profileTable);
		profileTable.setModel(peakTable);
		profileTable.getColumnModel().getColumn(4).setCellRenderer(new ScientificCellRenderer());
		profileTable.getColumnModel().getColumn(5).setCellRenderer(new ScientificCellRenderer());
		profileTable.getColumnModel().getColumn(7).setCellRenderer(new XicCellRenderer());

		profileTable.revalidate();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
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

			progressPanel.start();
			Thread tableLoader = new Thread(new Runnable() {

				public void run() {

					Range range = MathUtils.getRangeFromPPM(mass, ppm);

					List<Feature> resultList = new ArrayList<Feature>();
					if (profileContainer != null) {
						for (Feature profile : profileContainer) {
							if (range.contains(profile.getMz()))
								resultList.add(profile);
						}
					} else {
						for (FeatureSet spectrum : featureSetContainer) {
							for (Feature profile : spectrum) {
								if (range.contains(profile.getMz()))
									resultList.add(profile);
							}
						}
					}

					updateProfileTable(resultList);

					progressPanel.stop();
				}
			}, "TableLoader");
			tableLoader.start();
		}
	}

	class RunIdSearch implements Runnable {

		private int id;

		public RunIdSearch(int id) {

			this.id = id;
		}

		public void run() {

			progressPanel.start();
			Thread tableLoader = new Thread(new Runnable() {

				public void run() {

					List<Feature> resultList = new ArrayList<Feature>();

					if (profileContainer != null)
						resultList.add(profileContainer.getFeature(id));
					else {
						for (FeatureSet spectrum : featureSetContainer) {
							if (spectrum.getFeature(id) != null)
								resultList.add(spectrum.getFeature(id));
						}
					}
					updateProfileTable(resultList);

					progressPanel.stop();
				}
			}, "TableLoader");
			tableLoader.start();
		}
	}

	private void updateProfileTable(List<Feature> resultList) {

		Object[][] tableData = new Object[resultList.size()][profileTable.getColumnCount()];
		int i = 0;
		for (Feature result : resultList) {

			MassChromatogram xic = (MassChromatogram) result.getTrace(Constants.PADDING);

			tableData[i][0] = result.getId();
			tableData[i][1] = result.getRetentionTime();
			tableData[i][2] = xic.getLast().x - xic.getData().get(0).x;
			tableData[i][3] = result.getMz();
			tableData[i][4] = xic.getDeviation();
			tableData[i][5] = result.getArea();
			tableData[i][6] = FeatureUtils.getProfileLabel(result);
			tableData[i][7] = xic;

			i++;
		}
		DetailFeatureTableModel peakTable = new DetailFeatureTableModel();
		peakTable.setData(tableData);

		profileTable.setModel(peakTable);
		profileTable.getColumnModel().getColumn(4).setCellRenderer(new ScientificCellRenderer());
		profileTable.getColumnModel().getColumn(5).setCellRenderer(new ScientificCellRenderer());
		profileTable.getColumnModel().getColumn(7).setCellRenderer(new XicCellRenderer());

		profileTable.revalidate();
	}
}
