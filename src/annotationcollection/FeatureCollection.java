package annotationcollection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.collections15.Predicate;

import coordinatespace.CoordinateSpace;
import datastructures.IntervalTree;
import datastructures.IntervalTree.Node;
import annotation.Annotation;
import annotation.BlockedAnnotation;
import annotation.SAMFragment;
import annotation.SingleInterval;

public class FeatureCollection<T extends BlockedAnnotation> extends AbstractAnnotationCollection<T>{

	/**
	 * The reference coordinate system that features are mapped to
	 */
	private CoordinateSpace referenceCoordinateSpace;
	private Map<String, IntervalTree<T>> annotationTree;
	private int featureCount;
	
	/**
	 * The feature coordinate system that features are mapped from
	 * TODO This may not be needed because it is defined by the feature collection
	 */
	private CoordinateSpace featureCoordinateSpace;
	
	public FeatureCollection(CoordinateSpace referenceSpace){
		super();
		this.referenceCoordinateSpace=referenceSpace;
		this.annotationTree=new TreeMap<String, IntervalTree<T>>();
	}
	
	/**
	 * Add annotation to the collection
	 * @param annotation to add
	 */
	public void addAnnotation(T annotation){
		IntervalTree<T> tree=new IntervalTree<T>();
		if(annotationTree.containsKey(annotation.getReferenceName())){
			tree=annotationTree.get(annotation.getReferenceName());
		}
		tree.put(annotation.getReferenceStartPosition(), annotation.getReferenceEndPosition(), annotation);
		annotationTree.put(annotation.getReferenceName(), tree);
		featureCount++;
	}

	/**
	 * Get the number of features in this collection
	 * @return The number of features
	 */
	public int getCount() {
		return this.featureCount;
	}

	@Override
	public int numOverlappers(Annotation region, boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void writeToFile(String fileName) {
		CloseableIterator<T> iter=iterator();
		
		try{
			FileWriter writer=new FileWriter(fileName);
			while(iter.hasNext()){
				T next=iter.next();
				writer.write(next.toString()+"\n");
			}
			writer.close();
		}catch(IOException ex){throw new IllegalArgumentException();}
		
		iter.close();
	}

	@Override
	public CloseableIterator<T> iterator() {
		return new FilteredIterator<T>(new WrappedIterator(this.annotationTree), getFilters());
	}

	@Override
	public CloseableIterator<T> iterator(Annotation region, boolean fullyContained) {
		IntervalTree<T> tree=this.annotationTree.get(region.getReferenceName());
		Iterator<T> iter=tree.overlappingValueIterator(region.getReferenceStartPosition(), region.getReferenceEndPosition());
		return new FilteredIterator<T>(iter, getFilters());
	}

	/**
	 * @return The coordinateSpace of the reference for this annotation collection
	 */
	public CoordinateSpace getReferenceCoordinateSpace(){return this.referenceCoordinateSpace;}
	
	/**
	 * @return The coordinateSpace of the features for this annotation collection
	 */
	public CoordinateSpace getFeatureCoordinateSpace(){return this.featureCoordinateSpace;}
	
	public class WrappedIterator implements CloseableIterator<T>{

		Map<String, IntervalTree<T>> annotationTree;
		Iterator<String> referenceIterator;
		String currentReference;
		Iterator<T> currentTreeIterator;
		
		public WrappedIterator(Map<String, IntervalTree<T>> annotationTree){
			this.annotationTree=annotationTree;
			this.referenceIterator=annotationTree.keySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			if(currentTreeIterator==null || !currentTreeIterator.hasNext()){
				if(referenceIterator.hasNext()){
					String chr=referenceIterator.next();
					currentTreeIterator=annotationTree.get(chr).valueIterator();
					return hasNext();
				}
				else{return false;}
			}
			return true;
		}

		@Override
		public T next() {
			return currentTreeIterator.next();
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
		}
	}
	
}
