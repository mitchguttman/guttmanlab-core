package annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import datastructures.IntervalTree;


public class BlockedAnnotation extends AbstractAnnotation {

	//private Collection<SingleInterval> blocks; //TODO Replace this with tree
	
	private IntervalTree<SingleInterval> blocks;
	private String referenceName;
	private int startPosition;
	private int endPosition;
	private int size;
	private boolean started;
	private String name;
	private Strand orientation;
	private int numBlocks;
	
	/**
	 * An empty constructor
	 */
	public BlockedAnnotation(){
		this.blocks=new IntervalTree<SingleInterval>();
		this.started=false;
	}
	
	/**
	 * A blocked annotation is defined by its blocks
	 * @param blocks
	 * @param name 
	 */
	public BlockedAnnotation(Collection<Annotation> blocks, String name){
		this();
		this.name=name;
		for(Annotation block: blocks){
			addBlock(block);
		}
	}
	
	public BlockedAnnotation(Annotation block){
		this();
		this.name=block.getName();
		addBlock(block);
	}
	
	/**
	 * Add block to current blocks
	 * If overlaps existing block, merge
	 * Requires blocks have the same reference name and Strand
	 * @param block block to add
	 * @return if the block was successfully added
	 */
	public boolean addBlock(Annotation block) {
		boolean added=false;
		Iterator<SingleInterval> exons=block.getBlocks();
		while(exons.hasNext()){
			added=update(exons.next());
		}
		return added;
	}

	/**
	 * Helper method to add single interval
	 * @param interval The interval
	 * @return whether it was successfully added
	 */
	private boolean update(SingleInterval interval) {
		if(!started){
			this.referenceName=interval.getReferenceName();
			this.startPosition=interval.getReferenceStartPosition();
			this.endPosition=interval.getReferenceEndPosition();
			this.orientation=interval.getOrientation();
			started=true;
		}
		else{
			if(!this.referenceName.equalsIgnoreCase(interval.getReferenceName())){return false;}
			if(!this.orientation.equals(interval.getOrientation())){return false;}
			this.startPosition=Math.min(startPosition, interval.getReferenceStartPosition());
			this.endPosition=Math.max(endPosition, interval.getReferenceEndPosition());
		}
		
		
		//TODO Ensure that blocks that overlap an existing one are collapsed before adding
		//check if tree has an overlapper
		boolean hasOverlappers=blocks.hasOverlappers(interval.getReferenceStartPosition(), interval.getReferenceEndPosition());
		SingleInterval merged=interval;
		if(hasOverlappers){
			//pull, merge, and update
			Iterator<SingleInterval> iter=blocks.overlappingValueIterator(interval.getReferenceStartPosition(), interval.getReferenceEndPosition());
			while(iter.hasNext()){
				SingleInterval e=iter.next();
				merged=merge(merged, e);
				size-=e.size();
				numBlocks--;
			}
		}
		blocks.put(merged.getReferenceStartPosition(), merged.getReferenceEndPosition(), merged);
		size+=merged.size();
		numBlocks++;
		
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}

	
	public Iterator<SingleInterval> getBlocks() {
		return this.blocks.valueIterator();
	}

	@Override
	public String getReferenceName() {
		return this.referenceName;
	}

	@Override
	public int getReferenceStartPosition() {
		return this.startPosition;
	}

	@Override
	public int getReferenceEndPosition() {
		return this.endPosition;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Strand getOrientation() {
		return this.orientation;
	}


	@Override
	public int getNumberOfBlocks() {
		return this.numBlocks;
	}
	
	public int getRelativePositionFrom5PrimeOfFeature(int referenceStart){
		Iterator<SingleInterval> iter=this.blocks.overlappingValueIterator(this.getReferenceStartPosition(), referenceStart);
		int relativeSize=0;
		while(iter.hasNext()){
			SingleInterval interval=iter.next();
			if(interval.getReferenceEndPosition()<referenceStart){
				relativeSize+=interval.size(); //except when overlapping exactly the referenceStart
			}
			else{
				relativeSize+=(referenceStart-interval.getReferenceStartPosition());
			}
		}
		
		//If strand is neg, then position is from end
		if(getOrientation().equals(Annotation.Strand.NEGATIVE)){
			relativeSize=this.size-relativeSize;
		}
		return relativeSize;
	}
	
	public BlockedAnnotation convertToFeatureSpace(SingleInterval region){
		int featureStart=getRelativePositionFrom5PrimeOfFeature(region.getReferenceStartPosition());
		int featureEnd=getRelativePositionFrom5PrimeOfFeature(region.getReferenceEndPosition());
		BlockedAnnotation interval;
		if(getOrientation().equals(Strand.NEGATIVE)){
			interval=new BlockedAnnotation(new SingleInterval(getName(), featureEnd, featureStart));
		}
		else{interval=new BlockedAnnotation(new SingleInterval(getName(), featureStart, featureEnd));}
		return interval;
	}
}
