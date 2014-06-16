package peakcaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import peakcaller.action.ActionFactory;
import peakcaller.action.InitializeRegionsAsWindows;
import peakcaller.action.MergeCandidateRegions;
import peakcaller.action.PeakCallerAction;
import peakcaller.action.WriteCandidateRegions;
import pipeline.ConfigFile;
import pipeline.ConfigFileOption;
import pipeline.ConfigFileSection;

import broad.pda.annotation.BEDFileParser;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;

/**
 * A generic peak caller
 * Maintains collections of parent genes and candidate regions
 * Applies actions to change the set of candidate regions
 * Parameters and actions are specified in a config file
 * @author prussell
 *
 */
public class PeakCaller {
	
	protected Map<String, Collection<Gene>> genes;
	protected Map<String, Collection<Annotation>> candidateRegions;
	protected ConfigFile configFile;
	private static Logger logger = Logger.getLogger(PeakCaller.class.getName());
	
	private static String schedulerFlag = "scheduler";
	private static String mergeRegionsFlag = new MergeCandidateRegions().getName();
	private static String writeRegionsFlag = new WriteCandidateRegions().getName();
	private static String geneBedFlag = "genes_bed";
	private static String initializeRegionsAsWindowsFlag = new InitializeRegionsAsWindows().getName();

	private static ConfigFileOption schedulerOption = new ConfigFileOption(schedulerFlag, 2, false, false, false);
	private static ConfigFileOption mergeRegionsOption = new ConfigFileOption(mergeRegionsFlag, 1, false, true, false);
	private static ConfigFileOption writeRegionsOption = new ConfigFileOption(writeRegionsFlag, 2, false, true, false);
	private static ConfigFileOption geneBedOption = new ConfigFileOption(geneBedFlag, 2, false, false, true);
	private static ConfigFileOption initializeRegionsAsWindowsOption = new ConfigFileOption(initializeRegionsAsWindowsFlag, 3, false, true, false);

	private static String stepsSectionFlag = "steps";
	private static String genesSectionFlag = "genes";
	protected static ConfigFileSection schedulerSection = new ConfigFileSection(schedulerFlag, false);
	protected static ConfigFileSection genesSection = new ConfigFileSection(genesSectionFlag, true);
	public static ConfigFileSection stepsSection = new ConfigFileSection(stepsSectionFlag, true);
	

	
	public PeakCaller(String config) throws IOException {
		logger.info("");
		logger.info("Initializing peak caller...");
		createConfigFile(config);
		loadGenes();
	}
	
	/**
	 * Add options to the config file sections
	 */
	protected void populateConfigFileSections() {
		schedulerSection.addAllowableOption(schedulerOption);
		genesSection.addAllowableOption(geneBedOption);
		stepsSection.addAllowableOption(initializeRegionsAsWindowsOption);
		stepsSection.addAllowableOption(mergeRegionsOption);
		stepsSection.addAllowableOption(writeRegionsOption);
	}
	
	/**
	 * Create config file object by reading from a file
	 * Uses the sections and options specified in static fields
	 * @param fileName File name
	 * @return Config file object which can return option values
	 * @throws IOException
	 */
	protected void createConfigFile(String fileName) throws IOException {
		logger.info("Getting config file " + fileName);
		populateConfigFileSections();
		Collection<ConfigFileSection> sections = new ArrayList<ConfigFileSection>();
		sections.add(schedulerSection);
		sections.add(genesSection);
		sections.add(stepsSection);
		configFile = new ConfigFile(sections, fileName);
	}
	
	/**
	 * Load the genes of interest from the bed file specified in config file
	 * @throws IOException
	 */
	protected void loadGenes() throws IOException {
		String geneBed = configFile.getSingleValueString(genesSection, geneBedOption);
		logger.info("");
		logger.info("Loading genes from " + geneBed);
		genes = BEDFileParser.loadDataByChr(new File(geneBed));
	}

	
	/**
	 * Perform the actions specified in order in the config file
	 * @param factory An action factory object
	 */
	protected void performActions(ActionFactory factory) {
		logger.info("");
		logger.info("Performing actions in config file...");
		List<PeakCallerAction<? extends PeakCaller>> actions = factory.getActions(configFile);
		for(PeakCallerAction action : actions) {
			action.doWork(this);
		}
	}
	
	
	/**
	 * Get the set of candidate regions by chromosome
	 * @return Candidate regions by chromosome
	 */
	public Map<String, Collection<Annotation>> getCandidateRegions() {
		return candidateRegions;
	}
	
	/**
	 * Clear the set of candidate regions and initialize with the provided regions
	 * @param initialRegions The candidate regions to initialize with
	 */
	public void initializeCandidateRegions(Map<String, Collection<Annotation>> initialRegions) {
		candidateRegions = new TreeMap<String, Collection<Annotation>>();
		candidateRegions.putAll(initialRegions);
	}
	
	/**
	 * Get the set of candidate regions by chromosome
	 * @return Candidate regions by chromosome
	 */
	public Map<String, Collection<Gene>> getGenes() {
		return genes;
	}
	
	/**
	 * Check that the peak caller is ready to be used
	 * Throw an exception if there is a problem
	 */
	public void checkReady() {
		if(candidateRegions == null) {
			throw new IllegalStateException("Set of candidate regions is null");
		}
		if(genes == null) {
			throw new IllegalStateException("Set of genes is null");
		}
		if(genes.isEmpty()) {
			throw new IllegalStateException("Set of genes is empty");
		}
		if(candidateRegions.isEmpty()) {
			logger.warn("Set of candidate regions is empty");
		}
	}
	

}
