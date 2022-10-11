package transversal.dialog_toolbox;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import scenes.paneScenes.TitlePaneControler;

public class CustomDialog extends Dialog {
    Node parentNode;
    private String title;
    private TitlePaneControler titleCntrlr;

    public void setCDTitle(String title) {
        this.title=title;
        try{
            titleCntrlr.windowTitle.setText(this.title);
        }catch (Exception V){

        }
    }

    public void setCDHeaderText(String title){
        if(title!=null && title.length()>0){
            GridPane newContent = (GridPane) getDialogPane().getContent();
            newContent.getChildren().stream().filter(node->GridPane.getRowIndex(node)>0).forEach(node -> {
                GridPane.setRowIndex(node, GridPane.getRowIndex(node) + 1);
            });
            Label titleLabel = new Label(title);
            titleLabel.setUnderline(true);
            newContent.add(titleLabel,0,1);
            getDialogPane().setContent(newContent);
        }


    }

    public CustomDialog(Node parentNode) {
        this.parentNode = parentNode;
        this.initStyle(StageStyle.TRANSPARENT);
        this.initModality(Modality.APPLICATION_MODAL);
        this.parentNode.getScene().getRoot().setEffect(new BoxBlur());
        this.setOnHiding(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                parentNode.getScene().getRoot().setEffect(null);
            }
        });
        this.getDialogPane().getStylesheets().add(CustomDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
        this.getDialogPane().getStyleClass().add("customDialog");

    }

    public void setContent(GridPane contentNode) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/titlePane.fxml"));
            BorderPane titleBar = loader.load();
            titleCntrlr=loader.getController();
            titleCntrlr.windowTitle.setText(this.title);
            contentNode.getChildren().forEach(node -> {
                GridPane.setRowIndex(node, GridPane.getRowIndex(node) + 1);
            });
            contentNode.add(titleBar,0,0);
            GridPane.setColumnSpan(titleBar,GridPane.REMAINING);
        }catch (Exception E){
            E.printStackTrace(System.err);
        }
        getDialogPane().setContent(contentNode);
    }
}
