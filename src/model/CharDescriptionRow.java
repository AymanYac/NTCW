package model;

import java.util.HashMap;

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
		
		String url;
		String source;
		String author;
		String client_item_number;
		String item_id;
		public HashMap<String,CharacteristicValue[]> data = new HashMap<String,CharacteristicValue[]>();
		String rule_id;
		String class_segment;
		
		
		
		public String getRule_id() {
			return rule_id;
		}
		public void setRule_id(String rule_id) {
			this.rule_id = rule_id;
		}
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
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
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
		
		public String getClass_segment() {
			return class_segment;
		}
		public void setClass_segment(String class_segment) {
			this.class_segment = class_segment;
		}
		
		
		
		
		
}
