package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;

import model.GlobalConstants;

public class Translator {

    
    public static String translate(String langFrom, String langTo, String text) throws IOException {
        
    	try{
    		int script_url_choice =  Math.random()>0.5?0:1;
    		String urlStr = GlobalConstants.GOOGLE_TRANSLATE_SCRIPTS[script_url_choice] +
                    "?q=" + URLEncoder.encode(text, "UTF-8") +
                    "&target=" + langTo +
                    "&source=" + langFrom;
            URL url = new URL(urlStr);
            StringBuilder response = new StringBuilder();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            
    		
    		
        	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        	 String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 response.append(inputLine);
             }
             in.close();
             return StringEscapeUtils.unescapeHtml(response.toString());
        }catch(Exception V) {
        	V.printStackTrace(System.err);
        	return "";
        }
       
    }

}