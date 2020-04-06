package transversal.dialog_toolbox;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionDialog {
	
	public static void show(String title,String header,String content) {
		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		Exception ex = new FileNotFoundException(content);
	
		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();
	
		Label label = new Label("Kernel responded:");
	
		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
	
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
	
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
	
		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
	
		alert.showAndWait();
	}
	
	public static void unable_to_connect() {
		show("Connection issue","Connection issue","Connection issue");
	}
	public static void wrong_credential() {
		show("Wrong credentials","Wrong credentials","Wrong credentials");
	}

	public static void no_selected_project() {
		show("No active project","No active project","Please activate a project first using the radio button in the project set-up screen");
	}
	public static void empty_project_selected() {
		show("Unable to activate this project","Unable to activate this project","The current project is not complete and thus can not be activated");
	}

}
