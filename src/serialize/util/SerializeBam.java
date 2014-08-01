package serialize.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.log4j.Logger;


import broad.core.parser.CommandLineParser;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import net.sf.samtools.SAMRecordIterator;

/**
 * Convert a bam file to an avro file
 * @author prussell
 *
 */
public class SerializeBam {
	
	private static Logger logger = Logger.getLogger(SerializeBam.class.getName());
	
	/**
	 * Write the avro file
	 * @param schemaFile Avro schema file with .avsc extension
	 * @param inputBam Bam file to serialize
	 * @param outputAvro Avro file to write
	 * @throws IOException
	 */
	public static void serialize(String schemaFile, String inputBam, String outputAvro) throws IOException {
		
		// Reader for bam file
		SAMFileReader samReader = new SAMFileReader(new File(inputBam));
		SAMRecordIterator samIter = samReader.iterator();
		// Create the schema
		Schema schema = new Schema.Parser().parse(new File(schemaFile));
		// Create a record to hold sam record
		GenericRecord avroRec = new GenericData.Record(schema);
		// This file will have Avro output data
		File AvroFile = new File(outputAvro);
		// Create a writer to serialize the record
		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);		         
		DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
		dataFileWriter.setCodec(CodecFactory.snappyCodec());
		dataFileWriter.create(schema, AvroFile);
		
		// Iterate over bam file and write to Avro output file
		while(samIter.hasNext()) {
			SAMRecord samRecord = samIter.next();
			avroRec.put("qname", samRecord.getReadName());
			avroRec.put("flag", samRecord.getFlags());
			avroRec.put("rname", samRecord.getReferenceName());
			avroRec.put("pos", samRecord.getAlignmentStart());
			avroRec.put("mapq", samRecord.getMappingQuality());
			avroRec.put("cigar", samRecord.getCigarString());
			avroRec.put("rnext", samRecord.getMateReferenceName());
			avroRec.put("pnext", samRecord.getMateAlignmentStart());
			avroRec.put("tlen", samRecord.getInferredInsertSize());
			avroRec.put("seq", samRecord.getReadString());
			avroRec.put("qual", samRecord.getBaseQualityString());
			List<SAMTagAndValue> tags = samRecord.getAttributes();
			for(SAMTagAndValue tag : tags) {
				String name = "tag" + tag.tag;
				try {
					avroRec.put(name, tag.value);
				} catch(AvroRuntimeException e) {
					// TODO keep track of these and report
					// This exception is thrown if the schema does not contain the tag name
				}
			}
			dataFileWriter.append(avroRec);
		}  // end of for loop

		samReader.close();
		dataFileWriter.close();
		
	}
	
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime();
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-s", "Avro schema file with .avsc extension", true);
		p.addStringArg("-b", "Input bam file", true);
		p.addStringArg("-a", "Output Avro file with .avro extension", true);
		p.parse(args);
		String schema = p.getStringArg("-s");
		String bam = p.getStringArg("-b");
		String avro = p.getStringArg("-a");
		serialize(schema, bam, avro);
		long time = (System.nanoTime() - start) / 1000000000;
		logger.info("Total time: " + time + " seconds");
	}
	
}
