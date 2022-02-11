package transversal.dialog_toolbox;

import controllers.Auto_classification_progress;
import controllers.Project_parameters;
import controllers.Project_selection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import model.GenericClassRule;
import model.ItemFetcherRow;
import model.UserAccount;
import transversal.generic.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class ConfirmationDialog {
	
	public static void show(String title,String header,String yes,String no, Project_parameters parent, boolean confirmed) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
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
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
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
		
		
		Alert alert = new Alert(AlertType.WARNING);
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
			alert =new Alert(AlertType.ERROR);
		}else {
			alert =new Alert(AlertType.INFORMATION);
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
		
		Alert alert = new Alert(AlertType.WARNING);
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

		Alert alert = new Alert(AlertType.WARNING);
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
			alert =new Alert(AlertType.ERROR);
		}else {
			alert =new Alert(AlertType.INFORMATION);
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
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
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
		Alert alert = new Alert(AlertType.CONFIRMATION);
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

	public static boolean showCaracDeleteImpactConfirmation(int impactCount) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Confirm characteristic deletion impact");
		alert.setHeaderText(String.valueOf(impactCount)+" value(s) attached to this characteristic will be lost!");
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
		Alert alert = new Alert(AlertType.WARNING);
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

