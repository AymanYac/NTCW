package service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import controllers.Char_description;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.UserAccount;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

public class CharPatternServices {
	
	private static HashMap<String,LinkedHashSet<String>> specialwords;

	public static void scanSelectionForPatternDetection(Char_description parent, ClassCharacteristic active_char) {
		
		Unidecode unidecode = Unidecode.toAscii();
		String selected_text = "";
		selected_text = parent.ld.getSelectedText();
		if(selected_text.length()==0) {
			selected_text = parent.ld_translated.getSelectedText();
			if(selected_text.length()==0) {
				selected_text=parent.sd.getSelectedText();
				if(selected_text.length()==0) {
					selected_text=parent.sd_translated.getSelectedText();
				}
			}
		}
		System.out.println("Processing selected text ::: "+selected_text);
		//The correction of the selection without separator is equal to one of the values ("VAL") already listed for this characteristic without separator?
		//#Let's correct the selection
		String corrected_text = WordUtils.CORRECT(selected_text);
		//Let's clean the selection from all separators and do the same thing for the known values
		String[] SEPARATORS = new String[] {",",":",".","-"," ","/"};
		
		for(CharacteristicValue known_value:active_char.getKnownValues()) {
			String separator_free_known = known_value.getDataLanguageValue();
			String separator_free_selected = corrected_text;
			for(String sep:SEPARATORS) {
				separator_free_known = separator_free_known.replaceAll(sep, "");
				separator_free_selected = separator_free_selected.replaceAll(sep, "");
				if( unidecode.decode(separator_free_selected).toUpperCase().equals(
						unidecode.decode(separator_free_known).toUpperCase()) ){
//					If YES															
//					Value = "VAL"														
//					Rule = [Selection]
					parent.sendPatternValue(known_value);
					parent.sendPatternRule("["+selected_text+"]");
					return;
				}
			}
		}
														
//		If NO															
//			The characteristic is a TXT characteristic?														
//				If YES
		if(!active_char.getIsNumeric()) {
//			The characteristic is translatable?
//			If YES	
			if(active_char.getIsTranslatable()) {											
//				The selection includes one of the following characters: ":", "="? (e.g. "material: iron steel")										
//					If YES
				if(selected_text.contains(":") || selected_text.contains("=")) {
					String part_after_identifier;
					String part_before_identifier;
					if(selected_text.contains(":")) {
						part_before_identifier = selected_text.split(":")[0];
						part_after_identifier = selected_text.split(":")[1];
					}else {
						part_before_identifier = selected_text.split("=")[0];
						part_after_identifier = selected_text.split("=")[1];
					}
//					The correction of the part of the selection after the ":" or "=" is equal to one of the values ("VAL") already listed for this characteristic modulo separators?								
					String corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
					SEPARATORS = new String[] {",",":",".","-"," ","/"};
					
					for(CharacteristicValue known_value:active_char.getKnownValues()) {
						String separator_free_known = known_value.getDataLanguageValue();
						String separator_free_selected = corrected_part_after_identifier;
						for(String sep:SEPARATORS) {
							separator_free_known = separator_free_known.replaceAll(sep, "");
							separator_free_selected = separator_free_selected.replaceAll(sep, "");
							if( unidecode.decode(separator_free_selected).toUpperCase().equals(
									unidecode.decode(separator_free_known).toUpperCase()) ){
//								If YES							
//								Value = VAL						
//								Value (Pivot language) = PIVOTtranslation (VAL)						
//								Rule = "material"(|+1)["iron steel"]	
								parent.sendPatternValue(known_value);
								parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)["+part_after_identifier+"]");
								return;
							}
						}}		
//						If NO							
//							The correction of the part of the selection after the ":" or "=" is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
							corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
							SEPARATORS = new String[] {",",":",".","-"," ","/"};
							HashSet<CharacteristicValue> VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CharacteristicValue>();
							for(CharacteristicValue known_value:active_char.getKnownValues()) {
								String separator_free_known = known_value.getDataLanguageValue();
								String separator_free_selected = corrected_part_after_identifier;
								for(String sep:SEPARATORS) {
									separator_free_known = separator_free_known.replaceAll(sep, "");
									separator_free_selected = separator_free_selected.replaceAll(sep, "");
									if( unidecode.decode(separator_free_known).toUpperCase().contains(
											unidecode.decode(separator_free_selected).toUpperCase()) ){
										VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
									}}}
							if(VALUES_CONTAINING_AFTER_IDENTIFIER.size()==1) {
//								If YES					
//								Value = VAL			
								parent.sendPatternValue(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next());
//								Value (Pivot language) = PIVOTtranslation (VAL)				
//								Rule = Selection<"VAL">				
								parent.sendPatternRule(selected_text+"<\""+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+"\">");
								return;
							}else {
//								If NO					
//									The correction of the part of the selection after the ":" or "=" is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?				
//										If YES
								corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
								SEPARATORS = new String[] {",",":",".","-"," ","/"};
								VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CharacteristicValue>();
								for(CharacteristicValue known_value:active_char.getKnownValues()) {
									String separator_free_known = known_value.getUserLanguageValue();
									String separator_free_selected = corrected_part_after_identifier;
									for(String sep:SEPARATORS) {
										separator_free_known = separator_free_known.replaceAll(sep, "");
										separator_free_selected = separator_free_selected.replaceAll(sep, "");
										if( unidecode.decode(separator_free_known).toUpperCase().contains(
												unidecode.decode(separator_free_selected).toUpperCase()) ){
											VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
										}}}
								if(VALUES_CONTAINING_AFTER_IDENTIFIER.size()==1) {
//								
//											Value (Pivot language) = ENVAL		
//											Value = LOCALtranslation (ENVAL)
									parent.sendPatternValue(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next());
//									
//											Rule = Selection<LOCALtranslation("ENVAL")>	
									parent.sendPatternRule(selected_text+"<\""+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+"\">");
									return;
									
								}else {

//									If NO			
//										Value = Correction ("iron steel")
									CharacteristicValue new_value = new CharacteristicValue();
									new_value.setText_values(corrected_part_after_identifier+CharacteristicValue.dataLanguage);
									new_value.setParentChar(active_char);
									parent.sendPatternValue(new_value);
									parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)["+part_after_identifier+"]");
									return;
									
//										Rule = "material"(|+1)["iron steel"]
						
								}
								}
				}	
//					If NO (no : or = in the selection)									
//						The correction of the selection is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?
				corrected_text = WordUtils.CORRECT(selected_text);
				SEPARATORS = new String[] {",",":",".","-"," ","/"};
				HashSet<CharacteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
				for(CharacteristicValue known_value:active_char.getKnownValues()) {
					for(String known_value_sub_element:known_value.getDataLanguageValue().split(String.join("|", SEPARATORS))) {
						if( unidecode.decode(known_value_sub_element).toUpperCase().equals(
								unidecode.decode(corrected_text).toUpperCase()) ){
							VALUES_CONTAINING_SELECTION.add(known_value);
						}}}
				if(VALUES_CONTAINING_SELECTION.size()==1) {
//				
//							If YES							
//								Value = VAL						
//								Value (Pivot language) = PIVOTtranslation (VAL)
					parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//								Rule = [Selection]
					parent.sendPatternRule("["+selected_text+"]");
					return;
				}else {
//					If NO							
//					The correction of the selection is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?
					corrected_text = WordUtils.CORRECT(selected_text);
					SEPARATORS = new String[] {",",":",".","-"," ","/"};
					VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
					for(CharacteristicValue known_value:active_char.getKnownValues()) {
						for(String known_value_sub_element:known_value.getUserLanguageValue().split(String.join("|", SEPARATORS))) {
							if( unidecode.decode(known_value_sub_element).toUpperCase().equals(
									unidecode.decode(corrected_text).toUpperCase()) ){
								VALUES_CONTAINING_SELECTION.add(known_value);
							}}}
					if(VALUES_CONTAINING_SELECTION.size()==1) {
//					
//						If YES					
//							Value (Pivot language) = ENVAL				
//							Value = LOCALtranslation (ENVAL)
						parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//							Rule = Selection<LOCALtranslation("ENVAL")>
						parent.sendPatternRule(selected_text+"<"+VALUES_CONTAINING_SELECTION.iterator().next().getUserLanguageValue()+">");
						return;
					}else {
//						If NO					
//						Value = Correction (Selection)
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(corrected_text+CharacteristicValue.dataLanguage);
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//						Rule = [Selection]
						parent.sendPatternRule("["+selected_text+"]");
						return;
					}

				
					
					
				}								
			}
//			If NO (the char is not translatable)								
//			The selection contains at list 1 figure?
			int number_of_digits = selected_text.chars().mapToObj((c -> (char) c)).filter(c->Character.isDigit(c)).collect(Collectors.toList()).size();
//				If YES
			if(number_of_digits>0) {
//				The selection includes one of the following characters: ":", "="? (e.g. "abcd: ef12-gh34-ij56")	
				if(selected_text.split(":").length>1) {
//					If YES							
//					Value = ef12-gh34-ij56
					CharacteristicValue tmp = new CharacteristicValue();
					tmp.setText_values(selected_text.split(":")[1].trim());
					tmp.setParentChar(active_char);
					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]		
					parent.sendPatternRule("\""+selected_text.split(":")[0]+
					"\"(|+1)["+String.join("", selected_text.split(":")[1].chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
					return;
								
				}else if (selected_text.split("=").length>1) {
//					If YES							
//					Value = ef12-gh34-ij56
					CharacteristicValue tmp = new CharacteristicValue();
					tmp.setText_values(selected_text.split("=")[1].trim());
					tmp.setParentChar(active_char);
					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]		
					parent.sendPatternRule("\""+selected_text.split("=")[0]+
					"\"(|+1)["+String.join("", selected_text.split("=")[1].chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
					return;
	
				}else {
//					If NO (no : or = )
					String sw_splitter = beginsWithStopWords(selected_text,parent.account,active_char);
					if(sw_splitter!=null) {
//					The first terms or their corrections are stop words? (e.g. "abcd ef12-gh34-ij56" with abcd or its correction declared as a STOP word)						
//						If YES
//							Value = ef12-gh34-ij56	
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(selected_text.split(sw_splitter)[1]);
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//							Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]
						parent.sendPatternRule("\""+sw_splitter+
								"\"(|+1)["+String.join("", selected_text.split(sw_splitter)[1].chars().mapToObj((c -> (char) c)).
								map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
						return;
					}else {
//						If NO (e.g. "ab8cd9 ef12-gh34")					
//							Value = ab8cd9 ef12-gh34
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(selected_text);
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//						
//							Rule = ["ab"#"cd"#(|-1)@@##(|-1)@@##]
						String rule="[";
						boolean firstSepPassed=false;
						boolean last_is_alpha=false;
						for(int i=0;i<selected_text.length();i++) {
							char c = selected_text.charAt(i);
							if(Character.isAlphabetic(c)) {
								rule=rule+(firstSepPassed?"@":(last_is_alpha?"@":"\"@"));
								last_is_alpha = true;
							}else if (Character.isDigit(c)) {
								rule=rule+(firstSepPassed?"#":(last_is_alpha?"\"#":"#"));
								last_is_alpha = false;
							}else {
								rule=rule+("(|-1)");
								firstSepPassed=true;
							}
						}
					}
				}
			}else {
//				If NO	(no digit in text)								
//					The correction of the selection is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?								
				corrected_text = WordUtils.CORRECT(selected_text);
				SEPARATORS = new String[] {",",":",".","-"," ","/"};
				HashSet<CharacteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
				for(CharacteristicValue known_value:active_char.getKnownValues()) {
					String separator_free_known = known_value.getDataLanguageValue();
					String separator_free_selected = corrected_text;
					for(String sep:SEPARATORS) {
						separator_free_known = separator_free_known.replaceAll(sep, "");
						separator_free_selected = separator_free_selected.replaceAll(sep, "");
						if( unidecode.decode(separator_free_known).toUpperCase().contains(
								unidecode.decode(separator_free_selected).toUpperCase()) ){
							VALUES_CONTAINING_SELECTION.add(known_value);
						}}}
				if(VALUES_CONTAINING_SELECTION.size()==1) {
//						If YES							
//							Value = VAL		
					parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//							Rule = Selection<"VAL">	
					parent.sendPatternRule(selected_text+"<"+VALUES_CONTAINING_SELECTION.iterator().next().getDisplayValue()+">");
					return;
				}
//						If NO							
//							The correction of the selection includes one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
						corrected_text = WordUtils.CORRECT(selected_text);
						SEPARATORS = new String[] {",",":",".","-"," ","/"};
						HashSet<CharacteristicValue> VALUES_CONTAINED_IN_SELECTION = new HashSet<CharacteristicValue>();
						for(CharacteristicValue known_value:active_char.getKnownValues()) {
							String separator_free_known = known_value.getDataLanguageValue();
							String separator_free_selected = corrected_text;
							for(String sep:SEPARATORS) {
								separator_free_known = separator_free_known.replaceAll(sep, "");
								separator_free_selected = separator_free_selected.replaceAll(sep, "");
								if( unidecode.decode(separator_free_selected).toUpperCase().contains(
										unidecode.decode(separator_free_known).toUpperCase()) ){
									VALUES_CONTAINED_IN_SELECTION.add(known_value);
								}}}
						if(VALUES_CONTAINED_IN_SELECTION.size()==1) {
//						If YES							
//							Value = VAL		
						parent.sendPatternValue(VALUES_CONTAINED_IN_SELECTION.iterator().next());
//							Rule = Selection<"VAL">	
						parent.sendPatternRule(selected_text+"<"+VALUES_CONTAINED_IN_SELECTION.iterator().next().getDisplayValue()+">");
						return;
						}else {		
//								If NO					
//									The selection includes one of the following characters: ":", "="? (e.g. "abcd: efgh")
									if(selected_text.contains(":")||selected_text.contains("=")) {
//										If YES			
//										Value = Correction (efgh)		
//										Rule = "abcd"(|+1)["efgh"]							
									}else {
//										If NO			
//										The first terms of the selection or their corrections are stop words? (e.g. "abcd efgh", assuming that "abcd" and/or its correction has been declared as a stop word)
										String sw_splitter = beginsWithStopWords(selected_text,parent.account,active_char);
										if(sw_splitter!=null) {
//											
//											If YES	
//												Value = Correction (efgh)
											CharacteristicValue tmp = new CharacteristicValue();
											tmp.setText_values(selected_text.split(sw_splitter)[1]);
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
//												Rule = "abcd"(|+1)["efgh"]
											parent.sendPatternRule("\""+sw_splitter+"\"(|+1)[\""+selected_text.split(sw_splitter)[1]+"\"]");
											return;
											
										}else {
//											If NO	
//												Value = Correction (Selection)
											CharacteristicValue tmp = new CharacteristicValue();
											tmp.setText_values(corrected_text);
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
//												Rule = [Selection]
											parent.sendPatternRule("["+selected_text+"]");
											return;
										}
									}
	

						}
//
			}
		}
	}

	private static String beginsWithStopWords(String selected_text,UserAccount account, ClassCharacteristic active_char) {
		if(specialwords!=null) {
			
		}else {
			try {
				specialwords = new HashMap<String,LinkedHashSet<String>> ();
				LinkedHashSet<String> tmp_for = new LinkedHashSet<String>();
				LinkedHashSet<String> tmp_dw = new LinkedHashSet<String>();
				LinkedHashSet<String> tmp_stop = new LinkedHashSet<String>();
				Connection connX = Tools.spawn_connection();
				Statement stmtX = connX.createStatement();
				
				//Load the application special words
				//#
				ResultSet rsX = stmtX.executeQuery("select term_name from "+account.getActive_project()+".project_terms where application_term_status");
				while(rsX.next()) {
					tmp_for.add(rsX.getString("term_name").toUpperCase());
				}
				specialwords.put("FOR", tmp_for);
				rsX.close();
				
				//Load the drawing special words
				//#
				rsX = stmtX.executeQuery("select term_name from "+account.getActive_project()+".project_terms where drawing_term_status");
				while(rsX.next()) {
					tmp_dw.add(rsX.getString("term_name").toUpperCase());
				}
				specialwords.put("DW", tmp_dw);
				rsX.close();
				
				//Load the stop special words
				//#
				rsX = stmtX.executeQuery("select term_name from "+account.getActive_project()+".project_terms where stop_term_status");
				while(rsX.next()) {
					tmp_stop.add(rsX.getString("term_name").toUpperCase());
				}
				specialwords.put("STOP", tmp_stop);
				
				rsX.close();
				stmtX.close();
				connX.close();
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(selected_text.toUpperCase().startsWith(active_char.getCharacteristic_name().toUpperCase())){
			return selected_text.substring(0,active_char.getCharacteristic_name().length());
		}
		if(selected_text.toUpperCase().startsWith(active_char.getCharacteristic_name_translated().toUpperCase())){
			return selected_text.substring(0,active_char.getCharacteristic_name().length());
		}
		
		
		
		for(String key:specialwords.keySet()) {
			LinkedHashSet<String> tmp = specialwords.get(key);
			for(String sw:tmp) {
				if(selected_text.toUpperCase().startsWith(sw.toUpperCase())){
					return selected_text.substring(0,sw.length());
				}
				
			}
		}
		
		return null;
	}

}
