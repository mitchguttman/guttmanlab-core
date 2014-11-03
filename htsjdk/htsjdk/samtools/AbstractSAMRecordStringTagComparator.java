package htsjdk.samtools;

/**
 * Comparator for SAM tag with string values
 * @author prussell
 *
 */
public abstract class AbstractSAMRecordStringTagComparator extends
		AbstractSAMRecordTagComparator {

	public abstract String getTag();
	
	@Override
	public int fileOrderCompare(SAMRecord samRecord1, SAMRecord samRecord2) {
		// Return 0 if one of the records does not have the tag
		String tag1 = samRecord1.getStringAttribute(getTag()); // This method returns null if tag is missing; throws exception if value is not a string
		if(tag1 == null) return 0;
		String tag2 = samRecord2.getStringAttribute(getTag());
		if(tag2 == null) return 0;
		return tag1.compareTo(tag2);
	}
	
}
