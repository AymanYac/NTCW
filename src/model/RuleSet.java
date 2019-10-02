package model;

import javafx.scene.control.CheckBox;

public class RuleSet {
	String taxoid;
	String ruleJson;
	String language_id;
	
	String project_id;
	String project_name;
	String language;
	String Classifcation_system;
	String no_items;
	CheckBox referentData;
	Integer granularity;
	
	public RuleSet copy;
	
	public RuleSet(){
		
	}
	
	public RuleSet(RuleSet source) {
		this.taxoid = source.taxoid;
		this.ruleJson = source.ruleJson;
		this.language_id = source.language_id;
		this.project_id = source.project_id;
		this.project_name = source.project_name;
		this.language = source.language;
		this.Classifcation_system = source.Classifcation_system;
		this.no_items = source.no_items;
		this.referentData = new CheckBox();
		this.granularity = source.granularity;
	}
	

	public Integer getGranularity() {
		return granularity;
	}
	public void setGranularity(Integer granularity) {
		this.granularity = granularity;
	}
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public CheckBox getReferentData() {
		return referentData;
	}
	public void setReferentData(CheckBox referentData) {
		this.referentData = referentData;
	}
	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getClassifcation_system() {
		return Classifcation_system;
	}
	public void setClassifcation_system(String classifcation_system) {
		this.Classifcation_system = classifcation_system;
	}
	public String getNo_items() {
		return no_items;
	}
	public void setNo_items(String no_items) {
		this.no_items = no_items;
	}
	public String getTaxoid() {
		return taxoid;
	}
	public void setTaxoid(String taxoid) {
		this.taxoid = taxoid;
	}
	public String getRuleJson() {
		return ruleJson;
	}
	public void setRuleJson(String ruleJson) {
		this.ruleJson = ruleJson;
	}
	public String getLanguage_id() {
		return language_id;
	}
	public void setLanguage_id(String language_id) {
		this.language_id = language_id;
	}
	
	
}
