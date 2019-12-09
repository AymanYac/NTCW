package transversal.language_toolbox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import model.BinaryClassificationParameters;
import model.ClassCharacteristic;
import model.DescriptionType;
import model.GlobalConstants;
import model.UnitOfMeasure;
import transversal.generic.Tools;

public class WordUtils {
	
	public static String getSearchWords(String description) {
		
		List<String> tmp = Arrays.asList( getRawCut(description).split(" "));
		
		int i=0;
		String w1 =tmp.get(0);
		String w2 = null;
		for(String word:tmp) {
			
			if(i==1) {
				w2 = word;
				if(w2.length()>=GlobalConstants.SEARCH_WORD_LARGE) {
					return w1+" "+w2;
				}
			}
			if(i==2) {
				if(word.length()>=GlobalConstants.SEARCH_WORD_LARGE) {
					return w1+" "+w2+" "+word;
				}
				return w1;
			}
			i++;
			}
		return w1;
		}


	public static String getRawCut(String description) {
		
		int i=0;
		List<String> sentence = Arrays.asList(description.split(" "));
		for(String word: sentence) {
			if(isAlpha(word)) {
				i+=1;
				continue;
			}else {
				if(i==0) {
					return sentence.get(0);
				}
				return String.join(" ", sentence.subList(0, i));
			}
		}
		return String.join(" ", sentence);
	}
	
	
	public static boolean isAlpha(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}

	//Useful function to load all the known languages mapping
		public static HashMap<String, String> load_languages() throws ClassNotFoundException, SQLException {
			//For every known mapping language_id<-> language store in temporary variable
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from administration.languages where language_id in (select distinct data_language from administration.projects)");
			HashMap<String, String> ret = new HashMap<String,String>();
			//Add a dummy languge id for "All languages" language
			ret.put("DUMMY_LANGUAGE_UUID","All languages");
			while(rs.next()) {
				ret.put(rs.getString(1), rs.getString(2));
			}
			//Close the connection and return the mapping
			rs.close();
			stmt.close();
			conn.close();
			return ret;
		}


		public static String keepalpha(String input,boolean AlphabetOnly,boolean decode) {
			if(decode) {
				Unidecode unidecode = Unidecode.toAscii();
				input = unidecode.decodeAndTrim(input).toUpperCase();
				
			}else {
				input = input.toUpperCase();
			}
			if(AlphabetOnly) {
				input = keepwords(input);
			}
			/*
			int offset;
			
			for(int idx = 0;idx<input.length();idx++) {
				char car = input.charAt(idx);
				offset = 0;
				//if(idx!=0 && !Character.isDigit(car)) {
					//offset=1;
				//}
				if(!Character.isAlphabetic(car)) {
					return input.substring(0, idx+offset).trim().toUpperCase()+" "+keepalpha(input.substring(idx+1),AlphabetOnly,decode).trim().toUpperCase();
				}
			}
			*/
			return input.trim().toUpperCase();
			
		}


		public static String keepwords(String string) {
			return string.replaceAll("\\S*\\d+\\S*\\s*"," ").trim().replaceAll(" +", " ");
		}


		public static HashMap<String,String> CLEAN_DESC(ArrayList<String> input, HashMap<String, String> CORRECTIONS, BinaryClassificationParameters config, SpellCorrector sc, boolean isClassif) {
			HashMap<String,String> output = new HashMap<String,String>();
			String sd = input.get(0);
			String ld = input.get(1);
			
			output.put(DescriptionType.RAW_SHORT.toString(),sd);
			output.put(DescriptionType.RAW_LONG.toString(),ld);
			if(ld.length()>5) {
				output.put(DescriptionType.RAW_PREFERED.toString(), ld);
			}else {
				output.put(DescriptionType.RAW_PREFERED.toString(),sd);
			}
			
			
			if(isClassif) {
				sd = keepalpha(sd,config.getClassif_keepAlpha(),config.getClassif_cleanChar());
				ld = keepalpha(ld,config.getClassif_keepAlpha(),config.getClassif_cleanChar());
			}else {
				sd = keepalpha(sd,config.getPreclassif_keepAlpha(),config.getPreclassif_cleanChar());
				ld = keepalpha(ld,config.getPreclassif_keepAlpha(),config.getPreclassif_cleanChar());
			}
			
		
			output.put(DescriptionType.SIMPLE_SHORT.toString(),sd);
			output.put(DescriptionType.SIMPLE_LONG.toString(),ld);
			if(ld.length()>5) {
				output.put(DescriptionType.SIMPLE_PREFERED.toString(), ld);
			}else {
				output.put(DescriptionType.SIMPLE_PREFERED.toString(),sd);
			}
			
			
			
			sd = CORRECT(sd,CORRECTIONS,config,sc,isClassif);
			ld = CORRECT(ld,CORRECTIONS,config,sc,isClassif);
			
			
			output.put(DescriptionType.CLEAN_SHORT.toString(),sd);
			output.put(DescriptionType.CLEAN_LONG.toString(),ld);
			if(ld.length()>5) {
				output.put(DescriptionType.CLEAN_PREFERED.toString(), ld);
			}else {
				output.put(DescriptionType.CLEAN_PREFERED.toString(),sd);
			}
			/*
			output.put(DescriptionType.RAW_SHORT.toString(),sd);
			output.put(DescriptionType.RAW_LONG.toString(),ld);
			output.put(DescriptionType.SIMPLE_SHORT.toString(),sd);
			output.put(DescriptionType.SIMPLE_LONG.toString(),ld);
			
			output.put(DescriptionType.CLEAN_SHORT.toString(),sd);
			output.put(DescriptionType.CLEAN_LONG.toString(),ld);
			*/
			return output;
		}


