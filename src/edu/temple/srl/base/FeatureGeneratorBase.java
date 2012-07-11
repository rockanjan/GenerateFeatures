package edu.temple.srl.base;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import edu.temple.srl.datastructure.DataRow;
import edu.temple.srl.datastructure.Sentence;

public abstract class FeatureGeneratorBase {
	Logger logger = Logger.getLogger(FeatureGeneratorBase.class.getName());
	protected final int DEFAULT_VERB_INDEX = -1;
	protected static final String ALPHA = "A";
	protected static final String NUM = "N";
	protected static final String NO_VERB= "NV";
	protected static final String VERB= "V";
	protected static final String UP = "U";
	protected static final String DOWN = "D";
	protected static final String JOIN = "+";
	protected static final String NOUP = "NU";
	protected static final String NODOWN = "ND";
	protected static final String BEFORE = "B";
	protected static final String AFTER = "A";
	protected static final String ERROR = "E";
	protected static final String OOB = "OOB"; //out of bound
	protected static final String YES = "Y";
	protected static final String NO = "N";
	protected static final String NO_ARG = "O";
	protected static final String NO_PATH = "NPTH";
	protected static final String NO_SUFFIX = "NSF";
	protected static final String NO_HMM = "NH";
	protected static final String NO_WORD = "NW";
	protected static final String NO_HMM1 = "NH1";
	protected static final String NO_WORD1 = "NW1";
	protected static final String NO_HMM2 = "NH2";
	protected static final String NO_WORD2 = "NW2";
	
	private String inputFilename;
	private String outputFilename;
	ArrayList<Sentence> data;
	PrintWriter pw;
	public FeatureGeneratorBase(String inputFilename, String outputFilename){
		this.inputFilename = inputFilename;
		this.outputFilename = outputFilename;
	}
	public void setInputFile(String filename){
		inputFilename = filename;
	}
	public void setOutputfile(String filename){
		outputFilename = filename;
	}
	
	public void load() throws IOException{
		//check if output file is writable
		try{
			pw = new PrintWriter(new File(outputFilename));
		}
		catch(FileNotFoundException f){
			System.err.println("Output file cannot be written : " + outputFilename);
			System.exit(1);
		}
		
		//load the sentences into the memory
		BufferedReader br = null;
		try{
			 br = new BufferedReader(new FileReader(new File(inputFilename)));
		}
		catch(FileNotFoundException fileNotFoundException){
			System.err.println("Input file for feature generation not found: " + inputFilename);
			System.exit(1);
		}
		
		data = new ArrayList<Sentence>();
		String line = "";
		Sentence s = new Sentence();
		while( (line = br.readLine()) != null){
			if(!line.trim().equals("")){
				DataRow dr = new DataRow();
				dr.processLine(line.trim());
				s.add(dr);
			}
			else{
				//one sentence processing complete
				if(s.size() != 0){
					data.add(s);
				}
				//System.out.println(s.debugString());
				s = null;
				s = new Sentence();
			}
		}
		br.close();
	}
	
	public abstract void run();
	
	public void close(){
		pw.close();
	}
}
