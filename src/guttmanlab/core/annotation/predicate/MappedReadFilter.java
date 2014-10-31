package guttmanlab.core.annotation.predicate;

import guttmanlab.core.annotation.MappedFragment;
import org.apache.commons.collections15.Predicate;

public class MappedReadFilter<T extends MappedFragment> implements Predicate<T> {
		
	public boolean evaluate(T frag) {
		return !frag.getSamRecord(null).getReadUnmappedFlag();
	}

}
