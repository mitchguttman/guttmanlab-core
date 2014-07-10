package annotation;

import java.util.Iterator;

import annotation.Annotation.Strand;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;

/**
 * An abstract class that implements many of the shared features of an annotation
 * @author mguttman
 *
 */
public abstract class AbstractAnnotation implements Annotation {

	@Override
	public Annotation intersect(Annotation other) {
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
	public Annotation merge(Annotation other) {
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
	public Annotation minus(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public boolean overlaps(Annotation other) {
		//TODO This method still needs to be tested to ensure that it does what we expect
		//Check if the blocks overlap
		Iterator<SingleInterval> blocks1=getBlocks();
		while(blocks1.hasNext()){
			SingleInterval block1=blocks1.next();
			Iterator<SingleInterval> blocks2=other.getBlocks();
			while(blocks2.hasNext()){
				SingleInterval block2=blocks2.next();
				if(overlaps(block1, block2)){
					return true;
				}
			}	
		}
		return false;
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
	
	public Annotation convertToFeatureSpace(Annotation region){
		//Ensure that region overlaps feature
		if(overlaps(region)){
			int featureStart=getRelativePositionFrom5PrimeOfFeature(region.getReferenceStartPosition());
			int featureEnd=getRelativePositionFrom5PrimeOfFeature(region.getReferenceEndPosition());
			Annotation interval;
			if(featureStart>-1 && featureEnd>-1){
				if(getOrientation().equals(Strand.NEGATIVE)){
					interval=new SingleInterval(getName(), featureEnd, featureStart); //TODO Check strand orientation
				}
				else{interval=new SingleInterval(getName(), featureStart, featureEnd);}
				return interval;
			}
		}
		return null;
	}
	
	public Annotation convert(Annotation feature){
		//Ensure that region overlaps feature
		if(overlaps(feature)){
			int featureStart=feature.getRelativePositionFrom5PrimeOfFeature(getReferenceStartPosition());
			int featureEnd=feature.getRelativePositionFrom5PrimeOfFeature(getReferenceEndPosition());
			Annotation interval;
			if(featureStart>-1 && featureEnd>-1){
				if(getOrientation().equals(Strand.NEGATIVE)){
					interval=new SingleInterval(getName(), featureEnd, featureStart);
				}
				else{interval=new SingleInterval(getName(), featureStart, featureEnd);}
				return interval;
			}
		}
		return null;
	}
	
	public Annotation convertToReferenceSpace(Annotation featureAnnotation){
		BlockedAnnotation rtrn=new BlockedAnnotation();
		Iterator<SingleInterval> blocks = getBlocks();
		int sumBlocks=0;
		
		while(blocks.hasNext()){
			SingleInterval block=blocks.next();
			SingleInterval featureSpaceBlock=new SingleInterval(getName(), sumBlocks, sumBlocks+block.size());

			if(getOrientation().equals(Strand.NEGATIVE))
			{
				featureSpaceBlock= new SingleInterval(getName(), size()-(sumBlocks+block.size()),size()-sumBlocks);
			}
			
			if(featureAnnotation.overlaps(featureSpaceBlock)){
				//trim it, add it
				int shiftStart=0;
				int shiftEnd=0;
				if(featureAnnotation.getReferenceStartPosition()> featureSpaceBlock.getReferenceStartPosition()){
					shiftStart=featureAnnotation.getReferenceStartPosition()-featureSpaceBlock.getReferenceStartPosition();
				}
				if(featureAnnotation.getReferenceEndPosition()<featureSpaceBlock.getReferenceEndPosition())	{
					shiftEnd=featureSpaceBlock.getReferenceEndPosition()-featureAnnotation.getReferenceEndPosition();
				}
				block=block.trim(shiftStart, featureSpaceBlock.size()-shiftEnd);
				
				rtrn.addBlock(block);
			}
			sumBlocks=sumBlocks+block.size();
		}
		return rtrn;
		
	}
	
	@Override
	public String getCigarString(){
		Iterator<SingleInterval> blocks=getBlocks();
		String cigar="";
		
		int lastEnd=-1;
		while(blocks.hasNext()){
			SingleInterval block=blocks.next();
			if(lastEnd>0){
				int distance=block.getReferenceStartPosition()-lastEnd;
				cigar+=distance+"N";
			}
			cigar+=block.size()+"M";
			lastEnd=block.getReferenceEndPosition();
		}
		return cigar;
	}
	
	@Override
	public SAMRecord getSamRecord(SAMFileHeader header){
		SAMRecord record=new SAMRecord(header);
		record.setAlignmentStart(getReferenceStartPosition()+1);
		record.setCigarString(getCigarString());
		record.setReferenceName(getReferenceName());
		record.setReadName(getName());
		record.setReadNegativeStrandFlag(getOrientation().equals(Strand.NEGATIVE));
		return record;
	}
	
	public boolean fullyContained(Annotation other){
		//All blocks in other must be in blocks on this
		//Go through all blocks2 and check that they are in this
		Iterator<SingleInterval> blocks2=other.getBlocks();
		while(blocks2.hasNext()){
			SingleInterval block2=blocks2.next();
			boolean isInThis=false;
			//check that each block2 has some overlap with a block1
			Iterator<SingleInterval> blocks1=getBlocks();
			while(blocks1.hasNext()){
				SingleInterval block1=blocks1.next();
				if(block1.overlaps(block2)){
					isInThis=true;
					if(!fullyContained(block1, block2)){
						return false;
					}
				}
			}
			if(!isInThis){return false;} //There are no blocks in this that the block overlapped, cant be fully contained
		}
		
		return true;
	}
	
	private boolean fullyContained(SingleInterval block1, SingleInterval block2){
		//is only fully contained if block2 is equal to or a subset of block1
		if(block1.getReferenceStartPosition()<=block2.getReferenceStartPosition() && block1.getReferenceEndPosition()>=block2.getReferenceEndPosition()){
			return true;
		}
		return false;	
	}
	
}
