package edu.temple.srl.datastructure;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Sentence extends ArrayList<DataRow>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(Sentence.class.getName());
	public Sentence(){
		super();
	}
	
	public int getVerbWordIndex(){
		int returnValue = -1;
		for(DataRow dr : this){
			if(dr.getPredicateLabel().equals("V")){
				returnValue = dr.getIndex();
				break;
			}
		}
		return returnValue;
	}
	
	public String debugString(){
		StringBuilder sb = new StringBuilder();
		for(DataRow dr : this){
			sb.append(dr.getIndex() + " " + dr.getWord() + "\n");
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
}
