package model;

import transversal.generic.Tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProjectTemplate {
	
	
	private LinkedHashMap<String,Boolean> FOR_WORDS = new LinkedHashMap<String,Boolean>();
	private LinkedHashMap<String,Boolean> DW_WORDS = new LinkedHashMap<String,Boolean>();
	private LinkedHashMap<String,Boolean> STOP_WORDS = new LinkedHashMap<String,Boolean>();
	
	private LinkedHashMap<String,ArrayList<String>> LOGINS = new LinkedHashMap<String,ArrayList<String>>();
	
	private String pid=null;
	private String taxoID;
	private String taxoName;
	private String languageID;
	
	
	public String getLanguageID() {
		return languageID;
	}
	public void setLanguageID(String languageID) {
		this.languageID = languageID;
	}
	public String getPid() {
		if(this.pid!=null) {
			return pid;
		}
		this.pid=Tools.generate_uuid();
		return this.pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getTaxoID() {
		return taxoID;
	}
	public void setTaxoID(String taxoID) {
		this.taxoID = taxoID;
	}
	
	public LinkedHashMap<String,Boolean> getFOR_WORDS() {
		return FOR_WORDS;
	}
	public void setFOR_WORDS(LinkedHashMap<String,Boolean> fOR_WORDS) {
		FOR_WORDS = fOR_WORDS;
	}
	public LinkedHashMap<String,Boolean> getDW_WORDS() {
		return DW_WORDS;
	}
	public void setDW_WORDS(LinkedHashMap<String,Boolean> dW_WORDS) {
		DW_WORDS = dW_WORDS;
	}
	public LinkedHashMap<String,Boolean> getSTOP_WORDS() {
		return STOP_WORDS;
	}
	public void setSTOP_WORDS(LinkedHashMap<String,Boolean> sTOP_WORDS) {
		STOP_WORDS = sTOP_WORDS;
	}
	public LinkedHashMap<String, ArrayList<String>> getLOGINS() {
		return LOGINS;
	}
	public void setLOGINS(LinkedHashMap<String, ArrayList<String>> lOGINS) {
		LOGINS = lOGINS;
	}
	public String getTaxoName() {
		return taxoName;
	}
	public void setTaxoName(String taxoName) {
		this.taxoName = taxoName;
	}
	

}
