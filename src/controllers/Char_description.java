package controllers;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import controllers.paneControllers.*;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import model.*;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.json.simple.parser.ParseException;
import scenes.paneScenes.ClassComboDraftController;
import scenes.paneScenes.DescPaneController;
import service.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.data_exchange_toolbox.ScrollTimer;
import transversal.dialog_toolbox.*;
import transversal.generic.TextUtils;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Char_description {

	public boolean CHANGING_CLASS = false;
    public TextField urlLink;
	public SimpleBooleanProperty visibleRight = new SimpleBooleanProperty();
    public Menu projectMenu;
	public Menu homeMenu;
	public Menu dataMenu;
	public Menu automationMenu;
	public Menu navigationMenu;
	public Menu settingMenu;
	public ToolBar homeToolbar;
	public ToolBar dataToolbar;
	public ToolBar automationToolbar;
	public ToolBar navigationToolbar;
	public ToolBar settingToolbar;
	public TextArea helperAreaLeft;
	public TextArea helperAreaRight;


	@FXML MenuBar menubar;
	@FXML MenuBar secondaryMenuBar;
	@FXML public Menu counterTotal;
	@FXML public Menu counterRemaining;
	@FXML public Menu counterDaily;
	@FXML public Menu counterSelection;
	@FXML public ComboBox<ClassSegment> classCombo;
	private ClassSegment latestClassComboValue;
	private ListView classComboAutoCompleteView;
	private ListView classComboListView;
	private boolean droppingDown = false;

	@FXML GridPane grid;

	@FXML public AnchorPane leftAnchor;
	@FXML public AnchorPane rightAnchor;
	@FXML public Label aidLabel;
	@FXML public StyleClassedTextArea sd;
	@FXML public StyleClassedTextArea sd_translated;
	@FXML public StyleClassedTextArea ld;
	@FXML public StyleClassedTextArea ld_translated;
	public AutoCompleteBox_CharClassification classification;
	@FXML public TextField classification_style;
	@FXML public TextField search_text;

	@FXML ToolBar toolBar;
	@FXML Button paneToggle;
	@FXML Button classDDButton;
	@FXML public Button exportButton;
	@FXML public Button clearLinkButton;
	@FXML ToggleButton googleButton;
	@FXML ToggleButton tableButton;
	@FXML public ToggleButton ruleButton;
	@FXML public ToggleButton imageButton;
	@FXML public ToggleButton charButton;
	@FXML public ToggleButton conversionToggle;

	@FXML Button searchSettingButton;
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

	@FXML Button deleteValueLabel;
	@FXML Label custom_label_11;
	@FXML Label custom_label_12;
	@FXML Label custom_label_21;
	@FXML Label custom_label_22;
	@FXML Label rule_label;
	@FXML Label value_label;
	@FXML Label note_label;
	@FXML Label custom_label_value;


	public UserAccount account;
	public HashMap<String,ArrayList<String>> DescriptionSortColumns = new HashMap<String,ArrayList<String>>();
	public HashMap<String,ArrayList<String>> DescriptionSortDirs = new HashMap<String,ArrayList<String>>();


	public String user_language_gcode;
	public String data_language_gcode;
	public String user_language;
	public String data_language;


	public TablePane_CharClassif tableController;


	public Browser_CharClassif browserController;

	private GridPane rightAnchorImageGrid;
	private GridPane rightAnchorContentGrid;

	public ArrayList<Button> propButtons;



	public String lastRightPane="";


	private ImagePane_CharClassif imagePaneController;


	public ArrayList<String> CNAME_CID;
	public CharPane_CharClassif charPaneController;
	public RulePane_CharClassif rulePaneController;
	public ClassPane_CharClassif classPaneController;

	public CharClassifProposer proposer;



	private TextField[] uiDataFields;



	public AutoCompleteBox_CharValue translationAutoComplete;



	public AutoCompleteBox_CharValue valueAutoComplete;
	public boolean draftingRule=false;
	public CaracteristicValue lastInputValue;
	private DescPaneController descSettingController;
	private ScrollTimer scrollTimer = new ScrollTimer();


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
			Optional<ArrayList<Boolean>> choice = DescriptionExportExcelSheets.choicePopUp(aidLabel);
			if(choice.isPresent()){
				CharDescriptionExportServices.ExportItemDataForClass(null,this,choice.get().get(0),choice.get().get(1),choice.get().get(2),choice.get().get(3),choice.get().get(4));
			}

		} catch (IOException e) {
			e.printStackTrace(System.err);
    		ConfirmationDialog.show("File saving failed", "Results could not be saved.\nMake sure you have the rights to create files in this folder and that the file is not open by another application", "OK");
		}

	}
	@FXML void copyClientNumber2ClipBoard(){
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		try{
			content.putString(aidLabel.getText().split("Article ID: ")[1]);
			Clipboard.getSystemClipboard().setContent(content);
		}catch (Exception V){

		}
	}
	@FXML void initialize(){
		aidLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				copyClientNumber2ClipBoard();
			}
		});
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				grid.lookupAll("ToolBar").forEach(tb -> {
					if (tb instanceof ToolBar) {
						((ToolBar) tb).getItems().forEach(node -> {
							if (node instanceof HBox) {
								if (node.getStyleClass().contains("gapbox")) {
									//((HBox) node).setPrefWidth(projectMenu.getGraphic().getBoundsInParent().getMinX() - node.getBoundsInParent().getMinX());
									((HBox) node).setPrefWidth(10);
								}
							}
						});
					}
				});
			}
		});
		grid.lookupAll("Button.settingButton").forEach(btn->{
			if(btn instanceof Button){
				((Button) btn).setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/DescSettingPane.fxml"));
							Scene scene = new Scene(loader.load());
							Stage secondaryStage = new Stage();
							secondaryStage.initModality(Modality.WINDOW_MODAL);
							secondaryStage.initOwner(aidLabel.getScene().getWindow());
							secondaryStage.initStyle(StageStyle.TRANSPARENT);
							//secondaryStage.setMaximized(true);
							secondaryStage.setWidth(Math.floor(Screen.getPrimary().getBounds().getWidth()*0.7));
							secondaryStage.setScene(scene);
							secondaryStage.show();
							descSettingController = loader.getController();
							descSettingController.setParent(tableController.Parent,GridPane.getRowIndex(btn),GridPane.getColumnIndex(btn));
							descSettingController.setUserAccount(tableController.Parent.account);
							descSettingController.fillItems();
							descSettingController.setStageWidthProperty(secondaryStage);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		grid.lookupAll("Button.scrollDownButton").forEach(btn->{
			if(btn instanceof Button){
				((Button) btn).addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
							scrollTimer.prepareScrollDown(grid,btn);
						}
						if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
							scrollTimer.start();
						} else {
							scrollTimer.stop();
						}

					}
				});
			}
		});
		grid.lookupAll("Button.scrollUpButton").forEach(btn->{
			if(btn instanceof Button){
				((Button) btn).addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
							scrollTimer.prepareScrollUp(grid,btn);
						}
						if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
							scrollTimer.start();
						} else {
							scrollTimer.stop();
						}

					}
				});
			}
		});

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initRibbon();
			}
		});
		toolBarButtonListener();
		initializePropButtons();


	}

	private void initRibbon() {
		grid.lookupAll("ToolBar").forEach(tb->{
			tb.setVisible(false);
		});
		homeToolbar.setVisible(true);
		grid.lookupAll("#menuHeader").forEach(lt->{
			lt.getStyleClass().remove("activeMenu");
		});
		homeMenu.getStyleClass().add("activeMenu");

		Label homeLabel = new Label("Home");
		homeMenu.setGraphic(homeLabel);
		homeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				grid.lookupAll("ToolBar").forEach(tb->{
					tb.setVisible(false);
				});
				homeToolbar.setVisible(true);
				grid.lookupAll("#menuHeader").forEach(lt->{
					lt.getStyleClass().remove("activeMenu");
				});
				homeMenu.getStyleClass().add("activeMenu");
			}
		});
		Label dataLabel = new Label("Data Enhancement");
		dataMenu.setGraphic(dataLabel);
		dataLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				grid.lookupAll("ToolBar").forEach(tb->{
					tb.setVisible(false);
				});
				dataToolbar.setVisible(true);
				grid.lookupAll("#menuHeader").forEach(lt->{
					lt.getStyleClass().remove("activeMenu");
				});
				dataMenu.getStyleClass().add("activeMenu");
			}
		});
		Label automationLabel = new Label("Automation");
		automationMenu.setGraphic(automationLabel);
		automationLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				grid.lookupAll("ToolBar").forEach(tb->{
					tb.setVisible(false);
				});
				automationToolbar.setVisible(true);
				grid.lookupAll("#menuHeader").forEach(lt->{
					lt.getStyleClass().remove("activeMenu");
				});
				automationMenu.getStyleClass().add("activeMenu");
			}
		});
		Label navigationLabel = new Label("Display & Navigation");
		navigationMenu.setGraphic(navigationLabel);
		navigationLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				grid.lookupAll("ToolBar").forEach(tb->{
					tb.setVisible(false);
				});
				navigationToolbar.setVisible(true);
				grid.lookupAll("#menuHeader").forEach(lt->{
					lt.getStyleClass().remove("activeMenu");
				});
				navigationMenu.getStyleClass().add("activeMenu");
			}
		});
		Label settingLabel = new Label("Settings");
		settingMenu.setGraphic(settingLabel);
		settingLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				grid.lookupAll("ToolBar").forEach(tb->{
					tb.setVisible(false);
				});
				settingToolbar.setVisible(true);
				grid.lookupAll("#menuHeader").forEach(lt->{
					lt.getStyleClass().remove("activeMenu");
				});
				settingMenu.getStyleClass().add("activeMenu");
			}
		});
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
				tableController.charDescriptionTable.refresh();
			}
			
		});
		visibleRight.bind(charButton.selectedProperty().or(ruleButton.selectedProperty()).or(imageButton.selectedProperty()));

	}

	@FXML public void editSearchSettings() {
		ExternalSearchServices.editSearchPrefrence(this);
	}

	private void listen_for_keyboard_events() {
		Char_description parent = this;
		deleteValueLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				CaracteristicValue val = new CaracteristicValue();
				int active_char_index = Math.floorMod(tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
				ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId())
						.get(active_char_index);
				val.setParentChar(active_char);
				assignValueOnSelectedItems(val);
				ExternalSearchServices.refreshUrlAfterElemChange(parent);
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});
		
		uiDataFields = new TextField[] {value_field,uom_field,min_field_uom,max_field_uom,note_field_uom,rule_field,min_field,max_field,note_field,translated_value_field};
		
		aidLabel.getScene().setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent) 
            {
                try {
					handleKeyBoardEvent(keyEvent,true);
				} catch (IOException | ParseException | ClassNotFoundException | SQLException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });

		aidLabel.getScene().setOnKeyReleased(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent) 
            {
                try {
					handleKeyBoardEvent(keyEvent,false);
				} catch (IOException | ParseException | ClassNotFoundException | SQLException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });

		aidLabel.getScene().getWindow().focusedProperty().addListener(new ChangeListener<Boolean>()
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
					try {
						handleDataKeyBoardEvent(keyEvent,true);
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
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

	}

	protected void handleDataKeyBoardEvent(KeyEvent keyEvent, boolean pressed) throws SQLException {
		
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
		if(keyEvent.getCode().equals(KeyCode.R)) {
			account.PRESSED_KEYBOARD.put(KeyCode.R, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.T)) {
			account.PRESSED_KEYBOARD.put(KeyCode.T, pressed);
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
		if(keyEvent.getCode().equals(KeyCode.PAGE_DOWN)) {
			account.PRESSED_KEYBOARD.put(KeyCode.PAGE_DOWN, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.PAGE_UP)) {
			account.PRESSED_KEYBOARD.put(KeyCode.PAGE_UP, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.K)) {
			account.PRESSED_KEYBOARD.put(KeyCode.K, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.M)) {
			account.PRESSED_KEYBOARD.put(KeyCode.M, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.L)) {
			account.PRESSED_KEYBOARD.put(KeyCode.L, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.O)) {
			account.PRESSED_KEYBOARD.put(KeyCode.O, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.U)) {
			account.PRESSED_KEYBOARD.put(KeyCode.U, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.W)) {
			account.PRESSED_KEYBOARD.put(KeyCode.W, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.TAB)) {
			account.PRESSED_KEYBOARD.put(KeyCode.TAB, pressed);
		}

		if(keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.D)){
			int active_char_index = Math.floorMod(tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
			String activeClass = FxUtilTest.getComboBoxValue(classCombo).getSegmentId();
			ClassCaracteristic activeChar = CharValuesLoader.active_characteristics.get(activeClass).get(active_char_index);
			CharDescriptionRow firstSelectedRow = tableController.charDescriptionTable.getItems().get(Collections.min(tableController.charDescriptionTable.getSelectionModel().getSelectedIndices()));
			CaracteristicValue activeData = firstSelectedRow.getData(activeClass).get(activeChar.getCharacteristic_id());
			if(activeData!=null){
				lastInputValue=activeData;
				reassignPreviousValue();
			}
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
				if(this.account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)){
					tableController.charDescriptionTable.getSelectionModel().select(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()+1);
				}else{
					tableController.charDescriptionTable.getSelectionModel().clearAndSelect(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()+1);
				}
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						value_field.end();
						value_field.selectAll();
					}
				});
			}
			if((!this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) && account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
				if(this.account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)){
					tableController.charDescriptionTable.getSelectionModel().select(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()-1);
				}else{
					tableController.charDescriptionTable.getSelectionModel().clearAndSelect(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()-1);
				}
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						value_field.end();
						value_field.selectAll();
					}
				});
				
			}
			if (this.account.PRESSED_KEYBOARD.get(KeyCode.PAGE_DOWN)) {
				tableController.charDescriptionTable.requestFocus();
				duplicateKeyEvent(KeyCode.PAGE_DOWN);
				//parent_controller.fireClassScroll(rowIndex+1,KeyCode.DOWN);
			}
			else if (this.account.PRESSED_KEYBOARD.get(KeyCode.PAGE_UP)) {
				tableController.charDescriptionTable.requestFocus();
				duplicateKeyEvent(KeyCode.PAGE_UP);
				//parent_controller.fireClassScroll(rowIndex-1,KeyCode.UP);
			}
						
		}
		
	}

	public void duplicateKeyEvent(KeyCode key) {
		if(!GlobalConstants.AUTO_TEXT_FIELD_DUPLICATE_ACTION) {
			return;
		}
		try {

			Robot keyboardRobot = new Robot();
			if(key.equals(KeyCode.PAGE_UP)){
				keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_PAGE_UP);
			}
			if(key.equals(KeyCode.PAGE_DOWN)){
				keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_PAGE_DOWN);
			}

		} catch (AWTException e) {

		}
	}

	public void validateFieldsThenSkipToNext(Boolean TranslationProcessResult) throws SQLException {
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
				int idx = tableController.charDescriptionTable.getSelectionModel().getSelectedIndex();
				if(TranslationProcessResult!=null) {
					if(FxUtilTest.getComboBoxValue(classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
						CharValuesLoader.updateDefaultCharValue(idx,this);
					}else {
						CharValuesLoader.storeItemDatafromScreen(idx,this);
					}
				}
				if(!charButton.isSelected()){
					tableController.jumpNext();
					//tableController.tableGrid.getSelectionModel().clearAndSelect(idx+1);
				}
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
		}
	}
	private Boolean CheckForTranslationValidity() {
		ClassCaracteristic active_char;
		if(FxUtilTest.getComboBoxValue(classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			active_char = CharItemFetcher.defaultCharValues.get(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()).getKey();
			 
		}else {
			int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
			active_char = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId())
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
	protected void handleKeyBoardEvent(KeyEvent keyEvent, boolean pressed) throws IOException, ParseException, ClassNotFoundException, SQLException, URISyntaxException {
		
		
		
		if(keyEvent.getCode().equals(KeyCode.DELETE)){
			account.PRESSED_KEYBOARD.put(KeyCode.DELETE,pressed);
		}
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
		if(keyEvent.getCode().equals(KeyCode.R)) {
			account.PRESSED_KEYBOARD.put(KeyCode.R, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.T)) {
			account.PRESSED_KEYBOARD.put(KeyCode.T, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.P)) {
			account.PRESSED_KEYBOARD.put(KeyCode.P, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.PAGE_DOWN)) {
			account.PRESSED_KEYBOARD.put(KeyCode.PAGE_DOWN, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.PAGE_UP)) {
			account.PRESSED_KEYBOARD.put(KeyCode.PAGE_UP, pressed);
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
		if(keyEvent.getCode().equals(KeyCode.L)) {
			account.PRESSED_KEYBOARD.put(KeyCode.L, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.O)) {
			account.PRESSED_KEYBOARD.put(KeyCode.O, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.U)) {
			account.PRESSED_KEYBOARD.put(KeyCode.U, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.W)) {
			account.PRESSED_KEYBOARD.put(KeyCode.W, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.TAB)) {
			account.PRESSED_KEYBOARD.put(KeyCode.TAB, pressed);
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.SHIFT) && account.PRESSED_KEYBOARD.get(KeyCode.ENTER)) {
			this.lastRightPane="";
			if(this.charButton.isSelected()) {
				this.lastRightPane="CHARS";
			}
			if(this.imageButton.isSelected()) {
				this.lastRightPane="IMAGES";
			}
			if(this.ruleButton.isSelected()) {
				this.lastRightPane="RULES";
			}

			if(!GlobalConstants.TURN_OFF_IMAGE_SEARCH_FOR_DESCRIPTION){
				imageButton.setSelected(true);
			}
			launch_search(true);
		}
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.ENTER)) {
			int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]).size());
			try{
				proposer.clearPropButtons();
				String selectedText=proposer.getUserSelectedText();
				CharPatternServices.scanSelectionForPatternDetection(this,
						CharValuesLoader.active_characteristics.get(tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0])
								.get(active_char_index),selectedText);
			}catch (Exception V){
				V.printStackTrace(System.err);
			}
		}
		
		
		
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.ESCAPE)) {
			
			ExternalSearchServices.clearingURL(false);
			try {
				imagePaneController.imagePaneClose();
			}catch(Exception V) {
				
			}
			try {
				charPaneController.PaneClose();
			}catch(Exception V) {
				
			}
			try {
				rulePaneController.PaneClose();
			}catch(Exception V) {

			}

			boolean closingBrowser=false;
			try{
				closingBrowser=browserController.secondaryStage.isShowing();

			}catch (Exception V){

			}
			try{
				closingBrowser=closingBrowser || browserController.showingPdf.get();

			}catch (Exception V){

			}
			try{
				closingBrowser=closingBrowser || browserController.toolBar.isVisible();

			}catch (Exception V){

			}
			//We clicked escape to close search
			try {
				show_table();
			}catch(Exception V) {

			}


			try{
				browserController.secondaryStage.close();
			}catch (Exception V){

			}
			ruleButton.setSelected(false);
			charButton.setSelected(false);
			imageButton.setSelected(false);
			if(closingBrowser){
				if(this.lastRightPane.equals("CHARS")) {
					charButton.setSelected(true);
					view_chars();
				}
				if(this.lastRightPane.equals("IMAGES")) {
					imageButton.setSelected(true);
					search_image();
				}
				if(this.lastRightPane.equals("RULES")) {
					ruleButton.setSelected(true);
					view_rules();
				}
			}else{
				this.lastRightPane="";
			}

			
			
			
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.I)) {
			if(this.charButton.isSelected()) {
				//this.charPaneController.PaneClose();
			}else {
				imageButton.setSelected(false);
				charButton.setSelected(true);
				ruleButton.setSelected(false);
				load_char_pane();
			}
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.T)){
			load_class_pane();
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.R)) {
			String selectedText = proposer.getUserSelectedText();
			if(selectedText.length()>0){
				draftingRule=true;
				int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]).size());
				CharPatternServices.scanSelectionForPatternDetection(this,
						CharValuesLoader.active_characteristics.get(tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0])
								.get(active_char_index),selectedText);
				draftingRule=false;
			}
			if(this.ruleButton.isSelected()) {
				//this.rulePaneController.PaneClose();
			}else {
				imageButton.setSelected(false);
				charButton.setSelected(false);
				ruleButton.setSelected(true);
			}
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						 load_rule_pane();
					} catch (IOException | SQLException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.P)) {
			if(this.imageButton.isSelected()) {
				//this.imagePaneController.imagePaneClose();
			}else {
				imageButton.setSelected(true);
				charButton.setSelected(false);
				ruleButton.setSelected(false);
				load_image_pane(true);
			}
		}

		if(account.PRESSED_KEYBOARD.get(KeyCode.O) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			if(GlobalConstants.OPEN_LINKS_IN_EXTERNAL || account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)){
				Desktop.getDesktop().browse(new URL(urlLink.getText()).toURI());
			}else{
				try {
					browserController.browser.FORCE_PDF_IN_VIEWER = true;
					browserController.setContainerWindow();
					browserController.browser.loadPage(urlLink.getText());
				} catch (Exception V) {
					try {
						load_browser_pane();
						browserController.browser.FORCE_PDF_IN_VIEWER = true;
						browserController.setContainerWindow();
						browserController.browser.loadPage(urlLink.getText());
					} catch (Exception L) {
						L.printStackTrace(System.err);
					}
				}
				browserController.browser.FORCE_PDF_IN_VIEWER = GlobalConstants.FORCE_PDF_IN_VIEWER;
			}
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.K) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			tableController.previousChar();
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.M) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			tableController.nextChar();
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.L) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			linkUrlToItem(urlLink.getText());
		}
		if(account.PRESSED_KEYBOARD.get(KeyCode.W) && account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
			reassignPreviousValue();
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
			tableController.fireScrollNBDown(keyEvent.isShiftDown());
		}

		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
			tableController.fireScrollNBUp(keyEvent.isShiftDown());
		}

		if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.U)){
			ArrayList<Boolean> unknownCardinality = tableController.charDescriptionTable.getSelectionModel().getSelectedItems().stream().map(CharDescriptionRow::hasClearValue).collect(Collectors.toCollection(HashSet::new)).stream().collect(Collectors.toCollection(ArrayList::new));
			if(unknownCardinality.size()==1){
				tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(r->r.switchUnknownValues(account,null));
			}else{
				tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(r->r.markUnknownClearValues(account,null));
			}
			if(!charButton.isSelected()){
				tableController.jumpNext();
				//int idx = tableController.tableGrid.getSelectionModel().getSelectedIndex();
				//tableController.tableGrid.getSelectionModel().clearAndSelect(idx+1);
			}
			refresh_ui_display();
			tableController.charDescriptionTable.refresh();
			CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
		}

		return;
		
	}

	private void reassignPreviousValue() {
		if(lastInputValue!=null){
			CaracteristicValue val = lastInputValue.shallowCopy(account);
			val.setSource(DataInputMethods.MANUAL);
			val.setRule_id(null);
			assignValueOnSelectedItems(val);
			ExternalSearchServices.manualValueInput();
			if(!charButton.isSelected()){
				tableController.jumpNext();
				//int idx = tableController.tableGrid.getSelectionModel().getSelectedIndex();
				//tableController.tableGrid.getSelectionModel().clearAndSelect(idx+1);
			}
		}
	}

	public void linkUrlToItem(String URL) {
		if(!FxUtilTest.getComboBoxValue(classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			HashMap<String, CaracteristicValue> map = new HashMap<String, CaracteristicValue>();
			tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(row->{
				String itemClass = row.getClass_segment_string().split("&&&")[0];
				int	selected_col = Math.floorMod(tableController.selected_col, CharValuesLoader.active_characteristics.get(itemClass).size());
				ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).get(selected_col);
				CaracteristicValue val;
				try{
					val = row.getData(itemClass).get(active_char.getCharacteristic_id()).shallowCopy(account);
					if( (URL!=null && URL.length()>0) || (val.getRawDisplay()!=null && val.getRawDisplay().length()>0) ){
						val.setSource(DataInputMethods.MANUAL);
					}
					val.setRule_id(null);
					val.setUrl(URL);
					map.put(row.getItem_id(),val);
				}catch (Exception V){
					if(URL!=null && URL.length()>0){
						val = new CaracteristicValue();
						val.setAuthor(account.getUser_id());
						val.setSource(DataInputMethods.MANUAL);
						val.setRule_id(null);
						val.setUrl(URL);
						map.put(row.getItem_id(),val);
					}

				}

			});
			assignValueOnSelectedItems(map);
			ExternalSearchServices.refreshUrlAfterElemChange(this);
		}
	}


	private Node validateDataFields() throws SQLException {
		CharDescriptionRow row = tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
		String selectedRowClass = row.getClass_segment_string().split("&&&")[0];
		ClassCaracteristic active_char;
		int active_char_index;
		if(FxUtilTest.getComboBoxValue(classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			active_char_index = 0;
			active_char = CharItemFetcher.defaultCharValues.get(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()).getKey();
			 
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
		return null;
	}
	private boolean validateUomField(CharDescriptionRow row, int active_char_index) {
		String uomFieldText = uom_field.getText();
		if(uomFieldText!=null && uomFieldText.length()>0) {
			String row_class_id = row.getClass_segment_string().split("&&&")[0];
			Optional<UnitOfMeasure> matchinguom = UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->StringUtils.equalsIgnoreCase(u.getUom_symbol(),uomFieldText)||u.toString().equals(uomFieldText)).findAny();
			if(matchinguom.isPresent()) {
				
				if(UnitOfMeasure.ConversionPathExists(matchinguom.get(), CharValuesLoader.active_characteristics.get(row_class_id).get(active_char_index).getAllowedUoms())) {
					uom_field.setUom(matchinguom.get());
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
	private boolean validateValueField(ClassCaracteristic active_char,TextField target_field) throws SQLException {
		String originalValue = target_field.getText();
		if(originalValue == null || originalValue.length()==0) {
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
				//New code for manual value arithmetic
				if(textBetweenNumbers.size()>0){
					finishingText = textBetweenNumbers.get(textBetweenNumbers.size()-1);
					if(originalValue.trim().endsWith(finishingText)) {
						originalValue = originalValue.substring(0,originalValue.trim().length()-finishingText.length());
						ArrayList<UnitOfMeasure> uomsInSelection = WordUtils.parseCompatibleUoMs("%9"+finishingText.trim(),active_char);
						finishingUom = uomsInSelection.get(0);
						if(finishingUom!=null) {
							uom_field.setText(finishingUom.toString());
						}else{
							UoMDeclarationDialog.UomDeclarationPopUpAfterFailedFieldValidation(finishingText, uom_field, active_char);
						}
					}
				}
				if(numValuesInSelection.size()>1){
					if(numValuesInSelection.size()==2 && finishingText.trim().equals("")){
						target_field.setText(null);
						min_field_uom.setText(WordUtils.DoubleToString(Collections.min(numValuesInSelection)));
						max_field_uom.setText(WordUtils.DoubleToString(Collections.max(numValuesInSelection)));
						return false;
					}
					try{
						target_field.setText(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(originalValue.trim().toLowerCase().replace("x","*"))));
						return false;
					}catch (Exception V){
						target_field.setText(null);
						return true;
					}
				}else{
					if(numValuesInSelection.size()==0) {
						target_field.setText(null);
						return true;
					}
					if(numValuesInSelection.size()==1) {
						target_field.setText(WordUtils.DoubleToString(numValuesInSelection.get(0)));
						return false;
					}
				}
				target_field.setText(null);
				return true;
				/* old code before manual value arithmetic
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
				*/
			}else {
				//Numeric w/o UOM
				//New code : Implement arithmetics in manual value editing
				if(numValuesInSelection.size()>1){
					finishingText = textBetweenNumbers.get(textBetweenNumbers.size()-1);
					if(originalValue.trim().endsWith(finishingText)) {
						target_field.setText(null);
						return true;
					}
					if(numValuesInSelection.size()==2 && finishingText.trim().equals("")){
						target_field.setText(null);
						min_field.setText(WordUtils.DoubleToString(Collections.min(numValuesInSelection)));
						max_field.setText(WordUtils.DoubleToString(Collections.max(numValuesInSelection)));
						return false;
					}
					try{
						target_field.setText(String.valueOf(WordUtils.EVALUATE_ARITHMETIC(originalValue.trim().toLowerCase().replace("x","*"))));
						return false;
					}catch (Exception V){
						target_field.setText(null);
						return true;
					}
				}else{
					if(numValuesInSelection.size()==0) {
						target_field.setText(null);
						return true;
					}
					if(numValuesInSelection.size()==1) {
						target_field.setText(WordUtils.DoubleToString(numValuesInSelection.get(0)));
						return false;
					}
				}
				/* old code : before implementing manual value arithmetics
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
				}*/
				target_field.setText(null);
				return true;
			}
		}
		return false;
	}
	
	private void launch_search(boolean checkMethodSelect) throws IOException, ParseException {
		if(!GlobalConstants.TURN_OFF_IMAGE_SEARCH_FOR_DESCRIPTION){
			load_image_pane(checkMethodSelect);
		}
		search_google_inplace(checkMethodSelect);
	}

	@FXML public void view_rules() throws IOException, ParseException {
		if(!this.ruleButton.isSelected()) {
			this.rulePaneController.PaneClose();
		}else {
			imageButton.setSelected(false);
			charButton.setSelected(false);
			ruleButton.setSelected(true);
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					load_rule_pane();
				} catch (IOException | SQLException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@FXML public void search_image() throws IOException, ParseException {
		if(!this.imageButton.isSelected()) {
			this.imagePaneController.imagePaneClose();
		}else {
			imageButton.setSelected(true);
			charButton.setSelected(false);
			ruleButton.setSelected(false);
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					load_image_pane(false);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@FXML public void view_chars() throws IOException, ParseException, ClassNotFoundException, SQLException {
		if(!this.charButton.isSelected()) {
			this.charPaneController.PaneClose();
		}else {
			imageButton.setSelected(false);
			charButton.setSelected(true);
			ruleButton.setSelected(false);
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					load_char_pane();
				} catch (IOException | SQLException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@FXML public void show_table() throws IOException, ParseException {
		
		tableButton.setSelected(true);
		
		try {
			browserController.switch_pane_hide_browser(true);
		}catch(Exception V) {

		}
		
		try {
			tableController.charDescriptionTable.setVisible(true);
		}catch(Exception V) {
			
		}
		tableController.charDescriptionTable.requestFocus();
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
		decorate_menubar_with_desc_specific_menus();
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
		grid.add(classification, 1, 10);
		grid.add(uom_field, 5, 10);
		
		
		load_table_pane();
		tableController.redimensionGrid();
		listen_for_keyboard_events();
		//load_image_pane();
		//load_rule_pane();
		//load_taxo_pane();
		EventHandler closeHandler = new EventHandler<MouseEvent>() {
			/**
			 * Invoked when a specific event of the type for which this handler is
			 * registered happens.
			 *
			 * @param event the event which occurred
			 */
			@Override
			public void handle(MouseEvent event) {
				try {
					browserController.secondaryStage.close();
				} catch (Exception V) {

				}
				ConfirmationDialog.show("Saving latest modifications", "Click (OK) to persist local changes to remote server. This should only take a few seconds", "OK");
				Runnable onFailed = new Runnable() {
					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								ConfirmationDialog.show("Could not reach server", "Click (OK) to export project data to spreadsheet", "OK");
							}
						});
						try {
							CharDescriptionExportServices.ExportItemDataForClass(null, Char_description.this, true, true, true, true, true);
						} catch (ClassNotFoundException | SQLException | IOException classNotFoundException) {
							classNotFoundException.printStackTrace();
						}
					}
				};
				//CharDescriptionExportServices.flushItemDataToDBThreaded(account,onFailed);
				try {
					CharDescriptionExportServices.flushItemDataToDBNoThread(account);
				} catch (Exception V) {
					onFailed.run();
				}
				Platform.exit();
			}
		};
		ToolHeaderController.setCloseHandler(closeHandler);
		ToolHeaderController.closeHandlerSet.set(true);
		
		urlLink.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(urlLink.isFocused()){
					ExternalSearchServices.editingURL(newValue);
				}
			}
		});

		grid.lookupAll("StyleClassedTextArea").forEach(pa->{
			if(pa instanceof StyleClassedTextArea){
				//Disable multi selection
				((StyleClassedTextArea) pa).selectedTextProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if(newValue.length()>0){
							grid.lookupAll("StyleClassedTextArea").stream().filter(loopPA->loopPA!=pa).forEach(loopPA->{
								if(loopPA instanceof StyleClassedTextArea){
									((StyleClassedTextArea) loopPA).deselect();
								}
							});
							deselectBrowsers();
						}
					}
				});
				//Add scroll listeners
				((StyleClassedTextArea) pa).estimatedScrollYProperty().addListener(new ChangeListener<Double>() {
					@Override
					public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
						refreshScrollButton((StyleClassedTextArea)pa);
					}
				});
			}
		});

	}

	public void refreshScrollButton(StyleClassedTextArea pa) {
		try{
			Integer paCol = GridPane.getColumnIndex(pa);
			Integer paColSpan = GridPane.getColumnSpan(pa);
			Integer paRow = GridPane.getRowIndex(pa);
			Integer paRowSpan = GridPane.getRowSpan(pa);

			Integer finalPaColSpan = paColSpan!=null?new Integer(paColSpan -1):new Integer(0);
			Integer finalPaRowSpan = paRowSpan!=null?new Integer(paRowSpan -1):new Integer(0);

			Button su = (Button) grid.lookupAll("Button.scrollUpButton").stream()
					.filter(btn->( GridPane.getColumnIndex(btn).equals(paCol) && GridPane.getRowIndex(btn).equals(paRow) )
							|| (GridPane.getColumnIndex(btn) == paCol + finalPaColSpan && GridPane.getRowIndex(btn) == paRow + finalPaRowSpan)
							).findAny().get();
			Button sd = (Button) grid.lookupAll("Button.scrollDownButton").stream()
					.filter(btn->( GridPane.getColumnIndex(btn).equals(paCol) && GridPane.getRowIndex(btn).equals(paRow) )
							|| (GridPane.getColumnIndex(btn) == paCol + finalPaColSpan && GridPane.getRowIndex(btn) == paRow + finalPaRowSpan)
					).findAny().get();

			su.setVisible(true);
			sd.setVisible(true);
			if (Math.floor(pa.getEstimatedScrollY()+pa.heightProperty().get()
					-pa.totalHeightEstimateProperty().getValue()
					-pa.getPadding().getTop()-pa.getPadding().getBottom())==0) {
				sd.setVisible(false);
			}
			if (pa.getEstimatedScrollY() == 0.0) {
				su.setVisible(false);
			}
		}catch (Exception V){
			//V.printStackTrace(System.err);
		}
	}
	public void refreshScrollButtons() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				grid.lookupAll("StyleClassedTextArea").forEach(pa-> {
					refreshScrollButton((StyleClassedTextArea) pa);
				});
			}
		});
	}

	private void deselectBrowsers() {
		try{
			browserController.browser.nodeValue.getEngine().executeScript("window.getSelection().empty()");
		}catch (Exception V){
		}
		try{
			browserController.iceController.getDocumentViewController().clearSelectedText();
		}catch (Exception V){
		}
	}

	private void decorate_menubar_with_desc_specific_menus() {
		Menu desc = menubar.getMenus().get(4);
		menubar.setOnMouseEntered(e->{
			desc.show();
		});
		Char_description parent = this;
		MenuItem reeval_rules_true = new MenuItem("Reevaluate item values");
		reeval_rules_true.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				CharItemFetcher.allRowItems.parallelStream().forEach(CharDescriptionRow::reEvaluateCharRules);
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});

		MenuItem clear_unknowns = new MenuItem("Clear UNKNOWN values (selected items)                         Ctrl+U");
		clear_unknowns.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(charDescriptionRow -> charDescriptionRow.clearUnknownValues(null));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});
		MenuItem mark_as_known = new MenuItem("Mark blank values as UNKNOWN (selected items)         Ctrl+U");
		mark_as_known.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(r->r.markUnknownClearValues(account, null));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});

		MenuItem clear_unknowns_active_char = new MenuItem("Clear UNKNOWN values (active characteristic)");
		clear_unknowns_active_char.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				String activeClass = FxUtilTest.getComboBoxValue(classCombo).getSegmentId();
				int selected_col = Math.floorMod(tableController.selected_col, CharValuesLoader.active_characteristics.get(activeClass).size());
				ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(activeClass).get(selected_col);
				CharItemFetcher.allRowItems.forEach(r->r.clearUnknownValues(active_char.getCharacteristic_id()));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});
		MenuItem mark_as_known_active_char = new MenuItem("Mark blank values as UNKNOWN (active characteristic)");
		mark_as_known_active_char.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				String activeClass = FxUtilTest.getComboBoxValue(classCombo).getSegmentId();
				int selected_col = Math.floorMod(tableController.selected_col, CharValuesLoader.active_characteristics.get(activeClass).size());
				ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(activeClass).get(selected_col);
				CharItemFetcher.allRowItems.forEach(r->r.markUnknownClearValues(account,active_char.getCharacteristic_id()));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});

		MenuItem clear_unknowns_class = new MenuItem("Clear UNKNOWN values (active class)");
		clear_unknowns_class.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				tableController.charDescriptionTable.getItems().forEach(charDescriptionRow -> charDescriptionRow.clearUnknownValues(null));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});
		MenuItem mark_as_known_class = new MenuItem("Mark blank values as UNKNOWN (active class)");
		mark_as_known_class.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				if(!ConfirmationDialog.WarningClearingUnknownValues()){
					return;
				}
				tableController.charDescriptionTable.getItems().forEach(r->r.markUnknownClearValues(account, null));
				refresh_ui_display();
				tableController.charDescriptionTable.refresh();
				CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
			}
		});
		MenuItem launchDedup = new MenuItem("Launch Deduplication settings");
		launchDedup.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				try {
					DedupLaunchDialog.Settings(Tools.get_project_segments(parent.account).get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()),tableController.Parent);
				} catch (SQLException | ClassNotFoundException throwables) {
					throwables.printStackTrace();
				}
			}
		});

		desc.getItems().addAll(reeval_rules_true,clear_unknowns,mark_as_known,clear_unknowns_active_char,mark_as_known_active_char,clear_unknowns_class,mark_as_known_class,launchDedup);
	}


	private void decorate_class_combobox() throws SQLException, ClassNotFoundException {

		//CONTENT
		classCombo.getItems().clear();

		if(account.getUser_desc_classes()!=null) {
			for(String entry:account.getUser_desc_classes()) {
				for(String elem:CNAME_CID) {
					if (elem.startsWith(entry)) {
						ClassSegment tmp = new ClassSegment();
						tmp.setSegmentGranularity(Tools.get_project_granularity("ua2ff22e2317c48e8993e295a471b73b9"));
						tmp.setSegmentId(elem.split("&&&")[0]);
						tmp.setLevelName(tmp.getSegmentGranularity()-1,elem.split("&&&")[1]);
						tmp.setLevelNumber(tmp.getSegmentGranularity()-1,elem.split("&&&")[2]);
						classCombo.getItems().add(tmp);
						if(elem.split("&&&")[0].equals(account.getUser_desc_class())) {
							classCombo.getSelectionModel().select(tmp);
							classCombo.setValue(tmp);
						}
					}
				}
				
				
			}
		}

		classCombo.getItems().sort(new Comparator<ClassSegment>() {
			@Override
			public int compare(ClassSegment o1, ClassSegment o2) {
				return o1.getClassNumber().compareTo(o2.getClassNumber());
			}
		});
		//Behavior
		classCombo.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue){
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							classCombo.getEditor().selectAll();
						}
					});
				}
			}
		});

		classCombo.setEditable(true);
		Callback<AutoCompletionBinding.ISuggestionRequest, Collection<ClassSegment>> suggestionProvider = new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<ClassSegment>>() {
			@Override
			public Collection<ClassSegment> call(AutoCompletionBinding.ISuggestionRequest param) {
				String input = param.getUserText();
				Unidecode unidec = Unidecode.toAscii();
				ArrayList<ClassSegment> ret = classCombo.getItems().stream().filter(elem -> unidec.decodeTrimLowerCase(elem.toString()).contains(unidec.decodeTrimLowerCase(input)))
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
		AutoCompletionBinding<ClassSegment> completion = TextFields.bindAutoCompletion(classCombo.getEditor(), suggestionProvider);
		completion.setDelay(0);
		classCombo.setConverter(new StringConverter<ClassSegment>() {
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
				return classCombo.getItems().stream().filter(classe ->
						classe.getClassName().equalsIgnoreCase(string)||
								classe.toString().equalsIgnoreCase(string)||
								classe.getClassNumber().equalsIgnoreCase(string)||
								classe.getSegmentId().equalsIgnoreCase(string)).findFirst().orElse(latestClassComboValue);
			}
		});
		classCombo.valueProperty().addListener(new ChangeListener<ClassSegment>() {
			@Override
			public void changed(ObservableValue<? extends ClassSegment> observable, ClassSegment oldValue, ClassSegment newValue) {
				if(newValue!=null){
					classComboListView = ((ComboBoxListViewSkin) classCombo.getSkin()).getListView();
					classComboListView.getFocusModel().focus(classCombo.getItems().indexOf(newValue));
					classComboListView.scrollTo(newValue);
					latestClassComboValue =newValue;
					try {
						tableController.refresh_table_with_segment(newValue.getSegmentId());
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		classCombo.setOnShown(e->{
			ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) classCombo.getSkin();
			ListView<?> list = (ListView<?>) skin.getPopupContent();
			list.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
				if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
					classCombo.getEditor().selectAll();
				}
			});
			//list.setVisible(false);
			javafx.event.Event.fireEvent(list,new KeyEvent(KeyEvent.KEY_PRESSED,null,null,KeyCode.ESCAPE,false,false,false,false));
			droppingDown=true;
			completion.setUserInput("");
			classCombo.setOnShown(e2->{
				//list.setVisible(false);
				Event.fireEvent(list,new KeyEvent(KeyEvent.KEY_PRESSED,null,null,KeyCode.ESCAPE,false,false,false,false));
				droppingDown=true;
				completion.setUserInput("");
			});
		});
		classCombo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if(event.isControlDown() || event.isShiftDown()){
				return;
			}
			if(event.getCode().equals(KeyCode.DOWN) || event.getCode().equals(KeyCode.UP)){
				droppingDown=true;
				completion.setUserInput("");
			}
		});
		completion.prefWidthProperty().bind(classCombo.getEditor().widthProperty());
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
				classCombo.getEditor().selectAll();
				event.consume();
			}
		});
		completion.getAutoCompletionPopup().setOnShowing(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				final AutoCompletePopup source = (AutoCompletePopup) event.getSource();
				classComboAutoCompleteView = (ListView) (source.getSkin().getNode());
				classComboAutoCompleteView.getStylesheets().add(ClassComboDraftController.class.getResource("/styles/ComboBoxBlue.css").toExternalForm());
				classComboAutoCompleteView.setCellFactory(lv -> {
					ListCell<?> cell = new ListCell<Object>() {
						@Override
						public void updateItem(Object item, boolean empty) {
							super.updateItem(item, empty);
							if (empty) {
								setText(null);
							} else {
								setText(item.toString());
							}
							setOnMouseClicked(event->{
								classCombo.setValue((ClassSegment) item);
							});
						}
					};
					cell.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
						if (isNowHovered && ! cell.isEmpty()) {
							classComboAutoCompleteView.getSelectionModel().clearAndSelect(cell.getIndex());
						} else {
							//offHover
						}
					});

					return cell ;
				});
			}
		});
		completion.getAutoCompletionPopup().setOnShown(e->{
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if(classCombo.getValue()!=null && droppingDown){
						droppingDown=false;
						classComboAutoCompleteView.getSelectionModel().clearSelection();
						classComboAutoCompleteView.getSelectionModel().select(classCombo.getValue());
					}
					classComboAutoCompleteView.scrollTo(classComboAutoCompleteView.getSelectionModel().getSelectedIndex());
				}
			});

		});


	}
	@SuppressWarnings("static-access")
	private void load_table_pane() throws IOException, ClassNotFoundException, SQLException {
		
		tableButton.setSelected(true);
		setBottomRegionColumnSpans(false);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/TablePane_CharClassif.fxml"));
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
		//tableController.setCollapsedViewColumns(new String[] {"Completion Status","Question Status"});
		TablePane_CharClassif.loadLastSessionLayout();
		tableController.refresh_table_with_segment(account.getUser_desc_class(classCombo.getItems().get(1).getSegmentId()));
		tableController.restoreLastSessionLayout();
		System.gc();

	}

	@SuppressWarnings("static-access")
	public void load_image_pane(boolean checkMethodSelect) throws IOException, ParseException {
		

		if(!imageButton.isSelected()) {
			return;
		}
		
		setBottomRegionColumnSpans(true);
		if(imagePaneController!=null) {
			rightAnchor.getChildren().setAll(rightAnchorImageGrid);
			rightAnchor.setTopAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setBottomAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setLeftAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setRightAnchor(rightAnchorImageGrid, 0.0);
			
			imagePaneController.search_image(checkMethodSelect);
			;
			
		}else {
			;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/ImagePane_CharClassif.fxml"));
			rightAnchorImageGrid = loader.load();
			rightAnchor.getChildren().setAll(rightAnchorImageGrid);
			rightAnchor.setTopAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setBottomAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setLeftAnchor(rightAnchorImageGrid, 0.0);
			rightAnchor.setRightAnchor(rightAnchorImageGrid, 0.0);
			imagePaneController = loader.getController();
			imagePaneController.setParent(this);
			imagePaneController.search_image(checkMethodSelect);
			refresh_ui_display();
		}
		
		
	}
	@SuppressWarnings("static-access")
	public void load_class_pane() throws IOException {
		setBottomRegionColumnSpans(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/ClassPane_CharClassif.fxml"));
		rightAnchorContentGrid = loader.load();
		rightAnchor.getChildren().setAll(rightAnchorContentGrid);

		rightAnchor.setTopAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setBottomAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setLeftAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setRightAnchor(rightAnchorContentGrid, 0.0);

		classPaneController = loader.getController();
		classPaneController.setParent(this);
		refresh_ui_display();
	}
	@SuppressWarnings("static-access")
	public void load_rule_pane() throws IOException, ClassNotFoundException, SQLException {
		if(!ruleButton.isSelected()) {
			return;
		}

		setBottomRegionColumnSpans(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/RulePane_CharClassif.fxml"));
		rightAnchorContentGrid = loader.load();
		rightAnchor.getChildren().setAll(rightAnchorContentGrid);

		rightAnchor.setTopAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setBottomAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setLeftAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setRightAnchor(rightAnchorContentGrid, 0.0);

		rulePaneController = loader.getController();
		rulePaneController.setParent(this);
		refresh_ui_display();


	}

	@SuppressWarnings("static-access")
	public void load_char_pane() throws IOException, ClassNotFoundException, SQLException {
		if(!charButton.isSelected()) {
			return;
		}
		
		setBottomRegionColumnSpans(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/CharPane_CharClassif.fxml"));
		rightAnchorContentGrid = loader.load();
		rightAnchor.getChildren().setAll(rightAnchorContentGrid);

		rightAnchor.setTopAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setBottomAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setLeftAnchor(rightAnchorContentGrid, 0.0);
		rightAnchor.setRightAnchor(rightAnchorContentGrid, 0.0);

		charPaneController = loader.getController();
		charPaneController.setParent(this);
		refresh_ui_display();


	}
	
	private void load_browser_pane() throws IOException {
		System.out.println("Loading browser for the first time");
		googleButton.setSelected(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/paneScenes/Browser_CharClassif.fxml"));
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
			tableController.redimensionGrid();
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
			//V.printStackTrace(System.err);
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

		TextUtils.renderDescription(sd,tableController.charDescriptionTable.getSelectionModel().getSelectedItem(), helperAreaLeft.widthProperty());
		TextUtils.renderDescription(ld,tableController.charDescriptionTable.getSelectionModel().getSelectedItem(), helperAreaRight.widthProperty());
		TextUtils.renderDescription(sd_translated,tableController.charDescriptionTable.getSelectionModel().getSelectedItem(), helperAreaLeft.widthProperty());
		TextUtils.renderDescription(ld_translated,tableController.charDescriptionTable.getSelectionModel().getSelectedItem(), helperAreaRight.widthProperty());

		proposer.clearPropButtons();
		double RIGHT_TRANSLATE = 0.0*rule_field.getWidth();
		clear_data_fields();
		if(this.charButton.isSelected()) {
			this.charPaneController.load_item_chars();
		}
		if(this.ruleButton.isSelected() && !draftingRule) {
			this.rulePaneController.load_description_patterns();
		}
		CharDescriptionRow row = (CharDescriptionRow) this.tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
		if(row!=null) {
			
		}else {
			return;
		}
		try {
			
			ClassCaracteristic active_char = null;
			int selected_col = 0;
			if(!FxUtilTest.getComboBoxValue(classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
				selected_col = Math.floorMod(tableController.selected_col, CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).size());
				String itemSegementID = row.getClass_segment_string().split("&&&")[0];
				active_char = CharValuesLoader.active_characteristics.get(itemSegementID).get(selected_col);
				proposer.loadCharRuleProps(row,itemSegementID,active_char.getCharacteristic_id());
				try{
					if (row.getRulePropositions(active_char.getCharacteristic_id()).size() == 0) {
						proposer.loadCustomValues(CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).get(selected_col));
					}
				}catch (Exception V){
					//Null rule props
					proposer.loadCustomValues(CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).get(selected_col));
				}

			}else {
				active_char = CharItemFetcher.defaultCharValues.get(tableController.charDescriptionTable.getSelectionModel().getSelectedIndex()).getKey();
				
			}
			String itemClass = row.getClass_segment_string().split("&&&")[0];
			//deleteValueLabel.setTranslateX(custom_label_22.localToScene(custom_label_22.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX());
			try{
				String valueLink = row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getUrl();
				if(valueLink.length()>0){
					//urlLink.setText(valueLink);
				}else{
					//urlLink.setText(browserController.browser.latestPDFLink);
				}
			}catch (Exception V){
				try{
					//urlLink.setText(browserController.browser.latestPDFLink);
				}catch (Exception L){
				}
			}
			GridPane.setColumnIndex(deleteValueLabel,3);
			if(active_char.getIsNumeric()) {
				if( (active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0)) {

					UnitOfMeasure local_uom = null;
					String local_nom = null;
					String local_min = null;
					String local_max = null;
					
					CaracteristicValue val = row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id());
					try{
						Pair<ArrayList<String>, TextFlow> tmp = val.getFormatedDisplayAndUomPair(data_language!=user_language,conversionToggle.isSelected(), active_char);
						local_uom = UnitOfMeasure.RunTimeUOMS.get(tmp.getKey().get(0));
						local_nom = tmp.getKey().get(1);
						local_min = tmp.getKey().get(2);
						local_max = tmp.getKey().get(3);
					}catch(Exception V) {
					}
					
					
					//Add the allowed uoms to the autocomplete box
					for(String uom_id:active_char.getAllowedUoms()) {
						UnitOfMeasure loopUom = UnitOfMeasure.RunTimeUOMS.get(uom_id);
						if(loopUom!=null){
							this.uom_field.getEntries().add(loopUom);
						}
					}
					if(!conversionToggle.isSelected()) {
						if(local_uom!=null && this.uom_field.getEntries().contains(local_uom)) {

						}else if(local_uom!=null){
							this.uom_field.getEntries().add(local_uom);
						}
					}
					
					
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						if(!conversionToggle.isSelected()) {
							value_field.setText(local_nom);
						}else {
							value_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNominal_value());
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
							uom_field.setUom(UnitOfMeasure.RunTimeUOMS.get( row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getUom_id() ));
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
							min_field_uom.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getMin_value());
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
							max_field_uom.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getMax_value());
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
						note_field_uom.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNote());
					}catch(Exception V) {
						
					}
					note_field_uom.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					note_field_uom.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNominal_value());
					}catch(Exception V) {

					}
					value_field.setVisible(true);
					//Setting the minimum value
					custom_label_11.setText("Minimum value");
					custom_label_11.setTranslateX(+RIGHT_TRANSLATE);
					custom_label_11.setVisible(true);
					try{
						min_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getMin_value());
					}catch(Exception V) {
						
					}
					min_field.setVisible(true);
					min_field.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					
					//Setting the maximum value
					custom_label_12.setText("Maximum value");
					custom_label_12.setTranslateX(0.5*(rule_field.getWidth()+(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					custom_label_12.setVisible(true);
					try{
						max_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getMax_value());
					}catch(Exception V) {
						
					}
					max_field.setMaxWidth(0.5*(rule_field.getWidth()-(value_field.localToScene(value_field.getBoundsInLocal()).getMinX()-classification_style.localToScene(classification_style.getBoundsInLocal()).getMaxX())));
					max_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getRule_id());
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
						value_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the translated value
					custom_label_value.setText("Value ("+this.user_language_gcode.toUpperCase()+")");
					custom_label_value.setVisible(true);
					try{
						//translated_value_field.setText(this.tableController.translate2UserLanguage(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNominal_value()));
						translated_value_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getUserLanguageValue());
						
					}catch(Exception V) {
						
					}
					translated_value_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					GridPane.setColumnIndex(deleteValueLabel,5);
					//Setting the value
					value_label.setText("Value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					this.grid.setColumnSpan(value_field, 3);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getRule_id());
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
	public void assignValueOnSelectedItems(HashMap<String,CaracteristicValue> map) {

		//uiDirectValueRefresh(pattern_value);
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
		/*List<String> targetItemsIDs = tableController.tableGrid.getSelectionModel().getSelectedItems().stream().map(i->i.getItem_id()).collect(Collectors.toList());
		CharItemFetcher.allRowItems.parallelStream().filter(e->targetItemsIDs.contains(e.getItem_id()))
						.forEach(r->{
							CharValuesLoader.updateRuntimeDataForItem(r,FxUtilTest.getComboBoxValue(classCombo).getSegmentId(),CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id(),value);
							CharDescriptionExportServices.addItemCharDataToPush(r, FxUtilTest.getComboBoxValue(classCombo).getSegmentId(), CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id());

						});*/
		tableController.charDescriptionTable.getSelectionModel().getSelectedItems().parallelStream().forEach(r->{
			CharValuesLoader.updateRuntimeDataForItem(r,FxUtilTest.getComboBoxValue(classCombo).getSegmentId(),CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id(),map.get(r.getItem_id()));
			CharDescriptionExportServices.addItemCharDataToPush(r, FxUtilTest.getComboBoxValue(classCombo).getSegmentId(), CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id());
		});
		new HashSet<>(map.values()).forEach(value->{
			TranslationServices.beAwareOfNewValue(value, CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index));
			//TranslationServices.addThisValueToTheCharKnownSets(pattern_value, tableController.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index),true);
		});
		refresh_ui_display();
		tableController.charDescriptionTable.refresh();

	}
	public void assignValueOnSelectedItems(CaracteristicValue value) {

		//uiDirectValueRefresh(pattern_value);
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
		/*List<String> targetItemsIDs = tableController.tableGrid.getSelectionModel().getSelectedItems().stream().map(i->i.getItem_id()).collect(Collectors.toList());
		CharItemFetcher.allRowItems.parallelStream().filter(e->targetItemsIDs.contains(e.getItem_id()))
						.forEach(r->{
							CharValuesLoader.updateRuntimeDataForItem(r,FxUtilTest.getComboBoxValue(classCombo).getSegmentId(),CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id(),value);
							CharDescriptionExportServices.addItemCharDataToPush(r, FxUtilTest.getComboBoxValue(classCombo).getSegmentId(), CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id());
							
						});*/
		tableController.charDescriptionTable.getSelectionModel().getSelectedItems().parallelStream().forEach(r->{
			CharValuesLoader.updateRuntimeDataForItem(r,FxUtilTest.getComboBoxValue(classCombo).getSegmentId(),CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id(),value.shallowCopy(account));
			CharDescriptionExportServices.addItemCharDataToPush(r, FxUtilTest.getComboBoxValue(classCombo).getSegmentId(), CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index).getCharacteristic_id());
		});
		tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(CharDescriptionRow::reEvaluateCharRules);
		TranslationServices.beAwareOfNewValue(value, CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index));
		//TranslationServices.addThisValueToTheCharKnownSets(pattern_value, tableController.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).get(active_char_index),true);
		
		refresh_ui_display();
		tableController.charDescriptionTable.refresh();
		
	}
	public void sendSemiAutoPattern(CaracteristicValue pattern_value, String ruleString, String selectedText) {
		
		pattern_value.setSource(DataInputMethods.SEMI_CHAR_DESC);
		pattern_value.setAuthor(account.getUser_id());
		
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
		ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId())
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

		ruleString = WordUtils.correctDescriptionRuleSyntax(ruleString);

		if(draftingRule){
			CharDescriptionRow activeRow = tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
			String activeClass = tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0];
			int activeCharIndex = tableController.selected_col;
			ArrayList<ClassCaracteristic> activeChars = CharValuesLoader.active_characteristics.get(activeClass);
			ClassCaracteristic activeChar = activeChars.get(activeCharIndex%activeChars.size());
			GenericCharRule newRule = new GenericCharRule(ruleString, activeChar);
			newRule.setRegexMarker();
			if(newRule.parseSuccess()) {
				newRule.storeGenericCharRule();
				CharRuleResult draft = new CharRuleResult(newRule, selectedText, account);
				draft.setStatus("Draft");
				draft.setActionValue(pattern_value);
				activeRow.addRuleResult2Row(draft);
			}

		}else{
			pattern_value.setRule_id(ruleString);
			rule_field.setText(ruleString);
			CaracteristicValue tmp = pattern_value.shallowCopy(account);
			if(!proposer.selectionFromBrowser){
				tmp.setSource(DataInputMethods.SEMI_CHAR_DESC);
				assignValueOnSelectedItems(tmp);
				CharDescriptionRow targetItem = tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
				try{
					if (!charButton.isSelected()) {
						int idx = tableController.charDescriptionTable.getSelectionModel().getSelectedIndex();
						tableController.charDescriptionTable.getSelectionModel().clearAndSelect(idx + 1);
					}
				}catch (Exception V){

				}
				String finalRuleString = ruleString;
				new Thread(()->{
					CharPatternServices.applyItemRule(this,targetItem, finalRuleString);
					CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							refresh_ui_display();
						}
					});
				}).start();
			}else{
				tmp.setSource(DataInputMethods.MANUAL);
				tmp.setRule_id(null);
				assignValueOnSelectedItems(tmp);
				ExternalSearchServices.parsingValueFromURL();
			}
			CaracteristicValue copy = tmp.shallowCopy(account);
			copy.setSource(DataInputMethods.MANUAL);
			copy.setRule_id(null);
			this.lastInputValue=copy;
		}


	}
	
	public void sendPatternRule(String ruleString) {
	}
	
	public void sendPatternValue(CaracteristicValue pattern_value) {	
	}
	
	@SuppressWarnings("unused")
	private void uiDirectValueRefresh(CaracteristicValue pattern_value) {
		int active_char_index = Math.floorMod(this.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId()).size());
		ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(classCombo).getSegmentId())
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

	public void preparePatternProposition(String buttonText, CaracteristicValue preparedValue,
										  String preparedRule, ClassCaracteristic active_char, String selectedText) {
		if(active_char.getIsNumeric()) {
			preparedRule=WordUtils.reducePatternRuleSeparators(preparedRule);
		}
		try{
			proposer.addSemiAutoProposition(buttonText,preparedValue,preparedRule,active_char,selectedText);
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


	@FXML public void clearLink() {
		ExternalSearchServices.clearingURL(true);
	}

}
