package scenes.paneScenes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
//import org.scenicview.ScenicView;

import java.net.URL;

public class DescSettingTest extends Application {
    private static final String RESOURCE = "DescSettingPane.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        URL resource = DescSettingTest.class.getResource(RESOURCE);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();
        DescPaneController controller= fxmlLoader.getController();
        Scene scene = new Scene(root);

//        ScenicView.show(scene);
//       new JMetro(JMetro.Style.LIGHT).applyTheme(scene);

        primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}