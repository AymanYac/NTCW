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
import model.GlobalConstants;
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
		/*TRIM THE SELECTED TEXT IF TOO SHORT , PRECEDE AND FOLLOW WITH SPACE*/
		selected_text = selected_text.trim();
		if(selected_text.length()<GlobalConstants.CHAR_DESC_PATTERN_SELECTION_PHRASE_THRES) {
			selected_text= " "+selected_text+" ";
			System.out.println(":::SEPARATED SELECTION:::");
		}
		/*Put in double quotes rule portions that indicate a text value and
		 * store accordingly*/
		selected_text = selected_text.replace("\"", "~\"");
		
		
		//The correction of the selection without separator is equal to one of the values ("VAL") already listed for this characteristic without separator?
		//#Let's correct the selection
		System.out.println(":::"+selected_text+":::");
		String corrected_text = WordUtils.CORRECT(selected_text);
		System.out.println(":::"+corrected_text+":::");
		//Let's clean the selection from all separators and do the same thing for the known values
		String[] SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
		System.out.println("Trying to match the selection with known values");
		for(CharacteristicValue known_value:active_char.getKnownValues()) {
			System.out.println("We know value "+known_value.getDisplayValue());
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
					parent.sendPatternRule("["+WordUtils.QUOTE_NON_SEP_TEXT(corrected_text)+"]");
					System.out.println("=====>Match !");
					return;
				}
			}
		}
		System.out.println("No match found");				
//		If NO															
//			The characteristic is a TXT characteristic?														
//				If YES
		if(!active_char.getIsNumeric()) {
			System.out.println("The char is TXT");
//			The characteristic is translatable?
//			If YES	
			if(active_char.getIsTranslatable()) {	
				System.out.println("The char is translatable");
//				The selection includes one of the following characters: ":", "="? (e.g. "material: iron steel")										
//					If YES
				if(selected_text.contains(":") || selected_text.contains("=")) {
					System.out.println("The char contains : or =");
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
					SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
					System.out.println("Trying to match the after :/= segment with known values");
					for(CharacteristicValue known_value:active_char.getKnownValues()) {
						System.out.println("We know value "+known_value.getDataLanguageValue());
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
								parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)[\""+part_after_identifier+"\"]");
								System.out.println("=====>Match !");
								return;
							}
						}}
					System.out.println("Trying to match the after :/= segment with a subsegment of known values");
