package model;

import java.util.ArrayList;

public class BinaryClassificationParameters {
	Double Classif_buildSampleSize;
	Integer Classif_minimumTermLength;
	Boolean Classif_keepAlpha;
	DescriptionType Classif_baseDescriptionType;
	DescriptionType Classif_targetDescriptionType;
	Boolean Classif_cleanChar;
	Boolean Classif_cleanAbv;
	Boolean Classif_cleanSpell;
	Double Classif_Ta;
	Integer Classif_Tb;
	ArrayList<String> Classif_rank;
	Double Classif_epsilon;
	Double Classif_typeFactor;
	Integer Classif_granularity;
	
	Double Preclassif_buildSampleSize;
	Integer Preclassif_minimumTermLength;
	Boolean Preclassif_keepAlpha;
	DescriptionType Preclassif_baseDescriptionType;
	DescriptionType Preclassif_targetDescriptionType;
	Boolean Preclassif_cleanChar;
	Boolean Preclassif_cleanAbv;
	Boolean Preclassif_cleanSpell;
	Double Preclassif_Ta;
	Integer Preclassif_Tb;
	ArrayList<String> Preclassif_rank;
	Double Preclassif_epsilon;
	Double Preclassif_typeFactor;
	Integer Preclassif_granularity;
	
	public BinaryClassificationParameters(){
		this.Classif_buildSampleSize = (double) 100;
		this.Classif_minimumTermLength = 3;
		this.Classif_keepAlpha = true;
		this.Classif_baseDescriptionType = DescriptionType.SIMPLE_PREFERED;
		this.Classif_targetDescriptionType = DescriptionType.SIMPLE_PREFERED;
		Classif_cleanChar = false;
		Classif_cleanAbv = false;
		Classif_cleanSpell = false;
		Classif_Ta = (double) 90;
		Classif_Tb = 3;
		Classif_rank = new ArrayList<String>();
		Classif_rank.add("Accuracy");
		Classif_rank.add("Type");
		Classif_rank.add("Total");
		Classif_epsilon = Math.pow(10, -5);
		Classif_typeFactor = (double) 1;	
		
		
		this.Preclassif_buildSampleSize = (double) 100;
		this.Preclassif_minimumTermLength = 3;
		this.Preclassif_keepAlpha = true;
		this.Preclassif_baseDescriptionType = DescriptionType.SIMPLE_PREFERED;
		this.Preclassif_targetDescriptionType = DescriptionType.SIMPLE_PREFERED;
		Preclassif_cleanChar = false;
		Preclassif_cleanAbv = false;
		Preclassif_cleanSpell = false;
		Preclassif_Ta = (double) 90;
		Preclassif_Tb = 3;
		Preclassif_rank = new ArrayList<String>();
		Preclassif_rank.add("Accuracy");
		Preclassif_rank.add("Type");
		Preclassif_rank.add("Total");
		Preclassif_epsilon = Math.pow(10, -5);
		Preclassif_typeFactor = (double) 1;	
		
	}

	public Integer getClassif_granularity() {
		return Classif_granularity;
	}

	public void setClassif_granularity(Integer classif_granularity) {
		if(classif_granularity!=null) {
			Classif_granularity = classif_granularity;
		}else {
			Classif_granularity = 1;
		}
		
	}

	public Double getPreclassif_buildSampleSize() {
		return Preclassif_buildSampleSize;
	}

	public void setPreclassif_buildSampleSize(Double preclassif_buildSampleSize) {
		Preclassif_buildSampleSize = preclassif_buildSampleSize;
	}

	public Integer getPreclassif_minimumTermLength() {
		return Preclassif_minimumTermLength;
	}

	public void setPreclassif_minimumTermLength(Integer preclassif_minimumTermLength) {
		Preclassif_minimumTermLength = preclassif_minimumTermLength;
	}

	public Boolean getPreclassif_keepAlpha() {
		return Preclassif_keepAlpha;
	}

	public void setPreclassif_keepAlpha(Boolean preclassif_keepAlpha) {
		Preclassif_keepAlpha = preclassif_keepAlpha;
	}

	public DescriptionType getPreclassif_baseDescriptionType() {
		return Preclassif_baseDescriptionType;
	}

	public void setPreclassif_baseDescriptionType(DescriptionType preclassif_baseDescriptionType) {
		Preclassif_baseDescriptionType = preclassif_baseDescriptionType;
	}

	public DescriptionType getPreclassif_targetDescriptionType() {
		return Preclassif_targetDescriptionType;
	}

	public void setPreclassif_targetDescriptionType(DescriptionType preclassif_targetDescriptionType) {
		Preclassif_targetDescriptionType = preclassif_targetDescriptionType;
	}

	public Boolean getPreclassif_cleanChar() {
		return Preclassif_cleanChar;
	}

	public void setPreclassif_cleanChar(Boolean preclassif_cleanChar) {
		Preclassif_cleanChar = preclassif_cleanChar;
	}

