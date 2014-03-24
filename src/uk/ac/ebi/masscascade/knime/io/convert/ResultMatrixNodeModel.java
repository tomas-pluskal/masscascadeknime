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
package uk.ac.ebi.masscascade.knime.io.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.alignment.FeatureBinTableModel;
import uk.ac.ebi.masscascade.alignment.featurebins.FeatureBin;
import uk.ac.ebi.masscascade.alignment.featurebins.FeatureBinFiller;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.ScanContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featurecell.FeatureValue;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.datatypes.mscell.MsCell;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.TextUtils;
import uk.ac.ebi.masscascade.utilities.comparator.FeatureBinTimeComparator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the model implementation of the "ResultMatrix" node to build the m/z
 * to sample matrix.
 * 
 * @author Stephan Beisken
 */
public class ResultMatrixNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();
	private List<String> profileContainerIds = new ArrayList<>();

	/**
	 * Constructor for the node model.
	 */
	protected ResultMatrixNodeModel() {
		super(NodeUtils.createOPOs(2, 2), NodeUtils.createOPOs(1));
	}

	private final static double DEFAULT_RT_DELTA = 2.5;
	private final static double DEFAULT_PPM_DELTA = 500;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		Multimap<Integer, Container> profileContainers = HashMultimap.create();
		List<ScanContainer> rawContainers = new ArrayList<ScanContainer>();

		String colName = settings.getColumnName(Parameter.FEATURE_COLUMN) == null ? settings
				.getColumnName(Parameter.FEATURE_SET_COLUMN) : settings.getColumnName(Parameter.FEATURE_COLUMN);
		int colIndex = inData[0].getDataTableSpec().findColumnIndex(colName);

		String colNamRaw = settings.getColumnName(Parameter.DATA_COLUMN);
		int colIndexRaw = inData[0].getDataTableSpec().findColumnIndex(colNamRaw);

		String colLabel = settings.getColumnName(Parameter.LABEL_COLUMN);
		int colIndexLabel = inData[0].getDataTableSpec().findColumnIndex(colLabel);

		for (DataRow row : inData[0]) {
			DataCell cell = row.getCell(colIndex);
			DataCell cellRaw = row.getCell(colIndexRaw);
			DataCell groupCell = row.getCell(colIndexLabel);
			if (cell.isMissing() || cellRaw.isMissing())
				continue;

			if (cell instanceof FeatureValue) {
				profileContainers.put(((IntValue) groupCell).getIntValue(), ((FeatureValue) cell).getPeakDataValue());
			} else {
				profileContainers.put(((IntValue) groupCell).getIntValue(),
						((FeatureSetValue) cell).getFeatureSetDataValue());
			}
			rawContainers.add(((MsCell) cellRaw).getMsDataValue());
		}
		
		double ppm = settings.getDoubleOption(Parameter.MZ_WINDOW_PPM);
		double sec = settings.getDoubleOption(Parameter.TIME_WINDOW);
		double missing = settings.getDoubleOption(Parameter.MISSINGNESS);
		boolean gapFill = settings.getBooleanOption(Parameter.GAP_FILL);
		double defaultValue = settings.getDoubleOption(Parameter.DEFAULT);

		FeatureBinTableModel model = new FeatureBinTableModel(profileContainers, ppm, sec, missing);
		
		profileContainerIds = new ArrayList<>();
		for (int groupId : profileContainers.keySet()) {
			List<Container> featureCs = new ArrayList<>(profileContainers.get(groupId));
            // tmp solution: possibly working, needs more testing
            Collections.sort(featureCs, new ContainerComparator());
			for (Container orderedContainer : featureCs) {
				profileContainerIds.add(orderedContainer.getId());
			}
		}

		BufferedDataContainer dataContainer = exec.createDataContainer(new DataTableSpec(
				createOutputTableSpecification()));

		int id = 1;
		DataCell[] cellRow;
		List<FeatureBin> rows = model.getRows();
		Collections.sort(rows, new FeatureBinTimeComparator());
		Multimap<Integer, Integer> containerToRowId = HashMultimap.create();
		int rowI = 0;
		for (FeatureBin row : rows) {
			boolean isComplete = true;
			cellRow = new DataCell[model.getColumnCount() - 1];
			for (int i = 6; i < model.getColumnCount(); i++) {
				double intensity = row.isPresent(i - FeatureBin.COLUMNS);
				if (intensity > 0) {
					cellRow[i - 1] = new DoubleCell(intensity);
				} else {
					if (gapFill) {
						containerToRowId.put(i - FeatureBin.COLUMNS, rowI);
						isComplete = false;
					} else
						cellRow[i - 1] = DataType.getMissingCell();
				}
			}

			rowI++;

			if (isComplete) {
				cellRow[0] = new DoubleCell(row.getMz());
				cellRow[1] = new DoubleCell(row.getRt());
				cellRow[2] = new DoubleCell(row.getArea());
				cellRow[3] = new StringCell(row.getLabel());
				cellRow[4] = new DoubleCell(row.getMzDev());
				dataContainer.addRowToTable(new DefaultRow(new RowKey(id++ + ""), cellRow));
			}
		}

		Map<String, TreeMap<Double, Double>> containerToTimeToShift = new HashMap<>();
		if (inData[1] != null) {
			for (DataRow row : inData[1]) {
				String containerId = ((StringValue) row.getCell(0)).getStringValue();
				double shift = ((DoubleCell) row.getCell(2)).getDoubleValue();
				double time = ((DoubleCell) row.getCell(3)).getDoubleValue();

				if (containerToTimeToShift.containsKey(containerId)) {
					containerToTimeToShift.get(containerId).put(time, shift);
				} else {
					TreeMap<Double, Double> timeToShift = new TreeMap<>();
					timeToShift.put(time, shift);
					containerToTimeToShift.put(containerId, timeToShift);
				}
			}
		}

		FeatureBinFiller filler = new FeatureBinFiller(DEFAULT_RT_DELTA, DEFAULT_PPM_DELTA, defaultValue);
		for (Integer containerId : containerToRowId.keySet()) {
			String rawContainerId = TextUtils.cleanId(rawContainers.get(containerId).getId())[0];
			if (!containerToTimeToShift.isEmpty() && containerToTimeToShift.containsKey(rawContainerId))
				filler.setShifts(containerToTimeToShift.get(rawContainerId));
			filler.reverseFill(containerId, rawContainers.get(containerId), rows, containerToRowId.get(containerId));
		}

		int currentI = 0;
		Set<Integer> indices = new HashSet<>(containerToRowId.values());
		int defaultGaps = 0;
		int unfilledGaps = 0;
		for (FeatureBin row : rows) {

			if (!indices.contains(currentI++)) {
				continue;
			}

			cellRow = new DataCell[model.getColumnCount() - 1];
			cellRow[0] = new DoubleCell(row.getMz());
			cellRow[1] = new DoubleCell(row.getRt());
			cellRow[2] = new DoubleCell(row.getArea());
			cellRow[3] = new StringCell(row.getLabel());
			cellRow[4] = new DoubleCell(row.getMzDev());

			for (int i = 6; i < model.getColumnCount(); i++) {
				double intensity = row.isPresent(i - FeatureBin.COLUMNS);
				if (intensity == defaultValue) {
					// setWarningMessage("Could not fill gap at " + row.getMz()
					// + " m/z and " + row.getRt()
					// +
					// " with 500 ppm and 5 s tolerance. Using default value.");
					cellRow[i - 1] = new DoubleCell(intensity);
					defaultGaps++;
				} else if (intensity > 0) {
					cellRow[i - 1] = new DoubleCell(intensity);
				} else {
					cellRow[i - 1] = new DoubleCell(defaultValue);
					// setWarningMessage("Unfilled trace at: " + row.getMz() +
					// " m/z - " + row.getRt() + " s");
					unfilledGaps++;
				}
			}

			dataContainer.addRowToTable(new DefaultRow(new RowKey(id++ + ""), cellRow));
		}
		NodeLogger.getLogger(this.getClass()).info(
				"Gap filling: " + defaultGaps + " default gaps, " + unfilledGaps + " missing gaps.");

		dataContainer.close();

		return new BufferedDataTable[] { dataContainer.getTable() };
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		createColumnSpec(dataColumnSpecs, "m/z", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "rt", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "area", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "label", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "m/z dev", DoubleCell.TYPE);
		for (int i = 0; i < profileContainerIds.size(); i++)
			createColumnSpec(dataColumnSpecs, TextUtils.cleanId(profileContainerIds.get(i))[0], DoubleCell.TYPE);
		// }
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
			settings.setTextOption(Parameter.GAP_FILL, "" + Parameter.GAP_FILL.getDefaultValue());
			settings.setTextOption(Parameter.DEFAULT, "" + Parameter.DEFAULT.getDefaultValue());
		}

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.FEATURE_COLUMN, Parameter.FEATURE_SET_COLUMN);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getColumnName(Parameter.FEATURE_COLUMN) == null)
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_SET_COLUMN);
		else
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.MZ_WINDOW_PPM);
		NodeUtils.validateDoubleGreaterZero(tmpSettings, Parameter.TIME_WINDOW);
		NodeUtils.validateDoubleGreaterOrEqualZero(tmpSettings, Parameter.MISSINGNESS);
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
