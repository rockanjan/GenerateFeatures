package edu.temple.srl.datastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VocabIndex {

	/*
	 * Reads file to create feature. Converts the words into integer indices
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		int THRESHOLD = 2;
		// System.out.println("Integer Max Value = " + Integer.MAX_VALUE);
		String vocabIndexFile = "/home/anjan/Dropbox/research/vocab_index_threshold_2_or_more.txt";
		String NUM = "__NUM__";
		// File file = new File ("/temp/test.txt");
		File file = new File("/home/anjan/Dropbox/research/combined.words");
		Pattern p1 = Pattern.compile("(^-{0,1}[0-9]+\\.*[0-9]*)+"); // eg -9, 100,
																	// 100.001
																	// etc
		Pattern p2 = Pattern.compile("^-{0,1}[0-9]*\\.*[0-9]+"); // eg. -.5, .5
		Pattern p3 = Pattern.compile("^-{0,1}[0-9]{1,3}[,[0-9]{3}]*\\.*[0-9]*"); // matches
																					// 100,000
		Pattern p4 = Pattern.compile("[0-9]+\\\\/[0-9]+"); // four \ needed,
															// java converts it
															// to \\
		Pattern p5 = Pattern.compile("[0-9]+:[0-9]+"); // ratios and time
		Pattern p6 = Pattern.compile("([0-9]+-)+[0-9]+"); // 1-2-3, 1-2-3-4 etc
		int SIZE = 50000;

		HashMap<String, Integer> wordCount = new HashMap<String, Integer>(SIZE);

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		System.out.println("Counting...");
		while ((line = br.readLine()) != null) {
			if (!line.trim().equals("")) {
				String[] splitted = line.split(" ");
				String word = splitted[0].toLowerCase();
				Matcher m1 = p1.matcher(word);
				Matcher m2 = p2.matcher(word);
				Matcher m3 = p3.matcher(word);
				Matcher m4 = p4.matcher(word);
				Matcher m5 = p5.matcher(word);
				Matcher m6 = p6.matcher(word);
				if (m1.matches() || m2.matches() || m3.matches()
						|| m4.matches() || m5.matches() || m6.matches()) {
					word = NUM;
				}
				
				word = word.replaceAll("" +
						"([0-9]+\\\\/[0-9]+)|" +
						"(([0-9]+-)+[0-9]+)|" +
						"([0-9]+:[0-9]+)|" +
						"(^-{0,1}[0-9]{1,3}[,[0-9]{3}]*\\.*[0-9]*)|" +
						"(^-{0,1}[0-9]*\\.*[0-9]+)|" +
						"(^-{0,1}[0-9]+\\.*[0-9]*)+"
						, "__NUM__"); //for something like 10-years-old, 2-for-3 etc
				if (wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word) + 1);
				} else {
					wordCount.put(word, 1);
				}
			}
		}
		br.close();
		/*
		 * for( String key : wordCount.keySet()){ System.out.println(key +
		 * " --> " + wordCount.get(key)); }
		 */

		System.out.println("Writing...");
		// write it into an index file
		PrintWriter pw = new PrintWriter(vocabIndexFile);
		int i = 0;
		for (String key : wordCount.keySet()) {
			if (wordCount.get(key) >= THRESHOLD) {
				pw.println(++i + "\t" + key + "\t" + wordCount.get(key));
			} else {
				pw.println(-1 + "\t" + key + "\t" + wordCount.get(key));
			}
		}
		System.out.println("Size of vocab : " + i);
		pw.flush();
		pw.close();

		// Rearrange so that higher frequency gets lower integers
		// run script file
		String scriptFile = "/home/anjan/Dropbox/research/process_vocab.sh";
		System.out.println("Rearranging using script : " + scriptFile);
		Process p = Runtime.getRuntime().exec("sh " + scriptFile);
		p.waitFor();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		String s = null;
		// read the output from the command
		while ((s = stdInput.readLine()) != null) {
			System.out.println("Output from script:");
			System.out.println(s);
		}
		// read any errors from the attempted command
		while ((s = stdError.readLine()) != null) {
			System.out.println("Error from script: ");
			System.out.println(s);
		}
		System.out.println("Script executed with status " + p.exitValue());
		System.out.println("Done");

	}
}
