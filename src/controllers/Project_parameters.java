package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DataInputMethods;
import model.GenericClassRule;
import model.GlobalConstants;
import model.ItemFetcherRow;
import model.Project;
import model.ProjectTemplate;
import model.RuleSet;
import model.UserAccount;
import service.ItemFetcher;
import service.ManualRuleServices;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.data_exchange_toolbox.SpreadsheetUpload;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.dialog_toolbox.ItemUploadDialog;
import transversal.generic.AutoCompleteComboBoxListener;
import transversal.generic.Tools;

public class Project_parameters {
	
	public ProjectTemplate prj;
	
	LinkedHashMap<String,String> TaxoColumnMap = new LinkedHashMap<String,String>();
	LinkedHashMap<String,String> DataColumnMap = new LinkedHashMap<String,String>();
	File DATAFILE = null;
	
	
	private LinkedHashMap<String,String> LANGUAGES = new LinkedHashMap<String,String>();
	private LinkedHashMap<String,String> TRANSLATORS =  new LinkedHashMap<String,String>();
	private LinkedHashSet<String> LEVELS = new LinkedHashSet<String>();
	
	
	private ArrayList<RowConstraints> rc_special = new ArrayList<RowConstraints> ();
	private ArrayList<RowConstraints> rc_login = new ArrayList<RowConstraints> ();
	private LinkedHashSet<String> ROLES = new LinkedHashSet<String>();
	private LinkedHashMap<String,String> LOGINS = new LinkedHashMap<String,String>();
	
	private HashMap<String,String> SPECIAL_WORDS = new HashMap<String,String>();
	
	private HashMap<String,String> CREDENTIALS = new HashMap<String,String>();
	
	@FXML MenuBar menubar;
	@FXML
	public Accordion ACCORDION;
	@FXML TitledPane GENERAL;
	@FXML TitledPane CLASSIF;
	@FXML TitledPane DATA;
	@FXML TitledPane SPECIAL;
	@FXML private TableView<RuleSet> rules_table;
	@FXML
	//Binds to the screen's classification table project column
	private TableColumn<?, ?> Classification_projectColumn;
	@FXML
	//Binds to the screen's classification table language column
	private TableColumn<?, ?> Classification_languageColumn;
	@FXML
	//Binds to the screen's classification taxonomy column
	private TableColumn<?, ?> Classification_ruleCardColumn;
	@FXML
	//Binds to the screen's classification cardinality column
	private TableColumn<?, ?> Classification_cardColumn;
	@FXML
	//Binds to the screen's classficiation check box column
	private TableColumn<?, ?> Classification_checkboxColumn;
	@FXML private ProgressIndicator ruleLoadingIndicator;
	@FXML private ProgressIndicator ruleApplyIndicator;
	@FXML private Label ruleApplyLabel;
	@FXML private Button ruleApplyButton;
	@FXML private ProgressBar ruleApplyProgress;
	
	@FXML
	public TitledPane RULES;
	@FXML TitledPane USERS;
	
	
	
	@FXML TextField projectName;
	@FXML TextField targetQuality;
	@FXML TextField manualQuality;
	@FXML ComboBox<String> classificationLevels;
	@FXML ComboBox<String> dataLanguage;
	@FXML ComboBox<String> classifierLanguage;
	@FXML ComboBox<String> onlineTranslator;
	@FXML Button apply_general;
	@FXML Button supress_project;
	
	
	
	@FXML GridPane taxo_grid;
	@FXML ComboBox<String> taxoName;
	@FXML Label taxoFileLabel;
	@FXML TextField taxoFile;
	@FXML TextField nameL1;
	@FXML TextField nameL2;
	@FXML TextField nameL3;
	@FXML TextField nameL4;
	@FXML TextField L1ID;
	@FXML TextField L1NameD;
	@FXML TextField L1NameC;
	@FXML TextField L2ID;
	@FXML TextField L2NameD;
	@FXML TextField L2NameC;
	@FXML TextField L3ID;
	@FXML TextField L3NameD;
	@FXML TextField L3NameC;
	@FXML TextField L4ID;
	@FXML TextField L4NameD;
	@FXML TextField L4NameC;
	@FXML Button apply_taxo;
	
	@FXML GridPane data_grid;
	@FXML Label keep_data_label;
	@FXML RadioButton keep_data_button;
	
	@FXML TextFlow dataFileTextFlow;
	@FXML TextField dataFile;
	@FXML Label dataFileLabel;
	@FXML TextField aidColumn;
	@FXML TextField MGColumn;
	@FXML TextField PCColumn;
	@FXML TextField SDDColumn;
	@FXML TextField SDCColumn;
	@FXML TextField LDDColumn;
	@FXML TextField LDCColumn;
	@FXML TextField CIDColumn;
	
	@FXML Button apply_data;
	
	
	@FXML GridPane special_pane;
	@FXML Label label_template;
	@FXML CheckBox cb_template;
	@FXML Button btn_template;
	@FXML TextField for_field;
	@FXML Button for_button;
	@FXML TextField dw_field;
	@FXML Button dw_button;
	@FXML TextField stop_field;
	@FXML Button stop_button;
	
	
	@FXML GridPane login_pane;
	@FXML Label label_template_body;
	@FXML Button delete_login_button;
	@FXML Button add_button;
	@FXML ComboBox<String> login_box;
	@FXML ComboBox<String> passwd_box;
	@FXML ComboBox<String> profile_box;
	@FXML Label label_template_title;

	private String pid;
	private boolean newdata=false;
	private HashMap<String,String> TAXOS = new HashMap<String,String>();
	private boolean EDITING=false;
	private HashMap<Button,ArrayList<TextField>> DEPENDENT_FIELDS = new HashMap<Button,ArrayList<TextField>>();
	private boolean DATA_FILE_MISSING = true;
	private boolean TAXO_FILE_MISSING = true;
	public UserAccount account;

	private boolean REUSING_OLD_TAXO;

	private Integer cardinality;

	private boolean general_applied;

	private boolean classif_applied;

	private Thread RuleRefreshthread;
	
