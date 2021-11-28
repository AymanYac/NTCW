package model;

import org.apache.commons.lang3.StringUtils;
import service.CharPatternServices;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
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

	public static String SEP_CLASS = " '\\.,;:\\+=/\\\\|\\\\[\\\\]\\\\(\\\\)"+"-";
	public static String SEP_CLASS_NO_VERTICAL = " '\\.,;:\\+=/\\\\[\\\\]\\\\(\\\\)"+"-";

	//public static final String NUM_CLASS = "-?(?:[0-9]{1,3}(?:[. ,]+[0-9]{3,3})*|[0-9]+)(?:[.,][0-9]+)?";
	public static final String NUM_CLASS_POSITIVE = "(?:[0-9]{1,3}(?:[[ ]?.,]?[0-9]{3,3})*|[0-9]+)(?:[.,][0-9]+)?";
	public static final String NUM_CLASS = "-?"+GenericCharRule.NUM_CLASS_POSITIVE;

	private String ruleSyntax;
	private static  Unidecode unidecode;
	private ClassCaracteristic parentChar;

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
	public void setRegexMarker() {
		unidecode=unidecode!=null?unidecode: Unidecode.toAscii();
		String[] composedMarkers = WordUtils.splitComposedPattern(unidecode.decode(ruleMarker));
		/*
		dead code : removed .*( at the beggining of the regex rank element and removed ).* to fix : Regression: Nombre avec décimale tronqué pour cause d'expression régulière avare
		=>new bug : composed markers are not matched
		for(int i=0;i<composedMarkers.length;i++){
			if(i!=composedMarkers.length-1){
				regexMarker=regexMarker+"(?=.*("
						+WordUtils.quoteStringsInDescPattern(WordUtils.neonecObjectSyntaxToRegex(composedMarkers[i], sourceChar.getIsNumeric()?GenericCharRule.SEP_CLASS_NUM:GenericCharRule.SEP_CLASS_TXT,true))
						+").*)";
			}else{
				regexMarker=regexMarker+".*("
						+WordUtils.quoteStringsInDescPattern(WordUtils.neonecObjectSyntaxToRegex(composedMarkers[i], sourceChar.getIsNumeric()?GenericCharRule.SEP_CLASS_NUM:GenericCharRule.SEP_CLASS_TXT,true))
						+").*";
			}
		}

		 */
		if(composedMarkers.length>1){
			//The rule rank is supp to 1, the rule is composed
			regexMarker="^";
			for(int i=0;i<composedMarkers.length;i++){
				//added ? after * in (?=.*
				//* matches the previous token between zero and unlimited times, as many times as possible, giving back as needed (greedy)
				//*? matches the previous token between zero and unlimited times, as few times as possible, expanding as needed (lazy)
				//Because we want the .* block to be as small as possible when the meaningful block to be as large as possible (at least on 2nd degree groups)
				regexMarker+="(?=.*?("
						+WordUtils.quoteStringsInDescPattern(WordUtils.neonecObjectSyntaxToRegex(composedMarkers[i], true))
						+"))";
			}
			regexMarker+=".*$";
		}else {
			//The rule rank is == 1, the rule is simple
			//removed .*(
			//removed ).*
			regexMarker="(?="+WordUtils.quoteStringsInDescPattern(WordUtils.neonecObjectSyntaxToRegex(composedMarkers[0], true))+")";
		}
		return;
	}



	public Boolean parseSuccess() {
		return (parseFailed!=null)?!parseFailed:null;
	}
	
	public GenericCharRule(String active_rule, ClassCaracteristic parentCharVar) {
		//SEP_CLASS = " \\.,;:-=/";
		//SEP_CLASS = " [.],;:-[+]=/";
		//SEP_CLASS = " [.],;:[+]=/";
		try {
			parseRule(active_rule);
			parseFailed = false;
		}catch(Exception V) {
			//Problem parsing the marker
			parseFailed = true;
		}
		setParentChar(parentCharVar);
		
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


	public void storeGenericCharRule() {
		if(getCharRuleId()==null){
			setCharRuleId(Tools.generate_uuid());
		}
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

	public boolean isCompositRule() {
		return ruleCompositionRank()>1;
	}
	public int ruleCompositionRank() {
		return WordUtils.splitComposedPattern(ruleMarker).length;
	}

	public ClassCaracteristic getParentChar() {
		return this.parentChar;
	}
	public void setParentChar(ClassCaracteristic parentChar){
		this.parentChar = parentChar;
	}
}
