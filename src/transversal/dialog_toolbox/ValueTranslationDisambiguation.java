package transversal.dialog_toolbox;

import java.util.Optional;

import controllers.Char_description;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import model.CharValueTextSuggestion;

public class ValueTranslationDisambiguation {
	
	
	public static Boolean promptTranslationUpdate(Char_description parent,CharValueTextSuggestion result, String otherText) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Translation values ambiguous");
		alert.setHeaderText("There's currently a known translation for '"+result.getSource_value()+"':\n'"+
							result.getTarget_value()+"'. Do you wish to update to '"+otherText+"'?");
		
		ButtonType yesButton = new ButtonType("Yes: ("+result.getSource_value()+","+otherText+")");
        ButtonType noButton = new ButtonType("No: ("+result.getSource_value()+","+result.getTarget_value()+")");
        ButtonType cancelButton = new ButtonType("Cancel");
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
	
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

	public static Boolean promptTranslationWarning(Char_description parent, CharValueTextSuggestion result,
			String otherText) {
		
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Translation warning");
		alert.setHeaderText("Warning all item values '"+result.getSource_value()+"',"+
							result.getTarget_value()+"' will be saved as '"+result.getSource_value()+"',"+otherText+"'.\n"
							+"Do you wish to proceed ?");
		
		ButtonType yesButton = new ButtonType("Yes, replace all");
        ButtonType noButton = new ButtonType("No, keep known translation");
        ButtonType cancelButton = new ButtonType("Cancel");
		
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

	public static Boolean promptTranslationCreation(Char_description parent, CharValueTextSuggestion result,
			CharValueTextSuggestion otherResult) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Translation values creation");
		alert.setHeaderText("Do you wish to create the translation link:\n"+
							"'"+result.getSource_value()+"' <=> '"+ otherResult.getSource_value()+"'?");
		
		ButtonType yesButton = new ButtonType("Yes: ("+result.getSource_value()+","+otherResult.getSource_value()+")");
        ButtonType noButton = new ButtonType("No: ("+result.getSource_value()+")");
        ButtonType cancelButton = new ButtonType("Cancel");
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
	
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

	public static Boolean promptTranslationCreation(Char_description parent, CharValueTextSuggestion result,
			String otherText) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Translation values creation");
		alert.setHeaderText("Do you wish to create the translation link:\n"+
							"'"+result.getSource_value()+"' <=> '"+ otherText+"'?");
		
		ButtonType yesButton = new ButtonType("Yes: ("+result.getSource_value()+","+otherText+")");
        ButtonType noButton = new ButtonType("No: ("+result.getSource_value()+")");
        ButtonType cancelButton = new ButtonType("Cancel");
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
	
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
}
