package annotationcollection;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.CloseableIterator;
import peakcaller.predicate.RegionOverlapFilter;
import coordinatespace.CoordinateSpace;
import datastructures.IntervalTree;
import datastructures.Pair;
import datastructures.IntervalTree.Node;
import annotation.Annotation;
import annotation.BlockedAnnotation;
import annotation.PairedMappedFragment;
import annotation.SAMFragment;
import annotation.SingleInterval;
import annotation.Window;
import annotation.predicate.InsertSizeFilter;

/**
 * A test implementation to deal with Paired End reads
 * @author mguttman
 *
 */
public class PicardBAMPairedEndFragmentCollection extends AbstractAnnotationCollection<PairedMappedFragment<SAMFragment>>{

	private static final String extension=".pe.bam";
	private File fragmentFile;
	private PicardBAMReadCollection reads;
	private SAMFileReader fragmentReader;
	private int maxSize;
	private boolean useMaxSize=false;
	
	public PicardBAMPairedEndFragmentCollection(File bamFile){
		//Step 1: Initialize a Picard BAM Reader, represents reads
		reads=new PicardBAMReadCollection(bamFile);
		this.fragmentFile=makeFragmentFile(bamFile);
	}
	
	/**
	 * @param bamFile BAM File to initialize with
	 * @param maxSize The max insert size to use
	 */
	public PicardBAMPairedEndFragmentCollection(File bamFile, int maxSize){
		this(bamFile);
		setMaxInsertSize(maxSize);
	}
	
	private File makeFragmentFile(File bamFile) {
		String baseName=bamFile.getName().split(".bam")[0];
		return new File(baseName+"."+extension);
	}

	private SAMFileReader getPairedEndFragmentFile() {
		if(this.fragmentReader!=null){return fragmentReader;}
		else if(this.fragmentFile.exists()){
			//TODO We should check if the file exists, if so, parse
			//TODO How do we make sure that the files are in sync?
		}
		
		CloseableIterator<PairedMappedFragment<SAMFragment>> iter=iterator();
		SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(reads.getFileHeader(), false, fragmentFile);
		
		while(iter.hasNext()){
			PairedMappedFragment<SAMFragment> pair=iter.next();
			write(pair, writer);
		}
		
		iter.close();
		writer.close();
		
		fragmentReader=new SAMFileReader(fragmentFile);
		return fragmentReader;
	}
	
	/**
	 * Whether to use a max insert size
	 * @param size max insert size to use
	 */
	public void setMaxInsertSize(int size){
		this.maxSize=size;
		this.useMaxSize=true;
		InsertSizeFilter<PairedMappedFragment<SAMFragment>> filter=new InsertSizeFilter<PairedMappedFragment<SAMFragment>>(size);
		this.addFilter(filter);
	}
	
	private CloseableIterator<PairedMappedFragment<SAMFragment>> getPairedIterator(CloseableIterator<SAMFragment> iter) {
		return new PairedIterator(iter);
	}
	
	private class PairedIterator implements CloseableIterator<PairedMappedFragment<SAMFragment>>{

		CloseableIterator<SAMFragment> iter;
		Pair<SAMFragment> fullyFormed;
		Map<String, Pair<SAMFragment>> partials;
		String currentReference="";
		IntervalTree<String> matePosition;
		
		public PairedIterator(CloseableIterator<SAMFragment> iter){
			this.iter=iter;
			this.partials=new TreeMap<String, Pair<SAMFragment>>();
			this.matePosition=new IntervalTree<String>();
		}
		
		@Override
		public boolean hasNext() {
			if(fullyFormed!=null){return true;}
			else if(iter.hasNext()){update(); return hasNext();}
			return false;
		}

