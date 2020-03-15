package model;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import controllers.Char_description;
import service.TranslationServices;
import transversal.language_toolbox.WordUtils;

public class CharRuleResult {

	private GenericCharRule genericCharRule;
	private String matchedText;
	private String matchedBlock;
	private CharacteristicValue actionValue;

	public CharRuleResult(GenericCharRule matchedRule, String matchedText, String matchedGroup) {
		this.genericCharRule = matchedRule;
		this.matchedText = matchedText;
		this.matchedBlock = matchedGroup;
	}

	public GenericCharRule getGenericCharRule() {
		return genericCharRule;
	}

	public void setGenericCharRule(GenericCharRule genericCharRule) {
		this.genericCharRule = genericCharRule;
	}

	public String getMatchedText() {
		return matchedText;
	}

	public void setMatchedText(String matchedText) {
		this.matchedText = matchedText;
	}

	public String getMatchedBlock() {
		return matchedBlock;
	}

	public void setMatchedBlock(String matchedBlock) {
		this.matchedBlock = matchedBlock;
	}

	public void ruleActionToValue(Char_description parent) {
		actionValue = new CharacteristicValue();
		for(String action:genericCharRule.getRuleActions()) {
			if(action.startsWith("DL ")) {
				action=action.substring(3).trim();
				actionValue.setDataLanguageValue(action);
				actionValue.setUserLanguageValue(TranslationServices.getEntryTranslation(action, true));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(parent.account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("UL ")) {
				action=action.substring(3).trim();
				actionValue.setUserLanguageValue(action);
				actionValue.setDataLanguageValue(TranslationServices.getEntryTranslation(action, false));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(parent.account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("TXT ")) {
				action=action.substring(4).trim();
				actionValue.setTXTValue(action);
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(parent.account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("NOM ")) {
				try {
					action=action.substring(4).trim();
					ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
					Matcher tmp = Pattern.compile("%(\\d)").matcher(action);
				    StringBuffer sb = new StringBuffer();
				    while(tmp.find()){
				      String idx = tmp.group(1);
				      String replacement = String.valueOf(numValuesInSelection.get(Integer.valueOf(idx)-1));
				      tmp.appendReplacement(sb, replacement);
				    }
				    action = sb.toString();
				    actionValue.setNominal_value(String.valueOf(new DoubleEvaluator().evaluate(action)));
			        actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(parent.account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
					
		      } catch (Exception e) {
		    	  e.printStackTrace(System.err);
		      }
			}
			if(action.startsWith("MIN ")) {
				try {
					action=action.substring(4).trim();
					ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
					Matcher tmp = Pattern.compile("%(\\d)").matcher(action);
				    StringBuffer sb = new StringBuffer();
				    while(tmp.find()){
				      String idx = tmp.group(1);
				      String replacement = String.valueOf(numValuesInSelection.get(Integer.valueOf(idx)-1));
				      tmp.appendReplacement(sb, replacement);
				    }
				    action = sb.toString();
			    	actionValue.setMin_value(String.valueOf(new DoubleEvaluator().evaluate(action)));
			        actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(parent.account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
					
		      } catch (Exception e) {
		    	  e.printStackTrace(System.err);
		      }
			}
			
			if(action.startsWith("MAX ")) {
				try {
					action=action.substring(4).trim();
					ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
					Matcher tmp = Pattern.compile("%(\\d)").matcher(action);
				    StringBuffer sb = new StringBuffer();
				    while(tmp.find()){
				      String idx = tmp.group(1);
				      String replacement = String.valueOf(numValuesInSelection.get(Integer.valueOf(idx)-1));
				      tmp.appendReplacement(sb, replacement);
				    }
				    action = sb.toString();
			    	actionValue.setMax_value(String.valueOf(new DoubleEvaluator().evaluate(action)));
			        actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(parent.account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
					
		      } catch (Exception e) {
		    	  e.printStackTrace(System.err);
		      }
			}
			
			if(action.startsWith("MINMAX ")) {
				try {
					action=action.substring(7).trim();
					ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
					Matcher tmp = Pattern.compile("%(\\d)").matcher(action);
				    StringBuffer sb = new StringBuffer();
				    while(tmp.find()){
				      String idx = tmp.group(1);
				      String replacement = String.valueOf(numValuesInSelection.get(Integer.valueOf(idx)-1));
				      tmp.appendReplacement(sb, replacement);
				    }
				    action = sb.toString();
			    	actionValue.setMax_value(String.valueOf(new DoubleEvaluator().evaluate(action)));
			        actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(parent.account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
					
		      } catch (Exception e) {
		    	  e.printStackTrace(System.err);
		      }
			}
			
			if(action.startsWith("UOM ")) {
				try {
					final String symbol=action.substring(5).substring(0,action.length()-6).trim();
					Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->u.getUom_symbols().contains(symbol)).findAny();
					if(uom.isPresent()) {
						actionValue.setUom_id(uom.get().getUom_id());
						actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><",genericCharRule.getRuleActions())+">");
						actionValue.setAuthor(parent.account.getUser_id());
						actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
					}
					
					
		      } catch (Exception e) {
		    	  e.printStackTrace(System.err);
		      }
			}
			
		}
	}
	
	

}
