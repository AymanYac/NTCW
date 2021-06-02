package transversal.dialog_toolbox;


import com.google.gson.reflect.TypeToken;
import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.Pair;
import model.ClassCaracteristic;
import model.ClassSegment;
import model.ClassSegmentClusterComboRow;
import model.GlobalConstants;
import service.CharValuesLoader;
import service.DeduplicationServices;
import transversal.data_exchange_toolbox.ComplexMap2JdbcObject;
import transversal.generic.Tools;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DedupLaunchDialog {

    private static Dialog<ClassCaracteristic>  dialog;
    private static GridPane grid;
    private static TextField minMatches;
    private static TextField maxMismatches;
    private static TextField minMatchMismatchRatio;

    private static ComboBox<ClassSegmentClusterComboRow> sourceCharClassLink;
    private static ComboBox<ClassSegmentClusterComboRow> targetCharClassLink;
    private static Label sourceDetailsLabel;
    private static Label targetDetailsLabel;
    private static TableView<DedupLaunchDialogRow> caracWeightTable;
    private static ButtonType validateButtonType;
    private static ButtonType resetButtonType;
    private static ButtonType saveButtonType;
    private static final ObservableList<Integer>  highlightRows = FXCollections.observableArrayList();
    private static SimpleBooleanProperty allCarsPropertySelected = new SimpleBooleanProperty();
    private static SimpleBooleanProperty allCarsPropertyUndetermined = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty sameCarsPropertySelected = new SimpleBooleanProperty();
    private static SimpleBooleanProperty sameCarsPropertyUndetermined = new SimpleBooleanProperty(false);
    private static Char_description parent;
    private static ClassSegment currentItemSegment;
    private static HashMap<String,HashMap<String,DedupLaunchDialogRow>> savedWeights;
    private static HashMap<Integer, TableColumn> columnBase = new HashMap<Integer, TableColumn>();


    private static void showDetailedClassClusters(ClassSegment itemSegment,ComboBox<ClassSegmentClusterComboRow> ClassLink) {
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
                        int activeCount = (int) ClassLink.getValue().getRowSegments().stream().filter(p->p.getValue().getValue()).count();
                        if(activeCount== ClassLink.getValue().getRowSegments().size()){
                            //All cluster classes are active
                            Optional<ClassSegmentClusterComboRow> fullCluster = ClassLink.getItems().stream().filter(r -> r.getRowSegments().stream().map(p -> p.getKey()).collect(Collectors.toCollection(ArrayList::new)).equals(ClassLink.getValue().getRowSegments().stream().map(p -> p.getKey()).collect(Collectors.toCollection(ArrayList::new)))).findAny();
                            ArrayList<ClassSegmentClusterComboRow> pureClusters = ClassLink.getItems().stream().filter(r -> !r.toString().endsWith("(ies)")).collect(Collectors.toCollection(ArrayList::new));
                            ClassLink.getItems().clear();
                            ClassLink.getItems().addAll(pureClusters);
                            fullCluster.ifPresent(classSegmentClusterComboRow -> ClassLink.getSelectionModel().select(classSegmentClusterComboRow));
                        }else if(activeCount>1){
                            //Cluster has been edited and at least one other class is active
                            ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow("This and "+String.valueOf(activeCount-1)+" other category(ies)", ClassLink.getValue().getRowSegments());
                            ClassLink.getItems().add(cc);
                            ClassLink.getSelectionModel().select(cc);
                        }else{
                            //Only the current class is active
                            ArrayList<ClassSegmentClusterComboRow> pureClusters = ClassLink.getItems().stream().filter(r -> !r.toString().endsWith("(ies)")).collect(Collectors.toCollection(ArrayList::new));
                            ClassLink.getItems().clear();
                            ClassLink.getItems().addAll(pureClusters);
                            Optional<ClassSegmentClusterComboRow> classOnlyRow = ClassLink.getItems().stream().filter(r -> r.toString().equals("This category only")).findAny();
                            if(classOnlyRow.isPresent()){
                                ClassLink.getSelectionModel().select(classOnlyRow.get());
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
        tableview.getItems().addAll(ClassLink.getValue().getRowSegments());

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

        DedupLaunchDialog.parent = parent;
        DedupLaunchDialog.currentItemSegment = currentItemSegment;
        loadLastSessionWeights();

        // Create the custom dialog.
        create_dialog();

        // Create the carac labels and fields.
        create_dialog_fields();

        // Set fields layout
        set_fields_layout();

        //Set fields behavior
        set_fields_behavior();

        dialog.getDialogPane().setContent(grid);

        // Request focus on the char name by default.
        Platform.runLater(() -> {
            minMatches.requestFocus();
            sourceCharClassLink.getSelectionModel().select(0);
            targetCharClassLink.getSelectionModel().select(targetCharClassLink.getItems().size()-1);
        });
        dialog.showAndWait();
    }

    private static void loadSavedWeights() {
    }
    public static void loadLastSessionWeights() throws SQLException, ClassNotFoundException {

        Connection conn = Tools.spawn_connection_from_pool();
        PreparedStatement stmt = conn.prepareStatement("select "
                + "deduplication_weights"
                + " from administration.users_x_projects where project_id = ? and user_id = ?");
        stmt.setString(1, parent.account.getActive_project());
        stmt.setString(2, parent.account.getUser_id());
        ResultSet rs = stmt.executeQuery();
        rs.next();
        try {
            DedupLaunchDialog.savedWeights = (HashMap<String, HashMap<String, DedupLaunchDialogRow>>) ComplexMap2JdbcObject.deserializeFX(rs.getString("deduplication_weights"),new TypeToken<HashMap<String,HashMap<String,DedupLaunchDialogRow>>>(){}.getType());
        }catch(Exception V) {
            V.printStackTrace(System.err);
            DedupLaunchDialog.savedWeights = new HashMap<String, HashMap<String, DedupLaunchDialogRow>>();
        }
        if(DedupLaunchDialog.savedWeights == null){
            DedupLaunchDialog.savedWeights = new HashMap<String, HashMap<String, DedupLaunchDialogRow>>();
        }
        rs.close();
        stmt.close();
        conn.close();


    }

    private static void set_fields_behavior() throws SQLException, ClassNotFoundException {
        HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(parent.account);
        ChangeListener<String> paramListner = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Integer.parseInt(minMatches.getText());
                    Integer.parseInt(maxMismatches.getText());
                    double minMatchMismatchRatio = 1.0 / (Double.parseDouble(DedupLaunchDialog.minMatchMismatchRatio.getText()));
                    ((Button) dialog.getDialogPane().lookupButton(validateButtonType)).setDisable(false);
                } catch (Exception V) {
                    ((Button) dialog.getDialogPane().lookupButton(validateButtonType)).setDisable(true);
                }
            }
        };
        minMatches.textProperty().addListener(paramListner);
        maxMismatches.textProperty().addListener(paramListner);
        minMatchMismatchRatio.textProperty().addListener(paramListner);
        allCarsPropertySelected.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                caracWeightTable.getItems().stream().filter(DedupLaunchDialogRow::isNotSpecialRow).forEach(r->r.setAllCarac(newValue));
                if(newValue){
                    caracWeightTable.getItems().stream().filter(DedupLaunchDialogRow::isNotSpecialRow).forEach(r->r.setSameCarac(true));
                    refreshSameCarsProperty();
                }
                DedupLaunchDialog.caracWeightTable.refresh();
            }
        });
        sameCarsPropertySelected.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                caracWeightTable.getItems().stream().filter(Objects::nonNull).forEach(r->r.setSameCarac(newValue));
                if(!newValue){
                    caracWeightTable.getItems().stream().filter(Objects::nonNull).forEach(r->r.setAllCarac(false));
                    refreshSameCarsProperty();
                }
                DedupLaunchDialog.caracWeightTable.refresh();
            }
        });


        sourceCharClassLink.getItems().clear();
        targetCharClassLink.getItems().clear();
        IntStream.range(0,currentItemSegment.getSegmentGranularity()).forEach(lvl->{
            ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(lvl, currentItemSegment,sid2Segment);
            sourceCharClassLink.getItems().add(cc);
            targetCharClassLink.getItems().add(cc);
        });
        Collections.reverse(sourceCharClassLink.getItems());
        Collections.reverse(targetCharClassLink.getItems());
        ClassSegmentClusterComboRow cc = new ClassSegmentClusterComboRow(sid2Segment);
        sourceCharClassLink.getItems().add(cc);
        targetCharClassLink.getItems().add(cc);
        if(!GlobalConstants.DEDUP_CARAC_WISE) {
            sourceCharClassLink.valueProperty().addListener(new ChangeListener<ClassSegmentClusterComboRow>() {
                @Override
                public void changed(ObservableValue<? extends ClassSegmentClusterComboRow> observable, ClassSegmentClusterComboRow oldValue, ClassSegmentClusterComboRow newValue) {
                    targetCharClassLink.setValue(newValue);
                }
            });
            targetCharClassLink.valueProperty().addListener(new ChangeListener<ClassSegmentClusterComboRow>() {
                @Override
                public void changed(ObservableValue<? extends ClassSegmentClusterComboRow> observable, ClassSegmentClusterComboRow oldValue, ClassSegmentClusterComboRow targetValue) {
                    fillWeightTable(false);
                }
            });
        }else{
            sourceCharClassLink.valueProperty().bindBidirectional(targetCharClassLink.valueProperty());
            targetCharClassLink.valueProperty().addListener(new ChangeListener<ClassSegmentClusterComboRow>() {
                @Override
                public void changed(ObservableValue<? extends ClassSegmentClusterComboRow> observable, ClassSegmentClusterComboRow oldValue, ClassSegmentClusterComboRow newValue) {
                    fillWeightTablePairWise(oldValue,oldValue,false);
                }
            });
        }

        sourceDetailsLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showDetailedClassClusters(currentItemSegment,sourceCharClassLink);
            }
        });
        targetDetailsLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showDetailedClassClusters(currentItemSegment,targetCharClassLink);
            }
        });
        Button validationButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
        validationButton.setDisable(true);
        validationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                ArrayList<String> sourceSegmentIDS = sourceCharClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
                ArrayList<String> targetSegmentIDS = targetCharClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
                HashMap<String, ArrayList<Object>> weightTable = new HashMap<>();
                caracWeightTable.getItems().forEach(r->{
                    ArrayList<Object> tmp = new ArrayList<Object>();
                    tmp.add(r.getCarac());//Carac
                    tmp.add(r.getStrongWeight());//Strong
                    tmp.add(r.getWeakWeight());//Weak
                    tmp.add(r.getIncludedWeight());//Included
                    tmp.add(r.getAlternativeWeight());//Alternative
                    tmp.add(r.getUnknownWeight());//Unknown
                    tmp.add(r.getMismatchWeight());//Mismatch

                    if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
                        weightTable.put(r.getCarac().getCharacteristic_name(),tmp);
                    }else{
                        weightTable.put(r.getCarac().getCharacteristic_id(),tmp);
                    }
                });
                if(GlobalConstants.DEDUP_CARAC_WISE){
                    DeduplicationServices.scoreDuplicatesForClassesPairWise(sourceSegmentIDS,weightTable, Integer.parseInt(minMatches.getText()), Integer.parseInt(maxMismatches.getText()),  1.0 / (Double.parseDouble(minMatchMismatchRatio.getText())));
                }else{
                    try {
                        DeduplicationServices.scoreDuplicatesForClassesFull(sourceSegmentIDS,targetSegmentIDS,weightTable, Integer.parseInt(minMatches.getText()), Integer.parseInt(maxMismatches.getText()), 1.0 / (Double.parseDouble(minMatchMismatchRatio.getText())), parent);
                    } catch (SQLException | ClassNotFoundException | IOException throwables) {
                        throwables.printStackTrace(System.err);
                    }
                }
                showReport();
            }
        });

        Button saveBT = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBT.addEventFilter(ActionEvent.ACTION, event -> {
            HashMap<String, DedupLaunchDialogRow> tmp = new HashMap<String, DedupLaunchDialogRow>();
            caracWeightTable.getItems().stream().filter(r->r!=null && r.isNotSpecialRow()).forEach(r->{
                if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
                    tmp.put(r.getCarac().getCharacteristic_name(),r);
                }else{
                    tmp.put(r.getCarac().getCharacteristic_id(),r);
                }
            });
            DedupLaunchDialogRow.saveWeights(currentItemSegment.getSegmentId(),tmp);
            event.consume();
        });
        Button resetBT = (Button) dialog.getDialogPane().lookupButton(resetButtonType);
        resetBT.addEventFilter(ActionEvent.ACTION, event -> {
            fillWeightTable(true);
            event.consume();
        });

    }

    private static void fillWeightTablePairWise(ClassSegmentClusterComboRow value, ClassSegmentClusterComboRow valueCopy, boolean resetWeights) {
        caracWeightTable.getItems().clear();
        //caracWeightTable.getItems().add(null);
        caracWeightTable.getItems().add(new DedupLaunchDialogRow());
        ClassCaracteristic tmp = new ClassCaracteristic();
        tmp.setSequence(0);
        tmp.setCharacteristic_name("Item Class");
        tmp.setCharacteristic_id("CLASS_ID");
        caracWeightTable.getItems().add(new DedupLaunchDialogRow(tmp,resetWeights));
        caracWeightTable.getItems().addAll(GenerateWeightList(value, valueCopy, resetWeights));
        refreshAllCarsProperty();
        refreshSameCarsProperty();
    }

    private static void fillWeightTable(boolean resetWeights) {
        caracWeightTable.getItems().clear();
        highlightRows.clear();
        //caracWeightTable.getItems().add(null);
        caracWeightTable.getItems().add(new DedupLaunchDialogRow());
        ClassCaracteristic tmp = new ClassCaracteristic();
        tmp.setSequence(0);
        tmp.setCharacteristic_name("Item Class");
        tmp.setCharacteristic_id("CLASS_ID");
        caracWeightTable.getItems().add(new DedupLaunchDialogRow(tmp,resetWeights));
        caracWeightTable.getItems().addAll(GenerateWeightList(sourceCharClassLink.getValue(), targetCharClassLink.getValue(),resetWeights));
        highlightRows.setAll(Collections.singleton(0));
        refreshAllCarsProperty();
        refreshSameCarsProperty();
        caracWeightTable.refresh();
    }

    private static ArrayList<DedupLaunchDialogRow> GenerateWeightList(ClassSegmentClusterComboRow source, ClassSegmentClusterComboRow target, boolean resetWeights) {
        if(source==null || target == null){
            return new ArrayList<DedupLaunchDialogRow>();
        }
        if(!GlobalConstants.DEDUP_CARAC_WISE){
            if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
                HashSet<String> uniqueNameCarac = new HashSet<String>();
                ArrayList<DedupLaunchDialogRow> sourceCars = source.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(Objects::nonNull).flatMap(Collection::stream).filter(c -> uniqueNameCarac.add(c.getCharacteristic_name()))
                        //.map(car -> new Pair<ClassCaracteristic, Pair<BooleanProperty[],ArrayList<String>>>(car, new Pair<BooleanProperty[],ArrayList<String>>(new BooleanProperty[]{new SimpleBooleanProperty(true),new SimpleBooleanProperty(true)},new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"))))))
                        .map(car -> new DedupLaunchDialogRow(car,resetWeights))
                        .collect(Collectors.toCollection(ArrayList::new));
                ArrayList<DedupLaunchDialogRow> targetCars = target.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(Objects::nonNull).flatMap(Collection::stream).filter(c -> uniqueNameCarac.add(c.getCharacteristic_name()))
                        //.map(car -> new Pair<ClassCaracteristic, Pair<BooleanProperty[],ArrayList<String>>>(car, new Pair<BooleanProperty[],ArrayList<String>>(new BooleanProperty[]{new SimpleBooleanProperty(true),new SimpleBooleanProperty(true)},new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"))))))
                        .map(car -> new DedupLaunchDialogRow(car,resetWeights))
                        .collect(Collectors.toCollection(ArrayList::new));
                sourceCars.addAll(targetCars);
                return sourceCars;

            }
            HashSet<ClassCaracteristic> uniqueIDCars = source.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet<ClassCaracteristic>::new));
            uniqueIDCars.addAll(target.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet<ClassCaracteristic>::new)));
            return uniqueIDCars.stream()
                    //.map(car -> new Pair<ClassCaracteristic, Pair<BooleanProperty[],ArrayList<String>>>(car, new Pair<BooleanProperty[],ArrayList<String>>(new BooleanProperty[]{new SimpleBooleanProperty(true),new SimpleBooleanProperty(true)},new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"))))))
                    .map(car -> new DedupLaunchDialogRow(car,resetWeights))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        HashSet<String> retainedCars = null;
        for (Pair<ClassSegment, SimpleBooleanProperty> classSegmentSimpleBooleanPropertyPair : source.getRowSegments()) {
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
        ArrayList<ClassCaracteristic> uniqueIDCarac = source.getRowSegments().stream().filter(s -> s.getValue().getValue()).map(s -> CharValuesLoader.active_characteristics.get(s.getKey().getSegmentId())).filter(Objects::nonNull).flatMap(Collection::stream).filter(car -> finalRetainedCars.remove(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID ? car.getCharacteristic_name() : car.getCharacteristic_id())).collect(Collectors.toCollection(ArrayList::new));
        return uniqueIDCarac.stream()
                //.map(car -> new Pair<ClassCaracteristic, Pair<BooleanProperty[],ArrayList<String>>>(car, new Pair<BooleanProperty[],ArrayList<String>>(new BooleanProperty[]{new SimpleBooleanProperty(true),new SimpleBooleanProperty(true)},new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"))))))
                .map(car -> new DedupLaunchDialogRow(car,resetWeights))
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
        saveButtonType = new ButtonType("Save weights", ButtonData.LEFT);
        resetButtonType = new ButtonType("Reset weights", ButtonBar.ButtonData.LEFT);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType,resetButtonType,validateButtonType, cancelButtonType);

    }
    @SuppressWarnings("static-access")
    private static void create_dialog_fields() {
        grid = new GridPane();
        grid.setMinWidth(Math.floor(Screen.getPrimary().getBounds().getWidth() * 0.85));
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
        minMatchMismatchRatio = new TextField();
        minMatchMismatchRatio.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    skipToNextField(minMatchMismatchRatio);

                }
            }
        });

        sourceCharClassLink = new ComboBox<ClassSegmentClusterComboRow>();
        sourceCharClassLink.setMaxWidth(Integer.MAX_VALUE);
        sourceCharClassLink.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    //skipToNextField(charClassLink);

                }
            }
        });
        targetCharClassLink = new ComboBox<ClassSegmentClusterComboRow>();
        targetCharClassLink.setMaxWidth(Integer.MAX_VALUE);
        targetCharClassLink.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    event.consume();
                    //skipToNextField(charClassLink);

                }
            }
        });

        sourceDetailsLabel = new Label("View details...");
        sourceDetailsLabel.setUnderline(true);
        sourceDetailsLabel.setTextAlignment(TextAlignment.CENTER);
        sourceDetailsLabel.setFont(Font.font(sourceDetailsLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, sourceDetailsLabel.getFont().getSize()));
        grid.setHalignment(sourceDetailsLabel, HPos.CENTER);
        targetDetailsLabel = new Label("View details...");
        targetDetailsLabel.setUnderline(true);
        targetDetailsLabel.setTextAlignment(TextAlignment.CENTER);
        targetDetailsLabel.setFont(Font.font(targetDetailsLabel.getFont().getName(), FontWeight.LIGHT, FontPosture.ITALIC, targetDetailsLabel.getFont().getSize()));
        grid.setHalignment(targetDetailsLabel, HPos.CENTER);

        caracWeightTable = new TableView<DedupLaunchDialogRow>();
        caracWeightTable.setEditable(true);
        caracWeightTable.getSelectionModel().setCellSelectionEnabled(true);
        caracWeightTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        caracWeightTable.getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>() {
            @Override
            public void onChanged(Change<? extends TablePosition> p) {
                p.next();
                if(p.getAddedSubList().stream().filter(c->c.getColumn()<4).findAny().isPresent()){
                    ArrayList<Pair<Integer,Integer>> validSelection = caracWeightTable.getSelectionModel().getSelectedCells().stream().filter(c -> c.getColumn() > 3).map(c -> new Pair<Integer,Integer>(c.getRow(), c.getColumn())).collect(Collectors.toCollection(ArrayList::new));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            caracWeightTable.getSelectionModel().clearSelection();
                            validSelection.forEach(pair->{
                                caracWeightTable.getSelectionModel().selectRange(pair.getKey(),columnBase.get(pair.getValue()),pair.getKey(),columnBase.get(pair.getValue()));
                            });
                        }
                    });
                }
            }
        });
        // switch to edit mode on keypress
        // this must be KeyEvent.KEY_PRESSED so that the key gets forwarded to the editing cell; it wouldn't be forwarded on KEY_RELEASED
        caracWeightTable.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if( event.getCode() == KeyCode.ENTER) {
//                  event.consume(); // don't consume the event or else the values won't be updated;
                    return;
                }

                // switch to edit mode on keypress, but only if we aren't already in edit mode
                if( caracWeightTable.getEditingCell() == null) {
                    if( event.getCode().isLetterKey() || event.getCode().isDigitKey()) {

                        TablePosition focusedCellPosition = caracWeightTable.getFocusModel().getFocusedCell();
                        caracWeightTable.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());

                    }
                }

            }
        });
        caracWeightTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.D) && event.isControlDown()){
                    DedupLaunchDialogRow firstRow = caracWeightTable.getItems().get(caracWeightTable.getSelectionModel().getSelectedIndices().stream().min(Comparator.naturalOrder()).get());
                    caracWeightTable.getSelectionModel().getSelectedCells().forEach(c->{
                        int itemIdx = c.getRow();
                        int weightIdx = c.getColumn()-4;
                        caracWeightTable.getItems().get(itemIdx).getWeights().set(weightIdx,firstRow.getWeights().get(weightIdx));
                    });
                    caracWeightTable.refresh();
                }
            }
        });
        caracWeightTable.setRowFactory(new Callback<TableView<DedupLaunchDialogRow>, TableRow<DedupLaunchDialogRow>>() {
            @Override
            public TableRow<DedupLaunchDialogRow> call(TableView<DedupLaunchDialogRow> tableView) {
                final TableRow<DedupLaunchDialogRow> row = new TableRow<DedupLaunchDialogRow>() {
                    @Override
                    protected void updateItem(DedupLaunchDialogRow row, boolean empty){
                        super.updateItem(row, empty);
                        if (highlightRows.contains(getIndex())) {
                            if (! getStyleClass().contains("highlightedRow")) {
                                getStyleClass().add("highlightedRow");
                            }
                        } else {
                            getStyleClass().removeAll(Collections.singleton("highlightedRow"));
                        }
                    }
                };
                highlightRows.addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> change) {
                        if (highlightRows.contains(row.getIndex())) {
                            if (! row.getStyleClass().contains("highlightedRow")) {
                                row.getStyleClass().add("highlightedRow");
                            }
                        } else {
                            row.getStyleClass().removeAll(Collections.singleton("highlightedRow"));
                        }
                    }
                });
                return row;
            }
        });
        caracWeightTable.sortPolicyProperty().set(t -> {
            Comparator<DedupLaunchDialogRow> comparator = (r1, r2)
                    //-> r1.getCarac().getSequence()==0 ? -1 //this row on top
                    -> r1==null ? -1 //this row on top
                    //: r2.getCarac().getSequence()==0 ? 1 //this row on top
                    : r2==null ? 1 //this row on top
                    : t.getComparator() == null ? 0 //no column sorted: don't change order
                    : t.getComparator().compare(r1, r2); //columns are sorted: sort accordingly
            FXCollections.sort(caracWeightTable.getItems(), comparator);
            return true;
        });
        TableColumn col0 = new TableColumn("Sequence");
        col0.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, Integer>, ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, Integer> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getCarac().getSequence());
                }else{
                    return new ReadOnlyObjectWrapper("");
                }
            }
        });
        col0.setResizable(false);
        col0.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col1 = new TableColumn("Characteristic name");
        col1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getCarac().getCharacteristic_name());
                }else{
                    return new ReadOnlyObjectWrapper("* ALL CHARACTERISTICS *");
                }
            }
        });
        col1.setResizable(false);
        col1.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(26.0 / 100.0));

        TableColumn sameCarCol = new TableColumn("Same charac.");
        sameCarCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    CheckBox tmp = new CheckBox();
                    tmp.selectedProperty().bindBidirectional(r.getValue().sameCaracProperty());
                    tmp.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (!newValue) {
                                r.getValue().allCaracProperty().setValue(false);
                            }
                            refreshSameCarsProperty();
                        }
                    });
                    return new ReadOnlyObjectWrapper(tmp);
                }else{
                    CheckBox tmp = new CheckBox();
                    tmp.selectedProperty().bindBidirectional(sameCarsPropertySelected);
                    tmp.indeterminateProperty().bindBidirectional(sameCarsPropertyUndetermined);
                    return new ReadOnlyObjectWrapper(tmp);
                }
            }
        });
        sameCarCol.setResizable(false);
        sameCarCol.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0 / 100.0));

        TableColumn allDataCol = new TableColumn("All data");
        allDataCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    CheckBox tmp = new CheckBox();
                    tmp.selectedProperty().bindBidirectional(r.getValue().allCaracProperty());
                    tmp.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (newValue) {
                                r.getValue().sameCaracProperty().setValue(true);
                            }
                            refreshAllCarsProperty();
                        }
                    });
                    return new ReadOnlyObjectWrapper(tmp);
                }else {
                    CheckBox tmp = new CheckBox();
                    tmp.selectedProperty().bindBidirectional(allCarsPropertySelected);
                    tmp.indeterminateProperty().bindBidirectional(allCarsPropertyUndetermined);
                    return new ReadOnlyObjectWrapper(tmp);
                }
            }
        });
        allDataCol.setResizable(false);
        allDataCol.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0 / 100.0));

        EventHandler<TableColumn.CellEditEvent<DedupLaunchDialogRow, String>> editCommitHandler = new EventHandler<TableColumn.CellEditEvent<DedupLaunchDialogRow, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<DedupLaunchDialogRow, String> t) {
                String newVal;
                try {
                    newVal = String.valueOf(Double.valueOf(t.getNewValue()));
                } catch (Exception V) {
                    t.getTableView().refresh();
                    return;
                }
                t.getTableView().getSelectionModel().getSelectedCells().forEach(c->{
                    if(c.getColumn()<4){
                        return;
                    }
                    if (t.getTableView().getItems().get(c.getRow()).isNotSpecialRow()) {
                        ((DedupLaunchDialogRow) t.getTableView().getItems().get(
                                c.getRow())
                        ).getWeights().set(c.getColumn()-4, newVal);
                    } else {
                        t.getTableView().getItems().stream().filter(r -> r.isNotSpecialRow()).forEach(r -> r.getWeights().set(c.getColumn()-4, newVal));
                    }

                });
                t.getTableView().refresh();
            }
        };

        TableColumn col2 = new TableColumn("Strong");
        col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getStrongWeight());
                }else{
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getStrongWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col2.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col2.setOnEditCommit(editCommitHandler);
        col2.setResizable(false);
        col2.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col3 = new TableColumn("Weak");
        col3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getWeakWeight());
                }else{
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getWeakWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col3.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col3.setOnEditCommit(editCommitHandler);
        col3.setResizable(false);
        col3.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col4 = new TableColumn("Description");
        col4.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getIncludedWeight());
                }else {
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getIncludedWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col4.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col4.setOnEditCommit(editCommitHandler);
        col4.setResizable(false);
        col4.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col5 = new TableColumn("Alternative");
        col5.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getAlternativeWeight());
                }else {
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getAlternativeWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col5.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col5.setOnEditCommit(editCommitHandler);
        col5.setResizable(false);
        col5.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col6 = new TableColumn("Unknown");
        col6.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getUnknownWeight());
                }else {
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getUnknownWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col6.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col6.setOnEditCommit(editCommitHandler);
        col6.setResizable(false);
        col6.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        TableColumn col7 = new TableColumn("Mismatch");
        col7.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DedupLaunchDialogRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DedupLaunchDialogRow, String> r) {
                if(r.getValue().isNotSpecialRow()){
                    return new ReadOnlyObjectWrapper(r.getValue().getMismatchWeight());
                }else {
                    HashSet<String> distinctValues = DedupLaunchDialog.caracWeightTable.getItems().stream().filter(row->row.isNotSpecialRow()).map(row -> row.getMismatchWeight()).collect(Collectors.toCollection(HashSet::new));
                    if(distinctValues.size()==1){
                        return new ReadOnlyObjectWrapper<>(distinctValues.iterator().next());
                    }
                    return new ReadOnlyObjectWrapper<>("Multiple");
                }
            }
        });
        col7.setCellFactory(TextFieldTableCell.<DedupLaunchDialogRow>forTableColumn());
        col7.setOnEditCommit(editCommitHandler);
        col7.setResizable(false);
        col7.prefWidthProperty().bind(caracWeightTable.widthProperty().multiply(8.0/100.0));

        caracWeightTable.getColumns().add(col0);
        columnBase.put(0,col0);
        caracWeightTable.getColumns().add(col1);
        columnBase.put(1,col1);
        caracWeightTable.getColumns().add(sameCarCol);
        columnBase.put(2,sameCarCol);
        caracWeightTable.getColumns().add(allDataCol);
        columnBase.put(3,allDataCol);
        caracWeightTable.getColumns().add(col2);
        columnBase.put(4,col2);
        caracWeightTable.getColumns().add(col3);
        columnBase.put(5,col3);
        caracWeightTable.getColumns().add(col4);
        columnBase.put(6,col4);
        caracWeightTable.getColumns().add(col5);
        columnBase.put(7,col5);
        caracWeightTable.getColumns().add(col6);
        columnBase.put(8,col6);
        caracWeightTable.getColumns().add(col7);
        columnBase.put(9,col7);


    }

    private static void refreshAllCarsProperty() {
        long selected = caracWeightTable.getItems().stream().filter(r -> r.isNotSpecialRow() && r.allCaracProperty().getValue()).count();
        if(selected == caracWeightTable.getItems().size()-1){
            allCarsPropertySelected.setValue(true);
            allCarsPropertyUndetermined.setValue(false);
        }else if(selected == 0){
            allCarsPropertySelected.setValue(false);
            allCarsPropertyUndetermined.setValue(false);
        }else{
            allCarsPropertyUndetermined.setValue(true);
        }
    }
    private static void refreshSameCarsProperty() {
        long selected = caracWeightTable.getItems().stream().filter(r -> r.isNotSpecialRow() && r.sameCaracProperty().getValue()).count();
        if(selected == caracWeightTable.getItems().size()-1){
            sameCarsPropertySelected.setValue(true);
            sameCarsPropertyUndetermined.setValue(false);
        }else if(selected == 0){
            sameCarsPropertySelected.setValue(false);
            sameCarsPropertyUndetermined.setValue(false);
        }else{
            sameCarsPropertyUndetermined.setValue(true);
        }
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
        Label headerLabel1 = new Label("General settings");
        headerLabel1.setUnderline(true);
        grid.add(headerLabel1, 1, 0);

        grid.add(   new Label("Minimum matches"), 1, 2);
        grid.add(minMatches,3,2);
        grid.add(new Label("Maximum mismatches"), 1, 3);
        grid.add(maxMismatches,3,3);
        grid.add(new Label("Minimum match/mismatch ratio"), 1, 4);
        grid.add(minMatchMismatchRatio,3,4);
        grid.add(new Label("Compare items of:"), 1, 5);
        grid.add(sourceCharClassLink, 3, 5);
        grid.setHgrow(sourceCharClassLink, Priority.ALWAYS);
        grid.add(sourceDetailsLabel, 5, 5);
        grid.add(new Label("Compare with items of:"), 1, 6);
        grid.add(targetCharClassLink, 3, 6);
        grid.setHgrow(targetCharClassLink, Priority.ALWAYS);
        grid.add(targetDetailsLabel, 5, 6);

        Label headerLabel2 = new Label("Characteristic settings");
        headerLabel2.setUnderline(true);
        grid.add(headerLabel2, 1, 7);

        Label transLabel1 = new Label("Compare values with:");
        grid.add(transLabel1,1,8);
        transLabel1.translateXProperty().bind(caracWeightTable.widthProperty().multiply(0.36));
        Label transLabel2 = new Label("Scoring weight by comparison result type");
        grid.add(transLabel2,1,8);
        transLabel2.translateXProperty().bind(caracWeightTable.widthProperty().multiply(0.52));


        grid.add(caracWeightTable,1,9);
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

    private static class DedupLaunchDialogRow {
        private final ClassCaracteristic carac;
        private final SimpleBooleanProperty sameCarac;
        private final SimpleBooleanProperty allCarac;
        private final ArrayList<String> weights;
        private final boolean specialRow;

        public DedupLaunchDialogRow(ClassCaracteristic tmp,boolean resetWeights) {
            this.carac = tmp;
            DedupLaunchDialogRow previousWeight = null;
            try{
                previousWeight = DedupLaunchDialog.savedWeights.get(currentItemSegment.getSegmentId()).get(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID ? tmp.getCharacteristic_name() : tmp.getCharacteristic_id());
            }catch (Exception V){

            }
            if(!resetWeights && previousWeight!=null){
                this.sameCarac = previousWeight.sameCaracProperty();
                this.allCarac = previousWeight.allCaracProperty();
                this.weights = previousWeight.weights;
            }else{
                this.sameCarac = new SimpleBooleanProperty(true);
                this.allCarac = new SimpleBooleanProperty(true);
                this.weights = new ArrayList<>(Arrays.asList(new String("2.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("0.0"), new String("-2.0")));
            }
            this.specialRow = false;
            //new Pair<ClassCaracteristic, Pair<BooleanProperty[],ArrayList<String>>>(tmp, new Pair<BooleanProperty[],ArrayList<String>>(new BooleanProperty[]{new SimpleBooleanProperty(true),new SimpleBooleanProperty(true)},new ArrayList<>(Arrays.asList(new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("1.0")))))
        }

        public DedupLaunchDialogRow() {
            ClassCaracteristic tmp = new ClassCaracteristic();
            tmp.setSequence(0);
            tmp.setCharacteristic_name("Item Class");
            tmp.setCharacteristic_id("CLASS_ID");
            this.carac = tmp;
            this.sameCarac = new SimpleBooleanProperty(true);
            this.allCarac = new SimpleBooleanProperty(true);
            this.weights = new ArrayList<>(Arrays.asList(new String("2.0"), new String("1.0"), new String("1.0"), new String("1.0"), new String("0.0"), new String("-2.0")));
            this.specialRow = true;
        }

        public static void saveWeights(String segmentId, HashMap<String, DedupLaunchDialogRow> tmp) {
            DedupLaunchDialog.savedWeights.put(segmentId,tmp);
            new Thread (()->{
                Connection conn = null;
                PreparedStatement stmt=null;
                try {
                    conn = Tools.spawn_connection_from_pool();
                    stmt = conn.prepareStatement("update users_x_projects set deduplication_weights = ? where project_id = ? and user_id = ?");
                    stmt.setString(1, ComplexMap2JdbcObject.serializeFX(DedupLaunchDialog.savedWeights));
                    stmt.setString(2,parent.account.getActive_project());
                    stmt.setString(3,parent.account.getUser_id());
                    stmt.execute();
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    stmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }).start();
        }

        public ClassCaracteristic getCarac() {
            return carac;
        }

        public String getStrongWeight() {
            return getWeights().get(0);
        }

        public String getWeakWeight() {
            return getWeights().get(1);
        }

        public String getIncludedWeight() {
            return getWeights().get(2);
        }

        public String getAlternativeWeight() {
            return getWeights().get(3);
        }

        public String getUnknownWeight() {
            return getWeights().get(4);
        }

        public String getMismatchWeight() {
            return getWeights().get(5);
        }

        public ArrayList<String> getWeights() {
            return this.weights;
        }

        public boolean isSameCarac() {
            return sameCarac.get();
        }

        public SimpleBooleanProperty sameCaracProperty() {
            return sameCarac;
        }

        public void setSameCarac(boolean sameCarac) {
            this.sameCarac.set(sameCarac);
        }

        public boolean isAllCarac() {
            return allCarac.get();
        }

        public SimpleBooleanProperty allCaracProperty() {
            return allCarac;
        }

        public void setAllCarac(boolean allCarac) {
            this.allCarac.set(allCarac);
        }

        public boolean isNotSpecialRow() {
            return !specialRow;
        }
    }
}
