package transversal.dialog_toolbox;

import controllers.Char_description;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.CaracteristicValue;
import model.CharDescriptionRow;
import model.ClassCaracteristic;
import service.CharValuesLoader;
import transversal.generic.Tools;


public class PatternEditionDialog {
    private static Dialog<Object> dialog;
    private static GridPane contentGrid;
    private static CharDescriptionRow sourceItem;
    private static String sourceSegment;
    private static ClassCaracteristic sourceCarac;
    private static int sourceColumnIdx;
    private static CaracteristicValue sourceValue;
    private static String sourceRule;
    private static GridPane caracBlocks;
    private static ComboBox<ClassCaracteristic> addCaracCombo;
    private static Button addCaracButton;
    private static String dataLanguage;
    private static String userLanguage;
    private static String dataLanguageCode;
    private static String userLanguageCode;


    public static void editRule(Char_description parent) {
        dialog = new Dialog<>();
        dialog.setTitle("Description pattern edition");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("customDialog");

        // Set the button types.
        ButtonType applyBT = new ButtonType("Apply");
        //dialog.getDialogPane().getButtonTypes().addAll(applyBT,ButtonType.CLOSE);
        contentGrid = new GridPane();
        contentGrid.setHgap(10);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(10, 10, 10, 10));
        dialog.getDialogPane().setContent(contentGrid);


        storeSourceData(parent);
        //createFieldsLinePerCarac();
        createFieldsBlocPerCarac();
        setFieldsLayout();
        setFieldsBehavior();

