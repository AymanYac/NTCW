package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemFetcherRow {
	
	
	String item_id="";
	String client_item_number="";
	String short_description="";
	String long_description="";
	String short_description_translated="";
	String long_description_translated="";
	String material_group="";
	String preclassifiation="";
	
	String Rule_Segment_id;
	String Rule_Segment_number;
	String Rule_Segment_name;
	
	String Manual_segment_id;
	String Manual_segment_number;
	String Manual_segment_name;
	
	String Upload_segment_id;
	String Upload_segment_number;
	String Upload_segment_name;
	
	//String Display_segment_id;
	//String Display_segment_number;
	//String Display_segment_name;
	
	String source_Manual;
	String rule_id_Manual;
	String rule_description_Manual;
	String author_Manual;
	String reviewer_Manual;
	
	String source_Rules;
	String rule_id_Rules;
	String rule_description_Rules;
	String author_Rules;
	String reviewer_Rules;
	
	String source_Upload;
	String rule_id_Upload;
	String rule_description_Upload;
	String author_Upload;
	String reviewer_Upload;
	
	//String source_Display;
	//String rule_id_Display;
	//String rule_description_Display;
	//String author_Display;
	//String reviewer_Display;
	
	
	String online_preclassif;
	
	
	String W1;
	String W1W2;
	List<String> F1;
	List<String> F1F2;
	Boolean DWG=false;
	
	public ArrayList<String> itemRules = new ArrayList<String>();
	public static HashMap<String,GenericRule> staticRules = new HashMap<String,GenericRule>();
	

	public String getManual_segment_id() {
		return Manual_segment_id;
	}
	public void setManual_segment_id(String manual_segment_id) {
		Manual_segment_id = manual_segment_id;
	}
	public String getManual_segment_number() {
		return Manual_segment_number;
	}
	public void setManual_segment_number(String manual_segment_number) {
		Manual_segment_number = manual_segment_number;
	}
	public String getManual_segment_name() {
		return Manual_segment_name;
	}
	public void setManual_segment_name(String manual_segment_name) {
		Manual_segment_name = manual_segment_name;
	}
	public String getUpload_segment_id() {
		return Upload_segment_id;
	}
	public void setUpload_segment_id(String upload_segment_id) {
		Upload_segment_id = upload_segment_id;
	}
	public String getUpload_segment_number() {
		return Upload_segment_number;
	}
	public void setUpload_segment_number(String upload_segment_number) {
		Upload_segment_number = upload_segment_number;
	}
	public String getUpload_segment_name() {
		return Upload_segment_name;
	}
	public void setUpload_segment_name(String upload_segment_name) {
		Upload_segment_name = upload_segment_name;
	}
	
	
	
	public String getW1() {
		return W1;
	}
	public void setW1(String w1) {
		W1 = w1;
	}
	public String getW1W2() {
		return W1W2;
	}
	public void setW1W2(String w1w2) {
		W1W2 = w1w2;
	}
	public List<String> getF1() {
		return F1;
	}
	public void setF1(List<String> f1) {
		F1 = f1;
	}
	public void addF1(String f1) {
		if(F1!=null) {
			F1.add(f1);
		}else {
			F1 = new ArrayList<String>();
			F1.add(f1);
		}
	}
	
	public List<String> getF1F2() {
		return F1F2;
	}
	public void setF1F2(List<String> f2) {
		F1F2 = f2;
	}
	public void addF1F2(String f2) {
		if(F1F2!=null) {
			F1F2.add(f2);
		}else {
			F1F2 = new ArrayList<String>();
			F1F2.add(f2);
		}
	}
	
	
	public Boolean getDWG() {
		return DWG;
	}
	public void setDWG(Boolean dWG) {
		DWG = dWG;
	}
	public String getOnline_preclassif() {
		return online_preclassif;
	}
	public void setOnline_preclassif(String online_preclassif) {
		this.online_preclassif = online_preclassif;
	}
	public String getDisplay_segment_name() {
		if(this.Manual_segment_name!=null) {
			return Manual_segment_name;
		}
		if(this.Rule_Segment_name!=null) {
			return Rule_Segment_name;
		}
		return Upload_segment_name;
		
	}
	
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	public String getClient_item_number() {
		return client_item_number;
	}
	public void setClient_item_number(String client_item_number) {
		this.client_item_number = client_item_number;
	}
	public String getShort_description() {
		return short_description;
	}
	public void setShort_description(String short_description) { 
		 if(!(short_description!=null)) {
		 this.short_description = ""; return;
		}
		this.short_description = short_description;
	}
	public String getLong_description() {
		return long_description;
	}
	public void setLong_description(String long_description) { 
		 if(!(long_description!=null)) {
		 this.long_description = ""; return;
		}
		this.long_description = long_description;
	}
	public String getShort_description_translated() {
		return short_description_translated;
	}
	public void setShort_description_translated(String short_description_translated) { 
		 if(!(short_description_translated!=null)) {
		 this.short_description_translated = ""; return;
		}
		this.short_description_translated = short_description_translated;
	}
	public String getLong_description_translated() {
		return long_description_translated;
	}
	public void setLong_description_translated(String long_description_translated) { 
		 if(!(long_description_translated!=null)) {
		 this.long_description_translated = ""; return;
		}
		this.long_description_translated = long_description_translated;
	}
	public String getMaterial_group() {
		return material_group;
	}
	public void setMaterial_group(String material_group) {
		this.material_group = material_group;
	}
	public String getPreclassifiation() {
		return preclassifiation;
	}
	public void setPreclassifiation(String preclassifiation) {
		this.preclassifiation = preclassifiation;
	}
	
	public String getRule_Segment_id() {
		return Rule_Segment_id;
	}
	public void setRule_Segment_id(String segment_id) {
		this.Rule_Segment_id = segment_id;
	}
	
	public String getRule_Segment_number() {
        return Rule_Segment_number;
    }
    public void setRule_Segment_number(String segment_number) {
        this.Rule_Segment_number = segment_number;
    }
    
	
	
	
	
	
	public String getRule_Segment_name() {
		return Rule_Segment_name;
	}
	public void setRule_Segment_name(String segment_name) {
		this.Rule_Segment_name = segment_name;
	}
	
	public String getDisplay_segment_id() {
		
		if(this.Manual_segment_id!=null) {
			return Manual_segment_id;
		}
		if(this.Rule_Segment_id!=null) {
			return Rule_Segment_id;
		}
		return Upload_segment_id;
		
	}
	
	
	public String getDisplay_segment_number() {
		
		if(this.Manual_segment_number!=null) {
			return Manual_segment_number;
		}
		if(this.Rule_Segment_number!=null) {
			return Rule_Segment_number;
		}
		return Upload_segment_number;
		
	}
	
	
	public String getSource_Manual() {
		return source_Manual;
	}
	public void setSource_Manual(String source_Manual) {
		this.source_Manual = source_Manual;
	}
	public String getRule_id_Manual() {
		return rule_id_Manual;
	}
	public void setRule_id_Manual(String rule_id_Manual) {
		this.rule_id_Manual = rule_id_Manual;
	}
	public String getRule_description_Manual() {
		return rule_description_Manual;
	}
	public void setRule_description_Manual(String rule_description_Manual) {
		this.rule_description_Manual = rule_description_Manual;
	}
	public String getAuthor_Manual() {
		return author_Manual;
	}
	public void setAuthor_Manual(String author_Manual) {
		this.author_Manual = author_Manual;
	}
	public String getReviewer_Manual() {
		return reviewer_Manual;
	}
	public void setReviewer_Manual(String reviewer_Manual) {
		this.reviewer_Manual = reviewer_Manual;
	}
	public String getSource_Rules() {
		return source_Rules;
	}
	public void setSource_Rules(String source_Rules) {
		this.source_Rules = source_Rules;
	}
	public String getRule_id_Rules() {
		return rule_id_Rules;
	}
	public void setRule_id_Rules(String rule_id_Rules) {
		this.rule_id_Rules = rule_id_Rules;
	}
	public String getRule_description_Rules() {
		return rule_description_Rules;
	}
	public void setRule_description_Rules(String rule_description_Rules) {
		this.rule_description_Rules = rule_description_Rules;
	}
	public String getAuthor_Rules() {
		return author_Rules;
	}
	public void setAuthor_Rules(String author_Rules) {
		this.author_Rules = author_Rules;
	}
	public String getReviewer_Rules() {
		return reviewer_Rules;
	}
	public void setReviewer_Rules(String reviewer_Rules) {
		this.reviewer_Rules = reviewer_Rules;
	}
	public String getSource_Upload() {
		return source_Upload;
	}
	public void setSource_Upload(String source_Upload) {
		this.source_Upload = source_Upload;
	}
	public String getRule_id_Upload() {
		return rule_id_Upload;
	}
	public void setRule_id_Upload(String rule_id_Upload) {
		this.rule_id_Upload = rule_id_Upload;
	}
	public String getRule_description_Upload() {
		return rule_description_Upload;
	}
	public void setRule_description_Upload(String rule_description_Upload) {
		this.rule_description_Upload = rule_description_Upload;
	}
	public String getAuthor_Upload() {
		return author_Upload;
	}
	public void setAuthor_Upload(String author_Upload) {
		this.author_Upload = author_Upload;
	}
	public String getReviewer_Upload() {
		return reviewer_Upload;
	}
	public void setReviewer_Upload(String reviewer_Upload) {
		this.reviewer_Upload = reviewer_Upload;
	}
	public String getSource_Display() {
		if(this.source_Manual!=null) {
			return source_Manual;
		}
		if(this.source_Rules!=null) {
			return source_Rules;
		}
		return source_Upload;
	}
	
	public String getRule_id_Display() {
		if(this.rule_id_Manual!=null) {
			return rule_id_Manual;
		}
		if(this.rule_id_Rules!=null) {
			return rule_id_Rules;
		}
		return rule_id_Upload;
	}
	
	public String getRule_description_Display() {
		if(this.rule_description_Manual!=null) {
			return rule_description_Manual;
		}
		if(this.rule_description_Rules!=null) {
			return rule_description_Rules;
		}
		return rule_description_Upload;
	}
	
	public String getAuthor_Display() {
		if(this.author_Manual!=null) {
			return author_Manual;
		}
		if(this.author_Rules!=null) {
			return author_Rules;
		}
		return author_Upload;
	}
	
	public String getReviewer_Display() {
		if(this.reviewer_Manual!=null) {
			return reviewer_Manual;
		}
		if(this.reviewer_Rules!=null) {
			return reviewer_Rules;
		}
		return reviewer_Upload;
	}

	
	

}
