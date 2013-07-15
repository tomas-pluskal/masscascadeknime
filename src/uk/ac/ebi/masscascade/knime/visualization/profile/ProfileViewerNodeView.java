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
package uk.ac.ebi.masscascade.knime.visualization.profile;

import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.core.node.NodeView;

import uk.ac.ebi.masscascade.alignment.ProfileBinTable;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.Labels.LABELS;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;

/**
 * <code>NodeView</code> for the "ProfileAligner" Node. Shows all grouped profiles within the specified m/z and rt
 * tolerance.
 * 
 * @author Stephan Beisken
 */
public class ProfileViewerNodeView extends NodeView<ProfileViewerNodeModel> {

	private final static Dimension PREF_DISPLAY_SIZE = new Dimension(900, 600);

	private ProfileBinTable table;
	private SimpleSpectrum chart;

	private ChartPanel chartPanel;
	private JSplitPane mainPane;
	private JScrollPane tablePane;

	private GraphColor graphColor;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel The model (class: {@link ProfileAlignerNodeModel})
	 */
	protected ProfileViewerNodeView(final ProfileViewerNodeModel nodeModel) {

		super(nodeModel);

		layoutView();

		graphColor = new GraphColor();
		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.SPLINE, false);
		chart.setDefaultTracePainter(tracePainter);

		this.setComponent(mainPane);
	}

	private void layoutView() {

		chart = new SimpleSpectrum();
		chartPanel = new ChartPanel(chart);

		table = new ProfileBinTable();
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				loadData(table.getSelectedRows());
			}
		});

		tablePane = new JScrollPane(table);
		tablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, tablePane);
		mainPane.setPreferredSize(PREF_DISPLAY_SIZE);
		mainPane.setResizeWeight(0.4);
		mainPane.setDividerLocation(300);
	}

	private void loadData(int[] selectedRows) {

		chart.clearData();
		graphColor.reset();

		Color color = null;
		
		for (int selectedRow : selectedRows) {
			
			if (selectedRows.length > 1) color = graphColor.nextColor();
			int index = table.convertRowIndexToModel(selectedRow);

			Map<String, Profile> profiles = getNodeModel().getModel().getProfilesForRow(index);
			for (Map.Entry<String, Profile> entry : profiles.entrySet()) {
				if (selectedRows.length == 1) color = graphColor.nextColor();
				XYList data = entry.getValue().getTrace().getData();
				chart.addData(new DataSet.Builder(data, entry.getKey()).color(color).build());
			}
		}

		chartPanel.revalidate();
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
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {

		if (getNodeModel().getModel() == null) return;
		table.setModel(getNodeModel().getModel());
		table.revalidate();

		chart.setAxisTitle(LABELS.RT_FULL.getLabel(), LABELS.INTENSITY.getLabel());
	}
}
