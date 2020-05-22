package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.util.Pair;

public class ClassCaracteristic {
	//Char fields
	private String characteristic_id;
	private String characteristic_name;
	private String characteristic_name_translated;
	private Boolean isNumeric;
	private Boolean isTranslatable;
	
	//Segment_x_char fields
	private Integer sequence;
	private Boolean isCritical;
	private ArrayList<String> allowedValues;
	private ArrayList<String> allowedUoms;
	private Boolean isActive;
	
	public String getCharacteristic_id() {
		return characteristic_id;
	}
	public void setCharacteristic_id(String characteristic_id) {
		this.characteristic_id = characteristic_id;
	}
	public String getCharacteristic_name() {
		return characteristic_name;
	}
	public void setCharacteristic_name(String characteristic_name) {
		this.characteristic_name = characteristic_name;
	}
	public String getCharacteristic_name_translated() {
		return characteristic_name_translated;
	}
	public void setCharacteristic_name_translated(String chracteristic_name_translated) {
		this.characteristic_name_translated = chracteristic_name_translated;
	}
	public Boolean getIsNumeric() {
		return isNumeric;
	}
	public void setIsNumeric(Boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
	public Boolean getIsTranslatable() {
		return isTranslatable;
	}
	public void setIsTranslatable(Boolean isTranslatable) {
		this.isTranslatable = isTranslatable;
	}
	public Integer getSequence() {
		return sequence;
	}
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	public Boolean getIsCritical() {
		return isCritical;
	}
	public void setIsCritical(Boolean isCritical) {
		this.isCritical = isCritical;
	}
	public ArrayList<String> getAllowedValues() {
		return allowedValues;
	}
	public HashSet<CaracteristicValue> getKnownValues(){
		HashSet<CaracteristicValue> ret = CaracteristicValue.loadedValues.get(this.getCharacteristic_id());
		if(ret!=null) {
			return ret;
		}
		return new HashSet<CaracteristicValue>();
	}
	public void setAllowedValues(ArrayList<String> allowedValues) {
		this.allowedValues = allowedValues;
	}
	public ArrayList<String> getAllowedUoms() {
		return allowedUoms;
	}
	public void setAllowedUoms(ArrayList<String> allowedUoms) {
		this.allowedUoms = allowedUoms;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public void setAllowedValues(String[] strings) {
		this.allowedValues = new ArrayList<String>(Arrays.asList( strings ));
	}
	public void setAllowedUoms(String[] strings) {
		ArrayList<String> ret = (ArrayList<String>) new ArrayList<String>(Arrays.asList( strings )).stream().filter(s->s.length()>0).collect(Collectors.toList());
		if(ret.size()>0) {
			this.allowedUoms = ret;
		}
		
	}
	
	
	
	@Override
	public boolean equals(Object o)
	{
	     if (this == o) {
	         return true;
	     }
	     if (o instanceof ClassCaracteristic) {
	    	 ClassCaracteristic p = (ClassCaracteristic) o;
	         return p.hashCode()==this.hashCode();
	     }
	     return false;
	}
	@Override
	public int hashCode() {
		return getCharacteristic_id().hashCode();
	}
	
	
	public Pair<String,String> attemptUomSymbolInterpretationCorrection(String uomFalse){
		HashSet<String> fs = new HashSet<String>(UnitOfMeasure.RunTimeUOMS.get(uomFalse).getUom_symbols());
		
		Optional<String> match = 
		UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->UnitOfMeasure.ConversionPathExists(u, allowedUoms)).map(u->u.getUom_id())
		//allowedUoms.stream()
		.filter(uid->new HashSet<String>(UnitOfMeasure.RunTimeUOMS.get(uid).getUom_symbols()).removeAll(fs))
		.findAny();
		
		if(match.isPresent()) {
			return new Pair<String,String>(uomFalse,match.get());
		}
		return null;
	}
	
}
