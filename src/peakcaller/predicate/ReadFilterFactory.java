package peakcaller.predicate;

import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;

import pipeline.ConfigFileOptionValue;

import nextgen.core.alignment.Alignment;
import nextgen.core.readFilters.GenomicSpanFilter;
import nextgen.core.readFilters.NumHitsFilter;

/**
 * Get a sample name and read filter that are specified in a config file value
 * @author prussell
 *
 */
public class ReadFilterFactory {
	
	public static String NUM_HITS_FILTER_NAME = "read_num_hits_filter";
	public static String GENOMIC_SPAN_FILTER_NAME = "read_genomic_span_filter";
	private static Logger logger = Logger.getLogger(ReadFilterFactory.class.getName());
	
	/**
	 * Get the sample name specified on the config file line
	 * @param value The config file line
	 * @return The sample name from the config file line
	 */
	public static String getSampleName(ConfigFileOptionValue value) {
		if(value.getActualNumValues() < 2) {
			throw new IllegalArgumentException("For read filters, config file line is <filter_name> <sample_name> <filter_options>");
		}
		return value.asString(1);
	}
	
	/**
	 * Get the name of the filter specified on the config file line
	 * @param value The config file line
	 * @return The filter name from the config file line
	 */
	public static String getFilterName(ConfigFileOptionValue value) {
		if(value.getActualNumValues() < 2) {
			throw new IllegalArgumentException("For read filters, config file line is <filter_name> <sample_name> <filter_options>");
		}
		// Genomic span filter
		if(value.asString(0).equals(GENOMIC_SPAN_FILTER_NAME)) {
			try {
				@SuppressWarnings("unused")
				int maxSpan = value.asInt(2);
				return GENOMIC_SPAN_FILTER_NAME;
			} catch(Exception e) {
				logger.error("Config file format for genomic span filter: " + GENOMIC_SPAN_FILTER_NAME + " <sample_name> <max_genomic_span>");
				throw(e);
			}
		}
		// Num hits filter
		if(value.asString(0).equals(NUM_HITS_FILTER_NAME)) {
			try {
				@SuppressWarnings("unused")
				int maxNH = value.asInt(2);
				return NUM_HITS_FILTER_NAME;
			} catch(Exception e) {
				logger.error("Config file format for num hits filter: " + NUM_HITS_FILTER_NAME + " <sample_name> <max_num_hits>");
				throw(e);
			}
		}
		// Default
		throw new IllegalArgumentException(value.asString(0) + " is not a valid filter name. Options: " + NUM_HITS_FILTER_NAME + ", " + GENOMIC_SPAN_FILTER_NAME);
	}
	
	/**
	 * Get the read filter specified on the config file line
	 * @param value The config file line
	 * @return The read filter from the config file line
	 */
	public static Predicate<Alignment> getReadFilter(ConfigFileOptionValue value) {
		if(value.getActualNumValues() < 2) {
			throw new IllegalArgumentException("For read filters, config file line is <filter_name> <sample_name> <filter_options>");
		}
		// Genomic span filter
		if(value.asString(0).equals(GENOMIC_SPAN_FILTER_NAME)) {
			try {
				int maxSpan = value.asInt(2);
				return new GenomicSpanFilter(maxSpan);
			} catch(Exception e) {
				logger.error("Config file format for genomic span filter: " + GENOMIC_SPAN_FILTER_NAME + " <sample_name> <max_genomic_span>");
				throw(e);
			}
		}
		// Num hits filter
		if(value.asString(0).equals(NUM_HITS_FILTER_NAME)) {
			try {
				int maxNH = value.asInt(2);
				return new NumHitsFilter(maxNH);
			} catch(Exception e) {
				logger.error("Config file format for num hits filter: " + NUM_HITS_FILTER_NAME + " <sample_name> <max_num_hits>");
				throw(e);
			}
		}
		// Default
		throw new IllegalArgumentException(value.asString(0) + " is not a valid filter name. Options: " + NUM_HITS_FILTER_NAME + ", " + GENOMIC_SPAN_FILTER_NAME);
	}
	
}
