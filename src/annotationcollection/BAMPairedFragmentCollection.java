package annotationcollection;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.CloseableIterator;
import coordinatespace.CoordinateSpace;
import datastructures.IntervalTree;
import datastructures.Pair;
import datastructures.IntervalTree.Node;
import annotation.Annotation;
import annotation.PairedMappedFragment;
import annotation.SAMFragment;

/**
 * This class represents a PairedFragmentCollection
 * @author mguttman
 *
 */
public class BAMPairedFragmentCollection extends AbstractAnnotationCollection<PairedMappedFragment<SAMFragment>>{

	private static final String extension=".pe.bam";
	private static final String ALIGNMENT_CIGAR = "aC";
	private static final String MATE_CIGAR = "mC";
	private static final String MATE_MAPPING_QUALITY="mQ";
	private File fragmentFile;
	private BAMSingleReadCollection reads;
	private SpecialBAMPECollection fragmentReader;
	
	public BAMPairedFragmentCollection(File bamFile){
		//Step 1: Initialize a Picard BAM Reader, represents reads
		reads=new BAMSingleReadCollection(bamFile);
		this.fragmentFile=makeFragmentFile(bamFile);
	}
	
	private File makeFragmentFile(File bamFile) {
		String baseName=bamFile.getName().split(".bam")[0];
		File file=new File(baseName+extension);
		file.deleteOnExit(); //TODO We might want to cache this, but we need to figure out how to make sure the files are in sync
		return file;
	}
	
	private SpecialBAMPECollection getPairedEndFragmentFile() {
		if(this.fragmentReader!=null){return fragmentReader;}
		else if(this.fragmentFile.exists()){
			fragmentReader=new SpecialBAMPECollection(fragmentFile);
			return fragmentReader;
		}
		else{
			//Get the paired iterator
			CloseableIterator<PairedMappedFragment<SAMFragment>> iter=sortedIterator();
			//Create an indexed BAM file
			SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(reads.getFileHeader(), false, fragmentFile);
			//For each pair, write it with appropriate flags
			while(iter.hasNext()){
				PairedMappedFragment<SAMFragment> pair=iter.next();
				convertToCustomSAMFormat(pair, writer);
			}
			//Close iterator and writer
			iter.close();
			writer.close();
			//Make fragment reader of special format
			fragmentReader=new SpecialBAMPECollection(fragmentFile);
			return fragmentReader;
		}
	}
	
	/**
	 * Convert the paired sam fragment to a custom SAM line to write
	 * @param pair the pair of SAM reads to convert
	 * @param writer The SAM file writer to writ this to
	 */
	private void convertToCustomSAMFormat(PairedMappedFragment<SAMFragment> pair, SAMFileWriter writer) {
		SAMRecord fragment=convertToCustomSAMFormat(pair);
		writer.addAlignment(fragment);
	}
	
	/**
	 * Create a SAMRecord from this pair
	 * @param pair The paired-end reads
	 * @return custom SAMRecord
	 */
	private SAMRecord convertToCustomSAMFormat(PairedMappedFragment<SAMFragment> pair) {
		Pair<SAMFragment> minMax=getMinMax(pair);
		SAMRecord alignment=minMax.getValue1().getSamRecord();
		SAMRecord mate=minMax.getValue2().getSamRecord();
		
		SAMRecord record = alignment;
		record.setMateAlignmentStart(alignment.getMateAlignmentStart());
		record.setAlignmentStart(alignment.getAlignmentStart());
		
		record.setAttribute(ALIGNMENT_CIGAR, record.getCigarString()); //Alignment cigar
		record.setAttribute(MATE_CIGAR, mate.getCigarString());
		record.setAttribute(MATE_MAPPING_QUALITY, mate.getMappingQuality());
		
		int fragmentLength=Math.max(pair.getRead1().getReferenceEndPosition(), pair.getRead2().getReferenceEndPosition())- Math.min(pair.getRead1().getReferenceStartPosition(), pair.getRead2().getReferenceStartPosition());
	    record.setCigarString(fragmentLength + "M");	
		record.setInferredInsertSize(fragmentLength);
		return record;
	}
	
