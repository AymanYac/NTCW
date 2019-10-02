package model;

public class CharDescClassComboRow {
	private final String className;
	private final String classSegment;
	
	public CharDescClassComboRow(String className, String classSegment) {
		super();
		this.className = className;
		this.classSegment = classSegment;
	}
	
	public String getclassName() {
        return className ;
    }
	public String getClassSegment() {
		return classSegment;
	}

    @Override
    public String toString() {
        return getclassName();
    }
}
