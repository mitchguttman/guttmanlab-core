package guttmanlab.core.annotation.io;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.Gene;
import guttmanlab.core.annotation.SingleInterval;
import guttmanlab.core.annotation.Annotation.Strand;
import guttmanlab.core.annotationcollection.AnnotationCollection;
import guttmanlab.core.annotationcollection.FeatureCollection;
import guttmanlab.core.coordinatespace.CoordinateSpace;
import guttmanlab.core.datastructures.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This implementation will parse an entire file and read it into memory
 * @author mguttman
 *
 */
public class BEDFileIO implements AnnotationFileIO<Gene> {

	private CoordinateSpace referenceSpace;
	
	/**
	 * @param referenceSizes The reference coordinate information containing names and sizes
	 */
	public BEDFileIO(String referenceSizes){
		this.referenceSpace=new CoordinateSpace(referenceSizes);
	}
	
	public BEDFileIO(CoordinateSpace referenceSizes){
		this.referenceSpace=referenceSizes;
	}
	
	/**
	 * Write collection of annotations to a file
	 * @param regions The annotations to write
	 * @param outputBed Output bed file
	 * @throws IOException
	 */
	public static void writeToFile(AnnotationCollection<? extends Annotation> regions, String outputBed) throws IOException {
		FileWriter w = new FileWriter(outputBed);
		Iterator<? extends Annotation> iter = regions.sortedIterator();
		while(iter.hasNext()) {
			w.write(iter.next().toBED() + "\n");
		}
		w.close();
	}
	
	/**
	 * Static method to get the annotation collection represented in a bed file
	 * @param geneFile Bed file name 
	 * @param coordinateSpace The reference coordinate information containing names and sizes
	 * @return The collection of genes described in the bed file
	 * @throws IOException
	 */
	public static AnnotationCollection<Gene> loadFromFile(File geneFile, CoordinateSpace coordinateSpace) throws IOException {
		BEDFileIO bfio = new BEDFileIO(coordinateSpace);
		return bfio.loadFromFile(geneFile.getAbsolutePath());
	}
	
	/**
	 * Static method to get the annotation collection represented in a bed file
	 * @param geneFile Bed file name 
	 * @param chrSizeFile Table of chromosome names and sizes
	 * @return The collection of genes described in the bed file
	 * @throws IOException
	 */
	public static AnnotationCollection<Gene> loadFromFile(String fileName, String chrSizeFile) throws IOException {
		return loadFromFile(fileName, new CoordinateSpace(chrSizeFile));
	}
	
	public static AnnotationCollection<Gene> loadFromFile(String fileName, CoordinateSpace space) throws IOException {
		BEDFileIO bfio = new BEDFileIO(space);
		return bfio.loadFromFile(fileName);
	}
	
	/**
	 * Static method to get the annotation collection represented in a bed file, organized by reference name
	 * @param fileName Bed file name 
	 * @param referenceSizes The reference coordinate information containing names and sizes
	 * @return Map of reference name to the collection of genes on that reference described in the bed file
	 * @throws IOException
	 */
	public static Map<String, FeatureCollection<Gene>> loadFromFileByReferenceName(String fileName, String referenceSizes) throws IOException {
		CoordinateSpace refSpace = new CoordinateSpace(referenceSizes);
		Map<String, FeatureCollection<Gene>> rtrn = new TreeMap<String, FeatureCollection<Gene>>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String nextLine;
		while ((nextLine = reader.readLine()) != null) {
			Gene annotation=parse(nextLine);
			String reference = annotation.getReferenceName();
			if(!rtrn.containsKey(reference)) {
				rtrn.put(reference, new FeatureCollection<Gene>(refSpace));
			}
			rtrn.get(reference).addAnnotation(annotation);
		}
		reader.close();
		return rtrn;
	}
	
	@Override
	public AnnotationCollection<Gene> loadFromFile(String fileName) throws IOException {
		FeatureCollection<Gene> collection=new FeatureCollection<Gene>(referenceSpace);
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String nextLine;
		while ((nextLine = reader.readLine()) != null) {
			Gene annotation=parse(nextLine);
			collection.addAnnotation(annotation);
		}
		reader.close();
		return collection;
	}

	/**
	 * For each line, parse it into an Annotation with all information
	 * @param rawData
	 * @return An Annotation with all the information from the BED line
	 */
	private static Gene parse(String rawData) {
		String[] tokens = rawData.split("\t");
		String chr=(tokens[0]);
		int start=new Integer(tokens[1]);
		int end=new Integer(tokens[2]);
		Strand orientation=Strand.UNKNOWN;
		if(tokens.length > 3) {
			String name=tokens[3];
			if(tokens.length > 4) {
				@SuppressWarnings("unused")
				double score = new Double(tokens[4]); //TODO Currently unused
				if(tokens.length > 5){
					orientation= Annotation.Strand.fromString(tokens[5]);
					if(tokens.length > 11) { //Needs to have ALL block info to be parsed
						int cdsStart=Integer.parseInt(tokens[6]);
						int cdsEnd=Integer.parseInt(tokens[7]);
						String[] blockSizes=tokens[10].split(",");
						String[] blockStarts=tokens[11].split(",");
						Pair<List<Integer>> exonStartEnd=getBlockStartsAndEnds(blockStarts, blockSizes, new Integer(tokens[9]), start);
						Collection<Annotation> exons=new ArrayList<Annotation>();
						for(int i = 0; i < blockSizes.length; i++ ) {
							Annotation exon=new SingleInterval(chr, exonStartEnd.getValue1().get(i), exonStartEnd.getValue2().get(i), orientation, name);
							exons.add(exon);
						}
						Gene blockedAnnotation = new Gene(exons, cdsStart, cdsEnd, name);
						return blockedAnnotation;
					}
					else{ //size=6: Has orientation
						Gene g=new Gene(new SingleInterval(chr, start, end, orientation, name));
						return g;
					}
				}
				else{ //size=5: Has score
					Gene g=new Gene(new SingleInterval(chr, start, end, orientation, name));
					return g;
				}
			}
			else{ //size=4: Has name
				Gene g = new Gene(new SingleInterval(chr, start, end, orientation, name));
				return g;
			}
		}
		else{ // size=3: Has positions only
			String name=chr+":"+start+"-"+end;
			Gene g = new Gene(new SingleInterval(chr, start, end, orientation, name));
			return g;
		}
		
	}
	
	private static Pair<List<Integer>> getBlockStartsAndEnds(String[] blockStarts, String[] blockSizes, int size, int start){
		List<Integer> starts=new ArrayList<Integer> ();
		List<Integer>  end=new ArrayList<Integer> ();
		for(int i=0; i<size; i++){
			starts.add(start+new Integer(blockStarts[i].replaceAll("\"", "").trim()));
			end.add((Integer)starts.get(i)+new Integer(blockSizes[i].replaceAll("\"", "").trim()));
		}
		
		Pair<List<Integer>> rtrn=new Pair<List<Integer>>(starts, end);
		return rtrn;
	}


}