        dialog.showAndWait();

    }



    private static void storeSourceData(Char_description parent) {
        sourceItem = parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
        sourceSegment = sourceItem.getClass_segment_string().split("&&&")[0];
        sourceColumnIdx =  parent.tableController.selected_col%CharValuesLoader.active_characteristics.get(sourceSegment).size();
        sourceCarac = CharValuesLoader.active_characteristics.get(sourceSegment).get(sourceColumnIdx);
        sourceValue = sourceItem.getData(sourceSegment).get(sourceCarac.getCharacteristic_id());
        sourceRule = sourceValue.getRule_id();
        dataLanguage = parent.data_language;
        userLanguage = parent.user_language;
        dataLanguageCode = parent.data_language_gcode;
        userLanguageCode = parent.user_language_gcode;

    }

    private static void createFieldsBlocPerCarac() {
        contentGrid.add(new Label("Pattern rule"),0,0);
        TextField patternField = new TextField(sourceRule.split("<")[0]);
        contentGrid.add(patternField,1,0);
        GridPane.setColumnSpan(patternField,GridPane.REMAINING);
        caracBlocks = new GridPane();
        ScrollPane caracBlocksContainer = new ScrollPane();
        caracBlocksContainer.setContent(caracBlocks);
        caracBlocksContainer.setMinViewportHeight(256);
        caracBlocksContainer.setMinViewportWidth(256);
        caracBlocksContainer.getStylesheets().add(CaracDeclarationDialog.class.getResource("/styles/ScrollPaneTransparent.css").toExternalForm());
        caracBlocksContainer.setFitToWidth(true);
        contentGrid.add(caracBlocksContainer,0,1);
        GridPane.setColumnSpan(caracBlocksContainer,GridPane.REMAINING);
        addCaracCombo = new ComboBox<ClassCaracteristic>();
        addCaracCombo.getItems().addAll(CharValuesLoader.active_characteristics.get(sourceSegment));
        addCaracCombo.setValue(sourceCarac);
        contentGrid.add(addCaracCombo,2,2);
        GridPane.setHgrow(contentGrid, Priority.ALWAYS);
        addCaracButton = new Button("Add rule for characteristic");
        addCaracButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addCaracBlock(null);
            }
        });
        contentGrid.add(addCaracButton,3,2);
        addCaracBlock(sourceRule);
    }

    private static void addCaracBlock(String sourceRule) {
        ClassCaracteristic carac = addCaracCombo.getValue();
        addCaracCombo.getItems().remove(carac);
        if(addCaracCombo.getItems()!=null && addCaracCombo.getItems().size()>0) {
            addCaracCombo.getSelectionModel().clearAndSelect(0);
        }else{
            //Hide the add carac combo and button
            addCaracCombo.setVisible(false);
            addCaracButton.setVisible(false);
        }
        int maxRow = 1;
        for(Node node:caracBlocks.getChildren()){
            maxRow = Math.max(GridPane.getRowIndex(node)+1,maxRow);
        }
        addHeaderLine(carac,maxRow);
        maxRow+=1;
        //Add carac block lines
        if(carac.getIsNumeric()){
            addBlockLine("Nominal value",maxRow);
            maxRow+=1;
            addBlockLine("Minimum value",maxRow);
            maxRow+=1;
            addBlockLine("Maximum value",maxRow);
            maxRow+=1;
            if( (carac.getAllowedUoms()!=null && carac.getAllowedUoms().size()>0)) {
                addBlockLine("Unit of measure",maxRow);
                maxRow+=1;
            }
        }else{
            if(carac.getIsTranslatable() && dataLanguage!=userLanguage){
                addBlockLine("Value ("+dataLanguageCode+")",maxRow);
                maxRow+=1;
                addBlockLine("Value ("+userLanguageCode+")",maxRow);
                maxRow+=1;
            }else{
                addBlockLine("Value",maxRow);
                maxRow+=1;
            }
        }
        Button deleteBlockButton = new Button("Exclude characteristic " + carac.getCharacteristic_name());
        deleteBlockButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteCaracBlock(deleteBlockButton,carac);
            }
        });
        caracBlocks.add(deleteBlockButton,3,maxRow);
    }


    private static void deleteCaracBlock(Button deleteBlockButton, ClassCaracteristic carac) {
        Integer row2Remove = GridPane.getRowIndex(deleteBlockButton);
        Tools.deleteRow(caracBlocks,row2Remove);
        row2Remove=row2Remove-1;
        if(carac.getIsNumeric()){
            Tools.deleteRow(caracBlocks,row2Remove);
            row2Remove=row2Remove-1;
            Tools.deleteRow(caracBlocks,row2Remove);
            row2Remove=row2Remove-1;
            Tools.deleteRow(caracBlocks,row2Remove);
            row2Remove=row2Remove-1;
            if( (carac.getAllowedUoms()!=null && carac.getAllowedUoms().size()>0)) {
                Tools.deleteRow(caracBlocks,row2Remove);
            }
        }else{
            if(carac.getIsTranslatable() && dataLanguage!=userLanguage){
                Tools.deleteRow(caracBlocks,row2Remove);
                row2Remove=row2Remove-1;
                Tools.deleteRow(caracBlocks,row2Remove);
            }else{
                Tools.deleteRow(caracBlocks,row2Remove);
            }
        }
        //Delete header row
        Tools.deleteRow(caracBlocks,row2Remove);
    }

    private static void addHeaderLine(ClassCaracteristic carac, int maxRow) {
        Label fieldLabel = new Label("'"+carac.getCharacteristic_name()+"' fields:");
        fieldLabel.setMinWidth(56);
        fieldLabel.setUnderline(true);
        caracBlocks.add(fieldLabel,0,maxRow);
        Label valueField = new Label("Field pattern");
        valueField.setMinWidth(56);
        valueField.setUnderline(true);
        caracBlocks.add(valueField,1,maxRow);
        Label evalField = new Label("Field evaluation");
        evalField.setUnderline(true);
        evalField.setMinWidth(56);
        caracBlocks.add(evalField,2,maxRow);
        GridPane.setColumnSpan(evalField,GridPane.REMAINING);
    }

    private static void addBlockLine(String fieldName, int maxRow) {
        Label fieldLabel = new Label(fieldName);
        fieldLabel.setMinWidth(56);
        caracBlocks.add(fieldLabel,0,maxRow);
        TextField valueField = new TextField();
        valueField.setMinWidth(56);
        caracBlocks.add(valueField,1,maxRow);
        TextField evalField = new TextField();
        evalField.setEditable(false);
        evalField.setMinWidth(56);
        caracBlocks.add(evalField,2,maxRow);
        GridPane.setColumnSpan(evalField,GridPane.REMAINING);
    }

    private static void createFieldsLinePerCarac() {
        contentGrid.add(new Label("Pattern rule"),0,0);
        TextField patternField = new TextField(sourceRule.split("<")[0]);
        contentGrid.add(patternField,1,0);
        GridPane.setColumnSpan(patternField,GridPane.REMAINING);
        int valueEnum = 1;
        for(String value: sourceRule.split("<")[1].split("<")){
            contentGrid.add(new Label("Pattern value "+String.valueOf(valueEnum)),0,valueEnum);
            TextField valueField = new TextField(value.split(">")[0]);
            contentGrid.add(valueField,1,valueEnum);
            GridPane.setColumnSpan(valueField,2);
            ComboBox<ClassCaracteristic> caracLinkCombo = new ComboBox<ClassCaracteristic>();
            caracLinkCombo.getItems().addAll(CharValuesLoader.active_characteristics.get(sourceSegment));
            caracLinkCombo.setValue(sourceCarac);
            contentGrid.add(caracLinkCombo,3,valueEnum);
            Button deleteValueButton  = new Button("Delete value");
            deleteValueButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    deleteValueEntry(valueEnum);
                }
            });
            contentGrid.add(deleteValueButton,4,valueEnum);
        }
        Button addValueButton = new Button("Add value");
        addValueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addValueEntry(addValueButton);
            }
        });
        contentGrid.add(addValueButton,4,valueEnum+1);
    }

    private static void addValueEntry(Button addValueButton) {
        int valueEnum = 1;
        for(Node n:contentGrid.getChildren()){
            if(GridPane.getRowIndex(n)==GridPane.getRowIndex(addValueButton)-1 && GridPane.getColumnIndex(n)==0){
                valueEnum = Integer.parseInt(((Label) n).getText().split("Pattern value ")[1])+1;
            }
        }
        contentGrid.add(new Label("Pattern value "+String.valueOf(valueEnum)),0,valueEnum);
        TextField valueField = new TextField();
        contentGrid.add(valueField,1,valueEnum);
        GridPane.setColumnSpan(valueField,2);
        ComboBox<ClassCaracteristic> caracLinkCombo = new ComboBox<ClassCaracteristic>();
        caracLinkCombo.getItems().addAll(CharValuesLoader.active_characteristics.get(sourceSegment));
        caracLinkCombo.setValue(sourceCarac);
        contentGrid.add(caracLinkCombo,3,valueEnum);
        Button deleteValueButton  = new Button("Delete value");
        final int valueEnumCopy = valueEnum;
        deleteValueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteValueEntry(valueEnumCopy);
            }
        });
        contentGrid.add(deleteValueButton,4,valueEnum);
        GridPane.setRowIndex(addValueButton,GridPane.getRowIndex(addValueButton)+1);
    }

    private static void deleteValueEntry(int valueEnum) {
        Tools.deleteRow(contentGrid,valueEnum);
    }

    private static void setFieldsLayout() {

        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setPercentWidth(20);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(40);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(20);
        ColumnConstraints cc3 = new ColumnConstraints();
        cc3.setPercentWidth(20);
        contentGrid.getColumnConstraints().setAll(cc0,cc1,cc2,cc3);
        contentGrid.setMinWidth(240);

        ColumnConstraints bc0 = new ColumnConstraints();
        bc0.setPercentWidth(20);
        ColumnConstraints bc1 = new ColumnConstraints();
        bc1.setPercentWidth(40);
        ColumnConstraints bc2 = new ColumnConstraints();
        bc2.setPercentWidth(30);
        ColumnConstraints bc3 = new ColumnConstraints();
        bc3.setPercentWidth(10);
        caracBlocks.getColumnConstraints().setAll(cc0,cc1,cc2,cc3);
        caracBlocks.getStylesheets().add(CaracDeclarationDialog.class.getResource("/styles/DialogPane.css").toExternalForm());


    }

    private static void setFieldsBehavior() {
    }

}
