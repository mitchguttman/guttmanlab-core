package peakcaller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import pipeline.ConfigFile;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * Get a peak caller action from a config file line
 * @author prussell
 *
 */
public class ActionFactory {
	
	private static Logger logger = Logger.getLogger(ActionFactory.class.getName());
	
	private enum ActionName {
		
		WRITE_CANDIDATE_REGIONS,
		APPLY_RAW_COUNT_FILTER,
		MERGE_CANDIDATE_REGIONS,
		INITIALIZE_REGIONS_AS_WINDOWS_WITH_MIN_COUNT,
		INITIALIZE_REGIONS_AS_WINDOWS,
		APPLY_GENE_FPKM_FILTER,
		INITIALIZE_GENE_COUNTS,
		APPLY_BINOMIAL_FILTER,
		TRIM_MAX_CONTIGUOUS;
		
		public String toString() {
			switch(this) {
			case APPLY_RAW_COUNT_FILTER:
				return new ApplyRawCountFilter().getName();
			case APPLY_GENE_FPKM_FILTER:
				return new ApplyGeneFPKMFilter().getName();
			case MERGE_CANDIDATE_REGIONS:
				return new MergeCandidateRegions().getName();
			case TRIM_MAX_CONTIGUOUS:
				return new TrimMaxContiguous().getName();
			case WRITE_CANDIDATE_REGIONS:
				return new WriteCandidateRegions().getName();
			case INITIALIZE_REGIONS_AS_WINDOWS:
				return new InitializeRegionsAsWindows().getName();
			case INITIALIZE_GENE_COUNTS:
				return new InitializeGeneCounts().getName();
			case APPLY_BINOMIAL_FILTER:
				return new ApplyBinomialFilter().getName();
			case INITIALIZE_REGIONS_AS_WINDOWS_WITH_MIN_COUNT:
				return new InitializeRegionsAsWindowsWithMinCount().getName();
			default:
				throw new IllegalStateException("Missing from switch statement: " + toString());
			}
		}
		
		public static ActionName fromString(String name) {
			if(name.equals(new ApplyRawCountFilter().getName())) {
				return APPLY_RAW_COUNT_FILTER;
			}
			if(name.equals(new MergeCandidateRegions().getName())) {
				return MERGE_CANDIDATE_REGIONS;
			}
			if(name.equals(new TrimMaxContiguous().getName())) {
				return TRIM_MAX_CONTIGUOUS;
			}
			if(name.equals(new WriteCandidateRegions().getName())) {
				return WRITE_CANDIDATE_REGIONS;
			}
			if(name.equals(new InitializeRegionsAsWindows().getName())) {
				return INITIALIZE_REGIONS_AS_WINDOWS;
			}
			if(name.equals(new ApplyGeneFPKMFilter().getName())) {
				return APPLY_GENE_FPKM_FILTER;
			}
			if(name.equals(new InitializeGeneCounts().getName())) {
				return INITIALIZE_GENE_COUNTS;
			}
			if(name.equals(new ApplyBinomialFilter().getName())) {
				return APPLY_BINOMIAL_FILTER;
			}
			if(name.equals(new InitializeRegionsAsWindowsWithMinCount().getName())) {
				return INITIALIZE_REGIONS_AS_WINDOWS_WITH_MIN_COUNT;
			}
			throw new IllegalStateException("Name missing: " + name);
		}
		
	}
	
	/**
	 * OPTIONAL
	 * Alignment model by sample name
	 */
	private Map<String, AlignmentModel> data;

	/**
	 * OPTIONAL
	 * Cache of count scores for some regions
	 * Key is sample name
	 */
	private Map<String, Map<Annotation, CountScore>> regionCounts;

	
	/**
	 * OPTIONAL
	 * Cache of count scores for some parent genes
	 * Key is sample name
	 */
	private Map<String, Map<Gene, CountScore>> geneCounts;

	
	public ActionFactory() {
		data = null;
		regionCounts = null;
	}
	
	/**
	 * @param data Alignment data by sample name
	 */
	public ActionFactory(Map<String, AlignmentModel> alignmentData) {
		this(alignmentData, null);
	}
	
	/**
	 * @param data Alignment data by sample name
	 * @param existingRegionCounts An existing cache of counts for candidate regions or null if not using
	 */
	public ActionFactory(Map<String, AlignmentModel> alignmentData, Map<String, Map<Annotation, CountScore>> existingRegionCounts) {
		this(alignmentData, existingRegionCounts, null);
	}
	
	
	/**
	 * @param data Alignment data by sample name
	 * @param existingRegionCounts An existing cache of counts for candidate regions or null if not using
	 * @param existingGeneCounts An existing cache of counts for parent genes or null if not using
	 */
	public ActionFactory(Map<String, AlignmentModel> alignmentData, Map<String, Map<Annotation, CountScore>> existingRegionCounts, Map<String, Map<Gene, CountScore>> existingGeneCounts) {
		data = alignmentData;
		regionCounts = existingRegionCounts;
		geneCounts = existingGeneCounts;
	}
	
