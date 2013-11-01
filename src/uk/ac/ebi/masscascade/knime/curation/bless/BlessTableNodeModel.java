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
package uk.ac.ebi.masscascade.knime.curation.bless;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.knime.type.CDKCell;

import uk.ac.ebi.masscascade.alignment.featurebins.FeatureBinGenerator;
import uk.ac.ebi.masscascade.compound.CompoundEntity;
import uk.ac.ebi.masscascade.compound.CompoundSpectrum;
import uk.ac.ebi.masscascade.compound.CompoundSpectrumAdapter;
import uk.ac.ebi.masscascade.compound.NotationUtil;
import uk.ac.ebi.masscascade.interfaces.container.Container;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the model implementation of BlessTable.
 * 
 * @author Stephan Beisken
 */
public class BlessTableNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	private int gid;
	private int colIndex;

	/**
	 * Constructor for the node model.
	 */
	protected BlessTableNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		String colName = settings.getColumnName(Parameter.PEAK_COLUMN) == null ? settings
				.getColumnName(Parameter.FEATURE_SET_COLUMN) : settings.getColumnName(Parameter.PEAK_COLUMN);
		colIndex = inData[0].getSpec().findColumnIndex(colName);
		int groupIndex = inData[0].getSpec().findColumnIndex(settings.getColumnName(Parameter.LABEL_COLUMN));
		gid = 0;

		BufferedDataContainer dataContainer = exec.createDataContainer(new DataTableSpec(
				createOutputTableSpecification()));

		double ppm = settings.getDoubleOption(Parameter.MZ_WINDOW_PPM);
		double sec = settings.getDoubleOption(Parameter.TIME_WINDOW);
		double missing = settings.getDoubleOption(Parameter.MISSINGNESS);

		HashMultimap<Integer, Integer> cToPIdMap = null; // container to profile
															// id map
		if (missing != 100) {
			Multimap<Integer, Container> spectraContainer = HashMultimap.create();
			for (DataRow row : inData[0]) {
				DataCell spectrumCell = row.getCell(colIndex);
				DataCell groupCell = row.getCell(groupIndex);
				if (spectrumCell.isMissing())
					continue;

				exec.checkCanceled();
				spectraContainer.put(((IntValue) groupCell).getIntValue(),
						((FeatureSetValue) spectrumCell).getFeatureSetDataValue());
			}

			cToPIdMap = FeatureBinGenerator.createContainerToFeatureMap(spectraContainer, ppm, sec, missing);
		}

		int index = 0;
		for (DataRow row : inData[0]) {
			DataCell spectrumCell = row.getCell(colIndex);

			if (spectrumCell.isMissing())
				continue;

			exec.checkCanceled();

			FeatureSetContainer container = ((FeatureSetValue) spectrumCell).getFeatureSetDataValue();

			CompoundSpectrumAdapter adapter = new CompoundSpectrumAdapter();
			List<CompoundSpectrum> css = adapter.getSpectra(cToPIdMap, index++, container);

			BlessFrame frame = new BlessFrame(css, container.getId());
			frame.setVisible();

			Map<Integer, Integer> idToEntityId = frame.getIdToEntity();
			for (CompoundSpectrum cs : css) {
				DataCell[] resultCells = new DataCell[8];
				if (idToEntityId.containsKey(cs.getId()))
					addSpectrum(cs, idToEntityId.get(cs.getId()), resultCells, container.getId());
				else
					addSpectrum(cs, 0, resultCells, container.getId());
				dataContainer.addRowToTable(new DefaultRow(new RowKey(gid++ + ""), resultCells));
			}
		}

		dataContainer.close();
		return new BufferedDataTable[] { dataContainer.getTable() };
	}

	private void addSpectrum(CompoundSpectrum spectrum, int entityIndex, DataCell[] resultCells, String id) {

		int majorPeak = spectrum.getMajorPeak() - 1;
		CompoundEntity ce = spectrum.getCompound(entityIndex);

		resultCells[0] = new StringCell(id.substring(0, id.indexOf(Constants.DELIMITER)));
		resultCells[1] = new DoubleCell(spectrum.getPeakList().get(majorPeak).x);
		resultCells[2] = new DoubleCell(spectrum.getRetentionTime());
		resultCells[3] = new DoubleCell(spectrum.getPeakList().get(majorPeak).y);
		resultCells[4] = new StringCell(ce.getName());

		if (ce.getNotation(majorPeak + 1) == null)
			resultCells[5] = DataType.getMissingCell();
		else {
			IAtomContainer molecule = NotationUtil.getMoleculeTyped(ce.getNotation(majorPeak + 1));
			if (molecule == null)
				resultCells[5] = DataType.getMissingCell();
			else
				resultCells[5] = new CDKCell(molecule);
		}
		resultCells[6] = new DoubleCell(ce.getScore());
		resultCells[7] = new StringCell(ce.getStatus().name());
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		createColumnSpec(dataColumnSpecs, "id", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "mz", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "rt", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "intensity", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "name", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "molecule", CDKCell.TYPE);
		createColumnSpec(dataColumnSpecs, "score", DoubleCell.TYPE);
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
