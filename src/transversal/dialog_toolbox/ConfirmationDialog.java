package transversal.dialog_toolbox;

import java.util.Optional;

import controllers.Auto_classification_progress;
import controllers.Manual_classif;
import controllers.Project_parameters;
import controllers.Project_selection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import service.ItemDispatcher;
public class ConfirmationDialog {
	
	public static void show(String title,String header,String yes,String no, Project_parameters parent, boolean confirmed) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		
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
		
		ButtonType yesButton = new ButtonType(yes);
		
        alert.getButtonTypes().setAll(yesButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {
			 
	      } else {
	    	  ;
	    	  parent.proceed_cancel();
	      }
	}

	public static Boolean show(String title, String header, String yes, String no, String cancel,
			EventHandler<MouseEvent> eventHandler,Auto_classification_progress parent) {
		
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(header);
		
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
		
		ButtonType yesButton = new ButtonType(ok);
		
        alert.getButtonTypes().setAll(yesButton);
	
		Optional<ButtonType> option = alert.showAndWait();
		
	}

	
		
}

