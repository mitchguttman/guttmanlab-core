package peakcaller.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;
import peakcaller.ReadMappingPeakCaller;
import pipeline.ConfigFileOptionValue;

/**
 * Save counts over the parent genes
 * @author prussell
 *
 */
public class InitializeGeneCounts implements PeakCallerAction<ReadMappingPeakCaller> {

	private static Logger logger = Logger.getLogger(InitializeGeneCounts.class.getName());
	
	public InitializeGeneCounts() {}
	
	@Override
	public void doWork(ReadMappingPeakCaller peakCaller) {
		logger.info("");
		logger.info("Clearing and saving counts for parent genes...");
		Map<String, Map<Gene, CountScore>> counts = new HashMap<String, Map<Gene, CountScore>>();
		if(peakCaller.getGenes() == null) {
			throw new IllegalStateException("Set of genes is null");
		}
		if(peakCaller.getGenes().isEmpty()) {
			throw new IllegalStateException("Set of genes is empty");
		}
		for(String sample : peakCaller.getData().keySet()) {
			logger.info(sample);
			counts.put(sample, new HashMap<Gene, CountScore>());
			AlignmentModel model = peakCaller.getData().get(sample);
			for(String chr : peakCaller.getGenes().keySet()) {
				logger.info(chr);
				for(Gene gene : peakCaller.getGenes().get(chr)) {
					CountScore score = new CountScore(model, gene);
					counts.get(sample).put(gene, score);
				}
			}
		}
		peakCaller.initializeGeneCountsCache(counts);
		logger.info("Done initializing gene counts.");
	}
	
	@Override
	public String getName() {
		return "initialize_gene_counts";
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
