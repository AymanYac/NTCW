package service;

import controllers.Char_description;
import model.*;
import org.icepdf.ri.util.SearchTextTask;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CharPatternServices {
	
	private static HashMap<String,LinkedHashSet<String>> specialwords;
	private static HashMap<String, List<String>> charIdArrays;

	public static void scanSelectionForPatternDetection(Char_description parent, ClassCaracteristic active_char) {
		parent.refresh_ui_display();
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

		try{
			if(parent.browserController.showingPdf.getValue()) {
				String pdfSelection = parent.browserController.iceController.getDocumentViewController().getSelectedText();
				if (pdfSelection != null && pdfSelection.length() > 0) {
					selected_text = pdfSelection;
				}
			}

		}catch (Exception V){

		}
		System.out.println("Processing selected text ::: "+selected_text);
		/*TRIM THE SELECTED TEXT IF TOO SHORT , PRECEDE AND FOLLOW WITH SPACE*/
		selected_text = selected_text.trim();
		if(selected_text.length()<GlobalConstants.CHAR_DESC_PATTERN_SELECTION_PHRASE_THRES) {
			selected_text= " "+selected_text+" ";
			System.out.println(":::SEPARATED SELECTION:::");
		}
		/*Put in double quotes rule portions that indicate a text value and
		 * store accordingly
		 * ~ is used to escape intext quotes so as not to be mistaken with rule syntax quotes
		 * */
		selected_text = selected_text.replace("\"", "~\"");
		ArrayList<UnitOfMeasure> uomsInSelection = WordUtils.parseCompatibleUoMs(selected_text,active_char);
		ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(selected_text);
		
		
		//The correction of the selection without separator is equal to one of the values ("VAL") already listed for this characteristic without separator?
		//#Let's correct the selection
		System.out.println(":::"+selected_text+":::");
		String corrected_text = WordUtils.CORRECT(selected_text);
		System.out.println(":::"+corrected_text+":::");
		//Let's clean the selection from all separators and do the same thing for the known values
		String[] SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
		System.out.println("Trying to match the selection with known values");
		for(CaracteristicValue known_value:active_char.getKnownValues().stream().filter(k->(k!=null) && (k.getStdValue()!=null)).collect(Collectors.toList())) {
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
					parent.sendPatternRule(WordUtils.QUOTE_NON_SEP_TEXT(corrected_text,true)+"<DL "+known_value.getDataLanguageValue()+">");
					parent.sendSemiAutoPattern(known_value, WordUtils.QUOTE_NON_SEP_TEXT(corrected_text,true)+"<DL "+known_value.getDataLanguageValue()+">");
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
					for(CaracteristicValue known_value:active_char.getKnownValues()) {
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
								parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)\""+part_after_identifier+"\"<DL "+part_after_identifier+">");
								parent.sendSemiAutoPattern(known_value, "\""+part_before_identifier+"\"(|+1)\""+part_after_identifier+"\"<DL "+part_after_identifier+">");
								System.out.println("=====>Match !");
								return;
							}
						}}
					System.out.println("Trying to match the after :/= segment with a subsegment of known values");
//						If NO							
//							The correction of the part of the selection after the ":" or "=" is included in one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
							corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
							SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
							HashSet<CaracteristicValue> VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CaracteristicValue>();
							for(CaracteristicValue known_value:active_char.getKnownValues()) {
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
								parent.sendSemiAutoPattern(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next(), "\""+selected_text+"\""+"<\""+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+"\">");
								System.out.println("=====> Match!");
								return;
							}else {
								System.out.println("There are no/too many potential matches");
//								If NO					
//									The correction of the part of the selection after the ":" or "=" is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?
								System.out.println("Trying to match the after :/= segment with a subsegment of known values in pivot language");
								corrected_part_after_identifier = WordUtils.CORRECT(part_after_identifier);
								SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
								VALUES_CONTAINING_AFTER_IDENTIFIER = new HashSet<CaracteristicValue>();
								for(CaracteristicValue known_value:active_char.getKnownValues()) {
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
									parent.sendPatternRule("\""+selected_text+"\""+"<DL "+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+">");
									parent.sendSemiAutoPattern(VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next(), "\""+selected_text+"\""+"<DL "+VALUES_CONTAINING_AFTER_IDENTIFIER.iterator().next().getDataLanguageValue()+">");
									System.out.println("=====>Match !");
									return;
									
								}else {
									System.out.println("There are no/too many potential matches");
//									If NO			
//										Value = Correction ("iron steel")
									CaracteristicValue new_value = new CaracteristicValue();;;; new_value.setParentChar(active_char);
									new_value.setDataLanguageValue(corrected_part_after_identifier);

									parent.sendPatternValue(new_value);
									parent.sendPatternRule("\""+part_before_identifier+"\"(|+1)\""+part_after_identifier+"\"<DL "+part_after_identifier+">");
									parent.sendSemiAutoPattern(new_value, "\""+part_before_identifier+"\"(|+1)\""+part_after_identifier+"\"<DL "+part_after_identifier+">");
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
				HashSet<CaracteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CaracteristicValue>();
				for(CaracteristicValue known_value:active_char.getKnownValues()) {
					System.out.println("we known value "+known_value.getDataLanguageValue());
					if(!(known_value.getDataLanguageValue()!=null)){continue;}
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
					parent.sendPatternRule(WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<DL "+VALUES_CONTAINING_SELECTION.iterator().next()+">");
					parent.sendSemiAutoPattern(VALUES_CONTAINING_SELECTION.iterator().next(), WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<DL "+VALUES_CONTAINING_SELECTION.iterator().next()+">");
					System.out.println("=====> Match!");
					return;
				}else {
					System.out.println("We have no/too many possible matches");
//					If NO							
//					The correction of the selection is included in one and only one of the values in pivot language already listed ("ENVAL") for this characteristic (independent terms, modulo separators)?
					System.out.println("Trying to match the selection with subsegments of known values in pivot language");
					corrected_text = WordUtils.CORRECT(selected_text);
					SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
					VALUES_CONTAINING_SELECTION = new HashSet<CaracteristicValue>();
					for(CaracteristicValue known_value:active_char.getKnownValues()) {
						if(!(known_value.getUserLanguageValue()!=null)){continue;}
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
						parent.sendPatternRule("\""+selected_text+"\""+"<UL "+VALUES_CONTAINING_SELECTION.iterator().next().getUserLanguageValue()+">");
						parent.sendSemiAutoPattern(VALUES_CONTAINING_SELECTION.iterator().next(), "\""+selected_text+"\""+"<UL "+VALUES_CONTAINING_SELECTION.iterator().next().getUserLanguageValue()+">");
						System.out.println("====> Match");
						return;
					}else {
						System.out.println("We have no/too many potential matches");
//						If NO					
//						Value = Correction (Selection)
						CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
						tmp.setDataLanguageValue(corrected_text);

						parent.sendPatternValue(tmp);
//						Rule = [Selection]
						parent.sendPatternRule(WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<DL "+corrected_text+">");
						parent.sendSemiAutoPattern(tmp, WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<DL "+corrected_text+">");
						System.out.println("DEFAULT Value : correction of selection");
						return;
					}

				
					
					
				}								
			}
//			If NO (the char is not translatable)
			System.out.println("The char is not translatable");
//			The selection contains at least 1 figure?
			int number_of_digits = selected_text.chars().mapToObj((c -> (char) c)).filter(c->Character.isDigit(c)).collect(Collectors.toList()).size();
//				If YES
			if(number_of_digits>0) {
				System.out.println("The selection contains at least 1 digit");
//				The selection includes one of the following characters: ":", "="? (e.g. "abcd: ef12-gh34-ij56")	
				if(selected_text.split(":").length>1) {
					System.out.println("The selection includes :");
//					If YES							
//					Value = ef12-gh34-ij56
					CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
					tmp.setTXTValue(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1].trim()));

					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|+0)@@##(|+0)@@##]		
					/*
					parent.sendPatternRule("\""+selected_text.split(":")[0]+
					"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]).chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|+0)")).collect(Collectors.toList()))+"]");
					*/
					parent.sendPatternRule("\""+selected_text.split(":")[0]+
							"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
											WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]),false,false)+">");
							
					parent.sendSemiAutoPattern(tmp, "\""+selected_text.split(":")[0]+
							"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
											WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(":")[1]),false,false)+">");
							
					System.out.println("Match!");
					return;
								
				}else if (selected_text.split("=").length>1) {
					System.out.println("The selectin includes =");
//					If YES							
//					Value = ef12-gh34-ij56
					CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
					tmp.setTXTValue(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1].trim()));

					parent.sendPatternValue(tmp);
