package transversal.dialog_toolbox;


import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Pair;
import model.ClassCaracteristic;
import model.ClassSegment;
import model.ClassSegmentClusterComboRow;
import model.GlobalConstants;
import service.CharValuesLoader;
import service.DeduplicationServices;
import transversal.generic.Tools;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DedupLaunchDialog {

    private static Dialog<ClassCaracteristic>  dialog;
    private static GridPane grid;
    private static TextField minMatches;
    private static TextField maxMismatches;
    private static TextField maxMismatchRatio;

    private static ComboBox<ClassSegmentClusterComboRow> charClassLink;
    private static Label detailsLabel;
    private static TableView<Pair<ClassCaracteristic,ArrayList<String>>> caracWeightTable;
    private static ButtonType validateButtonType;


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
        TableView<Pair<ClassSegment, SimpleBooleanProperty>> tableview = new TableView<Pair<ClassSegment, SimpleBooleanProperty>>();

        TableColumn col1 = new TableColumn("Class ID");
        col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getKey().getClassNumber());
            }
        });
        col1.setResizable(false);
        col1.prefWidthProperty().bind(tableview.widthProperty().multiply(35 / 100.0));

        TableColumn col2 = new TableColumn("Class name");
        col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getKey().getClassName());
            }
        });
        col2.setResizable(false);
        col2.prefWidthProperty().bind(tableview.widthProperty().multiply(35 / 100.0));

        TableColumn col3 = new TableColumn("Select for deduplication ");
        col3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<Pair<ClassSegment, SimpleBooleanProperty>, CheckBox> r) {
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

    public static void Settings(ClassSegment currentItemSegment, Char_description parent) throws SQLException, ClassNotFoundException {


        // Create the custom dialog.
        create_dialog();

        // Create the carac labels and fields.
        create_dialog_fields();

        // Set fields layout
        set_fields_layout();

        //Set fields behavior
        set_fields_behavior(dialog,validateButtonType,currentItemSegment,parent);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the char name by default.
        Platform.runLater(() -> {
            minMatches.requestFocus();
            charClassLink.getSelectionModel().select(0);
        });
        dialog.showAndWait();
    }


    private static void set_fields_behavior(Dialog<ClassCaracteristic> dialog, ButtonType validateButtonType, ClassSegment currentItemSegment, Char_description parent) throws SQLException, ClassNotFoundException {
        HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(parent.account);
        ChangeListener<String> paramListner = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Integer.parseInt(minMatches.getText());
                    Integer.parseInt(maxMismatches.getText());
                    Double.parseDouble(maxMismatchRatio.getText());
                    ((Button) dialog.getDialogPane().lookupButton(validateButtonType)).setDisable(false);
                } catch (Exception V) {
                    ((Button) dialog.getDialogPane().lookupButton(validateButtonType)).setDisable(true);
                }
            }
        };
        minMatches.textProperty().addListener(paramListner);
        maxMismatches.textProperty().addListener(paramListner);
        maxMismatchRatio.textProperty().addListener(paramListner);

        charClassLink.getItems().clear();
        IntStream.range(0,currentItemSegment.getSegmentGranularity()).forEach(lvl->{
            ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(lvl, currentItemSegment,sid2Segment);
            charClassLink.getItems().add(cc);
        });
        Collections.reverse(charClassLink.getItems());
        ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment);
        charClassLink.getItems().add(cc);
        charClassLink.valueProperty().addListener(new ChangeListener<ClassSegmentClusterComboRow>() {
            @Override
            public void changed(ObservableValue<? extends ClassSegmentClusterComboRow> observable, ClassSegmentClusterComboRow oldValue, ClassSegmentClusterComboRow newValue) {
                caracWeightTable.getItems().clear();
                ClassCaracteristic tmp = new ClassCaracteristic();
                tmp.setSequence(0);
                tmp.setCharacteristic_name("Item Class");
                caracWeightTable.getItems().add(new Pair<ClassCaracteristic,ArrayList<String>>(tmp,new ArrayList<>(Arrays.asList(new String("1.0"),new String("1.0"),new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0")))));
                caracWeightTable.getItems().addAll(GenerateWeightList(newValue));
            }
        });

        detailsLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showDetailedClassClusters(currentItemSegment);
            }
        });
        Button validationButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
        validationButton.setDisable(true);
        validationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                ArrayList<String> targetSegmentIDS = charClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
                HashMap<String, ArrayList<Object>> weightTable = new HashMap<>();
                caracWeightTable.getItems().forEach(r->{
                    ArrayList<Object> tmp = new ArrayList<Object>();
                    tmp.add(r.getKey());
                    tmp.add(r.getValue().get(0));
                    tmp.add(r.getValue().get(1));
                    tmp.add(r.getValue().get(2));
                    tmp.add(r.getValue().get(3));
                    if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
                        weightTable.put(r.getKey().getCharacteristic_name(),tmp);
                    }else{
                        weightTable.put(r.getKey().getCharacteristic_id(),tmp);
                    }
                });
                if(GlobalConstants.DEDUP_CARAC_WISE){
                    DeduplicationServices.scoreDuplicatesForClassesPairWise(targetSegmentIDS,weightTable, Integer.parseInt(minMatches.getText()), Integer.parseInt(maxMismatches.getText()), Double.parseDouble(maxMismatchRatio.getText()));
                }else{
                    DeduplicationServices.scoreDuplicatesForClassesFull(targetSegmentIDS,weightTable, Integer.parseInt(minMatches.getText()), Integer.parseInt(maxMismatches.getText()), Double.parseDouble(maxMismatchRatio.getText()));
                }
                showReport();
            }
        });


    }

    private static ArrayList<Pair<ClassCaracteristic,ArrayList<String>>> GenerateWeightList(ClassSegmentClusterComboRow newValue) {
        if(newValue==null){
            return new ArrayList<Pair<ClassCaracteristic,ArrayList<String>>>();
        }
        if(!GlobalConstants.DEDUP_CARAC_WISE){
            if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
                HashSet<String> uniqueNameCarac = new HashSet<String>();
                return newValue.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).flatMap(ac -> ac.stream()).filter(c->uniqueNameCarac.add(c.getCharacteristic_name()))
                        .map(car->new Pair<ClassCaracteristic,ArrayList<String>>(car,new ArrayList<>(Arrays.asList(new String("1.0"),new String("1.0"),new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0")))))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
            HashSet<ClassCaracteristic> uniqueIDCarac = newValue.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).flatMap(ac -> ac.stream()).collect(Collectors.toCollection(HashSet<ClassCaracteristic>::new));
            return uniqueIDCarac.stream()
                    .map(car->new Pair<ClassCaracteristic,ArrayList<String>>(car,new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0")))))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        HashSet<String> retainedCars = null;
        for (Pair<ClassSegment, SimpleBooleanProperty> classSegmentSimpleBooleanPropertyPair : newValue.getRowSegments()) {
            if (classSegmentSimpleBooleanPropertyPair.getValue().getValue()) {
                try {
                    ArrayList<String> loopChars = CharValuesLoader.active_characteristics.get(classSegmentSimpleBooleanPropertyPair.getKey().getSegmentId()).stream().map(a -> GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID ? a.getCharacteristic_name() : a.getCharacteristic_id()).collect(Collectors.toCollection(ArrayList::new));
                    try {
                        retainedCars.retainAll(loopChars);
                    } catch (Exception V) {
                        retainedCars = new HashSet<>();
                        retainedCars.addAll(loopChars);
                    }
                }catch (Exception V){

                }
            }
        }
        HashSet<String> finalRetainedCars = retainedCars;
        ArrayList<ClassCaracteristic> uniqueIDCarac = newValue.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(ac -> ac != null).flatMap(ac -> ac.stream()).filter(car -> finalRetainedCars.remove(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID ? car.getCharacteristic_name() : car.getCharacteristic_id())).collect(Collectors.toCollection(ArrayList::new));
        return uniqueIDCarac.stream()
                .map(car->new Pair<ClassCaracteristic,ArrayList<String>>(car,new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0")))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static void showReport() {
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
        //col1.prefWidthProperty().bind(tableview.widthProperty().multiply(1.0 / (1.0*(firstRow.getSegmentGranularity()+1))));
        tableview.getColumns().add(col1);

        ArrayList<String> headerColumn = new ArrayList<String>();
        headerColumn.add("Domain");
        headerColumn.add("Group");
        headerColumn.add("Family");
        headerColumn.add("Category");
        grid.add(tableview,0,0);
        tableview.setMinWidth(800);
        dialog.getDialogPane().setContent(grid);
        //dialog.showAndWait();
    }

    private static void create_dialog() {
        dialog = new Dialog<>();
        dialog.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });
        dialog.setTitle("Item deduplication settings");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("customDialog");

        // Set the button types.
        validateButtonType = new ButtonType("Launch", ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, cancelButtonType);

    }
    @SuppressWarnings("static-access")
    private static void create_dialog_fields() {
        grid = new GridPane();
        minMatches = new TextField();
        minMatches.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    skipToNextField(minMatches);

                }
            }
        });
        maxMismatches = new TextField();
        maxMismatches.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    skipToNextField(maxMismatches);

                }
            }
        });
        maxMismatchRatio = new TextField();
        maxMismatchRatio.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    skipToNextField(maxMismatchRatio);

                }
            }
        });

        charClassLink = new ComboBox<ClassSegmentClusterComboRow>();
        charClassLink.setMaxWidth(Integer.MAX_VALUE);
        charClassLink.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    //skipToNextField(charClassLink);

                }
            }
        });

        detailsLabel = new Label("View details...");
        detailsLabel.setUnderline(true);
        detailsLabel.setTextAlignment(TextAlignment.CENTER);
        detailsLabel.setFont(Font.font(detailsLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, detailsLabel.getFont().getSize()));
        grid.setHalignment(detailsLabel, HPos.CENTER);

        caracWeightTable = new TableView<Pair<ClassCaracteristic,ArrayList<String>>>();
        caracWeightTable.setEditable(true);
        caracWeightTable.getSelectionModel().setCellSelectionEnabled(true);
        caracWeightTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        caracWeightTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.D) && event.isControlDown()){
                    Pair<ClassCaracteristic, ArrayList<String>> firstRow = caracWeightTable.getItems().get(caracWeightTable.getSelectionModel().getSelectedIndices().stream().min(Comparator.naturalOrder()).get());
                    caracWeightTable.getSelectionModel().getSelectedCells().forEach(c->{
                        int itemIdx = c.getRow();
                        int weightIdx = c.getColumn()-2;
                        caracWeightTable.getItems().get(itemIdx).getValue().set(weightIdx,firstRow.getValue().get(weightIdx));
                    });
                    caracWeightTable.refresh();
                }
            }
        });

        TableColumn col0 = new TableColumn("Sequence");
        col0.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, Integer>, ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, Integer> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getKey().getSequence());
            }
        });
        col0.setResizable(false);
        col0.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10 / 100.0));

        TableColumn col1 = new TableColumn("Characteristic name");
        col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getKey().getCharacteristic_name());
            }
        });
        col1.setResizable(false);
        col1.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(30 / 100.0));

        TableColumn col2 = new TableColumn("Strong match weight");
        col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(0));
            }
        });
        col2.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col2.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(0,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col2.setResizable(false);
        col2.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        TableColumn col3 = new TableColumn("Weak match weight");
        col3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(1));
            }
        });
        col3.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col3.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(1,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col3.setResizable(false);
        col3.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        TableColumn col4 = new TableColumn("Included weight");
        col4.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(2));
            }
        });
        col4.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col4.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(2,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col4.setResizable(false);
        col4.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        TableColumn col5 = new TableColumn("Alternative weight");
        col5.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(3));
            }
        });
        col5.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col5.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(3,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col5.setResizable(false);
        col5.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        TableColumn col6 = new TableColumn("Unknown weight");
        col6.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(3));
            }
        });
        col6.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col6.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(3,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col6.setResizable(false);
        col6.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        TableColumn col7 = new TableColumn("Mismatch weight");
        col7.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ClassCaracteristic,ArrayList<String>>, String> r) {
                return new ReadOnlyObjectWrapper(r.getValue().getValue().get(3));
            }
        });
        col7.setCellFactory(TextFieldTableCell.<Pair<ClassCaracteristic,ArrayList<String>>>forTableColumn());
        col7.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Pair<ClassCaracteristic,ArrayList<String>>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Pair<ClassCaracteristic, ArrayList<String>>, String> t) {
                        String newVal;
                        try{
                            newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                        }catch (Exception V){
                            t.getTableView().refresh();
                            return;
                        }
                        ((Pair<ClassCaracteristic,ArrayList<String>>) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).getValue().set(3,newVal);
                        t.getTableView().refresh();
                    }
                }
        );
        col7.setResizable(false);
        col7.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(10.0/100.0));

        caracWeightTable.getColumns().add(col0);
        caracWeightTable.getColumns().add(col1);
        caracWeightTable.getColumns().add(col2);
        caracWeightTable.getColumns().add(col3);
        caracWeightTable.getColumns().add(col4);
        caracWeightTable.getColumns().add(col5);
        caracWeightTable.getColumns().add(col6);
        caracWeightTable.getColumns().add(col7);


    }



    @SuppressWarnings("static-access")
    private static void set_fields_layout() {
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(0);
        col0.setFillWidth(true);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(35);
        col1.setFillWidth(true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(2);
        col2.setFillWidth(true);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(45);
        col3.setFillWidth(true);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(3);
        col4.setFillWidth(true);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(15);
        col5.setFillWidth(true);
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setPercentWidth(0);
        col6.setFillWidth(true);


        grid.getColumnConstraints().addAll(col0,col1,col2,col3,col4,col5,col6);

        grid.getChildren().clear();
        Label headerLabel1 = new Label("General deduplication settings");
        headerLabel1.setUnderline(true);
        grid.add(headerLabel1, 1, 0);

        grid.add(   new Label("Minimum matches"), 1, 2);
        grid.add(minMatches,3,2);
        grid.add(new Label("Maximum mismatches"), 1, 3);
        grid.add(maxMismatches,3,3);
        grid.add(new Label("Maximum mismatch/match ratio"), 1, 4);
        grid.add(maxMismatchRatio,3,4);
        grid.add(new Label("Run deduplication on"), 1, 5);
        grid.add(charClassLink, 3, 5);
        grid.setHgrow(charClassLink, Priority.ALWAYS);
        grid.add(detailsLabel, 5, 5);

        Label headerLabel2 = new Label("Deduplication settings at characteristic level");
        headerLabel2.setUnderline(true);
        grid.add(headerLabel2, 1, 6);

        grid.add(caracWeightTable,1,7);
        GridPane.setColumnSpan(caracWeightTable,GridPane.REMAINING);
    }

    private static void skipToNextField(Node node) {
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
        /*if(node instanceof TextField){
            ((BehaviorSkinBase) ((TextField)node).getSkin()).getBehavior().traverseNext();
        }
        if(node instanceof ComboBox){
            ((BehaviorSkinBase) ((ComboBox)node).getSkin()).getBehavior().traverseNext();
        }*/
    }
}
