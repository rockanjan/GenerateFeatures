package edu.temple.srl.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.temple.srl.datastructure.DataRow;
import edu.temple.srl.datastructure.Sentence;
import edu.temple.srl.datastructure.VocabIndexReader;
import edu.temple.srl.stemmer.Stemmer;
/*
 * Generates all features for the dependency
 * NOTE: other features like hmm+word+hmm for each word (for comma related), etc will be added from python script 
 */
public class FeatureGenerator extends FeatureGeneratorBase {
	Logger logger = Logger.getLogger(FeatureGenerator.class.getName());

	public FeatureGenerator(String inputFilename, String outputFilename) {
		super(inputFilename, outputFilename);
	}

	@Override
	public void run() {
		try {
			load();
		} catch (IOException ioe) {
			System.err.println("Error in processing input file");
			ioe.printStackTrace();
			System.exit(1);
		}
		for (Sentence s : data) {
			int verbWordIndex = s.getVerbWordIndex();
			ArrayList<Integer> verbToTop = new ArrayList<Integer>();
			int currentIndex = verbWordIndex;
			while (currentIndex != -1) {
				verbToTop.add(currentIndex);
				int parent = s.get(currentIndex).getParentIndex();
				currentIndex = parent;
			}
			for (DataRow dr : s) {
				// processing word level features like numeric, capitalization,
				// periods, hyphens, colons etc
				String wordFeatures[] = getWordFeatures(dr);
				String alphaNum = wordFeatures[0]; // alpha or numeric
				String fourDigitNum = wordFeatures[1]; // Y or N
				String hasPeriods = wordFeatures[2];
				String hasHyphens = wordFeatures[3];
				String hasCapitalized = wordFeatures[4];
				String hasSlashes = wordFeatures[5]; // like a\/k\/a
				String hasColons = wordFeatures[6];
				String hasDollars = wordFeatures[7];
				String suffix1 = wordFeatures[8];
				String suffix2 = wordFeatures[9];
				String suffix3 = wordFeatures[10];

				// more features from fei
				String beforeAfterPredicate = getBeforeOrAfterPredicate(dr,
						verbWordIndex);
				String hmmBeforePredicate = getHmmBeforePredicate(s, dr,
						verbWordIndex);
				String hmmAfterPredicate = getHmmAfterPredicate(s, dr,
						verbWordIndex);
				String hmmPathToPredicate = getHmmPathToPredicate(s, dr,
						verbWordIndex);
				String wordPathToPredicate = getWordPath(s, dr, verbWordIndex);
				//String stemmedWordPathToPredicate = getStemmedWordPath(s, dr,verbWordIndex);
				String naivePathToPredicate = getNaiveStatePath(s, dr, verbWordIndex);
				String distance = getDistance(dr, verbWordIndex);
				// for now discretize the distance, later have to use it as
				// continuous feature
				if (!distance.equals(FeatureGeneratorBase.NO_VERB)) {
					int intDistance = Integer.parseInt(distance);
					if (intDistance == 0 || intDistance == 1
							|| intDistance == 2) {
						// do nothing
					} else if (intDistance > 2 && intDistance <= 5) {
						distance = "3-5";
					} else if (intDistance >= 6 && intDistance <= 9) {
						distance = "6-9";
					} else if (intDistance >= 10 && intDistance <= 15) {
						distance = "10-15";
					} else if (intDistance >= 16) {
						distance = ">=16";
					}
				}
				// more features: words/hmms before verb, which might capture
				// info about passive verbs
				String[] combo = getCombinationsBeforePredicate(s, dr,
						verbWordIndex);
				String wordBeforePredicate = combo[0];
				String twoHmms = combo[1];
				String oneHmmOneWord = combo[2];
				String oneWordOneHmm = combo[3];
				String twoWords = combo[4];
				String threeHmms = combo[5];
				String twoHmmsWord = combo[6];
				String oneHmmsTwoWord = combo[7];
				String oneHmmOneWordOneHmm = combo[8];

				// verb specific features
				String[] verbSpecific = getVerbSpecific(s, verbWordIndex);
				String verbHmm = verbSpecific[0];
				String verbWord = verbSpecific[1];
				String verbStemmedWord = verbSpecific[2];
				String verbOneSuffix = verbSpecific[3];
				String verbTwoSuffix = verbSpecific[4];
				
				
				//dependency features
				String[] dependencyFeatures = getDependencyFeatures(s, dr, verbToTop);
				String directionWordToVerb = dependencyFeatures[0];
				String upAndDownDistance = dependencyFeatures[1];
				String totalUpAndDownDistance = dependencyFeatures[2];
				String hmmPathUptoCommonRoot = dependencyFeatures[3];
				String hmmPathCommonRootDownToVerb = dependencyFeatures[4];
				//classification features
				ArrayList<String> classificationFeatures = getClassificationFeatures(s, dr);
				String predArgFirstWord = classificationFeatures.get(0);
				String predArgLastWord = classificationFeatures.get(1);
				String predArgFirstHmm = classificationFeatures.get(2);
				String predArgLastHmm = classificationFeatures.get(3);
				String headDistance = classificationFeatures.get(4);
				String wordBeforeArg = classificationFeatures.get(5);
				String wordAfterArg =  classificationFeatures.get(6);
				
				//phrases
				String[] phraseFromCurrent = getPhraseFromCurrentWord(s, dr);
				String[] phraseUptoCurrent = getPhraseUptoCurrentWord(s, dr);
				String hmmPhraseFromCurrent = phraseFromCurrent[0];
				String wordPhraseFromCurrent = phraseFromCurrent[1];
				
				String hmmPhraseUptoCurrent = phraseUptoCurrent[0];
				String wordPhraseUptoCurrent = phraseUptoCurrent[1];
				//combine
				String wholeHmmPhrase = "";
				String wholeWordPhrase = "";
				

				if(! hmmPhraseUptoCurrent.equals(FeatureGeneratorBase.NO_PHRASE)) {
					wholeHmmPhrase += hmmPhraseUptoCurrent + FeatureGeneratorBase.JOIN;
					wholeWordPhrase += wordPhraseUptoCurrent + FeatureGeneratorBase.JOIN;
				}
				
				wholeHmmPhrase += dr.getHmmState();
				wholeWordPhrase += dr.getSmoothedWord();
				
				if(! hmmPhraseFromCurrent.equals(FeatureGeneratorBase.NO_PHRASE) ) {
					wholeHmmPhrase += FeatureGeneratorBase.JOIN + hmmPhraseFromCurrent;
					wholeWordPhrase += FeatureGeneratorBase.JOIN + wordPhraseFromCurrent;
				}
				
				//distance of the phrase from the current predicate
				String distPhrase = getCurrentPhraseDistanceFromPredicate(s, dr, verbWordIndex);
				
				pw.println(dr.getRowWithAppendedFeatureNew(
						alphaNum,
						fourDigitNum, hasPeriods, hasHyphens, hasCapitalized,
						hasSlashes,
						hasColons,
						hasDollars,
						suffix1,
						suffix2,
						suffix3,
						beforeAfterPredicate,// start of fei's features
						hmmBeforePredicate, hmmAfterPredicate,
						hmmPathToPredicate, wordPathToPredicate,
						//stemmedWordPathToPredicate, 
						distance,

						wordBeforePredicate, twoHmms, oneHmmOneWord,
						oneWordOneHmm, twoWords, threeHmms, twoHmmsWord,
						oneHmmsTwoWord, oneHmmOneWordOneHmm,

						verbHmm, verbWord.toLowerCase(), verbStemmedWord.toLowerCase(), 
						verbOneSuffix.toLowerCase(),
						verbTwoSuffix.toLowerCase(), naivePathToPredicate, 
						
						directionWordToVerb,
						upAndDownDistance,
						totalUpAndDownDistance,
						hmmPathUptoCommonRoot,
						hmmPathCommonRootDownToVerb,
						
						//classification features
						predArgFirstWord,
						predArgLastWord,
						predArgFirstHmm,
						predArgLastHmm,
						headDistance,
						wordBeforeArg,
						wordAfterArg,
						
						//phrase features
						hmmPhraseUptoCurrent,
						wordPhraseUptoCurrent,
						hmmPhraseFromCurrent,
						wordPhraseFromCurrent,
						wholeHmmPhrase,
						wholeWordPhrase,
						distPhrase
						));
			}
			pw.println();
		}
		close();
	}

