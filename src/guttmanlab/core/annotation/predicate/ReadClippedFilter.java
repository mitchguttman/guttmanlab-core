package guttmanlab.core.annotation.predicate;

import guttmanlab.core.annotation.SAMFragment;

import org.apache.commons.collections15.Predicate;

/**
 * A class that evaluates whether the read is clipped or padded
 * @author mguttman
 *
 * @param <T> which extends a SAMFragment
 */
public class ReadClippedFilter<T extends SAMFragment> implements Predicate<T>{

	@Override
	public boolean evaluate(T read) {
		String cigar=read.getSamRecord().getCigarString();
		if(cigar.contains("S") || cigar.contains("H") || cigar.contains("P")){return false;}
		return true;
	}

}
