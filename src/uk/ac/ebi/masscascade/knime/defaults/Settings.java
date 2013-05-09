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
package uk.ac.ebi.masscascade.knime.defaults;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.interfaces.Option;

/**
 * Interface for the default settings for MassCascade nodes.
 * 
 * @author Stephan Beisken
 */
public interface Settings {

	/**
	 * Sets the column name.
	 * 
	 * @param parameter a column name parameter
	 * @param name a column name
	 */
	public void setColumnName(Option parameter, String name);

	/**
	 * Sets the column name.
	 * 
	 * @param label a column name label
	 * @param name a column name
	 */
	public void setColumnName(String label, String name);

	/**
	 * Sets a text option (<code>JTextField</code>).
	 * 
	 * @param parameter a parameter for the String option
	 * @param name a String option name
	 */
	public void setTextOption(Option parameter, String name);

	/**
	 * Sets a text option (<code>JTextField</code>).
	 * 
	 * @param label a label for the String option
	 * @param name a String option name
	 */
	public void setTextOption(String label, String name);

	/**
	 * Sets a boolean option.
	 * 
	 * @param parameter a parameter for the boolean option
	 * @param bool a boolean value
	 */
	public void setBooleanOption(Option parameter, boolean bool);

	/**
	 * Sets an array of strings.
	 * 
	 * @param label a label for the String Array option
	 * @param array a String Array
	 */
	void setStringArrayOption(String label, String[] array);

	/**
	 * Returns the string array.
	 * 
	 * @param label the label for the string array
	 * @return the string array
	 */
	String[] getStringArrayOption(String label);

	/**
	 * Returns the string array.
	 * 
	 * @param parameter the parameter for the string array
	 * @return the string array
	 */
	String[] getStringArrayOption(Option parameter);

	/**
	 * Sets a boolean option.
	 * 
	 * @param label a label for the boolean option
	 * @param bool a boolean value
	 */
	public void setBooleanOption(String label, boolean bool);

	/**
	 * Returns the no. of set options.
	 * 
	 * @return the no. of set options
	 */
	public int getOptionMapSize();

	/**
	 * Returns a column name.
	 * 
	 * @param parameter a parameter identifying the column name
	 * @return the column name
	 */
	public String getColumnName(Option parameter);

	/**
	 * Returns a column name.
	 * 
	 * @param label a label identifying the column name
	 * @return the column name
	 */
	public String getColumnName(String label);

	/**
	 * Returns a text option.
	 * 
	 * @param parameter a parameter identifying the String option
	 * @return the String option
	 */
	public String getTextOption(Option parameter);

	/**
	 * Returns a text option.
	 * 
	 * @param label a label identifying the String option
	 * @return the String option
	 */
	public String getTextOption(String label);

	/**
	 * Returns an integer option.
	 * 
	 * @param parameter a parameter identifying the integer option
	 * @return the integer option
	 */
	public int getIntOption(Option parameter);

	/**
	 * Returns an integer option.
	 * 
	 * @param label a label identifying the integer option
	 * @return the integer option
	 */
	public int getIntOption(String label);

	/**
	 * Returns a double option.
	 * 
	 * @param label a label identifying the double option
	 * @return the double option
	 */
	public double getDoubleOption(Option parameter);

	/**
	 * Returns a double option.
	 * 
	 * @param label a label identifying the double option
	 * @return the double option
	 */
	public double getDoubleOption(String label);

	/**
	 * Returns a boolean option.
	 * 
	 * @param parameter a parameter identifying the boolean option
	 * @return the boolean option
	 */
	public boolean getBooleanOption(Option parameter);

	/**
	 * Returns a boolean option.
	 * 
	 * @param label a label identifying the boolean option
	 * @return the boolean option
	 */
	public boolean getBooleanOption(String label);

	/**
	 * Saves node settings to the settings template instance.
	 * 
	 * @param settings node settings
	 */
	public void saveSettings(final NodeSettingsWO settings);

	/**
	 * Loads node settings from the settings template instance.
	 * 
	 * @param settings node settings
	 * @throws InvalidSettingsException invalid behaviour
	 */
	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException;
}
