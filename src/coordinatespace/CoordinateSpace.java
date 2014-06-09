package coordinatespace;

import java.util.Collection;
import java.util.Map;

import annotation.Annotation;

/**
 * A coordinate space that is a subset of a background genome
 * @author prussell
 *
 */
public class CoordinateSpace {
	
	/**
	 * The background genome that this coordinate space is a subset of
	 * Key is chromosome name, value is chromosome length
	 */
	private Map<String, Integer> genomeChrSizes;
	
	/**
	 * This coordinate space expressed as annotations on the background genome
	 * Key is chromosome name, value is the set of annotations on that chromosome
	 */
	private Map<String, Collection<Annotation>> coordSpaceOnGenome;
	
}
