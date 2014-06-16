package peakcaller.action;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import peakcaller.PeakCaller;
import pipeline.ConfigFileOptionValue;

import nextgen.core.annotation.Annotation;

/**
 * Simple class to write current set of candidate regions to a bed file
 * @author prussell
 *
 */
public class WriteCandidateRegions implements PeakCallerAction<PeakCaller> {

	private String outBedFile;
	private static Logger logger = Logger.getLogger(WriteCandidateRegions.class.getName());
	
	/**
	 * Just for getting name
	 * Do not use
	 */
	public WriteCandidateRegions() {}
	
	/**
	 * @param outBed Bed file to write to
	 */
	public WriteCandidateRegions(String outBed) {
		outBedFile = outBed;
	}
	
	/**
	 * @param value A config file value specifying this action and the output bed file
	 */
	public WriteCandidateRegions(ConfigFileOptionValue value) {
		validateConfigFileLine(value);
		outBedFile = value.asString(1);
	}
	
	@Override
	public void doWork(PeakCaller peakCaller) {
		logger.info("");
		logger.info("Writing candidate regions to file " + outBedFile);
		peakCaller.checkReady();
		try {
			FileWriter w = new FileWriter(outBedFile);
			for(String chr : peakCaller.getCandidateRegions().keySet()) {
				for(Annotation region : peakCaller.getCandidateRegions().get(chr)) {
					w.write(region.toBED() + "\n");
				}
			}
			w.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public String getName() {
		return "write_candidate_regions";
	}

	@Override
	public void validateConfigFileLine(ConfigFileOptionValue value) {
		// Check that flag is the name of this action
		if(!value.getFlag().equals(getName())) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Flag (" + value.getFlag() + ") does not match name " + getName() + ")");
		}
		// Check for correct number of arguments
		if(value.getActualNumValues() != 2) {
			logger.error("Correct format:");
			logger.error(getConfigFileFieldDescription());
			throw new IllegalStateException("Number of fields including flag should be 2: " + value.getFullOptionLine());
		}
	}

	@Override
	public String getConfigFileFieldDescription() {
		return getName() + "\t<output_bed_file>";
	}

}
