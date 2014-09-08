package annotation;

import java.util.Collection;

import annotation.predicate.ReadFlag;

/**
 * An Abstract Class representing functionality specific to mapped reads
 * @author mguttman
 *
 */
public interface MappedFragment extends Annotation{

	public Collection<? extends ReadFlag> getReadFlags();
	
	public int getNumHits();
	
}
