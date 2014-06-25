package annotationcollection;


import java.io.File;


import coordinatespace.CoordinateSpace;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.CloseableIterator;
import annotation.Annotation;
import annotation.SAMFragment;

public class PicardBAMReadCollection extends AbstractAnnotationCollection<SAMFragment>{

	private SAMFileReader reader;
	private CoordinateSpace referenceSpace;
	
	public PicardBAMReadCollection(File bamFile){
		super();
		this.reader=new SAMFileReader(bamFile);
		this.referenceSpace=new CoordinateSpace(reader.getFileHeader());
	}

	@Override
	public CloseableIterator<SAMFragment> iterator() {
		return new FilteredIterator<SAMFragment>(new WrappedIterator(reader.iterator()), getFilters());
	}

	@Override
	public CloseableIterator<SAMFragment> iterator(Annotation region, boolean fullyContained) {
		SAMRecordIterator iter=reader.queryOverlapping(region.getReferenceName(), region.getReferenceStartPosition(), region.getReferenceEndPosition());
		return new FilteredIterator<SAMFragment>(new WrappedIterator(iter), getFilters());
	}

	@Override
	public void writeToFile(String fileName) {
		CloseableIterator<SAMFragment> iter= iterator();
		writeToFile(fileName, iter);
	}
	
	private void writeToFile(String fileName, CloseableIterator<SAMFragment> iter){
		SAMFileWriter writer=new SAMFileWriterFactory().makeSAMOrBAMWriter(this.reader.getFileHeader(), true, new File(fileName));
		
		while(iter.hasNext()){
			SAMFragment ann=iter.next();
			writer.addAlignment(ann.getSamRecord());
		}
		writer.close();
		iter.close();
	}
	
	public void writeToFile(String fileName, Annotation region) {
		CloseableIterator<SAMFragment> iter= iterator(region, false);
		writeToFile(fileName, iter);
	}
	
	public class WrappedIterator implements CloseableIterator<SAMFragment>{

		SAMRecordIterator iter;
		
		public WrappedIterator(SAMRecordIterator iter){
			this.iter=iter;
		}
		
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public SAMFragment next() {
			return new SAMFragment(iter.next());
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			iter.close();
		}
	}

	@Override
	public CoordinateSpace getReferenceCoordinateSpace() {
		return this.referenceSpace;
	}

	@Override
	public CoordinateSpace getFeatureCoordinateSpace() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SAMFileHeader getFileHeader(){return this.reader.getFileHeader();}
}
