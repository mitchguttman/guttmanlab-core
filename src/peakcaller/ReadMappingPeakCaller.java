package peakcaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nextgen.core.alignment.Alignment;
import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.coordinatesystem.TranscriptomeSpace;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;

import peakcaller.action.ActionFactory;
import peakcaller.action.ApplyBinomialFilter;
import peakcaller.action.ApplyGeneFPKMFilter;
import peakcaller.action.ApplyRawCountFilter;
import peakcaller.action.InitializeGeneCounts;
import peakcaller.action.InitializeRegionsAsWindowsWithMinCount;
import peakcaller.action.TrimMaxContiguous;
import peakcaller.predicate.ReadFilterFactory;
import pipeline.ConfigFile;
import pipeline.ConfigFileOption;
import pipeline.ConfigFileOptionValue;
import pipeline.ConfigFileSection;

import broad.core.parser.CommandLineParser;

/**
 * Peak caller that uses set(s) of mapped reads
 */
public class ReadMappingPeakCaller extends PeakCaller {
	
	/**
	 * Read filters by sample name
	 */
	private Map<String, Collection<Predicate<Alignment>>> readFilters;
	
	private static Logger logger = Logger.getLogger(ReadMappingPeakCaller.class.getName());
	
	/**
	 * Alignment model by sample name
	 */
	private Map<String, AlignmentModel> data;

	/**
	 * Count scores for initial set of regions
	 * Key is sample name
	 */
	private Map<String, Map<Annotation, CountScore>> countsCache;
		
	/**
	 * Count scores for parent genes
	 * Key is sample name
	 */
	private Map<String, Map<Gene, CountScore>> geneCountsCache;
		
	private static String bamFlag = "bam";
	private static String readGenomicSpanFilterFlag = ReadFilterFactory.GENOMIC_SPAN_FILTER_NAME;
	private static String readNumHitsFilterFlag = ReadFilterFactory.NUM_HITS_FILTER_NAME;
	private static String geneFpkmFilterFlag = new ApplyGeneFPKMFilter().getName();
	private static String regionCountFilterFlag = new ApplyRawCountFilter().getName();
	private static String regionBinomialFilterFlag = new ApplyBinomialFilter().getName();
	private static String trimMaxContiguousFlag = new TrimMaxContiguous().getName();
	private static String initializeGeneCountsFlag = new InitializeGeneCounts().getName();
	private static String initializeRegionsAsWindowsWithMinCountFlag = new InitializeRegionsAsWindowsWithMinCount().getName();
	//private static String regionReplicateFilterFlag = new ActionApplyReplicateFilter().getName(); //TODO implement replicate filter
	//private static String regionStrandFilterFlag = new ActionApplyStrandFilter().getName(); //TODO implement strand filter
	
	private static ConfigFileOption bamOption = new ConfigFileOption(bamFlag, 3, false, true, true);
	private static ConfigFileOption readGenomicSpanFilterOption = new ConfigFileOption(readGenomicSpanFilterFlag, 3, false, true, false);
	private static ConfigFileOption readNumHitsFilterOption = new ConfigFileOption(readNumHitsFilterFlag, 3, false, true, false);
	private static ConfigFileOption geneFpkmFilterOption = new ConfigFileOption(geneFpkmFilterFlag, 3, false, true, false);
	private static ConfigFileOption regionCountFilterOption = new ConfigFileOption(regionCountFilterFlag, 3, false, true, false);
	private static ConfigFileOption initializeGeneCountsOption = new ConfigFileOption(initializeGeneCountsFlag, 1, false, true, false);
	private static ConfigFileOption regionBinomialFilterOption = new ConfigFileOption(regionBinomialFilterFlag, 4, false, true, false);
	private static ConfigFileOption trimMaxContiguousOption = new ConfigFileOption(trimMaxContiguousFlag, 3, false, true, false);
	private static ConfigFileOption initializeRegionsAsWindowsWithMinCountOption = new ConfigFileOption(initializeRegionsAsWindowsWithMinCountFlag, 5, false, true, false);
	//private static ConfigFileOption regionReplicateFilterOption = new ConfigFileOption(regionReplicateFilterFlag, 3, false, true, false); //TODO implement replicate filter
	//private static ConfigFileOption regionStrandFilterOption = new ConfigFileOption(regionStrandFilterFlag, 3, false, true, false); //TODO implement strand filter
	
	private static String dataSectionFlag = "data";
	private static String readFiltersSectionFlag = "read_filters";
	private static ConfigFileSection dataSection = new ConfigFileSection(dataSectionFlag, true);
	private static ConfigFileSection readFiltersSection = new ConfigFileSection(readFiltersSectionFlag, false);
	
	
	private ReadMappingPeakCaller(String config) throws IOException {
		super(config);
		makeAlignmentModels();
		loadAndAddReadFilters();
	}
	
	
	
