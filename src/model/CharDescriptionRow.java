package model;

import controllers.Char_description;
import org.apache.commons.lang.StringUtils;
import service.CharValuesLoader;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;

import java.util.*;

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
		String preclassification;
		
		String client_item_number;
		String item_id;
		private HashMap<String,HashMap<String,CaracteristicValue>> data = new HashMap<String,HashMap<String,CaracteristicValue>>();
		private HashMap<String, HashMap<String,ArrayList<CharRuleResult>>> ruleResults = new HashMap<String, HashMap<String,ArrayList<CharRuleResult>>>();
		private HashMap<String, HashMap<String,HashMap<String, CharRuleResult>>> rulePropositions = new HashMap<String, HashMap<String,HashMap<String, CharRuleResult>>>();
		String class_segment_string;
		ClassSegment class_segment;
		
		public void setParent(Char_description parent) {
			this.parent = parent;
		}
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
		
		
		public HashMap<String, HashMap<String, HashMap<String, CharRuleResult>>> getRulePropositions() {
			return rulePropositions;
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
		
		public boolean hasDataInSegments(List<String> targetClasses) {
			return targetClasses.stream().anyMatch(s->hasDataDataInSegment(s));
		}
		
		private boolean hasDataDataInSegment(String segment) {
			return data.keySet().contains(segment);
		}
		
		
		public void addCharRuleResult(CharRuleResult newMatch, String segment, String charId) {
			//System.out.println("New rule match for item "+getClient_item_number()+" : "+newMatch.getActionValue().getDisplayValue(parent));
			try {
				this.ruleResults.get(segment).get(charId).add(newMatch);
			}catch(Exception V) {
				HashMap<String,ArrayList<CharRuleResult>> al = new HashMap<String,ArrayList<CharRuleResult>>();
				// initializing 
		        al.put(charId,new ArrayList<CharRuleResult>());
				this.ruleResults.put(segment, al);
				this.ruleResults.get(segment).get(charId).add(newMatch);
			}
		}
		
		public void disableSubTextRules(String segment, String charId) {
			try {
				ruleResults.get(segment).get(charId).stream().forEach(r->{
					
					Optional<CharRuleResult> SuperRule = ruleResults.get(segment).get(charId).stream()
					.filter(rloop->rloop.isSuperBlockOf(r)).findAny();
					if(SuperRule.isPresent()) {
						//System.out.println(SuperRule.get().getGenericCharRule().getRuleMarker()+" is a super rule for "+r.getGenericCharRule().getRuleMarker());
						r.addSuperRule(SuperRule);
					}
					
				});
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
		}
		public HashMap<String, CharRuleResult> returnUnfilledResults(String segment, String charId) {
			// Filter out subrules then return map of (value display) -> CharResult of unfilled results
			HashMap<String, CharRuleResult> distinctUnfilledResult = new HashMap<String,CharRuleResult>();
			HashSet<String> filledResultsMatched = new HashSet<String>();
			try {
				ruleResults.get(segment).get(charId).stream()
				.filter(r->!r.isSubRule()).filter(r->!itemHasDisplayValue(r,segment,filledResultsMatched))
				.forEach(r->distinctUnfilledResult.put(r.getActionValue().getDisplayValue(parent), r));;
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
			return distinctUnfilledResult;
			
		}
		private boolean itemHasDisplayValue(CharRuleResult r, String segment, HashSet<String> filledResultsMatched) {
			//filledResultsMatched tracks char idxes already matching a rule result
			//For the input char result check if there's a filled value that matches it
			//If it does, add the index to filledResultsMatched and return true
			return CharValuesLoader.active_characteristics.get(segment).stream().anyMatch(carac->{
				try {
					if(filledResultsMatched.contains(carac.getCharacteristic_id())) {
						return false;
					}
					String loopVal = getData(segment).get(carac.getCharacteristic_id()).getDisplayValue(parent);
					String ruleVal = r.getActionValue().getDisplayValue(parent);
					boolean ret = StringUtils.equalsIgnoreCase(loopVal, ruleVal);
					if(ret) {
						//System.out.println("known value "+ruleVal+" for rule "+r.getGenericCharRule().getRuleMarker()+" at index "+String.valueOf(idx+1));
						filledResultsMatched.add(carac.getCharacteristic_id());
					}
					return ret;
				}catch(Exception V) {
					return false;
				}
			});
		}
		
		public void setCharProp(String segment, String charId, HashMap<String, CharRuleResult> distinctResultsLeft) {
			try {
				this.rulePropositions.get(segment).put(charId,distinctResultsLeft);
			}catch(Exception V) {
				HashMap <String,HashMap<String, CharRuleResult>> al = new HashMap <String,HashMap<String, CharRuleResult>>();
				this.rulePropositions.put(segment, al);
				this.rulePropositions.get(segment).put(charId,distinctResultsLeft);
			}
		}
		public void reEvaluateRulesForChar(String segment, String charId) {
			//System.out.println("\tReevaluation rules for char "+charIdx+ " in segment "+segment);
			//If the value is MANUAL or UPLOAD, continue
			if(DataInputMethods.MANUAL.equals(getData(segment).get(charId).getSource())
				||DataInputMethods.PROJECT_SETUP_UPLOAD.equals(getData(segment).get(charId).getSource())) {
				return;
			}
			
			disableSubTextRules(segment,charId);
			HashMap<String, CharRuleResult> distinctResultsLeft = returnUnfilledResults(segment,charId);
			//System.out.println("**\tDistinct results :"+distinctResultsLeft.size());
			if(distinctResultsLeft.size()>0) {
				if(distinctResultsLeft.size()==1) {
					try{
						getRulePropositions().get(segment).get(charId).clear();
					}catch(Exception V) {
						
					}
					getData(segment).put(charId,distinctResultsLeft.values().stream().findFirst().get().getActionValue());
					CharDescriptionExportServices.addItemCharDataToPush(this,segment,charId);
				}else {
					setCharProp(segment,charId,distinctResultsLeft);
				}
			}
		}
		public void reEvaluateCharRules(String segment) {
			//System.out.println("reEvaluating rules for item "+getClient_item_number()+" in segment "+segment);
			CharValuesLoader.active_characteristics.get(segment).forEach(loopCarac->{
				try{
					reEvaluateRulesForChar(segment,loopCarac.getCharacteristic_id());
				}catch(Exception V) {
					
				}
			});
		}


    public boolean hasDataInCurrentClassForCarac(String characteristic_id) {
			String itemClass = getClass_segment_string().split("&&&")[0];
			return getData(itemClass).get(characteristic_id)!=null;
    }
}
