package annotation;

import java.util.Collection;
import java.util.Iterator;

import annotation.Annotation.Strand;
import net.sf.samtools.util.CloseableIterator;
import coordinatespace.CoordinateSpace;

/**
 * An abstract class that implements many of the shared features of an annotation
 * @author mguttman
 *
 */
public abstract class AbstractAnnotation implements Annotation{

	@Override
	public BlockedAnnotation intersect(Annotation other) {
		BlockedAnnotation rtrn=new BlockedAnnotation();
		Iterator<SingleInterval> blocks1=getBlocks();
		while(blocks1.hasNext()){
			SingleInterval block1=blocks1.next();
			Iterator<SingleInterval> blocks2=other.getBlocks();
			while(blocks2.hasNext()){
				SingleInterval block2=blocks2.next();
				SingleInterval inter=intersect(block1, block2);
				if(inter!=null){rtrn.addBlock(inter);}
			}
			
		}
		return rtrn;
	}
	
	/**
	 * Helper method to compute the overlap between single blocks
	 * @param block1 Block1
	 * @param block2 Block2
	 * @return The intersection or null if no intersection exists
	 */
	private SingleInterval intersect(SingleInterval block1, SingleInterval block2) {
		if(!overlaps(block1, block2)){return null;}
		int newStart=Math.max(block1.getReferenceStartPosition(), block2.getReferenceStartPosition());
		int newEnd=Math.min(block1.getReferenceEndPosition(), block2.getReferenceEndPosition());
		Strand consensus=Annotation.Strand.consensusStrand(block1.getOrientation(), block2.getOrientation());
		return new SingleInterval(block1.getReferenceName(), newStart, newEnd, consensus);
	}

	@Override
	public BlockedAnnotation merge(Annotation other) {
		BlockedAnnotation rtrn=new BlockedAnnotation();
		Iterator<SingleInterval> blocks1=getBlocks();
		while(blocks1.hasNext()){
			SingleInterval block1=blocks1.next();
			Iterator<SingleInterval> blocks2=other.getBlocks();
			while(blocks2.hasNext()){
				SingleInterval block2=blocks2.next();
				if(block1.overlaps(block2)){
					SingleInterval merge=merge(block1, block2);
					if(merge!=null){rtrn.addBlock(merge);}
				}
			}
			
		}
		return rtrn;
	}

	protected SingleInterval merge(SingleInterval block1, SingleInterval block2) {
		if(!overlaps(block1, block2)){return null;}
		
		int newStart=Math.min(block1.getReferenceStartPosition(), block2.getReferenceStartPosition());
		int newEnd=Math.max(block1.getReferenceEndPosition(), block2.getReferenceEndPosition());
		Strand consensus=Annotation.Strand.consensusStrand(block1.getOrientation(), block2.getOrientation());
		return new SingleInterval(block1.getReferenceName(), newStart, newEnd, consensus);
	}

	@Override
	public <T extends Annotation> T minus(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public boolean overlaps(Annotation other) {
		//Check if the blocks overlap
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Helper method to calculate overlaps from single blocks
	 * @param block1
	 * @param block2
	 * @return whether the blocks overlap
	 */
	private boolean overlaps(SingleInterval block1, SingleInterval block2){
		int newStart=Math.max(block1.getReferenceStartPosition(), block2.getReferenceStartPosition());
		int newEnd=Math.min(block1.getReferenceEndPosition(), block2.getReferenceEndPosition());
		
		Strand consensusStrand=Annotation.Strand.consensusStrand(block1.getOrientation(), block2.getOrientation());
		if(newStart<newEnd && block1.getReferenceName().equalsIgnoreCase(block2.getReferenceName()) && !consensusStrand.equals(Annotation.Strand.INVALID)){
			return true;
		}
		
		return false;
	}

	@Override
	public boolean contains(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public String toString(){
		return toBED(0,0,0);
	}
	
	public String toBED(int r, int g, int b){
		if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
			throw new IllegalArgumentException("RGB values must be between 0 and 255");
		}
		String rgb = r + "," + g + "," + b;
		Iterator<SingleInterval> exons = getBlocks();
		String rtrn=getReferenceName()+"\t"+getReferenceStartPosition()+"\t"+getReferenceEndPosition()+"\t"+(getName() == null ? toUCSC() : getName())+"\t0.0\t"+getOrientation()+"\t"+getReferenceEndPosition()+"\t"+getReferenceEndPosition()+"\t"+rgb+"\t"+getNumberOfBlocks();
		String sizes="";
		String starts="";
		while(exons.hasNext()){
			SingleInterval exon=exons.next();
			sizes=sizes+(exon.size())+",";
			starts=starts+(exon.getReferenceStartPosition()-getReferenceStartPosition())+",";
		}
		rtrn=rtrn+"\t"+sizes+"\t"+starts;
		return rtrn;
	}

	private String toUCSC() {
		return getReferenceName()+":"+getReferenceStartPosition()+"-"+getReferenceEndPosition();
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
