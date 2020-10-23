package controllers.paneControllers;

import controllers.Char_description;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.UnitOfMeasure;

public class RulePane_CharClassif {
    @FXML Button buttonAddRule;
    @FXML Button buttonDeleteRule;
    @FXML Button buttonSaveRule;
    @FXML Button PaneClose;
    @FXML ComboBox<UnitOfMeasure> valueFieldUOM;
    @FXML Label valueLabelL1;
    @FXML Label valueLabelL2;
    @FXML Label valueLabelR1;
    @FXML Label valueLabelR2;
    @FXML TableColumn patternColumn;
    @FXML TableColumn statusColumn;
    @FXML TableColumn valueColumn;
    @FXML TableView ruleView;
    @FXML TextField patternField;
    @FXML TextField valueFieldL1;
    @FXML TextField valueFieldL2;
    @FXML TextField valueFieldR1;
    @FXML TextField valueFieldR2;

    public Char_description parent;

    public void setParent(Char_description char_description) {
        this.parent = char_description;
    }
    public void load_description_patterns() {
        System.out.println("Refreshing item rules");

    }
    @FXML public void PaneClose() {
        parent.ruleButton.setSelected(false);
        parent.setBottomRegionColumnSpans(false);

    }
    @FXML public void addRuleButtonAction(ActionEvent actionEvent) {
    }
    @FXML public void deleteRuleButtonAction(ActionEvent actionEvent) {
    }
    @FXML public void saveRuleButtonAction(ActionEvent actionEvent) {
    }
}
