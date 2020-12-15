package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import org.apache.commons.lang3.StringUtils;
import service.CharValuesLoader;
import transversal.language_toolbox.Unidecode;

import java.util.*;
import java.util.stream.Collectors;

public class CharDescriptionRow {

	//Article completion
	//Question status
	//Description
	
	//Link
	//Source
	//Rule
	//Author
	//Article ID
	
		Boolean completionStatus;
		Boolean questionStatus;
		String short_desc;
		String short_desc_translated;
		String short_desc_corrected;
		String long_desc;
		String long_desc_translated;
		String long_desc_corrected;
		String material_group;
		String preclassification;
		
		String client_item_number;
		String item_id;
		private HashMap<String,HashMap<String,CaracteristicValue>> data = new HashMap<String,HashMap<String,CaracteristicValue>>();
		private HashMap<String,ArrayList<CharRuleResult>> ruleResults = new HashMap<String,ArrayList<CharRuleResult>>();
		String class_segment_string;
		ClassSegment class_segment;
		private static Unidecode unidecode;

	public void allocateDataField(String target_class) {
			if(this.data.containsKey(target_class)) {
				//This item class has already been initalized with the target class
			}else {
				this.data.put(target_class, new HashMap<String,CaracteristicValue>());
			}
			return;
		}

		public String getItem_id() {
			return item_id;
		}
		public void setItem_id(String item_id) {
			this.item_id = item_id;
		}
		public String getShort_desc() {
			return short_desc;
		}
		public void setShort_desc(String short_desc) {
			this.short_desc = short_desc;
		}
		public String getShort_desc_translated() {
			return short_desc_translated;
		}
		public void setShort_desc_translated(String short_desc_translated) {
			this.short_desc_translated = short_desc_translated;
		}
		public String getShort_desc_corrected() {
			return short_desc_corrected;
		}
		public void setShort_desc_corrected(String short_desc_corrected) {
			this.short_desc_corrected = short_desc_corrected;
		}
		public String getLong_desc() {
			return long_desc;
		}
		public void setLong_desc(String long_desc) {
			this.long_desc = long_desc;
		}
		public String getLong_desc_translated() {
			return long_desc_translated;
		}
		public void setLong_desc_translated(String long_desc_translated) {
			this.long_desc_translated = long_desc_translated;
		}
		public String getLong_desc_corrected() {
			return long_desc_corrected;
		}
		public void setLong_desc_corrected(String long_desc_corrected) {
			this.long_desc_corrected = long_desc_corrected;
		}
		
		public String getMaterial_group() {
			return material_group;
		}
		public void setMaterial_group(String material_group) {
			this.material_group = material_group;
		}
		
		public String getPreclassif() {
			return preclassification;
		}
		public void setPreclassif(String preclassif) {
			this.preclassification = preclassif;
		}
		
		public Boolean getCompletionStatus() {
			return completionStatus;
		}
		public void setCompletionStatus(Boolean completionStatus) {
			this.completionStatus = completionStatus;
		}
		public Boolean getQuestionStatus() {
			return questionStatus;
		}
		public void setQuestionStatus(Boolean questionStatus) {
			this.questionStatus = questionStatus;
		}
		
		public String getClient_item_number() {
			return client_item_number;
		}
		public void setClient_item_number(String client_item_number) {
			this.client_item_number = client_item_number;
		}
		public HashMap<String,CaracteristicValue> getData(String segment_id) {
			return data.get(segment_id);
		}
		
		
		public HashMap<String, HashMap<String, CaracteristicValue>> getData() {
			return data;
		}
		public String getClass_segment_string() {
			return class_segment_string;
		}
		public void setClass_segment_string(String class_segment_string) {
			this.class_segment_string = class_segment_string;
		}
		public ClassSegment getClass_segment() {
			return class_segment;
		}
		public void setClass_segment(ClassSegment class_segment) {
			try {
				this.class_segment_string=class_segment.getSegmentId()+"&&&"+class_segment.getClassName()+"&&&"+class_segment.getClassNumber();
			}catch(Exception V) {
				
			}
			this.class_segment = class_segment;
		}

	public HashMap<String, ArrayList<CharRuleResult>> getRuleResults() {
		return ruleResults;
	}

