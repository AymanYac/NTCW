package model;

import java.util.HashMap;
import java.util.HashSet;
import transversal.language_toolbox.Unidecode;

public class CharacteristicValue {
	
	public static String userLanguage;
	public static String dataLanguage;
	public static HashMap<String,HashSet<CharacteristicValue>> loadedValues;
	
	private String value_id;
	private String nominal_value;
	private String min_value;
	private String max_value;
	private String text_values;
	private String note;
	private String uom_id;
	private ClassCharacteristic parentChar;
	
	
	@Override
	public int hashCode() {
		Unidecode unidecode = Unidecode.toAscii();
		try{
			return unidecode.decodeAndTrim(getDisplayValue().toUpperCase()).hashCode() ;
		}catch(Exception V) {
			return 0;
		}
	}
	
	public String getText_values() {
		return text_values;
	}
	public void setText_values(String text_values) {
		this.text_values = text_values;
	}
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
	public String getDisplayValue() {
		String text_val = getDataLanguageValue();
		if(text_val!=null) {
			return text_val;
		}
		return nominal_value;
	}
	public boolean isNonEmpty() {
		int concatDataLength = 0;
		try {
			concatDataLength += getDisplayValue().length();
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
		return null;
	}
	
	public String getDataLanguageValue() {
		if(text_values!=null) {
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
		return null;
	}
	public void setParentChar(ClassCharacteristic classCharacteristic) {
		this.parentChar=classCharacteristic;
		addThisValuetoKnownValues(parentChar);
	}

	public void addThisValuetoKnownValues(ClassCharacteristic classCharacteristic) {
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
	
}
