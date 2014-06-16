package peakcaller.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import peakcaller.ReadMappingPeakCaller;
import peakcaller.predicate.FPKMFilter;
import peakcaller.score.FPKMScore;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * Apply a filter that removes parent genes with FPKM below a threshold in a specified sample
 * @author prussell
 *
 */
public class ApplyGeneFPKMFilter implements PeakCallerAction<ReadMappingPeakCaller> {
	
	private FPKMFilter filter;
	private static Logger logger = Logger.getLogger(ApplyGeneFPKMFilter.class.getName());
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public ApplyGeneFPKMFilter() {}
	
	/**
	 * @param data Alignment data to use for FPKM calculation
	 * @param minFPKM Minimum FPKM for a gene to pass filter
	 */
	public ApplyGeneFPKMFilter(AlignmentModel data, double minFPKM) {
		this(data, minFPKM, null);
	}
	
	/**
	 * @param data Alignment data to use for FPKM calculation
	 * @param minFPKM Minimum FPKM for a region to pass filter
	 * @param existingScores An existing cache of count score objects. This will be used if the gene is represented in the map; otherwise, new score is computed.
	 */
	public ApplyGeneFPKMFilter(AlignmentModel data, double minFPKM, Map<Annotation, CountScore> existingScores) {
		FPKMScore score = new FPKMScore(data, existingScores);
		filter = new FPKMFilter(score, minFPKM);
	}

	/**
	 * Instantiate with a line from a user supplied config file
	 * @param value Config file value
	 * @param data Alignment data by sample name
	 */
	public ApplyGeneFPKMFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> data) {
		this(value, data, null);
	}
	
	/**
	 * Instantiate with a line from a user supplied config file
	 * @param value Config file value
	 * @param data Alignment data by sample name
	 * @param existingGeneCounts Existing scores to use if possible, by sample name and region, or null if not using
	 */
	public ApplyGeneFPKMFilter(ConfigFileOptionValue value, Map<String, AlignmentModel> data, Map<String, Map<Gene, CountScore>> existingGeneCounts) {
		validateConfigFileLine(value);
		double minFPKM = value.asDouble(1);
		String sampleName = value.asString(2);
		if(!data.containsKey(sampleName)) {
			throw new IllegalArgumentException("Data map does not contain key " + sampleName + ".");
		}
		AlignmentModel am = data.get(sampleName);

		if(existingGeneCounts != null) {
			if(!existingGeneCounts.containsKey(sampleName)) {
				throw new IllegalArgumentException("Counts map does not contain key " + sampleName + ".");
			}
			Map<Gene, CountScore> scores = existingGeneCounts.get(sampleName);
			FPKMScore score = new FPKMScore(am, scores);
			filter = new FPKMFilter(score, minFPKM);
		} else {
			FPKMScore score = new FPKMScore(am);
			filter = new FPKMFilter(score, minFPKM);
		}
		logger.info("For gene FPKM filter, min FPKM is " + minFPKM + ".");
	}
	
	@Override
	public void doWork(ReadMappingPeakCaller peakCaller) {
		logger.info("");
		logger.info("Applyling gene FPKM filter...");
		Map<String, Collection<Gene>> genes = peakCaller.getGenes();
		for(String chr : genes.keySet()) {
			int removed = 0;
			int origSize = genes.get(chr).size();
			Iterator<Gene> iter = genes.get(chr).iterator();
			while(iter.hasNext()) {
				Annotation gene = iter.next();
				if(!filter.passes(gene)) {
					iter.remove();
					removed++;
				}
			}
			logger.info("Finished chromosome " + chr + ". Removed " + removed + " of " + origSize + " genes. " + genes.get(chr).size() + " genes remain.");
		}
		logger.info("Finished applying gene FPKM filter.");
	}

	@Override
	public String getName() {
		return "apply_gene_fpkm_filter";
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
		return(getName() + "\t<min_fpkm>\t<sample_name_to_use>");
	}

}