	/**
	 * Load alignment models from bam files specified in config file
	 * Use a transcriptome space with the genes of interest
	 */
	private void makeAlignmentModels() {
		logger.info("");
		logger.info("Making alignment models...");
		data = new HashMap<String, AlignmentModel>();
		TranscriptomeSpace transcriptomeSpace = new TranscriptomeSpace(genes);
		Collection<ConfigFileOptionValue> vals = configFile.getOptionValues(dataSection, bamOption);
		for(ConfigFileOptionValue val : vals) {
			String name = val.asString(1);
			String bam = val.asString(2);
			logger.info("Adding name=" + name + " bam=" + bam);
			AlignmentModel model = new AlignmentModel(bam, transcriptomeSpace);
			if(data.containsKey(name)) {
				throw new IllegalStateException("Data map already contains key " + name);
			}
			data.put(name, model);
		}
	}

	/**
	 * Load the read filters listed in the config file and add to alignment models
	 */
	private void loadAndAddReadFilters() {
		logger.info("");
		logger.info("Loading read filters");
		readFilters = new HashMap<String, Collection<Predicate<Alignment>>>();
		List<ConfigFileOptionValue> values = configFile.getOrderedValues(readFiltersSection);
		for(ConfigFileOptionValue value : values) {
			String sampleName = ReadFilterFactory.getSampleName(value);
			Predicate<Alignment> filter = ReadFilterFactory.getReadFilter(value);
			if(!readFilters.containsKey(sampleName)) {
				readFilters.put(sampleName, new ArrayList<Predicate<Alignment>>());
			}
			readFilters.get(sampleName).add(filter);
			logger.info("Added filter " + ReadFilterFactory.getFilterName(value) + " for sample " + sampleName + ".");
		}
		// Add the filters to the alignment models
		for(String sampleName : data.keySet()) {
			data.get(sampleName).addFilters(readFilters.get(sampleName));
		}
	}
	
	@Override
	protected void populateConfigFileSections() {
		super.populateConfigFileSections();
		dataSection.addAllowableOption(bamOption);
		readFiltersSection.addAllowableOption(readGenomicSpanFilterOption);
		readFiltersSection.addAllowableOption(readNumHitsFilterOption);
		stepsSection.addAllowableOption(geneFpkmFilterOption);
		stepsSection.addAllowableOption(regionBinomialFilterOption);
		stepsSection.addAllowableOption(regionCountFilterOption);
		stepsSection.addAllowableOption(initializeRegionsAsWindowsWithMinCountOption);
		//stepsSection.addAllowableOption(regionReplicateFilterOption); //TODO implement replicate filter
		//stepsSection.addAllowableOption(regionStrandFilterOption); //TODO implement strand filter
		stepsSection.addAllowableOption(trimMaxContiguousOption);
		stepsSection.addAllowableOption(initializeGeneCountsOption);
	}
	
	
	
	@Override
	protected void createConfigFile(String fileName) throws IOException {
		logger.info("");
		logger.info("Getting config file from " + fileName);
		populateConfigFileSections();
		Collection<ConfigFileSection> sections = new ArrayList<ConfigFileSection>();
		sections.add(schedulerSection);
		sections.add(dataSection);
		sections.add(readFiltersSection);
		sections.add(stepsSection);
		sections.add(genesSection);
		configFile = new ConfigFile(sections, fileName);
	}

	/**
	 * Get the alignment data
	 * @return The alignment data by sample name
	 */
	public Map<String, AlignmentModel> getData() {
		return data;
	}
	
	/**
	 * Reset the counts cache and populate with the given set
	 * @param counts Counts to initialize with
	 */
	public void initializeCountsCache(Map<String, Map<Annotation, CountScore>> counts) {
		countsCache = new HashMap<String, Map<Annotation, CountScore>>();
		countsCache.putAll(counts);
	}
	
	
	/**
	 * Reset the gene counts cache and populate with the given set
	 * @param counts Counts to initialize with
	 */
	public void initializeGeneCountsCache(Map<String, Map<Gene, CountScore>> counts) {
		geneCountsCache = new HashMap<String, Map<Gene, CountScore>>();
		geneCountsCache.putAll(counts);
	}
	
	
	public static void main(String[] args) throws IOException {
		
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-c", "Config file", true);
		p.parse(args);
		String config = p.getStringArg("-c");
		
		ReadMappingPeakCaller peakCaller = new ReadMappingPeakCaller(config);
		ActionFactory actionFactory = new ActionFactory(peakCaller.data, peakCaller.countsCache);
		peakCaller.performActions(actionFactory);
		
		logger.info("");
		logger.info("All done.");
		
	}
	
	
}