	private String[] getVerbSpecific(Sentence s, int verbIndex) {
		// verb hmm, verb word, stemmed verb word, 1-suffix, 2-suffix
		String hmm = FeatureGeneratorBase.NO_VERB;
		String verbWord = FeatureGeneratorBase.NO_VERB;
		String stemmedVerbWord = FeatureGeneratorBase.NO_VERB;
		String oneSuffix = FeatureGeneratorBase.NO_VERB;
		String twoSuffix = FeatureGeneratorBase.NO_VERB;
		if (verbIndex != -1) {
			DataRow dr = s.get(verbIndex);
			hmm = "" + dr.getHmmState();
			verbWord = dr.getWord();
			stemmedVerbWord = Stemmer.stemWord(verbWord).toLowerCase();
			oneSuffix = verbWord.substring(verbWord.length() - 1,
					verbWord.length());
			if (verbWord.length() >= 2) {
				twoSuffix = verbWord.substring(verbWord.length() - 2,
						verbWord.length());
			} else {
				twoSuffix = FeatureGeneratorBase.NO_SUFFIX;
			}
			verbWord = VocabIndexReader.getSmoothedString(verbWord);
		}
		String[] returnValue = { hmm, verbWord, stemmedVerbWord, oneSuffix,
				twoSuffix };
		return returnValue;
	}

