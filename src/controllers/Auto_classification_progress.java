package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.GlobalConstants;
import model.AbaqueRow;
import model.BinaryClassificationParameters;
import model.DataInputMethods;
import model.DescriptionFetchRow;
import model.GenericClassRule;
import model.UserAccount;
import service.Bigram;
import service.ConfusionMatrixReader;
import service.CorpusReader;
import service.DownloadSegmenter;
import service.ManualRuleServices;
import service.TimeMaster;
import service.TimeMasterTemplate;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.SpellCorrector;
import transversal.language_toolbox.WordUtils;

public class Auto_classification_progress {
	
	private String pid;
	
	public void setPid(String pidx) {
		this.pid=pidx;
	}
	
	@FXML private Button download_button;
	@FXML private ProgressBar download_progress;
	@FXML private ProgressBar auto_classif_progress;
	@FXML private ProgressBar reference_desc_progress;
	@FXML private ProgressBar ref_char_clean_progress;
	@FXML private ProgressBar ref_abv_clean_progress;
	@FXML private ProgressBar ref_spell_clean_progress;
	@FXML private ProgressBar rule_gen_progress;
	@FXML private ProgressBar word_gen_progress;
	@FXML private ProgressBar rule_set_progress;
	@FXML private ProgressBar target_desc_progress;
	@FXML private ProgressBar target_char_clean_progress;
	@FXML private ProgressBar target_abv_clean_progress;
	@FXML private ProgressBar target_spell_clean_progress;
	@FXML private ProgressBar rule_app_progress;
	@SuppressWarnings("rawtypes")
	@FXML private StackedBarChart actualChart;
	
	@FXML private ProgressBar preclass_download_progress;
	@FXML private ProgressBar preclass_auto_classif_progress;
	@FXML private ProgressBar preclass_reference_desc_progress;
	@FXML private ProgressBar preclass_ref_char_clean_progress;
	@FXML private ProgressBar preclass_ref_abv_clean_progress;
	@FXML private ProgressBar preclass_ref_spell_clean_progress;
	@FXML private ProgressBar preclass_rule_gen_progress;
	@FXML private ProgressBar preclass_word_gen_progress;
	@FXML private ProgressBar preclass_rule_set_progress;
	@FXML private ProgressBar preclass_target_desc_progress;
	@FXML private ProgressBar preclass_target_char_clean_progress;
	@FXML private ProgressBar preclass_target_abv_clean_progress;
	@FXML private ProgressBar preclass_target_spell_clean_progress;
	@FXML private ProgressBar preclass_rule_app_progress;
	
	
	
	@FXML GridPane grille;
	@FXML private StackedBarChart<String, Number> graph;
	@FXML private CategoryAxis xAxe;
	@FXML private NumberAxis yAxe;
	
	@FXML private StackedBarChart<String, Number> actualgraph;
	@FXML private CategoryAxis actualxAxe;
	@FXML private NumberAxis actualyAxe;
	
	
	@FXML
	//Binds to the graph's classification label
	private Label static_classifLabel;
	@FXML
	//Binds to the graph's classification label
	private Label static_preclassifLabel;
	
	@FXML
	//Binds to the graph's classification label
	private TextFlow static_classifTextFlow;
	@FXML
	//Binds to the graph's classification label
	private TextFlow static_preclassifTextFlow;
	
	
	@FXML
	//Binds to the graph's classification label
	private Label dynamic_classifLabel;
	@FXML
	//Binds to the graph's classification label
	private Label dynamic_preclassifLabel;
	
	
	
	@FXML Label time11;
	@FXML Label time12;
	@FXML Label time13;
	
	@FXML Label time21;
	@FXML Label time22;
	@FXML Label time23;
	
	@FXML Label time31;
	@FXML Label time32;
	@FXML Label time33;
	
	@FXML Label time41;
	@FXML Label time42;
	@FXML Label time43;
	
	@FXML Label time51;
	@FXML Label time52;
	@FXML Label time53;
	
	@FXML Label time61;
	@FXML Label time62;
	@FXML Label time63;
	
	@FXML Label time71;
	@FXML Label time72;
	@FXML Label time73;
	
	@FXML Label time81;
	@FXML Label time82;
	@FXML Label time83;
	
	@FXML Label time91;
	@FXML Label time92;
	@FXML Label time93;
	
	@FXML Label time101;
	@FXML Label time102;
	@FXML Label time103;
	
	@FXML Label time111;
	@FXML Label time112;
	@FXML Label time113;
	
	@FXML Label time121;
	@FXML Label time122;
	@FXML Label time123;
	
	@FXML Label time131;
	@FXML Label time132;
	@FXML Label time133;
	
	@FXML Label time141;
	@FXML Label time142;
	@FXML Label time143;
	
	@FXML Label time151;
	@FXML Label time152;
	@FXML Label time153;
	
	@FXML Label time161;
	@FXML Label time162;
	@FXML Label time163;
	
	@FXML Label time171;
	@FXML Label time172;
	@FXML Label time173;
	
	@FXML Label time181;
	@FXML Label time182;
	@FXML Label time183;
	
	@FXML Label time191;
	@FXML Label time192;
	@FXML Label time193;
	
	@FXML Label time201;
	@FXML Label time202;
	@FXML Label time203;
	
	@FXML Label time211;
	@FXML Label time212;
	@FXML Label time213;
	
	@FXML Label time221;
	@FXML Label time222;
	@FXML Label time223;
	
	@FXML Label time231;
	@FXML Label time232;
	@FXML Label time233;
	
	@FXML Label time241;
	@FXML Label time242;
	@FXML Label time243;
	
	@FXML Label time251;
	@FXML Label time252;
	@FXML Label time253;
	
	@FXML Label time261;
	@FXML Label time262;
	@FXML Label time263;
	
	@FXML Label time271;
	@FXML Label time272;
	@FXML Label time273;
	
	@FXML Label time281;
	@FXML Label time282;
	@FXML Label time283;
	
	@FXML Label time291;
	@FXML Label time292;
	@FXML Label time293;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private HashSet<String> REFERENCE_PROJECTS = new HashSet<String>();
	private HashSet<String> preclass_REFERENCE_PROJECTS = new HashSet<String>();
	
	
	
	private Integer ref_desc_cardinality = 0;
	private Integer preclass_ref_desc_cardinality = 0;
	
	private Integer target_desc_cardinality = 0;
	
	private HashMap<String,String> PROJECT2DATAMAP = new HashMap<String,String>();
	private HashMap<String,String> PROJECT2LANGUAGE = new HashMap<String,String>();
	private HashMap<String,HashMap<String,ArrayList<String>>> CLASSIFICATION_RULES =  new HashMap<String,HashMap<String,ArrayList<String>>>();
	private HashMap<String,HashMap<String,ArrayList<String>>> preclass_CLASSIFICATION_RULES = new HashMap<String,HashMap<String,ArrayList<String>>>();
	
	public BinaryClassificationParameters binaryClassificationParameters;
	private HashSet<Integer> buildRows;
	private HashSet<Integer> preclass_buildRows;
	
	private HashMap<String,ArrayList<String>> ITEMS_DICO = new HashMap<String,ArrayList<String>>();
	private HashMap<String,ArrayList<String>> preclass_ITEMS_DICO = new HashMap<String,ArrayList<String>>();
	
	private HashMap<String, ArrayList<DescriptionFetchRow>> DESCS = new HashMap<String,ArrayList<DescriptionFetchRow>>();
	private Bigram bigramCount;
	private HashMap<String, HashMap<String,String>> CLEAN_REFERENCES = new HashMap<String,HashMap<String,String>>();
	private HashMap<String, HashMap<String,String>> CLEAN_TARGETS = new HashMap<String,HashMap<String,String>>();
	
	private HashMap<String, HashMap<String,String>> preclass_CLEAN_REFERENCES = new HashMap<String,HashMap<String,String>>();
	private HashMap<String, HashMap<String,String>> preclass_CLEAN_TARGETS = new HashMap<String,HashMap<String,String>>();
	
	private ArrayList<AbaqueRow> ABAQUE = new ArrayList<AbaqueRow>(1024) ;
	private double classification_graph_coverage;
	private double classification_graph_accuracy;
	private double preclassification_graph_coverage;
	private double preclassification_graph_accuracy;
	private boolean cardinal_counted=false;
	private CorpusReader cr;
	private ConfusionMatrixReader cmr;
	private TimeMasterTemplate timeTemplate;
	private TimeMaster tm;
	private ArrayList<RowConstraints> rc = new ArrayList<RowConstraints> ();
	private ArrayList<ColumnConstraints> cc = new ArrayList<ColumnConstraints> ();
	
	private boolean isLaunched;
	private Auto_classification_launch parent;
	private UserAccount account;
	private HashMap<String, HashMap<String, String>> preclass_CLASSIFICATION_RULES_AGGREGATED;
	private HashMap<String, HashMap<String, String>> CLASSIFICATION_RULES_AGGREGATED;
	private DownloadSegmenter classif_downloadSegmenter;
	private DownloadSegmenter preclassif_downloadSegmenter;
	private boolean preclass_target_char_clean_progress_complete= false;
	private boolean preclass_target_abv_clean_progress_complete= false;
	private boolean preclass_target_spell_clean_progress_complete= false;
	private boolean target_char_clean_progress_complete= false;
	private boolean target_abv_clean_progress_complete= false;
	private boolean target_spell_clean_progress_complete= false;
	private boolean preclass_ref_char_clean_progress_complete= false;
	private boolean preclass_ref_abv_clean_progress_complete= false;
	private boolean preclass_ref_spell_clean_progress_complete= false;
	private boolean ref_char_clean_progress_complete= false;
	private boolean ref_abv_clean_progress_complete= false;
	private boolean ref_spell_clean_progress_complete= false;
	protected boolean forwarding = false;
	private boolean block_window_close = false;
	private HashMap<String, Double> PROGRESS_FACTORS;
	private HashMap<String, HashMap<String, String>> PROJECT_INFO;
	@FXML MenuBar menubar;
	private Auto_classification_progress this_controller;
	private Task<Void> TargetTask;
	private Task<Void> RuleGenTask;
	private Task<Void> cleanTask;
	private Task<Void> mainTask;
	private Task<Void> forwardTask;
	
	@SuppressWarnings("static-access")
	@FXML void initialize(){
		download_button.setVisible(false);
		decorate_menubar(menubar, account);
		load_abaque();
		load_row_contraints();
		grille.getRowConstraints().clear();
		grille.getRowConstraints().addAll(rc);
		
		//grille.getColumnConstraints().clear();
		//grille.getColumnConstraints().addAll(cc);
		
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
		static_classifLabel.setVisible(false);
		static_preclassifLabel.setVisible(false);
		
		actualgraph.setAnimated(false);
		actualgraph.setLegendVisible(false);
		actualgraph.setHorizontalGridLinesVisible(false);
		actualgraph.setVerticalGridLinesVisible(false);
		actualgraph.setVerticalZeroLineVisible(false);
		actualgraph.setHorizontalZeroLineVisible(false);
		actualgraph.getYAxis().setTickMarkVisible(false);
		actualgraph.getXAxis().setTickMarkVisible(false);
		actualgraph.getYAxis().setTickLabelsVisible(false);
		actualgraph.getXAxis().setTickLabelsVisible(false);
		dynamic_classifLabel.setVisible(false);
		dynamic_preclassifLabel.setVisible(false);
		
		disabled_tasks_transparent();
		
		for(Node noeud:grille.getChildren()) {
			try {
				if(grille.getColumnIndex(noeud)==7) {
					((Label) noeud).setEllipsisString("");
				}
			}catch(Exception V) {
				
			}
		}
		
		
		
		
	}

