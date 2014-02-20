/*
 * Copyright (C) 2014 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.masscascade.knime.alignment.ratiosets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.alignment.ratiosets.RatioFeatureSets;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetCell;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.parameters.ParameterMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the model implementation of the ratio sets compiler.
 * 
 * @author Stephan Beisken
 */
public class RatioSetsNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected RatioSetsNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		String colName = settings.getColumnName(Parameter.FEATURE_SET_COLUMN);
		int colIndex = inData[0].getDataTableSpec().findColumnIndex(colName);

		String colNameGroup = settings.getColumnName(Parameter.LABEL_COLUMN);
		int colIndexGroup = inData[0].getDataTableSpec().findColumnIndex(colNameGroup);

		Multimap<Integer, Container> container = HashMultimap.create();

		for (DataRow row : inData[0]) {
			DataCell cell = row.getCell(colIndex);
			DataCell cellGroup = row.getCell(colIndexGroup);
			if (cell.isMissing() || cellGroup.isMissing()) {
				continue;
			}
			container.put(((IntValue) cellGroup).getIntValue(),
					((FeatureSetValue) cell).getFeatureSetDataValue());
		}

		DataColumnSpec colSpec1 = new DataColumnSpecCreator("Ratio Set", FeatureSetCell.TYPE).createSpec();
		DataColumnSpec colSpec2 = new DataColumnSpecCreator("Group", IntCell.TYPE).createSpec();
		BufferedDataContainer dataContainer = exec.createDataContainer(new DataTableSpec(colSpec1, colSpec2));
		
		for (int group : container.keySet()) {
			ParameterMap pm = new ParameterMap();
			pm.put(Parameter.FEATURE_SET_LIST, new ArrayList<>(container.get(group)));
			RatioFeatureSets ratioSets = new RatioFeatureSets(pm);
			FeatureSetContainer c = (FeatureSetContainer) ratioSets.call();
			DataRow row = new DefaultRow(new RowKey("Row" + group), new FeatureSetCell(c), new IntCell(group));
			dataContainer.addRowToTable(row);
		}
		
		dataContainer.close();

		return new BufferedDataTable[] { dataContainer.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.DATA_COLUMN);
		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.FEATURE_SET_COLUMN);

		DataColumnSpec colSpec1 = new DataColumnSpecCreator("Ratio Set", FeatureSetCell.TYPE).createSpec();
		DataColumnSpec colSpec2 = new DataColumnSpecCreator("Group", IntCell.TYPE).createSpec();

		return new DataTableSpec[] { new DataTableSpec(colSpec1, colSpec2) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		NodeUtils.validateColumnSetting(tmpSettings, Parameter.DATA_COLUMN);
		NodeUtils.validateColumnSetting(tmpSettings, Parameter.FEATURE_SET_COLUMN);
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
	protected void reset() {
		// nothing to do
	}
}
