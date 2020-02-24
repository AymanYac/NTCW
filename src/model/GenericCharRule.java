package model;

import java.util.HashMap;

import transversal.language_toolbox.WordUtils;

public class GenericCharRule {

	private String ruleMarker;
	private String ruleAction;
	private String sourceClass;
	private ClassCharacteristic sourceChar;
	private String regexMarker;
	private Boolean parseFailed;
	private static String SEP_CLASS;
	private static HashMap<String,GenericCharRule> knownRules;
	
	
	
	public String getRuleMarker() {
		return ruleMarker;
	}

	public String getRuleAction() {
		return ruleAction;
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
		SEP_CLASS = " \\.,;:-=/";
		sourceClass = active_class;
		sourceChar = active_char;
		try {
			parseRule(active_rule);
		}catch(Exception V) {
			//Problem separating the marker and the action
			V.printStackTrace(System.err);
			parseFailed = true;
		}
		
	}

	private void parseRule(String active_rule) {
		int ruleMarkerEndDelimiter = active_rule.indexOf('<');
		ruleMarker = active_rule.substring(0,ruleMarkerEndDelimiter);
		ruleAction = active_rule.substring(ruleMarkerEndDelimiter);
		parseFailed = false;
		if(checkRuleMarkerAmbiguity(ruleMarker,ruleAction)) {
			//The marker is known the action is different
			parseFailed = null;
		}
		generateRegex();
	}

	private void generateRegex() {
		if(getSourceChar().getIsNumeric()) {
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"[","("),"]",")"),"%\\d","([-+]?[0-9]*[\\.,]?[0-9]+)"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
		}else {
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"[","("),"]",")"),"#","[0-9]"),"@","[a-z]"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
			if(getSourceChar().getIsTranslatable()) {
				
			}
		}
		System.out.println("Transformed marker: "+ruleMarker+" -> "+regexMarker);
	}

	private boolean checkRuleMarkerAmbiguity(String ruleMarker2, String ruleAction2) {
		try {
			return !knownRules.get(ruleMarker2).getRuleAction().equals(ruleMarker2);
		}catch(Exception V) {
			knownRules = (knownRules!=null)?knownRules:new HashMap<String,GenericCharRule>();
			knownRules.put(ruleMarker2, this);
			return false;
		}
	}


	

}
