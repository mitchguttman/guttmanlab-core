package guttmanlab.core.math;
import java.math.BigDecimal;

import jsc.distributions.Binomial;
import jsc.distributions.Poisson;

public class ScanStat {
	
	public static double getPValue(double controlCount, double sampleCount, double controlTotal, double sampleTotal, double winSize,int chrSize)
	{
		if(sampleCount < 2)
			return 1.0;
		
		double n = sampleCount + controlCount;
		double p = sampleTotal/(sampleTotal+controlTotal);
		double lambdaW = n*p;
		double lambda = lambdaW/winSize;
		double a=((sampleCount-lambdaW)/sampleCount)*(lambda*(chrSize-winSize)*poisson((int)sampleCount-1, lambdaW));     // poisson function = Poisson PDF
		double result=Fp((int)sampleCount-1, lambdaW)*Math.exp(-a);
		double pval = 1 - result;
		pval = Math.abs(pval);
		pval = Math.min(1, pval);
		pval = Math.max(pval, 0);
		return pval;
		
	}
	
	public static BigDecimal getBinomialPValue(double controlCount, double sampleCount, double controlTotal, double sampleTotal, double winSize)
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
		Poisson poiss=new Poisson(lambda);
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
