package peakcaller.action;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import pipeline.ConfigFileOptionValue;

import broad.core.annotation.MaximumContiguousSubsequence;
import broad.core.math.Statistics;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.annotation.Annotation.Strand;
import nextgen.core.model.AlignmentModel;

/**
 * Trim each candidate region using the position-level counts over the region from a specified file
 * Uses the trim max contiguous algorithm
 * @author prussell
 *
 */
public class TrimMaxContiguous implements PeakCallerAction<PeakCaller> {

	private static Logger logger = Logger.getLogger(TrimMaxContiguous.class.getName());
	private double trimQuantile;
	private AlignmentModel alignmentData;
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public TrimMaxContiguous() {}
	
	/**
	 * @param quantile Quantile parameter for trim max contiguous algorithm
	 */
	public TrimMaxContiguous(AlignmentModel data, double quantile) {
		alignmentData = data;
		trimQuantile = quantile;
	}
	
	/**
	 * @param value Config file value
	 * @param data Alignment data by sample name
	 */
	public TrimMaxContiguous(ConfigFileOptionValue value, Map<String, AlignmentModel> data) {
		validateConfigFileLine(value);
		trimQuantile = value.asDouble(1);
		String sampleName = value.asString(2);
		if(!data.containsKey(sampleName)) {
			throw new IllegalArgumentException("Data map does not contain key " + sampleName + ".");
		}
		alignmentData = data.get(sampleName);
	}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		logger.info("");
		logger.info("Trimming candidate regions...");
		peakCaller.checkReady();
		Map<String, Collection<Annotation>> candidateRegions = peakCaller.getCandidateRegions();
		for(String chr : candidateRegions.keySet()) {
			TreeSet<Annotation> treeThisChr = new TreeSet<Annotation>();
			treeThisChr.addAll(candidateRegions.get(chr));
			candidateRegions.put(chr, new TreeSet<Annotation>());
			int origSize = 0;
			int newSize = 0;
			for(Annotation region : treeThisChr) {
				try {
					origSize += region.getSize();
					List<Double> coverageData = alignmentData.getPositionCountList(new Gene(region));
					Annotation trimmed = TrimMaxContiguous.trimMaxContiguous(region, coverageData, trimQuantile);
					candidateRegions.get(chr).add(trimmed);
				} catch (IOException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					System.exit(-1);
				}

			}
			for(Annotation region : candidateRegions.get(chr)) {
				newSize += region.getSize();
			}
			logger.info("On chromosome " + chr + " total size went from " + origSize + " to " + newSize + ".");
		}
		logger.info("Done trimming regions.");
	}

	@Override
	public String getName() {
		return "trim_max_contiguous";
	}

	/**
	 * Trim the region to max contiguous subregion above a certain quantile
	 * @param window The region
	 * @param data Position level list of counts within the region
	 * @param quantile Quantile for trim max contiguous
	 * @return Trimmed region
	 */
	public static Annotation trimMaxContiguous(Annotation window, List<Double> data, double quantile) {
				
		if(window.getSize() != data.size()) {
			throw new IllegalArgumentException(window.toBED() + "\nAnnotation and data must have same size. Size=" + window.getSize() + " Data_size=" + data.size());
		}
		
		double[] array = new double[data.size()];
		for(int i=0; i < data.size(); i++) {
			array[i] = data.get(i).doubleValue();
		}
		Collections.sort(data);
		
		double cutoff = Statistics.quantile(data, quantile);
		for(int j=0; j<array.length; j++){
			double d = array[j] - cutoff;
			array[j] = d;
		}
	
		double[] maxSum = MaximumContiguousSubsequence.maxSubSum3(array);
			
		if(maxSum[0] > 0){
			int deltaStart = new Double(maxSum[1]).intValue();
			int deltaEnd =  new Double(data.size() - 1 - maxSum[2]).intValue();
			if(window.getStrand().equals(Strand.NEGATIVE)) {
			    int tmpStart = deltaStart;
			    deltaStart = deltaEnd;
			    deltaEnd = tmpStart;
			}
			window = window.trim(deltaStart, deltaEnd);
		}
				
		return window;
	}

	@Override
	public void validateConfigFileLine(ConfigFileOptionValue value) {
		// Check that flag is the name of this action
		if(!value.getFlag().equals(getName())) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Flag (" + value.getFlag() + ") does not match name " + getName() + ")");
		}
		// Check for correct number of arguments
		if(value.getActualNumValues() != 3) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Number of fields including flag should be 3: " + value.getFullOptionLine());
		}
		// Check that middle value is a double
		try {
			@SuppressWarnings("unused")
			double d = value.asDouble(1);
		} catch(NumberFormatException e) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Middle field must be a double: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return getName() + "\t<percentile_btw_0_and_1>\t<sample_name_to_use>";
	}

}
