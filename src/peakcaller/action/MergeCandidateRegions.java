package peakcaller.action;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import pipeline.ConfigFileOptionValue;


import nextgen.core.annotation.Annotation;
import nextgen.core.utils.AnnotationUtils;

/**
 * Merge overlapping candidate regions in the peak caller object
 * @author prussell
 *
 */
public class MergeCandidateRegions implements PeakCallerAction<PeakCaller> {

	public static Logger logger = Logger.getLogger(MergeCandidateRegions.class.getName());
	
	public MergeCandidateRegions() {}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		logger.info("");
		logger.info("Merging overlapping candidate regions...");
		peakCaller.checkReady();
		Map<String, Collection<Annotation>> candidateRegions = peakCaller.getCandidateRegions();
		for(String chr : candidateRegions.keySet()) {
			int origSize = candidateRegions.get(chr).size();
			TreeSet<Annotation> treeThisChr = new TreeSet<Annotation>();
			treeThisChr.addAll(candidateRegions.get(chr));
			Collection<Annotation> merged = AnnotationUtils.mergeOverlappingBlocks(treeThisChr);
			candidateRegions.put(chr, merged);
			int newSize = candidateRegions.get(chr).size();
			logger.info("On chromosome " + chr + " merged " + origSize + " regions into " + newSize + ".");
		}
		logger.info("Done merging overlapping regions.");
	}

	@Override
	public String getName() {
		return "merge_candidate_regions";
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
		if(value.getActualNumValues() != 1) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Number of fields including flag should be 1: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return getName();
	}

}
