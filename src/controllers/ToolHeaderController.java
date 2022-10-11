package controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ToolHeaderController {
    public Label neonecTitle;
    public static SimpleStringProperty titleProperty = new SimpleStringProperty("");
    public HBox buttonBox;
    public Label neonecIcon;

    public void closeWindow(MouseEvent event) {
        Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
        s.close();
    }

    @FXML void initialize(){
        neonecTitle.textProperty().bind(ToolHeaderController.titleProperty);
    }
}
