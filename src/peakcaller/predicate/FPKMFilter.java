package peakcaller.predicate;

import peakcaller.score.FPKMScore;
import nextgen.core.annotation.Annotation;

/**
 * Filter regions using a minimum FPKM
 * @author prussell
 *
 */
public class FPKMFilter implements RegionFilter {
	
	private double min;
	private FPKMScore score;
	
	/**
	 * @param fpkmScore FpkmScore object
	 * @param minFpkm Minimum FPKM to pass filter
	 */
	public FPKMFilter(FPKMScore fpkmScore, double minFpkm) {
		score = fpkmScore;
		min = minFpkm;
	}
	
	@Override
	public boolean passes(Annotation region) {
		double fpkm = score.getScore(region);
		return fpkm >= min;
	}
	
	// TODO if region passes this filter, add region to the cache in the FPKMScore object

}
