package transversal.dialog_toolbox;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import model.*;
import service.CharItemFetcher;
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
	private static Label uom0Label;
	private static ComboBox<String> uom0;
	private static Label uom1Label;
	private static AutoCompleteBox_UnitOfMeasure uom1;
	private static Label uom2Label;
	private static AutoCompleteBox_UnitOfMeasure uom2;
	private static Label detailsLabel;
	private static CheckBox sequenceCB;
	private static CheckBox criticalityCB;
	private static CheckBox uom0CB;
	private static CheckBox uom1CB;
	private static CheckBox uom2CB;
	private static ButtonType validateButtonType;
	private static HashMap<String,HashMap<String,ArrayList<UnitOfMeasure>>> templateUoMs = new HashMap<String,HashMap<String,ArrayList<UnitOfMeasure>>>();

	private static void showDetailedClassClusters() {
		System.out.println("XXXXX SHOWING DETAILS XXXXXXX");
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Listing all impacted classes");
		dialog.setHeaderText(null);
		dialog.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		TableView<ClassSegmentClusterComboRow> tableview = new TableView<ClassSegmentClusterComboRow>();
		grid.add(tableview,0,0);
		dialog.showAndWait();
	}

	public static void CaracDeclarationPopUp(UserAccount account, ClassSegment itemSegment, Pair<ClassSegment, ClassCaracteristic> editingCarac) throws SQLException, ClassNotFoundException {
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
		Platform.runLater(() -> {
			charName.requestFocus();
			if(editingCarac!=null){
				charName.processSelectedCarac(editingCarac);
			}
		});
		
		
		// Convert the result to a uom when the store button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == validateButtonType) {
		    	return createCarac();
		    }
		    return null;
		});

		dialog.showAndWait();



	}

	private static ClassCaracteristic createCarac() {
		// TODO Auto-generated method stub
		return null;
	}

	private static void set_fields_behavior(Dialog<ClassCaracteristic> dialog, ButtonType validateButtonType, UserAccount account, ClassSegment itemSegment) throws SQLException, ClassNotFoundException {
		//Fill the carac name field and the template UoMs DS
		ArrayList<ClassCaracteristic> uniqueCharTemplate = new ArrayList<ClassCaracteristic>();
		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(account);
		CharValuesLoader.active_characteristics.entrySet().stream()
		.map(e-> new Pair<String,ArrayList<ClassCaracteristic>>
				(e.getKey(),
				new ArrayList<ClassCaracteristic>(e.getValue().stream().filter(
						c->!c.matchesTemplates(uniqueCharTemplate)).collect(Collectors.toList()))
				)
			).flatMap(p->p.getValue().stream().map(c->new Pair<ClassSegment,ClassCaracteristic>(sid2Segment.get(p.getKey()),c)))
				.forEach(p2->{
					//Add entry to charName combo
					charName.entries.add(p2);
					//Fill the template UoMs DS
					if(p2.getValue().getAllowedUoms()!=null && p2.getValue().getAllowedUoms().size()>0){
						ArrayList<UnitOfMeasure> UoMs = p2.getValue().getAllowedUoms().stream().map(uid -> UnitOfMeasure.RunTimeUOMS.get(uid)).collect(Collectors.toCollection(ArrayList::new));
						HashMap<String, ArrayList<UnitOfMeasure>> tmp = templateUoMs.get(p2.getValue().getCharacteristic_id());
						tmp = tmp!=null?tmp:new HashMap<String,ArrayList<UnitOfMeasure>>();
						tmp.put(String.join(",",UoMs.stream().map(u->u.getUom_symbol()).collect(Collectors.toList())),UoMs);
						templateUoMs.put(p2.getValue().getCharacteristic_id(),tmp);
					}
				});

		charName.incompleteProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				uom0.getItems().clear();
				uom0.getItems().add("No unit of measure");
				uom0.getItems().add("Other...");
				if(!newValue){
					ClassCaracteristic selectedCar = charName.selectedEntry.getValue();
					charNameTranslated.setText(selectedCar.getCharacteristic_name_translated());
					if(selectedCar.getIsNumeric()){
						charType.getSelectionModel().select("Numeric");
					}else{
						charType.getSelectionModel().select("Text");
						charTranslability.getSelectionModel().select(selectedCar.getIsTranslatable()?"Translatable":"Not translatable");
					}
					charClassLink.getSelectionModel().select(0);
					int previousmaxSeq = 0;
					try{
						previousmaxSeq = Collections.max(sequence.getItems());
					}catch(Exception V){

					}
					sequence.getItems().clear();
					sequence.getItems().addAll(IntStream.range(1,Math.max(selectedCar.getSequence(),previousmaxSeq)).boxed().collect(Collectors.toList()));
					sequence.getSelectionModel().select(selectedCar.getSequence());
					criticality.getSelectionModel().select(selectedCar.getIsCritical()?"Critical":"Not critical");
					//Use the template UoM DS to set the uom0 combobox
					try{
						Set<String> UomKS = templateUoMs.get(charName.selectedEntry.getValue().getCharacteristic_id()).keySet();
						uom0.getItems().addAll(UomKS);
						uom0.getSelectionModel().select(2);
					}catch (Exception V){
						uom0.getSelectionModel().select(0);
					}

				}else{
					charNameTranslated.clear();
					charType.getSelectionModel().clearSelection();
					charTranslability.getSelectionModel().clearSelection();
					charClassLink.getSelectionModel().clearSelection();
					sequence.getSelectionModel().clearSelection();
					criticality.getSelectionModel().clearSelection();
					uom0.getSelectionModel().select("Other...");
				}
			}
		});

		charNameTranslated.disableProperty().bind(charName.textProperty().length().isEqualTo(0));
		charType.disableProperty().bind(charNameTranslated.textProperty().length().isEqualTo(0).or(charName.incompleteProperty.not()));
		charTranslability.disableProperty().bind(charType.valueProperty().isNull().or(charName.incompleteProperty.not()));
		charClassLink.disableProperty().bind(charTranslability.valueProperty().isNull());
		sequence.disableProperty().bind(charClassLink.valueProperty().isNull());
		sequenceCB.disableProperty().bind(sequence.valueProperty().isNull());
		criticality.disableProperty().bind(charClassLink.valueProperty().isNull().or(sequence.valueProperty().isNull()));
		criticalityCB.disableProperty().bind(criticality.valueProperty().isNull());
		uom0.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom0CB.disableProperty().bind(uom0.valueProperty().isNull());
		uom1.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom1CB.disableProperty().bind(uom1.incompleteProperty);
		uom2.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom2CB.disableProperty().bind(uom2.incompleteProperty);

		uom0.visibleProperty().bind(charName.incompleteProperty.not().and(charType.valueProperty().isEqualTo("Numeric")).and(uom0.valueProperty().isEqualTo("Other...").not()));
		uom0CB.visibleProperty().bind(uom0.visibleProperty());
		uom0Label.visibleProperty().bind(uom0.visibleProperty());
		uom1.visibleProperty().bind(uom0.valueProperty().isEqualTo("Other...").and(charType.valueProperty().isEqualTo("Numeric")));
		uom1CB.visibleProperty().bind(uom1.visibleProperty());
		uom1Label.visibleProperty().bind(uom1.visibleProperty());
		uom2.visibleProperty().bind(uom0.valueProperty().isEqualTo("Other...").and(charType.valueProperty().isEqualTo("Numeric")));
		uom2CB.visibleProperty().bind(uom2.visibleProperty());
		uom2Label.visibleProperty().bind(uom2.visibleProperty());

		charType.getItems().add("Numeric");
		charType.getItems().add("Text");
		charType.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue!=null && newValue.equals("Numeric")){
					charTranslability.getItems().clear();
					charTranslability.getItems().add("N/A");
					charTranslability.getSelectionModel().select(0);

				}else{
					charTranslability.getItems().clear();
					charTranslability.getItems().add("Translatable");
					charTranslability.getItems().add("Not translatable");
					charTranslability.getSelectionModel().clearSelection();

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

		sequence.getItems().addAll(IntStream.range(1,CharValuesLoader.active_characteristics.get(itemSegment.getSegmentId()).size()+1).boxed().collect(Collectors.toList()));

		criticality.getItems().add("Critical");
		criticality.getItems().add("Not critical");

		uom0.getItems().add("No unit of measure");
		uom0.getItems().add("Other...");
		uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());
		uom2.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());

		uom1.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue && uom1.incompleteProperty.getValue() && uom1.isVisible() && uom1.getText().replace(" ","").length()>0){
					UoMDeclarationDialog.GenericUomDeclarationPopUp(uom1.getText(),uom1);
				}
			}
		});
		uom2.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue && uom2.incompleteProperty.getValue() &&  uom2.isVisible() && uom2.getText().replace(" ","").length()>0){
					UoMDeclarationDialog.GenericUomDeclarationPopUp(uom2.getText(),uom2);
				}
			}
		});

		detailsLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				showDetailedClassClusters();
			}
		});

		Button validationButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
		validationButton.disableProperty().bind(criticalityCB.disableProperty().or(uom0CB.disableProperty().and(uom0CB.visibleProperty())).or(uom1CB.disableProperty().and(uom1CB.visibleProperty())));
		validationButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClassCaracteristic newCarac = loadCaracFromDialog();
				dispatchCaracOnClasses(newCarac);
				dialog.close();
			}
		});


	}


	private static void dispatchCaracOnClasses(ClassCaracteristic newCarac) {
		charClassLink.getValue().getRowSegments().parallelStream().forEach(s->{
			Optional<ClassCaracteristic> charClassMatch = CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> c.getCharacteristic_id().equals(newCarac.getCharacteristic_id())).findAny();
			if(charClassMatch.isPresent()){
				ClassCaracteristic copy = newCarac;
				if(!sequenceCB.isSelected()){
					copy.setSequence(charClassMatch.get().getSequence());
				}else{
					CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> !c.getCharacteristic_id().equals(newCarac.getCharacteristic_id()))
							.forEach(c->{
								if(charClassMatch.get().getSequence()<=copy.getSequence()){
									//Advancing
									if(c.getSequence()>=charClassMatch.get().getSequence() && c.getSequence()<=copy.getSequence()){
										c.setSequence(c.getSequence()-1);
									}
								}else{
									//Regressing
									if(c.getSequence()<=charClassMatch.get().getSequence() && c.getSequence()>=copy.getSequence()){
										c.setSequence(c.getSequence()+1);
									}
								}
							});
				}
				if(!criticalityCB.isSelected()){
					copy.setIsCritical(charClassMatch.get().getIsCritical());
				}
				if(!uom0CB.isSelected() && uom1CB.isSelected()){
					copy.setAllowedUoms(charClassMatch.get().getAllowedUoms());
				}
				int matchIndx = CharValuesLoader.active_characteristics.get(s.getSegmentId()).indexOf(charClassMatch);
				CharValuesLoader.active_characteristics.get(s.getSegmentId()).set(matchIndx,copy);
			}else{
				//The carac is not present insert
				CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> c.getSequence()>=newCarac.getSequence()).forEach(c->c.setSequence(c.getSequence()+1));
				CharValuesLoader.active_characteristics.get(s.getSegmentId()).add(newCarac);
				CharItemFetcher.allRowItems.stream().filter(r->r.getClass_segment_string().startsWith(s.getSegmentId())).forEach(r->{
					r.expandDataField(s.getSegmentId());
				});
			}
		});
	}

	private static ClassCaracteristic loadCaracFromDialog() {
		ClassCaracteristic newCarac = new ClassCaracteristic();
		ArrayList<String> newUoms = new ArrayList<String>();
		if(charName.incompleteProperty.getValue()){
			//New carac
			newCarac = new ClassCaracteristic();
			newCarac.setCharacteristic_id(Tools.generate_uuid());
			newCarac.setCharacteristic_name(charName.getText());
		}else{
			//Old carac
			newCarac = charName.selectedEntry.getValue();
			if(uom0.isVisible()){
				ArrayList<UnitOfMeasure> tmpUom = templateUoMs.get(newCarac.getCharacteristic_id()).get(uom0.getValue());
				if(tmpUom!=null){
					newUoms.addAll(tmpUom.stream().map(UnitOfMeasure::getUom_id).collect(Collectors.toCollection(ArrayList::new)));
				}
			}
		}
		newCarac.setCharacteristic_name_translated(charNameTranslated.getText());
		newCarac.setIsNumeric(charType.getValue().equals("Numeric"));
		newCarac.setIsTranslatable(charTranslability.getValue().equals("Translatable"));
		newCarac.setSequence(sequence.getValue());
		newCarac.setIsCritical(criticality.getValue().equals("Critical"));
		if(uom1.isVisible() && !uom1.incompleteProperty.getValue()){
			newUoms.add(uom1.selectedUom.getUom_id());
		}
		if(uom2.isVisible() && !uom2.incompleteProperty.getValue()){
			newUoms.add(uom2.selectedUom.getUom_id());
		}
		newCarac.setAllowedUoms(newUoms);

		return newCarac;
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
		uom0 = new ComboBox<String>();
		uom0.setMaxWidth(Integer.MAX_VALUE);
		uom1 = new AutoCompleteBox_UnitOfMeasure();
		uom1.setMaxWidth(Integer.MAX_VALUE);
		uom2 = new AutoCompleteBox_UnitOfMeasure();
		uom2.setMaxWidth(Integer.MAX_VALUE);
		
		
		detailsLabel = new Label("View details...");
		detailsLabel.setUnderline(true);
		detailsLabel.setTextAlignment(TextAlignment.CENTER);
		detailsLabel.setFont(Font.font(detailsLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, detailsLabel.getFont().getSize()));
		uom0Label = new Label("Main units of measure");
		uom1Label = new Label("Prefered unit of measure");
		uom2Label = new Label("Alternative unit of measure");
		grid.setHalignment(detailsLabel, HPos.CENTER);
		
		sequenceCB = new CheckBox();
		grid.setHalignment(sequenceCB, HPos.CENTER);
		criticalityCB = new CheckBox();
		grid.setHalignment(criticalityCB, HPos.CENTER);
		uom0CB = new CheckBox();
		grid.setHalignment(uom0CB,HPos.CENTER);
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
		grid.add(uom0Label, 1, 13);
		grid.add(uom1Label, 1, 13);
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
		grid.add(uom0, 3, 13);
		grid.setHgrow(uom0, Priority.ALWAYS);
		grid.add(uom1, 3, 13);
		grid.setHgrow(uom1, Priority.ALWAYS);
		grid.add(uom2, 3, 14);
		grid.setHgrow(uom2, Priority.ALWAYS);
		
		grid.add(sequenceCB, 5, 11);
		grid.add(criticalityCB, 5, 12);
		grid.add(uom0CB, 5, 13);
		grid.add(uom1CB, 5, 13);
		grid.add(uom2CB, 5, 14);
	}

	public static void CaracEditionPopUp(ClassCaracteristic carac, UserAccount account, TableView<CharDescriptionRow> tableGrid) throws SQLException, ClassNotFoundException {
		ClassSegment itemSegement = Tools.get_project_segments(account).get(tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]);
		Pair<ClassSegment, ClassCaracteristic> entry = new Pair<ClassSegment, ClassCaracteristic>(itemSegement, carac);
		CaracDeclarationDialog.CaracDeclarationPopUp(account, itemSegement,entry);


	}

	public static void CaracDeletion(ClassCaracteristic carac) {
	}
}
