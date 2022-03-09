package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.GlobalConstants;
import transversal.dialog_toolbox.ExceptionDialog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Locale;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = FXMLLoader.load(getClass().getResource("/scenes/Login_page.fxml"));
			Scene scene = new Scene(root,400,400);
			//scene.getStylesheets().add(getClass().getResource("/Styles/Login_page.css").toExternalForm());
			primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION);
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
