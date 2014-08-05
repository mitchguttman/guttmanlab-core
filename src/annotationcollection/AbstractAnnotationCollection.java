package annotationcollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.Predicate;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.util.CloseableIterator;
import annotation.Annotation;
import annotation.ContiguousWindow;
import annotation.DerivedAnnotation;
import annotation.SAMFragment;
import annotation.SingleInterval;
import annotation.Window;
import annotation.Annotation.Strand;
import coordinatespace.CoordinateSpace;
import datastructures.IntervalTree;

public abstract class AbstractAnnotationCollection<T extends Annotation> implements AnnotationCollection<T>{

	private Collection<Predicate<T>> filters;
	private int numAnnotations;

	public AbstractAnnotationCollection(){
		filters=new ArrayList<Predicate<T>>();
	}

	@Override
	public void addFilter(Predicate<T> filter) {
		filters.add(filter);
		numAnnotations = 0;
	}

	@Override
	public Collection<Predicate<T>> getFilters(){
		return filters;
	}

	@Override
	public AnnotationCollection<T> merge() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean overlaps(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <X extends Annotation> AnnotationCollection<DerivedAnnotation<X>> convertCoordinates(AnnotationCollection<X> readCollection, CoordinateSpace referenceSpaceForAnnotations, boolean fullyContained){
		//TODO Should check the coordinate space and convert appropriately
		return convertFromReferenceSpace(readCollection, referenceSpaceForAnnotations, fullyContained);
	}

	private <X extends Annotation> AnnotationCollection<DerivedAnnotation<X>> convertFromReferenceSpace(AnnotationCollection<X> readCollection, CoordinateSpace referenceSpace, boolean fullyContained){
		return new ConvertedSpace<X>(readCollection, this, referenceSpace, fullyContained);
	}

	
	
	@Override
	public CloseableIterator<? extends Window<T>> getWindows(Annotation region, int windowLength){
		CloseableIterator<T> iter=sortedIterator(region, false);
		return new WindowIterator<T>(iter, windowLength, region);
	}

	@Override
	public int numOverlappers(Annotation region, boolean fullyContained) {
		int counter=0;
		CloseableIterator<T> iter=sortedIterator(region, fullyContained);
		while(iter.hasNext()){
			iter.next();
			counter++;
		}
		iter.close();
		return counter;
	}
	
	@Override
	public void writeToBAM(String fileName){
		writeToBAM(fileName, sortedIterator());
	}
	

	@Override
	public void writeToBAM(String fileName, Annotation region, boolean fullyContained){
		writeToBAM(fileName, sortedIterator(region, fullyContained));
	}
	
	private void writeToBAM(String fileName, CloseableIterator<T> iter){
		SAMFileHeader header=getReferenceCoordinateSpace().getBAMFileHeader();
		SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(header, false, new File(fileName));
			
		while(iter.hasNext()){
			T ann=iter.next();
			writer.addAlignment(ann.getSamRecord(header));
		}
		writer.close();
		iter.close();
	}
	


	/**
	 * This class requires that you have a sorted iterator of reads
	 * @author mguttman
	 *
	 * @param <T1>
	 */
	public class WindowIterator<T1 extends Annotation> implements CloseableIterator<Window<T1>>{

		IntervalTree<Window<T1>> windows;
		CloseableIterator<T1> iter;
		Iterator<Window<T1>> fullyFormedWindows;
		int windowLength;
		boolean hasNext;
		boolean assumeForward;
		Annotation region;

		public WindowIterator(CloseableIterator<T1> iter, int windowLength, boolean assumeForward){
			this.iter=iter;
			this.windowLength=windowLength;
			this.windows=new IntervalTree<Window<T1>>();
			this.hasNext=false;
			this.assumeForward=assumeForward;
		}
		
		public WindowIterator(CloseableIterator<T1> iter, int windowLength)
		{
			this(iter,windowLength,true);
			this.region = null;
		}

		public WindowIterator(CloseableIterator<T1> iter, int windowLength,Annotation region) {
			this(iter,windowLength,true);
			this.region = region;
		}

		@Override
		public boolean hasNext() {
			if(fullyFormedWindows!=null && fullyFormedWindows.hasNext()){return true;}
			else if(iter.hasNext()){updateWindows(); return hasNext();}
			else if(!iter.hasNext() && !windows.isEmpty())
			{
				updateRemainingWindows();
				return hasNext();
			}
			
			return false;
		}

		private void updateRemainingWindows(){
			fullyFormedWindows = windows.valueIterator();
			windows = new IntervalTree<Window<T1>>();
		}
		@Override
		public Window<T1> next() {
			return fullyFormedWindows.next();
		}

		private void updateWindows(){
			T1 read=iter.next();
			//all windows with an end position before the start of this window
			fullyFormedWindows=removeFullyFormedWindows(read).iterator();
			addReadToWindows(read);
		}

		private void addReadToWindows(T1 read){
			//Create all windows which overlap read blocks
			Iterator<SingleInterval> interval=read.getBlocks();
			while(interval.hasNext()){
				SingleInterval block=interval.next();
				int start=Math.max(0, block.getReferenceStartPosition()-windowLength);
				int end = block.getReferenceEndPosition();
				if(region!=null)
					end = Math.min(region.getReferenceEndPosition(), block.getReferenceEndPosition());
				for(int i=start; i<end; i++){
					Window<T1> window=windows.remove(i, i+windowLength);
					if(window==null){
						//make a window
						window=new ContiguousWindow<T1>(read.getReferenceName(), i, i+windowLength, Strand.BOTH);
					}
					window.addAnnotation(read);
					windows.put(window.getReferenceStartPosition(), window.getReferenceEndPosition(), window);
				}
			}
		}

		private Collection<Window<T1>> removeFullyFormedWindows(T1 read) {
			Iterator<Window<T1>> iter;
			if(assumeForward)
				{iter=windows.getNodesBeforeInterval(read.getReferenceStartPosition(), read.getReferenceStartPosition());}
			else
				{iter=windows.getNodesAfterInterval(read.getReferenceEndPosition(), read.getReferenceEndPosition());}
			Collection<Window<T1>> rtrn=new ArrayList<Window<T1>>();
			while(iter.hasNext()){
				Window<T1> w=iter.next();
				if((!w.getReferenceName().equalsIgnoreCase(read.getReferenceName())) || (w.getReferenceEndPosition()<read.getReferenceStartPosition())){
					rtrn.add(w);
					windows.remove(w.getReferenceStartPosition(), w.getReferenceEndPosition());
				}
			}
			this.hasNext=!rtrn.isEmpty();
			return rtrn;
		}

		@Override
		public void remove() {
			this.iter.remove();
		}

		@Override
		public void close() {
			this.iter.close();
		}


	}

	public CoordinateSpace getFeatureCoordinateSpace(){
		//Iterate through all records
		CloseableIterator<T> iter=sortedIterator();
		Map<String, Integer> sizes=new TreeMap<String, Integer>();
		while(iter.hasNext()){
			T annotation=iter.next();
			int size=annotation.size();
			String name=annotation.getName();
			sizes.put(name, size);
		}
		iter.close();
		return new CoordinateSpace(sizes);
	}
	
	public int getNumAnnotations(){
		if(this.numAnnotations != 0)
			return this.numAnnotations;

		CloseableIterator<T> iter = this.sortedIterator();
		int count = 0;
		while(iter.hasNext())
		{
			iter.next();
			count++;
		}
		iter.close();
		this.numAnnotations = count;
		return this.numAnnotations;
	}
	
	
}