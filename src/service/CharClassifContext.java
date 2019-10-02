package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import controllers.Char_description;
import controllers.paneControllers.PropositionContext_CharClassif;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.CharClassProposition;

public class CharClassifContext {
	public static Stage lastStage;
	public static ArrayList<String> methods = new ArrayList<String>(5);
	public Char_description parent;
	public ArrayList<String> CID2NAME;
	
	
	public CharClassifContext() {
		methods.add(null);
		methods.add(null);
		methods.add(null);
		methods.add(null);
		methods.add(null);
	}

	public void showContext(Button prop, int idx) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/paneScenes/PropositionContext_CharClassif.fxml"));
		GridPane contextGrid = loader.load();
		PropositionContext_CharClassif controller = loader.getController();
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

	public void assignRecommendation(Button button, CharClassProposition prop) {
		button.setText(prop.getSegment_name());
		button.setOpacity(1.0);
		button.setOnAction((event) -> {
			  parent.fireClassChange(prop.getSegment_id()+"&&&"+prop.getSegment_name());
		});
	}

	public void setParent(Char_description parent_controller) {
		this.parent=parent_controller;
	}

	
	
}
