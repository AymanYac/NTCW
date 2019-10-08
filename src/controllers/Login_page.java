package controllers;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.GlobalConstants;
import model.UserAccount;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;


public class Login_page {
	
	@FXML
	private Label vnum;
	
	@FXML
	private TextField loginText;
	
	@FXML
	private PasswordField passwdText;
	
	
	//Checks the login and password
	//Positive Output : load_front_page()
	//Nevative Output : ExceptionDialog.wrong_credentials()
	@FXML
	public void connect(){
		
		//Intialize the initial profile value to null
		UserAccount role=null;
		
		//Update the return value with the return value of checkpass()
		try {
			role = Tools.checkpass(loginText.getText(),passwdText.getText());
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace(System.err);
			ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
			return;
		}
		
		
		//According to the profile value, decide output
		if(role!=null) {
			//Positive Output
			Stage stage = (Stage) passwdText.getScene().getWindow();
		    load_front_page(loginText.getText(),role,stage);
		    
		}else {
			//Nevative Output
			ExceptionDialog.show("WG000 wrong_credentials", "WG000 wrong_credentials", "WG000 wrong_credentials");
			
		}

	}
	
	/*
	 * "-Loads next screen's controller
		-Sets up next screen size
		-Sets up next screen's login and role
		-Closes current screen"

	 */
	
	 private void load_front_page(String user,UserAccount role, Stage parent) {
		 
		 
		 try {
			  
			 	//Loads next screen's controller, loading cursor
				Stage primaryStage = new Stage();
			    
			    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Front_page.fxml"));
				AnchorPane root = fxmlLoader.load();

			    controllers.Front_page controller = fxmlLoader.getController();
				
				Scene scene = new Scene(root,400,400);
				controller.scene = scene;
				scene.setCursor(Cursor.WAIT);
				
				primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION);
				primaryStage.setScene(scene);
				
				//Sets up next screen size
				primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
				primaryStage.show();
				
				//Sets up next screen's login and role, default cursor
				controller.setUserAccount(role);
			    //close the current window
			    parent.close();
			    scene.setCursor(Cursor.DEFAULT);
				
			    
			} catch(Exception e) {
				ExceptionDialog.show("FX001 front_page", "FX001 front_page", "FX001 front_page");
				e.printStackTrace();
			}

		 
		 
	}

	 //Launches the connect() routine when "ENTER" is pressed

	@FXML
     void enter_connect(KeyEvent event) {
		 if(event.getCode().equals(KeyCode.ENTER)) {
			 connect();
		 }
	 }
	
	
	@FXML void initialize(){
		vnum.setText(GlobalConstants.TOOL_VERSION);
	}

}
