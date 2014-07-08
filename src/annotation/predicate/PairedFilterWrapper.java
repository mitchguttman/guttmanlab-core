package annotation.predicate;

import org.apache.commons.collections15.Predicate;

import annotation.MappedFragment;
import annotation.PairedMappedFragment;

public class PairedFilterWrapper<T extends MappedFragment> implements Predicate<PairedMappedFragment<T>>{

	Predicate<T> test;
	
	public PairedFilterWrapper(Predicate<T> test){
		this.test=test;
	}

	@Override
	public boolean evaluate(PairedMappedFragment<T> pair) {
		return (test.evaluate(pair.getRead1()) && test.evaluate(pair.getRead2()));
	}

}
