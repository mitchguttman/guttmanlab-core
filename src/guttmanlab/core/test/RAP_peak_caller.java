package guttmanlab.core.test;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.PairedMappedFragment;
import guttmanlab.core.annotation.PopulatedWindow;
import guttmanlab.core.annotation.SAMFragment;
import guttmanlab.core.annotation.io.BEDFileIO;
import guttmanlab.core.annotationcollection.BAMPairedFragmentCollection;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import org.apache.commons.lang.time.StopWatch;

import jsc.distributions.Binomial;
import net.sf.samtools.util.CloseableIterator;

public class RAP_peak_caller {
	public static void main(String[] args) throws IOException
	{
		//set up
		StopWatch sw = new StopWatch();
		sw.start();
		if(args.length != 7)
			System.err.println("Usage: 1.SampleFile 2.ControlFile 3.OutputFile 4.GeneFile 5.winSize 6.stepSize 7.maxPValue");
		
		String SampleFile = args[0];
		String ControlFile = args[1];
		String OutputFile = args[2];
		String FeatureFile = args[3];
		int winSize = Integer.parseInt(args[4]);
		int stepSize = Integer.parseInt(args[5]);
		double maxP = Double.parseDouble(args[6]);
		
		PrintWriter writer = new PrintWriter(OutputFile, "UTF-8");
		BEDFileIO io =  new BEDFileIO("/storage/shared/CoreTestData/refspace.txt"); //mm9
		CloseableIterator<? extends Annotation> features = io.loadFromFile(FeatureFile).sortedIterator();
		Annotation region = null;

		while(features.hasNext())
		{
			region = features.next();

			//read input and sample files
			BAMPairedFragmentCollection bamPair = new BAMPairedFragmentCollection(new File(SampleFile));
			BAMPairedFragmentCollection bamInput = new BAMPairedFragmentCollection(new File(ControlFile));
			int controlTotal = bamInput.numOverlappers(region, true);
			int sampleTotal =  bamPair.numOverlappers(region, true);
			//get windows creates an iterator over all the windows for the specified region (in this case each chromosome from the genefile)
			CloseableIterator<? extends PopulatedWindow<PairedMappedFragment<SAMFragment>>> sampleWindows = bamPair.getPopulatedWindows(region, winSize,stepSize);
			CloseableIterator<? extends PopulatedWindow<PairedMappedFragment<SAMFragment>>> controlWindows = bamInput.getPopulatedWindows(region, winSize,stepSize);

			System.out.println("Sample: "+sampleTotal+"\tControl: "+controlTotal);
			System.out.println("Calculating windows for "+region.toString());

			PopulatedWindow<? extends PairedMappedFragment<SAMFragment>> currentSampleWin = null;
			PopulatedWindow<? extends PairedMappedFragment<SAMFragment>> currentControlWin = null;
			if(sampleWindows.hasNext() && controlWindows.hasNext())
			{
				currentSampleWin = sampleWindows.next();
				currentControlWin = controlWindows.next();
			}
			
			//getWindows returns all nonzero windows over the region.  we need to loop over the sample and control to align the windows 
			for(int i = Math.min(currentControlWin.getReferenceStartPosition(),currentSampleWin.getReferenceStartPosition()); i< region.getReferenceEndPosition()-stepSize; i+=stepSize)
			{ 
				int sampleCount = 0;
				int controlCount = 0;
					
				if(currentSampleWin.getReferenceStartPosition()==i)
				{
					sampleCount = currentSampleWin.getNumberOfAnnotationsInWindow();
					if(sampleWindows.hasNext())
						currentSampleWin = sampleWindows.next();
				}
				if(currentControlWin.getReferenceStartPosition()==i)
				{
					controlCount = currentControlWin.getNumberOfAnnotationsInWindow();
					if(controlWindows.hasNext())
						currentControlWin = controlWindows.next();
				}
				int chrSize = region.size();
				double pValue = getPValue(controlCount,sampleCount,controlTotal,sampleTotal,winSize,chrSize);
				if(i%1000000==0)
					System.out.println(i+"...");
				if(pValue <= maxP)
					writer.println(region.getName()+"\t"+i+"\t"+(i+winSize)+"\t"+pValue);
			}
			
			System.out.println("Time elapsed: "+sw.getTime());

		}	
		sw.stop();
		writer.close();
		System.out.println("Total time elapsed: "+sw.getTime());
	}

	public static double getPValue(double controlCount, double sampleCount, double controlTotal, double sampleTotal, double winSize,int chrSize)
	{
		if(sampleCount < 2)
			return 1.0;
		
		double n = sampleCount + controlCount;
		double p = sampleTotal/(sampleTotal+controlTotal);
		double lambdaW = n*p;  //this is already the expected value for the window
		double lambda = lambdaW/winSize;
		//double lambdaW = lambda*winSize;
		double a=((sampleCount-lambdaW)/sampleCount)*(lambda*(chrSize-winSize)*poisson((int)sampleCount-1, lambdaW));     // poisson function = Poisson PDF
		double result=Fp((int)sampleCount-1, lambdaW)*Math.exp(-a);
		double pval = 1 - result;
		pval = Math.abs(pval);
		pval = Math.min(1, pval);
		pval = Math.max(pval, 0);
		System.out.println("n\tp\tlambda\ta\tresult\tpval");
		System.out.println(n+"\t"+p+"\t"+lambda+"\t"+a+"\t"+result+"\t"+pval);
		return pval;
	}
	
	public static BigDecimal getBinomialPValue(double controlCount, double sampleCount, double controlTotal, double sampleTotal, double winSize,int chrSize)
	{
		
		BigDecimal one = new BigDecimal(1.0);
		if(controlCount + sampleCount < 2) return one;
		if(controlTotal + sampleTotal < 2) return one;
		
		// Compute binomial parameters and P value
		double p = sampleTotal/(sampleTotal + controlTotal);
		if(p == 0) return one;
		long n = (long) (controlCount + sampleCount);
		Binomial b = new Binomial(n,p);
		BigDecimal cdf = new BigDecimal(b.cdf(sampleCount));
		BigDecimal pdf = new BigDecimal(b.pdf(sampleCount));
		BigDecimal pval = one.subtract(cdf.subtract(pdf));
		if(pval.doubleValue() == 0)
			System.out.println(p+"\t"+n+"\tcdf"+b.cdf(sampleCount)+"\tpdf"+b.pdf(sampleCount)+"\tpval:"+pval);
		return pval;
		
	}
	
	public static double poisson(int k, double lambda){
		cern.jet.random.Poisson poiss=new cern.jet.random.Poisson(lambda, new cern.jet.random.engine.DRand());
		return poiss.pdf(k);
	}
	
	public static double Fp(int k,double lambdaW){
		double sum=0;
		for(int i=0; i<=k; i++){
			sum+=poisson(i, lambdaW);
		}
		return sum;
	}
	
}