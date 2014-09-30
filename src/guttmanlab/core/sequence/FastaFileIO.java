package guttmanlab.core.sequence;

import java.util.Collection;
import java.util.Iterator;

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
	public Collection<Sequence> readFromFile(String fileName);
	
	/**
	 * Get an iterator over the sequences specified in the file
	 * @param fileName Fasta file path
	 * @return An iterator over the sequences in the file
	 */
	public Iterator<Sequence> iterateThroughFile(String fileName);
	
	/**
	 * Write a collection of sequences to a fasta file
	 * @param seqs Sequences to write
	 * @param fileName File to write to
	 * @param basesPerLine Number of sequence bases per line in the output file
	 */
	public void writeToFile(Collection<Sequence> seqs, String fileName, int basesPerLine);
	
}
