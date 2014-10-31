package guttmanlab.core.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A simple extension of a BlockedAnnotation that keeps track of the start and end of a coding region
 * @author mguttman
 *
 */
public class Gene extends BlockedAnnotation{

	private static final int NO_CDS=-1;
	private int cdsStartPos=NO_CDS;
	private int cdsEndPos=NO_CDS;
	
	public Gene(Collection<Annotation> blocks, int cdsStartPos, int cdsEndPos, String name) {
		super(blocks, name);
		this.cdsEndPos=cdsEndPos;
		this.cdsStartPos=cdsStartPos;
	}
	
	public Gene(Annotation block) {
		super(block);
	}

	/**
	 * @return A BlockedAnnotation representing the coding region of the gene
	 */
	public Annotation getCodingRegion(){
		if(cdsStartPos==NO_CDS || cdsEndPos==NO_CDS){return null;}
		SingleInterval cds=new SingleInterval(getReferenceName(), cdsStartPos, cdsEndPos);
		return intersect(cds);
	}
	
	@Override
	public String toString(){
		return toBED(0,0,0);
	}
	
	@Override
	public String toBED(int r, int g, int b){
		if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
			throw new IllegalArgumentException("RGB values must be between 0 and 255");
		}
		String rgb = r + "," + g + "," + b;
		Iterator<SingleInterval> exons = getBlocks();
		String rtrn=getReferenceName()+"\t"+getReferenceStartPosition()+"\t"+getReferenceEndPosition()+"\t"+getName() +"\t0.0\t"+getOrientation()+"\t"+this.cdsStartPos+"\t"+this.cdsEndPos+"\t"+rgb+"\t"+getNumberOfBlocks();
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
	
	//TODO wite tests
	public Collection<SingleInterval> getIntronSet()
	{
		Collection<SingleInterval> introns = new ArrayList<SingleInterval>();
		Iterator<SingleInterval> blocks = getBlocks();
		SingleInterval first;
		if(blocks.hasNext()){ first = blocks.next(); }
		else{ return introns; }
		
		while(blocks.hasNext())
		{
			SingleInterval next = blocks.next();
			SingleInterval intron = new SingleInterval(getReferenceName(),first.getReferenceEndPosition(),next.getReferenceStartPosition());
			introns.add(intron);
			first = next;
		}
		
		return introns;
	}
	
}
