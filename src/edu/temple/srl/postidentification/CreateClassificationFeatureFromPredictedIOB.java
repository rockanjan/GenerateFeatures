package edu.temple.srl.postidentification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import sun.applet.Main;

public class CreateClassificationFeatureFromPredictedIOB {
	public static void main(String[] args) throws IOException {
		String outputFeatureFile = "/home/anjan/work/srl/jul30/dev.predictediob.classification"; 
		String fileWithOriginalIOB = "/home/anjan/work/conll05-backup/combined/dev.combined.final"; //only last column will be used
		String fileWithPredictedIOB = "/home/anjan/work/srl/aug3/dev.tag";
		int iobField = 7; //human index
		//replace the IOB field
		BufferedReader br1 = new BufferedReader(new FileReader(fileWithOriginalIOB));
		BufferedReader br2 = new BufferedReader(new FileReader(fileWithPredictedIOB));
		
		String finalFileWithPredictedIOB = "/tmp/predicted.final"; 
		PrintWriter pw = new PrintWriter(finalFileWithPredictedIOB);
		
		String line1 = "";
		String line2 = "";
		
		while( (line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
			String[] splittedOriginal = line1.split("(\\s+|\\t+)");
			String[] predictedSplitted = line2.split("(\\s+|\\t+)");
			if(! line1.trim().equals("")) {
				for(int i=0; i<iobField-1; i++){
					pw.print(splittedOriginal[i] + "\t");
				}
				
				pw.print(predictedSplitted[predictedSplitted.length - 1] + "\t");
				for(int i=iobField; i<splittedOriginal.length; i++){
					pw.print(splittedOriginal[i] + "\t");
				}				
			} else {
				if( ! line2.trim().equals("")) {
					System.err.println("Original and predicted files are not aligned");
				}
			}
			pw.println();
			pw.flush();
		}
		br1.close();
		br2.close();
		pw.close();
		
		//now create the new features using predicted IOB
		String[] arguments = {finalFileWithPredictedIOB, outputFeatureFile};
		System.out.println("Creating features...");
		edu.temple.srl.base.Main.main(arguments);
		System.out.println("Created feature file with the predicted IOB at " + outputFeatureFile);
	}
}
