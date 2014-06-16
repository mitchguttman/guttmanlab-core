package peakcaller.predicate;

import peakcaller.score.BinomialScore;
import nextgen.core.annotation.Annotation;

/**
 * Filter for binomial P value based on read counts
 * @author prussell
 *
 */
public class BinomialFilter implements RegionFilter {
	
	private double max;
	private BinomialScore score;
	
	/**
	 * @param binomialScore Binomial score object
	 * @param maxPval Max P value to pass filter
	 */
	public BinomialFilter(BinomialScore binomialScore, double maxPval) {
		if(maxPval < 0 || maxPval > 1) {
			throw new IllegalArgumentException("Max P value must be between 0 and 1");
		}
		max = maxPval;
		score = binomialScore;
	}
	
	@Override
	public boolean passes(Annotation region) {
		double pval = score.getScore(region);
		return pval <= max;
	}

}
