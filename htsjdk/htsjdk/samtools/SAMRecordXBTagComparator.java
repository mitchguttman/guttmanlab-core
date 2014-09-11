package htsjdk.samtools;

/**
 * Comparator for values of the XB tag
 * @author prussell
 *
 */
public class SAMRecordXBTagComparator extends
		AbstractSAMRecordStringTagComparator {
	
	private static final String tag = "XB";
	
	@Override
	public String getTag() {
		return tag;
	}

}
