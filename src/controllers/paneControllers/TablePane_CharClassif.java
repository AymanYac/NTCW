package controllers.paneControllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import model.CharDescriptionRow;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.ClassificationMethods;
import model.UserAccount;
import service.CharAdvancementUpdater;
import service.CharClassifProposer;
import service.TableViewExtra;
import service.Translator;
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



	private ArrayList<CharDescriptionRow> itemArray;



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

	/*
	public void fillTable_DYNAMIC(List<ItemFetcherRow> fillList) {
		setTableValueFactories();
		tableGrid.getItems().clear();
		
		oq = new ObservableDeque<ItemFetcherRow>();
		oq.addAll(fillList);
		fillList=null;
		tableGrid.setItems(oq);
		tableGrid.getSelectionModel().select(Math.floorDiv(oq.size(), 2));
		
	}
	public void fillTable_STATIC(List<ItemFetcherRow> fillList) {
		
		setTableValueFactories();
		tableGrid.getItems().clear();
		
		oa = FXCollections.observableArrayList();
		oa.addAll(fillList);
		fillList = null;
		tableGrid.setItems(oa);
		tvX = new TableViewExtra(tableGrid);
		unchangedColumns = Collections.unmodifiableList(new ArrayList<TableColumn<ItemFetcherRow, ?>>(tableGrid.getColumns()));
		//tableGrid.getItems().addAll(fillList);
		//tableGrid.getSelectionModel().select(Math.floorDiv(oq.size(), 2));
		
		try {
			restorePreviousLayout();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void restorePreviousLayout() throws ClassNotFoundException, SQLException {
		
		System.out.println("loading previous layout" );
		
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("select user_manual_propositions, "
				+ "user_manual_sorting_columns, user_manual_sorting_order"
				+ " from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		try {
			System.out.println(rs.getArray(1));
			account.setManualPropositions(rs.getArray(1));
			System.out.println(rs.getArray(2));
			account.setManualSortColumns(rs.getArray(2));
			System.out.println(rs.getArray(3));
			account.setManualSortDirs(rs.getArray(3));
			
		}catch(Exception V) {
			
		}
		
		rs.close();
		stmt.close();
		conn.close();
		
		try{
			applySortOrder(account.getManualSortColumns(),account.getManualSortDirs());
		}catch(Exception V) {
			
		}
		if(account.getManualPropositions()!=null && account.getManualPropositions().size() == GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS) {
			Parent.context.methods = account.getManualPropositions();
			Parent.proposer.proposeAgain();
		}
		
		try{
			selectLastClassifiedItem();
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
		
	}
	
	private void selectLastClassifiedItem() throws ClassNotFoundException, SQLException {
		String last_id = Tools.get_project_last_classified_item_id(account.getActive_project());
		if(last_id!=null) {
			
		}else {
			return;
		}
		for(Object row: tableGrid.getItems()) {
			if ( ((ItemFetcherRow) row).getItem_id().equals(last_id) ){
				tableGrid.getSelectionModel().select(row);
				tableGrid.scrollTo(tableGrid.getSelectionModel().getSelectedIndex());
				Parent.aidLabel.requestFocus();
				tableGrid.requestFocus();
				break;
			}
		}
	}
	public void listenTableScroll() {
		//bar = Tools.getVerticalScrollbar(tableGrid);
		//bar.setValue(bar.getMax()*0.5);
		//previous_visible_index = tvX.getFirstVisibleIndex();
        //bar.valueProperty().addListener(this::scrolled);
	}
	
	
	@SuppressWarnings("unchecked")
	public void setTableValueFactories() {
		
		account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, false);
		account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
		account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, false);
		account.PRESSED_KEYBOARD.put(KeyCode.D, false);
		account.PRESSED_KEYBOARD.put(KeyCode.U, false);
		account.PRESSED_KEYBOARD.put(KeyCode.DOWN, false);
		account.PRESSED_KEYBOARD.put(KeyCode.UP, false);
		
		itemIDColumn.setCellValueFactory(new PropertyValueFactory<>("client_item_number"));
		shortDescColumn.setCellValueFactory(new PropertyValueFactory<>("short_description"));
		longDescColumn.setCellValueFactory(new PropertyValueFactory<>("long_description"));
		mgColumn.setCellValueFactory(new PropertyValueFactory<>("Material_group"));
		preclassifColumn.setCellValueFactory(new PropertyValueFactory<>("Preclassifiation"));
		//classifColumn.setCellValueFactory(new PropertyValueFactory<>("segment_name"));
		sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source_Display"));
		ruleColumn.setCellValueFactory(new PropertyValueFactory<>("rule_description_Display"));
		//authorColumn.setCellValueFactory(new PropertyValueFactory<>("Author"));
		reviewedClassificationColumn.setCellValueFactory(new PropertyValueFactory<>("Display_segment_name"));
		reviewerColumn.setCellValueFactory(new PropertyValueFactory<>("Reviewer_Display"));
		
		tableGrid.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
            	//scrollEvent.consume();
            }     
		});
		//$
		tableGrid.getSortOrder().addListener((ListChangeListener)(c -> {
			saveSortOrder();
			tableGrid.scrollTo(tableGrid.getSelectionModel().getSelectedIndex());
			}));
		
		
		
		tableGrid.setOnKeyPressed(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                handleKeyBoardEvent(keyEvent,true);
            }
        });
         
		tableGrid.setOnKeyReleased(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                handleKeyBoardEvent(keyEvent,false);
            }
        });
		
		
		
		tableGrid.getSelectionModel().setSelectionMode(
			    SelectionMode.MULTIPLE
			);
		
		
		
		if(GlobalConstants.MANUAL_FETCH_ALL) {
			tableGrid.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			    if(newValue && traverseGridFocus) {
			    	;
			    	traverseGridFocus=false;
			    	Parent.classification.requestFocus();
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
			    	ItemFetcherRow tmp = (ItemFetcherRow) tableGrid.getItems().get(max_selected);
			    	proposer.loadPropositionsFor(tmp);
			    	
			    	
			    	Parent.aidLabel.setText("Article ID: "+tmp.getClient_item_number());
			    	Parent.sd.setText(tmp.getShort_description()+"\n\n\n\n\n");
			    	Parent.ld.setText(tmp.getLong_description()+"\n\n\n\n\n");
			    	Parent.material_group.setText(tmp.getMaterial_group());
			    	Parent.preclassification.setText(tmp.getPreclassifiation());
					Parent.classification.setText(tmp.getDisplay_segment_name()!=null?tmp.getDisplay_segment_name():"");
					Parent.classification_method.setText(tmp.getSource_Display());
					Parent.classification_rule.setText(tmp.getRule_description_Display());
					Parent.fireClassScroll(0, null);
					Parent.classification.requestFocus();
					traverseGridFocus=true;
					try {
						item_selection_routine(tmp);
					} catch (IOException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	
			    }else {
			    	Parent.counterSelection.setVisible(false);
			    }
			 });
		}else {
			tableGrid.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			    if (newSelection != null && check_selection ) {
			    	
			    	
			    	check_selection=false;
			        
			        int min_selected = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
			        int max_selected = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
			        
			        int current_index = (int) Math.ceil( (min_selected + max_selected)*0.5 );
			        
			        
			        ItemFetcherRow current_item = (ItemFetcherRow) tableGrid.getItems().get(current_index);
			        ;
			        if(current_item.getItem_id()!=old_item_id) {
			        	
			        	
			        	
			        	int old_item_index = ftc.orderedItems.indexOf(old_item_id);
			        	int new_item_index = ftc.orderedItems.indexOf(current_item.getItem_id());
			        	
			        	
			        	int scrolled_lines = (int) old_item_index - new_item_index;
			        	
				        ;
			        	
			        	
			        	
				        Platform.runLater(new Runnable() {
				        	  @Override public void run() {
				  		        	    
				     
							        try {
							        	
							        	if(scrolled_lines<0) {
							            	try {
							    				ftc.add_bottom( Math.abs(scrolled_lines), tvX, oq, tableGrid);
							    			} catch (ClassNotFoundException | SQLException e) {
							    				// TODO Auto-generated catch block
							    				e.printStackTrace();
							    			}
							            }else {
							            	try {
							            		ftc.add_top( Math.abs(scrolled_lines), tvX, oq, tableGrid);
							    			} catch (ClassNotFoundException | SQLException e) {
							    				// TODO Auto-generated catch block
							    				e.printStackTrace();
							    			}
							            }
							        	
							        	
							        	//old_item_id = ftc.orderedItems.get(ftc.orderedItems.indexOf(current_item.getItem_id())-scrolled_lines);
							        	old_item_id = ftc.orderedItems.get(ftc.orderedItems.indexOf(current_item.getItem_id()));
							        	for(ItemFetcherRow item:oq) {
							        		if(item.getItem_id().equals(old_item_id)) {
							        			;
							        		}
							        	}
							        	
							        	
							        	tableGrid.getSelectionModel().clearSelection();
							        	
							        	//tableGrid.getSelectionModel().selectRange(min_selected+Math.abs(scrolled_lines)+1, max_selected+Math.abs(scrolled_lines)+1+1);
							        	if(scrolled_lines>=0) {
							        		tableGrid.getSelectionModel().selectRange(min_selected+ scrolled_lines +1, max_selected+ scrolled_lines +1+1);
								        	
							        	}else {
							        		tableGrid.getSelectionModel().selectRange( min_selected+ scrolled_lines -1,max_selected+ scrolled_lines);
								        	
							        	}
							        	
							        	//tableGrid.getSelectionModel().select(max_selected-scrolled_lines);
							        	//tableGrid.getSelectionModel().select(0);
							        	
							        }catch(Exception V) {
							        	old_item_id = current_item.getItem_id();
							        }
							        
							        check_selection=true;     
				        }
			        	});
				        //
				        //tableGrid.getSelectionModel().selectRange(min_selected+scrolled_lines-1, max_selected+scrolled_lines-1);
				       
			        }
			        
			        
			    }else {
			    	
			    }
			});
		}
		
		
		 
	}
	*/
	protected void handleKeyBoardEvent(KeyEvent keyEvent, boolean pressed) {
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
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.D)) {
			//fireClassDown();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.U)) {
			//fireClassUp();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
			//fireScrollNBDown();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
			//fireScrollNBUp();
		}
	}
	/*
	public void fireScrollNBDown() {
		;
		int max = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		String cid_this = ((ItemFetcherRow) tableGrid.getItems().get(max)).getDisplay_segment_id();
		String cid_next = ((ItemFetcherRow) tableGrid.getItems().get(max+1)).getDisplay_segment_id();
		
		if(cid_this!=null && cid_next!=null) {
			try {
			while(((ItemFetcherRow) tableGrid.getItems().get(max+1)).getDisplay_segment_id() != null ) {
					max+=1;
				}
			}catch(Exception V) {
				max = tableGrid.getItems().size()-1;
			}
			tableGrid.getSelectionModel().clearAndSelect(max);
			if(max > tvX.getLastVisibleIndex()) {
				tableGrid.scrollTo(max-(tvX.getLastVisibleIndex()-tvX.getFirstVisibleIndex())+3);
				
			}
			}else {
			try {
				while(! ( ((ItemFetcherRow) tableGrid.getItems().get(max+1)).getDisplay_segment_id() != null ) ) {
					max+=1;
				}
			}catch(Exception V) {
				//Last item reached
			}
			
			tableGrid.getSelectionModel().clearAndSelect(max+1);
			if(max+1 > tvX.getLastVisibleIndex() ){
				tableGrid.scrollTo(max+1-(tvX.getLastVisibleIndex()-tvX.getFirstVisibleIndex())+3);
			}
			
		}
	}
	public void fireScrollNBUp() {
		;
		try {
			
			int min = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
			String cid_this = ((ItemFetcherRow) tableGrid.getItems().get(min)).getDisplay_segment_id();
			String cid_previous = ((ItemFetcherRow) tableGrid.getItems().get(min-1)).getDisplay_segment_id();
			
			if(cid_this!=null && cid_previous!=null) {
				while(((ItemFetcherRow) tableGrid.getItems().get(min-1)).getDisplay_segment_id() != null ) {
					min-=1;
				}
				tableGrid.getSelectionModel().clearAndSelect(min);
				if(min<tvX.getFirstVisibleIndex()) {
					tableGrid.scrollTo(min);
				}
			}else {
				while(! ( ((ItemFetcherRow) tableGrid.getItems().get(min-1)).getDisplay_segment_id() != null ) ) {
					min-=1;
				}
				tableGrid.getSelectionModel().clearAndSelect(min-1);
				if(min-1<tvX.getFirstVisibleIndex()) {
					tableGrid.scrollTo(min-1);
				}
			}
		}catch(Exception V) {
			
		}
		
	}
	public void fireClassDown() {
		int idx = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
		String result;
		if(oa.get(idx).getDisplay_segment_id()!=null) {
			result = oa.get(idx).getDisplay_segment_id()+"&&&"+oa.get(idx).getDisplay_segment_name();
		}else {
			result = null;
		}
		fireManualClassChange(result,false);
	}
	public void fireClassUp() {
		int idx = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		String result;
		if(oa.get(idx).getDisplay_segment_id()!=null) {
			result = oa.get(idx).getDisplay_segment_id()+"&&&"+oa.get(idx).getDisplay_segment_name();
		}else {
			result = null;
		}
		fireManualClassChange(result,false);
	}
	*/
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
		return Translator.translate("", this.user_language_gcode, description);
	}
	void scrolled(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        newValue.doubleValue();
        oldValue.doubleValue();
        
        
        
        
    }

	
	public void setUserLanguageGcode(String user_language_gcode) {
		this.user_language_gcode = user_language_gcode;
	}

	public void setParent(Char_description char_description,CharClassifProposer proposer) {
		;
		this.Parent=char_description;
		;
		proposer.setParent(char_description);
		Parent.propose(proposer.propositions,null,null,null,null);
	}
	/*
	public List<ItemFetcherRow> fireRuleClassBlank(ArrayList<String> itemsToBlank) {
		List<ItemFetcherRow> databaseSyncList = new ArrayList<ItemFetcherRow>();
		for( Object row : tableGrid.getItems() ) {
			
			if( itemsToBlank.contains( ((ItemFetcherRow)row).getItem_id()) ) {
					((ItemFetcherRow)row).setReviewer_Rules(null);
					//((ItemFetcherRow)row).setNew_segment_id(null);
					//((ItemFetcherRow)row).setNew_segment_name(null);
					((ItemFetcherRow)row).setRule_Segment_id(null);
					((ItemFetcherRow)row).setRule_Segment_name(null);
					((ItemFetcherRow)row).setRule_Segment_number(null);
					((ItemFetcherRow)row).setRule_description_Rules(null);
					((ItemFetcherRow)row).setSource_Rules(null);
					
				//Add items to the list to be pushed in the database
				
				databaseSyncList.add(((ItemFetcherRow)row));
				//Tools.ItemFetcherRow2ClassEvent(this,databaseSyncList,account);
			}else {
			continue;
			}
		}
		
		return databaseSyncList;
		
	}
	
	public List<ItemFetcherRow> fireRuleClassChange(HashMap<String,GenericRule> itemsToUpdate) {
		
		List<ItemFetcherRow> databaseSyncList = new ArrayList<ItemFetcherRow>();
		
		for( Object row : tableGrid.getItems() ) {
			
			if( itemsToUpdate.containsKey( ((ItemFetcherRow)row).getItem_id()) ) {
				GenericRule gr = itemsToUpdate.get(((ItemFetcherRow)row).getItem_id());
				((ItemFetcherRow)row).setReviewer_Rules(account.getUser_name());
				try{
					((ItemFetcherRow)row).setRule_Segment_id(gr.classif.get(0));
					((ItemFetcherRow)row).setRule_Segment_name(gr.classif.get(2));
					if(UUID2CID!=null) {
						
					}else {
						UUID2CID = Tools.UUID2CID(account.getActive_project());
					}
					((ItemFetcherRow)row).setRule_Segment_number(UUID2CID.get(gr.classif.get(1)));
					
				}catch(Exception V) {
					V.printStackTrace(System.err);
					//((ItemFetcherRow)row).setNew_segment_id(null);
					//((ItemFetcherRow)row).setNew_segment_name(null);
					((ItemFetcherRow)row).setRule_Segment_id(null);
					((ItemFetcherRow)row).setRule_Segment_name(null);
					((ItemFetcherRow)row).setRule_Segment_number(null);
				}
				((ItemFetcherRow)row).setSource_Rules(ClassificationMethods.USER_RULE);
				((ItemFetcherRow)row).setRule_description_Rules(gr.toString());
				
				//Add items to the list to be pushed in the database
				
				databaseSyncList.add(((ItemFetcherRow)row));
				//Tools.ItemFetcherRow2ClassEvent(this,databaseSyncList,account);
			}else {
			continue;
			}
		}
		return databaseSyncList;
	}
	*/
	public void fireManualClassChange(String result,boolean jumpNext) throws ClassNotFoundException, SQLException {
		
		List<CharDescriptionRow> databaseSyncList = new ArrayList<CharDescriptionRow>();
		HashMap<CharDescriptionRow,String> itemPreviousClasses = new HashMap<CharDescriptionRow,String>();
		for( Integer idx: (List<Integer>) tableGrid.getSelectionModel().getSelectedIndices()) {
			CharDescriptionRow item = ((TableView<CharDescriptionRow>) this.tableGrid).getItems().get(idx);
			itemPreviousClasses.put(item, item.getClass_segment());
			
			try{
				item.setClass_segment(result);
				
			}catch(Exception V) {
				item.setClass_segment(null);
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
		assignCharacteristicsToClass(target_class_id);
		assignValuesToItemsByClass_V2(target_class_id,target_class_name,itemList.stream().
												map(i -> i.getItem_id()).collect(Collectors.toList()));
		allignEmptyNewValuesOnOldOnes(itemPreviousClasses);
	}
	private void allignEmptyNewValuesOnOldOnes(HashMap<CharDescriptionRow, String> itemPreviousClasses) {
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
	/*
	public void fireClassSelection(int rowIndex, KeyCode kc) {
		if(GlobalConstants.MANUAL_FETCH_ALL) {
			return;
		}
		int min_selected = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
        int max_selected = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
        if(min_selected==-1) {
        	tableGrid.getSelectionModel().select(rowIndex);
        	fireScrollUpdate(kc);
        	return;
        }
        if(account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
        	tableGrid.getSelectionModel().selectRange(Math.min(min_selected, rowIndex), Math.max(max_selected, rowIndex)+1);
        	fireScrollUpdate(kc);
        	return;
        }
        if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL)) {
        	tableGrid.getSelectionModel().select(rowIndex);
        	fireScrollUpdate(kc);
        	return;
        }
        tableGrid.getSelectionModel().clearAndSelect(rowIndex);
        fireScrollUpdate(kc);
        
        
        
	}
	private void fireScrollUpdate(KeyCode kc) {
		int min_selected = (int) Collections.min(tableGrid.getSelectionModel().getSelectedIndices());
        int max_selected = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
        if(!(kc!=null)) {
        	return;
        }
        if(kc.equals(KeyCode.DOWN)) {
        	if(max_selected>tvX.getLastVisibleIndex()) {
        		tableGrid.scrollTo(tvX.getFirstVisibleIndex()+2);
        		;
        	}
        }else if (min_selected<tvX.getFirstVisibleIndex()) {
        	;
        	tableGrid.scrollTo(tvX.getFirstVisibleIndex()-1);
        }
        
	}
	*/
	public void setUserAccount(UserAccount account) {
		this.account=account;
		this.advancement = new CharAdvancementUpdater();
		advancement.setParentScene(this.Parent);
		advancement.account=account;
		advancement.refresh();
    	
		
	}
	/*
	public void fireClassScroll(int newRowIndex, KeyCode kc) {
		try{
			fireClassSelection(newRowIndex,kc);
			Parent.classification.setText(Parent.classification.getText()+"");
			Parent.classification.requestFocus();
			;
			//( (ItemFetcherRow) tableGrid.getItems().get(newRowIndex)).getNew_segment_name().requestFocus();
			
		}catch(Exception V) {
			
		}
		
	}
	*/
	public void collapseGrid(boolean visibleRight, GridPane parentGrid) {
		selectChartAtIndex(this.selected_col,visibleRight);
		
		
	}
	/*
	public void skipClassification() {
		;
		try {
			ItemFetcherRow row = ( (ItemFetcherRow)tableGrid.getSelectionModel().getSelectedItem() );
			String nouvel = Parent.classification.getText();
			String ancien = row.getDisplay_segment_name();
			
			if(nouvel.replace(" ", "").length()==0) {
				if(ancien!=null && ancien.length()>0) {
					;
					Parent.fireClassChange(null);
				}else {
					;
					
					jumpNext();
					
				}
			}else {
				if ( ancien.equals(nouvel) ) {
					;
					
					String result = row.getDisplay_segment_id()+"&&&"+row.getDisplay_segment_name();
					Parent.fireClassChange(result);
				}else {
					;
					
					Parent.classification.setText("");
					jumpNext();
					}
				
				}
			
			
		}catch(Exception V) {
			V.printStackTrace(System.err);
			jumpNext();
		}
	}
	
	private void saveSortOrder() {
		
		account.setManualSortColumns( new ArrayList<>() );
		account.setManualSortDirs( new ArrayList<>() );

	    for (TableColumn<ItemFetcherRow, ?> c : getSortOrder()) {
	    	account.getManualSortColumns().add(c.getText());
	    	account.getManualSortDirs().add(c.getSortType().toString());
	    }
	    
	    System.out.println("saving order");
	    try {
			transversal.data_exchange_toolbox.ComplexMap2JdbcObject.saveAccountProjectPreference(account);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

	private List<TableColumn<ItemFetcherRow, ?>> getSortOrder() {
		return tableGrid.getSortOrder();
	}
	
	private void applySortOrder(ArrayList<String> sortCols, ArrayList<String> sortDirs) {
	    List<TableColumn<ItemFetcherRow, ?>> sortOrder = getSortOrder();
	    sortOrder.clear();

	    for (int ix = 0; ix < sortCols.size(); ix++) {
	        for (TableColumn<ItemFetcherRow, ?> c : unchangedColumns) {
	            if (c.getText().equals(sortCols.get(ix))) {
	                sortOrder.add(c);
	                c.setSortType(TableColumn.SortType.valueOf(sortDirs.get(ix)));
	            }
	        }
	    }
	}
	*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void refresh_table_with_segment(String active_class) throws ClassNotFoundException, SQLException {
		
		
		//Set the static variable for CharacteristicValue
		CharacteristicValue.dataLanguage=this.Parent.data_language;
		CharacteristicValue.userLanguage=this.Parent.user_language;
			
		tvX = new TableViewExtra(tableGrid);
		
		this.classItems = getActiveItemsID(active_class);
		
		assignCharacteristicsToClass(active_class);
		
		fetchTableItems(active_class,classItems);
		
		assignValuesToItemsByClass_V2(active_class,this.Parent.classCombo.getSelectionModel().getSelectedItem().getclassName(),classItems);
		
		//Set the static variable for CharacteristicValue
		CharacteristicValue.dataLanguage=this.Parent.data_language;
		CharacteristicValue.userLanguage=this.Parent.user_language;
		
		
		
		fillTable();
		this.selected_col = -1;
		selectedLatestEditedItem();
		nextChar();
	}
	
	
	private  List<String> getActiveItemsID(String active_class) throws ClassNotFoundException, SQLException {
		String joinStatement = "";
		HashMap<String, String> classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(joinStatement,Tools.get_project_granularity(account.getActive_project()),account.getActive_project());
		List<String> classItems = classifiedItems.entrySet().stream().filter(m->m.getValue().endsWith(active_class)).map(Entry::getKey).collect(Collectors.toList());
		return classItems;
	}
	
	private void assignCharacteristicsToClass(String target_class_id) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("select * from (select characteristic_id,sequence,isCritical,allowedValues,allowedUoms,isActive from "+account.getActive_project()+".project_characteristics_x_segments where segment_id = ?) chars inner join "+account.getActive_project()+".project_characteristics on chars.characteristic_id = project_characteristics.characteristic_id order by sequence asc");
		stmt.setString(1, target_class_id);
		System.out.println(stmt.toString());
		ResultSet rs=stmt.executeQuery();
		
		this.active_characteristics.put(target_class_id, new ArrayList<ClassCharacteristic>());
		while(rs.next()) {
			ClassCharacteristic tmp = new ClassCharacteristic();
			tmp.setCharacteristic_id(rs.getString("characteristic_id"));
			tmp.setSequence(rs.getInt("sequence"));
			tmp.setIsCritical(rs.getBoolean("isCritical"));
			try{
				tmp.setAllowedValues(((String[]) rs.getArray("allowedValues").getArray()));
			}catch(Exception V) {
			}
			try {
				tmp.setAllowedUoms(((String[]) rs.getArray("allowedUoms").getArray()));
			}catch(Exception V) {
			}
			tmp.setIsActive(rs.getBoolean("isActive"));
			
			tmp.setCharacteristic_name(rs.getString("characteristic_name"));
			tmp.setCharacteristic_name_translated(rs.getString("characteristic_name_translated"));
			tmp.setIsNumeric(rs.getBoolean("isNumeric"));
			tmp.setIsTranslatable(rs.getBoolean("isTranslatable"));
			
			this.active_characteristics.get(target_class_id).add(tmp);
			loadAllowedValuesAsKnownValues(tmp);
			
		}
		rs.close();
		stmt.close();
		conn.close();
		
		try {
			for(CharDescriptionRow row:this.itemArray) {
				row.addDataField(target_class_id,active_characteristics.get(target_class_id).size());
			}
		}catch(Exception V) {
			//The items haven't been initialized yet, this means the items are being fetched for the first time
			System.out.println("Row data init error: No items");
		}
		
	}
	
	
	
	private void loadAllowedValuesAsKnownValues(ClassCharacteristic tmp) throws ClassNotFoundException, SQLException {
		if(tmp.getAllowedValues()!=null) {
			Connection conn = Tools.spawn_connection();
			String values_statement = "";
			for(int i=0;i<tmp.getAllowedValues().size();i++) {
				values_statement=values_statement+"?";
				if(i!=tmp.getAllowedValues().size()-1) {
					values_statement=values_statement+",";
				}
			}
			PreparedStatement stmt = conn.prepareStatement(
			"select * from "+account.getActive_project()+".project_values where value_id in ("+values_statement+")");
			for(int i=0;i<tmp.getAllowedValues().size();i++) {
				stmt.setString(i+1, tmp.getAllowedValues().get(i));
			}
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				CharacteristicValue val = new CharacteristicValue();
				val.setValue_id(rs.getString("value_id"));
				val.setText_values(rs.getString("text_values"));
				val.setNominal_value(rs.getString("nominal_value"));
				val.setMin_value(rs.getString("min_value"));
				val.setMax_value(rs.getString("max_value"));
				val.setNote(rs.getString("note"));
				val.setUom_id(rs.getString("uom_id"));
				val.addThisValuetoKnownValues(tmp);
			}
			
			
		}
	}
	private void fetchTableItems(String active_class, List<String> classItems) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt;
		ResultSet rs;
		
		stmt = conn.prepareStatement("select item_id, client_item_number, short_description,short_description_translated, long_description,long_description_translated from "+account.getActive_project()+".project_items where item_id in ('"+String.join("','", classItems)+"')");
		rs = stmt.executeQuery();
		try {
			this.itemArray.clear();
			System.gc();
		}catch(Exception V) {
			this.itemArray = new ArrayList<CharDescriptionRow>();
		}
		while(rs.next()) {
			CharDescriptionRow tmp = new CharDescriptionRow(active_class,active_characteristics.get(active_class).size());
			tmp.setItem_id(rs.getString("item_id"));
			tmp.setClient_item_number(rs.getString("client_item_number"));
			System.out.println(rs.getString("client_item_number"));
			tmp.setShort_desc(rs.getString("short_description"));
			tmp.setShort_desc_translated(rs.getString("short_description_translated"));
			tmp.setLong_desc(rs.getString("long_description"));
			tmp.setLong_desc_translated(rs.getString("long_description_translated"));
			this.itemArray.add(tmp);
		}
		rs.close();
		stmt.close();
		conn.close();
	}
	
	@SuppressWarnings("unused")
	private void assignValuesToItemsByClass_V1(String target_class_id, String target_class_name, List<String> target_items) throws ClassNotFoundException, SQLException {
		System.out.println("assigning values to items by class, no items "+target_items.size());
		
				
		
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt;
		ResultSet rs;
		
		stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,project_values.value_id,text_values,nominal_value,min_value,max_value,note,uom_id from "
				+ "(select * from "+account.getActive_project()+".project_items_x_values where item_id in ('"+String.join("','", target_items)+"')"
						+ "and characteristic_id in ('"+String.join("','", this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toSet()))+"')"
								+ ") data left join "+account.getActive_project()+".project_values "
										+ "on data.value_id = project_values.value_id");
		System.out.println(stmt.toString());
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
			val.setText_values(rs.getString("text_values"));
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
	private void assignValuesToItemsByClass_V2(String target_class_id, String target_class_name, List<String> target_items) throws ClassNotFoundException, SQLException {
		System.out.println("assigning values to items by class, no items "+target_items.size());
		
				
		
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt;
		ResultSet rs;
		
		stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,project_values.value_id,text_values,nominal_value,min_value,max_value,note,uom_id from "
				+ "(select * from "+account.getActive_project()+".project_items_x_values where characteristic_id in ('"+String.join("','", this.active_characteristics.get(target_class_id).stream().map(c->c.getCharacteristic_id()).collect(Collectors.toSet()))+"')"
								+ ") data left join "+account.getActive_project()+".project_values "
										+ "on data.value_id = project_values.value_id");
		System.out.println(stmt.toString());
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
			val.setText_values(rs.getString("text_values"));
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
					//keep full view columns visible if not in collasped views , hide otherwise
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
                        return new ReadOnlyObjectWrapper(r.getValue().getData(Parent.classCombo.getSelectionModel().getSelectedItem().getClassSegment())[dataIndex].getDisplayValue());
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
                handleKeyBoardEvent(keyEvent,true);
            }
        });
         
		tableGrid.setOnKeyReleased(new EventHandler<KeyEvent>() 
        {
            public void handle(final KeyEvent keyEvent) 
            {
                handleKeyBoardEvent(keyEvent,false);
            }
        });
		
		
		
		tableGrid.getSelectionModel().setSelectionMode(
			    SelectionMode.MULTIPLE
			);
		
		
		tableGrid.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    if(newValue && traverseGridFocus) {
		    	;
		    	traverseGridFocus=false;
		    	Parent.classification.requestFocus();
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
		    	Parent.proposer.loadPropositionsFor(tmp);
		    	
		    	
		    	Parent.aidLabel.setText("Article ID: "+tmp.getClient_item_number());
		    	Parent.sd.setText(tmp.getShort_desc()+"\n\n\n\n\n");
		    	Parent.ld.setText(tmp.getLong_desc()+"\n\n\n\n\n");
		    	//Parent.classification.setText(tmp.getDisplay_segment_name()!=null?tmp.getDisplay_segment_name():"");
				Parent.classification.requestFocus();
				traverseGridFocus=true;
				item_selection_routine(tmp);
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
