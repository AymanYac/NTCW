package transversal.generic;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;
import model.CharDescriptionRow;
import model.DescriptionDisplayElement;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import model.GlobalConstants;
import model.DescriptionDataElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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


    public static String getSelectedText(TextFlow ld) {
        AtomicReference<String> ret = new AtomicReference<>("");
        ld.lookupAll("TextField").forEach(tf->{
            if(tf instanceof TextField){
                ret.set(ret + ((TextField) tf).getSelectedText());
            }
        });
        return ret.get();
    }


    public static void renderDescription(TextFlow previewArea, CharDescriptionRow tmp, ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        renderDescription(previewArea,
                DescriptionDisplayElement.randomDisplayElements(tmp.getDescriptionDataFields().stream().map(e->e.getFieldName()).collect(Collectors.toCollection(ArrayList::new))),
                tmp.getDescriptionDataFields(),readOnlyDoubleProperty);
    }
    public static void renderDescription(TextFlow previewArea, List<DescriptionDisplayElement> items, List<DescriptionDataElement> fieldTableItems, ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        previewArea.getChildren().clear();
        items.forEach(elem->{
            Text txt = new Text(
                    (elem.prefix.get()!=null?elem.prefix.get():"")+
                            fieldTableItems.stream().filter(field->field.getFieldName().equals(elem.fieldName)).findFirst().get().getValue()+
                            (elem.suffix.get()!=null?elem.suffix.get():"")+
                            (elem.linebreak.get()?"\n":" ")
            );
            txt.setFill(elem.leftATableColumn.getValue()? GlobalConstants.DESC_NEONEC_GREEN:elem.rightATableColumn.getValue()?GlobalConstants.DESC_NEONEC_GREY: Color.BLACK);
            txt.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,elem.bold.get()? FontWeight.BOLD:FontWeight.THIN,elem.italic.get()?FontPosture.ITALIC:FontPosture.REGULAR,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
            TextArea tmp = new TextArea();
            tmp.setId(Tools.generate_uuid());
            areaRowCountMap.put(tmp.getId(),new SimpleIntegerProperty(20));
            /*tmp.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    tmp.setPrefWidth(TextUtils.computeTextWidth(tmp.getFont(),tmp.getText(), 0.0D) + 10);
                }
            });
            tmp.fontProperty().addListener(new ChangeListener<Font>() {
                @Override
                public void changed(ObservableValue<? extends Font> observable, Font oldValue, Font newValue) {
                    tmp.setPrefWidth(TextUtils.computeTextWidth(tmp.getFont(),tmp.getText(), 0.0D) + 10);
                }
            });*/


            tmp.setText((elem.prefix.get()!=null?elem.prefix.get():"")+
                    fieldTableItems.stream().filter(field->field.getFieldName().equals(elem.fieldName)).findFirst().get().getValue()+
                    (elem.suffix.get()!=null?elem.suffix.get():""));
            tmp.getStyleClass().clear();
            tmp.getStyleClass().add("basicText");
            if(elem.leftATableColumn.getValue()){
                tmp.getStyleClass().add("greenText");
            }
            if(elem.rightATableColumn.getValue()){
                tmp.getStyleClass().add("greyText");
            }
            if(elem.bold.get()){
                tmp.getStyleClass().add("boldText");
            }
            if(elem.italic.get()){
                tmp.getStyleClass().add("italicText");
            }

            int rowHeight = (int) tmp.getFont().getSize();
            tmp.prefHeightProperty().bindBidirectional(areaRowCountMap.get(tmp.getId()));
            tmp.minHeightProperty().bindBidirectional(areaRowCountMap.get(tmp.getId()));
            tmp.scrollTopProperty().addListener((ov, oldVal, newVal) -> {
                if(newVal.intValue() > rowHeight){
                    areaRowCountMap.get(tmp.getId()).set(areaRowCountMap.get(tmp.getId()).get() + newVal.intValue());
                }
            });
            tmp.prefWidthProperty().bind(readOnlyDoubleProperty);

            previewArea.getChildren().add(tmp);
            /*if(elem.linebreak.get()){
                previewArea.getChildren().add(new Text("\n"));
            }*/
        });
    }
}