	// word/hmm combinations before predicate
	private String[] getCombinationsBeforePredicate(Sentence s, DataRow dr,
			int verbIndex) {
		String hmmPredicate = FeatureGeneratorBase.NO_HMM;
		String hmmOneBefore = FeatureGeneratorBase.NO_HMM1;
		String hmmTwoBefore = FeatureGeneratorBase.NO_HMM2;

		String wordPredicate = FeatureGeneratorBase.NO_WORD;
		// include this only as feature as well
		String wordOneBefore = FeatureGeneratorBase.NO_WORD1;
		String wordTwoBefore = FeatureGeneratorBase.NO_WORD2;

		if (verbIndex >= 0) {
			hmmPredicate = "" + s.get(verbIndex).getHmmState();
			//wordPredicate = s.get(verbIndex).getWord();
			wordPredicate = s.get(verbIndex).getSmoothedWord() + "";
		}

		if (verbIndex >= 1) {
			hmmOneBefore = "" + s.get(verbIndex - 1).getHmmState();
			//wordOneBefore = s.get(verbIndex - 1).getWord();
			wordOneBefore = s.get(verbIndex - 1).getSmoothedWord() + "";
		}
		if (verbIndex >= 2) {
			hmmTwoBefore = "" + s.get(verbIndex - 2).getHmmState();
			//wordTwoBefore = "" + s.get(verbIndex - 2).getWord();
			wordTwoBefore = s.get(verbIndex - 2).getSmoothedWord() + "";
		}

		// two hmms
		String twoHmms = hmmOneBefore + FeatureGeneratorBase.JOIN
				+ hmmPredicate;
		// one hmm and word
		String oneHmmOneWord = hmmOneBefore + FeatureGeneratorBase.JOIN
				+ wordPredicate;
		// one word and hmm
		String oneWordOneHmm = wordOneBefore + FeatureGeneratorBase.JOIN
				+ hmmPredicate;
		// both words
		String twoWords = wordOneBefore + FeatureGeneratorBase.JOIN
				+ wordPredicate;

		// three hmms
		String threeHmms = hmmTwoBefore + FeatureGeneratorBase.JOIN
				+ hmmOneBefore + FeatureGeneratorBase.JOIN + hmmPredicate;
		// two hmms and word
		String twoHmmsWord = hmmTwoBefore + FeatureGeneratorBase.JOIN
				+ hmmOneBefore + FeatureGeneratorBase.JOIN + wordPredicate;
		// one hmm and two words
		String oneHmmsTwoWord = hmmTwoBefore + FeatureGeneratorBase.JOIN
				+ wordOneBefore + FeatureGeneratorBase.JOIN + wordPredicate;
		// one hmm one word one hmm
		String oneHmmOneWordOneHmm = hmmTwoBefore + FeatureGeneratorBase.JOIN
				+ wordOneBefore + FeatureGeneratorBase.JOIN + hmmPredicate;

		String[] returnString = { wordOneBefore, twoHmms, oneHmmOneWord,
				oneWordOneHmm, twoWords, threeHmms, twoHmmsWord,
				oneHmmsTwoWord, oneHmmOneWordOneHmm };
		return returnString;
	}

