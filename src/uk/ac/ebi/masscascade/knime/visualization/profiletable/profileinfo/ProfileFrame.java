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
package uk.ac.ebi.masscascade.knime.visualization.profiletable.profileinfo;

import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import uk.ac.ebi.masscascade.charts.SimpleSpectrum;
import uk.ac.ebi.masscascade.charts.SimpleSpectrum.PAINTERS;
import uk.ac.ebi.masscascade.core.PropertyManager;
import uk.ac.ebi.masscascade.interfaces.Chromatogram;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.interfaces.Property;
import uk.ac.ebi.masscascade.interfaces.Spectrum;
import uk.ac.ebi.masscascade.interfaces.container.SpectrumContainer;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.properties.Adduct;
import uk.ac.ebi.masscascade.properties.Isotope;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.math.MathUtils;

/**
 * Class implementing a profile information frame summarising all relevant profile information in a profile-centric
 * view.
 * 
 * @author Stephan Beisken
 */
public class ProfileFrame extends JFrame {

	private Profile profile;
	private final SpectrumContainer container;

	private JLabel idLabel;
	private JLabel rtLabel;
	private JLabel mzLabel;
	private JLabel areaLabel;
	private JComboBox<String> msnLabel;
	private JLabel spectrumLabel;

	private JPanel profilePanel;
	private SimpleSpectrum chromatrogam;

	private JTable adductTable;
	private JTable isotopeTable;
	private JTable identityTable;

	private GraphColor graphColorTraces;

	/**
	 * Constructs the profile frame.
	 * 
	 * @param title the title of the frame
	 * @param container the spectrum container
	 */
	public ProfileFrame(String title, SpectrumContainer container) {

		super(title);
		this.container = container;
		this.setLayout(new GridLayout(2, 1));

		graphColorTraces = new GraphColor();

		layoutNorth();
		layoutSouth();
	}

	/**
	 * Sets the profile to be highlighted.
	 * 
	 * @param profile the profile
	 */
	public void setProfile(Profile profile, String spectrumId) {

		this.profile = profile;
		fireUpdate(spectrumId);
	}