	private Pair<SAMFragment> getMinMax(PairedMappedFragment<SAMFragment> pair) {
		SAMFragment min=pair.getRead1();
		SAMFragment max=pair.getRead2();
		if(pair.getRead2().getReferenceStartPosition()<pair.getRead1().getReferenceStartPosition()){
			min=pair.getRead2();
			max=pair.getRead1();
		}
		
		Pair<SAMFragment> rtrn=new Pair<SAMFragment>(min, max);
		return rtrn;
	}
	
	@Override
	public CloseableIterator<PairedMappedFragment<SAMFragment>> sortedIterator() {
		return new FilteredIterator<PairedMappedFragment<SAMFragment>>(new PairedIterator(reads.sortedIterator()), getFilters());
	}

	@Override
	public CloseableIterator<PairedMappedFragment<SAMFragment>> sortedIterator(Annotation region, boolean fullyContained) {
		//Go through the fragments iterator and parse from SAM into new format
		SpecialBAMPECollection fragments=this.getPairedEndFragmentFile();
		return fragments.sortedIterator(region, fullyContained);
	}

	/**
	 * Make the paired end fragments from single end reads
	 * @author mguttman
	 *
	 */
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
			boolean onSameReference=read.getSamRecord().getReferenceName().equalsIgnoreCase(read.getSamRecord().getMateReferenceName());

