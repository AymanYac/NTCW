package controllers;

import controllers.paneControllers.Browser_CharClassif;
import controllers.paneControllers.CharPane_CharClassif;
import controllers.paneControllers.ImagePane_CharClassif;
import controllers.paneControllers.TablePane_CharClassif;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import model.*;
import org.json.simple.parser.ParseException;
import service.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.DescriptionExportExcelSheets;
import transversal.dialog_toolbox.UoMDeclarationDialog;
import transversal.dialog_toolbox.UrlBookMarkDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Char_description {

	public boolean CHANGING_CLASS = false;
	
	
	
	@FXML MenuBar menubar;
	@FXML MenuBar secondaryMenuBar;
	@FXML public Menu counterTotal;
	@FXML public Menu counterRemaining;
	@FXML public Menu counterDaily;
	@FXML public Menu counterSelection;
	@FXML public ComboBox<CharDescClassComboRow> classCombo;
	@FXML GridPane grid;
	
	@FXML public AnchorPane leftAnchor;
	@FXML public AnchorPane rightAnchor;
	@FXML public Label aidLabel;
	@FXML public TextArea sd;
	@FXML public TextArea sd_translated;
	@FXML public TextArea ld;
	@FXML public TextArea ld_translated;
	public AutoCompleteBox_CharClassification classification;
	@FXML public TextField classification_style;
	@FXML public TextField search_text;
	
	@FXML ToolBar toolBar;
	@FXML Button paneToggle;
	@FXML Button classDDButton;
	@FXML
	public Button exportButton;
	@FXML ToggleButton googleButton;
	@FXML ToggleButton tableButton;
	@FXML ToggleButton taxoButton;
	@FXML public ToggleButton imageButton;
	@FXML public ToggleButton charButton;
	@FXML public ToggleButton conversionToggle;
	
	@FXML Button prop1;
	@FXML Button prop2;
	@FXML Button prop3;
	@FXML Button prop4;
	@FXML Button prop5;
	
	@FXML public TextField max_field_uom;
	@FXML public TextField max_field;
	@FXML public TextField min_field_uom;
	@FXML public TextField min_field;
	@FXML public TextField note_field_uom;
	@FXML public TextField note_field;
	@FXML public TextField rule_field;
	public  AutoCompleteBox_UnitOfMeasure uom_field;
	//@FXML TextField uom_field;
	@FXML public TextField value_field;
	@FXML public TextField translated_value_field;
	
	@FXML Label custom_label_11;
	@FXML Label custom_label_12;
	@FXML Label custom_label_21;
	@FXML Label custom_label_22;
	@FXML Label rule_label;
	@FXML Label value_label;
	@FXML Label note_label;
	@FXML Label custom_label_value;
	
	
	public UserAccount account;

	
	private String user_language_gcode;
	private String data_language_gcode;
	public String user_language;
	public String data_language;

	
	public TablePane_CharClassif tableController;

	
	
	public Browser_CharClassif browserController;
	public StringProperty externalBrowserUrlProperty = new SimpleStringProperty();;
	
	private GridPane imageGrid;
	private GridPane ruleGrid;
	
	public ArrayList<Button> propButtons;
	


	private String lastRightPane="";


	private ImagePane_CharClassif imagePaneController;


	public ArrayList<String> CNAME_CID;
	public CharPane_CharClassif charPaneController;

	public CharClassifProposer proposer;



	private TextField[] uiDataFields;



	public AutoCompleteBox_CharValue translationAutoComplete;



	public AutoCompleteBox_CharValue valueAutoComplete;
	
	
	@FXML void nextBlank() {
	}
	@FXML void previousBlank() {
		}
	@FXML void firstBlank() {
	
	}
	@FXML void lastBlank() {
	
	}

	@SuppressWarnings("static-access")
	@FXML void switchPane() {
		if(grid.getColumnSpan(leftAnchor)==5) {
			setBottomRegionColumnSpans(false);
		}else {
			//Full window
			setBottomRegionColumnSpans(true);
		}
	}
	
	@FXML void classDD() {
	}
	
	@SuppressWarnings({ "resource", "unused" })
	@FXML void export() throws SQLException, ClassNotFoundException {
		
		try {
			Optional<ArrayList<Boolean>> choice = DescriptionExportExcelSheets.choicePopUp();
			if(choice.isPresent()){
				CharDescriptionExportServices.ExportItemDataForClass(null,this,choice.get().get(0),choice.get().get(1),choice.get().get(2),choice.get().get(3));
			}

		} catch (IOException e) {
			e.printStackTrace(System.err);
    		ConfirmationDialog.show("File saving failed", "Results could not be saved.\nMake sure you have the rights to create files in this folder and that the file is not open by another application", "OK");
		}
		
	}
	@FXML void initialize(){
		sd.setText("");
		sd_translated.setText("");
		ld.setText("");
		ld_translated.setText("");
		
		taxoButton.setDisable(true);
		
		toolBarButtonListener();
		initializePropButtons();

		
	}
	
	
	
	private void initializePropButtons() {
		propButtons = new ArrayList<Button> (GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS_OLD);
		propButtons.add(prop1);
		propButtons.add(prop2);
		propButtons.add(prop3);
		propButtons.add(prop4);
		propButtons.add(prop5);
		
		
		search_text.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (newPropertyValue)
		        {
		        	;
		        	 Platform.runLater(
		                     new Runnable() {
		                         @Override
		                         public void run() {

		         		            search_text.end();
		         		            search_text.selectAll();
		         		            
		                         }
		                     });
		        }
		    }

		});
		
		
	}
	private void toolBarButtonListener() {
		conversionToggle.setText("Value conversion: Yes");
		conversionToggle.setTooltip(new Tooltip("Display item values only in allowed uoms"));
		
		conversionToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
				if(newVal) {
					conversionToggle.setText("Value conversion: No");
				}else {
					conversionToggle.setText("Value conversion: Yes");
				}
				refresh_ui_display();
				tableController.tableGrid.refresh();
			}
			
		});

	}

	private void listen_for_keyboard_events() {
		
		
		uiDataFields = new TextField[] {value_field,uom_field,min_field_uom,max_field_uom,note_field_uom,rule_field,min_field,max_field,note_field,translated_value_field};
		
		sd.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                try {
					handleKeyBoardEvent(keyEvent,true);
				} catch (IOException | ParseException | ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
		
		sd.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                try {
					handleKeyBoardEvent(keyEvent,false);
				} catch (IOException | ParseException | ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
		
		sd.getScene().getWindow().focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		    	if(!newPropertyValue) {
		    		account.PRESSED_KEYBOARD.keySet().forEach(k->account.PRESSED_KEYBOARD.put(k, false));
		    	}
		    }
		});
		
		Arrays.asList(uiDataFields).forEach(n->{
			n.setOnKeyPressed(new EventHandler<KeyEvent>() 
	        {
	            public void handle(final KeyEvent keyEvent) 
	            {
	            	handleDataKeyBoardEvent(keyEvent,true);
	            }
	        });
		});
		
		Arrays.asList(uiDataFields).forEach(n->{
			n.focusedProperty().addListener(new ChangeListener<Boolean>(){

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(newValue) {
						n.selectAll();
					}
				}
				
			});
		});

		sd.getScene().getWindow().focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue && externalBrowserUrlProperty.get()!=null){
					UrlBookMarkDialog.promptBookMarkForItemClass(externalBrowserUrlProperty.get(),tableController.tableGrid.getSelectionModel().getSelectedItem(), externalBrowserUrlProperty);
				}
			}
		});
	}

	protected void handleDataKeyBoardEvent(KeyEvent keyEvent, boolean pressed) {
		
		if(keyEvent.getCode().equals(KeyCode.CONTROL)) {
			account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.SHIFT)) {
			account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
			account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.ENTER)) {
			account.PRESSED_KEYBOARD.put(KeyCode.ENTER, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.I)) {
			account.PRESSED_KEYBOARD.put(KeyCode.I, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.P)) {
			account.PRESSED_KEYBOARD.put(KeyCode.P, pressed);
		}
		
		if(keyEvent.getCode().equals(KeyCode.DOWN)) {
			account.PRESSED_KEYBOARD.put(KeyCode.DOWN, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.UP)) {
			account.PRESSED_KEYBOARD.put(KeyCode.UP, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.K)) {
			account.PRESSED_KEYBOARD.put(KeyCode.K, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.M)) {
			account.PRESSED_KEYBOARD.put(KeyCode.M, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.TAB)) {
			account.PRESSED_KEYBOARD.put(KeyCode.TAB, pressed);
		}
		
		
		Optional<TextField> focusedDataField = Arrays.asList(uiDataFields).stream().filter(e->e.isFocused()).findAny();
		if(focusedDataField.isPresent()) {
			if(account.PRESSED_KEYBOARD.get(KeyCode.TAB)) {
				Node pbNode = validateDataFields();
				if(!(pbNode!=null)) {
					//Do nothing all items so far are valid
				}else {
					
					int pbIdx = Arrays.asList(uiDataFields).indexOf(pbNode);
					int fcsIdx = Arrays.asList(uiDataFields).indexOf(focusedDataField.get());
					
					//If pbNode is at or before focusedDataField, return to pbNode
					if(pbIdx<=fcsIdx) {
						
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								uiDataFields[pbIdx].requestFocus();
							}
							
						});
						
					}else {
						
						//The pbNode is at the current focused field or after, do nothing
					}
				}
			}
			if(account.PRESSED_KEYBOARD.get(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)
					&& !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
				account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
				Boolean TranslationProcessResult = CheckForTranslationValidity();
				validateFieldsThenSkipToNext(TranslationProcessResult);
				
			}
			
			if((!this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) && account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
				tableController.tableGrid.getSelectionModel().clearAndSelect(tableController.tableGrid.getSelectionModel().getSelectedIndex()+1);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						value_field.end();
						value_field.selectAll();
					}
				});
			}
			if((!this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) && account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
				tableController.tableGrid.getSelectionModel().clearAndSelect(tableController.tableGrid.getSelectionModel().getSelectedIndex()-1);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						value_field.end();
						value_field.selectAll();
					}
				});
				
			}
			
						
		}
		
	}
	public void validateFieldsThenSkipToNext(Boolean TranslationProcessResult) {
		Node pbNode = validateDataFields();
		if(pbNode!=null) {
			//The item is not valid, focus on problem
			
			int pbIdx = Arrays.asList(uiDataFields).indexOf(pbNode);
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					uiDataFields[pbIdx].requestFocus();
				}
				
			});
			
		}else {
			//Skip to next item
			try{
				int idx = tableController.tableGrid.getSelectionModel().getSelectedIndex();
				if(TranslationProcessResult!=null) {
					if(classCombo.getValue().getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
						CharValuesLoader.updateDefaultCharValue(idx,this);
					}else {
						CharValuesLoader.storeItemDatafromScreen(idx,this);
					}
				}
				tableController.tableGrid.getSelectionModel().clearAndSelect(idx+1);
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
		}
	}
	private Boolean CheckForTranslationValidity() {
		ClassCaracteristic active_char;
		if(this.classCombo.getValue().getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			active_char = CharItemFetcher.defaultCharValues.get(tableController.tableGrid.getSelectionModel().getSelectedIndex()).getKey();
			 
		}else {
			int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
			active_char = CharValuesLoader.active_characteristics.get(classCombo.getValue().getClassSegment())
					.get(active_char_index);
		}
		
		if(active_char.getIsTranslatable()) {
			if(value_field.isFocused() && value_field.getText()!=null && value_field.getText().length()>0) {
				return valueAutoComplete.processValueOnFocusLost(true);
			}
			if(translated_value_field.getText()!=null && translated_value_field.getText().length()>0) {
				return translationAutoComplete.processValueOnFocusLost(false);
			}
			
			if(value_field.isFocused()) {
				return valueAutoComplete.processEmptyValueOnFocusLost(true);
			}
			
			if(translated_value_field.isFocused()) {
				return translationAutoComplete.processEmptyValueOnFocusLost(false);
			}
			
		}
		return true;
	}
	protected void handleKeyBoardEvent(KeyEvent keyEvent, boolean pressed) throws IOException, ParseException, ClassNotFoundException, SQLException {
		
		
		
		
		if(keyEvent.getCode().equals(KeyCode.CONTROL)) {
			account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.SHIFT)) {
			account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
			account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.ENTER)) {
			account.PRESSED_KEYBOARD.put(KeyCode.ENTER, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.I)) {
			account.PRESSED_KEYBOARD.put(KeyCode.I, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.P)) {
			account.PRESSED_KEYBOARD.put(KeyCode.P, pressed);
		}
		
		if(keyEvent.getCode().equals(KeyCode.DOWN)) {
			account.PRESSED_KEYBOARD.put(KeyCode.DOWN, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.UP)) {
			account.PRESSED_KEYBOARD.put(KeyCode.UP, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.K)) {
			account.PRESSED_KEYBOARD.put(KeyCode.K, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.M)) {
			account.PRESSED_KEYBOARD.put(KeyCode.M, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.TAB)) {
			account.PRESSED_KEYBOARD.put(KeyCode.TAB, pressed);
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.SHIFT) && account.PRESSED_KEYBOARD.get(KeyCode.ENTER)) {
			this.lastRightPane="";
			try {
				if(this.charButton.isSelected()) {
					this.lastRightPane="CHARS";
				}
			}catch(Exception V) {
				
			}
			
			try {
				if(this.imageButton.isSelected()) {
					this.lastRightPane="IMAGES";
				}
			}catch(Exception V) {
				
			}
			
			imageButton.setSelected(true);
			launch_search(true);
		}
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.ENTER)) {
			int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]).size());
			try{
				CharPatternServices.scanSelectionForPatternDetection(this,
						CharValuesLoader.active_characteristics.get(tableController.tableGrid.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0])
								.get(active_char_index));
			}catch (Exception V){
				V.printStackTrace(System.err);
			}
		}
		
		
		
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.ESCAPE)) {
			
			
			try {
				imagePaneController.imagePaneClose();
			}catch(Exception V) {
				
			}
			try {
				charPaneController.PaneClose();
			}catch(Exception V) {
				
			}
			
			try {
					//We clicked escape to close search
					try {
						show_table();
					}catch(Exception V) {
						
					}
					try{
						System.out.println("Closing Secondary Stage");
						browserController.secondaryStage.close();
					}catch (Exception V){

					}
					if(this.lastRightPane.equals("CHARS")) {
						charButton.setSelected(true);
						load_char_pane();
					}
					if(this.lastRightPane.equals("IMAGES")) {
						imageButton.setSelected(true);
						load_image_pane(true);
					}
					this.lastRightPane="";
			}catch(Exception V) {
				this.lastRightPane="";
			}
			
			
			
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.I)) {
			if(this.charButton.isSelected()) {
				this.charPaneController.PaneClose();
			}else {
				charButton.setSelected(true);
				load_char_pane();
			}
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.P)) {
			if(this.imageButton.isSelected()) {
				this.imagePaneController.imagePaneClose();
			}else {
				imageButton.setSelected(true);
				load_image_pane(true);
			}
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.K) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			tableController.previousChar();
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.M) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			tableController.nextChar();
		}
		
		
		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && pressed && keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1))) {
			firePropositionButton(1);
		}
		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && pressed && keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2))) {
			firePropositionButton(2);
		}
		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && pressed && keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3))) {
			firePropositionButton(3);
		}
		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && pressed && keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4))) {
			firePropositionButton(4);
		}
		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && pressed && keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5))) {
			firePropositionButton(5);
		}

		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
			tableController.fireScrollNBDown();
		}

		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
			tableController.fireScrollNBUp();
		}
		
		
		
		
		
		
		return;
		
	}

		

	private Node validateDataFields() {
		CharDescriptionRow row = tableController.tableGrid.getSelectionModel().getSelectedItem();
		String selectedRowClass = row.getClass_segment_string().split("&&&")[0];
		ClassCaracteristic active_char;
		int active_char_index;
		if(this.classCombo.getValue().getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			active_char_index = 0;
			active_char = CharItemFetcher.defaultCharValues.get(tableController.tableGrid.getSelectionModel().getSelectedIndex()).getKey();
			 
		}else {
			active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(selectedRowClass).size());
			active_char = CharValuesLoader.active_characteristics.get(selectedRowClass).get(active_char_index);
		}
		
		if(validateValueField(active_char,value_field) && value_field.isVisible()) {
			return value_field;
		};
		
		if(validateValueField(active_char,min_field) && min_field.isVisible()) {
			return min_field;
		};
		if(validateValueField(active_char,max_field) && max_field.isVisible()) {
			return max_field;
		};
		if(validateValueField(active_char,min_field_uom) && min_field_uom.isVisible()) {
			return min_field_uom;
		};
		if(validateValueField(active_char,max_field_uom) && max_field_uom.isVisible()) {
			return max_field_uom;
		};
		
		if(validateUomField(row,active_char_index) && uom_field.isVisible()) {
			uom_field.setUom(uom_field.getEntries().get(0));
			if(active_char.getAllowedUoms().size()>1){
				return uom_field;
			}
		}
		
		/*
		if(min_field.getText()!=null && min_field.getText().length()>0 && min_field.isVisible()) {
			try {
				Double.valueOf(min_field.getText().trim().replace(",", "."));
			}catch(Exception V) {
				min_field.setText(null);
				return min_field;
			}
		}
		if(max_field.getText()!=null && max_field.getText().length()>0 && max_field.isVisible()) {
			try {
				Double.valueOf(max_field.getText().trim().replace(",", "."));
			}catch(Exception V) {
				max_field.setText(null);
				return max_field;
			}
		}
		
		if(min_field_uom.getText()!=null && min_field_uom.getText().length()>0 && min_field_uom.isVisible()) {
			try {
				Double.valueOf(min_field_uom.getText().trim().replace(",", "."));
			}catch(Exception V) {
				min_field_uom.setText(null);
				return min_field_uom;
			}
		}
		if(max_field_uom.getText()!=null && max_field_uom.getText().length()>0 && max_field_uom.isVisible()) {
			try {
				Double.valueOf(max_field_uom.getText().trim().replace(",", "."));
			}catch(Exception V) {
				max_field_uom.setText(null);
				return max_field_uom;
			}
		}
		*/
		return null;
	}
	private boolean validateUomField(CharDescriptionRow row, int active_char_index) {
		String uomFieldText = uom_field.getText();
		if(uomFieldText!=null && uomFieldText.length()>0) {
			String row_class_id = row.getClass_segment_string().split("&&&")[0];
			Optional<UnitOfMeasure> matchinguom = UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->u.toString().equals(uomFieldText)).findAny();
			if(matchinguom.isPresent()) {
				
				if(UnitOfMeasure.ConversionPathExists(matchinguom.get(), CharValuesLoader.active_characteristics.get(row_class_id).get(active_char_index).getAllowedUoms())) {
					return false;
				}else {
					
					uom_field.setText("");
					return true;
				}
			}else {
				
				uom_field.setText("");
				return true;
			}
			
		}
		return uom_field.isVisible();
	}
	private boolean validateValueField(ClassCaracteristic active_char,TextField target_field) {
		String originalValue = target_field.getText();
		if(!(originalValue!=null) || originalValue.length()==0) {
			return false;
		}
		if(active_char.getIsNumeric()) {
			ArrayList<Double> numValuesInSelection = WordUtils.parseNumericalValues(originalValue);
			
			UnitOfMeasure finishingUom = null;
			boolean hasFinishingText=false;
			
			String selected_text = WordUtils.textWithoutParsedNumericalValues(originalValue);
			ArrayList<String> textBetweenNumbers = new ArrayList<String>();
			String[] textBetweenNumberstmp = (selected_text).split("%\\d");
			for(int i=0;i<textBetweenNumberstmp.length;i++) {
				textBetweenNumbers.add(textBetweenNumberstmp[i]);
			}
			
			
			String finishingText = null;
			if(active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
				//Numeric with uom
				try{
					finishingText = textBetweenNumbers.get(textBetweenNumbers.size()-1);
					if(originalValue.trim().endsWith(finishingText)) {
						if(numValuesInSelection.size()==2 && selected_text.replace(" ", "").contains("%1/%2")) {
							Double first = numValuesInSelection.get(0);
							Double second = numValuesInSelection.get(1);
							numValuesInSelection.clear();
							numValuesInSelection.add(first/second);
							
						}
						hasFinishingText=true;
					}else {
						if(finishingText.trim().equals("/")) {
							if(numValuesInSelection.size()==2) {
								Double first = numValuesInSelection.get(0);
								Double second = numValuesInSelection.get(1);
								numValuesInSelection.clear();
								numValuesInSelection.add(first/second);
								hasFinishingText=false;
								finishingText=null;
							}
						}else {
							if(finishingText.trim().equals("")) {
								if(numValuesInSelection.size()==2) {
									hasFinishingText=false;
									finishingText=null;
								}else {
									target_field.setText(null);
									return true;
								}
							}else {
								target_field.setText(null);
								return true;
							}
						}
						
					}
				}catch(Exception V) {
					hasFinishingText=false;
				}
				//String finishingValue = WordUtils.DoubleToString(numValuesInSelection.get(numValuesInSelection.size()-1));
				ArrayList<UnitOfMeasure> uomsInSelection = WordUtils.parseCompatibleUoMs("%9"+finishingText,active_char);
				if(uomsInSelection.size()>0) {
					finishingUom = uomsInSelection.get(0);
					if(finishingUom!=null) {
						
					}
					
					
				}else {
					finishingUom = null;
				}
				if(numValuesInSelection.size()==0) {
					target_field.setText(null);
					return true;
				}
				if(numValuesInSelection.size()==1) {
					target_field.setText(WordUtils.DoubleToString(numValuesInSelection.get(0)));
					if(finishingUom!=null) {
						uom_field.setText(finishingUom.toString());
						return false;
					}
					if(hasFinishingText) {
						UoMDeclarationDialog.UomDeclarationPopUpAfterFailedFieldValidation(finishingText, uom_field, active_char);
					}
					return false;
				}
				
				if(numValuesInSelection.size()==2) {
					target_field.setText(null);
					min_field_uom.setText(WordUtils.DoubleToString(Collections.min(numValuesInSelection)));
					max_field_uom.setText(WordUtils.DoubleToString(Collections.max(numValuesInSelection)));
					if(finishingUom!=null) {
						uom_field.setText(finishingUom.toString());
						return false;
					}
					if(hasFinishingText) {
						UoMDeclarationDialog.UomDeclarationPopUpAfterFailedFieldValidation(finishingText, uom_field, active_char);
					}
					return false;
				}
				target_field.setText(null);
				return true;
			}else {
				
				//Numeric w/o UOM
				try{
					finishingText = textBetweenNumbers.get(textBetweenNumbers.size()-1);
					if(originalValue.trim().endsWith(finishingText)) {
						
						target_field.setText(null);
						return true;
					}else {
						if(finishingText.trim().equals("/")) {
							
							if(numValuesInSelection.size()==2) {
								Double first = numValuesInSelection.get(0);
								Double second = numValuesInSelection.get(1);
								numValuesInSelection.clear();
								numValuesInSelection.add(first/second);
							}
						}else {
							if(finishingText.trim().equals("")) {
								
								if(numValuesInSelection.size()==2) {
									
								}else {
									target_field.setText(null);
									return true;
								}
							}else {
								
								target_field.setText(null);
								return true;
							}
						}
						
						
					}
				}catch(Exception V) {

				}
				
				if(numValuesInSelection.size()==0) {
					target_field.setText(null);
					return true;
				}
				if(numValuesInSelection.size()==1) {
					target_field.setText(WordUtils.DoubleToString(numValuesInSelection.get(0)));
					return false;
				}

				if(numValuesInSelection.size()==2) {
					target_field.setText(null);
					min_field.setText(WordUtils.DoubleToString(Collections.min(numValuesInSelection)));
					max_field.setText(WordUtils.DoubleToString(Collections.max(numValuesInSelection)));
					return false;
				}
				target_field.setText(null);
				return true;
			}
		}
		return false;
	}
	
	private void launch_search(boolean checkMethodSelect) throws IOException, ParseException {
		load_image_pane(checkMethodSelect);
		search_google_inplace(checkMethodSelect);
	}
	
	@FXML void search_image() throws IOException, ParseException {
		load_image_pane(false);
	}
	
	@FXML void view_chars() throws IOException, ParseException, ClassNotFoundException, SQLException {
		load_char_pane();
	}
	
	
	@FXML public void show_table() throws IOException, ParseException {
		
		tableButton.setSelected(true);
		
		try {
			browserController.switch_pane_hide_browser(true);
		}catch(Exception V) {
			
		}
		
		try {
			tableController.tableGrid.setVisible(true);
		}catch(Exception V) {
			
		}
		tableController.tableGrid.requestFocus();
	}
	
	public void setUserAccount(UserAccount account) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		this.account = account;
		this.account.setUser_desc_classes(  Tools.get_desc_classes(account,menubar) );
		this.account.setUser_desc_class(Tools.get_desc_class(account));
		this.proposer = new CharClassifProposer(this);
		
		CNAME_CID = Tools.SET_PROJECT_CLASSES_ARRAY(account);
		if(!(this.account.getUser_desc_class()!=null)) {
			try {
				this.account.setUser_desc_class( this.account.getUser_desc_classes()[0] );
			}catch(Exception V) {
				//User has no available classes for harmonization
			}
		}
		
		Tools.decorate_menubar(menubar,account);
		//decorate_description_bar(false);
		decorate_class_combobox();
		
		
		
		this.user_language_gcode = Tools.get_project_user_language_code(account.getActive_project());
		this.data_language_gcode = Tools.get_project_data_language_code(account.getActive_project());
		this.user_language = Tools.get_project_user_language(account.getActive_project());
		this.data_language = Tools.get_project_data_language(account.getActive_project());
		
		
		//this.UOMS = Tools.get_units_of_measures(user_language_gcode);
		UnitOfMeasure.RunTimeUOMS = UnitOfMeasure.fetch_units_of_measures(user_language_gcode);
		
		//classification tmp.setNew_segment_name( CID2NAME,j,parent_controller);
		classification = new AutoCompleteBox_CharClassification(this,classification_style.getStyle(),account);
		for( String entry : CNAME_CID) {
			classification.getEntries().add(entry);
		}
		uom_field = new AutoCompleteBox_UnitOfMeasure("SYMBOL");
		
		classification_style.setVisible(false);
		grid.add(classification, 1, 9);
		grid.add(uom_field, 5, 9);
		
		
		load_table_pane();
		tableController.collapseGrid(false,grid);
		listen_for_keyboard_events();
		//load_image_pane();
		//load_rule_pane();
		//load_taxo_pane();
		
		
		
		
		
	}

	
	private void decorate_class_combobox() {
		classCombo.getItems().clear();
		if(account.getUser_desc_classes()!=null) {
			for(String entry:account.getUser_desc_classes()) {
				for(String elem:CNAME_CID) {
					if (elem.startsWith(entry)) {
						CharDescClassComboRow tmp = new CharDescClassComboRow(elem.split("&&&")[0],elem.split("&&&")[1],elem.split("&&&")[2]);
						classCombo.getItems().add(tmp);
						if(elem.split("&&&")[0].equals(account.getUser_desc_class())) {
							classCombo.getSelectionModel().select(tmp);
						}
					}
				}
				
				
			}
		}else {
			
		}
		
		CharDescClassComboRow tmp = new CharDescClassComboRow(GlobalConstants.DEFAULT_CHARS_CLASS,GlobalConstants.DEFAULT_CHARS_CLASS,GlobalConstants.DEFAULT_CHARS_CLASS);
		classCombo.getItems().add(tmp);
		
		classCombo.getItems().sort(new Comparator<CharDescClassComboRow>(){

			@Override
			public int compare(CharDescClassComboRow o1, CharDescClassComboRow o2) {
				// TODO Auto-generated method stub
				return o1.getClassCode().compareTo( o2.getClassCode());
			}
			
		});
		
		classCombo.setOnAction(e -> {
			try {
				tableController.refresh_table_with_segment(classCombo.getValue().getClassSegment());
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
	}
	@SuppressWarnings("static-access")
	private void load_table_pane() throws IOException, ClassNotFoundException, SQLException {
		
		tableButton.setSelected(true);
		setBottomRegionColumnSpans(false);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/TablePane_CharClassif.fxml"));
		GridPane tableGrid = loader.load();
		tableController = loader.getController();
		leftAnchor.getChildren().setAll(tableGrid);
		leftAnchor.setTopAnchor(tableGrid, 0.0);
		leftAnchor.setBottomAnchor(tableGrid, 0.0);
		leftAnchor.setLeftAnchor(tableGrid, 0.0);
		leftAnchor.setRightAnchor(tableGrid, 0.0);
		
		tableController.setParent(this);
		tableController.setUserAccount(account);
		
		tableController.setUserLanguageGcode(user_language_gcode);
		/*
		if(GlobalConstants.MANUAL_FETCH_ALL) {
			tableController.fillTable_STATIC((List<ItemFetcherRow>) ftc.currentList_STATIC);
		}else {
			tableController.fillTable_DYNAMIC((List<ItemFetcherRow>) ftc.currentList_DYNAMIC);
		}*/
		tableController.setCollapsedViewColumns(new String[] {"Completion status","Question status"});
		tableController.refresh_table_with_segment(account.getUser_desc_class(classCombo.getItems().get(1).getClassSegment()));
		System.gc();
		 
		
	}

	@SuppressWarnings("static-access")
	public void load_image_pane(boolean checkMethodSelect) throws IOException, ParseException {
		

		if(!imageButton.isSelected()) {
			return;
		}
		
		setBottomRegionColumnSpans(true);
		if(imagePaneController!=null) {
			rightAnchor.getChildren().setAll(imageGrid);
			rightAnchor.setTopAnchor(imageGrid, 0.0);
			rightAnchor.setBottomAnchor(imageGrid, 0.0);
			rightAnchor.setLeftAnchor(imageGrid, 0.0);
			rightAnchor.setRightAnchor(imageGrid, 0.0);
			
			imagePaneController.search_image(checkMethodSelect);
			;
			
		}else {
			;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/ImagePane_CharClassif.fxml"));
			imageGrid = loader.load();
			rightAnchor.getChildren().setAll(imageGrid);
			rightAnchor.setTopAnchor(imageGrid, 0.0);
			rightAnchor.setBottomAnchor(imageGrid, 0.0);
			rightAnchor.setLeftAnchor(imageGrid, 0.0);
			rightAnchor.setRightAnchor(imageGrid, 0.0);
			imagePaneController = loader.getController();
			imagePaneController.setParent(this);
			imagePaneController.search_image(checkMethodSelect);
		}
		
		
	}
	
	@SuppressWarnings("static-access")
	public void load_char_pane() throws IOException, ClassNotFoundException, SQLException {
		if(!charButton.isSelected()) {
			return;
		}
		
		setBottomRegionColumnSpans(true);
		if(charPaneController!=null) {
			rightAnchor.getChildren().setAll(ruleGrid);
			rightAnchor.setTopAnchor(ruleGrid, 0.0);
			rightAnchor.setBottomAnchor(ruleGrid, 0.0);
			rightAnchor.setLeftAnchor(ruleGrid, 0.0);
			rightAnchor.setRightAnchor(ruleGrid, 0.0);
			
			charPaneController.load_item_chars();
			;
			
		}else {
			;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/CharPane_CharClassif.fxml"));
			ruleGrid = loader.load();
			rightAnchor.getChildren().setAll(ruleGrid);
			
			rightAnchor.setTopAnchor(ruleGrid, 0.0);
			rightAnchor.setBottomAnchor(ruleGrid, 0.0);
			rightAnchor.setLeftAnchor(ruleGrid, 0.0);
			rightAnchor.setRightAnchor(ruleGrid, 0.0);
			
			charPaneController = loader.getController();
			charPaneController.setParent(this);
			charPaneController.load_item_chars();
			
		}
		
		
	}
	
	private void load_browser_pane() throws IOException {
		googleButton.setSelected(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/Browser_CharClassif.fxml"));
		loader.load();
		browserController = loader.getController();
		browserController.setParent(this);
	}

	public void setBrowserFullScreen(){
		leftAnchor.toFront();
		grid.setRowIndex(leftAnchor,2);
		grid.setColumnIndex(leftAnchor,0);
		grid.setRowSpan(leftAnchor,GridPane.REMAINING);
		grid.setColumnSpan(leftAnchor,GridPane.REMAINING);
	}

	@SuppressWarnings("static-access")
	public void setBottomRegionColumnSpans(boolean visibleRight) {
		grid.setRowIndex(leftAnchor,grid.getRowIndex(rightAnchor));
		grid.setColumnIndex(leftAnchor,1);
		grid.setRowSpan(leftAnchor,grid.getRowSpan(rightAnchor));
		if(visibleRight) {
			rightAnchor.getChildren().removeAll(rightAnchor.getChildren());
			rightAnchor.setVisible(true);
			grid.setColumnSpan(rightAnchor, 3);
			grid.setColumnSpan(leftAnchor, 5);
			
		}else {
			rightAnchor.getChildren().removeAll(rightAnchor.getChildren());
			rightAnchor.setVisible(false);
			grid.setColumnSpan(leftAnchor, 9);
		}
		try{
			;
			tableController.collapseGrid(visibleRight,grid);
		}catch(Exception V) {
			//V.printStackTrace(System.err);
		}
		value_field.requestFocus();
		hideAutoCompletePopups();
		value_field.end();
		value_field.selectAll();
	}

	public void fireClassChange(String result) {
		this.CHANGING_CLASS=true;
		try {
			this.tableController.fireManualClassChange(result,true);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.CHANGING_CLASS=false;
	}

	
	@FXML void search_google() throws MalformedURLException, UnsupportedEncodingException, IOException, URISyntaxException {
		
		if(search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}
		
		
		Desktop.getDesktop().browse(new URL("https://www.google.com/search?q="+URLEncoder.encode(search_text.getText(),"UTF-8")).toURI());
		
	}
	
	@FXML void search_google_inplace() throws IOException {
		search_google_inplace(false);
	}
	
	void search_google_inplace(boolean checkMethodSelect) throws IOException {
		
		googleButton.setSelected(true);
		
		/*if(search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}*/
		
		try{
			browserController.search_google_inplace(checkMethodSelect);
		}catch(Exception V) {
			load_browser_pane();
			browserController.search_google_inplace(checkMethodSelect);
		}
        
	
	}


	
	@FXML void context1() throws IOException {
		
	}
	@FXML void context2() throws IOException {
		
		}
	@FXML void context3() throws IOException {
		
	}
	@FXML void context4() throws IOException {
		
	}
	@FXML void context5() throws IOException {
		
	}

	public void firePropositionButton(int i) {
		;
		try {
			Button button = propButtons.get(i-1);
			if(!button.isVisible()) {
				return;
			}
			button.fire();
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
	}
	
	@SuppressWarnings("static-access")
	public  void refresh_ui_display() {
		proposer.clearPropButtons();
		double RIGHT_TRANSLATE = 0.0*rule_field.getWidth();
		clear_data_fields();
		if(this.charButton.isSelected()) {
			this.charPaneController.load_item_chars();
		}
		CharDescriptionRow row = (CharDescriptionRow) this.tableController.tableGrid.getSelectionModel().getSelectedItem();
		if(row!=null) {
			
		}else {
			return;
		}
		try {
			
			ClassCaracteristic active_char = null;
			int selected_col = 0;
			if(!this.classCombo.getValue().getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
				selected_col = Math.floorMod(tableController.selected_col, CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).size());
				active_char = CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).get(selected_col);
				proposer.loadCharRuleProps(row,row.getClass_segment_string().split("&&&")[0],selected_col,CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).size());
				
			}else {
				active_char = CharItemFetcher.defaultCharValues.get(tableController.tableGrid.getSelectionModel().getSelectedIndex()).getKey();
				
			}
			
			if(active_char.getIsNumeric()) {
				if( (active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0)) {
					
					
					UnitOfMeasure local_uom = null;
					String local_nom = null;
					String local_min = null;
					String local_max = null;
					
					CaracteristicValue val = row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col];
					try{
						Pair<ArrayList<String>, TextFlow> tmp = val.getFormatedDisplayAndUomPair(this, active_char);
						local_uom = UnitOfMeasure.RunTimeUOMS.get(tmp.getKey().get(0));
						local_nom = tmp.getKey().get(1);
						local_min = tmp.getKey().get(2);
						local_max = tmp.getKey().get(3);
					}catch(Exception V) {
					}
					
					
					//Add the allowed uoms to the autocomplete box
					for(String uom_id:active_char.getAllowedUoms()) {
						this.uom_field.getEntries().add(UnitOfMeasure.RunTimeUOMS.get(uom_id));
						if(!conversionToggle.isSelected()) {
							if(this.uom_field.getEntries().contains(local_uom)) {
								
							}else {
								this.uom_field.getEntries().add(local_uom);
							}
						}
					}
					
					
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						if(!conversionToggle.isSelected()) {
							value_field.setText(local_nom);
						}else {
							value_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNominal_value());
						}
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the uom
					custom_label_11.setText("Unit of measure");
					custom_label_11.setVisible(true);
					try{
						if(!conversionToggle.isSelected()) {
							uom_field.setUom(local_uom);
						}else{
							uom_field.setUom(UnitOfMeasure.RunTimeUOMS.get( row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getUom_id() ));
						}
						//uom_field.setText(UnitOfMeasure.RunTimeUOMS.get( row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getUom_id() ).getUom_symbol());
					}catch(Exception V) {
						
					}
					uom_field.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					uom_field.setVisible(true);
					ObservableList<Node> workingCollection = FXCollections.observableArrayList(grid.getChildren());
					int index = -1;
					int uomIndex = 0;
					int valueIndex = 0;
					for(Node node:workingCollection) {
						index+=1;
						if(node instanceof AutoCompleteBox_UnitOfMeasure) {
							uomIndex=index;
						}
						if(node instanceof TextField) {
							if((TextField) node == value_field) {
								valueIndex=index;
							}
						}
					}
					Tools.moveItemInCollection(uomIndex,valueIndex+1,workingCollection);
					grid.getChildren().setAll(workingCollection);
					
					//Setting the minimum value
					custom_label_12.setText("Minimum value");
					custom_label_12.setTranslateX(0.5*(rule_field.getWidth()+(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					custom_label_12.setVisible(true);
					try{
						if(!conversionToggle.isSelected()) {
							min_field_uom.setText(local_min);
						}else{
							min_field_uom.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getMin_value());
						}
					}catch(Exception V) {
						
					}
					min_field_uom.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					min_field_uom.setVisible(true);
					//Setting the maximum value
					custom_label_21.setText("Maximum value");
					custom_label_21.setVisible(true);
					try {
						if(!conversionToggle.isSelected()) {
							max_field_uom.setText(local_max);
						}else{
							max_field_uom.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getMax_value());
						}
					}catch(Exception V) {
						
					}
					max_field_uom.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					max_field_uom.setVisible(true);
					//Setting the note
					custom_label_22.setText("Note");
					custom_label_22.setTranslateX(0.5*(rule_field.getWidth()+(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					custom_label_22.setVisible(true);
					try {
						note_field_uom.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field_uom.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					note_field_uom.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNominal_value());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the minimum value
					custom_label_11.setText("Minimum value");
					custom_label_11.setTranslateX(+RIGHT_TRANSLATE);
					custom_label_11.setVisible(true);
					try{
						min_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getMin_value());
					}catch(Exception V) {
						
					}
					min_field.setVisible(true);
					min_field.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					
					//Setting the maximum value
					custom_label_12.setText("Maximum value");
					custom_label_12.setTranslateX(0.5*(rule_field.getWidth()+(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					custom_label_12.setVisible(true);
					try{
						max_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getMax_value());
					}catch(Exception V) {
						
					}
					max_field.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					max_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				
				}
			}else {
				if(active_char.getIsTranslatable() && !this.user_language_gcode.equals(this.data_language_gcode)) {
					//Setting the value
					value_label.setText("Value ("+this.data_language_gcode.toUpperCase()+")");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the translated value
					custom_label_value.setText("Value ("+this.user_language_gcode.toUpperCase()+")");
					custom_label_value.setVisible(true);
					try{
						//translated_value_field.setText(this.tableController.translate2UserLanguage(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNominal_value()));
						translated_value_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getUserLanguageValue());
						
					}catch(Exception V) {
						
					}
					translated_value_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					//Setting the value
					value_label.setText("Value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					this.grid.setColumnSpan(value_field, 3);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(row.getClass_segment_string().split("&&&")[0])[selected_col].getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}
				
			}
		}catch(Exception V) {
			V.printStackTrace(System.err);
			return;
		}
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				value_field.requestFocus();
				hideAutoCompletePopups();
				value_field.end();
				value_field.selectAll();
			}
			
		});
		
	}
	@SuppressWarnings("static-access")
	private void clear_data_fields() {
		
		this.grid.setColumnSpan(value_field, 1);
		
		uom_field.getEntries().clear();
		
		
		max_field_uom.setVisible(false);
		max_field.setVisible(false);
		min_field_uom.setVisible(false);
		min_field.setVisible(false);
		note_field_uom.setVisible(false);
		note_field.setVisible(false);
		rule_field.setVisible(false);
		uom_field.setVisible(false);
		value_field.setVisible(false);
		translated_value_field.setVisible(false);
		
		custom_label_11.setVisible(false);
		custom_label_12.setVisible(false);
		custom_label_21.setVisible(false);
		custom_label_22.setVisible(false);
		rule_label.setVisible(false);
		value_label.setVisible(false);
		note_label.setVisible(false);
		custom_label_value.setVisible(false);
		

		max_field_uom.setText("");
		max_field.setText("");
		min_field_uom.setText("");
		min_field.setText("");
		note_field_uom.setText("");
		note_field.setText("");
		rule_field.setText("");
		uom_field.setText("");
		value_field.setText("");
		translated_value_field.setText("");	
	}
	
	public void AssignValueOnSelectedItems(CaracteristicValue value) {

		//uiDirectValueRefresh(pattern_value);
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
		List<String> targetItemsIDs = tableController.tableGrid.getSelectionModel().getSelectedItems().stream().map(i->i.getItem_id()).collect(Collectors.toList());
		CharItemFetcher.allRowItems.parallelStream().filter(e->targetItemsIDs.contains(e.getItem_id()))
						.forEach(r->{
							CharValuesLoader.updateRuntimeDataForItem(r,this.classCombo.getValue().getClassSegment(),active_char_index,value);
							CharDescriptionExportServices.addItemCharDataToPush(r, this.classCombo.getValue().getClassSegment(), active_char_index,CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
							
						});
		CharDescriptionExportServices.flushItemDataToDB(account);
		TranslationServices.beAwareOfNewValue(value, CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).get(active_char_index));
		//TranslationServices.addThisValueToTheCharKnownSets(pattern_value, tableController.active_characteristics.get(this.classCombo.getValue().getClassSegment()).get(active_char_index),true);
		
		refresh_ui_display();
		tableController.tableGrid.refresh();
		
	}
	public void sendSemiAutoPattern(CaracteristicValue pattern_value, String ruleString) {
		
		pattern_value.setSource(DataInputMethods.SEMI_CHAR_DESC);
		pattern_value.setAuthor(account.getUser_id());
		
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
		ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(classCombo.getValue().getClassSegment())
		.get(active_char_index);
		
		if(active_char.getIsNumeric()) {
			ruleString=WordUtils.reducePatternRuleSeparators(ruleString);
		}else {
			String[] SEPARATORS = new String[] {",","."," ","=",":","+","-","/","|","\\"};
			String loopRule = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
			while(true) {
				loopRule = WordUtils.reduceExtremetiesPatternRuleSeparators(ruleString,SEPARATORS,new ArrayList<String>());
				if(ruleString.equals(loopRule)) {
					break;
				}
				
				ruleString = loopRule;
			}
		}
		pattern_value.setRule_id(ruleString);
		rule_field.setText(ruleString);
		CharPatternServices.applyItemRule(this);
		AssignValueOnSelectedItems(pattern_value);
		
	}
	
	public void sendPatternRule(String ruleString) {
	}
	
	public void sendPatternValue(CaracteristicValue pattern_value) {	
	}
	
	@SuppressWarnings("unused")
	private void uiDirectValueRefresh(CaracteristicValue pattern_value) {
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
		ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(classCombo.getValue().getClassSegment())
		.get(active_char_index);
		
		if(active_char.getIsNumeric()) {
			if(active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
				try {
					uom_field.setUom(UnitOfMeasure.RunTimeUOMS.get(pattern_value.getUom_id()));
					//uom_field.setText(UnitOfMeasure.RunTimeUOMS.get(pattern_value.getUom_id()).getUom_symbol());
				}catch(Exception V) {
					
				}
			}
			try {
				min_field_uom.setText(pattern_value.getMin_value());
			}catch(Exception V) {
				
			}
			try {
				max_field_uom.setText(pattern_value.getMax_value());
			}catch(Exception V) {
				
			}
			try {
				value_field.setText(pattern_value.getNominal_value());
			}catch(Exception V) {
				
			}
			
		}else {
			value_field.setText(pattern_value.getDataLanguageValue());
			try {
				translated_value_field.setText(pattern_value.getUserLanguageValue());
			}catch(Exception V) {
				translated_value_field.setText("");
			}
		}
	}
	public void preparePatternProposition(int i, String buttonText, CaracteristicValue preparedValue,
			String preparedRule, ClassCaracteristic active_char) {
		
		preparedRule=WordUtils.reducePatternRuleSeparators(preparedRule);
		try{
			proposer.addSemiAutoProposition(buttonText,preparedValue,preparedRule,active_char);
		}catch(Exception V) {
			
		}
		
	}
	public void preparePatternProposition(String buttonText, CaracteristicValue preparedValue,
			String preparedRule, ClassCaracteristic active_char) {
		if(active_char.getIsNumeric()) {
			preparedRule=WordUtils.reducePatternRuleSeparators(preparedRule);
		}
		try{
			proposer.addSemiAutoProposition(buttonText,preparedValue,preparedRule,active_char);
		}catch(Exception V) {
			
		}
		
	}
	public void hideAutoCompletePopups() {
		try{
			valueAutoComplete.hidePopUp();
		}catch(Exception V) {
			
		}
		try{
			translationAutoComplete.hidePopUp();
		}catch(Exception V) {
			
		}
	}
	
	public void refreshAutoCompleteEntries() {
		try{
			valueAutoComplete.refresh_entries(true);
		}catch(Exception V) {
			
		}
		try{
			translationAutoComplete.refresh_entries(false);
		}catch(Exception V) {
			
		}
	}
	
	
	
}
