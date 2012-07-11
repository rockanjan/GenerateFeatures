package edu.temple.srl.stemmer;

import org.tartarus.snowball.SnowballStemmer;

public class Stemmer {
	static Class stemClass;
	static SnowballStemmer stemmer;
	
	
	private static void __init__(){
		if(stemClass == null){
			try {
				stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(stemmer == null){
			try {
				stemmer = (SnowballStemmer) stemClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		__init__();
		String testString = "catty";
		stemmer.setCurrent(testString);
		stemmer.stem();
		System.out.println(stemmer.getCurrent());
	}
	
	public static String stemWord(String input){
		__init__();
		stemmer.setCurrent(input);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}
