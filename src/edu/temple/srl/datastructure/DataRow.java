package edu.temple.srl.datastructure;

import java.util.logging.Logger;

import edu.temple.srl.stemmer.Stemmer;

/*
 * stores each row data of a sentence
 */
public class DataRow {
	private Logger logger = Logger.getLogger(DataRow.class.getName());
	private int index = -1;
	private int parentIndex = -2;
	private String word;
	private String pos;
	private String predicateLabel;
	private String intervenes; //number of verbs intervenining between word and current predicate
	private String identificationLabel;
	private String classificationLabel;
	private int hmmState;
	private int naiveState;
	private String chunk;
	private String ne;
	
	private int FIELDS = 12;
	
	public void processLine(String line){
		//processes a string of line, stores the field
		String[] splitted = line.split("(\\s+|\\t+)");
		if(splitted.length < FIELDS){
			System.err.println("DataRow: Cannot process line : " + line);
			System.exit(1);
		}
		index 		= Integer.parseInt(splitted[0]);
		parentIndex = Integer.parseInt(splitted[1]);
		word 		= splitted[2].trim();
		pos			= splitted[3].trim();
		predicateLabel = splitted[4].trim();
		intervenes = splitted[5];
		
		identificationLabel = splitted[6];
		classificationLabel = splitted[7];
		
		hmmState = Integer.parseInt(splitted[8]);
		naiveState = Integer.parseInt(splitted[9]);
		chunk = splitted[10];
		ne = splitted[11];
		if(splitted.length > FIELDS){
			System.err.println("WARNING: data row has more than required columns: " + line);
		}
	}
	
	/* variadic function */
	public String getRowWithAppendedFeatureNew(String... features) {
		StringBuilder sb = new StringBuilder();
		sb.append(VocabIndexReader.getSmoothedString(word.toLowerCase()) + " ");
		sb.append(word + " ");
		sb.append(Stemmer.stemWord(VocabIndexReader.getSmoothedString(word.toLowerCase())) + " ");
		sb.append(predicateLabel + " ");
		sb.append(intervenes + " ");
		sb.append(hmmState + " ");
		sb.append(naiveState + " ");
		sb.append(pos + " ");
		
		for(String feature: features){
			sb.append(feature + " ");
		}
		sb.append(chunk + " ");
		sb.append(ne + " ");
		sb.append(identificationLabel + " ");
		//sb.append(classificationLabel + " ");
		return sb.toString();
	}
	
	

	public String getSmoothedWord() {
		return VocabIndexReader.getSmoothedString(getWord());
	}
	public Logger getLogger() {
		return logger;
	}



	public void setLogger(Logger logger) {
		this.logger = logger;
	}



	public int getIndex() {
		return index;
	}



	public void setIndex(int index) {
		this.index = index;
	}



	public int getParentIndex() {
		return parentIndex;
	}



	public void setParentIndex(int parentIndex) {
		this.parentIndex = parentIndex;
	}



	public String getWord() {
		return word;
	}



	public void setWord(String word) {
		this.word = word;
	}



	public String getPos() {
		return pos;
	}



	public void setPos(String pos) {
		this.pos = pos;
	}



	public String getPredicateLabel() {
		return predicateLabel;
	}



	public void setPredicateLabel(String predicateLabel) {
		this.predicateLabel = predicateLabel;
	}



	public String getIntervenes() {
		return intervenes;
	}



	public void setIntervenes(String intervenes) {
		this.intervenes = intervenes;
	}



	public String getIdentificationLabel() {
		return identificationLabel;
	}



	public void setIdentificationLabel(String identificationLabel) {
		this.identificationLabel = identificationLabel;
	}



	public String getClassificationLabel() {
		return classificationLabel;
	}



	public void setClassificationLabel(String classificationLabel) {
		this.classificationLabel = classificationLabel;
	}



	public int getHmmState() {
		return hmmState;
	}



	public void setHmmState(int hmmState) {
		this.hmmState = hmmState;
	}



	public int getNaiveState() {
		return naiveState;
	}



	public void setNaiveState(int naiveState) {
		this.naiveState = naiveState;
	}	
	
	public String getChunk() {
		return chunk;
	}
	
	public String getNe(){
		return ne;
	}
}
