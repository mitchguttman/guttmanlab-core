package annotation;

import annotation.Annotation.Strand;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;

/**
 * Represents an annotation that was derived from another Annotation
 * @author mguttman
 *
 * @param <T> The type of the Annotation it was derived from
 */
public class DerivedAnnotation<T extends Annotation> extends BlockedAnnotation{

	private T parentDerivedFrom;
	
	public DerivedAnnotation(Annotation annotation, T parent){
		super(annotation);
		this.parentDerivedFrom=parent;
	}
	
	/**
	 * The parent annotation this was derived from
	 * @return A pointer to the parent this was derived from
	 */
	public T getParentAnnotation(){return parentDerivedFrom;}
	
}
