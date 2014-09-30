package guttmanlab.core.test;

import static org.junit.Assert.*;
import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.BlockedAnnotation;
import guttmanlab.core.annotation.DerivedAnnotation;
import guttmanlab.core.annotation.PairedMappedFragment;
import guttmanlab.core.annotation.SingleInterval;
import guttmanlab.core.annotation.Annotation.Strand;
import guttmanlab.core.annotation.predicate.PairedFilterWrapper;

import org.junit.Before;
import org.junit.Test;

public class AnnotationTest {
		private Annotation pos;
		private Annotation pos2;
		private Annotation neg;
		private Annotation both;
		
	@Before
	public void setUp() throws Exception {
		 pos = new BlockedAnnotation();
		 pos2 = new BlockedAnnotation();
		 neg = new BlockedAnnotation();
		 both = new BlockedAnnotation();
		
		pos.setOrientation(Strand.POSITIVE);
		pos2.setOrientation(Strand.POSITIVE);
		neg.setOrientation(Strand.NEGATIVE);
		both.setOrientation(Strand.BOTH);
	}

	@Test
	public void EqualsShouldCompareEnumValues() {
		Strand strand1 = pos.getOrientation();
		Strand strand2 = pos2.getOrientation();
		Strand strand3 = neg.getOrientation();
		
		assertEquals("two positive strands should be equal",strand1.equals(strand2),true);
		assertEquals("two different orientations should be not equal",strand2.equals(strand3),false);
	}
	
	@Test 
	public void trimAnnotationTest()
	{

		BlockedAnnotation blocked = new BlockedAnnotation();
		//DerivedAnnotation converted = new DerivedAnnotation(both, both);
		//PairedMappedFragment paired = new PairedMappedFragment<>(pair));
		SingleInterval block1 = new SingleInterval("a1",100,300);
		SingleInterval block2 = new SingleInterval("a1",350,500);
		SingleInterval block3 = new SingleInterval("a1",600,700);
		
		blocked.addBlock(block1);
		blocked.addBlock(block2);
		blocked.addBlock(block3);
		
		blocked.setOrientation(Strand.POSITIVE);
		Annotation blocked1 = blocked.trim(0,800);    //no trimming
		Annotation blocked2 = blocked.trim(150,650);  //trim first and last exon
		Annotation blocked3 = blocked.trim(330,550);  //between exons
		
		System.out.println(blocked.toString());
		System.out.println(blocked1.toString());
		System.out.println(blocked2.toString());
		System.out.println(blocked3.toString()+"\n");
		
		blocked.setOrientation(Strand.NEGATIVE);
		blocked1 = blocked.trim(0,800);    //no trimming
	    blocked2 = blocked.trim(150,650);  //trim first and last exon
		blocked3 = blocked.trim(330,550);  //between exons
		
		System.out.println(blocked.toString());
		System.out.println(blocked1.toString());
		System.out.println(blocked2.toString());
		System.out.println(blocked3.toString());
		blocked.setOrientation(Strand.BOTH);
		
		/*
		PairedMappedFragment paired1 = paired.trim(0,800);
		PairedMappedFragment paired2 = paired.trim(0,800);
		PairedMappedFragment paired3 = paired.trim(0,800);
		
		DerivedAnnotation converted1 = converted.trim(0,800);
		DerivedAnnotation converted2 = converted.trim(0,800);
		DerivedAnnotation converted3 = converted.trim(0,800);
		*/
		
		
	}
	
	@Test 
	public void testHashCodeEquals()
	{
		BlockedAnnotation blocked = new BlockedAnnotation();
		BlockedAnnotation blocked2 = new BlockedAnnotation();
		BlockedAnnotation blocked3 = new BlockedAnnotation();

		//DerivedAnnotation converted = new DerivedAnnotation(both, both);
		//PairedMappedFragment paired = new PairedMappedFragment<>(pair));
		SingleInterval block1 = new SingleInterval("a1",100,300);
		SingleInterval block2 = new SingleInterval("a1",350,500);
		SingleInterval block3 = new SingleInterval("a1",600,700);
	
		blocked.addBlock(block1);
		blocked.addBlock(block2);
		blocked.addBlock(block3);
		
		blocked2.addBlock(block1);
		blocked2.addBlock(block2);
		blocked2.addBlock(block3);
		
		blocked3.addBlock(block3);
		
		System.out.println(blocked.hashCode());
		System.out.println(blocked2.hashCode());
		System.out.println(blocked3.hashCode());
		System.out.println(block3.hashCode());
		
		assertEquals("blocked1 and 3 not equal.",false,blocked.equals(blocked3));
		assertEquals("blocked1 and 2 equal.",true,blocked.equals(blocked2));
		assertEquals("blocked3 and single interval 3 equal.",true,block3.equals(blocked3));
		assertEquals("blocked3 and single interval 3 have same hash code.",true,blocked3.hashCode()==block3.hashCode());
		assertEquals("blocked2 and 3 have different hash codes.",false,blocked3.hashCode()==blocked2.hashCode());
	}

}
