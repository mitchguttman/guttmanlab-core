package annotation.predicate;

import org.apache.commons.collections15.Predicate;

import annotation.PairedMappedFragment;
import annotation.SAMFragment;

public class InsertSizeFilter<T extends PairedMappedFragment<SAMFragment>> implements Predicate<T>{

	private int maxSize;
	
	public InsertSizeFilter(int maxSize){
		this.maxSize=maxSize;
	}
	
	@Override
	public boolean evaluate(T fragment) {
		if(fragment.fragmentLength()<maxSize){return true;}
		return false;
	}

}
