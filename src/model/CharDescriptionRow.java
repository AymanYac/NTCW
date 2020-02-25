package model;

import java.util.HashMap;
import java.util.List;

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
		
		String client_item_number;
		String item_id;
		private HashMap<String,CharacteristicValue[]> data = new HashMap<String,CharacteristicValue[]>();
		String class_segment;
		
		
		
		/*
		public CharDescriptionRow(String segment_id, int data_length) {
			this.data.put(segment_id, new CharacteristicValue[data_length]);
		}
		public CharDescriptionRow() {
			// TODO Auto-generated constructor stub
		}*/
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
		
		
		
		
		
}