	/**
	 * Layouts the upper part of the frame.
	 */
	private void layoutNorth() {

		JPanel northPanel = new JPanel(new GridBagLayout());
		northPanel.setBackground(Color.WHITE);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = 0;
		c.gridy = 0;

		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBorder(BorderFactory.createTitledBorder("Profile"));
		infoPanel.setBackground(Color.WHITE);

		infoPanel.add(new JLabel("Id: "), c);
		c.gridx = 1;
		idLabel = new JLabel();
		infoPanel.add(idLabel, c);
		c.gridx = 0;
		c.gridy++;

		infoPanel.add(new JLabel("m/z: "), c);
		c.gridx = 1;
		mzLabel = new JLabel();
		infoPanel.add(mzLabel, c);
		c.gridx = 0;
		c.gridy++;

		infoPanel.add(new JLabel("rt: "), c);
		c.gridx = 1;
		rtLabel = new JLabel();
		infoPanel.add(rtLabel, c);
		c.gridx = 0;
		c.gridy++;

		infoPanel.add(new JLabel("Area: "), c);
		c.gridx = 1;
		areaLabel = new JLabel();
		infoPanel.add(areaLabel, c);
		c.gridx = 0;
		c.gridy++;
		
		infoPanel.add(new JLabel("MSn IDs: "), c);
		c.gridx = 1;
		msnLabel = new JComboBox<String>();
		msnLabel.setBorder(null);
		infoPanel.add(msnLabel, c);
		c.gridx = 0;
		c.gridy++;
		
		infoPanel.add(new JLabel("Spectrum: "), c);
		c.gridx = 1;
		spectrumLabel = new JLabel();
		infoPanel.add(spectrumLabel, c);
		c.gridx = 0;
		c.gridy++;

		profilePanel = new JPanel(new GridLayout(1, 1));

		chromatrogam = new SimpleSpectrum();
		chromatrogam.setAxisTitle("", "");
		ChartPanel chart = new ChartPanel(chromatrogam);
		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.POLY, false);
		tracePainter.put(PAINTERS.DISC, false);
		chromatrogam.setDefaultTracePainter(tracePainter);
		profilePanel.add(chart);

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 10, 0, 10);
		c.fill = GridBagConstraints.BOTH;

		northPanel.add(infoPanel, c);

		c.gridx = 1;
		c.weightx = 2;
		c.weighty = 2;
		c.insets = new Insets(0, 0, 0, 0);

		northPanel.add(profilePanel, c);

		add(northPanel);
	}

	/**
	 * Layouts the lower part of the frame.
	 */
	private void layoutSouth() {

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(1, 3));

		identityTable = new JTable(new IdentityTableModel());
		identityTable.setFillsViewportHeight(true);
		identityTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				int row = identityTable.rowAtPoint(e.getPoint());
				int modelRow = identityTable.convertRowIndexToModel(row);
				boolean current = (Boolean) identityTable.getModel().getValueAt(modelRow, 2);
				identityTable.getModel().setValueAt(!current, modelRow, 2);
				identityTable.revalidate();
			}
		});
		JScrollPane identitySp = new JScrollPane(identityTable);
		identitySp.setBackground(Color.WHITE);
		identitySp.setBorder(BorderFactory.createTitledBorder("Identities"));
		southPanel.add(identitySp);

		isotopeTable = new JTable(new IsotopeTableModel());
		isotopeTable.setFillsViewportHeight(true);
		isotopeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (isotopeTable.getSelectedRow() != -1 || isotopeTable.getSelectedRow() < isotopeTable.getRowCount())
					loadData(isotopeTable, isotopeTable.getSelectedRows());
			}
		});
		JScrollPane isotopeSp = new JScrollPane(isotopeTable);
		isotopeSp.setBackground(Color.WHITE);
		isotopeSp.setBorder(BorderFactory.createTitledBorder("Isotopes"));
		southPanel.add(isotopeSp);

		adductTable = new JTable(new AdductTableModel());
		adductTable.setFillsViewportHeight(true);
		adductTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (adductTable.getSelectedRow() != -1 || adductTable.getSelectedRow() < adductTable.getRowCount())
					loadData(adductTable, adductTable.getSelectedRows());
			}
		});
		JScrollPane adductSp = new JScrollPane(adductTable);
		adductSp.setBackground(Color.WHITE);
		adductSp.setBorder(BorderFactory.createTitledBorder("Adducts"));
		southPanel.add(adductSp);

		add(southPanel);
	}

	/**
	 * Loads the selected profiles into the chart.
	 * 
	 * @param table the currently selected table
	 * @param selectedRows the selected rows in the table
	 */
	private void loadData(JTable table, int... selectedRows) {

		chromatrogam.clearData();
		graphColorTraces.reset();

		for (int selectedRow : selectedRows) {
			int id = Integer.parseInt("" + table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 2));

			Iterator<Spectrum> spectrumIterator = container.iterator();
			Spectrum spectrum = null;
			while (spectrumIterator.hasNext()) {
				spectrum = spectrumIterator.next();
				if (spectrum.getProfile(id) != null)
					break;
			}
			Chromatogram trace = spectrum.getProfile(id).getTrace(Constants.PADDING);
			Color color = graphColorTraces.nextColor();
			DataSet dataSet = new DataSet.Builder(trace.getData(), "" + spectrum.getProfile(id).getId()).color(color)
					.build();
			chromatrogam.addData(dataSet);
		}

		profilePanel.repaint();
	}

	/**
	 * Updates the window content when a new instance of the frame is called.
	 */
	private void fireUpdate(String id) {

		idLabel.setText("" + profile.getId());
		mzLabel.setText(MathUtils.THREE_DECIMAL_FORMAT.format(profile.getMzIntDp().x));
		rtLabel.setText(MathUtils.THREE_DECIMAL_FORMAT.format(profile.getRetentionTime()));
		areaLabel.setText(MathUtils.SCIENTIFIC_FORMAT.format(profile.getArea()));
		Map<Integer, Set<Integer>> msnMap = profile.getMsnScans();
		String msnIds = "";
		for (int msn : msnMap.keySet()) {
			for (int childId : msnMap.get(msn)) {
				msnIds = "MSn:" + (msn + 2) + " = id:" + childId + " ";
				msnLabel.addItem(msnIds);
			}
		}

		if (id != null) spectrumLabel.setText(id);

		DataSet profileData = new DataSet.Builder(profile.getPaddedData(Constants.PADDING).getXZSlice(), "").color(Color.BLUE).build();
		chromatrogam.addData(profileData);
		profilePanel.repaint();

		Set<Property> identities = profile.getProperty(PropertyManager.TYPE.Identity);
		((IdentityTableModel) identityTable.getModel()).setDataList(identities);

		TableRowSorter<IdentityTableModel> sorter = new TableRowSorter<IdentityTableModel>();
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setModel((IdentityTableModel) identityTable.getModel());
		sorter.setSortKeys(sortKeys);
		identityTable.setRowSorter(sorter);
		identityTable.revalidate();

		if (container != null) {

			Iterator<Spectrum> spectrumIterator = container.iterator();
			Spectrum spectrum = null;
			while (spectrumIterator.hasNext()) {
				spectrum = spectrumIterator.next();
				if (spectrum.getProfile(profile.getId()) != null)
					break;
			}
			if (spectrum != null) {
				loadSpectrumInfo(spectrum);
			}
		} else {
			Set<Property> isotopes = profile.getProperty(PropertyManager.TYPE.Isotope);
			((IsotopeTableModel) isotopeTable.getModel()).setDataList(isotopes);

			Set<Property> adducts = profile.getProperty(PropertyManager.TYPE.Adduct);
			((AdductTableModel) adductTable.getModel()).setDataList(adducts);
		}
	}

	/**
	 * Updates the individual property tables for the given spectrum.
	 * 
	 * @param spectrum the spectrum to be loaded
	 */
	private void loadSpectrumInfo(Spectrum spectrum) {

		Set<Property> isotopes = profile.getProperty(PropertyManager.TYPE.Isotope);
		Set<Integer> profileIsoParents = new HashSet<Integer>();
		for (Property prop : isotopes) {
			profileIsoParents.add(((Isotope) prop).getParentId());
		}

		Set<Property> adducts = profile.getProperty(PropertyManager.TYPE.Adduct);
		Set<Integer> profileAdductRefs = new HashSet<Integer>();
		profileAdductRefs.add(profile.getId());
		for (Property prop : adducts) {
			profileAdductRefs.add(((Adduct) prop).getParentId());
			profileAdductRefs.add(((Adduct) prop).getChildId());
		}

		Profile specProfile;
		List<Property> isoPropList = new ArrayList<Property>();
		List<Property> aduPropList = new ArrayList<Property>();

		for (int profileId : spectrum.getProfileMap().keySet()) {

			specProfile = spectrum.getProfile(profileId);
			for (Property prop : specProfile.getProperty(PropertyManager.TYPE.Isotope)) {
				if (((Isotope) prop).getParentId() == profile.getId()
						|| profileIsoParents.contains(((Isotope) prop).getParentId())) {
					isoPropList.add(prop);
				}
			}

			for (Property prop : specProfile.getProperty(PropertyManager.TYPE.Adduct)) {
				if (profileAdductRefs.contains(((Adduct) prop).getParentId())
						|| profileAdductRefs.contains(((Adduct) prop).getChildId())) {
						aduPropList.add(prop);
				}
			}
		}

		((IsotopeTableModel) isotopeTable.getModel()).setDataList(isoPropList);
		((AdductTableModel) adductTable.getModel()).setDataList(aduPropList);
	}
}
