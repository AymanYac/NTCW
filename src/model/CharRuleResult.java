package model;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import service.CharPatternServices;
import service.TranslationServices;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharRuleResult implements Serializable {

	public boolean action2ValueSuccess=false;
	private String genericCharRuleID;
	private String matchedBlock;
	private CaracteristicValue actionValue;
	private ArrayList<CharRuleResult> superRules= new ArrayList<CharRuleResult>();
	private ClassCaracteristic parentChar;
	private String status;
	private static Unidecode unidecode;

	public CharRuleResult(GenericCharRule activeGenericCharRule, ClassCaracteristic parentChar, String matchedGroup, UserAccount account) {
		this.genericCharRuleID = activeGenericCharRule.getCharRuleId();
		this.matchedBlock = matchedGroup;
		this.parentChar = parentChar;
		try{
			ruleActionToValue(account);
			action2ValueSuccess=true;
		}catch (Exception V){
			action2ValueSuccess=false;
		}
	}


	public String getGenericCharRuleID() {
		return genericCharRuleID;
	}

	public void setGenericCharRuleID(String genericCharRuleID) {
		this.genericCharRuleID = genericCharRuleID;
	}


	public String getMatchedBlock() {
		return matchedBlock;
	}

	public void setMatchedBlock(String matchedBlock) {
		this.matchedBlock = matchedBlock;
	}

	public CaracteristicValue getActionValue() {
		return actionValue;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void ruleActionToValue(UserAccount account) {
		actionValue = new CaracteristicValue();
		actionValue.setParentChar(parentChar);
		actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);

		GenericCharRule genericCharRule = CharPatternServices.descriptionRules.get(genericCharRuleID);
		for(String action: genericCharRule.getRuleActions()) {
			if(action.startsWith("DL ")) {
				action=action.substring(3).trim();
				actionValue.setDataLanguageValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL(action,matchedBlock));
				//actionValue.setDataLanguageValue(action);
				actionValue.setUserLanguageValue(TranslationServices.getEntryTranslation(action, true));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("UL ")) {
				action=action.substring(3).trim();
				actionValue.setUserLanguageValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL(action,matchedBlock));
				//actionValue.setUserLanguageValue(action);
				actionValue.setDataLanguageValue(TranslationServices.getEntryTranslation(action, false));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("TXT ")) {
				action=action.substring(4).trim();
				actionValue.setTXTValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL(action,matchedBlock));
				//actionValue.setTXTValue(action);
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("NOM ")) {

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
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);

			}
			if(action.startsWith("MIN ")) {

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
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("MAX ")) {

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
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("MINMAX ")) {

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
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("UOM ")) {
				final String symbol=action.substring(5).substring(0,action.length()-6).trim();
				Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->u.getUom_symbols().contains(symbol)).findAny();
				if(uom.isPresent()) {
					actionValue.setUom_id(uom.get().getUom_id());
					actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
				}



			}

		}
	}

	public boolean isSuperBlockOf(CharRuleResult r) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisBlock = unidecode.decodeAndTrim(getMatchedBlock());
		String targetBlock = unidecode.decodeAndTrim(r.getMatchedBlock());
		String thisValue = unidecode.decodeAndTrim(getActionValue().getDisplayValue(false,false));
		String targetValue = unidecode.decodeAndTrim(r.getActionValue().getDisplayValue(false,false));
		return thisBlock.length()>targetBlock.length() && StringUtils.equalsIgnoreCase(thisValue, targetValue);
	}

	public boolean isSuperMarkerOf(CharRuleResult r) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisPattern = unidecode.decodeAndTrim(getGenericCharRule().getRuleMarker());
		String targetPattern = unidecode.decodeAndTrim(r.getGenericCharRule().getRuleMarker());
		return thisPattern.length()>targetPattern.length() && StringUtils.containsIgnoreCase(thisPattern, targetPattern);
	}
	public void addSuperRule(Optional<CharRuleResult> superRule) {
		superRules.add(superRule.get());
	}

	public boolean isSubRule() {
		return this.superRules.size()>0;
	}


	public GenericCharRule getGenericCharRule() {
		return CharPatternServices.descriptionRules.get(getGenericCharRuleID());
	}

	public ClassCaracteristic getSourceChar() {return parentChar;}

	public void clearSuperRules() {
		superRules = new ArrayList<CharRuleResult>();
	}

	public ByteArrayInputStream getByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			return bais;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		return null;
	}
	public byte[] serialize(){
		return SerializationUtils.serialize(this);
	}
	public static CharRuleResult deserialize(byte[] byteArray) {
		CharRuleResult serializedResult = (CharRuleResult) SerializationUtils.deserialize(byteArray);
		return serializedResult;
	}

    public boolean isEquivalentOf(CharRuleResult newMatch) {
		return StringUtils.equalsIgnoreCase(this.getMatchedBlock(),newMatch.getMatchedBlock()) && this.getGenericCharRule().isEquivalent(newMatch.getGenericCharRule());
    }
}
