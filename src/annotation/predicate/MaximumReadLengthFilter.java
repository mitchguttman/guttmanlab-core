package annotation.predicate;

import org.apache.commons.collections15.Predicate;
import annotation.Annotation;

public class MaximumReadLengthFilter<T extends Annotation> implements Predicate<T>{

	int maxSize;
	
	public MaximumReadLengthFilter(int maxSize){
		this.maxSize=maxSize;
	}
	
	@Override
	public boolean evaluate(T read) {
		if(read.getReferenceEndPosition()-read.getReferenceStartPosition() < maxSize){return true;}
		return false;
	}

}
