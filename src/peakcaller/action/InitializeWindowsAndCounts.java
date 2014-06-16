package peakcaller.action;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import peakcaller.ReadMappingPeakCaller;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;
import nextgen.core.model.score.WindowScoreIterator;

/**
 * Initialize the set of candidate regions with sliding windows over the genes
 * Scan the windows and save counts
 * Deprecated because it's inefficient - should apply some filter instead of storing counts at this stage
 * @author prussell
 *
 */
public class InitializeWindowsAndCounts implements PeakCallerAction<ReadMappingPeakCaller> {

	private int windowSize;
	private int stepSize;
	private static Logger logger = Logger.getLogger(InitializeWindowsAndCounts.class.getName());
	
	/**
	 * Deprecated: this is an inefficient way to handle a cache.
	 * Just for getting name
	 * Do not use
	 */
	@Deprecated 
	public InitializeWindowsAndCounts() {}

	/**
	 * Deprecated: this is an inefficient way to handle a cache.
	 * @param window Window size
	 * @param step Step size
	 */
	@Deprecated
	public InitializeWindowsAndCounts(int window, int step) {
		windowSize = window;
		stepSize = step;
	}

	/**
	 * Deprecated: this is an inefficient way to handle a cache.
	 * Initialize from a config file line
	 * @param value Config file value
	 */
	@Deprecated
	public InitializeWindowsAndCounts(ConfigFileOptionValue value) {
		validateConfigFileLine(value);
		windowSize = value.asInt(1);
		stepSize = value.asInt(2);
	}
	

	@Override
	public void doWork(ReadMappingPeakCaller peakCaller) {
		// Initialize the windows
		InitializeRegionsAsWindows irw = new InitializeRegionsAsWindows(windowSize, stepSize);
		irw.doWork(peakCaller);
		// Save the counts
		logger.info("");
		logger.info("Clearing and saving counts for initial candidate regions...");
		Map<String, Map<Annotation, CountScore>> counts = new HashMap<String, Map<Annotation, CountScore>>();
		if(peakCaller.getCandidateRegions() == null) {
			throw new IllegalStateException("Set of candidate regions is null");
		}
		if(peakCaller.getCandidateRegions().isEmpty()) {
			throw new IllegalStateException("Set of candidate regions is empty");
		}
		int overlap = windowSize - stepSize;
		for(String sample : peakCaller.getData().keySet()) {
			logger.info(sample);
			counts.put(sample, new HashMap<Annotation, CountScore>());
			AlignmentModel model = peakCaller.getData().get(sample);
			for(String chr : peakCaller.getGenes().keySet()) {
				logger.info(chr);
				for(Gene gene : peakCaller.getGenes().get(chr)) {
					WindowScoreIterator<CountScore> iter = model.scan(gene, windowSize, overlap);
					while(iter.hasNext()) {
						CountScore score = iter.next();
						Annotation region = score.getAnnotation();
						if(!peakCaller.getCandidateRegions().get(chr).contains(region)) {
							logger.warn("Set of candidate regions does not contain window " + region.toUCSC());
						}
						counts.get(sample).put(region, score);
					}
				}
			}
		}
		peakCaller.initializeCountsCache(counts);
		logger.info("Done initializing counts.");
	}

	@Override
	public String getName() {
		return "initialize_windows_and_counts";
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
		// Check that the values are ints
		try {
			@SuppressWarnings("unused")
			int w = value.asInt(1);
			@SuppressWarnings("unused")
			int s = value.asInt(2);
		} catch(NumberFormatException e) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Second and third fields must be integers: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return(getName() + "\t<window_size>\t<step_size>");
	}

}
