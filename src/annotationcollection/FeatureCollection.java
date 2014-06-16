package annotationcollection;

import java.util.Collection;
import java.util.Iterator;

import coordinatespace.CoordinateSpace;
import annotation.Annotation;

public class FeatureCollection implements AnnotationCollection{

	/**
	 * The reference coordinate system that features are mapped to
	 */
	private CoordinateSpace referenceCoordinateSpace;
	
	/**
	 * The feature coordinate system that features are mapped from
	 */
	private CoordinateSpace featureCoordinateSpace;
	
	@Override
	public AnnotationCollection merge() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean overlaps(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean overlaps(AnnotationCollection other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int numOverlappers(Annotation region, boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Collection<? extends Annotation> getOverlappers(Annotation region,
			boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Iterator<? extends Annotation> iterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Iterator<? extends Annotation> iterator(Annotation region,
			boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <T extends Annotation> void addFilter(Predicate<T> filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Annotation convert(Annotation feature) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

}
