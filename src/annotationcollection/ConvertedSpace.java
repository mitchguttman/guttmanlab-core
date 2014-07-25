package annotationcollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.samtools.util.CloseableIterator;
import coordinatespace.CoordinateSpace;
import annotation.Annotation;
import annotation.BlockedAnnotation;
import annotation.DerivedAnnotation;
import annotation.Window;
import annotation.SingleInterval;
import annotation.Annotation.Strand;

public class ConvertedSpace<T extends Annotation> extends AbstractAnnotationCollection<DerivedAnnotation<T>>{

	private AnnotationCollection<? extends Annotation> featureMapping;
	private AnnotationCollection<T> readMapping;
	private boolean fullyContained;
	
	public ConvertedSpace(AnnotationCollection<T> readMapping, AnnotationCollection<? extends Annotation> featureMapping, CoordinateSpace referenceSpaceForAnnotations, boolean fullyContained){
		this.readMapping=readMapping;
		this.featureMapping=featureMapping;
		this.fullyContained=fullyContained;
	}
	
	@Override
	public CloseableIterator<DerivedAnnotation<T>> sortedIterator() {
		return new CoordinateConverterIterator<T>(readMapping.sortedIterator(), featureMapping, fullyContained);
	}

	@Override
	public CloseableIterator<DerivedAnnotation<T>> sortedIterator(Annotation region, boolean fullyContained) {
		//Step 1: Convert region to old reference space
		Collection<DerivedAnnotation<Annotation>> newCoordinateMapping=convertCoordinates(region, fullyContained);
		CloseableIterator<T> iter=readMapping.sortedIterator(newCoordinateMapping.iterator().next(), fullyContained);//TODO Fix this so it uses all mappings not just one
		return new CoordinateConverterIterator<T>(iter, featureMapping, fullyContained);
	}

	public <X extends Annotation> Collection<DerivedAnnotation<X>> convertCoordinates(X annotation, boolean fullyContained){
		//Check if annotation is in Reference or Feature space
		
		if(featureMapping.getReferenceCoordinateSpace().contains(annotation)){
			//If in Reference space
			return convertFromReference(annotation, fullyContained);
		}
		
		else if(featureMapping.getFeatureCoordinateSpace().contains(annotation)){
			//If in Feature space
			return convertFromFeature(annotation, fullyContained);
		}
		
		else{
			throw new IllegalArgumentException(annotation.getReferenceName()+":"+annotation.getReferenceStartPosition()+"-"+annotation.getReferenceEndPosition()+" annotation is not mapped to either Reference or Feature space");
		}
		
	}
	
	public <X extends Annotation> Collection<DerivedAnnotation<X>> convertFromFeature(X featureAnnotation, boolean fullyContained){
		Collection<DerivedAnnotation<X>> rtrn=new ArrayList<DerivedAnnotation<X>>();
		CloseableIterator<? extends Annotation> iter=featureMapping.sortedIterator();
		while(iter.hasNext()){
			Annotation referenceAnnotation=iter.next();
			if(referenceAnnotation.getName().equals(featureAnnotation.getReferenceName())){
				//if the names are the same then it is the same feature
				//trim to relative start and end	
				Annotation a=referenceAnnotation.convertToReferenceSpace(featureAnnotation);
				DerivedAnnotation<X> dA=new DerivedAnnotation<X>(a, featureAnnotation);
				rtrn.add(dA);	
			}
		}
		iter.close();
		return rtrn;
	}
	
	public <X extends Annotation> Collection<DerivedAnnotation<X>> convertFromReference(X annotation, boolean fullyContained){
		Collection<DerivedAnnotation<X>> rtrn=new ArrayList<DerivedAnnotation<X>>();

		//Find features overlapping the annotation
		CloseableIterator<? extends Annotation> iter=featureMapping.sortedIterator(annotation, fullyContained);
	
			//Adjust the coordinates of the feature as needed in featureSpace (ie as distance from start and end)
		while(iter.hasNext()){
			Annotation feature=iter.next();
			Annotation intersect=feature.intersect(annotation); //TODO Consider whether to remove this, it may not be needed and is expensive per read
			//Is annotation fully contained in feature
			boolean isFullyContained=feature.fullyContained(annotation);
			if(intersect.size()>0){
				if(!fullyContained || isFullyContained){
					//Annotation interval=feature.convertToFeatureSpace(intersect);
					Annotation converted=annotation.convert(feature);
					if(converted!=null){
						DerivedAnnotation<X> dInterval=new DerivedAnnotation<X>(converted, annotation);
						rtrn.add(dInterval);
					}
				}
			}
		}
		iter.close();
		
		return rtrn;
	}
	
	
	@Override
	public CoordinateSpace getReferenceCoordinateSpace() {
		return featureMapping.getFeatureCoordinateSpace(); //TODO Fix this
	}
	
	@Override
	public CoordinateSpace getFeatureCoordinateSpace() {
		return featureMapping.getReferenceCoordinateSpace(); //TODO Fix this
	}

	@Override
	public CloseableIterator<Window<DerivedAnnotation<T>>> getWindows(Annotation region, int windowLength)
	{
		//get read iterator overlapping the feature		
		CloseableIterator<DerivedAnnotation<T>> iter = sortedIterator(region,true); 
		//pass the read iterator to WindowIterator with boolean set by orientation of the feature
		CloseableIterator<Window<DerivedAnnotation<T>>> windows;
		if(region.getOrientation().equals(Strand.NEGATIVE))
			{windows = new WindowIterator<DerivedAnnotation<T>>(iter,windowLength,false);}
		else
			{windows = new WindowIterator<DerivedAnnotation<T>>(iter,windowLength,true);}
		//return the windowIterator
		return windows;
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
			if(!iter.hasNext())
			{
				System.err.println("converted read iterator was empty.");
				return false;
			}
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
			//We should iterate until we either run out of reads or find next
			boolean done=false;
			while(iter.hasNext() && !done){
				X annotation=iter.next();
				this.next=convertCoordinates(annotation, fullyContained).iterator();
				if(next.hasNext()){done=true;}
			}
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
	
}
