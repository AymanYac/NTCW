package service;

import controllers.Char_description;
import javafx.util.Pair;
import model.CaracteristicValue;
import model.CharValueTextSuggestion;
import model.ClassCaracteristic;
import model.GlobalConstants;
import org.apache.commons.lang.StringEscapeUtils;
import transversal.language_toolbox.Unidecode;

import javax.xml.crypto.Data;
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

public class TranslationServices {
	
	public static HashSet<String> knownLanguageIDs;
	
	public static HashMap<String,HashSet<CharValueTextSuggestion>> textEntries = new HashMap<String,HashSet<CharValueTextSuggestion>>();

	public static HashMap<String,String> Data2UserTermsDico;
	public static HashMap<String,String> User2DataTermsDico;

	private static HashMap<String,ArrayList<CaracteristicValue>> Data2ValuesDico;
	private static HashMap<String,ArrayList<CaracteristicValue>> User2ValuesDico;
	
	
	
    public static String webTranslate(String langFrom, String langTo, String text) throws IOException {
        
    	try{
    		//int script_url_choice =  Math.random()>0.5?0:0;
    		int script_url_choice =  0;
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
        	return "";
        }
       
    }

	public static ArrayList<CharValueTextSuggestion> getTextEntriesForActiveCharOnLanguages(Char_description parent, boolean isDataField) {
		
		try{
			String active_class = parent.tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0];
			int active_idx = parent.tableController.selected_col;
			active_idx%=CharValuesLoader.active_characteristics.get(active_class).size();
			ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(active_class).get(active_idx);
			return new ArrayList<CharValueTextSuggestion>(
					textEntries.get(active_char.getCharacteristic_id()).parallelStream().filter(
							s->s.hasSourceValue(isDataField)).collect(Collectors.toList()));
		}catch(Exception V) {
			return new ArrayList<CharValueTextSuggestion>();
		}
		
		
	}


	public static void beAwareOfNewValue(CaracteristicValue pattern_value, ClassCaracteristic charac) {
		if(charac.getIsNumeric()) {
			return;
		}
		
		if(charac.getIsTranslatable()) {
			String dataVal = pattern_value.getDataLanguageValue();
			String userVal = pattern_value.getUserLanguageValue();
			Unidecode unidecode = Unidecode.toAscii();
			
			Boolean conflictingLinks = createVerifiedDicoTranslationLink(dataVal,userVal,false);
			
			if(conflictingLinks!=null) {
				if(conflictingLinks) {
					
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
				addToDataValues(dataVal, unidecode, pattern_value);
			}
			
			//Add this charval to known values carrying this dataVal
			if(userVal!=null && userVal.replaceAll(" ", "").length()>0) {
				addToUserValues(userVal,unidecode,pattern_value);
			}
		}else {
			//Charac is not translatable only add this value for autocompletion abilities
			addThisValueToTheCharKnownSets(pattern_value, charac,false);
			
		}
		
		
		
		
		
	}

	private static void addToUserValues(String userVal, Unidecode unidecode, CaracteristicValue pattern_value) {
		
		User2ValuesDico = (User2ValuesDico!=null)?User2ValuesDico:new HashMap<String,ArrayList<CaracteristicValue>>();
		try {
			User2ValuesDico.get(unidecode.decode(userVal.toLowerCase())).add(pattern_value);
		}catch(Exception V) {
			ArrayList<CaracteristicValue> tmp = new ArrayList<CaracteristicValue>();
			tmp.add(pattern_value);
			User2ValuesDico.put(unidecode.decode(userVal.toLowerCase()), tmp);
		}
	}

	private static void addToDataValues(String dataVal,Unidecode unidecode, CaracteristicValue pattern_value) {
		Data2ValuesDico = (Data2ValuesDico!=null)?Data2ValuesDico:new HashMap<String,ArrayList<CaracteristicValue>>();
		try {
			Data2ValuesDico.get(unidecode.decode(dataVal.toLowerCase())).add(pattern_value);
		}catch(Exception V) {
			ArrayList<CaracteristicValue> tmp = new ArrayList<CaracteristicValue>();
			tmp.add(pattern_value);
			Data2ValuesDico.put(unidecode.decode(dataVal.toLowerCase()), tmp);
		}
	}
	
	private static void cleanValueDicts() {
		
		Unidecode unidec = Unidecode.toAscii();
		
		User2ValuesDico = (User2ValuesDico!=null)?User2ValuesDico:new HashMap<String,ArrayList<CaracteristicValue>>();
		User2ValuesDico.replaceAll((k,v)->cleanUserValueArray(v,k,unidec));
		
		Data2ValuesDico = (Data2ValuesDico!=null)?Data2ValuesDico:new HashMap<String,ArrayList<CaracteristicValue>>();
		Data2ValuesDico.replaceAll((k,v)->cleanDataValueArray(v,k,unidec));
	}

	private static ArrayList<CaracteristicValue> cleanDataValueArray(ArrayList<CaracteristicValue> v, String k, Unidecode unidec) {
		
		final String key = unidec.decodeAndTrim(k).toLowerCase();
		return new ArrayList<CaracteristicValue>(
				v.parallelStream().filter(cv->ValueisCleanWithRespectToKey(cv.getDataLanguageValue(),unidec,key)).collect(Collectors.toList())
				);
	}

	private static Boolean ValueisCleanWithRespectToKey(String valueInLanguage, Unidecode unidec, String key) {
		try{
			return unidec.decodeAndTrim(valueInLanguage).toLowerCase().equals(key);
		}catch(Exception V) {
			return true;
		}
	}

	private static ArrayList<CaracteristicValue> cleanUserValueArray(ArrayList<CaracteristicValue> v, String k, Unidecode unidec) {
		final String key = unidec.decodeAndTrim(k).toLowerCase();
		return new ArrayList<CaracteristicValue>(
				v.parallelStream().filter(cv->ValueisCleanWithRespectToKey(cv.getUserLanguageValue(),unidec,key)).collect(Collectors.toList())
				);
	}

	public static void addThisValueToTheCharKnownSets(CaracteristicValue pattern_value, ClassCaracteristic charac , boolean logUserLanguageValue) {
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

	public static Pair<String,String> createUnverifiedDicoTranslationLink(String dataVal, String userVal) {
    	System.out.println("XXXXXXX Unverified link "+dataVal+"/"+userVal);
		Data2UserTermsDico = (Data2UserTermsDico!=null)?Data2UserTermsDico:new HashMap<String,String>();
		User2DataTermsDico = (User2DataTermsDico!=null)?User2DataTermsDico:new HashMap<String,String>();


		Unidecode unidecode = Unidecode.toAscii();
		if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0 &&
				userVal!=null && userVal.replaceAll(" ", "").length()>0) {

			String dataValTrimmed = unidecode.decodeAndTrim(dataVal.toLowerCase());
			String userValTrimmed = unidecode.decodeAndTrim(userVal.toLowerCase());
			boolean matched;
			if(Data2UserTermsDico.containsKey(dataValTrimmed)){
				//dataval known
				if(Data2UserTermsDico.get(dataValTrimmed)!=null){
					//dataval translation known
					matched = userValTrimmed.equals(unidecode.decodeAndTrim(Data2UserTermsDico.get(dataValTrimmed).toLowerCase()));
					if(!matched){
						System.out.println("conflict with known userval for input dataval");
						return  new Pair<String,String>(dataVal,Data2UserTermsDico.get(dataValTrimmed));
					}
					System.out.println("no conflict with known userval for input dataval");
					return  null;
				}else{
					//dataVal translation unknown
					System.out.println("conflict with unknown userval for unput dataval");
					return  new Pair<String,String>(dataVal,null);
				}

			}else {
				//dataVal not known
				if(User2DataTermsDico.containsKey(userValTrimmed)){
					//The userval translation is known or the userval has an explicit no translation : conflict
					System.out.println("conflict with known dataval for input userval");
					return  new Pair<String,String>(User2DataTermsDico.get(userValTrimmed),userVal);
				}else{
					System.out.println("new entry");
					//New entry
					Data2UserTermsDico.put(dataValTrimmed, userVal);
					User2DataTermsDico.put(userValTrimmed, dataVal);
					return null;
				}

			}


		}else {
			//One of the values is null
			if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0) {
				//the userval is null
				String dataValTrimmed = unidecode.decodeAndTrim(dataVal.toLowerCase());
				if(Data2UserTermsDico.get(dataValTrimmed)!=null){
					return new Pair<String, String>(dataVal,Data2UserTermsDico.get(dataValTrimmed));
				}
				Data2UserTermsDico.put(dataValTrimmed, null);
				return  null;
			}
			if(userVal!=null && userVal.replaceAll(" ", "").length()>0) {
				String userValTrimmed = unidecode.decodeAndTrim(userVal.toLowerCase());
				if(User2DataTermsDico.get(userValTrimmed)!=null){
					return new Pair<String, String>(User2DataTermsDico.get(userValTrimmed),userVal);
				}
				User2DataTermsDico.put(userValTrimmed, null);
				return null;
			}
			return null;
		}

	}
	public static Boolean createVerifiedDicoTranslationLink(String dataVal, String userVal, boolean forceUpdate) {
		Data2UserTermsDico = (Data2UserTermsDico!=null)?Data2UserTermsDico:new HashMap<String,String>();
		User2DataTermsDico = (User2DataTermsDico!=null)?User2DataTermsDico:new HashMap<String,String>();
		
		
		Unidecode unidecode = Unidecode.toAscii();
		if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0 &&
				userVal!=null && userVal.replaceAll(" ", "").length()>0) {
			
			String dataValTrimmed = unidecode.decodeAndTrim(dataVal.toLowerCase());
			String userValTrimmed = unidecode.decodeAndTrim(userVal.toLowerCase());
			
			try{
				boolean matched = unidecode.decodeAndTrim(Data2UserTermsDico.get(dataValTrimmed).toLowerCase()).equals(userValTrimmed);
				//matched = matched && unidecode.decodeAndTrim(User2DataTermsDico.get(userValTrimmed).toLowerCase()).equals(dataValTrimmed);
				if(forceUpdate) {
					Data2UserTermsDico.put(dataValTrimmed, userVal);
					User2DataTermsDico.put(userValTrimmed, dataVal);
				}
				return !matched;
			}catch(Exception V) {
				//dataVal not known
				Data2UserTermsDico.put(dataValTrimmed, userVal);
				User2DataTermsDico.put(userValTrimmed, dataVal);
				return null;
			}
			
			
		}else {
			//One of the values is null
			if(dataVal!=null && dataVal.replaceAll(" ", "").length()>0) {
				String dataValTrimmed = unidecode.decodeAndTrim(dataVal.toLowerCase());
				Data2UserTermsDico.put(dataValTrimmed, null);
			}
			if(userVal!=null && userVal.replaceAll(" ", "").length()>0) {
				String userValTrimmed = unidecode.decodeAndTrim(userVal.toLowerCase());
				User2DataTermsDico.put(userValTrimmed, null);
			}
			return null;
		}
		
	}

	public static void updateTranslation(CharValueTextSuggestion result, boolean keyIsData, String newValue) {
		
		CharValueTextSuggestion result_for_deletion = result.flipIsDataFieldSuggestion().flipIsDataFieldSuggestion();
		removeTranslation(result_for_deletion);
		
		updateTextEntriesTranslationSuggestions(result.getSource_value(),keyIsData,newValue);
		//removeTranslationLink(result.getTarget_value(),!keyIsData);
		//updateTranslationLink(result.getTarget_value(),!keyIsData,result.getSource_value());
		updateTextEntriesTranslationSuggestions(newValue,!keyIsData,result.getSource_value());
		
		updateTranslatedValues(result.getSource_value(),keyIsData,newValue);
		//removeTranslationValues(result.getTarget_value(),!keyIsData);
		//updateTranslatedValues(result.getTarget_value(),!keyIsData,result.getSource_value());
		updateTranslatedValues(newValue,!keyIsData,result.getSource_value());
		
		
		textEntries.replaceAll((k,v)->new HashSet(v));
		/*textEntries.forEach((k,v)->{
			textEntries.put(k, new HashSet(v));
		});*/
	}

	public static void removeTranslation(CharValueTextSuggestion result_original) {
		
		CharValueTextSuggestion result = result_original.flipIsDataFieldSuggestion().flipIsDataFieldSuggestion();
		updateTextEntriesTranslationSuggestions(result.getSource_value(),result.isDataFieldSuggestion(),null);
		updateTranslatedValues(result.getSource_value(),result.isDataFieldSuggestion(),null);
		
		if(!result.isDataFieldSuggestion()) {
			createVerifiedDicoTranslationLink(result.getTarget_value(),null,true);
		}else {
			createVerifiedDicoTranslationLink(null,result.getTarget_value(),true);
		}
		
		textEntries.replaceAll((k,v)->new HashSet(v));
		/*textEntries.forEach((k,v)->{
			textEntries.put(k, new HashSet(v));
		});*/
	}

	private static void updateTranslatedValues(String key, boolean keyIsData, String newValue) {
		
		
		Unidecode unidecode = Unidecode.toAscii();
		if(keyIsData) {
			try {
				Data2ValuesDico.get(unidecode.decode(key.toLowerCase())).parallelStream().forEach(
						v->{
							
							v.setUserLanguageValue(newValue);
							addToUserValues(newValue, unidecode, v);
						});
			}catch(Exception V) {

			}
		}else {
			try {
				User2ValuesDico.get(unidecode.decode(key.toLowerCase())).parallelStream().forEach(
						v->{
							
							v.setDataLanguageValue(newValue);
							addToDataValues(newValue, unidecode, v);
							
						});
			}catch(Exception V) {
				
			}
		}
		
		cleanValueDicts();
		
	}

	
	
	

	private static void updateTextEntriesTranslationSuggestions(String key, boolean keyIsData, String newValue) {
		
		createVerifiedDicoTranslationLink(key,newValue,true);
		
		textEntries.values().parallelStream().forEach(a->a.stream().forEach(s->{
			
			if(keyIsData) {
				if(s.isDataFieldSuggestion()) {
					if(s.getSource_value() != null && s.getSource_value().equals(key)) {
						s.setTarget_value(newValue);
						
					}
				}else {
					if(s.getTarget_value() != null && s.getTarget_value().equals(key)) {
						s.setSource_value(newValue);
						
					}
				}
			}else {
				if(!s.isDataFieldSuggestion()) {
					if(s.getSource_value() != null && s.getSource_value().equals(key)) {
						s.setTarget_value(newValue);
						
					}
				}else {
					if(s.getTarget_value() != null && s.getTarget_value().equals(key)) {
						s.setSource_value(newValue);
						
					}
				}
			}
			
		}));
	}

	public static String getEntryTranslation(String source_value, boolean sourceInDataLanguage) {
		try {
			Unidecode unidecode = Unidecode.toAscii();
			if(sourceInDataLanguage) {
				return Data2UserTermsDico.get(unidecode.decodeAndTrim(source_value).toLowerCase());
			}else {
				return User2DataTermsDico.get(unidecode.decodeAndTrim(source_value).toLowerCase());
			}
		}catch(Exception V) {
			return null;
		}
	}

	

    
}