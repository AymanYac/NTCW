package transversal.dialog_toolbox;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.*;
import service.CharValuesLoader;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class SearchBarCustomizerDialog {
    private static Dialog<Object> dialog;
    private static GridPane contentGrid;
    private static CharDescriptionRow sourceItem;
    private static String sourceSegment;
    private static GridPane elemBlocks;
    private static String dataLanguageCode;
    private static String userLanguageCode;
    private static ChangeListener<? super String> concatGenerator;
    private static Label previewLabel;


    public static void editSearchPrefrence(Char_description parent) {
        dialog = new Dialog<>();
        dialog.setTitle("Custom search text");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("customDialog");

        // Set the button types.
        ButtonType clearBT = new ButtonType("Clear", ButtonBar.ButtonData.LEFT);
        ButtonType applyBT = new ButtonType("Save", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(clearBT,applyBT,ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(applyBT).addEventFilter(ActionEvent.ACTION, event -> {
            // handle cancel button code here
            event.consume();
            saveSearchPreference(parent.account);
            ArrayList<ArrayList<String>> concatElems = loadSettingsFromScreen();
            String itemSearchSentence = evaluateSearchSentence(concatElems, sourceItem, sourceSegment);
            parent.search_text.setText(itemSearchSentence);
            dialog.close();
        });
        dialog.getDialogPane().lookupButton(clearBT).addEventFilter(ActionEvent.ACTION, event -> {
            // handle cancel button code here
            event.consume();
            for(int i=0;i<20;i++){
                Tools.deleteRow(elemBlocks,0);
            }
        });
        contentGrid = new GridPane();
        contentGrid.setHgap(10);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(10, 10, 10, 10));
        dialog.getDialogPane().setContent(contentGrid);


        storeSourceData(parent);
        //createFieldsLinePerCarac();
        createFieldsBlocPerElem(parent.account);
        setFieldsLayout();

        dialog.showAndWait();

    }

    private static void saveSearchPreference(UserAccount account) {
        ArrayList<ArrayList<String>> concatElems = loadSettingsFromScreen();
        account.saveSearchSettings(concatElems);
        dialog.close();
    }


    private static void storeSourceData(Char_description parent) {
        sourceItem = parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
        sourceSegment = sourceItem.getClass_segment_string().split("&&&")[0];
        dataLanguageCode = parent.data_language_gcode.toUpperCase();
        userLanguageCode = parent.user_language_gcode.toUpperCase();
        concatGenerator = (ChangeListener<String>) (observable, oldValue, newValue) -> {
            new Thread (()->{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                ArrayList<ArrayList<String>> concatElems = loadSettingsFromScreen();
                String itemSearchSentence = evaluateSearchSentence(concatElems, sourceItem, sourceSegment);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        previewLabel.setText(itemSearchSentence);
                    }
                });
            }).start();

        };
    }

    private static ArrayList<ArrayList<String>> loadSettingsFromScreen() {
        ArrayList<ArrayList<String>> concatElems = new ArrayList<ArrayList<String>>();
        Node[][] gridPaneNodes = new Node[4][99] ;
        for (Node child : elemBlocks.getChildren()) {
            Integer column = GridPane.getColumnIndex(child);
            Integer row = GridPane.getRowIndex(child);
            if (column != null && row != null) {
                try{
                    gridPaneNodes[column][row] = child ;
                }catch (Exception V){

                }
            }
        }
        for (int row = 0; row < 99; row++) {
            try{
                Node first = gridPaneNodes[0][row];
                Node second = gridPaneNodes[1][row];
                Node third = gridPaneNodes[2][row];
                ArrayList<String> lineElem = loadElemFromFields((ComboBox) first, (ComboBox) second, (ComboBox) third);
                concatElems.add(lineElem);
            }catch (Exception V){

            }
        }
        return concatElems;
    }

    public static String evaluateSearchSentence(ArrayList<ArrayList<String>> concatElems, CharDescriptionRow sourceItem, String sourceSegment) {
        AtomicReference<String> searchSentence = new AtomicReference<>("");
        concatElems.forEach(elem->{
            if(elem.get(0).equals(CustomSearchElements.DESCRIPTION_OVERVIEW)){
                if(elem.get(1).equals(CustomSearchElements.SHORT)
                        ||
                    (elem.get(1).equals(CustomSearchElements.LONG_OR_SHORT)&&!(sourceItem.getLong_desc()!=null && sourceItem.getLong_desc().length()>0))
                ){
                    if(elem.get(2).equals(CustomSearchElements.DL)
                    ||
                    (elem.get(2).equals(CustomSearchElements.UL_OR_DL) && !(sourceItem.getShort_desc_translated()!=null && sourceItem.getShort_desc_translated().length()>0))
                    ){
                        searchSentence.set(searchSentence +" "+ WordUtils.getSearchWords(sourceItem.getShort_desc()));
                    }
                    else{
                        searchSentence.set(searchSentence +" "+ WordUtils.getSearchWords(sourceItem.getShort_desc_translated()));
                    }
                }
                else{
                    if(elem.get(2).equals(CustomSearchElements.DL)
                            ||
                            (elem.get(2).equals(CustomSearchElements.UL_OR_DL) && !(sourceItem.getLong_desc_translated()!=null && sourceItem.getLong_desc_translated().length()>0))
                    ){
                        searchSentence.set(searchSentence +" "+ WordUtils.getSearchWords(sourceItem.getLong_desc()));
                    }
                    else{
                        searchSentence.set(searchSentence +" "+ WordUtils.getSearchWords(sourceItem.getLong_desc_translated()));
                    }
                }

            }
            else if (elem.get(0).equals(CustomSearchElements.CATEGORY)){
                if(elem.get(1).equals(CustomSearchElements.DL)
                        ||
                    (elem.get(1).equals(CustomSearchElements.UL_OR_DL) && !(sourceItem.getClass_segment().getClassNameTranslated()!=null && sourceItem.getClass_segment().getClassNameTranslated().length()>0))
                ){
                    searchSentence.set(searchSentence +" "+ sourceItem.getClass_segment().getClassName());
                }
                else{
                    searchSentence.set(searchSentence +" "+ sourceItem.getClass_segment().getClassNameTranslated());
                }
            }
            else if(elem.get(0).equals(CustomSearchElements.FREE_TEXT)){
                searchSentence.set(searchSentence +" "+ elem.get(1));
            }
            else{
                ClassCaracteristic caracMatch = CharValuesLoader.active_characteristics.get(sourceSegment).stream().filter(carac ->
                        (String.valueOf(carac.getSequence()) + "-" + carac.getCharacteristic_name()).equals(elem.get(0))).findAny().get();
                CaracteristicValue sourceValue = sourceItem.getData(sourceSegment).get(caracMatch.getCharacteristic_id());
                String sourceRule ="";
                try {
                    if (sourceValue.getSource().equals(DataInputMethods.AUTO_CHAR_DESC)) {
                        sourceRule = sourceItem.getRuleResults().get(caracMatch.getCharacteristic_id()).stream().filter(result -> result.getStatus() != null && result.getStatus().equals("Applied")).findAny().get().getMatchedBlock();
                    }
                    if (sourceValue.getSource().equals(DataInputMethods.SEMI_CHAR_DESC)) {
                        sourceRule = sourceItem.getRuleResults().get(caracMatch.getCharacteristic_id()).stream().filter(result -> result.getGenericCharRule().getRuleSyntax() != null && result.getGenericCharRule().getRuleSyntax().equals(sourceValue.getRule_id())).findAny().get().getMatchedBlock();
                    }
                }catch (Exception V){
                    
                }

                if(elem.get(1).equals(CustomSearchElements.VALUE)){
                    try{
                        if (!caracMatch.getIsNumeric() && caracMatch.getIsTranslatable()) {
                            if (elem.get(2).equals(dataLanguageCode)
                                    ||
                                    (elem.get(2).equals(userLanguageCode + " or " + dataLanguageCode) && !(sourceValue.getUserLanguageValue() != null && sourceValue.getUserLanguageValue().length() > 0))
                            ) {
                                searchSentence.set(searchSentence + " " + sourceValue.getDataLanguageValue());
                            } else {
                                searchSentence.set(searchSentence + " " + sourceValue.getUserLanguageValue());
                            }
                        } else {
                            searchSentence.set(searchSentence + " " + sourceValue.getDisplayValue(false, false));
                        }
                    }
                    catch (Exception V){

                    }
                }
                else if(elem.get(1).equals(CustomSearchElements.PATTERN)){
                    searchSentence.set(searchSentence +" "+ sourceRule);
                }
                else{
                    try{
                        searchSentence.set(searchSentence +" "+ caracMatch.getCharacteristic_name()+" "+sourceValue.getDisplayValue(false,false));
                    }catch (Exception V){

                    }
                }
            }
        });
        return searchSentence.get();
    }

    private static void createFieldsBlocPerElem(UserAccount account) {
        elemBlocks = new GridPane();
        ScrollPane elemBlocksScroll = new ScrollPane();
        elemBlocksScroll.setContent(elemBlocks);
        elemBlocksScroll.setMinViewportHeight(256);
        elemBlocksScroll.setMinViewportWidth(256);
        elemBlocksScroll.getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/ScrollPaneTransparent.css").toExternalForm());
        elemBlocksScroll.setFitToWidth(true);
        contentGrid.add(elemBlocksScroll,0,1);
        GridPane.setColumnSpan(elemBlocksScroll,GridPane.REMAINING);
        GridPane.setHgrow(contentGrid, Priority.ALWAYS);
        loadElemBlocks(account);
        insertAddCombo(account);
    }

    private static void insertAddCombo(UserAccount account) {
        ComboBox<String> addCombo = new ComboBox<String>();
        GridPane.setHgrow(addCombo,Priority.ALWAYS);
        addCombo.setMaxWidth(Double.MAX_VALUE);
        addCombo.setScaleX(0.9);
        addCombo.getItems().add("Description overview");
        addCombo.getItems().add("Category name");
        addCombo.getItems().add("Free text");
        addCombo.getItems().addAll(CharValuesLoader.active_characteristics.get(sourceSegment).stream().map(carac ->
                String.valueOf(carac.getSequence())+"-"+carac.getCharacteristic_name()).collect(Collectors.toCollection(ArrayList::new)));
        addFirstElemListener(addCombo,null,null,true);
        previewLabel = new Label();
        contentGrid.add(previewLabel,0,2);
        GridPane.setColumnSpan(previewLabel,GridPane.REMAINING);
        contentGrid.add(addCombo,1,3);
        contentGrid.add(new Label("Add a new search element:"),0,3);
    }

    private static void loadElemBlocks(UserAccount account) {
        ArrayList<ArrayList<String>> preferences = account.getSearchSettings(sourceSegment);
        preferences.forEach(elem->{
            addElemBlock(elem);
        });
    }

    private static void addElemBlock(ArrayList<String> elem) {
        int maxRow = 1;
        for(Node node: elemBlocks.getChildren()){
            maxRow = Math.max(GridPane.getRowIndex(node)+1,maxRow);
        }
        maxRow+=1;
        //Add carac block lines
        ComboBox<String> first = new ComboBox<String>();
        GridPane.setHgrow(first,Priority.ALWAYS);
        first.setMaxWidth(Double.MAX_VALUE);
        first.setScaleX(0.9);
        ComboBox<String> second = new ComboBox<String>();
        GridPane.setHgrow(second,Priority.ALWAYS);
        second.setMaxWidth(Double.MAX_VALUE);
        second.setScaleX(0.9);
        ComboBox<String> third = new ComboBox<String>();
        GridPane.setHgrow(third,Priority.ALWAYS);
        third.setMaxWidth(Double.MAX_VALUE);
        third.setScaleX(0.9);
        first.valueProperty().addListener(concatGenerator);
        second.valueProperty().addListener(concatGenerator);
        second.getEditor().textProperty().addListener(concatGenerator);
        third.valueProperty().addListener(concatGenerator);
        first.getItems().add("Description overview");
        first.getItems().add("Category name");
        first.getItems().add("Free text");
        first.getItems().addAll(CharValuesLoader.active_characteristics.get(sourceSegment).stream().map(carac ->
                String.valueOf(carac.getSequence())+"-"+carac.getCharacteristic_name()).collect(Collectors.toCollection(ArrayList::new)));
        addFirstElemListener(first,second,third, false);
        addSecondElemListener(first,second,third);
        if(elem.get(0).equals(CustomSearchElements.DESCRIPTION_OVERVIEW)){
            first.setValue("Description overview");
            if(elem.get(1).equals(CustomSearchElements.SHORT)){
                second.setValue("Short description");
            }else if(elem.get(1).equals(CustomSearchElements.LONG)){
                second.setValue("Long description");
            }else if(elem.get(1).equals(CustomSearchElements.SHORT_OR_LONG)){
                second.setValue("Short or Long description");
            }else{
                second.setValue("Long or Short description");
            }
            if(elem.get(2).equals(CustomSearchElements.DL)){
                third.setValue(dataLanguageCode);
            }else if(elem.get(2).equals(CustomSearchElements.UL)){
                third.setValue(userLanguageCode);
            }else if(elem.get(2).equals(CustomSearchElements.DL_OR_UL)){
                third.setValue(dataLanguageCode+" or "+userLanguageCode);
            }else{
                third.setValue(userLanguageCode+" or "+dataLanguageCode);
            }
        }
        else if(elem.get(0).equals(CustomSearchElements.CATEGORY)){
            first.setValue("Category name");
            if(elem.get(1).equals(CustomSearchElements.DL)){
                second.setValue(dataLanguageCode);
            }else if(elem.get(1).equals(CustomSearchElements.UL)){
                second.setValue(userLanguageCode);
            }else if(elem.get(1).equals(CustomSearchElements.DL_OR_UL)){
                second.setValue(dataLanguageCode+" or "+userLanguageCode);
            }else{
                second.setValue(userLanguageCode+" or "+dataLanguageCode);
            }
        }
        else if(elem.get(0).equals(CustomSearchElements.FREE_TEXT)){
            first.setValue(CustomSearchElements.FREE_TEXT);
            second.getEditor().setText(elem.get(1));
        }
        else {
            try{
                first.setValue(first.getItems().stream().filter(item -> item.equals(elem.get(0))).findAny().get());
            }catch (Exception V){

            }
            if(elem.get(1).equals(CustomSearchElements.VALUE)){
                second.setValue("Value");
                loadThirdFieldForCaracValue(third,elem);
            }else if(elem.get(1).equals(CustomSearchElements.PATTERN)){
                second.setValue("Identified pattern");
            }else{
                second.setValue("Characteristic name + Value");
                if(elem.get(2).equals(CustomSearchElements.DL)){
                    third.setValue(dataLanguageCode);
                }else if(elem.get(2).equals(CustomSearchElements.UL)){
                    third.setValue(userLanguageCode);
                }else if(elem.get(2).equals(CustomSearchElements.DL_OR_UL)){
                    third.setValue(dataLanguageCode+" or "+userLanguageCode);
                }else{
                    third.setValue(userLanguageCode+" or "+dataLanguageCode);
                }
            }
        }
        elemBlocks.add(first,0,maxRow);
        elemBlocks.add(second,1,maxRow);
        elemBlocks.add(third,2,maxRow);
        Button closeButton = new Button("x");
        closeButton.minHeightProperty().bind(first.heightProperty());
        closeButton.maxHeightProperty().bind(first.heightProperty());
        closeButton.prefHeightProperty().bind(first.heightProperty());
        closeButton.minWidthProperty().bind(first.heightProperty());
        closeButton.maxWidthProperty().bind(first.heightProperty());
        closeButton.prefWidthProperty().bind(first.heightProperty());

        GridPane.setHalignment(closeButton, HPos.CENTER);
        int finalMaxRow = maxRow;
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Tools.deleteRow(elemBlocks,Integer.valueOf(finalMaxRow));
            }
        });
        elemBlocks.add(closeButton,3,maxRow);
    }


    private static void loadThirdFieldForCaracValue(ComboBox<String> third, ArrayList<String> elem) {
        if(thirdFieldShouldBeVisibleForCaracValue(elem)){
            third.setVisible(true);
            try{
                if(elem.get(2).equals(CustomSearchElements.DL)){
                    third.setValue(dataLanguageCode);
                }else if(elem.get(2).equals(CustomSearchElements.UL)){
                    third.setValue(userLanguageCode);
                }else if(elem.get(2).equals(CustomSearchElements.DL_OR_UL)){
                    third.setValue(dataLanguageCode+" or "+userLanguageCode);
                }else{
                    third.setValue(userLanguageCode+" or "+dataLanguageCode);
                }
            }catch (Exception V){

            }
        }
    }

    private static boolean thirdFieldShouldBeVisibleForCaracValue(ArrayList<String> elem) {
        try{
            Optional<ClassCaracteristic> caracMatch = CharValuesLoader.active_characteristics.get(sourceSegment).stream().filter(carac ->
                    (String.valueOf(carac.getSequence()) + "-" + carac.getCharacteristic_name()).equals(elem.get(0))).findAny();
            return caracMatch.isPresent() && !caracMatch.get().getIsNumeric() && caracMatch.get().getIsTranslatable();
        }catch (Exception V){
            return false;
        }

    }

    private static void addFirstElemListener(ComboBox<String> first, ComboBox<String> second, ComboBox<String> third, boolean createNewElem) {
        first.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!(newValue!=null)){
                    return;
                }
                if (createNewElem){
                    first.getEditor().clear();
                    addElemBlock(loadElemFromFields(first,second,third));
                    return;
                }
                try{
                    second.getItems().clear();
                }catch (Exception V){

                }
                try{
                    third.getItems().clear();
                }catch (Exception V){

                }
                third.getItems().add(dataLanguageCode);
                third.getItems().add(userLanguageCode);
                third.getItems().add(dataLanguageCode+" or "+userLanguageCode);
                third.getItems().add(userLanguageCode+" or "+dataLanguageCode);

                try{
                    second.getEditor().clear();
                }catch (Exception V){

                }
                second.setEditable(false);
                third.setVisible(false);
                if(newValue.equals(CustomSearchElements.DESCRIPTION_OVERVIEW)){
                    second.getItems().add("Short description");
                    second.getItems().add("Long description");
                    second.getItems().add("Short or Long description");
                    second.getItems().add("Long or Short description");
                    third.setVisible(true);
                }
                else if (newValue.equals(CustomSearchElements.CATEGORY)){
                    second.getItems().add(dataLanguageCode);
                    second.getItems().add(userLanguageCode);
                    second.getItems().add(dataLanguageCode+" or "+userLanguageCode);
                    second.getItems().add(userLanguageCode+" or "+dataLanguageCode);
                }
                else if(newValue.equals(CustomSearchElements.FREE_TEXT)){
                    second.setEditable(true);
                }
                else {
                    second.getItems().add("Value");
                    second.getItems().add("Identified pattern");
                    second.getItems().add("Characteristic name + Value");
                    if(thirdFieldShouldBeVisibleForCaracValue(loadElemFromFields(first, second, third))){
                        third.setVisible(true);
                    }
                }
                try{
                    second.setValue(second.getItems().get(0));
                }catch (Exception V){

                }
            }
        });
    }

    private static void addSecondElemListener(ComboBox<String> first, ComboBox<String> second, ComboBox<String> third) {
        second.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!(newValue!=null)){
                    return;
                }
                try{
                    third.setValue(third.getItems().get(0));
                }catch (Exception V){

                }
                if(newValue.equals("Value")){
                    third.setVisible(false);
                    loadThirdFieldForCaracValue(third,loadElemFromFields(first,second,third));
                }
                else if(newValue.equals("Identified pattern")){
                    third.setVisible(false);
                }
                else if(newValue.equals("Characteristic name + Value")){
                    third.setVisible(true);
                }
            }
        });
    }

    private static ArrayList<String> loadElemFromFields(ComboBox<String> first, ComboBox<String> second, ComboBox<String> third) {
        try{
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(first.getValue());
            if(second.getValue().toLowerCase().equals(dataLanguageCode)){
                ret.add(CustomSearchElements.DL);
            }else if(second.getValue().toLowerCase().equals(userLanguageCode)){
                ret.add(CustomSearchElements.UL);
            }else if(second.getValue().toLowerCase().equals(userLanguageCode+" or "+dataLanguageCode)){
                ret.add(CustomSearchElements.UL_OR_DL);
            }else if(second.getValue().toLowerCase().equals(dataLanguageCode+" or "+userLanguageCode)){
                ret.add(CustomSearchElements.DL_OR_UL);
            }else{
                if(second.isEditable()){
                    ret.add(second.getEditor().getText());
                }else{
                    ret.add(second.getValue());
                }
            }
            if(third.isVisible()){
                if(third.getValue().toLowerCase().equals(dataLanguageCode)){
                    ret.add(CustomSearchElements.DL);
                }else if(third.getValue().toLowerCase().equals(userLanguageCode)){
                    ret.add(CustomSearchElements.UL);
                }else if(third.getValue().toLowerCase().equals(userLanguageCode+" or "+dataLanguageCode)){
                    ret.add(CustomSearchElements.UL_OR_DL);
                }else{
                    ret.add(CustomSearchElements.DL_OR_UL);
                }
            }else{
                ret.add(null);
            }
            return ret;
        }catch (Exception V){
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(first.getValue());
            ret.add("");
            ret.add("");
            return ret;
        }

    }


    private static void setFieldsLayout() {

        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setPercentWidth(30);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(30);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(30);
        ColumnConstraints cc3 = new ColumnConstraints();
        cc3.setPercentWidth(10);
        contentGrid.getColumnConstraints().setAll(cc0,cc1,cc2,cc3);
        contentGrid.setMinWidth(240);

        elemBlocks.getColumnConstraints().setAll(cc0,cc1,cc2,cc3);
        elemBlocks.getStylesheets().add(CaracDeclarationDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
        previewLabel.setStyle("-fx-fill: #8496AE;-fx-font-style: italic");
    }

}