//						If NO							
//							The correction of the part of the selection after the ":" or "=" is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
							corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
							SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
							HashSet<CharacteristicValue> VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CharacteristicValue>();
							for(CharacteristicValue known_value:active_char.getKnownValues()) {
								System.out.println("We know value "+known_value.getDataLanguageValue());
								/*
								String separator_free_known = known_value.getDataLanguageValue();
								String separator_free_selected = corrected_part_after_identifier;
								for(String sep:SEPARATORS) {
									separator_free_known = separator_free_known.replaceAll(sep, "");
									separator_free_selected = separator_free_selected.replaceAll(sep, "");
									if( unidecode.decode(separator_free_known).toUpperCase().contains(
											unidecode.decode(separator_free_selected).toUpperCase()) ){
										VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
										System.out.println("Potential Match !");
									}}*/
								if(WordUtils.TermWiseInclusion(corrected_part_after_identifier,known_value.getDataLanguageValue(),true)) {
									VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
									System.out.println("Potential Match !");
								}	
							
							}
							
							if(VALUES_CONTAINING_AFTER_IDENTIFIER.size()==1) {
								System.out.println("We only have one potential match !");
//								If YES					
//								Value = VAL			
								parent.sendPatternValue(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next());
//								Value (Pivot language) = PIVOTtranslation (VAL)				
//								Rule = Selection<"VAL">				
								parent.sendPatternRule("\""+selected_text+"\""+"<\""+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+"\">");
								System.out.println("=====> Match!");
								return;
							}else {
								System.out.println("There are no/too many potential matches");
//								If NO					
//									The correction of the part of the selection after the ":" or "=" is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?
								System.out.println("Trying to match the after :/= segment with a subsegment of known values in pivot language");
								corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
								SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
								VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CharacteristicValue>();
								for(CharacteristicValue known_value:active_char.getKnownValues()) {
									System.out.println("We know value "+known_value.getUserLanguageValue());
									/*String separator_free_known = known_value.getUserLanguageValue();
									String separator_free_selected = corrected_part_after_identifier;
									for(String sep:SEPARATORS) {
										separator_free_known = separator_free_known.replaceAll(sep, "");
										separator_free_selected = separator_free_selected.replaceAll(sep, "");
										if( unidecode.decode(separator_free_known).toUpperCase().contains(
												unidecode.decode(separator_free_selected).toUpperCase()) ){
											VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
											System.out.println("Potential Match !");
										}}*/
									if(WordUtils.TermWiseInclusion(corrected_part_after_identifier,known_value.getUserLanguageValue(),true)) {
										VALUES_CONTAINING_AFTER_IDENTIFIER.add(known_value);
										System.out.println("Potential Match !");
									}	
								}
								if(VALUES_CONTAINING_AFTER_IDENTIFIER.size()==1) {
									System.out.println("We only have one potential match !");
//									If YES
//											Value (Pivot language) = ENVAL		
//											Value = LOCALtranslation (ENVAL)
									parent.sendPatternValue(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next());
//									
//											Rule = Selection<LOCALtranslation("ENVAL")>	
									parent.sendPatternRule("\""+selected_text+"\""+"<\""+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+"\">");
									System.out.println("=====>Match !");
									return;
									
								}else {
									System.out.println("There are no/too many potential matches");
//									If NO			
//										Value = Correction ("iron steel")
									CharacteristicValue new_value = new CharacteristicValue();
									new_value.setText_values(corrected_part_after_identifier+CharacteristicValue.dataLanguage);
									new_value.setParentChar(active_char);
									parent.sendPatternValue(new_value);
									parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)[\""+part_after_identifier+"\"]");
									System.out.println("DEFAULT: The value is the corrected selection");
									return;
									
//										Rule = "material"(|+1)["iron steel"]
						
								}
								}
				}
				System.out.println("No :/= in the selection");
//					If NO (no : or = in the selection)									
//						The correction of the selection is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?
				System.out.println("Trying to match the selection with subsegments of known values");
				corrected_text = WordUtils.CORRECT(selected_text);
				SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
				HashSet<CharacteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
				for(CharacteristicValue known_value:active_char.getKnownValues()) {
					System.out.println("we known value "+known_value.getDataLanguageValue());
					for(String known_value_sub_element:known_value.getDataLanguageValue().split(String.join("|", SEPARATORS))) {
						if( unidecode.decode(known_value_sub_element).toUpperCase().equals(
								unidecode.decode(corrected_text).toUpperCase()) ){
							VALUES_CONTAINING_SELECTION.add(known_value);
							System.out.println("Potential Match !");
						}}}
				if(VALUES_CONTAINING_SELECTION.size()==1) {
					System.out.println("We have only one possible match");
//				
//							If YES							
//								Value = VAL						
//								Value (Pivot language) = PIVOTtranslation (VAL)
					parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//								Rule = [Selection]
					parent.sendPatternRule("["+WordUtils.QUOTE_NON_SEP_TEXT(selected_text)+"]");
					System.out.println("=====> Match!");
					return;
				}else {
					System.out.println("We have no/too many possible matches");
//					If NO							
//					The correction of the selection is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?
					System.out.println("Trying to match the selection with subsegments of known values in pivot language");
					corrected_text = WordUtils.CORRECT(selected_text);
					SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
					VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
					for(CharacteristicValue known_value:active_char.getKnownValues()) {
						System.out.println("we know value "+known_value.getUserLanguageValue());
						String[] subelements;
						try{
							subelements = known_value.getUserLanguageValue().split(String.join("|", SEPARATORS));
						}catch(Exception V) {
							subelements = new String[] {""};
						}
						for(String known_value_sub_element:subelements) {
							if( unidecode.decode(known_value_sub_element).toUpperCase().equals(
									unidecode.decode(corrected_text).toUpperCase()) ){
								VALUES_CONTAINING_SELECTION.add(known_value);
								System.out.println("Potential Match!");
							}}}
					if(VALUES_CONTAINING_SELECTION.size()==1) {
						System.out.println("We only have on potential match");
//					
//						If YES					
//							Value (Pivot language) = ENVAL				
//							Value = LOCALtranslation (ENVAL)
						parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//							Rule = Selection<LOCALtranslation("ENVAL")>
						parent.sendPatternRule("\""+selected_text+"\""+"<"+VALUES_CONTAINING_SELECTION.iterator().next().getUserLanguageValue()+">");
						System.out.println("====> Match");
						return;
					}else {
						System.out.println("We have no/too many potential matches");
//						If NO					
//						Value = Correction (Selection)
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(corrected_text+CharacteristicValue.dataLanguage);
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//						Rule = [Selection]
						parent.sendPatternRule("["+WordUtils.QUOTE_NON_SEP_TEXT(selected_text)+"]");
						System.out.println("DEFAULT Value : correction of selection");
						return;
					}

				
					
					
				}								
			}
