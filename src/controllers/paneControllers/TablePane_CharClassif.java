package controllers.paneControllers;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import model.*;
import org.json.simple.parser.ParseException;
import service.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.dialog_toolbox.FxUtilTest;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TablePane_CharClassif {


	@FXML public TableView<CharDescriptionRow> tableGrid;
	
	
	
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



	private static ArrayList<String> collapsedViewColumns;



	@SuppressWarnings("rawtypes")
	private TableViewExtra tvX;



	private List<String> classItems;
	private boolean allowOverWriteAccountPreference = true;

	public void restoreLastSessionLayout() {
		try{
			applySortOrder(account.getDescriptionSortColumns(),account.getDescriptionSortDirs());
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
	}

	public void restorePreviousLayout(){
		System.out.println("Restoring layout for "+FxUtilTest.getComboBoxValue(Parent.classCombo).getclassName());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try{
					applySortOrder(Parent.DescriptionSortColumns.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()),Parent.DescriptionSortDirs.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()));
				}catch(Exception V) {
					V.printStackTrace(System.err);
				}
			}
		});

	}

	private void applySortOrder(ArrayList<String> SortColumns, ArrayList<String> SortDirs) {
		if(SortColumns!=null && SortDirs!=null){
			allowOverWriteAccountPreference = false;
			tableGrid.getSortOrder().clear();
			for (int ix = 0; ix < SortColumns.size(); ix++) {
				for (TableColumn<CharDescriptionRow, ?> c : tableGrid.getColumns()) {
					if (c.getText().equals(SortColumns.get(ix))) {
						System.out.print(SortColumns.get(ix)+" ");
						tableGrid.getSortOrder().add(c);
						c.setSortType(TableColumn.SortType.valueOf(SortDirs.get(ix)));
						System.out.println(SortDirs.get(ix));
					}
				}
			}

			selected_col = account.getDescriptionActiveIdx()-1;
			nextChar();
			allowOverWriteAccountPreference=true;
		}

	}

	public static void loadLastSessionLayout() throws SQLException, ClassNotFoundException {

		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("select "
				+ "user_description_sorting_columns, user_description_sorting_order, user_description_active_index"
				+ " from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		try {
			account.setDescriptionSortColumns(rs.getArray(1));
			account.setDescriptionSortDirs(rs.getArray(2));
			account.setDescriptionActiveIdx(rs.getInt(3));
		}catch(Exception V) {
			V.printStackTrace(System.err);
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
		this.Parent.CHANGING_CLASS=true;
		this.Parent.classification.setText(tmp.getClass_segment_string().split("&&&")[1]);
		this.Parent.CHANGING_CLASS=false;
		if(tmp.getLong_desc().length()>0) {
			Parent.search_text.setText(WordUtils.getSearchWords(tmp.getLong_desc()));
		}else {
			Parent.search_text.setText(WordUtils.getSearchWords(tmp.getShort_desc()));
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
					
						
					TimeUnit.MILLISECONDS.sleep(800);
					Platform.runLater(new Runnable(){

						@Override
						public void run() {

							try {
								Parent.load_image_pane(false);
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
				translated_sd = tmp.getShort_desc_translated()!=null?tmp.getShort_desc_translated():translate2UserLanguage(tmp.getShort_desc());
	    		translated_ld = tmp.getLong_desc_translated()!=null?tmp.getLong_desc_translated():translate2UserLanguage(tmp.getLong_desc());
	    	
		    	advancement.refresh();
		    	
		    	
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
					
					Parent.sd_translated.setText(translated_sd+"\n\n\n\n\n");
					Parent.ld_translated.setText(translated_ld+"\n\n\n\n\n");
					
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
		//translationThread.start();
		
		
		
		
		
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
		
	}
	
	public void fireManualClassChange(String result,boolean jumpNext) throws ClassNotFoundException, SQLException {
		
		List<CharDescriptionRow> databaseSyncList = new ArrayList<CharDescriptionRow>();
		HashMap<CharDescriptionRow,String> itemPreviousClasses = new HashMap<CharDescriptionRow,String>();
		for( Integer idx: (List<Integer>) tableGrid.getSelectionModel().getSelectedIndices()) {
			CharDescriptionRow item = ((TableView<CharDescriptionRow>) this.tableGrid).getItems().get(idx);
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
			databaseSyncList.add(((TableView<CharDescriptionRow>) this.tableGrid).getItems().get(idx));
			
		}
		
		processClassChange(result.split("&&&")[0],result.split("&&&")[1],itemPreviousClasses);
		
		try{
			if(jumpNext) {
				jumpNext();
				
			}
		}catch(Exception V) {
			
		}
		
		tableGrid.refresh();
		Tools.CharDescriptionRow2ClassEvent(databaseSyncList,account,DataInputMethods.MANUAL);
		
		
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
				String itemClass = row.getClass_segment_string().split("&&&")[0];
				row.getData(itemClass).entrySet().forEach(e->{
					CaracteristicValue value = e.getValue();
					if(value!=null) {
						//Fresh value loaded during values assignement by new class
					}else {
						//Try to allign the new empty value on the old one
						ClassCaracteristic new_char = CharValuesLoader.active_characteristics.get(itemClass).stream().filter(c->c.getCharacteristic_id().equals(e.getKey())).findAny().get();
						//Search for an old char with the same name
						CharValuesLoader.active_characteristics.get(itemPreviousClasses.get(row).split("&&&")[0]).stream().filter(old_char->{
							if(
									old_char.getCharacteristic_name().toLowerCase().equals(new_char.getCharacteristic_name().toLowerCase())
							) {
								row.getData(itemClass).put(new_char.getCharacteristic_id(),row.getData(itemPreviousClasses.get(row).split("&&&")[0]).get(old_char.getCharacteristic_id()));
								CharDescriptionExportServices.addItemCharDataToPush(row,row.getClass_segment_string().split("&&&")[0],new_char.getCharacteristic_id());
								return true;
							}
							return false;
						}).findFirst();
					}
				});
			}
			CharDescriptionExportServices.flushItemDataToDB(account);
		}
	}
	private void jumpNext() {
		
		int currentIdx = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		tableGrid.getSelectionModel().clearAndSelect(1+ currentIdx);
		scrollSelectedItemVisible();
	}

	public void setUserAccount(UserAccount account) {
		this.account=account;
		this.advancement = new CharAdvancementUpdater();
		advancement.setParentScene(this.Parent);
		advancement.account=account;
		advancement.refresh();
    	
		
	}
	
	public void collapseGrid(boolean visibleRight, GridPane parentGrid) {
		selectChartAtIndex(this.selected_col,visibleRight);
		
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void refresh_table_with_segment(String active_class) throws ClassNotFoundException, SQLException {
		account.setUser_desc_class(active_class);
		Tools.set_desc_class(account);
		if(!active_class.equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			Parent.charButton.setDisable(false);

			this.classItems = getActiveItemsID(active_class);
			
			CharItemFetcher.fetchAllItems(account.getActive_project(),this);
			ClassCharacteristicsLoader.loadAllClassCharacteristic(this,account.getActive_project());
			CharItemFetcher.initClassDataFields(this);
			ClassCharacteristicsLoader.loadKnownValuesAssociated2Items(account.getActive_project());
			CharItemFetcher.generateItemArray(classItems,this);
			
			//Not needed any more loadAllClassCharWithKnownValues calls loadAllKnownValuesAssociated2Items
			//assignValuesToItemsByClass_V2(active_class,FxUtilTest.getComboBoxValue(Parent.classCombo).getclassName(),classItems);


			restorePreviousLayout();
			
			fillTable(false);
			selectLastDescribedItem();
			this.selected_col = -1;
			nextChar();

		}else {
			try {
				Parent.charPaneController.PaneClose();
			}catch(Exception V) {
				
			}
			Parent.charButton.setDisable(true);
			
			CharItemFetcher.generateDefaultCharEditingItems(this);
			
			fillTable(true);
			selectFirstItem();
			this.selected_col = 0;
			Parent.refresh_ui_display();
		}
		
	}

	private void selectLastDescribedItem() {
		Optional<LocalDateTime> latestDescriptionTimeInClass = tableGrid.getItems().stream().map(
				r -> r.getData().values().stream().flatMap(a -> a.values().stream()).filter(v->v!=null && v.getDisplayValue(Parent).length()>0).map(v -> v.getDescriptionTime()).max(new Comparator<LocalDateTime>() {
					@Override
					public int compare(LocalDateTime o1, LocalDateTime o2) {
						return o1.compareTo(o2);
					}
				})
		).filter(m -> m.isPresent()).map(m -> m.get()).findAny();
		if(latestDescriptionTimeInClass.isPresent()){
			Optional<CharDescriptionRow> latestEditedItem = tableGrid.getItems().stream().filter(
					r -> r.getData().values().stream().flatMap(a-> a.values().stream())
							.filter(v -> v != null && v.getDescriptionTime().isEqual(latestDescriptionTimeInClass.get())).findAny().isPresent()).findAny();
			if(latestEditedItem.isPresent()){
				tableGrid.getSelectionModel().clearSelection();
				tableGrid.getSelectionModel().select(latestEditedItem.get());
				tableGrid.scrollTo(tableGrid.getSelectionModel().getSelectedIndex());
			}
		}else{
			tableGrid.getSelectionModel().clearAndSelect(0);
		}

	}


	public  List<String> getActiveItemsID(String active_class) throws ClassNotFoundException, SQLException {
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
		if(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			return;
		}
		selected_col+=1;
		selectChartAtIndex(selected_col,Parent.charButton.isSelected());
	}
	public void previousChar() {
		if(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
			return;
		}
		this.selected_col-=1;
		selectChartAtIndex(selected_col,Parent.charButton.isSelected());
	}

	@SuppressWarnings("rawtypes")
	private void selectChartAtIndex(int i, boolean collapsedView) {
		if(selected_col<0){
			selected_col = selected_col + CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).size();
		}
		int selected_col = Math.floorMod(i,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).size());
		List<String> char_headers = CharValuesLoader.returnSortedCopyOfClassCharacteristic(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).stream().map(c->c.getCharacteristic_name()).collect(Collectors.toList());
		for( Object col:this.tableGrid.getColumns()) {
			int idx = char_headers.indexOf(((TableColumn)col).getText());
			if(idx!=selected_col ) {
				if(collapsedViewColumns.contains(((TableColumn)col).getText())) {
					//Hide/Show collapse only columns
					((TableColumn)col).setVisible(collapsedView);
					continue;
				}
				
				if(idx==-1) {
					//keep full view columns visible if not in collapsed views , hide otherwise
					//Unless column is description column
					if(((TableColumn)col).getText().equals("Description")) {
						((TableColumn)col).prefWidthProperty().bind(tableGrid.widthProperty().multiply(collapsedView?0.68:0.3));;
						((TableColumn)col).setVisible(true);
						continue;
					}
					((TableColumn)col).setVisible(!collapsedView);
					continue;
				}
				//This a non active characteristic
				((TableColumn)col).setVisible(false);
			}else {
				//this an active characteristic
				/*if(this.defaultColumnStyle!=null) {
					((TableColumn)col).setStyle(defaultColumnStyle+"-fx-background-color: #AAAAAA;");
				}else {
					this.defaultColumnStyle = ((TableColumn)col).getStyle();
					((TableColumn)col).setStyle(defaultColumnStyle+"-fx-background-color: #AAAAAA;");
				}*/
				
				((TableColumn)col).setVisible(true);
				
			}
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
		
		
				
		Parent.refresh_ui_display();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fillTable(boolean defaultValueCharClassActive) {
		this.tableGrid.getItems().clear();
        this.tableGrid.getColumns().clear();
        
        if(!defaultValueCharClassActive) {
        	
        	for(String colname : this.collapsedViewColumns) {
            	TableColumn tmp = new TableColumn<>(colname);
            	tmp.setCellValueFactory(new PropertyValueFactory<>(colname.replace(" ", "")));
                tmp.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
                tmp.setResizable(false);
                tmp.setVisible(false);
                this.tableGrid.getColumns().add(tmp);
                
            }
            
            TableColumn classNameColumn = new TableColumn<>("Class Name");
            classNameColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
                 public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                     
                     return new ReadOnlyObjectWrapper(r.getValue().getClass_segment_string().split("&&&")[1]);
                 }
              });
            this.tableGrid.getColumns().add(classNameColumn);
            classNameColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.085));;
            classNameColumn.setResizable(false);
            classNameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
            
            
            
            
            
            
            
            TableColumn descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
                 public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                     if(r.getValue().getLong_desc()!=null && r.getValue().getLong_desc().length()>0) {
                         return new ReadOnlyObjectWrapper(r.getValue().getLong_desc());
                     }
                     try{
                    	 return new ReadOnlyObjectWrapper(r.getValue().getShort_desc());
                     }catch(Exception V) {
                    	 return new ReadOnlyObjectWrapper("");
                     }
                 }
              });
            this.tableGrid.getColumns().add(descriptionColumn);
            descriptionColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.4));;
            descriptionColumn.setResizable(false);
            descriptionColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

            CharValuesLoader.returnSortedCopyOfClassCharacteristic(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).stream().forEach(characteristic->{
				TableColumn col = new TableColumn<>(characteristic.getCharacteristic_name());
				col.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
						try{
							return new ReadOnlyObjectWrapper(r.getValue().getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(characteristic.getCharacteristic_id()).getDisplayValue(Parent));
						}catch(Exception V) {
							//Object has null data at daataIndex
							return new ReadOnlyObjectWrapper("");
						}
					}
				});
				col.setVisible(false);
				col.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
				col.setResizable(false);

				this.tableGrid.getColumns().add(col);
			});
        	/*
        	for(int i=0;i<CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).size();i++) {
                ClassCaracteristic characteristic = CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(i);
                final int dataIndex = i;
                TableColumn col = new TableColumn<>(characteristic.getCharacteristic_name());
                col.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
                     public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                        try{
                            return new ReadOnlyObjectWrapper(r.getValue().getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment())[dataIndex].getDisplayValue(Parent));
                        }catch(Exception V) {
                            //Object has null data at daataIndex
                            return new ReadOnlyObjectWrapper("");
                        }
                     }
                  });
                col.setVisible(false);
                col.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
                col.setResizable(false);
                
                this.tableGrid.getColumns().add(col);
            }
        	*/
        	TableColumn linkColumn = new TableColumn<>("Link");
            //linkColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
            linkColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                    try{
						String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
                    	return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getUrl());
                    }catch(Exception V) {
                   	 return new ReadOnlyObjectWrapper("");
                    }
                }
             });
            linkColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
            linkColumn.setResizable(false);
            this.tableGrid.getColumns().add(linkColumn);
            
        	Parent.classification.setEditable(true);
        	Parent.classification.setDisable(false);
        }else {
        	Parent.classification.setEditable(false);
        	Parent.classification.setDisable(true);
        	
        	TableColumn CaracNameColumn = new TableColumn<>("Caracteristic name");
        	CaracNameColumn.setCellValueFactory(new PropertyValueFactory<>("Short_desc"));
        	CaracNameColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.3));;
        	CaracNameColumn.setResizable(false);
            this.tableGrid.getColumns().add(CaracNameColumn);
            
            TableColumn CaracValueColumn = new TableColumn<>("Caracteristic value");
            CaracValueColumn.setCellValueFactory(new PropertyValueFactory<>("Long_desc"));
            CaracValueColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.3));;
            CaracValueColumn.setResizable(false);
            this.tableGrid.getColumns().add(CaracValueColumn);
            
            
        }
        
        
        
        TableColumn sourceColumn = new TableColumn<>("Source");
        //sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                try{String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
					return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getSource());
                }catch(Exception V) {
               	 return new ReadOnlyObjectWrapper("");
                }
            }
         });
        
        sourceColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        sourceColumn.setResizable(false);
        this.tableGrid.getColumns().add(sourceColumn);
        
        TableColumn ruleColumn = new TableColumn<>("Rule");
        //ruleColumn.setCellValueFactory(new PropertyValueFactory<>("rule_id"));
        ruleColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                try{
                	String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
					return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getRule_id());
                }catch(Exception V) {
               	 return new ReadOnlyObjectWrapper("");
                }
            }
         });
        
        ruleColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        ruleColumn.setResizable(false);
        this.tableGrid.getColumns().add(ruleColumn);
        
        TableColumn authorColumn = new TableColumn<>("Author");
        //authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                try{
					String itemClass = r.getValue().getClass_segment_string().split("&&&")[0];
					return new ReadOnlyObjectWrapper(r.getValue().getData(itemClass).get(CharValuesLoader.active_characteristics.get(itemClass).get(selected_col).getCharacteristic_id()).getAuthorName());
                }catch(Exception V) {
               	 return new ReadOnlyObjectWrapper("");
                }
            }
         });
        
        authorColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        authorColumn.setResizable(false);
        this.tableGrid.getColumns().add(authorColumn);
        
        TableColumn articleColumn = new TableColumn<>("Article ID");
        articleColumn.setCellValueFactory(new PropertyValueFactory<>("client_item_number"));
        articleColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        articleColumn.setResizable(false);
        this.tableGrid.getColumns().add(articleColumn);

		tvX = new TableViewExtra(tableGrid);
        this.tableGrid.getItems().addAll(this.itemArray);
        //this.tableGrid.refresh();

		tableGrid.getSortOrder().addListener((ListChangeListener)(c -> {
			if(allowOverWriteAccountPreference){
				saveSortOrder();
			}
			tableGrid.scrollTo(tableGrid.getSelectionModel().getSelectedIndex());
		}));

		tableGrid.setOnKeyPressed(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                initKeyBoardEvent(keyEvent,true);
            }
        });
         
		tableGrid.setOnKeyReleased(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                initKeyBoardEvent(keyEvent,false);
            }
        });
		
		
		
		tableGrid.getSelectionModel().setSelectionMode(
			    SelectionMode.SINGLE
			);
		
		
		tableGrid.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
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
		tableGrid.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null ) {
		    	
		    	Parent.counterSelection.setVisible(true);
		    	Parent.counterSelection.setText("Selected items: "+String.valueOf( tableGrid.getSelectionModel().getSelectedIndices().size()) );
		    	
		    	int max_selected = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		    	CharDescriptionRow tmp = (CharDescriptionRow) tableGrid.getItems().get(max_selected);
		    	
		    	
		    	Parent.aidLabel.setText("Article ID: "+tmp.getClient_item_number());
		    	Parent.sd.setText(tmp.getShort_desc()+"\n\n\n\n\n");
		    	Parent.ld.setText(tmp.getLong_desc()+"\n\n\n\n\n");
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
	public void setCollapsedViewColumns(String[] strings) {
		this.collapsedViewColumns = new ArrayList<String>();
		for(String elem:strings) {
			this.collapsedViewColumns.add(elem);
		}
	}
	
	
	private void selectFirstItem() {
		//Temporary : select first item
		this.tableGrid.getSelectionModel().select(0);
	}

	public void fireScrollNBUp() {
		try {
			int active_char_index = Math.floorMod(Parent.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).size());
			int min = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
			CharDescriptionRow thisItem = ((CharDescriptionRow) tableGrid.getItems().get(min));
			CharDescriptionRow previousItem = ((CharDescriptionRow) tableGrid.getItems().get(min-1));
			String data_this = "";
			String data_previous="";
			try{
				data_this = thisItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
			}catch (Exception V){

			}
			try{
				data_previous = previousItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
			}catch (Exception V){

			}

			if(data_this.length()>0  && data_previous.length()>0) {
				while(data_previous.length()>0) {
					min-=1;
					previousItem = ((CharDescriptionRow) tableGrid.getItems().get(min-1));
					data_previous="";
					try{
						data_previous = previousItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
					}catch (Exception V){

					}
				}
				tableGrid.getSelectionModel().clearAndSelect(Math.max(0,min));
				scrollSelectedItemVisible();
			}else {
				while(! ( data_previous.length()>0 ) ) {
					min-=1;
					previousItem = ((CharDescriptionRow) tableGrid.getItems().get(min-1));
					data_previous="";
					try{
						data_previous = previousItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
					}catch (Exception V){

					}
				}
				tableGrid.getSelectionModel().clearAndSelect(Integer.max(min-1,0));
				scrollSelectedItemVisible();

			}
		}catch(Exception V) {

		}
	}


	public void fireScrollNBDown() {

		try {
			int active_char_index = Math.floorMod(Parent.tableController.selected_col,CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).size());
			int max = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
			CharDescriptionRow thisItem = ((CharDescriptionRow) tableGrid.getItems().get(max));
			CharDescriptionRow nextItem = ((CharDescriptionRow) tableGrid.getItems().get(max+1));
			String data_this = "";
			String data_next="";
			try{
				data_this = thisItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
			}catch (Exception V){

			}
			try{
				data_next = nextItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
			}catch (Exception V){

			}

			if(data_this.length()>0  && data_next.length()>0) {
				while(data_next.length()>0) {
					max+=1;
					nextItem = ((CharDescriptionRow) tableGrid.getItems().get(max+1));
					data_next="";
					try{
						data_next = nextItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
					}catch (Exception V){

					}
				}
				tableGrid.getSelectionModel().clearAndSelect(Math.min(tableGrid.getItems().size(),max));
				scrollSelectedItemVisible();
			}else {
				while(! ( data_next.length()>0 ) ) {
					max+=1;
					nextItem = ((CharDescriptionRow) tableGrid.getItems().get(max+1));
					data_next="";
					try{
						data_next = nextItem.getData(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(CharValuesLoader.active_characteristics.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).get(active_char_index)).getDisplayValue(Parent);
					}catch (Exception V){

					}
				}
				tableGrid.getSelectionModel().clearAndSelect(Math.min(tableGrid.getItems().size(),max+1));
				scrollSelectedItemVisible();

			}
		}catch(Exception V) {

		}
	}

	private void scrollSelectedItemVisible() {
		int selectedIdx = tableGrid.getSelectionModel().getSelectedIndex();
		int fvIdx = tvX.getFirstVisibleIndex();
		int lvIdx = tvX.getLastVisibleIndex();
		if( fvIdx<=selectedIdx && selectedIdx<=lvIdx){
			return;
		}
		tableGrid.scrollTo(selectedIdx);
	}

	public void refresh_table_preserve_sort_order() throws SQLException, ClassNotFoundException {
		allowOverWriteAccountPreference = false;
		account.getDescriptionSortColumns().clear();
		account.getDescriptionSortDirs().clear();
		for (TableColumn<CharDescriptionRow, ?> c : tableGrid.getSortOrder()) {

			account.getDescriptionSortColumns().add(c.getText());
			account.getDescriptionSortDirs().add(c.getSortType().toString());
		}
		account.setDescriptionActiveIdx(selected_col);
		int SI = tableGrid.getSelectionModel().getSelectedIndex();

		refresh_table_with_segment(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment());

		if(account.getDescriptionSortColumns().stream().filter(tc->!tableGrid.getColumns().stream().map(c->c.getText()).collect(Collectors.toList()).contains(tc)).findAny().isPresent()){
			//At least a sorting column is no longer available, do not restore order
		}else{
			//Restore order
			restoreLastSessionLayout();
			//tableGrid.getSelectionModel().clearAndSelect(SI);
		}
		try{
			//Parent.charPaneController.tableGrid.scrollTo(Parent.charPaneController.tableGrid.getSelectionModel().getSelectedIndex());
		}catch (Exception E){

		}
		allowOverWriteAccountPreference=true;
	}

	private void saveSortOrder() {
		System.out.println("XXX Saving sort order for "+FxUtilTest.getComboBoxValue(Parent.classCombo).getclassName()+"XXX");
		System.out.println(tableGrid.getSortOrder().stream().map(e->e.getText()).collect(Collectors.toList()));
		account.getDescriptionSortColumns().clear();
		Parent.DescriptionSortColumns.put(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment(), new ArrayList<String>());
		account.getDescriptionSortDirs().clear();
		Parent.DescriptionSortDirs.put(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment(), new ArrayList<String>());

		for (TableColumn<CharDescriptionRow, ?> c : tableGrid.getSortOrder()) {
			account.getDescriptionSortColumns().add(c.getText());
			Parent.DescriptionSortColumns.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).add(c.getText());
			account.getDescriptionSortDirs().add(c.getSortType().toString());
			Parent.DescriptionSortDirs.get(FxUtilTest.getComboBoxValue(Parent.classCombo).getClassSegment()).add(c.getSortType().toString());
		}

		account.setDescriptionActiveIdx(selected_col);

		try {
			transversal.data_exchange_toolbox.ComplexMap2JdbcObject.saveAccountProjectPreferenceForDescription(account);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
