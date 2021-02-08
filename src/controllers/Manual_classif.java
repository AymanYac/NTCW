package controllers;

import controllers.paneControllers.Browser_ManualClassif;
import controllers.paneControllers.ImagePane_ManualClassif;
import controllers.paneControllers.RulePane_ManualClassif;
import controllers.paneControllers.TablePane_ManualClassif;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import model.*;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.json.simple.parser.ParseException;
import service.ItemFetcher;
import service.ManualClassifContext;
import service.ManualClassifProposer;
import service.ManualRuleServices;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class Manual_classif {

	public UserAccount account;

	
	private String user_language_gcode;

	private ItemFetcher ftc;

	public TablePane_ManualClassif tableController;

	public boolean CHANGING_CLASS = false;

	public ImagePane_ManualClassif imagePaneController;

	private Browser_ManualClassif browserController;

	public RulePane_ManualClassif rulePaneController;
	
	
	private boolean toggleSwitch=true;

	private GridPane imageGrid;
	private GridPane ruleGrid;
	
	public ArrayList<Button> propButtons;
	
	
	@FXML MenuBar menubar;
	@FXML public Menu counterTotal;
	@FXML public Menu counterRemaining;
	@FXML public Menu counterDaily;
	@FXML public Menu counterSelection;
	
	@FXML GridPane grid;
	
	@FXML public AnchorPane leftAnchor;
	@FXML public AnchorPane rightAnchor;
	@FXML public Label aidLabel;
	@FXML public TextArea sd;
	@FXML public TextArea sd_translated;
	@FXML public TextArea ld;
	@FXML public TextArea ld_translated;
	@FXML public TextField material_group;
	@FXML public TextField preclassification;
	public AutoCompleteBox_ManualClassification classification;
	@FXML public TextField classification_style;
	@FXML public TextField classification_method;
	@FXML public TextField classification_rule;
	@FXML public TextField search_text;
	
	@FXML ToolBar toolBar;
	@FXML Button paneToggle;
	@FXML Button classDDButton;
	@FXML Button exportButton;
	@FXML ToggleButton googleButton;
	@FXML ToggleButton tableButton;
	@FXML ToggleButton taxoButton;
	@FXML public ToggleButton imageButton;
	@FXML public ToggleButton rulesButton;
	
	@FXML Button prop1;
	@FXML Button prop2;
	@FXML Button prop3;
	@FXML Button prop4;
	@FXML Button prop5;
	@FXML Label pcLabel;
	@FXML Label lcLabel;
	


	public ManualClassifProposer proposer;


	public ManualClassProposition lcProp;


	public ManualClassProposition pcProp;


	public ManualClassifContext context;


	private String lastRightPane="";
	
	
	@FXML void nextBlank() {
		tableController.fireScrollNBDown();
	}
	@FXML void previousBlank() {
		tableController.fireScrollNBUp();
		}
	@FXML void firstBlank() {
		;
		//tableController.tableGrid.scrollTo(0);
		tableController.tableGrid.getSelectionModel().clearAndSelect(0);
		tableController.fireScrollNBDown();
		tableController.tableGrid.scrollTo(tableController.tableGrid.getSelectionModel().getSelectedIndex()+1);
		tableController.tableGrid.getSelectionModel().clearAndSelect(tableController.tableGrid.getSelectionModel().getSelectedIndex()+1);

	}
	@FXML void lastBlank() {
		;
		int max_index = tableController.tableGrid.getItems().size() - 1;
		//tableController.tableGrid.scrollTo(max_index);
		tableController.tableGrid.getSelectionModel().clearAndSelect(max_index);
		tableController.fireScrollNBUp();
		tableController.tableGrid.scrollTo(tableController.tableGrid.getSelectionModel().getSelectedIndex()-1);
		tableController.tableGrid.getSelectionModel().clearAndSelect(tableController.tableGrid.getSelectionModel().getSelectedIndex()-1);

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
		tableController.fireClassDown();
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	@FXML void export() throws SQLException, ClassNotFoundException {
		FileChooser fileChooser = new FileChooser();
		
		
		
		Date instant = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat( "MMMMM_dd" );
	    String time = sdf.format( instant );
	    
	    String PROJECT_NAME;
	    Connection conn = Tools.spawn_connection_from_pool();
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("select project_name from projects where project_id='"+account.getActive_project()+"'");
	    rs.next();
	    PROJECT_NAME = rs.getString("project_name");
	    rs.close();
	    
	    rs = stmt.executeQuery("select segment_id,level_"+Tools.get_project_granularity(account.getActive_project())+"_number from "+account.getActive_project()+".project_segments");
	    HashMap<String, String> UUID2CID = new HashMap<String,String>();
	    while(rs.next()) {
	    	UUID2CID.put(rs.getString("segment_id"),rs.getString(2));
	    }
	    UUID2CID.put("","");
	    rs.close();
	    stmt.close();
	    conn.close();
	    
		fileChooser.setInitialFileName(PROJECT_NAME+"_"+time);
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        
        if(file != null){
        	
        	try {
        		
        		
        		
        	SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
            Sheet sh = wb.createSheet("Project Items");
            
    		
    		int i =0;
    		Row row = sh.createRow(i);
    		
    		Cell cell = row.createCell(0);
    		cell.setCellValue("Client item number");
    		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); Font font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(0).setCellStyle(style);
    		 
    		 XSSFCellStyle general_header_style = (XSSFCellStyle) wb.createCellStyle();
    		 general_header_style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
    		 general_header_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 general_header_style.setFont(font);
    		 
    		 XSSFCellStyle general_content_style = (XSSFCellStyle) wb.createCellStyle();
    		 general_content_style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    		 general_content_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 general_content_style.setFont(font);
    		 Font general_content_font = wb.createFont();
    		 general_content_font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    		 general_content_font.setBold(false);
    		 general_content_style.setFont(general_content_font);
    		 
    		 
    		 
    		cell = row.createCell(1);
    		cell.setCellValue("Short description");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(1).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(2);
    		cell.setCellValue("Long description");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(2).setCellStyle(style);
    		 
    		 
    		 
    		 
    		cell = row.createCell(3);
    		cell.setCellValue("Material Group");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(3).setCellStyle(style);
    		 
    		 
    		 cell = row.createCell(4);
     		cell.setCellValue("Preclassification");
     		style = (XSSFCellStyle) wb.createCellStyle();
     		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
     		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
     		 row.getCell(4).setCellStyle(style);
     		
    		 
    		cell = row.createCell(5);
    		cell.setCellValue("Classification number");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(5).setCellStyle(style);
    		 
    		 
    		 cell = row.createCell(6);
     		cell.setCellValue("Classification name");
     		style = (XSSFCellStyle) wb.createCellStyle();
     		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
     		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
     		 row.getCell(6).setCellStyle(style);
     		 
     		 
     		cell = row.createCell(7);
    		cell.setCellValue("Source");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
   		 	style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
   		 	row.getCell(7).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(8);
    		cell.setCellValue("Rule");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
   		 	style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
   		 	row.getCell(8).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(9);
    		cell.setCellValue("Author");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
   		 	style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
   		 	row.getCell(9).setCellStyle(style);
    		 
    		 XSSFCellStyle classif_style = (XSSFCellStyle) wb.createCellStyle();
    		 classif_style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 classif_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 classif_style.setFont(font);
    		 
    		  
    		ObservableList<ItemFetcherRow> rws = tableController.tableGrid.getItems();
    		
    		CellStyle percentStyle = wb.createCellStyle();
    		percentStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
    		
    		
    		
    		for(ItemFetcherRow rw:rws) {
    			i=i+1;
    			ArrayList<String> result_row = new ArrayList<String>(10);
    			result_row.add(rw.getClient_item_number());
    			result_row.add(rw.getShort_description());
    			result_row.add(rw.getLong_description());
    			result_row.add(rw.getMaterial_group());
    			result_row.add(rw.getPreclassifiation());
    			result_row.add( rw.getDisplay_segment_number() );
    			result_row.add(rw.getDisplay_segment_name());
    			result_row.add(rw.getSource_Display());
    			result_row.add(rw.getRule_description_Display());
    			result_row.add(rw.getAuthor_Display());
    			
    			
    			
    			row = sh.createRow(i);
    	        for(int cellnum = 0; cellnum < 10; cellnum++){
    	            cell = row.createCell(cellnum);
    	            cell.setCellValue(result_row.get(cellnum));
    	            
    	        }
    			
    			
    			
    		}
    		sh.setColumnWidth(0,256*15);
    		sh.setColumnWidth(1,256*50);
    		sh.setColumnWidth(2,256*50);
    		sh.setColumnWidth(3,256*15);
    		sh.setColumnWidth(4,256*34);
    		sh.setColumnWidth(5,256*25);
    		sh.setColumnWidth(6,256*34);
    		sh.setColumnWidth(7,256*9);
    		sh.setColumnWidth(8,256*50);
    		sh.setColumnWidth(9,256*9);

    		
    		sh.setZoom(70);
    	
    		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
            wb.write(out);
            out.close();

            // dispose of temporary files backing this workbook on disk
            wb.dispose();
    		
            
    		ConfirmationDialog.show("File saved", "Results successfully saved in\n"+file.getAbsolutePath(), "OK");
    		
        	}catch(Exception V) {
        		V.printStackTrace(System.err);
        		ConfirmationDialog.show("File saving failed", "Results could not be saved in\n"+file.getAbsolutePath()+"\nMake sure you have the rights to create files in this folder and that the file is not open by another application", "OK");
        	}
        }
	}
	@FXML void initialize(){
		sd.setText("");
		sd_translated.setText("");
		ld.setText("");
		ld_translated.setText("");
		material_group.setText("");
		preclassification.setText("");
		
		taxoButton.setDisable(true);
		
		toolBarButtonListener();
		initializePropButtons();
		this.context = new ManualClassifContext();
		context.setParent(this);
		
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
		
		int foundSeparators = 0;
		for(Node node:toolBar.getItems()) {
			if(node instanceof Button) {
				((Button)node).setText("");
			}
			if(node instanceof ToggleButton) {
				((ToggleButton)node).setText("");
			}
			
			if(node instanceof Separator) {
				foundSeparators+=1;
			}else if(node instanceof ToggleButton && foundSeparators>GlobalConstants.MANUAL_CLASSIF_SEPARATOR) {
				if(foundSeparators>GlobalConstants.MANUAL_CLASSIF_SEPARATOR+1) {
					((ToggleButton) node).selectedProperty().addListener(((observable, oldValue, newValue) -> {
					   if(newValue && toggleSwitch) {
						   deselect_toolbar_before_seprator(false);
						   toggleSwitch = false;
							((ToggleButton) node).setSelected(true);
							toggleSwitch = true;
					   }
					}));
					
				}else {
					((ToggleButton) node).selectedProperty().addListener(((observable, oldValue, newValue) -> {
						   if(newValue && toggleSwitch) {
							   deselect_toolbar_before_seprator(true);
							   toggleSwitch = false;
								((ToggleButton) node).setSelected(true);
								toggleSwitch = true;
						   }
						}));
				}
			}
		}
		imageButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			   if(!newValue && toggleSwitch ) {
				   toggleSwitch = false;
				  try {
					  imagePaneController.imagePaneClose();
				  }catch(Exception V) {
					  
				  }
				  toggleSwitch = true;
			   }
			}));
		
		rulesButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			   if(!newValue && toggleSwitch ) {
				   toggleSwitch = false;
				  try {
					  rulePaneController.PaneClose();
				  }catch(Exception V) {
					  
				  }
				  toggleSwitch = true;
			   }
			}));
		
		
		
		/*
		googleButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			   if(!newValue && toggleSwitch ) {
				   toggleSwitch = false;
				  try {
					  show_table();
				  }catch(Exception V) {
					  
				  }
				  toggleSwitch = true;
			   }
			}));
		
		tableButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			   if(!newValue && toggleSwitch ) {
				   toggleSwitch = false;
				  try {
					  search_google_inplace();
				  }catch(Exception V) {
					  
				  }
				  toggleSwitch = true;
			   }
			}));*/
	}

	private void listen_for_keyboard_events() {
		account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
		account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, false);
		account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, false);
		account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
		account.PRESSED_KEYBOARD.put(KeyCode.R, false);
		account.PRESSED_KEYBOARD.put(KeyCode.P, false);
		
		
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
		if(keyEvent.getCode().equals(KeyCode.R)) {
			account.PRESSED_KEYBOARD.put(KeyCode.R, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.P)) {
			account.PRESSED_KEYBOARD.put(KeyCode.P, pressed);
		}
		
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.SHIFT) && account.PRESSED_KEYBOARD.get(KeyCode.ENTER)) {
			this.lastRightPane="";
			try {
				if(this.rulesButton.isSelected()) {
					this.lastRightPane="RULES";
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
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.ESCAPE)) {
			
			
			try {
				imagePaneController.imagePaneClose();
			}catch(Exception V) {
				
			}
			try {
				rulePaneController.PaneClose();
			}catch(Exception V) {
				
			}
			
			try {
				if(browserController.closeButton.isVisible()) {
					//We clicked escape to close search
					try {
						show_table();
					}catch(Exception V) {
						
					}
					if(this.lastRightPane.equals("RULES")) {
						rulesButton.setSelected(true);
						load_rule_pane();
					}
					if(this.lastRightPane.equals("IMAGES")) {
						imageButton.setSelected(true);
						load_image_pane(true);
					}
					this.lastRightPane="";
				}else {
					this.lastRightPane="";
				}
			}catch(Exception V) {
				this.lastRightPane="";
			}
			
			
			
		}
		
		if(account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && account.PRESSED_KEYBOARD.get(KeyCode.R)) {
			if(this.rulesButton.isSelected()) {
				this.rulePaneController.PaneClose();
			}else {
				rulesButton.setSelected(true);
				load_rule_pane();
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
		
		
	}

	private void deselect_toolbar_before_seprator(boolean b) {
		int foundSeparators = 0;
		for(Node node:toolBar.getItems()) {
			if(node instanceof Separator) {
				foundSeparators+=1;
			}else if(node instanceof ToggleButton && foundSeparators>GlobalConstants.MANUAL_CLASSIF_SEPARATOR) {
				if(foundSeparators>GlobalConstants.MANUAL_CLASSIF_SEPARATOR+1) {
					if(!b) {
						;
						((ToggleButton) node).setSelected(false);
					}
				}else {
					if(b) {
						;
						((ToggleButton) node).setSelected(false);
					}
				}
			}
		}
		classification.requestFocus();
	}

	private void launch_search(boolean checkMethodSelect) throws IOException, ParseException {
		load_image_pane(checkMethodSelect);
		search_google_inplace(checkMethodSelect);
	}
	
	@FXML void search_image() throws IOException, ParseException {
		load_image_pane(false);
	}
	
	@FXML void search_rule() throws IOException, ParseException, ClassNotFoundException, SQLException {
		load_rule_pane();
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
		Tools.decorate_menubar(menubar,account);
		decorate_menubar_with_classif_specific_menus();
		this.user_language_gcode = Tools.get_project_user_language_code(account.getActive_project());
		
		this.proposer = new ManualClassifProposer(account.getActive_project());
		this.ftc = new ItemFetcher(account.getActive_project(),this);
		//classification tmp.setNew_segment_name( CID2NAME,j,parent_controller);
		classification = new AutoCompleteBox_ManualClassification(this,classification_style.getStyle(),account);
		for( Entry<String, String> entry : ftc.CID2NAME.entrySet()) {
			classification.getEntries().add(entry.getKey()+"&&&"+entry.getValue());
		}
		classification_style.setVisible(false);
		grid.add(classification, 5, 9);
		this.context.CID2NAME = ftc.CID2NAME;
		
		
		load_table_pane();
		tableController.collapseGrid(false,grid);
		listen_for_keyboard_events();
		//load_image_pane();
		//load_rule_pane();
		//load_taxo_pane();
		
		
		
		
		
	}

	
	private void decorate_menubar_with_classif_specific_menus() {
		
		Menu manual_classif = menubar.getMenus().get(3);
		menubar.setOnMouseEntered(e->{
			manual_classif.show();
		});
		MenuItem reeval_rules_true = new MenuItem("Refresh rules on all items");
		Manual_classif parent = this;
		reeval_rules_true.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
					ManualRuleServices.reEvaluateAllActiveRules(true,parent,reeval_rules_true);
				} catch(Exception e) {
					e.printStackTrace(System.err);
				}
		    }
		});

		MenuItem reeval_rules_false = new MenuItem("Refresh rules on unclassified items");
		reeval_rules_false.setOnAction(new EventHandler<ActionEvent>() {
				    public void handle(ActionEvent t) {
				    	try {
				    		ManualRuleServices.reEvaluateAllActiveRules(false,parent,reeval_rules_false);
						} catch(Exception e) {
							e.printStackTrace(System.err);
						}
				    }
				});
		
		manual_classif.getItems().addAll(reeval_rules_true,reeval_rules_false);
		
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	private void load_table_pane() throws IOException {
		
		tableButton.setSelected(true);
		setBottomRegionColumnSpans(false);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/TablePane_ManualClassif.fxml"));
		GridPane tableGrid = loader.load();
		tableController = loader.getController();
		leftAnchor.getChildren().setAll(tableGrid);
		leftAnchor.setTopAnchor(tableGrid, 0.0);
		leftAnchor.setBottomAnchor(tableGrid, 0.0);
		leftAnchor.setLeftAnchor(tableGrid, 0.0);
		leftAnchor.setRightAnchor(tableGrid, 0.0);
		
		tableController.setParent(this,proposer);
		tableController.setUserAccount(account);
		
		tableController.setItemFetcher(ftc);
		tableController.setUserLanguageGcode(user_language_gcode);
		if(GlobalConstants.MANUAL_FETCH_ALL) {
			tableController.fillTable_STATIC((List<ItemFetcherRow>) ftc.currentList_STATIC);
		}else {
			tableController.fillTable_DYNAMIC((List<ItemFetcherRow>) ftc.currentList_DYNAMIC);
		}
		 System.gc();
		 Platform.runLater(new Runnable() {
	            @Override public void run() {
	            	tableController.listenTableScroll();
	            	
	            	/*try {
						TimeUnit.SECONDS.sleep(1);
						tableController.listenTableScroll();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
	        		
	            }
	        });
		
		
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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/ImagePane_ManualClassif.fxml"));
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
	
	@SuppressWarnings({ "static-access", "unused" })
	private void load_taxo_pane() throws IOException {
		taxoButton.setSelected(true);
		setBottomRegionColumnSpans(true);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/TaxoPane_ManualClassif.fxml"));
		AnchorPane TaxoGrid = loader.load();
		rightAnchor.getChildren().setAll(TaxoGrid);
		
		rightAnchor.setTopAnchor(TaxoGrid, 0.0);
		rightAnchor.setBottomAnchor(TaxoGrid, 0.0);
		rightAnchor.setLeftAnchor(TaxoGrid, 0.0);
		rightAnchor.setRightAnchor(TaxoGrid, 0.0);
		
	}
	
	@SuppressWarnings("static-access")
	public void load_rule_pane() throws IOException, ClassNotFoundException, SQLException {
		
		
		if(!rulesButton.isSelected()) {
			return;
		}
		
		setBottomRegionColumnSpans(true);
		if(rulePaneController!=null) {
			rightAnchor.getChildren().setAll(ruleGrid);
			rightAnchor.setTopAnchor(ruleGrid, 0.0);
			rightAnchor.setBottomAnchor(ruleGrid, 0.0);
			rightAnchor.setLeftAnchor(ruleGrid, 0.0);
			rightAnchor.setRightAnchor(ruleGrid, 0.0);
			
			rulePaneController.load_item_rules();
			;
			
		}else {
			;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/RulePane_ManualClassif.fxml"));
			ruleGrid = loader.load();
			rightAnchor.getChildren().setAll(ruleGrid);
			
			rightAnchor.setTopAnchor(ruleGrid, 0.0);
			rightAnchor.setBottomAnchor(ruleGrid, 0.0);
			rightAnchor.setLeftAnchor(ruleGrid, 0.0);
			rightAnchor.setRightAnchor(ruleGrid, 0.0);
			
			rulePaneController = loader.getController();
			rulePaneController.setParent(this);
			rulePaneController.load_item_rules();
			
		}
		
	}
	
	private void load_browser_pane() throws IOException {
		googleButton.setSelected(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/Browser_ManualClassif.fxml"));
		loader.load();
		browserController = loader.getController();
		browserController.setParent(this);
	}

	@SuppressWarnings("static-access")
	public void setBottomRegionColumnSpans(boolean visibleRight) {
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
		classification.requestFocus();
		
	}

	public void fireClassChange(String result) {
		this.CHANGING_CLASS=true;
		tableController.fireManualClassChange(result,true);
		this.CHANGING_CLASS=false;
	}

	public void fireClassSelection(int rowIndex, KeyCode kc) {
		tableController.fireClassSelection(rowIndex,kc);
	}

	public void fireClassScroll(int newRowIndex, KeyCode kc) {
		tableController.fireClassScroll(newRowIndex,kc);
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

	public void prepare_image_pane() {
		if(!imageButton.isSelected()) {
			return;
		}
		imagePaneController.prepare_images();
	}
	
	@FXML void context1() throws IOException {
		this.context.showContext(prop1,0);
	}
	@FXML void context2() throws IOException {
		this.context.showContext(prop2,1);
		}
	@FXML void context3() throws IOException {
		this.context.showContext(prop3,2);
	}
	@FXML void context4() throws IOException {
		this.context.showContext(prop4,3);
	}
	@FXML void context5() throws IOException {
		this.context.showContext(prop5,4);
	}
	public void propose(ArrayList<ManualClassProposition> propositions, ManualClassProposition pc, ManualClassProposition lc, ManualClassProposition mg, ManualClassProposition f5) {
		
		this.pcProp = pc;
		this.lcProp = lc;
		
		if(pcProp!=null) {
			pcLabel.setText("Pre-classification (Ctrl+Q)");
		}else {
			pcLabel.setText("Pre-classification");
		}
		if(lcProp!=null) {
			lcLabel.setText("Classification suggestions [ (Ctrl+W) for last used class ] :");
		}else {
			lcLabel.setText("Classification suggestions");
		}
		
		try {
			Iterator<ManualClassProposition> itr = propositions.iterator();
			for(int i=0;i<GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS_OLD;i++) {
				Button button = propButtons.get(i);
				button.setVisible(true);
				if(itr.hasNext()) {
					ManualClassProposition prop = itr.next();
					context.assignRecommendation(button,prop);
				}else {
					context.disableButton(button);
				}
			}
		}catch(Exception V) {
			for(int i=0;i<GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS_OLD;i++) {
				Button button = propButtons.get(i);
				context.disableButton(button);
			}
		}
		
		try {
			int i=3;
			Button button = propButtons.get(i);
			button.setVisible(true);
			if(mg!=null) {
				ManualClassProposition prop = mg;
				button.setText(prop.getSegment_name());
				button.setOpacity(1.0);
				button.setOnAction((event) -> {
					  fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
				});
			}else {
				context.disableButton(button);
			}
			
		}catch(Exception V) {
			V.printStackTrace(System.err);
			propButtons.get(3).setVisible(false);
			context.disableButton(propButtons.get(3));
		}
		
		try {
			int i=4;
			Button button = propButtons.get(i);
			button.setVisible(true);
			if(f5!=null) {
				ManualClassProposition prop = f5;
				button.setText(prop.getSegment_name());
				button.setOpacity(1.0);
				button.setOnAction((event) -> {
					  fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
				});
			}else {
				context.disableButton(button);
			}
			
		}catch(Exception V) {
			V.printStackTrace(System.err);
			context.disableButton(propButtons.get(4));
		}
		
		
	}
	public void fireProposition(int i) {
		;
		try {
			Button button = propButtons.get(i-1);
			if(!button.isVisible()) {
				return;
			}
			button.fire();
		}catch(Exception V) {
		}
	}
	
	public void firePropositionPreclass() {
		ManualClassProposition prop = null;
		if(pcProp!=null) {
			prop = pcProp;
		}else {
			return;
		}
		fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
	}
	public void firePropositionPreviousclass() {
		ManualClassProposition prop = null;
		if(lcProp!=null) {
			prop = lcProp;
		}else {
			return;
		}
		fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
	}
	
}