	private String[] getWordFeatures(DataRow dr) {
		String word = dr.getWord();
		int wordIndex = dr.getIndex(); // to check capitalization, if it's first
										// word or not
		String suffix1 = FeatureGeneratorBase.NO_SUFFIX, suffix2 = FeatureGeneratorBase.NO_SUFFIX, suffix3 = FeatureGeneratorBase.NO_SUFFIX;
		String alphaNum; // alpha or numeric
		String fourDigitNum; // Y or N
		String hasPeriods;
		String hasHyphens;
		String hasCapitalized;
		String hasSlashes; // like a\/k\/a
		String hasColons;
		String hasDollars;
		suffix1 = word.substring(word.length() - 1, word.length());
		if (word.length() > 1) {
			suffix2 = word.substring(word.length() - 2, word.length());
		}
		if (word.length() > 2) {
			suffix3 = word.substring(word.length() - 3, word.length());
		}
		Pattern p = Pattern.compile("^-{0,1}[0-9]+\\.*[0-9]*"); // eg -9, 100,
																// 100.001 etc
		Pattern p2 = Pattern.compile("^-{0,1}[0-9]*\\.*[0-9]+"); // eg. -.5, .5
		Pattern p3 = Pattern.compile("^-{0,1}[0-9]{1,3}[,[0-9]{3}]*\\.*[0-9]*"); // matches
																					// 100,000
		Pattern p4 = Pattern.compile("[0-9]+\\\\/[0-9]+"); // four \ needed,
															// java converts it
															// to \\
		Pattern p5 = Pattern.compile("[0-9]+:[0-9]+"); // ratios and time
		Pattern p6 = Pattern.compile("([0-9]+-)+[0-9]+"); // 1-2-3, 1-2-3-4 etc
		Matcher m = p.matcher(word);
		Matcher m2 = p2.matcher(word);
		Matcher m3 = p3.matcher(word);
		Matcher m4 = p4.matcher(word);
		Matcher m5 = p5.matcher(word);
		Matcher m6 = p6.matcher(word);
		// alpha or num
		if (m.matches() || m2.matches() || m3.matches() || m4.matches()
				|| m5.matches() || m6.matches()) {
			// System.out.println(word);
			alphaNum = FeatureGeneratorBase.NUM;
		} else {
			alphaNum = FeatureGeneratorBase.ALPHA;
		}
		// four digit
		Matcher fourDigitMatcher = Pattern.compile("[0-9]{4}").matcher(word);
		if (fourDigitMatcher.matches()) {
			if (wordIndex == 0) {

			}
			fourDigitNum = FeatureGeneratorBase.YES;
		} else {
			fourDigitNum = FeatureGeneratorBase.NO;
		}
		// period
		Matcher periodMatcher = Pattern.compile(".*\\..*").matcher(word);
		if (periodMatcher.matches())
			hasPeriods = FeatureGeneratorBase.YES;
		else
			hasPeriods = FeatureGeneratorBase.NO;

		// hyphen
		Matcher hyphenMatcher = Pattern.compile(".*-.*").matcher(word);
		if (hyphenMatcher.matches())
			hasHyphens = FeatureGeneratorBase.YES;
		else
			hasHyphens = FeatureGeneratorBase.NO;

		// capitalized
		Pattern capitalizedPattern = Pattern.compile(".*[A-Z]+.*");
		Matcher capitalized = capitalizedPattern.matcher(word);
		hasCapitalized = FeatureGeneratorBase.NO;
		if (capitalized.matches()) {
			if (wordIndex == 0) {// first word
				// remove the first char, and if it still matches, then add to
				// hasCapitalized
				String firstCharRemoved = word.substring(1);
				Matcher cap = Pattern.compile(".*[A-Z]+.*").matcher(
						firstCharRemoved);
				if (cap.matches()) {
					hasCapitalized = FeatureGeneratorBase.YES;
				}
			} else { // it's not first word and is capitalized
				hasCapitalized = FeatureGeneratorBase.YES;
			}
		}

		// has slashes (backslashes)
		Matcher slashMatcher = Pattern.compile(".*/.*").matcher(word);
		if (slashMatcher.matches())
			hasSlashes = FeatureGeneratorBase.YES;
		else
			hasSlashes = FeatureGeneratorBase.NO;

		// colons
		Matcher colonMatcher = Pattern.compile(".*:.*").matcher(word);
		if (colonMatcher.matches())
			hasColons = FeatureGeneratorBase.YES;
		else
			hasColons = FeatureGeneratorBase.NO;

		Matcher dollarMatcher = Pattern.compile(".*\\$.*").matcher(word);
		if (dollarMatcher.matches())
			hasDollars = FeatureGeneratorBase.YES;
		else
			hasDollars = FeatureGeneratorBase.NO;
		String wordFeatures[] = { alphaNum, fourDigitNum, hasPeriods,
				hasHyphens, hasCapitalized, hasSlashes, hasColons, hasDollars,
				suffix1, suffix2, suffix3 };
		return wordFeatures;
	}

	// Fei feature
	private String getBeforeOrAfterPredicate(DataRow dr, int verbIndex) {
		String returnValue = FeatureGeneratorBase.ERROR;
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		if (dr.getPredicateLabel().equals("V")) {
			return "V";
		}
		if (dr.getIndex() < verbIndex) {
			return FeatureGeneratorBase.BEFORE;
		}
		if (dr.getIndex() > verbIndex) {
			return FeatureGeneratorBase.AFTER;
		}
		return returnValue;
	}

	private String getDistance(DataRow dr, int verbIndex) {
		String returnValue = FeatureGeneratorBase.ERROR;
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		if (dr.getPredicateLabel().equals("V")) {
			return "0";
		}
		returnValue = Math.abs(dr.getIndex() - verbIndex) + "";
		return returnValue;
	}

	private String getHmmBeforePredicate(Sentence s, DataRow dr, int verbIndex) {
		String returnValue = FeatureGeneratorBase.ERROR;
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		if (verbIndex == 0) { // if verb is itself the first word
			return FeatureGeneratorBase.OOB;
		}
		returnValue = "" + s.get(verbIndex - 1).getHmmState();
		return returnValue;
	}

