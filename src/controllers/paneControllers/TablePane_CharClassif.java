package controllers.paneControllers;

import com.google.gson.reflect.TypeToken;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Callback;
import model.*;
import org.json.simple.parser.ParseException;
import service.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.data_exchange_toolbox.ComplexMap2JdbcObject;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.dialog_toolbox.FxUtilTest;
import service.ExternalSearchServices;
import transversal.generic.TextUtils;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TablePane_CharClassif {


	@FXML public TableView<CharDescriptionRow> charDescriptionTable;
	
	
	
	public static Char_description Parent;
	protected String translated_sd;
	protected String translated_ld;
	private static UserAccount account;
	private CharAdvancementUpdater advancement;
	//public HashMap<String,ArrayList<ClassCharacteristic>> active_characteristics = new HashMap<String,ArrayList<ClassCharacteristic>>();



	public ArrayList<CharDescriptionRow> itemArray;



	public int selected_col;



	private Task<Void> translationTask;



	private boolean stop_translation;



	private Thread translationThread;



	private String user_language_gcode;



	private boolean traverseGridFocus;


	@SuppressWarnings("rawtypes")
	public TableViewExtra tvX;



	private List<String> classItems;
	private boolean allowOverWriteAccountPreference = true;
	private boolean autoScrollToSelection=true;
	private ArrayList<String> hiddenColumns = new ArrayList<String>();
	private HashMap<String,Double> collapsedColumns = new HashMap<>();
	private HashMap<String,Double> visibleColumns = new HashMap<>();
	private boolean alreadyListeningWidthChange = false;

	public void restoreLastSessionLayout() {
		try{
			applySortOrder(account.getDescriptionSortColumns(),account.getDescriptionSortDirs());
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
	}

	public void restorePreviousLayout(){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try{
					applySortOrder(Parent.DescriptionSortColumns.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()),Parent.DescriptionSortDirs.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()));
				}catch(Exception V) {
					V.printStackTrace(System.err);
				}
			}
		});

	}

	private void applySortOrder(ArrayList<String> SortColumns, ArrayList<String> SortDirs) {
		if(SortColumns!=null && SortDirs!=null){
			allowOverWriteAccountPreference = false;
			charDescriptionTable.getSortOrder().clear();
			for (int ix = 0; ix < SortColumns.size(); ix++) {
				for (TableColumn<CharDescriptionRow, ?> c : charDescriptionTable.getColumns()) {
					if (c.getText().equals(SortColumns.get(ix))) {
						charDescriptionTable.getSortOrder().add(c);
						c.setSortType(TableColumn.SortType.valueOf(SortDirs.get(ix)));
					}
				}
			}

			try{
				selected_col = CharValuesLoader.active_characteristics.get(
						FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId())
						.stream().map(c->c.getCharacteristic_id())
						.collect(Collectors.toCollection(ArrayList::new))
						.indexOf(account.getActiveChar())-1;
				selected_col=selected_col==-2?-1:selected_col;
			}catch (Exception V){
				selected_col = -1;
			}
			nextChar();
			allowOverWriteAccountPreference=true;
		}

	}

	public static void loadLastSessionLayout() throws SQLException, ClassNotFoundException {

		Connection conn = Tools.spawn_connection_from_pool();
		PreparedStatement stmt = conn.prepareStatement("select "
				+ "user_description_sorting_columns, user_description_sorting_order,search_preferences, activeCharID, activeItemID"
				+ " from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		try {
			account.setDescriptionSortColumns(rs.getArray("user_description_sorting_columns"));
			account.setDescriptionSortDirs(rs.getArray("user_description_sorting_order"));
			account.setSearchSettings(ComplexMap2JdbcObject.deserialize(rs.getString("search_preferences"),new TypeToken<ArrayList<ArrayList<String>>>(){}.getType()));
			account.setActiveItem(rs.getString("activeItemID"));
			account.setActiveChar(rs.getString("activeCharID"));
		}catch(Exception V) {
			//V.printStackTrace(System.err);
		}
		rs.close();
		stmt.close();
		conn.close();


	}


	protected void initKeyBoardEvent(KeyEvent keyEvent, boolean pressed) {
		if(keyEvent.getCode().equals(KeyCode.CONTROL)) {
			account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.SHIFT)) {
			account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.D)) {
			account.PRESSED_KEYBOARD.put(KeyCode.D, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.U)) {
			account.PRESSED_KEYBOARD.put(KeyCode.U, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.DOWN)) {
			account.PRESSED_KEYBOARD.put(KeyCode.DOWN, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.UP)) {
			account.PRESSED_KEYBOARD.put(KeyCode.UP, pressed);
		}
		
		
	}
	
	
	@SuppressWarnings("deprecation")
	private void item_selection_routine(CharDescriptionRow tmp)  {
		Parent.CHANGING_CLASS=true;
		Parent.classification.setText(tmp.getClass_segment_string().split("&&&")[1]);
		Parent.CHANGING_CLASS=false;
		ExternalSearchServices.refreshUrlAfterElemChange(Parent);
		if(GlobalConstants.ALLOW_DESC_SEARCH_BAR_CUSTOMIZATION){
			CharDescriptionRow sourceItem = charDescriptionTable.getSelectionModel().getSelectedItem();
			String sourceSegment = sourceItem.getClass_segment_string().split("&&&")[0];
			ArrayList<ArrayList<String>> settings = account.getSearchSettings(sourceSegment);
			Parent.search_text.setText(ExternalSearchServices.evaluateSearchSentence(settings,sourceItem,sourceSegment));
		}else{
			if(tmp.getLong_desc()!=null && tmp.getLong_desc().length()>0) {
				Parent.search_text.setText(WordUtils.getSearchWords(tmp.getLong_desc()));
			}else {
				Parent.search_text.setText(WordUtils.getSearchWords(tmp.getShort_desc()));
			}
		}
		try{
			if(autoScrollToSelection){
				scrollToSelectedItem(tmp, 0);
			}
		}catch (Exception V){

		}

		Parent.refresh_ui_display();
		
		
		
		try {
			translationTask.cancel();
			stop_translation=true;
			translationThread.stop();
		}catch(Exception V) {
			
		}
		translationTask = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				try {

					translated_sd = tmp.getShort_desc_translated();
					translated_ld = tmp.getLong_desc_translated();
					Platform.runLater(new Runnable(){

						@Override
						public void run() {

							TextUtils.renderDescription(Parent.sd_translated,tmp,Parent.helperAreaLeft.widthProperty());
							TextUtils.renderDescription(Parent.ld_translated,tmp,Parent.helperAreaRight.widthProperty());

						}

					});
					TimeUnit.MILLISECONDS.sleep(800);
					Platform.runLater(new Runnable(){

						@Override
						public void run() {

							try {
								Parent.load_image_pane(false);
								if(GlobalConstants.ENABLE_TRANSLATION){
									translated_sd = tmp.getShort_desc_translated()!=null?tmp.getShort_desc_translated():translate2UserLanguage(tmp.getShort_desc());
									translated_ld = tmp.getLong_desc_translated()!=null?tmp.getLong_desc_translated():translate2UserLanguage(tmp.getLong_desc());
								}
							} catch (IOException | ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						});
				} catch ( InterruptedException e1) {
					
					// TODO Auto-generated catch block
				}
				
				stop_translation = false;
		    	advancement.refresh(Parent);
		    	
		    	
		    	return null;
		    }
		};
		translationTask.setOnSucceeded(e -> {
			
			if(stop_translation) {
				return;
			}
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					if(GlobalConstants.ENABLE_TRANSLATION){
						Parent.sd_translated.getChildren().add(new TextField(translated_sd+"\n\n\n\n\n"));
						Parent.ld_translated.getChildren().add(new TextField(translated_ld+"\n\n\n\n\n"));
					}
					
				}
				
				});
		
			});

		translationTask.setOnFailed(e -> {
		    Throwable problem = translationTask.getException();
		    //problem.printStackTrace();
		    problem.printStackTrace(System.err);
		    
		    //problem.printStackTrace(System.err);
		});

		translationTask.setOnCancelled(e -> {
		    
			;
		});
		
		translationThread = new Thread(translationTask);; translationThread.setDaemon(true);
		translationThread.setName("Trnsl");
		translationThread.start();
		
		
		
		
		
	}

	private void scrollToSelectedItem(CharDescriptionRow tmp, int offset) {
		int target = charDescriptionTable.getItems().indexOf(tmp);
		if(tvX.getFirstVisibleIndex()>target-offset || tvX.getLastVisibleIndex()<target-offset){
			tvX.scrollToIndex(Math.max(target-offset,0));
			return;
		}
		if (offset==1 && tvX.getLastVisibleIndex()<target){
			System.out.println("Scroll advance");
			charDescriptionTable.scrollTo(Math.max(target-offset,0));
		}

	}

	public String translate2UserLanguage(String description) throws IOException {
		if(description!=null) {
			
		}else {
			return null;
		}
		if(description.replace(" ", "").length()==0) {
			return "";
		}
		return TranslationServices.webTranslate("", this.user_language_gcode, description);
	}
	void scrolled(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        newValue.doubleValue();
        oldValue.doubleValue();
        
        
        
        
    }

	
	public void setUserLanguageGcode(String user_language_gcode) {
		this.user_language_gcode = user_language_gcode;
	}

	public void setParent(Char_description char_description) {
		this.Parent=char_description;
		MenuItem addCustomValue = new MenuItem("Use current value as custom proposition");
		addCustomValue.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				CharDescriptionRow row = charDescriptionTable.getSelectionModel().getSelectedItem();
				int activeCol = Math.floorMod(selected_col, CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).size());
				String itemClass = row.getClass_segment_string().split("&&&")[0];
				String activeCharId = CharValuesLoader.active_characteristics.get(itemClass).get(activeCol).getCharacteristic_id();
				CaracteristicValue activeData = row.getData(itemClass).get(activeCharId);
				Parent.proposer.addCustomValue(activeCharId,activeData,Parent.account);
				Parent.refresh_ui_display();
			}
		});
		ContextMenu customMenu = new ContextMenu();
		customMenu.getItems().add(addCustomValue);
		// only display context menu for non-empty rows:
		charDescriptionTable.setRowFactory(new Callback<TableView<CharDescriptionRow>, TableRow<CharDescriptionRow>>() {
			@Override
			public TableRow<CharDescriptionRow> call(TableView<CharDescriptionRow> tableView) {
				TableRow<CharDescriptionRow> row = new TableRow<CharDescriptionRow>();
				row.contextMenuProperty().bind(
						Bindings.when(row.emptyProperty())
								.then((ContextMenu)null)
								.otherwise(customMenu));
				return row;
			}
		});
	}
	
	public void fireManualClassChange(String result,boolean jumpNext) throws ClassNotFoundException, SQLException {
		
		List<CharDescriptionRow> databaseSyncList = new ArrayList<CharDescriptionRow>();
		HashMap<CharDescriptionRow,String> itemPreviousClasses = new HashMap<CharDescriptionRow,String>();
		for( Integer idx: (List<Integer>) charDescriptionTable.getSelectionModel().getSelectedIndices()) {
			CharDescriptionRow item = ((TableView<CharDescriptionRow>) this.charDescriptionTable).getItems().get(idx);
			itemPreviousClasses.put(item, item.getClass_segment_string());
			
			try{
				//item.setClass_segment(result);
				//tmp.setClass_segment(loop_class_id+"&&&"+loop_class_name+"&&&"+loop_class_number);
				item.setClass_segment_string(result);
				String[] resultSplit = result.split("&&&");
				CharItemFetcher.classifiedItems.put(item.getItem_id(),
						resultSplit[2]+"&&&"
						+resultSplit[1]+"&&&"
						+DataInputMethods.MANUAL+"&&&"
						+account.getUser_id()+"&&&"
						+resultSplit[0]);
			}catch(Exception V) {
				item.setClass_segment_string(null);
				CharItemFetcher.classifiedItems.put(item.getItem_id(), null);
				V.printStackTrace(System.err);
			}
			
			//Add items to the list to be pushed in the database
			databaseSyncList.add(((TableView<CharDescriptionRow>) this.charDescriptionTable).getItems().get(idx));
			
		}
		
		processClassChange(result.split("&&&")[0],result.split("&&&")[1],itemPreviousClasses);
		
		try{
			if(jumpNext) {
				jumpNext();
				
			}
		}catch(Exception V) {
			
		}
		
		charDescriptionTable.refresh();
		Tools.CharDescriptionRow2ClassEvent(databaseSyncList,account,DataInputMethods.MANUAL);
		CharPatternServices.applyNewClassRules(databaseSyncList,Parent);
		
	}
	private void processClassChange(String target_class_id, String target_class_name, HashMap<CharDescriptionRow, String> itemPreviousClasses) throws ClassNotFoundException, SQLException {
		
		Set<CharDescriptionRow> itemList = itemPreviousClasses.keySet();
		CharItemFetcher.allocateDataFieldForClassOnItems(target_class_id,itemList,this);
		
		//Not needed anymore database values are loaded at start
		//assignValuesToItemsByClass_V2(target_class_id,target_class_name,itemList.stream().
		//										map(i -> i.getItem_id()).collect(Collectors.toList()));
		
		allignEmptyNewValuesOnOldClassOnes(itemPreviousClasses);
	}
	private void allignEmptyNewValuesOnOldClassOnes(HashMap<CharDescriptionRow, String> itemPreviousClasses) {
		for(CharDescriptionRow row:itemPreviousClasses.keySet()) {
			if(row.getClass_segment_string().equals(itemPreviousClasses.get(row))) {
				//No class change
			}else {
				String currentClass = row.getClass_segment_string().split("&&&")[0];
				String previousClass = itemPreviousClasses.get(row).split("&&&")[0];
				if(GlobalConstants.ITEM_CHAR_DATA_TO_BE_COPIED_FROM_SAME_CARS_ONLY_WHEN_RECLASSIFIYING){
					CharValuesLoader.active_characteristics.get(currentClass).forEach(car -> {
						CaracteristicValue previousValue = row.getData(previousClass).get(car.getCharacteristic_id());
						CaracteristicValue currentValue = row.getData(currentClass).get(car.getCharacteristic_id());
						if (previousValue != null) {
							if (currentValue != null) {
								if (currentValue.getRawDisplay().length() == 0) {
									row.getData(currentClass).put(car.getCharacteristic_id(), previousValue);
									CharDescriptionExportServices.addItemCharDataToPush(row, currentClass, car.getCharacteristic_id());
								} else if (previousValue.getDescriptionTime().isAfter(currentValue.getDescriptionTime())) {
									row.getData(currentClass).put(car.getCharacteristic_id(), previousValue);
									CharDescriptionExportServices.addItemCharDataToPush(row, currentClass, car.getCharacteristic_id());
								}
							} else {
								row.getData(currentClass).put(car.getCharacteristic_id(), previousValue);
								CharDescriptionExportServices.addItemCharDataToPush(row, currentClass, car.getCharacteristic_id());
							}
						}
					});
				}else{
					//BAD CODE ONLY COPIES TO EXISTING CARAC FIELDS
					row.getData(currentClass).entrySet().forEach(e->{
						CaracteristicValue value = e.getValue();
						if(value!=null) {
							//Fresh value loaded during values assignement by new class
						}else {
							//Try to allign the new empty value on the old one
							ClassCaracteristic new_char = CharValuesLoader.active_characteristics.get(currentClass).stream().filter(c->c.getCharacteristic_id().equals(e.getKey())).findAny().get();
							//Search for an old char with the same name
							CharValuesLoader.active_characteristics.get(itemPreviousClasses.get(row).split("&&&")[0]).stream().filter(old_char->{
								if(
										old_char.getCharacteristic_name().toLowerCase().equals(new_char.getCharacteristic_name().toLowerCase())
								) {
									row.getData(currentClass).put(new_char.getCharacteristic_id(),row.getData(itemPreviousClasses.get(row).split("&&&")[0]).get(old_char.getCharacteristic_id()));
									CharDescriptionExportServices.addItemCharDataToPush(row,row.getClass_segment_string().split("&&&")[0],new_char.getCharacteristic_id());
									return true;
								}
								return false;
							}).findFirst();
						}
					});
				}
			}
			CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
		}
	}
	public void jumpNext() {
		
		int currentIdx = (int) Collections.max(charDescriptionTable.getSelectionModel().getSelectedIndices());
		autoScrollToSelection=false;
		charDescriptionTable.getSelectionModel().clearAndSelect(1+ currentIdx);
		scrollToSelectedItem(charDescriptionTable.getSelectionModel().getSelectedItem(), 1);
		autoScrollToSelection=true;
	}

	public void setUserAccount(UserAccount account) throws SQLException {
		TablePane_CharClassif.account =account;
		this.advancement = new CharAdvancementUpdater();
		advancement.setParentScene(Parent);
		advancement.account=account;
		advancement.refresh(Parent);
    	
		
	}
	
	public void redimensionGrid() {
		selectChartAtIndex(this.selected_col);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void refresh_table_with_segment(String active_class) throws ClassNotFoundException, SQLException {
		Parent.proposer.clearCustomValues();
		account.setUser_desc_class(active_class);
		Tools.set_desc_class(account);
		if(!active_class.equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			charDescriptionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			Parent.charButton.setDisable(false);

			this.classItems = getActiveItemsID(active_class);

			CharPatternServices.loadDescriptionRules(account.getActive_project());
			CharItemFetcher.fetchAllItems(account.getActive_project(),false);
			ClassCharacteristicsLoader.loadAllClassCharacteristic(account.getActive_project(),false);
			CharItemFetcher.initClassDataFields();
			ClassCharacteristicsLoader.loadKnownValuesAssociated2Items(account.getActive_project(),false);
			CharItemFetcher.generateItemArray(classItems,this);
			
			//Not needed any more loadAllClassCharWithKnownValues calls loadAllKnownValuesAssociated2Items
			//assignValuesToItemsByClass_V2(active_class,FxUtilTest.getComboBoxValue(Parent.classCombo).getclassName(),classItems);

			setColumns();
			restorePreviousLayout();

			fillTable(false);
			selectLastDescribedItem();
			this.selected_col = -1;
			nextChar();
			tvX = new TableViewExtra(charDescriptionTable);
			charDescriptionTable.refresh();
		}else {
			charDescriptionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			try {
				Parent.charPaneController.PaneClose();
			}catch(Exception V) {
				
			}
			try {
				Parent.rulePaneController.PaneClose();
			}catch(Exception V) {

			}
			Parent.charButton.setDisable(true);
			Parent.ruleButton.setDisable(true);

			CharItemFetcher.generateDefaultCharEditingItems(this);
			
			fillTable(true);
			selectFirstItem();
			this.selected_col = 0;
			Parent.refresh_ui_display();
		}
		
	}

	private void selectLastDescribedItem() {
		if(GlobalConstants.DESCRIPTION_RESTORE_PERSISTED_ITEM) {
			try{
				Optional<CharDescriptionRow> toSelect = charDescriptionTable.getItems().parallelStream().filter(i -> i.getItem_id().equals(account.getActiveItem())).findAny();
				if (toSelect.isPresent()) {
					charDescriptionTable.getSelectionModel().clearSelection();
					charDescriptionTable.getSelectionModel().select(toSelect.get());
				} else {
					charDescriptionTable.getSelectionModel().clearAndSelect(0);
				}
			}catch (Exception V){
				charDescriptionTable.getSelectionModel().clearAndSelect(0);
			}
		}else{
			Optional<LocalDateTime> latestDescriptionTimeInClass = charDescriptionTable.getItems().stream().map(
					r -> r.getData().values().stream().flatMap(a -> a.values().stream()).filter(v -> v != null && v.getDisplayValue(Parent).length() > 0).map(v -> v.getDescriptionTime()).filter(t -> t != null).max(new Comparator<LocalDateTime>() {
						@Override
						public int compare(LocalDateTime o1, LocalDateTime o2) {
							return o1.compareTo(o2);
						}
					})
			).filter(m -> m.isPresent()).map(m -> m.get()).findAny();
			if (latestDescriptionTimeInClass.isPresent()) {
				Optional<CharDescriptionRow> latestEditedItem = charDescriptionTable.getItems().stream().filter(
						r -> r.getData().values().stream().flatMap(a -> a.values().stream())
								.filter(v -> v != null && v.getDescriptionTime().isEqual(latestDescriptionTimeInClass.get())).findAny().isPresent()).findAny();
				if (latestEditedItem.isPresent()) {
					charDescriptionTable.getSelectionModel().clearSelection();
					charDescriptionTable.getSelectionModel().select(latestEditedItem.get());
					charDescriptionTable.scrollTo(charDescriptionTable.getSelectionModel().getSelectedIndex());
				}
			} else {
				charDescriptionTable.getSelectionModel().clearAndSelect(0);
			}
		}

	}


	public static List<String> getActiveItemsID(String active_class) throws ClassNotFoundException, SQLException {
		String joinStatement = "";
		if(CharItemFetcher.classifiedItems!=null) {
			
		}else {
			CharItemFetcher.classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(joinStatement,Tools.get_project_granularity(account.getActive_project()),account.getActive_project());
			Tools.get_project_segments(account);
		}
		/*classifiedItems.entrySet().parallelStream().collect(
				Collectors.groupingBy(Map.Entry::getValue,Collectors.counting()))
		.forEach((k,v)->{
			
		});*/

		List<String> classItems = CharItemFetcher.classifiedItems.entrySet().stream().filter(m->m.getValue().contains(active_class)).map(Entry::getKey).collect(Collectors.toList());
		return classItems;
	}
	
	
	
	
	
	
	/*
	@SuppressWarnings("unused")
	private void assignValuesToItemsByClass_V1(String target_class_id, String target_class_name, List<String> target_items) throws ClassNotFoundException, SQLException {
		
		
				
		
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt;
		ResultSet rs;
		
		stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,project_values.value_id,text_value_data_language,text_value_user_language,nominal_value,min_value,max_value,note,uom_id from "
				+ "(select * from "+account.getActive_project()+".project_items_x_values where item_id in ('"+String.join("','", target_items)+"')"
						+ "and characteristic_id in ('"+String.join("','", this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toSet()))+"')"
								+ ") data left join "+account.getActive_project()+".project_values "
										+ "on data.value_id = project_values.value_id");
		
		rs = stmt.executeQuery();
		List<String> charIdArray = this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toList());
		while(rs.next()) {
			String item_id = rs.getString("item_id");
			String characteristic_id = rs.getString("characteristic_id");
			String user_id = rs.getString("user_id");
			String description_method = rs.getString("description_method");
			String description_rule_id = rs.getString("description_rule_id");
			CharacteristicValue val = new CharacteristicValue();
			val.setValue_id(rs.getString("value_id"));
			val.setDataLanguageValue(rs.getString("text_value_data_language"));
			val.setUserLanguageValue(rs.getString("text_value_user_language"));
			val.setNominal_value(rs.getString("nominal_value"));
			val.setMin_value(rs.getString("min_value"));
			val.setMax_value(rs.getString("max_value"));
			val.setNote(rs.getString("note"));
			val.setUom_id(rs.getString("uom_id"));
			val.setParentChar(this.active_characteristics.get(target_class_id).get(charIdArray.indexOf(characteristic_id)));
			for(CharDescriptionRow row:this.itemArray) {
				if(row.getItem_id().equals(item_id)) {
					row.getData(target_class_id)[charIdArray.indexOf(characteristic_id)]=val;
					row.setAuthor(user_id);
					row.setSource(description_method);
					row.setRule_id(description_rule_id);		
				}
				if(row.getClass_segment()!=null) {
					
				}else {
					row.setClass_segment(target_class_id+"&&&"+target_class_name);
				}
			}
		}
		
		rs.close();
		stmt.close();
		conn.close();
	}
	
	//Version 2 loads all values associated to the class and if item in table, loads the data
	//in the item
	@SuppressWarnings("unused")
	private void assignValuesToItemsByClass_V2(String target_class_id, String target_class_name, List<String> target_items) throws ClassNotFoundException, SQLException {
		
		
				
		
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt;
		ResultSet rs;
		
		stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,project_values.value_id,text_value_data_language, text_value_user_language,nominal_value,min_value,max_value,note,uom_id from "
				+ "(select * from "+account.getActive_project()+".project_items_x_values where characteristic_id in ('"+String.join("','", this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toSet()))+"')"
								+ ") data left join "+account.getActive_project()+".project_values "
										+ "on data.value_id = project_values.value_id");
		
		rs = stmt.executeQuery();
		List<String> charIdArray = this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toList());
		while(rs.next()) {
			String item_id = rs.getString("item_id");
			String characteristic_id = rs.getString("characteristic_id");
			String user_id = rs.getString("user_id");
			String description_method = rs.getString("description_method");
			String description_rule_id = rs.getString("description_rule_id");
			CharacteristicValue val = new CharacteristicValue();
			val.setValue_id(rs.getString("value_id"));
			val.setDataLanguageValue(rs.getString("text_value_data_language"));
			val.setUserLanguageValue(rs.getString("text_value_user_language"));
			val.setNominal_value(rs.getString("nominal_value"));
			val.setMin_value(rs.getString("min_value"));
			val.setMax_value(rs.getString("max_value"));
			val.setNote(rs.getString("note"));
			val.setUom_id(rs.getString("uom_id"));
			val.setParentChar(this.active_characteristics.get(target_class_id).get(charIdArray.indexOf(characteristic_id)));
			for(CharDescriptionRow row:this.itemArray) {
				if(row.getItem_id().equals(item_id) && target_items.contains(row.getItem_id())) {
					row.getData(target_class_id)[charIdArray.indexOf(characteristic_id)]=val;
					row.setAuthor(user_id);
					row.setSource(description_method);
					row.setRule_id(description_rule_id);		
				}
				if(row.getClass_segment()!=null) {
					
				}else {
					row.setClass_segment(target_class_id+"&&&"+target_class_name);
				}
			}
		}
		
		rs.close();
		stmt.close();
		conn.close();
	}
	
	*/
	
	public void nextChar() {
		if(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			return;
		}
		selected_col+=1;
		selectChartAtIndex(selected_col);
	}
	public void previousChar() {
		if(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			return;
		}
		this.selected_col-=1;
		selectChartAtIndex(selected_col);
	}

	@SuppressWarnings("rawtypes")
	private void selectChartAtIndex(int i) {
		clearActiveColumnId();
		Parent.lastInputValue = null;
		while(selected_col<0){
			selected_col = selected_col + CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size();
		}
		int selected_col = Math.floorMod(i,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size());
		ClassCaracteristic activeChar = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).get(selected_col);
		for( TableColumn col:this.charDescriptionTable.getColumns()) {
			col.setVisible(
					( (!hiddenColumns.contains(col.getText()) && !hiddenColumns.contains(col.getId())) || (col.getId()!=null && col.getId().equals(activeChar.getCharacteristic_id())))
				&&	(!Parent.visibleRight.get() || collapsedColumns.containsKey(col.getText()) || collapsedColumns.containsKey(col.getId()))
			);
			if(col.isVisible()){
				try{
					col.prefWidthProperty().bind(charDescriptionTable.widthProperty().multiply(
							((Parent.visibleRight.get() ? collapsedColumns : visibleColumns).get(col.getText()))));
				}catch (Exception V){
					col.prefWidthProperty().bind(charDescriptionTable.widthProperty().multiply(
							((Parent.visibleRight.get() ? collapsedColumns : visibleColumns).get(col.getId()))));
				}
			}
			col.setId(col.getId()!=null && col.getId().equals(activeChar.getCharacteristic_id())?"active-column":col.getId());
		}
		Double visibleWidth = charDescriptionTable.getColumns().stream().filter(TableColumnBase::isVisible).filter(col -> col.getWidth() > 0).mapToDouble(col -> col.getWidth()).sum();
		if(visibleWidth<0.98*charDescriptionTable.getWidth()){
			System.out.println("Fixing");
			Optional<TableColumn<CharDescriptionRow, ?>> activeCol = charDescriptionTable.getColumns().stream().filter(col -> col.getId()!=null && col.getId().equals("active-column")).findFirst();
			if(activeCol.isPresent()){
				activeCol.get().prefWidthProperty().unbind();
				activeCol.get().prefWidthProperty().set(activeCol.get().getWidth()+(charDescriptionTable.getWidth()*0.98-(visibleWidth)));
			}
		}else{
			System.out.print("No column underflow: "+visibleWidth);
			System.out.print(">=");
			System.out.println(charDescriptionTable.getWidth()*0.98);
		}

		if(Parent.valueAutoComplete!=null) {
			Parent.valueAutoComplete.refresh_entries(true);
		}else {
			Parent.valueAutoComplete = new AutoCompleteBox_CharValue(Parent, Parent.value_field, true, account);
		}
		
		if(Parent.translationAutoComplete!=null) {
			Parent.translationAutoComplete.refresh_entries(false);
		}else {
			Parent.translationAutoComplete = new AutoCompleteBox_CharValue(Parent, Parent.translated_value_field, false, account);
			Parent.valueAutoComplete.setSibling(Parent.translationAutoComplete);
			Parent.translationAutoComplete.setSibling(Parent.valueAutoComplete);
		}

		ExternalSearchServices.refreshUrlAfterCaracChange(Parent);
		Parent.refresh_ui_display();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				//setSkinHeaderClickListeners();
				setObjectHeaderClickListeners();
				charDescriptionTable.getColumns().forEach(tmp->{
					if(alreadyListeningWidthChange){
						return;
					}
					tmp.widthProperty().addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
							//(Parent.visibleRight.get()?collapsedColumns:visibleColumns).put(tmp.getId() != null ? tmp.getId() : tmp.getText(),newValue.doubleValue()/charDescriptionTable.getWidth());
						}
					});
				});
				alreadyListeningWidthChange=true;
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fillTable(boolean defaultValueCharClassActive) {
		this.charDescriptionTable.getItems().clear();
        this.charDescriptionTable.getColumns().clear();
		this.charDescriptionTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        if(!defaultValueCharClassActive) {
			HashSet<String> colnames = new HashSet<String>();
			colnames.addAll(visibleColumns.keySet());
			colnames.addAll(collapsedColumns.keySet());
			colnames.addAll(hiddenColumns);
			colnames.forEach(colname->{
				TableColumn tmp = new TableColumn<>(colname);
				Optional<ClassCaracteristic> charMatch = CharValuesLoader.active_characteristics.values().parallelStream()
						.flatMap(Collection::stream)
						.filter(c -> c.getCharacteristic_id().equals(colname)).findAny();
				if(colname.equals("Completion Status")) {
					tmp.setCellValueFactory(new PropertyValueFactory<>("CompletionStatus"));
					tmp.setComparator(new Comparator() {
						@Override
						public int compare(Object o1, Object o2) {
							ObservableList<Node> children1 = ((StackPane) o1).getChildren();
							AtomicInteger ret1 = new AtomicInteger();
							ret1.set(0);
							children1.forEach(node -> {
								if (node instanceof Circle) {
									Paint fill = ((Circle) node).getFill();
									if (fill.toString().contains("bd392")) {
										//empty=
										ret1.addAndGet(-100);
									} else {
										//full
										ret1.addAndGet(+100);
									}
								} else if (node instanceof Text) {
									if (((Text) node).getText().contains("*")) {
										//has unknown
										ret1.addAndGet(-10);
									}
									;
								}
							});
							ObservableList<Node> children2 = ((StackPane) o2).getChildren();
							AtomicInteger ret2 = new AtomicInteger();
							ret2.set(0);
							children2.forEach(node -> {
								if (node instanceof Circle) {
									Paint fill = ((Circle) node).getFill();
									if (fill.toString().contains("bd392")) {
										//empty=
										ret2.addAndGet(-100);
									} else {
										//full
										ret2.addAndGet(+100);
									}
								} else if (node instanceof Text) {
									if (((Text) node).getText().contains("*")) {
										//has unknown
										ret2.addAndGet(-10);
									}
									;
								}
							});
							return Integer.compare(ret1.get(), ret2.get());
						}
					});
				}else if (colname.equals("Class Name")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							return new ReadOnlyObjectWrapper(r.getValue().getClass_segment_string().split("&&&")[1]);
							/*
							classNameColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.085));;
							classNameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
							 */
						}
					});
				}else if (colname.startsWith("Short Description") || colname.startsWith("Long Description")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							return new ReadOnlyObjectWrapper<>(CharClassifProposer.getCustomDescription(r.getValue(),colname));
						}
						/*descriptionColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.4));;
						descriptionColumn.setStyle( "-fx-alignment: CENTER-LEFT;");*/
					});
				}else if(colname.equals("Link")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							try{
								String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
								return new ReadOnlyObjectWrapper(
										WordUtils.shortUrlDisplay(
												r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id()).getUrl()
										)
								);
							}catch(Exception V) {
								return new ReadOnlyObjectWrapper("");
							}
						}
					});
				}else if(colname.equals("Source")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							try{String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
								return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id()).getSource());
							}catch(Exception V) {
								return new ReadOnlyObjectWrapper("");
							}
						}
					});
				}else if(colname.equals("Rule")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							try{
								String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
								String activeCharId = CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id();
								CaracteristicValue activeData = r.getValue().getData(itemClass).get(activeCharId);
								if(activeData.getSource().equals(DataInputMethods.AUTO_CHAR_DESC)){
									return new ReadOnlyObjectWrapper(r.getValue().getRuleResults().get(activeCharId).stream().filter(result -> result.getStatus()!=null && result.getStatus().equals("Applied")).findAny().get().getMatchedBlock());
								}
								if(activeData.getSource().equals(DataInputMethods.SEMI_CHAR_DESC)){
									return new ReadOnlyObjectWrapper(r.getValue().getRuleResults().get(activeCharId).stream().filter(result->result.getGenericCharRule()!=null).filter(result -> result.getGenericCharRule().getRuleSyntax()!=null && result.getGenericCharRule().getRuleSyntax().equals(activeData.getRule_id())).findAny().get().getMatchedBlock());
								}
								return new ReadOnlyObjectWrapper(activeData.getRule_id());
							}catch(Exception V) {
								return new ReadOnlyObjectWrapper("");
							}
						}
					});
				}else if(colname.equals("Author")){
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							try{
								String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
								return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id()).getAuthorName());
							}catch(Exception V) {
								return new ReadOnlyObjectWrapper("");
							}
						}
					});
				}else if(colname.equals("Article ID")){
					tmp.setCellValueFactory(new PropertyValueFactory<>("client_item_number"));
				}else if(charMatch.isPresent()){
					tmp.setId(charMatch.get().getCharacteristic_id());
					tmp.setText(charMatch.get().getCharacteristic_name());
					tmp.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
						public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
							CaracteristicValue val = r.getValue().getData(r.getValue().getClass_segment_string().split("&&&")[0]).get(charMatch.get().getCharacteristic_id());
							if(val!=null){
								String dsp = null;
								dsp = val.getDisplayValue(Parent);
								if(dsp!=null && dsp.length()>0){
									return new ReadOnlyObjectWrapper(dsp);
								}else if (r.getValue().getRulePropositions(colname).size()>0){
									return new ReadOnlyObjectWrapper<>("*PENDING*");
								}else{
									return new ReadOnlyObjectWrapper("");
								}
							}else if(CharValuesLoader.active_characteristics.get(r.getValue().getClass_segment_string().split("&&&")[0]).stream().anyMatch(loopchar->loopchar.getCharacteristic_id().equals(charMatch.get().getCharacteristic_id()))){
								//item has empty value
								return new ReadOnlyObjectWrapper("");
							}else{
								//charMatch not applicable
								return new ReadOnlyObjectWrapper("N/A");
							}
							/*
							col.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
							 */
						}
					});
				}else{
					tmp.setCellValueFactory(new PropertyValueFactory<>(colname.replace(" ", "")));
				}
				tmp.setResizable(true);
				tmp.setVisible(false);
				this.charDescriptionTable.getColumns().add(tmp);
			});
        	Parent.classification.setEditable(true);
        	Parent.classification.setDisable(false);
        }else {
        	Parent.classification.setEditable(false);
        	Parent.classification.setDisable(true);
        	
        	TableColumn CaracNameColumn = new TableColumn<>("Caracteristic name");
        	CaracNameColumn.setCellValueFactory(new PropertyValueFactory<>("Short_desc"));
        	CaracNameColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.3));;
        	CaracNameColumn.setResizable(false);
            this.charDescriptionTable.getColumns().add(CaracNameColumn);
            
            TableColumn CaracValueColumn = new TableColumn<>("Caracteristic value");
            CaracValueColumn.setCellValueFactory(new PropertyValueFactory<>("Long_desc"));
            CaracValueColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.3));;
            CaracValueColumn.setResizable(false);
            this.charDescriptionTable.getColumns().add(CaracValueColumn);

			TableColumn sourceColumn = new TableColumn<>("Source");
			//sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
			sourceColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
					try{String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
						return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id()).getSource());
					}catch(Exception V) {
						return new ReadOnlyObjectWrapper("");
					}
				}
			});

			sourceColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.1));;
			sourceColumn.setResizable(false);
			this.charDescriptionTable.getColumns().add(sourceColumn);

			TableColumn ruleColumn = new TableColumn<>("Rule");
			//ruleColumn.setCellValueFactory(new PropertyValueFactory<>("rule_id"));
			ruleColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
					try{
						String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
						String activeCharId = CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id();
						CaracteristicValue activeData = r.getValue().getData(itemClass).get(activeCharId);
						if(activeData.getSource().equals(DataInputMethods.AUTO_CHAR_DESC)){
							return new ReadOnlyObjectWrapper(r.getValue().getRuleResults().get(activeCharId).stream().filter(result -> result.getStatus()!=null && result.getStatus().equals("Applied")).findAny().get().getMatchedBlock());
						}
						if(activeData.getSource().equals(DataInputMethods.SEMI_CHAR_DESC)){
							return new ReadOnlyObjectWrapper(r.getValue().getRuleResults().get(activeCharId).stream().filter(result->result.getGenericCharRule()!=null).filter(result -> result.getGenericCharRule().getRuleSyntax()!=null && result.getGenericCharRule().getRuleSyntax().equals(activeData.getRule_id())).findAny().get().getMatchedBlock());
						}
						return new ReadOnlyObjectWrapper(activeData.getRule_id());
					}catch(Exception V) {
						return new ReadOnlyObjectWrapper("");
					}
				}
			});

			ruleColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.1));;
			ruleColumn.setResizable(false);
			this.charDescriptionTable.getColumns().add(ruleColumn);

			TableColumn authorColumn = new TableColumn<>("Author");
			//authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
			authorColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
					try{
						String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
						return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(WordUtils.modColIndex(selected_col,CharValuesLoader.active_characteristics.get(itemClass))).getCharacteristic_id()).getAuthorName());
					}catch(Exception V) {
						return new ReadOnlyObjectWrapper("");
					}
				}
			});

			authorColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.1));;
			authorColumn.setResizable(false);
			this.charDescriptionTable.getColumns().add(authorColumn);

			TableColumn articleColumn = new TableColumn<>("Article ID");
			articleColumn.setCellValueFactory(new PropertyValueFactory<>("client_item_number"));
			articleColumn.prefWidthProperty().bind(this.charDescriptionTable.widthProperty().multiply(0.1));;
			articleColumn.setResizable(false);
			this.charDescriptionTable.getColumns().add(articleColumn);
            
        }

		this.charDescriptionTable.getItems().addAll(this.itemArray);


		charDescriptionTable.getSortOrder().addListener((ListChangeListener)(c -> {
			if(allowOverWriteAccountPreference){
				saveSortOrder();
			}
			charDescriptionTable.scrollTo(charDescriptionTable.getSelectionModel().getSelectedIndex());
			charDescriptionTable.refresh();
		}));

		charDescriptionTable.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent) 
            {
                initKeyBoardEvent(keyEvent,true);
            }
        });
         
		charDescriptionTable.setOnKeyReleased(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent) 
            {
                initKeyBoardEvent(keyEvent,false);
            }
        });
		
		
		

		
		
		charDescriptionTable.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    if(newValue && traverseGridFocus) {
		    	;
		    	traverseGridFocus=false;
		    	Parent.value_field.requestFocus();
		    	Parent.hideAutoCompletePopups();
		    	Parent.value_field.end();
		    	Parent.value_field.selectAll();
		    }
		});
		Parent.classification.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    if(newValue) {
		    	;
		    	Parent.classification.selectAll();
		    }
		});
		charDescriptionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null ) {
		    	
		    	Parent.counterSelection.setVisible(true);
		    	Parent.counterSelection.setText("Selected items: "+String.valueOf( charDescriptionTable.getSelectionModel().getSelectedIndices().size()) );
		    	
		    	int max_selected = (int) Collections.max(charDescriptionTable.getSelectionModel().getSelectedIndices());
		    	CharDescriptionRow tmp = (CharDescriptionRow) charDescriptionTable.getItems().get(max_selected);
		    	
		    	
		    	Parent.aidLabel.setText("Article ID: "+tmp.getClient_item_number());
				TextUtils.renderDescription(Parent.sd,tmp, Parent.helperAreaRight.widthProperty());
				TextUtils.renderDescription(Parent.ld,tmp, Parent.helperAreaRight.widthProperty());
		    	item_selection_routine(tmp);
		    	Parent.value_field.requestFocus();
		    	Parent.hideAutoCompletePopups();
		    	Parent.refreshAutoCompleteEntries();
				Parent.value_field.end();
				Parent.value_field.selectAll();
				traverseGridFocus=true;
				
		    }else {
		    	Parent.counterSelection.setVisible(false);
		    }
		 });
	}

	private void setObjectHeaderClickListeners(){
		charDescriptionTable.getColumns().forEach(column->{
			final ContextMenu popup = new ContextMenu();
			popup.setAutoHide(true);
			final MenuItem ligne0 = new MenuItem("Hide Column");
			final MenuItem ligne1 = new MenuItem("Default table display");
			final Menu ligne2 = new Menu("Display additional column...");

			ligne0.setDisable(column.getId()!=null && column.getId().equals("active-column"));
			ligne0.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if(column.getId()!=null && column.getId().equals("active-column")){
						ClassCaracteristic missingID = CharValuesLoader.active_characteristics.get(Parent.classCombo.getValue().getSegmentId()).stream()
								.filter(loopCar -> loopCar.getCharacteristic_name().equals(column.getText()) && !charDescriptionTable.getColumns().stream().map(TableColumn::getId).collect(Collectors.toCollection(ArrayList::new))
										.contains(loopCar.getCharacteristic_id())).findAny().get();
						column.setId(missingID.getCharacteristic_id());
					}
					if(Parent.visibleRight.get()){
						Double widthGained = collapsedColumns.get(column.getId() != null ? column.getId() : column.getText());
						collapsedColumns.remove(column.getId() != null ? column.getId() : column.getText());
						distributeWidthMargin(widthGained,collapsedColumns, null);
					}else{
						Double widthGained = visibleColumns.get(column.getId() != null ? column.getId() : column.getText());
						visibleColumns.remove(column.getId() != null ? column.getId() : column.getText());
						distributeWidthMargin(widthGained,visibleColumns, null);
						hiddenColumns.add(column.getId() != null ? column.getId() : column.getText());
					}
					redimensionGrid();
					popup.hide();
				}
			});
			ligne1.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					restaureDefaultColumns();
					redimensionGrid();
					popup.hide();
				}
			});

			hiddenColumns.forEach(hiddenField->{
				Optional<ClassCaracteristic> charMatch = CharValuesLoader.active_characteristics.get(Parent.classCombo.getValue().getSegmentId()).stream().filter(car -> car.getCharacteristic_id().equals(hiddenField)).findAny();
				MenuItem elem = new MenuItem(charMatch.isPresent()?charMatch.get().getCharacteristic_name():hiddenField);
				elem.setOnAction((event)->{
					if(column.getId()!=null && column.getId().equals("active-column")){
						ClassCaracteristic missingID = CharValuesLoader.active_characteristics.get(Parent.classCombo.getValue().getSegmentId()).stream()
								.filter(loopCar -> loopCar.getCharacteristic_name().equals(column.getText()) && !charDescriptionTable.getColumns().stream().map(TableColumn::getId).collect(Collectors.toCollection(ArrayList::new))
										.contains(loopCar.getCharacteristic_id())).findAny().get();
						column.setId(missingID.getCharacteristic_id());
					}
					hiddenColumns.add(column.getId()!=null?column.getId():column.getText());

					if(Parent.visibleRight.get()){
						collapsedColumns.put(hiddenField,collapsedColumns.get(column.getId())!=null?collapsedColumns.get(column.getId()):collapsedColumns.get(column.getText()));
						distributeWidthMargin(-collapsedColumns.get(hiddenField),collapsedColumns, column.getId()!=null?column.getId():column.getText());
					}
					visibleColumns.put(hiddenField,visibleColumns.get(column.getId())!=null?visibleColumns.get(column.getId()):visibleColumns.get(column.getText()));
					distributeWidthMargin(-visibleColumns.get(hiddenField),visibleColumns, column.getId()!=null?column.getId():column.getText());

					if(hiddenColumns.contains(column.getText())){
						hiddenColumns.remove(column.getText());
					}else{
						hiddenColumns.remove(column.getId());
					}
					hiddenColumns.remove(hiddenField);
					redimensionGrid();
				});
				ligne2.getItems().add(elem);
			});


			popup.getItems().add(ligne0);
			popup.getItems().add(ligne1);
			popup.getItems().add(ligne2);

			column.setContextMenu(popup);

		});
	}

	private void distributeWidthMargin(Double delta, HashMap<String, Double> targetList, String exception) {
		while(selected_col<0){
			selected_col = selected_col + CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size();
		}
		selected_col = Math.floorMod(selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size());
		String activeCharID = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).get(selected_col).getCharacteristic_id();
		Double denom = targetList.entrySet().stream().filter(e->(!hiddenColumns.contains(e.getKey()) || activeCharID.equals(e.getKey()))).map(Entry::getValue).mapToDouble(Double::doubleValue).sum();
		targetList.entrySet().stream().filter(entry->exception == null || !entry.getKey().equals(exception)).forEach(entry->{
			try{
				targetList.put(entry.getKey(),entry.getValue().doubleValue()*(1+delta/denom));
			}catch (Exception V){

			}
		});
	}

	private void clearActiveColumnId() {
		try{
			charDescriptionTable.getColumns().stream().filter(loopCol -> loopCol.getId()!=null && loopCol.getId().equals("active-column")).findAny().ifPresent(activecol -> {
				ClassCaracteristic missingID = CharValuesLoader.active_characteristics.get(Parent.classCombo.getValue().getSegmentId()).stream()
						.filter(loopCar -> loopCar.getCharacteristic_name().equals(activecol.getText()) && !charDescriptionTable.getColumns().stream().map(TableColumn::getId).collect(Collectors.toCollection(ArrayList::new))
								.contains(loopCar.getCharacteristic_id())).findAny().get();
				activecol.setId(missingID.getCharacteristic_id());
			});
		}catch (Exception V){
			//no problem, intitiating screen
		}
	}

	private void setSkinHeaderClickListeners() {
		// Step 1: Get the table header row.
		TableHeaderRow headerRow = null;
		for (Node n : ((TableViewSkin<?>) charDescriptionTable.getSkin()).getChildren()) {
			if (n instanceof TableHeaderRow) {
				headerRow = (TableHeaderRow) n;
			}
		}
		if (headerRow == null) {
			return;
		}

		// Step 2: Get the list of the header columns.
		NestedTableColumnHeader ntch = (NestedTableColumnHeader) headerRow.getChildren().get(1);
		ObservableList<TableColumnHeader> headers = ntch.getColumnHeaders();

		// Step 3: Add click listener to the header columns.
		for (int i = 0; i < headers.size(); i++) {
			TableColumnHeader header = headers.get(i);
			System.out.println("Setting header for column "+header.getTableColumn().getText());
			final int index = i;
			header.setOnMouseClicked(mouseEvent -> {
				// Optional:
				// Get the TableColumnBase (which is the object responsible
				// for displaying the content of the column.)
				TableColumnBase column = header.getTableColumn();

				// Step 4: Handle double mouse click event.
				if (mouseEvent.getButton() == MouseButton.SECONDARY) {
					System.out.println("Header cell " + index + " clicked! " + column.getText());
					final Popup popup = new Popup();
					popup.setAutoHide(true);
					final Label ligne0 = new Label("Hide Column");
					final Label ligne1 = new Label("Default table display");
					final Label ligne2 = new Label("Display additional column...");

					ligne0.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							popup.hide();
							if(Parent.visibleRight.get()){

							}else{

							}
						}
					});
					ligne0.setDisable(column.getId()!=null && column.getId().equals("active-column"));

					ligne1.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							popup.hide();
						}
					});
					ligne2.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							popup.hide();
						}
					});

					setPopupLigneStyle(ligne0);
					setPopupLigneStyle(ligne1);
					setPopupLigneStyle(ligne2);


					GridPane contentGrid = new GridPane();
					contentGrid.add(ligne0, 0, 0);
					contentGrid.add(ligne1, 0, 1);
					contentGrid.add(ligne2, 0, 2);
					contentGrid.setHgrow(ligne0, Priority.ALWAYS);
					ligne0.setMaxWidth(Integer.MAX_VALUE);
					contentGrid.setHgrow(ligne1, Priority.ALWAYS);
					ligne1.setMaxWidth(Integer.MAX_VALUE);
					contentGrid.setHgrow(ligne2, Priority.ALWAYS);
					ligne2.setMaxWidth(Integer.MAX_VALUE);
					contentGrid.setGridLinesVisible(true);

					popup.getContent().clear();
					popup.getContent().add(contentGrid);

					popup.show(header, mouseEvent.getScreenX() + 10, mouseEvent.getScreenY());
				}
			});
		}
	}

			private void setPopupLigneStyle(Node ligne) {
				final String HOVERED_BUTTON_STYLE = "-fx-background-color:#212934; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#ACB9CA;";
				final String STANDARD_BUTTON_STYLE="-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;";
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

	public void setColumns() {
		hiddenColumns.clear();
		collapsedColumns.clear();
		visibleColumns.clear();
		restaureDefaultColumns();
	}

	private void restaureDefaultColumns() {
		this.visibleColumns.put("Completion Status",0.085);
		this.visibleColumns.put("Class Name",0.085);
		this.visibleColumns.put("Long Description 1",0.215);
		this.visibleColumns.put("Link",0.1);
		this.visibleColumns.put("Source",0.1);
		this.visibleColumns.put("Rule",0.1);
		this.visibleColumns.put("Author",0.1);
		this.visibleColumns.put("Article ID",0.1);

		this.collapsedColumns.put("Completion Status",0.2);
		this.collapsedColumns.put("Long Description 1",0.675);

		CharValuesLoader.active_characteristics.get(Parent.classCombo.getValue().getSegmentId()).forEach(carac->{
			this.visibleColumns.put(carac.getCharacteristic_id(),0.1);
			this.collapsedColumns.put(carac.getCharacteristic_id(),0.1);
			this.hiddenColumns.add(carac.getCharacteristic_id());
		});
	}


	private void selectFirstItem() {
		//Temporary : select first item
		this.charDescriptionTable.getSelectionModel().select(0);
	}

	public void fireScrollNBUp(Boolean shiftDown) {
		try {
			int active_char_index = Math.floorMod(Parent.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size());
			String activeClass = FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId();
			ClassCaracteristic activeChar = CharValuesLoader.active_characteristics.get(activeClass).get(active_char_index);
			int min = (int) Collections.min(charDescriptionTable.getSelectionModel().getSelectedIndices());
			CharDescriptionRow thisItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(min));
			CharDescriptionRow previousItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(min-1));
			String data_this = "";
			String data_previous="";
			try{
				data_this = thisItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
			}catch (Exception V){
			}
			try{
				data_previous = previousItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
			}catch (Exception V){
			}

			if(data_this.length()>0  && data_previous.length()>0) {
				while(data_previous.length()>0) {
					min-=1;
					previousItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(min-1));
					data_previous="";
					try{
						data_previous = previousItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
					}catch (Exception V){
					}
				}
				if(shiftDown){
					charDescriptionTable.getSelectionModel().selectRange(Math.max(0,min),(int) Collections.min(charDescriptionTable.getSelectionModel().getSelectedIndices()));
				}else{
					charDescriptionTable.getSelectionModel().clearAndSelect(Math.max(0,min));
				}
				scrollToSelectedItem(charDescriptionTable.getSelectionModel().getSelectedItem(), 0);
			}else {
				while(! ( data_previous.length()>0 ) ) {
					min-=1;
					previousItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(min-1));
					data_previous="";
					try{
						data_previous = previousItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
					}catch (Exception V){
					}
				}
				if(shiftDown){
					charDescriptionTable.getSelectionModel().selectRange(Math.max(0,min),(int) Collections.min(charDescriptionTable.getSelectionModel().getSelectedIndices()));
				}else{
					charDescriptionTable.getSelectionModel().clearAndSelect(Math.max(0,min-1));
				}
				scrollToSelectedItem(charDescriptionTable.getSelectionModel().getSelectedItem(), 0);

			}
		}catch(Exception V) {
		}
	}


	public void fireScrollNBDown(Boolean shiftDown) {
		try {
			int active_char_index = Math.floorMod(Parent.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).size());
			String activeClass = FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId();
			ClassCaracteristic activeChar = CharValuesLoader.active_characteristics.get(activeClass).get(active_char_index);
			int max = (int) Collections.max(charDescriptionTable.getSelectionModel().getSelectedIndices());
			CharDescriptionRow thisItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(max));
			CharDescriptionRow nextItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(max+1));
			String data_this = "";
			String data_next="";
			try{
				data_this = thisItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
			}catch (Exception V){
			}
			try{
				data_next = nextItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
			}catch (Exception V){
			}

			if(data_this.length()>0  && data_next.length()>0) {
				while(data_next.length()>0) {
					max+=1;
					nextItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(max+1));
					data_next="";
					try{
						data_next = nextItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
					}catch (Exception V){
					}
				}
				if(shiftDown){
					charDescriptionTable.getSelectionModel().selectRange((int) Collections.max(charDescriptionTable.getSelectionModel().getSelectedIndices()),Math.min(charDescriptionTable.getItems().size(),max+1));
				}else{
					charDescriptionTable.getSelectionModel().clearAndSelect(Math.min(charDescriptionTable.getItems().size(),max));
				}
				scrollToSelectedItem(charDescriptionTable.getSelectionModel().getSelectedItem(), 0);
			}else {
				while(! ( data_next.length()>0 ) ) {
					max+=1;
					nextItem = ((CharDescriptionRow) charDescriptionTable.getItems().get(max+1));
					data_next="";
					try{
						data_next = nextItem.getData(activeClass).get(activeChar.getCharacteristic_id()).getRawDisplay();
					}catch (Exception V){
					}
				}
				if(shiftDown){
					charDescriptionTable.getSelectionModel().selectRange((int) Collections.max(charDescriptionTable.getSelectionModel().getSelectedIndices()),Math.min(charDescriptionTable.getItems().size(),max+1));
				}else{
					charDescriptionTable.getSelectionModel().clearAndSelect(Math.min(charDescriptionTable.getItems().size(),max+1));
				}
				scrollToSelectedItem(charDescriptionTable.getSelectionModel().getSelectedItem(), 0);

			}
		}catch(Exception V) {
		}
	}

	public void refresh_table_preserve_sort_order() throws SQLException, ClassNotFoundException {
		allowOverWriteAccountPreference = false;
		account.getDescriptionSortColumns().clear();
		account.getDescriptionSortDirs().clear();
		for (TableColumn<CharDescriptionRow, ?> c : charDescriptionTable.getSortOrder()) {

			account.getDescriptionSortColumns().add(c.getText());
			account.getDescriptionSortDirs().add(c.getSortType().toString());
		}
		CharDescriptionRow SI = charDescriptionTable.getSelectionModel().getSelectedItem();
		int SC = new Integer(selected_col);
		refresh_table_with_segment(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId());

		if(account.getDescriptionSortColumns().stream().filter(tc->!charDescriptionTable.getColumns().stream().map(c->c.getText()).collect(Collectors.toList()).contains(tc)).findAny().isPresent()){
			//At least a sorting column is no longer available, do not restore order
		}else{
			//Restore order
			restoreLastSessionLayout();
			//tableGrid.getSelectionModel().clearAndSelect(SI);
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				charDescriptionTable.getSelectionModel().clearSelection();
				charDescriptionTable.getSelectionModel().select(SI);
				selected_col = SC-1;
				nextChar();
			}
		});
		allowOverWriteAccountPreference=true;
	}

	private void saveSortOrder() {
		account.getDescriptionSortColumns().clear();
		Parent.DescriptionSortColumns.put(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId(), new ArrayList<String>());
		account.getDescriptionSortDirs().clear();
		Parent.DescriptionSortDirs.put(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId(), new ArrayList<String>());

		for (TableColumn<CharDescriptionRow, ?> c : charDescriptionTable.getSortOrder()) {
			account.getDescriptionSortColumns().add(c.getText());
			Parent.DescriptionSortColumns.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).add(c.getText());
			account.getDescriptionSortDirs().add(c.getSortType().toString());
			Parent.DescriptionSortDirs.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getSegmentId()).add(c.getSortType().toString());
		}


		try {
			transversal.data_exchange_toolbox.ComplexMap2JdbcObject.saveAccountProjectPreferenceForDescription(account);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


    public void ReevaluateItems(HashSet<String> items2Update) {
		 CharItemFetcher.allRowItems.parallelStream().filter(r-> items2Update.contains(r.getItem_id())).forEach(CharDescriptionRow::reEvaluateCharRules);
		 CharDescriptionExportServices.flushItemDataToDBThreaded(account, null);
	}
}