package annotationcollection;


import java.util.Collection;
import java.util.Iterator;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.collections15.Predicate;

import coordinatespace.CoordinateSpace;
import annotation.Annotation;
import annotation.BlockedAnnotation;

/**
 * A container of annotations
 * @author prussell
 *
 */
public interface AnnotationCollection<T extends Annotation> {
	
	/**
	 * Merge the annotations in this collection
	 * @return A new annotation collection consisting of the merged annotations
	 */
	public AnnotationCollection<T> merge();
	
	/**
	 * @param other An annotation
	 * @return True iff the annotation overlaps some annotation in this collection
	 */
	public boolean overlaps(Annotation other);
	
	/**
	 * @param other Another annotation collection
	 * @return True iff some annotation in this collection overlaps some annotation in the other collection
	 */
	public boolean overlaps(AnnotationCollection<? extends Annotation> other);
	
	/**
	 * Get the number of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region in order to count
	 * @return The number of overlappers
	 */
	public int numOverlappers(Annotation region, boolean fullyContained);
	
	/**
	 * @return An iterator over all the annotations in this collection
	 */
	public CloseableIterator<T> iterator();
	
	/**
	 * Get an iterator over the set of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region
	 * @return Iterator over the set of overlappers
	 */	
	public CloseableIterator<T> iterator(Annotation region, boolean fullyContained);
	
	/**
	 * Add a filter that will be applied when getting subsets or iterators
	 * @param filter A predicate that is true if the annotation passes the filter (should be kept)
	 */
	public void addFilter(Predicate<T> filter);
	
	/**
	 * Get all the filters passed to this class
	 * @return A collection of all predicate filters
	 */
	public Collection<Predicate<T>> getFilters();
	
	
	/**
	 * Write the collection of annotations (using filters) to a file
	 * @param fileName The file to write to
	 */
	public void writeToFile(String fileName);

	/**
	 * @return The reference coordinate space
	 */
	public CoordinateSpace getReferenceCoordinateSpace();

	/**
	 * 
	 * @return The feature coordinate space
	 */
	public CoordinateSpace getFeatureCoordinateSpace();
	
	/** TODO Consider returning an AnnotationCollection
	 * Convert the annotation from the reference space
	 * If the reference equals the reference of the FeatureSpace it will return feature space
	 * If the reference equals the feature in the FeatureSpace it will return reference space
	 * If the reference doesn't equal either, it will throw an Exception
	 * @param annotations An iterator of the annotations to convert
	 * @param referenceSpaceForAnnotations The reference coordinate space of the annotation to map (must match either reference or feature space)
	 * @return An iterator of annotations in the new feature space
	 */
	public CloseableIterator<BlockedAnnotation> convertCoordinates(CloseableIterator<? extends Annotation> annotations, CoordinateSpace referenceSpaceForAnnotations);
	
}
