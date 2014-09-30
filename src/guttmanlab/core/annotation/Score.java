package guttmanlab.core.annotation;

public interface Score {

	/**
	 * @return Returns a numeric score
	 */
	public double getScore();
	
	/**
	 * @return Returns a boolean for whether this score is significant
	 */
	public boolean isSignificant();
}