	public void setRuleResults( HashMap<String, ArrayList<CharRuleResult>> SerializedRuleResults) {
			if(SerializedRuleResults!=null){
				this.ruleResults = SerializedRuleResults;
			}
	}


		
		public void disableSubTextRules(String charId) {
			try {
				ruleResults.get(charId).stream().forEach(r->{
					
					Optional<CharRuleResult> SuperRule = ruleResults.get(charId).stream()
					.filter(rloop->(rloop.isSuperMarkerOf(r)||rloop.isSuperBlockOf(r)) ).findAny();
					if(SuperRule.isPresent()) {
						//System.out.println(SuperRule.get().getGenericCharRule().getRuleMarker()+" is a super rule for "+r.getGenericCharRule().getRuleMarker());
						r.addSuperRule(SuperRule);
					}
					
				});
			}catch(Exception V) {
				//V.printStackTrace(System.err);
			}
		}

		public boolean itemHasDisplayValue(CharRuleResult r){
			String targetVal = r.getActionValue().getDisplayValue(false, false);
			return getData(getClass_segment_string().split("&&&")[0]).values().stream().filter(loopVal->
				loopVal!=null && StringUtils.equalsIgnoreCase(loopVal.getDisplayValue(false,false),targetVal)
			).findAny().isPresent();
		}

		public void reEvaluateCharRules(){
			CharDescriptionRow r = this;
			//For each couple active item I / rule N
			r.getRuleResults().values().forEach(a->{
				a.stream().filter(result -> !result.isDraft()).forEach(result->{
					//The status of the rule N becomes empty for the item I
					result.setStatus(null);
					result.clearSuperRules();
				});
			});
			//Supress all auto values on the item
			r.getData(r.getClass_segment_string().split("&&&")[0]).entrySet()
					.removeIf(e->e.getValue()!=null && e.getValue().getSource().equals(DataInputMethods.AUTO_CHAR_DESC));

			//For each couple active item I / rule N
			//The pattern of the rule is included in the pattern of another rule applying to the same item and characteristic
			//The value of the rule is equal to the value of another rule N' applying to the same characteristic
			// 	and The identified pattern of N is strictly longer than the identified pattern of N'
			//The status remains empty
			r.getRuleResults().keySet().forEach(charId ->r.disableSubTextRules(charId));

			//The status of the row becomes "Suggestion j+1" for the item I,with j the highest suggestion # already present (initiated at 1)
			HashMap<String, ArrayList<CaracteristicValue>> knownRuleValues = new HashMap<String, ArrayList<CaracteristicValue>>();
			r.getRuleResults().entrySet().stream().forEach(e->e.getValue().stream().filter(result->!result.isSubRule() && !result.isDraft()).forEach(result -> {
				try{
					int valIndx = knownRuleValues.get(e.getKey()).indexOf(result.getActionValue());
					if(valIndx==-1){
						//New value
						knownRuleValues.get(e.getKey()).add(result.getActionValue());
						result.setStatus("Suggestion "+String.valueOf(knownRuleValues.get(e.getKey()).size()));
					}else{
						//Known value
						result.setStatus("Suggestion "+String.valueOf(valIndx)+1);
					}
				}catch (Exception V){
					knownRuleValues.put(e.getKey(),new ArrayList<CaracteristicValue>());
					knownRuleValues.get(e.getKey()).add(result.getActionValue());
					result.setStatus("Suggestion "+String.valueOf(knownRuleValues.get(e.getKey()).size()));
				}
			}));

			//For each couple active item I / rule N
			r.getRuleResults().keySet().forEach(charId->{
				ArrayList<CharRuleResult> suggestions = r.getRuleResults().get(charId)
						.stream()
						.filter(result -> result.getStatus()!=null && result.getStatus().startsWith("Suggestion "))
						.collect(Collectors.toCollection(ArrayList::new));
				if(suggestions.size()==1){
					//The status is "Suggestion 1" and The value of the row is not similar to the the value of another row with a status different from "empty"
					//=>wording: The status is "Suggestion 1" and other rows with similar value are empty or suggestion 1
					//There is no status different from "Suggestion 1" or "Empty" for another row related to the same characteristic
					if(!r.itemHasDisplayValue(suggestions.get(0))) {
						//The value of the row is not similar to the item value for another characteristic
						suggestions.get(0).setStatus("Applied");
					}
				}else if(!suggestions.stream().anyMatch(result -> result.getStatus().equals("Suggestion 2"))){
					suggestions.forEach(result -> result.setStatus("Applied"));
				}
			});

			//The characteristic value is updated based on the applied rule
			r.getRuleResults().keySet().forEach(charId->{
				Optional<CharRuleResult> appliedResult = r.getRuleResults().get(charId).stream()
						.filter(result -> result.getStatus()!=null).filter(result -> result.getStatus().equals("Applied")).findAny();
				if(appliedResult.isPresent()){
					String itemClass=r.getClass_segment_string().split("&&&")[0];
					if(r.getData(itemClass)!=null){
						if(r.getData(itemClass).get(charId)!=null){
							if(r.getData(itemClass).get(charId).getSource()!=null){
								return;
							}
						}
					}
					//The remaining value can only be auto, assign the "Applied" Rule value
					CharValuesLoader.updateRuntimeDataForItem(r,itemClass,charId,appliedResult.get().getActionValue());

				}
			});
		}

