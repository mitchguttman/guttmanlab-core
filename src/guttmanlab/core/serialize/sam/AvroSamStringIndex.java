package guttmanlab.core.serialize.sam;

import guttmanlab.core.annotationcollection.FeatureCollection;
import guttmanlab.core.serialize.AvroIndex;
import guttmanlab.core.serialize.AvroStringIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.avro.generic.GenericRecord;

public class AvroSamStringIndex implements AvroIndex<String> {

	private AvroStringIndex stringIndex;
	
	public AvroSamStringIndex(String avroFileName, String schemaFile, String indexedFieldName) throws IOException {
		stringIndex = new AvroStringIndex(avroFileName, schemaFile, indexedFieldName);
	}

	@Override
	public void loadAndValidateIndex() throws IOException {
		stringIndex.loadAndValidateIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public AvroSamRecord seek(String key) throws IOException {
		GenericRecord genericRecord = stringIndex.seek(key);
		return new AvroSamRecord(genericRecord);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AvroSamRecord> get(String key) throws IOException {
		List<GenericRecord> genericRecords = stringIndex.get(key);
		List<AvroSamRecord> rtrn = new ArrayList<AvroSamRecord>();
		for(GenericRecord record : genericRecords) {
			rtrn.add(new AvroSamRecord(record));
		}
		return rtrn;
	}

	@Override
	public long getCurrentFilePosition() throws IOException {
		return stringIndex.getCurrentFilePosition();
	}

	/**
	 * Get all records with the key
	 * Optionally exclude records with some attribute contained in a set of values to exclude
	 * @param key The key to search for
	 * @param nameOfAttributeForExclusionSet The name of the attribute to check for exclusion, or null if not using
	 * @param attributeValuesToExclude The attribute values to exclude, or null if not using
	 * @return The set of records with the desired key, minus records with the other attribute contained in the exclusion set
	 * @throws IOException
	 */
	public List<AvroSamRecord> get(String key, String nameOfAttributeForExclusionSet, Collection<String> attributeValuesToExclude) throws IOException {
		List<GenericRecord> genericRecords = stringIndex.get(key, nameOfAttributeForExclusionSet, attributeValuesToExclude);
		List<AvroSamRecord> rtrn = new ArrayList<AvroSamRecord>();
		for(GenericRecord record : genericRecords) {
			rtrn.add(new AvroSamRecord(record));
		}
		return rtrn;
	}

	/**
	 * Get all records with the key as an annotation collection
	 * @param key The key to search for
	 * @return The set of records with the desired key
	 * @throws IOException
	 */
	public FeatureCollection<AvroSamRecord> getAsAnnotationCollection(String key) throws IOException {
		return fromList(get(key));
	}

	/**
	 * Get all records with the key as an annotation collection
	 * Optionally exclude records with some attribute contained in a set of values to exclude
	 * @param key The key to search for
	 * @param nameOfAttributeForExclusionSet The name of the attribute to check for exclusion, or null if not using
	 * @param attributeValuesToExclude The attribute values to exclude, or null if not using
	 * @return The set of records with the desired key, minus records with the other attribute contained in the exclusion set
	 * @throws IOException
	 */
	public FeatureCollection<AvroSamRecord> getAsAnnotationCollection(String key, String nameOfAttributeForExclusionSet, Collection<String> attributeValuesToExclude) throws IOException {
		return fromList(get(key, nameOfAttributeForExclusionSet, attributeValuesToExclude));
	}
	
	private static FeatureCollection<AvroSamRecord> fromList(List<AvroSamRecord> list) {
		FeatureCollection<AvroSamRecord> rtrn = new FeatureCollection<AvroSamRecord>(null);
		for(AvroSamRecord record : list) {
			rtrn.add(record);
		}
		return rtrn;
	}
	
}
