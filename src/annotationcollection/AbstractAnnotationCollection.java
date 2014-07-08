package annotationcollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.Predicate;

import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.util.CloseableIterator;
import annotation.Annotation;
import annotation.BlockedAnnotation;
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

	public AbstractAnnotationCollection(){
		filters=new ArrayList<Predicate<T>>();
	}

	@Override
	public void addFilter(Predicate<T> filter) {
		filters.add(filter);
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
	public <X extends Annotation> CloseableIterator<DerivedAnnotation<X>> convertCoordinates(CloseableIterator<X> annotations, CoordinateSpace referenceSpaceForAnnotations, boolean fullyContained){
		//TODO Should check the coordinate space and convert appropriately
		return convertFromReferenceSpace(annotations, fullyContained);
	}

	//TODO We should consider what to do with partial overlaps here
	private <X extends Annotation> CloseableIterator<DerivedAnnotation<X>> convertFromReferenceSpace(CloseableIterator<? extends Annotation> iterator, boolean fullyContained){
		return new CoordinateConverterIterator(iterator, this, fullyContained);
	}

	public <X extends Annotation> Collection<DerivedAnnotation<X>> convertCoordinates(X annotation, boolean fullyContained){
		//Check if annotation is in Reference or Feature space
		
		//If in Reference space
		return convertFromReference(annotation, fullyContained);
		
		//If in Feature space
		return convertFromFeature(annotation, fullyContained);
		
	}
	
	private <X extends Annotation> Collection<DerivedAnnotation<X>> convertFromFeature(X featureAnnotation, boolean fullyContained){
		Collection<DerivedAnnotation<X>> rtrn=new ArrayList<DerivedAnnotation<X>>();
		CloseableIterator<T> iter=sortedIterator();
		while(iter.hasNext()){
			T referenceAnnotation=iter.next();
			if(referenceAnnotation.getName().equals(featureAnnotation.getName())){
				//if the names are the same then it is the same feature
				//trim to relative start and end
				Annotation a=referenceAnnotation.convertToReferenceSpace(featureAnnotation);
				DerivedAnnotation<X> dA=new DerivedAnnotation<X>(a, featureAnnotation);
				rtrn.add(dA);
			}
		}
		return rtrn;
	}
	
	private <X extends Annotation> Collection<DerivedAnnotation<X>> convertFromReference(X annotation, boolean fullyContained){
		Collection<DerivedAnnotation<X>> rtrn=new ArrayList<DerivedAnnotation<X>>();

		//Find features overlapping the annotation
		CloseableIterator<? extends Annotation> iter=sortedIterator(annotation, fullyContained);

		//Adjust the coordinates of the feature as needed in featureSpace (ie as distance from start and end)
		while(iter.hasNext()){
			Annotation feature=iter.next();
			Annotation intersect=feature.intersect(annotation); 
			if(intersect.size()>0){
				Annotation interval=feature.convertToFeatureSpace(intersect);
				DerivedAnnotation<X> dInterval=new DerivedAnnotation<X>(interval, annotation);
				rtrn.add(dInterval);
			}
		}
		return rtrn;
	}
	
	@Override
	public CloseableIterator<? extends Window<T>> getWindows(Annotation region, int windowLength){
		CloseableIterator<T> iter=sortedIterator(region, false);
		return new WindowIterator<T>(iter, windowLength);
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
		SAMFileWriter writer=new SAMFileWriterFactory().setCreateIndex(true).makeSAMOrBAMWriter(getReferenceCoordinateSpace().getBAMFileHeader(), false, new File(fileName));
			
		CloseableIterator<T> iter=sortedIterator();
		while(iter.hasNext()){
			T ann=iter.next();
			writer.addAlignment(ann.getSAMRecord());
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

		public WindowIterator(CloseableIterator<T1> iter, int windowLength){
			this.iter=iter;
			this.windowLength=windowLength;
			this.windows=new IntervalTree<Window<T1>>();
			this.hasNext=false;
		}

		@Override
		public boolean hasNext() {
			if(fullyFormedWindows!=null && fullyFormedWindows.hasNext()){return true;}
			else if(iter.hasNext()){updateWindows(); return hasNext();}
			return false;
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
			//Make all windows overlapping read blocks
			Iterator<SingleInterval> interval=read.getBlocks();
			while(interval.hasNext()){
				SingleInterval block=interval.next();
				int start=Math.max(0, block.getReferenceStartPosition()-windowLength);
				for(int i=start; i<block.getReferenceEndPosition(); i++){
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
			Iterator<Window<T1>> iter=windows.getNodesBeforeInterval(read.getReferenceStartPosition(), read.getReferenceStartPosition());
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


	public class CoordinateConverterIterator<X extends Annotation> implements CloseableIterator<DerivedAnnotation<X>>{

		CloseableIterator<X> iter;
		Iterator<DerivedAnnotation<X>> next;
		boolean started;
		AnnotationCollection<? extends Annotation> mapping;
		boolean fullyContained;

		public CoordinateConverterIterator(CloseableIterator<X> iterator, AnnotationCollection<? extends Annotation> mapping, boolean fullyContained){
			this.iter=iterator;
			this.started=false;
			this.mapping=mapping;
			this.fullyContained=fullyContained;
		}

		@Override
		public boolean hasNext() {
			if((!started || !next.hasNext()) && iter.hasNext()){
				started=true;
				findNext();
				return hasNext();
			}
			if(next.hasNext()){
				return true;
			}

			return false;
		}

		private void findNext() {
			X annotation=iter.next();
			this.next=convertCoordinates(annotation, fullyContained).iterator();
		}

		@Override
		public DerivedAnnotation<X> next() {
			return next.next();
		}

		@Override
		public void remove() {
			this.iter.remove();
		}

		@Override
		public void close() {
			this.iter.close();

		}}

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
		return new CoordinateSpace(sizes);
	}
	
}