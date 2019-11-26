package transversal.dialog_toolbox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.UnitOfMeasure;
import model.UomClassComboRow;
import transversal.generic.Tools;
public class UoMDeclarationDialog {
	
	
	public static void show(String title, String header, String ok) {
		Alert alert = null;
		if(title.contains("failed")) {
			alert =new Alert(AlertType.ERROR);
		}else {
			alert =new Alert(AlertType.INFORMATION);
		}
		alert.setTitle(title);
		alert.setHeaderText(header);
		
		ButtonType yesButton = new ButtonType(ok);
		
        alert.getButtonTypes().setAll(yesButton);
	
		alert.showAndWait();
		
	}

	public static void UomConversionPopUp(UnitOfMeasure following_uom, ArrayList<String> allowedUoms,
			Char_description parent) {
		System.out.println(":::: Display a pop-up asking to enter conversion rate towards the declared UoM (mandatory)\r\n" + 
				" ::::");
		
		
		/*
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Unknown conversion rate");
		dialog.setHeaderText("Conversion formula from "+following_uom.getUom_name()+"("+following_uom.getUom_symbol()+") to "+UnitOfMeasure.RunTimeUOMS.get(allowedUoms.get(0)).getUom_name()+"("+UnitOfMeasure.RunTimeUOMS.get(allowedUoms.get(0)).getUom_symbol()+")");
		dialog.setContentText("1 "+following_uom.getUom_symbol()+"= X * "+UnitOfMeasure.RunTimeUOMS.get(allowedUoms.get(0)).getUom_symbol()+"; X=");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    System.out.println("Conversion multiplier: " + result.get());
		}
		*/
	}

	public static void UomDeclarationPopUp(Char_description parent, String proposedUomSymbol, CharacteristicValue preparedValue, String preparedRule,
			ClassCharacteristic active_char) {
		// Create the custom dialog.
		Dialog<UnitOfMeasure> dialog = new Dialog<>();
		dialog.setTitle("New unit of measure declaration");
		dialog.setHeaderText("Defining a new unit of measure");

		// Set the button types.
		ButtonType validateButtonType = new ButtonType("Store new unit", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

		// Create the  uom labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField uomName = new TextField();
		uomName.setPromptText("");
		TextField uomSymbol = new TextField();
		uomSymbol.setText(proposedUomSymbol);
		TextField uomAlt = new TextField();
		TextField uomMultiplier = new TextField();
		uomMultiplier.setPromptText("");
		ComboBox<UomClassComboRow> uomChoice = new ComboBox<UomClassComboRow>();
		uomChoice.getItems().addAll(UnitOfMeasure.RunTimeUOMS.values().stream()
				.filter(u -> UnitOfMeasure.ConversionPathExists(u, active_char.getAllowedUoms()))
				.map(u->new UomClassComboRow(u)).collect(Collectors.toList()));
		
		grid.add(new Label("Unit's name (in english):"), 0, 0);
		grid.add(uomName, 1, 0);
		grid.add(new Label("Unit's display symbol"), 0, 1);
		grid.add(uomSymbol, 1, 1);
		grid.add(new Label("Unit's alternative writings, separate with \",\":"), 0, 2);
		grid.add(uomAlt, 1, 2);
		grid.add(new Label("Unit's multiplier: "), 0, 3);
		grid.add(uomMultiplier, 1, 3);
		grid.add(new Label("Unit's base"), 0, 4);
		grid.add(uomChoice, 1, 4);
		uomChoice.getSelectionModel().select(0);
		
		
		// Enable/Disable validation button depending on whether all fields were entered.
		Node validationButton = dialog.getDialogPane().lookupButton(validateButtonType);
		validationButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		uomName.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				Double.valueOf(uomMultiplier.getText().replace(",", "."));
			}catch(Exception V) {
			    validationButton.setDisable(true);
			    return;
			}
			validationButton.setDisable(uomName.getText().trim().isEmpty() || uomSymbol.getText().split(",")[0].trim().isEmpty());
		});
		uomSymbol.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				Double.valueOf(uomMultiplier.getText().replace(",", "."));
			}catch(Exception V) {
			    validationButton.setDisable(true);
			    return;
			}
			validationButton.setDisable(uomName.getText().trim().isEmpty() || uomSymbol.getText().split(",")[0].trim().isEmpty());
		});
		uomMultiplier.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				Double.valueOf(uomMultiplier.getText().replace(",", "."));
			}catch(Exception V) {
			    validationButton.setDisable(true);
			    return;
			}
			validationButton.setDisable(uomName.getText().trim().isEmpty() || uomSymbol.getText().split(",")[0].trim().isEmpty());
		});
		
		
		
		

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> uomName.requestFocus());

		// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == validateButtonType) {
		    	UnitOfMeasure newUom = new UnitOfMeasure();
		    	newUom.setUom_id(Tools.generate_uuid());
		    	newUom.setUom_name(uomName.getText());
		    	newUom.setUom_symbols(uomSymbol.getText().split(","));
		    	newUom.setUom_base_id(uomChoice.getSelectionModel().getSelectedItem().getUnitOfMeasure().getUom_base_id());
		    	newUom.setUom_multiplier(uomChoice.getSelectionModel().getSelectedItem().getUnitOfMeasure()
		    			.getUom_multiplier().multiply(
		    			new BigDecimal(Double.valueOf(uomMultiplier.getText().replace(",", ".")))).toString());
		        return newUom;
		    }
		    return null;
		});

		Optional<UnitOfMeasure> result = dialog.showAndWait();

		result.ifPresent(newUom -> {
			System.out.println(newUom.getUom_id());
		    System.out.println(newUom.getUom_name());
		    preparedValue.setUom_id(newUom.getUom_id());
		    parent.sendPatternValue(preparedValue);
		    parent.sendPatternRule(preparedRule.replace("$$$UOM_SYMBOL$$$", newUom.getUom_symbol()));
		});
		
		
	}

	
		
}

