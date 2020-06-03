package transversal.dialog_toolbox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import model.*;
import service.CharValuesLoader;
import transversal.generic.Tools;

public class CaracDeclarationDialog {

	private static Dialog<ClassCaracteristic>  dialog;
	private static GridPane grid;
	private static AutoCompleteBox_CharDeclarationName charName;
	private static TextField charNameTranslated;
	private static ComboBox<String> charType;
	private static ComboBox<String> charTranslability;
	private static ComboBox<ClassSegmentClusterComboRow> charClassLink;
	private static ComboBox<Integer> sequence;
	private static ComboBox<String> criticality;
	private static Label uom1Label;
	private static AutoCompleteBox_UnitOfMeasure uom1;
	private static Label uom2Label;
	private static AutoCompleteBox_UnitOfMeasure uom2;
	private static Label detailsLabel;
	private static CheckBox sequenceCB;
	private static CheckBox criticalityCB;
	private static CheckBox uom1CB;
	private static CheckBox uom2CB;
	private static ButtonType validateButtonType;
	
	@SuppressWarnings("static-access")
	public static void CaracDeclarationPopUp(UserAccount account, ClassSegment itemSegment) throws SQLException, ClassNotFoundException {
		// Create the custom dialog.
		create_dialog();
		
		// Create the carac labels and fields.
		create_dialog_fields();
				
		// Set fields layout
		set_fields_layout();
				
		//Set fields behavior
		set_fields_behavior(dialog,validateButtonType,account,itemSegment);
				
				
		dialog.getDialogPane().setContent(grid);

		// Request focus on the char name by default.
		Platform.runLater(() -> charName.requestFocus());
		
		
		// Convert the result to a uom when the store button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == validateButtonType) {
		    	return createCarac();
		    }
		    return null;
		});

		Optional<ClassCaracteristic> result = dialog.showAndWait();

		result.ifPresent(carac -> {
			carac.setCharacteristic_id(Tools.generate_uuid());
		});
		

	}

	private static ClassCaracteristic createCarac() {
		// TODO Auto-generated method stub
		return null;
	}

	private static void set_fields_behavior(Dialog<ClassCaracteristic> dialog, ButtonType validateButtonType, UserAccount account, ClassSegment itemSegment) throws SQLException, ClassNotFoundException {
		//Fill the carac name field
		ArrayList<ClassCaracteristic> uniqueCharTemplate = new ArrayList<ClassCaracteristic>();
		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(account);
		CharValuesLoader.active_characteristics.entrySet().stream()
		.map(e-> new Pair<String,ArrayList<ClassCaracteristic>>
				(e.getKey(),
				new ArrayList<ClassCaracteristic>(e.getValue().stream().filter(
						c->!c.matchesTemplates(uniqueCharTemplate)).collect(Collectors.toList()))
				)
			).flatMap(p->p.getValue().stream().map(c->new Pair<ClassSegment,ClassCaracteristic>(sid2Segment.get(p.getKey()),c)))
				.forEach(p2->charName.entries.add(p2));
			//.forEach(p2->System.out.println(p2.getValue().getCharacteristic_name()+"["+p2.getKey().getClassName()+"]"));

		charNameTranslated.disableProperty().bind(charName.textProperty().length().isEqualTo(0));
		charType.disableProperty().bind(charNameTranslated.textProperty().length().isEqualTo(0));
		charTranslability.disableProperty().bind(charType.valueProperty().isNull());
		charClassLink.disableProperty().bind(charTranslability.valueProperty().isNull());
		sequence.disableProperty().bind(charClassLink.valueProperty().isNull());
		sequenceCB.disableProperty().bind(sequence.valueProperty().isNull());
		criticality.disableProperty().bind(charClassLink.valueProperty().isNull());
		criticalityCB.disableProperty().bind(criticality.valueProperty().isNull());
		uom1.disableProperty().bind(charClassLink.valueProperty().isNull());
		uom1CB.disableProperty().bind(uom1.textProperty().length().isEqualTo(0));
		uom2.disableProperty().bind(charClassLink.valueProperty().isNull());
		uom2CB.disableProperty().bind(uom2.textProperty().length().isEqualTo(0));
		uom1.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));
		uom1CB.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));
		uom1Label.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));
		uom2.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));
		uom2CB.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));
		uom2Label.visibleProperty().bind(charType.valueProperty().isEqualTo("Numeric"));

		charType.getItems().add("Numeric");
		charType.getItems().add("Text");
		charType.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue.equals("Numeric")){
					charTranslability.getItems().clear();
					charTranslability.getItems().add("N/A");
					charTranslability.getSelectionModel().select(0);
					charTranslability.setDisable(true);
				}else{
					charTranslability.getItems().clear();
					charTranslability.getItems().add("Translatable");
					charTranslability.getItems().add("Not translatable");
					charTranslability.getSelectionModel().clearSelection();
					charTranslability.setDisable(false);
				}
			}
		});
		IntStream.range(0,itemSegment.getSegmentGranularity()).forEach(lvl->{
			ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(lvl, itemSegment,sid2Segment);
			charClassLink.getItems().add(cc);
		});
		Collections.reverse(charClassLink.getItems());
		ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment);
		charClassLink.getItems().add(cc);



	}

	private static void create_dialog() {
		dialog = new Dialog<>();
		dialog.setTitle("New class characteristic declaration");
		dialog.setHeaderText(null);
		dialog.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");
		
		// Set the button types.
		validateButtonType = new ButtonType("Apply", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

	}
	@SuppressWarnings("static-access")
	private static void create_dialog_fields() {
		grid = new GridPane();
		charName = new AutoCompleteBox_CharDeclarationName();
		charNameTranslated = new TextField();
		charType = new ComboBox<String>();
		charType.setMaxWidth(Integer.MAX_VALUE);
		charTranslability = new ComboBox<String>();
		charTranslability.setMaxWidth(Integer.MAX_VALUE);
		charClassLink = new ComboBox<ClassSegmentClusterComboRow>();
		charClassLink.setMaxWidth(Integer.MAX_VALUE);
		
		sequence = new ComboBox<Integer>();
		sequence.setMaxWidth(Integer.MAX_VALUE);
		criticality = new ComboBox<String>();
		criticality.setMaxWidth(Integer.MAX_VALUE);
		uom1 = new AutoCompleteBox_UnitOfMeasure();
		uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());
		uom1.setMaxWidth(Integer.MAX_VALUE);
		uom2 = new AutoCompleteBox_UnitOfMeasure();
		uom2.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());
		uom2.setMaxWidth(Integer.MAX_VALUE);
		
		
		detailsLabel = new Label("View details...");
		detailsLabel.setUnderline(true);
		detailsLabel.setTextAlignment(TextAlignment.CENTER);
		detailsLabel.setFont(Font.font(detailsLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, detailsLabel.getFont().getSize()));
		grid.setHalignment(detailsLabel, HPos.CENTER);
		
		sequenceCB = new CheckBox();
		grid.setHalignment(sequenceCB, HPos.CENTER);
		criticalityCB = new CheckBox();
		grid.setHalignment(criticalityCB, HPos.CENTER);
		uom1CB = new CheckBox();
		grid.setHalignment(uom1CB, HPos.CENTER);
		uom2CB = new CheckBox();
		grid.setHalignment(uom2CB, HPos.CENTER);
	}

	


	@SuppressWarnings("static-access")
	private static void set_fields_layout() {
		// TODO Auto-generated method stub
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		ColumnConstraints col0 = new ColumnConstraints();
	    col0.setPercentWidth(0);
	    col0.setFillWidth(true);
		ColumnConstraints col1 = new ColumnConstraints();
	    col1.setPercentWidth(30);
	    col1.setFillWidth(true);
	    ColumnConstraints col2 = new ColumnConstraints();
	    col2.setPercentWidth(2);
	    col2.setFillWidth(true);
	    ColumnConstraints col3 = new ColumnConstraints();
	    col3.setPercentWidth(40);
	    col3.setFillWidth(true);
	    ColumnConstraints col4 = new ColumnConstraints();
	    col4.setPercentWidth(3);
	    col4.setFillWidth(true);
	    ColumnConstraints col5 = new ColumnConstraints();
	    col5.setPercentWidth(25);
	    col5.setFillWidth(true);
	    ColumnConstraints col6 = new ColumnConstraints();
	    col6.setPercentWidth(0);
	    col6.setFillWidth(true);
	    
	    
	    grid.getColumnConstraints().addAll(col0,col1,col2,col3,col4,col5,col6);
	    
		grid.getChildren().clear();
		Label headerLabel1 = new Label("General characteristic attributes");
		headerLabel1.setUnderline(true);
		grid.add(headerLabel1, 1, 0);
		
		grid.add(new Label("Characteristic name"), 1, 2);
		grid.add(new Label("Characteristic name translated"), 1, 3);
		grid.add(new Label("Characteristic type"), 1, 4);
		grid.add(new Label("Translation type"), 1, 5);
		grid.add(new Label("Link this characteristic with"), 1, 6);
		
		Label headerLabel2 = new Label("Characteristic attributes at class level");
		headerLabel2.setUnderline(true);
		grid.add(headerLabel2, 1, 9);
		Label headerLabel3 = new Label("Update existing parameters for other classes");
		headerLabel3.setWrapText(true);
		headerLabel3.setFont(Font.font(headerLabel3.getFont().getName(), FontWeight.LIGHT, FontPosture.REGULAR, headerLabel3.getFont().getSize()));
		headerLabel3.setTextAlignment(TextAlignment.CENTER);
		grid.setHalignment(headerLabel3, HPos.CENTER);
		
		grid.add(headerLabel3, 5, 9);
		
		grid.add(new Label("Sequence"), 1, 11);
		grid.add(new Label("Criticality"), 1, 12);
		uom1Label = new Label("Prefered unit of measure");
		grid.add(uom1Label, 1, 13);
		uom2Label = new Label("Alternative unit of measure");
		grid.add(uom2Label, 1, 14);
		
		
		grid.add(charName, 3, 2);
		grid.add(charNameTranslated, 3, 3);
		grid.add(charType, 3, 4);
		grid.setHgrow(charType, Priority.ALWAYS);
		grid.add(charTranslability, 3, 5);
		grid.setHgrow(charTranslability, Priority.ALWAYS);
		grid.add(charClassLink, 3, 6);
		grid.setHgrow(charClassLink, Priority.ALWAYS);
		
		grid.add(detailsLabel, 5, 6);
		
		
		grid.add(sequence, 3, 11);
		grid.setHgrow(sequence, Priority.ALWAYS);
		grid.add(criticality, 3, 12);
		grid.setHgrow(criticality, Priority.ALWAYS);
		grid.add(uom1, 3, 13);
		grid.setHgrow(uom1, Priority.ALWAYS);
		grid.add(uom2, 3, 14);
		grid.setHgrow(uom2, Priority.ALWAYS);
		
		grid.add(sequenceCB, 5, 11);
		grid.add(criticalityCB, 5, 12);
		grid.add(uom1CB, 5, 13);
		grid.add(uom2CB, 5, 14);
	}

}
