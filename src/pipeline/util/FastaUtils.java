package pipeline.util;

import java.io.IOException;



import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;

import pipeline.LSFJob;
import pipeline.OGSJob;
import pipeline.Scheduler;


/**
 * @author prussell
 *
 */
public class FastaUtils {

	private static Logger logger = Logger.getLogger(FastaUtils.class.getName());
	
	/**
	 * Index a fasta file using samtools faidx and write bsub output file to working directory
	 * @param fastaFileName The fasta file to index
	 * @param samtoolsExecutable Path to samtools executable
	 * @param scheduler Scheduler
     * @param drmaaSession Active DRMAA session. Pass null if not using OGS. There should only be one active session at a time. Session should have been created in the main method of the class calling this method.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws DrmaaException 
	 */
	public static void indexFastaFile(String fastaFileName, String samtoolsExecutable, Scheduler scheduler, Session drmaaSession) throws IOException, InterruptedException, DrmaaException {
		indexFastaFile(fastaFileName, samtoolsExecutable, ".", scheduler, drmaaSession);
	}
	
	/**
	 * Index a fasta file using samtools faidx
	 * @param fastaFileName The fasta file to index
	 * @param samtoolsExecutable Path to samtools executable
	 * @param bsubOutDir Directory to write bsub output to
	 * @param scheduler Scheduler
     * @param drmaaSession Active DRMAA session. Pass null if not using OGS. There should only be one active session at a time. Session should have been created in the main method of the class calling this method.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws DrmaaException 
	 */
	public static void indexFastaFile(String fastaFileName, String samtoolsExecutable, String bsubOutDir, Scheduler scheduler, Session drmaaSession) throws IOException, InterruptedException, DrmaaException {
		String cmmd = samtoolsExecutable + " faidx " + fastaFileName;
		logger.info("Running samtools command: " + cmmd);
		switch(scheduler) {
		case LSF:
			String jobID = Long.valueOf(System.currentTimeMillis()).toString();
			logger.info("LSF job ID is " + jobID + ".");
			LSFJob job = new LSFJob(Runtime.getRuntime(), jobID, cmmd, bsubOutDir + "/index_fasta_" + jobID + ".bsub", "hour", 4);
			job.submit();
			logger.info("Waiting for samtools faidx job to finish...");
			job.waitFor();
			break;
        case OGS:
            if(drmaaSession == null) {
                    throw new IllegalArgumentException("DRMAA session is null. Must provide an active DRMAA session to use OGS. There can only be one active session at a time. Session should have been created in the main method of the class calling this method.");
            }
            OGSJob ogsJob = new OGSJob(drmaaSession, cmmd, "index_fasta", null);
            ogsJob.submit();
            logger.info("OGS job ID is " + ogsJob.getID() + ".");
			logger.info("Waiting for samtools faidx job to finish...");
			ogsJob.waitFor();
            break;
		default:
			throw new IllegalArgumentException("Scheduler " + scheduler.toString() + " not supported.");
		}
	}


}
