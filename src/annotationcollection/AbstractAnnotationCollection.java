package annotationcollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.Predicate;

import net.sf.samtools.util.CloseableIterator;
import annotation.Annotation;
import annotation.BlockedAnnotation;
import annotation.SingleInterval;
import coordinatespace.CoordinateSpace;

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
	public boolean overlaps(AnnotationCollection<? extends Annotation> other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public CloseableIterator<BlockedAnnotation> convertCoordinates(CloseableIterator<? extends Annotation> annotations, CoordinateSpace referenceSpaceForAnnotations){
		return convertFromReferenceSpace(annotations);
		
		//If the reference equals the reference of the FeatureSpace it will return feature space
		/*if(reference.equals(mapping.getReferenceCoordinateSpace())){
			return convertFromReferenceSpace(annotation);
		}
		
		//If the reference equals the feature in the FeatureSpace it will return reference space
		if(reference.equals(mapping.getFeatureCoordinateSpace())){
			//return convertFromFeatureSpace(annotation);
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO");
		}
		
		throw new IllegalArgumentException("The coordinate spaces are not equal to either Reference or Feature");*/
	}
	
	private CloseableIterator<BlockedAnnotation> convertFromReferenceSpace(CloseableIterator<? extends Annotation> iterator){
		return new CoordinateConverterIterator(iterator, this); //TODO This may be dangerous depending on what gets passed
	}
	
	public class CoordinateConverterIterator implements CloseableIterator<BlockedAnnotation>{
		
		CloseableIterator<? extends Annotation> iter;
		Iterator<BlockedAnnotation> next;
		boolean started;
		AnnotationCollection<? extends Annotation> mapping;
		
		
		public CoordinateConverterIterator(CloseableIterator<? extends Annotation> iterator, AnnotationCollection<? extends Annotation> mapping){
			this.iter=iterator;
			this.started=false;
			this.mapping=mapping;
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
			Annotation annotation=iter.next();
			this.next=convert(annotation).iterator();
		}

		private Collection<BlockedAnnotation> convert(Annotation annotation) {
			Collection<BlockedAnnotation> rtrn=new ArrayList<BlockedAnnotation>();
			
			//Find features overlapping the annotation
			CloseableIterator<? extends Annotation> iter=mapping.iterator(annotation, false);
			
			//Adjust the coordinates of the feature as needed in featureSpace (ie as distance from start and end)
			while(iter.hasNext()){
				Annotation feature=iter.next();
				BlockedAnnotation intersect=feature.intersect(annotation); 
				if(intersect.size()>0){
					BlockedAnnotation interval=feature.convertToFeatureSpace(new SingleInterval(intersect.getReferenceName(), intersect.getReferenceStartPosition(), intersect.getReferenceEndPosition()));
					rtrn.add(interval);
				}
			}
			return rtrn;
		}

		@Override
		public BlockedAnnotation next() {
			return next.next();
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			this.iter.close();
			
		}}
	
}
