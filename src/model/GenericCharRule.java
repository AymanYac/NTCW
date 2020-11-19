package model;

import org.apache.commons.lang3.StringUtils;
import service.CharPatternServices;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericCharRule {

	private String charRuleId;
	private String ruleMarker;
	private ArrayList<String> ruleActions;
	private String regexMarker;
	private Boolean parseFailed;
	public static String SEP_CLASS;
	private String ruleSyntax;

	public String getCharRuleId() {
		return charRuleId;
	}

	public void setCharRuleId(String charRuleId) {
		this.charRuleId = charRuleId;
	}

	public String getRuleSyntax() {
		return ruleSyntax;
	}

	public String getRuleMarker() {
		return ruleMarker;
	}

	public ArrayList<String> getRuleActions() {
		return ruleActions;
	}

	public String getRegexMarker() {
		return regexMarker;
	}

	public Boolean parseSuccess() {
		return (parseFailed!=null)?!parseFailed:null;
	}
	
	public GenericCharRule(String active_rule) {
		//SEP_CLASS = " \\.,;:-=/";
		//SEP_CLASS = " [.],;:-[+]=/";
		//SEP_CLASS = " [.],;:[+]=/";
		SEP_CLASS = " \"'[.],;:[+]=/\\|\\[\\]\\(\\)";
		try {
			parseRule(active_rule);
			parseFailed = false;
		}catch(Exception V) {
			//Problem parsing the marker
			parseFailed = true;
		}
		
	}

	private void parseRule(String active_rule) {
		int ruleMarkerEndDelimiter = active_rule.indexOf('<');
		ruleSyntax = active_rule;
		ruleMarker = active_rule.substring(0,ruleMarkerEndDelimiter);
		String ruleAction = active_rule.substring(ruleMarkerEndDelimiter);
		Pattern regexPattern = Pattern.compile("<([^<>]*)>");
		Matcher m = regexPattern.matcher(ruleAction);
		ruleActions = new ArrayList<String>();
	    while(m.find()){
	    	ruleActions.add(m.group(1));
	    }
	}

	public void generateRegex(ClassCaracteristic sourceChar) {
		if(sourceChar.getIsNumeric()) {
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"%\\d","[-+]?[0-9]+([. ,]?[0-9]{3,3})*[0-9]*([.,][0-9]+)?"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
		}else {
			String SEP_CLASS=String.valueOf(GenericCharRule.SEP_CLASS+"-");
			regexMarker = WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.substituteRuleItemToRegexItem(WordUtils.quote(ruleMarker),"\"",""),"#","[0-9]"),"@","[a-z]"),"(|+0)","["+SEP_CLASS+"]?"),"(|+1)","["+SEP_CLASS+"]+");
			if(sourceChar.getIsTranslatable()) {

			}
		}
		try{

		}catch (Exception V){
			parseFailed = true;
		}
	}


	public void storeGenericCharRule() {
		setCharRuleId(Tools.generate_uuid());
		CharPatternServices.descriptionRules.put(getCharRuleId(),this);
	}

	public void dropGenericCharRule() {
		CharPatternServices.descriptionRules.remove(getCharRuleId());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (o instanceof GenericCharRule) {
			GenericCharRule p = (GenericCharRule) o;
			return p.hashCode()==this.hashCode();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return getCharRuleId().hashCode();
	}
	@Override
	public String toString() {
		return getRuleMarker();
	}

	public boolean isEquivalent(GenericCharRule newRule) {
		return StringUtils.equals(this.getCharRuleId(),newRule.getCharRuleId()) || StringUtils.equalsIgnoreCase(this.ruleSyntax,newRule.ruleSyntax);
	}
}
