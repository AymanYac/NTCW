package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import controllers.Char_description;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

public class CharacteristicValue {
	public boolean ManualValueReviewed=false;
	
	public static HashMap<String,HashSet<CharacteristicValue>> loadedValues;
	
	private String value_id;
	private String dataLanguageValue;
	private String userLanguageValue;
	private String nominal_value;
	private String min_value;
	private String max_value;
	private String note;
	private String uom_id;
	
	private String source;
	private String author;
	private String rule_id;
	private String url;
	
	
	private ClassCharacteristic parentChar;

	
	
	
	
	public CharacteristicValue() {
		super();
		this.value_id = Tools.generate_uuid();
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
		try {
			if(Double.valueOf(min_value)>Double.valueOf(max_value)) {
				this.min_value=this.max_value;
				this.max_value=min_value;
				return;
			}
		}catch(Exception V) {
			//min_value or max_value is null
		}
		this.min_value = min_value;
	}
	public String getMax_value() {
		return max_value;
	}
	public void setMax_value(String max_value) {
		try {
			if(Double.valueOf(this.min_value)>Double.valueOf(max_value)) {
				this.max_value=this.min_value;
				this.min_value=max_value;
				return;
			}
		}catch(Exception V) {
			//min_value or max_value is null
		}
		this.max_value = max_value;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	//Std value is nominal value if numeric, datalanguageValue else
	public String getStdValue() {
		String text_val = getDataLanguageValue();
		if(text_val!=null) {
			return text_val;
		}
		return getNominal_value();
	}
	
	//Display value is user formatted display value (value column in table and item export)
	public String getDisplayValue(Char_description parent) {
		String ret = WordUtils.textFlowToString(getFormatedDisplayAndUomPair(parent,parentChar).getValue());
		if(ManualValueReviewed && ret.length()==0) {
			return "*UNKNOWN*";
		}
		return ret;
	}
	
	public Pair<ArrayList<String>, TextFlow> getFormatedDisplayAndUomPair(Char_description parent,ClassCharacteristic parentChar) {
		ArrayList<Text> textes = new ArrayList<Text>();
		String local_uom_symbol="";
		UnitOfMeasure local_uom = null;
		String local_Nominal_value=null;
		String local_Min_value=null;
		String local_Max_value=null;
		
		if(!parentChar.getIsNumeric()) {
			Text tmp = new Text(getDataLanguageValue());
			tmp.setFill(GlobalConstants.CHAR_TXT_COLOR);
			tmp.setFont(Font.font(GlobalConstants.CHAR_TXT_FONT,GlobalConstants.CHAR_TXT_WEIGHT,GlobalConstants.CHAR_TXT_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
			textes.add(tmp);
			if(parentChar.getIsTranslatable() && !parent.user_language.equals(parent.data_language) && getUserLanguageValue()!=null) {
				//tmp = new Text(" ("+translateValue(this.getValue()).getNominal_value()+")");
				tmp = new Text(" ("+getUserLanguageValue()+")");
				tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
				tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
				textes.add(tmp);
				
			}
			
		}else {
			
			try{
				
				UnitOfMeasure current_uom = UnitOfMeasure.RunTimeUOMS.get(getUom_id());
				if((!(current_uom!=null)) || parentChar.getAllowedUoms().contains(current_uom.getUom_id()) || parent.conversionToggle.isSelected()) {
					//Either there's no uom or the uom is included in the allowed uoms
					//Or the user doesn't want conversion
					//No conversion and show the input value
					try{
						local_uom_symbol = current_uom.getUom_symbol();
						local_uom = current_uom;
					}catch(Exception V) {
						//V.printStackTrace(System.err);
					}
					local_Nominal_value = getNominal_value();
					local_Max_value = getMax_value();
					local_Min_value = getMin_value();
				}else {
					//Converting to base uom
					UnitOfMeasure base_uom = UnitOfMeasure.RunTimeUOMS.get(current_uom.getUom_base_id());
					try{
						local_uom_symbol = base_uom.getUom_symbol();
						local_uom = base_uom;
					}catch(Exception V) {
						V.printStackTrace(System.err);
					}
					try{
						local_Nominal_value = String.valueOf( new BigDecimal( getNominal_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					try{
						local_Max_value = String.valueOf( new BigDecimal( getMax_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					try{
						local_Min_value = String.valueOf( new BigDecimal( getMin_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					for(String uom:parentChar.getAllowedUoms()) {
						UnitOfMeasure loopUom = UnitOfMeasure.RunTimeUOMS.get(uom);
						if(loopUom.getUom_base_id().equals(base_uom.getUom_id())) {
							
							try{
								local_uom_symbol = loopUom.getUom_symbol();
								local_uom = loopUom;
							}catch(Exception V) {
								V.printStackTrace(System.err);
							}
							try{
								local_Nominal_value = String.valueOf( new BigDecimal( local_Nominal_value.replace(",", ".").replace(" ", "") ).divide(loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							try{
								local_Max_value = String.valueOf( new BigDecimal( local_Max_value.replace(",", ".").replace(" ", "") ).divide(loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							try{
								local_Min_value = String.valueOf( new BigDecimal( local_Min_value.replace(",", ".").replace(" ", "") ).divide( loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							break;
							
						}
					}
					
					
				}
			}catch(Exception V) {
				V.printStackTrace(System.err);
				local_uom_symbol="";
				local_uom=null;
			}
			
			if(local_Nominal_value!=null && local_Nominal_value.replace(" ","").length() > 0) {
				//Has nominal value
				@SuppressWarnings("static-access")
				Text tmp = new Text(local_Nominal_value+" "+local_uom_symbol);
				tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
				tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
				textes.add(tmp);
				if(local_Min_value!=null && local_Min_value.replace(" ","").length() > 0) {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text(" ("+local_Min_value+" to "+local_Max_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}else {
						tmp = new Text(" (Min:"+local_Min_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}
				}else {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text(" (Max:"+local_Max_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						}
				}
			}else {
				//No nominal
				Text tmp;
				if(local_Min_value!=null && local_Min_value.replace(" ","").length() > 0) {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text("("+local_Min_value+" to "+local_Max_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}else {
						tmp = new Text("(Min:"+local_Min_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}
				}else {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text("(Max:"+local_Max_value+" "+local_uom_symbol+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						}
			}
		}
		}
		TextFlow ret = new TextFlow(textes.toArray(new Text[textes.size()]));
		ret.setMinHeight(0);
		ret.setPrefHeight(0);
		
		ArrayList<String> ls = new ArrayList<String>();
		ls.add((local_uom!=null?local_uom.getUom_id():null));
		ls.add(local_Nominal_value);
		ls.add(local_Min_value);
		ls.add(local_Max_value);
		
		Pair<ArrayList<String>,TextFlow> tmp = new Pair<ArrayList<String>,TextFlow>(ls,ret);
		return tmp;
	}
	
	public boolean isNonEmpty() {
		int concatDataLength = 0;
		try {
			concatDataLength += getStdValue().length();
		}catch(Exception V) {
			
		}
		try {
			concatDataLength += getMin_value().length();
		}catch(Exception V) {
			
		}
		try {
			concatDataLength += getMax_value().length();
		}catch(Exception V) {
			
		}
				
		return concatDataLength>0;
	}
	public String getUserLanguageValue() {
		return userLanguageValue;
		/*
		if(text_values!=null) {
			try {
				for(String lang_val : text_values.split("&&&")) {
					if(lang_val.endsWith(CharacteristicValue.userLanguage)) {
						char[] ret =new char[lang_val.length()-CharacteristicValue.userLanguage.length()];
						lang_val.getChars(0, lang_val.length()-CharacteristicValue.userLanguage.length(),ret,0);
						return new String(ret);
					}
				}
			}catch(Exception V) {
				V.printStackTrace();
				return null;
			}
		}
		return null;*/
	}
	
	public String getDataLanguageValue() {
		return dataLanguageValue;
		/*if(text_values!=null) {
			try {
				for(String lang_val : text_values.split("&&&")) {
					if(lang_val.endsWith(CharacteristicValue.dataLanguage)) {
						char[] ret =new char[lang_val.length()-CharacteristicValue.dataLanguage.length()];
						lang_val.getChars(0, lang_val.length()-CharacteristicValue.dataLanguage.length(),ret,0);
						return new String(ret);
					}
				}
			}catch(Exception V) {
				V.printStackTrace();
				return null;
			}
		}
		//If no language was specified during upload/previous filling and the carac is not translatable
		//Send whatever text the value contains
		if(!this.parentChar.getIsTranslatable()) {
			return this.text_values;
		}
		return null;*/
	}
	
	
	public void setDataLanguageValue(String dataLanguageValue) {
		this.dataLanguageValue=dataLanguageValue;
	}
	
	public void setUserLanguageValue(String userLanguageValue) {
		this.userLanguageValue=userLanguageValue;
	}
	
	public String getUom_id() {
		return uom_id;
	}
	public void setUom_id(String uom_id) {
		this.uom_id = uom_id;
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
	public String getRule_id() {
		return rule_id;
	}
	public void setRule_id(String rule_id) {
		this.rule_id = rule_id;
	}
	
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setParentChar(ClassCharacteristic classCharacteristic) {
		this.parentChar=classCharacteristic;
		addThisValuetoKnownValuesForCharacteristic(parentChar);
	}

	public void addThisValuetoKnownValuesForCharacteristic(ClassCharacteristic classCharacteristic) {
		if(classCharacteristic.getIsNumeric()) {
			return;
		}
		try {
			if(loadedValues.containsKey(classCharacteristic.getCharacteristic_id())) {
				HashSet<CharacteristicValue> tmp = loadedValues.get(classCharacteristic.getCharacteristic_id());
				tmp.add(this);
				loadedValues.put(classCharacteristic.getCharacteristic_id(), tmp);
			}else {
				HashSet<CharacteristicValue> tmp = new HashSet<CharacteristicValue>();
				tmp.add(this);
				loadedValues.put(classCharacteristic.getCharacteristic_id(), tmp);
			}
			
		}catch(Exception V) {
			loadedValues = new HashMap<String,HashSet<CharacteristicValue>>();
			if(loadedValues.containsKey(classCharacteristic.getCharacteristic_id())) {
				HashSet<CharacteristicValue> tmp = loadedValues.get(classCharacteristic.getCharacteristic_id());
				tmp.add(this);
				loadedValues.put(classCharacteristic.getCharacteristic_id(), tmp);
			}else {
				HashSet<CharacteristicValue> tmp = new HashSet<CharacteristicValue>();
				tmp.add(this);
				loadedValues.put(classCharacteristic.getCharacteristic_id(), tmp);
			}
		}
	}
	
	
	
	
	public String getNominal_value_truncated() {
		try {
			double val = Double.valueOf(getNominal_value());
			if(val == (long) val) {
				return getNominal_value();
			}
			return String.format("%.2f", Double.valueOf(getNominal_value()));
		}catch(Exception V) {
			V.printStackTrace();
			return "";
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
	     if (this == o) {
	         return true;
	     }
	     if (o instanceof CharacteristicValue) {
	    	 CharacteristicValue p = (CharacteristicValue) o;
	         return p.hashCode()==this.hashCode();
	     }
	     return false;
	}
	@Override
	public int hashCode() {
		Unidecode unidecode = Unidecode.toAscii();
		try{
			String concatData = "";
			try {
				concatData += getStdValue();
			}catch(Exception V) {
				
			}
			try {
				concatData += getMin_value();
			}catch(Exception V) {
				
			}
			try {
				concatData += getMax_value();
			}catch(Exception V) {
				
			}
			
			return unidecode.decodeAndTrim(concatData.toUpperCase()).hashCode() ;
		}catch(Exception V) {
			return 0;
		}
	}
	
	//Alias for set datalanguageValue used to distinguish setting value for non translatable chars
	public void setTXTValue(String text) {
		setDataLanguageValue(text);
	}
	public String getAuthorName() {
		try{
			return Tools.userID2Author.get(getAuthor());
		}catch(Exception V) {
			return getAuthor();
		}
	}
	
}
