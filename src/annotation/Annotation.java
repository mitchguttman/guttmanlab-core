package annotation;

import java.util.Collection;

import coordinatespace.CoordinateSpace;

/**
 * 
 * @author prussell
 *
 */
public interface Annotation {
	
	/**
	 * @return The name of this annotation
	 */
	public String getName();
	
	/**
	 * @return The coordinate space that this annotation refers to
	 */
	public CoordinateSpace getCoordinateSpace();
	
	/**
	 * @param other Another annotation
	 * @return True iff this annotation overlaps the other annotation
	 */
	public boolean overlaps(Annotation other);

	/**
	 * @param other Another annotation
	 * @return True iff this annotation contains the other annotation
	 */
	public boolean contains(Annotation other);
	
	/**
	 * Merge with another annotation
	 * @param other Another annotation
	 * @return A new annotation representing a merging of the two
	 */
	public Annotation merge(Annotation other);
	
	/**
	 * Subtract another annotation from this annotation
	 * @param other Another annotation
	 * @return A new annotation representing the part of this annotation remaining after removing the other annotation
	 */
	public Annotation minus(Annotation other);

	/**
	 * Returns the blocks contained in this annotation
	 * @return Collection of annotations that each represent a contiguous block within the annotation
	 */
	public Collection<Annotation> getBlocks();
	
}
