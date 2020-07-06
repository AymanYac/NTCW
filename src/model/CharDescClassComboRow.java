package model;

public class CharDescClassComboRow {
	private final String className;
	private final String classSegment;
	private final String classCode;

	public CharDescClassComboRow(String classSegment,String className,String classCode) {
		this.className = className;
		this.classSegment = classSegment;
		this.classCode = classCode;
	}
	
	public String getclassName() {
        return className ;
    }
	public String getClassSegment() {
		return classSegment;
	}
	public String getClassCode() {
		return classCode;
	}

    @Override
    public String toString() {
        if(getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)){
        	return GlobalConstants.DEFAULT_CHARS_CLASS;
		}
		return getClassCode()+" "+getclassName();
    }
}
