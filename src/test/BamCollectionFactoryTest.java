package test;

import static org.junit.Assert.*;

import net.sf.samtools.util.CloseableIterator;

import org.junit.Test;

import annotation.BAMFragmentCollectionFactory;
import annotation.MappedFragment;
import annotationcollection.AbstractAnnotationCollection;

public class BamCollectionFactoryTest {

	private static String singleBam = "/storage/Projects/RibosomeProfiling/Ribosome.bam";
	private static String pairedBam = "/storage/Users/prussell/Testing/new_core_code_base/port_peak_caller/input.bam";
	
	@Test
	public void testSingleDetection() {
		assertTrue(!BAMFragmentCollectionFactory.isPairedEnd(singleBam));
	}

	@Test
	public void testPairedDetection() {
		assertTrue(BAMFragmentCollectionFactory.isPairedEnd(pairedBam));
	}
	
	@Test
	public void testSingleImplementation() {
		AbstractAnnotationCollection<? extends MappedFragment> data = BAMFragmentCollectionFactory.createFromBam(singleBam);
		CloseableIterator<? extends MappedFragment> iter = data.sortedIterator();
		iter.hasNext();
		assertEquals("annotation.SAMFragment", iter.next().getClass().getName());
	}
	
	@Test
	public void testForceSingleImplementation() {
		AbstractAnnotationCollection<? extends MappedFragment> data = BAMFragmentCollectionFactory.createFromBam(pairedBam, true);
		CloseableIterator<? extends MappedFragment> iter = data.sortedIterator();
		iter.hasNext();
		assertEquals("annotation.SAMFragment", iter.next().getClass().getName());
	}
	
	@Test
	public void testPairedImplementation() {
		AbstractAnnotationCollection<? extends MappedFragment> data = BAMFragmentCollectionFactory.createFromBam(pairedBam);
		CloseableIterator<? extends MappedFragment> iter = data.sortedIterator();
		iter.hasNext();
		assertEquals("annotation.PairedMappedFragment", iter.next().getClass().getName());
	}
	
}
