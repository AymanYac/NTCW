package scenes.paneScenes;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.util.ArrayList;


public class testController {
    @FXML public TableColumn clear;
    @FXML public TableColumn down;
    @FXML public TableColumn up;
    @FXML public TableColumn suffix;
    @FXML public TableColumn prefix;
    @FXML public TableColumn leftATableColumn;
    @FXML public TableColumn rightATableColumn;
    @FXML public TableColumn italic;
    @FXML public TableColumn bold;
    @FXML public TableColumn linebreak;
    @FXML public TableColumn translate;
    @FXML public TableColumn fieldName;
    @FXML public TableColumn position;
    @FXML public TableColumn field;
    @FXML public TableColumn example;
    @FXML public TableColumn add;

    @FXML
    BorderPane mainBorderPane;
    @FXML
    BorderPane titleBar;
    @FXML
    TableView<fieldDS> fieldTable;
    @FXML
    TableView<elemDS> elementTable;

    double r = 10;

    @FXML void initialize(){

        setElemsColumns();
        ArrayList<elemDS> elems = new ArrayList<>();
        elems.add(new elemDS("Description FR"));
        elems.add(new elemDS("Description IT"));
        elems.add(new elemDS("Description EN"));
        elems.add(new elemDS("PO FR"));
        elems.add(new elemDS("PO IT"));
        elems.add(new elemDS("PO EN"));
        elementTable.getItems().addListener(new ListChangeListener<elemDS>() {
            @Override
            public void onChanged(Change<? extends elemDS> c) {
                elementTable.getItems().forEach(item->item.position.set(elementTable.getItems().indexOf(item)));
                
            }
        });
        //elementTable.getItems().setAll(elems);

        setFieldsColumns();
        ArrayList<fieldDS> fields = new ArrayList<>();
        fields.add(new fieldDS("Article ID","00000180199"));
        fields.add(new fieldDS("INTERNAL NUMBER","ABRA0001"));
        fields.add(new fieldDS("Description FR","TRANSFORMATEUR TRIPHASE 18 KVA"));
        fields.add(new fieldDS("Description IT","TRASFORMATORE TRIFASE 18 KVA"));
        fields.add(new fieldDS("Description EN","THREE PHASE TRANSFORMER 18 KVA"));
        fields.add(new fieldDS("PO FR","TRANSFO. MONOPHASE NORME EN60742 SIMPLE ECRAN SECONDAIRE"));
        fields.add(new fieldDS("PO IT","TR.2500VA V.230-400/110-0-110|Completo di schermo elettrostatico"));
        fields.add(new fieldDS("PO EN","TRANSFO. SINGLE-PHASE EN60742 SEC.230V 250V A PRIM.380"));
        fields.add(new fieldDS("PLM Concatenation","Tension = 400 V | Type du raccordement = Vis | Hauteur d'encombrement = 2M"));
        fields.add(new fieldDS("PLM Manufacturer info","SIEMENS 3SB3400-3S"));
        fields.add(new fieldDS("Vendor information","SIEMENS SAS : 3SB3400-3S\n"));
        fields.add(new fieldDS("Material group","Repuestos mantenimiento"));
        fields.add(new fieldDS("Sourcing Family","Ceco almacén efectos y repuestos"));
        fields.add(new fieldDS("SSR","AETNA GROUP 0001354366"));
        fields.add(new fieldDS("Données de base","Type de composant : Mecanique | Fabriquant impose : oui | Ref 0001354366"));
        fieldTable.getItems().setAll(fields);
    }

