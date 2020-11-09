package model;


import org.apache.commons.lang3.text.WordUtils;

public enum DescriptionType {
	RAW_PREFERED("short_description, long_description"),
	RAW_LONG("long_description"),
	RAW_SHORT("short_description"),
	SIMPLE_PREFERED("simple_datalanguage_sd,simple_datalanguage_ld"),
	SIMPLE_LONG("simple_datalanguage_ld"),
	SIMPLE_SHORT("simple_datalanguage_sd"),
	CLEAN_PREFERED("clean_datalanguage_sd, clean_datalanguage_ld"),
	CLEAN_LONG("clean_datalanguage_ld"),
	CLEAN_SHORT("clean_datalanguage_sd");
	
	
	private String name=null;
	
	DescriptionType(String name){
		this.name=name;
	}
	
	public String toString() {
		return name;
	}
	
	public String toPrintString() {
		return WordUtils.capitalize(name.replace("_datalanguage_", " ").replace(",", " + ").replace("_", " ").replace("sd", "short description").replace("ld", "long description"));
		
	}
}
