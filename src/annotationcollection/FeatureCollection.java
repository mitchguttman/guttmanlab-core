package annotationcollection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.util.CloseableIterator;
import coordinatespace.CoordinateSpace;
import datastructures.IntervalTree;
import annotation.Annotation;
import annotation.BlockedAnnotation;

public class FeatureCollection<T extends BlockedAnnotation> extends AbstractAnnotationCollection<T> {

	/**
	 * The reference coordinate system that features are mapped to
	 */
	private CoordinateSpace referenceCoordinateSpace;
	private Map<String, IntervalTree<T>> annotationTree;
	private int featureCount;
	
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

	public void writeToFile(String fileName) {
		CloseableIterator<T> iter=sortedIterator();
		try{writeToFile(fileName, iter);}catch(IOException ex){ex.printStackTrace();}
	}
	
	private void writeToFile(String fileName, CloseableIterator<T> iter) throws IOException{
		FileWriter writer=new FileWriter(fileName);
		while(iter.hasNext()){
			T next=iter.next();
			writer.write(next.toString()+"\n");
		}
		writer.close();
		iter.close();
	}

	@Override
	public CloseableIterator<T> sortedIterator() {
		return new FilteredIterator<T>(new WrappedIterator(this.annotationTree), getFilters());
	}

	@Override
	public CloseableIterator<T> sortedIterator(Annotation region, boolean fullyContained) {
		IntervalTree<T> tree=this.annotationTree.get(region.getReferenceName());
		Iterator<T> iter=tree.overlappingValueIterator(region.getReferenceStartPosition(), region.getReferenceEndPosition());
		return new FilteredIterator<T>(iter, getFilters());
	}

	/**
	 * @return The coordinateSpace of the reference for this annotation collection
	 */
	public CoordinateSpace getReferenceCoordinateSpace(){return this.referenceCoordinateSpace;}
	
	
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

	public void writeToFile(String fileName, Annotation region) {
		try{writeToFile(fileName, sortedIterator(region, false));
		}catch(IOException ex){ex.printStackTrace();}
	}
	
}
