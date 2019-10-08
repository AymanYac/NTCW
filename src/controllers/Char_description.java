package controllers;

import java.awt.Desktop;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.json.simple.parser.ParseException;

import controllers.paneControllers.Browser_CharClassif;
import controllers.paneControllers.CharPane_CharClassif;
import controllers.paneControllers.ImagePane_CharClassif;
import controllers.paneControllers.TablePane_CharClassif;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import model.GlobalConstants;
import model.CharClassProposition;
import model.AutoCompleteBox_CharClassification;
import model.AutoCompleteBox_UnitOfMeasure;
import model.CharDescClassComboRow;
import model.CharDescriptionRow;
import model.ClassCharacteristic;
import model.UnitOfMeasure;
import model.UserAccount;
import service.CharClassifContext;
import service.CharClassifProposer;
import service.CharPatternServices;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;

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
	@FXML Button exportButton;
	@FXML ToggleButton googleButton;
	@FXML ToggleButton tableButton;
	@FXML ToggleButton taxoButton;
	@FXML public ToggleButton imageButton;
	@FXML public ToggleButton charButton;
	
	@FXML Button prop1;
	@FXML Button prop2;
	@FXML Button prop3;
	@FXML Button prop4;
	@FXML Button prop5;
	
	@FXML TextField max_field_uom;
	@FXML TextField max_field;
	@FXML TextField min_field_uom;
	@FXML TextField min_field;
	@FXML TextField note_field_uom;
	@FXML TextField note_field;
	@FXML TextField rule_field;
	private AutoCompleteBox_UnitOfMeasure uom_field;
	//@FXML TextField uom_field;
	@FXML TextField value_field;
	@FXML TextField translated_value_field;
	
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

	
	
	private Browser_CharClassif browserController;
	
	
	private GridPane imageGrid;
	private GridPane ruleGrid;
	
	public ArrayList<Button> propButtons;
	
	
	

	public CharClassifProposer proposer;


	public CharClassProposition lcProp;


	public CharClassProposition pcProp;


	public CharClassifContext context;


	private String lastRightPane="";


	private ImagePane_CharClassif imagePaneController;


	public ArrayList<String> CNAME_CID;
	public HashMap<String, UnitOfMeasure> UOMS;
	private CharPane_CharClassif charPaneController;



	public Set<CharDescriptionRow> ROW_SYNC_POOL = new HashSet<CharDescriptionRow>();



	

	
	
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
		FileChooser fileChooser = new FileChooser();
		
		
		
		Date instant = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat( "MMMMM_dd" );
	    String time = sdf.format( instant );
	    
	    String PROJECT_NAME;
	    Connection conn = Tools.spawn_connection();
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
    		 
    		  
    		ObservableList<CharDescriptionRow> rws = tableController.tableGrid.getItems();
    		
    		CellStyle percentStyle = wb.createCellStyle();
    		percentStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
    		
    		
    		
    		for(CharDescriptionRow rw:rws) {
    			i=i+1;
    			ArrayList<String> result_row = new ArrayList<String>(10);
    			/*
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
    			
    			*/
    			
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
		
		taxoButton.setDisable(true);
		
		toolBarButtonListener();
		initializePropButtons();
		this.context = new CharClassifContext();
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
		
	}

	private void listen_for_keyboard_events() {
		account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
		account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, false);
		account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, false);
		account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
		account.PRESSED_KEYBOARD.put(KeyCode.I, false);
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
		if(keyEvent.getCode().equals(KeyCode.I)) {
			account.PRESSED_KEYBOARD.put(KeyCode.I, pressed);
		}
		if(keyEvent.getCode().equals(KeyCode.P)) {
			account.PRESSED_KEYBOARD.put(KeyCode.P, pressed);
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
			int active_char_index = Math.floorMod(this.tableController.selected_col,this.tableController.active_characteristics.get(this.classCombo.getValue().getClassSegment()).size());
			CharPatternServices.scanSelectionForRuleCreation(this,
					this.tableController.active_characteristics.get(classCombo.getValue().getClassSegment())
					.get(active_char_index));
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
				if(browserController.closeButton.isVisible()) {
					//We clicked escape to close search
					try {
						show_table();
					}catch(Exception V) {
						
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
				}else {
					this.lastRightPane="";
				}
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
		this.UOMS = Tools.get_units_of_measures("en");
		
		this.proposer = new CharClassifProposer(account.getActive_project());
		//classification tmp.setNew_segment_name( CID2NAME,j,parent_controller);
		classification = new AutoCompleteBox_CharClassification(this,classification_style.getStyle(),account);
		for( String entry : CNAME_CID) {
			classification.getEntries().add(entry);
		}
		uom_field = new AutoCompleteBox_UnitOfMeasure(this,classification_style.getStyle(),account);
		
		classification_style.setVisible(false);
		grid.add(classification, 1, 9);
		grid.add(uom_field, 5, 9);
		this.context.CID2NAME = CNAME_CID;
		
		
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
						CharDescClassComboRow tmp = new CharDescClassComboRow(elem.split("&&&")[1],elem.split("&&&")[0]);
						classCombo.getItems().add(tmp);
						if(elem.split("&&&")[0].equals(account.getUser_desc_class())) {
							classCombo.getSelectionModel().select(tmp);
						}
					}
				}
				
				
			}
		}else {
			
		}
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
		
		tableController.setParent(this,proposer);
		tableController.setUserAccount(account);
		
		tableController.setUserLanguageGcode(user_language_gcode);
		/*
		if(GlobalConstants.MANUAL_FETCH_ALL) {
			tableController.fillTable_STATIC((List<ItemFetcherRow>) ftc.currentList_STATIC);
		}else {
			tableController.fillTable_DYNAMIC((List<ItemFetcherRow>) ftc.currentList_DYNAMIC);
		}*/
		tableController.setCollapsedViewColumns(new String[] {"Completion status","Question status"});
		tableController.refresh_table_with_segment(account.getUser_desc_class());
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
	public void propose(ArrayList<CharClassProposition> propositions, CharClassProposition pc, CharClassProposition lc, CharClassProposition mg, CharClassProposition f5) {
		
		this.pcProp = pc;
		this.lcProp = lc;
		
		if(pcProp!=null) {
			//pcLabel.setText("Pre-classification (Ctrl+Q)");
		}else {
			//pcLabel.setText("Pre-classification");
		}
		if(lcProp!=null) {
			//lcLabel.setText("Classification suggestions [ (Ctrl+W) for last used class ] :");
		}else {
			//lcLabel.setText("Classification suggestions");
		}
		
		try {
			Iterator<CharClassProposition> itr = propositions.iterator();
			for(int i=0;i<GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS_OLD;i++) {
				Button button = propButtons.get(i);
				button.setVisible(true);
				if(itr.hasNext()) {
					CharClassProposition prop = itr.next();
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
				CharClassProposition prop = mg;
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
				CharClassProposition prop = f5;
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
	
	@SuppressWarnings("static-access")
	public void refresh_ui_display() {
		
		double RIGHT_TRANSLATE = 0.0*rule_field.getWidth();
		clear_data_fields();
		if(this.charButton.isSelected()) {
			this.charPaneController.load_item_chars();
		}
		CharDescriptionRow row = (CharDescriptionRow) this.tableController.tableGrid.getSelectionModel().getSelectedItem();
		try {
			int selected_col = Math.floorMod(tableController.selected_col, tableController.active_characteristics.get(row.getClass_segment().split("&&&")[0]).size());
			ClassCharacteristic active_char = tableController.active_characteristics.get(row.getClass_segment().split("&&&")[0]).get(selected_col);
			if(active_char.getIsNumeric()) {
				if( (active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0)) {
					//Add the allowed uoms to the autocomplete box
					for(String uom_id:active_char.getAllowedUoms()) {
						this.uom_field.getEntries().add(UOMS.get(uom_id));
					}
					
					
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNominal_value());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the uom
					custom_label_11.setText("Measure unit");
					custom_label_11.setVisible(true);
					try{
						uom_field.setText(UOMS.get( row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getUom_id() ).getUom_symbol());
					}catch(Exception V) {
						
					}
					uom_field.setMaxWidth(0.49*rule_field.getWidth());
					uom_field.setVisible(true);
					//Setting the minimum value
					custom_label_12.setText("Minimum value");
					custom_label_12.setTranslateX(rule_field.getWidth()*0.51);
					custom_label_12.setVisible(true);
					try{
						min_field_uom.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getMin_value());
					}catch(Exception V) {
						
					}
					min_field_uom.setMaxWidth(0.49*rule_field.getWidth());
					min_field_uom.setVisible(true);
					//Setting the maximum value
					custom_label_21.setText("Maximum value");
					custom_label_21.setVisible(true);
					try {
						max_field_uom.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getMax_value());
					}catch(Exception V) {
						
					}
					max_field_uom.setMaxWidth(0.49*rule_field.getWidth());
					max_field_uom.setVisible(true);
					//Setting the note
					custom_label_22.setText("Note");
					custom_label_22.setTranslateX(rule_field.getWidth()*0.51);
					custom_label_22.setVisible(true);
					try {
						note_field_uom.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field_uom.setMaxWidth(0.49*rule_field.getWidth());
					note_field_uom.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					//Setting the nominal value
					value_label.setText("Nominal value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNominal_value());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the minimum value
					custom_label_11.setText("Minimum value");
					custom_label_11.setTranslateX(+RIGHT_TRANSLATE);
					custom_label_11.setVisible(true);
					try{
						min_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getMin_value());
					}catch(Exception V) {
						
					}
					min_field.setVisible(true);
					min_field.setMaxWidth(0.49*rule_field.getWidth());
					
					//Setting the maximum value
					custom_label_12.setText("Maximum value");
					custom_label_12.setTranslateX(rule_field.getWidth()*0.51);
					custom_label_12.setVisible(true);
					try{
						max_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getMax_value());
					}catch(Exception V) {
						
					}
					max_field.setMaxWidth(0.49*rule_field.getWidth());
					max_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getRule_id());
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
						value_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					//Setting the translated value
					custom_label_value.setText("Value ("+this.user_language_gcode.toUpperCase()+")");
					custom_label_value.setVisible(true);
					try{
						//translated_value_field.setText(this.tableController.translate2UserLanguage(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNominal_value()));
						translated_value_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getUserLanguageValue());
						
					}catch(Exception V) {
						
					}
					translated_value_field.setVisible(true);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}else {
					//Setting the value
					value_label.setText("Value");
					value_label.setVisible(true);
					try{
						value_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getDataLanguageValue());
					}catch(Exception V) {
						
					}
					value_field.setVisible(true);
					this.grid.setColumnSpan(value_field, 3);
					//Setting the note
					note_label.setText("Note");
					note_label.setVisible(true);
					try {
						note_field.setText(row.getData(row.getClass_segment().split("&&&")[0])[selected_col].getNote());
					}catch(Exception V) {
						
					}
					note_field.setVisible(true);
					//Setting the rule
					rule_label.setText("Rule");
					rule_label.setVisible(true);
					try{
						rule_field.setText(row.getRule_id());
					}catch(Exception V) {
						
					}
					rule_field.setVisible(true);
				}
				
			}
		}catch(Exception V) {
			V.printStackTrace(System.err);
			return;
		}
		
		
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
	
}
