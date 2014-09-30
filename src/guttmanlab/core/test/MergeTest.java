package guttmanlab.core.test;
import static org.junit.Assert.*;
import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.BlockedAnnotation;
import guttmanlab.core.annotation.SingleInterval;
import guttmanlab.core.annotation.Annotation.Strand;
import guttmanlab.core.annotationcollection.FeatureCollection;
import guttmanlab.core.coordinatespace.CoordinateSpace;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class MergeTest {
	
	@Test
	public void MergeTwoSingleBlockAnnotations() {
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		a1.addBlock(block1);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block2 = new SingleInterval("a1",200,500);		
		a2.addBlock(block2);
		
		Annotation a3 = a1.merge(a2);
		assertEquals("merged should have 1 block.",1,a3.getNumberOfBlocks());
		assertEquals("merged start = 100",100,a3.getReferenceStartPosition());
		assertEquals("merged end = 500",500,a3.getReferenceEndPosition());
	}
	
	@Test
	public void MergeTwoBlockedAnnotations() {
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		SingleInterval block2 = new SingleInterval("a1",350,500);
		a1.addBlock(block1);
		a1.addBlock(block2);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block3 = new SingleInterval("a1",50,200);
		SingleInterval block4 = new SingleInterval("a1",325,400);
		a2.addBlock(block3);
		a2.addBlock(block4);
		
		Annotation a3 = a1.merge(a2);
		assertEquals("merged should have 2 blocks.",2,a3.getNumberOfBlocks());
		Iterator<SingleInterval> iter = a3.getBlocks();
		Annotation a31 = iter.next();
		Annotation a32 = iter.next();
		
		assertEquals("block1 start = 50",50,a31.getReferenceStartPosition());
		assertEquals("block1 end = 300",300,a31.getReferenceEndPosition());
		assertEquals("block2 start = 325",325,a32.getReferenceStartPosition());
		assertEquals("block2 end = 500",500,a32.getReferenceEndPosition());
	}
	
	@Test
	public void CollapseToSingleInterval() {
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		SingleInterval block2 = new SingleInterval("a1",350,500);
		a1.addBlock(block1);
		a1.addBlock(block2);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block3 = new SingleInterval("a1",50,200);
		SingleInterval block4 = new SingleInterval("a1",275,400);
		a1.addBlock(block3);
		a2.addBlock(block4);
		
		Annotation a3 = a1.merge(a2);
		assertEquals("merged should have 1 blocks.",1,a3.getNumberOfBlocks());
		assertEquals("block1 start = 50",50,a3.getReferenceStartPosition());
		assertEquals("block2 end = 500",500,a3.getReferenceEndPosition());
	}
	
	//@Test //appears orientation is not used by overlaps() 
	public void AnnotationsMustHaveCompatibleOrientations() {
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		a1.addBlock(block1);
		a1.setOrientation(Strand.POSITIVE);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block2 = new SingleInterval("a1",200,500);		
		a2.addBlock(block2);
		a2.setOrientation(Strand.NEGATIVE);

		Annotation a3 = a1.merge(a2);
		assertEquals("merged should have 0 blocks.",0,a3.getNumberOfBlocks());
		
		a2.setOrientation(Strand.BOTH);
		a3 = a1.merge(a2);
		assertEquals("merged should have 1 block.",1,a3.getNumberOfBlocks());
	}
	
	@Test
	public void FeatureCollectionMerge2to1() {
		Map<String,Integer> mapping = new TreeMap<String,Integer>();
		mapping.put("a1",1);
		mapping.put("a2",1000);
		CoordinateSpace fcspace = new CoordinateSpace(mapping);
		FeatureCollection<BlockedAnnotation> fc = new FeatureCollection<BlockedAnnotation>(fcspace);
		
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		a1.addBlock(block1);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block2 = new SingleInterval("a1",200,500);		
		a2.addBlock(block2);
		
		fc.add(a1);
		fc.add(a2);
		
		FeatureCollection<BlockedAnnotation> fc_merged = fc.merge();
		assertEquals("merged fc contains one annotation",1,fc_merged.getNumAnnotations());
	}
	
	@Test
	public void FeatureCollectionMerge3to1() {
		Map<String,Integer> mapping = new TreeMap<String,Integer>();
		mapping.put("a1",1);
		mapping.put("a2",1000);
		CoordinateSpace fcspace = new CoordinateSpace(mapping);
		FeatureCollection<BlockedAnnotation> fc = new FeatureCollection<BlockedAnnotation>(fcspace);
		
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		a1.addBlock(block1);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block2 = new SingleInterval("a1",200,500);		
		a2.addBlock(block2);
		
		BlockedAnnotation a3 = new BlockedAnnotation();
		SingleInterval block3 = new SingleInterval("a1",350,600);		
		a3.addBlock(block3);
		
		fc.add(a1);
		fc.add(a2);
		fc.add(a3);
		
		FeatureCollection<BlockedAnnotation> fc_merged = fc.merge();
		assertEquals("merged fc contains one annotation",1,fc_merged.getNumAnnotations());
	}
	
	@Test
	public void FeatureCollectionMerge4to2() {
		Map<String,Integer> mapping = new TreeMap<String,Integer>();
		mapping.put("a1",1);
		mapping.put("a2",1000);
		CoordinateSpace fcspace = new CoordinateSpace(mapping);
		FeatureCollection<BlockedAnnotation> fc = new FeatureCollection<BlockedAnnotation>(fcspace);
		
		BlockedAnnotation a1 = new BlockedAnnotation();
		SingleInterval block1 = new SingleInterval("a1",100,300);
		a1.addBlock(block1);
		
		BlockedAnnotation a2 = new BlockedAnnotation();
		SingleInterval block2 = new SingleInterval("a1",200,500);		
		a2.addBlock(block2);
		
		BlockedAnnotation a3 = new BlockedAnnotation();
		SingleInterval block3 = new SingleInterval("a1",600,700);		
		a3.addBlock(block3);
		
		BlockedAnnotation a4 = new BlockedAnnotation();
		SingleInterval block4 = new SingleInterval("a1",650,750);		
		a4.addBlock(block4);
		
		fc.add(a1);
		fc.add(a2);
		fc.add(a3);
		fc.add(a4);
		
		FeatureCollection<BlockedAnnotation> fc_merged = fc.merge();
		assertEquals("merged fc contains two annotations",2,fc_merged.getNumAnnotations());
	}
	
}
