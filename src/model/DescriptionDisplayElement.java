package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DescriptionDisplayElement {
    public static HashMap<String, List<DescriptionDisplayElement>> DisplaySettings = new HashMap<String, List<DescriptionDisplayElement>>();
    public static String fontSizeMode = "midFont";
    public SimpleIntegerProperty position = new SimpleIntegerProperty();
    public String fieldName;
    public SimpleBooleanProperty translate = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty linebreak = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty bold = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty italic = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty leftATableColumn = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty rightATableColumn = new SimpleBooleanProperty(false);
    public SimpleStringProperty prefix = new SimpleStringProperty();
    public SimpleStringProperty suffix = new SimpleStringProperty();

    public DescriptionDisplayElement(String fieldName, boolean translate, boolean lineBreak, boolean bold, boolean italic, boolean green, boolean white, String prefix, String suffix) {
        this.fieldName = fieldName;
        this.translate.set(translate);
        this.linebreak.set(lineBreak);
        this.bold.set(bold);
        this.italic.set(italic);
        this.leftATableColumn.set(green);
        this.rightATableColumn.set(white);
        this.prefix.set(prefix);
        this.suffix.set(suffix);
    }

    public static DescriptionDisplayElement createDescriptionDisplayElement(String fieldName) {
        double color = Math.random();
        return new DescriptionDisplayElement(fieldName, Math.random() > 0.5, Math.random()>0.5, Math.random() > 0.5, 0.8 > color && color > 0.5, color > 0.8, Math.random() > 0.5,"","");
    }

    public static List<DescriptionDisplayElement> randomDisplayElements(ArrayList<String> keySet) {
        /*if(randomizedSetting!=null){
            return randomizedSetting;
        }*/
        double color = Math.random();
        return keySet.stream().map(fieldName->new DescriptionDisplayElement(fieldName, Math.random() > 0.5, Math.random()>0.5, Math.random() > 0.5, 0.8 > color && color > 0.5, color > 0.8, Math.random() > 0.5,"",""))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<DescriptionDisplayElement> returnElementsForItem(CharDescriptionRow tmp, Integer rowIndex, Integer columnIndex) {
        List<DescriptionDisplayElement> ret = DescriptionDisplayElement.DisplaySettings.get(rowIndex.toString() + columnIndex.toString());
        if(ret==null){
            ret = DescriptionDisplayElement.randomDisplayElements(tmp.getDescriptionDataFields().stream().map(e -> e.getFieldName()).collect(Collectors.toCollection(ArrayList::new)));
            DescriptionDisplayElement.DisplaySettings.put(rowIndex.toString()+columnIndex.toString(),ret);
        }
        return ret;
    }

}
