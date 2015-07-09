package guttmanlab.core.annotation;

import guttmanlab.core.annotationcollection.AnnotationCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A class that represent a simple contiguous interval
 * This is the basis for all features
 * @author mguttman
 *
 */
public class SingleInterval extends AbstractAnnotation{

	private String referenceName;
	private int startPos;
	private int endPos;
	private Strand orientation;
	private String featureName;
	
	public SingleInterval(String refName, int start, int end, Strand orientation){
		this(refName, start, end, orientation, "");
	}
	
	public SingleInterval(String refName, int start, int end, Strand orientation, String featureName){
		this.referenceName=refName;
		this.startPos=start;
		this.endPos=end;
		this.orientation=orientation;
		this.featureName=featureName;
	}
	
	public SingleInterval(String refName, int start, int end) {
		this(refName, start, end, Strand.UNKNOWN, "");
	}

	@Override
	public String getName() {
		return this.featureName;
	}

	@Override
	public String getReferenceName() {
		return this.referenceName;
	}

	@Override
	public int getReferenceStartPosition() {
		return this.startPos;
	}

	@Override
	public int getReferenceEndPosition() {
		return this.endPos;
	}


	@Override
	public Iterator<SingleInterval> getBlocks() {
		Collection<SingleInterval> rtrn=new ArrayList<SingleInterval>();
		rtrn.add(this);
		return rtrn.iterator();
	}
	
	@Override
	public int size() {
		return endPos-startPos;
	}

	@Override
	public Strand getOrientation() {
		return this.orientation;
	}

	@Override
	public int getNumberOfBlocks() {
		return 1;
	}

	@Override
	//FIXME This should be merged with BlockedAnnotation
	public int getRelativePositionFrom5PrimeOfFeature(int referenceStart) {
		if(referenceStart>=this.getReferenceEndPosition() || referenceStart<this.getReferenceStartPosition()){return -1;} //This start position is past the feature
		int relative=referenceStart-getReferenceStartPosition();
		if(getOrientation().equals(Strand.NEGATIVE)){
			relative=size()-relative;
		}
		return relative;
	}

	@Override
	public void setOrientation(Strand orientation) {
		this.orientation=orientation;
	}

	/**
	 * Trim this block to the relative start and end position provided
	 * @param relativeStartPosition relative start position
	 * @param relativeEndPosition relative end position
	 * @return
	 */
	public SingleInterval trim(int relativeStart, int relativeEnd) {
		if(getOrientation().equals(Strand.NEGATIVE)){
			int newEnd=getReferenceEndPosition()-relativeStart;
			int newStart=getReferenceEndPosition()-relativeEnd;
			return new SingleInterval(getReferenceName(), newStart, newEnd);
		}
		else{
			return new SingleInterval(getReferenceName(), getReferenceStartPosition()+relativeStart, getReferenceStartPosition()+relativeEnd);
		}
	}
/*
    //9/25/14 @cburghard methods overwritten in AbstractAnnotation
	public int hashCode() {
		String s = referenceName + "_" + featureName + "_" + startPos + "_" + endPos + "_" + orientation.toString();
		return s.hashCode();
	}
	
	public boolean equals(Object o) {
		if(!o.getClass().equals(getClass())) return false;
		SingleInterval other = (SingleInterval) o;
		if(startPos != other.startPos) return false;
		if(endPos != other.endPos) return false;
		if(!orientation.equals(other.orientation)) return false;
		if(!referenceName.equals(other.referenceName)) return false;
		if(!featureName.equals(other.featureName)) return false;
		return true;
	}
*/
	@Override
	public AnnotationCollection<DerivedAnnotation<? extends Annotation>> getWindows(
			int windowSize, int stepSize) {
		throw new UnsupportedOperationException();
	}
	
	
}
