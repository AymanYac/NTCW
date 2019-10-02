package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import model.GlobalConstants;
import transversal.dialog_toolbox.ExceptionDialog;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("/Scenes/Login_page.fxml"));
			Scene scene = new Scene(root,400,400);
			//scene.getStylesheets().add(getClass().getResource("/Styles/Login_page.css").toExternalForm());
			primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION);
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
	
	public static void main(String[] args) {
		launch(args);
	}
}
