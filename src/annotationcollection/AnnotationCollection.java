package annotationcollection;

import java.util.Collection;
import java.util.Iterator;

import annotation.Annotation;

/**
 * A container of annotations
 * @author prussell
 *
 */
public interface AnnotationCollection {
	
	/**
	 * Merge the annotations in this collection
	 * @return A new annotation collection consisting of the merged annotations
	 */
	public AnnotationCollection merge();
	
	/**
	 * @param other An annotation
	 * @return True iff the annotation overlaps some annotation in this collection
	 */
	public boolean overlaps(Annotation other);
	
	/**
	 * @param other Another annotation collection
	 * @return True iff some annotation in this collection overlaps some annotation in the other collection
	 */
	public boolean overlaps(AnnotationCollection other);
	
	/**
	 * @return The total number of annotations in this collection
	 */
	public int getCount();
	
	/**
	 * Get the number of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region in order to count
	 * @return The number of overlappers
	 */
	public int getCount(Annotation region, boolean fullyContained);
	
	/**
	 * Get the set of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region
	 * @return The set of overlappers
	 */
	public Collection<? extends Annotation> getOverlappers(Annotation region, boolean fullyContained);
	
	/**
	 * @return An iterator over all the annotations in this collection
	 */
	public Iterator<? extends Annotation> getIterator();
	
	/**
	 * Get an iterator over the set of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region
	 * @return Iterator over the set of overlappers
	 */	
	public Iterator<? extends Annotation> getIterator(Annotation region, boolean fullyContained);
	
	
}
