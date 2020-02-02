package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import controllers.Char_description;
import model.CharValueTextSuggestion;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.GlobalConstants;
import transversal.language_toolbox.Unidecode;

public class TranslationServices {
	
	public static HashSet<String> knownLanguageIDs;
	
	public static HashMap<String,HashSet<CharValueTextSuggestion>> textEntries = new HashMap<String,HashSet<CharValueTextSuggestion>>();

	private static HashMap<String,String> Data2UserTermsDico;
	private static HashMap<String,String> User2DataTermsDico;

	private static HashMap<String,ArrayList<CharacteristicValue>> Data2ValuesDico;
	private static HashMap<String,ArrayList<CharacteristicValue>> User2ValuesDico;
	
	
	
    public static String webTranslate(String langFrom, String langTo, String text) throws IOException {
        
    	try{
    		int script_url_choice =  Math.random()>0.5?0:0;
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

	public static ArrayList<CharValueTextSuggestion> getTextEntriesForActiveCharOnLanguages(Char_description parent, boolean isDataField) {
		
		try{
			String active_class = parent.tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment().split("&&&")[0];
			int active_idx = parent.tableController.selected_col;
			active_idx%=parent.tableController.active_characteristics.get(active_class).size();
			ClassCharacteristic active_char = parent.tableController.active_characteristics.get(active_class).get(active_idx);
			return new ArrayList<CharValueTextSuggestion>(
					textEntries.get(active_char.getCharacteristic_id()).parallelStream().filter(
							s->s.hasSourceValue(isDataField)).collect(Collectors.toList()));
		}catch(Exception V) {
			return new ArrayList<CharValueTextSuggestion>();
		}
		
		
	}


	public static void beAwareOfNewValue(CharacteristicValue pattern_value, ClassCharacteristic charac) {
		if(charac.getIsNumeric()) {
			return;
		}
		
		if(charac.getIsTranslatable()) {
			String dataVal = pattern_value.getDataLanguageValue();
			String userVal = pattern_value.getUserLanguageValue();
			Unidecode unidecode = Unidecode.toAscii();
			
			Boolean conflictingLinks = createTranslationLink(dataVal,userVal);
			
			if(conflictingLinks!=null) {
				if(conflictingLinks) {
					System.out.println("Translation conflict on pair <"+dataVal+">-<"+userVal+">, known: "+Data2UserTermsDico.get(unidecode.decodeAndTrim(dataVal.toLowerCase())));
				}else {
					//Link is known, no conflicts
					addThisValueToTheCharKnownSets(pattern_value, charac,true);
				}
			}else {
				//Link created or no link to create (one of the values is null)
				addThisValueToTheCharKnownSets(pattern_value, charac,true);
			}
			
			//Add this charval to known values carrying this dataVal
			if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0) {
				Data2ValuesDico = (Data2ValuesDico!=null)?Data2ValuesDico:new HashMap<String,ArrayList<CharacteristicValue>>();
				try {
					Data2ValuesDico.get(unidecode.decode(dataVal.toLowerCase())).add(pattern_value);
				}catch(Exception V) {
					ArrayList<CharacteristicValue> tmp = new ArrayList<CharacteristicValue>();
					tmp.add(pattern_value);
					Data2ValuesDico.put(unidecode.decode(dataVal.toLowerCase()), tmp);
				}
			}
			
			//Add this charval to known values carrying this dataVal
			if(userVal!=null && userVal.replaceAll(" ", "").length()>0) {
				
				User2ValuesDico = (User2ValuesDico!=null)?User2ValuesDico:new HashMap<String,ArrayList<CharacteristicValue>>();
				try {
					User2ValuesDico.get(unidecode.decode(userVal.toLowerCase())).add(pattern_value);
				}catch(Exception V) {
					ArrayList<CharacteristicValue> tmp = new ArrayList<CharacteristicValue>();
					tmp.add(pattern_value);
					User2ValuesDico.put(unidecode.decode(userVal.toLowerCase()), tmp);
				}
			}
		}else {
			//Charac is not translatable only add this value for autocompletion abilities
			addThisValueToTheCharKnownSets(pattern_value, charac,false);
			
		}
		
		
		
		
		
	}

	private static void addThisValueToTheCharKnownSets(CharacteristicValue pattern_value, ClassCharacteristic charac , boolean logUserLanguageValue) {
		CharValueTextSuggestion tmp = new CharValueTextSuggestion("DATA","USER");
		tmp.addValueInLanguage("DATA", pattern_value.getDataLanguageValue());
		tmp.addValueInLanguage("USER", pattern_value.getUserLanguageValue());
		
		try {
			textEntries.get(charac.getCharacteristic_id()).add(tmp);
		}catch(Exception V) {
			HashSet<CharValueTextSuggestion> arr = new HashSet<CharValueTextSuggestion>();
			arr.add(tmp);
			textEntries.put(charac.getCharacteristic_id(), arr);
		}
		if(logUserLanguageValue) {
			tmp = new CharValueTextSuggestion("USER","DATA");
			tmp.addValueInLanguage("DATA", pattern_value.getDataLanguageValue());
			tmp.addValueInLanguage("USER", pattern_value.getUserLanguageValue());
			try {
				textEntries.get(charac.getCharacteristic_id()).add(tmp);
			}catch(Exception V) {
				HashSet<CharValueTextSuggestion> arr = new HashSet<CharValueTextSuggestion>();
				arr.add(tmp);
				textEntries.put(charac.getCharacteristic_id(), arr);
			}
		}
		
	}

	private static Boolean createTranslationLink(String dataVal, String userVal) {
		Data2UserTermsDico = (Data2UserTermsDico!=null)?Data2UserTermsDico:new HashMap<String,String>();
		User2DataTermsDico = (User2DataTermsDico!=null)?User2DataTermsDico:new HashMap<String,String>();
		
		
		Unidecode unidecode = Unidecode.toAscii();
		if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0 &&
				userVal!=null && userVal.replaceAll(" ", "").length()>0) {
			
			String dataValTrimmed = unidecode.decodeAndTrim(dataVal.toLowerCase());
			String userValTrimmed = unidecode.decodeAndTrim(userVal.toLowerCase());
			
			try{
				boolean matched = unidecode.decodeAndTrim(Data2UserTermsDico.get(dataValTrimmed).toLowerCase()).equals(userValTrimmed);
				return !matched;
			}catch(Exception V) {
				//dataVal not known
				Data2UserTermsDico.put(dataValTrimmed, userVal);
				User2DataTermsDico.put(userValTrimmed, dataVal);
				return null;
			}
			
			
		}else {
			//One of the values is null no need to store translation link
			return null;
		}
		
	}

    
}