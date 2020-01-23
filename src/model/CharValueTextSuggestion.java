package model;

import java.util.HashSet;
import java.util.Optional;

import javafx.util.Pair;

public class CharValueTextSuggestion {
	
	HashSet<Pair<String,String>> valueLangPair = new HashSet<Pair<String,String>>();
	private CharValueTextSuggestion tmp;
	private String sourceLanguage;
	private String targetLanguage;

	public CharValueTextSuggestion(CharValueTextSuggestion s, String sourceLanguage, String targetLanguage) {
		tmp = new CharValueTextSuggestion();
		tmp.valueLangPair = valueLangPair;
		this.sourceLanguage = sourceLanguage;
		this.targetLanguage = targetLanguage;
		
	}

	public CharValueTextSuggestion() {
		// TODO Auto-generated constructor stub
	}

	public Boolean valueTextContains(String text) {
		
		return null;
	}

	public String getSource_value() {
		Optional<Pair<String, String>> potPair = valueLangPair.stream().filter(p->(p.getValue().equals(sourceLanguage))||(!(p.getValue()!=null)))
								.findAny();
		if(potPair.isPresent()) {
			return potPair.get().getKey();
		}
		return null;
	}
	
	public String getTarget_value() {
		Optional<Pair<String, String>> potPair = valueLangPair.stream().filter(p->(p.getValue().equals(targetLanguage)))
				.findAny();
		if(potPair.isPresent()) {
		return potPair.get().getKey();
		}
		return null;
	}
	
	public String getDisplay_value() {
		if(getSource_value()!=null) {
			if(getTarget_value()!=null) {
				return getSource_value()+" ("+getTarget_value()+") ";
			}else {
				return getSource_value();
			}
		}
		return null;
	}

	public void addValueInLanguage(String value, Optional<String> languageID) {
		Pair<String, String> pair = new Pair<String,String>(value,(languageID.isPresent()?languageID.toString():null));
		valueLangPair.add(pair);
	}

	public boolean hasSourceLanguageValue() {
		return valueLangPair.stream().anyMatch(p->
				!(p.getValue()!=null)||(p.getValue().equals(sourceLanguage))
				);
	}

}
