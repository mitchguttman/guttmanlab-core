package test;

import junit.framework.TestCase;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.util.CloseableIterator;

import org.junit.Before;
import org.junit.Test;

import annotation.Annotation;
import annotation.Annotation.Strand;
import annotation.DerivedAnnotation;
import annotation.Gene;
import annotation.SAMFragment;
import annotation.SingleInterval;
import annotation.PopulatedWindow;
import annotation.io.BEDFileIO;
import annotationcollection.AnnotationCollection;
import annotationcollection.BAMSingleReadCollection;
import coordinatespace.CoordinateSpace;

public class GetWindowsTest {
	
	private BAMSingleReadCollection bam;
	private SAMFileHeader fhead;
	private CoordinateSpace refSpace;
	private String fname;
	private BEDFileIO io;
	private AnnotationCollection<Gene> features;	
	@Before
	public void setUp() throws IOException
	{
		this.bam = new BAMSingleReadCollection(new File("/storage/shared/CoreTestData/chr19.clean.sorted.bam"));
		this.fhead = bam.getFileHeader(); 
		this.refSpace = new CoordinateSpace(fhead);  
		this.fname = "/storage/shared/CoreTestData/RefSeq.bed";  
		this.io =  new BEDFileIO("/storage/shared/CoreTestData/refspace.txt"); //use existing coordinateSpace?
		this.features = io.loadFromFile(fname);
	}
	
	//Test a single exon positive gene with only three reads
	@Test //Pass
	public void Ccdc87GetWindowReadCounts() throws IOException{
		CloseableIterator<Gene> iter = features.sortedIterator();
		Map<Integer,Integer> map1 = new HashMap<Integer,Integer>();
		
		//expect 164 windows w/1 read, 188 with 2, and 12 with 3.
		Map<Integer,Integer> expectedMap = new HashMap<Integer,Integer>();
		expectedMap.put(1, 164);
		expectedMap.put(2, 188);
		expectedMap.put(3, 12);
		
		Annotation a = null;
		while(iter.hasNext())
		{
			a = iter.next();
			if(a.getName().equals("NM_025741"))
				break;
		}
		
		iter.close();
		
		CloseableIterator<? extends PopulatedWindow<SAMFragment>> windows = bam.getPopulatedWindows(a, 50);
		
		int count = 0;
		while(windows.hasNext())
		{
			PopulatedWindow<SAMFragment> win = windows.next();
			int reads = win.getNumberOfAnnotationsInWindow();

			if(map1.containsKey(reads))
				{
					count = map1.get(reads);
					map1.remove(reads);
					map1.put(reads, count+1);
				}
			else
				map1.put(reads, 1);
		}
		
		System.out.println(map1.toString());
		assertEquals("Window read counts for Ccdc87",true,map1.equals(expectedMap));
	}
	
	//Test a single exon negative gene 
	@Test //Pass
	public void Neat1GetWindowReadCount() throws IOException{
		CloseableIterator<Gene> iter = features.sortedIterator();
		Map<Integer,Integer> map1 = new HashMap<Integer,Integer>();
		Annotation a = null;
		while(iter.hasNext())
		{
			a = iter.next();
			if(a.getName().equals("NR_003513"))
				break;
		}
		
		iter.close();
		CloseableIterator<? extends PopulatedWindow<SAMFragment>> windows = bam.getPopulatedWindows(a, 50);
		
		int winCount = 0;
		while(windows.hasNext())
		{
			PopulatedWindow<SAMFragment> win = windows.next();
			winCount++;
			int reads = win.getNumberOfAnnotationsInWindow();
			int count = 0;
			if(map1.containsKey(reads))
				{
					count = map1.get(reads);
					map1.remove(reads);
					map1.put(reads, count+1);
				}
			else
				map1.put(reads, 1);
		}
		
		System.out.println(map1.toString());
		assertEquals("shoud return 3370 nonempty windows of size 50",3370,winCount);
	}
	
	@Test
	//Verifies the correct reads are returned for CCdc87, a multi exon negative strand gene.
	public void Neat1ConvertCoordinatesGetWindows() throws IOException{
		CloseableIterator<Gene> iter = features.sortedIterator();
		Map<Integer,Integer> map1 = new HashMap<Integer,Integer>();
		Annotation a = null;
		while(iter.hasNext()) 
		{
			a = iter.next();
			if(a.getName().equals("NR_003513"))
				break;
		}
		iter.close();
		
		Annotation b = new SingleInterval(a.getName(), 0, a.size()-1);
		b.setOrientation(Strand.BOTH);
		AnnotationCollection<DerivedAnnotation<SAMFragment>> converted = features.convertCoordinates(bam, refSpace, true);
		CloseableIterator<? extends PopulatedWindow<DerivedAnnotation<SAMFragment>>> windows = converted.getPopulatedWindows(b, 50);

		int winCount = 0;
		while(windows.hasNext())
		{
			PopulatedWindow<DerivedAnnotation<SAMFragment>> win = windows.next();
			winCount++;
			int reads = win.getNumberOfAnnotationsInWindow();
			int count = 0;
			if(map1.containsKey(reads))
				{
					count = map1.get(reads);
					map1.remove(reads);
					map1.put(reads, count+1);
				}
			else
				map1.put(reads, 1);
		}
		
		System.out.println(map1.toString());
		assertEquals("converted coordinates windows",3177,winCount);
	}
	
}
