package transversal.dialog_toolbox;

import controllers.Auto_classification_progress;
import controllers.Project_parameters;
import controllers.Project_selection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import model.GenericClassRule;
import model.ItemFetcherRow;
import model.UserAccount;
import transversal.generic.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class ConfirmationDialog {
	
	public static void show(String title,String header,String yes,String no, Project_parameters parent, boolean confirmed) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
 
		
        alert.getButtonTypes().setAll(yesButton, noButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {
	         //this.label.setText("No selection!");
	      } else if (option.get() == yesButton) {
	    	  ;
	    	 if(!confirmed) {
	    		 parent.confirm_supress();
	    	 }else {
	    		 parent.supress();
	    	 }
	         //this.label.setText("File deleted!");
	      } else if (option.get() == noButton) {
	    	  ;
	         //this.label.setText("Cancelled!");
	      } else {
	    	  ;
	         //this.label.setText("-");
	      }
	   }

	public static void show(String title, String header, String yes, String no, Project_selection parent,
			boolean confirmed) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
 
		
        alert.getButtonTypes().setAll(yesButton, noButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {
	         //this.label.setText("No selection!");
	      } else if (option.get() == yesButton) {
	    	  ;
	    	 if(!confirmed) {
	    		 parent.confirm_supress();
	    	 }else {
	    		 parent.supress();
	    	 }
	         //this.label.setText("File deleted!");
	      } else if (option.get() == noButton) {
	    	  ;
	         //this.label.setText("Cancelled!");
	      } else {
	    	  ;
	         //this.label.setText("-");
	      }
	}

	public static void show(String title, String header, String yes, String no, String cancel,Auto_classification_progress parent) {
		
		
		Alert alert = new Alert(AlertType.WARNING);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
        ButtonType cancelButton = new ButtonType(cancel);
		
        alert.getButtonTypes().setAll(yesButton, noButton,cancelButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {

	      } else if (option.get() == yesButton) {
	    	  ;
	         parent.proceed_with_saving();
	      } else if (option.get() == noButton) {
	    	  ;
	         parent.proceed_without_saving();
	    	 
	      } else {
	    	  ;
	    	  parent.proceed_cancel();
	      }

		
	}
	
	public static void show(String title, String header, String yes, Auto_classification_progress parent) {
		Alert alert = null;
		if(title.contains("failed")) {
			alert =new Alert(AlertType.ERROR);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		}else {
			alert =new Alert(AlertType.INFORMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		}
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
		
        alert.getButtonTypes().setAll(yesButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {
			 
	      } else {
	    	  try{
	    	  	parent.proceed_cancel();
			  }catch (Exception V){

			  }
	      }
	}

	public static Boolean show(String title, String header, String yes, String no, String cancel,
			EventHandler<MouseEvent> eventHandler,Auto_classification_progress parent) {
		
		Alert alert = new Alert(AlertType.WARNING);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
        ButtonType cancelButton = new ButtonType(cancel);
		
        alert.getButtonTypes().setAll(yesButton, noButton,cancelButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {

	      } else if (option.get() == yesButton) {
	    	  ;
	         return true;
	      } else if (option.get() == noButton) {
	    	  ;
	         return false;
	      } else {
	    	  ;
	    	  return null;
	      }
		return null;
	}

	public static Boolean show(String title, String header, String yes, String no, String cancel,
			EventHandler<ActionEvent> eventHandler, Auto_classification_progress parent, Object object) {

		Alert alert = new Alert(AlertType.WARNING);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
        ButtonType cancelButton = new ButtonType(cancel);
		
        alert.getButtonTypes().setAll(yesButton, noButton,cancelButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {

	      } else if (option.get() == yesButton) {
	    	  ;
	         return true;
	      } else if (option.get() == noButton) {
	    	  ;
	         return false;
	      } else {
	    	  ;
	    	  return null;
	      }
		return null;
	}

	public static void show(String title, String header, String ok) {
		Alert alert = null;
		if(title.contains("failed")) {
			alert =new Alert(AlertType.ERROR);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		}else {
			alert =new Alert(AlertType.INFORMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		}
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(ok);
		
        alert.getButtonTypes().setAll(yesButton);
	
		alert.showAndWait();
		
	}
	
	
	public static void showRuleImportConfirmation(String title,String header,String yes,String no, Project_parameters parent, UserAccount account, ArrayList<GenericClassRule> grs, ArrayList<ArrayList<String[]>> itemRuleMaps, ArrayList<Boolean> activeStatuses, ArrayList<String> METHODS, List<ItemFetcherRow> databaseSyncLists, String datainputmethod) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		ButtonType yesButton = new ButtonType(yes);
        ButtonType noButton = new ButtonType(no);
 
		
        alert.getButtonTypes().setAll(yesButton, noButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {
	         //this.label.setText("No selection!");
	      } else if (option.get() == yesButton) {
	    	  Tools.StoreRules(account, grs, itemRuleMaps, activeStatuses, METHODS);
			  Tools.ItemFetcherRow2ClassEvent(databaseSyncLists,account,datainputmethod);	
	         //this.label.setText("File deleted!");
	      } else if (option.get() == noButton) {
	    	  ;
	         //this.label.setText("Cancelled!");
	      } else {
	    	  ;
	         //this.label.setText("-");
	      }
	   }


    public static Boolean showCaracDeleteScopeConfirmation() {
		Alert alert = new Alert(AlertType.CONFIRMATION);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle("Confirm characteristic deletion scope");
		alert.setHeaderText("Delete this characteristic for:");
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");

		ButtonType yesButton = new ButtonType("This class only");
		ButtonType noButton = new ButtonType("All classes");
		ButtonType cancelButton = new ButtonType("Cancel");


		alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

		Optional<ButtonType> option = alert.showAndWait();
		if(option.isPresent()){
			if (option.get() == noButton) {
				return true;
			} else if (option.get() == yesButton) {
				return false;
			} else {
				return null;
			}
		}else{
			return null;
		}
    }

	public static String showCaracValueEdition(String title, String oldValue){
		Alert alert = new Alert(AlertType.NONE);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle(title);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");

		GridPane content = new GridPane();
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(45);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(10);
		ColumnConstraints c3 = new ColumnConstraints();
		c3.setPercentWidth(45);
		content.getColumnConstraints().addAll(c1,c2,c3);

		content.add(new Label("Current value:"),0,0);
		content.add(new Label("New value:"),2,0);
		TextField ancien = new TextField(oldValue);
		ancien.setDisable(true);
		ancien.setEditable(false);
		TextField nouveau = new TextField();
		content.add(ancien,0,1);
		content.add(nouveau,2,1);
		Button btn = new Button();
		btn.setId("arrowButton");
		content.add(btn,1,1);
		GridPane.setHalignment(btn, HPos.CENTER);

		alert.getDialogPane().setContent(content);

		ButtonType yesButton = new ButtonType("Apply");
		ButtonType noButton = new ButtonType("Cancel");
		alert.getButtonTypes().setAll(yesButton, noButton);
		((Button)alert.getDialogPane().lookupButton(yesButton)).disableProperty().bind(nouveau.textProperty().isEmpty());

		Optional<ButtonType> option = alert.showAndWait();
		if(option.isPresent()){
			if (option.get() == yesButton) {
				return nouveau.getText();
			} else {
				return null;
			}
		}else{
			return null;
		}
	}

	public static boolean showCaracOrValueDeleteImpactConfirmation(String warningText) {
		Alert alert = new Alert(AlertType.WARNING);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle("Confirm deletion impact");
		alert.setHeaderText(warningText);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");

		ButtonType yesButton = new ButtonType("Proceed anyway");
		ButtonType noButton = new ButtonType("Cancel");


		alert.getButtonTypes().setAll(yesButton, noButton);

		Optional<ButtonType> option = alert.showAndWait();
		if(option.isPresent()){
			if (option.get() == yesButton) {
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
	}

    public static boolean WarningClearingUnknownValues() {
		Alert alert = new Alert(AlertType.WARNING);alert.initModality(Modality.APPLICATION_MODAL);alert.initStyle(StageStyle.TRANSPARENT);
		alert.setTitle("Confirm action");
		alert.setHeaderText("All empty values of the current class' items will be marked as *UNKNOWN*");
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");

		ButtonType yesButton = new ButtonType("Proceed anyway");
		ButtonType noButton = new ButtonType("Cancel");


		alert.getButtonTypes().setAll(yesButton, noButton);

		Optional<ButtonType> option = alert.showAndWait();
		if(option.isPresent()){
			if (option.get() == yesButton) {
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
    }
}

