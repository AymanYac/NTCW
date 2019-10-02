package service;



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfusionMatrixReader {
    
    final static String DATAFILE_LOC = "/scripts/confusion_matrix.txt";
    final private HashMap<String,Integer> confusionMatrix = new HashMap<>();
    final private HashMap<String,Integer> countMatrix = new HashMap<>();
    public ConfusionMatrixReader() 
    {
        try {
            readConfusionMatrix();
        } catch (Exception ex) {
        	ex.printStackTrace(System.err);
            Logger.getLogger(ConfusionMatrixReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readConfusionMatrix() 
            throws FileNotFoundException, IOException, URISyntaxException
    {
    	FileInputStream fis;
        fis = new FileInputStream(Paths.get(getClass().getResource(DATAFILE_LOC).toURI()).toString());
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        
        while( in.ready() )
        {
            String line = in.readLine();
            int space = line.lastIndexOf(' ');
            String keys = line.substring(0,space);
            try {
                int count = Integer.parseInt(line.substring(space+1));
                confusionMatrix.put(keys, count);

                String key = keys.substring(0,keys.indexOf('|'));  
                Integer value = countMatrix.get(key);
                if (value==null) {
                    value = 0;
                }
                countMatrix.put(key, value+count);
            } catch(NumberFormatException e) {
                System.err.println("problems with string <"+line+">");
            }
        }
    }
    
    /**
     * Returns the count for the pair <error>|<correct> in the confusion
     * matrix, e.g. "c|ct" is 36
     * 
     * @param error
     * @param correct
     * @return
     */
    public int getConfusionCount(String error, String correct) 
    {
    	error = error.toLowerCase();
    	correct = correct.toLowerCase();
        Integer count = confusionMatrix.get(error+"|"+correct);
        count = count==null?0:count;
        return count + 1; // smoothing
    }
    
    public int getCharCount(String sequence) {
    	sequence = sequence.toLowerCase();
        Integer count = countMatrix.get(sequence);
        count = count==null?0:count;
        return count + 1; // smoothing
    }
}