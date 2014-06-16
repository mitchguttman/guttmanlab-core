package peakcaller.predicate;

import nextgen.core.annotation.Annotation;

/**
 * Any filter for regions
 * @author prussell
 *
 */
public interface RegionFilter {
	
	/**
	 * Determine whether the region passes this filter
	 * @param region The region
	 * @return true iff the region passes this filter
	 */
	public boolean passes(Annotation region);
	
}
