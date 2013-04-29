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

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * Default settings for MassCascade nodes.
 * 
 * @author Stephan Beisken
 */
public class DefaultSettings implements Settings {

	private final Map<String, String> optionMap;

	public DefaultSettings() {
		optionMap = new HashMap<String, String>();
	}

	@Override
	public void setColumnName(String label, String name) {
		optionMap.put(label, name);
	}

	@Override
	public void setTextOption(String label, String name) {
		optionMap.put(label, name);
	}

	@Override
	public void setBooleanOption(String label, boolean bool) {
		optionMap.put(label, "" + bool);
	}

	@Override
	public void setStringArrayOption(String label, String[] array) {
		String res = "";
		for (String s : array)
			res += s + "~";
		res.substring(0, res.length() - 1);
		optionMap.put(label, res);
	}

	@Override
	public int getOptionMapSize() {
		return optionMap.size();
	}

	@Override
	public String getColumnName(String label) {
		return optionMap.get(label);
	}

	@Override
	public String getTextOption(String label) {
		return optionMap.get(label);
	}

	@Override
	public int getIntOption(String label) {
		try {
			return Integer.parseInt(optionMap.get(label));
		} catch (Exception exception) {
			return (int) Double.parseDouble(optionMap.get(label));
		}
	}

	@Override
	public double getDoubleOption(String label) {
		return Double.parseDouble(optionMap.get(label));
	}

	@Override
	public boolean getBooleanOption(String label) {
		return Boolean.parseBoolean(optionMap.get(label));
	}

	@Override
	public String[] getStringArrayOption(String label) {
		return (optionMap.get(label) == null) ? new String[0] : optionMap.get(label).split("~");
	}

	@Override
	public void setColumnName(Parameter parameter, String name) {
		setColumnName(parameter.getDescription(), name);
	}

	@Override
	public void setTextOption(Parameter parameter, String name) {
		setTextOption(parameter.getDescription(), name);
	}

	@Override
	public void setBooleanOption(Parameter parameter, boolean bool) {
		setBooleanOption(parameter.getDescription(), bool);
	}

	@Override
	public String getColumnName(Parameter parameter) {
		return getColumnName(parameter.getDescription());
	}

	@Override
	public String getTextOption(Parameter parameter) {
		return getTextOption(parameter.getDescription());
	}

	@Override
	public int getIntOption(Parameter parameter) {
		return getIntOption(parameter.getDescription());
	}

	@Override
	public double getDoubleOption(Parameter parameter) {
		return getDoubleOption(parameter.getDescription());
	}

	@Override
	public boolean getBooleanOption(Parameter parameter) {
		return getBooleanOption(parameter.getDescription());
	}

	@Override
	public String[] getStringArrayOption(Parameter parameter) {
		return getStringArrayOption(parameter.getDescription());
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {

		settings.addStringArray("keys", optionMap.keySet().toArray(new String[] {}));
		settings.addStringArray("values", optionMap.values().toArray(new String[] {}));
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		String[] keys = settings.getStringArray("keys");
		String[] values = settings.getStringArray("values");

		int i = 0;
		for (String key : keys) {
			optionMap.put(key, values[i]);
			i++;
		}
	}
}
