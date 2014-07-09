package annotation;

import java.util.Iterator;

import annotation.Annotation.Strand;
import net.sf.samtools.SAMRecord;

/**
 * 
 * @author prussell
 *
 */
public interface Annotation {
	
	/**
	 * An enumeration of strand possibilities
	 * @author mguttman
	 *
	 */
	public enum Strand {
		POSITIVE('+'), NEGATIVE('-'), UNKNOWN('?'), UNSTRANDED('*'), BOTH('*'), INVALID('^');
		private char value;
		
		private Strand(char value) {
			this.value = value;
		}
		
		private Strand(String value) {
			if (value.length() > 1) throw new IllegalArgumentException("Illegal strand string");
			this.value = value.charAt(0);
		}

		public String toString() {
			return "" + value;
		}
		
		public Strand getReverseStrand() {
			if(this.equals(POSITIVE)){
				return NEGATIVE;
			}
			else if(this.equals(NEGATIVE)){
				return POSITIVE;
			}
			else if(this.equals(UNSTRANDED)){
				return UNSTRANDED;
			}
			else{
				return UNKNOWN;
			}
		}
		
		public static Strand consensusStrand(Strand strand1, Strand strand2) {
			Strand rtrn=INVALID;
			if(strand1.equals(BOTH)){
				rtrn=strand2;
			}
			if(strand2.equals(BOTH)){
				rtrn=strand1;
			}
			if(strand1.equals(strand2)){
				rtrn=strand1;
			}	
			return rtrn;
		}
		
		public static Strand fromString(String value) {
			if (value.equals("+")) return POSITIVE;
			if (value.equals("-")) return NEGATIVE;
			if(value.equals("*")) return UNSTRANDED;
			else return UNKNOWN;
		}

		public static Strand valueOf(boolean isNegativeStrandFlag) {
			if(isNegativeStrandFlag){return NEGATIVE;}
			else{return POSITIVE;}
		}
	}
	
	/**
	 * @return The name of this annotation
	 */
	public String getName();
	
	/**
	 * @return The name of the reference
	 */
	public String getReferenceName();
	
	/**
	 * @return The start position on the reference
	 */
	public int getReferenceStartPosition();
	
	/**
	 * @return The end position on the reference
	 */
	public int getReferenceEndPosition();
	
	/**
	 * @param other Another annotation
	 * @return True if any of the block in this annotation overlaps the blocks of the other annotation
	 */
	public boolean overlaps(Annotation other);

	/**
	 * @param other Another annotation
	 * @return True iff this annotation contains the other annotation
	 */
	public boolean contains(Annotation other);
	
	/**
	 * Merge with another annotation
	 * @param other Another annotation
	 * @return A new annotation representing a merging of the two
	 */
	public Annotation merge(Annotation other);
	
	/**
	 * Subtract another annotation from this annotation
	 * @param other Another annotation
	 * @return A new annotation representing the part of this annotation remaining after removing the other annotation
	 */
	public Annotation minus(Annotation other);
	
	/**
	 * Intersect another annotation with this annotation
	 * @param other Another annotation
	 * @return A new annotation representing the overlapping regions of the 2 annotations
	 */
	public Annotation intersect(Annotation other);
	
	/**
	 * Get blocks in the alignment
	 * @return The blocks of the alignment
	 */
	public Iterator<SingleInterval> getBlocks();
	
	/**
	 * Get number of blocks in the annotation
	 * @return The number of blocks
	 */
	public int getNumberOfBlocks();
	
	/**
	 * Return the size of the annotation
	 * @return size of the blocks in the annotation
	 */
	public int size();
	
	/**
	 * Return the orientation of the annotation
	 * @return
	 */
	public Strand getOrientation();

	/**
	 * Return a BED string representation of the Annotation
	 * @param r Red color
	 * @param g Green color
	 * @param b Blue color
	 * @return String representation
	 */
	public String toBED(int r, int g, int b);
	
	/**
	 * Convert this region into the feature space
	 * @param region In reference space
	 * @return Region in Feature space
	 */
	public Annotation convertToFeatureSpace(Annotation region);
	
	/**
	 * Convert this region from feature space into reference space
	 * @param featureAnnotation In feature space
	 * @return Region in reference space
	 */
	public Annotation convertToReferenceSpace(Annotation featureAnnotation);
	
	/**
	 * Get the relative coordinate in feature space relative to the 5' of the feature
	 * @param referenceStart Reference position
	 * @return Feature position (from 5' start) or -1 if doesn't overlap
	 */
	public int getRelativePositionFrom5PrimeOfFeature(int referenceStart);
	
	/**
	 * Return the CIGAR string representation of the annotation
	 * @return CIGAR format
	 */
	public String getCigarString();

	/**
	 * Set the orientation of this feature
	 * @param orientation
	 */
	public void setOrientation(Strand orientation);

	/**
	 * Returns this Annotation as a SAMRecord
	 * @return A SAMRecord representation of this annotation
	 */
	public SAMRecord getSamRecord();
	
}