//					Rule = "abcd"(|+1)[@@##(|+0)@@##(|+0)@@##]		
					/*parent.sendPatternRule("\""+selected_text.split("=")[0]+
					"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]).chars().mapToObj((c -> (char) c)).
					map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|+0)")).collect(Collectors.toList()))+"]");
					*/
					parent.sendPatternRule("\""+selected_text.split("=")[0]+
							"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
											WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]),false,false)+">");
							
					parent.sendSemiAutoPattern(tmp, "\""+selected_text.split("=")[0]+
							"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
									WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
											WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split("=")[1]),false,false)+">");
							
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
						CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
						tmp.setTXTValue(WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1].trim()));

						parent.sendPatternValue(tmp);
//							Rule = "abcd"(|+1)[@@##(|+0)@@##(|+0)@@##]
						/*parent.sendPatternRule("\""+sw_splitter+
								"\"(|+1)["+String.join("", WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]).chars().mapToObj((c -> (char) c)).
								map(c->Character.isAlphabetic(c)?"@":(Character.isDigit(c)?"#":"(|+0)")).collect(Collectors.toList()))+"]");
						*/
						parent.sendPatternRule("\""+sw_splitter+
								"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
										WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
												WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]),false,false)+">");
						parent.sendSemiAutoPattern(tmp, "\""+sw_splitter+
								"\"(|+1)"+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
										WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]),false,true)+"<TXT "+WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(
												WordUtils.TRIM_LEADING_SEPARATORS(selected_text.split(sw_splitter)[1]),false,false)+">");
						
						System.out.println("Match!");
						return;
					}else {
						System.out.println("The selection doesn't start with a stop word");
//						If NO (e.g. "ab8cd9 ef12-gh34")					
//							Value = ab8cd9 ef12-gh34
						CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
						tmp.setTXTValue(selected_text);

						parent.sendPatternValue(tmp);
//						
//							Rule = ["ab"#"cd"#(|+0)@@##(|+0)@@##]
						
						parent.sendPatternRule(WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(selected_text, true,true)+"<TXT "+
						WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(selected_text, true,false)
						+">");
						parent.sendSemiAutoPattern(tmp, WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(selected_text, true,true)+"<TXT "+
						WordUtils.ALPHANUM_PATTERN_RULE_INREPLACE(selected_text, true,false)
						+">");
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
				HashSet<CaracteristicValue> VALUES_CONTAINING_SELECTION = new HashSet<CaracteristicValue>();
				for(CaracteristicValue known_value:active_char.getKnownValues()) {
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
					parent.sendPatternRule("\""+selected_text+"\""+"<TXT "+VALUES_CONTAINING_SELECTION.iterator().next().getStdValue()+">");
					parent.sendSemiAutoPattern(VALUES_CONTAINING_SELECTION.iterator().next(), "\""+selected_text+"\""+"<TXT "+VALUES_CONTAINING_SELECTION.iterator().next().getStdValue()+">");
					System.out.println("=====> Match!");
					return;
				}
				System.out.println("We have no/too many potential matches");
				System.out.println("Trying to find a supersegment in known values _ data language");
//						If NO							
//							The correction of the selection includes one and only one of the values already listed ("VAL") for this characteristic (independent terms, modulo separators)?						
						corrected_text = WordUtils.CORRECT(selected_text);
						SEPARATORS = new String[] {",",":","\\.","-"," ","/"};
						HashSet<CaracteristicValue> VALUES_CONTAINED_IN_SELECTION = new HashSet<CaracteristicValue>();
						for(CaracteristicValue known_value:active_char.getKnownValues()) {
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
						parent.sendPatternRule(selected_text+"<TXT "+VALUES_CONTAINED_IN_SELECTION.iterator().next().getStdValue()+">");
						parent.sendSemiAutoPattern(VALUES_CONTAINED_IN_SELECTION.iterator().next(), selected_text+"<TXT "+VALUES_CONTAINED_IN_SELECTION.iterator().next().getStdValue()+">");
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
										CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

										try{
											tmp.setTXTValue(WordUtils.CORRECT(selected_text.split(":")[1]));

											parent.sendPatternValue(tmp);
											parent.sendPatternRule("\""+selected_text.split(":")[0]+"\""+"(|+1)\""+selected_text.split(":")[1]+"\"<TXT "+selected_text.split(":")[1]+">");
											parent.sendSemiAutoPattern(tmp, "\""+selected_text.split(":")[0]+"\""+"(|+1)\""+selected_text.split(":")[1]+"\"<TXT "+selected_text.split(":")[1]+">");
										}catch(Exception V) {
											tmp.setTXTValue(WordUtils.CORRECT(selected_text.split("=")[1]));

											parent.sendPatternValue(tmp);
											parent.sendPatternRule("\""+selected_text.split("=")[0]+"\""+"(|+1)\""+selected_text.split("=")[1]+"\"<TXT "+selected_text.split("=")[1]+">");
											parent.sendSemiAutoPattern(tmp, "\""+selected_text.split("=")[0]+"\""+"(|+1)\""+selected_text.split("=")[1]+"\"<TXT "+selected_text.split("=")[1]+">");
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
											CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
											tmp.setTXTValue(selected_text.split(sw_splitter)[1]);

											parent.sendPatternValue(tmp);
//												Rule = "abcd"(|+1)["efgh"]
											parent.sendPatternRule("\""+sw_splitter+"\"(|+1)\""+selected_text.split(sw_splitter)[1]+"\"<TXT "+selected_text.split(sw_splitter)[1]+">");
											parent.sendSemiAutoPattern(tmp, "\""+sw_splitter+"\"(|+1)\""+selected_text.split(sw_splitter)[1]+"\"<TXT "+selected_text.split(sw_splitter)[1]+">");
											System.out.println("====> Match");
											return;
											
										}else {
											System.out.println("The selected text doesn't start with a stop word");
//											If NO	
//												Value = Correction (Selection)
											CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
											tmp.setTXTValue(corrected_text);

											parent.sendPatternValue(tmp);
//												Rule = [Selection]
											parent.sendPatternRule(WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<TXT "+WordUtils.QUOTE_NON_SEP_TEXT(selected_text,false)+">");
											parent.sendSemiAutoPattern(tmp, WordUtils.QUOTE_NON_SEP_TEXT(selected_text,true)+"<TXT "+WordUtils.QUOTE_NON_SEP_TEXT(selected_text,false)+">");
											System.out.println("Default match");
											return;
										}
									}
	

						}
//
			}
		}else if (active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
			
			selected_text = WordUtils.textWithoutParsedNumericalValues(selected_text);

//			The characteristic is a characteristic with Unit(s) of Measure?
			System.out.println("The characteristic is a characteristic with Unit(s) of Measure");
//					If YES
			boolean figureInSelection = selected_text.chars().mapToObj((c->(char) c)).anyMatch(c -> Character.isDigit(c));
			if(figureInSelection) {
//				The selection includes at least one figure?
				System.out.println("The selection includes at least one figure");
//				If YES
				
//					The selection includes 1 and only 1 numerical value (including decimals with "." or "," or negative values)?
				if(numValuesInSelection.size()==1) {
					System.out.println("The selection includes 1 and only 1 numerical value");
					boolean letterInSelection = figureInSelection = selected_text.chars().mapToObj((c->(char) c)).anyMatch(c -> Character.isAlphabetic(c));
					if(letterInSelection) {
//						The selection includes at least 1 letter
						System.out.println("The selection includes at least 1 letter");
						
						if(uomsInSelection.stream().filter(u->u!=null).collect(Collectors.toList()).size()>0) {
//							The numerical value is followed by a known unit of measure?
							System.out.println("The numerical value is followed by a known unit of measure");
							UnitOfMeasure following_uom = uomsInSelection.get(0);
							if(selected_text.toLowerCase().contains("max")) {
//								The selection contains "MAX"? (e.g. "Temp. Max.=50�C")
								System.out.println("The selection contains \"MAX\" (e.g. \"Temp. Max.=50�C\")");
								CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

								tmp.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								tmp.setUom_id(following_uom.getUom_id());
								parent.sendPatternValue(tmp);
								parent.sendPatternRule("\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<MAX %1><UOM \""+following_uom.getUom_symbol()+"\">");
								parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<MAX %1><UOM \""+following_uom.getUom_symbol()+"\">");
								return;
							}
							if(selected_text.toLowerCase().contains("min")&&!following_uom.getUom_name().contains("min")) {
/*									The selection contains "MIN" before the numerical value? (e.g. "Minimum voltage=12V")
* 									Satyam:
								Before the value only to avoid "MIN" =Minute in the unit of measure
								Ayman:
								Propsed bypass: allow "MIN" contained in text if unit of measure is not "minutes"
*/
								System.out.println("The selection contains \"MIN\" before the numerical value (e.g. \"Minimum voltage=12V\")");
								CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

								tmp.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								tmp.setUom_id(following_uom.getUom_id());
								parent.sendPatternValue(tmp);
								parent.sendPatternRule("\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<MIN %1><UOM \""+following_uom.getUom_symbol()+"\">");
								parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<MIN %1><UOM \""+following_uom.getUom_symbol()+"\">");
								return;
							}else {
								System.out.println("The selection does not contain \"MIN\" nor \"MAX\"  (e.g. \"LZ= 1160 MM\")");
								CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

								tmp.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								tmp.setUom_id(following_uom.getUom_id());
								parent.sendPatternValue(tmp);
								parent.sendPatternRule("\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<NOM %1><UOM \""+following_uom.getUom_symbol()+"\">");
								parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
										selected_text.replace(",", ".").indexOf("%1"))
								+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length())+"\""
										+"<NOM %1><UOM \""+following_uom.getUom_symbol()+"\">");
								return;
							}
							
						}else {
							// The numerical value is not followed by a known unit of measure
							System.out.println("The numerical value is not followed by a known unit of measure");
							if(selected_text.trim().endsWith("%1")) {
								//The numerical value is at the end of the selection
								System.out.println("The numerical value is at the end of the selection");
								if(active_char.getAllowedUoms().size()==1) {
//									Only 1 UoM is declared for the characteristic? (e.g. "MM")
									System.out.println("Only 1 UoM is declared for the characteristic? (e.g. \"MM\")");
									UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));
									if(selected_text.toLowerCase().contains("max")) {
//										The selection contains "MAX"?  (e.g. "Puissance Max=2000")
										System.out.println("The selection contains \"MAX\"  (e.g. \"Puissance Max=2000\")");
										CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

										tmp.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										tmp.setUom_id(infered_uom.getUom_id());
										parent.sendPatternValue(tmp);
										parent.sendPatternRule("\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MAX %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MAX %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										return;
									}
									if(selected_text.toLowerCase().contains("min")&&!infered_uom.getUom_name().contains("min")) {
	/*									The selection contains "MIN" before the numerical value? (e.g. "Minimum voltage=12")
	 * 									Satyam:
										Before the value only to avoid "MIN" =Minute in the unit of measure
										Ayman:
										Propsed bypass: allow "MIN" contained in text if unit of measure is not "minutes"
	*/
										System.out.println("The selection contains \"MIN\" before the numerical value (e.g. \"Minimum voltage=12\")");
										CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

										tmp.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										tmp.setUom_id(infered_uom.getUom_id());
										parent.sendPatternValue(tmp);
										parent.sendPatternRule("\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MIN %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MIN %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										return;
									}else {
										System.out.println("The selection does not contain \"MIN\" nor \"MAX\"  (e.g. \"LZ= 1160\")");
										CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);

										tmp.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										tmp.setUom_id(infered_uom.getUom_id());
										parent.sendPatternValue(tmp);
										parent.sendPatternRule("\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<NOM %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										parent.sendSemiAutoPattern(tmp, "\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<NOM %1><UOM \""+infered_uom.getUom_symbol()+"\">");
										return;
									}

								}else {
								//If NO (e.g. MM or IN)
								System.out.println("The characteristic has more than one UoM (e.g. MM or IN)");
								
									for(int i=0;i<active_char.getAllowedUoms().size();i++) {
										String loop_uom_id = active_char.getAllowedUoms().get(i);
										UnitOfMeasure loop_uom = UnitOfMeasure.RunTimeUOMS.get(loop_uom_id);
									if(selected_text.toLowerCase().contains("max")) {
	//									The selection contains "MAX"?  (e.g. "Long. Max=1160")
										System.out.println("The selection contains \"MAX\"  (e.g. \"Long. Max=1160\")");
										CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);

										preparedValue.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue.setUom_id(loop_uom.getUom_id());
										
										String preparedRule = "\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MAX %1><UOM \""+loop_uom.getUom_symbol()+"\">";
										
										parent.preparePatternProposition(i,"Max= "+preparedValue.getMax_value()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule,active_char);
										continue;
									}
									if(selected_text.toLowerCase().contains("min")&&!loop_uom.getUom_name().contains("min")) {
	/*									The selection contains "MIN" before the numerical value?  (e.g. "Long. Mini=1160")
	 * 									Satyam:
										Before the value only to avoid "MIN" =Minute in the unit of measure
										Ayman:
										Propsed bypass: allow "MIN" contained in text if unit of measure is not "minutes"
	*/
										System.out.println("The selection contains \"MIN\" before the numerical value  (e.g. \"Long. Mini=1160\")");
										CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue.setUom_id(loop_uom.getUom_id());
										
										String preparedRule = "\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<MIN %1><UOM \""+loop_uom.getUom_symbol()+"\">";
										
										parent.preparePatternProposition(i,"Min= "+preparedValue.getMin_value()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
										continue;
									}else {
										System.out.println("The selection does not contain \"MIN\" nor \"MAX\"  (e.g. \"LZ=1160\")");
										CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue.setUom_id(loop_uom.getUom_id());
	
										String preparedRule ="\""+selected_text.substring(0,
												selected_text.replace(",", ".").indexOf("%1"))
										+"\"(|+0)%1<NOM %1><UOM \""+loop_uom.getUom_symbol()+"\">";
										
										parent.preparePatternProposition(i,preparedValue.getNominal_value_truncated()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
										continue;
									}
									}return;
								
								
								
								
								
								}
							}else {
								//The numeric value is not at the end of the selection (e.g. "Puissance 2 XX", assuming that W is the declared UoM and kW is also known with a ratio 1000)
								System.out.println("The numeric value is not at the end of the selection (e.g. \"Puissance 2 XX\", assuming that W is the declared UoM and kW is also known with a ratio 1000)");
								//!!Multiple allowed uoms possible
								int i;
								for(i=0;i<active_char.getAllowedUoms().size();i++) {
									String loop_uom_id = active_char.getAllowedUoms().get(i);
									UnitOfMeasure loop_uom = UnitOfMeasure.RunTimeUOMS.get(loop_uom_id);
								if(selected_text.toLowerCase().contains("max")) {
//									The selection contains "MAX"?  (e.g. "Long. Max=1160")
									System.out.println("The selection contains \"MAX\"  (e.g. \"Long. Max=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id(loop_uom.getUom_id());
									System.out.println(selected_text);
									System.out.println(numValuesInSelection.get(0));
									String preparedRule = "\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<MAX %1><UOM \""+loop_uom.getUom_symbol()+"\">";
									
									parent.preparePatternProposition(i,"Max= "+preparedValue.getMax_value()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
									continue;
								}
								if(selected_text.toLowerCase().contains("min")&&!loop_uom.getUom_name().contains("min")) {
/*									The selection contains "MIN" before the numerical value?  (e.g. "Long. Mini=1160")
 * 									Satyam:
									Before the value only to avoid "MIN" =Minute in the unit of measure
									Ayman:
									Propsed bypass: allow "MIN" contained in text if unit of measure is not "minutes"
*/
									System.out.println("The selection contains \"MIN\" before the numerical value  (e.g. \"Long. Mini=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id(loop_uom.getUom_id());
									
									String preparedRule = "\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<MIN %1><UOM \""+loop_uom.getUom_symbol()+"\">";
									
									parent.preparePatternProposition(i,"Min= "+preparedValue.getMin_value()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
									continue;
								}else {
									System.out.println("The selection does not contain \"MIN\" nor \"MAX\"  (e.g. \"LZ=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id(loop_uom.getUom_id());

									String preparedRule ="\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<NOM %1><UOM \""+loop_uom.getUom_symbol()+"\">";
									
									parent.preparePatternProposition(i,preparedValue.getNominal_value_truncated()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
									continue;
								}
								}
								
								String UOM_INFERED_NAME=selected_text.substring(selected_text.replace(",", ".").indexOf("%1")
										+"%1".length()).trim();
								if(selected_text.toLowerCase().contains("max")) {
//									The selection contains "MAX"?  (e.g. "Long. Max=1160")
									System.out.println("The selection contains \"MAX\"  (e.g. \"Long. Max=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id("$$$UOM_ID$$$");
									
									String preparedRule = "\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<MAX %1><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
									
									parent.preparePatternProposition(i,"Max= "+preparedValue.getMax_value()+" \""+UOM_INFERED_NAME+"\"",preparedValue,preparedRule, active_char);
									return;
								}
								if(selected_text.toLowerCase().contains("min")&&!UOM_INFERED_NAME.contains("min")) {
/*									The selection contains "MIN" before the numerical value?  (e.g. "Long. Mini=1160")
 * 									Satyam:
									Before the value only to avoid "MIN" =Minute in the unit of measure
									Ayman:
									Propsed bypass: allow "MIN" contained in text if unit of measure is not "minutes"
*/
									System.out.println("The selection contains \"MIN\" before the numerical value  (e.g. \"Long. Mini=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id("$$$UOM_ID$$$");
									
									String preparedRule = "\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<MIN %1><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
									
									parent.preparePatternProposition(i,"Min= "+preparedValue.getMin_value()+" \""+UOM_INFERED_NAME+"\"",preparedValue,preparedRule, active_char);
									return;
								}else {
									System.out.println("The selection does not contain \"MIN\" nor \"MAX\"  (e.g. \"LZ=1160\")");
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id("$$$UOM_ID$$$");

									String preparedRule ="\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1(|+0)"+"\""+selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")+
													"%1".length())+"\""
											+"<NOM %1><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
									
									parent.preparePatternProposition(i,preparedValue.getNominal_value_truncated()+" \""+UOM_INFERED_NAME+"\"",preparedValue,preparedRule, active_char);
									return;
								}
								
								
							}
							
						}
					}else {
					//The selection contains no letters  (e.g. "1160")
						System.out.println("The selection contains no letters  (e.g. \"1160\")");
						if(active_char.getAllowedUoms().size()==1) {
//							Only 1 UoM is declared for the characteristic? (e.g. "MM")
							System.out.println("Only 1 UoM is declared for the characteristic? (e.g. \"MM\")");
							UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));
								CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
								tmp.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								tmp.setUom_id(infered_uom.getUom_id());
								parent.sendPatternValue(tmp);
								parent.sendPatternRule(null);
								parent.sendSemiAutoPattern(tmp,null);
								return;
							}else {
								for(int i=0;i<active_char.getAllowedUoms().size();i++) {
									String loop_uom_id = active_char.getAllowedUoms().get(i);
									UnitOfMeasure loop_uom = UnitOfMeasure.RunTimeUOMS.get(loop_uom_id);
									CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue.setUom_id(loop_uom.getUom_id());
									/*
									String preparedRule ="\""+selected_text.substring(0,
											selected_text.replace(",", ".").indexOf("%1"))
									+"\"(|+0)%1<NOM %1><UOM \""+loop_uom.getUom_symbol()+"\">";
									*/
									String preparedRule=null;
									parent.preparePatternProposition(i,preparedValue.getNominal_value_truncated()+" "+loop_uom.getUom_symbol()+" ("+loop_uom.getUom_name()+")",preparedValue,preparedRule, active_char);
									

								}return;
							
							}


						
					}
				}else {
					if(numValuesInSelection.size() == 2) {
//						The selection includes 2 and only 2 numerical values (including decimals with "." or "," or negative values)
						System.out.println("The selection includes 2 and only 2 numerical values (including decimals with \".\" or \",\" or negative values)");
						String inbetweenNumbersText = selected_text.substring(
							selected_text.replace(",", ".").indexOf("%1")+
									"%1".length(),
							selected_text.replace(",", ".").indexOf("%2")
							);
						inbetweenNumbersText = String.join("",  inbetweenNumbersText.chars().mapToObj((c -> (char) c)).
						map(c->(Character.isAlphabetic(c)||Character.isDigit(c)||c.equals('/')||c.equals(':'))?String.valueOf(c):"(|+0)").
						collect(Collectors.toList()));
						System.out.println("inbetweenNumbersText");
						System.out.println(inbetweenNumbersText);
						if(WordUtils.RuleSyntaxContainsSep(inbetweenNumbersText, "/")
								||WordUtils.RuleSyntaxContainsSep(inbetweenNumbersText, ":")
								) {
							//The 2 numerical values are separated by "/" or ":"? More precisely: the selection follows the pattern (X+0)%(|+0)"/"(|+0)%(X+0) where "/" can also be replaced by ":" (e.g.  "Size 1/2"")
							System.out.println("The 2 numerical values are separated by \"/\" or \":\"");
							
							if(uomsInSelection.get(1)!=null) {
							//The 2nd numerical value is followed by a known unit of measure?
								System.out.println("The 2nd numerical value is followed by a known unit of measure");
								CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
								preparedValue1.setUom_id(uomsInSelection.get(1).getUom_id());
								preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
								String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%2")+
														"%2".length())
										+"\""+"<NOM %1/%2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
								parent.preparePatternProposition(0, preparedValue1.getNominal_value_truncated()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue1, preparedRule1, active_char);
								
								
								CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
								preparedValue2.setUom_id(uomsInSelection.get(1).getUom_id());
								preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
								
								String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%2")+
														"%2".length())
										+"\""+"<MINMAX %1><MINMAX %2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
								parent.preparePatternProposition(1, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue2, preparedRule2, active_char);
								return;
							}else {
								if(selected_text.replace(",", ".").trim().endsWith("%2")) {
									//The 2nd numerical value is at the end of the selection?
									System.out.println("The 2nd numerical value is at the end of the selection");
									if(active_char.getAllowedUoms().size()==1) {
									//Only 1 UoM is declared for the characteristic? (e.g. "Tension 12/24")
										System.out.println("Only 1 UoM is declared for the characteristic");
										UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));

										CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
										preparedValue3.setUom_id(infered_uom.getUom_id());
										preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
												+"<NOM %1><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(0, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
										
										CaracteristicValue preparedValue4 = new CaracteristicValue();;;; preparedValue4.setParentChar(active_char);
										preparedValue4.setUom_id(infered_uom.getUom_id());
										preparedValue4.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										String preparedRule4 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
												+"<NOM %2><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(1, preparedValue4.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue4, preparedRule4, active_char);

										
										CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setUom_id(infered_uom.getUom_id());
										preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
												+"<NOM %1/%2><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(2, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id(infered_uom.getUom_id());
										preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
												+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(3, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
										
										
										return;
									}else {
										//!!Multiple allowed uoms possible
										int i;
										for(i=0;i<active_char.getAllowedUoms().size();i++) {
											String infered_uom_id = active_char.getAllowedUoms().get(i);
											UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
											CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
											preparedValue1.setUom_id(infered_uom.getUom_id());
											preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
											String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
													+"<NOM %1/%2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(2*i, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
											
											
											CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
											preparedValue2.setUom_id(infered_uom.getUom_id());
											preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											
											String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2"
													+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(2*i+1, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
											continue;
										}
										return;
									}
								}else {
									int i;
									for(i=0;i<active_char.getAllowedUoms().size();i++) {
										String infered_uom_id = active_char.getAllowedUoms().get(i);
										UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
										CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setUom_id(infered_uom.getUom_id());
										preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %1/%2><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(2*i, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id(infered_uom.getUom_id());
										preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
										parent.preparePatternProposition(2*i+1, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
										continue;
									}
									String UOM_INFERED_NAME=selected_text.substring(selected_text.replace(",", ".").indexOf("%2")
											+"%2".length()).trim();
									CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
									preparedValue1.setUom_id("$$$UOM_ID$$$");
									preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
									String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
											"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
											+"\""+selected_text.substring(
													selected_text.replace(",", ".").indexOf("%2")+
															"%2".length())
											+"\""+"<NOM %1/%2><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
									parent.preparePatternProposition(2*i, preparedValue1.getNominal_value_truncated()+" \""+UOM_INFERED_NAME+"\"", preparedValue1, preparedRule1, active_char);
									
									
									CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
									preparedValue2.setUom_id("$$$UOM_ID$$$");
									preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									
									String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
											"%1"))+"\"(|+0)%1(|+0)\"/\"(|+0)%2(|+0)"
											+"\""+selected_text.substring(
													selected_text.replace(",", ".").indexOf("%2")+
															"%2".length())
											+"\""+"<MINMAX %1><MINMAX %2><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
									parent.preparePatternProposition(2*i+1, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" \""+UOM_INFERED_NAME+"\"", preparedValue2, preparedRule2, active_char);
									
									
									return;
								}
							}
						}else {
							inbetweenNumbersText = selected_text.substring(
									selected_text.replace(",", ".").indexOf("%1")+
											"%1".length(),
									selected_text.replace(",", ".").indexOf("%2")
									);
								
							inbetweenNumbersText = String.join("", inbetweenNumbersText.chars().mapToObj((c -> (char) c)).
							map(c->(Character.isAlphabetic(c)||Character.isDigit(c)||c.equals('+'))?String.valueOf(c):"(|+0)").
							collect(Collectors.toList()));
							if(WordUtils.RuleSyntaxContainsSep(inbetweenNumbersText, "+")) {
								System.out.println("The 2 numerical values are separated by \"+\"");
								
								if(uomsInSelection.get(1)!=null) {
								//The 2nd numerical value is followed by a known unit of measure?
									System.out.println("The 2nd numerical value is followed by a known unit of measure");
									
									CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setUom_id(uomsInSelection.get(1).getUom_id());
										preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %1><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.preparePatternProposition(0, preparedValue1.getNominal_value_truncated()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id(uomsInSelection.get(1).getUom_id());
										preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.preparePatternProposition(1, preparedValue2.getNominal_value_truncated()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue2, preparedRule2, active_char);
										
										
										

										CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
										preparedValue3.setUom_id(uomsInSelection.get(1).getUom_id());
										preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
										String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %1+%2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.preparePatternProposition(2, preparedValue3.getNominal_value_truncated()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue3, preparedRule3, active_char);
										
										
										return;
								}else {
									if(selected_text.replace(",", ".").trim().endsWith("%2")) {
										//The 2nd numerical value is at the end of the selection?
										System.out.println("The 2nd numerical value is at the end of the selection");
										int i;
										for(i=0;i<active_char.getAllowedUoms().size();i++) {
											String infered_uom_id = active_char.getAllowedUoms().get(i);
											UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
											CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
											preparedValue1.setUom_id(infered_uom.getUom_id());
											preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2"
													+"<NOM %1><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
											
											
											CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
											preparedValue2.setUom_id(infered_uom.getUom_id());
											preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2"
													+"<NOM %2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i+1, preparedValue2.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
											
											
											

											CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
											preparedValue3.setUom_id(infered_uom.getUom_id());
											preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
											String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2"
													+"<NOM %1+%2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i+2, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
											
											
											continue;
										}
										return;
									}else {
										int i;
										for(i=0;i<active_char.getAllowedUoms().size();i++) {
											String infered_uom_id = active_char.getAllowedUoms().get(i);
											UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
											CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
											preparedValue1.setUom_id(infered_uom.getUom_id());
											preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%2")+
																	"%2".length())
													+"\""+"<NOM %1><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
											
											
											CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
											preparedValue2.setUom_id(infered_uom.getUom_id());
											preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%2")+
																	"%2".length())
													+"\""+"<NOM %2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i+1, preparedValue2.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
											
											
											

											CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
											preparedValue3.setUom_id(infered_uom.getUom_id());
											preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
											String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%2")+
																	"%2".length())
													+"\""+"<NOM %1+%2><UOM \""+infered_uom.getUom_symbol()+"\">";
											parent.preparePatternProposition(3*i+2, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
											
											
											continue;
										}
										
										
										String UOM_INFERED_NAME=selected_text.substring(selected_text.replace(",", ".").indexOf("%2")
												+"%2".length()).trim();
										CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setUom_id("$$$UOM_ID$$$");
										preparedValue1.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %1><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
										parent.preparePatternProposition(3*i, preparedValue1.getNominal_value_truncated()+" \""+UOM_INFERED_NAME, preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id("$$$UOM_ID$$$");
										preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %2><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
										parent.preparePatternProposition(3*i+1, preparedValue2.getNominal_value_truncated()+" \""+UOM_INFERED_NAME, preparedValue2, preparedRule2, active_char);
										
										
										

										CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
										preparedValue3.setUom_id("$$$UOM_ID$$$");
										preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
										String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\"+\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %1+%2><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
										parent.preparePatternProposition(3*i+2, preparedValue3.getNominal_value_truncated()+" \""+UOM_INFERED_NAME, preparedValue3, preparedRule3, active_char);
										
										
										
										return;
									}
								}
							
							}else{
								
								inbetweenNumbersText = selected_text.substring(
										selected_text.replace(",", ".").indexOf("%1")+
												"%1".length(),
										selected_text.replace(",", ".").indexOf("%2")
										);
									
								inbetweenNumbersText = String.join("", inbetweenNumbersText.chars().mapToObj((c -> (char) c)).
								map(c->(Character.isAlphabetic(c)||Character.isDigit(c)||c.equals('-'))?String.valueOf(c):"(|+0)").
								collect(Collectors.toList()));
								if(WordUtils.RuleSyntaxContainsSep(inbetweenNumbersText, "-")
										){
								//The 2 numerical values are separated by "-"?
									System.out.println("The 2 numerical values are separated by \"-\"");
									
									if(uomsInSelection.get(1)!=null) {
									//The 2nd numerical value is followed by a known unit of measure?
										System.out.println("The 2nd numerical value is followed by a known unit of measure");
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id(uomsInSelection.get(1).getUom_id());
										preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\":\"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<MINMAX %1><MINMAX %2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.sendPatternValue(preparedValue2);
										parent.sendPatternRule(preparedRule2);
										parent.sendSemiAutoPattern(preparedValue2,preparedRule2);
										return;
									}else {
										if(selected_text.replace(",", ".").trim().endsWith("%2")) {
											//The 2nd numerical value is at the end of the selection?
											System.out.println("The 2nd numerical value is at the end of the selection");
											if(active_char.getAllowedUoms().size()==1) {
											//Only 1 UoM is declared for the characteristic? (e.g. "Tension 12:24")
												System.out.println("Only 1 UoM is declared for the characteristic");
												UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));
													
												
												CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
												preparedValue2.setUom_id(infered_uom.getUom_id());
												preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												
												String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
														"%1"))+"\"(|+0)%1(|+0)\":\"(|+0)%2"
														+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
												parent.sendPatternValue(preparedValue2);
												parent.sendPatternRule(preparedRule2);
												parent.sendSemiAutoPattern(preparedValue2,preparedRule2);
												return;
											}else {
												int i;
												for(i=0;i<active_char.getAllowedUoms().size();i++) {
													String infered_uom_id = active_char.getAllowedUoms().get(i);
													UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
														
													
													CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
													preparedValue2.setUom_id(infered_uom.getUom_id());
													preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
													preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
													
													String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
															"%1"))+"\"(|+0)%1(|+0)\":\"(|+0)%2"
															+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
													parent.preparePatternProposition(i, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
													continue;
												}
												return;
											}
										}else {
											int i;
											for(i=0;i<active_char.getAllowedUoms().size();i++) {
												String infered_uom_id = active_char.getAllowedUoms().get(i);
												UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
													
												
												CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
												preparedValue2.setUom_id(infered_uom.getUom_id());
												preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												
												String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
														"%1"))+"\"(|+0)%1(|+0)\":\"(|+0)%2(|+0)"
														+"\""+selected_text.substring(
																selected_text.replace(",", ".").indexOf("%2")+
																		"%2".length())
														+"\""+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom.getUom_symbol()+"\">";
												parent.preparePatternProposition(i, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
												continue;
											}
											String UOM_INFERED_NAME=selected_text.substring(selected_text.replace(",", ".").indexOf("%2")
													+"%2".length()).trim();
											
											
											CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
											preparedValue2.setUom_id("$$$UOM_ID$$$");
											preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											
											String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\":\"(|+0)%2(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%2")+
																	"%2".length())
													+"\""+"<MINMAX %1><MINMAX %2><UOM \""+"$$$UOM_SYMBOL$$$"+"\">";
											parent.preparePatternProposition(i, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" \""+UOM_INFERED_NAME+"\"", preparedValue2, preparedRule2, active_char);
											
											
											return;
										}
									}
									
									
									
								}else {
									inbetweenNumbersText = selected_text.substring(
											selected_text.replace(",", ".").indexOf("%1")
											+"%1".length(),
											selected_text.replace(",", ".").indexOf("%2")
											);
										
									
									if(uomsInSelection.get(1)!=null) {
									//The 2nd numerical value is followed by a known unit of measure
										System.out.println("The 2nd numerical value is followed by a known unit of measure");
										CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedValue1.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										preparedValue1.setUom_id(uomsInSelection.get(1).getUom_id());
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\""
												+inbetweenNumbersText+"\""+"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<MINMAX %1><MINMAX %2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.preparePatternProposition(0, preparedValue1.getMin_value()+" to "+preparedValue1.getMax_value()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										preparedValue2.setUom_id(uomsInSelection.get(1).getUom_id());
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\""
												+inbetweenNumbersText+"\""+"(|+0)%2(|+0)"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%2")+
																"%2".length())
												+"\""+"<NOM %2><UOM \""+uomsInSelection.get(1).getUom_symbol()+"\">";
										parent.preparePatternProposition(1, preparedValue2.getNominal_value_truncated()+" "+uomsInSelection.get(1).getUom_symbol()+" ("+uomsInSelection.get(1).getUom_name()+")", preparedValue2, preparedRule2, active_char);
										return;
									}else {
										if(uomsInSelection.get(0)!=null) {
											//The 1st numerical value is followed by a known unit of measure
											System.out.println("The 1st numerical value is followed by a known unit of measure");
											CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
											preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedValue2.setUom_id(uomsInSelection.get(0).getUom_id());
											String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\""
													+inbetweenNumbersText+"\""+"(|+0)%2(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%2")+
																	"%2".length())
													+"\""+"<NOM %1><UOM \""+uomsInSelection.get(0).getUom_symbol()+"\">";
											parent.sendPatternValue(preparedValue2);
											parent.sendPatternRule(preparedRule2);
											parent.sendSemiAutoPattern(preparedValue2,preparedRule2);
											return;
										}else {
											// (e.g. "Pression = 10 @T=100") with "bar" and "psi" as declared UoM
											int i;
											for(i=0;i<active_char.getAllowedUoms().size();i++) {
												String infered_uom_id = active_char.getAllowedUoms().get(i);
												UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(infered_uom_id);
												CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
												preparedValue1.setUom_id(infered_uom.getUom_id());
												preparedValue1.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedValue1.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												
												String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
														"%1"))+"\"(|+0)%1(|+0)\""
														+inbetweenNumbersText+"\""+"(|+0)%2"
														+"\""+selected_text.substring(
																selected_text.replace(",", ".").indexOf("%2")+
																		"%2".length())
														+"\""+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom+"\">";
												parent.preparePatternProposition(3*i, preparedValue1.getMin_value()+" to "+preparedValue1.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
												
												
												CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
												preparedValue2.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedValue2.setUom_id(infered_uom.getUom_id());
												String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
														"%1"))+"\"(|+0)%1(|+0)\""
														+inbetweenNumbersText+"\""+"(|+0)%2"
														+"\""+selected_text.substring(
																selected_text.replace(",", ".").indexOf("%2")+
																		"%2".length())
														+"\""+"<NOM %1><UOM \""+infered_uom+"\">";
												parent.preparePatternProposition(3*i+1, preparedValue2.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
												
												CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
												preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												preparedValue3.setUom_id(infered_uom.getUom_id());
												String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
														"%1"))+"\"(|+0)%1(|+0)\""
														+inbetweenNumbersText+"\""+"(|+0)%2"
														+"\""+selected_text.substring(
																selected_text.replace(",", ".").indexOf("%2")+
																		"%2".length())
														+"\""+"<NOM %2><UOM \""+infered_uom+"\">";
												parent.preparePatternProposition(3*i+2, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
												
												
												continue;
											}
											return;
										}
									}
								}
							}
						}
					}else {
						System.out.println("The selection includes "+numValuesInSelection.size()+" and only "+numValuesInSelection.size()+" numerical values (including decimals with \".\" or \",\" or negative values)");
						if(numValuesInSelection.size()==3) {
							//The selection includes 3 and only 3 numerical values (including decimals with "." or "," or negative values)
							System.out.println("The selection includes 3 and only 3 numerical values (including decimals with \".\" or \",\" or negative values)");
							
							String inbetweenNumbersText1 = selected_text.substring(
									selected_text.replace(",", ".").indexOf("%1")
									+"%1".length(),
									selected_text.replace(",", ".").indexOf("%2")
									);
							
							System.out.println(WordUtils.DoubleToString(numValuesInSelection.get(0)));
							System.out.println(numValuesInSelection.get(0));
							System.out.println(WordUtils.DoubleToString(numValuesInSelection.get(1)));
							System.out.println(numValuesInSelection.get(1));
							System.out.println(WordUtils.DoubleToString(numValuesInSelection.get(2)));
							System.out.println(numValuesInSelection.get(2));
							
							String inbetweenNumbersText2 = selected_text.substring(
									selected_text.replace(",", ".").indexOf("%2")
									+"%2".length(),
									selected_text.replace(",", ".").indexOf("%3")
									);
								
							if(uomsInSelection.get(2)!=null) {
								//The 3rd numerical value is followed by a known unit of measure
								System.out.println("The 3rd numerical value is followed by a known unit of measure");
								
								UnitOfMeasure infered_uom = uomsInSelection.get(2);
								

								CaracteristicValue preparedValueA = new CaracteristicValue();;;; preparedValueA.setParentChar(active_char);
								preparedValueA.setUom_id(infered_uom.getUom_id());
								preparedValueA.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								
								String preparedRuleA = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\""
										+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
										+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%3")+
														"%3".length())
										+"\""+"<NOM %1><UOM \""+infered_uom+"\">";
								parent.preparePatternProposition(0, preparedValueA.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValueA, preparedRuleA, active_char);
								
								
								CaracteristicValue preparedValueB = new CaracteristicValue();;;; preparedValueB.setParentChar(active_char);
								preparedValueB.setUom_id(infered_uom.getUom_id());
								preparedValueB.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
								
								String preparedRuleB = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\""
										+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
										+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%3")+
														"%3".length())
										+"\""+"<NOM %2><UOM \""+infered_uom+"\">";
								parent.preparePatternProposition(1, preparedValueB.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValueB, preparedRuleB, active_char);
								
								CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
								preparedValue3.setUom_id(infered_uom.getUom_id());
								preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
								
								String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\""
										+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
										+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%3")+
														"%3".length())
										+"\""+"<NOM %3><UOM \""+infered_uom+"\">";
								parent.preparePatternProposition(2, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
								
								
								
								
								CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
								preparedValue1.setUom_id(infered_uom.getUom_id());
								preparedValue1.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								preparedValue1.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
								
								String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\""
										+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
										+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%3")+
														"%3".length())
										+"\""+"<MINMAX %1><MINMAX %3><UOM \""+infered_uom+"\">";
								parent.preparePatternProposition(3, preparedValue1.getMin_value()+" to "+preparedValue1.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
								
								CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
								preparedValue2.setUom_id(infered_uom.getUom_id());
								preparedValue2.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
								preparedValue2.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
								
								String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
										"%1"))+"\"(|+0)%1(|+0)\""
										+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
										+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
										+"\""+selected_text.substring(
												selected_text.replace(",", ".").indexOf("%3")+
														"%3".length())
										+"\""+"<MINMAX %2><MINMAX %3><UOM \""+infered_uom+"\">";
								parent.preparePatternProposition(4, preparedValue2.getMin_value()+" to "+preparedValue2.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
								
								
								
								
							}else {
								if(uomsInSelection.get(1)!=null) {
									//The 2nd numerical value is followed by a known unit of measure
									System.out.println("The 2nd numerical value is followed by a known unit of measure");
									
									UnitOfMeasure infered_uom = uomsInSelection.get(1);
									
									CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
									preparedValue1.setUom_id(infered_uom.getUom_id());
									preparedValue1.setMin_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedValue1.setMax_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									
									String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
											"%1"))+"\"(|+0)%1(|+0)\""
											+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
											+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
											+"\""+selected_text.substring(
													selected_text.replace(",", ".").indexOf("%3")+
															"%3".length())
											+"\""+"<MINMAX %1><MINMAX %2><UOM \""+infered_uom+"\">";
									parent.preparePatternProposition(0, preparedValue1.getMin_value()+" to "+preparedValue1.getMax_value()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
									
									
									CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
									preparedValue3.setUom_id(infered_uom.getUom_id());
									preparedValue3.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									
									String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
											"%1"))+"\"(|+0)%1(|+0)\""
											+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
											+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
											+"\""+selected_text.substring(
													selected_text.replace(",", ".").indexOf("%3")+
															"%3".length())
											+"\""+"<NOM %2><UOM \""+infered_uom+"\">";
									parent.preparePatternProposition(1, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
									
								}else {
									if(uomsInSelection.get(0)!=null) {
										//The 1st numerical value is followed by a known unit of measure
										System.out.println("The 1st numerical value is followed by a known unit of measure");
										if(WordUtils.RuleSyntaxContainsSep(String.join("",inbetweenNumbersText2.chars().mapToObj((c -> (char) c)).
												map(c->(Character.isAlphabetic(c)||Character.isDigit(c)||c.equals('/'))?String.valueOf(c):"(|+0)").
												collect(Collectors.toList())),"/")) {
											//The values 2 and 3 are separated by "(|+0)/(|+0)"
											System.out.println("The values 2 and 3 are separated by \"(|+0)/(|+0)\"");
											
											UnitOfMeasure infered_uom = uomsInSelection.get(0);
											CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
											preparedValue3.setUom_id(infered_uom.getUom_id());
											preparedValue3.setNominal_value(WordUtils.DoubleToString(
													numValuesInSelection.get(0)+
													numValuesInSelection.get(1)/numValuesInSelection.get(2)
													));
											
											String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\""
													+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
													+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%3")+
																	"%3".length())
													+"\""+"<NOM %1+%2/%3><UOM \""+infered_uom+"\">";
											
											parent.sendPatternValue(preparedValue3);
											parent.sendPatternRule(preparedRule3);
											parent.sendSemiAutoPattern(preparedValue3,preparedRule3);
											
										}else {
											
											UnitOfMeasure infered_uom = uomsInSelection.get(0);
											CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
											preparedValue3.setUom_id(infered_uom.getUom_id());
											preparedValue3.setNominal_value(WordUtils.DoubleToString(
													numValuesInSelection.get(0)
													));
											
											String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
													"%1"))+"\"(|+0)%1(|+0)\""
													+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
													+inbetweenNumbersText2+"\""+"(|+0)%3(|+0)"
													+"\""+selected_text.substring(
															selected_text.replace(",", ".").indexOf("%3")+
																	"%3".length())
													+"\""+"<NOM %1><UOM \""+infered_uom+"\">";
											
											parent.sendPatternValue(preparedValue3);
											parent.sendPatternRule(preparedRule3);
											parent.sendSemiAutoPattern(preparedValue3,preparedRule3);
											
										}
									}else {
										UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));
										
										CaracteristicValue preparedValue1 = new CaracteristicValue();;;; preparedValue1.setParentChar(active_char);
										preparedValue1.setUom_id(infered_uom.getUom_id());
										preparedValue1.setNominal_value(WordUtils.DoubleToString(
												numValuesInSelection.get(0)
												));
										
										String preparedRule1 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\""
												+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
												+inbetweenNumbersText2+"\""+"(|+0)%3"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%3")+
																"%3".length())
												+"\""+"<NOM %1><UOM \""+infered_uom+"\">";
										
										parent.preparePatternProposition(0, preparedValue1.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue1, preparedRule1, active_char);
										
										
										CaracteristicValue preparedValue2 = new CaracteristicValue();;;; preparedValue2.setParentChar(active_char);
										preparedValue2.setUom_id(infered_uom.getUom_id());
										preparedValue2.setNominal_value(WordUtils.DoubleToString(
												numValuesInSelection.get(1)
												));
										
										String preparedRule2 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\""
												+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
												+inbetweenNumbersText2+"\""+"(|+0)%3"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%3")+
																"%3".length())
												+"\""+"<NOM %2><UOM \""+infered_uom+"\">";
										
										parent.preparePatternProposition(1, preparedValue2.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue2, preparedRule2, active_char);
										
										
										CaracteristicValue preparedValue3 = new CaracteristicValue();;;; preparedValue3.setParentChar(active_char);
										preparedValue3.setUom_id(infered_uom.getUom_id());
										preparedValue3.setNominal_value(WordUtils.DoubleToString(
												numValuesInSelection.get(2)
												));
										
										String preparedRule3 = "\""+selected_text.substring(0,selected_text.replace(",", ".").indexOf(
												"%1"))+"\"(|+0)%1(|+0)\""
												+inbetweenNumbersText1+"\""+"(|+0)%2(|+0)\""
												+inbetweenNumbersText2+"\""+"(|+0)%3"
												+"\""+selected_text.substring(
														selected_text.replace(",", ".").indexOf("%3")+
																"%3".length())
												+"\""+"<NOM %3><UOM \""+infered_uom+"\">";
										
										parent.preparePatternProposition(2, preparedValue3.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue3, preparedRule3, active_char);
										
									}
								}
								
							}
						}else {
							//The selection contains 4+ digits
							System.out.println("4+ digits in selection");

							ArrayList<String> textBetweenNumbers = new ArrayList<String>();
							String[] textBetweenNumberstmp = (selected_text+" ").split("%\\d");
							for(int i=0;i<textBetweenNumberstmp.length;i++) {
								textBetweenNumbers.add("\""+textBetweenNumberstmp[i]+"\"");
							}
							UnitOfMeasure infered_uom = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0));
							
							for(int i=0;i<numValuesInSelection.size();i++) {
								CaracteristicValue preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
								preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(i)));
								preparedValue.setUom_id(infered_uom.getUom_id());
								String preparedRule = WordUtils.generateRuleSyntax(
										textBetweenNumbers, new String[] {"NOM"}, new String [] {"%"+String.valueOf(i+1)}, null);
								parent.preparePatternProposition(
										preparedValue.getNominal_value_truncated()+" "+infered_uom.getUom_symbol()+" ("+infered_uom.getUom_name()+")", preparedValue, preparedRule, active_char);
								
							}
						}
					}
					
				}
			}else {
				System.out.println("the selection includes no numerical value");
//				If NO (the selection includes no numerical value e.g. "abcd")																																			
//				Do nothing																																		
//
				return;
			}
			
		}else {
			System.out.println("The char is numeric w/o UoM");
			int number_of_digits = selected_text.chars().mapToObj((c -> (char) c)).filter(c->Character.isDigit(c)).collect(Collectors.toList()).size();
			int number_of_chars = selected_text.chars().mapToObj((c -> (char) c)).filter(c->Character.isAlphabetic(c)).collect(Collectors.toList()).size();
			selected_text = WordUtils.textWithoutParsedNumericalValues(selected_text);

			ArrayList<String> textBetweenNumbers = new ArrayList<String>();
			String[] textBetweenNumberstmp = (selected_text+" ").split("%\\d");
			for(int i=0;i<textBetweenNumberstmp.length;i++) {
				textBetweenNumbers.add("\""+textBetweenNumberstmp[i]+"\"");
			}
			
			CaracteristicValue preparedValue;
			String preparedRule;
			
			if(number_of_digits>0) {
				System.out.println("The selection contains at least 1 digit");
				if(numValuesInSelection.size()==1) {
					System.out.println("The selection includes no more than one numeric value");
					if(number_of_chars>0) {
						System.out.println("The selection includes at least one alphabetic char");
						CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
						tmp.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
						parent.sendPatternValue(tmp);
						parent.sendPatternRule(WordUtils.generateRuleSyntax(
								textBetweenNumbers,new String[] {"NOM"},new String[] {"%1"},null
								));
						parent.sendSemiAutoPattern(tmp,WordUtils.generateRuleSyntax(
								textBetweenNumbers,new String[] {"NOM"},new String[] {"%1"},null
								));
					}else {
						System.out.println("No letter in selection");
						CaracteristicValue tmp = new CaracteristicValue();;;; tmp.setParentChar(active_char);
						tmp.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
						parent.sendPatternValue(tmp);
						parent.sendPatternRule(null);
						parent.sendSemiAutoPattern(tmp,null);
					}
				}else {
					if(numValuesInSelection.size()==2) {
						System.out.println("The selection contains exactly 2 nums");
						if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "x")
								||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "X")
								||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "*")){
							
							System.out.println("The 2 numerical values are separated by \"X\" or \"*\"? More precisely: the selection follows the pattern (X+0)%(|+0)\"X\"(|+0)%(X+0) where \"X\" can also be replaced by \"*\" (e.g.  \"CONDUCTORS: 5x1.50 SQMM\")");
							
							preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
							preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
							preparedRule = WordUtils.generateRuleSyntax(
									textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
							parent.preparePatternProposition(
									preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
							
							preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
							preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
							preparedRule = WordUtils.generateRuleSyntax(
									textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
							parent.preparePatternProposition(
									preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
							
							preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
							preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)));
							preparedRule = WordUtils.generateRuleSyntax(
									textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2"}, null);
							parent.preparePatternProposition(
									preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
						}else {
							if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "+")){
								
								System.out.println("The 2 numerical values are separated by \"+\" ?");
								
								preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
								preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
								preparedRule = WordUtils.generateRuleSyntax(
										textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
								parent.preparePatternProposition(
										preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
								
								preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
								preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
								preparedRule = WordUtils.generateRuleSyntax(
										textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
								parent.preparePatternProposition(
										preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
								
								preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
								preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
								preparedRule = WordUtils.generateRuleSyntax(
										textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2"}, null);
								parent.preparePatternProposition(
										preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
							}else {
								if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "/")
										||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), ":")){
									
									System.out.println("The 2 numerical values are separated by \"/\" ?");
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1/%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
								}else {
									System.out.println("The 2 numerical values are not separated by + or / or *");
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1/%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
								}
							}
						}
					}else {
						if(numValuesInSelection.size()==3) {
							System.out.println("The selection includes exactly 3 numerical values");
							if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "x")
									||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "X")
									||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "*")) {
								System.out.println("The numerical values 1 and 2 are separated by *");
								
								if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "x")
									||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "X")
									||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "*")) {
									System.out.println("The numerical values 2 and 3 are separated by *");
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)*numValuesInSelection.get(2)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2*%3"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									
								}else {
									if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "+")){
										System.out.println("Num 2 and 3 are separated by +");
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
									}else {
										System.out.println("Num 2 and 3 are not separated by +");
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									}
								}
							}else {
								if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "+")){
									System.out.println("Num 1 and 2 are separated by +");
									if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "x")
											||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "X")
											||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "*")) {
										System.out.println("The numerical values 2 and 3 are separated by *");
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)*numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2*%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
									}else {
										if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "+")){
											System.out.println("Num 2 and 3 are separated with +");
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)+numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2+%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
										}else {
											System.out.println("Num val 2 and 3 not separted by +");
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)+numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2+%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
										}
									}
								}else {
									if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "/")
											||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), ":")){
										System.out.println("Num values 1 and 2 separated by /");
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1/%2"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										
										preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
										preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)/numValuesInSelection.get(1)+numValuesInSelection.get(2)));
										preparedRule = WordUtils.generateRuleSyntax(
												textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1/%2+%3"}, null);
										parent.preparePatternProposition(
												preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
										
										
									}else {
										if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "x")
												||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "X")
												||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "*")) {
											System.out.println("The numerical values 2 and 3 are separated by *");
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)*numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2*%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											
											preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
											preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)*numValuesInSelection.get(2)));
											preparedRule = WordUtils.generateRuleSyntax(
													textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2*%3"}, null);
											parent.preparePatternProposition(
													preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
											
											
										}else {
											if(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), ":")
													||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "/")){
												System.out.println("Num values 2 and 3 are separated by /");
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)/numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2/%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)/numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2/%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
											}else {
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(1)+numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%2+%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
												preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)*numValuesInSelection.get(2)));
												preparedRule = WordUtils.generateRuleSyntax(
														textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2*%3"}, null);
												parent.preparePatternProposition(
														preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
												
												
											}
										}
									}
								}
								
							}
						}else {
							if(numValuesInSelection.size()==4) {
								System.out.println("There are exactly 4 numeric values in selection");
								if(
										(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "x")
												||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "X")
												||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(1), "*"))
										
									&&  (WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(2), "+"))
									
									&&	(WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(3), "x")
											||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(3), "X")
											||WordUtils.FreeRuleSyntaxContainsSep(textBetweenNumbers.get(3), "*"))
									
										) {
									System.out.println("The values are separated respectively by \"X\" or \"*\", \"+\" and \"X\" or \"*\"");
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)+numValuesInSelection.get(2)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1+%3"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)*numValuesInSelection.get(1)+numValuesInSelection.get(2)*numValuesInSelection.get(3)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1*%2+%3*%4"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									
								}else {
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(0)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%1"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(1)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%2"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(2)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%3"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(3)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%4"}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
								}
							}else {
								System.out.println("There are more than 4 numerical values");
								
								for(int i=0;i<numValuesInSelection.size();i++) {
									preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
									preparedValue.setNominal_value(WordUtils.DoubleToString(numValuesInSelection.get(i)));
									preparedRule = WordUtils.generateRuleSyntax(
											textBetweenNumbers, new String[] {"NOM"}, new String [] {"%"+String.valueOf(i+1)}, null);
									parent.preparePatternProposition(
											preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
									
								}
								
								
							}
						}
							
					}
				}
			}else {
				System.out.println("There are no numeric values in selection");
				for(int i=0;i<5;i++) {
					preparedValue = new CaracteristicValue();;;; preparedValue.setParentChar(active_char);
					preparedValue.setNominal_value(String.valueOf(i+1));
					preparedRule = WordUtils.generateRuleSyntax(
							textBetweenNumbers, new String[] {""}, new String [] {String.valueOf(i+1)+" "}, null);
					parent.preparePatternProposition(
							preparedValue.getNominal_value_truncated(), preparedValue, preparedRule, active_char);
					
				}
			}
			
		}
	}

	private static String beginsWithStopWords(String selected_text,UserAccount account, ClassCaracteristic active_char) {
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

		
		
		
		for(String key:specialwords.keySet()) {
			LinkedHashSet<String> tmp = specialwords.get(key);
			for(String sw:tmp) {
				if(sw!=null){
				}else{
					continue;
				}
				if(WordUtils.CORRECT(selected_text).toUpperCase().startsWith(sw.toUpperCase())){
					return selected_text.substring(0,sw.length());
				}
				
			}
		}
		
		return null;
	}

	public static void applyItemRule(Char_description parent) {
		
		if(!(charIdArrays!=null)) {
			charIdArrays = new HashMap<String,List<String>>();
			CharValuesLoader.active_characteristics.forEach((k,v)->{
			charIdArrays.put(k, v.stream().map(c->c.getCharacteristic_id()).collect(Collectors.toList()));	
			});
		}
		
		
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		String activeRule = parent.rule_field.getText();
		if(activeRule!=null && activeRule.replaceAll(" ", "").length()>0) {
			String activeClass = parent.tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0];
			int activeCharIndex = parent.tableController.selected_col;
			ArrayList<ClassCaracteristic> activeChars = CharValuesLoader.active_characteristics.get(activeClass);
			ClassCaracteristic activeChar = activeChars.get(activeCharIndex%activeChars.size());
			GenericCharRule activeCharRule = new GenericCharRule(activeClass,activeChar,activeRule);
			if(activeCharRule.parseSuccess()) {
				System.out.println("Trying rule "+activeCharRule.getRuleMarker());
				Pattern regexPattern = Pattern.compile("["+GenericCharRule.SEP_CLASS+"]"+activeCharRule.getRegexMarker()+"["+GenericCharRule.SEP_CLASS+"]",Pattern.CASE_INSENSITIVE);
				List<String> targetClasses = CharValuesLoader.active_characteristics
						.entrySet().stream().filter(
								e->e.getValue().stream().anyMatch(a->a.getCharacteristic_id().equals(activeChar.getCharacteristic_id())))
						.map(e->e.getKey()).collect(Collectors.toList());
				CharItemFetcher.allRowItems.stream().forEach(r->{
					tryRuleOnItem(r,activeCharRule,regexPattern,targetClasses,parent);
				});
			}
		}else {
			
		}
		
		CharDescriptionExportServices.flushItemDataToDB(parent.account);
	}

	private static void tryRuleOnItem(CharDescriptionRow r, GenericCharRule activeCharRule, Pattern regexPattern, List<String> targetClasses, Char_description parent) {
		if(!r.hasDataInSegments(targetClasses)) {
			return;
		}
		boolean exitOnShortDesc = false;
		Matcher m;
		HashSet<String> matchingSegments = new HashSet<String>();
		if(r.getShort_desc()!=null) {
			m = regexPattern.matcher(" "+r.getShort_desc()+" ");
			exitOnShortDesc = false;
			while(m.find()) {
				matchingSegments.addAll(
				addCharResult2Row(r,activeCharRule,r.getShort_desc(),m.group(),parent)
				);
				exitOnShortDesc = false;
			}
		}
		if(exitOnShortDesc || !(r.getLong_desc()!=null)) {
			matchingSegments.forEach(segment->r.reEvaluateCharRules(segment));
			return;
		}
		m = regexPattern.matcher(" "+r.getLong_desc()+" ");
		while(m.find()) {
			matchingSegments.addAll(
					addCharResult2Row(r,activeCharRule,r.getLong_desc(),m.group(),parent)
					);
		}
		matchingSegments.forEach(segment->r.reEvaluateCharRules(segment));
		
	}

	private static ArrayList<String> addCharResult2Row(CharDescriptionRow matchedRow, GenericCharRule matchedRule, String matchedText,
			String matchedGroup, Char_description parent) {
		/*
		System.out.println("Rule "+matchedRule.getRuleMarker()+":"+
				"\n\t matches item "+matchedRow.getClient_item_number()+
				"\n\t with desc "+matchedText+
				"\n\t at group "+matchedGroup);*/
			
		ArrayList<String> matchingSegments = new ArrayList<String>();
		//For every segment that contains char in item
		for(String segment:matchedRow.getData().keySet()) {
			IntStream.range(0, charIdArrays.get(segment).size()).filter(i -> charIdArrays.get(segment).get(i).equals(matchedRule.getSourceChar().getCharacteristic_id()))
			.forEach(charIdx->{
				if(!(matchedRow.getData(segment).get(charIdArrays.get(segment).get(charIdx))!=null)) {
					matchedRow.getData(segment).put(charIdArrays.get(segment).get(charIdx),new CaracteristicValue());
					matchedRow.getData(segment).get(charIdArrays.get(segment).get(charIdx)).setParentChar(CharValuesLoader.active_characteristics.get(segment).get(charIdx));
				}
				
				CharRuleResult newMatch = new CharRuleResult(matchedRule,matchedText,matchedGroup,CharValuesLoader.active_characteristics.get(segment).get(charIdx));
				newMatch.ruleActionToValue(parent);
				matchedRow.addCharRuleResult(newMatch,segment,charIdArrays.get(segment).get(charIdx));
				matchingSegments.add(segment);
			});
		}
		return matchingSegments;
		
	}

}
