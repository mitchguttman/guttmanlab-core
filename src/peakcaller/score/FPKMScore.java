package peakcaller.score;

import java.util.Map;

import nextgen.core.annotation.Annotation;
import nextgen.core.model.AlignmentModel;
import nextgen.core.model.score.CountScore;

/**
 * A score that reports the FPKM over a region with respect to an alignment model
 * @author prussell
 *
 */
public class FPKMScore implements RegionScore {
	
	private Map<? extends Annotation, CountScore> scoreMap;
	private AlignmentModel data;
	
	/**
	 * @param alignmentData Read mapping data
	 */
	public FPKMScore(AlignmentModel alignmentData) {
		this(alignmentData, null);
	}
	
	/**
	 * @param alignmentData Read mapping data
	 * @param existingCountScores Existing count scores for some annotations. Use the score in this map if it exists.
	 */
	public <T extends Annotation> FPKMScore(AlignmentModel alignmentData, Map<T, CountScore> existingCountScores) {
		data = alignmentData;
		scoreMap = existingCountScores;
	}
	
	@Override
	public double getScore(Annotation region) {
		if(scoreMap != null) {
			CountScore score = scoreMap.get(region);
			if(score != null) return score.getRPKM();
		}
		CountScore score = new CountScore(data, region);
		// TODO figure out how to cache score
		return score.getRPKM();
	}

	@Override
	public String getName() {
		return "raw_count_score";
	}

}
