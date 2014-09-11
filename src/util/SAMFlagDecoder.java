package util;

/**
 * Utility class for decoding an integer SAM flag and getting the encoded information
 * @author prussell
 *
 */
public class SAMFlagDecoder {
	
	private String binaryString;
	private int stringLength;
	
	private int posMultipleSegments = 0;
	private int posEachSegmentProperlyAligned = 1;
	private int posSegmentUnmapped = 2;
	private int posNextSegmentInTemplateUnmapped = 3;
	private int posSeqReverseComplemented = 4;
	private int posSeqOfNextSegmentInTemplateReversed = 5;
	private int posFirstSegmentInTemplate = 6;
	private int posLastSegmentInTemplate = 7;
	private int posSecondaryAlignment = 8;
	private int posNotPassingQualityControls = 9;
	private int posPcrOrOpticalDuplicate = 10;
	private int posSupplementaryAlignment = 11;
	
	public SAMFlagDecoder(int flag) {
		if(flag < 0) {
			throw new IllegalArgumentException("Flag must be >= 0");
		}
		binaryString = Integer.toBinaryString(flag);
		stringLength = binaryString.length();
	}
	
	public boolean templateHasMultipleSegmentsInSequencing() {
		return bitIsTrue(posMultipleSegments);
	}
	
	public boolean eachSegmentProperlyAligned() {
		return bitIsTrue(posEachSegmentProperlyAligned);
	}
	
	public boolean segmentUnmapped() {
		return bitIsTrue(posSegmentUnmapped);
	}
	
	public boolean nextSegmentInTemplateUnmapped() {
		return bitIsTrue(posNextSegmentInTemplateUnmapped);
	}
	
	public boolean seqIsReverseComplemented() {
		return bitIsTrue(posSeqReverseComplemented);
	}
	
	public boolean seqOfNextSegmentInTemplateIsReverseComplemented() {
		return bitIsTrue(posSeqOfNextSegmentInTemplateReversed);
	}
	
	public boolean firstSegmentInTemplate() {
		return bitIsTrue(posFirstSegmentInTemplate);
	}
	
	public boolean lastSegmentInTemplate() {
		return bitIsTrue(posLastSegmentInTemplate);
	}
	
	public boolean secondaryAlignment() {
		return bitIsTrue(posSecondaryAlignment);
	}
	
	public boolean pcrOrOpticalDuplicate() {
		return bitIsTrue(posPcrOrOpticalDuplicate);
	}
	
	public boolean notPassingQualityControls() {
		return bitIsTrue(posNotPassingQualityControls);
	}
	
	public boolean supplementaryAlignment() {
		return bitIsTrue(posSupplementaryAlignment);
	}
	
	private boolean bitIsTrue(int pos) {
		if(stringLength <= pos) {
			return false;
		}
		char c = binaryString.charAt(stringLength - 1 - pos);
		return c == '1';
	}
	
}