		private void update() {
			SAMFragment read=iter.next();
			
			//When switching from chromosome we should clear cache
			if(!read.getReferenceName().equalsIgnoreCase(currentReference)){
				currentReference=read.getReferenceName();
				this.partials=new TreeMap<String, Pair<SAMFragment>>();
				this.matePosition=new IntervalTree<String>();
			}
			
			
			//check if read has a pair
			boolean isPaired=read.getSamRecord().getReadPairedFlag();
			boolean mateMapped=!read.getSamRecord().getMateUnmappedFlag();
			
			if(isPaired && mateMapped){
				Pair<SAMFragment> pair;
				if(partials.containsKey(read.getName())){
					pair=partials.remove(read.getName());
				}
				else{pair=new Pair<SAMFragment>();}
				
				if(read.getSamRecord().getFirstOfPairFlag()){
					if(pair.hasValue1()){System.err.println("WARN: Overriding value 1");}
					pair.setValue1(read);
				}
				else{
					if(pair.hasValue2()){System.err.println("WARN: Overriding value 2");}
					pair.setValue2(read);
				}
				
				if(pair.hasValue1() && pair.hasValue2()){
					fullyFormed=pair;
					this.matePosition.remove(read.getSamRecord().getAlignmentStart(), read.getSamRecord().getAlignmentStart());
				}
				else if(read.getSamRecord().getAlignmentStart()<read.getSamRecord().getMateAlignmentStart()){
					partials.put(read.getName(), pair);
					this.matePosition.put(read.getSamRecord().getMateAlignmentStart(), read.getSamRecord().getMateAlignmentStart(), read.getName());
				}
				else{
					//TODO Consider saving the unmapped reads
				}
				
				removePartials(read);
			}
		}

		/**
		 * Check the number of partials less than the read-max
		 * @param read
		 * @return
		 */
		private void removePartials(SAMFragment read) {
			Iterator<Node<String>> iter=this.matePosition.reverseIterator(read.getSamRecord().getAlignmentStart(), read.getSamRecord().getAlignmentStart());
			while(iter.hasNext()){
				Node<String> node=iter.next();
				Pair<SAMFragment> pair=this.partials.remove(node.getValue()); //TODO Consider saving the unmapped reads
				this.matePosition.remove(node.getStart(), node.getEnd());
			}
		}

		@Override
		public PairedMappedFragment<SAMFragment> next() {
			PairedMappedFragment<SAMFragment> rtrn=new PairedMappedFragment<SAMFragment>(fullyFormed);
			fullyFormed=null;
			return rtrn;
		}

		@Override
		public void remove() {
			iter.remove();
		}

		@Override
		public void close() {
			iter.close();
			
		}}

	private void write(PairedMappedFragment<SAMFragment> pair, SAMFileWriter writer) {
		SAMRecord fragment=toSAMRecord(pair);
		writer.addAlignment(fragment);
	}
	
	
	public SAMRecord toSAMRecord(PairedMappedFragment<SAMFragment> pair) {
		SAMFragment min=getMin(pair);
		
		SAMRecord record = min.getSamRecord();
		record.setMateAlignmentStart(min.getSamRecord().getMateAlignmentStart());
		record.setAlignmentStart(min.getSamRecord().getAlignmentStart());
		
		int fragmentLength=Math.max(pair.getRead1().getReferenceEndPosition(), pair.getRead2().getReferenceEndPosition())- Math.min(pair.getRead1().getReferenceStartPosition(), pair.getRead2().getReferenceStartPosition());
	    record.setCigarString(fragmentLength + "M");	
		record.setInferredInsertSize(fragmentLength);
		return record;
	}

	private SAMFragment getMin(PairedMappedFragment<SAMFragment> pair) {
		SAMFragment rtrn=pair.getRead1();
		if(pair.getRead2().getReferenceStartPosition()<pair.getRead1().getReferenceStartPosition()){rtrn=pair.getRead2();}
		return rtrn;
	}

	/**
	 * @return An iterator over all the annotations in this collection
	 */
	public CloseableIterator<PairedMappedFragment<SAMFragment>> iterator(){
		//go through the PairedEnd fragments
		CloseableIterator<PairedMappedFragment<SAMFragment>> iter=new FilteredIterator<PairedMappedFragment<SAMFragment>>(getPairedIterator(reads.iterator()), getFilters());
		return iter;
	}
	
