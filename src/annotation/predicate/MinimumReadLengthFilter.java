package annotation.predicate;

import org.apache.commons.collections15.Predicate;
import annotation.Annotation;

public class MinimumReadLengthFilter<T extends Annotation> implements Predicate<T>{

	int minSize;
	
	public MinimumReadLengthFilter(int minSize){
		this.minSize=minSize;
	}
	
	@Override
	public boolean evaluate(T read) {
		if(read.size()>minSize){return true;}
		return false;
	}

}
