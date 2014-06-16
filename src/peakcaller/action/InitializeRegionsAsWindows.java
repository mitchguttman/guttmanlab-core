package peakcaller.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.feature.GeneWindow;

/**
 * Initialize the set of candidate regions with sliding windows over the genes
 * @author prussell
 *
 */
public class InitializeRegionsAsWindows implements PeakCallerAction<PeakCaller> {
	
	protected int windowSize;
	protected int stepSize;
	private static Logger logger = Logger.getLogger(InitializeRegionsAsWindows.class.getName());
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public InitializeRegionsAsWindows() {}

	/**
	 * @param window Window size
	 * @param step Step size
	 */
	public InitializeRegionsAsWindows(int window, int step) {
		windowSize = window;
		stepSize = step;
	}
	
	/**
	 * Initialize from a config file line
	 * @param value Config file value
	 */
	public InitializeRegionsAsWindows(ConfigFileOptionValue value) {
		validateConfigFileLine(value);
		windowSize = value.asInt(1);
		stepSize = value.asInt(2);
	}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		logger.info("");
		logger.info("Clearing and initializing candidate windows...");
		if(peakCaller.getGenes() == null) {
			throw new IllegalStateException("Gene set is null");
		}
		if(peakCaller.getGenes().isEmpty()) {
			throw new IllegalStateException("Gene set is empty");
		}
		Map<String, Collection<Annotation>> candidateRegions = new HashMap<String, Collection<Annotation>>();
		int numWindows = 0;
		for(String chr : peakCaller.getGenes().keySet()) {
			logger.info(chr);
			candidateRegions.put(chr, new TreeSet<Annotation>());			
			for(Gene gene : peakCaller.getGenes().get(chr)) {
				Collection<GeneWindow> windows = gene.getWindows(windowSize, stepSize, 0);
				numWindows += windows.size();
				candidateRegions.get(chr).addAll(windows);
			}
		}
		peakCaller.initializeCandidateRegions(candidateRegions);
		logger.info("Added " + numWindows + " initial windows.");
	}

	@Override
	public String getName() {
		return "initialize_regions_as_windows";
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
