package eu.davidgamez.mas.test.midi;

import org.junit.Test;

public class TestPerformance {
	
	@Test public void testAdvanceLoadBuffer(){
		try{
			//Test variables
			long startTime_us, startTime_ns;
			int numberOfTests = 100;
			long [] resultArray = new long[numberOfTests];
			
			//Test 10 ms delay
			for (int i=0; i<numberOfTests; ++i){
				startTime_us = System.nanoTime() / 1000;
				Thread.sleep(10);
				resultArray[i] = System.nanoTime()/1000 - startTime_us;
			}
			printResults("Single sleep duration", resultArray, 10);
			
			//Test 45 ms delay
			for (int i=0; i<numberOfTests; ++i){
				startTime_us = System.nanoTime() / 1000;
				Thread.sleep(45);
				resultArray[i] = System.nanoTime()/1000 - startTime_us;
			}
			printResults("Single sleep duration", resultArray, 45);

			//Test 1ms sleep durations
			for (int i=0; i<numberOfTests; ++i){
				startTime_us = System.nanoTime() / 1000;
				Thread.sleep(1);
				resultArray[i] = System.nanoTime()/1000 - startTime_us;
			}
			printResults("Single sleep duration", resultArray, 1);
			
			//Test multiple short durations
			for (int i=0; i<numberOfTests; ++i){
				startTime_ns = System.nanoTime();
				while(System.nanoTime() - startTime_ns < 10000000)
					Thread.sleep(1);
				resultArray[i] = (System.nanoTime() - startTime_ns)/1000;
			}
			printResults("Multiple short delays", resultArray, 10);
			
		}
		catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	private void printResults(String description, long[] resArray, int expectedTime){
		System.out.println(description + ". Expected time: " + expectedTime + "; mean: " + getMean(resArray)/1000 + "; standard deviation: " + getStandardDeviation(resArray)/1000);
	}
	
	
	private double getMean(long [] resArray){
		double mean = 0.0;
		for(int i=0; i<resArray.length; ++i){
			mean += resArray[i];
		}
		return mean/resArray.length;
	}
	
	private double getStandardDeviation(long [] resArray){
		double mean = getMean(resArray);
		double tmpSum = 0.0;
		for(int i=0; i<resArray.length; ++i)
			tmpSum += (mean - resArray[i]) * (mean - resArray[i]);
		return Math.sqrt(tmpSum / resArray.length);
	}
	
}
