package controllers.paneControllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
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
import model.AutoCompleteBox_CharValue;
import model.CharDescriptionRow;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.ClassificationMethods;
import model.UserAccount;
import service.CharAdvancementUpdater;
import service.CharItemFetcher;
import service.ClassCharacteristicsLoader;
import service.TableViewExtra;
import service.TranslationServices;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class TablePane_CharClassif {
	
	
	@FXML public TableView<CharDescriptionRow> tableGrid;
	
	
	
	private Char_description Parent;
	protected String translated_sd;
	protected String translated_ld;
	private UserAccount account;
	private CharAdvancementUpdater advancement;
	public HashMap<String,ArrayList<ClassCharacteristic>> active_characteristics = new HashMap<String,ArrayList<ClassCharacteristic>>();



	public ArrayList<CharDescriptionRow> itemArray;



	public int selected_col;



	private Task<Void> translationTask;



	private boolean stop_translation;



	private Thread translationThread;



	private String user_language_gcode;



	private boolean traverseGridFocus;



	private ArrayList<String> collapsedViewColumns;



	@SuppressWarnings("rawtypes")
	private TableViewExtra tvX;



	private List<String> classItems;



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
		this.Parent.classification.setText(tmp.getClass_segment().split("&&&")[1]);
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
		translationThread.start();
		
		
		
		
		
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
			itemPreviousClasses.put(item, item.getClass_segment());
			
			try{
				//item.setClass_segment(result);
				//tmp.setClass_segment(loop_class_id+"&&&"+loop_class_name+"&&&"+loop_class_number);
				item.setClass_segment(result);
				String[] resultSplit = result.split("&&&");
				CharItemFetcher.classifiedItems.put(item.getItem_id(),
						resultSplit[2]+"&&&"
						+resultSplit[1]+"&&&"
						+ClassificationMethods.MANUAL+"&&&"
						+account.getUser_id()+"&&&"
						+resultSplit[0]);
			}catch(Exception V) {
				item.setClass_segment(null);
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
		Tools.CharDescriptionRow2ClassEvent(databaseSyncList,account,ClassificationMethods.MANUAL);
		
		
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
			if(row.getClass_segment().equals(itemPreviousClasses.get(row))) {
				//No class change
			}else {
				for(int idx=0;idx<row.getData(row.getClass_segment().split("&&&")[0]).length;idx++) {
					CharacteristicValue value = row.getData(row.getClass_segment().split("&&&")[0])[idx];
					if(value!=null) {
						//Fresh value loaded during values assignement by new class
					}else {
						//Try to allign the new empty value on the old one
						ClassCharacteristic new_char = active_characteristics.get(row.getClass_segment().split("&&&")[0]).get(idx);
						//Search for an old char with the same name
						for(int idx2=0;idx2<active_characteristics.get(itemPreviousClasses.get(row).split("&&&")[0]).size();idx2++ ) {
							ClassCharacteristic old_char = active_characteristics.get(itemPreviousClasses.get(row).split("&&&")[0]).get(idx2);
							if(
									old_char.getCharacteristic_name().toLowerCase().equals(new_char.getCharacteristic_name().toLowerCase())
									||
									old_char.getCharacteristic_name_translated().toLowerCase().equals(new_char.getCharacteristic_name_translated().toLowerCase()
							)) {
								row.getData(row.getClass_segment().split("&&&")[0])[idx]=row.getData(itemPreviousClasses.get(row).split("&&&")[0])[idx2];
								break;
							}
						}
						
						
					}
				}
			}
			this.Parent.ROW_SYNC_POOL.add(row);
		}
	}
	private void jumpNext() {
		
		int currentIdx = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		tableGrid.getSelectionModel().clearAndSelect(1+ currentIdx);
		if(1+currentIdx>tvX.getLastVisibleIndex()) {
			tableGrid.scrollTo(currentIdx+1);
			//tableGrid.scrollTo(tvX.getFirstVisibleIndex()+2);
		}
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
		
		
			
		tvX = new TableViewExtra(tableGrid);
		
		this.classItems = getActiveItemsID(active_class);
		
		CharItemFetcher.fetchAllItems(account.getActive_project(),this);
		ClassCharacteristicsLoader.loadAllClassCharacteristic(this,account.getActive_project());
		CharItemFetcher.initClassDataFields(this);
		ClassCharacteristicsLoader.loadKnownValuesAssociated2Items(this,account.getActive_project());
		CharItemFetcher.loadItemArray(classItems,this);
		
		//Not needed any more loadAllClassCharWithKnownValues calls loadAllKnownValuesAssociated2Items
		//assignValuesToItemsByClass_V2(active_class,this.Parent.classCombo.getSelectionModel().getSelectedItem().getclassName(),classItems);
		
		
		
		
		fillTable();
		this.selected_col = -1;
		selectedLatestEditedItem();
		nextChar();
	}
	
	
	public  List<String> getActiveItemsID(String active_class) throws ClassNotFoundException, SQLException {
		String joinStatement = "";
		if(CharItemFetcher.classifiedItems!=null) {
			
		}else {
			CharItemFetcher.classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(joinStatement,Tools.get_project_granularity(account.getActive_project()),account.getActive_project());
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
		this.selected_col+=1;
		selectChartAtIndex(selected_col,this.Parent.charButton.isSelected());
	}
	public void previousChar() {
		this.selected_col-=1;
		selectChartAtIndex(selected_col,this.Parent.charButton.isSelected());
	}

	@SuppressWarnings("rawtypes")
	private void selectChartAtIndex(int i,boolean collapsedView) {
		int selected_col = Math.floorMod(i,this.active_characteristics.get(Parent.classCombo.getValue().getClassSegment()).size());
		List<String> char_headers = this.active_characteristics.get(Parent.classCombo.getValue().getClassSegment()).stream().map(c->c.getCharacteristic_name()).collect(Collectors.toList());
		for( Object col:this.tableGrid.getColumns()) {
			int idx = char_headers.indexOf(((TableColumn)col).getText());
			if(idx!=selected_col ) {
				if(this.collapsedViewColumns.contains(((TableColumn)col).getText())) {
					//Hide/Show collapse only columns
					((TableColumn)col).setVisible(collapsedView);
					continue;
				}
				
				if(idx==-1) {
					//keep full view columns visible if not in collapsed views , hide otherwise
					//Unless column is description column
					if(((TableColumn)col).getText().equals("Description")) {
						((TableColumn)col).prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(collapsedView?0.68:0.3));;
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
		}
		
		
				
		Parent.refresh_ui_display();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fillTable() {
		this.tableGrid.getItems().clear();
        this.tableGrid.getColumns().clear();
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
                 
                 return new ReadOnlyObjectWrapper(r.getValue().getClass_segment().split("&&&")[1]);
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
                 return new ReadOnlyObjectWrapper(r.getValue().getShort_desc());
             }
          });
        this.tableGrid.getColumns().add(descriptionColumn);
        descriptionColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.4));;
        descriptionColumn.setResizable(false);
        descriptionColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
        
        for(int i=0;i<this.active_characteristics.get(Parent.classCombo.getValue().getClassSegment()).size();i++) {
            ClassCharacteristic characteristic = this.active_characteristics.get(Parent.classCombo.getValue().getClassSegment()).get(i);
            final int dataIndex = i;
            TableColumn col = new TableColumn<>(characteristic.getCharacteristic_name());
            col.setCellValueFactory(new Callback<CellDataFeatures<CharDescriptionRow, String>, ObservableValue<String>>() {
                 public ObservableValue<String> call(CellDataFeatures<CharDescriptionRow, String> r) {
                    try{
                        return new ReadOnlyObjectWrapper(r.getValue().getData(Parent.classCombo.getSelectionModel().getSelectedItem().getClassSegment())[dataIndex].getDisplayValue(Parent,characteristic));
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
        
        TableColumn linkColumn = new TableColumn<>("Link");
        linkColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        linkColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        linkColumn.setResizable(false);
        this.tableGrid.getColumns().add(linkColumn);
        
        TableColumn sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        sourceColumn.setResizable(false);
        this.tableGrid.getColumns().add(sourceColumn);
        
        TableColumn ruleColumn = new TableColumn<>("Rule");
        ruleColumn.setCellValueFactory(new PropertyValueFactory<>("rule_id"));
        ruleColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        ruleColumn.setResizable(false);
        this.tableGrid.getColumns().add(ruleColumn);
        
        TableColumn authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        authorColumn.setResizable(false);
        this.tableGrid.getColumns().add(authorColumn);
        
        TableColumn articleColumn = new TableColumn<>("Article ID");
        articleColumn.setCellValueFactory(new PropertyValueFactory<>("client_item_number"));
        articleColumn.prefWidthProperty().bind(this.tableGrid.widthProperty().multiply(0.1));;
        articleColumn.setResizable(false);
        this.tableGrid.getColumns().add(articleColumn);
        
        
        this.tableGrid.getItems().setAll(this.itemArray);
        //this.tableGrid.refresh();
        
        

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
			    SelectionMode.MULTIPLE
			);
		
		
		tableGrid.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    if(newValue && traverseGridFocus) {
		    	;
		    	traverseGridFocus=false;
		    	Parent.value_field.requestFocus();
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
	
	
	private void selectedLatestEditedItem() {
		//Temporary : select first item
		this.tableGrid.getSelectionModel().select(0);
	}

}
