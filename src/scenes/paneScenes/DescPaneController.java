package scenes.paneScenes;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.CharDescriptionRow;
import model.DescriptionDataElement;
import model.DescriptionDisplayElement;
import model.UserAccount;
import org.fxmisc.richtext.StyleClassedTextArea;
import transversal.generic.TextUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class DescPaneController {
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
    @FXML public TableColumn field;
    @FXML public TableColumn example;
    @FXML public TableColumn add;
    @FXML public Button incFontButton;
    @FXML public Button decFontButton;
    @FXML public Button applyButton;
    @FXML public Button cancelButton;
    @FXML StyleClassedTextArea previewArea;

    @FXML BorderPane mainBorderPane;
    @FXML BorderPane titleBar;
    @FXML TableView<DescriptionDataElement> fieldTable;
    @FXML TableView<DescriptionDisplayElement> elementTable;

    private Char_description parent;
    private UserAccount account;
    private Integer parentRowIndex;
    private Integer parentColumnIndex;

    @FXML void initialize(){

        setElemsColumns();
        elementTable.getItems().addListener(new ListChangeListener<DescriptionDisplayElement>() {
            @Override
            public void onChanged(Change<? extends DescriptionDisplayElement> c) {
                elementTable.getItems().forEach(item->item.position.set(elementTable.getItems().indexOf(item)));
                refresh_preview();
            }
        });
        setFieldsColumns();
        incFontButton.setVisible(!DescriptionDisplayElement.fontSizeMode.equals("bigFont"));
        decFontButton.setVisible(!DescriptionDisplayElement.fontSizeMode.equals("smallFont"));
        incFontButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String fontMode = DescriptionDisplayElement.getFontSizeForKey(parentRowIndex.toString() + parentColumnIndex.toString());
                if(fontMode.equals("midFont")){
                    incFontButton.setVisible(false);
                    DescriptionDisplayElement.fontSizeMode.put(parentRowIndex.toString() + parentColumnIndex.toString(),"bigFont");
                }else{
                    //fontSizeMode = smallFont
                    decFontButton.setVisible(true);
                    DescriptionDisplayElement.fontSizeMode.put(parentRowIndex.toString() + parentColumnIndex.toString(),"midFont");
                }
                refresh_preview();
            }
        });
        decFontButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String fontMode = DescriptionDisplayElement.getFontSizeForKey(parentRowIndex.toString() + parentColumnIndex.toString());
                if(fontMode.equals("midFont")){
                    decFontButton.setVisible(false);
                    DescriptionDisplayElement.fontSizeMode.put(parentRowIndex.toString() + parentColumnIndex.toString(),"smallFont");
                }else{
                    //fontSizeMode = smallFont
                    incFontButton.setVisible(true);
                    DescriptionDisplayElement.fontSizeMode.put(parentRowIndex.toString() + parentColumnIndex.toString(),"midFont");
                }
                refresh_preview();
            }
        });
        //fillDummyItems();
    }

    private void refresh_preview() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TextUtils.renderDescription(previewArea,elementTable.getItems(),fieldTable.getItems(),previewArea.widthProperty(),DescriptionDisplayElement.getFontSizeForKey(parentRowIndex.toString()+parentColumnIndex.toString()));
                previewArea.setWrapText(true);
            }
        });
    }

    private void setFieldsColumns() {
        field.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDataElement, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DescriptionDataElement, String> param) {
                return new ReadOnlyObjectWrapper(param.getValue().getFieldName());
            }
        });
        example.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDataElement, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DescriptionDataElement, String> param) {
                return new ReadOnlyObjectWrapper(param.getValue().getValue());
            }
        });
        add.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDataElement, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DescriptionDataElement, String> param) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().add(DescriptionDisplayElement.createDescriptionDisplayElement(param.getValue().getFieldName()));
                        elementTable.refresh();
                    }
                });
                btn.getStyleClass().add("addButton");
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        //add.setCellValueFactory(new PropertyValueFactory<>("FieldName"));
    }

    private void setElemsColumns() {


        fieldName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, String>, ObservableValue<String>>() {
              @Override
              public ObservableValue<String> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, String> param) {
                  return new ReadOnlyObjectWrapper(param.getValue().fieldName);
              }
          });
        translate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().translate);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        linebreak.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().linebreak);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        bold.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().bold);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        italic.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().italic);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        leftATableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue){
                            r.getValue().rightATableColumn.set(false);
                        }
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().leftATableColumn);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        rightATableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                CheckBox cb = new CheckBox();
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue){
                            r.getValue().leftATableColumn.set(false);
                        }
                        refresh_preview();
                    }
                });
                cb.selectedProperty().bindBidirectional(r.getValue().rightATableColumn);
                return new ReadOnlyObjectWrapper(cb);
            }
        });
        prefix.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                TextField tf = new TextField();
                tf.textProperty().bindBidirectional(r.getValue().prefix);
                return new ReadOnlyObjectWrapper(tf);
            }
        });
        suffix.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox>, ObservableValue<CheckBox>>() {
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, CheckBox> r) {
                TextField tf = new TextField();
                tf.textProperty().bindBidirectional(r.getValue().suffix);
                return new ReadOnlyObjectWrapper(tf);
            }
        });
        up.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, Button> b) {
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
                btn.getStyleClass().add("upButton");
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        down.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, Button> b) {
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
                btn.getStyleClass().add("downButton");
                return new ReadOnlyObjectWrapper(btn);
            }
        });
        clear.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DescriptionDisplayElement, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<DescriptionDisplayElement, Button> b) {
                Button btn = new Button();
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        elementTable.getItems().remove(b.getValue());
                        elementTable.refresh();
                    }
                });
                btn.getStyleClass().add("clearButton");
                return new ReadOnlyObjectWrapper(btn);
            }
        });
    }

    public void fillDummyItems(){
        ArrayList<DescriptionDisplayElement> elems = new ArrayList<>();
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("Description FR"));
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("Description IT"));
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("Description EN"));
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("PO FR"));
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("PO IT"));
        elems.add(DescriptionDisplayElement.createDescriptionDisplayElement("PO EN"));
        ArrayList<DescriptionDataElement> fields = new ArrayList<>();
        fields.add(new DescriptionDataElement("Article ID","00000180199"));
        fields.add(new DescriptionDataElement("INTERNAL NUMBER","ABRA0001"));
        fields.add(new DescriptionDataElement("Description FR","TRANSFORMATEUR TRIPHASE 18 KVA"));
        fields.add(new DescriptionDataElement("Description IT","TRASFORMATORE TRIFASE 18 KVA"));
        fields.add(new DescriptionDataElement("Description EN","THREE PHASE TRANSFORMER 18 KVA"));
        fields.add(new DescriptionDataElement("PO FR","TRANSFO. MONOPHASE NORME EN60742 SIMPLE ECRAN SECONDAIRE"));
        fields.add(new DescriptionDataElement("PO IT","TR.2500VA V.230-400/110-0-110|Completo di schermo elettrostatico"));
        fields.add(new DescriptionDataElement("PO EN","TRANSFO. SINGLE-PHASE EN60742 SEC.230V 250V A PRIM.380"));
        fields.add(new DescriptionDataElement("PLM Concatenation","Tension = 400 V | Type du raccordement = Vis | Hauteur d'encombrement = 2M"));
        fields.add(new DescriptionDataElement("PLM Manufacturer info","SIEMENS 3SB3400-3S"));
        fields.add(new DescriptionDataElement("Vendor information","SIEMENS SAS : 3SB3400-3S\n"));
        fields.add(new DescriptionDataElement("Material group","Repuestos mantenimiento"));
        fields.add(new DescriptionDataElement("Sourcing Family","Ceco almacén efectos y repuestos"));
        fields.add(new DescriptionDataElement("SSR","AETNA GROUP 0001354366"));
        fields.add(new DescriptionDataElement("Données de base","Type de composant : Mecanique | Fabriquant impose : oui | Ref 0001354366"));

        fillTable(new LinkedList<DescriptionDisplayElement>(),fields);
    }

    private void fillTable(List<DescriptionDisplayElement> elements, List<DescriptionDataElement> fields) {
        elementTable.getItems().setAll(elements);
        fieldTable.getItems().setAll(fields);
    }

    public void setParent(Char_description parent, Integer rowIndex, Integer columnIndex) {
        this.parent = parent;
        this.parentRowIndex = rowIndex;
        this.parentColumnIndex = columnIndex;
    }

    public void setUserAccount(UserAccount account) {
        this.account = account;
    }

    public void fillItems() {
        CharDescriptionRow tmp = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
        fillTable(DescriptionDisplayElement.returnElementsForItem(tmp,parentRowIndex,parentColumnIndex),tmp.getDescriptionDataFields());
    }

    public void setStageWidthProperty(Stage stage) {
        elementTable.prefWidthProperty().bind(stage.widthProperty().multiply(0.6));
        //fieldTable.prefWidthProperty().bind(widthProperty.multiply(0.4));
        /*stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    try{
                        parent.refresh_ui_display();
                        stage.close();
                    }catch (Exception E){

                    }
                }
            }
        });*/
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    DescriptionDisplayElement.DisplaySettings.put(parentRowIndex.toString() + parentColumnIndex.toString(), elementTable.getItems());
                    parent.refresh_ui_display();
                    parent.tableController.charDescriptionTable.refresh();
                    stage.close();
                }catch (Exception E){

                }
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
            }
        });
    }
}
