/*
 * Copyright (C) 2014 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.masscascade.knime.curation.brush;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.knime.type.CDKCell;

import uk.ac.ebi.masscascade.commons.Evidence;
import uk.ac.ebi.masscascade.compound.CompoundEntity;
import uk.ac.ebi.masscascade.compound.CompoundSpectrum;
import uk.ac.ebi.masscascade.compound.NotationUtil;
import uk.ac.ebi.masscascade.utilities.xyz.XYPoint;

class BrushAggregator {

	private HashMap<Double, AggregationUnit> mzMapping;

	BrushAggregator() {

		mzMapping = new HashMap<>();
	}

	void add(List<CompoundSpectrum> css) {

		for (CompoundSpectrum cs : css) {
			int majorPeak = cs.getMajorPeak() - 1;
			double mz = cs.getPeakList().get(majorPeak).x;
			mz = FastMath.round(mz * 1000.0) / 1000.0; // third decimal point
			if (mzMapping.containsKey(mz)) {
				AggregationUnit au = mzMapping.get(mz);
				au.addMeta(cs.getPeakList().get(majorPeak).y, cs.getRetentionTime());
				for (CompoundEntity ce : cs.getBest(100)) {
					au.add(ce, majorPeak);
				}
			} else {
				XYPoint xy = cs.getPeakList().get(majorPeak);
				AggregationUnit au = new AggregationUnit(mz, cs.getRetentionTime(), xy.y);
				for (CompoundEntity ce : cs.getBest(100)) {
					au.add(ce, majorPeak);
				}
				mzMapping.put(mz, au);
			}
		}
	}
	
	Set<Double> mzs() {
		return mzMapping.keySet();
	}
	
	public Iterator<DataCell[]> rows(final double mz, final int group) {
		
		return new Iterator<DataCell[]>() {

			private int m = 0;
			
			@Override
			public boolean hasNext() {
				return m != mzMapping.get(mz).j;
			}

			@Override
			public DataCell[] next() {
				
				AggregationUnit au = mzMapping.get(mz);
				
				DataCell[] dataCells = new DataCell[8];

				dataCells[0] = new IntCell(group);
				dataCells[1] = new DoubleCell(au.mz);
				List<DoubleCell> timeCells = new ArrayList<>();
				for (double time : au.timeByArea[0]) {
					timeCells.add(new DoubleCell(time));
				}
				List<DoubleCell> areaCells = new ArrayList<>();
				for (double area : au.timeByArea[1]) {
					areaCells.add(new DoubleCell(area));
				}
				dataCells[2] = CollectionCellFactory.createListCell(timeCells);
				dataCells[3] = CollectionCellFactory.createListCell(areaCells);
				dataCells[4] = new StringCell(au.name[m]);

				if (au.notation[m] == null)
					dataCells[5] = DataType.getMissingCell();
				else {
					IAtomContainer molecule = NotationUtil.getMoleculeTyped(au.notation[m]);
					if (molecule == null)
						dataCells[5] = DataType.getMissingCell();
					else
						dataCells[5] = new CDKCell(molecule);
				}
				dataCells[6] = new DoubleCell(au.score[m]);
				dataCells[7] = new StringCell(Evidence.values()[au.evidence[m]].name());
				
				m++;
				
				return dataCells;
			}

			@Override
			public void remove() {
				// nothing to do
			}};
	}

	class AggregationUnit {

		private double mz;
		private double[][] timeByArea;
		private String name[];
		private String notation[];
		private double score[];
		private int evidence[];

		private int i = 0;
		private int j = 0;

		AggregationUnit(double mz, double time, double area) {

			this.mz = mz;

			timeByArea = new double[2][1];
			timeByArea[0][i] = time;
			timeByArea[1][i] = area;
			i++;
			
			name = new String[0];
			notation = new String[0];
			score = new double[0];
			evidence = new int[0];
		}

		void add(CompoundEntity ce, int id) {

			String ceName = ce.getName();
			int m = ArrayUtils.indexOf(name, ceName);
			if (m == ArrayUtils.INDEX_NOT_FOUND) {
				
				name = Arrays.copyOf(name, j + 1);
				notation = Arrays.copyOf(notation, j + 1);
				score = Arrays.copyOf(score, j + 1);
				evidence = Arrays.copyOf(evidence, j + 1);
				
				name[j] = ceName;
				notation[j] = ce.getNotation(id + 1);
				score[j] = ce.getScore();
				evidence[j] = ce.getEvidence().ordinal();
				
				j++;
			} else {
				score[m] = (score[m] + ce.getScore()) / 2.0;
				if (ce.getEvidence().ordinal() > evidence[m]) {
					evidence[m] = ce.getEvidence().ordinal();
				}
			}
		}
		
		void addMeta(double area, double time) {

			timeByArea[0] = Arrays.copyOf(timeByArea[0], i + 1);
			timeByArea[1] = Arrays.copyOf(timeByArea[1], i + 1);
			
			timeByArea[0][i] = time;
			timeByArea[1][i] = area;
			i++;
		}
	}
}
