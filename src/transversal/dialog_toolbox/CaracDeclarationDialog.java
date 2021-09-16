package transversal.dialog_toolbox;

import com.google.gson.internal.LinkedHashTreeMap;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import model.*;
import org.apache.commons.lang3.StringUtils;
import service.CharItemFetcher;
import service.CharPatternServices;
import service.CharValuesLoader;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static transversal.data_exchange_toolbox.CharDescriptionExportServices.*;

public class CaracDeclarationDialog {

	private static Dialog<ClassCaracteristic>  dialog;
	private static GridPane grid;
	private static AutoCompleteBox_CharDeclarationName charName;
	private static Label searchLabel;
	private static TextField charNameTranslated;
	private static ComboBox<String> charType;
	private static ComboBox<String> charTranslability;
	private static ComboBox<ClassSegmentClusterComboRow> charClassLink;
	private static ComboBox<ComboPair<Integer,String>> sequence;
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
	private static Unidecode unidecode;


	private static void showDetailedClassClusters(ClassSegment itemSegment) {
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Listing all impacted classes");
		dialog.setHeaderText(null);
		dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		TableView<Pair<ClassSegment,SimpleBooleanProperty>> tableview = new TableView<Pair<ClassSegment, SimpleBooleanProperty>>();

		TableColumn col1 = new TableColumn("Class ID");
		col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, String> r) {
				return new ReadOnlyObjectWrapper(r.getValue().getKey().getClassNumber());
			}
		});
		col1.setResizable(false);
		col1.prefWidthProperty().bind(tableview.widthProperty().multiply(35 / 100.0));

		TableColumn col2 = new TableColumn("Class name");
		col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, String> r) {
				return new ReadOnlyObjectWrapper(r.getValue().getKey().getClassName());
			}
		});
		col2.setResizable(false);
		col2.prefWidthProperty().bind(tableview.widthProperty().multiply(35 / 100.0));

		TableColumn col3 = new TableColumn("Active characteristic");
		col3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, CheckBox>, ObservableValue<CheckBox>>() {
			public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<Pair<ClassSegment,SimpleBooleanProperty>, CheckBox> r) {
				CheckBox cb = new CheckBox();
				cb.setDisable(r.getValue().getKey().equals(itemSegment));
				cb.selectedProperty().bindBidirectional(r.getValue().getValue());
				cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						int activeCount = (int) charClassLink.getValue().getRowSegments().stream().filter(p->p.getValue().getValue()).count();
						if(activeCount==charClassLink.getValue().getRowSegments().size()){
							//All cluster classes are active
							Optional<ClassSegmentClusterComboRow> fullCluster = charClassLink.getItems().stream().filter(r -> r.getRowSegments().stream().map(p -> p.getKey()).collect(Collectors.toCollection(ArrayList::new)).equals(charClassLink.getValue().getRowSegments().stream().map(p -> p.getKey()).collect(Collectors.toCollection(ArrayList::new)))).findAny();
							ArrayList<ClassSegmentClusterComboRow> pureClusters = charClassLink.getItems().stream().filter(r -> !r.toString().endsWith("(ies)")).collect(Collectors.toCollection(ArrayList::new));
							charClassLink.getItems().clear();
							charClassLink.getItems().addAll(pureClusters);
							fullCluster.ifPresent(classSegmentClusterComboRow -> charClassLink.getSelectionModel().select(classSegmentClusterComboRow));
						}else if(activeCount>1){
							//Cluster has been edited and at least one other class is active
							ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow("This and "+String.valueOf(activeCount-1)+" other category(ies)",charClassLink.getValue().getRowSegments());
							charClassLink.getItems().add(cc);
							charClassLink.getSelectionModel().select(cc);
						}else{
							//Only the current class is active
							ArrayList<ClassSegmentClusterComboRow> pureClusters = charClassLink.getItems().stream().filter(r -> !r.toString().endsWith("(ies)")).collect(Collectors.toCollection(ArrayList::new));
							charClassLink.getItems().clear();
							charClassLink.getItems().addAll(pureClusters);
							Optional<ClassSegmentClusterComboRow> classOnlyRow = charClassLink.getItems().stream().filter(r -> r.toString().equals("This category only")).findAny();
							if(classOnlyRow.isPresent()){
								charClassLink.getSelectionModel().select(classOnlyRow.get());
							}
						}
					}
				});
				return new ReadOnlyObjectWrapper(cb);
			}
		});
		col3.setResizable(false);
		col3.prefWidthProperty().bind(tableview.widthProperty().multiply(27 / 100.0));

		tableview.getColumns().add(col1);
		tableview.getColumns().add(col2);
		tableview.getColumns().add(col3);

		//IntStream.range(0,20).forEach(idx->tableview.getItems().addAll(charClassLink.getValue().getRowSegments()));
		tableview.getItems().addAll(charClassLink.getValue().getRowSegments());

		grid.add(tableview,0,0);
		tableview.setMinWidth(800);
		GridPane.setColumnSpan(tableview,GridPane.REMAINING);

		Button sab = new Button("Select All");
		sab.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				tableview.getItems().stream().forEach(r->r.getValue().set(true));
			}
		});
		Button uab = new Button("Deselect All");
		uab.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				tableview.getItems().stream().filter(r->!r.getKey().getSegmentId().equals(itemSegment.getSegmentId())).forEach(r->r.getValue().set(false));
			}
		});
		grid.add(sab,1,1);
		grid.add(uab,2,1);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(80);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(10);
		ColumnConstraints c3 = new ColumnConstraints();
		c3.setPercentWidth(10);
		grid.getColumnConstraints().addAll(c1,c2,c3);
		dialog.getDialogPane().setContent(grid);

		dialog.showAndWait();
	}

	public static void CaracDeclarationPopUp(UserAccount account, ClassSegment currentItemSegment, Pair<ClassSegment, ClassCaracteristic> editingCarac, Char_description parent,Integer defaultSeq) throws SQLException, ClassNotFoundException {


		// Create the custom dialog.
		create_dialog();
		
		// Create the carac labels and fields.
		create_dialog_fields(editingCarac,account,currentItemSegment,parent);

		// Set fields layout
		set_fields_layout();
				
		//Set fields behavior
		set_fields_behavior(dialog,validateButtonType,account,currentItemSegment,"NAME",editingCarac,parent,defaultSeq);
				
		dialog.getDialogPane().setContent(grid);

		// Request focus on the char name by default.
		Platform.runLater(() -> {
			charName.requestFocus();
			if(editingCarac!=null){
				charName.processSelectedCarac(editingCarac,editingCarac);
			}
		});

		//grid.getRowConstraints().forEach(rc->rc.setVgrow(Priority.ALWAYS));

		dialog.showAndWait();



	}


	private static void set_fields_behavior(Dialog<ClassCaracteristic> dialog, ButtonType validateButtonType, UserAccount account, ClassSegment currentItemSegment, String templateCriterion, Pair<ClassSegment, ClassCaracteristic> editingCarac, Char_description parent,Integer defaultSeq) throws SQLException, ClassNotFoundException {
		//Fill the carac name field and the template UoMs DS
		ArrayList<ClassCaracteristic> uniqueCharTemplate = new ArrayList<ClassCaracteristic>();
		HashSet<String> uniqueCharIDs = new HashSet<String>();
		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(account);
		if(editingCarac!=null){

		}else{
			CharValuesLoader.active_characteristics.entrySet().stream()
					.map(e-> new Pair<String,ArrayList<ClassCaracteristic>>
							(e.getKey(),
									new ArrayList<ClassCaracteristic>(e.getValue().stream().filter(
											c->uniqueCharIDs.add(c.getCharacteristic_id())).collect(Collectors.toList()))
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
		}


		charName.incompleteProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				uom0.getItems().clear();
				uom1.getEntries().clear();
				uom2.getEntries().clear();

				if(!newValue){
					ClassCaracteristic selectedCar = charName.selectedEntry.getValue();
					if(editingCarac!=null){
						charName.setText(selectedCar.getCharacteristic_name());
					}
					charNameTranslated.setText(selectedCar.getCharacteristic_name_translated());
					if(selectedCar.getIsNumeric()){
						charType.getSelectionModel().select("Numeric");
					}else{
						charType.getSelectionModel().select("Text");
						charTranslability.getSelectionModel().select(selectedCar.getIsTranslatable()?"Translatable":"Not translatable");
					}
					ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment,charName.selectedEntry,templateCriterion,currentItemSegment);
					if(cc.toString().endsWith("category(ies)")){
						charClassLink.getItems().add(cc);
						charClassLink.getSelectionModel().select(cc);
					}else{
						charClassLink.getSelectionModel().select(0);
					}

					//sequence.getSelectionModel().select(Integer.valueOf(Math.min(selectedCar.getSequence(),Collections.max((Collection<? extends Integer>) sequence.getItems().stream().map(i->i.getKey()).collect(Collectors.toCollection(ArrayList::new))))));
					if(editingCarac!=null){
						System.out.println("Editing carac seq");
						sequence.getSelectionModel().select(selectedCar.getSequence()-1);
					}else if(defaultSeq!=null){
						System.out.println("Default seq =>"+defaultSeq);
						sequence.getSelectionModel().select(defaultSeq-1);
					}else{
						System.out.println("No default seq");
						sequence.getSelectionModel().select(sequence.getItems().size()-1);
					}
					criticality.getSelectionModel().select(selectedCar.getIsCritical()?"Critical":"Not critical");
					//Use the template UoM DS to set the uom0 combobox
					try{
						if(editingCarac!=null && charName.selectedEntry.getValue().getAllowedUoms()!=null && charName.selectedEntry.getValue().getAllowedUoms().size()>0){
							uom0.getItems().add("Other...");
							uom0.getSelectionModel().select(0);
							uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->UnitOfMeasure.ConversionPathExists(u,charName.selectedEntry.getValue().getAllowedUoms())).collect(Collectors.toCollection(ArrayList::new)));
							uom2.getEntries().addAll(uom1.getEntries());
							uom1.clear();
							uom2.clear();
							uom1.print_uom_in_parent(UnitOfMeasure.RunTimeUOMS.get(charName.selectedEntry.getValue().getAllowedUoms().get(0)));
							try{
								uom2.print_uom_in_parent(UnitOfMeasure.RunTimeUOMS.get(charName.selectedEntry.getValue().getAllowedUoms().get(1)));
							}catch (Exception V){

							}
						}else{
							Set<String> UomKS = templateUoMs.get(charName.selectedEntry.getValue().getCharacteristic_id()).keySet();
							uom0.getItems().addAll(UomKS);
							uom0.getItems().add("Other...");
							uom0.getSelectionModel().select(0);
							uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->UnitOfMeasure.ConversionPathExists(u,charName.selectedEntry.getValue().getAllowedUoms())).collect(Collectors.toCollection(ArrayList::new)));
							uom2.getEntries().addAll(uom1.getEntries());
							uom1.clear();
							uom2.clear();
						}
					}catch (Exception V){
						uom0.getItems().add("No unit of measure");
						uom0.getSelectionModel().select(0);
						uom1.clear();
						uom2.clear();
					}

				}else{
					charNameTranslated.clear();
					charType.getSelectionModel().clearSelection();
					charTranslability.getSelectionModel().clearSelection();
					charClassLink.getSelectionModel().clearSelection();
					charClassLink.getItems().clear();
					IntStream.range(0,currentItemSegment.getSegmentGranularity()).forEach(lvl->{
						ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(lvl, currentItemSegment,sid2Segment);
						charClassLink.getItems().add(cc);
					});
					Collections.reverse(charClassLink.getItems());
					ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment);
					charClassLink.getItems().add(cc);
					charClassLink.getSelectionModel().select(0); //Select this category only on carac creation
					sequence.getSelectionModel().clearSelection();
					//sequence.getSelectionModel().select(Integer.valueOf(CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).size()+1));
					if(defaultSeq!=null){
						sequence.getSelectionModel().select(defaultSeq-1);
					}else{
						sequence.getSelectionModel().select(sequence.getItems().size()-1);
					}
					criticality.getSelectionModel().clearSelection();
					criticality.getSelectionModel().select("Not critical");
					uom0.getItems().add("No unit of measure");
					uom0.getItems().add("Other...");
					uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());
					uom2.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values());
				}

			}
		});

		charNameTranslated.disableProperty().bind(charName.textProperty().length().isEqualTo(0));
		charType.disableProperty().bind(charName.incompleteProperty.not());
		charTranslability.disableProperty().bind(charType.valueProperty().isNull().or(charName.incompleteProperty.not()));
		charClassLink.disableProperty().bind(charTranslability.valueProperty().isNull());
		sequence.disableProperty().bind(charClassLink.valueProperty().isNull().or(charTranslability.valueProperty().isNull()));
		sequenceCB.disableProperty().bind(sequence.valueProperty().isNull().or(sequence.disableProperty()));
		criticality.disableProperty().bind(charClassLink.valueProperty().isNull().or(sequence.valueProperty().isNull()).or(sequence.disableProperty()));
		criticalityCB.disableProperty().bind(criticality.valueProperty().isNull().or(criticality.disableProperty()));
		uom0.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom0CB.disableProperty().bind(uom0.valueProperty().isNull());
		uom1.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom1CB.disableProperty().bind(uom1.incompleteProperty);
		uom2.disableProperty().bind(charClassLink.valueProperty().isNull().or(criticality.valueProperty().isNull()));
		uom2CB.disableProperty().bind(uom2.incompleteProperty);

		uom0.visibleProperty().bind((charName.incompleteProperty.not().or(charType.valueProperty().isEqualTo("Numeric"))).and(uom0.valueProperty().isEqualTo("Other...").not()));
		uom0CB.visibleProperty().bind(uom0.visibleProperty());
		uom0Label.visibleProperty().bind(uom0.visibleProperty());
		uom1.visibleProperty().bind(uom0.visibleProperty().not().and(charType.valueProperty().isEqualTo("Numeric")));
		uom1CB.visibleProperty().bind(uom1.visibleProperty());
		uom1Label.visibleProperty().bind(uom1.visibleProperty());
		uom2.visibleProperty().bind(uom0.visibleProperty().not().and(charType.valueProperty().isEqualTo("Numeric")));
		uom2CB.visibleProperty().bind(uom2.visibleProperty());
		uom2Label.visibleProperty().bind(uom2.visibleProperty());

		uom1.incompleteProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue && charName.incompleteProperty.getValue()){
					if(uom2.incompleteProperty.getValue() || (uom2.selectedUom!=null && !UnitOfMeasure.ConversionPathExists(uom2.selectedUom, Collections.singletonList(uom1.selectedUom.getUom_id()))) ){
						uom2.clear();
					}
					uom2.getEntries().clear();
					uom2.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->UnitOfMeasure.ConversionPathExists(u, Collections.singletonList(uom1.selectedUom.getUom_id()))).collect(Collectors.toCollection(ArrayList::new)));

				}
			}
		});
		uom2.incompleteProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue && charName.incompleteProperty.getValue()){
					if(uom1.incompleteProperty.getValue() || (uom1.selectedUom!=null && !UnitOfMeasure.ConversionPathExists(uom1.selectedUom, Collections.singletonList(uom2.selectedUom.getUom_id()))) ){
						uom1.clear();
					}
					uom1.getEntries().clear();
					uom1.getEntries().addAll(UnitOfMeasure.RunTimeUOMS.values().stream().filter(u->UnitOfMeasure.ConversionPathExists(u, Collections.singletonList(uom2.selectedUom.getUom_id()))).collect(Collectors.toCollection(ArrayList::new)));

				}
			}
		});

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
		charClassLink.getItems().clear();
		IntStream.range(0,currentItemSegment.getSegmentGranularity()).forEach(lvl->{
			ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(lvl, currentItemSegment,sid2Segment);
			charClassLink.getItems().add(cc);
		});
		Collections.reverse(charClassLink.getItems());
		ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment);
		charClassLink.getItems().add(cc);

		//sequence.getItems().addAll(IntStream.range(1,CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).size()+2).boxed().collect(Collectors.toList()));
		sequence.getItems().addAll(CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).stream().map(c->new ComboPair<Integer,String>(c.getSequence(),c.getCharacteristic_name())).collect(Collectors.toCollection(ArrayList::new)));
		if(!(editingCarac!=null)){
			sequence.getItems().add(new ComboPair<>(CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).stream().map(c->c.getSequence()).max(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			}).get()+1,ComboPair.NEW_ENTRY_LABEL));
		}
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
					try{
						if (charName.incompleteProperty.getValue()) {
							UoMDeclarationDialog.GenericUomDeclarationPopUp(uom1.getText(), uom1);
						} else {
							UoMDeclarationDialog.GenericUomDeclarationPopUpRestrictedConvertibility(uom1.getText(), uom1, charName.selectedEntry.getValue());
						}
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
			}
		});
		uom2.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(!newValue && uom2.incompleteProperty.getValue() &&  uom2.isVisible() && uom2.getText().replace(" ","").length()>0){
					try{
						if (charName.incompleteProperty.getValue()) {
							UoMDeclarationDialog.GenericUomDeclarationPopUp(uom2.getText(), uom2);
						} else {
							UoMDeclarationDialog.GenericUomDeclarationPopUpRestrictedConvertibility(uom2.getText(), uom2, charName.selectedEntry.getValue());
						}
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
			}
		});

		detailsLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				showDetailedClassClusters(currentItemSegment);
			}
		});

		Button validationButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
		validationButton.disableProperty().bind(criticalityCB.disableProperty().or(uom0CB.disableProperty().and(uom0CB.visibleProperty())).or(uom1CB.disableProperty().and(uom1CB.visibleProperty())));
		validationButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ClassCaracteristic newCarac = loadCaracFromDialog(editingCarac);
				ArrayList<ClassSegment> droppedClassInsertions = dispatchCaracOnClassesReturnDropped(newCarac, charClassLink.getValue().getRowSegments(),currentItemSegment,account,parent);
				dialog.close();
				showDroppedClassInsertions(droppedClassInsertions);
			}
		});


	}

	private static void showDroppedClassInsertions(ArrayList<ClassSegment> droppedClassInsertions) {
		if(droppedClassInsertions.size()==0){
			return;
		}
		ClassSegment firstRow = droppedClassInsertions.get(0);
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Listing dropped characteristic insertion");
		dialog.setHeaderText("Duplicate characteristic name within segments is not allowed. The following classes have not been changed:");
		dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		TableView<ClassSegment> tableview = new TableView<ClassSegment>();

		TableColumn col1 = new TableColumn("Class ID");
		col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ClassSegment, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<ClassSegment, String> r) {
				return new ReadOnlyObjectWrapper(r.getValue().getClassNumber());
			}
		});
		col1.setResizable(false);
		col1.prefWidthProperty().bind(tableview.widthProperty().multiply(1.0 / (1.0*(firstRow.getSegmentGranularity()+1))));
		tableview.getColumns().add(col1);

		ArrayList<String> headerColumn = new ArrayList<String>();
		headerColumn.add("Domain");
		headerColumn.add("Group");
		headerColumn.add("Family");
		headerColumn.add("Category");

		IntStream.range(0,firstRow.getSegmentGranularity()).forEach(idx->{
			TableColumn tmp = new TableColumn(headerColumn.get(idx));
			tmp.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ClassSegment, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(TableColumn.CellDataFeatures<ClassSegment, String> r) {
					return new ReadOnlyObjectWrapper(r.getValue().getLevelName(idx));
				}
			});
			tmp.setResizable(false);
			tmp.prefWidthProperty().bind(tableview.widthProperty().multiply(1.0 / (1.0*(firstRow.getSegmentGranularity()+1))));
			tableview.getColumns().add(tmp);
		});


		tableview.getItems().addAll(droppedClassInsertions);

		grid.add(tableview,0,0);
		tableview.setMinWidth(800);
		dialog.getDialogPane().setContent(grid);

		dialog.showAndWait();
	}


	public static ArrayList<ClassSegment> dispatchCaracOnClassesReturnDropped(ClassCaracteristic newCarac, ArrayList<Pair<ClassSegment, SimpleBooleanProperty>> targetClasses, ClassSegment currentItemSegment, UserAccount account,Char_description parent)  {
		ArrayList<ClassSegment> droppedClassInsertions = new ArrayList<ClassSegment>();
		HashMap<String,Pair<String,String>> ruleRerunTargets = new HashMap<String,Pair<String,String>>();
		targetClasses.parallelStream().filter(p->p.getValue().getValue()).map(Pair::getKey).forEach(s->{

			if(!(CharValuesLoader.active_characteristics.get(s.getSegmentId()) !=null)){
				//The segment has no defined caracs
				CharValuesLoader.active_characteristics.put(s.getSegmentId(),new ArrayList<ClassCaracteristic>());
			}
			Optional<ClassCaracteristic> charClassMatch = CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> c.getCharacteristic_id().equals(newCarac.getCharacteristic_id())).findAny();
			ClassCaracteristic copy = newCarac.shallowCopy();
			if(charClassMatch.isPresent()){
				if(!sequenceCB.isSelected() && !s.equals(currentItemSegment)){
					copy.setSequence(charClassMatch.get().getSequence());
				}else{
					CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> !c.getCharacteristic_id().equals(copy.getCharacteristic_id()))
							.forEach(c->{
								if(charClassMatch.get().getSequence()<=copy.getSequence()){
									//Advancing
									if(c.getSequence()>= charClassMatch.get().getSequence() && c.getSequence() <= copy.getSequence()){
										c.setSequence(c.getSequence()-1);
										addCaracDefinitionToPush(c,s);
									}
								}else{
									//Regressing
									if(c.getSequence() <= charClassMatch.get().getSequence() && c.getSequence()>=copy.getSequence()){
										c.setSequence(c.getSequence()+1);
										addCaracDefinitionToPush(c,s);
									}
								}
							});
				}
				if(!criticalityCB.isSelected() && !s.equals(currentItemSegment)){
					copy.setIsCritical(charClassMatch.get().getIsCritical());
				}
				if(!uom0CB.isSelected() && !uom1CB.isSelected() && !uom2CB.isSelected() && !s.equals(currentItemSegment)){
					copy.setAllowedUoms(charClassMatch.get().getAllowedUoms());
				}

				int matchIndx = CharValuesLoader.active_characteristics.get(s.getSegmentId()).indexOf(charClassMatch.get());
				CharValuesLoader.active_characteristics.get(s.getSegmentId()).set(matchIndx,copy);
				/*CharValuesLoader.active_characteristics.get(s.getSegmentId()).sort(new Comparator<ClassCaracteristic>() {
					@Override
					public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
						return o1.getSequence().compareTo(o2.getSequence());
					}
				});*/
				addCaracDefinitionToPush(copy,s);
			}else{
				//The carac is not present insert if the charac name is not already used within the segment
				if(CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().anyMatch(c->StringUtils.equalsIgnoreCase(c.getCharacteristic_name(),copy.getCharacteristic_name()))){
					droppedClassInsertions.add(s);
					return;
				}
				//Disabled, insert new carac at sequence max
				// CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c -> c.getSequence()>=newCarac.getSequence()).forEach(c->c.setSequence(c.getSequence()+1));
				if(!s.equals(currentItemSegment)){
					try{
						if(sequenceCB.isSelected()){
							//Force use the config sequence if the sequence CB is selected
							copy.setSequence(Math.min(copy.getSequence(),CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().map(ClassCaracteristic::getSequence).max(Integer::compare).get()+1));
							CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().filter(c-> c.getSequence()>=copy.getSequence()).forEach(c->{
								c.setSequence(c.getSequence()+1);
								addCaracDefinitionToPush(c,s);
							});
						}else{
							copy.setSequence(CharValuesLoader.active_characteristics.get(s.getSegmentId()).stream().map(ClassCaracteristic::getSequence).max(Integer::compare).get()+1);
						}
					}catch (NoSuchElementException V){
						//The segment has no caracs
						copy.setSequence(1);
					}
				}else{
					//Current class advance other caracs
					CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).stream().filter(c-> c.getSequence()>=copy.getSequence()).forEach(c->{
						c.setSequence(c.getSequence()+1);
						addCaracDefinitionToPush(c,s);
					});
					parent.tableController.selected_col=copy.getSequence()-1;
				}
				CharValuesLoader.active_characteristics.get(s.getSegmentId()).add(copy);
				ruleRerunTargets.put(s.getSegmentId()+copy.getCharacteristic_id(),new Pair<String,String>(s.getSegmentId(), copy.getCharacteristic_id()));
				addCaracDefinitionToPush(copy,s);
			}
			CharValuesLoader.active_characteristics.get(s.getSegmentId()).sort(new Comparator<ClassCaracteristic>() {
				@Override
				public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
					return o1.getSequence().compareTo(o2.getSequence());
				}
			});
		});
		try {
			flushCaracDefinitionToDB(account);
		} catch (SQLException | ClassNotFoundException throwables) {
			throwables.printStackTrace();
		}


		Task rerunTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				ruleRerunTargets.values().parallelStream().forEach(p->{
					String loopSegmentId = p.getKey();
					String loopCar = p.getValue();
					ArrayList<String> loopCars = new ArrayList<String>();
					loopCars.add(loopCar);
					ArrayList<CharDescriptionRow> loopItems = CharItemFetcher.allRowItems.parallelStream().filter(r -> r.getClass_segment_string().startsWith(p.getKey())).collect(Collectors.toCollection(ArrayList::new));
					CharPatternServices.ruleRerun(loopItems, parent.account, loopCars);
				});
				return null;
			}
		};
		rerunTask.setOnSucceeded(e->{
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					parent.refresh_ui_display();
				}
			});
			CharDescriptionExportServices.flushItemDataToDB(parent.account);
		});
		Thread thread = new Thread(rerunTask);; thread.setDaemon(true);
		thread.setName("Rerunning rules after char Import");
		thread.start();

		return droppedClassInsertions;
	}

	private static ClassCaracteristic
	loadCaracFromDialog(Pair<ClassSegment, ClassCaracteristic> editingCarac) {
		ClassCaracteristic newCarac = new ClassCaracteristic();
		ArrayList<String> newUoms = new ArrayList<String>();
		if(charName.incompleteProperty.getValue()){
			//New carac
			newCarac = new ClassCaracteristic();
			newCarac.setCharacteristic_id(Tools.generate_uuid());
			newCarac.setCharacteristic_name(charName.getText());
		}else{
			//Old carac
			newCarac = charName.selectedEntry.getValue().shallowCopy();
			if(uom0.isVisible() && templateUoMs.get(newCarac.getCharacteristic_id())!=null){
				ArrayList<UnitOfMeasure> tmpUom = templateUoMs.get(newCarac.getCharacteristic_id()).get(uom0.getValue());
				if(tmpUom!=null){
					newUoms.addAll(tmpUom.stream().map(UnitOfMeasure::getUom_id).collect(Collectors.toCollection(ArrayList::new)));
				}
			}
		}
		if(editingCarac!=null){
			newCarac.setCharacteristic_name(charName.getText());
		}
		newCarac.setCharacteristic_name_translated(charNameTranslated.getText());
		newCarac.setIsNumeric(charType.getValue().equals("Numeric"));
		newCarac.setIsTranslatable(charTranslability.getValue().equals("Translatable"));
		//newCarac.setSequence(sequence.getValue().getKey());
		newCarac.setSequence(FxUtilTest.getComboBoxValue(sequence).getKey());
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
		dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");
		
		// Set the button types.
		validateButtonType = new ButtonType("Apply", ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, cancelButtonType);

	}
	@SuppressWarnings("static-access")
	private static void create_dialog_fields(Pair<ClassSegment, ClassCaracteristic> editingCarac, UserAccount account, ClassSegment currentItemSegment,Char_description parent) {
		grid = new GridPane();
		charName = new AutoCompleteBox_CharDeclarationName(editingCarac);
		searchLabel = new Label("Advanced search...");
		searchLabel.setUnderline(true);
		searchLabel.setTextAlignment(TextAlignment.CENTER);
		GridPane.setHalignment(searchLabel,HPos.CENTER);
		searchLabel.setFont(Font.font(searchLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, searchLabel.getFont().getSize()));
		searchLabel.setVisible(!(editingCarac!=null));
		searchLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					showAdvancedSearchPane(account,currentItemSegment,parent);
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		charNameTranslated = new TextField();
		charNameTranslated.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(charNameTranslated);

				}
			}
		});

		charType = new ComboBox<String>();
		charType.setMaxWidth(Integer.MAX_VALUE);
		charType.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(charType);

				}
			}
		});

		charTranslability = new ComboBox<String>();
		charTranslability.setMaxWidth(Integer.MAX_VALUE);
		charTranslability.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(charTranslability);

				}
			}
		});

		charClassLink = new ComboBox<ClassSegmentClusterComboRow>();
		charClassLink.setMaxWidth(Integer.MAX_VALUE);
		charClassLink.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(charClassLink);

				}
			}
		});

		sequence = new ComboBox<ComboPair<Integer,String>>();
		FxUtilTest.autoCompleteComboBoxPlus(sequence, (typedText, itemToCompare) -> itemToCompare.getValue().toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.getKey().toString().equals(typedText));
		sequence.setMaxWidth(Integer.MAX_VALUE);
		sequence.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(sequence);

				}
			}
		});

		criticality = new ComboBox<String>();
		criticality.setMaxWidth(Integer.MAX_VALUE);
		criticality.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(criticality);

				}
			}
		});

		uom0 = new ComboBox<String>();
		uom0.setMaxWidth(Integer.MAX_VALUE);
		uom0.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(uom0);

				}
			}
		});
		uom1 = new AutoCompleteBox_UnitOfMeasure("NAME_AND_SYMBOL");
		uom1.setMaxWidth(Integer.MAX_VALUE);
		uom1.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){skipToNextField(uom1);

				}
			}
		});
		uom2 = new AutoCompleteBox_UnitOfMeasure("NAME_AND_SYMBOL");
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

	private static void showAdvancedSearchPane(UserAccount account, ClassSegment currentItemSegment, Char_description parent) throws SQLException, ClassNotFoundException {
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Advance characteristic search");
		dialog.setHeaderText(null);
		dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");

		// Set the button types.
		ButtonType importButtonType = new ButtonType("Import selected",ButtonData.APPLY);
		ButtonType closeButtonType = new ButtonType("Close",ButtonData.CANCEL_CLOSE);

		dialog.getDialogPane().getButtonTypes().addAll(importButtonType,closeButtonType);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.add(new Label("Search by characteristic name"),0,0);
		Button conjButton = new Button("AND");
		conjButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (conjButton.getText().equals("AND")){
					conjButton.setText("OR");
				}else{
					conjButton.setText("AND");
				}
			}
		});
		grid.add(conjButton,1,0);
		GridPane.setHalignment(conjButton,HPos.CENTER);
		grid.add(new Label("Search by class name"),2,0);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(40);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(20);
		ColumnConstraints c3 = new ColumnConstraints();
		c3.setPercentWidth(40);
		grid.getColumnConstraints().setAll(c1,c2,c3);
		
		ComboBox<ClassCaracteristic> caracCombo = new ComboBox<ClassCaracteristic>();
		unidecode = Unidecode.toAscii();

		FxUtilTest.autoCompleteComboBoxPlus(caracCombo, (typedText, itemToCompare) -> StringUtils.containsIgnoreCase(unidecode.decodeAndTrim(itemToCompare.getCharacteristic_name()),unidecode.decodeAndTrim(typedText)));
		grid.add(caracCombo,0,1);
		ComboBox<ClassSegment> segCombo = new ComboBox<ClassSegment>();
		FxUtilTest.autoCompleteComboBoxPlus(segCombo, (typedText, itemToCompare) -> StringUtils.containsIgnoreCase(unidecode.decodeAndTrim(itemToCompare.getClassName()),unidecode.decodeAndTrim(typedText)) || StringUtils.containsIgnoreCase(itemToCompare.getClassNumber().trim(),typedText.trim()));
		grid.add(segCombo,2,1);

		TableView<AdvancedResultRow> resultTable = new TableView<AdvancedResultRow>();
		resultTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		TableColumn col1 = new TableColumn("Characteristic Name");
		col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdvancedResultRow, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<AdvancedResultRow, String> r) {
				return new ReadOnlyObjectWrapper(r.getValue().getCarac());
			}
		});
		col1.setResizable(false);
		col1.prefWidthProperty().bind(resultTable.widthProperty().multiply(50 / 100.0));

		TableColumn col2 = new TableColumn("Category Name");
		col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdvancedResultRow, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<AdvancedResultRow, String> r) {
				return new ReadOnlyObjectWrapper(r.getValue().getSegment());
			}
		});
		col2.setResizable(false);
		col2.prefWidthProperty().bind(resultTable.widthProperty().multiply(50 / 100.0));
		resultTable.getColumns().setAll(col1,col2);

		grid.add(resultTable,0,2);
		GridPane.setColumnSpan(resultTable,GridPane.REMAINING);

		RowConstraints r1 = new RowConstraints();
		r1.setPercentHeight(10);
		r1.setVgrow(Priority.ALWAYS);
		RowConstraints r2 = new RowConstraints();
		r2.setPercentHeight(10);
		r2.setVgrow(Priority.ALWAYS);
		RowConstraints r3 = new RowConstraints();
		r3.setPercentHeight(80);
		r3.setVgrow(Priority.ALWAYS);
		grid.getRowConstraints().setAll(r1,r2,r3);

		dialog.getDialogPane().setContent(grid);

		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(account);
		HashSet<String> uniqueCaracNames = new HashSet<String>();
		CharValuesLoader.active_characteristics.values().stream().flatMap(x->x.stream())
				.filter(c->uniqueCaracNames.add(c.getCharacteristic_name()))
				.collect(Collectors.toCollection(HashSet::new)).forEach(c->caracCombo.getItems().add(c));
		caracCombo.getItems().sort(new Comparator<ClassCaracteristic>() {
			@Override
			public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
				return o1.getCharacteristic_name().compareTo(o2.getCharacteristic_name());
			}
		});
		sid2Segment.values().forEach(s->segCombo.getItems().add(s));
		segCombo.getItems().sort(new Comparator<ClassSegment>() {
			@Override
			public int compare(ClassSegment o1, ClassSegment o2) {
				return o1.getClassNumber().compareTo(o2.getClassNumber());
			}
		});

		Button importButton = (Button) dialog.getDialogPane().lookupButton(importButtonType);
		importButton.visibleProperty().bind(resultTable.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0));
		importButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(resultTable.getSelectionModel().getSelectedIndices().size()>1){
					resultTable.getSelectionModel().getSelectedItems().forEach(r->{
						ClassCaracteristic tmp = r.getCarac().shallowCopy();
						tmp.setSequence(CharValuesLoader.active_characteristics.get(currentItemSegment.getSegmentId()).size() + 1);
						ArrayList<Pair<ClassSegment, SimpleBooleanProperty>> charClassMatchLocal = new ArrayList<Pair<ClassSegment, SimpleBooleanProperty>>();
						charClassMatchLocal.add(new Pair<ClassSegment,SimpleBooleanProperty>(currentItemSegment,new SimpleBooleanProperty(true)));
						dispatchCaracOnClassesReturnDropped(tmp,charClassMatchLocal,currentItemSegment,account,parent);
						dialog.close();
						CaracDeclarationDialog.dialog.close();
						return;
					});
				}
				Pair<ClassSegment, ClassCaracteristic> selectedEntry = new Pair<>(resultTable.getSelectionModel().getSelectedItem().getSegment(), resultTable.getSelectionModel().getSelectedItem().getCarac());
				charName.setText(selectedEntry.getValue().getCharacteristic_name()+(false?"":" (e.g. "+selectedEntry.getKey().getClassName()+")"));
				charName.selectedEntry = selectedEntry;
				charName.incompleteProperty.setValue(false);
				charName.hideEntriesPopup();
				dialog.close();
			}
		});

		caracCombo.setConverter(new StringConverter<ClassCaracteristic>() {

			@Override
			public String toString(ClassCaracteristic object) {
				if (object == null) return null;
				return object.toString();
			}

			@Override
			public ClassCaracteristic fromString(String string) {
				Optional<ClassCaracteristic> match = caracCombo.getItems().stream().filter(e -> e.toString().equals(string)).findAny();
				if(match.isPresent()){
					return match.get();
				}
				return null;
			}
		});
		caracCombo.valueProperty().addListener(new ChangeListener<ClassCaracteristic>() {
			@Override
			public void changed(ObservableValue<? extends ClassCaracteristic> observable, ClassCaracteristic oldValue, ClassCaracteristic newValue) {
				resultTable.getItems().setAll(updateAdvancedSearchResults(caracCombo,segCombo,resultTable,conjButton,sid2Segment));
			}
		});
		segCombo.setConverter(new StringConverter<ClassSegment>() {

			@Override
			public String toString(ClassSegment object) {
				if (object == null) return null;
				return object.toString();
			}

			@Override
			public ClassSegment fromString(String string) {
				Optional<ClassSegment> match = segCombo.getItems().stream().filter(e -> e.toString().equals(string)).findAny();
				if(match.isPresent()){
					return match.get();
				}
				return null;
			}
		});
		segCombo.valueProperty().addListener(new ChangeListener<ClassSegment>() {
			@Override
			public void changed(ObservableValue<? extends ClassSegment> observable, ClassSegment oldValue, ClassSegment newValue) {
				resultTable.getItems().setAll(updateAdvancedSearchResults(caracCombo,segCombo,resultTable,conjButton,sid2Segment));
			}
		});

		segCombo.valueProperty().addListener(new ChangeListener<ClassSegment>() {
			@Override
			public void changed(ObservableValue<? extends ClassSegment> observable, ClassSegment oldValue, ClassSegment newValue) {
				System.out.println("New seg value "+newValue);
			}
		});
		conjButton.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				resultTable.getItems().setAll(updateAdvancedSearchResults(caracCombo,segCombo,resultTable,conjButton,sid2Segment));
			}
		});

		GridPane.setHgrow(segCombo,Priority.ALWAYS);
		GridPane.setHgrow(caracCombo,Priority.ALWAYS);

		dialog.showAndWait();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				caracCombo.requestFocus();
			}
		});

	}

	private static ArrayList<AdvancedResultRow> updateAdvancedSearchResults(ComboBox<ClassCaracteristic> caracCombo, ComboBox<ClassSegment> segCombo, TableView<AdvancedResultRow> resultTable, Button conjButton, HashMap<String, ClassSegment> sid2Segment) {
		ArrayList<AdvancedResultRow> retainedRows = new ArrayList<AdvancedResultRow>();
		CharValuesLoader.active_characteristics.entrySet().forEach(e->{
			ClassSegment loopSegment = sid2Segment.get(e.getKey());
			if(conjButton.getText().equals("AND") && segCombo.getValue()!=null && !loopSegment.equals(segCombo.getValue())){
				return;
			}
			e.getValue().forEach(loopCarac->{
				if(conjButton.getText().equals("AND") && caracCombo.getValue()!=null && !loopCarac.getCharacteristic_name().equals(caracCombo.getValue().getCharacteristic_name())){
					return;
				}
				if(caracCombo.getValue()!=null && loopCarac.getCharacteristic_name().equals(caracCombo.getValue().getCharacteristic_name())
					||
					segCombo.getValue()!=null && loopSegment.equals(segCombo.getValue())){
					retainedRows.add(new AdvancedResultRow(loopSegment,loopCarac));
				}
			});
		});
		return retainedRows;
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
		grid.add(searchLabel,5,2);
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

	public static void CaracEditionPopUp(ClassCaracteristic carac, UserAccount account, TableView<CharDescriptionRow> tableGrid,Char_description parent) throws SQLException, ClassNotFoundException {
		ClassSegment itemSegement = Tools.get_project_segments(account).get(tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]);
		Pair<ClassSegment, ClassCaracteristic> entry = new Pair<ClassSegment, ClassCaracteristic>(itemSegement, carac);
		CaracDeclarationDialog.CaracDeclarationPopUp(account, itemSegement,entry,parent,null);


	}

	public static void CaracDeletion(ClassCaracteristic carac, String itemSegment,UserAccount account) throws SQLException, ClassNotFoundException {
		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(account);
		Boolean scopeChoice = ConfirmationDialog.showCaracDeleteScopeConfirmation();
		if(scopeChoice!=null){

			int impactedValuesOnDelete = (int) CharItemFetcher.allRowItems.stream().filter(r->(scopeChoice||r.getClass_segment_string().split("&&&")[0].equals(itemSegment)))
					.filter(r->r.hasDataInCurrentClassForCarac(carac.getCharacteristic_id())).count();

			if(impactedValuesOnDelete>0){
				if(!ConfirmationDialog.showCaracDeleteImpactConfirmation(impactedValuesOnDelete)){
					return;
				}
			}

			CharValuesLoader.active_characteristics.entrySet().stream().filter(e->scopeChoice||e.getKey().equals(itemSegment)).forEach(e->{
				Optional<ClassCaracteristic> localCarac = e.getValue().stream().filter(c -> c.getCharacteristic_id().equals(carac.getCharacteristic_id())).findAny();
				if(localCarac.isPresent()){
					CharValuesLoader.active_characteristics.get(e.getKey()).stream().filter(c->c.getSequence() > localCarac.get().getSequence())
							.forEach(c->{
								c.setSequence(c.getSequence()-1);
								addCaracDefinitionToPush(c,sid2Segment.get(e.getKey()));
							});
					CharValuesLoader.active_characteristics.put(e.getKey(),
							e.getValue().stream().filter(c->!c.getCharacteristic_id().equals(carac.getCharacteristic_id())).collect(Collectors.toCollection(ArrayList::new))
					);
					/*	CharValuesLoader.active_characteristics.get(e.getKey()).sort(new Comparator<ClassCaracteristic>() {
						@Override
						public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
							return o1.getSequence().compareTo(o2.getSequence());
						}
					});*/
					addCaracDefinitionToDisable(carac,e.getKey());
				}

			});
		}
		try {
			flushCaracDeleteToDB(account);
			flushCaracDefinitionToDB(account);
		} catch (SQLException | ClassNotFoundException throwables) {
			throwables.printStackTrace();
		}
	}

	public static void skipToNextField(Node node) {
		/*
		KeyEvent newEvent
				= new KeyEvent(
				null,
				null,
				KeyEvent.KEY_PRESSED,
				"",
				"\t",
				KeyCode.TAB,
				false,
				false,
				false,
				false
		);
		Event.fireEvent( node, newEvent );
		if(node instanceof TextField){
			((BehaviorSkinBase) ((TextField)node).getSkin()).getBehavior().traverseNext();
		}
		if(node instanceof ComboBox){
			((BehaviorSkinBase) ((ComboBox)node).getSkin()).getBehavior().traverseNext();
		}*/
	}
}