	/**
	 * Get an iterator over the set of annotations in this collection that overlap the region
	 * @param region Region to check for overlappers
	 * @param fullyContained Whether the overlappers must be fully contained in the region
	 * @return Iterator over the set of overlappers
	 */	
	public CloseableIterator<PairedMappedFragment<SAMFragment>> iterator(Annotation region, boolean fullyContained){
		//This is the iterator that gets complicated because there may be fragments that overlap region where neither read does
		
		//Step 1: Setup Paired End Fragment parser, represents fragments
		fragmentReader=getPairedEndFragmentFile();
		
		//query fragments overlapping region
		Annotation fragmentRegion=getFragmentRegions(region);
		
		//go through each record from low to high of fragment and test overlap of fragment
		CloseableIterator<PairedMappedFragment<SAMFragment>> readIter=new FilteredIterator<PairedMappedFragment<SAMFragment>>(getPairedIterator(this.reads.iterator(fragmentRegion, fullyContained)), getFilters(), new RegionOverlapFilter<PairedMappedFragment<SAMFragment>>(region));
		
		return readIter;
	}
	
	private Annotation getFragmentRegions(Annotation region) {
		SAMRecordIterator iter=fragmentReader.queryOverlapping(region.getReferenceName(), region.getReferenceStartPosition(), region.getReferenceEndPosition());
		
		int minStart=-1;
		int maxEnd=-1;
		String referenceName="";
		while(iter.hasNext()){
			SAMRecord record=iter.next();
			if(!this.useMaxSize || record.getAlignmentEnd()-record.getAlignmentStart()<this.maxSize){
				if(minStart>-1 && maxEnd>-1){
					minStart=Math.min(minStart, record.getAlignmentStart()-1);
					maxEnd=Math.max(maxEnd, record.getAlignmentEnd());
				}
				else{
					minStart=record.getAlignmentStart()-1;
					maxEnd=record.getAlignmentEnd();
				}
				
				referenceName=record.getReferenceName();
			}
		}
		return new SingleInterval(referenceName, minStart, maxEnd);
	}

	/**
	 * Write the collection of annotations (using filters) to a file
	 * @param fileName The file to write to
	 */
	public void writeToFile(String fileName){
		CloseableIterator<PairedMappedFragment<SAMFragment>> iter=iterator();
		writeToFile(fileName, iter);
	}

	@Override
	public CoordinateSpace getReferenceCoordinateSpace() {
		return this.reads.getReferenceCoordinateSpace();
	}

	@Override
	public CoordinateSpace getFeatureCoordinateSpace() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void writeToFile(String fileName, Annotation region) {
		CloseableIterator<PairedMappedFragment<SAMFragment>> iter=iterator(region, false);
		writeToFile(fileName, iter);
	}

	private void writeToFile(String fileName, CloseableIterator<PairedMappedFragment<SAMFragment>> iter) {
		SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(reads.getFileHeader(), false, new File(fileName));
		
		while(iter.hasNext()){
			PairedMappedFragment<SAMFragment> ann=iter.next();
			writer.addAlignment(ann.getRead1().getSamRecord());
			writer.addAlignment(ann.getRead2().getSamRecord());
		}
		writer.close();
		iter.close();
		
	}
	
	@Override
	public CloseableIterator<BlockedAnnotation> convertCoordinates(CloseableIterator<? extends Annotation> annotations, CoordinateSpace referenceSpaceForAnnotations){
		//TODO Should check the coordinate space and convert appropriately
		throw new UnsupportedOperationException("TODO");
		
		//TODO We need to override this method because it is not well defined to convert paired fragments into the new space
		//We should do this by converting the reads to the new space and then establishing a new paired object
	}
	
	@Override
	public CloseableIterator<? extends Window<PairedMappedFragment<SAMFragment>>> getWindows(Annotation region, int windowLength){
		throw new UnsupportedOperationException("TODO");
		//TODO This may need to be rewritten
	}

}
