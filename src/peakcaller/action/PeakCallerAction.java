package peakcaller.action;

import peakcaller.PeakCaller;
import pipeline.ConfigFileOptionValue;

/**
 * Any action that manipulates a PeakCaller object in any way
 * @author prussell
 *
 */
public interface PeakCallerAction<T extends PeakCaller> {
	
	/**
	 * Do the action for the peak caller
	 * @param peakCaller Peak caller object
	 */
	public void doWork(T peakCaller);
	
	/**
	 * Get the name of the action
	 * @return The name of the action
	 */
	public String getName();
	
	/**
	 * Make sure a config file value correctly matches this class
	 * Throw an exception if doesn't match
	 * @param value Config file option value
	 */
	public void validateConfigFileLine(ConfigFileOptionValue value);
	
	/**
	 * Get a string description of the fields needed to declare this action in a config file
	 * @return Config file help line
	 */
	public String getConfigFileFieldDescription();
	
}
