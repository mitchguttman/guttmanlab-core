package annotation.predicate;

import annotation.SAMFragment;

import org.apache.commons.collections15.Predicate;

/**
 * A class that evaluates whether the read has an Indel or other not spliced or contiguous mapping
 * @author mguttman
 *
 * @param <T> which extends a SAMFragment
 */
public class IndelFilter<T extends SAMFragment> implements Predicate<T>{

	@Override
	public boolean evaluate(T read) {
		String cigar=read.getSamRecord().getCigarString();
		if(cigar.contains("D") || cigar.contains("I")){return false;}
		return true;
	}

}
