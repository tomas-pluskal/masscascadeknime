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
package uk.ac.ebi.masscascade.knime.database.metlin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.knime.type.CDKCell;

/**
 * This is the model implementation of Metlin.
 * 
 * @author Stephan Beisken
 */
public class MetlinNodeModel extends NodeModel {

	private MetlinNodeSettings searchSettings = new MetlinNodeSettings();
	private char[] ch = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	/**
	 * Constructor for the node model.
	 */
	protected MetlinNodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		MetlinNodeConnect metlin = new MetlinNodeConnect();

		DataTableSpec spec = inData[0].getDataTableSpec();
		int mzColIndex = spec.findColumnIndex(searchSettings.getMzColName());
		double mzTol = searchSettings.getMzTolerance();
		int noRes = searchSettings.getNoResults();

		DataColumnSpec[] colOutSpec = new DataColumnSpec[spec.getNumColumns() + 2];
		int i;
		for (i = 0; i < spec.getNumColumns(); i++) {
			colOutSpec[i] = spec.getColumnSpec(i);
		}
		colOutSpec[i] = new DataColumnSpecCreator("Metlin Hit", StringCell.TYPE).createSpec();
		colOutSpec[i + 1] = new DataColumnSpecCreator("Metlin Mol", CDKCell.TYPE).createSpec();

		DataTableSpec newSpec = new DataTableSpec(colOutSpec);
		BufferedDataContainer container = exec.createDataContainer(newSpec);
		;

		for (DataRow row : inData[0]) {

			DataCell cell = row.getCell(mzColIndex);
			double mass = ((DoubleValue) cell).getDoubleValue();

			String[] compounds = null;

			int j;
			try {
				compounds = metlin.findCompounds(mass, mzTol, noRes);
				j = 0;
				for (String compound : compounds) {
					String[] compoundName = metlin.getCompound(compound);

					List<DataCell> dataCells = new ArrayList<DataCell>();
					for (DataCell oldCell : row) {
						dataCells.add(oldCell);
					}

					IAtomContainer compoundStructure = metlin.getStructure(compound);
					compoundStructure.setProperty(CDKConstants.NAMES, compoundName[0]);
					compoundStructure.setProperty(CDKConstants.FORMULA, compoundName[1]);

					StringCell metCell = new StringCell(compoundName[0]);
					CDKCell cdkCell = new CDKCell(compoundStructure);
					dataCells.add(metCell);
					dataCells.add(cdkCell);

					container.addRowToTable(new DefaultRow(new RowKey(row.getKey().getString() + ch[j]), dataCells));
					j++;
				}
			} catch (IOException e) {
				setWarningMessage("HTTP 403 for " + mass);
			}
		}

		container.close();
		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		searchSettings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		searchSettings.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		MetlinNodeSettings tmpSettings = new MetlinNodeSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getMzColName() == null || tmpSettings.getMzColName().length() == 0) {
			throw new InvalidSettingsException("No mz column chosen");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// nothing to do
	}

}
