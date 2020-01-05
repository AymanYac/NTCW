package transversal.dialog_toolbox;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import service.CharClassifProposer;
import transversal.generic.Tools;
public class UoMDeclarationDialog {
	
	
	
	private static TextField uomName;
	private static TextField uomSymbol;
	private static TextField uomAlt;
	private static TextField uomMultiplier;
	private static ComboBox<UomClassComboRow> uomChoice;
	private static GridPane grid;
	private static Node validationButton;
	private static String CharUomFamily = "";
	

	public static void UomDeclarationPopUp(Char_description parent, String proposedUomSymbol, int activeButtonIndex,
			ClassCharacteristic active_char) {
		CharUomFamily = UnitOfMeasure.RunTimeUOMS.get(active_char.getAllowedUoms().get(0)).getUom_base_id();
		
		//Get the corresponding rule and value
		CharacteristicValue preparedValue=CharClassifProposer.getValueForButton(activeButtonIndex);
		String preparedRule=CharClassifProposer.getRuleForButton(activeButtonIndex);
		
		// Create the custom dialog.
		Dialog<UnitOfMeasure> dialog = new Dialog<>();
		dialog.setTitle("New unit of measure declaration");
		dialog.setHeaderText("Defining a new unit of measure");

		// Set the button types.
		ButtonType validateButtonType = new ButtonType("Store new unit", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

		// Create the  uom labels and fields.

		grid = new GridPane();
		uomName = new TextField();
		uomSymbol = new TextField();
		uomAlt = new TextField();
		uomMultiplier = new TextField();
		uomChoice = new ComboBox<UomClassComboRow>();

		clear_fields(proposedUomSymbol, active_char);
		
		setFieldListeners(dialog,validateButtonType, proposedUomSymbol, active_char);
		
		
		dialog.getDialogPane().setContent(grid);

		// Request focus on the multiplier field by default.
		Platform.runLater(() -> uomMultiplier.requestFocus());
		
		
		// Convert the result to a uom when the store button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == validateButtonType) {
		    	return createUomfromField();
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

	private static void setFieldListeners(Dialog<UnitOfMeasure> dialog, ButtonType validateButtonType,String proposedUomSymbol,ClassCharacteristic active_char) {
		// Enable/Disable validation button depending on whether all fields were entered.
		validationButton = dialog.getDialogPane().lookupButton(validateButtonType);
		validationButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		uomName.textProperty().addListener((observable, oldValue, newValue) -> {
			check_validation_disable();
			
			});
		uomSymbol.textProperty().addListener((observable, oldValue, newValue) -> {
			check_validation_disable();
			
			});
		uomMultiplier.textProperty().addListener((observable, oldValue, newValue) -> {
			check_validation_disable();
			});
		
		uomMultiplier.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    focusState(newValue, proposedUomSymbol, active_char, dialog, validateButtonType);
		});
		
		uomAlt.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			check_name_and_symbol_in_alts();
		});
		
		uomAlt.textProperty().addListener((observable, oldValue, newValue) -> {
			remove_dupp_alts();
			});
		
	}

	private static void remove_dupp_alts() {
		uomAlt.setText(  String.join(",", Arrays.stream( uomAlt.getText().split(",") ).collect(Collectors.toCollection(LinkedHashSet::new))));
		
	}

	private static void check_name_and_symbol_in_alts() {
		if(uomName.isFocused()||uomSymbol.isFocused()) {
			return;
		}
		String name = uomName.getText();
		String symb = uomSymbol.getText();
		name = name.length()>0?name:null;
		symb = symb.length()>0?symb:null;
		
		if(uomAlt.getText().startsWith((name!=null?name+",":"")+(symb!=null?symb+",":""))) {
			
		}else {
			uomAlt.setText((name!=null?name+",":"")+(symb!=null?symb+",":"")+uomAlt.getText());
		}
		
	}

	private static void focusState(Boolean uomMultiplierInFocus, String proposedUomSymbol, ClassCharacteristic active_char, Dialog<UnitOfMeasure> dialog, ButtonType validateButtonType) {
		if (uomMultiplierInFocus) {
	        System.out.println("Focus Gained");
	    }
	    else {
	        System.out.println("Focus Lost");
	        try {
				double inputMultiplier = Double.valueOf(uomMultiplier.getText().replace(",", "."));
				UnitOfMeasure inputBaseUom = uomChoice.getValue().getUnitOfMeasure();
				double inputMultiplierToBase = inputMultiplier * inputBaseUom.getUom_multiplier().doubleValue();
				UnitOfMeasure matchedUom = UnitOfMeasure.CheckIfMultiplierIsKnown(inputMultiplierToBase,inputBaseUom.getUom_base_id());
				if(matchedUom!=null) {
					//Matched uom
					fill_fields(matchedUom,proposedUomSymbol);
				}else {
					//No uom match
					clear_fields(proposedUomSymbol, active_char);
				}
			}catch(Exception V) {
				try {
					double inputMultiplier = Double.valueOf(uomMultiplier.getText().replace(",", ".").split("/")[0])/
					Double.valueOf(uomMultiplier.getText().replace(",", ".").split("/")[1]);
					UnitOfMeasure inputBaseUom = uomChoice.getValue().getUnitOfMeasure();
					double inputMultiplierToBase = inputMultiplier * inputBaseUom.getUom_multiplier().doubleValue();
					UnitOfMeasure matchedUom = UnitOfMeasure.CheckIfMultiplierIsKnown(inputMultiplierToBase,inputBaseUom.getUom_base_id());
					if(matchedUom!=null) {
						//Matched uom
						fill_fields(matchedUom,proposedUomSymbol);
					}else {
						//No uom match
						clear_fields(proposedUomSymbol, active_char);
					}
				}catch(Exception G) {
					
				}
			}
	    }
	}

	private static void fill_fields(UnitOfMeasure matchedUom,String proposedUomSymbol) {
		uomName.setText(matchedUom.getUom_name());
		uomSymbol.setText(matchedUom.getUom_symbol());
		uomAlt.setText(matchedUom.getUom_name()+","+String.join(",", matchedUom.getUom_symbols())+","+proposedUomSymbol);
		//uomMultiplier.setText(String.valueOf( matchedUom.getUom_multiplier().doubleValue() ));
		uomMultiplier.setText("1.0");
		selectUomInComboBoxByUomId(matchedUom.getUom_id());
		check_name_and_symbol_in_alts();
	}

	private static void selectUomInComboBoxByUomId(String targetID) {
		for(int i=0;i<uomChoice.getItems().size();i++) {
			if( uomChoice.getItems().get(i).getUnitOfMeasure().getUom_id().equals(targetID) ) {
				uomChoice.getSelectionModel().select(i);
				return;
			}
		}
	}

	private static void clear_fields(String proposedUomSymbol,ClassCharacteristic active_char) {
		System.out.println("clearing fields");
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		uomName.setText("");
		uomSymbol.setText(proposedUomSymbol);
		//uomMultiplier.setText("");
		uomChoice.getItems().addAll(UnitOfMeasure.RunTimeUOMS.values().stream()
				.filter(u -> UnitOfMeasure.ConversionPathExists(u, active_char.getAllowedUoms()))
				.map(u->new UomClassComboRow(u)).collect(Collectors.toSet()));
		uomAlt.setText("");
		
		grid.getChildren().clear();
		grid.add(new Label("1 "+proposedUomSymbol+" ="), 0, 0);
		grid.add(uomMultiplier, 1, 0);
		grid.add(uomChoice, 2, 0);
		
		grid.add(new Label("Unit's symbol"), 0, 1);
		grid.add(uomSymbol, 1, 1);
		GridPane.setColumnSpan(uomSymbol, 3);
		
		grid.add(new Label("Unit's name (in english):"), 0, 2);
		grid.add(uomName, 1, 2);
		GridPane.setColumnSpan(uomName, 3);
		
		grid.add(new Label("Unit's alternative forms, separate with \",\":"), 0, 3);
		grid.add(uomAlt, 1, 3);
		GridPane.setColumnSpan(uomAlt, 3);
		
		sortUomChoiceList();
		selectUomInComboBoxByUomId(active_char.getAllowedUoms().get(0));
		check_name_and_symbol_in_alts();
	}

	private static void sortUomChoiceList() {
		uomChoice.getItems().sorted(new Comparator<UomClassComboRow>(){

			@Override
			public int compare(UomClassComboRow arg0, UomClassComboRow arg1) {
				// TODO Auto-generated method stub
				return arg1.getUnitOfMeasure().getUom_name().compareToIgnoreCase(
						arg0.getUnitOfMeasure().getUom_name())
						+ (
							(arg0.getUnitOfMeasure().getUom_base_id().equals(CharUomFamily)
							&& arg1.getUnitOfMeasure().getUom_base_id().equals(CharUomFamily)
							)?500:0);
			}
			
		});
	}

	private static UnitOfMeasure createUomfromField() {
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

	private static void check_validation_disable() {
		Boolean falseNumberFormat=false;
		try {
			Double.valueOf(uomMultiplier.getText().replace(",", "."));
		}catch(Exception V) {
			try {
				@SuppressWarnings("unused")
				double tmp = Double.valueOf(uomMultiplier.getText().replace(",", ".").split("/")[0])/
				Double.valueOf(uomMultiplier.getText().replace(",", ".").split("/")[1]);
			}catch(Exception G) {
				falseNumberFormat=true;
			}
			
		}
		validationButton.setDisable(falseNumberFormat || uomName.getText().trim().isEmpty() || uomSymbol.getText().split(",")[0].trim().isEmpty());
	
	}


	
		
}

