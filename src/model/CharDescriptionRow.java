package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;

import controllers.Char_description;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;

public class CharDescriptionRow {

	//Article completion
	//Question status
	//Description
	
	//Link
	//Source
	//Rule
	//Author
	//Article ID
	
		private Char_description parent;
		Boolean completionStatus;
		Boolean questionStatus;
		String short_desc;
		String short_desc_translated;
		String short_desc_corrected;
		String long_desc;
		String long_desc_translated;
		String long_desc_corrected;
		String material_group;
		
		String client_item_number;
		String item_id;
		private HashMap<String,CharacteristicValue[]> data = new HashMap<String,CharacteristicValue[]>();
		private HashMap<String, ArrayList<CharRuleResult>[]> ruleResults = new HashMap<String,ArrayList<CharRuleResult>[]>();
		private HashMap<String, ArrayList<HashMap<String, CharRuleResult>>> rulePropositions = new HashMap<String, ArrayList<HashMap<String, CharRuleResult>>>();
		String class_segment;
		
		public void setParent(Char_description parent) {
			this.parent = parent;
		}
		public void allocateDataField(String target_class, int data_length) {
			if(this.data.containsKey(target_class)) {
				//This item class has already been initalized with the target class
			}else {
				this.data.put(target_class, new CharacteristicValue[data_length]);
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
		public CharacteristicValue[] getData(String segment_id) {
			return data.get(segment_id);
		}
		
		
		public HashMap<String, ArrayList<HashMap<String, CharRuleResult>>> getRulePropositions() {
			return rulePropositions;
		}
		public HashMap<String, CharacteristicValue[]> getData() {
			return data;
		}
		public String getClass_segment() {
			return class_segment;
		}
		public void setClass_segment(String class_segment) {
			this.class_segment = class_segment;
		}
		public boolean hasDataInSegments(List<String> targetClasses) {
			return targetClasses.stream().anyMatch(s->hasDataDataInSegment(s));
		}
		
		private boolean hasDataDataInSegment(String segment) {
			return data.keySet().contains(segment);
		}
		
		
		public void addCharRuleResult(CharRuleResult newMatch, String segment, int charIdx, int charIdSize) {
			try {
				this.ruleResults.get(segment)[charIdx].add(newMatch);
			}catch(Exception V) {
				ArrayList<CharRuleResult>[] al = new ArrayList[charIdSize];
				// initializing 
		        for (int i = 0; i < charIdSize; i++) { 
		            al[i] = new ArrayList<CharRuleResult>(); 
		        }
				this.ruleResults.put(segment, al);
				this.ruleResults.get(segment)[charIdx].add(newMatch);
			}
		}
		
		public void disableSubTextRules(String segment, int charIdx) {
			try {
				ruleResults.get(segment)[charIdx].stream().forEach(r->{
					
					Optional<CharRuleResult> SuperRule = ruleResults.get(segment)[charIdx].stream()
					.filter(rloop->rloop.isSuperBlockOf(r)).findAny();
					if(SuperRule.isPresent()) {
						System.out.println(SuperRule.get().getGenericCharRule().getRuleMarker()+" is a super rule for "+r.getGenericCharRule().getRuleMarker());
						r.addSuperRule(SuperRule);
					}
					
				});
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
		}
		public HashMap<String, CharRuleResult> returnUnfilledResults(String segment, int charIdx, int size) {
			// Filter out subrules then return map of (value display) -> CharResult of unfilled results
			HashMap<String, CharRuleResult> distinctUnfilledResult = new HashMap<String,CharRuleResult>();
			HashSet<Integer> filledResultsMatched = new HashSet<Integer>();
			try {
				ruleResults.get(segment)[charIdx].stream()
				.filter(r->!r.isSubRule()).filter(r->!itemHasDisplayValue(r,segment,charIdx,size,filledResultsMatched))
				.forEach(r->distinctUnfilledResult.put(r.getActionValue().getDisplayValue(parent), r));;
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
			return distinctUnfilledResult;
			
		}
		private boolean itemHasDisplayValue(CharRuleResult r, String segment, int charIdx, int size, HashSet<Integer> filledResultsMatched) {
			//filledResultsMatched tracks char idxes already matching a rule result
			//For the input char result check if there's a filled value that matches it
			//If it does, add the index to filledResultsMatched and return true
			return IntStream.range(0, size).anyMatch(idx->{
				try {
					if(filledResultsMatched.contains(idx)) {
						return false;
					}
					String loopVal = getData(segment)[idx].getDisplayValue(parent);
					String ruleVal = r.getActionValue().getDisplayValue(parent);
					boolean ret = StringUtils.equalsIgnoreCase(loopVal, ruleVal);
					if(ret) {
						System.out.println("known value "+ruleVal+" for rule "+r.getGenericCharRule().getRuleMarker()+" at index "+String.valueOf(idx+1));
						filledResultsMatched.add(idx);
					}
					return ret;
				}catch(Exception V) {
					return false;
				}
			});
		}
		
		public void setCharProp(String segment, int charIdx, HashMap<String, CharRuleResult> distinctResultsLeft, int charIdSize) {
			try {
				this.rulePropositions.get(segment).set(charIdx,distinctResultsLeft);
			}catch(Exception V) {
				ArrayList<HashMap<String, CharRuleResult>> al = new ArrayList<HashMap<String, CharRuleResult>>(charIdSize);
				// initializing 
		        for (int i = 0; i < charIdSize; i++) { 
		            al.add(new HashMap<String,CharRuleResult>()); 
		        }
		        
				this.rulePropositions.put(segment, al);
				this.rulePropositions.get(segment).set(charIdx,distinctResultsLeft);
			}
		}
		public void reEvaluateRulesForChar(String segment, int charIdx, int charIdxSize) {
			//If the value is MANUAL or UPLOAD, continue
			if(DataInputMethods.MANUAL.equals(getData(segment)[charIdx].getSource())
				||DataInputMethods.PROJECT_SETUP_UPLOAD.equals(getData(segment)[charIdx].getSource())) {
				return;
			}
			
			disableSubTextRules(segment,charIdx);
			HashMap<String, CharRuleResult> distinctResultsLeft = returnUnfilledResults(segment,charIdx,charIdxSize);
			System.out.println("**\tDistinct results :"+distinctResultsLeft.size());
			if(distinctResultsLeft.size()>0) {
				if(distinctResultsLeft.size()==1) {
					try{
						getRulePropositions().get(segment).get(charIdx).clear();
					}catch(Exception V) {
						
					}
					getData(segment)[charIdx] = distinctResultsLeft.values().stream().findFirst().get().getActionValue();
					CharDescriptionExportServices.addCharDataToPush(this,segment,charIdx,charIdxSize);
				}else {
					setCharProp(segment,charIdx,distinctResultsLeft,charIdxSize);
				}
			}
		}
		public void reEvaluateCharRules(String segment, int charIdxSize) {
			IntStream.range(0, charIdxSize).forEach(loopIdx->{
				try{
					reEvaluateRulesForChar(segment,loopIdx,charIdxSize);
				}catch(Exception V) {
					
				}
			});
		}

		
}
