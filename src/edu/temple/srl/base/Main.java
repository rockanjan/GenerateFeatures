package edu.temple.srl.base;

import java.io.IOException;

import edu.temple.srl.datastructure.VocabIndexReader;

public class Main {
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("Usage: <createfeaturenew> inputfile outputfile");
			System.exit(1);
		}
		String vocabIndexFile = "/home/anjan/Dropbox/vocab_index_final.before.txt";
		VocabIndexReader.__init__(vocabIndexFile);
		
		FeatureGenerator generator = new FeatureGenerator(args[0], args[1]);
		System.out.println("Running Feature Generator");
		System.out.println("Input file: " + args[0]);
		System.out.println("Output file: " + args[1]);
		generator.run();
		System.out.println("Done!");
	}
}
