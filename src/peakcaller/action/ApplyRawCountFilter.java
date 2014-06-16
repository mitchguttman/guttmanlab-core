package peakcaller.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import peakcaller.predicate.RawCountFilter;
import peakcaller.score.RawCountScore;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * Apply a filter that removes candidate regions with count below a threshold in a specified sample
 * @author prussell
 *
 */
public class ApplyRawCountFilter implements PeakCallerAction<PeakCaller> {
	
	private RawCountFilter filter;
	private static Logger logger = Logger.getLogger(ApplyRawCountFilter.class.getName());
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public ApplyRawCountFilter() {}
	
	/**
	 * @param data Alignment data to use for raw counts
	 * @param minCount Minimum count for a region to pass filter
	 */
	public ApplyRawCountFilter(AlignmentModel data, double minCount) {
		this(data, minCount, null);
	}
	
	/**
	 * @param data Alignment data to use for raw counts
	 * @param minCount Minimum count for a region to pass filter
	 */
	public ApplyRawCountFilter(AlignmentModel data, double minCount, Map<Annotation, CountScore> existingCounts) {
		RawCountScore score = new RawCountScore(data, existingCounts);
		filter = new RawCountFilter(score, minCount);
	}

	/**
	 * Instantiate with a line from a user supplied config file
	 * @param value Config file value
	 * @param data Alignment data by sample name
	 * @param existingCounts Existing count scores to use if possible, by sample name and region, or null if not using
	 */
	public ApplyRawCountFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> data) {
		this(value, data, null);
	}
	
	/**
	 * Instantiate with a line from a user supplied config file
	 * @param value Config file value
	 * @param data Alignment data by sample name
	 * @param existingCounts Existing count scores to use if possible, by sample name and region, or null if not using
	 */
	public ApplyRawCountFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> data, Map<String, Map<Annotation, CountScore>> existingCounts) {
		validateConfigFileLine(value);
		double minCount = value.asDouble(1);
		String sampleName = value.asString(2);
		if(!data.containsKey(sampleName)) {
			throw new IllegalArgumentException("Data map does not contain key " + sampleName + ".");
		}
		AlignmentModel am = data.get(sampleName);

		if(existingCounts != null) {
			if(!existingCounts.containsKey(sampleName)) {
				throw new IllegalArgumentException("Counts map does not contain key " + sampleName + ".");
			}
			Map<Annotation, CountScore> scores = existingCounts.get(sampleName);
			RawCountScore score = new RawCountScore(am, scores);
			filter = new RawCountFilter(score, minCount);
		} else {
			RawCountScore score = new RawCountScore(am);
			filter = new RawCountFilter(score, minCount);
		}
	}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		logger.info("");
		logger.info("Applyling raw count filter...");
		peakCaller.checkReady();
		Map<String, Collection<Annotation>> candidateRegions = peakCaller.getCandidateRegions();
		for(String chr : candidateRegions.keySet()) {
			int removed = 0;
			int origSize = candidateRegions.get(chr).size();
			Iterator<Annotation> iter = candidateRegions.get(chr).iterator();
			while(iter.hasNext()) {
				Annotation region = iter.next();
				if(!filter.passes(region)) {
					iter.remove();
					removed++;
				}
			}
			logger.info("Finished chromosome " + chr + ". Removed " + removed + " of " + origSize + " regions. " + candidateRegions.get(chr).size() + " regions remain.");
		}
		logger.info("Finished applying raw count filter.");
	}

	@Override
	public String getName() {
		return "apply_raw_count_filter";
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
		return(getName() + "\t<min_count>\t<sample_name_to_use>");
	}

}
