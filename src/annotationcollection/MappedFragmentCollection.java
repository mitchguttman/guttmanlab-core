package annotationcollection;

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
	 * Write entire dataset to a new file with coordinates converted to other coordinate space according to this object's coordinate converter
	 * @param outputFile File to write to
	 */
	public void writeConvertedFile(String outputFile);
	
}
