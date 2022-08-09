package transversal.dialog_toolbox;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.CharDescriptionRow;
import org.controlsfx.control.ToggleSwitch;

public class UrlBookMarkDialog {
    static boolean pageCorrectionEnded = true;

    public static void promptBookMarkForItemClass(String pageURL, CharDescriptionRow selectedItem, StringProperty browserUrlProperty) {
        Dialog dialog = new Dialog<>();
        dialog.setTitle("External PDF bookmark");
        dialog.setHeaderText("Bookmark this PDF?");
        dialog.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("customDialog");


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Label label = new Label("Page");
        label.setMinWidth(10);
        grid.add(label,0,0);
        TextField page = new TextField();
        page.setMinWidth(10);
        page.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(page.getText().chars().filter(c-> !Character.isDigit(c)).count()>0){
                    page.setText(page.getText().chars().filter(c-> Character.isDigit(c)).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString());
                }
            }
        });
        grid.add(page,1,0);

        ToggleSwitch scope = new ToggleSwitch();
        scope.setMinWidth(60);
        scope.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    scope.setText("This item only");
                }else{
                    scope.setText("The item class");
                }
            }
        });
        scope.setSelected(true);
        grid.add(scope,2,0);



        // Set the button types.
        ButtonType yesButton = new ButtonType("Bookmark", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Dismiss", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(yesButton,noButton);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(newConfig -> {
            System.out.println("Exiting bookmark");
            browserUrlProperty.setValue(null);
        });



    }
}
