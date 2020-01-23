package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import controllers.Char_description;
import model.CharValueTextSuggestion;
import model.ClassCharacteristic;
import model.GlobalConstants;
import transversal.generic.Tools;

public class TranslationServices {
	
	public static HashSet<String> knownLanguageIDs;
	
	public static HashMap<String,ArrayList<CharValueTextSuggestion>> textEntries = new HashMap<String,ArrayList<CharValueTextSuggestion>>();

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

	public static ArrayList<CharValueTextSuggestion> getTextEntriesForActiveCharOnLanguages(Char_description parent, String sourceLanguage, String targetLanguage) {
		String active_class = parent.tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment().split("&&&")[0];
		int active_idx = parent.tableController.selected_col;
		active_idx%=parent.tableController.active_characteristics.get(active_class).size();
		ClassCharacteristic active_char = parent.tableController.active_characteristics.get(active_class).get(active_idx);
		return new ArrayList<CharValueTextSuggestion>(
				textEntries.get(active_char.getCharacteristic_id()).parallelStream().map(s->new CharValueTextSuggestion(s,sourceLanguage,targetLanguage))
							.filter(e->e.hasSourceLanguageValue()).collect(Collectors.toList())
							);
	}

	public static void addTextEntry(String textValues, String characteristic_id) {
		
		try {
			knownLanguageIDs = (knownLanguageIDs!=null)?knownLanguageIDs:TranslationServices.loadKnownLanguageIDs();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<CharValueTextSuggestion> tmp = textEntries.get(characteristic_id);
		tmp = (tmp!=null)?tmp:new ArrayList<CharValueTextSuggestion>();
		
		CharValueTextSuggestion sugg = new CharValueTextSuggestion();
		for(String value:textValues.split("&&&")) {
			Optional<String> languageID = knownLanguageIDs.parallelStream().filter(l->value.endsWith(l)).findAny();
			if(languageID.isPresent()) {
				sugg.addValueInLanguage(value.substring(0, value.length()-languageID.get().length()),languageID);
			}else {
				sugg.addValueInLanguage(value,languageID);
			}
		}
		tmp.add(sugg);
		textEntries.put(characteristic_id, tmp);
		
	}

	private static HashSet<String> loadKnownLanguageIDs() throws ClassNotFoundException, SQLException {
		HashSet<String> ret = new HashSet<String>();
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select language_id from administration.languages");
		while(rs.next()) {
			ret.add(rs.getString("language_id"));
		}
		
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}

    
}