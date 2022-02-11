package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.GlobalConstants;
import model.UserAccount;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Front_page {

	
	@FXML
	private Label vnum;
	
	@FXML
	private Button setupButton;
	
	public Scene scene;
	private UserAccount account;

	
	@FXML
	void setup(){
		Stage stage = (Stage) setupButton.getScene().getWindow();
		load_project_selection();
		stage.close();
		
	}
	
	
	
	@FXML
	void rule_def(){
		//if there's currently an actif project, proceed to the classification
		String activated_pid = account.getActive_project();
		Stage stage = (Stage) setupButton.getScene().getWindow();

			if(activated_pid!=null) {
				
			    try {
			    	
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Rule_definition.fxml"));
					AnchorPane root = fxmlLoader.load();

					Scene scene = new Scene(root,400,400);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();

					controllers.Rule_definition controller = fxmlLoader.getController();
					controller.setUserAccount(account);
					
					
					stage.close();
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 Rule_definition", "FX001 Rule_definition", "FX001 Rule_definition");
					e.printStackTrace(System.err);
				}
			}else {
				//Else raise an alert message dialog
				ExceptionDialog.no_selected_project();
			}
		
	}
	
	@FXML
	void item_def(){
		
		//if there's currently an actif project, proceed to the classification
		String activated_pid = account.getActive_project();
		Stage stage = (Stage) setupButton.getScene().getWindow();

			if(activated_pid!=null) {
				
			    try {
			    	
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Manual_classif.fxml"));
					AnchorPane root = fxmlLoader.load();

					Scene scene = new Scene(root,400,400);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();

					controllers.Manual_classif controller = fxmlLoader.getController();
					controller.setUserAccount(account);
					
					
					stage.close();
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 Rule_definition", "FX001 Rule_definition", "FX001 Rule_definition");
					e.printStackTrace(System.err);
				}
			}else {
				//Else raise an alert message dialog
				ExceptionDialog.no_selected_project();
			}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@FXML
	void autoclassif(){
		//if there's currently an actif project, proceed to the classification
		String activated_pid = account.getActive_project();
		Stage stage = (Stage) setupButton.getScene().getWindow();

				if(activated_pid!=null) {
					
				    try {
				    	Connection conn = Tools.spawn_connection_from_pool();
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
					    
					    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Auto_classification_launch.fxml"));
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
						
						stage.close();
						
					} catch(Exception e) {
						ExceptionDialog.show("FX001 auto_classification_launch", "FX001 auto_classification_launch", "FX001 auto_classification_launch");
						e.printStackTrace(System.err);
					}
				}else {
					//Else raise an alert message dialog
					ExceptionDialog.no_selected_project();
				}
	}

	
	@FXML
	void char_def(){
		
		//if there's currently an actif project, proceed to the classification
		String activated_pid = account.getActive_project();
		Stage stage = (Stage) setupButton.getScene().getWindow();

			if(activated_pid!=null) {
				
			    try {
			    	
				    Stage primaryStage = new Stage();
				    primaryStage.initStyle(StageStyle.TRANSPARENT);
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Char_description.fxml"));
					AnchorPane root = fxmlLoader.load();

					Scene scene = new Scene(root,400,400);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					//primaryStage.setFullScreen(true);
					primaryStage.show();

					controllers.Char_description controller = fxmlLoader.getController();
					controller.setUserAccount(account);
					
					
					stage.close();
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 Char_description", "FX001 Char_description", "FX001 Char_description");
					e.printStackTrace(System.err);
				}
			}else {
				//Else raise an alert message dialog
				ExceptionDialog.no_selected_project();
			}
		
	}
	
	
	
	
	
	
	
	
	
	

	private void load_project_selection() {
		try {
			;
		    Stage primaryStage = new Stage();
		    
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Project_selection.fxml"));
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
			
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 project_selection", "FX001 project_selection", "FX001 project_selection");
			e.printStackTrace();
		}
	}

	public void setUserAccount(UserAccount role) {
		this.account = role;
	}
	
	@FXML void initialize(){
		vnum.setText(GlobalConstants.TOOL_VERSION);
	}

	
	
	
}
