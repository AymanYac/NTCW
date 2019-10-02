package model;

import javafx.scene.control.RadioButton;

public class Project {
	private String pid;
	private String name;
	private String dataLanguage;
	private String quality;
	private double desiredQuality;
	private double manualQuality;
	private String leDate;
	
	private String noItems;
	private int coveredItems;
	private RadioButton circle;
	
	private String classificationSystemId;
	private String classifierLanguage;
	private boolean privacyStatus;
	private Integer numberOfLevels;
	private boolean suppressionStatus;
	private String Coverage;
	
	
	
	
	public double getManualQuality() {
		return manualQuality;
	}

	public void setManualQuality(double manualQuality) {
		this.manualQuality = manualQuality;
	}

	

	public String getClassificationSystemId() {
		return classificationSystemId;
	}

	public void setClassificationSystemId(String classificationSystemId) {
		this.classificationSystemId = classificationSystemId;
	}

	public String getClassifierLanguage() {
		return classifierLanguage;
	}

	public void setClassifierLanguage(String classifierLanguage) {
		this.classifierLanguage = classifierLanguage;
	}

	public boolean getPrivacyStatus() {
		return privacyStatus;
	}

	public void setPrivacyStatus(boolean privacyStatus) {
		this.privacyStatus = privacyStatus;
	}

	public Integer getNumberOfLevels() {
		return numberOfLevels;
	}

	public void setNumberOfLevels(Integer numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}

	public boolean isSuppressionStatus() {
		return suppressionStatus;
	}

	public void setSuppressionStatus(boolean suppressionStatus) {
		this.suppressionStatus = suppressionStatus;
	}

	public double getDesiredQuality() {
		return desiredQuality;
	}

	public void setDesiredQuality(double desiredQuality) {
		this.desiredQuality = desiredQuality;
	}

	public Project() {
		this.pid = "";
		this.name="";
		this.dataLanguage="";
		this.noItems="";
		this.coveredItems=0;
		this.quality="0 %";
		this.leDate="";
	}

	public RadioButton getCircle() {
		return circle;
	}

	public void setCircle(RadioButton circle) {
		this.circle = circle;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataLanguage() {
		return dataLanguage;
	}

	public void setDataLanguage(String dataLanguage) {
		this.dataLanguage = dataLanguage;
	}

	public String getNoItems() {
		return noItems;
	}

	public void setNoItems(String noItems) {
		this.noItems = noItems;
	}

	public int getCoveredItems() {
		return coveredItems;
	}

	public void setCoveredItems(int i) {
		this.coveredItems = i;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getLeDate() {
		return leDate;
	}

	public void setLeDate(String leDate) {
		this.leDate = leDate;
	}

	public String getCoverage() {
		return Coverage;
	}

	public void setCoverage(String coverage) {
		Coverage = coverage;
	}

	
}
