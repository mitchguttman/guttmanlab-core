package annotation;

import java.util.Collection;
import java.util.Map;

/**
 * Methods to read from and write to standard annotation file formats
 * @author prussell
 *
 */
public interface AnnotationFileIO {
	
	/**
	 * Read the file and get the annotations as annotation objects
	 * @param fileName File name
	 * @return The collection of annotations described in the file
	 */
	public Collection<Annotation> loadFromFile(String fileName);
	
	/**
	 * Read the file and get the annotations organized by chromosome
	 * @param fileName File name
	 * @return Map of chromosome name to the collection of annotations on that chromosome in the file
	 */
	public Map<String, Collection<Annotation>> loadFromFileByChr(String fileName);
	
	/**
	 * Write a collection of annotations to a file
	 * @param annotations The annotations to write
	 * @param fileName The file to write to
	 */
	public void writeToFile(Collection<Annotation> annotations, String fileName);
}
