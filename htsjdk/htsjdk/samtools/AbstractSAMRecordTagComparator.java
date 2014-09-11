package htsjdk.samtools;

/**
 * Comparator for values of a SAM tag
 * @author prussell
 *
 */
public abstract class AbstractSAMRecordTagComparator implements SAMRecordComparator {
			
	@Override
	public int compare(SAMRecord samRecord1, SAMRecord samRecord2) {
		// Method body copied from SAMRecordQueryNameComparator
        int cmp = fileOrderCompare(samRecord1, samRecord2);
        if (cmp != 0) {
            return cmp;
        }

        final boolean r1Paired = samRecord1.getReadPairedFlag();
        final boolean r2Paired = samRecord2.getReadPairedFlag();

        if (r1Paired || r2Paired) {
            if (!r1Paired) return 1;
            else if (!r2Paired) return -1;
            else if (samRecord1.getFirstOfPairFlag()  && samRecord2.getSecondOfPairFlag()) return -1;
            else if (samRecord1.getSecondOfPairFlag() && samRecord2.getFirstOfPairFlag()) return 1;
        }

        if (samRecord1.getReadNegativeStrandFlag() != samRecord2.getReadNegativeStrandFlag()) {
            return (samRecord1.getReadNegativeStrandFlag()? 1: -1);
        }
        if (samRecord1.getNotPrimaryAlignmentFlag() != samRecord2.getNotPrimaryAlignmentFlag()) {
            return samRecord2.getNotPrimaryAlignmentFlag()? -1: 1;
        }
        if (samRecord1.getSupplementaryAlignmentFlag() != samRecord2.getSupplementaryAlignmentFlag()) {
            return samRecord2.getSupplementaryAlignmentFlag() ? -1 : 1;
        }
        final Integer hitIndex1 = samRecord1.getIntegerAttribute(SAMTag.HI.name());
        final Integer hitIndex2 = samRecord2.getIntegerAttribute(SAMTag.HI.name());
        if (hitIndex1 != null) {
            if (hitIndex2 == null) return 1;
            else {
                cmp = hitIndex1.compareTo(hitIndex2);
                if (cmp != 0) return cmp;
            }
        } else if (hitIndex2 != null) return -1;
        return 0;
	}


}
