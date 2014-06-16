package peakcaller.score;

import java.util.Map;

import jsc.distributions.Binomial;

import org.apache.log4j.Logger;

import nextgen.core.annotation.Annotation;
import nextgen.core.annotation.Gene;
import nextgen.core.feature.GeneWindow;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * The score is a binomial P value
 * Binomial parameters are based read counts on the parent gene
 * Cache the score object
 * @author prussell
 *
 */
public class BinomialScore implements RegionScore {

	private Map<Annotation, CountScore> controlScoreMap;
	private Map<Annotation, CountScore> sampleScoreMap;
	private Map<Gene, CountScore> controlGeneScoreMap;
	private Map<Gene, CountScore> sampleGeneScoreMap;
	private AlignmentModel controlData;
	private AlignmentModel sampleData;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(BinomialScore.class.getName());
	
	
	/**
	 * @param controlAlignmentData Alignment data to use for control sample
	 * @param sampleAlignmentData Alignment data to use for sample of interest
	 * @param controlRegionCounts Cache of region counts for control sample or null if not using
	 * @param sampleRegionCounts Cache of region counts for sample of interest or null if not using
	 * @param controlGeneCounts Cache of parent gene counts for control sample or null if not using
	 * @param sampleGeneCounts Cache of parent gene counts for sample of interest or null if not using
	 */
	public BinomialScore(AlignmentModel controlAlignmentData, AlignmentModel sampleAlignmentData, Map<Annotation, CountScore> controlRegionCounts, Map<Annotation, CountScore> sampleRegionCounts, Map<Gene, CountScore> controlGeneCounts, Map<Gene, CountScore> sampleGeneCounts) {
		controlData = controlAlignmentData;
		sampleData = sampleAlignmentData;
		controlScoreMap = controlRegionCounts;
		sampleScoreMap = sampleRegionCounts;
		controlGeneScoreMap = controlGeneCounts;
		sampleGeneScoreMap = sampleGeneCounts;
	}
	
	@Override
	public double getScore(Annotation region) {
		
		/*
		 * Region should already be a GeneWindow
		 * This is necessary so we can get the parent annotation
		 * Program will crash if not a GeneWindow
		 */
		GeneWindow window = (GeneWindow)region;
		Annotation parent = window.getSourceAnnotations().iterator().next();
		Gene parentGene = (Gene)parent;
		
		// Get the counts needed to make the binomial distribution and compute P-value
		double controlRegionCount = computeCount(region, controlScoreMap, controlData);
		double controlGeneCount = computeCount(parentGene, controlGeneScoreMap, controlData);
		double sampleRegionCount = computeCount(region, sampleScoreMap, sampleData);
		double sampleGeneCount = computeCount(parentGene, sampleGeneScoreMap, sampleData);
		
		// If can't compute P value return 1
		if(controlRegionCount + sampleRegionCount < 2) return 1;
		if(controlGeneCount + sampleGeneCount < 2) return 1;
		
		// Compute binomial parameters and P value
		double p = sampleGeneCount/(sampleGeneCount + controlGeneCount);
		if(p == 0) return 1;
		long n = (long) (controlRegionCount + sampleRegionCount);
		Binomial b = new Binomial(n,p);
		double pval = 1 - b.cdf(sampleRegionCount);
		return pval;
	}
	
	/**
	 * Get the read mapping count over the region
	 * Try to get from cache first
	 * If not in cache, compute and put in cache then return
	 * @param region The region
	 * @param scores A map of counts that may or may not contain this region as a key, or null if not using
	 * @param data The relevant alignment data
	 * @return The count over the region
	 */
	private static <T extends Annotation> double computeCount(T region, Map<T, CountScore> scores, AlignmentModel data) {
		if(scores != null) {
			CountScore score = scores.get(region);
			if(score != null) return score.getCount();
		}
		CountScore newScore = new CountScore(data, region);
		if(scores != null) scores.put(region, newScore);
		return newScore.getCount();
	}
	
	
	@Override
	public String getName() {
		return "binomial_score";
	}

}
