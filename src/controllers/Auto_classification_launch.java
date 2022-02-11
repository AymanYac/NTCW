package controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.*;
import service.ConcurentTask;
import service.TimeMasterTemplate;
import transversal.data_exchange_toolbox.AutoClassificationBenchmark;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Auto_classification_launch {

	//Custom data structure to store for all the rows in the classification_progress screen the corresponding time management data
	private TimeMasterTemplate timeTemplate;
	//Stores the current project's project_id
	private String pid;
	//Stores the known mappings language_id <-> language_name
	private HashMap<String, String> LANGUAGES;
	//Stores the list of available projects to use as reference for rule building
	private LinkedList<RuleSet> RULES = new LinkedList<RuleSet>();
	//Stores the list of project ids currently selected by the users for classification
	private HashSet<String> REFERENCE_PROJECTS = new HashSet<String>();
	//Stores the list of project ids currently selected by the users for pre-classification
	private HashSet<String> PRECLASSIFICATION_REFERENCE_PROJECTS = new HashSet<String>();
	//Stores the active project's cardinality
	private Integer target_desc_cardinality;
	//Stores the classification/pre-classification parameters used by the user or automatically calculated
	private BinaryClassificationParameters binaryClassificationParameters ;
	//Stores the abacus values used to automatically calculate optimal classification/pre-classification parameters
	private ArrayList<AbaqueRow> ABAQUE = new ArrayList<AbaqueRow>(1024) ;
	
	//Stores the active project's classification system id
	private String taxoID;
	//Stores the mappings classification reference_projects <-> granularities
	protected HashMap<String, Integer> PRECLASSIFICATION_GRANULARITY = new HashMap<String,Integer>();
	//Stores the mappings pre-classification reference_projects <-> granularities
	protected HashMap<String, Integer> CLASSIFICATION_GRANULARITY = new HashMap<String,Integer>();
	//Stores the active project's desired quality
	private double target_desc_accuracy;
	//Stores the expected classification coverage
	double classification_coverage;
	//Stores the expected pre-classification coverage
	double preclassification_coverage;
	//Stores the expected classification quality
	double classification_quality;
	//Stores the expected pre-classification coverage
	double preclassification_quality;
	//Stores the calculated classification minimum accuracy parameter
	private Double Classification_ta;
	//Stores the calculated classification minimum base parameter
	private Integer Classification_tb;
	//Stores the calculated pre-classification minimum accuracy parameter
	private Double PreClassification_ta;
	//Stores the calculated pre-classification minimum base parameter
	private Integer PreClassification_tb;
	//Stores the classification build size
	private double ClassificationBS;
	//Stores the pre-classification build size
	private double PreClassificationBS;
	//Stores the benchmark controller
	private AutoClassificationBenchmark benchmark_controller;
	//Stores the active project's language_id
	private String languageID;
	//Stores the user's account details
	private UserAccount account;
	
	
	//Binds to the screen's classification table
	@FXML
	private TableView<RuleSet> classfication_table;
	//Binds to the screen's classification language combo box
	@FXML
	private ComboBox<String> autoclass_lang_combo;
	//Binds to the screen's pre-classification language combo box
	@FXML
	private ComboBox<String> preclass_lang_combo;
	//Binds to the screen's pre-classification taxonomy combo box
	@FXML
	private ComboBox<String> preclass_taxo_combo;
	@FXML
	//Binds to the screen's classification label
	private Label class_taxo_combo;
	
	@FXML
	//Binds to the screen's classification table project column
	private TableColumn<?, ?> Classification_projectColumn;
	@FXML
	//Binds to the screen's classification table language column
	private TableColumn<?, ?> Classification_languageColumn;
	@FXML
	//Binds to the screen's classification taxonomy column
	private TableColumn<?, ?> Classification_taxoColumn;
	@FXML
	//Binds to the screen's classification cardinality column
	private TableColumn<?, ?> Classification_cardColumn;
	@FXML
	//Binds to the screen's classficiation check box column
	private TableColumn<?, ?> Classification_checkboxColumn;
	
	
	@FXML
	//Binds to the screen's pre-classfication table
	private TableView<RuleSet> preclassfication_table;
	@FXML
	//Binds to the screen's pre-classification project column
	private TableColumn<?, ?> preClassification_projectColumn;
	@FXML
	//Binds to the screen's pre-classification language column
	private TableColumn<?, ?> preClassification_languageColumn;
	@FXML
	//Binds to the screen's pre-classification taxonomy column
	private TableColumn<?, ?> preClassification_taxoColumn;
	@FXML
	//Binds to the screen's pre-classification cardinality column
	private TableColumn<?, ?> preClassification_cardColumn;
	@FXML
	//Binds to the screen's pre-classification check box column
	private TableColumn<?, ?> preClassification_checkboxColumn;
	@FXML
	//Binds to the screen's graph
	private StackedBarChart<String, Number> graph;
	@FXML
	//Binds to the screen's x axis
	private CategoryAxis xAxe;
	@FXML
	//Binds to the screen's y axis
	private NumberAxis yAxe;
	@FXML
	//Binds to the graph's classification label
	private Label classifLabel;
	@FXML
	//Binds to the graph's classification label
	private Label preclassifLabel;
	@FXML
	public
	//Binds to the screen's estimation label
	Label estimated;
	@FXML
	//Binds to the screen's "Launch classification" Button
	private Button launch_button;
	Stage parametersScene;
	private ConcurentTask triple_dots_task;
	public Auto_classification_parameters parametersController;
	@FXML MenuBar menubar;
	
	
	@FXML TextFlow classifTextFlow;
	@FXML TextFlow preclassifTextFlow;
	private ConcurentTask triple_dots_tables;

	
	@FXML void clicking_hand() {
		menubar.getScene().setCursor(Cursor.HAND);
	}
	
	@FXML void noclicking_hand() {
		menubar.getScene().setCursor(Cursor.DEFAULT);
	}
	
	
	public void setPid(String pid) {
		this.pid=pid;
	}
	
	@FXML void detailed_settings() {
		//Stage stage = (Stage) launch_button.getScene().getWindow();
	    //stage.close();
	    
		if(this.parametersScene!=null) {
			parametersController.setPid(this.pid);
			parametersController.setRefenceProjects(REFERENCE_PROJECTS);
			parametersController.setTarget_desc_cardinality(this.target_desc_cardinality);
			parametersController.setClassificationBS(this.ClassificationBS);
			parametersController.setPreClassificationBS(this.PreClassificationBS);
			
			parametersScene.show();
			return;
		}
		
		
	    try {
		    Stage primaryStage = new Stage();
		    
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Auto_classification_parameters.fxml"));
			AnchorPane root = fxmlLoader.load();

			Scene scene = new Scene(root,400,400);
			
			primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
			primaryStage.setScene(scene);
			//primaryStage.setMinHeight(768);
			//primaryStage.setMinWidth(1024);
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			primaryStage.show();
			
			this.parametersScene = primaryStage;
			controllers.Auto_classification_parameters controller = fxmlLoader.getController();
			this.parametersController = controller;
			
			controller.setUserAccount(account);
			
		    controller.setPid(this.pid);
			controller.setRefenceProjects(REFERENCE_PROJECTS);
			controller.setTarget_desc_cardinality(this.target_desc_cardinality);
			controller.setClassificationTa(this.Classification_ta);
			controller.setPreClassificationTa(this.PreClassification_ta);
			controller.setClassficationTb(this.Classification_tb);
			controller.setPreClassificationTb(this.PreClassification_tb);
			controller.setClassificationBS(this.ClassificationBS);
			;
			controller.setPreClassificationBS(this.PreClassificationBS);
			;
			controller.parent = this;
			try{
				controller.setClassificationGranularity(Collections.min(this.CLASSIFICATION_GRANULARITY.values()));
			}catch(Exception V) {
				controller.setClassificationGranularity(4);
			}
			try {
				controller.setPreClassificationGranularity(Collections.min(this.PRECLASSIFICATION_GRANULARITY.values()));
			}catch(Exception W) {
				controller.setPreClassificationGranularity(4);
			}
			
			
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 Auto_class_parameters", "FX001 Auto_class_parameters", "FX001 Auto_class_parameters");
			e.printStackTrace();
		}
	}
	//Binds to the screen's estimted time label. called to simulate a benchmark for time duration estimation
	@FXML void simulate() {
		
		
		try {
			benchmark_controller.cleanTask.cancel();
			benchmark_controller.mainTask.cancel();
			benchmark_controller.RuleGenTask.cancel();
			benchmark_controller.TargetTask.cancel();
		}catch(Exception V) {
			try {
				benchmark_controller.mainTask.cancel();
				benchmark_controller.RuleGenTask.cancel();
				benchmark_controller.TargetTask.cancel();
			}catch(Exception V1) {
				try {
					benchmark_controller.RuleGenTask.cancel();
					benchmark_controller.TargetTask.cancel();
				}catch(Exception V2) {
					try {
						benchmark_controller.TargetTask.cancel();
					}catch(Exception V3) {
						
					}
				}
			}
		}
		
		
		
		
		
		//create the classification benchmark controller
		benchmark_controller = new AutoClassificationBenchmark();
		//Let the controller know its parent (the auto classification launch controller)
		benchmark_controller.parent = this;
		//Set the active project for the benchmark controller
		benchmark_controller.setPid(this.pid);
		//Set the target cardinality for the benchmark controller
		benchmark_controller.setTarget_desc_cardinality(this.target_desc_cardinality);
		
		
		if(this.binaryClassificationParameters!=null) {
			;
			benchmark_controller.setConfig(this.binaryClassificationParameters);
			try{
				benchmark_controller.binaryClassificationParameters.setClassif_granularity(Collections.min(this.CLASSIFICATION_GRANULARITY.values()));
			}catch(Exception V) {
				benchmark_controller.binaryClassificationParameters.setClassif_granularity(4);
			}
			try {
				benchmark_controller.binaryClassificationParameters.setPreclassif_granularity(Collections.min(this.PRECLASSIFICATION_GRANULARITY.values()));
			}catch(Exception W) {
				benchmark_controller.binaryClassificationParameters.setPreclassif_granularity(4);
			}
			
			
			this.binaryClassificationParameters = benchmark_controller.binaryClassificationParameters;
			
			
		}else {
			;
			benchmark_controller.setConfig(new BinaryClassificationParameters());
			
			if(Classification_ta!=null) {
				benchmark_controller.binaryClassificationParameters.setClassif_Ta(Classification_ta);
			}else {
				benchmark_controller.binaryClassificationParameters.setClassif_Ta(GlobalConstants.MIN_TA);
			}
			if(Classification_tb!=null) {
				benchmark_controller.binaryClassificationParameters.setClassif_Tb(Classification_tb);
			}else {
				benchmark_controller.binaryClassificationParameters.setClassif_Tb(GlobalConstants.MIN_TC);
			}
			if(PreClassification_ta!=null) {
				benchmark_controller.binaryClassificationParameters.setPreclassif_Ta(PreClassification_ta);
			}else {
				benchmark_controller.binaryClassificationParameters.setPreclassif_Ta(GlobalConstants.MIN_TA);
			}
			if(PreClassification_tb!=null) {
				benchmark_controller.binaryClassificationParameters.setPreclassif_Tb(PreClassification_tb);
			}else {
				benchmark_controller.binaryClassificationParameters.setPreclassif_Tb(GlobalConstants.MIN_TC);
			}
			
			try{
				benchmark_controller.binaryClassificationParameters.setClassif_granularity(Collections.min(this.CLASSIFICATION_GRANULARITY.values()));
			}catch(Exception V) {
				benchmark_controller.binaryClassificationParameters.setClassif_granularity(4);
			}
			try {
				benchmark_controller.binaryClassificationParameters.setPreclassif_granularity(Collections.min(this.PRECLASSIFICATION_GRANULARITY.values()));
			}catch(Exception W) {
				benchmark_controller.binaryClassificationParameters.setPreclassif_granularity(4);
			}
			
			
			this.binaryClassificationParameters = benchmark_controller.binaryClassificationParameters;
			
			
		}
		
		//Launch the benchmark
		benchmark_controller.setRefenceProjectsSIMULATION(REFERENCE_PROJECTS,PRECLASSIFICATION_REFERENCE_PROJECTS);
		
		
		//Launch the triple dots
		try {
			triple_dots_task.stop();
			triple_dots_task = new ConcurentTask(this);
		}catch(Exception V) {
			triple_dots_task = new ConcurentTask(this);
		}
		
		
		
	}
	
	//Triggered when the user click the "Launch classification" button
	@FXML void launch() {
		Stage stage = (Stage) launch_button.getScene().getWindow();
	    
	    
	    try {
	    	//Create new window
		    Stage primaryStage = new Stage();
		    //Load the progress screen's controller
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Auto_classification_progress.fxml"));
			AnchorPane root = fxmlLoader.load();
			//Set the new window with the controller
			Scene scene = new Scene(root,400,400);
			//Set the window's title
			primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
			primaryStage.setScene(scene);
			//Set the window's size
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			//Show the window
			primaryStage.show();
			//Set the time template on the controller
			controllers.Auto_classification_progress controller = fxmlLoader.getController();
			controller.setTemplate(timeTemplate);
			//Set the user account details
			controller.setUserAccount(account);
			//Set the controller's active project id
			controller.setPid(this.pid);
			//Set the controller active project cardinality
			controller.setTarget_desc_cardinality(this.target_desc_cardinality);
			//If the user has already input classification/pre-classification parameters, use these
			if(this.binaryClassificationParameters!=null) {
				;
				controller.setConfig(this.binaryClassificationParameters);
				

				try{
					controller.binaryClassificationParameters.setClassif_granularity(Collections.min(this.CLASSIFICATION_GRANULARITY.values()));
				}catch(Exception V) {
					controller.binaryClassificationParameters.setClassif_granularity(4);
				}
				try {
					controller.binaryClassificationParameters.setPreclassif_granularity(Collections.min(this.PRECLASSIFICATION_GRANULARITY.values()));
				}catch(Exception W) {
					controller.binaryClassificationParameters.setPreclassif_granularity(4);
				}

				this.binaryClassificationParameters = controller.binaryClassificationParameters;
				
				
				
				
			}else {
				//Else if no classification/pre-classification parameters are set (The user has no custom classification parameters, thus we use the default optimal parameters)
				;
				controller.setConfig(new BinaryClassificationParameters());
				
				if(Classification_ta!=null) {
					controller.binaryClassificationParameters.setClassif_Ta(Classification_ta);
				}else {
					controller.binaryClassificationParameters.setClassif_Ta(GlobalConstants.MIN_TA);
				}
				if(Classification_tb!=null) {
					controller.binaryClassificationParameters.setClassif_Tb(Classification_tb);
				}else {
					controller.binaryClassificationParameters.setClassif_Tb(GlobalConstants.MIN_TC);
				}
				if(PreClassification_ta!=null) {
					controller.binaryClassificationParameters.setPreclassif_Ta(PreClassification_ta);
				}else {
					controller.binaryClassificationParameters.setPreclassif_Ta(GlobalConstants.MIN_TA);
				}
				if(PreClassification_tb!=null) {
					controller.binaryClassificationParameters.setPreclassif_Tb(PreClassification_tb);
				}else {
					controller.binaryClassificationParameters.setPreclassif_Tb(GlobalConstants.MIN_TC);
				}
				

				try{
					controller.binaryClassificationParameters.setClassif_granularity(Collections.min(this.CLASSIFICATION_GRANULARITY.values()));
				}catch(Exception V) {
					controller.binaryClassificationParameters.setClassif_granularity(4);
				}
				try {
					controller.binaryClassificationParameters.setPreclassif_granularity(Collections.min(this.PRECLASSIFICATION_GRANULARITY.values()));
				}catch(Exception W) {
					controller.binaryClassificationParameters.setPreclassif_granularity(4);
				}

				this.binaryClassificationParameters = controller.binaryClassificationParameters;
				
				
				
				
			}
			
			//Set the classfication/pre-classification parameters
			controller.setRefenceProjects(REFERENCE_PROJECTS,PRECLASSIFICATION_REFERENCE_PROJECTS,true,this);
			
			//Close the current screen
			stage.close();
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 Auto_class_progress", "FX001 Auto_class_progress", "FX001 Auto_class_progress");
			e.printStackTrace();
		}
	}
	
	//Launches automatically at the window's launch
	@SuppressWarnings("rawtypes")
	@FXML void initialize(){
		Tools.decorate_menubar(menubar, account);
		
		ArrayList<TableView> tmp = new ArrayList<TableView>(2);
		tmp.add(classfication_table);
		tmp.add(preclassfication_table);
		try {
			triple_dots_tables.stop();
			triple_dots_tables = new ConcurentTask(tmp);
		}catch(Exception V) {
			triple_dots_tables = new ConcurentTask(tmp);
		}
		
		launch_button.setDisable(true);
		Task<Void> executeAppTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	
		    	//Donwload the known languages and store in the LANGUAGES variable
				try {
					LANGUAGES = WordUtils.load_languages();
				} catch (ClassNotFoundException e) {
					ExceptionDialog.show("CF001 pg_class", "CF001 pg_class", "CF001 pg_class");
					e.printStackTrace();
				} catch (SQLException e) {
					ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
					e.printStackTrace();
				}
				//Call the load_rules procedure
				//Call the fill_combos
				//Set the combo boxes to the first element
				
		    	
		    	
		        load_rules();

				fill_combos();
				Platform.runLater(new Runnable() {
				    public void run() {
				    	autoclass_lang_combo.getSelectionModel().select(0);
						preclass_lang_combo.getSelectionModel().select(0);
						preclass_taxo_combo.getSelectionModel().select(0);
				    }
				});
				return null;
		    }
		};
		executeAppTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			;
			
			try {
				triple_dots_tables.stop();
			}catch(Exception V) {
			}
			
			
			update_classification_table();
			update_preclassification_table();
		});

		executeAppTask.setOnFailed(e -> {
		    Throwable problem = executeAppTask.getException();
		    /* code to execute if task throws exception */
		    problem.printStackTrace(System.err);
		    ;
		});

		executeAppTask.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
		
		Thread thread = new Thread(executeAppTask);; thread.setDaemon(true);
		thread.setName("ReferenceProjectLoading");
		thread.start();
		
		graph.setAnimated(false);
		graph.setLegendVisible(false);
		graph.setHorizontalGridLinesVisible(false);
		graph.setVerticalGridLinesVisible(false);
		graph.setVerticalZeroLineVisible(false);
		graph.setHorizontalZeroLineVisible(false);
		graph.getYAxis().setTickMarkVisible(false);
		graph.getXAxis().setTickMarkVisible(false);
		graph.getYAxis().setTickLabelsVisible(false);
		graph.getXAxis().setTickLabelsVisible(false);
		classifLabel.setVisible(false);
		preclassifLabel.setVisible(false);
		
		
		
	}
	//Usefull function to fill the classification available reference projects
	private void update_classification_table() {
		//Empty the content of the classification reference projects
		classfication_table.getItems().clear();
		//Set column 1 to point to the project_name field in the RULES data structure
		Classification_projectColumn.setCellValueFactory(new PropertyValueFactory<>("project_name"));
		//Set column 2 to point to the language field in the RULES data structure
		Classification_languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
		//Set column 3 to point to the classification_system field in the RULES data structure
		Classification_taxoColumn.setCellValueFactory(new PropertyValueFactory<>("Classifcation_system"));
		//Set column 4 to point to the no_items field in the RULES data structure
		Classification_cardColumn.setCellValueFactory(new PropertyValueFactory<>("no_items"));
		//Set column 5 to point to the referent_data field in the RULES data structure
		Classification_checkboxColumn.setCellValueFactory(new PropertyValueFactory<>("referentData"));
		//For every known RULE in the RULES data strucutre
		LinkedList<RuleSet> tmp = new LinkedList<RuleSet>();
		for(RuleSet rule:RULES) {
			if(rule.getTaxoid().equals(taxoID))
				//	If the rule satisfies the language and classification system conditions set by the combo boxes, add the row to the table
			if( rule.getLanguage().equals(autoclass_lang_combo.getValue()) ||  autoclass_lang_combo.getValue().equals("All languages")) {
				tmp.add(rule);
			}
		}
		
		classfication_table.getItems().addAll(tmp);
		
	}
	
	private void update_preclassification_table() {
		
		preclassfication_table.getItems().clear();
		preClassification_projectColumn.setCellValueFactory(new PropertyValueFactory<>("project_name"));
		preClassification_languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
		preClassification_taxoColumn.setCellValueFactory(new PropertyValueFactory<>("Classifcation_system"));
		preClassification_cardColumn.setCellValueFactory(new PropertyValueFactory<>("no_items"));
		preClassification_checkboxColumn.setCellValueFactory(new PropertyValueFactory<>("referentData"));
		
		LinkedList<RuleSet> tmp = new LinkedList<RuleSet>();
		for(RuleSet rule:RULES) {
			try {
				//Add also the proposed autoclassification projects to the preclassification (true)
				if(!rule.getTaxoid().equals(taxoID)) {
					if( (rule.getLanguage().equals(preclass_lang_combo.getValue()) ||  preclass_lang_combo.getValue().equals("All languages")) && ( rule.getTaxoid().equals(preclass_taxo_combo.getValue()) || preclass_taxo_combo.getValue().equals("All classification systems") ) ) {
						tmp.add(rule);
					}
				}else {//(true)
					if( (rule.copy.getLanguage().equals(preclass_lang_combo.getValue()) ||  preclass_lang_combo.getValue().equals("All languages")) && ( rule.copy.getTaxoid().equals(preclass_taxo_combo.getValue()) || preclass_taxo_combo.getValue().equals("All classification systems") ) ) {
						tmp.add(rule.copy);
					}
				}
					
			}catch(Exception V) {
				
			}
			
		}
		
		preclassfication_table.getItems().addAll(tmp);
		
	}

	//Loads the known project's usable for reference for classification
	private void load_rules() throws ClassNotFoundException, SQLException {
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Connection conn3 = Tools.spawn_connection_from_pool();
		Statement ps3 = conn3.createStatement();
		//Set the taxoID variable
		//Set the languageID variable
		ResultSet rs3 = ps3.executeQuery("select classification_system_name,data_language from administration.projects where project_id='"+pid+"'");
		;
		rs3.next();
		this.taxoID = rs3.getString(1);
		this.languageID=rs3.getString(2);
		rs3.close();
		ps3.close();
		conn3.close();
		
		
		Connection conn = Tools.spawn_connection_from_pool();
		Statement ps = conn.createStatement();
		ResultSet rs = ps.executeQuery("select * from administration.projects where classification_system_name is not null and suppression_status=false");
		
		Connection conn2 = Tools.spawn_connection_from_pool();
		Statement ps2 = conn2.createStatement();
		//For every known projects
		while(rs.next()) {
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
			
			//Example query : select count(*) from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM u95a5fb608e6a47548432695ae0f78968.project_classification_event where item_id in (select distinct item_id from u95a5fb608e6a47548432695ae0f78968.project_items) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  u95a5fb608e6a47548432695ae0f78968.project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join u95a5fb608e6a47548432695ae0f78968.project_items on rich_events.item_id = project_items.item_id
			ResultSet rs2 = ps2.executeQuery("select count(*) from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM "+rs.getString("project_id")+".project_classification_event where item_id in (select distinct item_id from "+rs.getString("project_id")+".project_items) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  "+rs.getString("project_id")+".project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join "+rs.getString("project_id")+".project_items on rich_events.item_id = project_items.item_id");
			
			rs2.next();
			tmp.setProject_id(rs.getString("project_id"));
			tmp.setProject_name(rs.getString("project_name"));
			tmp.setLanguage_id(rs.getString("data_language"));
			tmp.setLanguage(LANGUAGES.get(tmp.getLanguage_id()));
			tmp.setTaxoid(rs.getString("classification_system_name"));
			tmp.setClassifcation_system(tmp.getTaxoid());
			if(rs2.getInt(1)==0) {
				//The reference project has no classified items
				rs2.close();
				continue;
			}
			tmp.setNo_items(Tools.formatThounsands(rs2.getInt(1)));
			tmp.setGranularity(rs.getInt("number_of_levels"));
			CheckBox cb = new CheckBox();
			//	If the project uses the same classification system (it can be used for classification)
			if(tmp.getTaxoid().equals(taxoID)) {
				//		Add a change listener whenever the check box cell changes states
				cb.selectedProperty().addListener(new ChangeListener<Boolean>() {

			        @Override
			        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			            if(newValue){
			                //			If the check box has been checked, add the project id to the classification reference projects
			            	//			And add the granularity to the classification reference granularities
			            	REFERENCE_PROJECTS.add(tmp.getProject_id());
			            	CLASSIFICATION_GRANULARITY.put(tmp.getProject_id(),tmp.getGranularity());
			            	;

			            }else{

			                //			If the check box has been unchecked, remove the project id from the classificaiton reference projects
			            	//			And remove the granularity from the classification reference granularities
			            	
			            	REFERENCE_PROJECTS.remove(tmp.getProject_id());
			            	CLASSIFICATION_GRANULARITY.remove(tmp.getProject_id());
			            	;
			            }
			            //		Call the update_classif_coverage routine
			            update_classif_coverage();
			        }
			    });
				
				//Create a copy of the current rule to be used for preclassification
				RuleSet tmp2 = new RuleSet(tmp);
				tmp2.getReferentData().selectedProperty().addListener(new ChangeListener<Boolean>() {
					//		Add a change listener whenever the check box cell changes states
					
			        @Override
			        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			            if(newValue){
			            	//			If the check box has been checked, add the project id to the pre-classification reference projects
			            	//			And add the granularity to the pre-classification reference granularities
			            	PRECLASSIFICATION_REFERENCE_PROJECTS.add(tmp.getProject_id());
			            	PRECLASSIFICATION_GRANULARITY.put(tmp.getProject_id(),tmp.getGranularity());
			            	;

			            }else{

			            	//			If the check box has been unchecked, remove the project id from the pre-classificaiton reference projects
			            	//			And remove the granularity from the pre-classification reference granularities
			            	
			            	PRECLASSIFICATION_REFERENCE_PROJECTS.remove(tmp.getProject_id());
			            	PRECLASSIFICATION_GRANULARITY.remove(tmp.getProject_id());
			            	;
			            }
			            //		Call the update_classif_coverage routine
			            update_preclassif_coverage();
			        }
			    });
				
				//Add the copy in the copy field of the RuleSet data structure
				tmp.copy = tmp2;
				
				
			}else {
				//	Else (the project uses a different classification system (it can only be used for preclassification))
				cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
					//		Add a change listener whenever the check box cell changes states
					
			        @Override
			        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			            if(newValue){
			            	//			If the check box has been checked, add the project id to the pre-classification reference projects
			            	//			And add the granularity to the pre-classification reference granularities
			            	PRECLASSIFICATION_REFERENCE_PROJECTS.add(tmp.getProject_id());
			            	PRECLASSIFICATION_GRANULARITY.put(tmp.getProject_id(),tmp.getGranularity());
			            	;

			            }else{

			            	//			If the check box has been unchecked, remove the project id from the pre-classificaiton reference projects
			            	//			And remove the granularity from the pre-classification reference granularities
			            	
			            	PRECLASSIFICATION_REFERENCE_PROJECTS.remove(tmp.getProject_id());
			            	PRECLASSIFICATION_GRANULARITY.remove(tmp.getProject_id());
			            	;
			            }
			            //		Call the update_classif_coverage routine
			            update_preclassif_coverage();
			        }
			    });
			}
			//Add the project to the known reference projects
			tmp.setReferentData(cb);
			RULES.add(tmp);
			//Close the connection
			rs2.close();
		};
		
		rs.close();
		ps.close();
		ps2.close();
		conn.close();
		conn2.close();
		
	}

	//Calculates the classification coverage after deciding the optimal parameter values
	protected void update_classif_coverage() {
		//Initialized the classification reference cardinality to 0
		int reference_cadinality = 0;
		//For every project in the classification reference adds its cardinality to the classification reference cardinality
		for(RuleSet rule:RULES) {
			if(REFERENCE_PROJECTS.contains(rule.getProject_id())) {
				reference_cadinality += Integer.parseInt(rule.getNo_items().replace(" ", ""));
			}
		}
		//Gets the build sample size: the ratio between the classification reference cardinality and the active project's cardinality
		double bs = (1.0*reference_cadinality)/this.target_desc_cardinality;
		//For every row in the abacus
		AbaqueRow best_config = null;
		for(AbaqueRow rw:ABAQUE) {
			//	If we reach the maximum abacus build sample, leave
			if(rw.getBs()>100*bs) {
				break;
			}
			//	If the abacus accuracy drops bellow the target accurary while we have met the build sample size, leave
			if(rw.getClass_accuracy()<this.target_desc_accuracy && rw.getBs()>=100*bs) {
				break;
			}
			//	If the current row satisfies the target accuray while meeting the build sample size, choose the current configuration as the best so far
			if(rw.getClass_accuracy()>=this.target_desc_accuracy) {
				best_config = rw;
			}
		}
		//If we have reached an optimal configuration
		if(best_config!=null) {
			;
			//	Set the controllers expected coverage based on this configuration
			this.classification_coverage = best_config.getCoverage();
			//	Set the controllers expected quality based on this configuration
			this.classification_quality = best_config.getClass_accuracy();
			//	Save the optimal parameters
			this.Classification_ta = best_config.getTa();
			this.Classification_tb = best_config.getTc();
			try {
				this.binaryClassificationParameters.setClassif_Ta(this.Classification_ta);
				this.binaryClassificationParameters.setClassif_Tb(this.Classification_tb);
			}catch(Exception V) {
				
			}
		}else {
		//Else set the coverage, the quality to 0, and discard the parameters
			this.classification_coverage = 0;
			this.classification_quality = 0;
			this.Classification_ta = null;
			this.Classification_tb = null;
			
			try {
				this.binaryClassificationParameters.setClassif_Ta(this.Classification_ta);
				this.binaryClassificationParameters.setClassif_Tb(this.Classification_tb);
			}catch(Exception V) {
				
			}
		}
		this.ClassificationBS = bs;
		//Call the update_chart routine
		update_chart();
		simulate();
	}

	//Updates the graphical chart when called
	@SuppressWarnings("unchecked") void update_chart() {
		
		//Defines the x axis of the label, its title and the associated data
		xAxe.setCategories(FXCollections.<String>observableArrayList(Arrays.asList
				   ("Estimated coverage using reference data")));
		//Prepare XYChart.Series objects by setting data 
		XYChart.Series<String, Number> series1 = new XYChart.Series<>(); 
		//Sets the series 1 name: Classification
		series1.setName("Classification (est. quality: "+(int)Math.ceil(this.classification_quality)+"%)");
		//Sets the series 1 data: CLassification
		
		//Decorated the graph
		decorate_graph();
		
		series1.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.classification_coverage)); 
		//Sets the series 2 name: Classification
		XYChart.Series<String, Number> series2 = new XYChart.Series<>(); 
		//Sets the series 2 data: Pre-classification
		series2.setName("Pre-classification (est. quality: "+(int)Math.ceil(this.preclassification_quality)+"%)");
		series2.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.preclassification_coverage-this.classification_coverage));
		//Disconnects the graph from its previous contents
		graph.getData().clear();
		//Reconnects the graph with the newly set up axis
		graph.getData().addAll(series1,series2);
		//Set the y axis to non auto ranging
		yAxe.setAutoRanging(false);
		//Set the y axis upper bound
		yAxe.setUpperBound(100);
		
		
	}

	private void decorate_graph() {
		classifLabel.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_coverage));
		classifTextFlow.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_coverage));
		classifTextFlow.setVisible(this.classification_coverage>10);
		classifLabel.setVisible(false);
		
		classifLabel.setText("Coverage: "+(int)Math.ceil(this.classification_coverage)+"%\nQuality "+(int)Math.ceil(this.classification_quality)+"%");
		Text text1 = new Text();
		text1.setText("Coverage: "+(int)Math.ceil(this.classification_coverage)+"%");
		text1.toFront();
		Text text2 = new Text();
		text2.setText("\nEst. quality: "+(int)Math.ceil(this.classification_quality)+"%");
		text2.toFront();
		classifTextFlow.getChildren().clear();
		
		text1.setStyle("-fx-font-weight	: bold");
		text1.setFill(classifLabel.getTextFill());
		
		text2.setFill(classifLabel.getTextFill());
		text2.setStyle("-fx-font-style: italic");
		
		
		//classifLabel.toFront();
		DoubleProperty fontSize = new SimpleDoubleProperty(10);
		fontSize.bind(yAxe.widthProperty().add(yAxe.heightProperty()).divide(60));
		classifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		classifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		classifTextFlow.getChildren().add(text1);
		classifTextFlow.getChildren().add(text2);
		
		preclassifLabel.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.preclassification_coverage));
		preclassifTextFlow.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.preclassification_coverage));
		preclassifLabel.setVisible(false);
		preclassifTextFlow.setVisible((this.preclassification_coverage-this.classification_coverage)>10);
		
		preclassifLabel.setText("Coverage: "+(int)Math.ceil(this.preclassification_coverage-this.classification_coverage)+"%\nQuality "+(int)Math.ceil(this.preclassification_quality)+"%");
		Text text3 = new Text();
		text3.setText("Coverage: "+(int)Math.ceil(this.preclassification_coverage-this.classification_coverage)+"%");
		Text text4 = new Text();
		text4.setText("\nEst. quality: "+(int)Math.ceil(this.preclassification_quality)+"%");
		preclassifTextFlow.toFront();
		//preclassifLabel.toFront();
		
		preclassifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		preclassifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		
		preclassifTextFlow.getChildren().clear();
		
		text3.setStyle("-fx-font-weight	: bold");
		text3.setFill(classifLabel.getTextFill());
		
		text4.setFill(classifLabel.getTextFill());
		text4.setStyle("-fx-font-style: italic");
		
		preclassifTextFlow.getChildren().add(text3);
		preclassifTextFlow.getChildren().add(text4);
	}

	protected void update_preclassif_coverage() {
		int reference_cadinality = 0;
		for(RuleSet rule:RULES) {
			if(PRECLASSIFICATION_REFERENCE_PROJECTS.contains(rule.getProject_id())) {
				reference_cadinality += Integer.parseInt(rule.getNo_items().replace(" ", ""));
			}
		}
		double bs = (1.0*reference_cadinality)/this.target_desc_cardinality;
		
		AbaqueRow best_config = null;
		for(AbaqueRow rw:ABAQUE) {
			if(rw.getBs()>100*bs) {
				break;
			}
			if(rw.getClass_accuracy()<this.target_desc_accuracy && rw.getBs()>=100*bs) {
				break;
			}
			if(rw.getClass_accuracy()>=this.target_desc_accuracy) {
				best_config = rw;
			}
		}
		if(best_config!=null) {
			;
			this.preclassification_coverage = best_config.getCoverage();
			this.preclassification_quality = best_config.getClass_accuracy();

			this.PreClassification_ta = best_config.getTa();
			this.PreClassification_tb = best_config.getTc();
			try {
				this.binaryClassificationParameters.setPreclassif_Ta(this.PreClassification_ta);
				this.binaryClassificationParameters.setPreclassif_Tb(this.PreClassification_tb);
			}catch(Exception V) {
				
			}
			
		}else {
			this.preclassification_coverage = 0;
			this.preclassification_quality = 0;
			this.PreClassification_ta = null;
			this.PreClassification_tb = null;
			try {
				this.binaryClassificationParameters.setPreclassif_Ta(this.PreClassification_ta);
				this.binaryClassificationParameters.setPreclassif_Tb(this.PreClassification_tb);
			}catch(Exception V) {
				
			}
		}
		
		this.PreClassificationBS = bs;
		update_chart();
		simulate();
	}
	//Fills the screen's combo box and sets their behaviour
	private void fill_combos() throws ClassNotFoundException, SQLException {
		//For every language known
		for(String lang:LANGUAGES.keySet()) {
			//	if the language matches the active project's language, or the user has chosen all languages
			if(lang.equals(this.languageID) || lang.equals("DUMMY_LANGUAGE_UUID")) {
				//	add the language to the combo boxes
				autoclass_lang_combo.getItems().add(LANGUAGES.get(lang));
				preclass_lang_combo.getItems().add(LANGUAGES.get(lang));
				
			}
		}
		//Add a change listener to the classification language combo box
		//	Every time a change happens, call the update_classification_table procedure
		autoclass_lang_combo.valueProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        update_classification_table();
		    }
		});
		
		//Add a change listener to the classification language combo box
		//	Every time a change happens, call the update_preclassification_table procedure
				
		preclass_lang_combo.valueProperty().addListener(new ChangeListener<String>() {

		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        update_preclassification_table();
		    }
		});
		
		
		
		Connection conn = Tools.spawn_connection_from_pool();
		Statement ps = conn.createStatement();
		ResultSet rs = ps.executeQuery("select project_id,classification_system_name from administration.projects");
		preclass_taxo_combo.getItems().add("All classification systems");
		preclass_taxo_combo.valueProperty().addListener(new ChangeListener<String>() {

		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        update_preclassification_table();
		    }
		});
		
		//Download all the mappings project_id<->classification systems
		//For every mapping
		while(rs.next()) {
			if(rs.getString(1).equals(pid)) {
				//	if the project_id matches the active project's
				//		skip
				
			}else {
				//	else, add the classification system to the combo boxes
				try {
					if(preclass_taxo_combo.getItems().contains(rs.getString(2))) {
						continue;
					}
					preclass_taxo_combo.getItems().add(rs.getString(2));
				}catch(Exception V) {
					//Null pointer
				}
				
			}
			
		}
		rs.close();
		//Fill the abacus rows
		rs = ps.executeQuery("select base_sample_size, class_accuracy,  rule_baseline_threshold, rule_accuracy_threshold,coverage from public_ressources.abacus_values");
		while(rs.next()) {
			AbaqueRow rw = new AbaqueRow();
			rw.setBs(rs.getInt(1));
			rw.setClass_accuracy(rs.getDouble(2));
			rw.setTc(rs.getInt(3));
			rw.setTa(rs.getDouble(4));
			rw.setCoverage(rs.getDouble(5));
			rw.setFamily_accuracy(0.0);
			ABAQUE.add(rw);
		}
		rs.close();
		ps.close();
		conn.close();
		
	}
	

	public void setTarget_desc_cardinality(Integer Target_desc_cardinality) {
		this.target_desc_cardinality = Target_desc_cardinality;
	}

	public void setRefenceProjects(HashSet<String> rEFERENCE_PROJECTS2) {
		this.REFERENCE_PROJECTS = rEFERENCE_PROJECTS2;
	}

	public void setConfig(BinaryClassificationParameters tmp) {
		this.binaryClassificationParameters=tmp;
	}

	public void setTarget_desc_accuracy(double target_desc_accuracy2) {
		;
		;
		this.target_desc_accuracy = target_desc_accuracy2;
	}
	//Triggered when the benchmark is done and we have input times to predict time duration
	public void ready2Launch(TimeMasterTemplate template, AutoClassificationBenchmark auto_classification_benchmark, HashMap<String, Double> RET) {
		
		triple_dots_task.stop();
		
		Double download_time = RET.get("this fetch");
		Double clean_time=0.0;
		for(String key:RET.keySet()) {
			if(key.contains("clean") && RET.get(key)>clean_time) {
				clean_time = RET.get(key);
			}
		}
		
		double reference_cadinality = 0;
		boolean hasclassif = false;
		boolean haspreclassif = false;
		for(RuleSet rule:RULES) {
			if(REFERENCE_PROJECTS.contains(rule.getProject_id()) ) {
				hasclassif = true;
				reference_cadinality += Integer.parseInt(rule.getNo_items().replace(" ", ""));
			}
			if(PRECLASSIFICATION_REFERENCE_PROJECTS.contains(rule.getProject_id())) {
				haspreclassif = true;
				reference_cadinality += Integer.parseInt(rule.getNo_items().replace(" ", ""));
			}
		}
		
		double number_of_time_cleaning_target=hasclassif?(haspreclassif?2:1):(haspreclassif?1:0);
		
		download_time = download_time * (2.0*this.target_desc_cardinality+reference_cadinality) / 5000.0;
		clean_time = clean_time * (number_of_time_cleaning_target*this.target_desc_cardinality+reference_cadinality) / 5000.0;
		
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		;
		launch_button.setDisable(false);
		Duration estimated_time = Duration.ofNanos((long) (download_time+clean_time));
		;
		estimated.setText("Estimated time: "+Tools.formatDuration(estimated_time));
		try {
			parametersController.estimated.setText("Estimated time: "+Tools.formatDuration(estimated_time));
		}catch(Exception V) {
			
		}
		
		
		//Empties the benchmark controller to free memory
		auto_classification_benchmark=null;
		//Sets the timeTemplate variable to the template produced by the benchmark
		timeTemplate = template;
		
		
		
		
		
		
	}

	public void setExpectedTime(double expected_time, Auto_classification_progress secondary_auto_classification_controller) {
		launch_button.setDisable(false);
		estimated.setText("Estimated time: "+Tools.formatDuration(Duration.ofNanos((long) expected_time)));
		try {
			parametersController.estimated.setText("Estimated time: "+Tools.formatDuration(Duration.ofNanos((long) expected_time)));
		}catch(Exception V) {
			
		}
		secondary_auto_classification_controller=null;
	}

	public void setUserAccount(UserAccount account) {
		Tools.decorate_menubar(menubar, account);
		this.account=account;
	}

	public void setTargetProject(Project activated_project) {
	}
	
	
	
}
