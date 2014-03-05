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
package uk.ac.ebi.masscascade.knime.visualization.featuretable.featureinfo;

import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
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
import uk.ac.ebi.masscascade.core.PropertyType;
import uk.ac.ebi.masscascade.interfaces.Chromatogram;
import uk.ac.ebi.masscascade.interfaces.Feature;
import uk.ac.ebi.masscascade.interfaces.FeatureSet;
import uk.ac.ebi.masscascade.interfaces.Property;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.visualization.GraphColor;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Constants.MSN;
import uk.ac.ebi.masscascade.properties.Adduct;
import uk.ac.ebi.masscascade.properties.Identity;
import uk.ac.ebi.masscascade.properties.Isotope;
import uk.ac.ebi.masscascade.utilities.AnnotationUtils;
import uk.ac.ebi.masscascade.utilities.DataSet;
import uk.ac.ebi.masscascade.utilities.FeatureUtils;
import uk.ac.ebi.masscascade.utilities.math.MathUtils;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Class implementing a feature information frame summarising all relevant
 * feature information in a feature-centric view.
 * 
 * @author Stephan Beisken
 */
public class FeatureFrame extends JFrame {

	private Feature feature;
	private final FeatureSetContainer container;

	private JLabel idLabel;
	private JLabel rtLabel;
	private JLabel mzLabel;
	private JLabel areaLabel;
	private JComboBox<String> msnLabel;
	private JLabel spectrumLabel;

	private JPanel profilePanel;
	private SimpleSpectrum chromatrogam;
	private SimpleSpectrum msnSpectrum;

	private JTable adductTable;
	private JTable isotopeTable;
	private JTable fragmentTable;
	private JTable identityTable;

	private MoleculePanel moleculePanel;

	private GraphColor graphColorTraces;

	/**
	 * Constructs the feature frame.
	 * 
	 * @param title the title of the frame
	 * @param container the featureSet container
	 */
	public FeatureFrame(String title, FeatureSetContainer container) {

		super(title);
		this.container = container;
		this.setLayout(new GridLayout(2, 1));
		this.setPreferredSize(new Dimension(800, 600));

		graphColorTraces = new GraphColor();

		layoutNorth();
		layoutSouth();
	}