    private void setFieldsColumns() {
        field.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<fieldDS, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<fieldDS, String> param) {
                return new ReadOnlyObjectWrapper(param.getValue().getFieldName());
            }
        });
        example.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<fieldDS, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<fieldDS, String> param) {
                return new ReadOnlyObjectWrapper(param.getValue().getValue());
            }
        });
        add.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<fieldDS, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<fieldDS, String> param) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().add(new elemDS(param.getValue().getFieldName()));
                        elementTable.refresh();
                    }
                });
                btn.getStylesheets().add(testController.class.getResource("/Styles/DescPane.css").toExternalForm());
                btn.getStyleClass().add("addButton");
                btn.setPrefSize(20,20);
                btn.setShape(new Circle(r));
                btn.setMinSize(2*r, 2*r);
                btn.setShape(new Circle(1.5));
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        //add.setCellValueFactory(new PropertyValueFactory<>("FieldName"));
    }

    private void setElemsColumns() {



        position.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<elemDS, String> param) {
                return new ReadOnlyObjectWrapper(String.valueOf(elementTable.getItems().indexOf(param.getValue())+1));
            }
        });
        fieldName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, String>, ObservableValue<String>>() {
              @Override
              public ObservableValue<String> call(TableColumn.CellDataFeatures<elemDS, String> param) {
                  return new ReadOnlyObjectWrapper(param.getValue().fieldName);
              }
          });
        translate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().translate);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        linebreak.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().linebreak);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        bold.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().bold);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        italic.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().italic);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        leftATableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().leftATableColumn);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        rightATableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().bindBidirectional(r.getValue().rightATableColumn);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        prefix.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                TextField tf = new TextField();
                tf.textProperty().bindBidirectional(r.getValue().prefix);
                return new ReadOnlyObjectWrapper(tf);
            }
        });
        suffix.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<elemDS, CheckBox> r) {
                TextField tf = new TextField();
                tf.textProperty().bindBidirectional(r.getValue().suffix);
                return new ReadOnlyObjectWrapper(tf);
            }
        });
        up.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<elemDS, Button> b) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().set(b.getValue().position.get(),elementTable.getItems().get(b.getValue().position.get()-1));
                        elementTable.getItems().set(b.getValue().position.get()-1,b.getValue());
                        elementTable.refresh();
                    }
                });
                btn.visibleProperty().bind(b.getValue().position.greaterThan(0));
                btn.getStylesheets().add(testController.class.getResource("/Styles/DescPane.css").toExternalForm());
                btn.getStyleClass().add("upButton");
                btn.setPrefSize(20,20);
                btn.setShape(new Circle(r));
                btn.setMinSize(2*r, 2*r);
                btn.setShape(new Circle(1.5));
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        down.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<elemDS, Button> b) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().set(b.getValue().position.get(),elementTable.getItems().get(b.getValue().position.get()+1));
                        elementTable.getItems().set(b.getValue().position.get()+1,b.getValue());
                        elementTable.refresh();
                    }
                });
                btn.visibleProperty().bind(b.getValue().position.lessThan(elementTable.getItems().size()-1));
                btn.getStylesheets().add(testController.class.getResource("/Styles/DescPane.css").toExternalForm());
                btn.getStyleClass().add("downButton");
                btn.setPrefSize(20,20);
                btn.setShape(new Circle(r));
                btn.setMinSize(2*r, 2*r);
                btn.setShape(new Circle(1.5));
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        clear.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<elemDS, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<elemDS, Button> b) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().remove(b.getValue());
                        elementTable.refresh();
                    }
                });
                btn.getStylesheets().add(testController.class.getResource("/Styles/DescPane.css").toExternalForm());
                btn.getStyleClass().add("clearButton");
                btn.setPrefSize(20,20);
                btn.setShape(new Circle(r));
                btn.setMinSize(2*r, 2*r);
                btn.setShape(new Circle(1.5));
                return new ReadOnlyObjectWrapper(btn);
            }
        });
    }

    private class elemDS {
        SimpleIntegerProperty position = new SimpleIntegerProperty();
        String fieldName;
        SimpleBooleanProperty translate = new SimpleBooleanProperty(false);
        SimpleBooleanProperty linebreak = new SimpleBooleanProperty(false);
        SimpleBooleanProperty bold = new SimpleBooleanProperty(false);
        SimpleBooleanProperty italic = new SimpleBooleanProperty(false);
        SimpleBooleanProperty leftATableColumn = new SimpleBooleanProperty(false);
        SimpleBooleanProperty rightATableColumn = new SimpleBooleanProperty(false);
        SimpleStringProperty prefix = new SimpleStringProperty();
        SimpleStringProperty suffix = new SimpleStringProperty();

        public elemDS(String fieldName, boolean translate, boolean lineBreak, boolean bold, boolean italic, boolean green, boolean white, String prefix, String suffix) {
            this.fieldName = fieldName;
            this.translate.set(translate);
            this.linebreak.set(lineBreak);
            this.bold.set(bold);
            this.italic.set(italic);
            this.leftATableColumn.set(green);
            this.rightATableColumn.set(white);
            this.prefix.set(prefix);
            this.suffix.set(suffix);
        }

        public elemDS(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    private class fieldDS {
        private String fieldName;
        private String value;

        public fieldDS(String fieldName, String fieldValue) {
            this.fieldName = fieldName;
            this.value = fieldValue;
        }
        public String getFieldName() {
            return fieldName;
        }
        public String getValue() {
            return value;
        }
    }
}
