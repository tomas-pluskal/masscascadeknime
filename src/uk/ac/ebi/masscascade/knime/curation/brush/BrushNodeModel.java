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
package uk.ac.ebi.masscascade.knime.curation.brush;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.openscience.cdk.knime.type.CDKCell;

import uk.ac.ebi.masscascade.alignment.featurebins.FeatureBinGenerator;
import uk.ac.ebi.masscascade.brush.FeatureSetCourt;
import uk.ac.ebi.masscascade.compound.CompoundSpectrum;
import uk.ac.ebi.masscascade.compound.CompoundSpectrumAdapter;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the model implementation of BlessTable.
 * 
 * @author Stephan Beisken
 */
public class BrushNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	private int gid;
	private int colIndex;
	private int groupIndex;

	/**
	 * Constructor for the node model.
	 */
	protected BrushNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		// retrieve column names and indices
		String colName = settings.getColumnName(Parameter.FEATURE_SET_COLUMN);
		colIndex = inData[0].getSpec().findColumnIndex(colName);
		String groupName = settings.getColumnName(Parameter.LABEL_COLUMN);
		groupIndex = inData[0].getSpec().findColumnIndex(groupName);
		gid = 0;

		// build parameter map
		ParameterMap params = new ParameterMap();
		params.put(Parameter.ELEMENT_FILTER, settings.getBooleanOption(Parameter.ELEMENT_FILTER));
		params.put(Parameter.ISOTOPE_FILTER, settings.getBooleanOption(Parameter.ISOTOPE_FILTER));
		params.put(Parameter.FRAGMENTATION_FILTER, settings.getBooleanOption(Parameter.FRAGMENTATION_FILTER));
		params.put(Parameter.RELATION_FILTER, settings.getBooleanOption(Parameter.RELATION_FILTER));

		// define settings
		double ppm = settings.getDoubleOption(Parameter.MZ_WINDOW_PPM);
		double sec = settings.getDoubleOption(Parameter.TIME_WINDOW);
		double missing = settings.getDoubleOption(Parameter.MISSINGNESS);

		// build the output data container
		BufferedDataContainer dataContainer = exec.createDataContainer(new DataTableSpec(
				createOutputTableSpecification()));

		// group input samples by their group
		Multimap<Integer, DataCell> groupToDataCells = HashMultimap.create();
		for (DataRow row : inData[0]) {
			DataCell groupCell = row.getCell(groupIndex);
			if (groupCell.isMissing()) {
				continue;
			}
			groupToDataCells.put(((IntValue) groupCell).getIntValue(), row.getCell(colIndex));
		}

		// container id to profile id map
		HashMultimap<Integer, Integer> cToPIdMap = null;
		// iterate over every group and process
		for (int group : groupToDataCells.keySet()) {

			Multimap<Integer, Container> spectraContainer = HashMultimap.create();
			for (DataCell spectrumCell : groupToDataCells.get(group)) {
				if (spectrumCell.isMissing()) {
					continue;
				}
				exec.checkCanceled();
				spectraContainer.put(group, ((FeatureSetValue) spectrumCell).getFeatureSetDataValue());
			}

			// bin profiles across spectrum containers
			cToPIdMap = FeatureBinGenerator.createContainerToFeatureMap(spectraContainer, ppm, sec, missing);
			int index = 0;

			// process spectrum cells of a group one by one using the
			// "cToPIdMap" from above
			BrushAggregator ba = new BrushAggregator();

			List<FeatureSetContainer> featureCs = new ArrayList<>();
			for (DataCell spectrumCell : groupToDataCells.get(group)) {

				if (spectrumCell.isMissing()) {
					continue;
				}

				featureCs.add(((FeatureSetValue) spectrumCell).getFeatureSetDataValue());
			}

			// tmp solution: possibly working, needs more testing
			Collections.sort(featureCs, new ContainerComparator());

			for (FeatureSetContainer featureC : featureCs) {

				exec.checkCanceled();

				CompoundSpectrumAdapter adapter = new CompoundSpectrumAdapter((int) (100 - missing) * 2);
				List<CompoundSpectrum> css = adapter.getSpectra(cToPIdMap, index++, featureC);

				// define filter criteria and run
				FeatureSetCourt court = new FeatureSetCourt(css);
				court.setParameters(params);
				css = court.call();

				// convert and consolidate remaining compound spectra
				ba.add(css);
			}

			// add to output container
			for (double mz : ba.mzs()) {
				Iterator<DataCell[]> iter = ba.rows(mz, group);
				while (iter.hasNext()) {
					DataCell[] dataCells = iter.next();
					dataContainer.addRowToTable(new DefaultRow(new RowKey(gid++ + ""), dataCells));
				}
			}
		}

		dataContainer.close();
		return new BufferedDataTable[] { dataContainer.getTable() };
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		createColumnSpec(dataColumnSpecs, "group", IntCell.TYPE);
		createColumnSpec(dataColumnSpecs, "mz", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "rt", ListCell.getCollectionType(DoubleCell.TYPE));
		createColumnSpec(dataColumnSpecs, "area", ListCell.getCollectionType(DoubleCell.TYPE));
		createColumnSpec(dataColumnSpecs, "name", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "molecule", CDKCell.TYPE);
		createColumnSpec(dataColumnSpecs, "avg score", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "status", StringCell.TYPE);

		return dataColumnSpecs.toArray(new DataColumnSpec[] {});
	}

	/**
	 * Creates a single column specification.
	 */
	private void createColumnSpec(List<DataColumnSpec> dataColumnSpecs, String colName, DataType cellType) {

		DataColumnSpec colSpec = new DataColumnSpecCreator(colName, cellType).createSpec();
		dataColumnSpecs.add(colSpec);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (settings.getOptionMapSize() == 0) {
			settings.setTextOption(Parameter.MZ_WINDOW_PPM, "" + Parameter.MZ_WINDOW_PPM.getDefaultValue());
			settings.setTextOption(Parameter.TIME_WINDOW, "" + Parameter.TIME_WINDOW.getDefaultValue());
			settings.setTextOption(Parameter.MISSINGNESS, "" + Parameter.MISSINGNESS.getDefaultValue());
			settings.setBooleanOption(Parameter.ELEMENT_FILTER, true);
		}
		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.FEATURE_SET_COLUMN);
		return new DataTableSpec[] { new DataTableSpec(createOutputTableSpecification()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.TIME_WINDOW);
		NodeUtils.validateDoubleGreaterOrEqualZero(tmpSettings, Parameter.MISSINGNESS);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_SET_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.LABEL_COLUMN);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to do
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing to do
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettings(settings);
	}

	@Override
	protected void reset() {
		// nothing to do
	}
}

class ContainerComparator implements Comparator<Container> {

	@Override
	public int compare(Container o1, Container o2) {
		return o1.getId().compareTo(o2.getId());
	}
}
