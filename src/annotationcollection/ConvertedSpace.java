package annotationcollection;

import net.sf.samtools.util.CloseableIterator;
import coordinatespace.CoordinateSpace;
import annotation.Annotation;

public class ConvertedSpace<T extends Annotation> extends AbstractAnnotationCollection<T>{

	private AnnotationCollection featureMapping;
	private AnnotationCollection readMapping;
	
	@Override
	public CloseableIterator<T> sortedIterator() {
		return new CoordinateConverterIterator(readMapping.sortedIterator(), mapping);
	}

	@Override
	public CloseableIterator<T> sortedIterator(Annotation region, boolean fullyContained) {
		//Step 1: Convert region to old reference space
		Annotation newCoordinateMapping=mapping.convertCoordinates(region, fullyContained);
		CloseableIterator<T> iter=readMapping.sortedIterator(newCoordinateMapping, fullyContained);
		return new CoordinateConverterIterator(iter, mapping);
	}


	@Override
	public CoordinateSpace getReferenceCoordinateSpace() {
		return featureMapping.getFeatureCoordinateSpace();
	}

	//TODO Consider how to override Windows()
	
}