	public Boolean getPreclassif_cleanAbv() {
		return Preclassif_cleanAbv;
	}

	public void setPreclassif_cleanAbv(Boolean preclassif_cleanAbv) {
		Preclassif_cleanAbv = preclassif_cleanAbv;
	}

	public Boolean getPreclassif_cleanSpell() {
		return Preclassif_cleanSpell;
	}

	public void setPreclassif_cleanSpell(Boolean preclassif_cleanSpell) {
		Preclassif_cleanSpell = preclassif_cleanSpell;
	}

	public Double getPreclassif_Ta() {
		return Preclassif_Ta;
	}

	public void setPreclassif_Ta(Double preclassif_Ta) {
		Preclassif_Ta = preclassif_Ta;
	}

	public Integer getPreclassif_Tb() {
		return Preclassif_Tb;
	}

	public void setPreclassif_Tb(Integer preclassif_Tb) {
		Preclassif_Tb = preclassif_Tb;
	}

	public ArrayList<String> getPreclassif_rank() {
		return Preclassif_rank;
	}

	public void setPreclassif_rank(ArrayList<String> preclassif_rank) {
		Preclassif_rank = preclassif_rank;
	}

	public Double getPreclassif_epsilon() {
		return Preclassif_epsilon;
	}

	public void setPreclassif_epsilon(Double preclassif_epsilon) {
		Preclassif_epsilon = preclassif_epsilon;
	}

	public Double getPreclassif_typeFactor() {
		return Preclassif_typeFactor;
	}

	public void setPreclassif_typeFactor(Double preclassif_typeFactor) {
		Preclassif_typeFactor = preclassif_typeFactor;
	}

	public Integer getPreclassif_granularity() {
		return Preclassif_granularity;
	}

	public void setPreclassif_granularity(Integer preclassif_granularity) {
		if(preclassif_granularity!=null) {
			Preclassif_granularity = preclassif_granularity;
		}else {
			Preclassif_granularity = 1;
		}
		
	}

	public Double getClassif_buildSampleSize() {
		return Classif_buildSampleSize;
	}

	public void setClassif_buildSampleSize(Double classif_buildSampleSize) {
		Classif_buildSampleSize = classif_buildSampleSize;
	}

	public Integer getClassif_minimumTermLength() {
		return Classif_minimumTermLength;
	}

	public void setClassif_minimumTermLength(Integer classif_minimumTermLength) {
		Classif_minimumTermLength = classif_minimumTermLength;
	}

	public Boolean getClassif_keepAlpha() {
		return Classif_keepAlpha;
	}

	public void setClassif_keepAlpha(Boolean classif_keepAlpha) {
		Classif_keepAlpha = classif_keepAlpha;
	}

	public DescriptionType getClassif_baseDescriptionType() {
		return Classif_baseDescriptionType;
	}

	public void setClassif_baseDescriptionType(DescriptionType classif_baseDescriptionType) {
		Classif_baseDescriptionType = classif_baseDescriptionType;
	}

	public DescriptionType getClassif_targetDescriptionType() {
		return Classif_targetDescriptionType;
	}

	public void setClassif_targetDescriptionType(DescriptionType classif_targetDescriptionType) {
		Classif_targetDescriptionType = classif_targetDescriptionType;
	}

	public Boolean getClassif_cleanChar() {
		return Classif_cleanChar;
	}

	public void setClassif_cleanChar(Boolean classif_cleanChar) {
		Classif_cleanChar = classif_cleanChar;
	}

	public Boolean getClassif_cleanAbv() {
		return Classif_cleanAbv;
	}

	public void setClassif_cleanAbv(Boolean classif_cleanAbv) {
		Classif_cleanAbv = classif_cleanAbv;
	}

	public Boolean getClassif_cleanSpell() {
		return Classif_cleanSpell;
	}

	public void setClassif_cleanSpell(Boolean classif_cleanSpell) {
		Classif_cleanSpell = classif_cleanSpell;
	}

	public Double getClassif_Ta() {
		return Classif_Ta;
	}

	public void setClassif_Ta(Double classif_Ta) {
		Classif_Ta = classif_Ta;
	}

	public Integer getClassif_Tb() {
		return Classif_Tb;
	}

	public void setClassif_Tb(Integer classif_Tb) {
		Classif_Tb = classif_Tb;
	}

	public ArrayList<String> getClassif_rank() {
		return Classif_rank;
	}

	public void setClassif_rank(ArrayList<String> classif_rank) {
		Classif_rank = classif_rank;
	}

	public Double getClassif_epsilon() {
		return Classif_epsilon;
	}

	public void setClassif_epsilon(Double classif_epsilon) {
		Classif_epsilon = classif_epsilon;
	}

	public Double getClassif_typeFactor() {
		return Classif_typeFactor;
	}

	public void setClassif_typeFactor(Double classif_typeFactor) {
		Classif_typeFactor = classif_typeFactor;
	}
	
	
	
}
