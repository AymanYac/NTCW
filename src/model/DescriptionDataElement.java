package model;

public class DescriptionDataElement {
    private String fieldName;
    private String value;

    public DescriptionDataElement(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.value = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }
}
