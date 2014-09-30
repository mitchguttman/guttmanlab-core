package guttmanlab.core.annotation.predicate;

import guttmanlab.core.annotation.MappedFragment;
import guttmanlab.core.annotation.PairedMappedFragment;

import org.apache.commons.collections15.Predicate;

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
