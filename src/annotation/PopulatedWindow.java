package annotation;

import java.util.Iterator;

/**
 * A window with overlappers
 * @author mguttman
 *
 */
public interface PopulatedWindow<T extends Annotation> extends Annotation{

	/**
	 * The annotation that this Window was made from
	 * @return The parent annotation that this Window was made from
	 */
	public Annotation getParentAnnotation();
	
	/**
	 * A score for this window
	 * @return A score object for this annotation
	 */
	public Score getScore();
	
	/**
	 * Add annotations overlapping this window
	 * @param annotation An annotation overlapping this window
	 */
	public void addAnnotation(T annotation); 
	
	/**
	 * @return The number of annotation stored in this window
	 */
	public int getNumberOfAnnotationsInWindow();

	/**
	 * @return Returns an iterator over the features overlapping the window
	 */
	public Iterator<T> getAnnotationsInWindow();
}
