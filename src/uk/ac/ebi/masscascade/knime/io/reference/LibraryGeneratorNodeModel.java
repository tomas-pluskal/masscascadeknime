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
package uk.ac.ebi.masscascade.knime.io.reference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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

import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.librarycell.LibraryCell;
import uk.ac.ebi.masscascade.knime.defaults.DefaultSettings;
import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Constants.ION_MODE;
import uk.ac.ebi.masscascade.reference.ReferenceContainer;
import uk.ac.ebi.masscascade.reference.ReferenceSpectrum;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * This is the model implementation of ProfileConverter. Node to extract peak information from the peak colletions. All
 * peak details are converted into a table matrix, where all peak collections are shown in succession.
 * 
 * @author Stephan Beisken
 */
public class LibraryGeneratorNodeModel extends NodeModel {

	private final Settings settings = new DefaultSettings();

	/**
	 * Constructor for the node model.
	 */
	protected LibraryGeneratorNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();

		int idCol = inSpec.findColumnIndex(settings.getTextOption("id"));
		int titleCol = inSpec.findColumnIndex(settings.getTextOption("title"));
		int sourceCol = inSpec.findColumnIndex(settings.getTextOption("source"));
		int nameCol = inSpec.findColumnIndex(settings.getTextOption("name"));
		int notationCol = inSpec.findColumnIndex(settings.getTextOption("notation"));
		int massCol = inSpec.findColumnIndex(settings.getTextOption("mass"));
		int formulaCol = inSpec.findColumnIndex(settings.getTextOption("formula"));
		int instrumentCol = inSpec.findColumnIndex(settings.getTextOption("instrument"));
		int ionModeCol = inSpec.findColumnIndex(settings.getTextOption("ion mode"));
		int ceCol = inSpec.findColumnIndex(settings.getTextOption("collision energy"));
		int ptypeCol = inSpec.findColumnIndex(settings.getTextOption("precursor type"));
		int pTypeMassCol = inSpec.findColumnIndex(settings.getTextOption("precursor mass"));
		int mzCol = inSpec.findColumnIndex(settings.getTextOption("mz list"));
		int intensityCol = inSpec.findColumnIndex(settings.getTextOption("intensity list"));

		String libName = settings.getTextOption("Library Name");
		String libSource = settings.getTextOption("Library Source");
		int libMsn = settings.getIntOption("Library MSn");
		
		ReferenceContainer referenceContainer = new ReferenceContainer(libName, libSource, Constants.MSN.get(libMsn));

		for (DataRow row : inData[0]) {

			String id = idCol == -1 ? "" : getString(row.getCell(idCol));
			String title = titleCol == -1 ? "" : getString(row.getCell(titleCol));
			String source = sourceCol == -1 ? "" : getString(row.getCell(sourceCol));
			String name = nameCol == -1 ? "" : getString(row.getCell(nameCol));
			String notation = notationCol == -1 ? "" : getString(row.getCell(notationCol));
			Double mass = massCol == -1 ? 0 : getDouble(row.getCell(massCol));
			String formula = formulaCol == -1 ? "" : getString(row.getCell(formulaCol));
			String instrument = instrumentCol == -1 ? "" : getString(row.getCell(instrumentCol));
			String ionMode = ionModeCol == -1 ? "" : getString(row.getCell(ionModeCol));
			int ce = ceCol == -1 ? 0 : getInt(row.getCell(ceCol));
			String ptype = ptypeCol == -1 ? "" : getString(row.getCell(ptypeCol));
			Double pTypeMass = pTypeMassCol == -1 ? 0 : getDouble(row.getCell(pTypeMassCol));
			TreeSet<XYPoint> mzIntSet = getMzIntSet(row.getCell(mzCol), row.getCell(intensityCol));

			ION_MODE mode = ionMode.toLowerCase().contains("pos") ? ION_MODE.POSITIVE : ION_MODE.NEGATIVE;

			XYPoint basePeak = mzIntSet.iterator().next();
			for (XYPoint xp : mzIntSet) {
				if (xp.y >= basePeak.y)
					basePeak = xp;
			}

			ReferenceSpectrum spectrum = new ReferenceSpectrum(id, title, source, name, notation, mass, formula,
					instrument, mode, ptype, pTypeMass, ce, mzIntSet, basePeak);
			referenceContainer.addSpectrum(spectrum);
		}

