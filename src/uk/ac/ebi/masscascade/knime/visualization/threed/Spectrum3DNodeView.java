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
package uk.ac.ebi.masscascade.knime.visualization.threed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import org.knime.core.data.DataRow;
import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableContentModel;
import org.knime.core.node.tableview.TableView;

import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsCell;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>NodeView</code> for the "MsViewer" Node. Visualises the data (scans and meta information) of the selected mass
 * spectrometry run.
 * 
 * @author Stephan Beisken
 */
@Deprecated
public class Spectrum3DNodeView extends NodeView<Spectrum3DNodeModel> {

	private TableView tableView;
	private int msDataCol;
	private JSplitPane graphicsPanel;
//	private JScrollPane tablePane;
	private JMenu msnMenu;
	private List<JMenuItem> msnLevelItems;
//	private int xRes, yRes;
	private int lastSelected;
//	private Ploót3DSurface display;
	private ScanContainer rawContainer;
	private final Settings settings;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link Spectrum3DNodeModel})
	 */
	protected Spectrum3DNodeView(final Spectrum3DNodeModel nodeModel) {

		super(nodeModel);

		settings = nodeModel.getSettings();
		getDataColumns(nodeModel);

		layoutTableView(nodeModel);
		layoutMenuBar();

//		display = new Plot3DSurface();

//		graphicsPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, display, tablePane);
		graphicsPanel.setEnabled(false);
		graphicsPanel.setResizeWeight(0.75);
		graphicsPanel.setDividerLocation(350);

		this.setComponent(graphicsPanel);
	}

	/**
	 * Finds the column index of the MS data and peak column.
	 */
	private void getDataColumns(Spectrum3DNodeModel nodeModel) {

		TableContentModel tableContentModel = nodeModel.getContentModel();

		msDataCol = tableContentModel.getDataTable().getDataTableSpec()
				.findColumnIndex(settings.getColumnName(Parameter.DATA_COLUMN));
	}

	/**
	 * Configures the MS data view.
	 */
	private void layoutTableView(Spectrum3DNodeModel nodeModel) {

		tableView = new TableView(nodeModel.getContentModel());
		tableView.getContentTable().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {

				loadRunData(tableView.getContentTable().getSelectedRow(), Constants.MSN.MS1);
			}
		});

//		tablePane = new JScrollPane(tableView);
	}

	/**
	 * Configures the menu bar.
	 */
	private void layoutMenuBar() {

		JMenuBar menuBar = getJMenuBar();
		menuBar.add(tableView.createHiLiteMenu());
		menuBar.add(tableView.createViewMenu());

		msnMenu = new JMenu("MSn Level");
		menuBar.add(msnMenu);

		msnLevelItems = new ArrayList<JMenuItem>();
	}

	/**
	 * Loads the 3D MS data.
	 */
	private void loadRunData(int rowIndex, Constants.MSN level) {

		DataRow row = NodeUtils.getDataRow(getNodeModel().getInternalTables()[0], rowIndex);

		rawContainer = ((MsCell) row.getCell(msDataCol)).getMsDataValue();
		lastSelected = rowIndex;

		updateMsnLevelItems();

//		xRes = settings.getIntOption(Parameter.TIME_RESOLUTION);
//		yRes = settings.getIntOption(Parameter.MASS_RESOLUTION);

//		display.setData(getBinnedData(level));

		graphicsPanel.validate();
		graphicsPanel.repaint();
	}

	/**
	 * Returns the binned data needed for the 3D plot.
	 */
//	private Binned2DData getBinnedData(Constants.MSN level) {

//		Unbinned3DDataImpl unbinned3DData = new Unbinned3DDataImpl();

//		for (Scan scan : rawContainer) {
//			for (XYPoint dataPoint : scan.getData()) {
//				unbinned3DData.addDataPoint(new Point3f((float) scan.getRetentionTime(), (float) dataPoint.x,
//						(float) dataPoint.y));
//			}
//			scan = null;
//		}
//
//		Binned2DData binned2dData = unbinned3DData.getBinned2DData(xRes, yRes);
//		unbinned3DData = null;
//
//		return binned2dData;
//	}

	/**
	 * Updates the MSn Menu items based on the raw data.
	 */
	private void updateMsnLevelItems() {

		for (JMenuItem item : msnLevelItems) {
			msnMenu.remove(item);
		}
		for (int i = 1; i <= rawContainer.getScanLevels().size(); i++) {
			JMenuItem tmpItem = new JMenuItem("MS Level " + i);
			tmpItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					loadRunData(
							lastSelected,
							Constants.MSN.get(Integer.parseInt(((JMenuItem) e.getSource()).getText().substring(
									"MS Level ".length()))));
				}
			});
			msnMenu.add(tmpItem);
			msnLevelItems.add(tmpItem);
		}
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
}
