package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import annotation.Gene;
import annotation.io.BEDFileIO;
import annotationcollection.FeatureCollection;

public class FeatureCollectionTest {
	
	private static String bedFile = "/storage/Annotations/RefSeq/mm9/RefSeq.bed";
	private static String referenceSizes = "/storage/Genomes/mm9/sizes";
	private Map<String, FeatureCollection<Gene>> genesByReference;
	private FeatureCollection<Gene> genesChr1;
	
	@Before
	public void setUp() {
		try {
			genesByReference = BEDFileIO.loadFromFileByReferenceName(bedFile, referenceSizes);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		genesChr1 = genesByReference.get("chr1");
	}
	
	@Test
	public void testChr1Num() {
		assertEquals(genesChr1.size(), 1624);
	}
	
	@Test
	public void testAddRemove() {
		Iterator<Gene> iter = genesChr1.iterator();
		assertTrue(iter.hasNext());
		Gene gene = iter.next();
		genesChr1.remove(gene);
		assertTrue(!genesChr1.contains(gene));
		assertEquals(1623.0, genesChr1.size(), 0.001);
		genesChr1.add(gene);
		assertTrue(genesChr1.contains(gene));
		assertEquals(1624.0, genesChr1.size(), 0.001);
		genesChr1.clear();
		assertTrue(genesChr1.isEmpty());
	}
	
}
