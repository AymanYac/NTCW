package controllers.paneControllers;

import controllers.Manual_classif;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import model.*;
import org.json.simple.parser.ParseException;
import service.*;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TablePane_ManualClassif {
	
	@FXML TableColumn itemIDColumn;
	@FXML TableColumn shortDescColumn;
	@FXML TableColumn longDescColumn;
	@FXML TableColumn mgColumn;
	@FXML TableColumn preclassifColumn;
	//@FXML TableColumn classifColumn;
	@FXML TableColumn sourceColumn;
	@FXML TableColumn ruleColumn;
	//@FXML TableColumn authorColumn;
	@FXML TableColumn reviewedClassificationColumn;
	@FXML TableColumn reviewerColumn;
	@FXML public TableView<ItemFetcherRow> tableGrid;
	
	
	
	private ObservableDeque<ItemFetcherRow> oq;
	private ObservableList<ItemFetcherRow> oa;
	private TableViewExtra tvX;
	private ItemFetcher ftc;
	private boolean check_selection = true;
	private String old_item_id;
	private String user_language_gcode;
	private Manual_classif Parent;
	private boolean stop_translation;
	protected String translated_sd;
	protected String translated_ld;
	private Thread translationThread;
	private UserAccount account;
	private Task<Void> translationTask;
	private boolean traverseGridFocus;
	//public Connection liveConnection;
	private ManualClassifProposer proposer;
	private HashMap<String, String> UUID2CID;
	private ManualAdvancementUpdater advancement;
	private List<TableColumn<ItemFetcherRow, ?>> unchangedColumns ;

	
	@SuppressWarnings("unchecked")
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
		
		
		
		Connection conn = Tools.spawn_connection_from_pool();
		PreparedStatement stmt = conn.prepareStatement("select user_manual_propositions, "
				+ "user_manual_sorting_columns, user_manual_sorting_order"
				+ " from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		try {
			
			account.setManualPropositions(rs.getArray(1));
			
			account.setManualSortColumns(rs.getArray(2));
			
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
			ManualClassifContext.methods = account.getManualPropositions();
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
		for(ItemFetcherRow row: tableGrid.getItems()) {
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
			fireClassDown();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.U)) {
			fireClassUp();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
			//fireScrollNBDown();
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
			//fireScrollNBUp();
		}
	}
	
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
			scrollToSelectedItem(tableGrid.getSelectionModel().getSelectedItem());
		}else {
			try {
				while(! ( ((ItemFetcherRow) tableGrid.getItems().get(max+1)).getDisplay_segment_id() != null ) ) {
					max+=1;
				}
			}catch(Exception V) {
				//Last item reached
			}
			tableGrid.getSelectionModel().clearAndSelect(max+1);
			scrollToSelectedItem(tableGrid.getSelectionModel().getSelectedItem());
			
		}
	}

	private void scrollToSelectedItem(ItemFetcherRow selectedItem) {
		tvX.scrollToIndex(tableGrid.getItems().indexOf(selectedItem));
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
	
	@SuppressWarnings("deprecation")
	private void item_selection_routine(ItemFetcherRow tmp) throws IOException, ParseException {
		
		if(tmp.getLong_description().length()>0) {
			Parent.search_text.setText(WordUtils.getSearchWords(tmp.getLong_description()));
		}else {
			Parent.search_text.setText(WordUtils.getSearchWords(tmp.getShort_description()));
		}
		
		
		Parent.prepare_image_pane();
		
		
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
					
					Platform.runLater(new Runnable(){

						@Override
						public void run() {
							try {
								Parent.load_rule_pane();
							} catch (IOException | ClassNotFoundException | SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						});
					
					
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
		    	translated_sd = tmp.getShort_description_translated().replace(" ", "").length()>0?tmp.getShort_description_translated():translate2UserLanguage(tmp.getShort_description());
		    	translated_ld = tmp.getLong_description_translated().replace(" ", "").length()>0?tmp.getLong_description_translated():translate2UserLanguage(tmp.getLong_description());
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
			/*
			try {
				TimeUnit.MILLISECONDS.sleep(0);
				Parent.load_image_pane();
			} catch (IOException | ParseException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			*/
			
			});

		translationTask.setOnFailed(e -> {
		    Throwable problem = translationTask.getException();
		    //problem.printStackTrace();
		    problem.printStackTrace(System.err);
		    /* code to execute if task throws exception */
		    //problem.printStackTrace(System.err);
		});

		translationTask.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
		
		translationThread = new Thread(translationTask);; translationThread.setDaemon(true);
		translationThread.setName("Trnsl");
		translationThread.start();
		
		
		
		
		
	}
	protected String translate2UserLanguage(String description) throws IOException {
		if(description.replace(" ", "").length()==0) {
			return "";
		}
		return TranslationServices.webTranslate("", this.user_language_gcode, description);
	}
	void scrolled(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        newValue.doubleValue();
        oldValue.doubleValue();
        
        
        
    }

	public void setItemFetcher(ItemFetcher ftc) {
		this.ftc=ftc;
	}
	public void setUserLanguageGcode(String user_language_gcode) {
		this.user_language_gcode = user_language_gcode;
	}
	public void setParent(Manual_classif parent_controller,ManualClassifProposer proposer) {
		;
		this.Parent=parent_controller;
		;
		this.proposer = proposer;
		proposer.setParent(parent_controller);
		Parent.propose(proposer.propositions,null,null,null,null);
	}
	
	
	
	
	
	public void fireManualClassChange(String result,boolean jumpNext) {
		
		List<ItemFetcherRow> databaseSyncList = new ArrayList<ItemFetcherRow>();
		for( Integer idx: (List<Integer>) tableGrid.getSelectionModel().getSelectedIndices()) {
			try{
				oa.get(idx).setManual_segment_id(result.split("&&&")[0]);
				oa.get(idx).setManual_segment_name(result.split("&&&")[1]);
				if(UUID2CID!=null) {
					
				}else {
					UUID2CID = Tools.UUID2CID(account.getActive_project());
				}
				oa.get(idx).setManual_segment_number(UUID2CID.get(result.split("&&&")[0]));
				oa.get(idx).setAuthor_Manual(account.getUser_name());
				oa.get(idx).setReviewer_Manual(account.getUser_name());
				oa.get(idx).setRule_description_Manual(null);
				oa.get(idx).setRule_id_Manual(null);
				oa.get(idx).setSource_Manual(DataInputMethods.MANUAL);
				
				ManualClassProposition lastClassProp = new ManualClassProposition();
				lastClassProp.setSegment_id(result.split("&&&")[0]);
				lastClassProp.setSegment_name(result.split("&&&")[1]);
				
				lastClassProp.setExpectMore(false);
				lastClassProp.setProposer("LastClass");
				proposer.lastClassProp = lastClassProp;
				
			}catch(Exception V) {
				oa.get(idx).setManual_segment_id(null);
				oa.get(idx).setManual_segment_name(null);
				oa.get(idx).setManual_segment_number(null);
				oa.get(idx).setAuthor_Manual(null);
				oa.get(idx).setReviewer_Manual(null);
				oa.get(idx).setRule_description_Manual(null);
				oa.get(idx).setRule_id_Manual(null);
				oa.get(idx).setSource_Manual(null);
				
				oa.get(idx).setUpload_segment_id(null);
				oa.get(idx).setUpload_segment_name(null);
				oa.get(idx).setUpload_segment_number(null);
				oa.get(idx).setAuthor_Upload(null);
				oa.get(idx).setReviewer_Upload(null);
				oa.get(idx).setRule_description_Upload(null);
				oa.get(idx).setRule_id_Upload(null);
				oa.get(idx).setSource_Upload(null);
				
				oa.get(idx).setRule_Segment_id(null);
				oa.get(idx).setRule_Segment_name(null);
				oa.get(idx).setRule_Segment_number(null);
				oa.get(idx).setAuthor_Rules(null);
				oa.get(idx).setReviewer_Rules(null);
				oa.get(idx).setRule_description_Rules(null);
				oa.get(idx).setRule_id_Rules(null);
				oa.get(idx).setSource_Rules(null);
				
				
			}
			
			//Add items to the list to be pushed in the database
			
			databaseSyncList.add(oa.get(idx));
		}
		
		
		
		try{
			if(jumpNext) {
				jumpNext();
				
			}
		}catch(Exception V) {
			
		}
		tableGrid.refresh();
		Tools.ItemFetcherRow2ClassEvent(databaseSyncList,account,DataInputMethods.MANUAL);
		
		
	}
	private void jumpNext() {
		//Code to go to the next item if the user is not in rule pane mode
		try {
			if(this.Parent.rulesButton.isSelected()) {
				
				ItemFetcherRow current_row = ((ItemFetcherRow)tableGrid.getSelectionModel().getSelectedItem());
				ManualRuleServices.scrollEvaluateItem(current_row,this.Parent);
				
			}else {
				
			}
		}catch(Exception V) {
			
		}
		
		
		int currentIdx = (int) Collections.max(tableGrid.getSelectionModel().getSelectedIndices());
		tableGrid.getSelectionModel().clearAndSelect(1+ currentIdx);
		scrollToSelectedItem(tableGrid.getSelectionModel().getSelectedItem());
	}
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
	public void setUserAccount(UserAccount account) {
		this.account=account;
		this.advancement = new ManualAdvancementUpdater();
		advancement.setParentScene(this.Parent);
		advancement.account=account;
		advancement.refresh();
    	
		
	}
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
	public void collapseGrid(boolean visibleRight, GridPane parentGrid) {
		
		//itemIDColumn
		//shortDescColumn
		//longDescColumn
		//mgColumn
		//preclassifColumn
		//classifColumn
		//ruleColumn
		//sourceColumn
		//authorColumn
		
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		double availableWidth = primaryScreenBounds.getWidth()*0.185;
		double leftWidth = availableWidth * 3;
		double leftcolWidth = leftWidth * 0.2;
		double rightWidth = availableWidth *2;
		double rightcolWidth = rightWidth / 4.0;
		
		
		if(visibleRight) {
			itemIDColumn.setMinWidth(0);
			ruleColumn.setMinWidth(0);
			sourceColumn.setMinWidth(0);
			reviewerColumn.setMinWidth(0);
			
			//////////////
			
			itemIDColumn.setMaxWidth(0);
			ruleColumn.setMaxWidth(0);
			sourceColumn.setMaxWidth(0);
			reviewerColumn.setMaxWidth(0);
			
            
		}else {
			itemIDColumn.setMinWidth(0.5*rightcolWidth);
			ruleColumn.setMinWidth(2.5*rightcolWidth);
			sourceColumn.setMinWidth(0.5*rightcolWidth);
			reviewerColumn.setMinWidth(0.75*rightcolWidth);
			
			//////////////
			
			//itemIDColumn.setMaxWidth(0.5*rightcolWidth);
			ruleColumn.setMaxWidth(2.5*rightcolWidth);
			sourceColumn.setMaxWidth(0.5*rightcolWidth);
			reviewerColumn.setMaxWidth(0.5*rightcolWidth);
			
			
		}
		
		shortDescColumn.setMinWidth(1.5*leftcolWidth);
		longDescColumn.setMinWidth(1.5*leftcolWidth);
		mgColumn.setMinWidth(0.5*leftcolWidth);
		preclassifColumn.setMinWidth(0.75*leftcolWidth);
		reviewedClassificationColumn.setMinWidth(0.75*leftcolWidth);

		//////////////
		
		shortDescColumn.setMaxWidth(1.5*leftcolWidth);
		longDescColumn.setMaxWidth(1.5*leftcolWidth);
		mgColumn.setMaxWidth(0.5*leftcolWidth);
		preclassifColumn.setMaxWidth(0.75*leftcolWidth);
		reviewedClassificationColumn.setMaxWidth(0.75*leftcolWidth);
        
		
		
		
	}
	public void skipClassification() {
		account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
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
	    
	    
	    try {
			transversal.data_exchange_toolbox.ComplexMap2JdbcObject.saveAccountProjectPreferenceForClassification(account);
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
	

}
