package controllers.paneControllers;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import model.*;
import org.apache.commons.lang3.StringUtils;
import service.CharPatternServices;
import service.CharValuesLoader;
import service.TranslationServices;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class RulePane_CharClassif {
    @FXML Button buttonAddRule;
    @FXML Button buttonDeleteRule;
    @FXML Button buttonSaveRule;
    @FXML Button PaneClose;
    @FXML ComboBox<UnitOfMeasure> valueFieldUoM;
    @FXML Label valueLabelL1;
    @FXML Label valueLabelL2;
    @FXML Label valueLabelR1;
    @FXML Label valueLabelR2;
    @FXML TableColumn patternColumn;
    @FXML TableColumn statusColumn;
    @FXML TableColumn valueColumn;
    @FXML TableView<CharRuleResult> ruleView;
    @FXML TextField patternField;
    @FXML TextField valueFieldL1;
    @FXML TextField valueFieldR1;
    @FXML TextField valueFieldR2;
    @FXML ComboBox<ClassCaracteristic> caracCombo;

    public Char_description parent;
    private static CharDescriptionRow sourceItem;
    private static String sourceSegment;
    private static ClassCaracteristic sourceCarac;
    private static int sourceColumnIdx;
    private static ArrayList<CharRuleResult> sourceRules = new ArrayList<CharRuleResult>();
    private static String dataLanguage;
    private static String userLanguage;
    private static String dataLanguageCode;
    private static String userLanguageCode;
    private static Unidecode unidecode;
    private ChangeListener<String> dataFieldCL;
    private ChangeListener<String> userFieldCL;
    private AutoCompleteTextField autoL1;
    private AutoCompleteTextField autoR1;


    public void setParent(Char_description char_description) {
        autoL1=new AutoCompleteTextField(valueFieldL1,new ArrayList<>());
        autoR1=new AutoCompleteTextField(valueFieldR1,new ArrayList<>());

        this.parent = char_description;
        ruleView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CharRuleResult>() {
            @Override
            public void changed(ObservableValue<? extends CharRuleResult> observable, CharRuleResult oldValue, CharRuleResult newValue) {
                disableSaveAndEditButton(false);
                try{
                    valueFieldUoM.getSelectionModel().clearSelection();
                }catch (Exception V){

                }
                patternField.clear();
                valueFieldL1.clear();
                valueFieldR1.clear();
                valueFieldR2.clear();
                if(!(newValue!=null)){
                    return;
                }
                patternField.setText(newValue.getGenericCharRule().getRuleMarker());
                if(newValue.getSourceChar().getIsNumeric()){
                    if(newValue.getSourceChar().getAllowedUoms()!=null && newValue.getSourceChar().getAllowedUoms().size()>0){
                        newValue.getGenericCharRule().getRuleActions().forEach(action->{
                            if(action.startsWith("NOM ")) {
                                action=action.substring(4).trim();
                                valueFieldL1.setText(action);
                            }
                            if(action.startsWith("MIN ")) {
                                action=action.substring(4).trim();
                                valueFieldR1.setText(action);
                            }

                            if(action.startsWith("MAX ")) {
                                action=action.substring(4).trim();
                                valueFieldR2.setText(action);
                            }

                            if(action.startsWith("MINMAX ")) {
                                action=action.substring(7).trim();
                                if(valueFieldR1.getText()!=null && valueFieldR1.getText().length()>0){
                                    valueFieldR2.setText(action);
                                }else{
                                    valueFieldR1.setText(action);
                                }
                            }
                            if(action.startsWith("UOM ")) {
                                final String symbol=action.substring(5).substring(0,action.length()-6).trim();
                                try{
                                    valueFieldUoM.setValue(valueFieldUoM.getItems().stream().filter(u-> StringUtils.equalsIgnoreCase(u.getUom_symbol(),symbol)).findAny().get());
                                }catch (Exception V){
                                    try {
                                        UnitOfMeasure newUom = UnitOfMeasure.RunTimeUOMS.values().stream().filter(loopUom -> loopUom.getUom_symbol().equalsIgnoreCase(symbol)).findAny().get();
                                        valueFieldUoM.getItems().add(newUom);
                                        valueFieldUoM.setValue(newUom);
                                    }catch (Exception E){
                                        valueFieldUoM.getSelectionModel().clearAndSelect(0);
                                    }

                                }
                            }
                        });
                    }else{

                        newValue.getGenericCharRule().getRuleActions().forEach(action->{
                            if(action.startsWith("NOM ")) {
                                action=action.substring(4).trim();
                                valueFieldL1.setText(action);
                            }
                            if(action.startsWith("MIN ")) {
                                action=action.substring(4).trim();
                                valueFieldR1.setText(action);
                            }

                            if(action.startsWith("MAX ")) {
                                action=action.substring(4).trim();
                                valueFieldR2.setText(action);
                            }

                            if(action.startsWith("MINMAX ")) {
                                action=action.substring(7).trim();
                                if(valueFieldR1.getText()!=null && valueFieldR1.getText().length()>0){
                                    valueFieldR2.setText(action);
                                }else{
                                    valueFieldR1.setText(action);
                                }
                            }
                        });
                    }
                }else{
                    newValue.getGenericCharRule().getRuleActions().forEach(action->{
                        if(action.startsWith("DL ")) {
                            action=action.substring(3).trim();
                            valueFieldL1.setText(action);
                        }
                        if(action.startsWith("UL ")) {
                            action=action.substring(3).trim();
                            valueFieldR1.setText(action);
                        }
                        if(action.startsWith("TXT ")) {
                            action=action.substring(4).trim();
                            valueFieldL1.setText(action);
                        }

                    });
                }
            }
        });
        ruleView.setRowFactory( tv -> {
            TableRow<CharRuleResult> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    CharRuleResult rowData = row.getItem();
                    CaracteristicValue val = rowData.getActionValue().shallowCopy(parent.account);
                    //val.setRule_id(null);
                    val.setSource(DataInputMethods.SEMI_CHAR_DESC);
                    parent.assignValueOnSelectedItems(val);
                    parent.tableController.jumpNext();
                    parent.refresh_ui_display();
                }
            });
            return row ;
        });
        caracCombo.valueProperty().addListener(new ChangeListener<ClassCaracteristic>() {
            @Override
            public void changed(ObservableValue<? extends ClassCaracteristic> observable, ClassCaracteristic oldValue, ClassCaracteristic newValue) {
                if(ClassCaracteristic.caracsAreHomogenous(oldValue,newValue)){
                    CharRuleResult oldResult = ruleView.getSelectionModel().getSelectedItem();
                    if(oldResult!=null){
                        CharRuleResult oldResultCopy = oldResult.shallowCopy(newValue,parent.account);
                        oldResultCopy.setStatus("Draft");
                        parent.tableController.tableGrid.getSelectionModel().getSelectedItem().addRuleResult2Row(oldResultCopy);
                    }
                }
                if(newValue!=null){
                    hideFields();
                    fetchRules(newValue);
                }

            }
        });
        ChangeListener<? super String> fieldEditListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String newSyntax = loadRuleFromPane(false);
                CharRuleResult oldRule = ruleView.getSelectionModel().getSelectedItem();
                if(oldRule!=null && oldRule.getGenericCharRule()!=null && oldRule.getGenericCharRule().getRuleSyntax()!=null){
                    disableSaveAndEditButton(StringUtils.equalsIgnoreCase(newSyntax,oldRule.getGenericCharRule().getRuleSyntax()));
                }
                disableSaveAndEditButton(false);
            }
        };
        patternField.textProperty().addListener(fieldEditListener);
        valueFieldL1.textProperty().addListener(fieldEditListener);
        valueFieldR1.textProperty().addListener(fieldEditListener);
        valueFieldR2.textProperty().addListener(fieldEditListener);
        dataFieldCL = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                unidecode = unidecode != null ? unidecode : Unidecode.toAscii();
                if (newValue != null &&
                        TranslationServices.getTextEntriesForCharOnLanguages(caracCombo.getValue(), true).stream()
                                .anyMatch(e ->
                                        StringUtils.equalsIgnoreCase(unidecode.decode(e.getSource_value()), unidecode.decode(newValue))
                                )) {
                    valueFieldR1.setText(null);
                }
            }
        };
        userFieldCL = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                unidecode = unidecode != null ? unidecode : Unidecode.toAscii();
                if (newValue != null &&
                        TranslationServices.getTextEntriesForCharOnLanguages(caracCombo.getValue(), false).stream()
                                .anyMatch(e ->
                                        StringUtils.equalsIgnoreCase(unidecode.decode(e.getSource_value()), unidecode.decode(newValue))
                                )) {
                    valueFieldL1.setText(null);
                }
            }
        };

    }

    private void disableSaveAndEditButton(boolean disable) {
        if(1==1){
            return;
        }
        if(disable){
            buttonAddRule.setDisable(false);
            buttonSaveRule.setDisable(true);
            buttonDeleteRule.setDisable(true);
            return;
        }
        CharRuleResult currentRule = ruleView.getSelectionModel().getSelectedItem();
        if(currentRule!=null){

        }
        buttonSaveRule.setDisable(false);
        if(currentRule.isDraft()){
            buttonSaveRule.setDisable(true);
        }

    }

    public void load_description_patterns() {
        setLayoutAndDS();
    }

    private void fetchRules(ClassCaracteristic sourceCarac) {
        try{
            sourceRules.clear();
            sourceRules.addAll(new ArrayList<>(sourceItem.getRuleResults().get(sourceCarac.getCharacteristic_id()).stream().filter(r->r.getGenericCharRule()!=null).collect(Collectors.toCollection(ArrayList::new))));
        }catch (Exception V){
            //No rules for current item for current carac
        }
        displaySourceRules();
    }

    private void displaySourceRules() {
        try{
            ruleView.getItems().clear();
        }catch (Exception V){

        }
        ruleView.getItems().addAll(sourceRules);
        refreshLayout();
        selectPrimaryRule();
    }

    private void refreshLayout() {
        try{
            valueFieldUoM.getItems().clear();
        }catch (Exception V){

        }
        patternField.clear();
        valueFieldL1.clear();
        valueFieldR1.clear();
        valueFieldR2.clear();

        if(caracCombo.getValue().getIsNumeric()){
            if(caracCombo.getValue().getAllowedUoms()!=null && caracCombo.getValue().getAllowedUoms().size()>0){
                GridPane.setColumnSpan(valueLabelL1,1);
                valueLabelL1.setText("Nominal Value");
                valueLabelL1.setVisible(true);
                valueFieldL1.setVisible(true);
                GridPane.setColumnSpan(valueLabelL2,1);
                valueLabelL2.setText("UoM");
                valueLabelL2.setVisible(true);
                valueFieldUoM.setVisible(true);
                GridPane.setColumnSpan(valueLabelR1,1);
                valueLabelR1.setText("Min value");
                valueLabelR1.setVisible(true);
                valueFieldR1.setVisible(true);
                GridPane.setColumnSpan(valueLabelR2,1);
                valueLabelR2.setText("Max value");
                valueLabelR2.setVisible(true);
                valueFieldR2.setVisible(true);
                GridPane.setColumnSpan(valueFieldL1,1);
                GridPane.setColumnSpan(valueFieldR1,1);
                GridPane.setColumnSpan(valueFieldR2,1);
                GridPane.setColumnSpan(valueFieldUoM,1);

                /*valueFieldUoM.getItems().setAll(
                        new ArrayList<>(
                                caracCombo.getValue().getAllowedUoms().stream()
                                        .map(uid->UnitOfMeasure.RunTimeUOMS.get(uid))
                                        .collect(Collectors.toCollection(ArrayList::new))
                        ));*/
                valueFieldUoM.getItems().setAll(
                        new ArrayList<>(
                                UnitOfMeasure.RunTimeUOMS.values().stream()
                                        .filter(uom->UnitOfMeasure.ConversionPathExists(uom,caracCombo.getValue().getAllowedUoms()))
                                        .collect(Collectors.toCollection(ArrayList::new))
                        ));
                UnitOfMeasure defaultUom = new ArrayList<>(
                        caracCombo.getValue().getAllowedUoms().stream()
                                .map(uid -> UnitOfMeasure.RunTimeUOMS.get(uid))
                                .collect(Collectors.toCollection(ArrayList::new))
                ).get(0);
                valueFieldUoM.setValue(defaultUom);
            }else{
                GridPane.setColumnSpan(valueLabelL1,3);
                valueLabelL1.setText("Nominal Value");
                valueLabelL1.setVisible(true);
                valueFieldL1.setVisible(true);
                GridPane.setColumnSpan(valueLabelR1,1);
                valueLabelR1.setText("Min value");
                valueLabelR1.setVisible(true);
                valueFieldR1.setVisible(true);
                GridPane.setColumnSpan(valueLabelR2,1);
                valueLabelR2.setText("Max value");
                valueLabelR2.setVisible(true);
                valueFieldR2.setVisible(true);
                GridPane.setColumnSpan(valueFieldL1,3);
                GridPane.setColumnSpan(valueFieldR1,1);
                GridPane.setColumnSpan(valueFieldR2,1);
            }
        }else{
            if(caracCombo.getValue().getIsTranslatable() && !dataLanguage.equals(userLanguage)){
                GridPane.setColumnSpan(valueLabelL1,3);
                valueLabelL1.setText("Value ("+dataLanguageCode.toUpperCase()+")");
                valueLabelL1.setVisible(true);
                GridPane.setColumnSpan(valueFieldL1,3);
                valueFieldL1.setVisible(true);
                autoL1.getEntries().clear();
                autoL1.getEntries().addAll(TranslationServices.getTextEntriesForCharOnLanguages(caracCombo.getValue(),true));
                valueFieldL1.textProperty().addListener(dataFieldCL);
                GridPane.setColumnSpan(valueLabelR1,3);
                valueLabelR1.setText("Value ("+userLanguageCode.toUpperCase()+")");
                valueLabelR1.setVisible(true);
                GridPane.setColumnSpan(valueFieldR1,3);
                valueFieldR1.setVisible(true);
                autoR1.getEntries().clear();
                autoR1.getEntries().addAll(TranslationServices.getTextEntriesForCharOnLanguages(caracCombo.getValue(),false));
                valueFieldR1.textProperty().addListener(userFieldCL);
            }else{
                GridPane.setColumnSpan(valueLabelL2,3);
                valueLabelL2.setText("Value");
                valueLabelL2.setVisible(true);
                GridPane.setColumnSpan(valueFieldL1,7);
                valueFieldL1.setVisible(true);
                autoL1.getEntries().clear();
                autoL1.getEntries().addAll(TranslationServices.getTextEntriesForCharOnLanguages(caracCombo.getValue(),true));

            }
        }
        ruleView.refresh();
    }

    private void selectPrimaryRule() {
        Optional<CharRuleResult> draftRule = ruleView.getItems().stream().filter(r -> r.getStatus() != null && r.getStatus().equals("Draft")).findAny();
        if(draftRule.isPresent()){
            ruleView.getSelectionModel().select(draftRule.get());
            return;
        }
        Optional<CharRuleResult> appliedRule = ruleView.getItems().stream().filter(r -> r.getStatus() != null && r.getStatus().equals("Applied")).findAny();
        if(appliedRule.isPresent()){
            ruleView.getSelectionModel().select(appliedRule.get());
            return;
        }
        Optional<CharRuleResult> suggRule = ruleView.getItems().stream().filter(r -> r.getStatus() != null && r.getStatus().equals("Suggestion 1")).findAny();
        if(suggRule.isPresent()){
            ruleView.getSelectionModel().select(suggRule.get());
            return;
        }
        ruleView.getSelectionModel().select(0);
    }

    private void setLayoutAndDS() {
        autoL1.getEntries().clear();
        autoR1.getEntries().clear();
        hideFields();
        patternColumn.setCellValueFactory(new PropertyValueFactory<>("MatchedBlock"));
        patternColumn.prefWidthProperty().bind(ruleView.widthProperty()
                        .multiply(50 / 100.0));
        patternColumn.setResizable(false);

        valueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CharRuleResult, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CharRuleResult, String> r) {

                return new ReadOnlyObjectWrapper(r.getValue().getActionValue().getDisplayValue(parent));
            }
        });
        valueColumn.prefWidthProperty().bind(ruleView.widthProperty()
                .multiply(30 / 100.0));
        valueColumn.setResizable(false);

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));
        statusColumn.prefWidthProperty().bind(ruleView.widthProperty()
                .multiply(20 / 100.0));
        statusColumn.setResizable(false);

        sourceItem = parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
        sourceSegment = sourceItem.getClass_segment_string().split("&&&")[0];
        sourceColumnIdx =  parent.tableController.selected_col% CharValuesLoader.active_characteristics.get(sourceSegment).size();
        sourceCarac = CharValuesLoader.active_characteristics.get(sourceSegment).get(sourceColumnIdx);
        dataLanguage = parent.data_language;
        userLanguage = parent.user_language;
        dataLanguageCode = parent.data_language_gcode;
        userLanguageCode = parent.user_language_gcode;

        caracCombo.getItems().setAll(CharValuesLoader.active_characteristics.get(sourceSegment));
        caracCombo.setValue(null);
        caracCombo.setValue(sourceCarac);

    }

    private void hideFields() {
        valueFieldL1.setVisible(false);
        valueFieldL1.textProperty().removeListener(dataFieldCL);
        valueFieldR1.setVisible(false);
        valueFieldR1.textProperty().removeListener(userFieldCL);
        valueFieldR2.setVisible(false);
        valueLabelL1.setVisible(false);
        valueLabelL2.setVisible(false);
        valueLabelR1.setVisible(false);
        valueLabelR2.setVisible(false);
        valueFieldUoM.setVisible(false);
    }

    @FXML public void PaneClose() {
        parent.ruleButton.setSelected(false);
        parent.setBottomRegionColumnSpans(false);

    }
    @FXML public void addRuleButtonAction() throws SQLException, ClassNotFoundException {
        if(ruleView.getSelectionModel().getSelectedItem()!=null && ruleView.getSelectionModel().getSelectedItem().isDraft()){
            parent.tableController.tableGrid.getSelectionModel().getSelectedItem().dropRuleResultFromRow(ruleView.getSelectionModel().getSelectedItem());
        }
        GenericCharRule newRule = new GenericCharRule(loadRuleFromPane(true), caracCombo.getValue());
        newRule.setRegexMarker();
        if(newRule.parseSuccess()) {
            newRule.storeGenericCharRule();
            try{
                CharPatternServices.suppressGenericRuleInDB(parent.account.getActive_project(),newRule.getCharRuleId(),false);
            }catch (Exception V){
                V.printStackTrace(System.err);
                ExceptionDialog.show("Connection Error","Could not reach server","Rule could not be saved. Please restart");
            }
            CharPatternServices.quickApplyRule(newRule,caracCombo.getValue(),parent);
            new Thread(() -> {
                parent.tableController.ReevaluateItems(CharPatternServices.applyRule(newRule,caracCombo.getValue(),parent.account));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        parent.refresh_ui_display();
                        parent.tableController.tableGrid.refresh();
                    }
                });
            }).start();
        }
    }
    @FXML public void deleteRuleButtonAction() throws SQLException, ClassNotFoundException {
        if(ruleView.getSelectionModel().getSelectedItem()!=null){
            GenericCharRule oldRule = ruleView.getSelectionModel().getSelectedItem().getGenericCharRule();
            new Thread(()->{
                parent.tableController.ReevaluateItems(CharPatternServices.unApplyRule(oldRule,caracCombo.getValue(),parent.account));
                try{
                    oldRule.dropGenericCharRule();
                    CharPatternServices.suppressGenericRuleInDB(parent.account.getActive_project(),oldRule.getCharRuleId(),true);
                }catch (Exception V){

                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        parent.refresh_ui_display();
                        parent.tableController.tableGrid.refresh();
                    }
                });
            }).start();
        }
        parent.refresh_ui_display();
        parent.tableController.tableGrid.refresh();
    }
    @FXML public void saveRuleButtonAction() throws SQLException, ClassNotFoundException {
        new Thread(()-> {
            HashSet<String> items2Reevaluate = new HashSet<String>();

            if (ruleView.getSelectionModel().getSelectedItem() != null) {
                GenericCharRule oldRule = ruleView.getSelectionModel().getSelectedItem().getGenericCharRule();
                items2Reevaluate.addAll(CharPatternServices.unApplyRule(oldRule, caracCombo.getValue(), parent.account));
                oldRule.dropGenericCharRule();
                try {
                    CharPatternServices.suppressGenericRuleInDB( parent.account.getActive_project(), oldRule.getCharRuleId(), true);
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }
            }
            GenericCharRule newRule = new GenericCharRule(loadRuleFromPane(true), caracCombo.getValue());
            newRule.setRegexMarker();
            if (newRule.parseSuccess()) {
                newRule.storeGenericCharRule();
                try {
                    CharPatternServices.suppressGenericRuleInDB(parent.account.getActive_project(), newRule.getCharRuleId(), false);
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }
                CharPatternServices.quickApplyRule(newRule,caracCombo.getValue(),parent);
                items2Reevaluate.addAll(CharPatternServices.applyRule(newRule, caracCombo.getValue(), parent.account));
            }
            parent.tableController.ReevaluateItems(items2Reevaluate);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    parent.refresh_ui_display();
                    parent.tableController.tableGrid.refresh();
                }
            });
        }).start();
    }

    private String loadRuleFromPane(boolean correctInputs) {
        ClassCaracteristic activeChar = caracCombo.getValue();
        if(correctInputs){
            correctRulePaneInputs(activeChar.getIsNumeric());
        }
        String rule_id=patternField.getText();
        if(activeChar.getIsNumeric()){
            if(activeChar.getAllowedUoms()!=null && activeChar.getAllowedUoms().size()>0){
                rule_id = rule_id+
                        ((valueFieldL1.getText()!=null && valueFieldL1.getText().length()>0)?"<NOM "+valueFieldL1.getText()+">":"")+
                        ((valueFieldUoM.getValue()!=null)?"<UOM '"+valueFieldUoM.getValue().getUom_symbol()+"'>":"<UOM '"+UnitOfMeasure.RunTimeUOMS.get(activeChar.getAllowedUoms().get(0)).getUom_symbol()+"'>")+
                        ((valueFieldR1.getText()!=null && valueFieldR1.getText().length()>0)?"<MIN "+valueFieldR1.getText()+">":"")+
                        ((valueFieldR2.getText()!=null && valueFieldR2.getText().length()>0)?"<MAX "+valueFieldR2.getText()+">":"");
            }else{
                rule_id = rule_id+
                        ((valueFieldL1.getText()!=null && valueFieldL1.getText().length()>0)?"<NOM "+valueFieldL1.getText()+">":"")+
                        ((valueFieldR1.getText()!=null && valueFieldR1.getText().length()>0)?"<MIN "+valueFieldR1.getText()+">":"")+
                        ((valueFieldR2.getText()!=null && valueFieldR2.getText().length()>0)?"<MAX "+valueFieldR2.getText()+">":"");
            }
        }else{
            if(activeChar.getIsTranslatable() && !dataLanguage.equals(userLanguage)){
                rule_id = rule_id+
                        ((valueFieldL1.getText()!=null && valueFieldL1.getText().length()>0)?"<DL "+valueFieldL1.getText()+">":"")+
                        ((valueFieldR1.getText()!=null && valueFieldR1.getText().length()>0)?"<UL "+valueFieldR1.getText()+">":"");
            }else{
                rule_id = rule_id+
                        ((valueFieldL1.getText()!=null && valueFieldL1.getText().length()>0)?"<TXT "+valueFieldL1.getText()+">":"");
            }
        }
        return rule_id;
    }

    private void correctRulePaneInputs(boolean isNumeric) {
        patternField.setText(WordUtils.correctDescriptionRuleSyntaxElement(patternField.getText(),false));
        valueFieldL1.setText(WordUtils.correctDescriptionRuleSyntaxElement(valueFieldL1.getText(),isNumeric));
        valueFieldR1.setText(WordUtils.correctDescriptionRuleSyntaxElement(valueFieldR1.getText(),isNumeric));
        valueFieldR2.setText(WordUtils.correctDescriptionRuleSyntaxElement(valueFieldR2.getText(),isNumeric));
    }
}
