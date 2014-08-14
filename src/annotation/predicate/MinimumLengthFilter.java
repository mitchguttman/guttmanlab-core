package annotation.predicate;

import org.apache.commons.collections15.Predicate;
import annotation.Annotation;

public class MinimumLengthFilter<T extends Annotation> implements Predicate<T>{

	int minSize;
	
	public MinimumLengthFilter(int minSize){
		this.minSize=minSize;
	}
	
	@Override
	public boolean evaluate(T annot) {
		if(annot.size()>minSize){return true;}
		return false;
	}

}
