package sequence;

import java.util.Collection;
import annotation.Annotation;

/**
 * A nucleotide sequence
 * @author prussell
 *
 */
public class Sequence {
	
	private String sequence;
	private String name;
	
	public Sequence(String name, String seq){
		this.name=name;
		this.sequence=seq;
	}
	
	public Sequence(String seq){
		this.sequence=seq;
	}
	
	/**
	 * @return A new sequence that is the reverse complement of this sequence
	 */
	public Sequence reverseComplement() {
		Sequence tmpSeq = new Sequence(this.sequence);
		tmpSeq.reverse();
		return tmpSeq;
	}
	
	
	private void reverse() {
		String seqBases = getSequenceBases();
		String reversedSeq = "";
		for(int j = seqBases.length() - 1; j >= 0 ; j--) {
			char c = seqBases.charAt(j);
			if('c' == c) {
				reversedSeq+='g';
			}else if ('C' == c) {
				reversedSeq+='G';
			}else if ('G' == c) {
				reversedSeq+='C';
			}else if ('g' == c) {
				reversedSeq+=('c');
			}else if ('a' == c) {
				reversedSeq+=('t');
			}else if ('A' == c) {
				reversedSeq+=('T');
			}else if('t' == c) {
				reversedSeq+=('a');
			}else if('T' == c) {
				reversedSeq+=('A');
			}else if('N'==c){
				reversedSeq+=('N');
			}else if('n'==c){
				reversedSeq+=('n');
			}else {
				reversedSeq+=(c);
			}
		}
		
		this.sequence = reversedSeq;
	}
	
	/**
	 * @return Return the sequence bases
	 */
	public String getSequenceBases() {
		return this.sequence;
	}
	
	public String getName(){
		return this.name;
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
		throw new UnsupportedOperationException("TODO");
	}
	
}
