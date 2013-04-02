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
package uk.ac.ebi.masscascade.knime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;

import uk.ac.ebi.masscascade.knime.defaults.Settings;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * Class containing convenience methods for KNIME node extensions.
 * 
 * @author Stephan Beisken
 */
public class NodeUtils {

	/**
	 * Returns a list of data table specifications.
	 * 
	 * @param inSpecs inputs specifications
	 * @param settings settings file
	 * @param column source column
	 * @return the list of data table specifications
	 * @throws InvalidSettingsException unexpected behaviour
	 */
	public static DataTableSpec[] getDataTableSpec(final DataTableSpec inSpec, Settings settings,
			Parameter... sourceColumns) throws InvalidSettingsException {

		int msDataCol = -1;
		Parameter sourceColumn = sourceColumns[0];
		for (int j = 0; j< sourceColumns.length; j++) {
			sourceColumn = sourceColumns[j];
			msDataCol = inSpec.findColumnIndex(settings.getColumnName(sourceColumn.getDescription()));
			String name = "";

			if (msDataCol == -1) {
				int i = 0;
				for (DataColumnSpec dcs : inSpec) {
					if (dcs.getType().isCompatible(NodeParm.columnClass.get(sourceColumn))) {
						msDataCol = i;
					}
					i++;
				}

				if (msDataCol != -1) {
					name = inSpec.getColumnSpec(msDataCol).getName();
					settings.setColumnName(sourceColumn.getDescription(), name);
				} else continue;
			}

			if (inSpec.getColumnSpec(msDataCol).getType().isCompatible(NodeParm.columnClass.get(sourceColumn)))
				break;
		}

		if (!inSpec.getColumnSpec(msDataCol).getType().isCompatible(NodeParm.columnClass.get(sourceColumn))) {
			throw new InvalidSettingsException("Target cell column "
					+ settings.getColumnName(sourceColumn.getDescription()) + " not found");
		}

		return new DataTableSpec[] { inSpec };
	}

	/**
	 * Returns a list of optional data table specifications.
	 * 
	 * @param inSpecs inputs specifications
	 * @param settings settings file
	 * @param column source column
	 * @return the list of data table specifications
	 * @throws InvalidSettingsException unexpected behaviour
	 */
	public static DataTableSpec[] getOptionalDataTableSpec(final DataTableSpec inSpec, Settings settings,
			Parameter sourceColumn) throws InvalidSettingsException {

		int msDataCol = inSpec.findColumnIndex(settings.getColumnName(sourceColumn.getDescription()));
		String name = "";

		if (msDataCol == -1) {
			int i = 0;
			for (DataColumnSpec dcs : inSpec) {
				if (dcs.getType().isCompatible(NodeParm.columnClass.get(sourceColumn))) {
					msDataCol = i;
				}
				i++;
			}

			if (msDataCol != -1) {
				name = inSpec.getColumnSpec(msDataCol).getName();
				settings.setColumnName(sourceColumn.getDescription(), name);
			}
		}

		return new DataTableSpec[] { inSpec };
	}

	/**
	 * Loads the file identifiers.
	 * 
	 * @param internDir internal directory
	 * @param exec executable environment
	 * @param databaseIds list of file identifiers
	 * @throws IOException unexpected input output behaviour
	 */
	public static void loadInternals(final File internDir, final ExecutionMonitor exec, List<File> ids)
			throws IOException {

		FileReader fileReader = new FileReader(internDir + File.separator + "masscascade");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			File file = new File(line);
			if (!file.exists())
				throw new IOException("Serialized data file missing: " + line);
			ids.add(file);
		}
	}

	/**
	 * Saves the file identifiers.
	 * 
	 * @param internDir internal directory
	 * @param exec executable environment
	 * @param databaseIds list of file identifiers
	 * @throws IOException unexpected input output behaviour
	 */
	public static void saveInternals(final File internDir, final ExecutionMonitor exec, List<File> ids)
			throws IOException {

		FileWriter fileWriter = new FileWriter(internDir + File.separator + "masscascade");
		for (File id : ids) {
			fileWriter.write(id.getAbsolutePath() + "\n");
		}
		fileWriter.close();
	}

	/**
	 * Deletes the files with the given file identifiers.
	 * 
	 * @param ids list of file identifiers
	 * @return success
	 */
	public static void deleteScanFiles(List<File> ids) {

		for (File id : ids)
			id.delete();
		ids.clear();
	}

	/**
	 * Gets a specific data row.
	 * 
	 * @param dataTable the data table containing all rows
	 * @param rowIndex the row index
	 * @return the data row
	 */
	public static DataRow getDataRow(BufferedDataTable dataTable, int rowIndex) {

		DataCell[] cells = new DataCell[dataTable.getDataTableSpec().getNumColumns()];
		Arrays.fill(cells, DataType.getMissingCell());
		DataRow row = new DefaultRow(new RowKey("1"), cells);

		int runningIndex = 0;
		for (DataRow dataRow : dataTable) {

			if (runningIndex == rowIndex) {
				row = dataRow;
				break;
			}
			runningIndex++;
		}

		return row;
	}

	/**
	 * Validates the input column against the settings.
	 * 
	 * @param tmpSettings the settings.
	 * @param column the input column
	 * @throws InvalidSettingsException if invalid
	 */
	public static void validateColumnSetting(Settings tmpSettings, Parameter column) throws InvalidSettingsException {

		if (tmpSettings.getColumnName(column.getDescription()) == null
				|| tmpSettings.getColumnName(column.getDescription()).length() == 0) {

			throw new InvalidSettingsException("No valid column: " + column.name());
		}
	}

	public static void validateDoubleGreaterZero(Settings tmpSettings, Parameter parameter)
			throws InvalidSettingsException {

		if (tmpSettings.getDoubleOption(parameter) <= 0)
			throw new InvalidSettingsException(parameter.getDescription() + ": Value must be positive.");
	}

	public static void validateTextNotEmpty(Settings tmpSettings, Parameter parameter) throws InvalidSettingsException {

		if (tmpSettings.getTextOption(parameter).isEmpty())
			throw new InvalidSettingsException("Token must not be empty.");
	}

	public static void validateRange(Settings tmpSettings, Parameter parameter) throws InvalidSettingsException {

		String[] elements = tmpSettings.getTextOption(parameter).split("-");
		if (elements.length != 2) {
			throw new InvalidSettingsException("Range format exception: Lower-Upper.");
		} else {
			double ll = Double.parseDouble(elements[0]);
			double ul = Double.parseDouble(elements[1]);
			if (ul < ll)
				throw new InvalidSettingsException("Range format exception: Lower-Upper.");
		}
	}
}
