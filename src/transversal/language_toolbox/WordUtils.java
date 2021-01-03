package transversal.language_toolbox;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.*;
import org.apache.commons.lang3.StringUtils;
import transversal.generic.Tools;

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

public class WordUtils {
	
	private static String ruleString;
	private static int textIdx;
	private static Unidecode unidecode;


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

	public static String ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(GenericCharRule rule, String action, String matchedBlock) {
		if(!(matchedBlock!=null)){
			return null;
		}
		//Transformer la syntaxe de la rule pattern neonec en regex
		String SEP_CLASS=GenericCharRule.SEP_CLASS_TXT;
		//StringBuilder markerToConsume = new StringBuilder(rule.neonecToRegexMarker(SEP_CLASS).replace("\\Q", "").replace("\\E", ""));
		StringBuilder markerToConsume = new StringBuilder(WordUtils.quoteCompositionMarkerInDescPattern(rule.getRuleMarker()));
		StringBuilder matchedBlockToConsume = new StringBuilder(matchedBlock);
		//Créer les variables temporaires correspondant aux variables du rule pattern
		ArrayList<String> mandatorySeparators = new ArrayList<String>();
		ArrayList<String> optionalSeparators = new ArrayList<String>();
		ArrayList<String> digitCharacters = new ArrayList<>();
		ArrayList<String> alphaCharacters = new ArrayList<>();
		ArrayList<String> mandatoryStrings = new ArrayList<>();
		ArrayList<String> optionalStrings = new ArrayList<>();
		//Chercher l'identified pattern dans la description, en assignant les variables
		while (markerToConsume.length()>0){
			if(
					ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"#",digitCharacters,SEP_CLASS)!=null ||
							ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"@",alphaCharacters,SEP_CLASS)!=null ||
							ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"(|+0)",optionalSeparators,SEP_CLASS)!=null ||
							ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"(|+1)",mandatorySeparators,SEP_CLASS)!=null ||
							ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"(*+0)",optionalStrings,SEP_CLASS)!=null ||
							ALPHANUM_PATTERN_STEP_USING_NEONEC(markerToConsume,matchedBlockToConsume,"(*+1)",mandatoryStrings,SEP_CLASS)!=null){
			}else if(markerToConsume.toString().startsWith("\"")){
				markerToConsume = new StringBuilder(markerToConsume.substring(1));
			}else {
				markerToConsume = new StringBuilder(markerToConsume.substring(1));
				matchedBlockToConsume = new StringBuilder(matchedBlockToConsume.substring(1));
			}
		}

		//Créer l'item value à partir de la rule value et des variables
		StringBuilder value = new StringBuilder();
		action = WordUtils.quoteStringsInDescPattern(action).replace("\\Q", "").replace("\\E", "");
		while(action.length()>0){
			if(action.startsWith("#")){
				value.append(digitCharacters.get(0));
				digitCharacters.remove(0);
				action=action.substring(1);
				continue;
			}
			if(action.startsWith("@")){
				value.append(alphaCharacters.get(0));
				alphaCharacters.remove(0);
				action=action.substring(1);
				continue;
			}
			if(action.startsWith("(|+0)")){
				value.append(optionalSeparators.get(0));
				optionalSeparators.remove(0);
				action=action.substring("(|+0)".length());
				continue;
			}
			if(action.startsWith("(|+1)")){
				value.append(mandatorySeparators.get(0));
				mandatorySeparators.remove(0);
				action=action.substring("(|+1)".length());
				continue;
			}
			if(action.startsWith("(*+0)")){
				value.append(optionalStrings.get(0));
				optionalStrings.remove(0);
				action=action.substring("(*+0)".length());
				continue;
			}
			if(action.startsWith("(*+1)")){
				value.append(mandatoryStrings.get(0));
				mandatoryStrings.remove(0);
				action=action.substring("(*+1)".length());
				continue;
			}
			value.append(action.substring(0,1));
			action=action.substring(1);
		}
		return value.toString();
	}
	private static Boolean ALPHANUM_PATTERN_STEP_USING_NEONEC(StringBuilder markerToConsume, StringBuilder matchedBlockToConsume, String stepNeonec, ArrayList<String> stepValues,String SEP_CLASS) {
		if(markerToConsume.toString().startsWith(stepNeonec)){
			String stepRegex=WordUtils.neonecObjectSyntaxToRegex(stepNeonec,SEP_CLASS,false);
			markerToConsume.replace(0,stepNeonec.length(),"");
			Pattern p = Pattern.compile("("+stepRegex+")"+WordUtils.quoteStringsInDescPattern(WordUtils.neonecObjectSyntaxToRegex(markerToConsume.toString(),SEP_CLASS,false)),Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(matchedBlockToConsume);
			if(m.find()){
				String consumableMatch = String.valueOf(m.group(1));
				stepValues.add(consumableMatch);
				matchedBlockToConsume.replace(0,consumableMatch.length(),"");
				return true;
			}
			return false;
		}
		return null;
	}

	public static String neonecObjectSyntaxToRegex(String markerToConsume, String SEP_CLASS,boolean inSeparators) {
		if(inSeparators){
			markerToConsume="$$$$$$$$$"+markerToConsume+"$$$$$$$$$$$$$$$$$$";
			markerToConsume="(|+1)"+markerToConsume;
			markerToConsume=markerToConsume+"(|+1)";
		}
		String ret = markerToConsume
				.replaceAll("%\\d(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "[-+]?[0-9]+(?:[. ,]?[0-9]{3,3})*[0-9]*(?:[.,][0-9]+)?")
				.replaceAll("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "[0-9]")
				.replaceAll("@(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "[a-z]")
				.replaceAll("\\(\\|\\+0\\)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "[" + SEP_CLASS + "]*?")
				.replaceAll("\\(\\|\\+1\\)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "[" + SEP_CLASS + "]+?")
				.replaceAll("\\(\\*\\+0\\)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", ".*?")
				.replaceAll("\\(\\*\\+1\\)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", ".+?");

		ret = ret.replace("$$$$$$$$$$$$$$$$$$",")");
		ret = ret.replace("$$$$$$$$$","(");
		return ret;
		}

	private static Boolean ALPHANUM_PATTERN_STEP_USING_REGEX(StringBuilder markerToConsume, StringBuilder matchedBlockToConsume, String stepRegex, ArrayList<String> stepValues) {
		if(markerToConsume.toString().startsWith(stepRegex)){
			markerToConsume.replace(0,stepRegex.length(),"");
			Pattern p = Pattern.compile("("+stepRegex+")"+markerToConsume,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(matchedBlockToConsume);
			if(m.find()){
				String consumableMatch = m.group(1);
				stepValues.add(consumableMatch);
				matchedBlockToConsume.replace(0,consumableMatch.length(),"");
				return true;
			}
			return false;
		}
		return null;
	}


	public static String ALPHANUM_PATTERN_RULE_EVAL_BLOCKWISE(String action, String matchedBlock) {
		String actionCopy = action.replace("#","[0-9]");
		actionCopy = actionCopy.replace("@","[A-Z]");
		actionCopy = actionCopy.replace("(|+0)","[\\W]*");
		actionCopy = actionCopy.replace("(|+1)","[\\W]+");
		Pattern p = Pattern.compile(actionCopy,Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(matchedBlock);
		if(matcher.find()){
			return matcher.group(0);
		}
		return action;
	}

		public static String ALPHANUM_PATTERN_RULE_INREPLACE(String selected_text, boolean keepAlphaBeforeFirstSep,boolean quoteOuterText) {
			//e.g. "abcd ef12-gh34-ij56"
			//Rule = ["ab"#"cd"#(|+0)@@##(|+0)@@##]
			quoteOuterText=false;//Quoting is done in the next step, self correcting syntax
			String rule="";
			boolean firstSepPassed=!keepAlphaBeforeFirstSep;
			boolean last_is_alpha=false;
			for(int i=0;i<selected_text.length();i++) {
				char c = selected_text.charAt(i);
				if(Character.isAlphabetic(c)) {
					rule=rule+(firstSepPassed?"@":(last_is_alpha?c:(i==0&&quoteOuterText?"\""+c:c)));
					last_is_alpha = true;
				}else if (Character.isDigit(c)) {
					rule=rule+(firstSepPassed?"#":(last_is_alpha&&quoteOuterText?"\"#":"#"));
					last_is_alpha = false;
				}else {
					rule=rule+(last_is_alpha&&!firstSepPassed&&quoteOuterText?"\"(|+0)":"(|+0)");
					if(i!=0) {
						firstSepPassed=true;
					}
				}
			}
			
			return rule;
		}


		public static String QUOTE_NON_SEP_TEXT(String input, boolean quoteOuterText) {
			String output = "";
			boolean inQuote = false;
			for(int i=0;i<input.length();i++) {
				char c = input.charAt(i);
				if(Character.isAlphabetic(c) || Character.isDigit(c)) {
					if(i==0) {
						output=(quoteOuterText?"\"":"")+c;
					}
					else if(inQuote) {
						output=output+c;
					}else {
						output=output+"\""+c;
					}
					inQuote=true;
				}else{
					if(inQuote) {
						output=output+"\"(|+0)";
					}else {
						if(i==0) {
							output="(|+0)";
						}
					}
					inQuote = false;
				}
				}
			if(inQuote) {
				output=output+(quoteOuterText?"\"":"");
			}
			return output;
		}


		public static boolean TermWiseInclusion(String toSearch, String searchIn,
				boolean splitbySeparators) {
			if(!(toSearch!=null) || !(searchIn!=null) || toSearch.replace(" ","").length()==0 || searchIn.replace(" ","").length()==0){
				return false;
			}
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
			ArrayList<Double> ret = new ArrayList<Double>();
			if(!(selected_text!=null)) {
				return ret;
			}
			
			//(including decimals with "." or "," or negative values
			
			String numericPatternString="";
			//patternString = "(-?\\+?\\d+(\\.\\d+)?)";
			//"HQH-12-212 hQH+29 QSD2+3" -> "-12,212,+29,2,3"
			//numericPatternString = "(((?<!\\d+(\\.\\d+)?)-)?((?<!\\d+(\\.\\d+)?)\\+)?\\d+(\\.\\d+)?)";
			numericPatternString = "(((?<!\\d(\\s){0,1})-)?\\d+(\\.\\d+)?)";
			//"HQH-12.2 -212v -21hQH+29 QSD2+3" -> "-12.2,212,-21,29,2,3"
			
			Pattern p = Pattern.compile(numericPatternString);
			Matcher m = p.matcher(selected_text.replace(" ","").replaceAll("(.*)[,.]([0-9]+.*)","$1______$2").replace(",", "").replace(".","").replace("______","."));
			while (m.find()) {
				  ret.add(Double.valueOf( m.group(0)) );
				}
			
			
			return ret;
		}
		
		public static String textWithoutParsedNumericalValues(String selected_text) {
			
			if(!(selected_text!=null)) {
				return "";
			}
			//(including decimals with "." or "," or negative values
			
			String numericPatternString="";
			//patternString = "(-?\\+?\\d+(\\.\\d+)?)";
			//"HQH-12-212 hQH+29 QSD2+3" -> "-12,212,+29,2,3"
			//numericPatternString = "(((?<!\\d+(\\.\\d+)?)-)?((?<!\\d+(\\.\\d+)?)\\+)?\\d+(\\.\\d+)?)";
			numericPatternString = "(((?<!\\d(\\s){0,1})-)?\\d+(\\.\\d+)?)";
			//"HQH-12.2 -212v -21hQH+29 QSD2+3" -> "-12.2,212,-21,29,2,3"
			
			Pattern p = Pattern.compile("(?<!%)"+numericPatternString);
			Matcher m = p.matcher(selected_text.replace(" ","").replaceAll("(.*)[,.]([0-9]+.*)","$1______$2").replace(",", "").replace(".","").replace("______","."));
			
			int i=0;
			while (m.find()) {
				i+=1;
				//selected_text = selected_text.replace(" ","").replaceAll("(.*)[,.]([0-9]+.*)","$1______$2").replace(",", "").replace(".","").replace("______",".").replaceFirst("(?<!%)"+Pattern.quote(m.group(0)), "%"+String.valueOf(i));
				selected_text = selected_text.replaceAll("(.*)[,.]([0-9]+.*)","$1______$2").replace(",", "").replace(".","").replace("______",".").replaceFirst("(?<!%)"+Pattern.quote(m.group(0)), "%"+String.valueOf(i));
				//ret.add(Double.valueOf( m.group(0)) );
				  
				}
			
			return selected_text;
		}



		public static ArrayList<UnitOfMeasure> parseCompatibleUoMs(String selected_text, ClassCaracteristic active_char) {
			
			//~ is used to escape intext quotes so as not to be mistaken with rule syntax quotes
			selected_text = selected_text.replace("~\"","\"");
			String pattern = "(-?\\d+(\\.\\d+)?)";
		    String patternPlusOverLaps = pattern+"(?=(" + "(.*)" + ")).";
		    Pattern p = Pattern.compile(patternPlusOverLaps);
		    
		    Matcher m = p.matcher(selected_text.replace(" ","").replaceAll("(.*)[,.]([0-9]+.*)","$1______$2").replace(",", "").replace(".","").replace("______","."));
			
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


	

		public static String reducePatternRuleSeparators(String preparedRule) {
			try {
				//Removed : + - and / from separators to account for "sep" cases
				String[] SEPARATORS = new String[] {",","."," ","=",":","+","-","/","|","\\"};
				ArrayList<String> exceptions = WordUtils.parsePatternRuleSeparatorExceptions(SEPARATORS,preparedRule);
				
				String loopRule = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
				while(true) {
					loopRule = reduceExtremetiesPatternRuleSeparators(preparedRule,SEPARATORS,exceptions);
					if(preparedRule.equals(loopRule)) {
						break;
					}
					
					preparedRule = loopRule;
				}
				
				
				
				return preparedRule;
			}catch(Exception V) {
				//V.printStackTrace(System.err);
				return preparedRule;
			}
		}


		private static ArrayList<String> parsePatternRuleSeparatorExceptions(String[] SEPARATORS, String preparedRule) {
			ArrayList<String> ret = new ArrayList<String>();
			String ruleClause = preparedRule.split("<")[1];
			for(String sepField:ruleClause.split("%")) {
				sepField = sepField.split(">")[0];
				for(String sep:SEPARATORS) {
					if(sepField.contains(sep)) {
						ret.add(sep);
					}
				}
			}
			return ret;
		}


		public static String reduceExtremetiesPatternRuleSeparators(String preparedRule,
				String[] SEPARATORS, ArrayList<String> exceptions) {
			
			String ruleClause = null;
			ArrayList<String> ruleActions = new ArrayList<String>();
			Pattern regexPattern = Pattern.compile("<([^<>]*)>");
			Matcher m = regexPattern.matcher(preparedRule);
			while(m.find()){
		    	ruleActions.add(m.group(1));
		    }
			ruleClause = preparedRule.substring(0,preparedRule.indexOf("<"+ruleActions.get(0)));
			for(String sep:SEPARATORS) {
				if(exceptions.contains(sep)&&!sep.equals(" ")) {
					continue;
				}
				ruleClause = ruleClause.replaceAll("(?<!~)\""+Pattern.quote(sep)+"+", "(|+1)\"")
					      .replaceAll(Pattern.quote(sep)+"+(?<!~)\"", "\"(|+1)")
					      .replaceAll("(?<!~)\"\"", "");
			}
			ruleClause = ruleClause.replaceAll("(\\(\\|\\+0\\))+","(|+0)")
						.replaceAll("(\\(\\|\\+1\\))+","(|+1)")
						.replaceAll("(\\(\\|\\+0\\))+\\(\\|\\+1\\)","(|+1)")
						.replaceAll("\\(\\|\\+1\\)(\\(\\|\\+0\\))+","(|+1)");
			
			if(ruleClause.endsWith("(|+1)")){
				ruleClause= ruleClause.substring(0,ruleClause.length()-"(|+1)".length());
			}
			if(ruleClause.startsWith("(|+1)")){
				ruleClause= ruleClause.substring("(|+1)".length());
			}
			if(ruleClause.endsWith("(|+0)")){
				ruleClause= ruleClause.substring(0,ruleClause.length()-"(|+0)".length());
			}
			if(ruleClause.startsWith("(|+0)")){
				ruleClause= ruleClause.substring("(|+0)".length());
			}
			
			
			//return ruleClause+((uomClause!=null)?"<UOM"+uomClause:"");
			return ruleClause+(ruleActions.size()>0?"<":"")+String.join("><", ruleActions)+(ruleActions.size()>0?">":"");
			
		}


		public static String generateRuleSyntax(ArrayList<String> textBetweenNumbers, String[] roles, String[] players, UnitOfMeasure uom) {
			ruleString="";
			textIdx = 0;
			textBetweenNumbers.forEach(t->{
				
				if(textIdx>0) {
					ruleString = ruleString+"(|+0)%"+String.valueOf(textIdx)+"(|+0)";
				}
				ruleString = ruleString+t;
				textIdx+=1;
			});
			
			if(ruleString.endsWith(" \"")) {
				ruleString= ruleString.substring(0,ruleString.length()-2)+"\"";
			}
			
			
			if(roles!=null) {
				for(int i=0;i<roles.length;i++) {
					if(roles[i].length()==0){
						roles[i]="NOM";
					}
					ruleString = ruleString+"<"+roles[i]+" "+players[i]+">";
				}
			}
			if(uom!=null) {
				ruleString = ruleString+"<UOM \""+uom.getUom_symbol()+"\">";
			}
			
			return String.valueOf(ruleString);
			
		}


		public static boolean FreeRuleSyntaxContainsSep(String freeRule, String sep) {
			
			return WordUtils.RuleSyntaxContainsSep(String.join("",freeRule.chars().mapToObj((c -> (char) c)).
					map(c->(Character.isAlphabetic(c)||Character.isDigit(c)||String.valueOf(c).equals(sep))?String.valueOf(c):"(|+0)").
					collect(Collectors.toList())),sep);
		}
		
		public static boolean RuleSyntaxContainsSep(String searchText, String sep) {
			
			searchText = searchText.replace("~\"","\"");
			return searchText.contains("(|+0)"+sep+"(|+0)")
					||searchText.equals("(|+0)"+sep+"")
					||searchText.equals(""+sep+"(|+0)")
					||searchText.equals(""+sep+"");
		}


		public static String textFlowToString(TextFlow tf) {
		    StringBuilder sb = new StringBuilder();
		    tf.getChildren().stream()
		            .filter(t -> Text.class.equals(t.getClass()))
		            .forEach(t -> sb.append(((Text) t).getText()));
		    return sb.toString();
		}


		public static String substituteRuleItemToRegexItem(String ruleMarker, String ancien, String nouveau) {
			//matches ancien which is followed by any characters followed by a certain number of pairs of " or ', followed by a any characters till the end
			if(ancien.equals("%\\d")) {
				return ruleMarker.replaceAll(ancien+"(?=([^\"]*[\"][^\"]*[\"])*[^\"]*$)", nouveau);
			}
			return ruleMarker.replaceAll(Pattern.quote(ancien)+"(?=([^\"]*[\"][^\"]*[\"])*[^\"]*$)", nouveau);
		}

		private static String removeStringsInDescPattern(String transformed) {
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(transformed);
			return m.replaceAll("");
		}

		public static String quoteStringsInDescPattern(String ruleMarker) {
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(ruleMarker);
			return m.replaceAll("\\\\Q"+"$1"+"\\\\E");
			//String q = Pattern.quote(ruleMarker);
			//return q.substring(2).substring(0,q.length()-4);
		}

		public static String quoteCompositionMarkerInDescPattern(String ruleMarker) {
			Pattern p = Pattern.compile("\\+(?=[^ *\\d|%])(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			Matcher m = p.matcher(ruleMarker);
			return m.replaceAll("\""+"$0"+"\"");
			//String q = Pattern.quote(ruleMarker);
			//return q.substring(2).substring(0,q.length()-4);
		}


		public static boolean startsWithIgnoreCase(List<String> sub, List<String> supp) {
			unidecode=(unidecode!=null)?unidecode:Unidecode.toAscii();
			for(int idx=0;idx<sub.size();idx++) {
				String elem = sub.get(idx);
				try {
					if(!StringUtils.equalsIgnoreCase(unidecode.decodeAndTrim(elem),
													unidecode.decodeAndTrim(supp.get(idx+1))
													)){
						return false;
					}
				}catch(Exception V) {
					return false;
				}
			}
			return true;
		}


		public static boolean filterClassNameAutoCompelete(String className, String typedText) {
			unidecode=(unidecode!=null)?unidecode:Unidecode.toAscii();
			if(typedText.chars().mapToObj(c->(char)c).anyMatch(c->Character.isLetter(c))) {
				return unidecode.decodeAndTrim(className.split("&&&")[1]).toLowerCase().contains(unidecode.decodeAndTrim(typedText).toLowerCase());
			}
			return className.split("&&&")[1].contains(typedText);
		}

	public static String correctDescriptionRuleSyntax(String ruleString) {
		if(!(ruleString!=null)){
			return null;
		}
		int ruleMarkerEndDelimiter = ruleString.indexOf('<');
		String ruleMarker = ruleString.substring(0, ruleMarkerEndDelimiter);
		ruleMarker = WordUtils.correctDescriptionRuleSyntaxElement(ruleMarker,false);
		String ruleActions = ruleString.substring(ruleMarkerEndDelimiter);
		Pattern regexPattern = Pattern.compile("<([^<>]*)>");
		Matcher m = regexPattern.matcher(ruleActions);

		ruleString=ruleMarker;
		while(m.find()){
			String action = m.group(1);
			String actionPrefix = action.split(" ")[0];
			String actionSuffix = action.substring(actionPrefix.length()+1);
			if(actionPrefix.startsWith("UOM")){

			}
			else if(actionPrefix.startsWith("NOM") || actionPrefix.startsWith("MIN") || actionPrefix.startsWith("MAX") || actionPrefix.startsWith("MINMAX")){
				actionSuffix=WordUtils.correctDescriptionRuleSyntaxElement(actionSuffix,true);
			}else{
				actionSuffix=WordUtils.correctDescriptionRuleSyntaxElement(actionSuffix,false);
			}
			ruleString=ruleString+"<"+actionPrefix+" "+actionSuffix+">";
		}
		return ruleString;
	}

	public static String correctDescriptionRuleSyntaxElement(String initial, boolean processingNumericValue){
		if(!(initial!=null) || initial.length()==0){
			return null;
		}
		String transformed="";
//Dim char As Integer 'caractère analysé
//Dim numeric_value_index As Integer 'index n de la valeur numérique %n
		int numericValueIndex;
//Dim special_characters(19) As String 'liste de caractères spéciaux (ici designée pour les patterns/champs TXT)
		ArrayList<String> specialCharacters;
		if(processingNumericValue){
			specialCharacters=new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","8","9","+","*","-","/","%","%1","%2","%3","%4","%5","%6","#","(#+0)","(#+1)","(#+2)","(#+3)","(#+4)","(#+5)","(#+6)"));
		}else{
			specialCharacters=new ArrayList<>(Arrays.asList("+","*","(*+0)","(*+1)","(*+2)","(*+3)","(*+4)","(*+5)","(*+6)","|","(|+0)","(|+1)","(|+2)","(|+3)","(|+4)","(|+5)","(|+6)","%","%1","%2","%3","%4","%5","%6","#","(#+0)","(#+1)","(#+2)","(#+3)","(#+4)","(#+5)","(#+6)","@","(@+0)","(@+1)","(@+2)","(@+3)","(@+4)","(@+5)","(@+6)"));
		}
//Dim sp_char_index As Integer 'index dans la liste des caractères spéciaux
		int spCharIndex;
//
//Dim in_quotes As Variant '1 si entre "", -1 sinon
		boolean inQuotes = false;
//Dim is_special As Integer '1 si special character, 0 sinon
		boolean isSpecial = false;
//Dim is_previous_special As Integer '1 si le caractère précédent est un  special character, 0 sinon
		boolean isPreviousSpecial = false;
//'Correction du pattern
//			transformed(row) = initial(row)
		transformed = initial;

//    'Passe de remplacement de "|" par "(|+1)" et "*" par "(*+1)" ///A étendre aux # et @\\\
//    in_quotes = -1
//    caractère analysé
		int charIdx=0;
//    While char <= Len(transformed(row))
		while(charIdx<transformed.length()) {
//        If Mid(transformed(row), char, 1) = """" Then
			if(transformed.charAt(charIdx)=='"') {

//            in_quotes = in_quotes * (-1)
				inQuotes=!inQuotes;
//        End If
			}
//        If Mid(transformed(row), char, 1) = "|" And in_quotes = -1 Then
			if(transformed.charAt(charIdx)=='|' && !inQuotes) {
//            transformed(row) = Left(transformed(row), char - 1) & "(|+1)" & Right(transformed(row), Len(transformed(row)) - char)
				transformed = transformed.substring(0,charIdx)+"(|+1)"+transformed.substring(charIdx+1);
//            char = char + 1
				charIdx++;
//        End If
			}
//        If Mid(transformed(row), char, 1) = "*" And in_quotes = -1 Then
			if(transformed.charAt(charIdx)=='*' && !inQuotes) {
//            transformed(row) = Left(transformed(row), char - 1) & "(*+1)" & Right(transformed(row), Len(transformed(row)) - char)
				transformed = transformed.substring(0,charIdx)+"(*+1)"+transformed.substring(charIdx+1);
//            char = char + 1
				charIdx++;
//        End If
			}
//        char = char + 1
			charIdx++;
//    Wend
		}


//    'Passe de remplacement de "((|+1)+" par "(|+" et "((*+1)+" par "(*+" ///A étendre aux # et @\\\
//    in_quotes = -1
		inQuotes=false;
//    char = 1
		charIdx=0;
//    While char <= Len(transformed(row))
		while(charIdx<transformed.length()) {
//        If Mid(transformed(row), char, 1) = """" Then
			if(transformed.charAt(charIdx)=='"') {
//            in_quotes = in_quotes * -1
				inQuotes=!inQuotes;
//        End If
			}
//        If Mid(transformed(row), char, 7) = "((|+1)+" And in_quotes = -1 Then
			if(transformed.substring(charIdx).startsWith("((|+1)+") && !inQuotes) {
//            transformed(row) = Left(transformed(row), char - 1) & "(|+" & Right(transformed(row), Len(transformed(row)) - char - 6)
				transformed = transformed.substring(0,charIdx)+"(|+"+transformed.substring(charIdx+7);
//        End If
			}
//        If Mid(transformed(row), char, 7) = "((*+1)+" And in_quotes = -1 Then
			if(transformed.substring(charIdx).startsWith("((*+1)+") && !inQuotes) {
//            transformed(row) = Left(transformed(row), char - 1) & "(*+" & Right(transformed(row), Len(transformed(row)) - char - 6)
				transformed = transformed.substring(0,charIdx)+"(*+"+transformed.substring(charIdx+7);
//        End If
			}
//        char = char + 1
			charIdx++;
//    Wend
		}


//    'Passe de remplacement de "%" par "%n"
//    in_quotes = -1
		inQuotes=false;
//    numeric_value_index = 1
		numericValueIndex=1;
//    char = 1
		charIdx=0;
//    While char <= Len(transformed(row)) - 1
		while(charIdx<transformed.length()-1) {
//        If Mid(transformed(row), char, 1) = """" Then
			if (transformed.charAt(charIdx) == '"') {
//            in_quotes = in_quotes * (-1)
				inQuotes = !inQuotes;
//        End If
			}
//        If Mid(transformed(row), char, 1) = "%" And IsNumeric(Mid(transformed(row), char + 1, 1)) = False And in_quotes = -1 Then
			if(transformed.charAt(charIdx)=='%' && !inQuotes && !Character.isDigit(transformed.charAt(charIdx+1))) {
//            transformed(row) = Left(transformed(row), char - 1) & "%" & numeric_value_index & Right(transformed(row), Len(transformed(row)) - char)
				transformed=transformed.substring(0,charIdx)+"%"+String.valueOf(numericValueIndex)+transformed.substring(charIdx+1);
//            numeric_value_index = numeric_value_index + 1
				numericValueIndex++;
//        End If
			}
//        char = char + 1
			charIdx++;
//    Wend
		}
//    If Right(transformed(row), 1) = "%" Then
		if(transformed.endsWith("%")) {
//        transformed(row) = transformed(row) & numeric_value_index
			transformed=transformed+String.valueOf(numericValueIndex);
			numericValueIndex++;
//    End If
		}
//    'Passe de remplacement de String par "String"
//    in_quotes = -1
		inQuotes=false;
//    is_special = 0
		isSpecial=false;
//    sp_length = 1
		int specialLen=1;
//
//    'Vérification du premier caractère
//    If Left(transformed(row), 1) = """" Then
		if(transformed.startsWith("\"")) {
//        in_quotes = in_quotes * -1
			inQuotes=!inQuotes;
//    End If
		}
//    For sp_char_index = 0 To 19
		for(String specialCharacter:specialCharacters) {
//        If Left(transformed(row), Len(special_characters(sp_char_index))) = special_characters(sp_char_index) And in_quotes = -1 Then
			if(transformed.startsWith(specialCharacter) && !inQuotes){
//                is_special = 1
				isSpecial=true;
//                sp_length = Len(special_characters(sp_char_index))
				specialLen=specialCharacter.length();
//        End If
			}
//    Next
		}
//    If is_special = 0 And Left(transformed(row), 1) <> """" Then
		if(!isSpecial && !transformed.startsWith("\"")) {
//        transformed(row) = """" & transformed(row)
			transformed = "\""+transformed;
//    End If
		}
//    char = 1 + sp_length
		charIdx = specialLen;
//
//    'Vérification des caractères centraux
//    While char <= Len(transformed(row))
		while(charIdx<transformed.length()) {
//        is_previous_special = is_special
			isPreviousSpecial=isSpecial;
//        If Mid(transformed(row), char, 1) = """" Then
			if(transformed.charAt(charIdx)=='"') {
//            in_quotes = in_quotes * -1
				inQuotes=!inQuotes;
//        End If
			}
//        is_special = 0
			isSpecial=false;
//        sp_length = 1
			specialLen=1;
//        For sp_char_index = 0 To 19
			for(String specialCharacter:specialCharacters) {
//            If Mid(transformed(row), char, Len(special_characters(sp_char_index))) = special_characters(sp_char_index) And in_quotes = -1 Then
				if (transformed.substring(charIdx).startsWith(specialCharacter) && !inQuotes) {
//                is_special = 1
					isSpecial=true;
//                sp_length = Len(special_characters(sp_char_index))
					specialLen=specialCharacter.length();
//            End If
				}
//        Next
			}
//        If is_previous_special = 1 And is_special = 0 And Mid(transformed(row), char, 1) <> """" Then
			if(isPreviousSpecial && !isSpecial && transformed.charAt(charIdx)!='"') {
//            transformed(row) = Left(transformed(row), char - 1) & """" & Right(transformed(row), Len(transformed(row)) - char + 1)
				transformed=transformed.substring(0,charIdx)+"\""+transformed.substring(charIdx);
//            char = char + 1
				charIdx++;
//        End If
			}
//        If is_previous_special = 0 And is_special = 1 And Mid(transformed(row), char - 1, 1) <> """" Then
			if(!isPreviousSpecial && isSpecial && transformed.charAt(charIdx-1)!='"'){
//            transformed(row) = Left(transformed(row), char - 1) & """" & Right(transformed(row), Len(transformed(row)) - char + 1)
				transformed=transformed.substring(0,charIdx)+"\""+transformed.substring(charIdx);
//            char = char + 1
				charIdx++;
//        End If
			}
//        char = char + sp_length
			charIdx=charIdx+specialLen;
//    Wend
		}
//    'Vérification du dernier caractère
//    If is_special = 0 And Right(transformed(row), 1) <> """" Then
		if(!isSpecial && !transformed.endsWith("\"")) {
//        transformed(row) = transformed(row) & """"
			transformed=transformed+"\"";
//    End If
		}
		//Si valeur numérique, supprimer à la fin tous les caractères non-numériques hors caractères spéciaux
		if(processingNumericValue){
			transformed=WordUtils.removeStringsInDescPattern(transformed);
		}
//    'Affichage
		return transformed;
	}


	public static String[] splitComposedPattern(String rulePattern) {
		return rulePattern.split("(?<![\\|\\*])\\+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		//return rulePattern.split("\\+(?=[^ *\\d|%])(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		//return rulePattern.split("\\+(?=(?:[^\"%\\d]*\"[^\"]*\")*[^\"]*$)");
		//(?<!x), means "only if it doesn't have "x" before this point".
		//(?=[^\d]), means "only if not followed by digit after this point".
		//\\+           // Split on "+"sign
		//(?=         // Followed by
		//		(?:      // Start a non-capture group
		//			[^"]*  // 0 or more non-quote characters
		//			"      // 1 quote
		//			[^"]*  // 0 or more non-quote characters
		//			"      // 1 quote
		//		)*       // 0 or more repetition of non-capture group (multiple of 2 quotes will be even)
		//		[^"]*    // Finally 0 or more non-quotes
		//		$        // Till the end  (This is necessary, else every comma will satisfy the condition))
	}

	public static String NUM_PATTERN_RULE_EVAL(String action, ArrayList<Double> numValuesInSelection) {
		return (new WordUtils.Rewriter("%(\\d)")
		{
			public String replacement()
			{
				int intValue = Integer.parseInt(group(1));
				Double finalValue = numValuesInSelection.get(intValue-1);
				return String.valueOf(numValuesInSelection.get(intValue-1));
			}
		}.rewrite(action));
	}

	public static String trimTextField(String text) {
		if(text!=null){
			return text.trim();
		}
		return null;
	}

	public static int modColIndex(int selected_col, ArrayList<ClassCaracteristic> classCaracteristics) {
		while(selected_col<0){
			selected_col = selected_col + classCaracteristics.size();
		}
		return Math.floorMod(selected_col,classCaracteristics.size());

	}


	public abstract static class Rewriter {
		private Pattern pattern;
		private Matcher matcher;

		/**
		 * Constructs a rewriter using the given regular expression; the syntax is
		 * the same as for 'Pattern.compile'.
		 */
		public Rewriter(String regex) {
			this.pattern = Pattern.compile(regex);
		}

		/**
		 * Returns the input subsequence captured by the given group during the
		 * previous match operation.
		 */
		public String group(int i) {
			return matcher.group(i);
		}

		/**
		 * Overridden to compute a replacement for each match. Use the method
		 * 'group' to access the captured groups.
		 */
		public abstract String replacement();

		/**
		 * Returns the result of rewriting 'original' by invoking the method
		 * 'replacement' for each match of the regular expression supplied to the
		 * constructor.
		 */
		public String rewrite(CharSequence original) {
			this.matcher = pattern.matcher(original);
			StringBuffer result = new StringBuffer(original.length());
			while (matcher.find()) {
				matcher.appendReplacement(result, "");
				result.append(replacement());
			}
			matcher.appendTail(result);
			return result.toString();
		}
	}
}
