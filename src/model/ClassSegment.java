package model;

import java.util.ArrayList;

public class ClassSegment {
	int segmentGranularity;
	String segmentId;
	ArrayList<String> levelNumbers = new ArrayList<String>(4);
	ArrayList<String> levelNames = new ArrayList<String>(4);
	ArrayList<String> levelNamesTranslated = new ArrayList<String>(4);
	
	
	public int getSegmentGranularity() {
		return segmentGranularity;
	}
	public void setSegmentGranularity(int segmentGranularity) {
		this.segmentGranularity = segmentGranularity;
	}
	public String getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
	}
	
	public void setLevelNumber(int level,String number) {
		levelNumbers.set(level, number);
	}
	public void setLevelName(int level,String name) {
		levelNames.set(level, name);
	}
	public void setLevelNameTranslated(int level,String name) {
		levelNamesTranslated.set(level, name);
	}
	
	public String getLevelNumber(int level) {
		return levelNumbers.get(level);
	}
	public String getLevelName(int level) {
		return levelNames.get(level);
	}
	public String getLevelNameTranslated(int level) {
		return levelNamesTranslated.get(level);
	}
	
	public String getClassNumber() {
		return levelNumbers.get(getSegmentGranularity()-1);
	}
	public String getClassName() {
		return levelNames.get(getSegmentGranularity()-1);
	}
	public String getClassNameTranslated() {
		return levelNamesTranslated.get(getSegmentGranularity()-1);
	}
	
	public void setClassNumber(String number) {
		levelNumbers.set(getSegmentGranularity()-1, number);
	}
	public void setClassName(String name) {
		levelNames.set(getSegmentGranularity()-1, name);
	}
	public void setClassNameTranslated(String name) {
		levelNamesTranslated.set(getSegmentGranularity()-1, name);
	}
	
	public boolean hasSameClassNumbersAsSegment(ClassSegment tmpSegment) {
		return tmpSegment.levelNumbers.equals(this.levelNumbers);
	}
	
}
