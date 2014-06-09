package sequence;

import java.util.Collection;
import java.util.List;

/**
 * Methods to read sequences from a fasta file or write sequences to a fasta file
 * @author prussell
 *
 */
public interface FastaFileIO {
	
	/**
	 * Read a fasta file and get sequences in the order of the file
	 * @param fileName Fasta file path
	 * @return The sequences in the file as sequence objects
	 */
	public List<Sequence> readFromFile(String fileName);
	
	/**
	 * Write a collection of sequences to a fasta file
	 * @param seqs Sequences to write
	 * @param fileName File to write to
	 * @param basesPerLine Number of sequence bases per line in the output file
	 */
	public void writeToFile(Collection<Sequence> seqs, String fileName, int basesPerLine);
	
}
