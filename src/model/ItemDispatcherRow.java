package model;

public class ItemDispatcherRow {
	
	String item_id="";
	String client_item_number="";
	String short_description="";
	String long_description="";
	String short_description_translated="";
	String long_description_translated="";
	String material_group="";
	String preclassifiation="";
	Integer row_number;
	
	
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
	public Integer getRow_number() {
		return row_number;
	}
	public void setRow_number(Integer row_number) {
		this.row_number = row_number;
	}
	
	
	
	
}
