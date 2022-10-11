package application;

import controllers.ToolHeaderController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.GlobalConstants;
import model.UserAccount;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		setUserAgentStylesheet("/styles/skin.css");
		if(GlobalConstants.DEV_RUNTIME){
			try {
				UserAccount account = Tools.checkpass("Ayman", "neonec");
				String activated_pid = account.getActive_project();
				if(activated_pid!=null) {

					try {
						Stage newStage = new Stage();
						newStage.initStyle(StageStyle.TRANSPARENT);
						FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Char_description.fxml"));
						Pane root = fxmlLoader.load();

						Scene scene = new Scene(root,400,400);

						ToolHeaderController.titleProperty.setValue("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
						newStage.setScene(scene);
						//primaryStage.setMinHeight(768);
						//primaryStage.setMinWidth(1024);
						newStage.setMinHeight(768);newStage.setMinWidth(1024);newStage.setMaximized(true);newStage.setResizable(false);newStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
						//primaryStage.setFullScreen(true);
						newStage.show();

						controllers.Char_description controller = fxmlLoader.getController();
						controller.setUserAccount(account);

					} catch(Exception e) {
						ExceptionDialog.show("FX001 Char_description", "FX001 Char_description", "FX001 Char_description");
						e.printStackTrace(System.err);
					}
				}else {
					//Else raise an alert message dialog
					ExceptionDialog.no_selected_project();
				}
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			Pane root = FXMLLoader.load(getClass().getResource("/scenes/Login_page.fxml"));
			Scene scene = new Scene(root,400,400);
			//scene.getStylesheets().add(getClass().getResource("/styles/Login_page.css").toExternalForm());
			ToolHeaderController.titleProperty.setValue("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION);
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setScene(scene);
			//primaryStage.setMinHeight(768);
			//primaryStage.setMinWidth(1024);
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			primaryStage.show();
			
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 : Login", "FX001 : Login", "FX001 : Login");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Locale.setDefault(Locale.ENGLISH);
		if(GlobalConstants.REDIRECT_OUTSTREAM){
			if(!GlobalConstants.REDIRECT_ERR_ONLY){
				System.setOut(new PrintStream(new FileOutputStream(GlobalConstants.OUT_LOG, true)));
			}
			System.setErr(new PrintStream(new FileOutputStream(GlobalConstants.OUT_LOG, true)));
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX >" + LocalDateTime.now() + "< XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX >" + LocalDateTime.now() + "< XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		}
		launch(args);
	}
}
