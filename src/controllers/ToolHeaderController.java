package controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ToolHeaderController {
    public Label neonecTitle;
    public static SimpleStringProperty titleProperty = new SimpleStringProperty("");
    public static SimpleBooleanProperty closeHandlerSet = new SimpleBooleanProperty(false);
    public HBox buttonBox;
    public Label neonecIcon;
    public Label winClose;
    public static EventHandler<? super MouseEvent> closeHandler;

    public static void setCloseHandler(EventHandler newCloseHandler) {
        closeHandler=newCloseHandler;
    }


    @FXML void initialize(){
        neonecTitle.textProperty().bind(ToolHeaderController.titleProperty);
        closeHandlerSet.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    winClose.setOnMouseClicked(closeHandler);
                }
            }
        });
    }

    public void closeWindow(MouseEvent event) {
        if(closeHandlerSet.not().get()){
            Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
            s.close();
        }
    }
}
