package service;

import java.util.regex.Pattern;

import javax.sql.rowset.CachedRowSet;

import model.GlobalConstants;
import model.BinaryClassificationParameters;
import model.DescriptionFetchRow;
import transversal.language_toolbox.NgramParser;

import java.util.regex.Matcher;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

public class Bigram
{
    public Set<String> samples;
    public HashMap<String, HashMap<String, Double>> counts;
    public HashMap<String, Double> unigramCounts;
    public final String START = ":S";
    
    // For add-one smoothing
    public Set<String> wordSet; // Used to find the vocabulary
    public double vocabSize; // Size of the vocabulary
    
    // For Good Turing Smoothing
    public double numTrainingBigrams; // The size of the training set (# non-distinct words)
    public HashMap<Double, Double> numberOfBigramsWithCount; // The number of bigrams that occur x times
    public boolean goodTuringCountsAvailable = false; // True when good turing counts are available
	private boolean trimmed_frequents=false;
    
    public HashMap<String, HashMap<String, Double>> getCounts() {
		return counts;
	}

	public void setCounts(HashMap<String, HashMap<String, Double>> counts) {
		this.counts = counts;
	}

	public HashMap<String, Double> getUnigramCounts() {
		
		if(trimmed_frequents) {
			return unigramCounts;
		}
		trimmed_frequents = true;
		;
		if(unigramCounts.size()>0) {
			List<Entry<String, Double>> greatest = findGreatest(unigramCounts, Math.floor(GlobalConstants.CLEANSING_THRESHOLD*unigramCounts.size()));
			unigramCounts.clear();
			Iterator<Entry<String, Double>> it = greatest.iterator();
			while(it.hasNext()) {
				Entry<String, Double> entry = it.next();
				unigramCounts.put(entry.getKey(), entry.getValue());
				;
			}
		};
		
		
		
		return unigramCounts;
	}

	 private static <K, V extends Comparable<? super V>> List<Entry<K, V>>  findGreatest(Map<K, V> map, double n)
	 {
	     Comparator<? super Entry<K, V>> comparator = 
	         new Comparator<Entry<K, V>>()
	     {
	         @Override
	         public int compare(Entry<K, V> e0, Entry<K, V> e1)
	         {
	             V v0 = e0.getValue();
	             V v1 = e1.getValue();
	             return v0.compareTo(v1);
	         }
	     };
	     PriorityQueue<Entry<K, V>> highest = 
	         new PriorityQueue<Entry<K,V>>((int) n, comparator);
	     for (Entry<K, V> entry : map.entrySet())
	     {
	         highest.offer(entry);
	         while (highest.size() > n)
	         {
	             highest.poll();
	         }
	     }
	
	     List<Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
	     while (highest.size() > 0)
	     {
	         result.add(highest.poll());
	     }
	     return result;
	 }

	public void setUnigramCounts(HashMap<String, Double> unigramCounts) {
		this.unigramCounts = unigramCounts;
	}

	public static Bigram trainNGram(HashMap<String, ArrayList<DescriptionFetchRow>> dESCS,BinaryClassificationParameters config) throws SQLException
    {

		Boolean alphabetOnlyParameter = config.getClassif_keepAlpha() || config.getPreclassif_keepAlpha();
		Boolean decodeParameter = config.getClassif_cleanChar() || config.getPreclassif_cleanChar();
		Boolean SkipCorpusBuilding = !(config.getClassif_cleanAbv() || config.getPreclassif_cleanAbv() || config.getClassif_cleanSpell() || config.getPreclassif_cleanSpell());
		
		Bigram b = new Bigram();
		if(!SkipCorpusBuilding) {
			NgramParser p = new NgramParser(dESCS,alphabetOnlyParameter, decodeParameter);
	        HashSet<String> set = p.parse();
	        
	        b = new Bigram(set);
	        b.train();
	        
	       
		}
        
		
		 return b;
        //;

        //;
        
        //NgramParser test = new NgramParser(args[1], true);
        //HashSet<String> testset = test.parse();
        //;
    }
    
    public Bigram(Set<String> samples)
    {
        this.samples = samples;
        this.counts = new HashMap<String, HashMap<String, Double>>();
        this.unigramCounts = new HashMap<String, Double>();
        
        this.wordSet = new HashSet<String>();
        
        this.numberOfBigramsWithCount = new HashMap<Double, Double>();
    }
    
    public Bigram() {
		this.samples=new HashSet<String>();
		this.counts = new HashMap<String, HashMap<String, Double>>();
        this.unigramCounts = new HashMap<String, Double>();
        
        this.wordSet = new HashSet<String>();
        
        this.numberOfBigramsWithCount = new HashMap<Double, Double>();
	}

