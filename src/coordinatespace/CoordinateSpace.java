package coordinatespace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMSequenceRecord;

/**
 * A coordinate space that is a subset of a background genome
 * @author prussell
 *
 */
public class CoordinateSpace {
	
	/**
	 * Description of reference sequences in this coordinate space
	 * Key is reference name, value is reference length
	 */
	private Map<String, Integer> refSizes;
		
	/**
	 * 
	 * @param referenceSizesFile File containing reference names and lengths
	 */
	public CoordinateSpace(String referenceSizesFile){
		this.refSizes=getRefSeqLengthsFromTable(referenceSizesFile);
	}

	public CoordinateSpace(Map<String, Integer> sizes){
		this.refSizes=sizes;
	}
	
	public CoordinateSpace(SAMFileHeader fileHeader) {
		this.refSizes=getRefSeqLengthsFromSamHeader(fileHeader);
	}

	@Override
	public boolean equals(Object o){
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * @return Map associating each reference name with sequence length
	 */
	public Map<String, Integer> getRefSeqLengths() {
		return refSizes;
	}
	
	/**
	 * Get the lengths of the reference sequences from a SAM file header
	 * @param header SAM file header
	 * @return Map associating each reference name with sequence length
	 */
	private Map<String, Integer> getRefSeqLengthsFromSamHeader(SAMFileHeader header) {
		Map<String, Integer> rtrn=new TreeMap<String, Integer>();
		List<SAMSequenceRecord> records = header.getSequenceDictionary().getSequences();
		if (records.size() > 0) {
			for (SAMSequenceRecord rec : header.getSequenceDictionary().getSequences()) {
				String chr = rec.getSequenceName();
				int size=rec.getSequenceLength();
				rtrn.put(chr, size);
		    	}
		    }
		return rtrn;
	}
	
	/**
	 * Parse the reference sizes file
	 * @param referenceSizesFile Tab-delimited file with reference names (ie chromosomes) and lengths
	 * @return Map associating each reference name with sequence length
	 */
	private Map<String, Integer> getRefSeqLengthsFromTable(String referenceSizesFile) {
		Map<String, Integer> rtrn=new TreeMap<String, Integer>();
		
		try{	
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(referenceSizesFile)));
			String nextLine;
			while ((nextLine = reader.readLine()) != null && (nextLine.trim().length() > 0)) {
				String[] tokens=nextLine.split("\t| +");
				rtrn.put(tokens[0], new Integer(tokens[1]));
			}
			reader.close();
		}catch(IOException ex){ex.printStackTrace();}
		return rtrn;
	}

	/**
	 * Convert the CoordinateSpace into a SAMFileHeader
	 * @return The SAMFileHeader
	 */
	public SAMFileHeader getBAMFileHeader() {
		SAMFileHeader header=new SAMFileHeader();

		//add sequences
		for(String refSeq: this.refSizes.keySet()){
			int size=this.refSizes.get(refSeq)+1; //TODO I think this is correct for the header
			SAMSequenceRecord seq=new SAMSequenceRecord(refSeq, size);
			header.addSequence(seq);
		}

		//Set sort order
		header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
				
		return header;
	}
	
	
	
}
