package annotation;

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
	public <T extends Annotation> T merge(Annotation other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockedAnnotation intersect(Annotation other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfBlocks() {
		return 1;
	}

	@Override
	public int getRelativePositionFrom5PrimeOfFeature(int referenceStart) {
		int relative=referenceStart-getReferenceStartPosition();
		if(getOrientation().equals(Strand.NEGATIVE)){
			relative=size()-relative;
		}
		return relative;
	}

	
}
