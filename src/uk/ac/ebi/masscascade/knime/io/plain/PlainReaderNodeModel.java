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
package uk.ac.ebi.masscascade.knime.io.plain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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

import uk.ac.ebi.masscascade.core.container.file.featureset.FileFeatureSetContainer;
import uk.ac.ebi.masscascade.core.feature.FeatureImpl;
import uk.ac.ebi.masscascade.core.featureset.FeatureSetImpl;
import uk.ac.ebi.masscascade.interfaces.Feature;
import uk.ac.ebi.masscascade.interfaces.FeatureSet;
import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.interfaces.container.FeatureSetContainer;
import uk.ac.ebi.masscascade.knime.NodePlugin;
import uk.ac.ebi.masscascade.knime.NodeUtils;
import uk.ac.ebi.masscascade.knime.datatypes.featuresetcell.FeatureSetCell;
import uk.ac.ebi.masscascade.parameters.Constants;
import uk.ac.ebi.masscascade.parameters.Constants.ION_MODE;
import uk.ac.ebi.masscascade.parameters.Constants.MSN;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;
import uk.ac.ebi.masscascade.utilities.xyz.XYList;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

/**
 * This is the model implementation of the plain reader.
 * 
 * @author Stephan Beisken
 */
public class PlainReaderNodeModel extends NodeModel {

	private List<File> scanFileIds = new ArrayList<File>();

	/**
	 * Constructor for the node model.
	 */
	protected PlainReaderNodeModel() {
		super(2, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		
		int nrowId = 0;
		String parent = "";
		Set<Feature> nfsSet = new HashSet<>();
		XYList nxyList = new XYList();
		for (DataRow row : inData[1]) {
			double mz = ((DoubleCell) row.getCell(0)).getDoubleValue();
			double intensity = ((DoubleCell) row.getCell(1)).getDoubleValue();
			Range mzRange = new ExtendableRange(mz);
			Feature feature = new FeatureImpl(nrowId++, new XYPoint(mz, intensity), 10, mzRange);
			feature.addFeaturePoint(new XYPoint(mz, intensity), 10);
			feature.addFeaturePoint(new XYPoint(mz, intensity), 10);
			feature.closeFeature();
			nfsSet.add(feature);
			nxyList.add(new XYPoint(mz, intensity));
			parent = ((StringCell) row.getCell(2)).getStringValue();
		}
		FeatureSet nfs = new FeatureSetImpl(0, nxyList, new ExtendableRange(9, 11), 10, nfsSet);
		
		int rowId = 0;
		Set<Feature> fsSet = new HashSet<>();
		XYList xyList = new XYList();
		for (DataRow row : inData[0]) {
			try {
				double mz = ((DoubleCell) row.getCell(0)).getDoubleValue();
				double intensity = ((DoubleCell) row.getCell(1)).getDoubleValue();
				Range mzRange = new ExtendableRange(mz);
				Feature feature = new FeatureImpl(rowId, new XYPoint(mz, intensity), 10, mzRange);
				feature.addFeaturePoint(new XYPoint(mz, intensity), 10);
				feature.addFeaturePoint(new XYPoint(mz, intensity), 10);
				feature.closeFeature();
				if (row.getKey().getString().equals(parent)) {
					Map<Constants.MSN, Set<Integer>> msnMap = new HashMap<>();
					Set<Integer> msnSet = new HashSet<>();
					msnSet.add(0);
					msnMap.put(Constants.MSN.MS2, msnSet);
					feature.setMsnScans(msnMap);
					nfs.setParent(feature.getId(), feature.getMz(), 0);
					feature.addMsnSpectrum(MSN.MS2, nfs);
				}
				fsSet.add(feature);
				xyList.add(new XYPoint(mz, intensity));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			rowId++;
		}

		FeatureSetContainer fsContainer = new FileFeatureSetContainer(
				"challenge", ION_MODE.POSITIVE, NodePlugin.getProjectDirectory());
		FeatureSet fs = new FeatureSetImpl(0, xyList, new ExtendableRange(9, 11), 10, fsSet);
		fsContainer.addFeatureSet(fs);
		fsContainer.finaliseFile();
		scanFileIds.add(fsContainer.getDataFile());
		
		BufferedDataContainer fsCont = exec.createDataContainer(createOutSpec()[0]);
		DataCell fsCell = new FeatureSetCell(fsContainer);
		fsCont.addRowToTable(new DefaultRow("0", fsCell));
		fsCont.close();

		return new BufferedDataTable[] { fsCont.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		NodeUtils.deleteScanFiles(scanFileIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return createOutSpec();
	}

	private DataTableSpec[] createOutSpec() {

		DataColumnSpec fsData = new DataColumnSpecCreator("Feature Set", FeatureSetCell.TYPE).createSpec();
		return new DataTableSpec[] { new DataTableSpec(fsData) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		NodeUtils.loadInternals(internDir, exec, scanFileIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		NodeUtils.saveInternals(internDir, exec, scanFileIds);
	}
}
