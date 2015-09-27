import java.io.*;
import java.util.*;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bigram {
	
	public Scanner readFile() {
		Scanner input = null;
		try {
			input = new Scanner(new File("./Corpus.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return input;
	}
	
	public double totalWordsInDocument() {
		
		Scanner sc = readFile();
		double count = 0;
        while (sc.hasNext()) {

            String [] s = sc.next().split("d*[.@:=#-]"); 
            for (int i = 0; i < s.length; i++) {
                if (!s[i].isEmpty()){
                    count++;
                }   
            }           
        }
        return count;
	}
	
	public Map<String, Double> unigramCount() {
		
        Scanner input = readFile();
        Map<String, Double> wordCounts = new TreeMap<String, Double>();
        while (input.hasNext()) {
            String next = input.next().toLowerCase();
            if (!wordCounts.containsKey(next)) {
                wordCounts.put(next, (double) 1);
            } else {
                wordCounts.put(next, wordCounts.get(next) + 1);
            }
        }
        return wordCounts;
	}
	
	public double bigramCount(String word1, String word2) {
		
		String regex = word1.toLowerCase() + "\\s+" + word2.toLowerCase();
		Scanner input = readFile();
		double count = 0;
		while(input.hasNextLine()) {
			String line = input.nextLine().toLowerCase();
			Pattern pattern = Pattern.compile(regex);
			Matcher  matcher = pattern.matcher(line);
			while(matcher.find()) {
				count++;
			}
		}
		return count;
	}
	
	public double [][] getBigramCountTable(String sentence) {
			
		sentence = sentence.replaceAll("\\.", "").toLowerCase();
		String [] sentenceTokens = sentence.split("\\s+");
		double [][] bigramTable = new double[sentenceTokens.length][sentenceTokens.length];
		for(int i = 0; i < sentenceTokens.length; i++) {
			for(int j = 0; j < sentenceTokens.length; j++) {
	
				bigramTable[i][j] = bigramCount(sentenceTokens[i], sentenceTokens[j]);			
			}
		}
		return bigramTable;
	}
	
	public double [][] getBigramProbabilityTable(String sentence) {
		
		sentence = sentence.replaceAll("\\.", "").toLowerCase();
		String [] sentenceTokens = sentence.split("\\s+");
		double [][] bigramTable = new double[sentenceTokens.length][sentenceTokens.length];
		Map<String, Double> unigramCountMap = unigramCount();
		for(int i = 0; i < sentenceTokens.length; i++) {
			for(int j = 0; j < sentenceTokens.length; j++) {
				
				if(unigramCountMap.containsKey(sentenceTokens[i])) {
					bigramTable[i][j] = bigramCount(sentenceTokens[i], sentenceTokens[j]) / unigramCountMap.get(sentenceTokens[i]);
				}
				else {
					bigramTable[i][j] = 0;	
				}
			}
		}
		return bigramTable;
	}
	
	public double [][] getAddOneSmoothingBigramProbabilityTable(String sentence) {
		
		sentence = sentence.replaceAll("\\.", "").toLowerCase();
		String [] sentenceTokens = sentence.split("\\s+");
		double [][] bigramTable = new double[sentenceTokens.length][sentenceTokens.length];
		Map<String, Double> unigramCountMap = unigramCount();
		for(int i = 0; i < sentenceTokens.length; i++) {
			for(int j = 0; j < sentenceTokens.length; j++) {
				
				if(unigramCountMap.containsKey(sentenceTokens[i])) {
					bigramTable[i][j] = ( bigramCount(sentenceTokens[i], sentenceTokens[j]) + 1 ) / (unigramCountMap.get(sentenceTokens[i]) + unigramCountMap.size());
				}
				else {
					bigramTable[i][j] = ( bigramCount(sentenceTokens[i], sentenceTokens[j]) + 1 ) / unigramCountMap.size();
				}
			}
		}
		return bigramTable;
	}
	
	public double [][] getGoodTuringBigramProbabilityTable(String sentence) {
		
		double [][] b = getBigramCountTable(sentence);
		Map<String, Double> unigramCountMap = unigramCount();
		double totalWords = totalWordsInDocument();
		Collection<Double> c = unigramCountMap.values();
		double [][] good = new double[b.length][b.length];
		for(int i = 0; i < b.length; i++) {
			for(int j = 0; j < b.length; j++) {
				double Nc1 = Collections.frequency(c, b[i][j] + 1);
				double Nc = Collections.frequency(c, b[i][j]);
				if(Nc == 0) {
					double count_zero = Collections.frequency(c, 1);
					good[i][j] = count_zero / totalWords;
				}
				else {
					double count_star = (b[i][j] + 1) * (Nc1 / Nc);
					good[i][j] = count_star / totalWords;
				}
			}
		}
		return good;
	}
	
	public double calculateTotalProbabilityWithoutSmoothing(String sentence) {
		
		double [][] bigramTable = getBigramProbabilityTable(sentence);
		double total = 1;
	    for(int i = 0; i < bigramTable.length - 1; i++) {
			for(int j = i+1; j <= i+1; j++) {
				total *= bigramTable[i][j];
			}
		}
	    return total;
	}
	
	public double calculateTotalProbabilityWithSmoothing(String sentence) {
		
		double [][] bigramTable = getAddOneSmoothingBigramProbabilityTable(sentence);
		double total = 1;
	    for(int i = 0; i < bigramTable.length - 1; i++) {
			for(int j = i+1; j <= i+1; j++) {
				total *= bigramTable[i][j];
			}
		}
	    return total;
	}
	
	public double calculateTotalProbabilityWithGoodTuringDiscounting(String sentence) {
		
		double [][] bigramTable = getGoodTuringBigramProbabilityTable(sentence);
		double total = 1;
	    for(int i = 0; i < bigramTable.length - 1; i++) {
			for(int j = i+1; j <= i+1; j++) {
				total *= bigramTable[i][j];
			}
		}
	    return total;
	}
	
	public static void main(String [] args) throws Exception {
		
		Bigram b = new Bigram();
		double prob = b.calculateTotalProbabilityWithSmoothing("The President has reliquished control of the board.");
		System.out.println(prob);
		
		
	}

}
