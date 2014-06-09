package annotation;

import coordinatespace.CoordinateSpace;

public class BlockedAnnotation implements Annotation {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public CoordinateSpace getCoordinateSpace() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean overlaps(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean contains(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Annotation merge(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Annotation minus(Annotation other) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * A helper class where we can implement methods for single intervals to use in the general blocked implementations
	 * @author prussell
	 *
	 */
	private class SingleInterval {
		
	}
	
}
