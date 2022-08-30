package scenes.paneScenes;

import com.sun.glass.ui.Window;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class test extends Application {
    private static final String RESOURCE = "DescSettingPane.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        URL resource = test.class.getResource(RESOURCE);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();
        DescPaneController controller= fxmlLoader.getController();
        controller.fillDummyItems();
        Scene scene = new Scene(root);

//        ScenicView.show(scene);
//       new JMetro(JMetro.Style.LIGHT).applyTheme(scene);

        //primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setWidth(Screen.getPrimary().getBounds().getWidth()*0.7);
        primaryStage.setX(0);
        controller.setStageWidthProperty(primaryStage.widthProperty());
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}