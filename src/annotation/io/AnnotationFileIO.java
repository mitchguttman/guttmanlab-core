package annotation.io;

import java.io.IOException;

import annotation.Annotation;
import annotationcollection.AnnotationCollection;

/**
 * Methods to read from and write to standard annotation file formats
 * @author prussell
 *
 */
public interface AnnotationFileIO<T extends Annotation> {
	
	/**
	 * Read the file and get the annotations as annotation objects
	 * @param fileName File name
	 * @return The collection of annotations described in the file
	 * @throws IOException 
	 */
	public  AnnotationCollection<T> loadFromFile(String fileName) throws IOException;
	
}
