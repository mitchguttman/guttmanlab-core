package serialize;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.log4j.Logger;

/**
 * Implementations of methods that can tolerate the generic type
 * @author prussell
 *
 * @param <T>
 */
public abstract class AbstractAvroIndex<T extends Comparable<T>> implements AvroIndex<T> {
	
	private String avroFile; // The avro data file based on the schema
	protected String indexFile; // The index written by this class
	protected TreeMap<T, Long> positionsByKey; // File position of record at beginning of each block
	protected DataFileReader<GenericRecord> reader; // Reader for avro file
	private Schema schema; // Data schema
	protected String indexedField; // The name of the field in the schema that is indexed by this index
	//protected GenericRecord genericRecord; // A generic record to read data into
	private static Logger logger = Logger.getLogger(AbstractAvroIndex.class.getName());
	
	/**
	 * Get index file name corresponding to an avro file name
	 * @param avroFileName Avro file name
	 * @return Index file name
	 */
	public static String getIndexFileName(String avroFileName) {
		return avroFileName + ".index";
	}
	
	/**
	 * Read index information from index file
	 * @param avroFileName Avro file
	 * @param schemaFile Avro schema file
	 * @param indexedFieldName The name of the field in the schema that is indexed in this index
	 */
	public AbstractAvroIndex(String avroFileName, String schemaFile, String indexedFieldName) throws IOException {
		logger.info("Loading avro index...");
		avroFile = avroFileName;
		indexFile = getIndexFileName(avroFile);		
		File f = new File(indexFile);
		if(!f.exists()) {
			throw new IllegalStateException("No index exists for avro file " + avroFile + ".");
		}
		schema = new Schema.Parser().parse(new File(schemaFile));
		indexedField = indexedFieldName;
		//genericRecord = new GenericData.Record(schema);
		DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
		reader = new DataFileReader<GenericRecord>(new File(avroFile), datumReader);
		loadAndValidateIndex();
		logger.info("Done loading and validating index.");
	}

	/**
	 * Get a lower bound on the file position of the first occurrence of a key
	 * @param key
	 * @return The start position of the first block containing the key,
	 * or if the first record containing the key happens to be the first record of a block,
	 * this method will return the beginning of the previous block
	 */
	private long getLowerBoundStartPos(T key) {
		// The file position of the last key before the key of interest
		// Head map is exclusive in case the key extends over multiple blocks
		try {
			long rtrn = positionsByKey.headMap(key, false).lastEntry().getValue().longValue();
			return rtrn;
		} catch(NullPointerException e) {
			if(positionsByKey.firstEntry().getKey().equals(key)) {
				throw new IllegalStateException("Query does not work for the very first barcode in file: " + key + ". TODO: fix.");
			}
			throw e;
		}
	}
	
	/**
	 * Move the data reader to the beginning of the first block containing the key,
	 * or if the first record containing the key happens to be the first record of a block,
	 * this method will move the reader to the beginning of the previous block
	 * @param <D>
	 * @param key The key
	 * @throws IOException 
	 */
	protected void seekToBlockStart(T key) throws IOException {
		reader.seek(getLowerBoundStartPos(key));
	}
	
	/**
	 * @return The current position of the reader
	 * @throws IOException 
	 */
	public long getCurrentFilePosition() throws IOException {
		return reader.tell();
	}
	
	/**
	 * Write a file index with respect to the specified field
	 * @param fieldName Field to index
	 * @param schemaFile Avro schema file
	 * @param inputAvro Avro file to index
	 * @param output Index file to write
	 * @throws IOException
	 */
	public static void writeIndex(String fieldName, String schemaFile, String inputAvro) throws IOException {
		logger.info("");
		logger.info("Writing index for file " + inputAvro + " based on field " + fieldName + "...");
		long start = System.nanoTime();
		// Create a schema
		Schema schema = new Schema.Parser().parse(new File(schemaFile));
		// Create a record using schema
		GenericRecord avroRec = new GenericData.Record(schema);
		DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
		DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File(inputAvro), datumReader);
		// Write index to a file
		FileWriter w = new FileWriter(getIndexFileName(inputAvro));
		// Go to the first sync point
		dataFileReader.previousSync();
		while (true) {
			long pos = dataFileReader.tell();
			try {
				avroRec = dataFileReader.next();
			} catch(NoSuchElementException e) {
				break;
			}
			Object val = avroRec.get(fieldName);
			if(val == null) {
				w.close();
				throw new IllegalStateException("Indexed value cannot be null");
			}
			w.write(val.toString() + "\t" + pos + "\n");
			dataFileReader.sync(pos);
		}
		w.close();
		long time = (System.nanoTime() - start) / 1000000000;
		logger.info("Wrote index in " + time + " seconds.");
	}
	
	
}
