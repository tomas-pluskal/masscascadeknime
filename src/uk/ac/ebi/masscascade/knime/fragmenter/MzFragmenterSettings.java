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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds all settings for the hydrogen adder node.
 * 
 * @author Stephan Beisken
 */
public class MzFragmenterSettings {
	
    private int treeDepth;
    private int noThreads;
    private double massThreshold;
    private double peakIntensity;
    private String molColumnName;

    private boolean breakAromaticRings = false;
    private boolean breakLikelyBonds = true;
    private boolean formulaRedundancy = true;
    private boolean calculateEnergies = true;
    
    public int getTreeDepth() {
		return treeDepth;
	}

	public int getNoThreads() {
		return noThreads;
	}

	public double getMassThreshold() {
		return massThreshold;
	}

	public double getPeakIntensity() {
		return peakIntensity;
	}

	public String getMolColumnName() {
		return molColumnName;
	}

	public boolean isBreakAromaticRings() {
		return breakAromaticRings;
	}

	public boolean isBreakLikelyBonds() {
		return breakLikelyBonds;
	}

	public boolean isFormulaRedundancy() {
		return formulaRedundancy;
	}

	public boolean isCalculateEnergies() {
		return calculateEnergies;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}

	public void setNoThreads(int noThreads) {
		this.noThreads = noThreads;
	}

	public void setMassThreshold(double massThreshold) {
		this.massThreshold = massThreshold;
	}

	public void setPeakIntensity(double peakIntensity) {
		this.peakIntensity = peakIntensity;
	}

	public void setMolColumnName(String molColumnName) {
		this.molColumnName = molColumnName;
	}

	public void setBreakAromaticRings(boolean breakAromaticsRings) {
		this.breakAromaticRings = breakAromaticsRings;
	}

	public void setBreakLikelyBonds(boolean breakLikelyBonds) {
		this.breakLikelyBonds = breakLikelyBonds;
	}

	public void setFormulaRedundancy(boolean formulaRedundancy) {
		this.formulaRedundancy = formulaRedundancy;
	}

	public void setCalculateEnergies(boolean calculateEnergies) {
		this.calculateEnergies = calculateEnergies;
	}

	/**
     * Saves the settings into the given node settings object.
     * 
     * @param settings a node settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {

        settings.addInt("treeDepth", treeDepth);
        settings.addInt("noThreads", noThreads);
        settings.addDouble("massThreshold", massThreshold);
        settings.addDouble("peakIntensity", peakIntensity);
        
        settings.addBoolean("calculateEnergies", calculateEnergies);
        settings.addBoolean("breakAromaticRings", breakAromaticRings);
        settings.addBoolean("breakLikelyBonds", breakLikelyBonds);
        settings.addBoolean("formulaRedundancy", formulaRedundancy);
        
        settings.addString("molColumnName", molColumnName);
    }

    /**
     * Loads the settings from the given node settings object.
     * 
     * @param settings a node settings object
     * @throws InvalidSettingsException if not all required settings are
     *             available
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	treeDepth = settings.getInt("treeDepth");
    	noThreads = settings.getInt("noThreads");
    	massThreshold = settings.getDouble("massThreshold");
    	peakIntensity = settings.getDouble("peakIntensity");
    	
    	calculateEnergies = settings.getBoolean("calculateEnergies");
    	breakAromaticRings = settings.getBoolean("breakAromaticRings");
    	breakLikelyBonds = settings.getBoolean("breakLikelyBonds");
    	formulaRedundancy = settings.getBoolean("formulaRedundancy");
    	
    	molColumnName = settings.getString("molColumnName");
    }
}
