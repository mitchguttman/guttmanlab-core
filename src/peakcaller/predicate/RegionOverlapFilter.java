package peakcaller.predicate;

import org.apache.commons.collections15.Predicate;

import annotation.Annotation;

public class RegionOverlapFilter<T extends Annotation> implements Predicate<T>{

	private Annotation regionToOverlap;
	
	public RegionOverlapFilter(Annotation regionToOverlap){
		this.regionToOverlap=regionToOverlap;
	}
	
	@Override
	public boolean evaluate(T read) {
		//check if the read overlaps the region
		return read.overlaps(regionToOverlap);
	}

}