	public void train()
    {
        // Regexp to match words (starting with optional apos) or any punctuation (with probably extra escaping)
        String regexp = "('?\\w+|\\p{Punct})";
        Pattern pattern = Pattern.compile(regexp);
        for (String sample : samples) {
            Matcher matcher = pattern.matcher(sample);
            String previousWord = START; // originally set to beginning-of-sentence marker
            while (matcher.find()) {
                // Set unigram counts (for word1)
                double unigramCount = 0.0;
                if (unigramCounts.containsKey(previousWord)) {
                    unigramCount = unigramCounts.get(previousWord);
                }
                unigramCounts.put(previousWord, unigramCount+1.0);
                
                // Get the new match (word2)
                String match = matcher.group();
                wordSet.add(match);
                
                // Get access to (or create) the count map for word1.
                HashMap<String, Double> innerCounts;
                if (counts.containsKey(previousWord)) {
                    innerCounts = counts.get(previousWord);
                } else {
                    innerCounts = new HashMap<String, Double>();
                    counts.put(previousWord, innerCounts);
                }
                
                // Add to the size of the training set for gt-smoothing
                numTrainingBigrams += 1;
                
                // Set bigram counts
                double count = 0.0;
                if (innerCounts.containsKey(match)) {
                    count = innerCounts.get(match);
                    
                    // Decrement the number of bigrams with old count for gt-smoothing
                    //numberOfBigramsWithCount.put(count,
                    //    numberOfBigramsWithCount.get(count) - 1.0);
                }
                innerCounts.put(match, count+1.0);
                /*
                // Increment the number of bigrams with the new count for gt-smoothing
                if (!numberOfBigramsWithCount.containsKey(count+1.0)) {
                    numberOfBigramsWithCount.put(count+1.0, 1.0);
                } else {
                    numberOfBigramsWithCount.put(count+1.0,
                        numberOfBigramsWithCount.get(count+1.0) + 1.0);
                }
                */
                // Update previousWord
                previousWord = match;
            }
        }
        
        vocabSize = wordSet.size();
    }
    
    public double count(String word1, String word2)
    {
        if (counts.containsKey(word1) && counts.get(word1).containsKey(word2)) {
            return counts.get(word1).get(word2);
        }
        return 0.0;
    }
    
    public double unigramCount(String word)
    {
        if (unigramCounts.containsKey(word)) {
            return unigramCounts.get(word);
        }
        return 0.0;
    }
    
    public double unsmoothedProbability(String word1, String word2)
    {
        if (counts.containsKey(word1)) {
            if (counts.get(word1).containsKey(word2)) {
                return counts.get(word1).get(word2) / unigramCounts.get(word1);
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
    
    public double addOneSmoothedProbability(String word1, String word2)
    {
        // (count(word1 word2) + 1) / (count(word1) + V)
        return (count(word1, word2) + 1.0) / (unigramCount(word1) + vocabSize);
    }
    
    public double goodTuringSmoothedProbability(String word1, String word2)
    {
        if (!goodTuringCountsAvailable) {
            ;
            makeGoodTuringCounts();
            ;
        }
        
        // If this bigram has occurred, return good turing probability
        double gtcount = count(word1, word2);
        if (gtcount > 0.0) {
            return gtcount / unigramCount(word1);
        }
        // Otherwise, return N1/N as per book (page 101?)
        return numberOfBigramsWithCount.get(1.0) / numTrainingBigrams;
    }
    
    public void makeGoodTuringCounts()
    {
        // Generate good turing counts
        for (String word1 : counts.keySet()) {
            HashMap<String, Double> innerMap = counts.get(word1);
            double unigramCount = 0;
            for (String word2 : innerMap.keySet()) {
                double count = innerMap.get(word2);
                if (!numberOfBigramsWithCount.containsKey(count+1)) {
                    numberOfBigramsWithCount.put(count+1, 0.0);
                }
                // c* = (c+1) * N(c+1) / N(c)
                double newCount = (count + 1)*(numberOfBigramsWithCount.get(count+1.0))/(numberOfBigramsWithCount.get(count));
                innerMap.put(word2, newCount);
                unigramCount += newCount;
            }
            unigramCounts.put(word1, unigramCount);
        }
        goodTuringCountsAvailable = true;
    }
    
    public void showCounts()
    {
        for (String word1 : counts.keySet()) {
            for (String word2 : counts.get(word1).keySet()) {
                ;
            }
        }
    }

    public String getSentence() {
        String sentence = "";
        String currentWord = START;
        String nextWord = START;
        //creates a sentence until a period is found
        //(400 is jic it doesn't find a period)
        while (!currentWord.equals(".") && sentence.length() <= 400) {
            Set<String> keySet = counts.get(currentWord).keySet();
            // rand is like a random dart thrown onto a dart board
            // multiplied by totalCount for precision (since P(word) is small)
            double rand = Math.random() * unigramCounts.get(currentWord);
            Iterator<String> i = keySet.iterator();
            //looking at all the words to see where the dart lands
            while (i.hasNext() && rand >= 0) {
                nextWord = i.next();
                rand -= (double) counts.get(currentWord).get(nextWord);
            }
            currentWord = nextWord;
            sentence += nextWord + " ";
        }
        return sentence;
    }

    public double perplexity(Set<String> testSamples) {
        float product = 1;
        int wordCount = 0;
        Stack<Double> products = new Stack<Double>();
        String regexp = "('?\\w+|\\p{Punct})";
        Pattern pattern = Pattern.compile(regexp);

        // counting number of words in test set
        for (String sample : testSamples) {
            Matcher matcher = pattern.matcher(sample);
            String previousWord = START;
            while (matcher.find()) {
                String match = matcher.group();
                
                products.push(goodTuringSmoothedProbability(previousWord, match));
                wordCount++;
                
                // Update previousWord
                previousWord = match;
            }
        }

        // computing the necessary exponent
        double power = 1.0 / wordCount;

        // computing perplexity based on probabilities
        while (!products.empty()) {
            product *= Math.pow(products.pop(), power);
        }
        return 1 / product;
    }
}