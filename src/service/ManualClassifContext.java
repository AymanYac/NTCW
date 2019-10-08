package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import controllers.Manual_classif;
import controllers.paneControllers.PropositionContext_ManualClassif;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.ManualClassProposition;

public class ManualClassifContext {
	public static Stage lastStage;
	public static ArrayList<String> methods = new ArrayList<String>(5);
	public Manual_classif parent;
	public HashMap<String, String> CID2NAME;
	
	
	public ManualClassifContext() {
		methods.add(null);
		methods.add(null);
		methods.add(null);
		methods.add(null);
		methods.add(null);
	}

	public void showContext(Button prop, int idx) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/PropositionContext.fxml"));
		GridPane contextGrid = loader.load();
		PropositionContext_ManualClassif controller = loader.getController();
		controller.setParent(this);
		controller.setIdx(idx);
		controller.selectPreviousItem();
		controller.setClasses(CID2NAME);
		
		closeLast();
		Stage Stage = new Stage();
		lastStage = Stage;
		Stage.initStyle(StageStyle.UNDECORATED);
		Scene scene = new Scene(contextGrid, prop.getWidth(), 5* prop.getHeight());
		Stage.setScene(scene);
		Stage.setX(prop.localToScene(prop.getBoundsInLocal()).getMinX());
		Stage.setY(prop.localToScene(prop.getBoundsInLocal()).getMaxY());
		Stage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        {
		        	
		        	Stage.close();
		        }
		    }

		});
		controller.setStage(Stage);
		Stage.show();
	}
	
	public void closeLast() {
		try {
			lastStage.close();
		}catch(Exception V) {
			
		}
	}

	public void disableButton(Button button) {
		button.setOnAction((event) -> {
			  
		});
		button.setText("--");
		button.setOpacity(0.5);
	}

	public void assignRecommendation(Button button, ManualClassProposition prop) {
		button.setText(prop.getSegment_name());
		button.setOpacity(1.0);
		button.setOnAction((event) -> {
			  parent.fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
		});
	}

	public void setParent(Manual_classif manual_classif) {
		this.parent=manual_classif;
	}
	
	
}