//			If NO (the char is not translatable)
			System.out.println("The char is not translatable");
//			The selection contains at list 1 figure?
			int number_of_digits = selected_text.chars().mapToObj((c -> (char) c)).filter(c->Character.isDigit(c)).collect(Collectors.toList()).size();
//				If YES
			if(number_of_digits>0) {
				System.out.println("The selection contains at least 1 digit");
//				The selection includes one of the following characters: ":", "="? (e.g. "abcd: ef12-gh34-ij56")	
				if(selected_text.split(":").length>1) {
					System.out.println("The selection includes :");
//					If YES							
//					Value = ef12-gh34-ij56
					CharacteristicValue tmp = new CharacteristicValue();
					tmp.setText_values(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1].trim()));
					tmp.setParentChar(active_char);
					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]		
					/*
					parent.sendPatternRule("\""+selected_text.split(":")[0]+
					"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]).chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
					*/
					parent.sendPatternRule("\""+selected_text.split(":")[0]+
							"\"(|+1)["+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]),false)+"]");
							
					
					System.out.println("Match!");
					return;
								
				}else if (selected_text.split("=").length>1) {
					System.out.println("The selectin includes =");
//					If YES							
//					Value = ef12-gh34-ij56
					CharacteristicValue tmp = new CharacteristicValue();
					tmp.setText_values(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1].trim()));
					tmp.setParentChar(active_char);
					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]		
					/*parent.sendPatternRule("\""+selected_text.split("=")[0]+
					"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]).chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
					*/
					parent.sendPatternRule("\""+selected_text.split("=")[0]+
							"\"(|+1)["+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]),false)+"]");
							
					
					System.out.println("Match!");
					return;
	
				}else {
					System.out.println("No :/= in selected text");
//					If NO (no : or = )
					String sw_splitter = beginsWithStopWords(selected_text,parent.account,active_char);
					if(sw_splitter!=null) {
						System.out.println("The selection begins with stop word "+sw_splitter);
//					The first terms or their corrections are stop words? (e.g. "abcd ef12-gh34-ij56" with abcd or its correction declared as a STOP word)						
//						If YES
//							Value = ef12-gh34-ij56	
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1].trim()));
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//							Rule = "abcd"(|+1)[@@##(|-1)@@##(|-1)@@##]
						/*parent.sendPatternRule("\""+sw_splitter+
								"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]).chars().mapToObj((c -> (char) c)).
								map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|-1)")).collect(Collectors.toList()))+"]");
						*/
						parent.sendPatternRule("\""+sw_splitter+
								"\"(|+1)["+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
										WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]),false)+"]");
						
						System.out.println("Match!");
						return;
					}else {
						System.out.println("The selection doesn't start with a stop word");
//						If NO (e.g. "ab8cd9 ef12-gh34")					
//							Value = ab8cd9 ef12-gh34
						CharacteristicValue tmp = new CharacteristicValue();
						tmp.setText_values(selected_text);
						tmp.setParentChar(active_char);
						parent.sendPatternValue(tmp);
//						
//							Rule = ["ab"#"cd"#(|-1)@@##(|-1)@@##]
						
						parent.sendPatternRule("["+
						WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(selected_text, true)
						+"]");
						System.out.println("Match!");
//						
					}
				}
			}else {
				System.out.println("No digit in text");
//				If NO	(no digit in text)								
//					The correction of the selection is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?								
				System.out.println("Trying to find a subsegment in known values _ data language");
				corrected_text = WordUtils.CORRECT(selected_text);
				SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
				HashSet<CharacteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CharacteristicValue>();
				for(CharacteristicValue known_value:active_char.getKnownValues()) {
					System.out.println("We known value "+known_value.getDataLanguageValue());
					/*
					String separator_free_known = known_value.getDataLanguageValue();
					String separator_free_selected = corrected_text;
					for(String sep:SEPARATORS) {
						separator_free_known = separator_free_known.replaceAll(sep, "");
						separator_free_selected = separator_free_selected.replaceAll(sep, "");
						if( unidecode.decode(separator_free_known).toUpperCase().contains(
								unidecode.decode(separator_free_selected).toUpperCase()) ){
							System.out.println("Potential Match!");
							VALUES_CONTAINING_SELECTION.add(known_value);
						}}*/
					if(WordUtils.TermWiseInclusion(corrected_text,known_value.getDataLanguageValue(),true)) {
						VALUES_CONTAINING_SELECTION.add(known_value);
						System.out.println("Potential Match !");
					}		
				}
				if(VALUES_CONTAINING_SELECTION.size()==1) {
					System.out.println("We have only one potential match");
//						If YES							
//							Value = VAL		
					parent.sendPatternValue(VALUES_CONTAINING_SELECTION.iterator().next());
//							Rule = Selection<"VAL">	
					parent.sendPatternRule("\""+selected_text+"\""+"<\""+VALUES_CONTAINING_SELECTION.iterator().next().getDisplayValue()+"\">");
					System.out.println("=====> Match!");
					return;
				}
				System.out.println("We have no/too many potential matches");
				System.out.println("Trying to find a supersegment in known values _ data language");
//						If NO							
//							The correction of the selection includes one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
						corrected_text = WordUtils.CORRECT(selected_text);
						SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
						HashSet<CharacteristicValue> VALUES_CONTAINED_IN_SELECTION = new HashSet<CharacteristicValue>();
						for(CharacteristicValue known_value:active_char.getKnownValues()) {
							System.out.println("We know value "+known_value.getDataLanguageValue());
							/*
							String separator_free_known = known_value.getDataLanguageValue();
							String separator_free_selected = corrected_text;
							for(String sep:SEPARATORS) {
								separator_free_known = separator_free_known.replaceAll(sep, "");
								separator_free_selected = separator_free_selected.replaceAll(sep, "");
								if( unidecode.decode(separator_free_selected).toUpperCase().contains(
										unidecode.decode(separator_free_known).toUpperCase()) ){
									System.out.println("Potential Match !");
									VALUES_CONTAINED_IN_SELECTION.add(known_value);
								}}*/
							if(WordUtils.TermWiseInclusion(known_value.getDataLanguageValue(),corrected_text,true)) {
								VALUES_CONTAINED_IN_SELECTION.add(known_value);
								System.out.println("Potential Match !");
							}
							}
						if(VALUES_CONTAINED_IN_SELECTION.size()==1) {
							System.out.println("We have only one potential match");
//						If YES							
//							Value = VAL		
						parent.sendPatternValue(VALUES_CONTAINED_IN_SELECTION.iterator().next());
//							Rule = Selection<"VAL">	
						parent.sendPatternRule(selected_text+"<\""+VALUES_CONTAINED_IN_SELECTION.iterator().next().getDisplayValue()+"\">");
						System.out.println("====> Match!");
						return;
						}else {	
							System.out.println("We have no/too many potential matches");
//								If NO					
//									The selection includes one of the following characters: ":", "="? (e.g. "abcd: efgh")
									if(selected_text.contains(":")||selected_text.contains("=")) {
										System.out.println("the selection contains : or =");
//										If YES			
//										Value = Correction (efgh)
										CharacteristicValue tmp = new CharacteristicValue();
										try{
											tmp.setText_values(WordUtils.CORRECT(selected_text.split(":")[1]));
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
											parent.sendPatternRule("\""+selected_text.split(":")[0]+"\""+"(|+1)[\""+selected_text.split(":")[1]+"\"]");
										}catch(Exception V) {
											tmp.setText_values(WordUtils.CORRECT(selected_text.split("=")[1]));
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
											parent.sendPatternRule("\""+selected_text.split("=")[0]+"\""+"(|+1)[\""+selected_text.split("=")[1]+"\"]");
											
										}
//										Rule = "abcd"(|+1)["efgh"]		
										System.out.println("DEFAULT MATCH Correction");
									}else {
										System.out.println("The selection doesn't contain = nor :");
//										If NO			
//										The first terms of the selection or their corrections are stop words? (e.g. "abcd efgh", assuming that "abcd" and/or its correction has been declared as a stop word)
										String sw_splitter = beginsWithStopWords(selected_text,parent.account,active_char);
										if(sw_splitter!=null) {
											System.out.println("The selection starts with stop word "+sw_splitter);
//											
//											If YES	
//												Value = Correction (efgh)
											CharacteristicValue tmp = new CharacteristicValue();
											tmp.setText_values(selected_text.split(sw_splitter)[1]);
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
//												Rule = "abcd"(|+1)["efgh"]
											parent.sendPatternRule("\""+sw_splitter+"\"(|+1)[\""+selected_text.split(sw_splitter)[1]+"\"]");
											System.out.println("====> Match");
											return;
											
										}else {
											System.out.println("The selected text doesn't start with a stop word");
//											If NO	
//												Value = Correction (Selection)
											CharacteristicValue tmp = new CharacteristicValue();
											tmp.setText_values(corrected_text);
											tmp.setParentChar(active_char);
											parent.sendPatternValue(tmp);
//												Rule = [Selection]
											parent.sendPatternRule("["+WordUtils.QUOTE_NON_SEP_TEXT(selected_text)+"]");
											System.out.println("Default match");
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
		
		if(WordUtils.CORRECT(selected_text).toUpperCase().startsWith(active_char.getCharacteristic_name().toUpperCase())){
			return selected_text.substring(0,active_char.getCharacteristic_name().length());
		}
		if(WordUtils.CORRECT(selected_text).toUpperCase().startsWith(active_char.getCharacteristic_name_translated().toUpperCase())){
			return selected_text.substring(0,active_char.getCharacteristic_name().length());
		}
		
		
		
		for(String key:specialwords.keySet()) {
			LinkedHashSet<String> tmp = specialwords.get(key);
			for(String sw:tmp) {
				if(WordUtils.CORRECT(selected_text).toUpperCase().startsWith(sw.toUpperCase())){
					return selected_text.substring(0,sw.length());
				}
				
			}
		}
		
		return null;
	}

}
