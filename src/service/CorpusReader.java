package service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CorpusReader 
{
 
    private HashMap<String,Integer> ngrams;
    public Set<String> vocabulary;
	public Bigram bigram;
    public HashMap<String,String> EXPANSIONS = new HashMap<String,String>();
    public CorpusReader(Bigram b) throws IOException
    {  
    	this.bigram = b;
        readNGrams(b);
        readVocabulary(b);
    }
    
    /**
     * Returns the n-gram count of <NGram> in the file
     * 
     * 
     * @param nGram : space-separated list of words, e.g. "roulement double rangée" : 3-gram example
     * @return 0 if <NGram> cannot be found, 
     * otherwise count of <NGram> in file
     */
     public int getNGramCount(String nGram) throws  NumberFormatException
    {
        if(nGram == null || nGram.length() == 0)
        {
        	
            //throw new IllegalArgumentException("NGram must be non-empty.");
        	return 0;
        }
        Integer value = ngrams.get(nGram);
        return value==null?0:value;
    }
    
    private void readNGrams(Bigram b) throws 
            FileNotFoundException, IOException, NumberFormatException
    {
        ngrams = new HashMap<>();

        
        for (String w1:b.getCounts().keySet()) {
        	for(String w2:b.getCounts().get(w1).keySet()) {
        		String phrase = String.valueOf(Math.floor(b.getCounts().get(w1).get(w2))).split("\\.")[0]+" "+w1+" "+w2;
	            phrase = phrase.trim();
	            count_line(phrase);
	            
        	}
        }
        
        for(String w:b.getUnigramCounts().keySet()) {
        	String phrase = String.valueOf(Math.floor(b.getUnigramCounts().get(w))).split("\\.")[0]+" "+w;
        	count_line(phrase);
            
        }
    }
    
    
    private void count_line(String phrase) {
    	String s1, s2;
        int j = phrase.indexOf(" ");

        s1 = phrase.substring(0, j);
        s2 = phrase.substring(j + 1, phrase.length());

        int count = 0;
        try {
            count = Integer.parseInt(s1);
            ngrams.put(s2, count);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("NumberformatError: " + s1);
        }
	}

	private void readVocabulary(Bigram b) throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();
        
        vocabulary.addAll(b.getUnigramCounts().keySet());
        
        
    }
    
    /**
     * Returns the size of the number of unique words in the dataset
     * 
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() 
    {
        return vocabulary.size();
    }
    
    /**
     * Returns the subset of words in set that are in the vocabulary
     * 
     * @param set
     * @return 
     */
    public HashSet<String> inVocabulary(Set<String> set) 
    {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }
    
    public boolean inVocabulary(String word) 
    {
       return vocabulary.contains(word);
    }    
    
    public double getSmoothedCount(String NGram, boolean candidateLeft)
    {
        String[] words = NGram.split(" ");
        if(NGram == null || NGram.length() == 0 || words.length != 2)
        {
            ;
            throw new IllegalArgumentException("NGram must be of length two");
        }
        
        double smoothedCount;
        int nGramCount = getNGramCount(NGram);
        if (candidateLeft) {
            smoothedCount = (double) (nGramCount + 1) / (double) (getNGramCount(words[1]) + 1);
        } else {
            smoothedCount = (double) (nGramCount + 1) / (double) (getNGramCount(words[0]) + 1);
        }
        
        return (double) smoothedCount;        
    }
}