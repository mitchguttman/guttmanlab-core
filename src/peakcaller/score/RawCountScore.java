package peakcaller.score;

import java.util.Map;

import nextgen.core.annotation.Annotation;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * A score that simply reports the raw count over a region
 * @author prussell
 *
 */
public class RawCountScore implements RegionScore {
	
	private Map<Annotation, CountScore> scoreMap;
	private AlignmentModel data;
	
	/**
	 * @param alignmentData Read mapping data
	 */
	public RawCountScore(AlignmentModel alignmentData) {
		this(alignmentData, null);
	}
	
	/**
	 * @param alignmentData Read mapping data
	 * @param existingCounts Existing counts for some annotations. Use the count in this map if it exists.
	 */
	public RawCountScore(AlignmentModel alignmentData, Map<Annotation, CountScore> existingCounts) {
		data = alignmentData;
		scoreMap = existingCounts;
	}
	
	/**
	 * Cache a count score object for a region
	 * If score map is null don't do anything
	 * @param region The region
	 * @param score The score object
	 */
	public void cacheCountScore(Annotation region, CountScore score) {
		if(scoreMap == null) return;
		scoreMap.put(region, score);
	}
	
	/**
	 * Get CountScore object for a region
	 * @param region The region
	 * @return The CountScore object
	 */
	public CountScore getCountScore(Annotation region) {
		if(scoreMap != null) {
			CountScore score = scoreMap.get(region);
			if(score != null) {
				return score;
			}
		}
		CountScore rtrn = new CountScore(data, region);
		return rtrn;
	}
	
	@Override
	public double getScore(Annotation region) {
		CountScore score = getCountScore(region);
		return score.getCount();
	}

	@Override
	public String getName() {
		return "raw_count_score";
	}

}
