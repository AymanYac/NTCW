package controllers.paneControllers;

import controllers.Char_description;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Callback;
import model.CustomTableColumn;
import org.controlsfx.control.Rating;
import transversal.generic.PixelUtils;

import java.util.stream.IntStream;

public class ClassPane_CharClassif {
    private static final String HOVERED_BUTTON_STYLE = "-fx-background-color:#212934; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#ACB9CA;";
    private static final String STANDARD_BUTTON_STYLE="-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;";

    public TableView<String> ruleView;
    public CustomTableColumn<String,String> ruleType;
    public CustomTableColumn<String,String> matchingPattern;
    public CustomTableColumn<String,HBox> defaultBehaviour;
    public CustomTableColumn<String,Rating> defaultStrength;
    public GridPane contentPane;
    private Char_description parent;

    public void PaneClose(ActionEvent actionEvent) {
    }

    public void addRule(KeyEvent keyEvent) {
    }

    public void addRuleButtonAction(ActionEvent actionEvent) {
        System.out.println("Hello");
        ruleView.getItems().add("hqs");
        ruleView.refresh();
    }

    public void setParent(Char_description char_description) {
        parent = char_description;
        ruleType.setCellValueFactory(new Callback<CustomTableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CustomTableColumn.CellDataFeatures<String, String> param) {
                return new ReadOnlyObjectWrapper<>("MAIN DESC + DWG");
            }
        });
        matchingPattern.setCellValueFactory(new Callback<CustomTableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CustomTableColumn.CellDataFeatures<String, String> param) {
                return new ReadOnlyObjectWrapper<>("Long "+param.getValue());
            }
        });
        defaultBehaviour.setCellValueFactory(new Callback<CustomTableColumn.CellDataFeatures<String, HBox>, ObservableValue<HBox>>() {
            @Override
            public ObservableValue<HBox> call(CustomTableColumn.CellDataFeatures<String, HBox> param) {
                HBox tmp = new HBox();
                tmp.translateXProperty().bind((defaultBehaviour.widthProperty().subtract(tmp.widthProperty())).divide(4));
                tmp.setId("behaviourBox");
                tmp.getStylesheets().clear();
                tmp.getStylesheets().add(getClass().getResource("/styles/ratingAndBehaviour.css").toExternalForm());
                ToggleButton btn1 = new ToggleButton("Hide");
                ToggleButton btn2 = new ToggleButton("Show");
                ToggleButton btn3 = new ToggleButton("Tick");
                tmp.getChildren().addAll(btn1,btn2,btn3);
                tmp.getChildren().forEach(btn->{
                    ((ToggleButton) btn).getStyleClass().clear();
                    ((ToggleButton) btn).getStyleClass().add("fieldbutton");
                });
                btn1.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue){
                            btn2.setSelected(false);
                            btn3.setSelected(false);
                        }else{
                            if(btn2.isSelected() || btn3.isSelected()){

                            }else{
                                btn1.setSelected(true);
                            }
                        }
                    }
                });
                btn2.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue){
                            btn1.setSelected(false);
                            btn3.setSelected(false);
                        }else{
                            if(btn1.isSelected() || btn3.isSelected()){

                            }else{
                                btn2.setSelected(true);
                            }
                        }
                    }
                });
                btn3.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(newValue){
                            btn2.setSelected(false);
                            btn1.setSelected(false);
                        }else{
                            if(btn2.isSelected() || btn1.isSelected()){

                            }else{
                                btn3.setSelected(true);
                            }
                        }
                    }
                });
                btn2.setSelected(true);
                return new ReadOnlyObjectWrapper<>(tmp);
            }
        });
        defaultStrength.setCellValueFactory(new Callback<CustomTableColumn.CellDataFeatures<String, Rating>, ObservableValue<Rating>>() {
            @Override
            public ObservableValue<Rating> call(CustomTableColumn.CellDataFeatures<String, Rating> param) {
                Rating tmp = new Rating();
                tmp.setRating(3);
                tmp.getStylesheets().clear();
                tmp.getStylesheets().add(getClass().getResource("/styles/ratingAndBehaviour.css").toExternalForm());
                return new ReadOnlyObjectWrapper<>(tmp);
            }
        });


        contentPane.lookupAll("#dotButton").forEach(btn->{
            btn.setOnMouseClicked(event -> {

                final Label ligne0 = new Label("Hide");
                final Label ligne1 = new Label("Input field");
                final Label ligne2 = new Label("Checkbox");

                setStyle(ligne0);
                setStyle(ligne1);
                setStyle(ligne2);

                final Popup popup = new Popup();
                popup.setAutoHide(true);
                addSecondaryPopup(ligne1,"field");
                addSecondaryPopup(ligne2,"checkbox");

                GridPane contentGrid = new GridPane();
                contentGrid.add(ligne0,0,0);
                contentGrid.setHgrow(ligne0, Priority.ALWAYS);
                ligne0.setMaxWidth(Integer.MAX_VALUE);
                contentGrid.add(ligne1,0,1);
                contentGrid.setHgrow(ligne1, Priority.ALWAYS);
                ligne1.setMaxWidth(Integer.MAX_VALUE);
                contentGrid.add(ligne2,0,2);
                ligne2.setMaxWidth(Integer.MAX_VALUE);

                contentGrid.setGridLinesVisible(true);
                popup.getContent().clear();
                popup.getContent().add(contentGrid);
                popup.show(btn, ((MouseEvent)event).getScreenX() + 10, ((MouseEvent)event).getScreenY());
            });
        });


        IntStream.range(1,5).forEach(idx->{
            ruleView.getItems().add("Example "+idx);
        });
        ruleView.refresh();
    }

    private void addSecondaryPopup(Label ligne0, String fieldType) {
        final Popup popup_ = new Popup();
        ligne0.setOnMouseEntered(entered->{

            final Label ligne0_ = new Label("Contains");
            Label ligne1_ = new Label("Starts with");
            final Label ligne2_ = new Label("Finishes with");
            if(fieldType.equals("checkbox")){
                ligne1_ = new Label("Same data field");
            }


            setStyle(ligne0_);
            setStyle(ligne1_);
            setStyle(ligne2_);

            GridPane contentGrid_ = new GridPane();
            contentGrid_.add(ligne0_,0,0);
            contentGrid_.setHgrow(ligne0_, Priority.ALWAYS);
            ligne0_.setMaxWidth(Integer.MAX_VALUE);
            contentGrid_.add(ligne1_,0,1);
            contentGrid_.setHgrow(ligne1_, Priority.ALWAYS);
            ligne1_.setMaxWidth(Integer.MAX_VALUE);
            if(fieldType.equals("field")){
                contentGrid_.add(ligne2_, 0, 2);
                ligne2_.setMaxWidth(Integer.MAX_VALUE);
            }

            contentGrid_.setGridLinesVisible(true);
            popup_.getContent().clear();
            popup_.getContent().add(contentGrid_);
            if(popup_.getWidth()+ligne0.localToScreen(ligne0.getBoundsInLocal()).getMaxX()< Screen.getPrimary().getBounds().getWidth()){
                popup_.show(ligne0, ligne0.localToScreen(ligne0.getBoundsInLocal()).getMaxX(), ligne0.localToScreen(ligne0.getBoundsInLocal()).getMinY());
            }else{
                popup_.show(ligne0, ligne0.localToScreen(ligne0.getBoundsInLocal()).getMinX()-popup_.getWidth(), ligne0.localToScreen(ligne0.getBoundsInLocal()).getMinY());
            }


            contentGrid_.setOnMouseExited(exited->{
                popup_.hide();
            });

        });
        ligne0.setOnMouseExited(exited->{
            com.sun.glass.ui.Robot robot =
                    com.sun.glass.ui.Application.GetApplication().createRobot();
            if(!PixelUtils.position_in_bounds(robot.getMouseX(),robot.getMouseY(),popup_.getX(),popup_.getWidth(),popup_.getY(),popup_.getHeight())){
                popup_.hide();
            }
        });
    }

    private void setStyle(Node ligne) {
        ligne.styleProperty().bind(
                Bindings
                        .when(ligne.hoverProperty())
                        .then(
                                new SimpleStringProperty(HOVERED_BUTTON_STYLE)
                        )
                        .otherwise(
                                new SimpleStringProperty(STANDARD_BUTTON_STYLE)
                        )
        );
    }
}