	private String getHmmAfterPredicate(Sentence s, DataRow dr, int verbIndex) {
		String returnValue = FeatureGeneratorBase.ERROR;
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		if (verbIndex == s.size() - 1) {
			return FeatureGeneratorBase.OOB;
		}
		returnValue = "" + s.get(verbIndex + 1).getHmmState();
		return returnValue;
	}

	private String getHmmPathToPredicate(Sentence s, DataRow dr, int verbIndex) {
		String returnValue = "";
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		int currentIndex = dr.getIndex();
		if (currentIndex < verbIndex) {
			for (int i = currentIndex + 1; i < verbIndex; i++) {
				returnValue += s.get(i).getHmmState();
				if (i != verbIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		} else {
			for (int i = verbIndex + 1; i < currentIndex; i++) {
				returnValue += s.get(i).getHmmState();
				if (i != currentIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		}
		if (returnValue.equals("")) {
			returnValue = FeatureGeneratorBase.NO_PATH;
		}
		if (dr.getIndex() == verbIndex) {
			returnValue = FeatureGeneratorBase.VERB;
		}
		return returnValue;
	}

	private String getWordPath(Sentence s, DataRow dr, int verbIndex) {
		String returnValue = "";
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		int currentIndex = dr.getIndex();
		if (currentIndex < verbIndex) {
			for (int i = currentIndex + 1; i < verbIndex; i++) {
				//returnValue += s.get(i).getWord();
				returnValue += s.get(i).getSmoothedWord();
				if (i != verbIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		} else {
			for (int i = verbIndex + 1; i < currentIndex; i++) {
				//returnValue += s.get(i).getWord();
				returnValue += s.get(i).getSmoothedWord();
				if (i != currentIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		}
		if (returnValue.equals("")) {
			returnValue = FeatureGeneratorBase.NO_PATH;
		}
		if (dr.getIndex() == verbIndex) {
			returnValue = FeatureGeneratorBase.VERB;
		}
		return returnValue;
	}
	
	private String getNaiveStatePath(Sentence s, DataRow dr, int verbIndex){
		String returnValue = "";
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		int currentIndex = dr.getIndex();
		if (currentIndex < verbIndex) {
			for (int i = currentIndex + 1; i < verbIndex; i++) {
				returnValue += s.get(i).getNaiveState();
				if (i != verbIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		} else {
			for (int i = verbIndex + 1; i < currentIndex; i++) {
				returnValue += s.get(i).getNaiveState();
				if (i != currentIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		}
		if (returnValue.equals("")) {
			returnValue = FeatureGeneratorBase.NO_PATH;
		}
		if (dr.getIndex() == verbIndex) {
			returnValue = FeatureGeneratorBase.VERB;
		}
		return returnValue;
	}

	private String getStemmedWordPath(Sentence s, DataRow dr, int verbIndex) {
		String returnValue = "";
		if (verbIndex == -1) {
			return FeatureGeneratorBase.NO_VERB;
		}
		int currentIndex = dr.getIndex();
		if (currentIndex < verbIndex) {
			for (int i = currentIndex + 1; i < verbIndex; i++) {
				returnValue += Stemmer.stemWord(s.get(i).getSmoothedWord());
				if (i != verbIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		} else {
			for (int i = verbIndex + 1; i < currentIndex; i++) {
				returnValue += Stemmer.stemWord(s.get(i).getSmoothedWord());
				if (i != currentIndex - 1) {
					returnValue += FeatureGeneratorBase.JOIN;
				}
			}
		}
		if (returnValue.equals("")) {
			returnValue = FeatureGeneratorBase.NO_PATH;
		}
		if (dr.getIndex() == verbIndex) {
			returnValue = FeatureGeneratorBase.VERB;
		}
		return returnValue;
	}

	/*****************************************Dependency feature*******************************/
	
	private String[] getDependencyFeatures(Sentence s, DataRow dr,
			ArrayList<Integer> verbToTop) {
		int FEATURE_SIZE = 5;
		String[] returnValue = new String[FEATURE_SIZE];
		//get features
		String feature1 = getDirectionFromWordToVerb(s, dr, verbToTop);
		String feature2and3[] = getNumberUpsDownsAndTotalDistance(s, dr, verbToTop);
		String feature4and5[] = getHmmPathUptoCommonRootAndDownToVerb(s, dr, verbToTop);
		//String feature6 = getWordCommonRootVerb(s, dr, verbToTop);
		//combine
		returnValue[0] = feature1;
		returnValue[1] = feature2and3[0];
		returnValue[2] = feature2and3[1];
		returnValue[3] = feature4and5[0];
		returnValue[4] = feature4and5[1];
		//returnValue[5] = feature6;
		return returnValue;
	}
	
	//1
	private String getDirectionFromWordToVerb(Sentence s, DataRow dr,
			ArrayList<Integer> verbToTop){
		if (verbToTop.size() == 0) {
			return FeatureGeneratorBase.NO_VERB;
		}
		String feature = "";
		int currentIndex = dr.getIndex();
		while (!verbToTop.contains(currentIndex)) {
			int currentNBayesState = s.get(currentIndex).getNaiveState();
			//feature += currentNBayesState;
			feature += FeatureGeneratorBase.UP;
			currentIndex = s.get(currentIndex).getParentIndex();
		}
		// starting descend from index in the array of the list of nodes from
		// verb to root
		int descendStartIndex = verbToTop.indexOf(currentIndex);
		for (int i = descendStartIndex; i >= 0; i--) {
			int nBayesState = s.get(verbToTop.get(i)).getNaiveState();
			//feature += nBayesState;
			if (i != 0) {
				feature += FeatureGeneratorBase.DOWN;
			}
		}
		if(feature.equals("")){
			//it's the verb itself
			feature = "V";
		}
		return feature;
	}
	//2 and 3
	private String[] getNumberUpsDownsAndTotalDistance(Sentence s, DataRow dr,
			ArrayList<Integer> verbToTop){
		String[] returnValue = new String[2];
		if (verbToTop.size() == 0) {
			returnValue[0] = FeatureGeneratorBase.NO_VERB;
			returnValue[1] = FeatureGeneratorBase.NO_VERB;
			return returnValue;
		}
		String feature = "";
		int currentIndex = dr.getIndex();
		int upCount = 0;
		int downCount = 0;
		int totalCount = 0;
		while (!verbToTop.contains(currentIndex)) {
			upCount++;
			currentIndex = s.get(currentIndex).getParentIndex();
		}
		feature += upCount;
		feature += FeatureGeneratorBase.JOIN;
		// starting descend from index in the array of the list of nodes from
		// verb to root
		int descendStartIndex = verbToTop.indexOf(currentIndex);
		for (int i = descendStartIndex; i >= 0; i--) {
			int nBayesState = s.get(verbToTop.get(i)).getNaiveState();
			downCount++;
		}
		downCount--; //there will be one extra count in the loop
		feature += downCount;
		returnValue[0] = feature;
		totalCount = upCount + downCount;
		returnValue[1] = "" + totalCount; 
		return returnValue;
	}
	//4 and 5
	private String[] getHmmPathUptoCommonRootAndDownToVerb(Sentence s, DataRow dr,
			ArrayList<Integer> verbToTop){
		String[] returnString = new String[2]; //returns 2 features
		if (verbToTop.size() == 0) {
			returnString[0] = FeatureGeneratorBase.NO_VERB;
			returnString[1] = FeatureGeneratorBase.NO_VERB;
			return returnString;
		}
		String feature = "";
		int currentIndex = dr.getIndex();
		while (!verbToTop.contains(currentIndex)) {
			int currentHmmState = s.get(currentIndex).getHmmState();
			feature += currentHmmState;
			currentIndex = s.get(currentIndex).getParentIndex();
			feature += FeatureGeneratorBase.JOIN;
		}
		//add the common root for up feature
		feature += s.get(verbToTop.get(verbToTop.indexOf(currentIndex))).getHmmState();
		/*
		if(feature.equals("")){
			feature += FeatureGeneratorBase.NOUP;
		}
		*/
		returnString[0] = feature;
		
		feature = "";
		int descendStartIndex = verbToTop.indexOf(currentIndex);
		for (int i = descendStartIndex; i >= 0; i--) {
			int hmmState = s.get(verbToTop.get(i)).getHmmState();
			feature += hmmState;
			if (i != 0) {
				feature += FeatureGeneratorBase.JOIN;
			}
		}
		
		//this condition may never satisfy
		if(feature.equals("")){
			feature += FeatureGeneratorBase.NODOWN;
		}
		
		returnString[1] = feature;
		return returnString;
	}
	
	//6 : TODO: check every condition before use
	private String getWordCommonRootVerb(Sentence s, DataRow dr,
			ArrayList<Integer> verbToTop){
		if (verbToTop.size() == 0) {
			return FeatureGeneratorBase.NO_VERB;
		}
		if(dr.getPredicateLabel().equals("V")){
			return s.get(dr.getIndex()).getHmmState() + "";
		}
		String feature = "";
		int currentIndex = dr.getIndex();
		int currentNBayesState = s.get(currentIndex).getHmmState();
		feature += currentNBayesState;
		boolean hasCommonRoot = false;
		while (!verbToTop.contains(currentIndex)) {
			hasCommonRoot = true;
			currentIndex = s.get(currentIndex).getParentIndex();
			currentNBayesState = s.get(currentIndex).getHmmState();
		}
		feature += FeatureGeneratorBase.JOIN;
		if(hasCommonRoot){
			//add the common root for up feature
			feature += s.get(verbToTop.get(verbToTop.indexOf(currentIndex))).getHmmState();
			feature += FeatureGeneratorBase.JOIN;
		}
		int descendStartIndex = verbToTop.indexOf(currentIndex);
		int verbNaiveState = s.get(verbToTop.get(0)).getHmmState();
		feature += verbNaiveState;
		return feature;
	}
	
	/*****************Classification Features*********************/
	private ArrayList<String> getClassificationFeatures(Sentence s, DataRow dr){
		ArrayList<String> features = new ArrayList<String>();
		//combination feature
		//predicate + first argument word, predicate+ last argument word,
		//predicate + first argument HMM, predicate+ last argument HMM
		if(dr.getPredicateLabel().equals("V")){
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			features.add(FeatureGeneratorBase.VERB);
			return features;
		}
		String bioLabel = dr.getIdentificationLabel();
		String predArgFirstWord = FeatureGeneratorBase.NO_ARG;
		String predArgLastWord = FeatureGeneratorBase.NO_ARG;
		String predArgFirstHmm = FeatureGeneratorBase.NO_ARG;
		String predArgLastHmm = FeatureGeneratorBase.NO_ARG;
		int headDistance = -1; 
		String wordBeforeArg = FeatureGeneratorBase.NO_ARG;
		String wordAfterArg = FeatureGeneratorBase.NO_ARG;
		
		String predicateWord = FeatureGeneratorBase.NO_VERB;
		int verbIndex = s.getVerbWordIndex();
		if(verbIndex >= 0){
			 predicateWord = s.get(verbIndex).getSmoothedWord();
		}
		
		String firstWord = "";
		String lastWord = "";
		Integer firstHmm = null;
		Integer lastHmm = null;
		
		if(bioLabel.equals("O")){	
			//do nothing
		} else if(bioLabel.equals("B")){
			if(predicateWord.equals(FeatureGeneratorBase.NO_VERB)){
				System.err.println("Sentence without a verb has an argument");
				s.debugString();
				System.exit(1);
			}
			headDistance = Math.abs(verbIndex - dr.getIndex());
			if(dr.getIndex() != 0){
				wordBeforeArg = s.get(dr.getIndex()-1).getSmoothedWord();
			} else {
				wordBeforeArg = FeatureGeneratorBase.OOB;
			}
			firstWord = dr.getSmoothedWord();
			firstHmm = dr.getHmmState();
			//for last
			int currentIndex = dr.getIndex();
			for(int i=currentIndex+1; i<s.size(); i++){
				String nextBio = s.get(i).getIdentificationLabel();
				if(nextBio.equals("B") || nextBio.equals("O")){
					lastWord = s.get(i-1).getSmoothedWord();
					lastHmm = s.get(i-1).getHmmState();
					wordAfterArg = s.get(i).getSmoothedWord();
					break;
				}
			}
			if(lastWord.isEmpty()){ //end of sentence and still we had I
				lastWord = s.get(s.size()-1).getSmoothedWord();
				lastHmm = s.get(s.size()-1).getHmmState();
				wordAfterArg = FeatureGeneratorBase.OOB;
			}
		} else if(bioLabel.equals("I")){
			//find initial B
			int currentIndex = dr.getIndex();
			for(int i=currentIndex-1 ; i>=0; i--){
				String previousBio = s.get(i).getIdentificationLabel();
				if(previousBio.equals("B")){
					firstWord = s.get(i).getSmoothedWord();
					firstHmm = s.get(i).getHmmState();
					//head distance
					headDistance = Math.abs(verbIndex - i);
					if(i != 0){
						wordBeforeArg = s.get(i-1).getSmoothedWord();
					} else {
						wordAfterArg = FeatureGeneratorBase.OOB;
					}
					break;
				}
			}
			
			for(int i=currentIndex+1; i<s.size(); i++){
				String nextBio = s.get(i).getIdentificationLabel();
				if(nextBio.equals("B") || nextBio.equals("O")){
					lastWord = s.get(i-1).getSmoothedWord();
					lastHmm = s.get(i-1).getHmmState();
					wordAfterArg = s.get(i).getSmoothedWord();
					break;
				}
			}
			if(lastWord.isEmpty()){ //end of sentence and still we had I
				lastWord = s.get(s.size()-1).getSmoothedWord();
				lastHmm = s.get(s.size()-1).getHmmState();
				wordAfterArg = FeatureGeneratorBase.OOB;
			}
		} else {
			System.err.println("Unexpected BIO tag " + bioLabel);
			System.exit(1);
		}
		if(! bioLabel.equals("O")) {
			predArgFirstWord = predicateWord + FeatureGeneratorBase.JOIN + firstWord;
			predArgLastWord = predicateWord + FeatureGeneratorBase.JOIN + lastWord;
			predArgFirstHmm = predicateWord + FeatureGeneratorBase.JOIN + firstHmm;
			predArgLastHmm = predicateWord + FeatureGeneratorBase.JOIN + lastHmm;
		}
		//add the features into return array
		features.add(predArgFirstWord);
		features.add(predArgLastWord);
		features.add(predArgFirstHmm);
		features.add(predArgLastHmm);
		
		//the number of tokens between the ﬁrst token of the argument	phrase and the target predicate
		features.add(headDistance + "");
		//words before and after the argument
		features.add(wordBeforeArg);
		features.add(wordAfterArg);
		
		
		return features;
	}

	/*****************Phrases between punctuations****************/
	
	//current word EXCLUDED in both cases
	//a. phrases(hmm and word) from current verb or previous punctuation or BOS upto the current word
	//b. phrases from the current word upto current verb or punctuation or EOS
	
	//a.
	private String[] getPhraseUptoCurrentWord(Sentence s, DataRow dr) {
		String returnString[] = {FeatureGeneratorBase.NO_PHRASE, FeatureGeneratorBase.NO_PHRASE};
		int currentIndex = dr.getIndex();
		if(currentIndex > 0){
			//find the index of either previous punctuation, current verb or BOS
			int startIndex = -1; 
			for(int i=currentIndex-1; i>=0; i--) {
				if(FeatureGeneratorBase.punctuations.contains(s.get(i).getWord()) || s.get(i).getPredicateLabel().equals("V")) {
					startIndex = i;
					break;
				}
			}
			//if startIndex still is -1, reached BOS
			String hmmPhrase = "";
			String wordPhrase = "";
			for(int i=startIndex+1; i<currentIndex; i++) {
				hmmPhrase += s.get(i).getHmmState();
				wordPhrase += s.get(i).getSmoothedWord();
				if(i != currentIndex-1) {
					hmmPhrase += FeatureGeneratorBase.JOIN;
					wordPhrase += FeatureGeneratorBase.JOIN;
				}
			}
			if(! hmmPhrase.isEmpty() && ! wordPhrase.isEmpty() ) {
				returnString[0] = hmmPhrase;
				returnString[1] = wordPhrase;
			}
		}
		return returnString;
	}
	
	//b.
	private String[] getPhraseFromCurrentWord(Sentence s, DataRow dr) { 
		String returnString[] = {FeatureGeneratorBase.NO_PHRASE, FeatureGeneratorBase.NO_PHRASE};
		int currentIndex = dr.getIndex();
		if(currentIndex < s.size()) {
			//find the index of either next punctuation, current verb or EOS
			int endIndex = s.size(); //invalid index
			for(int i=currentIndex+1; i<s.size(); i++) {
				if(FeatureGeneratorBase.punctuations.contains(s.get(i).getWord()) || s.get(i).getPredicateLabel().equals("V")) {
					endIndex = i;
					break;
				}
			}
			//if endIndex is still size of sentence, reached EOS
			String hmmPhrase = "";
			String wordPhrase = "";
			for(int i=currentIndex+1; i<endIndex; i++) {
				hmmPhrase += s.get(i).getHmmState();
				wordPhrase += s.get(i).getSmoothedWord();
				if(i != endIndex-1) {
					hmmPhrase += FeatureGeneratorBase.JOIN;
					wordPhrase += FeatureGeneratorBase.JOIN;
				}
			}
			if(! hmmPhrase.isEmpty() && ! wordPhrase.isEmpty() ) {
				returnString[0] = hmmPhrase;
				returnString[1] = wordPhrase;
			}
		}
		return returnString;
	}
	
	//c. phrase distance from the predicate
	
	private String getCurrentPhraseDistanceFromPredicate(Sentence s, DataRow dr, int verbWordIndex) {
		String distance = "NV";
		if(verbWordIndex == -1) {
			return distance;
		}
		int dist = 0;
		if(dr.getIndex() == verbWordIndex) {
			distance = "V";
			return distance;
		}
		else if(dr.getIndex() < verbWordIndex) {
			for(int i=dr.getIndex()+1; i<verbWordIndex; i++) {
				if(s.get(i).getWord().equals(",") | s.get(i).getWord().equals("--")) {
					dist++;
				}
			}
		} 
		else {
			for(int i=verbWordIndex+1; i<dr.getIndex(); i++) {
				if(s.get(i).getWord().equals(",") | s.get(i).getWord().equals("--")) {
					dist++;
				}
			}
		}
		return dist + "";
	}
}