			if(isPaired && mateMapped && onSameReference){
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
	
	@Override
	public void writeToFile(String fileName) {
		writeToFile(fileName, sortedIterator());
	}

	@Override
	public void writeToFile(String fileName, Annotation region) {
		writeToFile(fileName, sortedIterator(region, false));
	}

	private void writeToFile(String fileName, CloseableIterator<PairedMappedFragment<SAMFragment>> iter) {
		SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(reads.getFileHeader(), false, new File(fileName));
		
		int counter=0;
		while(iter.hasNext()){
			PairedMappedFragment<SAMFragment> ann=iter.next();
			writer.addAlignment(ann.getRead1().getSamRecord());
			writer.addAlignment(ann.getRead2().getSamRecord());
			counter++;
			if(counter%10000 ==0){System.err.println(counter);}
		}
		writer.close();
		iter.close();
		
	}
	
	@Override
	public CoordinateSpace getReferenceCoordinateSpace() {
		return reads.getReferenceCoordinateSpace();
	}

	@Override
	public CoordinateSpace getFeatureCoordinateSpace() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	public SAMFileHeader getFileHeader() {
		return reads.getFileHeader();
	}

	/**
	 * This class will parse our special PE format BAM file 
	 * @author mguttman
	 */
	private class SpecialBAMPECollection extends AbstractAnnotationCollection<PairedMappedFragment<SAMFragment>>{

		private SAMFileReader reader;
		private CoordinateSpace referenceSpace;
		
		public SpecialBAMPECollection(File bamFile){
			super();
			this.reader=new SAMFileReader(bamFile);
			this.referenceSpace=new CoordinateSpace(reader.getFileHeader());
		}
		
		@Override
		public CloseableIterator<PairedMappedFragment<SAMFragment>> sortedIterator() {
			return new FilteredIterator<PairedMappedFragment<SAMFragment>>(new WrappedIterator(reader.iterator()), getFilters());
		}

		@Override
		public CloseableIterator<PairedMappedFragment<SAMFragment>> sortedIterator(Annotation region, boolean fullyContained) {
			SAMRecordIterator iter=reader.queryOverlapping(region.getReferenceName(), region.getReferenceStartPosition(), region.getReferenceEndPosition());
			return new FilteredIterator<PairedMappedFragment<SAMFragment>>(new WrappedIterator(iter), getFilters());
		}

		@Override
		public void writeToFile(String fileName) {
			writeToFile(fileName, sortedIterator());
		}

		@Override
		public void writeToFile(String fileName, Annotation region) {
			writeToFile(fileName, sortedIterator(region, false));
		}
		
		private void writeToFile(String fileName, CloseableIterator<PairedMappedFragment<SAMFragment>> iter){
			SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(reader.getFileHeader(), false, new File(fileName));
		
			while(iter.hasNext()){
				PairedMappedFragment<SAMFragment> ann=iter.next();
				writer.addAlignment(ann.getRead1().getSamRecord());
				writer.addAlignment(ann.getRead2().getSamRecord());
			}
			writer.close();
			iter.close();
		}
		
		@Override
		public CoordinateSpace getReferenceCoordinateSpace() {
			return this.referenceSpace;
		}

		@Override
		public CoordinateSpace getFeatureCoordinateSpace() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO");
		}
		
		
		private class WrappedIterator implements CloseableIterator<PairedMappedFragment<SAMFragment>>{

			SAMRecordIterator iter;
			
			public WrappedIterator(SAMRecordIterator iter){
				this.iter=iter;
			}
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public PairedMappedFragment<SAMFragment> next() {
				SAMRecord record=iter.next();
				PairedMappedFragment<SAMFragment> reads=getSAMRecords(reader.getFileHeader(), record);
				return reads;
			}

			/**
			 * Split the record into the two component reads
			 * @param fileHeader The file header from the parsed BAM file
			 * @param record The record to split
			 * @return The paired end fragment with the two reads
			 */
			private PairedMappedFragment<SAMFragment> getSAMRecords(SAMFileHeader fileHeader, SAMRecord record) {
				//Make Read1
				SAMRecord read1=new SAMRecord(fileHeader);
				read1.setAlignmentStart(record.getAlignmentStart());
				read1.setCigarString(record.getAttribute(ALIGNMENT_CIGAR).toString());
				read1.setFirstOfPairFlag(record.getFirstOfPairFlag());
				read1.setReadName(record.getReadName());
				read1.setReadNegativeStrandFlag(record.getReadNegativeStrandFlag());
				read1.setReferenceName(record.getReferenceName());
				read1.setReadPairedFlag(true);
				read1.setMappingQuality(record.getMappingQuality());
				
				//Make Read2
				SAMRecord read2=new SAMRecord(fileHeader);
				read2.setAlignmentStart(record.getMateAlignmentStart());
				read2.setCigarString(record.getAttribute(MATE_CIGAR).toString());
				read2.setFirstOfPairFlag(!record.getFirstOfPairFlag());
				read2.setReadName(record.getReadName());
				read2.setReadNegativeStrandFlag(record.getMateNegativeStrandFlag());
				read2.setReferenceName(record.getMateReferenceName());
				read2.setReadPairedFlag(true);
				read2.setMappingQuality(new Integer(record.getAttribute(MATE_MAPPING_QUALITY).toString()));
				
				//Add mate info to each read
				//Read1
				read1.setMateAlignmentStart(record.getMateAlignmentStart());
				read1.setMateNegativeStrandFlag(record.getMateNegativeStrandFlag());
				read1.setMateReferenceName(record.getMateReferenceName());
				read1.setMateUnmappedFlag(record.getMateUnmappedFlag());
				read1.setProperPairFlag(record.getProperPairFlag());
				
				//Read2
				read2.setMateAlignmentStart(record.getAlignmentStart());
				read2.setMateNegativeStrandFlag(record.getReadNegativeStrandFlag());
				read2.setMateReferenceName(record.getReferenceName());
				read2.setMateUnmappedFlag(record.getReadUnmappedFlag());
				read2.setProperPairFlag(record.getProperPairFlag());
				
				SAMFragment frag1=new SAMFragment(read1);
				SAMFragment frag2=new SAMFragment(read2);
				
				if(!record.getFirstOfPairFlag()){return new PairedMappedFragment<SAMFragment>(frag1, frag2);}
				
				return new PairedMappedFragment<SAMFragment>(frag1, frag2);	
			}

			@Override
			public void remove() {iter.remove();}

			@Override
			public void close() {iter.close();}
		}
	}	
}