    public boolean hasDataInCurrentClassForCarac(String characteristic_id) {
			String itemClass = getClass_segment_string().split("&&&")[0];
			return getData(itemClass).get(characteristic_id)!=null;
    }

	public void addRuleResult2Row(CharRuleResult newMatch) {
		if(!newMatch.action2ValueSuccess){
			return;
		}
		try{
			if(this.ruleResults.get(newMatch.getSourceChar().getCharacteristic_id()).stream().anyMatch(result -> result.isEquivalentOf(newMatch))){
				return;
			}
		}catch (Exception V){

		}
		try {
			this.ruleResults.get(newMatch.getSourceChar().getCharacteristic_id()).add(newMatch);
		}catch(Exception V) {
			this.ruleResults.put(newMatch.getSourceChar().getCharacteristic_id(), new ArrayList<CharRuleResult>());
			this.ruleResults.get(newMatch.getSourceChar().getCharacteristic_id()).add(newMatch);
		}
	}

	public void dropRuleResultFromRow(CharRuleResult oldMatch) {
		try {
			String oldCharId = oldMatch.getSourceChar().getCharacteristic_id();
			this.ruleResults.put(oldCharId,
					this.ruleResults.get(oldCharId).stream().filter(result->!result.getGenericCharRuleID().equals(oldMatch.getGenericCharRuleID())).collect(Collectors.toCollection(ArrayList::new)));
		}catch(Exception V) {

		}
	}

	public ArrayList<CharRuleResult> getRulePropositions(String charId) {
		ArrayList<CharRuleResult> ret = new ArrayList<CharRuleResult>();
		CaracteristicValue charItemData = getData(getClass_segment_string().split("&&&")[0]).get(charId);
		if(
				(charItemData!=null && charItemData.getDisplayValue(false,false).length()>0)
				|| (getRuleResults().get(charId).stream().anyMatch(result -> result.getStatus()!=null && result.getStatus().equals("Applied")))
		){
			return ret;
		}
		try{
			ret = new ArrayList<CharRuleResult>(
					getRuleResults().get(charId).stream()
							.filter(result -> result.getStatus() != null && result.getStatus().startsWith("Suggestion "))
							.collect(Collectors.toCollection(ArrayList::new)));
		}catch (Exception V){

		}
		return ret;
	}

	@Override
	public int hashCode() {
		return getItem_id().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj!=null){
			if(obj instanceof CharDescriptionRow){
				return ((CharDescriptionRow) obj).getItem_id().equals(this.getItem_id());
			}
		}
		return false;
	}

	public String getAccentFreeDescriptions() {
		unidecode = unidecode!=null?unidecode: Unidecode.toAscii();
		return (getShort_desc()!=null?unidecode.decode(getShort_desc()):"")+" "+(getLong_desc()!=null?unidecode.decode(getLong_desc()):"");
	}

	public ObservableBooleanValue hasDataInCurrentClassForCurrentCarac(int selected_col) {
		String itemClass = getClass_segment_string().split("&&&")[0];
		String activeCharId = CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id();
		SimpleBooleanProperty simp = new SimpleBooleanProperty();
		simp.set(hasDataInCurrentClassForCarac(activeCharId));
		return simp;
	}
}
