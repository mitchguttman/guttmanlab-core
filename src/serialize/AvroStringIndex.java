package serialize;

import general.StringParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.apache.log4j.Logger;


/**
 * An index for a string field
 * @author prussell
 *
 */
public class AvroStringIndex extends AbstractAvroIndex<String> {

	private static Logger logger = Logger.getLogger(AvroStringIndex.class.getName());
	
	/**
	 * Read index information from index file
	 * @param avroFileName Avro file
	 * @param schemaFile Avro schema file
	 * @param indexedFieldName The name of the field in the schema that is indexed in this index
	 */
	public AvroStringIndex(String avroFileName, String schemaFile, String indexedFieldName) throws IOException {
		super(avroFileName, schemaFile, indexedFieldName);
	}

	@Override
	public void loadAndValidateIndex() throws IOException {
		logger.info("");
		logger.info("Reading index from file " + indexFile + ".");
		positionsByKey = new TreeMap<String, Long>();
		FileReader r = new FileReader(indexFile);
		BufferedReader b = new BufferedReader(r);
		StringParser s = new StringParser();
		long previous = 0;
		long current = 0;
		while(b.ready()) {
			s.parse(b.readLine());
			String key = s.asString(0);
			current = s.asLong(1);
			// Check that the block numbers are increasing
			if(current <= previous) {
				b.close();
				throw new IllegalStateException("Index validation error: wrong order for file blocks " + previous + ", " + current + ".");
			}
			// Check that the first record in this block has the reported key
			GenericData.Record record = null;
			reader.seek(current);
			//System.out.println("\n" + current + "\t" + reader.tell());
			//reader.previousSync();
			//System.out.println(current + "\t" + reader.tell());
			try {
				record = new GenericData.Record((Record) reader.next(), true);
			} catch(AvroRuntimeException e) {
				logger.warn("Skipping record at position " + current);
				continue;
			}
			//System.out.println(current + "\t" + reader.tell());
			String recordKey = record.get(indexedField).toString();
			if(!recordKey.equals(key)) {
				b.close();
				throw new IllegalStateException("At file block " + reader.tell() + ": Index validation error: beginning of block " + current + " in avro file has key " + recordKey + ". Index has " + key + ".");
			}
			if(!positionsByKey.containsKey(key)) {
				positionsByKey.put(key, Long.valueOf(current));
			}
			previous = current;
		}
		b.close();
	}
	

	@Override
	public GenericRecord seek(String key) throws IOException {
		seekToBlockStart(key);
		while(true) {
			try {
				GenericData.Record record = new GenericData.Record((Record) reader.next(), true);
				Object currKey = record.get(indexedField);
				int compare = 0;
				String strCurrKey = currKey.toString();
				compare = strCurrKey.compareTo(key);
				//logger.info(key + "\t" + strCurrKey);
				if(compare == 0) {
					return record;
				}
				if(compare > 0) {
					throw new NoSuchElementException("Key " + key + " not found.");
				}
			} catch(NoSuchElementException e) {
				break;
			}
		}
		throw new NoSuchElementException("Key " + key + " not found.");
	}

	@Override
	public List<GenericRecord> get(String key) throws IOException {
		GenericData.Record first = new GenericData.Record((Record) seek(key), true);
		List<GenericRecord> rtrn = new ArrayList<GenericRecord>();
		rtrn.add(first);
		while(true) {
			try {
				GenericData.Record record = new GenericData.Record((Record) reader.next(), true);
				Object currKey = record.get(indexedField);
				int compare = 0;
				String strCurrKey = currKey.toString();
				compare = strCurrKey.compareTo(key);
				if(compare == 0) {
					rtrn.add(record);
				} else if(compare > 0) {
					return rtrn;
				} else {
					throw new IllegalStateException("Wrong sort order for keys " + key + " " + strCurrKey);
				}
			} catch(NoSuchElementException e) {
				break;
			}
		}
		return rtrn;
	}
	

	
}