	/**
	 * Sets the feature to be highlighted.
	 * 
	 * @param feature the feature
	 */
	public void setProfile(Feature feature, String spectrumId) {

		this.feature = feature;
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
		infoPanel.setBorder(BorderFactory.createTitledBorder("Feature"));
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
		msnLabel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				String msnString = (String) msnLabel.getSelectedItem();
				msnString = msnString.substring(2, 3);
				Constants.MSN msn = Constants.MSN.get(msnString);

				if (feature.hasMsnSpectra(MSN.MS2)) {

					FeatureSet msnSpec = feature.getMsnSpectra(msn).get(0);
					XYList data = msnSpec.getData();
					Map<XYPoint, String> annotations = AnnotationUtils
							.getAnnotations(msnSpec.getFeaturesMap().values());
					Collections.sort(data);

					msnSpectrum.clearData();
					msnSpectrum.addData(new DataSet.Builder(data, ""
							+ MathUtils.roundToThreeDecimals(msnSpec.getRetentionTime())).color(Color.BLUE)
							.annotations(annotations).build());
					msnSpectrum.updateTraceRanges(true);
				}
				profilePanel.repaint();
			}
		});
		infoPanel.add(msnLabel, c);
		c.gridx = 0;
		c.gridy++;

		infoPanel.add(new JLabel("FeatureSet: "), c);
		c.gridx = 1;
		spectrumLabel = new JLabel();
		infoPanel.add(spectrumLabel, c);
		c.gridx = 0;
		c.gridy++;

		profilePanel = new JPanel(new GridLayout(1, 2));

		chromatrogam = new SimpleSpectrum();
		chromatrogam.setAxisTitle("", "");
		ChartPanel chart = new ChartPanel(chromatrogam);
		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainter = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainter.put(PAINTERS.SPLINE, false);
		tracePainter.put(PAINTERS.DISC, false);
		chromatrogam.setDefaultTracePainter(tracePainter);
		profilePanel.add(chart);

		msnSpectrum = new SimpleSpectrum();
		msnSpectrum.setAxisTitle("", "");
		ChartPanel chartSpectrum = new ChartPanel(msnSpectrum);
		Map<SimpleSpectrum.PAINTERS, Boolean> tracePainterSpectrum = new HashMap<SimpleSpectrum.PAINTERS, Boolean>();
		tracePainterSpectrum.put(PAINTERS.BAR, false);
		tracePainterSpectrum.put(PAINTERS.LABEL, false);
		msnSpectrum.setDefaultTracePainter(tracePainterSpectrum);
		profilePanel.add(chartSpectrum);

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
				Identity tmpIdentity = ((IdentityTableModel) identityTable.getModel()).getIdentityAt(modelRow);
				String notation = tmpIdentity.getNotation();
				moleculePanel.drawMolecule(notation);
				moleculePanel.revalidate();

				String msnString = (String) msnLabel.getSelectedItem();
				if (msnString != null) {
					msnString = msnString.substring(2);
					Constants.MSN msn = Constants.MSN.get(msnString);

					Multimap<Double, Identity> msnMassToIdentity = HashMultimap.create();
					Set<String> duplicates = new HashSet<>();
					for (Feature msnProfile : feature.getMsnSpectra(msn).get(0)) {
						if (!msnProfile.hasProperty(PropertyType.Identity))
							continue;

						for (Identity identity : msnProfile.getProperty(PropertyType.Identity, Identity.class)) {
							if (tmpIdentity.getId().contains(identity.getSource())) {
								String dupString = msnProfile.getMz() + "-" + identity.getNotation();
								if (duplicates.contains(dupString))
									continue;
								duplicates.add(dupString);
								msnMassToIdentity.put(msnProfile.getMz(), (Identity) identity);
							}
						}
					}

					((FragmentTableModel) fragmentTable.getModel()).setDataList(msnMassToIdentity);
				}
				TableRowSorter<FragmentTableModel> sorter = new TableRowSorter<FragmentTableModel>();
				List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
				sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
				sorter.setModel((FragmentTableModel) fragmentTable.getModel());
				sorter.setSortKeys(sortKeys);
				fragmentTable.setRowSorter(sorter);
				fragmentTable.revalidate();
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

		fragmentTable = new JTable(new FragmentTableModel());
		fragmentTable.setFillsViewportHeight(true);
		fragmentTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				int row = fragmentTable.rowAtPoint(e.getPoint());
				String notation = (String) fragmentTable.getValueAt(row, 1);
				moleculePanel.drawMolecule(notation);
				moleculePanel.revalidate();
			}
		});
		JScrollPane fragmentSp = new JScrollPane(fragmentTable);
		fragmentSp.setBackground(Color.WHITE);
		fragmentSp.setBorder(BorderFactory.createTitledBorder("Fragments"));

		JPanel middleSplitPanel = new JPanel(new GridLayout(2, 1));
		middleSplitPanel.add(isotopeSp);
		middleSplitPanel.add(fragmentSp);

		southPanel.add(middleSplitPanel);

		adductTable = new JTable(new AdductTableModel());
		adductTable.setFillsViewportHeight(true);
		adductTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (adductTable.getSelectedRow() != -1 || adductTable.getSelectedRow() < adductTable.getRowCount())
					loadAdductData(adductTable, adductTable.getSelectedRows());
			}
		});
		JScrollPane adductSp = new JScrollPane(adductTable);
		adductSp.setBackground(Color.WHITE);
		adductSp.setBorder(BorderFactory.createTitledBorder("Adducts"));

		JPanel splitPanel = new JPanel(new GridLayout(2, 1));
		moleculePanel = new MoleculePanel();
		moleculePanel.setBorder(BorderFactory.createTitledBorder("Molecule"));
		splitPanel.add(adductSp);
		splitPanel.add(moleculePanel);

		southPanel.add(splitPanel);

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

			Iterator<FeatureSet> spectrumIterator = container.iterator();
			FeatureSet featureSet = null;
			while (spectrumIterator.hasNext()) {
				featureSet = spectrumIterator.next();
				if (featureSet.getFeature(id) != null)
					break;
			}
			Feature feature = featureSet.getFeature(id);
			Chromatogram trace = feature.getTrace(Constants.PADDING);
			Color color = graphColorTraces.nextColor();
			DataSet dataSet = new DataSet.Builder(trace.getData(), "" + feature.getId()).color(color).build();
			chromatrogam.addData(dataSet);
		}

		profilePanel.repaint();
	}

	/**
	 * Loads the selected profiles into the chart.
	 * 
	 * @param table the currently selected table
	 * @param selectedRows the selected rows in the table
	 */
	private void loadAdductData(JTable table, int... selectedRows) {

		chromatrogam.clearData();
		graphColorTraces.reset();

		for (int selectedRow : selectedRows) {
			int pid = Integer.parseInt("" + table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 1));
			int did = Integer.parseInt("" + table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 2));

			Iterator<FeatureSet> spectrumIterator = container.iterator();
			FeatureSet featureSet = null;
			while (spectrumIterator.hasNext()) {
				featureSet = spectrumIterator.next();
				if (featureSet.getFeature(pid) != null)
					break;
			}
			Feature pFeature = featureSet.getFeature(pid);
			Chromatogram pTrace = pFeature.getTrace(Constants.PADDING);
			Color pColor = graphColorTraces.nextColor();
			DataSet pDataSet = new DataSet.Builder(pTrace.getData(), "" + pFeature.getId()).color(pColor).build();
			chromatrogam.addData(pDataSet);

			Feature dFeature = featureSet.getFeature(did);
			Chromatogram dTrace = dFeature.getTrace(Constants.PADDING);
			Color dColor = graphColorTraces.nextColor();
			DataSet dDataSet = new DataSet.Builder(dTrace.getData(), "" + dFeature.getId()).color(dColor).build();
			chromatrogam.addData(dDataSet);
		}

		profilePanel.repaint();
	}

	/**
	 * Updates the window content when a new instance of the frame is called.
	 */
	private void fireUpdate(String id) {

		idLabel.setText("" + feature.getId());
		mzLabel.setText(MathUtils.THREE_DECIMAL_FORMAT.format(feature.getMzIntDp().x));
		rtLabel.setText(MathUtils.THREE_DECIMAL_FORMAT.format(feature.getRetentionTime()));
		areaLabel.setText(MathUtils.SCIENTIFIC_FORMAT.format(feature.getArea()));
		Map<Constants.MSN, Set<Integer>> msnMap = feature.getMsnScans();
		String msnIds = "";
		for (Constants.MSN msn : msnMap.keySet()) {
			for (int childId : msnMap.get(msn)) {
				msnIds = msn.name() + " = id:" + childId + " ";
				msnLabel.addItem(msnIds);
			}
		}

		if (id != null)
			spectrumLabel.setText(id);

		DataSet profileData = new DataSet.Builder(feature.getPaddedData(Constants.PADDING).getXZSlice(), "").color(
				Color.BLUE).build();
		chromatrogam.addData(profileData);
		profilePanel.repaint();

		Set<Identity> identities = feature.getProperty(PropertyType.Identity, Identity.class);
		((IdentityTableModel) identityTable.getModel()).setDataList(FeatureUtils.getGroupedIdentities(identities));

		TableRowSorter<IdentityTableModel> sorter = new TableRowSorter<IdentityTableModel>();
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setModel((IdentityTableModel) identityTable.getModel());
		sorter.setSortKeys(sortKeys);
		identityTable.setRowSorter(sorter);
		identityTable.revalidate();

		if (container != null) {

			Iterator<FeatureSet> spectrumIterator = container.iterator();
			FeatureSet featureSet = null;
			while (spectrumIterator.hasNext()) {
				featureSet = spectrumIterator.next();
				if (featureSet.getFeature(feature.getId()) != null)
					break;
			}
			if (featureSet != null) {
				loadSpectrumInfo(featureSet);
			}
		} else {
			Set<Isotope> isotopes = feature.getProperty(PropertyType.Isotope, Isotope.class);
			((IsotopeTableModel) isotopeTable.getModel()).setDataList(isotopes);

			Set<Adduct> adducts = feature.getProperty(PropertyType.Adduct, Adduct.class);
			((AdductTableModel) adductTable.getModel()).setDataList(adducts);
		}
	}

	/**
	 * Updates the individual property tables for the given featureSet.
	 * 
	 * @param featureSet the featureSet to be loaded
	 */
	private void loadSpectrumInfo(FeatureSet featureSet) {

		Set<Isotope> isotopes = feature.getProperty(PropertyType.Isotope, Isotope.class);
		Set<Integer> profileIsoParents = new HashSet<Integer>();
		for (Isotope prop : isotopes) {
			profileIsoParents.add(prop.getParentId());
		}

		Set<Adduct> adducts = feature.getProperty(PropertyType.Adduct, Adduct.class);
		Set<Integer> profileAdductRefs = new HashSet<Integer>();

		for (Adduct prop : adducts) {
			int pi = prop.getParentId();
			profileAdductRefs.add(pi);
		}

		Feature specProfile;
		List<Property> isoPropList = new ArrayList<>();
		List<Property> aduPropList = new ArrayList<>();

		for (int profileId : featureSet.getFeaturesMap().keySet()) {

			specProfile = featureSet.getFeature(profileId);
			if (profileIsoParents.contains(profileId)) {
				for (Isotope prop : specProfile.getProperty(PropertyType.Isotope, Isotope.class)) {
					isoPropList.add(prop);
				}
			}

			if (profileAdductRefs.contains(specProfile.getId())) {
				for (Adduct prop : specProfile.getProperty(PropertyType.Adduct, Adduct.class)) {
					aduPropList.add(prop);
				}
			}
		}

		((IsotopeTableModel) isotopeTable.getModel()).setDataList(isoPropList);
		TableRowSorter<IsotopeTableModel> isoSorter = new TableRowSorter<IsotopeTableModel>();
		List<RowSorter.SortKey> isoSortKeys = new ArrayList<RowSorter.SortKey>();
		isoSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		isoSorter.setModel((IsotopeTableModel) isotopeTable.getModel());
		isoSorter.setSortKeys(isoSortKeys);
		isotopeTable.setRowSorter(isoSorter);
		isotopeTable.revalidate();

		((AdductTableModel) adductTable.getModel()).setDataList(aduPropList);
	}
}
