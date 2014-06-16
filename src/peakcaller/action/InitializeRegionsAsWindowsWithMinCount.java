package peakcaller.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;
import nextgen.core.model.score.WindowScoreIterator;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import peakcaller.ReadMappingPeakCaller;
import peakcaller.predicate.RawCountFilter;
import peakcaller.score.RawCountScore;
import pipeline.ConfigFileOptionValue;

/**
 * Clear the set of candidate regions and initialize as windows
 * Only store regions with a minimum read count for a specified sample
 * @author prussell
 *
 */
public class InitializeRegionsAsWindowsWithMinCount extends InitializeRegionsAsWindows {

	private static Logger logger = Logger.getLogger(InitializeRegionsAsWindowsWithMinCount.class.getName());
	private String sampleToCheck;
	private double minCount;
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public InitializeRegionsAsWindowsWithMinCount() {}

	/**
	 * @param window Window size
	 * @param step Step size
	 */
	public InitializeRegionsAsWindowsWithMinCount(int window, int step) {
		super(window, step);
	}
	
	/**
	 * Initialize from a config file line
	 * @param value Config file value
	 */
	public InitializeRegionsAsWindowsWithMinCount(ConfigFileOptionValue value) {
		validateConfigFileLine(value);
		windowSize = value.asInt(1);
		stepSize = value.asInt(2);
		sampleToCheck = value.asString(3);
		minCount = value.asDouble(4);
		logger.info("For " + getName() + ", sample being used is " + sampleToCheck + " and min count is " + minCount + ".");
	}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		// The peak caller has to be a read mapping peak caller
		try {
			ReadMappingPeakCaller rmpc = (ReadMappingPeakCaller) peakCaller;
			logger.info("");
			logger.info("Clearing and initializing candidate windows...");
			logger.info("Enforcing a minimum count of " + minCount + " in sample " + sampleToCheck + ".");
			if(rmpc.getGenes() == null) {
				throw new IllegalStateException("Gene set is null");
			}
			if(rmpc.getGenes().isEmpty()) {
				throw new IllegalStateException("Gene set is empty");
			}
			Map<String, Collection<Annotation>> candidateRegions = new HashMap<String, Collection<Annotation>>();
			int numWindowsPassed = 0;
			int numWindowsFailed = 0;
			int overlap = windowSize - stepSize;
			// Initialize the read mapping data, score and filter objects
			AlignmentModel data = rmpc.getData().get(sampleToCheck);
			RawCountScore rcs = new RawCountScore(data);
			RawCountFilter rcf = new RawCountFilter(rcs, minCount);
			
			for(String chr : rmpc.getGenes().keySet()) {
				logger.info(chr);
				candidateRegions.put(chr, new TreeSet<Annotation>());			
				for(Gene gene : rmpc.getGenes().get(chr)) {
					WindowScoreIterator<CountScore> scoreIter = data.scan(gene, windowSize, overlap);
					while(scoreIter.hasNext()) {
						CountScore score = scoreIter.next();
						if(rcf.passes(score)) {
							numWindowsPassed++;
							Annotation window = score.getAnnotation();
							candidateRegions.get(chr).add(window);
						} else {
							numWindowsFailed++;
						}
					}
				}
			}
			rmpc.initializeCandidateRegions(candidateRegions);
			logger.info("Added " + numWindowsPassed + " initial windows. " + numWindowsFailed + " windows did not pass raw count filter.");
		} catch(ClassCastException e) {
			throw new ClassCastException("Peak caller must be a read mapping peak caller.");
		}
	}

	@Override
	public String getName() {
		return "initialize_regions_as_windows_with_min_count";
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
		if(value.getActualNumValues() != 5) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Number of fields including flag should be 5: " + value.getFullOptionLine());
		}
		// Check that the values are correctly formatted
		try {
			@SuppressWarnings("unused")
			int w = value.asInt(1);
			@SuppressWarnings("unused")
			int s = value.asInt(2);
			@SuppressWarnings("unused")
			double c = value.asDouble(4);
		} catch(NumberFormatException e) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Second and third fields must be integers and fifth field must be double: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return(getName() + "\t<window_size>\t<step_size>\t<sample_name_to_use_for_min_count>\t<min_count>");
	}


}
