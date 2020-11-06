package controllers.paneControllers;

import controllers.Char_description;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import model.*;
import org.apache.commons.lang.StringUtils;
import service.CharPatternServices;
import service.CharValuesLoader;

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
    @FXML TextField valueFieldL2;
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


    public void setParent(Char_description char_description) {
        this.parent = char_description;
        ruleView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CharRuleResult>() {
            @Override
            public void changed(ObservableValue<? extends CharRuleResult> observable, CharRuleResult oldValue, CharRuleResult newValue) {
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
                                if(valueFieldR1.isVisible()){
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
                                    valueFieldUoM.getSelectionModel().clearAndSelect(0);
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
        caracCombo.valueProperty().addListener(new ChangeListener<ClassCaracteristic>() {
            @Override
            public void changed(ObservableValue<? extends ClassCaracteristic> observable, ClassCaracteristic oldValue, ClassCaracteristic newValue) {
                if(newValue!=null){
                    fetchRules(newValue);
                }

            }
        });
        ChangeListener<? super String> fieldEditListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

            }
        };
        valueFieldL1.textProperty().addListener(fieldEditListener);
        valueFieldL2.textProperty().addListener(fieldEditListener);
        valueFieldR1.textProperty().addListener(fieldEditListener);
        valueFieldR2.textProperty().addListener(fieldEditListener);

    }
    public void load_description_patterns() {
        setLayoutAndDS();
    }

    private void fetchRules(ClassCaracteristic sourceCarac) {
        try{
            sourceRules.clear();
            sourceRules.addAll(new ArrayList<>(sourceItem.getRuleResults().get(sourceCarac.getCharacteristic_id())));
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
        valueFieldL2.clear();
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

                valueFieldUoM.getItems().setAll(
                        new ArrayList<>(
                                caracCombo.getValue().getAllowedUoms().stream()
                                        .map(uid->UnitOfMeasure.RunTimeUOMS.get(uid))
                                        .collect(Collectors.toCollection(ArrayList::new))
                        ));
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
                GridPane.setColumnSpan(valueLabelR1,3);
                valueLabelR1.setText("Value ("+userLanguageCode.toUpperCase()+")");
                valueLabelR1.setVisible(true);
                GridPane.setColumnSpan(valueFieldR1,3);
                valueFieldR1.setVisible(true);
            }else{
                GridPane.setColumnSpan(valueLabelL2,3);
                valueLabelL2.setText("Value");
                valueLabelL2.setVisible(true);
                GridPane.setColumnSpan(valueFieldL1,7);
                valueFieldL1.setVisible(true);

            }
        }
        ruleView.refresh();
    }

    private void selectPrimaryRule() {
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
        valueFieldL1.setVisible(false);
        valueFieldL2.setVisible(false);
        valueFieldR1.setVisible(false);
        valueFieldR2.setVisible(false);
        valueLabelL1.setVisible(false);
        valueLabelL2.setVisible(false);
        valueLabelR1.setVisible(false);
        valueLabelR2.setVisible(false);

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

    @FXML public void PaneClose() {
        parent.ruleButton.setSelected(false);
        parent.setBottomRegionColumnSpans(false);

    }
    @FXML public void addRuleButtonAction() throws SQLException, ClassNotFoundException {
        GenericCharRule newRule = new GenericCharRule(loadRuleFromPane());
        newRule.generateRegex(caracCombo.getValue());
        if(newRule.parseSuccess()) {
            newRule.storeGenericCharRule();
            CharPatternServices.suppressGenericRuleInDB(null,parent.account.getActive_project(),newRule.getCharRuleId(),false);
            parent.tableController.ReevaluateItems(CharPatternServices.applyRule(newRule,caracCombo.getValue(),parent.account));
        }
        parent.refresh_ui_display();
    }
    @FXML public void deleteRuleButtonAction() throws SQLException, ClassNotFoundException {
        if(ruleView.getSelectionModel().getSelectedItem()!=null){
            GenericCharRule oldRule = ruleView.getSelectionModel().getSelectedItem().getGenericCharRule();
            oldRule.dropGenericCharRule();
            CharPatternServices.suppressGenericRuleInDB(null,parent.account.getActive_project(),oldRule.getCharRuleId(),true);
            parent.tableController.ReevaluateItems(CharPatternServices.unApplyRule(oldRule,caracCombo.getValue()));
        }
        parent.refresh_ui_display();
    }
    @FXML public void saveRuleButtonAction() throws SQLException, ClassNotFoundException {
        HashSet<String> items2Reevaluate = new HashSet<String>();

        if(ruleView.getSelectionModel().getSelectedItem()!=null){
            GenericCharRule oldRule = ruleView.getSelectionModel().getSelectedItem().getGenericCharRule();
            oldRule.dropGenericCharRule();
            CharPatternServices.suppressGenericRuleInDB(null,parent.account.getActive_project(),oldRule.getCharRuleId(),true);
            items2Reevaluate.addAll(CharPatternServices.unApplyRule(oldRule,caracCombo.getValue()));
        }
        GenericCharRule newRule = new GenericCharRule(loadRuleFromPane());
        newRule.generateRegex(caracCombo.getValue());
        if(newRule.parseSuccess()) {
            newRule.storeGenericCharRule();
            CharPatternServices.suppressGenericRuleInDB(null,parent.account.getActive_project(),newRule.getCharRuleId(),false);
            items2Reevaluate.addAll(CharPatternServices.applyRule(newRule, caracCombo.getValue(),parent.account));
        }

        parent.tableController.ReevaluateItems(items2Reevaluate);
        parent.refresh_ui_display();
    }

    private String loadRuleFromPane() {
        ClassCaracteristic activeChar = caracCombo.getValue();
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
}
