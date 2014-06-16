package peakcaller.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

import org.apache.log4j.Logger;

import peakcaller.ReadMappingPeakCaller;
import peakcaller.predicate.BinomialFilter;
import peakcaller.score.BinomialScore;
import pipeline.ConfigFileOptionValue;

/**
 * Apply a filter that removes candidate regions with binomial P value above a threshold
 * Specify the control sample and sample of interest
 * Binomial parameters are calculated relative to the parent gene
 * @author prussell
 *
 */
public class ApplyBinomialFilter implements PeakCallerAction<ReadMappingPeakCaller> {

	private static Logger logger = Logger.getLogger(ApplyBinomialFilter.class.getName());
	private BinomialFilter filter;
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public ApplyBinomialFilter() {}

	/**
	 * @param value Config file line
	 * @param alignmentData Alignment data by sample name
	 */
	public ApplyBinomialFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> alignmentData) {
		this(value, alignmentData, null, null);
	}
	
	
	/**
	 * @param value Config file line
	 * @param alignmentData Alignment data by sample name
	 * @param regionCounts Cache of region counts for by sample name or null if not using
	 * @param geneCounts Cache of parent gene counts by sample name or null if not using
	 */
	public ApplyBinomialFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> alignmentData, Map<String, Map<Annotation, CountScore>> regionCounts, Map<String, Map<Gene, CountScore>> geneCounts) {
		validateConfigFileLine(value);
		double maxPval = value.asDouble(1);
		String control = value.asString(2);
		String sample = value.asString(3);
		logger.info("For binomial filter, max P value is " + maxPval + ". Control sample is " + control + ". Signal sample is " + sample);
		AlignmentModel controlData = alignmentData.get(control);
		AlignmentModel sampleData = alignmentData.get(sample);
		Map<Annotation, CountScore> controlCounts = null;
		Map<Annotation, CountScore> sampleCounts = null;
		Map<Gene, CountScore> ctrlGeneCounts = null;
		Map<Gene, CountScore> smplGeneCounts = null;
		if(regionCounts != null) {
			controlCounts = regionCounts.get(control);
			sampleCounts = regionCounts.get(sample);
		}
		if(geneCounts != null) {
			ctrlGeneCounts = geneCounts.get(control);
			smplGeneCounts = geneCounts.get(sample);
		}
		BinomialScore score = new BinomialScore(controlData, sampleData, controlCounts, sampleCounts, ctrlGeneCounts, smplGeneCounts);
		filter = new BinomialFilter(score, maxPval);
	}
	
	@Override
	public void doWork(ReadMappingPeakCaller peakCaller) {
		logger.info("");
		logger.info("Applyling binomial filter...");
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
		logger.info("Finished applying binomial filter.");
	}

	@Override
	public String getName() {
		return "apply_binomial_filter";
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
		if(value.getActualNumValues() != 4) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Number of fields including flag should be 4: " + value.getFullOptionLine());
		}
		// Check that second value is a double
		try {
			@SuppressWarnings("unused")
			double d = value.asDouble(1);
		} catch(NumberFormatException e) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Second field must be a double between 0 and 1: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return(getName() + "\t<max_pval>\t<control_sample_name>\t<sample_name_to_use>");
	}

}
