package model;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import service.CharPatternServices;
import service.TranslationServices;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.io.*;
import java.util.*;

public class CharRuleResult implements Serializable {

	public boolean action2ValueSuccess=false;
	private String genericCharRuleID;
	private String matchedBlock;
	private CaracteristicValue actionValue;
	private ArrayList<CharRuleResult> superRules= new ArrayList<CharRuleResult>();
	private String status;
	private static Unidecode unidecode;

	public CharRuleResult(GenericCharRule activeGenericCharRule, String matchedGroup, UserAccount account) {
		this.genericCharRuleID = activeGenericCharRule.getCharRuleId();
		this.matchedBlock = matchedGroup;
		try{
			ruleActionToValue(account);
			action2ValueSuccess=true;
		}catch (Exception V){
			if(matchedGroup!=null){
				V.printStackTrace(System.err);
			}
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
		actionValue.setParentChar(getGenericCharRule().getParentChar());
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
				if(actionValue.getMin_value()!=null && actionValue.getMin_value().length()>0){
					actionValue.setMax_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				}else{
					actionValue.setMin_value(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(action)));
				}
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
		return StringUtils.containsIgnoreCase(thisPattern.replaceAll("%\\d","%d"), targetPattern.replaceAll("%\\d","%d")) && thisPattern.length()>targetPattern.length();
	}
	public boolean isSuperBlockOf(CharRuleResult r, boolean allowEquality) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		String thisBlock = unidecode.decodeAndTrim(getMatchedBlock());
		String targetBlock = unidecode.decodeAndTrim(r.getMatchedBlock());
		return StringUtils.containsIgnoreCase(thisBlock, targetBlock) && (thisBlock.length()>targetBlock.length() || allowEquality);
	}
	public boolean isEqualValueOf(CharRuleResult r){
		try{
			return r.getActionValue()!=null && getActionValue()!=null
					&& r.getActionValue().getDisplayValue(false,false).equalsIgnoreCase(getActionValue().getDisplayValue(false,false));
		}catch (Exception V){
			return false;
		}
	}
	
	public boolean isSuperValueOf(CharRuleResult r){
		if(!getGenericCharRule().getParentChar().getIsNumeric().equals(r.getGenericCharRule().getParentChar().getIsNumeric())){
			return false;
		}
		if(getGenericCharRule().getParentChar().getIsNumeric()){
			ArrayList<Double> thisNums = getActionValue().getNonNullNumericsWithRepeat();
			ArrayList<Double> targetNums = r.getActionValue().getNonNullNumericsWithRepeat();
			Optional<Double> missingLoopInThis = targetNums.stream().filter(num -> !thisNums.remove(num)).findAny();
			return !missingLoopInThis.isPresent();
		}else{
			String thisTxt = getActionValue().getDataLanguageValue();
			String targetTxt = r.getActionValue().getDataLanguageValue();
			if(thisTxt!=null && targetTxt!=null && thisTxt.length()>0 && targetTxt.length()>0){
				if(targetTxt.length()<GlobalConstants.SEARCH_WORD_LARGE){
					return new ArrayList<String>(Arrays.asList(thisTxt.split("["+GenericCharRule.SEP_CLASS+"]|\""))).stream().anyMatch(elem->elem.trim().equalsIgnoreCase(targetTxt.trim()));
				}else{
					return thisTxt.toLowerCase().contains(targetTxt.toLowerCase());
				}
			}
			return false;
		}
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

	public ClassCaracteristic getSourceChar() {return getGenericCharRule().getParentChar();}

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
		GenericCharRule tmpRule = new GenericCharRule(getGenericCharRule().getRuleSyntax(),newValue);
		tmpRule.storeGenericCharRule();
		CharRuleResult tmpResult = new CharRuleResult(tmpRule,matchedBlock,account);
		return tmpResult;
	}

	public boolean isOrphan() {
		return !(getGenericCharRule()!=null);
	}


	public boolean sharesCurrentItemValue(ClassCaracteristic classCarac, CaracteristicValue currentItemValue) {
		return currentItemValue!=null && !currentItemValue.getSource().equals(DataInputMethods.AUTO_CHAR_DESC) && getActionValue()!=null
				&& currentItemValue.getDisplayValue(false,false).equalsIgnoreCase(getActionValue().getDisplayValue(false,false));
	}


	public boolean isQuasiRedundantSpanningRule(String charId, HashMap<String, ArrayList<CharRuleResult>> ruleResults) {
		return ruleResults.entrySet().stream().filter(e->!e.getKey().equals(charId)).map(e->e.getValue()).flatMap(Collection::stream).filter(r->!r.isSubRule()).anyMatch(r->r.isSpanningRedundantWith(this));
	}

	boolean isSpanningRedundantWith(CharRuleResult r) {
		return isSuperBlockOf(r,true) && r.isSuperBlockOf(this,true) && isEqualValueOf(r);
	}
}
