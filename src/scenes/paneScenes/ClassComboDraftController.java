package scenes.paneScenes;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.ClassSegment;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ClassComboDraftController {
    private ArrayList<ClassSegment> elemList = new ArrayList<ClassSegment>();
    private AutoCompletionBinding<ClassSegment> binding;

    @FXML ComboBox<ClassSegment> field = new ComboBox<>();
    private ClassSegment latestValue;
    private ListView autoCompleteView;
    private ListView comboListView;

    @FXML void initialize(){
        try {
            loadClasses();
            field.getItems().addAll(elemList);
            field.setEditable(true);

            Callback<AutoCompletionBinding.ISuggestionRequest, Collection<ClassSegment>> suggestionProvider = new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<ClassSegment>>() {
                @Override
                public Collection<ClassSegment> call(AutoCompletionBinding.ISuggestionRequest param) {
                    String input = param.getUserText();
                    Unidecode unidec = Unidecode.toAscii();
                    ArrayList<ClassSegment> ret = elemList.stream().filter(elem -> unidec.decodeTrimLowerCase(elem.toString()).contains(unidec.decodeTrimLowerCase(input)))
                            .sorted(new Comparator<ClassSegment>() {
                                @Override
                                public int compare(ClassSegment o1, ClassSegment o2) {
                                    return o1.toString().compareTo(o2.toString())
                                            + (o1.toString().startsWith(input) ? -100 : 0)
                                            + (o2.toString().startsWith(input) ? +100 : 0);
                                }
                            }).collect(Collectors.toCollection(ArrayList::new));
                    if(ret.size()<2){
                        return null;
                    }else{
                        return ret;
                    }
                }
            };
            AutoCompletionBinding<ClassSegment> completion = TextFields.bindAutoCompletion(field.getEditor(), suggestionProvider);
            field.setConverter(new StringConverter<ClassSegment>() {
                @Override
                public String toString(ClassSegment object) {
                    if (object == null){
                        return null;
                    } else {
                        return object.toString();
                    }
                }
                @Override
                public ClassSegment fromString(String string) {
                    return field.getItems().stream().filter(classe ->
                            classe.getClassName().equalsIgnoreCase(string)||
                            classe.toString().equalsIgnoreCase(string)||
                            classe.getClassNumber().equalsIgnoreCase(string)||
                            classe.getSegmentId().equalsIgnoreCase(string)).findFirst().orElse(latestValue);
                }
            });
            field.valueProperty().addListener(new ChangeListener<ClassSegment>() {
                @Override
                public void changed(ObservableValue<? extends ClassSegment> observable, ClassSegment oldValue, ClassSegment newValue) {
                    if(newValue!=null){
                        System.out.println("new value "+newValue.getClassName());
                        comboListView = ((ComboBoxListViewSkin) field.getSkin()).getListView();
                        comboListView.getFocusModel().focus(field.getItems().indexOf(newValue));
                        comboListView.scrollTo(newValue);
                        latestValue=newValue;
                    }
                }
            });
            field.setOnShown(e->{
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) field.getSkin();
                ListView<?> list = (ListView<?>) skin.getPopupContent();
                list.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
                        field.getEditor().selectAll();
                    }
                });
                field.setOnShown(null);
            });
            completion.prefWidthProperty().bind(((TextField)completion.getCompletionTarget()).widthProperty());
            completion.setHideOnEscape(true);
            completion.getAutoCompletionPopup().addEventFilter(KeyEvent.ANY,event -> {
                if(event.getCode().equals(KeyCode.UP)){
                    final AutoCompletePopup source = (AutoCompletePopup) event.getSource();
                    ListView node = (ListView) (source.getSkin().getNode());
                    if(node.getSelectionModel().isSelected(0)){
                        node.getSelectionModel().clearAndSelect(node.getItems().size()-1);
                        node.scrollTo(node.getItems().size()-1);
                        event.consume();
                    }
                }else if(event.getCode().equals(KeyCode.DOWN)) {
                    final AutoCompletePopup source = (AutoCompletePopup) event.getSource();
                    ListView node = (ListView) (source.getSkin().getNode());
                    if (node.getSelectionModel().isSelected(node.getItems().size() - 1)) {
                        node.getSelectionModel().clearAndSelect(0);
                        node.scrollTo(0);
                        event.consume();
                    }
                }else if(event.getCode().equals(KeyCode.A) && event.isControlDown()){
                    field.getEditor().selectAll();
                    event.consume();
                }
            });
            completion.getAutoCompletionPopup().setOnShowing(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    final AutoCompletePopup source = (AutoCompletePopup) event.getSource();
                    autoCompleteView = (ListView) (source.getSkin().getNode());
                    autoCompleteView.setCellFactory(lv -> {
                        ListCell<?> cell = new ListCell<Object>() {
                            @Override
                            public void updateItem(Object item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setText(null);
                                } else {
                                    setText(item.toString());
                                }
                            }
                        };
                        cell.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                            if (isNowHovered && ! cell.isEmpty()) {
                                autoCompleteView.getSelectionModel().clearAndSelect(cell.getIndex());
                            } else {
                                //offHover
                            }
                        });

                        return cell ;
                    });
                }
            });
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void loadClasses() throws SQLException, ClassNotFoundException {
        Connection conn = Tools.spawn_connection_from_pool();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from " + "ua2ff22e2317c48e8993e295a471b73b9" + ".project_segments");
        while (rs.next()) {
            ClassSegment tmp = new ClassSegment();
            tmp.setSegmentGranularity(Tools.get_project_granularity("ua2ff22e2317c48e8993e295a471b73b9"));
            tmp.setSegmentId(rs.getString(1));
            for (int lvl = 0; lvl < 4; lvl++) {
                tmp.setLevelNumber(lvl, rs.getString(1 + lvl * 3 + 1));
                tmp.setLevelName(lvl, rs.getString(1 + lvl * 3 + 2));
                tmp.setLevelNameTranslated(lvl, rs.getString(1 + lvl * 3 + 3));
            }
            elemList.add(tmp);
        }
        rs.close();
        stmt.close();
        conn.close();
    }
}
