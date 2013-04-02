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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import uk.ac.ebi.masscascade.interfaces.Range;
import uk.ac.ebi.masscascade.utilities.range.ExtendableRange;

public class MetlinNodeConnect {

	public static final String metLinSearchAddress = "http://metlin.scripps.edu/metabo_list.php?";
	public static final String metLinEntryAddress = "http://metlin.scripps.edu/metabo_info.php?molid=";
	public static final String metLinStructureAddress1 = "http://metlin.scripps.edu/structure/";
	public static final String metLinStructureAddress2 = ".mol";

	/**
	 */
	public String[] findCompounds(double mass, double mzTolerance, int numOfResults) throws IOException {

		double absoluteTolerance = mass / 1000000 * mzTolerance;
		Range toleranceRange = new ExtendableRange(mass - absoluteTolerance, mass + absoluteTolerance);

		String queryAddress = metLinSearchAddress + "mass_min=" + toleranceRange.getLowerBounds() + "&mass_max="
				+ toleranceRange.getUpperBounds();

		URL queryURL = new URL(queryAddress);

		// Submit the query
		String queryResult = retrieveData(queryURL);

		Vector<String> results = new Vector<String>();

		// Find IDs in the HTML data
		Pattern pat = Pattern.compile("\"metabo_info.php\\?molid=([0-9]+)\">\\1");
		Matcher matcher = pat.matcher(queryResult);
		while (matcher.find()) {
			String MID = matcher.group(1);
			results.add(MID);
			if (results.size() == numOfResults)
				break;
		}

		return results.toArray(new String[0]);
	}

	/**
	 * This method retrieves the details about METLIN compound
	 * 
	 */
	public String[] getCompound(String ID) throws IOException {

		URL entryURL = new URL(metLinEntryAddress + ID);
		String metLinEntry = retrieveData(entryURL);

		String compoundName = null;
		String compoundFormula = null;

		// Find compound name
		Pattern patName = Pattern.compile("</iframe>',1,500,450\\)\"><font color=blue><b>(.+?)</b>", Pattern.DOTALL);
		Matcher matcherName = patName.matcher(metLinEntry);
		if (matcherName.find()) {
			compoundName = matcherName.group(1);
		}

		// Find compound formula
		Pattern patFormula = Pattern.compile("Formula.*?<td.*?</script>(.+?)</td>", Pattern.DOTALL);
		Matcher matcherFormula = patFormula.matcher(metLinEntry);
		if (matcherFormula.find()) {
			String htmlFormula = matcherFormula.group(1);
			compoundFormula = htmlFormula.replaceAll("<[^>]+>", "");
		}

		if (compoundName == null) {
			throw (new IOException("Could not parse compound name for compound " + ID));
		}

		return new String[] { compoundName, compoundFormula };
	}

	public IAtomContainer getStructure(String id) throws IOException, CDKException {

		URL structureUrl = new URL(metLinStructureAddress1 + id + metLinStructureAddress2);
		String structure = retrieveData(structureUrl);
		String repairedStructure = repairStructure(structure);
		MDLReader reader = new MDLReader();
		reader.setReaderMode(IChemObjectReader.Mode.RELAXED);
		reader.setReader(new ByteArrayInputStream(repairedStructure.getBytes()));
		ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
		List<IAtomContainer> containerList = ChemFileManipulator.getAllAtomContainers(chemFile);
		return containerList.get(0);
	}

	private String retrieveData(URL url) throws IOException {

		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-agent", "MZmine 2");
		InputStream is = connection.getInputStream();

		if (is == null) {
			throw new IOException("Could not establish a connection to " + url);
		}

		StringBuffer buffer = new StringBuffer();

		try {
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");

			char[] cb = new char[1024];

			int amtRead = reader.read(cb);
			while (amtRead > 0) {
				buffer.append(cb, 0, amtRead);
				amtRead = reader.read(cb);
			}

		} catch (UnsupportedEncodingException e) {
			// This should never happen, because UTF-8 is supported
			e.printStackTrace();
		}

		is.close();

		return buffer.toString();

	}

	private String repairStructure(String structure) throws IOException {

		StringBuilder sbl = new StringBuilder();
		BufferedReader srd = new BufferedReader(new StringReader(structure));
		String line = "";
		int lineCounter = 0;
		String extraInformation = "   0  0  0  0  0  0  0  0  0  0  0  0";

		while ((line = srd.readLine()) != null) {

			line = line.replaceAll("\\s+$", "");
			if (lineCounter > 3 && line.length() < 40 && line.matches(".*\\D+$")) {
				line += extraInformation;
			}
			sbl.append(line + "\n");
			lineCounter++;
		}
		return sbl.toString();
	}
}
