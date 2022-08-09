package transversal.generic;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import model.CharDescriptionRow;
import model.DescriptionDisplayElement;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import model.GlobalConstants;
import model.DescriptionDataElement;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextUtils {

    static final Text helper;
    static final double DEFAULT_WRAPPING_WIDTH;
    static final double DEFAULT_LINE_SPACING;
    static final String DEFAULT_TEXT;
    static final TextBoundsType DEFAULT_BOUNDS_TYPE;
    private static HashMap<String, SimpleIntegerProperty> areaRowCountMap = new HashMap<>();

    static {
        helper = new Text();
        DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
        DEFAULT_LINE_SPACING = helper.getLineSpacing();
        DEFAULT_TEXT = helper.getText();
        DEFAULT_BOUNDS_TYPE = helper.getBoundsType();
    }

    public static double computeTextWidth(Font font, String text, double help0) {
        // Toolkit.getToolkit().getFontLoader().computeStringWidth(field.getText(),
        // field.getFont());

        helper.setText(text);
        helper.setFont(font);

        helper.setWrappingWidth(0.0D);
        helper.setLineSpacing(0.0D);
        double d = Math.min(helper.prefWidth(-1.0D), help0);
        helper.setWrappingWidth((int) Math.ceil(d));
        d = Math.ceil(helper.getLayoutBounds().getWidth());

        helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        helper.setLineSpacing(DEFAULT_LINE_SPACING);
        helper.setText(DEFAULT_TEXT);
        return d;
    }


    public static String getSelectedText(StyleClassedTextArea ld) {
        return ld.getSelectedText();
    }


    public static void renderDescription(StyleClassedTextArea previewArea, CharDescriptionRow tmp, ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        renderDescription(previewArea,
                DescriptionDisplayElement.randomDisplayElements(tmp.getDescriptionDataFields().stream().map(e->e.getFieldName()).collect(Collectors.toCollection(ArrayList::new))),
                tmp.getDescriptionDataFields(),readOnlyDoubleProperty);
    }
    public static void renderDescription(StyleClassedTextArea previewArea, List<DescriptionDisplayElement> items, List<DescriptionDataElement> fieldTableItems, ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        previewArea.clear();
        StringBuilder sb = new StringBuilder();
        ArrayList<Integer> zones = new ArrayList<>();
        ArrayList<ArrayList<String>> styles= new ArrayList<>();
        items.forEach(elem->{
            sb.append((elem.prefix.get()!=null?elem.prefix.get():"")+
                    fieldTableItems.stream().filter(field->field.getFieldName().equals(elem.fieldName)).findFirst().get().getValue()+
                    (elem.suffix.get()!=null?elem.suffix.get():"")+
                    (elem.linebreak.get()?"\n":" "));
            zones.add(sb.length());
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.add("basicText");
            if(elem.leftATableColumn.getValue()){
                tmp.add("greenText");
            }
            if(elem.rightATableColumn.getValue()){
                tmp.add("greyText");
            }
            if(elem.bold.get()){
                tmp.add("boldText");
            }
            if(elem.italic.get()){
                tmp.add("italicText");
            }
            styles.add(tmp);
        });
        previewArea.insertText(0,sb.toString());
        IntStream.range(0,zones.size()).forEach(idx->{
            previewArea.setStyle(idx>0?zones.get(idx-1):0,idx<zones.size()-1?zones.get(idx):sb.length(),styles.get(idx));
        });
        previewArea.setWrapText(true);
        previewArea.prefWidthProperty().bind(readOnlyDoubleProperty);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                previewArea.scrollYToPixel(0.0);
            }
        });
    }
}