		public static String CORRECT(String desc, HashMap<String, String> CORRECTIONS, BinaryClassificationParameters config, SpellCorrector sc, boolean isClassif) {
		
			ArrayList<String> ret = new ArrayList<String>();
			for(String word:desc.split(" ")) {
				
				/*if( (isClassif && !config.getClassif_keepAlpha() && word.matches(".*\\d+.*")) || (!isClassif && !config.getPreclassif_keepAlpha() && word.matches(".*\\d+.*")) ) {
					continue;
				}*/
				
				if( (isClassif && (config.getClassif_cleanAbv() )) || (!isClassif && (config.getPreclassif_cleanAbv() )) ) {
					ret.add(sc.expand_abv(word));
				}else {
					ret.add(word);
				}
				
				
				
				/*else if(CORRECTIONS.keySet().contains(word)) {
					ret.add(CORRECTIONS.get(word));
				}else {
					ret.add(word);
				}*/
			}
			if( (isClassif && (config.getClassif_cleanSpell())) || (!isClassif && ( config.getPreclassif_cleanSpell())) ) {
				return sc.correctPhrase(String.join(" ", ret));
			}
			return String.join(" ", ret);
			
		}


		public static HashMap<String, String> getCorrections(String languageTable) throws ClassNotFoundException, SQLException {
			HashMap<String,String> ret = new HashMap<String,String>();
			return ret;/*
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			try{
				ResultSet rs = stmt.executeQuery("select * from"+languageTable);
				while(rs.next()) {
					ret.put(rs.getString(1).toUpperCase(), rs.getString(2).toUpperCase());
				}
			}catch(Exception E) {
				
			}
			
			return ret;*/
		}


		public static String CORRECT(String selected_text) {
			// TODO Implement the correction procedure, more parameters may be needed
			Unidecode unidecode = Unidecode.toAscii();
			
			return unidecode.decode( selected_text );
		}


		public static String TRIM_LEADING_SEPARATORS(String string) {
			String ret="";
			boolean firstAlphaNumEncountred=false;
			for(char c:string.toCharArray()) {
				if(firstAlphaNumEncountred) {
					ret=ret+c;
					continue;
				}
				if(Character.isAlphabetic(c)||Character.isDigit(c)) {
					ret=ret+c;
					firstAlphaNumEncountred=true;
					continue;
				}
			}
			return ret;
		}


		public static String ALPHANUM_PATTERN_RULE_INREPLACE(String selected_text, boolean keepAlphaBeforeFirstSep) {
			//e.g. "abcd ef12-gh34-ij56"
			//Rule = ["ab"#"cd"#(|-1)@@##(|-1)@@##]
			String rule="";
			boolean firstSepPassed=!keepAlphaBeforeFirstSep;
			boolean last_is_alpha=false;
			for(int i=0;i<selected_text.length();i++) {
				char c = selected_text.charAt(i);
				if(Character.isAlphabetic(c)) {
					rule=rule+(firstSepPassed?"@":(last_is_alpha?c:(i==0?"\""+c:c)));
					last_is_alpha = true;
				}else if (Character.isDigit(c)) {
					rule=rule+(firstSepPassed?"#":(last_is_alpha?"\"#":"#"));
					last_is_alpha = false;
				}else {
					rule=rule+(last_is_alpha&&!firstSepPassed?"\"(|-1)":"(|-1)");
					if(i!=0) {
						firstSepPassed=true;
					}
				}
			}
			
			return rule;
		}


