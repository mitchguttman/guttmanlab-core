package peakcaller.predicate;

import peakcaller.score.RawCountScore;
import nextgen.core.annotation.Annotation;
import nextgen.core.model.score.CountScore;

/**
 * Filter regions using a minimum raw count
 * If the region passes the filter, cache the region and score provided that the RawCountScore object is keeping a cache
 * @author prussell
 *
 */
public class RawCountFilter implements RegionFilter {
	
	private double min;
	private RawCountScore score;
	
	/**
	 * @param rawCountScore RawCountScore object
	 * @param minCount Minimum count to pass filter
	 */
	public RawCountFilter(RawCountScore rawCountScore, double minCount) {
		score = rawCountScore;
		min = minCount;
	}
	
	/**
	 * Use a CountScore object instead of a RawCountScore object
	 * Check if the count is at least the min
	 * @param cs CountScore object
	 * @return True iff the count is >= the min count
	 */
	public boolean passes(CountScore cs) {
		boolean rtrn = cs.getCount() >= min;
		if(rtrn) {
			// Only cache the score if the region passes this filter
			score.cacheCountScore(cs.getAnnotation(), cs);
		}
		return rtrn;
	}
	
	@Override
	public boolean passes(Annotation region) {
		CountScore cs = score.getCountScore(region);
		double count = cs.getCount();
		boolean rtrn = count >= min;
		if(rtrn) {
			// Only cache the score if the region passes this filter
			score.cacheCountScore(region, cs);
		}
		return rtrn;
	}

}
