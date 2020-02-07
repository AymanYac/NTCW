package model;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.util.Pair;
import transversal.language_toolbox.Unidecode;

public class CharValueTextSuggestion {
	
	ArrayList<Pair<String,String>> langValuePair;
	private String sourceLanguage;
	private String targetLanguage;
	private Unidecode unidecode;

	
	public CharValueTextSuggestion(String sourceLanguage, String targetLanguage) {
		this.langValuePair = new ArrayList<Pair<String,String>>(2);
		this.sourceLanguage=sourceLanguage;
		this.targetLanguage=targetLanguage;
	}
	
	public Boolean isDataFieldSuggestion() {
		return sourceLanguage.equals("DATA");
	}
	
	public CharValueTextSuggestion flipIsDataFieldSuggestion() {
		CharValueTextSuggestion tmp = new CharValueTextSuggestion(targetLanguage,sourceLanguage);
		tmp.langValuePair = (ArrayList<Pair<String, String>>) this.langValuePair.clone();
		return tmp;
	}

	public String getSource_value() {
		Optional<Pair<String, String>> potPair = langValuePair.stream().filter(p->(p.getKey().equals(sourceLanguage)))
								.findAny();
		if(potPair.isPresent()) {
			return potPair.get().getValue();
		}
		return null;
	}
	
	public void setSource_value(String newSourceValue) {
		langValuePair = new ArrayList<Pair<String,String>>(langValuePair.stream().filter(p->!(p.getKey().equals(sourceLanguage))).collect(Collectors.toList()));
		addValueInLanguage(sourceLanguage,newSourceValue);
	}
	
	public String getTarget_value() {
		Optional<Pair<String, String>> potPair = langValuePair.stream().filter(p->(p.getKey().equals(targetLanguage)))
				.findAny();
		if(potPair.isPresent()) {
		return potPair.get().getValue();
		}
		return null;
	}
	
	public void setTarget_value(String newSourceValue) {
		langValuePair = new ArrayList<Pair<String,String>>(langValuePair.stream().filter(p->!(p.getKey().equals(targetLanguage))).collect(Collectors.toList()));
		addValueInLanguage(targetLanguage,newSourceValue);
	}
	
	public String getDisplay_value() {
		if(getSource_value()!=null && getSource_value().replace(" ", "").length()>0) {
			if(getTarget_value()!=null && getTarget_value().replace(" ", "").length()>0) {
				return getSource_value()+" ("+getTarget_value()+") ";
			}else {
				return getSource_value();
			}
		}
		return null;
	}

	public void addValueInLanguage(String language, String value) {
		Pair<String, String> pair = new Pair<String,String>(language,value);
		langValuePair.add(pair);
	}

	
	public boolean hasSourceValue(boolean isDataField) {
		
		if(isDataField) {
			if(sourceLanguage.equals("USER")) {
				return false;
			}
			return langValuePair.stream().anyMatch(p->p.getKey().equals("DATA") && p.getValue()!=null);
		}
		if(sourceLanguage.equals("DATA")) {
			return false;
		}
		return langValuePair.stream().anyMatch(p->p.getKey().equals("USER") && p.getValue()!=null); 
	}

	public boolean sourceTextContains(String subText) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		try{
			return unidecode.decodeAndTrim(getSource_value().toLowerCase()).contains(unidecode.decodeAndTrim(subText.toLowerCase()));
		}catch(Exception V) {
			return false;
		}
		/*
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		if(sourceLanguage.equals("DATA")) {
			return langValuePair.stream().anyMatch(p->p.getKey().equals("DATA") &&
					unidecode.decodeAndTrim(p.getValue().toLowerCase()).contains(unidecode.decodeAndTrim(subText.toLowerCase())));
		}
		return langValuePair.stream().anyMatch(p->p.getKey().equals("USER") &&
				unidecode.decodeAndTrim(p.getValue().toLowerCase()).contains(unidecode.decodeAndTrim(subText.toLowerCase())));
				*/
	}
	
	public boolean sourceTextEquals(String text) {
		unidecode = (unidecode!=null)?unidecode:Unidecode.toAscii();
		try{
			return unidecode.decodeAndTrim(getSource_value().toLowerCase()).equals(unidecode.decodeAndTrim(text.toLowerCase()));
		}catch(Exception V) {
			return false;
		}
	}

	@Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof CharValueTextSuggestion)) {
            return false;
        }

        CharValueTextSuggestion tmp = (CharValueTextSuggestion) o;
        Unidecode unidec = Unidecode.toAscii();
        return isDataFieldSuggestion().equals(isDataFieldSuggestion()) && unidec.decodeAndTrim(tmp.getSource_value()).toLowerCase().equals(unidec.decodeAndTrim(getSource_value()).toLowerCase())
        		&&
        		unidec.decodeAndTrim(tmp.getTarget_value()).toLowerCase().equals(unidec.decodeAndTrim(getTarget_value()).toLowerCase());
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
    	Unidecode unidec = Unidecode.toAscii();
    	return (unidec.decodeAndTrim(getSource_value()).toLowerCase()
    			+unidec.decodeAndTrim(getTarget_value()).toLowerCase()
    			+"|<>|"+String.valueOf(isDataFieldSuggestion())
    			).hashCode();
    }

	
	
}
