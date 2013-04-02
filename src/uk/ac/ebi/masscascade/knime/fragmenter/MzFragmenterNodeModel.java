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
package uk.ac.ebi.masscascade.knime.fragmenter;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.openscience.cdk.knime.type.CDKValue;

/**
 * This is the model implementation of MzFragmenter.
 * 
 * @author Stephan Beisken
 */
public class MzFragmenterNodeModel extends NodeModel {

	// the logger instance
	// private static final NodeLogger logger = NodeLogger
	// .getLogger(MzFragmenterNodeModel.class);

	private final MzFragmenterSettings fragSettings = new MzFragmenterSettings();

	/**
	 * Constructor for the node model.
	 */
	protected MzFragmenterNodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
/*
		DataTableSpec spec = inData[0].getDataTableSpec();
		int molColIndex = spec.findColumnIndex(fragSettings.getMolColumnName());

		List<DataCell[]> rowList = new ArrayList<DataCell[]>();
		int maxOcc = 0;
		for (DataRow row : inData[0]) {

			DataCell cell = row.getCell(molColIndex);
			IMolecule molecule = ((CDKValue) cell).getMolecule();

			Fragmenter fragmenter = new Fragmenter(new Vector<Peak>(), fragSettings.getMassThreshold(),
					fragSettings.isBreakAromaticRings(), fragSettings.isFormulaRedundancy(),
					fragSettings.isBreakLikelyBonds());
			System.out.println("1");
			List<File> fragmentFileList = fragmenter.generateFragmentsEfficient(molecule, true,
					fragSettings.getTreeDepth(), molecule.getID());
			System.out.println("2");
			List<IAtomContainer> generatedFrags = Molfile.ReadfolderTemp(fragmentFileList);

			DataCell[] fragCells = new DataCell[generatedFrags.size() + 1];
			fragCells[0] = new CDKCell(molecule);
			if (generatedFrags.size() > maxOcc) {
				maxOcc = generatedFrags.size();
			}
			for (int i = 0; i < generatedFrags.size(); i++) {
				IMolecule newFrag = (IMolecule) generatedFrags.get(i);
				fragCells[i + 1] = new CDKCell(newFrag);
			}
			rowList.add(fragCells);
		}
		DataColumnSpec[] colSpec = new DataColumnSpec[maxOcc + 1];
		colSpec[0] = new DataColumnSpecCreator("Molecule", CDKCell.TYPE).createSpec();
		for (int i = 1; i <= maxOcc; i++) {
			colSpec[i] = new DataColumnSpecCreator("Fragment" + (i + 1), CDKCell.TYPE).createSpec();
		}
		DataTableSpec newSpec = new DataTableSpec(colSpec);
		BufferedDataContainer container = exec.createDataContainer(newSpec);
		;

		int key = 1;
		for (DataCell[] dataCells : rowList) {
			container.addRowToTable(new DefaultRow(RowKey.createRowKey(key), dataCells));
			key++;
		}
		container.close();

		return new BufferedDataTable[] { container.getTable() };
*/
		return null;
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

		int molCol = inSpecs[0].findColumnIndex(fragSettings.getMolColumnName());
		if (molCol == -1) {
			for (DataColumnSpec dcs : inSpecs[0]) {
				if (dcs.getType().isCompatible(CDKValue.class)) {
					if (molCol >= 0) {
						molCol = -1;
						break;
					} else {
						molCol = inSpecs[0].findColumnIndex(dcs.getName());
					}
				}
			}

			if (molCol != -1) {
				String name = inSpecs[0].getColumnSpec(molCol).getName();
				setWarningMessage("Using '" + name + "' as molecule column");
				fragSettings.setMolColumnName(name);
			}
		}

		if (molCol == -1) {
			throw new InvalidSettingsException("Molecule column '" + fragSettings.getMolColumnName()
					+ "' does not exist");
		}

		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		fragSettings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		fragSettings.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		MzFragmenterSettings tmpSettings = new MzFragmenterSettings();
		tmpSettings.loadSettings(settings);

		if (tmpSettings.getMolColumnName() == null || tmpSettings.getMolColumnName().length() == 0) {
			throw new InvalidSettingsException("No molecule column chosen");
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
