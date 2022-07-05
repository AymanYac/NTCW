package transversal.dialog_toolbox;

import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Optional;


public class DescriptionExportExcelSheets {
    public static Optional<ArrayList<Boolean>> choicePopUp(){
        // Create the custom dialog.
        Dialog<ArrayList<Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Exporting item description data");
        dialog.setHeaderText("Please choose export sheets");
        dialog.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("customDialog");

        // Set the button types.
        ButtonType validateButtonType = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

        // Create the  uom labels and fields.

        GridPane grid = new GridPane();
        grid.add(new Label("Export items in review format"),0,0);
        CheckBox reviewCB = new CheckBox();
        reviewCB.setSelected(true);
        grid.add(reviewCB,1,0);
        grid.add(new Label("Export items in transaction format"),0,1);
        CheckBox baseCB = new CheckBox();
        baseCB.setSelected(true);
        grid.add(baseCB,1,1);
        grid.add(new Label("Export project taxonomy"),0,2);
        CheckBox taxoCB = new CheckBox();
        taxoCB.setSelected(true);
        grid.add(taxoCB,1,2);
        grid.add(new Label("Export known txt values"),0,3);
        CheckBox kvCB = new CheckBox();
        kvCB.setSelected(true);
        grid.add(kvCB,1,3);
        grid.add(new Label("Export known rules"),0,4);
        CheckBox rlCB = new CheckBox();
        rlCB.setSelected(true);
        grid.add(rlCB,1,4);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(80);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(20);
        grid.getColumnConstraints().addAll(c1,c2);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(validateButtonType).disableProperty().bind((reviewCB.selectedProperty().or(baseCB.selectedProperty()).or(taxoCB.selectedProperty()).or(kvCB.selectedProperty())).not());

        // Convert the result to a uom when the store button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == validateButtonType) {
                ArrayList<Boolean> tmp = new ArrayList<Boolean>();
                tmp.add(reviewCB.isSelected());
                tmp.add(baseCB.isSelected());
                tmp.add(taxoCB.isSelected());
                tmp.add(kvCB.isSelected());
                tmp.add(rlCB.isSelected());
                return tmp;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
