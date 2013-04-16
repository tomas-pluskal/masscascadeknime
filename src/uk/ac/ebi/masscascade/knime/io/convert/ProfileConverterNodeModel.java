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
package uk.ac.ebi.masscascade.knime.io.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.base.node.parallel.builder.ThreadedTableBuilderNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.RowAppender;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.core.PropertyManager;
import uk.ac.ebi.masscascade.core.chromatogram.MassChromatogram;
import uk.ac.ebi.masscascade.interfaces.Profile;
import uk.ac.ebi.masscascade.interfaces.Property;
import uk.ac.ebi.masscascade.interfaces.Spectrum;
import uk.ac.ebi.masscascade.interfaces.container.ProfileContainer;
import uk.ac.ebi.masscascade.interfaces.container.SpectrumContainer;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.profilecell.ProfileValue;
import uk.ac.ebi.masscascade.knime.datatypes.singleprofilecell.SingleProfileCell;
import uk.ac.ebi.masscascade.knime.datatypes.spectrumcell.SpectrumValue;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Parameter;
import uk.ac.ebi.masscascade.utilities.ProfUtils;

/**
 * This is the model implementation of ProfileConverter. Node to extract peak information from the peak colletions. All
 * peak details are converted into a table matrix, where all peak collections are shown in succession.
 * 
 * @author Stephan Beisken
 */
public class ProfileConverterNodeModel extends ThreadedTableBuilderNodeModel {

	private final Settings settings = new DefaultSettings();

	private int selProfile;
	private int colIndex;
	private DataColumnSpec[] dataColumnSpecs;

	/**
	 * Constructor for the node model.
	 */
	protected ProfileConverterNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] prepareExecute(DataTable[] data) throws Exception {

		DataTableSpec inSpec = data[0].getDataTableSpec();

		String colName = settings.getColumnName(Parameter.PEAK_COLUMN) == null ? settings
				.getColumnName(Parameter.SPECTRUM_COLUMN) : settings.getColumnName(Parameter.PEAK_COLUMN);
		colIndex = inSpec.findColumnIndex(colName);
		dataColumnSpecs = createOutputTableSpecification();

		selProfile = -1;
		String prof = settings.getTextOption("Select Profile");
		if (prof != null && !prof.isEmpty()) {
			try {
				selProfile = Integer.parseInt(prof);
			} catch (Exception exception) {
				// fall through
			}
		}

		return new DataTableSpec[] { new DataTableSpec(dataColumnSpecs) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRow(DataRow inRow, BufferedDataTable[] additionalData, RowAppender[] outputTables)
			throws Exception {

		DataCell peakCell = inRow.getCell(colIndex);

		if (peakCell.isMissing())
			return;

		DataCell[] resultCells = new DataCell[dataColumnSpecs.length];

		if (peakCell instanceof ProfileValue) {
			ProfileContainer container = ((ProfileValue) peakCell).getPeakDataValue();
			if (selProfile == -1)
				addProfiles(container.iterator(), container.getId(), resultCells, outputTables[0]);
			else {
				if (container.getProfile(selProfile) != null)
					addProfile(container.getProfile(selProfile), container.getId(), resultCells, outputTables[0]);
			}
		} else if (peakCell instanceof SpectrumValue) {
			SpectrumContainer container = ((SpectrumValue) peakCell).getSpectrumDataValue();
			if (selProfile == -1)
				for (Spectrum spectrum : container)
					addProfiles(spectrum.iterator(), container.getId() + "-" + spectrum.getIndex(), resultCells,
							outputTables[0]);
			else
				for (Spectrum spectrum : container)
					if (spectrum.getProfileMap().containsKey(selProfile)) {
						addProfiles(spectrum.iterator(), container.getId() + "-" + spectrum.getIndex(), resultCells,
								outputTables[0]);
						break;
					}
		} else {
			throw new IllegalArgumentException("No peak data cell at " + peakCell + ": "
					+ peakCell.getClass().getName());
		}
	}

	private void addProfiles(Iterator<Profile> profileIterator, String id, DataCell[] resultCells,
			RowAppender outputTable) {
		while (profileIterator.hasNext())
			addProfile(profileIterator.next(), id, resultCells, outputTable);
	}

	private void addProfile(Profile profile, String id, DataCell[] resultCells, RowAppender outputTable) {

		MassChromatogram xic = (MassChromatogram) profile.getTrace(Constants.PADDING);

		resultCells[0] = new IntCell(profile.getId());
		resultCells[1] = new DoubleCell(profile.getRetentionTime());
		resultCells[2] = new DoubleCell(xic.getData().get(xic.getData().size() - 1).x - xic.getData().get(0).x);
		resultCells[3] = new DoubleCell(profile.getMzIntDp().x);
		resultCells[4] = new DoubleCell(profile.getMzIntDp().y);
		resultCells[5] = new DoubleCell(xic.getDeviation());
		resultCells[6] = new DoubleCell(profile.getArea());
		resultCells[7] = new SingleProfileCell(profile.getData());

		String[] profileInfos = ProfUtils.getProfileInfo(profile);
		String profileInfo = profileInfos[0] + "\n" + profileInfos[1] + "\n" + profileInfos[2];
		resultCells[8] = new StringCell(profileInfo);

		List<DoubleCell> res = new ArrayList<DoubleCell>();
		for (Property prop : profile.getProperty(PropertyManager.TYPE.Score))
			res.add(new DoubleCell(prop.getValue(Double.class)));
		resultCells[9] = CollectionCellFactory.createListCell(res);

		outputTable.addRowToTable(new DefaultRow(new RowKey(id + "-" + profile.getId()), resultCells));
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();

		createColumnSpec(dataColumnSpecs, "id", IntCell.TYPE);
		createColumnSpec(dataColumnSpecs, "rt", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "width", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "mz", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "intensity", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "deviation", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "area", DoubleCell.TYPE);
		createColumnSpec(dataColumnSpecs, "profile", SingleProfileCell.TYPE);
		createColumnSpec(dataColumnSpecs, "label", StringCell.TYPE);
		createColumnSpec(dataColumnSpecs, "scores", ListCell.getCollectionType(DoubleCell.TYPE));

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

		NodeUtils.getDataTableSpec(inSpecs[0], settings, Parameter.SPECTRUM_COLUMN, Parameter.PEAK_COLUMN);
		return new DataTableSpec[] { new DataTableSpec(createOutputTableSpecification()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getColumnName(Parameter.PEAK_COLUMN) == null)
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.SPECTRUM_COLUMN);
		else
			NodeUtils.validateColumnSetting(tmpSettings, Parameter.PEAK_COLUMN);
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
