package annotation.predicate;

import org.apache.commons.collections15.Predicate;

import annotation.MappedFragment;


public class SAMFragmentNumHitsFilter implements Predicate<MappedFragment> {

	private int maxNumHits;
	
	public SAMFragmentNumHitsFilter(int maxHits) {
		maxNumHits = maxHits;
	}
	
	@Override
	public boolean evaluate(MappedFragment fragment) {
		return fragment.getNumHits() <= maxNumHits;
	}

}
