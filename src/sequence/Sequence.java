package sequence;

import java.util.Collection;

import annotation.Annotation;

/**
 * A nucleotide sequence
 * @author prussell
 *
 */
public class Sequence {
	
	/**
	 * @return A new sequence that is the reverse complement of this sequence
	 */
	public Sequence reverseComplement() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Soft mask the specified regions
	 * Throw an exception if the annotations do not refer to this sequence
	 * @param regions The regions to mask with respect to this sequence
	 * @return A new sequence with the regions soft masked
	 */
	public Sequence softMask(Collection<Annotation> regions) {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Hard mask the specified regions
	 * Throw an exception if the annotations do not refer to this sequence
	 * @param regions The regions to mask with respect to this sequence
	 * @return A new sequence with the regions hard masked
	 */
	public Sequence hardMask(Collection<Annotation> regions) {
		// TODO
		throw new UnsupportedOperationException();
	}
	
}
