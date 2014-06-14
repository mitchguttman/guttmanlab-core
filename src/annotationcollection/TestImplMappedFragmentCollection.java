package annotationcollection;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.Predicate;

import annotation.Annotation;
import annotation.MappedFragment;
import coordinatespace.CoordinateConverter;

public class TestImplMappedFragmentCollection implements MappedFragmentCollection {

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
	public Collection<MappedFragment> getOverlappers(Annotation region,
			boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Iterator<MappedFragment> iterator(
			Annotation region, boolean fullyContained) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public CoordinateConverter getCoordinateConverter() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Iterator<MappedFragment> iterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void writeConvertedFile(String outputFile) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <T extends Annotation> void addFilter(Predicate<T> filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}


}
