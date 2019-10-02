package model;

public class CharacteristicValue {
	String value_id;
	String nominal_value;
	String min_value;
	String max_value;
	String note;
	String uom_id;
	
	
	
	public String getUom_id() {
		return uom_id;
	}
	public void setUom_id(String uom_id) {
		this.uom_id = uom_id;
	}
	public String getValue_id() {
		return value_id;
	}
	public void setValue_id(String value_id) {
		this.value_id = value_id;
	}
	public String getNominal_value() {
		return nominal_value;
	}
	public void setNominal_value(String nominal_value) {
		this.nominal_value = nominal_value;
	}
	public String getMin_value() {
		return min_value;
	}
	public void setMin_value(String min_value) {
		this.min_value = min_value;
	}
	public String getMax_value() {
		return max_value;
	}
	public void setMax_value(String max_value) {
		this.max_value = max_value;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getDisplayValue() {
		return nominal_value;
	}
	public boolean isNonEmpty() {
		
		return (nominal_value.length()+min_value.length()+max_value.length())>0;
	}
	
}
