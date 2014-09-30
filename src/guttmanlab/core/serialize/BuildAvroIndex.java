package guttmanlab.core.serialize;

import guttmanlab.core.util.CommandLineParser;

import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * Build an index for an avro file
 * @author prussell
 *
 */
public class BuildAvroIndex {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(BuildAvroIndex.class.getName());
	
	public static void main(String[] args) throws IOException {
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-s", "Avro schema file with .avsc extension", true);
		p.addStringArg("-a", "Input Avro file with .avro extension", true);
		p.addStringArg("-f", "Name of field to index (must match schema)", true);
		p.parse(args);
		String schema = p.getStringArg("-s");
		String avro = p.getStringArg("-a");
		String field = p.getStringArg("-f");
		AbstractAvroIndex.writeIndex(field, schema, avro);
	}

}
