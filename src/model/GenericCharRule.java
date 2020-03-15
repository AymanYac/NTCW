package model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transversal.language_toolbox.WordUtils;

public class GenericCharRule {

	private String ruleMarker;
	private ArrayList<String> ruleActions;
	private String sourceClass;
	private ClassCharacteristic sourceChar;
	private String regexMarker;
	private Boolean parseFailed;
	public static String SEP_CLASS;
	
	
	
	public String getRuleMarker() {
		return ruleMarker;
	}

	public ArrayList<String> getRuleActions() {
		return ruleActions;
	}

	public String getSourceClass() {
		return sourceClass;
	}

	public ClassCharacteristic getSourceChar() {
		return sourceChar;
	}

	public String getRegexMarker() {
		return regexMarker;
	}

	public Boolean parseSuccess() {
		return (parseFailed!=null)?!parseFailed:null;
	}
	
	public GenericCharRule(String active_class, ClassCharacteristic active_char, String active_rule) {
		//SEP_CLASS = " \\.,;:-=/";
		SEP_CLASS = " [.],;:-[+]=/";
		sourceClass = active_class;
		sourceChar = active_char;
		try {
			parseRule(active_rule);
			parseFailed = false;
		}catch(Exception V) {
			//Problem separating the marker and the action
			V.printStackTrace(System.err);
			parseFailed = true;
		}
		
	}

	private void parseRule(String active_rule) {
		int ruleMarkerEndDelimiter = active_rule.indexOf('<');
		ruleMarker = active_rule.substring(0,ruleMarkerEndDelimiter);
		String ruleAction = active_rule.substring(ruleMarkerEndDelimiter);
		Pattern regexPattern = Pattern.compile("<([^<>]*)>");
		Matcher m = regexPattern.matcher(ruleAction);
		ruleActions = new ArrayList<String>();
	    while(m.find()){
	    	ruleActions.add(m.group(1));
	    }
		generateRegex();
	}

	private void generateRegex() {
		if(getSourceChar().getIsNumeric()) {
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"%\\d","[-+]?[0-9]*[.,]?[0-9]+"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
		}else {
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"#","[0-9]"),"@","[a-z]"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
			if(getSourceChar().getIsTranslatable()) {
				
			}
		}
		System.out.println("Transformed marker: "+ruleMarker+" -> "+regexMarker);
	}


	

}
