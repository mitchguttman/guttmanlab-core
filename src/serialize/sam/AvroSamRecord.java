package serialize.sam;

import java.util.Collection;
import java.util.Iterator;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import util.SAMFlagDecoder;

import annotation.Annotation;
import annotation.BlockedAnnotation;
import annotation.DerivedAnnotation;
import annotation.MappedFragment;
import annotation.SAMFragment;
import annotation.SingleInterval;
import annotation.predicate.ReadFlag;
import annotationcollection.AnnotationCollection;

public class AvroSamRecord extends BlockedAnnotation implements GenericRecord, MappedFragment {
	
	private GenericRecord record;
	private Annotation annotation;
	private boolean firstReadTranscriptionStrand;
	
	public AvroSamRecord(GenericRecord genericRecord) {
		this(genericRecord, true);
	}
	
	public AvroSamRecord(GenericRecord genericRecord, boolean firstReadIsTranscriptionStrand) {
		
		// Get basic attributes
		firstReadTranscriptionStrand = firstReadIsTranscriptionStrand;
		record = genericRecord;
		String cigar = getStringAttribute("cigar");
		String chr = getReferenceName();
		int start = getReferenceStartPosition();
		String name = getName();
		
		// Determine strand
		int flag = getIntAttribute("flag");
		SAMFlagDecoder decoder = new SAMFlagDecoder(flag);
		boolean isPaired = decoder.templateHasMultipleSegmentsInSequencing();
		boolean isFirst = decoder.firstSegmentInTemplate();
		boolean plusStrand = !decoder.seqIsReverseComplemented();
		Strand strand = null;
		if(isPaired) {
			if((firstReadTranscriptionStrand && isFirst) || (!firstReadTranscriptionStrand && !isFirst)) {
				strand = plusStrand ? Strand.POSITIVE : Strand.NEGATIVE;
			}
			if((firstReadTranscriptionStrand && !isFirst) || (!firstReadTranscriptionStrand && isFirst)) {
				strand = plusStrand ? Strand.NEGATIVE : Strand.POSITIVE;
			}
		} else {
			if(firstReadTranscriptionStrand && plusStrand || !firstReadTranscriptionStrand && !plusStrand) {
				strand = Strand.POSITIVE;
			}
			if(firstReadTranscriptionStrand && !plusStrand || !firstReadTranscriptionStrand && plusStrand) {
				strand = Strand.NEGATIVE;
			}
		}
		
		// Construct annotation
		annotation = SAMFragment.parseCigar(cigar, chr, start, strand, name);
	}
	
	public Object getAttribute(String attributeName) {
		return record.get(attributeName);
	}
	
	public String getStringAttribute(String attributeName) {
		return record.get(attributeName).toString();
	}
	
	public int getIntAttribute(String attributeName) {
		return (int) record.get(attributeName);
	}

	@Override
	public Object get(int attributeName) {
		return record.get(attributeName);
	}

	@Override
	public void put(int attributeName, Object value) {
		throw new UnsupportedOperationException("AvroSamRecord objects are immutable");
	}

	@Override
	public Schema getSchema() {
		return record.getSchema();
	}

	@Override
	public Object get(String attributeName) {
		return record.get(attributeName);
	}

	@Override
	public void put(String attributeName, Object value) {
		throw new UnsupportedOperationException("AvroSamRecord objects are immutable");
	}

	@Override
	public String getName() {
		return getStringAttribute("qname");
	}

	@Override
	public String getReferenceName() {
		return getStringAttribute("rname");
	}

	@Override
	public int getReferenceStartPosition() {
		return getIntAttribute("pos");
	}

	@Override
	public int getReferenceEndPosition() {
		return annotation.getReferenceEndPosition();
	}

	@Override
	public Iterator<SingleInterval> getBlocks() {
		return annotation.getBlocks();
	}

	@Override
	public int getNumberOfBlocks() {
		return annotation.getNumberOfBlocks();
	}

	@Override
	public int size() {
		return annotation.size();
	}

	@Override
	public Strand getOrientation() {
		return annotation.getOrientation();
	}

	@Override
	public int getRelativePositionFrom5PrimeOfFeature(int referenceStart) {
		return annotation.getRelativePositionFrom5PrimeOfFeature(referenceStart);
	}

	@Override
	public AnnotationCollection<DerivedAnnotation<? extends Annotation>> getWindows(int windowSize, int stepSize) {
		return annotation.getWindows(windowSize, stepSize);
	}

	@Override
	public void setOrientation(Strand orientation) {
		throw new UnsupportedOperationException("AvroSamRecord objects are immutable");
	}

	@Override
	public Collection<? extends ReadFlag> getReadFlags() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumHits() {
		return getIntAttribute("tagNH");
	}
	
}