	@SuppressWarnings("static-access")
	private void decorate_menubar(MenuBar menubar2, UserAccount account2) {
		
		
		for(Node noeud:grille.getChildren()) {
			
			if(!(grille.getRowIndex(noeud)!=null)) {
				continue;
			}
			
			if(grille.getRowIndex(noeud)==43) {
				noeud.setVisible(false);
			}
		}
		this_controller = this;
		
		Label homeLabel = new Label("Home");
		homeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    	try {
		    		if(block_window_close) {
				    	Boolean ret = ConfirmationDialog.show("Results available", "If you choose to leave this screen, the current results will be lost.\nDo you wish to proceed anyway?", "Save", "Quit without saving", "Cancel", this,this_controller);
				    	if(ret!=null) {
				    		if(ret) {
				    			save_to_file();
				    			return;
				    		}else {
				    			
				    		}
				    	}else {
				    		return;
				    	}
		    		}
					  
				 	//Loads next screen's controller, loading cursor
					Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Front_page.fxml"));
					AnchorPane root = fxmlLoader.load();

				    controllers.Front_page controller = fxmlLoader.getController();
					
					Scene scene = new Scene(root,400,400);
					controller.scene = scene;
					scene.setCursor(Cursor.WAIT);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					
					//Sets up next screen size
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();
					
					//Sets up next screen's login and role, default cursor
					controller.setUserAccount(account);
				    //close the current window
					kill_threads();
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 front_page", "FX001 front_page", "FX001 front_page");
					e.printStackTrace();
				}
		    }
		});
		
		Menu home = new Menu();
		home.setGraphic(homeLabel);
		
		
		Menu project_selection = menubar.getMenus().get(1);
		project_selection.getItems().clear();
		
		MenuItem activate_project = new MenuItem("Project selection");
		activate_project.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
		    		
		    		if(block_window_close) {
				    	Boolean ret = ConfirmationDialog.show("Results available", "If you choose to leave this screen, the current results will be lost.\nDo you wish to proceed anyway?", "Save", "Quit without saving", "Cancel", this,this_controller,null);
				    	if(ret!=null) {
				    		if(ret) {
				    			save_to_file();
				    			return;
				    		}else {
				    			
				    		}
				    	}else {
				    		return;
				    	}
		    		}
					
					;
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Project_selection.fxml"));
					AnchorPane root = fxmlLoader.load();

				    controllers.Project_selection controller = fxmlLoader.getController();
					
					Scene scene = new Scene(root,400,400);
					controller.scene = scene;
					scene.setCursor(Cursor.WAIT);
				    
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();
					
					
					controller.setUserAccount(account);
				    controller.load_projects_list();
					scene.setCursor(Cursor.DEFAULT);
					//close the current window
					kill_threads();
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 project_selection", "FX001 project_selection", "FX001 project_selection");
					e.printStackTrace();
				}
		    }
		});
		
		MenuItem new_project = new MenuItem("New project creation");
		new_project.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	  
			    try {
			    	
			    	
			    	if(block_window_close) {
				    	Boolean ret = ConfirmationDialog.show("Results available", "If you choose to leave this screen, the current results will be lost.\nDo you wish to proceed anyway?", "Save", "Quit without saving", "Cancel", this,this_controller,null);
				    	if(ret!=null) {
				    		if(ret) {
				    			save_to_file();
				    			return;
				    		}else {
				    			
				    		}
				    	}else {
				    		return;
				    	}
		    		}
					
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Project_parameters.fxml"));
					AnchorPane root = fxmlLoader.load();

					Scene scene = new Scene(root,400,400);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();

					controllers.Project_parameters controller = fxmlLoader.getController();
					
					controller.setUserAccount(account);
					
					//close the current window
					kill_threads();
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
					
				    
					
				} catch(Exception e) {
					e.printStackTrace(System.err);
					ExceptionDialog.show("FX001 project_parameters", "FX001 project_parameters", "FX001 project_parameters");
				}
		    }});
		
		project_selection.getItems().addAll(activate_project,new_project);
		
		
		Menu auto_classif = new Menu();
		Label auto_classif_label = new Label("Automated classification");
		auto_classif_label.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
			//if there's currently an actif project, proceed to the classification
			String activated_pid = account.getActive_project();
			Stage stage = (Stage) menubar.getScene().getWindow();
					if(activated_pid!=null) {
						
					    try {
					    	
					    	
					    	if(block_window_close) {
						    	Boolean ret = ConfirmationDialog.show("Results available", "If you choose to leave this screen, the current results will be lost.\nDo you wish to proceed anyway?", "Save", "Quit without saving", "Cancel", this,this_controller);
						    	if(ret!=null) {
						    		if(ret) {
						    			save_to_file();
						    			return;
						    		}else {
						    			
						    		}
						    	}else {
						    		return;
						    	}
				    		}
							
					    	Connection conn = Tools.spawn_connection();
					    	Statement stmt = conn.createStatement();
					    	ResultSet rs = stmt.executeQuery("select target_quality from administration.projects where project_id='"+activated_pid+"'");
					    	rs.next();
					    	double target_desc_accuracy = rs.getDouble(1);
					    	
					    	rs = stmt.executeQuery("select count(distinct item_id) from "+activated_pid+".project_items");
					    	rs.next();
					    	int target_desc_cardinality = rs.getInt(1);
					    	
					    	rs.close();
					    	stmt.close();
					    	conn.close();
					    	
					    	
					    	
						    Stage primaryStage = new Stage();
						    
						    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Auto_classification_launch.fxml"));
							AnchorPane root = fxmlLoader.load();

							Scene scene = new Scene(root,400,400);
							
							primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
							primaryStage.setScene(scene);
							//primaryStage.setMinHeight(768);
							//primaryStage.setMinWidth(1024);
							primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
							primaryStage.show();

							controllers.Auto_classification_launch controller = fxmlLoader.getController();
							
							controller.setUserAccount(account);
							
							controller.setPid(activated_pid);
							controller.setTarget_desc_cardinality(target_desc_cardinality);
							controller.setTarget_desc_accuracy(target_desc_accuracy);
							
							kill_threads();
							stage.close();
							
						} catch(Exception e) {
							ExceptionDialog.show("FX001 auto_classification_launch", "FX001 auto_classification_launch", "FX001 auto_classification_launch");
							e.printStackTrace(System.err);
						}
					}else {
						//Else raise an alert message dialog
						ExceptionDialog.no_selected_project();
					}
		}});
		
		auto_classif.setGraphic(auto_classif_label);
		
		Menu manual_classif = new Menu("Semi-manual classification");
		manual_classif.setDisable(true);
		Menu review = new Menu("Classification review");
		review.setDisable(true);
		Menu dashboard = new Menu("Project Dashboard");
		dashboard.setDisable(true);
		Menu help = new Menu("Help");
		help.setDisable(true);
		
		
		
		menubar.getMenus().clear();
		menubar.getMenus().addAll(home,project_selection,auto_classif,manual_classif,review,dashboard,help);
	}

	protected void kill_threads() {
		try {
			TargetTask.cancel();
			RuleGenTask.cancel();
			cleanTask.cancel();
			mainTask.cancel();
			forwardTask.cancel();
		}catch(Exception W) {
			try{
				RuleGenTask.cancel();
				cleanTask.cancel();
				mainTask.cancel();
				forwardTask.cancel();
			}catch(Exception X) {
				try {
					cleanTask.cancel();
					mainTask.cancel();
					forwardTask.cancel();
				}catch(Exception Y) {
					try {
						mainTask.cancel();
						forwardTask.cancel();
					}catch(Exception Z) {
						try{
							forwardTask.cancel();
						}catch(Exception V) {
							
						}
					}
				}
			}
			
		}
		System.gc();
		
	}

	private void disabled_tasks_transparent() {
		
	}

	private void load_row_contraints() {
		HashSet<Integer> FILLED_ROWS = new HashSet<Integer>(Arrays.asList(3,6,7,8,9,10,12,13,14,16,17,18,19,21,24,27,28,29,30,31,33,34,35,37,38,39,40,42,43));
		for(int i = 0;i<51;i++) {
			RowConstraints tmp = new RowConstraints();
			if(FILLED_ROWS.contains(i)) {
				tmp.setPercentHeight(3);
			}else {
				if(i==22) {
					tmp.setPercentHeight(3);
				}else {
					tmp.setPercentHeight(0);
				}
				
			}
			if(i==0) {
				tmp.setPercentHeight(6);
			}
			if(i<3) {
				switch(i) {
				case 1:
					tmp.setPercentHeight(4);
				case 2:
					tmp.setPercentHeight(2);
				}
			}
			rc.add(tmp);
		}
		
		for(int i=0;i<14;i++) {
			
			ColumnConstraints tmp = new ColumnConstraints();
			
			switch(i) {
			case 0:
				tmp.setPercentWidth(4);
				cc.add(tmp);
			case 1:
				tmp.setPercentWidth(26);
				cc.add(tmp);
			case 2:
				tmp.setPercentWidth(7);
				cc.add(tmp);
			case 3:
				tmp.setPercentWidth(12);
				cc.add(tmp);
			case 4:
				tmp.setPercentWidth(7);
				cc.add(tmp);
			case 5:
				tmp.setPercentWidth(8);
				cc.add(tmp);
			case 6:
				tmp.setPercentWidth(8);
				cc.add(tmp);
			case 7:
				tmp.setPercentWidth(0);
				cc.add(tmp);
			case 8:
				tmp.setPercentWidth(0);
				cc.add(tmp);
			case 9:
				tmp.setPercentWidth(15);
				cc.add(tmp);
			case 10:
				tmp.setPercentWidth(0);
				cc.add(tmp);
			case 11:
				tmp.setPercentWidth(15);
				cc.add(tmp);
			case 12:
				tmp.setPercentWidth(0);
				cc.add(tmp);
			case 13:
				tmp.setPercentWidth(0);
				cc.add(tmp);
			}
		}
		
	}

	private void load_abaque() {
		try {
			Connection conn = Tools.spawn_connection();
			Statement ps = conn.createStatement();
			ResultSet rs;
			//#
			rs = ps.executeQuery("select base_sample_size, class_accuracy,  rule_baseline_threshold, rule_accuracy_threshold,coverage from public_ressources.abacus_values");
			while(rs.next()) {
				AbaqueRow rw = new AbaqueRow();
				rw.setBs(rs.getInt(1));
				rw.setClass_accuracy(rs.getDouble(2));
				rw.setTc(rs.getInt(3));
				rw.setTa(rs.getDouble(4));
				rw.setCoverage(rs.getDouble(5));
				rw.setFamily_accuracy(0);
				ABAQUE.add(rw);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	private void launch_preclassification() {

		/*
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
				preclass_download_progress.setProgress(1.0);
				tm.stopRow(16);
			}
			
			});
		
		try {
			this.bigramCount = Bigram.trainNGram(DESCS,this.binaryClassificationParameters);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//boolean alphabetOnlyParameter, boolean decodeParameter
		

		
		try {
			cr = new CorpusReader(this.bigramCount);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        cmr = new ConfusionMatrixReader();
        
        */
		
		if(this.preclass_ref_desc_cardinality==0) {
			for(Node noeud:grille.getChildren()) {
			try {	
				if( grille.getColumnIndex(noeud)==3 && grille.getRowIndex(noeud)>=24 && grille.getRowIndex(noeud)<=43) {
					set_semi_opaque(noeud);
					((ProgressBar) noeud).setProgress(1);
				}
				
			}catch(Exception V) {
					
				}
			}
			
			try {
				classification_complete();
			} catch (IOException | ClassNotFoundException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}

		TargetTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	tm.startSummaryRow(24);
		    	preclass_clean_targets();
				return null;
		    }
		};
		TargetTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			preclass_target_desc_progress.setProgress(1);
			update_preclassif_progress();
			
			tm.stopRow(24);
			try {
				preclass_apply_rules();
				tm.stopRow(28);
				preclass_rule_app_progress.setProgress(1);
			} catch (ClassNotFoundException e1) {
				ExceptionDialog.show("CF001 pg_class", "CF001 pg_class", "CF001 pg_class");
				e1.printStackTrace();
			} catch (SQLException e1) {
				ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
				e1.printStackTrace();
			}
			
			try {
				classification_complete();
			} catch (IOException | ClassNotFoundException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		});

		TargetTask.setOnFailed(e -> {
		    Throwable problem = TargetTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		TargetTask.setOnCancelled(e -> {
		    ;
			
		});
		
		
		
		
		RuleGenTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	preclass_generate_rules();
				return null;
		    }

			
		};
		RuleGenTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			tm.stopRow(21);
			preclass_rule_gen_progress.setProgress(1);
			update_preclassif_progress();
			
			Thread TargetThread = new Thread(TargetTask);; TargetThread.setDaemon(true);
			TargetThread.start();
			
			
			
		});

		RuleGenTask.setOnFailed(e -> {
		    Throwable problem = RuleGenTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		RuleGenTask.setOnCancelled(e -> {
		    ;
			
		});
		
		
		
		
		cleanTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	download_data(true);
		    	preclass_clean_references();
				return null;
		    }
		};
		cleanTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			tm.stopRow(17);
			preclass_reference_desc_progress.setProgress(1);
			update_preclassif_progress();
			
			
			Thread Rulethread = new Thread(RuleGenTask);; Rulethread.setDaemon(true);
			Rulethread.start();
			
		});

		cleanTask.setOnFailed(e -> {
		    Throwable problem = cleanTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		cleanTask.setOnCancelled(e -> {
		    ;
			
		});
		
		Thread cleanThread = new Thread(cleanTask);; cleanThread.setDaemon(true);
		cleanThread.start();
	}
	
	
	@SuppressWarnings({ "resource", "deprecation" })
	@FXML private void save_to_file() throws ClassNotFoundException, SQLException, IOException {
		FileChooser fileChooser = new FileChooser();
		
		
		
		Date instant = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat( "MMMMM_dd" );
	    String time = sdf.format( instant );
	    
		fileChooser.setInitialFileName(PROJECT_INFO.get(pid).get("project_name")+"_"+time);
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(static_classifLabel.getScene().getWindow());
        
        if(file != null){
        	
        	try {
        		
        		download_button.getScene().setCursor(Cursor.WAIT);
        		
        	SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
            Sheet sh = wb.createSheet("Results");
            
    		
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
    		cell.setCellValue("Autoclassification - Level "+this.binaryClassificationParameters.getClassif_granularity().toString()+" number");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(4).setCellStyle(style);
    		 
    		 
    		 
    		 
    		cell = row.createCell(5);
    		cell.setCellValue("Autoclassification - Level "+this.binaryClassificationParameters.getClassif_granularity().toString()+" name");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(5).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(6);
    		cell.setCellValue("Autoclassification - Rule");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(6).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(7);
    		cell.setCellValue("Autoclassification - Rule accuracy");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(7).setCellStyle(style);
    		 
    		 XSSFCellStyle classif_style = (XSSFCellStyle) wb.createCellStyle();
    		 classif_style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 classif_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 classif_style.setFont(font);
    		 
    		 
    		cell = row.createCell(8);
    		cell.setCellValue("Autoclassification - Rule base");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(8).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(9);
    		cell.setCellValue("Preclassification - Level "+this.binaryClassificationParameters.getPreclassif_granularity().toString()+" number");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(9).setCellStyle(style);
    		 
    		 XSSFCellStyle preclassif_style = (XSSFCellStyle) wb.createCellStyle();
    		 preclassif_style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 preclassif_style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 preclassif_style.setFont(font);
    		 
    		 
    		 
    		 
    		cell = row.createCell(10);
    		cell.setCellValue("Preclassification - Level "+this.binaryClassificationParameters.getPreclassif_granularity().toString()+" name");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(10).setCellStyle(style);
    		 
    		 
    		cell = row.createCell(11);
    		cell.setCellValue("Preclassification - Rule");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(11).setCellStyle(style);
    		 
    		 
    		 
    		 
    		cell = row.createCell(12);
    		cell.setCellValue("Preclassification - Rule accuracy");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(12).setCellStyle(style);
    		 
    		 
    		 
    		cell = row.createCell(13);
    		cell.setCellValue("Preclassification - Rule base");
    		style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND); font = wb.createFont(); font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); font.setBold(true); style.setFont(font);
    		 row.getCell(13).setCellStyle(style);
    		 
    		ArrayList<DescriptionFetchRow> rws = DESCS.get(this.pid);
    		
    		CellStyle percentStyle = wb.createCellStyle();
    		percentStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
    		
    		
    		
    		for(DescriptionFetchRow rw:rws) {
    			i=i+1;
    			ArrayList<String> result_row = new ArrayList<String>(14);
    			
    			String aid = rw.getAid();  result_row.add(aid);
    			String sd = rw.getSd();  result_row.add(sd);
    			String ld = rw.getLd();  result_row.add(ld);
    			String mg = rw.getMG();  result_row.add(mg);
    			
    			String classif_cid;
    			String classif_cname;
    			String classif_rule;
    			String classif_acc;
    			String classif_base;
    			String preclassif_cid;
    			String preclassif_cname;
    			String preclassif_rule;
    			String preclassif_acc;
    			String preclassif_base;
    			
    			
    			if(ITEMS_DICO.containsKey(aid)) {
    				classif_cid= ITEMS_DICO.get(aid).get(1);  result_row.add(classif_cid);
    				classif_cname= ITEMS_DICO.get(aid).get(2);  result_row.add(classif_cname);
    				classif_rule= ITEMS_DICO.get(aid).get(3);  result_row.add(classif_rule.replace("MAIN=", "").replace("|COMP=", ":"));
    				classif_acc= ITEMS_DICO.get(aid).get(4);  result_row.add(classif_acc);
    				classif_base= ITEMS_DICO.get(aid).get(5);  result_row.add(classif_base);
    			}else {
    				classif_cid= "";  result_row.add(classif_cid);
    				classif_cname= "";  result_row.add(classif_cname);
    				classif_rule= "";  result_row.add(classif_rule);
    				classif_acc= "";  result_row.add(classif_acc);
    				classif_base= "";  result_row.add(classif_base);
    			}
    			
    			if(preclass_ITEMS_DICO.containsKey(aid)) {
    				preclassif_cid= preclass_ITEMS_DICO.get(aid).get(1);  result_row.add(preclassif_cid);
    				preclassif_cname= preclass_ITEMS_DICO.get(aid).get(2);  result_row.add(preclassif_cname);
    				preclassif_rule= preclass_ITEMS_DICO.get(aid).get(3);  result_row.add(preclassif_rule.replace("MAIN=", "").replace("|COMP=", ":"));
    				preclassif_acc= preclass_ITEMS_DICO.get(aid).get(4);  result_row.add(preclassif_acc);
    				preclassif_base= preclass_ITEMS_DICO.get(aid).get(5);  result_row.add(preclassif_base);
    			}else {
    				preclassif_cid= "";  result_row.add(preclassif_cid);
    				preclassif_cname= "";  result_row.add(preclassif_cname);
    				preclassif_rule= "";  result_row.add(preclassif_rule);
    				preclassif_acc= "";  result_row.add(preclassif_acc);
    				preclassif_base= "";  result_row.add(preclassif_base);
    			}
    			
    			row = sh.createRow(i);
    	        for(int cellnum = 0; cellnum < 14; cellnum++){
    	            cell = row.createCell(cellnum);
    	            if(cellnum == 7 || cellnum == 12) {
    	            	cell.setCellStyle(percentStyle);
    	        		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    	        		try{
    	        			cell.setCellValue(Double.parseDouble(result_row.get(cellnum)));
    	        		}catch(Exception V) {
    	        			cell.setCellType(Cell.CELL_TYPE_STRING);
            	            cell.setCellValue(result_row.get(cellnum));
    	        		}
    	            }else {
    	            	
    	            	if(cellnum == 8 || cellnum == 13) {
    	            		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    	            	}else {
    	            		cell.setCellType(Cell.CELL_TYPE_STRING);
    	            	}
    	            	
        	            cell.setCellValue(result_row.get(cellnum));
    	            }
    	            
    	        }
    			
    			
    			
    		}
    		
    		sh.setColumnWidth(0,15*256);
    		sh.setColumnWidth(1,38*256);
    		sh.setColumnWidth(2,38*256);
    		sh.setColumnWidth(3,20*256);
    		sh.setColumnWidth(4,14*256);
    		sh.setColumnWidth(5,25*256);
    		sh.setColumnWidth(6,33*256);
    		sh.setColumnWidth(7,9*256);
    		sh.setColumnWidth(8,9*256);
    		sh.setColumnWidth(9,14*256);
    		sh.setColumnWidth(10,25*256);
    		sh.setColumnWidth(11,33*256);
    		sh.setColumnWidth(12,9*256);
    		sh.setColumnWidth(13,9*256);
    		
    		
    		sh.setZoom(70);
    		
    		
    		
    		
    		
    		
    		
    		sh = wb.createSheet("Parameters");
    		row = sh.createRow(0);
    		cell = row.createCell(0);
    		cell.setCellValue("GENERAL INFORMATION");
    		cell.setCellStyle(general_header_style);
    		row = sh.createRow(1);
    		row = sh.createRow(2);
    		
    		cell = row.createCell(0);
    		cell.setCellValue("Active project name");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(1);
    		cell.setCellValue("Active project language");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(2);
    		cell.setCellValue("Active project classification system");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(3);
    		cell.setCellValue("Active project size");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(4);
    		cell.setCellValue("Number of classified items");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(5);
    		cell.setCellValue("% of classified items");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(6);
    		cell.setCellValue("Number of pre-classified items");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(7);
    		cell.setCellValue("% of pre-classified items");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(8);
    		cell.setCellValue("Extract date");
    		cell.setCellStyle(general_header_style);
    		cell = row.createCell(9);
    		cell.setCellValue("Extract time");
    		cell.setCellStyle(general_header_style);
    		
    		
    		
    		
    		row = sh.createRow(3);
    		cell = row.createCell(0);
    		cell.setCellValue(PROJECT_INFO.get(this.pid).get("project_name"));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(1);
    		cell.setCellValue(PROJECT_INFO.get(this.pid).get("project_language"));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(2);
    		cell.setCellValue(PROJECT_INFO.get(this.pid).get("classification_system"));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(3);
    		cell.setCellValue(PROJECT_INFO.get(this.pid).get("project_size"));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(4);
    		cell.setCellValue(String.valueOf(ITEMS_DICO.size()));
    		cell.setCellStyle(general_content_style);
    		
    		
    		
    		CellStyle percentStyle_parameters = wb.createCellStyle();
    		percentStyle_parameters.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    		percentStyle_parameters.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		percentStyle_parameters.setFont(general_content_font);
    		percentStyle_parameters.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
    		
   		 	cell = row.createCell(5);
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell.setCellValue( (ITEMS_DICO.size()*1.0)/(1.0*this.target_desc_cardinality) );
    		
    		cell = row.createCell(6);
    		cell.setCellValue(String.valueOf(preclass_ITEMS_DICO.size()));
    		cell.setCellStyle(general_content_style);
    		
    		cell = row.createCell(7);
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell.setCellValue((preclass_ITEMS_DICO.size()*1.0)/(1.0*this.target_desc_cardinality) );
    		
    		
    		cell = row.createCell(8);
    		
    		cell.setCellValue(LocalDate.now().toString());
    		cell.setCellStyle(general_content_style);

    		cell = row.createCell(9);
    		
    		sdf = new SimpleDateFormat( "HH:mm" );
    	    time = sdf.format( instant );
    	    
    		cell.setCellValue(time);
    		cell.setCellStyle(general_content_style);
    		
    		
    		row = sh.createRow(4);
    		row=sh.createRow(5);
    		cell = row.createCell(0);
    		cell.setCellValue("Autoclassification parameters");
    		cell.setCellStyle(classif_style);
    		
    		row= sh.createRow(6);
    		row= sh.createRow(7);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project names");
    		cell.setCellStyle(classif_style);
    		i=1;
    		for(String pid:REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_name"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(8);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project languages");
    		cell.setCellStyle(classif_style);
    		i=1;
    		for(String pid:REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_language"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(9);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project classification systems");
    		cell.setCellStyle(classif_style);
    		i=1;
    		for(String pid:REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("classification_system"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(10);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project sizes");
    		cell.setCellStyle(classif_style);
    		i=1;
    		for(String pid:REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_size"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		
    		row = sh.createRow(11);
    		
    		row=sh.createRow(12);
    		cell = row.createCell(0);
    		cell.setCellValue("% of base data used as reference");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(1);
    		cell.setCellValue("Minimum term length");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(2);
    		cell.setCellValue("Term type");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(3);
    		cell.setCellValue("Description type");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(4);
    		cell.setCellValue("Accent & specific characters replacement");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(5);
    		cell.setCellValue("Cleansing of abbreviations");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(6);
    		cell.setCellValue("Cleansing of mispelling");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(7);
    		cell.setCellValue("Minimum rule accuracy (Ta)");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(8);
    		cell.setCellValue("Minimum rule base (Tb)");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(9);
    		cell.setCellValue("Parameters ranking");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(10);
    		cell.setCellValue("Relative paramter weight");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(11);
    		cell.setCellValue("Rule type coefficient");
    		cell.setCellStyle(classif_style);
    		cell = row.createCell(12);
    		cell.setCellValue("Granularity level");
    		cell.setCellStyle(classif_style);
    		
    		row=sh.createRow(13);
    		cell = row.createCell(0);
    		cell.setCellValue(0.01 * this.binaryClassificationParameters.getClassif_buildSampleSize());
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell = row.createCell(1);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_minimumTermLength().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(2);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_keepAlpha()?"Alphanumeric":"Alphabetical only");
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(3);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_baseDescriptionType().toPrintString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(4);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_cleanChar().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(5);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_cleanAbv().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(6);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_cleanSpell().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(7);
    		cell.setCellValue(0.01 * this.binaryClassificationParameters.getClassif_Ta());
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell = row.createCell(8);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_Tb().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(9);
    		cell.setCellValue(String.join(",", this.binaryClassificationParameters.getClassif_rank()));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(10);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_epsilon().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(11);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_typeFactor().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(12);
    		cell.setCellValue(this.binaryClassificationParameters.getClassif_granularity().toString());
    		cell.setCellStyle(general_content_style);
    		
    		row=sh.createRow(14);
    		row=sh.createRow(15);
    		cell = row.createCell(0);
    		cell.setCellValue("Preclassification parameters");
    		cell.setCellStyle(preclassif_style);
    		row=sh.createRow(16);

    		row= sh.createRow(17);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project names");
    		cell.setCellStyle(preclassif_style);
    		i=1;
    		for(String pid:preclass_REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_name"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(18);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project languages");
    		cell.setCellStyle(preclassif_style);
    		i=1;
    		for(String pid:preclass_REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_language"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(19);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project classification systems");
    		cell.setCellStyle(general_content_style);
    		cell.setCellStyle(preclassif_style);
    		i=1;
    		for(String pid:preclass_REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("classification_system"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		row=sh.createRow(20);
    		cell = row.createCell(0);
    		cell.setCellValue("Build project sizes");
    		cell.setCellStyle(preclassif_style);
    		i=1;
    		for(String pid:preclass_REFERENCE_PROJECTS) {
    			cell = row.createCell(i);
    			cell.setCellValue(PROJECT_INFO.get(pid).get("project_size"));
    			cell.setCellStyle(general_content_style);
    			i+=1;
    		}
    		
    		row = sh.createRow(21);
    		row = sh.createRow(22);
    		
    		cell = row.createCell(0);
    		cell.setCellValue("% of base data used as reference");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(1);
    		cell.setCellValue("Minimum term length");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(2);
    		cell.setCellValue("Term type");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(3);
    		cell.setCellValue("Description type");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(4);
    		cell.setCellValue("Accent & specific characters replacement");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(5);
    		cell.setCellValue("Cleansing of abbreviations");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(6);
    		cell.setCellValue("Cleansing of mispelling");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(7);
    		cell.setCellValue("Minimum rule accuracy (Ta)");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(8);
    		cell.setCellValue("Minimum rule base (Tb)");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(9);
    		cell.setCellValue("Parameters ranking");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(10);
    		cell.setCellValue("Relative paramter weight");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(11);
    		cell.setCellValue("Rule type coefficient");
    		cell.setCellStyle(preclassif_style);
    		cell = row.createCell(12);
    		cell.setCellValue("Granularity level");
    		cell.setCellStyle(preclassif_style);
    		
    		row=sh.createRow(23);
    		cell = row.createCell(0);
    		cell.setCellValue(0.01 * this.binaryClassificationParameters.getPreclassif_buildSampleSize());
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell = row.createCell(1);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_minimumTermLength().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(2);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_keepAlpha()?"Alphanumeric":"Alphabetical only");
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(3);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_baseDescriptionType().toPrintString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(4);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_cleanChar().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(5);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_cleanAbv().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(6);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_cleanSpell().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(7);
    		cell.setCellValue(0.01 * this.binaryClassificationParameters.getPreclassif_Ta());
    		cell.setCellStyle(percentStyle_parameters);
    		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
    		cell = row.createCell(8);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_Tb().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(9);
    		cell.setCellValue(String.join(",", this.binaryClassificationParameters.getPreclassif_rank()));
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(10);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_epsilon().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(11);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_typeFactor().toString());
    		cell.setCellStyle(general_content_style);
    		cell = row.createCell(12);
    		cell.setCellValue(this.binaryClassificationParameters.getPreclassif_granularity().toString());
    		cell.setCellStyle(general_content_style);
    		
    		

    		sh.setColumnWidth(0,30*256);
    		sh.setColumnWidth(1,22*256);
    		sh.setColumnWidth(2,22*256);
    		sh.setColumnWidth(3,22*256);
    		sh.setColumnWidth(4,22*256);
    		sh.setColumnWidth(5,22*256);
    		sh.setColumnWidth(6,22*256);
    		sh.setColumnWidth(7,22*256);
    		sh.setColumnWidth(8,22*256);
    		sh.setColumnWidth(9,22*256);
    		sh.setColumnWidth(10,22*256);
    		sh.setColumnWidth(11,22*256);
    		sh.setColumnWidth(12,22*256);
    		
    		sh.setZoom(70);
    		
    		
    		
    		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
            wb.write(out);
            out.close();

            // dispose of temporary files backing this workbook on disk
            wb.dispose();
    		wb.dispose();
    		
    		download_button.getScene().setCursor(Cursor.DEFAULT);
    		ConfirmationDialog.show("File saved", "Results successfully saved in\n"+file.getAbsolutePath(), "OK", this);
    		block_window_close = false;
        	}catch(Exception V) {
        		download_button.getScene().setCursor(Cursor.DEFAULT);
        		ConfirmationDialog.show("File saving failed", "Results could not be saved in\n"+file.getAbsolutePath()+"\nMake sure you have the rights to create files in this folder and that the file is not open by another application", "OK", this);
        	}
        }
	}
	

	private HashMap<String, HashMap<String, String>> LOAD_PROJECT_INFO() throws ClassNotFoundException, SQLException {
		
		HashMap<String, HashMap<String, String>> p_info = new HashMap<String,HashMap<String,String>>();
		
		LinkedHashMap<String, String> LANGUAGES = load_languages();
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("select * from administration.projects");
		while(rs.next()) {
			HashMap<String, String> tmp = new HashMap<String,String>();
			tmp.put("project_name", rs.getString("project_name"));
			tmp.put("project_language",LANGUAGES.get(rs.getString("data_language")));
			tmp.put("classification_system",rs.getString("classification_system_name"));
			Connection conn2 = Tools.spawn_connection();
			Statement stmt2 = conn2.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select count(distinct client_item_number) from "+rs.getString("project_id")+".project_items");
			rs2.next();
			tmp.put( "project_size",String.valueOf(rs2.getInt(1)) );
			rs2.close();
			stmt2.close();
			conn2.close();
			
			
			p_info.put(rs.getString("project_id"), tmp);
		}
		rs.close();
		st.close();
		conn.close();
		
		return p_info;
	}

	//Useful function to load all the known languages mapping
	private LinkedHashMap<String, String> load_languages() throws ClassNotFoundException, SQLException {
		//For every known mapping language_id<-> language store in temporary variable
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from administration.languages");
		LinkedHashMap<String, String> ret = new LinkedHashMap<String,String>();
		//Add a dummy languge id for "All languages" language
		ret.put("DUMMY_LANGUAGE_UUID","All languages");
		while(rs.next()) {
			ret.put(rs.getString("language_id"), rs.getString("language_name"));
		}
		//Close the connection and return the mapping
		rs.close();
		stmt.close();
		conn.close();
		return ret;
	}

	private void classification_complete() throws IOException, ClassNotFoundException, SQLException {
		tm.stopRow(15);
		block_window_close = true;
		download_button.setVisible(true);
		
		PROJECT_INFO = new HashMap<String,HashMap<String,String>>();
		PROJECT_INFO = LOAD_PROJECT_INFO();
		upload_data();
		
	}

	private void upload_data() {
		mainTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	
		    	Connection conn = Tools.spawn_connection();
		    	HashMap<String, ArrayList<String>> items_x_rules = new HashMap<String,ArrayList<String>>();
		    	HashMap<String,GenericClassRule> staticRules = new HashMap<String,GenericClassRule>();
		    	Statement st = conn.createStatement();
		    	
		    	ResultSet rs = st.executeQuery("select rule_id, main, application, complement, material_group," + 
						"pre_classification, drawing, class_id, active_status, source_project_id from "+pid+".project_rules");
				System.out.println("select rule_id, main, application, complement, material_group," + 
						"pre_classification, drawing, class_id, active_status, source_project_id from "+pid+".project_rules");
				while(rs.next()) {
					GenericClassRule gr = new GenericClassRule();
					gr.setMain(rs.getString("main"));
					gr.setApp(rs.getString("application"));
					gr.setComp(rs.getString("complement"));
					gr.setMg(rs.getString("material_group"));
					gr.setPc(rs.getString("pre_classification"));
					gr.setDwg(rs.getBoolean("drawing"));
					gr.classif=new ArrayList<> ( Arrays.asList( rs.getString("class_id").split("&&&") ) );
					gr.active=rs.getBoolean("active_status");
					gr.setSource_project_id(rs.getString("source_project_id"));
					staticRules.put(gr.toString(), gr);
				}
				rs.close();
				
				
		    	
				rs = st.executeQuery("select item_id,rule_id from "+pid+".project_items_x_rules");
				System.out.println("select item_id,rule_id from "+pid+".project_items_x_rules");
				while(rs.next()) {
					String item = rs.getString("item_id");
					String rule = rs.getString("rule_id");
					try {
						ArrayList<String> tmp = items_x_rules.get(item);
						tmp.add(rule);
						items_x_rules.put(item, tmp);
					}catch(Exception V) {
						//Item has no entry
						ArrayList<String> tmp = new ArrayList<String>();
						tmp.add(rule);
						items_x_rules.put(item, tmp);
					}
				}
				
				rs.close();
				st.close();
				conn.close();
				
				
		    	
		    	Connection conn1 = Tools.spawn_connection();
		    	Connection conn2 = Tools.spawn_connection();
		    	
		    	PreparedStatement ps1 = conn1.prepareStatement("update "+pid+".project_items set pre_classification = ? where item_id = ?");
		    	PreparedStatement ps2 = conn2.prepareStatement("insert into "+pid+".project_classification_event(classification_event_id,item_id,segment_id,classification_method,rule_id,user_id,classification_date,classification_time) values (?,?,?,'"+DataInputMethods.BINARY_CLASSIFICATION+"',?,'"+account.getUser_id()+"',?,clock_timestamp())");
		    	
		    	ArrayList<DescriptionFetchRow> rws = DESCS.get(pid);
	    		
		    	HashMap<String,String> CID2UUID = new HashMap<String,String>();
		    	Connection conn4 = Tools.spawn_connection();
		    	Statement st4 = conn4.createStatement();
		    	ResultSet rs4 = st4.executeQuery("select segment_id,level_"+binaryClassificationParameters.getClassif_granularity().toString()+"_number from "+pid+".project_segments");
		    	while(rs4.next()) {
		    		CID2UUID.put(rs4.getString(2), rs4.getString("segment_id"));
		    	}
		    	rs4.close();
		    	st4.close();
		    	conn4.close();
		    	
		    	ArrayList<GenericClassRule> CLASSIFICATION_GRS = new ArrayList<GenericClassRule>();
		    	ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		    	
		    	
	    		for(DescriptionFetchRow rw:rws) {
	    			
	    			String aid = rw.getAid();
	    			String classif_cid;
	    			String classif_cname;
	    			String preclassif_cname;
	    			String classif_rule;
	    			
	    			
	    			if(ITEMS_DICO.containsKey(aid)) {
	    				classif_cid= ITEMS_DICO.get(aid).get(1);  
	    				classif_cname= ITEMS_DICO.get(aid).get(2);  
	    				classif_rule= ITEMS_DICO.get(aid).get(3);
	    				
	    				GenericClassRule gr = new GenericClassRule();
	    				try {
	    					gr.setMain(classif_rule.split("MAIN=")[1].split("\\|")[0]);
	    				}catch(Exception V) {
	    					
	    				}
	    				try {
	    					gr.setComp(classif_rule.split("\\|COMP=")[1].split("\\|")[0]);
	    				}catch(Exception V) {
	    					
	    				}
	    				gr.classif.set(0, CID2UUID.get(classif_cid));
	    				gr.classif.set(1, classif_cid);
	    				gr.classif.set(2, classif_cname);
	    				gr.active=true;
	    				gr.matched=true;
	    				
	    				try {
							ArrayList<String> tmp = items_x_rules.get(aid);
							tmp.add(gr.toString());
							items_x_rules.put(aid, tmp);
						}catch(Exception V) {
							//Item has no entry
							ArrayList<String> tmp = new ArrayList<String>();
							tmp.add(gr.toString());
							items_x_rules.put(aid, tmp);
						}
	    				
	    				int max_score = Short.MIN_VALUE;
	    				HashSet<GenericClassRule> bestRules = new HashSet<GenericClassRule>();
	    				
	    				for( String loopRuleDesc : items_x_rules.get(aid)) {
	    					GenericClassRule loopRule = staticRules.get(loopRuleDesc);
	    					Boolean loopRuleActive = loopRule.active;
	    					
	    					if(loopRuleActive && loopRule.classif.get(0)!=null) {
	    						int loopRuleScore = ManualRuleServices.scoreManualRule(loopRule);
	    						if(loopRuleScore<max_score) {
	    							continue;
	    						}else if(loopRuleScore==max_score) {
	    							bestRules.add(loopRule);
	    						}else {
	    							bestRules.clear();
	    							bestRules.add(loopRule);
	    							max_score = loopRuleScore;
	    						}
	    					}
	    				}
	    				GenericClassRule finalRule = ManualRuleServices.manageRuleDisambiguation(bestRules);
	    				if(finalRule!=null) {
	    					CLASSIFICATION_GRS.add(finalRule);
		    				String[] itemRule = new String [] {null,null};
		    				itemRule[0] = rw.getAuid();
		    				itemRule[1] = finalRule.toString();
		    				itemRuleMap.add(itemRule);
		    				String event_id = Tools.generate_uuid();
		    				ps2.setString(1, event_id);
		    				ps2.setString(2, rw.getAuid());
		    				ps2.setString(3, CID2UUID.get(classif_cid) );
		    				ps2.setString(4, finalRule.toString());
		    				ps2.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
		    				ps2.addBatch();
	    				}else{
	    					
	    				}
	    				
	    			}
	    			
	    			
	    			if(preclass_ITEMS_DICO.containsKey(aid)) {
	    				preclassif_cname= preclass_ITEMS_DICO.get(aid).get(2);
	    				ps1.setString(1, preclassif_cname);
	    				ps1.setString(2, rw.getAuid());
	    				ps1.addBatch();
	    				
	    			}
	    			
	    			
	    			
	    		}
	    		
	    		Tools.StoreAutoRules(account,CLASSIFICATION_GRS,itemRuleMap,true,DataInputMethods.BINARY_CLASSIFICATION);
    			
	    		ps2.executeBatch();
	    		//ps2W.executeBatch();
	    		ps1.executeBatch();
		    	;
		    	ps1.clearBatch();
		    	ps2.clearBatch();
		    	//ps2W.clearBatch();
		    	
		    	ps1.close();
		    	ps2.close();
		    	//ps2W.close();
		    	
		    	conn1.close();
		    	conn2.close();
		    	//conn2W.close();
		    	  	
		    	CID2UUID = null;
		    	
		    	return null;
		    }
		};
		mainTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			
		});

		mainTask.setOnFailed(e -> {
		    Throwable problem = mainTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		mainTask.setOnCancelled(e -> {
		    ;
			
		});
		
		Thread mainThread = new Thread(mainTask);; mainThread.setDaemon(true);
		mainThread.start();
	}

	private void launch_classification() throws ClassNotFoundException, SQLException {
		
		
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
				//Old code auto increment
				//download_progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				//preclass_download_progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				
			}
			
			});
		
		
		
		mainTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	donwload_vocabulary();
				return null;
		    }
		};
		mainTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			if(isLaunched) {
				main_tasks();
			}else {
				parent.setExpectedTime(tm.grid.get(0).remaining_time+tm.grid.get(14).remaining_time,this);
			}
			
		});

		mainTask.setOnFailed(e -> {
		    Throwable problem = mainTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		mainTask.setOnCancelled(e -> {
		    ;
			
		});
		
		Thread mainThread = new Thread(mainTask);; mainThread.setDaemon(true);
		mainThread.start();
		
		
		
		
		
	}
	
	@SuppressWarnings("static-access")
	private void main_tasks() {
		if(this.ref_desc_cardinality==0) {
			for(Node noeud:grille.getChildren()) {
			try {	
				if( grille.getColumnIndex(noeud)==3 && grille.getRowIndex(noeud)>=3 && grille.getRowIndex(noeud)<=21) {
					noeud.setDisable(true);
					((ProgressBar) noeud).setProgress(1);
				}
				
			}catch(Exception V) {
					
				}
			}
			tm.stopRow(1);
			launch_preclassification();
			return;
		}
		
		TargetTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	clean_targets();
				return null;
		    }
		};
		TargetTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			target_desc_progress.setProgress(1);
			update_classif_progress();
			
			try {
				apply_rules();
				rule_app_progress.setProgress(1);
				launch_preclassification();
			} catch (ClassNotFoundException e1) {
				ExceptionDialog.show("CF001 pg_class", "CF001 pg_class", "CF001 pg_class");
				e1.printStackTrace();
			} catch (SQLException e1) {
				ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
				e1.printStackTrace();
			}
			
			
		});

		TargetTask.setOnFailed(e -> {
		    Throwable problem = TargetTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		TargetTask.setOnCancelled(e -> {
		    ;
			
		});
		
		
		
		
		RuleGenTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	generate_rules();
				return null;
		    }
		};
		RuleGenTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			rule_gen_progress.setProgress(1.0);
			update_classif_progress();
			Thread TargetThread = new Thread(TargetTask);; TargetThread.setDaemon(true);
			TargetThread.start();
			
			
			
		});

		RuleGenTask.setOnFailed(e -> {
		    Throwable problem = RuleGenTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		RuleGenTask.setOnCancelled(e -> {
		    ;
			
		});
		
		
		
		
		cleanTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	//count_ref_descs();
				clean_references();
				return null;
		    }
		};
		cleanTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			reference_desc_progress.setProgress(1);
			update_classif_progress();
			
			Thread Rulethread = new Thread(RuleGenTask);; Rulethread.setDaemon(true);
			Rulethread.start();
			
		});

		cleanTask.setOnFailed(e -> {
		    Throwable problem = cleanTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		cleanTask.setOnCancelled(e -> {
		    ;
			
		});
		
		Thread cleanThread = new Thread(cleanTask);; cleanThread.setDaemon(true);
		cleanThread.start();
	}

	public void donwload_vocabulary() throws ClassNotFoundException, SQLException, IOException {
		count_cardinals();
		
		
		double char_weight = GlobalConstants.UNI_WEIGHT;
		double spell_weight = GlobalConstants.SPELL_WEIGHT;
		double abv_weight = GlobalConstants.ABV_WEIGHT;
			
		double increment_step=
				(this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0) +
				(this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0) + 
				(this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0); 

		char_weight = (1.0/increment_step)*(this.ref_desc_cardinality>0?1:0);
		spell_weight = (GlobalConstants.SPELL_WEIGHT/GlobalConstants.UNI_WEIGHT)*char_weight;
		abv_weight = (GlobalConstants.ABV_WEIGHT/GlobalConstants.UNI_WEIGHT)*char_weight;
		
		tm = new TimeMaster();
		tm.initiate_grid(this.target_desc_cardinality,this.ref_desc_cardinality,this.preclass_ref_desc_cardinality);
		
		//Class
		tm.fillRow(this,2, time21, time22, time23, timeTemplate.grid.get(1),1,isLaunched);
		tm.fillRow(this,4, time41, time42, time43, timeTemplate.grid.get(3),this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0,isLaunched);
		tm.fillRow(this,5, time51, time52, time53, timeTemplate.grid.get(4),this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0,isLaunched);
		tm.fillRow(this,6, time61, time62, time63, timeTemplate.grid.get(5),this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0,isLaunched);
		tm.fillRow(this,8, time81, time82, time83, timeTemplate.grid.get(7),1,isLaunched);
		tm.fillRow(this,9, time91, time92, time93, timeTemplate.grid.get(8),1,isLaunched);
		tm.fillRow(this,11, time111, time112, time113, timeTemplate.grid.get(10),this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0,isLaunched);
		tm.fillRow(this,12, time121, time122, time123, timeTemplate.grid.get(11),this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0,isLaunched);
		tm.fillRow(this,13, time131, time132, time133, timeTemplate.grid.get(12),this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0,isLaunched);
		tm.fillRow(this,14, time141, time142, time143, timeTemplate.grid.get(13),1,isLaunched);
		
		
		tm.fillSummaryRow(10, time101, time102, time103, (HashSet<Integer>) Stream.of(11,12,13).collect(Collectors.toSet()));
		tm.fillSummaryRow(7, time71, time72, time73, (HashSet<Integer>) Stream.of(8,9).collect(Collectors.toSet()));
		tm.fillSummaryRow(3, time31, time32, time33, (HashSet<Integer>) Stream.of(4,5,6).collect(Collectors.toSet()));
		tm.fillSummaryRow(1, time11, time12, time13, (HashSet<Integer>) Stream.of(2,3,7,10,14).collect(Collectors.toSet()));
		
		char_weight = GlobalConstants.UNI_WEIGHT;
		spell_weight = GlobalConstants.SPELL_WEIGHT;
		abv_weight = GlobalConstants.ABV_WEIGHT;
		
		increment_step=
				(this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0) +
				(this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0) + 
				(this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0); 

		char_weight = (1.0/increment_step)*(this.preclass_ref_desc_cardinality>0?1:0);
		spell_weight = (GlobalConstants.SPELL_WEIGHT/GlobalConstants.UNI_WEIGHT)*char_weight;
		abv_weight = (GlobalConstants.ABV_WEIGHT/GlobalConstants.UNI_WEIGHT)*char_weight;
		
		
		
		
		//Preclass
		tm.fillRow(this,16, time161, time162, time163, timeTemplate.grid.get(15),1,isLaunched);
		tm.fillRow(this,18, time181, time182, time183, timeTemplate.grid.get(17),this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0,isLaunched);
		tm.fillRow(this,19, time191, time192, time193, timeTemplate.grid.get(18),this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0,isLaunched);
		tm.fillRow(this,20, time201, time202, time203, timeTemplate.grid.get(19),this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0,isLaunched);
		tm.fillRow(this,22, time221, time222, time223, timeTemplate.grid.get(21),1,isLaunched);
		tm.fillRow(this,23, time231, time232, time233, timeTemplate.grid.get(22),1,isLaunched);
		tm.fillRow(this,25, time251, time252, time253, timeTemplate.grid.get(24),this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0,isLaunched);
		tm.fillRow(this,26, time261, time262, time263, timeTemplate.grid.get(25),this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0,isLaunched);
		tm.fillRow(this,27, time271, time272, time273, timeTemplate.grid.get(26),this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0,isLaunched);
		tm.fillRow(this,28, time281, time282, time283, timeTemplate.grid.get(27),1,isLaunched);
		
		tm.fillSummaryRow(24, time241, time242, time243, (HashSet<Integer>) Stream.of(25,26,27).collect(Collectors.toSet()));
		tm.fillSummaryRow(21, time211, time212, time213, (HashSet<Integer>) Stream.of(22,23).collect(Collectors.toSet()));
		tm.fillSummaryRow(17, time171, time172, time173, (HashSet<Integer>) Stream.of(18,19,20).collect(Collectors.toSet()));
		tm.fillSummaryRow(15, time151, time152, time153, (HashSet<Integer>) Stream.of(16,17,21,24,28).collect(Collectors.toSet()));
		
		
		
	
		
		//tm.startRow(2); Non segmented_contructor
		//tm.startRow(16); Non segmented_contructor
		
		
		
		
		
		
		

		Platform.runLater(new Runnable(){

			@Override
			public void run() {

				update_static_graph();
				
			}
			
			});
		
		
		
		
		
		
		
		download_data(false);
		
		
        	
	}

	

	private void download_data(boolean IS_PRECLASS) throws ClassNotFoundException, SQLException, IOException {
		
		DESCS.clear();
		
		
		Integer lower;
		Integer upper;
		if(!IS_PRECLASS) {
			
			tm.startRow(2,this.classif_downloadSegmenter);
			tm.startSummaryRow(1);
			
			
			for(String pidx:REFERENCE_PROJECTS) {
				while(true) {
					ArrayList<Integer> tmp = this.classif_downloadSegmenter.get_next_range(pidx);
					lower = tmp.get(0);
					upper = tmp.get(1);
					if(upper==0) {
						break;
					}
					
					FETCH_DESCRIPTIONS(pidx,true,lower,upper,IS_PRECLASS);
				}
				
				
			}
			
			while(true) {
				ArrayList<Integer> tmp = this.classif_downloadSegmenter.get_next_range(pid);
				lower = tmp.get(0);
				upper = tmp.get(1);
				if(upper==0) {
					break;
				}
				
				FETCH_DESCRIPTIONS(this.pid,false,lower,upper,IS_PRECLASS);
			}
			
			download_progress.setProgress(1.0);
			tm.stopRow(2);
			

			this.bigramCount = Bigram.trainNGram(DESCS,this.binaryClassificationParameters);
			//boolean alphabetOnlyParameter, boolean decodeParameter
			

			
			cr = new CorpusReader(this.bigramCount);
	        cmr = new ConfusionMatrixReader();
			
			
		}else {
			tm.startRow(16,this.preclassif_downloadSegmenter);
			tm.startSummaryRow(15);
			
			for(String pidx:preclass_REFERENCE_PROJECTS) {
				while(true) {
					ArrayList<Integer> tmp = this.preclassif_downloadSegmenter.get_next_range(pidx);
					lower = tmp.get(0);
					upper = tmp.get(1);
					if(upper==0) {
						break;
					}
					
					FETCH_DESCRIPTIONS(pidx,true,lower,upper,IS_PRECLASS);
				}
				
				
			}
			
			while(true) {
				ArrayList<Integer> tmp = this.preclassif_downloadSegmenter.get_next_range(pid);
				lower = tmp.get(0);
				upper = tmp.get(1);
				if(upper==0) {
					break;
				}
				
				FETCH_DESCRIPTIONS(this.pid,false,lower,upper,IS_PRECLASS);
			}
			
			preclass_download_progress.setProgress(1.0);
			tm.stopRow(16);
			
			this.bigramCount = Bigram.trainNGram(DESCS,this.binaryClassificationParameters);
			//boolean alphabetOnlyParameter, boolean decodeParameter
			

			
			cr = new CorpusReader(this.bigramCount);
	        cmr = new ConfusionMatrixReader();
			
		}
		
		
		
		
		
	}

	@SuppressWarnings("unchecked")
	protected void update_static_graph() {
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
		static_classifLabel.setVisible(false);
		static_preclassifLabel.setVisible(false);
		
		
		
		
		double ref_desc_ratio = this.ref_desc_cardinality*1.0/this.target_desc_cardinality;
		double preclass_ref_desc_ratio = this.preclass_ref_desc_cardinality*1.0/this.target_desc_cardinality;
		
		//find closest coverage and accuracy for classification 
		Integer closest_bs=null;
		for(AbaqueRow rw:ABAQUE) {
			Integer rwbs = rw.getBs();
			if(!(closest_bs!=null) && Math.abs(rwbs-(ref_desc_ratio)*this.binaryClassificationParameters.getClassif_buildSampleSize())<=10) {
				closest_bs=rwbs;
				break;
			}
		}
		
		if(!(closest_bs!=null)) {
			closest_bs = GlobalConstants.MAX_BS;
		}
		
		try {
			Connection conn = Tools.spawn_connection();
			//#
			PreparedStatement stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs);
			stmt.setDouble(2, Math.min(Math.floor(this.binaryClassificationParameters.getClassif_Ta()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.floor(this.binaryClassificationParameters.getClassif_Tb()), GlobalConstants.MAX_TC));
			//#
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			double new_accuracy = rs.getDouble(1);
			double new_coverage = rs.getDouble(2);
			//#
			stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs+10);
			stmt.setDouble(2, Math.min(Math.floor(this.binaryClassificationParameters.getClassif_Ta()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.floor(this.binaryClassificationParameters.getClassif_Tb()), GlobalConstants.MAX_TC));
			
			rs = stmt.executeQuery();
			rs.next();
			Double second_coverage=null;
			try {
				second_coverage = rs.getDouble(2);
			}catch (Exception V){
				second_coverage = null;
			}
			rs.close();
			stmt.close();
			conn.close();
			
			if(this.binaryClassificationParameters.getClassif_buildSampleSize()==0) {
				new_coverage = 0;
			}
			if(this.binaryClassificationParameters.getClassif_buildSampleSize()<10) {
				new_coverage = new_coverage*(this.binaryClassificationParameters.getClassif_buildSampleSize()/10);
			}
			if(second_coverage!=null &&!(this.binaryClassificationParameters.getClassif_buildSampleSize()<10)) {
				new_coverage = new_coverage + (second_coverage-new_coverage)*((this.binaryClassificationParameters.getClassif_buildSampleSize()-closest_bs)/10);
			}
		
			new_coverage = ref_desc_ratio>0?new_coverage:0;
			this.classification_graph_coverage = new_coverage;
			this.classification_graph_accuracy = (new_coverage!=0)?new_accuracy:0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		//find closest coverage and accuracy for preclassification 
		closest_bs=null;
		for(AbaqueRow rw:ABAQUE) {
			Integer rwbs = rw.getBs();
			if(!(closest_bs!=null) && Math.abs(rwbs-(preclass_ref_desc_ratio)*this.binaryClassificationParameters.getPreclassif_buildSampleSize())<=10) {
				closest_bs=rwbs;
				break;
			}
		}
		if(!(closest_bs!=null)) {
			closest_bs = GlobalConstants.MAX_BS;
		}
		try {
			Connection conn = Tools.spawn_connection();
			//#
			PreparedStatement stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs);
			stmt.setDouble(2, Math.min(Math.floor(this.binaryClassificationParameters.getPreclassif_Ta()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.floor(this.binaryClassificationParameters.getPreclassif_Tb()), GlobalConstants.MAX_TC));
			;
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			double new_accuracy = rs.getDouble(1);
			double new_coverage = rs.getDouble(2);
			//#
			stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs+10);
			stmt.setDouble(2, Math.min(Math.floor(this.binaryClassificationParameters.getPreclassif_Ta()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.floor(this.binaryClassificationParameters.getPreclassif_Tb()), GlobalConstants.MAX_TC));
			
			rs = stmt.executeQuery();
			rs.next();
			Double second_coverage=null;
			try {
				second_coverage = rs.getDouble(2);
			}catch (Exception V){
				second_coverage = null;
			}
			rs.close();
			stmt.close();
			conn.close();
			
			if(this.binaryClassificationParameters.getPreclassif_buildSampleSize()==0) {
				new_coverage = 0;
			}
			if(this.binaryClassificationParameters.getPreclassif_buildSampleSize()<10) {
				new_coverage = new_coverage*(this.binaryClassificationParameters.getPreclassif_buildSampleSize()/10);
			}
			if(second_coverage!=null &&!(this.binaryClassificationParameters.getPreclassif_buildSampleSize()<10)) {
				new_coverage = new_coverage + (second_coverage-new_coverage)*((this.binaryClassificationParameters.getPreclassif_buildSampleSize()-closest_bs)/10);
				}
			
			
			new_coverage = preclass_ref_desc_ratio>0?new_coverage:0;
			this.preclassification_graph_coverage = new_coverage;
			this.preclassification_graph_accuracy = (new_coverage!=0)?new_accuracy:0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		
		
		//Defining the y axis
		xAxe.setCategories(FXCollections.<String>observableArrayList(Arrays.asList
				   ("Estimated coverage using reference data")));
		//Prepare XYChart.Series objects by setting data 
		XYChart.Series<String, Number> series1 = new XYChart.Series<>(); 
		series1.setName("Classification quality: "+(int)Math.floor(this.classification_graph_accuracy)+"%");
		series1.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.classification_graph_coverage)); 
		XYChart.Series<String, Number> series2 = new XYChart.Series<>(); 
		series2.setName("Pre-classification quality: "+(int)Math.floor(this.preclassification_graph_accuracy)+"%");
		series2.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.preclassification_graph_coverage-this.classification_graph_coverage));
		graph.getData().clear();
		graph.getData().addAll(series1,series2);
		yAxe.setAutoRanging(false);
		yAxe.setUpperBound(100);
		
		;
		;
		
		static_classifLabel.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_graph_coverage));
		static_classifTextFlow.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_graph_coverage));
		
		static_classifLabel.setVisible(false);
		static_classifTextFlow.setVisible(this.classification_graph_coverage>10);
		
		Text text1 = new Text();
		text1.setText("Coverage: "+(int)Math.floor(this.classification_graph_coverage)+"%");
		text1.toFront();
		static_classifLabel.setText("Coverage: "+(int)Math.floor(this.classification_graph_coverage)+"%\nQuality "+(int)Math.floor(this.classification_graph_accuracy)+"%");
		//static_classifLabel.toFront();
		Text text2 = new Text();
		text2.setText("\nEst. quality: "+(int)Math.floor(this.classification_graph_accuracy)+"%");
		text2.toFront();
		static_classifTextFlow.getChildren().clear();
		
		text1.setStyle(static_classifLabel.getStyle());
		text1.setStyle("-fx-font-weight	: bold");
		text1.setFill(static_classifLabel.getTextFill());
		
		text2.setStyle(static_classifLabel.getStyle());
		text2.setFill(static_classifLabel.getTextFill());
		text2.setStyle("-fx-font-style: italic");
		
		
		static_classifTextFlow.getChildren().add(text1);
		static_classifTextFlow.getChildren().add(text2);
		
		
		DoubleProperty fontSize = new SimpleDoubleProperty(10);
		fontSize.bind(yAxe.widthProperty().add(yAxe.heightProperty()).divide(70));
		static_classifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		static_classifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		
		
		static_preclassifLabel.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-(this.preclassification_graph_coverage)));
		static_preclassifTextFlow.setTranslateY((graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-(this.preclassification_graph_coverage)));
		
		static_preclassifLabel.setVisible(false);
		static_preclassifTextFlow.setVisible((this.preclassification_graph_coverage-this.classification_graph_coverage)>10);
		Text text3 = new Text();
		text3.setText("Coverage: "+(int)Math.floor(this.preclassification_graph_coverage-this.classification_graph_coverage)+"%");
		static_preclassifLabel.setText("Coverage: "+(int)Math.floor(this.preclassification_graph_coverage-this.classification_graph_coverage)+"%\nQuality "+(int)Math.floor(this.preclassification_graph_accuracy)+"%");
		//static_preclassifLabel.toFront();
		Text text4 = new Text();
		text4.setText("\nEst. quality: "+(int)Math.floor(this.preclassification_graph_accuracy)+"%");
		text4.toFront();
		static_preclassifTextFlow.getChildren().clear();
		
		text3.setStyle(static_preclassifLabel.getStyle());
		text3.setStyle("-fx-font-weight	: bold");
		text3.setFill(static_preclassifLabel.getTextFill());
		text4.setStyle(static_preclassifLabel.getStyle());
		text4.setFill(static_preclassifLabel.getTextFill());
		text4.setStyle("-fx-font-style: italic");
		
		
		static_preclassifTextFlow.getChildren().add(text3);
		static_preclassifTextFlow.getChildren().add(text4);
		
		static_preclassifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		static_preclassifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		
		
		
		
		//initalize the actual graph
		update_actual_graph();
		

		
	}

	@SuppressWarnings("unchecked")
	private void update_actual_graph() {
		
		actualgraph.setAnimated(false);
		actualgraph.setLegendVisible(false);
		actualgraph.setHorizontalGridLinesVisible(false);
		actualgraph.setVerticalGridLinesVisible(false);
		actualgraph.setVerticalZeroLineVisible(false);
		actualgraph.setHorizontalZeroLineVisible(false);
		actualgraph.getYAxis().setTickMarkVisible(false);
		actualgraph.getXAxis().setTickMarkVisible(false);
		actualgraph.getYAxis().setTickLabelsVisible(false);
		actualgraph.getXAxis().setTickLabelsVisible(false);
		dynamic_classifLabel.setVisible(false);
		dynamic_preclassifLabel.setVisible(false);
		
		
		HashSet<String> preclass_items = new HashSet<String>();
		preclass_items.addAll(preclass_ITEMS_DICO.keySet());
		preclass_items.addAll(ITEMS_DICO.keySet());
		
		
		double preclass_y = (100.0 * preclass_items.size())/this.target_desc_cardinality;
		double class_y = (100.0 * ITEMS_DICO.size())/this.target_desc_cardinality;
		
		dynamic_classifLabel.setTranslateY((actualgraph.getHeight()-actualyAxe.getHeight()) + actualyAxe.getHeight()*0.01*(100-class_y));
		dynamic_classifLabel.setVisible(class_y>5);
		dynamic_classifLabel.setText("Coverage: "+(int)Math.floor(class_y)+"%");
		dynamic_classifLabel.toFront();
		DoubleProperty fontSize = new SimpleDoubleProperty(10);
		fontSize.bind(actualyAxe.widthProperty().add(actualyAxe.heightProperty()).divide(60));
		dynamic_classifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		dynamic_preclassifLabel.setTranslateY((actualgraph.getHeight()-actualyAxe.getHeight()) + actualyAxe.getHeight()*0.01*(100-preclass_y));
		dynamic_preclassifLabel.setVisible((preclass_y-class_y)>5);
		dynamic_preclassifLabel.setText("Coverage: "+(int)Math.floor(preclass_y-class_y)+"%");
		dynamic_preclassifLabel.toFront();
		dynamic_preclassifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		
		
		
		
		
		
		actualxAxe.setCategories(FXCollections.<String>observableArrayList(Arrays.asList
				   ("Actual coverage using reference data")));
		//Prepare XYChart.Series objects by setting data 
		XYChart.Series<String, Number> Live_series1 = new XYChart.Series<>(); 
		Live_series1.setName("Real-time classification");
		Live_series1.getData().add(new XYChart.Data<>("Actual coverage using reference data", this.classification_graph_coverage)); 
		XYChart.Series<String, Number> Live_series2 = new XYChart.Series<>(); 
		Live_series2.setName("Real-time pre-classification");
		Live_series2.getData().add(new XYChart.Data<>("Actual coverage using reference data", (preclass_y-class_y)));
		Live_series1.getData().clear();
		Live_series1.getData().add(new XYChart.Data<>("Actual coverage using reference data", (class_y)));
		actualgraph.getData().clear();
		actualgraph.getData().addAll(Live_series1,Live_series2);
		actualyAxe.setAutoRanging(false);
		actualyAxe.setUpperBound(100);
	}

	private void count_cardinals() throws ClassNotFoundException, SQLException {
		if(cardinal_counted) {
			return;
		}		

		this.classif_downloadSegmenter = new DownloadSegmenter();
		this.preclassif_downloadSegmenter = new DownloadSegmenter();
		cardinal_counted=true;
		
		Connection conn = Tools.spawn_connection();
		Connection conn2 = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn2.createStatement();
		ResultSet rs = null;
		ResultSet rs2 = null;
		
		
		for(String pid:this.preclass_REFERENCE_PROJECTS) {
			//#
			rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
			rs.next();
			PROJECT2DATAMAP .put(pid,pid+".project_items");
			PROJECT2LANGUAGE.put(pid,rs.getString(1));
			//#
			rs2 = stmt2.executeQuery("select count( distinct item_id ) from "+pid+".project_items where item_id in (select distinct item_id from "+pid+".project_classification_event where segment_id is not null)");
			rs2.next();
			this.preclassif_downloadSegmenter.initialize_count(pid, rs2.getInt(1));
			this.preclass_ref_desc_cardinality=this.preclass_ref_desc_cardinality+ rs2.getInt(1);
			
			
			rs2.close();
		}
		
		for(String pid:this.REFERENCE_PROJECTS) {
			
			//#
			rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
			rs.next();
			PROJECT2DATAMAP .put(pid,pid+".project_items");
			PROJECT2LANGUAGE.put(pid,rs.getString(1));
			//#
			rs2 = stmt2.executeQuery("select count( distinct item_id ) from "+pid+".project_items where item_id in (select distinct item_id from "+pid+".project_classification_event where segment_id is not null)");
			rs2.next();
			this.classif_downloadSegmenter.initialize_count(pid, rs2.getInt(1));
			this.ref_desc_cardinality=this.ref_desc_cardinality+ rs2.getInt(1);
			
			
			rs2.close();
		}
		//#
		rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
		rs.next();
		PROJECT2DATAMAP .put(pid,pid+".project_items");
		PROJECT2LANGUAGE.put(pid,rs.getString(1));
		this.classif_downloadSegmenter.initialize_count(pid, this.target_desc_cardinality);
		this.preclassif_downloadSegmenter.initialize_count(pid, this.target_desc_cardinality);
		
		
		rs.close();
		
		stmt.close();
		stmt2.close();
		
		conn.close();
		conn2.close();

		
	}

	private void FETCH_DESCRIPTIONS(String pid, boolean isREFERENCE, int LOWER, int UPPER,boolean IS_PRECLASSIF) throws ClassNotFoundException, SQLException {
		
		
		increment_data_progressbars(IS_PRECLASSIF);
		
		Connection conn = Tools.spawn_connection();
		String query=null;
		if(isREFERENCE) {
			//Example query: select client_item_number,short_description,long_description,material_group,level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM u95a5fb608e6a47548432695ae0f78968.project_classification_event where item_id in (select distinct item_id from u95a5fb608e6a47548432695ae0f78968.project_items where row_number between 20000 and 40000) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  u95a5fb608e6a47548432695ae0f78968.project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join u95a5fb608e6a47548432695ae0f78968.project_items on rich_events.item_id = project_items.item_id
			query="select project_items.item_id, client_item_number,short_description,long_description,material_group,level_1_number,level_1_name_translated,level_2_number,level_2_name_translated,level_3_number,level_3_name_translated,level_4_number,level_4_name_translated from (select item_id , level_1_number,level_1_name_translated,level_2_number,level_2_name_translated,level_3_number,level_3_name_translated,level_4_number,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM "+pid+".project_classification_event where item_id in (select distinct item_id from "+pid+".project_items where row_number between "+LOWER+" and "+UPPER+") ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  "+pid+".project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join "+pid+".project_items on rich_events.item_id = project_items.item_id";
			;
			;
		}else {
			query="SELECT project_items.item_id, client_item_number,short_description,long_description,material_group FROM "+PROJECT2DATAMAP.get(pid)+" where row_number between "+LOWER+" and "+UPPER+"";
		}
		

		PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		//#
		;
		ResultSet rs = stmt.executeQuery();
		
		int rowcount = 0;
		if (rs.last()) {
			  rowcount = rs.getRow();
			  rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
			}
		ArrayList<DescriptionFetchRow> RESULT = new ArrayList<DescriptionFetchRow>(rowcount);
		
		while(rs.next()) {
			DescriptionFetchRow row = new DescriptionFetchRow();
			row.setAuid(rs.getString("item_id"));
			row.setAid(rs.getString("client_item_number"));
			row.setSd(rs.getString("short_description"));
			row.setLd(rs.getString("long_description"));
			row.setMG(rs.getString("material_group"));
			try {
				row.setCid(rs.getString("level_"+(IS_PRECLASSIF?this.binaryClassificationParameters.getPreclassif_granularity().toString():this.binaryClassificationParameters.getClassif_granularity().toString())+"_number"));
				row.setCname(rs.getString("level_"+(IS_PRECLASSIF?this.binaryClassificationParameters.getPreclassif_granularity().toString():this.binaryClassificationParameters.getClassif_granularity().toString())+"_name_translated"));
			}catch(Exception V) {
				if(isREFERENCE) {
					V.printStackTrace(System.err);
				}
			}
			RESULT.add(row);
			
		}
		try{
			DESCS.get(pid).addAll(RESULT);
		}catch(Exception V) {
			//First segment downloaded in this project
			DESCS.put(pid, RESULT);
		}
		
		
		
		
		
		rs.close();
		stmt.close();
		conn.close();
		
		
	}
	
	
	
	
	private void increment_data_progressbars(Boolean IS_PRECLASS) {
		if(!IS_PRECLASS) {
			download_progress.setProgress(download_progress.getProgress()+(1.0/this.classif_downloadSegmenter.getSegment_no()));
			tm.increment_data_row(2,this.classif_downloadSegmenter);
			update_classif_progress();
			
		}else {
			preclass_download_progress.setProgress(preclass_download_progress.getProgress()+(1.0/this.preclassif_downloadSegmenter.getSegment_no()));
			tm.increment_data_row(16,this.preclassif_downloadSegmenter);
			update_preclassif_progress();
		}
		
	}

	protected void preclass_clean_targets() throws IOException, ClassNotFoundException, SQLException {
		if(this.binaryClassificationParameters.getPreclassif_targetDescriptionType().toString().startsWith("raw") || this.preclass_ref_desc_cardinality==0) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		
        
		HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
		
		ArrayList<DescriptionFetchRow>  rs = DESCS.get(pid);
		for(DescriptionFetchRow row:rs) {
			
			if(preclass_target_char_clean_progress.getProgress()>0.99){preclass_target_char_clean_progress_complete = true;}
			if(preclass_target_abv_clean_progress.getProgress()>0.99){preclass_target_abv_clean_progress_complete = true;}
			if(preclass_target_spell_clean_progress.getProgress()>0.99){preclass_target_spell_clean_progress_complete = true;}
			
			
			
			//
			double char_weight = GlobalConstants.UNI_WEIGHT;
			double spell_weight = GlobalConstants.SPELL_WEIGHT;
			double abv_weight = GlobalConstants.ABV_WEIGHT;
			
				
			double increment_step=
					(this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0) +
					(this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0) + 
					(this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0); 
			increment_step=1.0/increment_step;
			
			preclass_target_desc_progress.setProgress(preclass_target_desc_progress.getProgress()+(1.0/target_desc_cardinality));
			update_preclassif_progress();
			
			if(this.binaryClassificationParameters.getPreclassif_cleanChar()) {
				tm.startRow(25, target_desc_cardinality*(char_weight*increment_step));
				tm.increment(25);
				
				preclass_target_char_clean_progress.setProgress(preclass_target_char_clean_progress.getProgress()+(1.0/(char_weight * increment_step*target_desc_cardinality)));
			}else {
							fast_forward(preclass_target_char_clean_progress); preclass_target_char_clean_progress_complete=true;
			}
			if(this.binaryClassificationParameters.getPreclassif_cleanAbv()) {
				if(preclass_target_char_clean_progress_complete) {
					tm.startRow(26, target_desc_cardinality*(abv_weight*increment_step));
					tm.increment(26);
					
					preclass_target_abv_clean_progress.setProgress(preclass_target_abv_clean_progress.getProgress()+(1.0/(abv_weight * increment_step*target_desc_cardinality)));
				}
			}else {
				if(preclass_target_char_clean_progress_complete) {
									fast_forward(preclass_target_abv_clean_progress); preclass_target_abv_clean_progress_complete=true;
				}
				
			}
			if(this.binaryClassificationParameters.getPreclassif_cleanSpell()) {
				if(preclass_target_char_clean_progress_complete && preclass_target_abv_clean_progress_complete) {
					tm.startRow(27, target_desc_cardinality*(spell_weight*increment_step));
					tm.increment(27);
					
					
					preclass_target_spell_clean_progress.setProgress(preclass_target_spell_clean_progress.getProgress()+(1.0/(spell_weight * increment_step*target_desc_cardinality)));
				}
				
			}else {
				if(preclass_target_char_clean_progress_complete && preclass_target_abv_clean_progress_complete) {

									fast_forward(preclass_target_spell_clean_progress); preclass_target_spell_clean_progress_complete=true;
				}
			}
			
			if(preclass_target_char_clean_progress_complete) {
				tm.stopRow(25);
			}if(preclass_target_abv_clean_progress_complete) {
				tm.stopRow(26);
			}if(preclass_target_spell_clean_progress_complete) {
				tm.stopRow(27);
			}
			
			

			//Correction
			ArrayList<String> input = new ArrayList<String>();
			input.add(row.getSd());
			input.add(row.getLd());
			HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,false);
			input =null;
			preclass_CLEAN_TARGETS.put(row.getAid(),output);
			
			
		}
	}

	private void fast_forward(ProgressBar pbar) {
		
		forwardTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	if(forwarding) {
		    		TimeUnit.MILLISECONDS.sleep(1200);
		    		pbar.setProgress(1);
			    	forwarding=false;
		    	}else {
		    		forwarding = true;
		    		TimeUnit.MILLISECONDS.sleep(600);
			    	pbar.setProgress(1);
			    	forwarding=false;
		    	}
		    	return null;
		    	
		    	/*
		    	if(forwarding ) {
		    		TimeUnit.MILLISECONDS.sleep(400);
		    	}else {
		    		forwarding = true;
		    		TimeUnit.MILLISECONDS.sleep(100);
			    	
		    	}
		    	set_semi_opaque(pbar);
		    	for(int i=1;i<3;i++) {
		    		TimeUnit.MILLISECONDS.sleep(200);
		    		double p = i*0.5;
		    		pbar.setProgress(p);
		    		
		    	}
				forwarding = false;
				
				return null;*/
		    }
		};
		forwardTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
		});

		forwardTask.setOnFailed(e -> {
		    Throwable problem = forwardTask.getException();
		    /* code to execute if task throws exception */
		    
		    problem.printStackTrace(System.err);
		});

		forwardTask.setOnCancelled(e -> {
		    ;
			
		});
		
		Thread forwardThread = new Thread(forwardTask);; forwardThread.setDaemon(true);
		forwardThread.start();
		
		
	}

	@SuppressWarnings("static-access")
	public void set_semi_opaque(Node node) {
		
		int ligne = grille.getRowIndex(node);
		
		
		
		
		for(Node noeud:grille.getChildren()) {
			HashSet<Integer> tmp = new HashSet<Integer>();
			tmp.add(1);
			tmp.add(5);
			tmp.add(6);
			tmp.add(7);
			
			if(!(grille.getRowIndex(noeud)!=null)) {
				continue;
			}
			if(grille.getRowIndex(noeud)==ligne) {
				if(tmp.contains(grille.getColumnIndex(noeud))) {
					noeud.setOpacity(0.5);
				}
				if(grille.getColumnIndex(noeud)==3) {
					noeud.setDisable(true);
				}
			}
			if(grille.getRowIndex(noeud)==43) {
				noeud.setVisible(false);
			}
			
		}
	}

	private void clean_targets() throws ClassNotFoundException, SQLException, IOException {
		
		if(this.binaryClassificationParameters.getClassif_targetDescriptionType().toString().startsWith("raw") || this.ref_desc_cardinality==0) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		tm.startSummaryRow(10);
        
		HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
		
		ArrayList<DescriptionFetchRow>  rs = DESCS.get(pid);
		for(DescriptionFetchRow row:rs) {
			
			
			if(target_char_clean_progress.getProgress()>0.99){target_char_clean_progress_complete = true;}
			if(target_abv_clean_progress.getProgress()>0.99){target_abv_clean_progress_complete = true;}
			if(target_spell_clean_progress.getProgress()>0.99){target_spell_clean_progress_complete = true;}
			
			
			
			//
			double char_weight = GlobalConstants.UNI_WEIGHT;
			double spell_weight = GlobalConstants.SPELL_WEIGHT;
			double abv_weight = GlobalConstants.ABV_WEIGHT;
				
			double increment_step=
					(this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0) +
					(this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0) + 
					(this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0); 
			increment_step=1.0/increment_step;
			
			target_desc_progress.setProgress(target_desc_progress.getProgress()+(1.0/target_desc_cardinality));
			update_classif_progress();
			
			if(this.binaryClassificationParameters.getClassif_cleanChar()) {
				tm.startRow(11, target_desc_cardinality*(char_weight*increment_step));
				tm.increment(11);
				
				target_char_clean_progress.setProgress(target_char_clean_progress.getProgress()+(1.0/(char_weight * increment_step*target_desc_cardinality)));
			}else {
							fast_forward(target_char_clean_progress); target_char_clean_progress_complete=true;
			}
			if(this.binaryClassificationParameters.getClassif_cleanAbv()) {
				if(target_char_clean_progress_complete) {
					tm.startRow(12, target_desc_cardinality*(abv_weight*increment_step));
					tm.increment(12);
					
					target_abv_clean_progress.setProgress(target_abv_clean_progress.getProgress()+(1.0/(abv_weight * increment_step*target_desc_cardinality)));
				}
			}else {
				if(target_char_clean_progress_complete) {
									fast_forward(target_abv_clean_progress); target_abv_clean_progress_complete=true;
				}
				
			}
			if(this.binaryClassificationParameters.getClassif_cleanSpell()) {
				if(target_char_clean_progress_complete && target_abv_clean_progress_complete) {
					tm.startRow(13, target_desc_cardinality*(spell_weight*increment_step));
					tm.increment(13);
					
					target_spell_clean_progress.setProgress(target_spell_clean_progress.getProgress()+(1.0/(spell_weight * increment_step*target_desc_cardinality)));
				}
				
			}else {
				if(target_char_clean_progress_complete && target_abv_clean_progress_complete) {
									fast_forward(target_spell_clean_progress); target_spell_clean_progress_complete=true;
				}
				
			}
			
			if(target_char_clean_progress_complete) {
				tm.stopRow(11);
			}if(target_abv_clean_progress_complete) {
				tm.stopRow(12);
			}if(target_spell_clean_progress_complete) {
				tm.stopRow(13);
			}
			

			//Correction
			ArrayList<String> input = new ArrayList<String>();
			input.add(row.getSd());
			input.add(row.getLd());
			HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,true);
			input =null;
			CLEAN_TARGETS.put(row.getAid(),output);
			
			
		}
		tm.stopRow(10);
	}
	
	private void preclass_generate_rules() {
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tm.startSummaryRow(21);
		tm.startRow(22);
		
		
		this.preclass_buildRows = Tools.RandomSample(preclass_ref_desc_cardinality,this.binaryClassificationParameters.getPreclassif_buildSampleSize());
		
		int base_desc_count = 0;
		
		String target_desc = this.binaryClassificationParameters.getPreclassif_baseDescriptionType().toString();
		for(String aid:preclass_CLEAN_REFERENCES.keySet()) {
			
			preclass_word_gen_progress.setProgress(preclass_word_gen_progress.getProgress()+(1.0/preclass_ref_desc_cardinality));
			preclass_rule_gen_progress.setProgress(preclass_rule_gen_progress.getProgress()+(0.8/preclass_ref_desc_cardinality));
			update_preclassif_progress();
			
			try {
				if( preclass_CLEAN_REFERENCES.get(aid).get("cid").length() <4 || !preclass_buildRows.contains(base_desc_count)) {
					base_desc_count=base_desc_count+1;
					continue;
				}}catch(Exception G){
					update_preclassif_progress();
					preclass_rule_set_progress.setProgress(preclass_rule_set_progress.getProgress()+(1.0)/preclass_CLASSIFICATION_RULES.size());
					preclass_rule_gen_progress.setProgress(preclass_rule_gen_progress.getProgress()+( 1.0/ (0.2 * preclass_ref_desc_cardinality * preclass_CLASSIFICATION_RULES.size() )));
					base_desc_count=base_desc_count+1;
					continue;
				}
				base_desc_count=base_desc_count+1;
				String description = preclass_CLEAN_REFERENCES.get(aid).get(target_desc);
				String[] desc = description.split(" ");
				String cid = preclass_CLEAN_REFERENCES.get(aid).get("cid");
				String cname = preclass_CLEAN_REFERENCES.get(aid).get("cname");
				String rule;
				try{
					rule = "MAIN="+desc[0];
				}catch(Exception G) {
					
					
					continue;
				}
				preclass_learn_rule(rule,cid,cname);
				try{
					for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
						if(wn.length()<this.binaryClassificationParameters.getPreclassif_minimumTermLength()) {
							continue;
						}
						
						rule = "MAIN="+desc[0]+"|COMP="+wn;
						preclass_learn_rule(rule,cid,cname);
					}
				}catch(Exception V) {
					
				}
				
				
			}
		tm.stopRow(22);
		tm.startRow(23);
		
		HashMap<String,HashMap<String,String>> TMP = new HashMap<String,HashMap<String,String>>();
		
		for(String rule:preclass_CLASSIFICATION_RULES.keySet()) {
			HashMap<String,String> tmp = new HashMap<String,String>();
			int max = 0;
			int total = 0;
			String MF = null;
			String MFNAME = null;
			for(String cid:preclass_CLASSIFICATION_RULES.get(rule).keySet()) {
				
				total = total + Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(0));
				
				if(Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(0))>max) {
					max = Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(0));
					MF = cid;
					MFNAME = preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(1);
				}
				if(Math.random()<0.0001) {
					
					
				}
			}
			if(rule.contains("|COMP=")) {
				tmp.put("Type",String.valueOf(this.binaryClassificationParameters.getPreclassif_typeFactor()));
			}else {
				tmp.put("Type","0");
			}
			tmp.put("Total", String.valueOf(total));
			tmp.put("MF",MF);
			tmp.put("Accuracy", String.valueOf((1.0*max)/total));
			tmp.put("MFNAME",MFNAME);
			if(total>=this.binaryClassificationParameters.getPreclassif_Tb()
					&& ((1.0*max)/total)*100.0>=this.binaryClassificationParameters.getPreclassif_Ta()) {
				TMP.put(rule, tmp);
			}else {
				//The rule failed the required minimum
			}
			update_preclassif_progress();
			preclass_rule_set_progress.setProgress(preclass_rule_set_progress.getProgress()+(1.0)/preclass_CLASSIFICATION_RULES.size());
			preclass_rule_gen_progress.setProgress(preclass_rule_gen_progress.getProgress()+( 1.0/ (0.2 * preclass_ref_desc_cardinality * preclass_CLASSIFICATION_RULES.size() )));
			}
		preclass_CLASSIFICATION_RULES.clear();
		preclass_CLASSIFICATION_RULES = null;
		preclass_CLASSIFICATION_RULES_AGGREGATED = TMP;
		TMP = null;
		
		tm.stopRow(23);
		tm.stopRow(21);
		
		
	}

	private void generate_rules() throws ClassNotFoundException, SQLException {
		
		
		tm.startSummaryRow(7);
		tm.startRow(8);
		
		
		try {
			TimeUnit.MILLISECONDS.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.buildRows = Tools.RandomSample(ref_desc_cardinality,this.binaryClassificationParameters.getClassif_buildSampleSize());
		
		int base_desc_count = 0;
		
		String target_desc = this.binaryClassificationParameters.getClassif_baseDescriptionType().toString();
		for(String aid:CLEAN_REFERENCES.keySet()) {
			
				word_gen_progress.setProgress(word_gen_progress.getProgress()+(1.0/ref_desc_cardinality));
				rule_gen_progress.setProgress(rule_gen_progress.getProgress()+(0.8/ref_desc_cardinality));
				update_classif_progress();
				try {
					if( CLEAN_REFERENCES.get(aid).get("cid").length() <4 || !buildRows.contains(base_desc_count)) {
						base_desc_count=base_desc_count+1;
						continue;
					}
					
				}catch(Exception V) {
					//No cid
					base_desc_count=base_desc_count+1;
					update_classif_progress();
					rule_set_progress.setProgress(rule_set_progress.getProgress()+(1.0)/CLASSIFICATION_RULES.size());
					rule_gen_progress.setProgress(rule_gen_progress.getProgress()+( 1.0 / (0.2 * ref_desc_cardinality * CLASSIFICATION_RULES.size() )));
					continue;
				}
				
				base_desc_count=base_desc_count+1;
				String description = CLEAN_REFERENCES.get(aid).get(target_desc);
				String[] desc = description.split(" ");
				String cid = CLEAN_REFERENCES.get(aid).get("cid");
				String cname = CLEAN_REFERENCES.get(aid).get("cname");
				String rule;
				try{
					rule = "MAIN="+desc[0];
				}catch(Exception G) {
					
					
					continue;
				}
				learn_rule(rule,cid,cname);
				try{
					for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
						if(wn.length()<this.binaryClassificationParameters.getClassif_minimumTermLength()) {
							continue;
						}
						
						rule = "MAIN="+desc[0]+"|COMP="+wn;
						learn_rule(rule,cid,cname);
					}
				}catch(Exception V) {
					
				}
					
				
			}
		tm.stopRow(8);
		tm.startRow(9);
		
		HashMap<String,HashMap<String,String>> TMP = new HashMap<String,HashMap<String,String>>();
		
		for(String rule:CLASSIFICATION_RULES.keySet()) {
			HashMap<String,String> tmp = new HashMap<String,String>();
			int max = 0;
			int total = 0;
			String MF = null;
			String MFNAME = null;
			for(String cid:CLASSIFICATION_RULES.get(rule).keySet()) {
				
				total = total + Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid).get(0));
				
				if(Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid).get(0))>max) {
					max = Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid).get(0));
					MF = cid;
					MFNAME = CLASSIFICATION_RULES.get(rule).get(cid).get(1);
				}
				
			}
			if(rule.contains("|COMP=")) {
				tmp.put("Type",String.valueOf(this.binaryClassificationParameters.getClassif_typeFactor()));
			}else {
				tmp.put("Type","0");
			}
			tmp.put("Total", String.valueOf(total));
			tmp.put("MF",MF);
			tmp.put("MFNAME",MFNAME);
			tmp.put("Accuracy", String.valueOf((1.0*max)/total));
			if(total>=this.binaryClassificationParameters.getClassif_Tb()
					&& ((1.0*max)/total)*100.0>=this.binaryClassificationParameters.getClassif_Ta()) {
				TMP.put(rule, tmp);
			}else {
				//The rule failed the required minimum
			}
			update_classif_progress();
			rule_set_progress.setProgress(rule_set_progress.getProgress()+(1.0)/CLASSIFICATION_RULES.size());
			rule_gen_progress.setProgress(rule_gen_progress.getProgress()+( 1.0 / (0.2 * ref_desc_cardinality * CLASSIFICATION_RULES.size() )));
		}
		CLASSIFICATION_RULES.clear();
		CLASSIFICATION_RULES = null;
		CLASSIFICATION_RULES_AGGREGATED = TMP;
		TMP = null;
		tm.stopRow(9);
		tm.stopRow(7);
		
		
	}
	
	private void preclass_learn_rule(String rule, String cid, String cname) {
		if(preclass_CLASSIFICATION_RULES.containsKey(rule)) {
			if(preclass_CLASSIFICATION_RULES.get(rule).containsKey(cid)) {
				String count = String.valueOf(Integer.parseInt(preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(0)) + 1);
				String oldcname = preclass_CLASSIFICATION_RULES.get(rule).get(cid).get(1);
				
				HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
				tmp=preclass_CLASSIFICATION_RULES.get(rule);
				ArrayList<String> tmparr = new ArrayList<String>(2);
				tmparr.add(count);
				tmparr.add(oldcname);
				tmp.put(cid, tmparr);
				preclass_CLASSIFICATION_RULES.put(rule, tmp);
			}else {
				String count = "1";
				HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
				tmp=preclass_CLASSIFICATION_RULES.get(rule);
				ArrayList<String> tmparr = new ArrayList<String>(2);
				tmparr.add(count);
				tmparr.add(cname);
				tmp.put(cid, tmparr);
				preclass_CLASSIFICATION_RULES.put(rule, tmp);
			}
		}else {
			String count = "1";
			HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
			ArrayList<String> tmparr = new ArrayList<String>(2);
			tmparr.add(count);
			tmparr.add(cname);
			tmp.put(cid, tmparr);
			preclass_CLASSIFICATION_RULES.put(rule, tmp);
		}
	}

	private void learn_rule(String rule, String cid, String cname) {
		if(CLASSIFICATION_RULES.containsKey(rule)) {
			if(CLASSIFICATION_RULES.get(rule).containsKey(cid)) {
				String count = String.valueOf(Integer.parseInt(CLASSIFICATION_RULES.get(rule).get(cid).get(0)) + 1);
				String oldcname = CLASSIFICATION_RULES.get(rule).get(cid).get(1);
				
				HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
				tmp=CLASSIFICATION_RULES.get(rule);
				ArrayList<String> tmparr = new ArrayList<String>(2);
				tmparr.add(count);
				tmparr.add(oldcname);
				tmp.put(cid, tmparr);
				CLASSIFICATION_RULES.put(rule, tmp);
			}else {
				String count = "1";
				HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
				tmp=CLASSIFICATION_RULES.get(rule);
				ArrayList<String> tmparr = new ArrayList<String>(2);
				tmparr.add(count);
				tmparr.add(cname);
				tmp.put(cid, tmparr);
				CLASSIFICATION_RULES.put(rule, tmp);
			}
		}else {
			String count = "1";
			HashMap<String,ArrayList<String>> tmp = new HashMap<String,ArrayList<String>>();
			ArrayList<String> tmparr = new ArrayList<String>(2);
			tmparr.add(count);
			tmparr.add(cname);
			tmp.put(cid, tmparr);
			CLASSIFICATION_RULES.put(rule, tmp);
		}
	}
	
	protected void preclass_clean_references() throws IOException, ClassNotFoundException, SQLException {
		tm.startSummaryRow(17);
    	
		if(this.binaryClassificationParameters.getPreclassif_baseDescriptionType().toString().startsWith("raw")) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		
		for(String pid:preclass_REFERENCE_PROJECTS) {
			
			HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
			
			ArrayList<DescriptionFetchRow> rs = DESCS.get(pid);
			for(DescriptionFetchRow row:rs) {
				
				if(preclass_ref_char_clean_progress.getProgress()>0.99){preclass_ref_char_clean_progress_complete = true;}
				if(preclass_ref_abv_clean_progress.getProgress()>0.99){preclass_ref_abv_clean_progress_complete = true;}
				if(preclass_ref_spell_clean_progress.getProgress()>0.99){preclass_ref_spell_clean_progress_complete = true;}
				
				
				
				
				//
				double char_weight = GlobalConstants.UNI_WEIGHT;
				double spell_weight = GlobalConstants.SPELL_WEIGHT;
				double abv_weight = GlobalConstants.ABV_WEIGHT;
				
					
				double increment_step=
						(this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0) +
						(this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0) + 
						(this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0); 
				increment_step=1.0/increment_step;
				
				preclass_reference_desc_progress.setProgress(preclass_reference_desc_progress.getProgress()+(1.0/preclass_ref_desc_cardinality));
				update_preclassif_progress();
				
				if(this.binaryClassificationParameters.getPreclassif_cleanChar()) {
					//
					tm.startRow(18, preclass_ref_desc_cardinality*(char_weight*increment_step));
					tm.increment(18);
					preclass_ref_char_clean_progress.setProgress(preclass_ref_char_clean_progress.getProgress()+(1.0/(char_weight * increment_step*preclass_ref_desc_cardinality)));
				}else {
									fast_forward(preclass_ref_char_clean_progress); preclass_ref_char_clean_progress_complete=true;
				}
				if(this.binaryClassificationParameters.getPreclassif_cleanAbv()) {
					if(preclass_ref_char_clean_progress_complete) {
						tm.startRow(19, preclass_ref_desc_cardinality*(abv_weight*increment_step));
						tm.increment(19);
						preclass_ref_abv_clean_progress.setProgress(preclass_ref_abv_clean_progress.getProgress()+(1.0/(abv_weight * increment_step*preclass_ref_desc_cardinality)));
					}
				}else {
					if(preclass_ref_char_clean_progress_complete) {
											fast_forward(preclass_ref_abv_clean_progress); preclass_ref_abv_clean_progress_complete=true;
					}
					
				}
				if(this.binaryClassificationParameters.getPreclassif_cleanSpell()) {
					if(preclass_ref_char_clean_progress_complete && preclass_ref_abv_clean_progress_complete) {
						tm.startRow(20, preclass_ref_desc_cardinality*(spell_weight*increment_step));
						tm.increment(20);
						preclass_ref_spell_clean_progress.setProgress(preclass_ref_spell_clean_progress.getProgress()+(1.0/(spell_weight * increment_step*preclass_ref_desc_cardinality)));
					}
					
				}else {
					if(preclass_ref_char_clean_progress_complete && preclass_ref_abv_clean_progress_complete) {
											fast_forward(preclass_ref_spell_clean_progress); preclass_ref_spell_clean_progress_complete=true;
					}
					
				}
				
				if(preclass_ref_char_clean_progress_complete) {
					tm.stopRow(18);
				}if(preclass_ref_abv_clean_progress_complete) {
					tm.stopRow(19);
				}if(preclass_ref_spell_clean_progress_complete) {
					tm.stopRow(20);
				}
				
				//Correction
				ArrayList<String> input = new ArrayList<String>();
				input.add(row.getSd());
				input.add(row.getLd());
				HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,false);
				output.put("cid", row.getCid());
				output.put("cname",row.getCname());
				input =null;
				preclass_CLEAN_REFERENCES.put(row.getAid(),output);
				
				
				
			}
		}
	}

	private void clean_references() throws ClassNotFoundException, SQLException, IOException {
		
		tm.startSummaryRow(3);
    	
		if(this.binaryClassificationParameters.getClassif_baseDescriptionType().toString().startsWith("raw")) {
			return;
		}
		
		
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
		
        
		for(String pid:REFERENCE_PROJECTS) {
			
			HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
			
			ArrayList<DescriptionFetchRow> rs = DESCS.get(pid);
			for(DescriptionFetchRow row:rs) {
				

				if(ref_char_clean_progress.getProgress()>0.99){ref_char_clean_progress_complete = true;}
				if(ref_abv_clean_progress.getProgress()>0.99){ref_abv_clean_progress_complete = true;}
				if(ref_spell_clean_progress.getProgress()>0.99){ref_spell_clean_progress_complete = true;}
				
				
				//
				double char_weight = GlobalConstants.UNI_WEIGHT;
				double spell_weight = GlobalConstants.SPELL_WEIGHT;
				double abv_weight = GlobalConstants.ABV_WEIGHT;
					
				double increment_step=
						(this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0) +
						(this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0) + 
						(this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0); 
				increment_step=1.0/increment_step;
				
				reference_desc_progress.setProgress(reference_desc_progress.getProgress()+(1.0/ref_desc_cardinality));
				update_classif_progress();
				
				if(this.binaryClassificationParameters.getClassif_cleanChar()) {
					tm.startRow(4, ref_desc_cardinality*(char_weight*increment_step));
					tm.increment(4);
					
					ref_char_clean_progress.setProgress(ref_char_clean_progress.getProgress()+(1.0/(char_weight*increment_step*ref_desc_cardinality)));
				}else {
									fast_forward(ref_char_clean_progress); ref_char_clean_progress_complete=true;
				}
				if(this.binaryClassificationParameters.getClassif_cleanAbv()) {
					if(ref_char_clean_progress_complete) {
						tm.startRow(5, ref_desc_cardinality*(abv_weight*increment_step));
						tm.increment(5);
						
						ref_abv_clean_progress.setProgress(ref_abv_clean_progress.getProgress()+(1.0/(abv_weight * increment_step*ref_desc_cardinality)));
					}
				}else {
					if(ref_char_clean_progress_complete) {
											fast_forward(ref_abv_clean_progress); ref_abv_clean_progress_complete=true;
					}
					
				}
				if(this.binaryClassificationParameters.getClassif_cleanSpell()) {
					if(ref_char_clean_progress_complete && ref_abv_clean_progress_complete) {
						tm.startRow(6, ref_desc_cardinality*(spell_weight*increment_step));
						tm.increment(6);
						
						ref_spell_clean_progress.setProgress(ref_spell_clean_progress.getProgress()+(1.0/(spell_weight * increment_step*ref_desc_cardinality)));
					}
					
				}else {
					if(ref_char_clean_progress_complete && ref_abv_clean_progress_complete) {
											fast_forward(ref_spell_clean_progress); ref_spell_clean_progress_complete=true;
					}
					
				}
				
				if(ref_char_clean_progress_complete) {
					tm.stopRow(4);
				}if(ref_abv_clean_progress_complete) {
					tm.stopRow(5);
				}if(ref_spell_clean_progress_complete) {
					tm.stopRow(6);
				}
				
				
				//Correction
				ArrayList<String> input = new ArrayList<String>();
				input.add(row.getSd());
				input.add(row.getLd());
				HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,true);
				output.put("cid", row.getCid());
				output.put("cname",row.getCname());
				input =null;
				CLEAN_REFERENCES.put(row.getAid(),output);
				
				
				
			}
		}
		tm.stopRow(3);
	}

	public void setRefenceProjects(HashSet<String> rEFERENCE_PROJECTS, HashSet<String> rEFERENCE_PROJECTS2, boolean isLaunched, Auto_classification_launch parent) throws ClassNotFoundException, SQLException {
		this.REFERENCE_PROJECTS = rEFERENCE_PROJECTS;
		this.preclass_REFERENCE_PROJECTS = rEFERENCE_PROJECTS2;
		this.isLaunched = isLaunched;
		this.parent = parent;
		
		download_button.getScene().getWindow().setOnCloseRequest(event -> {
		    ;
		    if(block_window_close) {
		    	ConfirmationDialog.show("Results available", "If you choose to leave this screen, the current results will be lost.\nDo you wish to proceed anyway?", "Save", "Quit without saving", "Cancel", this);
		    	event.consume();
		    }else {
		    	proceed_without_saving();
		    }
		    
		});
		
		PROGRESS_FACTORS = new HashMap<String,Double>();
		PROGRESS_FACTORS.put("download", 0.4);
		PROGRESS_FACTORS.put("clean", 0.5);
		PROGRESS_FACTORS.put("rule", 0.1);
		
		
		
		launch_classification();
		
	}

	public void setTarget_desc_cardinality(Integer Target_desc_cardinality) {
		this.target_desc_cardinality = Target_desc_cardinality;
	}

	public void setConfig(BinaryClassificationParameters binaryClassificationParameters) {
		
		this.binaryClassificationParameters = binaryClassificationParameters;
	}
	

	private void preclass_apply_rules() throws ClassNotFoundException, SQLException {
		
		try {
			TimeUnit.MILLISECONDS.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		tm.startRow(28);
		
		
		String target_desc = this.binaryClassificationParameters.getPreclassif_targetDescriptionType().toString();

		for(String aid:preclass_CLEAN_TARGETS.keySet()) {
			String description = preclass_CLEAN_TARGETS.get(aid).get(target_desc);
			String[] desc = description.split(" ");
			String rule;
			try{
				rule = "MAIN="+desc[0];
			}catch(Exception G) {
				preclass_rule_app_progress.setProgress(preclass_rule_app_progress.getProgress()+(1.0/target_desc_cardinality));
				update_preclassif_progress();
				
				
				continue;
			}
			
			preclass_apply_rule(aid,rule);
			try{
				for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
					rule = "MAIN="+desc[0]+"|COMP="+wn;
					preclass_apply_rule(aid,rule);
				}
			}catch(Exception V) {
				
			}
			
			preclass_rule_app_progress.setProgress(preclass_rule_app_progress.getProgress()+(1.0/target_desc_cardinality));
			update_preclassif_progress();
			
		}
		
		
		update_actual_graph();
	}
	
	
	private void update_preclassif_progress() {
		
		double progress = preclass_download_progress.getProgress()*PROGRESS_FACTORS.get("download");
		progress+= preclass_reference_desc_progress.getProgress()*PROGRESS_FACTORS.get("clean")*0.5;
		progress+= preclass_rule_gen_progress.getProgress()*PROGRESS_FACTORS.get("rule")*0.5;
		progress+= preclass_target_desc_progress.getProgress()*PROGRESS_FACTORS.get("clean")*0.5;
		progress+= preclass_rule_app_progress.getProgress()*PROGRESS_FACTORS.get("rule")*0.5;
		
		preclass_auto_classif_progress.setProgress(progress);
		
	}

	public void apply_rules() throws ClassNotFoundException, SQLException {
		
		try {
			TimeUnit.MILLISECONDS.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tm.startRow(14);
		String target_desc = this.binaryClassificationParameters.getClassif_targetDescriptionType().toString();
		
		
		
		for(String aid:CLEAN_TARGETS.keySet()) {
			String description = CLEAN_TARGETS.get(aid).get(target_desc);
			String[] desc = description.split(" ");
			String rule;
			try{
				rule = "MAIN="+desc[0];
			}catch(Exception G) {
				rule_app_progress.setProgress(rule_app_progress.getProgress()+(1.0/target_desc_cardinality));
				update_classif_progress();
				G.printStackTrace(System.err);
				continue;
			}
			
			apply_rule(aid,rule);
			try{
				for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
					rule = "MAIN="+desc[0]+"|COMP="+wn;
					apply_rule(aid,rule);
				}
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
			
			rule_app_progress.setProgress(rule_app_progress.getProgress()+(1.0/target_desc_cardinality));
			update_classif_progress();
			
		}
		
		tm.stopRow(14);
		tm.stopRow(1);
		
		update_actual_graph();
		
		
	}
	
	private void update_classif_progress() {
		
		double progress = download_progress.getProgress()*PROGRESS_FACTORS.get("download");
		progress+= reference_desc_progress.getProgress()*PROGRESS_FACTORS.get("clean")*0.5;
		progress+= rule_gen_progress.getProgress()*PROGRESS_FACTORS.get("rule")*0.5;
		progress+= target_desc_progress.getProgress()*PROGRESS_FACTORS.get("clean")*0.5;
		progress+= rule_app_progress.getProgress()*PROGRESS_FACTORS.get("rule")*0.5;
		
		auto_classif_progress.setProgress(progress);
		
	}

	private void preclass_apply_rule(String aid, String rule) {
		if(!preclass_CLASSIFICATION_RULES_AGGREGATED.containsKey(rule)) {
			
			return;
		}
		Double score = Tools.score_rule(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule),this.binaryClassificationParameters,false);
		if(score==null) {
			return;
		}
		if(preclass_ITEMS_DICO.containsKey(aid)) {
			if(score>Double.parseDouble(preclass_ITEMS_DICO.get(aid).get(0))) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(score.toString());
				tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MF"));
				tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MFNAME"));
				tmp.add(rule);
				tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Accuracy"));
				tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Total"));
				
				preclass_ITEMS_DICO.put(aid, tmp);
			}
		}else {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(score.toString());
			tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MF"));
			tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MFNAME"));
			tmp.add(rule);
			tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Accuracy"));
			tmp.add(preclass_CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Total"));
			
			preclass_ITEMS_DICO.put(aid, tmp);
			
			
		}
		
	}

	private void apply_rule(String aid, String rule) {
		if(!CLASSIFICATION_RULES_AGGREGATED.containsKey(rule)) {
			return;
		}
		Double score = Tools.score_rule(CLASSIFICATION_RULES_AGGREGATED.get(rule),this.binaryClassificationParameters,true);
		if(score==null) {
			return;
		}
		if(ITEMS_DICO.containsKey(aid)) {
			if(score>Double.parseDouble(ITEMS_DICO.get(aid).get(0))) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(score.toString());
				tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MF"));
				tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MFNAME"));
				tmp.add(rule);
				tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Accuracy"));
				tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Total"));
				
				ITEMS_DICO.put(aid, tmp);
			}
		}else {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(score.toString());
			tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MF"));
			tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("MFNAME"));
			tmp.add(rule);
			tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Accuracy"));
			tmp.add(CLASSIFICATION_RULES_AGGREGATED.get(rule).get("Total"));
			ITEMS_DICO.put(aid, tmp);
		}
	}

	public void setTemplate(TimeMasterTemplate timeTemplate) {
		this.timeTemplate = timeTemplate;
		
	}

	public void setUserAccount(UserAccount account) {
		decorate_menubar(menubar, account);
		this.account = account;
	}

	public void proceed_with_saving() {
		try {
			save_to_file();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void proceed_without_saving() {
		;
		 Stage stage = (Stage) download_button.getScene().getWindow();
		 stage.close();
		 Platform.exit();
		 ;
	}

	public void proceed_cancel() {
		return;
	}
	
}