	/**
	 * Get an ordered list of actions specified in the steps section of a config file
	 * @param configFile The config file
	 * @return The list of actions in the order of the config file
	 */
	public List<PeakCallerAction<? extends PeakCaller>> getActions(ConfigFile configFile) {
		logger.info("");
		logger.info("Getting ordered list of actions from congig file...");
		List<ConfigFileOptionValue> values = configFile.getOrderedValues(PeakCaller.stepsSection);
		List<PeakCallerAction<? extends PeakCaller>> rtrn = new ArrayList<PeakCallerAction<? extends PeakCaller>>();
		for(ConfigFileOptionValue value : values) {
			rtrn.add(getAction(value));
		}
		return rtrn;
	}
	
	/**
	 * Create an action object from a config file value
	 * @param value Config file value
	 * @return Action specified in the value
	 */
	public PeakCallerAction<? extends PeakCaller> getAction(ConfigFileOptionValue value) {
		String name = value.asString(0);
		ActionName actionName = ActionName.fromString(name);
		switch(actionName) {
		case APPLY_RAW_COUNT_FILTER:
			new ApplyRawCountFilter().validateConfigFileLine(value);
			if(data == null) {
				throw new IllegalStateException("Cannot use raw count filter without supplying alignment data");
			}
			ApplyRawCountFilter rcf = new ApplyRawCountFilter(value, data, regionCounts);
			logger.info("Added " + rcf.getName());
			return rcf;
		case APPLY_BINOMIAL_FILTER:
			new ApplyBinomialFilter().validateConfigFileLine(value);
			if(data == null) {
				throw new IllegalStateException("Cannot use binomial filter without supplying alignment data");
			}
			ApplyBinomialFilter abf = new ApplyBinomialFilter(value, data, regionCounts, geneCounts);
			logger.info("Added " + abf.getName());
			return abf;
		case APPLY_GENE_FPKM_FILTER:
			new ApplyGeneFPKMFilter().validateConfigFileLine(value);
			if(data == null) {
				throw new IllegalStateException("Cannot use gene FPKM filter without supplying alignment data");
			}
			ApplyGeneFPKMFilter gff = new ApplyGeneFPKMFilter(value, data, geneCounts);
			logger.info("Added " + gff.getName());
			return gff;
		case MERGE_CANDIDATE_REGIONS:
			MergeCandidateRegions mcr = new MergeCandidateRegions();
			mcr.validateConfigFileLine(value);
			logger.info("Added " + mcr.getName());
			return mcr;
		case TRIM_MAX_CONTIGUOUS:
			new TrimMaxContiguous().validateConfigFileLine(value);
			if(data == null) {
				throw new IllegalStateException("Cannot use trim max contiguous without supplying alignment data");
			}
			TrimMaxContiguous tmc = new TrimMaxContiguous(value, data);
			logger.info("Added " + tmc.getName());
			return tmc;
		case WRITE_CANDIDATE_REGIONS:
			new WriteCandidateRegions().validateConfigFileLine(value);
			WriteCandidateRegions wcr = new WriteCandidateRegions(value);
			logger.info("Added " + wcr.getName());
			return wcr;
		case INITIALIZE_REGIONS_AS_WINDOWS:
			new InitializeRegionsAsWindows().validateConfigFileLine(value);
			InitializeRegionsAsWindows irw = new InitializeRegionsAsWindows(value);
			logger.info("Added " + irw.getName());
			return irw;
		case INITIALIZE_REGIONS_AS_WINDOWS_WITH_MIN_COUNT:
			new InitializeRegionsAsWindowsWithMinCount().validateConfigFileLine(value);
			InitializeRegionsAsWindowsWithMinCount irwmc = new InitializeRegionsAsWindowsWithMinCount(value);
			logger.info("Added " + irwmc.getName());
			return irwmc;
		case INITIALIZE_GENE_COUNTS:
			InitializeGeneCounts igc = new InitializeGeneCounts();
			igc.validateConfigFileLine(value);
			logger.info("Added " + igc.getName());
			return igc;
		default:
			throw new IllegalStateException("Action name " + name + " missing from switch statement.");
		}
	}
	
	
}
