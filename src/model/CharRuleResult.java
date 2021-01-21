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
			V.printStackTrace(System.err);
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
	public void setActionValue(CaracteristicValue pattern_value) {
		action2ValueSuccess=true;
		actionValue=pattern_value;
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
				actionValue.setDataLanguageValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock));
				//actionValue.setDataLanguageValue(action);
				actionValue.setUserLanguageValue(TranslationServices.getEntryTranslation(action, true));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("UL ")) {
				action=action.substring(3).trim();
				actionValue.setUserLanguageValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock));
				//actionValue.setUserLanguageValue(action);
				actionValue.setDataLanguageValue(TranslationServices.getEntryTranslation(action, false));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("TXT ")) {
				action=action.substring(4).trim();
				actionValue.setTXTValue(WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock));
				//actionValue.setTXTValue(action);
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
			}
			if(action.startsWith("NOM ")) {
				action=action.substring(4).trim();
				//ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
				//action = WordUtils.NUM_PATTERN_RULE_EVAL(action,numValuesInSelection);
				action = WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock);
				actionValue.setNominal_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);

			}
			if(action.startsWith("MIN ")) {

				action=action.substring(4).trim();
				//ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
				//action = WordUtils.NUM_PATTERN_RULE_EVAL(action,numValuesInSelection);
				action = WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock);
				actionValue.setMin_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("MAX ")) {

				action=action.substring(4).trim();
				//ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
				//action = WordUtils.NUM_PATTERN_RULE_EVAL(action,numValuesInSelection);
				action = WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock);
				actionValue.setMax_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("MINMAX ")) {

				action=action.substring(7).trim();
				//ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(matchedBlock);
				//action = WordUtils.NUM_PATTERN_RULE_EVAL(action,numValuesInSelection);
				action = WordUtils.ALPHANUM_PATTERN_RULE_EVAL_STEPWISE(genericCharRule,action,matchedBlock);
				actionValue.setMax_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
				actionValue.setAuthor(account.getUser_id());
				actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);


			}

			if(action.startsWith("UOM ")) {
				final String symbol=action.substring(5).substring(0,action.length()-6).trim();
				Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->u.toString().equals(symbol) || u.getUom_symbols().contains(symbol)).findAny();
				if(uom.isPresent()) {
					actionValue.setUom_id(uom.get().getUom_id());
					actionValue.setRule_id(genericCharRule.getRuleMarker()+"<"+String.join("><", genericCharRule.getRuleActions())+">");
					actionValue.setAuthor(account.getUser_id());
					actionValue.setSource(DataInputMethods.AUTO_CHAR_DESC);
				}



			}

		}
	}

	public boolean isRedudantWith(CharRuleResult r) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisBlock = unidecode.decodeAndTrim(getMatchedBlock());
		String targetBlock = unidecode.decodeAndTrim(r.getMatchedBlock());
		String thisValue = unidecode.decodeAndTrim(getActionValue().getDisplayValue(false,false));
		String targetValue = unidecode.decodeAndTrim(r.getActionValue().getDisplayValue(false,false));
		return StringUtils.equalsIgnoreCase(thisValue, targetValue) && thisBlock.length()>targetBlock.length();
	}

	public boolean isSuperMarkerOf(CharRuleResult r) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisPattern = unidecode.decodeAndTrim(getGenericCharRule().getRuleMarker());
		String targetPattern = unidecode.decodeAndTrim(r.getGenericCharRule().getRuleMarker());
		return StringUtils.containsIgnoreCase(thisPattern, targetPattern) && thisPattern.length()>targetPattern.length();
	}
	public boolean isSuperBlockOf(CharRuleResult r) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisBlock = unidecode.decodeAndTrim(getMatchedBlock());
		String targetBlock = unidecode.decodeAndTrim(r.getMatchedBlock());
		return StringUtils.containsIgnoreCase(thisBlock, targetBlock) && thisBlock.length()>targetBlock.length();
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
	public void setParentChar(ClassCaracteristic newValue) {
		parentChar=newValue;
	}

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


    public boolean isDraft() {
		return getStatus()!=null && getStatus().equals("Draft");
    }

	public CharRuleResult shallowCopy(ClassCaracteristic newValue,UserAccount account) {
		CharRuleResult tmp = new CharRuleResult(getGenericCharRule(),newValue,matchedBlock,account);
		return tmp;
	}

	public boolean isOrphan() {
		return !(getGenericCharRule()!=null);
	}


}