	//This function is ran when the controller of this screen is created in the project selection screen, it sets up the necessary content and conrol variables
	public Project_parameters() {
		
		
		//Set the ROLES variable with the appropriate available roles
		ROLES.add("Project Manager");
		ROLES.add("Classifier");
		ROLES.add("Reviewer");
		
		try {
			//Set the LANGUAGES variable with the appropriate available languages
			LANGUAGES = load_languages();
			//Set the TRANSLATORS variable with the appropriate available translators
			
			TRANSLATORS = load_translators();
		} catch (ClassNotFoundException | SQLException e) {
			ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
			
		}
		
		//Set the LEVELS variable with the appropriate available granularities
		LEVELS.add("1");
		LEVELS.add("2");
		LEVELS.add("3");
		LEVELS.add("4");
		
		try {
			Connection connX= Tools.spawn_connection();
			Statement stmt = connX.createStatement();
			//#
			//Set the TAXOS variable with the appropriate available classification systems
			ResultSet rs = stmt.executeQuery("select distinct classification_system_name, classification_system_id from administration.projects where project_name is not null and classification_system_id is not null and (suppression_status is null or not suppression_status)");
			while(rs.next()) {
				try{
					if(rs.getString("classification_system_name")!=null && rs.getString("classification_system_name").length()>0) {
						TAXOS.put(rs.getString("classification_system_id"),rs.getString("classification_system_name").toUpperCase());
					}
					
				}catch(Exception v) {
					
				}
			}
			
			
			rs.close();
			stmt.close();
			connX.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Set the rc_special variable with the desired row constraints
		int data_length = 12;
		for(int row=0;row<data_length+6;row++) {
			if(row==0) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(5);
		        rc_special.add(tmp);
			}else if (row==1) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(10);
		        rc_special.add(tmp);
			}else if(row<16) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(5);
				rc_special.add(tmp);
			}else if(row==16) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(10);
		        rc_special.add(tmp);
			}else {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(5);
				rc_special.add(tmp);
			}
		}
		//Set the rc_login variable with the desired row constraints
		data_length = 10;
		for(int row=0;row<data_length+6;row++) {
			if(row==0) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(5);
		        rc_login.add(tmp);
			}else if (row==1) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(10);
		        rc_login.add(tmp);
			}else if(row<14) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(6);
				rc_login.add(tmp);
			}else if(row==14) {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(10);
		        rc_login.add(tmp);
			}else {
				RowConstraints tmp = new RowConstraints();
		        tmp.setPercentHeight(5);
				rc_login.add(tmp);
			}
		}
		
		
		
		
	}
	//Triggered when the user clicks the "Suppress project" Button.
	@FXML void supress_project() {
		//If the project has classified items (known by calling the hasCoverage routine)
		if(hasCoverage()) {
			//	Call transversal.dialog_toolbox.ConfirmationDialog
			ConfirmationDialog.show("Confirm action", "Do you whish to supress this project ?", "Delete the project", "Cancel",this,false);
		}else {
			//	Else call directly the suppress() routine
			confirm_supress();
		}
		
	}
	
	//Function to know if the current project has classified items in order to decide the suppress button behavior
	private boolean hasCoverage() {
		//If we are not in editing mode (creating a new project) return false
		if(!this.EDITING) {
			return false;
		}
		Connection conn;
		try {
			//Create a connection
			conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			// Get the count of the items with classified events associated
			//#
			ResultSet rs = stmt.executeQuery("select count (item_id) from "+this.pid+".project_classification_event");
			int count = 0;
			rs.next();
			count = rs.getInt(1);
			//Close connection
			rs.close();
			stmt.close();
			conn.close();
			//Return true if the count is > 0; false otherwise
			return count!=0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	//Function to know if the current project has classified items in order to decide the suppress button behavior
	private boolean hasItems() {
		//If we are not in editing mode (creating a new project) return false
		if(!this.EDITING) {
			return false;
		}
		if(this.cardinality!=null) {
			return this.cardinality != 0;
		}
		Connection conn;
		try {
			//Create a connection
			conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			// Get the count of the items with classified events associated
			//#
			ResultSet rs = stmt.executeQuery("select count (item_id) from "+this.pid+".project_items");
			int count = 0;
			rs.next();
			count = rs.getInt(1);
			this.cardinality=count;
			//Close connection
			rs.close();
			stmt.close();
			conn.close();
			//Return true if the count is > 0; false otherwise
			return count!=0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	

	public void confirm_supress() {
		
		ConfirmationDialog.show("Confirm action", "Project suppression can't be undone, proceed ?", "Procede", "Cancel",this,true);
	}
	//Sets this project's suppressed status to True in the database
	public void supress() {
		Connection conn;
		//Create a connection
		try {
			conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			//Update the project by setting its suppressed status column to True
			//#
			stmt.executeUpdate("update administration.projects set suppression_status=true where project_id='"+this.pid+"'");
			//Close the connection
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Load the previous screen
		previous_screen();
	}
	
	
	//Loads the entire project in memory while in editing mode and calls specific loading functions for each section
	private ProjectTemplate load_project(String pid2) throws SQLException, ClassNotFoundException {
		ProjectTemplate nouveau = new ProjectTemplate();
		nouveau.setPid(pid2);
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		//Create a connection to fetch the project's info
		//#
		ResultSet rs = stmt.executeQuery("select * from administration.projects where project_id='"+pid2+"'");
		rs.next();
		//Create temporary variable to store the project's name, data and classifier language ids, translator, granularity, qualities
		String prjName = rs.getString("project_name");
		String datalanguageID = rs.getString("data_language");
		nouveau.setLanguageID(datalanguageID);
		String userlanguageID = rs.getString("classifier_language");
		String translatorID = rs.getString("translator_id");
		Integer classificationLevel = rs.getInt("number_of_levels");
		double targetQ = rs.getDouble("target_quality");
		double manualQ = rs.getDouble("manual_quality");
		//Call the (load_general) procedure
		load_general(nouveau,prjName,datalanguageID,userlanguageID,translatorID,classificationLevel,targetQ,manualQ);
		//Load the project's special words
		
		HashMap<String,LinkedHashSet<String>> specialwords = new HashMap<String,LinkedHashSet<String>> ();
		specialwords = fill_special_words(nouveau.getPid(),false);
		
		
		//Call the load_special routine
		load_special(nouveau,specialwords,true);
		
		Connection connX = Tools.spawn_connection();
		Statement stmtX = connX.createStatement();
		//Load the project's users
		LinkedHashMap<String,ArrayList<String>> users = new LinkedHashMap<String,ArrayList<String>> ();
		//#
		
		ResultSet rsX = stmtX.executeQuery("select user_name,users.user_id,user_project_profile from administration.users join administration.users_x_projects on users.user_id=users_x_projects.user_id and project_id='"+pid2+"' and user_project_profile is not null");
		while(rsX.next()) {
			ArrayList<String> tmp2 = new ArrayList<String> ();
			tmp2.add(rsX.getString("user_id"));
			tmp2.add(rsX.getString("user_project_profile"));
			users.put(rsX.getString("user_name"), tmp2);
		}
		rsX.close();
		stmtX.close();
		connX.close();
		
		
		//Call the load_users routine
		load_users(nouveau,users);
		
		
		//Call the load_taxo routine
		load_taxo(nouveau,rs.getString("classification_system_id"));
		nouveau.setTaxoName(rs.getString("classification_system_name"));
		//Call the load_data routine
		load_datapane_layout();
		
		//Close the connection
		rs.close();
		stmt.close();
		conn.close();
		//Save the newly create ProjectTemplate data structure
		return nouveau;
	}

	private HashMap<String, LinkedHashSet<String>> fill_special_words(String pid_or_language,Boolean publique) throws ClassNotFoundException, SQLException {
		HashMap<String,LinkedHashSet<String>> specialwords = new HashMap<String,LinkedHashSet<String>> ();
		
		if(!publique) {
		
			
			LinkedHashSet<String> tmp_for = new LinkedHashSet<String>();
			LinkedHashSet<String> tmp_dw = new LinkedHashSet<String>();
			LinkedHashSet<String> tmp_stop = new LinkedHashSet<String>();
			Connection connX = Tools.spawn_connection();
			Statement stmtX = connX.createStatement();
			
			//Load the application special words
			//#
			ResultSet rsX = stmtX.executeQuery("select term_name from "+pid_or_language+".project_terms where application_term_status");
			while(rsX.next()) {
				tmp_for.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("FOR", tmp_for);
			rsX.close();
			
			//Load the drawing special words
			//#
			rsX = stmtX.executeQuery("select term_name from "+pid_or_language+".project_terms where drawing_term_status");
			while(rsX.next()) {
				tmp_dw.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("DW", tmp_dw);
			rsX.close();
			
			//Load the stop special words
			//#
			rsX = stmtX.executeQuery("select term_name from "+pid_or_language+".project_terms where stop_term_status");
			while(rsX.next()) {
				tmp_stop.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("STOP", tmp_stop);
			
			rsX.close();
			stmtX.close();
			connX.close();
			
		}else {
			
			LinkedHashSet<String> tmp_for = new LinkedHashSet<String>();
			LinkedHashSet<String> tmp_dw = new LinkedHashSet<String>();
			LinkedHashSet<String> tmp_stop = new LinkedHashSet<String>();
			Connection connX = Tools.spawn_connection();
			Statement stmtX = connX.createStatement();
			
			//Load the application special words
			//#
			ResultSet rsX = stmtX.executeQuery("select term_name from public_ressources.terms where data_language='"+pid_or_language+"' and application_term_status");
			while(rsX.next()) {
				tmp_for.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("FOR", tmp_for);
			rsX.close();
			
			//Load the drawing special words
			//#
			rsX = stmtX.executeQuery("select term_name from public_ressources.terms where data_language='"+pid_or_language+"' and drawing_term_status");
			while(rsX.next()) {
				tmp_dw.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("DW", tmp_dw);
			rsX.close();
			
			//Load the stop special words
			//#
			rsX = stmtX.executeQuery("select term_name from public_ressources.terms where data_language='"+pid_or_language+"' and stop_term_status");
			while(rsX.next()) {
				tmp_stop.add(rsX.getString("term_name").toUpperCase());
			}
			specialwords.put("STOP", tmp_stop);
			
			rsX.close();
			stmtX.close();
			connX.close();
			
			
			
			
			
			
		}
		
		return specialwords;
		
	}
	//Usefull function to set the LOGINS field in the ProjectTemplate data structure
	private void load_users(ProjectTemplate nouveau, LinkedHashMap<String, ArrayList<String>> users2) {
		try{
			//Set the LOGINS field in the ProjectTemplate data structure
			nouveau.setLOGINS(users2);
		}catch(Exception X) {
			
		}
	}


	
	//Sets up the DW_WORDS, FOR_WORDS, STOP_WORDS for the projectTemplate data structure
	private void load_special(ProjectTemplate nouveau, HashMap<String, LinkedHashSet<String>> specialwords, boolean same_project) {
		
		
		
		//The function is either called in the same_project=true mode or same_project=false_mode
				//For each special field in the project Template data structure, the same_project flag is used to decide if the word is ticked (belongs to the project) or not (the word belongs to the project's data language)
				//Set up the STOP words
				try{
					for(String st:specialwords.get("STOP")) {
						if(nouveau.getSTOP_WORDS().containsKey(st) && nouveau.getSTOP_WORDS().get(st)){continue;}else{nouveau.getSTOP_WORDS().put(st,same_project);}
						
					}
					//nouveau.getSTOP_WORDS().addAll(specialwords.get("STOP"));
				}catch(Exception X) {
					
				}
				//Set up the DRAWING words
				try{
					for(String st:specialwords.get("DW")) {
						if(nouveau.getDW_WORDS().containsKey(st) && nouveau.getDW_WORDS().get(st)){continue;}else{nouveau.getDW_WORDS().put(st,same_project);}
						
					}
					//nouveau.getDW_WORDS().addAll(specialwords.get("DWG"));
				}catch(Exception Y) {
					
				}
				//Set up the FOR words
				try {
					for(String st:specialwords.get("FOR")) {
						if(nouveau.getFOR_WORDS().containsKey(st) && nouveau.getFOR_WORDS().get(st)){continue;}else{nouveau.getFOR_WORDS().put(st,same_project);}
						
					}
					//nouveau.getFOR_WORDS().addAll(specialwords.get("FOR"));
				}catch(Exception Z) {
					
				}
		
	}

	//Usefull function to set the DataID field in the ProjectTemplate data structure and make the layout ready
	@SuppressWarnings("static-access")
	private void load_datapane_layout() {
		
		/*if(!(dataName!=null) || dataName.length()==0) {
			return;
		}*/
		//Set the DataID field in the ProjectTemplate data structure
		/*dataFile.setText(dataName.split("_DATA_")[1]);*/
		
		
		//keep_data_label.setText("Project already contains items, update existing items?");
		keep_data_button.setSelected(false);
		
		for(Node noeud:data_grid.getChildren()) {
			if(data_grid.getRowIndex(noeud)>2) {
				noeud.setVisible(!DATA_FILE_MISSING);
			}
		}
		
		keep_data_label.setVisible(hasItems() && !DATA_FILE_MISSING);
		keep_data_button.setVisible(hasItems() && !DATA_FILE_MISSING);
		
		decorate(dataFileLabel);
		
		
		
		//dataFileLabel.setText(hasItems()?"Data import file\n (Project already contains items)":"Data import file");
		
		//dataFile.setText("Project already contains items");
		/*nouveau.setDataID(dataName); old code
		for(Node noeud:data_grid.getChildren()) {
			if(data_grid.getRowIndex(noeud)>2 && data_grid.getRowIndex(noeud)<9) {
				noeud.setVisible(false);
			}
		}*/
	}

	private void decorate(Label dataFileLabel) {
		
		dataFileLabel.setVisible(false);
		Text text1 = new Text(dataFileLabel.getText());
		text1.setStyle(dataFileLabel.getStyle());
		text1.setFill(dataFileLabel.getTextFill());
		dataFileTextFlow.getChildren().clear();
		dataFileTextFlow.getChildren().add(text1);
		
		if(hasItems()) {
			Text text2=new Text("\nWARNING: Project already contains items");
			
			text2.setFill(Color.RED);
			text2.setStyle("-fx-font-style:italic;-fx-font-weight:normal");
			
			dataFileTextFlow.getChildren().add(text2);
		}
	}
	//Usefull function to set the DataID field in the ProjectTemplate data structure
	@SuppressWarnings("static-access")
	private void load_taxo(ProjectTemplate nouveau, String old_taxo) {
		
		try {
			if(old_taxo.length()==0) {
				return;
			}
			//Set the taxo name in the combo box field
			taxoName.setValue(TAXOS.get(old_taxo));
			//Hide all the column mapping text field in the classification system
			for(Node noeud :taxo_grid.getChildren()) {
				noeud.setDisable(true);
				if( (taxo_grid.getRowIndex(noeud)>1 && taxo_grid.getRowIndex(noeud)<15 )  ) {
					noeud.setVisible(false);
				}
			}
			
			//sync_classification_level_definition(classificationLevels.getValue());
			
		}catch(Exception X) {
			
		}
		//Set the taxoID field in the ProjectTemplate data structure
		nouveau.setTaxoID(old_taxo);
	}

	//In editing mode, loads the project data in the first pane
	private void load_general(ProjectTemplate nouveau,String prjName,String datalanguageID,String userlanguageID,String translatorID,Integer classificationLevel,double targetQ, double manualQ) {
		//Set the project name
		projectName.setText(prjName);
		//Set the project data language
		try{
			for(String langugage:LANGUAGES.keySet()) {
				if(LANGUAGES.get(langugage).equals(datalanguageID)) {
					dataLanguage.setValue(langugage);
					break;
				}
			}
			
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
		//Set the project data language
		try {
			for(String langugage:LANGUAGES.keySet()) {
				if(LANGUAGES.get(langugage).equals(userlanguageID)) {
					classifierLanguage.setValue(langugage);
					break;
				}
			}
		}catch(Exception W) {
			W.printStackTrace(System.err);
		}
		//Set the project online translator
		try {
			for(String translator:TRANSLATORS.keySet()) {
				if(TRANSLATORS.get(translator).equals(translatorID)) {
					onlineTranslator.setValue(translator);
					break;
				}
			}
		}catch(Exception X) {
			X.printStackTrace(System.err);
		}
		//Set the project granularity
		try {
			classificationLevels.setValue(String.valueOf(classificationLevel));
		}catch(Exception Y) {
			Y.printStackTrace(System.err);
		}
		//Set the project's granularity
		targetQuality.setText(String.valueOf(targetQ));
		manualQuality.setText(String.valueOf(manualQ));
	}

	//loads all the available translators in <String,String> variable
	private LinkedHashMap<String, String> load_translators() throws ClassNotFoundException, SQLException {
		//Create a connection
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		//Select all the available pairs (translator_id,translator_name)
		//#
		ResultSet rs = stmt.executeQuery("select * from administration.translators");
		//Create a temporary mapping (translator_name<->translator_id)
		LinkedHashMap<String, String> ret = new LinkedHashMap<String,String>();
		//For every pair in the result set:
		while(rs.next()) {
			//Fill the key with the translator name and the value with the translator_id
			ret.put(rs.getString(2), rs.getString(1));
		}
		//Close the connection
		rs.close();
		stmt.close();
		conn.close();
		//Return the temporary mapping
		return ret;
	}

	//loads all the available languages in <String,String> variable
	private LinkedHashMap<String, String> load_languages() throws ClassNotFoundException, SQLException {
		//Create a connection
		//#
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		//Select all the available pairs (language_id,language_name)
		//#
		ResultSet rs = stmt.executeQuery("select * from administration.languages");
		//Create a temporary mapping (language_name<->language_id)
		LinkedHashMap<String, String> ret = new LinkedHashMap<String,String>();
		//For every pair in the result set
		while(rs.next()) {
			//Fill the key with the language name and the value with the language_id
			ret.put(rs.getString(2), rs.getString(1));
		}
		//Close the connection
		rs.close();
		stmt.close();
		conn.close();
		// return the temporary mapping
		return ret;
	}


	
	public void setPid(String pid) {
		this.pid=pid;
	}
	
	
	//initialize is an optional JavaFX method that launches automatically when the screen finishes loading
	@FXML void initialize(){
		
		Tools.decorate_menubar(menubar, account);
		//Activate the transiftion between the panes of the accordion to animated
		
		set_panes_animated();
		//Set the first pane as the default expanded pane in the accordion
		ACCORDION.setExpandedPane(GENERAL);
		//Call the (prefill_combo_boxes) procedure
		prefill_combo_boxes();
		//Call the (handle_import_files) procedure
		handle_import_files();
		//Sleep the current program for a second to allow the instructions from the previous screen to initialize the ProjectTemplate data structure
		Platform.runLater(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		//After the sleep time, test the ProjectTemplate data structure
		//If the data structure is not null, this means we are in editing mode
			if(this.prj!=null) {
				try {
					
					//	Call the (load_project) routine
					this.prj = load_project(this.prj.getPid());
					//	Set the EDITING variable to true
					EDITING = true;
					//	Set the keep or replace data label and button to not visible
					keep_data_label.setVisible(false);
					keep_data_button.setVisible(false);
					//	Disable for editing the granularity, the project name, the data and classifier languages
					classificationLevels.setDisable(EDITING);
					projectName.setDisable(EDITING);
					dataLanguage.setDisable(EDITING);
					classifierLanguage.setDisable(EDITING);
					dataFile.setText("Select File");
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		//Else (we are creating a new project)
			}else {
				
				CLASSIF.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			        if (isNowExpanded) {
			        	if(general_applied) {
			        		
			        	}else {
			        		GENERAL.setExpanded(true);
			        	}
			        }});
				
				DATA.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			        if (isNowExpanded) {
			        	if(classif_applied) {
			        		
			        	}else {
			        		CLASSIF.setExpanded(true);
			        	}
			        }});
				
				RULES.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			        if (isNowExpanded) {
			        	if(classif_applied) {
			        		
			        	}else {
			        		CLASSIF.setExpanded(true);
			        	}
			        }});
				
				
				SPECIAL.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			        if (isNowExpanded) {
			        	if(general_applied) {
			        		
			        	}else {
			        		GENERAL.setExpanded(true);
			        	}
			        }});
				
				USERS.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			        if (isNowExpanded) {
			        	if(general_applied) {
			        		
			        	}else {
			        		GENERAL.setExpanded(true);
			        	}
			        }});
				
				
				
				
				
				//	Set the keep or replace data label and button to not visible
				keep_data_label.setVisible(false);
				keep_data_button.setVisible(false);
				//	Create a new ProjectTemplate data structure to store the input data
				this.prj = new ProjectTemplate();
				dataFile.setText("Select File");
			}
		//Set the data pane layout
			load_datapane_layout();
		//Call the (load_all_special) procedure
			load_all_special(this.prj);
		//Call the (update_login_pane) procedure
			update_login_pane();
	    });
		
		sync_classification_level_definition("0");
		
		//Call the AutoCompleteComboBoxListener procedure on the data and classifier languages combo boxes
		new AutoCompleteComboBoxListener(dataLanguage);
		new AutoCompleteComboBoxListener(classifierLanguage);
		
	}
	// Loads the corresponding special words based on the currently selected data language
	public void load_all_special(ProjectTemplate prj) {
		
		//If the data language hasn't been set yet, exist function
		//Else
		if(prj.getLanguageID()!=null) {
			HashMap<String, LinkedHashSet<String>> specialwords;
			try {
				specialwords = fill_special_words(prj.getLanguageID(),true);
				load_special(prj,specialwords,false);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			return;
		}
		//Call the update_special_pane procedure
		
		update_special_pane();
		
		
		
	}

	//Tries to add the typed FOR special word to the currently known STOP special words for the project
	@FXML public void add_for() {
	//If the typed FOR word only contains spaces or is already associated to the project (contained within the FOR_WORDS field in the ProjectTemplate data structure), do nothing and exit function
	if( for_field.getText().replace(" ", "").length()==0 || this.prj.getFOR_WORDS().containsKey(for_field.getText().toUpperCase()) ){
		for_field.setText("");
		return;
	}
	//Else add the typed FOR word to the known special words of the current project using the FOR_WORDS field in the ProjectTemplate data structure
	this.prj.getFOR_WORDS().put(for_field.getText().toUpperCase(),true);
	add_public_special(for_field.getText().toUpperCase(),"FOR");
	//Refresh the special pane
	update_special_pane();
	for_field.setText("");
	}
	
	
	private void add_public_special(String special_term, String type) {
		
		try {
			Connection conn = Tools.spawn_connection();
			PreparedStatement ps = conn.prepareStatement("insert into public_ressources.terms values (?,?,?,?,?,?)");
			ps.setString(1, Tools.generate_uuid());
			ps.setString(2, special_term.toUpperCase());
			ps.setString(3, this.prj.getLanguageID());
			if(type.equals("FOR")) {
				ps.setBoolean(4, false);
				ps.setBoolean(5, true);
				ps.setBoolean(6, false);
			}else if(type.equals("STOP")) {
				ps.setBoolean(4, true);
				ps.setBoolean(5, false);
				ps.setBoolean(6, false);
			}else {
				ps.setBoolean(4, false);
				ps.setBoolean(5, false);
				ps.setBoolean(6, true);
			}
			ps.execute();
			ps.close();
			conn.close();
			
			
			
			
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	//Tries to add the typed DRAWING special word to the currently known DRAWING special words for the project
	@FXML public void add_dw() {
		//If the typed DRAWING word only contains spaces or is already associated to the project (contained within the DW_WORDS field in the ProjectTemplate data structure), do nothing and exit function
		if( dw_field.getText().replace(" ", "").length()==0 || this.prj.getDW_WORDS().containsKey(dw_field.getText().toUpperCase()) ){
			dw_field.setText("");
			return;
		}
		//Else add the typed DRAWING word to the known special words of the current project using the DW_WORDS field in the ProjectTemplate data structure
		this.prj.getDW_WORDS().put(dw_field.getText().toUpperCase(),true);
		add_public_special(dw_field.getText().toUpperCase(),"DW");
		dw_field.setText("");
		//Refresh the special pane
		update_special_pane();
	}
	//Tries to add the typed STOP special word to the currently known stop special words for the project
	@FXML public void add_stop() {
		//If the typed stop words only contains spaces or is already associated to the project (contained within the STOP_WORDS field in the ProjectTemplate data structure), do nothing and exit function
		if( stop_field.getText().replace(" ", "").length()==0 || this.prj.getSTOP_WORDS().containsKey(stop_field.getText().toUpperCase()) ){
			stop_field.setText("");
			return;
		}
		//Else add the typed STOP word to the known special words of the current project using the STOP_WORDS field in the ProjectTemplate data structure
		this.prj.getSTOP_WORDS().put(stop_field.getText().toUpperCase(),true);
		add_public_special(stop_field.getText().toUpperCase(),"STOP");
		stop_field.setText("");
		//Refresh the special pane
		update_special_pane();
	}
	
	
	//Adds logins and roles to the LOGINS field in the ProjectTemplate data structure
	@FXML public void add_login() {
		//If the selected login value is empty or the project has already a role associated to the current login , do nothing and exist function
		if ( login_box.getValue().replace(" ", "").length()==0 || /*passwd_box.getValue().replace(" ", "").length()==0 ||*/ this.prj.getLOGINS().containsKey(login_box.getValue().toUpperCase()) ) {
			login_box.setValue("");
			passwd_box.setValue("");
			profile_box.setValue("");
			profile_box.setMinWidth(250);;
			
			
			
			return;
		}
		//Create a temporary array
		ArrayList<String> tmp = new ArrayList<String>();
		//Store the user_id in the first slot of the array
		tmp.add(LOGINS.get(login_box.getValue()));
		//Store the profile value in the second slot of the array
		tmp.add(profile_box.getValue());
		//Store the temporary array in the LOGINS field of the ProjectTemplate data structure
		this.prj.getLOGINS().put(login_box.getValue(),tmp);
		//Set the current user as project manager, always
		
		login_box.getItems().remove(login_box.getValue());
		
		
		
		// Call the (update_login_pane) routine
		update_login_pane();
		//Empty the values in all the input fields
		login_box.setValue("");
		passwd_box.setValue("");
		profile_box.setValue("");
		profile_box.setMinWidth(250);;
		
		
		
		
	}
	//Action triggered when the user clicks the "Apply" Button in the general information section
	@FXML public void apply_general() {
		general_applied = true;
		//Switch the active accordion pane to the classification system pane
		ACCORDION.setExpandedPane(CLASSIF);
		//Call the routine (save_project())
		Platform.runLater(()->{
			try {
				save_project();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	//Action triggered when the user clicks the "Apply" Button in the classification system upload section
	@FXML public void apply_taxo() {
		classif_applied = true;
		//Switch the active accordion pane to item upload section
		ACCORDION.setExpandedPane(DATA);
		
		//Call the routine (save_taxo())
		//Set the TaxoID field in the ProjectTemplate data structure to the return value of the (save_taxo()) routine
		Platform.runLater(()->{
			try {
				this.prj.setTaxoID(save_taxo());
				save_project();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		
	}
	//Action triggered when the user clicks the "Apply" Button in the item upload section
	@FXML public void apply_data() throws ClassNotFoundException, SQLException {
	//Switch the active accordion pane to special
		if(!newdata) {
			return;
		}
		//Call the routine (save_data())
		//Set the dataID field in the ProjectTemplate data structure to the return value of the (save_data()) routine
		ItemUploadDialog.uploadItems(this);
		
	}
	public boolean reEvaluateClassifRules(ArrayList<String> affectedItemIDs, Label progressStage, ProgressBar progressBar,ArrayList<GenericClassRule> grs, ArrayList<ArrayList<String[]>> itemRuleMaps, ArrayList<Boolean> activeStatuses, ArrayList<String> METHODS, List<ItemFetcherRow> databaseSyncLists, String datainputmethod) throws ClassNotFoundException, SQLException {
		account.setActive_project(this.prj.getPid());
		boolean reevaluateClassifiedItems = GlobalConstants.REFRESH_ALL_RULE_ITEMS_ON_UPLOAD;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				progressStage.setText("Loading active project rules");
				progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			}});
		ArrayList<GenericClassRule> newImportedRules = SpreadsheetUpload.getKnownClassificationRules(this.prj.getPid());
		if(newImportedRules.size()>0) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressStage.setText("Loading project items");
					progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				}});
			
			ItemFetcher ftc = new ItemFetcher(this.prj.getPid(),null);
			if(ftc.currentList_STATIC.size()>0) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						progressStage.setText("Applying "+String.valueOf(newImportedRules.size())+" active rules on "+String.valueOf(affectedItemIDs.size())+" updated items");
					}});
				ManualRuleServices.i = 0.0;
				
				double ruleNumber = newImportedRules.parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()).size();
				
				ManualRuleServices.StreamRulesOnItemFetcherRows(ruleNumber, newImportedRules.parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()),
						ftc.currentList_STATIC.parallelStream()
						.filter(row -> affectedItemIDs.contains(row.getItem_id()))
						.filter(row -> reevaluateClassifiedItems || !(((ItemFetcherRow) row).getDisplay_segment_id()!=null) ).collect(Collectors.toList())
						,progressBar,
						null
						,account
						,databaseSyncLists
						,grs
						,itemRuleMaps
						,activeStatuses
						,METHODS);
						
				
				
				return true;
				
				
			}else {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						progressStage.setText("Project has no items");
						progressBar.setVisible(false);
					}});
				
				return false;
			}
			
				
		}else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressStage.setText("Project has no active rules");
					progressBar.setVisible(false);
				}});
			
			return false;
		}
	}
	//Action triggered when the user clicks the "Apply" Button in the special section. Deprecated as of February 26 (replaced by the calls to update_user_and_specials on the buttons)
	@FXML public void apply_special() {
		//Switch the active accordion pane to the profile pane
		//Call the routine (save_project())
		ACCORDION.setExpandedPane(USERS);
		Platform.runLater(()->{
			try {
				save_project();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	//Action triggered when the user clicks the "Apply" Button in the user information section. Deprecated as of February 26 (replaced by the calls to update_user_and_specials on the buttons)
	@FXML public void apply_project() throws SQLException {
		//Collapse the accordion at once
		USERS.setExpanded(false);
		//Call the save_project() routine
		save_project();
		
		
		
		
		
		
	}

//Asks from java FX to close the window parent
	private void close_window() {
		Stage stage = (Stage) apply_general.getScene().getWindow();
	    stage.close();
		
	}
	
	//Usefull function to close the current screen after loading the "Project selection" screen
	@FXML private void previous_screen() {
		try {
		    Stage primaryStage = new Stage();
		    
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Project_selection.fxml"));
			AnchorPane root = fxmlLoader.load();

		    controllers.Project_selection controller = fxmlLoader.getController();
			
			Scene scene = new Scene(root,400,400);
			
			primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
			primaryStage.setScene(scene);
			//primaryStage.setMinHeight(768);
			//primaryStage.setMinWidth(1024);
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			primaryStage.show();
			controller.setUserAccount(account);
			;
			;
			controller.load_projects_list();
			
			close_window();
			
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 project_selection", "FX001 project_selection", "FX001 project_selection");
			e.printStackTrace();
		}
	}

	//Performs various operations to get the desired behaviour for uploading the data and taxonomy files
	private void handle_import_files() {
		//Set the taxonomy file location text field to "Select File"
		taxoFile.setText("Select File");
		//Add to the taxonomy name text field a change listener
		taxoName.getEditor().textProperty().addListener(new ChangeListener<String>() {

		    @SuppressWarnings("static-access")
			@Override
		    public void changed(ObservableValue<? extends String> observable, 
		                                    String oldValue, String newValue) {
		    	
		    	
		    	
		    	//	Every time the value changes if the text contains chars other than alphanumerical or underscore, change it to "_" then start the function over
		    	if(!newValue.matches("[^a-zA-Z_0-9]")) {
		    		taxoName.setValue(newValue.replaceAll("[^a-zA-Z_0-9]","_"));
		    	}
		    	
		    	
		    	//	Every time the value changes if the text isn't empty, make the taxonomy file location visible
		    	//	If the contained text doesn't correspond to known taxonomies
		    	try {
		    		if(TAXOS.containsValue(newValue.toUpperCase())){
		    			//		If it is, hide the taxonomy import file, enable the "Apply" Button, set the TaxoID field in the ProjectTemplate data structure to the classficaton system id corresponding to the input classification system name, hide all the column mapping textfields
						
		    			taxoFile.setVisible(false);
		    			taxoFileLabel.setVisible(false);
		    			
						apply_taxo.setDisable(false);
						for(String taxoID:TAXOS.keySet()) {
							if(TAXOS.get(taxoID).equals(newValue.toUpperCase())) {
								
								prj.setTaxoID(taxoID);
								break;
							}else {
								
							}
						}
						
						for(Node noeud :taxo_grid.getChildren()) {
							if( (taxo_grid.getRowIndex(noeud)>1 && taxo_grid.getRowIndex(noeud)<15 )  ) {
								noeud.setVisible(false);
							}
						}
						REUSING_OLD_TAXO=true;
		    			refreshRuleSet(REUSING_OLD_TAXO);
		    		}else {
		    			REUSING_OLD_TAXO=false;
		    			refreshRuleSet(REUSING_OLD_TAXO);
		    			if(newValue.length()>0) {
		    				taxoFile.setVisible(true);
			    			taxoFileLabel.setVisible(true);
			    			TAXO_FILE_MISSING=true;
			    			taxoFile.setText("Select File");
		    			}else {
		    				taxoFile.setVisible(false);
			    			taxoFileLabel.setVisible(false);
			    			TAXO_FILE_MISSING=true;
			    			taxoFile.setText("Select File");
		    			}
		    			sync_classification_level_definition(classificationLevels.getValue());
		    			
		    		
		    		}
		        }catch(Exception V) {
		        	V.printStackTrace(System.err);
		        	taxoFile.setVisible(false);
		        	taxoFileLabel.setVisible(false);
		        }
		        
		        prj.setTaxoName(newValue.toUpperCase());
		    	
		    }
		});
		//Add to the taxonomy name combo box file a change listener
		taxoName.valueProperty().addListener(new ChangeListener<String>() {
			@SuppressWarnings("static-access")
			@Override
			public void changed(ObservableValue <? extends String> arg0, String arg1, String arg2) {
				/*if (taxoName.getValue().equals("New...")) {
					for(Node noeud :taxo_grid.getChildren()) {
						noeud.setVisible(true);
					}*/
					
					 	
                //	Every time the value changes check if the current value is contained within the known classification systems
				//		If it is, hide the taxonomy import file, unable the "Apply" Button, set the TaxoID field in the ProjectTemplate data structure to the classficaton system id corresponding to the input classification system name, hide all the column mapping textfields
				if(TAXOS.containsValue(taxoName.getValue())){
					//RENAME=false;
					taxoFile.setVisible(false);
					taxoFileLabel.setVisible(false);
					apply_taxo.setDisable(false);
					for(String taxoID:TAXOS.keySet()) {
						if(TAXOS.get(taxoID).equals(taxoName.getValue().toUpperCase())) {
							
							prj.setTaxoID(taxoID);
							break;
						}else {
							
						}
					}
					for(Node noeud :taxo_grid.getChildren()) {
						if( (taxo_grid.getRowIndex(noeud)>1 && taxo_grid.getRowIndex(noeud)<15 )  ) {
							noeud.setVisible(false);
						}
					}
					REUSING_OLD_TAXO=true;
					refreshRuleSet(REUSING_OLD_TAXO);
				//		Else, call the (sync_classification_level_definition)
				}else {
					sync_classification_level_definition(classificationLevels.getValue());
					REUSING_OLD_TAXO=false;
					refreshRuleSet(REUSING_OLD_TAXO);
				}
				
				prj.setTaxoName(taxoName.getValue().toUpperCase());
			}
			
		});
		
		//Add to the item file location text field a mouse click listener
		dataFile.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				// Every time the mouse is clicked
				Platform.runLater(new Runnable() {
				    @Override public void run() {
				    	//	Create a file explorer to allow the user to browse the file system
				    	FileChooser fileChooser = new FileChooser();

		                //	Set an extension filter to only allow excel workbooks to be selected
		                FileChooser.ExtensionFilter extFilter = 
		                        new FileChooser.ExtensionFilter("Excel workbooks (*.xlsx)", "*.xlsx");
		                fileChooser.getExtensionFilters().add(extFilter);

		                // Show the file explorer
		                DATAFILE = fileChooser.showOpenDialog(taxoName.getScene().getWindow());
		                //	If the user successfully loads the file
		                if (DATAFILE != null) {
		                	//Set the DATA_FILE_MISSING variable to false
		                    DATA_FILE_MISSING=false;
		                    
		                	newdata=true;
		                	//		Set the item file location text field to the selected file path
		                    dataFile.setText(DATAFILE.getName().split("\\.xlsx")[0]);
		                    dataFile.setText(DATAFILE.getAbsolutePath());
		                    dataFile.setEditable(false);
		                    //		Call the CHECK_FILL procedure
		                    CHECK_FILL(apply_data,false);
		                //	Else, the user cancelled the operation, has no rights over the file, etc...
		                }else {
		                	DATA_FILE_MISSING=true;
		                	//		Set the item file location text field to empty
		                	//		Set the DATA_FILE_MISSING variable to true
		                	//		Call the CHECK_FILL procedure
		                	DATAFILE = null;
		                	dataFile.setText("Select File");
		                	CHECK_FILL(apply_data,false);
		                }
				}});
			}
			
		});
		
		dataFile.textProperty().addListener((observable, oldValue, newValue) -> {
		    load_datapane_layout();
		});
		
		
	}
	
	
	
	@SuppressWarnings("deprecation")
	protected void refreshRuleSet(boolean knownTaxoChosen) {
		//Set column 1 to point to the project_name field in the RULES data structure
		Classification_projectColumn.setCellValueFactory(new PropertyValueFactory<>("project_name"));
		//Set column 2 to point to the language field in the RULES data structure
		Classification_languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
		//Set column 3 to point to the classification_system field in the RULES data structure
		Classification_ruleCardColumn.setCellValueFactory(new PropertyValueFactory<>("no_rules"));
		//Set column 4 to point to the no_items field in the RULES data structure
		Classification_cardColumn.setCellValueFactory(new PropertyValueFactory<>("no_items"));
		//Set column 5 to point to the referent_data field in the RULES data structure
		Classification_checkboxColumn.setCellValueFactory(new PropertyValueFactory<>("referentData"));
		try {
			RuleRefreshthread.stop();
		}catch(Exception V) {
			
		}
		if(knownTaxoChosen) {
			
			Task<Void> executeAppTask = new Task<Void>() {
			    @Override
			    protected Void call() throws Exception {
			    	ruleLoadingIndicator.setVisible(true);
					System.out.println("Refreshing rule set "+String.valueOf(knownTaxoChosen));
					Connection conn;
					conn = Tools.spawn_connection();
					Statement ps = conn.createStatement();
					ResultSet rs = ps.executeQuery("select * from administration.projects where classification_system_name = '"+taxoName.getValue()+"' and (not suppression_status or suppression_status is null) and project_id is not null");
					System.out.println("select * from administration.projects where classification_system_name = '"+taxoName.getValue()+"' and (not suppression_status or suppression_status is null) and project_id is not null and project_id !='"+account.getActive_project()+"'");
					Connection conn2 = Tools.spawn_connection();
					Statement ps2 = conn2.createStatement();
					//For every known projects
					while(rs.next()) {
						try {
							//	Create a RuleSet data structure
							RuleSet tmp = new RuleSet();
							if(!(rs.getString("classification_system_name")!=null)) {
								continue;
							}
							//	Set the project's id
							//	Set the project's name
							//	Set the project's language id
							//	Set the project's language
							//	Set the project's classification system id
							//	Set the project's classification system name
							//	Set the project's cardinality
							//	Set the project's granularity
							//	Set the project's combo box
							
							String SUMquery = "select sum(count) from ("
									+QueryFormater.FetchClassifProgresionByDateByUser(rs.getString("project_id"))
									+") progress";
							int sum = 0;
							ResultSet rs2 = ps2.executeQuery(SUMquery);
							//ResultSet rs2 = ps2.executeQuery("select count(*) from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM "+rs.getString("project_id")+".project_classification_event where item_id in (select distinct item_id from "+rs.getString("project_id")+".project_items) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  "+rs.getString("project_id")+".project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join "+rs.getString("project_id")+".project_items on rich_events.item_id = project_items.item_id");
							
							while(rs2.next()) {
								sum+=rs2.getInt(1);
							};
							tmp.setProject_id(rs.getString("project_id"));
							tmp.setProject_name(rs.getString("project_name"));
							tmp.setLanguage_id(rs.getString("data_language"));
							tmp.setLanguage(LANGUAGES.entrySet().stream().filter(e->e.getValue().equals(tmp.getLanguage_id())).findAny().get().getKey());
							tmp.setTaxoid(rs.getString("classification_system_name"));
							tmp.setClassifcation_system(tmp.getTaxoid());
							
							
							tmp.setNo_items(Tools.formatThounsands(sum));
							
							rs2 = ps2.executeQuery("SELECT COUNT(DISTINCT rule_id) FROM "+rs.getString("project_id")+".project_rules where active_status");
							rs2.next();
							if(rs2.getInt(1)==0) {
								//The reference project has no classified items
								rs2.close();
								continue;
							}
							tmp.setNo_rules(Tools.formatThounsands(rs2.getInt(1)));
							tmp.setGranularity(rs.getInt("number_of_levels"));
							CheckBox cb = new CheckBox();
							cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
	
								@Override
								public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
									ruleApplyLabel.setVisible(false);
									ruleApplyProgress.setVisible(false);
									ruleApplyIndicator.setVisible(false);
									ruleApplyButton.setVisible(rules_table.getItems().stream().anyMatch(r->r.getReferentData().isSelected()));
								}
								
							});
							tmp.setReferentData(cb);
							rules_table.getItems().add(tmp);
							//Close the connection
							rs2.close();
						}catch(Exception V) {
							
						}
					};
					
					rs.close();
					ps.close();
					ps2.close();
					conn.close();
					conn2.close();
					
					return null;
			    }
			};
			
			executeAppTask.setOnSucceeded(new EventHandler<WorkerStateEvent>()
			{
			    @Override
			    public void handle(WorkerStateEvent t){
			    	System.out.println("refresh Rule Set success");
			    	ruleLoadingIndicator.setVisible(false);
			    }
			});
			
			
			
			
			RuleRefreshthread = new Thread(executeAppTask);; RuleRefreshthread.setDaemon(true);
			RuleRefreshthread.setName("Refreshing rule set from chosen taxo");
			RuleRefreshthread.start();
			
			
		}else {
			rules_table.getItems().clear();
		}
		
	}
	
	@FXML void importCharRules() throws ClassNotFoundException, SQLException {
		account.setActive_project(this.prj.getPid());
		rules_table.getItems().forEach(r->r.getReferentData().setDisable(true));
		boolean reevaluateClassifiedItems = false;
		ArrayList<GenericClassRule> newImportedRules = new ArrayList<GenericClassRule>();
		ruleApplyLabel.setVisible(true);
		ruleApplyIndicator.setVisible(true);
		ruleApplyLabel.setText("Replicating rules ...");
		rules_table.getItems().stream().forEach(r->{
			try {
				newImportedRules.addAll(SpreadsheetUpload.importClassRules(r.getProject_id(),this.prj.getPid()));
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		System.out.println("Applying "+String.valueOf(newImportedRules.size())+" new imported rules");
		if(newImportedRules.size()>0) {
			ruleApplyLabel.setText("Loading project items ...");
			ItemFetcher ftc = new ItemFetcher(this.prj.getPid(),null);
			if(ftc.currentList_STATIC.size()>0) {
				ruleApplyProgress.setVisible(true);
				ruleApplyLabel.setText("Applying "+String.valueOf(newImportedRules.size())+" new imported rules on "+String.valueOf(ftc.currentList_STATIC.size())+" items.");
				
				List<ItemFetcherRow> databaseSyncLists = new ArrayList<ItemFetcherRow>();
				ArrayList<GenericClassRule> grs = new ArrayList<GenericClassRule>();
				ArrayList<ArrayList<String[]>> itemRuleMaps = new ArrayList<ArrayList<String[]>>();
				ArrayList<Boolean> activeStatuses = new ArrayList<Boolean>();
				ArrayList<String> METHODS = new ArrayList<String>();
				ManualRuleServices.i = 0.0;
				Task<Void> task = new Task<Void>() {
				    
					@Override
				    protected Void call() throws Exception {

						
						double ruleNumber = newImportedRules.parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()).size();
						
						ManualRuleServices.StreamRulesOnItemFetcherRows(ruleNumber, newImportedRules.parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()),
								ftc.currentList_STATIC.parallelStream()
								.filter(row -> reevaluateClassifiedItems || !(((ItemFetcherRow) row).getDisplay_segment_id()!=null) ).collect(Collectors.toList())
								,ruleApplyProgress,
								null
								,account
								,databaseSyncLists
								,grs
								,itemRuleMaps
								,activeStatuses
								,METHODS);
								
						
						
						return null;
					}
					};
				task.setOnSucceeded(e -> {
					;
					
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							ruleApplyProgress.setProgress(1);
							ruleApplyIndicator.setVisible(false);
							ruleApplyLabel.setText("Import success.");
							rules_table.getItems().forEach(r->r.getReferentData().setDisable(false));
							
						}
						
					});
					ConfirmationDialog.showRuleImportConfirmation("Confirm rule import", String.valueOf(databaseSyncLists.size())+" previously unclassified items have been classified. Do you wish to save?", "Yes, save results", "No, discard", this, account, grs, itemRuleMaps, activeStatuses, METHODS, databaseSyncLists,DataInputMethods.IMPORTED_CLASSIFICATION_RULE);
					
					});

				task.setOnFailed(e -> {
				    Throwable problem = task.getException();
				    /* code to execute if task throws exception */
				    problem.printStackTrace(System.err);
				    ruleApplyProgress.setProgress(1);
					ruleApplyIndicator.setVisible(false);
					ruleApplyLabel.setText("Import failed.");
					rules_table.getItems().forEach(r->r.getReferentData().setDisable(false));
				    
				});

				task.setOnCancelled(e -> {
				    /* task was cancelled */
					ruleApplyProgress.setProgress(1);
					ruleApplyIndicator.setVisible(false);
					ruleApplyLabel.setText("Import failed.");
					rules_table.getItems().forEach(r->r.getReferentData().setDisable(false));
					;
				});
					
					Thread thread = new Thread(task);; thread.setDaemon(true);
					thread.setName("Importing rules");
					thread.start();
			}else {
				//No items
				ruleApplyProgress.setProgress(1);
				ruleApplyIndicator.setVisible(false);
				ruleApplyLabel.setText("No items in project.");
				rules_table.getItems().forEach(r->r.getReferentData().setDisable(false));
			}
			
				
		}else {
			//No rules
			ruleApplyProgress.setProgress(1);
			ruleApplyIndicator.setVisible(false);
			ruleApplyLabel.setText("No new rules to apply.");
			rules_table.getItems().forEach(r->r.getReferentData().setDisable(false));
		}
		
	}
	
	
	//Adds to a specific button a listener that keeps it disabled until all its dependency text fields are filled
	private void ADD_FILL_LISTENER(Button target,boolean isTaxo) {
		//Initialize the button to disabled at first
		target.setDisable(true);
		
		//For every dependency text field
		for(TextField dependency:DEPENDENT_FIELDS.get(target)) {
			//Create a change listener that calls (CHECK_FILL) whenever the value changes
			dependency.textProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable,
			            String oldValue, String newValue) {
			    	CHECK_FILL(target,isTaxo);
			    }
			});
		}
	}

	//Called whenever a dependency text field's value changes
	protected void CHECK_FILL(Button target, boolean isTaxo) {
		//Initialize a temporary variable to 1;
		int test = 1;
		//For every text field in the dependency list
		for(TextField dependency:DEPENDENT_FIELDS.get(target)) {
			//If the text field is shown to the user
			if(dependency.isVisible()) {
				try {
					//Multiply the temporary variable by the lenghth of the text field
					test=test*dependency.getText().length();
				}catch(Exception V) {
					target.setDisable(true);
					return;
				}
			}
		}
		//If the target button is the item upload "Apply" button
		if(!isTaxo) {
			//If the temporary variable is now zero disable the button (at least one dependency is empty) else if the DATA_FILE_MISSING is true (no file upload has been made yet) disable the button, enable otherwise
			target.setDisable(test==0?true:this.DATA_FILE_MISSING);
		//Else if the target button is the taxonomy upload "Apply" button
		}else {
			//If the temporary variable is now zero disable the button (at least one dependency is empty) else if the TAXO_FILE_MISSING is true (no file upload has been made yet) disable the button, enable otherwise
			target.setDisable(test==0?true:this.TAXO_FILE_MISSING);
		}
		
	}
	
	
	//Long block of instructions to initialize the combo boxes appearance, content and behavior
	private void prefill_combo_boxes() {
		//Set the dependent text field for the "Apply" button in the item upload section
		//Add the fill listener on the "Apply" button in the item upload section: call the ADD_FILL_LISTENER procedure
		DEPENDENT_FIELDS.put(apply_data, new ArrayList<>(Arrays.asList(aidColumn,SDDColumn,LDDColumn,MGColumn,SDCColumn,LDCColumn)));
		ADD_FILL_LISTENER(apply_data,false);
		//Set the dependent text field for the "Apply" button in the classification system upload section
		//Add the fill listener on the "Apply" button in the classification system upload section: call the ADD_FILL_LISTENER procedure
		DEPENDENT_FIELDS.put(apply_taxo, new ArrayList<>(Arrays.asList(nameL1,nameL2,nameL3,nameL4,L1ID,L1NameD,L1NameC,L2ID,L2NameD,L2NameC,L3ID,L3NameD,L3NameC,L4ID,L4NameD,L4NameC)));
		ADD_FILL_LISTENER(apply_taxo,true);
		//Set the dependent text field for the "Apply" button in the general information section
		//Add the fill listener on the "Apply" button in the general information section upload section: call the ADD_FILL_LISTENER procedure
				
		apply_general.setDisable(true);
		projectName.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
		    	check_apply();
		    }
		});
		
		targetQuality.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
		    	check_apply();
		    }
		});
		
		manualQuality.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
		    	check_apply();
		    }
		});
		
		//Add the known roles to the profile_box combo box
		profile_box.getItems().addAll(ROLES);
		//Set the profile_box appearance
		profile_box.setMinWidth(250);;
		
		
		//Add the known languages to the data language combo box
		dataLanguage.getItems().addAll(LANGUAGES.keySet());
		//Add a change listener on the data language combo box
		dataLanguage.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	for(String lang:LANGUAGES.keySet()) {
            		//	Every time the value changes set the LanguageID in the ProjectTemplate data structure
            		//	Call the load_all_special procedure
            		//	Call the check_apply procedure
            		if(lang.equals(newValue)) {
            			prj.setLanguageID(LANGUAGES.get(lang));
            			load_all_special(prj);
            			check_apply();
            			break;
            		}
            	}
                
            }
        });
		
		//Add the known languages to the classifier language combo box
		classifierLanguage.getItems().addAll(LANGUAGES.keySet());
		//Add a change listener on the classifier language combo box
		classifierLanguage.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	//	Every time the value changes Call the check_apply procedure
        		
            	check_apply();
            }
        });
		//Add the known translator to the online translators combo box
		//Add a change listener on the online translators combo box
		//		Every time the value changes Call the check_apply procedure
		
		onlineTranslator.getItems().addAll(TRANSLATORS.keySet());
		onlineTranslator.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	check_apply();
            }
        });
		//Add the known classification levels to the project granularity combo box
				//Add a change listener on the project granularity combo box
				//		Every time the value changes Call the check_apply procedure
				//		Every time the value changes Call the sync_classification_level_definition procedure
		
		
		classificationLevels.getItems().addAll(LEVELS);
		classificationLevels.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	sync_classification_level_definition(newValue);
            	check_apply();
            }
        });
		//Add the known classifications systems to the taxonomy name combo box
		HashSet<String> tmpTaxos = new HashSet<String>();
		tmpTaxos.addAll(TAXOS.values());
		taxoName.getItems().addAll(tmpTaxos);
		
	}

	
	//Called whenever a field in the general pane changes value (used to enable or disable the corresponding "Apply" Button)
	protected void check_apply() {
		try{
			//initialize a temporary value to 1
			//multiply this value by the length of projectName, dataLanguage, classifierLanguage, onlineTranslator, classificationLevels, targetQuality, manualQuality
			int test = projectName.getText().length()*
					dataLanguage.getValue().length()*
					classifierLanguage.getValue().length()*
					onlineTranslator.getValue().length()*
					classificationLevels.getValue().length()*
					targetQuality.getText().length()*
					manualQuality.getText().length();
			//try to parse the quality fields as decimal numbers
			//if it fails or the values lie outside of 0,100, set the test value to 0
			
			try {
				
				targetQuality.setStyle("-fx-border-color: red;");
				manualQuality.setStyle("-fx-border-color: red;");
				
				double tq = Double.parseDouble(targetQuality.getText());
				targetQuality.setStyle("-fx-border-color: white;");
				
				double mq = Double.parseDouble(manualQuality.getText());
				manualQuality.setStyle("-fx-border-color: white;");
				
				if(tq<=100 && tq>=0) {
					if(mq<=100 && mq>=0) {
						
					}else {
						test = 0;
						manualQuality.setStyle("-fx-border-color: red;");
					}
				}else {
					test=0;
					targetQuality.setStyle("-fx-border-color: red;");
					manualQuality.setStyle("-fx-border-color: red;");
				}
			}catch(Exception V) {
				apply_general.setDisable(true);
				return;
			}
			
			
			
			//If the test value is 0 , disable the button, else enable it
					apply_general.setDisable(test==0);
					return;
					
					
		}catch(Exception V) {
			apply_general.setDisable(true);
			return;
		}
				
		
	}
	
	

	//Hides the inappropriate column mapping fields when the project granularity changes
	@SuppressWarnings("static-access")
	protected void sync_classification_level_definition(String newValue) {
		;
		//For every row in the classification grid pane between 2 and 15 included
		//	If the row number is supperior to 2 + 3 * granularity , hide the row
		for(Node noeud:taxo_grid.getChildren()) {
    		noeud.setVisible(true);
    		if(taxo_grid.getRowIndex(noeud)>1 && taxo_grid.getRowIndex(noeud)<15) {
    			try {
    			if(taxo_grid.getRowIndex(noeud)>((Integer.parseInt(newValue)*3)+2)) {
    				noeud.setVisible(false);
    			}else {
    				noeud.setVisible(!TAXO_FILE_MISSING);
    			}
    			}catch(Exception V) {
    				noeud.setVisible(false);
    			}
    		}
    	}
		try {
			taxoFile.setVisible(taxoName.getValue().length()>0?true:false);
			taxoFileLabel.setVisible(taxoName.getValue().length()>0?true:false);
		}catch(Exception V) {
			taxoFile.setVisible(false);
			taxoFileLabel.setVisible(false);
		}
		
	}

	//Usefull function to dispatch the "animated" property from the accordion to the child panes

	private void set_panes_animated() {
		//For every pane in the accordion, set the pane to animated
		GENERAL.setAnimated(true);
		CLASSIF.setAnimated(true);
		DATA.setAnimated(true);
		RULES.setAnimated(true);
		SPECIAL.setAnimated(true);
		USERS.setAnimated(true);
	}


    public void update_login_pane() {
    	
    	
    	
    	//Create a temporary array
		ArrayList<String> tmp2 = new ArrayList<String>();
		//Store the user_id in the first slot of the array
		tmp2.add(this.account.getUser_id());
		//Store the profile value in the second slot of the array
		tmp2.add("Project Manager");
		//Store the temporary array in the LOGINS field of the ProjectTemplate data structure
		this.prj.getLOGINS().put(this.account.getUser_name(),tmp2);
		
    	CREDENTIALS = new HashMap<String,String>();
    	
		String title_template = label_template_title.getStyle();
		String body_template = label_template_body.getStyle();
		String delete_template = delete_login_button.getStyle();
		String box_template = login_box.getStyle();
		
		login_pane.getChildren().clear();
		int data_length = 10;
		
		for(int row=0;row<data_length+6;row++) {
			
			for(int column=0;column<9;column++) {
				if(row==0) {
					login_pane.add(new Label(""), column, row);
				}else if (row==1) {
					if(column==1) {
						Label lgn = new Label("LOGIN");
						lgn.setStyle(title_template);
						login_pane.add(lgn, column, row);
					}else if(column==3) {
						Label pwd = new Label("Password");
						pwd.setStyle(title_template);
						//login_pane.add(pwd, column, row);
					}else if (column==5) {
						Label pfl = new Label("PROJECT PROFILE");
						pfl.setStyle(title_template);
						login_pane.add(pfl, column, row);
					}else if (column==8) {
						//login_pane.add(apply_project, column, row);
					}
				}else if (row==2) {
					login_pane.add(new Label(""), column, row);
				}else if (row<13) {
					if(column==1) {
						String text = null;
						try {
							text = (String) this.prj.getLOGINS().keySet().toArray()[row-3];
						} catch(Exception e) {
							text = null;
						}
						if(text!=null) {
							Label lgn = new Label(text);
							lgn.setStyle(body_template);
							
							
							Label pwd = new Label(this.prj.getLOGINS().get(text).get(0));
							pwd.setStyle(body_template);
							
							
							ComboBox<String> pfl = new ComboBox<String>();
							pfl.setMaxHeight(15);
							//pfl.setMaxWidth(20);
							pfl.getItems().addAll(ROLES);
							pfl.setValue(this.prj.getLOGINS().get(text).get(1));
							pfl.setStyle(box_template);
							pfl.getStylesheets().add(getClass().getResource("/Styles/ComboBoxGrey.css").toExternalForm());
							pfl.setMaxHeight(28);
							pfl.setMinHeight(28);
							pfl.setPrefHeight(28);
							pfl.setMinWidth(1.0 * profile_box.getWidth());
							pfl.setMaxWidth(1.0 * profile_box.getWidth());
							pfl.setPrefWidth(1.0 * profile_box.getWidth());
							
							pfl.valueProperty().addListener(new ChangeListener<String>() {

								@SuppressWarnings("static-access")
								@Override
								public void changed(ObservableValue <? extends String> arg0, String arg1, String arg2) {

									ArrayList<String> tmp = prj.getLOGINS().get(CREDENTIALS.get((login_pane.getRowIndex(lgn)-0)+","+login_pane.getColumnIndex(lgn)));
									tmp.set(1, pfl.getValue());
									prj.getLOGINS().put(CREDENTIALS.get((login_pane.getRowIndex(lgn)-0)+","+login_pane.getColumnIndex(lgn)),tmp);
									pfl.setStyle(box_template);
									pfl.getStylesheets().add(getClass().getResource("/Styles/ComboBoxGrey.css").toExternalForm());
									try {
										update_users_and_specials();
									} catch (ClassNotFoundException | SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
								
							});
							
							Button btn = new Button("Remove");
							btn.setOnAction(new EventHandler<ActionEvent>() {

								@SuppressWarnings("static-access")
								@Override
								public void handle(ActionEvent e) {
									prj.getLOGINS().remove(CREDENTIALS.get((login_pane.getRowIndex(lgn)-0)+","+login_pane.getColumnIndex(lgn)));
									login_box.getItems().add(CREDENTIALS.get((login_pane.getRowIndex(lgn)-0)+","+login_pane.getColumnIndex(lgn)));
									update_login_pane();
								}
								
							});
							btn.setStyle(delete_template);
							btn.getStylesheets().add(getClass().getResource("/Styles/ButtonGrey.css").toExternalForm());
							btn.setMaxHeight(28);
							btn.setMinHeight(28);
							btn.setPrefWidth(28);
							//btn.setAlignment(value);
							
							login_pane.add(lgn, column, row);
							//login_pane.add(pwd, column+2, row);
							login_pane.add(pfl, column+4, row);
							login_pane.add(btn, column+6, row);
							
							CREDENTIALS.put(row+","+column, text);
							
						}else {
							login_pane.add(new Label(""), column, row);
						}
						
					}
				}else if(row==13) {
					login_pane.add(new Label(""), column, row);
				}else if(row==14) {
					if(column==1) {
						login_pane.add(login_box, column, row);
						//login_pane.add(passwd_box,column+2,row);
						login_pane.add(profile_box,column+4,row);
						profile_box.setMinWidth(250);;
						
						
						
						login_pane.add(add_button,column+6,row);
					}
					
				}else {
					login_pane.add(new Label(""), column, row);
				}
			}
		}
		login_pane.getRowConstraints().clear();
		login_pane.getRowConstraints().addAll(rc_login);
		
		Platform.runLater(new Runnable() {
		    @Override public void run() {
		    	try {
					update_users_and_specials();
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		
	}
	
    // Refreshes the special grid pane content
	public void update_special_pane() {
		//Empty the SPECIAL_WORDS variable
		SPECIAL_WORDS = new HashMap<String,String>();
		
		//Make a backup of the label style
		String label_style = label_template.getStyle();
		//Make a backup of the button style
		String button_style = btn_template.getStyle();
		//Make a backup of the combo box style
		String cb_style = cb_template.getStyle();
		special_pane.getColumnConstraints();
		//Clear all content from the special grid pane
		special_pane.getChildren().clear();
		//Set the maximum data length as the maximum size between the FOR_WORDS, STOP_WORDS of the ProjectTemplate data structure (this maximum size is helpfull in establishing the correct row constraints for the grid pane)
		int data_length = Math.max(this.prj.getFOR_WORDS().size(), Math.max(this.prj.getDW_WORDS().size(), this.prj.getSTOP_WORDS().size()));
		data_length = 12;
		//We loop (data length + 6) times in order to create the required number of rows within the special grid pane. For each row:
		for(int row=0;row<data_length+6;row++) {
		//	For each column from 1 to 16
			for(int column=0;column<16;column++) {
				if(row==0) {
		//		If it's the first row , we create empty labels
					special_pane.add(new Label(""), column, row);
				}else if (row==1) {
		//		If it's the second row
					if(column==1) {
		//			If it's the second column we create the "Application words" title
						special_pane.add(new Label("'Application' words"), column, row);
					}else if(column==6) {
		//			If it's the seventh column we create the "Drawing words" title
						special_pane.add(new Label("'Drawing' words"), column, row);
					}else if (column==11) {
		//			If it's the twelfth column we create the "Stop words" title
						special_pane.add(new Label("'Stop' words"), column, row);
					}else if(column==15) {
							//special_pane.add(apply_words, column, row);
					}
				}else if (row==2) {
		//		If it's the third row , we create empty labels
					special_pane.add(new Label(""), column, row);
				}else if (row<15) {
		//		If it's the forth row and beyond
					if(column==1 || column==6 || column==11) {
		//			if the column is 2, 7 or 12
		//				Empty the TARGET variable
						LinkedHashSet<String> TARGET = new LinkedHashSet<String>();						
						if(column==1) {
		//					If it's the second column add to the TARGET variable all the known for words
							TARGET.addAll(this.prj.getFOR_WORDS().keySet());
						}else if(column==6) {
		//					If it's the sixth column add to the TARGET variable all the known drawing words
							TARGET.addAll(this.prj.getDW_WORDS().keySet());
						}else {
		//					Else (column 12) add to the TARGET variable all the known stop words
							TARGET.addAll(this.prj.getSTOP_WORDS().keySet());
						}
		//				Initialize a temporary text variable to null
						String text = null;
						try {
		//				Try to set the temporary text variable to the element contained within the TARGET variable at the (row-3)-th position. If it fails this means there's such element at this position and leave the variable null
							text = (String) TARGET.toArray()[row-3];
						} catch(Exception e) {
							text = null;
						}
						
		//				If the text variable isn't null		
						if(text!=null) {
		//					Create the label and set its text field to the text variable
							Label lbl = new Label(text);
		//					Set the label's style to the back up label style
							lbl.setStyle(label_style);
		//					Add to the special grid pane at the current column and row the created label
							special_pane.add(new Label(text), column, row);
							
							
							if(column==1) {
								
								
								
							}
		//					Create the checkbox
							CheckBox cb = new CheckBox();
		//					If it's column 2 and the boolean associated, in the FOR_WORDS field within the ProjectTemplate data structure, to the text variable is true (meaning this project specifically uses this FOR word instead of it just being associated to the selected language), set the combo box to selected, else deselect it
							if(column==1 && prj.getFOR_WORDS().get(text)) {						
								cb.setSelected(true);
		//					If it's column 7 and the boolean associated, in the DW_WORDS field within the ProjectTemplate data structure, to the text variable is true (meaning this project specifically uses this DRAWING word instead of it just being associated to the selected language), set the combo box to selected, else deselect it
							}else if(column==6 && prj.getDW_WORDS().get(text)) {
								cb.setSelected(true);
		//					If it's column 12 and the boolean associated, in the FOR_WORDS field within the ProjectTemplate data structure, to the text variable is true (meaning this project specifically uses this STOP word instead of it just being associated to the selected language), set the combo box to selected, else deselect it
							}else if(column==11 && prj.getSTOP_WORDS().get(text)){
								cb.setSelected(true);
							}
		//					If it's column 2
							if(column==1) {
								cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
        //						Add a change listener to the checkbox
							        @SuppressWarnings("static-access")
									@Override
							        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							            // TODO Auto-generated method stub
							            if(newValue){
	    //							If the listener detects combo box changed to selected, it adds the label text to the FOR_WORDS field within the ProjectTemplate data structure and associate the boolean true to it					
							            	prj.getFOR_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), true);
		//							Else (the listener detects combo box changed to unselected), add the label text to the FOR_WORDS field within the ProjectTemplate data structure and associate the boolean false to it		    	
							            }else{
							            	prj.getFOR_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), false);
									    	
							            }
							            Platform.runLater(new Runnable() {
							    		    @Override public void run() {
		//                           Call the (update_users_and_specials) routine					    		    	
							    		    	try {
													update_users_and_specials();
												} catch (ClassNotFoundException | SQLException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
							    		    }
							    		});
							        }
							    });
							}else if(column==6) {
		//					If it's column 7
								cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
		//						Add a change listener to the checkbox
							        @SuppressWarnings("static-access")
									@Override
							        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							            // TODO Auto-generated method stub
							        	if(newValue){
		//							If the listener detects combo box changed to selected, it adds the label text to the DW_WORDS field within the ProjectTemplate data structure and associate the boolean true to it
							            	prj.getDW_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), true);
		//							Else (the listener detects combo box changed to unselected), add the label text to the DW_WORDS field within the ProjectTemplate data structure and associate the boolean false to it							    	
							            }else{
							            	prj.getDW_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), false);
									    	
							            }
							        	Platform.runLater(new Runnable() {
							    		    @Override public void run() {
		//                           Call the (update_users_and_specials) routine					    		    	
							    		    	try {
													update_users_and_specials();
												} catch (ClassNotFoundException | SQLException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
							    		    }
							    		});
							        }
							    });
							}else {
								cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
        //					If it's column 12
		//						Add a change listener to the checkbox
							        @SuppressWarnings("static-access")
									@Override
							        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							            // TODO Auto-generated method stub
							        	if(newValue){
        //							If the listener detects combo box changed to selected, it adds the label text to the STOP_WORDS field within the ProjectTemplate data structure and associate the boolean true to it	
							            	prj.getSTOP_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), true);
									    	
							            }else{
							            	prj.getSTOP_WORDS().put(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)), false);
	    //							Else (the listener detects combo box changed to unselected), add the label text to the STOP_WORDS field within the ProjectTemplate data structure and associate the boolean false to it								    	
							            }
							        	Platform.runLater(new Runnable() {
							    		    @Override public void run() {
	    //                           Call the (update_users_and_specials) routine  	
							    		    	try {
													update_users_and_specials();
												} catch (ClassNotFoundException | SQLException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
							    		    }
							    		});
							        }
							    });
							}
		//                  Set the combo box style to the back up style
							cb.setStyle(cb_style);
							cb.setMaxHeight(17);
							cb.setMaxWidth(17);
		//                  Add to the special pane the combo box at the corresponding row and add the corresponding column + 2
							special_pane.add(cb, column+2, row);
		//                  Add to the SPECIAL_WORDS variables the current text variable and its coordinates
							SPECIAL_WORDS.put(row+","+(column+2), text);
		//                  Create button with an "X" as the text field
							Button btn = new Button("X");
		//                  Set the button's style to the backup style
							btn.setStyle(button_style);
							btn.setAlignment(Pos.CENTER_RIGHT);
		//                  If it's the first column
							if(column==1) {
		//                  	Add to the button a trigger that removes the contained text in the SPECIAL_WORDS variable at the current coordinates from the FOR_WORDS field in the ProjectTemplate data structure then launches the (update_special_pane) procedure
								btn.setOnAction(new EventHandler<ActionEvent>() {
								    @SuppressWarnings("static-access")
									@Override public void handle(ActionEvent e) {
								    	//prj.getFOR_WORDS().remove(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)));
								    	remove_public_special(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)),1);
								    	update_special_pane();
								    }
								    });
		//                  If it's the 7th column
							}else if(column==6){
		//                  	Add to the button a trigger that removes the contained text in the SPECIAL_WORDS variable at the current coordinates from the DW_WORDS field in the ProjectTemplate data structure then launches the (update_special_pane) procedure
								btn.setOnAction(new EventHandler<ActionEvent>() {
								    @SuppressWarnings("static-access")
									@Override public void handle(ActionEvent e) {
								    	//prj.getDW_WORDS().remove(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)));
								    	remove_public_special(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)),6);
								    	update_special_pane();
								    }
								    });
							}else {
		//                  If it's the 12th column
								btn.setOnAction(new EventHandler<ActionEvent>() {
		//                  	Add to the button a trigger that removes the contained text in the SPECIAL_WORDS variable at the current coordinates from the STOP_WORDS field in the ProjectTemplate data structure then launches the (update_special_pane) procedure
								    @SuppressWarnings("static-access")
									@Override public void handle(ActionEvent e) {
								    	//prj.getSTOP_WORDS().remove(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)));
								    	remove_public_special(SPECIAL_WORDS.get(special_pane.getRowIndex(cb)+","+special_pane.getColumnIndex(cb)),11);
								    	update_special_pane();
								    }
								    });
							}
							
		//                  Set the button's style to the corresponding css class					
							btn.getStylesheets().add(getClass().getResource("/Styles/CloseButtonRed.css").toExternalForm());
							btn.setMaxHeight(20);
							btn.setMaxWidth(20);
		//                  Add the button to the special pane at the corresponding row and to the corresponding column +3
							special_pane.add(btn, column+3, row);
						}else {
							special_pane.add(new Label(""), column, row);
						}
					}
					
					
					
		//		If we are at row 16 add the corresponding text fields and add_buttons : for_field, for_button, dw_field, dw_button, stop_field, stop_button	
				}else if(row==15) {
					special_pane.add(new Label(""), column, row);
				}else if(row==16) {
					if(column==1) {
						special_pane.add(for_field, column, row);
						special_pane.add(for_button,column+2,row);
					}
					if(column==6) {
						special_pane.add(dw_field, column, row);
						special_pane.add(dw_button,column+2,row);
					}
					if(column==11) {
						special_pane.add(stop_field, column, row);
						special_pane.add(stop_button,column+2,row);
					}
					
				}else {
					special_pane.add(new Label(""), column, row);
				}
			}
		}
		//     Remove all row constraints for the special grid pane
		special_pane.getRowConstraints().clear();
		//     Set the row constraints to the back up row constraints
		special_pane.getRowConstraints().addAll(rc_special);
		//special_pane.getColumnConstraints().clear();
		//special_pane.getColumnConstraints().addAll(cc_special);
		
		//     Run the (update_user_and_specials()) routine
		
		Platform.runLater(new Runnable() {
		    @Override public void run() {
		    	try {
					update_users_and_specials();
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		
	}
	
	

	protected void remove_public_special(String special_term, int column) {
		
		
		
		
		
		
		try {
			
			
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();

			if(column==1) {
				this.prj.getFOR_WORDS().remove(special_term);
				stmt.execute("delete from public_ressources.terms where term_name ='"+special_term+"' and application_term_status and data_language ='"+this.prj.getLanguageID()+"'");
				
			}else if(column==11) {
				this.prj.getSTOP_WORDS().remove(special_term);
				stmt.execute("delete from public_ressources.terms where term_name ='"+special_term+"' and stop_term_status and data_language ='"+this.prj.getLanguageID()+"'");
				
			}else {
				this.prj.getDW_WORDS().remove(special_term);
				stmt.execute("delete from public_ressources.terms where term_name ='"+special_term+"' and drawing_term_status and data_language ='"+this.prj.getLanguageID()+"'");
				
			}
			
			stmt.close();
			conn.close();
			
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void update_users_and_specials() throws ClassNotFoundException, SQLException {
		
		
		 HashMap<String,LinkedHashSet<String>> tmp = new  HashMap<String,LinkedHashSet<String>>();
		 
		 LinkedHashSet<String> temp = new LinkedHashSet<String>();
		 for(String tp:this.prj.getFOR_WORDS().keySet()) {
			 if(this.prj.getFOR_WORDS().get(tp)) {
				 temp.add(tp);
			 }
		 }
		 tmp.put("FOR", temp);
		 
		 temp = new LinkedHashSet<String>();
		 for(String tp:this.prj.getSTOP_WORDS().keySet()) {
			 if(this.prj.getSTOP_WORDS().get(tp)) {
				 temp.add(tp);
			 }else {
				 
			 }
			 
		 }
		 tmp.put("STOP",temp);
		 
		 temp = new LinkedHashSet<String>();
		 for(String tp:this.prj.getDW_WORDS().keySet()) {
			 if(this.prj.getDW_WORDS().get(tp)) {
				 temp.add(tp);
			 }else {
				 
			 }
		 }
		 tmp.put("DW",temp);
		 
		 
		 Connection conn = Tools.spawn_connection();
			try {
				Tools toolbox = new Tools();
				toolbox.create_project_schema(this.prj.getPid());
				//#
				PreparedStatement stmt = conn.prepareStatement("delete from "+this.prj.getPid()+".project_terms");
				
				stmt.execute();
				stmt.close();
				stmt = conn.prepareStatement("insert into "+this.prj.getPid()+".project_terms values(?,?,?,?,?)");
				//Setting the special words
				for(String key:tmp.keySet()) {
					//Setting the application words
					if(key.equals("FOR")) {
						for(String word:tmp.get(key)) {
							stmt.setString(1, Tools.generate_uuid());
							stmt.setString(2, word);
							stmt.setBoolean(3, false);
							stmt.setBoolean(4, true);
							stmt.setBoolean(5, false);
							stmt.addBatch();
						}
					}
					//Setting the stop words
					if(key.equals("STOP")) {
						for(String word:tmp.get(key)) {
							stmt.setString(1, Tools.generate_uuid());
							stmt.setString(2, word);
							stmt.setBoolean(3, true);
							stmt.setBoolean(4, false);
							stmt.setBoolean(5, false);
							stmt.addBatch();
						}
					}
					//Setting the stop words
					if(key.equals("DW")) {
						for(String word:tmp.get(key)) {
							stmt.setString(1, Tools.generate_uuid());
							stmt.setString(2, word);
							stmt.setBoolean(3, false);
							stmt.setBoolean(4, false);
							stmt.setBoolean(5, true);
							stmt.addBatch();
						}
					}
				}
				//#
				stmt.executeBatch();
				stmt.close();
				
				//Setting the users
				
				//Set the current user as project manager, always	
					//Create a temporary array
					ArrayList<String> tmp2 = new ArrayList<String>();
					//Store the user_id in the first slot of the array
					tmp2.add(this.account.getUser_id());
					//Store the profile value in the second slot of the array
					tmp2.add("Project Manager");
					//Store the temporary array in the LOGINS field of the ProjectTemplate data structure
					this.prj.getLOGINS().put(this.account.getUser_name(),tmp2);
					;
					;
					this.account.getUser_projects().put(this.prj.getPid(), "Project Manager");
					;
				
				stmt = conn.prepareStatement("delete from administration.users_x_projects where project_id='"+this.prj.getPid()+"' and user_project_profile is not null");
				//#
				stmt.execute();
				stmt = conn.prepareStatement("insert into administration.users_x_projects values(?,?,?,?)");
				for(String login:this.prj.getLOGINS().keySet()) {
					stmt.setString(1, this.prj.getLOGINS().get(login).get(0));
					stmt.setString(2, this.prj.getPid());
					stmt.setString(3, this.prj.getLOGINS().get(login).get(1));
					stmt.setBoolean(4, false);
					stmt.addBatch();
				}
				//#
				stmt.executeBatch();
				stmt.close();
				//*
				stmt = conn.prepareStatement("insert into administration.projects (project_id,last_edit_date) values (?,?) on conflict(project_id) do update set last_edit_date = ?"); 
				stmt.setString(1, this.prj.getPid());
				stmt.setDate(2, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				stmt.setDate(3, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				//#
				stmt.execute();
				stmt.close();
				conn.close();
				 
				  
			} catch (SQLException e) {
				ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
				e.printStackTrace();
				return;
			}
			
			 
			 
			 
		 
		 
	}
	//Stores the project in the database
	private void save_project() throws SQLException {
		//Creates a connection
		Connection conn = null;
		try {
			conn = Tools.spawn_connection();
		} catch (ClassNotFoundException | SQLException e) {
			ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
			return;
		}
		Tools toolbox = new Tools();
		toolbox.create_project_schema(this.prj.getPid());
		//Prepares the fields to be uploaded
		PreparedStatement stmt = conn.prepareStatement("insert into administration.projects(project_id,project_name,data_language,classifier_language,translator_id,number_of_levels,target_quality,manual_quality,classification_system_id,last_edit_date,privacy_status,current_quality,suppression_status,classification_system_name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?) on conflict(project_id) do update set  project_name=?, data_language=?, classifier_language=?, translator_id=?, number_of_levels=?, target_quality=?, manual_quality=?, classification_system_id=?, last_edit_date=?, privacy_status=?, current_quality=?, suppression_status=?, classification_system_name=?");

		//Sets up the fields to be uploaded
		 stmt.setString(1,this.prj.getPid());
		 stmt.setString(2, projectName.getText());
		 stmt.setString(15, projectName.getText());
		 
		 try{
			 stmt.setString(3, LANGUAGES.get(dataLanguage.getValue()));
			 stmt.setString(16, LANGUAGES.get(dataLanguage.getValue()));
		 }catch(Exception v) {
			 stmt.setString(3, dataLanguage.getValue());
			 stmt.setString(16, dataLanguage.getValue());
		 }
		 try{
			 stmt.setString(4, LANGUAGES.get(classifierLanguage.getValue()));
			 stmt.setString(17, LANGUAGES.get(classifierLanguage.getValue()));
		 }catch(Exception v) {
			 stmt.setString(4, classifierLanguage.getValue());
			 stmt.setString(17, classifierLanguage.getValue());
		 }
		 try{
			 stmt.setString(5, TRANSLATORS.get(onlineTranslator.getValue()));
			 stmt.setString(18, TRANSLATORS.get(onlineTranslator.getValue()));
		 }catch(Exception v) {
			 stmt.setString(5, onlineTranslator.getValue());
			 stmt.setString(18, onlineTranslator.getValue());
		 }
		 stmt.setInt(6, Integer.parseInt(classificationLevels.getValue()));
		 stmt.setInt(19, Integer.parseInt(classificationLevels.getValue()));
		 
		 stmt.setDouble(7, Double.parseDouble(targetQuality.getText()));
		 stmt.setDouble(20, Double.parseDouble(targetQuality.getText()));
		 
		 stmt.setDouble(8, Double.parseDouble(manualQuality.getText()));
		 stmt.setDouble(21, Double.parseDouble(manualQuality.getText()));
		 
		 try {
			update_users_and_specials();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 stmt.setString(9, this.prj.getTaxoID());
		 stmt.setString(22, this.prj.getTaxoID());
		 
		 
		 stmt.setDate(10, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
		 stmt.setDate(23, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
		 
		 stmt.setBoolean(11, true);
		 stmt.setBoolean(24, true);
		 
		 stmt.setDouble(12, 0.0);
		 stmt.setDouble(25, 0.0);
		 
		 stmt.setBoolean(13, false);
		 stmt.setBoolean(26, false);
		 
		 stmt.setString(14, this.prj.getTaxoName());
		 stmt.setString(27, this.prj.getTaxoName());
		 
		 //#
		 stmt.execute();
		 stmt.close();
		 conn.close();
		 
		 			
		    
			
	}


	//Stores the uploaded item file as the items corresponding to the current project
	public ArrayList<String> save_data(ProgressBar progressBar) throws SQLException {
		System.out.println("saving items");
		ArrayList<String> affectedItemIDs = new ArrayList<String>();
		//If the CIDColumn text field is not empty and no granularity selected, stop upload
		try {
			if(CIDColumn.getText().length()>0 && (Integer.parseInt(classificationLevels.getValue())<1 || Integer.parseInt(classificationLevels.getValue())>4) ) {
				ExceptionDialog.show("Project classification levels not defined", "To be able to upload item classes, you first have to define the project's classification system levels", "");
				return null;
			}
		}catch(Exception V) {
			if(CIDColumn.getText().length()>0 ) {
				ExceptionDialog.show("Project classification levels not defined", "To be able to upload item classes, you first have to define the project's classification system levels", "");
				return null;
			}
		}
		
		
		//If the CIDColumn text field is not empty and no classification system entered stop upload
		try {
			if(CIDColumn.getText().length()>0 && !(this.prj.getTaxoName().length()>0) ) {
				ExceptionDialog.show("Project classification system not defined", "To be able to upload item classes, you first have to define the project's classification system", "");
				return null;
			}
		}catch(Exception V) {
			if(CIDColumn.getText().length()>0 ) {
				ExceptionDialog.show("Project classification system not defined", "To be able to upload item classes, you first have to define the project's classification system", "");
				return null;
			}
		}
		
		
		
		//Call the get_data_column_map procedure
		DataColumnMap = get_data_column_map(DataColumnMap);
		Tools toolbox = new Tools();
		toolbox.create_project_schema(this.prj.getPid());
		String tableName = this.prj.getPid()+".project_items";
		
		//Call the transversal.data_exchange_toolbox.SpreadsheetUpload.loadSheetInDatabase
		try {
			String cidcolumn = null;
			try {
				cidcolumn = CIDColumn.getText().toUpperCase();
			}catch(Exception V) {
				
			}
			Integer granularity = null;
			try{
				granularity = Integer.parseInt(classificationLevels.getValue());
			}catch(Exception V) {
				
			}
			HashSet<String> failedcid = new HashSet<String>();
			affectedItemIDs = SpreadsheetUpload.streamSheetInDatabase(tableName, DATAFILE.getAbsolutePath(), DataColumnMap,keep_data_button.isSelected(),cidcolumn,granularity,failedcid,	account,progressBar);
			failedcid = new HashSet<String>(failedcid.stream().filter(c->c!=null).collect(Collectors.toSet()));
			if(failedcid.size()>0) {
				ExceptionDialog.show("Unknown classification numbers", "There's a total of "+String.valueOf(failedcid.size())+" classes that are not known in the classification system ", String.join("\n", failedcid));
			}
		} catch (ClassNotFoundException e) {
			ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
		} catch (FileNotFoundException e) {
			ExceptionDialog.show("IO001 taxo_file", "IO001 taxo_file", "IO001 taxo_file");
			
		} catch (IOException e) {
			ExceptionDialog.show("IO000 permission_error", "IO000 permission_error", "IO000 permission_error");
		}
		//Return an operation status to confirm success otherwise throw error dialog
		return affectedItemIDs;
		
	}

	//Stores the uploaded taxonomy file as the classification system corresponding to the current project
	private String save_taxo() throws SQLException {
		Tools toolbox = new Tools();
		toolbox.create_project_schema(this.prj.getPid());
		if(REUSING_OLD_TAXO) {
			SpreadsheetUpload.CopySheetFromDatabase(this.prj.getTaxoID(),this.prj.getPid()+".project_segments");
			return this.prj.getPid()+".project_segments";
		}
		
		
		
		
		//Call the get_taxo_column_map procedure
		TaxoColumnMap = get_taxo_column_map(TaxoColumnMap);
		String tableName = this.prj.getPid()+".project_segments";
		
		//Call the transversal.data_exchange_toolbox.SpreadsheetUpload.loadSheetInDatabase
		try {
			SpreadsheetUpload.streamSheetInDatabase(tableName, taxoFile.getText(), TaxoColumnMap,false,null, Integer.MIN_VALUE,null,account,null);
		} catch (ClassNotFoundException e) {
			ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
		} catch (FileNotFoundException e) {
			ExceptionDialog.show("IO001 data_file", "IO001 data_file", "IO001 data_file");
			
		} catch (IOException e) {
			ExceptionDialog.show("IO000 permission_error", "IO000 permission_error", "IO000 permission_error");
		}
		//Return an operation status to confirm success otherwise throw error dialog
		return tableName;
	}
	
	//Triggered when the user clicks on the taxonomy file location textfield
	@FXML private void browse_taxo() {
		Platform.runLater(new Runnable() {
		    @Override public void run() {
		    	//Create a file browser to allow the file to select the file he wishes to upload
		    	FileChooser fileChooser = new FileChooser();

                //Set extension filter in order to only be able to select excel workbooks
                FileChooser.ExtensionFilter extFilter = 
                        new FileChooser.ExtensionFilter("Excel workbooks (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show the file browser
                File file = fileChooser.showOpenDialog(taxoName.getScene().getWindow());
                //If the user successfully loads the file
                if (file != null) {
                	//  Set the taxonomy file location text field to the selected file's location
                    taxoFile.setText(file.getAbsolutePath());
                    //  Set the taxonomy file location text field to uneditable
                    taxoFile.setEditable(false);
                    REUSING_OLD_TAXO=false;
                    refreshRuleSet(REUSING_OLD_TAXO);
                    //  Set the TAXO_FILE_MISSING variable to false
                    TAXO_FILE_MISSING=false;
                    //  Call the CHECK_FILL procedure
                    CHECK_FILL(apply_taxo,true);
                //Else (the user cancelled the upload or has no accessing rights over the file, etc..
                }else {
                	//  Empty the taxonomy name text field
                	taxoName.setValue("");
                	//  Empty the taxonomy file location text field
                	taxoFile.setText("Select File");
                	//  Set the TAXO_FILE_MISSING variable to true
                	TAXO_FILE_MISSING=true;
                	//  Call the CHECK_FILL procedure
                	CHECK_FILL(apply_taxo,true);
                }
                
                //Call the (sync_classification_level_definition) procedure
                for(Node noeud:taxo_grid.getChildren()) {
                	noeud.setVisible(true);
                }
                String a = "4";
                try {
                	a=classificationLevels.getValue();
                }catch(Exception v) {
                	
                }
                sync_classification_level_definition(a);
		}});
	}



	//Usefull function to get to fill the DataColumnMap Variable from the user input
	private LinkedHashMap<String, String> get_data_column_map(LinkedHashMap<String, String> DataColumnMap) {
		DataColumnMap.put(aidColumn.getText().toUpperCase(), "client_item_number");
		DataColumnMap.put(SDDColumn.getText().toUpperCase(), "short_description");
		DataColumnMap.put(LDDColumn.getText().toUpperCase(), "long_description");
		DataColumnMap.put(MGColumn.getText().toUpperCase(), "material_group");
		DataColumnMap.put(PCColumn.getText().toUpperCase(), "pre_classification");
		DataColumnMap.put(SDCColumn.getText().toUpperCase(), "short_description_translated");
		DataColumnMap.put(LDCColumn.getText().toUpperCase(), "long_description_translated");
		
		return DataColumnMap;
	}

	//Usefull function to get to fill the TaxoColumnMap Variable from the user input
	private LinkedHashMap<String, String> get_taxo_column_map(LinkedHashMap<String, String> TaxoColumnMap) {
		TaxoColumnMap.put(L1ID.getText().toUpperCase(), "level_1_number");
		TaxoColumnMap.put(L1NameD.getText().toUpperCase(), "level_1_name");
		TaxoColumnMap.put(L1NameC.getText().toUpperCase(), "level_1_name_translated");
		TaxoColumnMap.put(L2ID.getText().toUpperCase(), "level_2_number");
		TaxoColumnMap.put(L2NameD.getText().toUpperCase(), "level_2_name");
		TaxoColumnMap.put(L2NameC.getText().toUpperCase(), "level_2_name_translated");
		TaxoColumnMap.put(L3ID.getText().toUpperCase(), "level_3_number");
		TaxoColumnMap.put(L3NameD.getText().toUpperCase(), "level_3_name");
		TaxoColumnMap.put(L3NameC.getText().toUpperCase(), "level_3_name_translated");
		TaxoColumnMap.put(L4ID.getText().toUpperCase(), "level_4_number");
		TaxoColumnMap.put(L4NameD.getText().toUpperCase(), "level_4_name");
		TaxoColumnMap.put(L4NameC.getText().toUpperCase(), "level_4_name_translated");
		
		return TaxoColumnMap;
	}

	public void setUserAccount(UserAccount account) {
		Tools.decorate_menubar(menubar, account);
		this.account = account;

		Connection connX;
		try {
			connX = Tools.spawn_connection();
			Statement stmt = connX.createStatement();
			//#
			//Set the LOGINS variable with the appropriate available user logins
			ResultSet rs = stmt.executeQuery("select user_name,user_id from administration.users where user_id!='"+this.account.getUser_id()+"'");
			while(rs.next()) {
				LOGINS.put(rs.getString("user_name"),rs.getString("user_id"));
			}
			
			//Add all the known logins to the login combo box
			login_box.getItems().addAll(LOGINS.keySet());
			//Set the login combo box text field to non editable
			login_box.setEditable(false);
			
			rs.close();
			stmt.close();
			connX.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	public void setTargetProject(Project selected_project) {
	}
	
}
