package model;

import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
		if(CaracteristicValue.loadedValues.get(this.getCharacteristic_id())!=null){
			HashSet<CaracteristicValue> ret = CaracteristicValue.loadedValues.get(this.getCharacteristic_id()).stream().filter(v->v!=null).collect(Collectors.toCollection(HashSet::new));
			if(ret!=null) {
				return ret;
			}
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
		System.out.println("Trying to reinterpret "+UnitOfMeasure.RunTimeUOMS.get(uomFalse).getUom_name()+" to match "+String.join(",",allowedUoms.stream().map(uid->UnitOfMeasure.RunTimeUOMS.get(uid).getUom_name()).collect(Collectors.toList())));
		HashSet<String> fs = new HashSet<String>(UnitOfMeasure.RunTimeUOMS.get(uomFalse).getUom_symbols());
		
		Optional<String> match = 
		UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->UnitOfMeasure.ConversionPathExists(u, allowedUoms)).map(UnitOfMeasure::getUom_id)
		//allowedUoms.stream()
		.filter(uid->new HashSet<String>(UnitOfMeasure.RunTimeUOMS.get(uid).getUom_symbols()).removeAll(fs))
		.findAny();
		
		if(match.isPresent()) {
			System.out.println("=>reinterpreted to "+UnitOfMeasure.RunTimeUOMS.get(match.get()).getUom_name());
			return new Pair<String,String>(uomFalse,match.get());
		}
		System.out.println("=>Unable to reinterpret");
		return null;

	}


	public String
	getTemplateSignature(String templateCriterion){
		String ret="";
		if(templateCriterion!=null && templateCriterion.equals("NAME")){
			ret+=this.getCharacteristic_name()+"&&&";
		}else{
			ret+=this.getCharacteristic_id()+"&&&";
		}
		return 	ret
				//+this.getSequence().toString()+"&&&"
				+this.getIsCritical().toString()+"&&&"
				+(getAllowedUoms()!=null?String.join("&&&",getAllowedUoms()):"");
	}
	
	public boolean matchesTemplates(ArrayList<ClassCaracteristic> templates, ClassSegment currentMapInstance, HashMap<String,HashSet<ClassSegment>> templateMaps , String templateCriterion) {
		boolean ret =  templates.stream()
				.filter(t->templateCriterion.equals("NAME") || t.getCharacteristic_id().equals(this.getCharacteristic_id()))
				.filter(t->t.getCharacteristic_name().equals(this.getCharacteristic_name()))
				//.filter(t->t.getSequence().equals(this.getSequence()))
				.filter(t->t.getIsCritical().equals(this.getIsCritical()))
				.anyMatch(t->  ( t.getAllowedUoms()!=null && this.getAllowedUoms()!=null && t.getAllowedUoms().equals(this.allowedUoms) )
							|| ( !(t.getAllowedUoms()!=null) && !(this.getAllowedUoms()!=null) )
				);
		if(!ret){
			templates.add(this);
		}
		try {
			templateMaps.get(this.getTemplateSignature(templateCriterion)).add(currentMapInstance);
		}catch (Exception V){
			templateMaps.put(this.getTemplateSignature(templateCriterion),new HashSet<ClassSegment>());
			templateMaps.get(this.getTemplateSignature(templateCriterion)).add(currentMapInstance);
		}
		return ret;
	}

    public ClassCaracteristic shallowCopy() {
		ClassCaracteristic tmp = new ClassCaracteristic();
		tmp.setCharacteristic_id(this.getCharacteristic_id());
		tmp.setCharacteristic_name(this.getCharacteristic_name());
		tmp.setCharacteristic_name_translated(this.getCharacteristic_name_translated());
		tmp.setIsNumeric(this.getIsNumeric());
		tmp.setIsTranslatable(this.getIsTranslatable());
		tmp.setSequence(this.getSequence());
		tmp.setIsCritical(this.getIsCritical());
		if(this.getAllowedValues()!=null){
			tmp.setAllowedValues((ArrayList<String>) this.getAllowedValues().clone());
		}
		if(this.getAllowedUoms()!=null){
			tmp.setAllowedUoms((ArrayList<String>) this.getAllowedUoms().clone());
		}
		tmp.setIsActive(this.getIsActive());
		return tmp;
    }
}
