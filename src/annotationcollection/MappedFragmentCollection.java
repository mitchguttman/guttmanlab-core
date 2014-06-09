package annotationcollection;

import org.apache.commons.collections15.Predicate;

import annotation.MappedFragment;

import coordinatespace.CoordinateConverter;

/**
 * A container of fragments mapped to a genome
 * @author prussell
 *
 */
public interface MappedFragmentCollection extends AnnotationCollection {
	
	/**
	 * @return The coordinate converter for this mapping collection
	 */
	public CoordinateConverter getCoordinateConverter();
	
	/**
	 * Add a fragment filter
	 * @param filter Predicate that evaluates to true when the fragment passes the filter (should be kept)
	 */
	public void addFragmentFilter(Predicate<MappedFragment> filter);
	
	/**
	 * Write entire dataset to a new file with coordinates converted to other coordinate space according to this object's coordinate converter
	 * @param outputFile File to write to
	 */
	public void writeConvertedFile(String outputFile);
	
}