		BufferedDataContainer outData = exec
				.createDataContainer(new DataTableSpec(createOutputTableSpecification()[0]));
		outData.addRowToTable(new DefaultRow(new RowKey("Row1"), new LibraryCell(referenceContainer)));
		outData.close();
		return new BufferedDataTable[] { outData.getTable() };
	}

	private String getString(DataCell cell) {
		return (cell.isMissing()) ? "" : ((StringValue) cell).getStringValue();
	}

	private int getInt(DataCell cell) {
		return (cell.isMissing()) ? 0 : ((IntValue) cell).getIntValue();
	}

	private double getDouble(DataCell cell) {
		return (cell.isMissing()) ? 0.0 : ((DoubleValue) cell).getDoubleValue();
	}

	private TreeSet<XYPoint> getMzIntSet(DataCell mzCell, DataCell intCell) {

		TreeSet<XYPoint> mzIntSet = new TreeSet<>();
		if (mzCell.isMissing() || intCell.isMissing())
			return mzIntSet;

		Iterator<DataCell> mzIter = ((ListCell) mzCell).iterator();
		Iterator<DataCell> intIter = ((ListCell) intCell).iterator();

		while (mzIter.hasNext() && intIter.hasNext()) {

			DataCell mz = mzIter.next();
			double mzValue = (mz instanceof DoubleCell) ? ((DoubleCell) mz).getDoubleValue() : ((IntCell) mz)
					.getIntValue();
			DataCell intensity = intIter.next();
			double intensityValue = (intensity instanceof DoubleCell) ? ((DoubleCell) intensity).getDoubleValue()
					: ((IntCell) intensity).getIntValue();

			mzIntSet.add(new XYPoint(mzValue, intensityValue));
		}

		return mzIntSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		configureSingle(inSpecs[0], "id");
		configureSingle(inSpecs[0], "title");
		configureSingle(inSpecs[0], "source");
		configureSingle(inSpecs[0], "name");
		configureSingle(inSpecs[0], "notation");
		configureSingle(inSpecs[0], "mass");
		configureSingle(inSpecs[0], "formula");
		configureSingle(inSpecs[0], "instrument");
		configureSingle(inSpecs[0], "ion mode");
		configureSingle(inSpecs[0], "collision energy");
		configureSingle(inSpecs[0], "precursor type");
		configureSingle(inSpecs[0], "precursor mass");
		configureSingle(inSpecs[0], "mz list");
		configureSingle(inSpecs[0], "intensity list");
		
		String libName = settings.getTextOption("Library Name");
		if (libName == null || libName.isEmpty()) settings.setTextOption("Library Name", "MyLib");
		String libSource = settings.getTextOption("Library Source");
		if (libSource == null || libSource.isEmpty()) settings.setTextOption("Library Source", "MySource");
		String msn = settings.getTextOption("Library MSn");
		if (msn == null || msn.isEmpty()) settings.setTextOption("Library MSn", "1"); 

		return new DataTableSpec[] { new DataTableSpec(createOutputTableSpecification()) };
	}

	/**
	 * Creates the table output specification.
	 */
	private DataColumnSpec[] createOutputTableSpecification() {

		List<DataColumnSpec> dataColumnSpecs = new ArrayList<DataColumnSpec>();
		createColumnSpec(dataColumnSpecs, "Library", LibraryCell.TYPE);

		return dataColumnSpecs.toArray(new DataColumnSpec[] {});
	}
	
	private void configureSingle(DataTableSpec inSpec, String description) throws InvalidSettingsException {
		if (settings.getTextOption(description) != null && !settings.getTextOption(description).isEmpty())
			NodeUtils.getOptionalDataTableSpec(inSpec, settings, description);
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
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		
		Settings tmpSettings = new DefaultSettings();
		tmpSettings.loadSettings(settings);
		
		if (tmpSettings.getTextOption("Library Name").isEmpty())
			throw new InvalidSettingsException("String for \"Library Name\" must not be empty.");
		if (tmpSettings.getTextOption("Library Source").isEmpty())
			throw new InvalidSettingsException("String for \"Library Source\" must not be empty.");
		if (tmpSettings.getIntOption("Library MSn") < 1 || tmpSettings.getIntOption("Library MSn") > 5)
			throw new InvalidSettingsException("Integer for \"Library MSn\" must be greater than 0 and less than 6");
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
