package peakcaller.score;


import nextgen.core.annotation.Annotation;

/**
 * Any score function on regions
 * @author prussell
 *
 */
public interface RegionScore {
	
	/**
	 * Get the value of the score on a region
	 * @param region The region
	 * @return The value of the score
	 */
	public double getScore(Annotation region);
	
	/**
	 * Get the name of the score
	 * @return The name of the score
	 */
	public String getName();
	
}
