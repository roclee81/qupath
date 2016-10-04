/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package qupath.lib.analysis.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for computing basic statistics from values as they are added.
 * <p>
 * This is useful e.g. when iterating through pixels, computing statistics from masked/labelled values.
 * <p>
 * Warning! This maintains a sum as a double - for many pixels and/or 16-bit data this may lead to imprecision 
 * (although for small regions, and especially optical densities, it should be fine).
 * <p>
 * A warning is logged for particularly large values.
 * 
 * @author Pete Bankhead
 *
 */
public class RunningStatistics {
	
	private static Logger logger = LoggerFactory.getLogger(RunningStatistics.class);
	
	// See http://www.johndcook.com/standard_deviation.html
	
	private static double LARGE_DOUBLE_THRESHOLD = Math.pow(2, 53) - 1; // Largest integer that can be stored, maintaining accuracy of all smaller integers?
	
	public static int MAX_EXPAND = 100;
	
	private int numNaNs = 0;
	
	protected long size = 0;
	private double sum = 0, min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;

	private double m1 = 0, s1 = 0;
	
	
	public RunningStatistics() {}
	
	public long size() {
		return size;
	}
	
	/**
	 * NaNs are ignored
	 * 
	 * @param val
	 */
	public void addValue(double val) {
		if (Double.isNaN(val)) {
			numNaNs++;
			return;
		}
		// Update the count
		size++;
		// Update the running sum, min & max
		sum += val;
		if (val < min)
			min = val;
		if (val > max)
			max = val;
		// Update values for variance calculation
		if (size == 1) {
			m1 = val;
		} else {
			double mNew = m1 + (val - m1) / size;
			s1 = s1 + (val - m1)*(val - mNew);
			m1 = mNew;
		}
	}
	
	public long getNumNaNs() {
		return numNaNs;
	}
	
	public long nPixels() {
		return size;
	}
	
	public double getSum() {
		if (Math.abs(sum) > LARGE_DOUBLE_THRESHOLD)
			logger.warn("Sum in {} is particularly large ({}), beware imprecision!", getClass().getSimpleName(), sum);
		return sum;
	}
	
	public double getMean() {
		return (size == 0) ? Double.NaN : getSum() / size;
	}
	
	public double getVariance() {
		if (Math.abs(s1) > LARGE_DOUBLE_THRESHOLD)
			logger.warn("Variance parameter s1 in {} is particularly large ({}), beware imprecision!", getClass().getSimpleName(), sum);
		return (size <= 1) ? Double.NaN : s1 / (size - 1);
	}
	
	public double getStdDev() {
		return Math.sqrt(getVariance());
	}
	
	public double getMin() {
		return (size == 0) ? Double.NaN : min;
	}
	
	public double getMax() {
		return (size == 0) ? Double.NaN : max;
	}
	
	public double getRange() {
		return (size == 0) ? Double.NaN : max - min;
	}
	
}