		public static String QUOTE_NON_SEP_TEXT(String input) {
			String output = "";
			boolean inQuote = false;
			for(int i=0;i<input.length();i++) {
				char c = input.charAt(i);
				if(Character.isAlphabetic(c) || Character.isDigit(c)) {
					if(inQuote) {
						output=output+c;
					}else {
						output=output+"\""+c;
					}
					inQuote=true;
				}else{
					if(inQuote) {
						output=output+"\"(|-1)";
					}else {
						if(i==0) {
							output="(|-1)";
						}
					}
					inQuote = false;
				}
				}
			if(inQuote) {
				output=output+"\"";
			}
			return output;
		}


		public static boolean TermWiseInclusion(String toSearch, String searchIn,
				boolean splitbySeparators) {
			
			Unidecode unidecode = Unidecode.toAscii();
			toSearch = unidecode.decodeAndTrim(toSearch).toUpperCase();
			searchIn = unidecode.decodeAndTrim(searchIn).toUpperCase();
			if(splitbySeparators) {
				toSearch = String.join("",toSearch.chars().mapToObj((c -> (char) c)).map(
						c->Character.isAlphabetic(c)||Character.isDigit(c)?String.valueOf(c):" ").collect(Collectors.toList()));
				toSearch=" "+toSearch+" ";
				
				searchIn = String.join("",searchIn.chars().mapToObj((c -> (char) c)).map(
						c->Character.isAlphabetic(c)||Character.isDigit(c)?String.valueOf(c):" ").collect(Collectors.toList()));
				searchIn=" "+searchIn+" ";
			}
			
			return searchIn.contains(toSearch);
		}


		public static ArrayList<Double> parseNumericalValues(String selected_text) {
			//(including decimals with "." or "," or negative values
			
			Pattern p = Pattern.compile("(-?\\d+(\\.\\d+)?)");
			Matcher m = p.matcher(selected_text.replace(",", "."));
			
			ArrayList<Double> ret = new ArrayList<Double>();
			while (m.find()) {
				  ret.add(Double.valueOf( m.group(0)) );
				}
			return ret;
		}


		public static ArrayList<UnitOfMeasure> parseCompatibleUoMs(String selected_text, ClassCharacteristic active_char) {
			
			//~ is used to escape intext quotes so as not to be mistaken with rule syntax quotes
			selected_text = selected_text.replace("~\"","\"");
			String pattern = "(-?\\d+(\\.\\d+)?)";
		    String patternPlusOverLaps = pattern+"(?=(" + "(.*)" + ")).";
		    Pattern p = Pattern.compile(patternPlusOverLaps);
		    
		    Matcher m = p.matcher(selected_text.replace(",", "."));
			
			ArrayList<UnitOfMeasure> ret = new ArrayList<UnitOfMeasure>();
			while (m.find()) {
				UnitOfMeasure tmp = UnitOfMeasure.lookUpUomInText_V2(m.group(3),active_char.getAllowedUoms());
				ret.add(tmp);
				}
			//Add nulls to avoid IndexOutOfBoundsException when checking more uoms
			ret.add(null);
			ret.add(null);
			ret.add(null);
			ret.add(null);
			return ret;
		}

		
		public static ArrayList<UnitOfMeasure> parseKnownUoMs(String selected_text) {
			
			String pattern = "(-?\\d+(\\.\\d+)?)";
		    String patternPlusOverLaps = pattern+"(?=(" + "(.*)" + ")).";
		    Pattern p = Pattern.compile(patternPlusOverLaps);
		    
		    Matcher m = p.matcher(selected_text.replace(",", "."));
			
			ArrayList<UnitOfMeasure> ret = new ArrayList<UnitOfMeasure>();
			while (m.find()) {
				UnitOfMeasure tmp = UnitOfMeasure.lookUpUomInText_V2(m.group(3),null);
				ret.add(tmp);
				}
			return ret;
		}

		
		
		
		public static String replacePunctuationSplit(String text, boolean split) {
			//Replace any char in !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
			//by spaces
			text = text.replaceAll("\\p{Punct}", " ");
			if(split) {
				return text.split(" ")[0];
			}
			return text;
		}


		public static String DoubleToString(double d) {
			if(d == (long) d)
		        return String.format("%d",(long)d);
		    else
		        return String.format("%s",d);
		}


		public static boolean RuleSyntaxContainsSep(String searchText, String sep) {
			searchText = searchText.replace("~\"","\"");
			return searchText.contains("(|+0)"+sep+"(|+0)")
					||searchText.equals("(|+0)"+sep+"")
					||searchText.equals(""+sep+"(|+0)")
					||searchText.equals(""+sep+"");
		}
}
