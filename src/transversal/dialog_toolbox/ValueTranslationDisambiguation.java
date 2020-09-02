package transversal.dialog_toolbox;

import controllers.Char_description;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.CharValueTextSuggestion;

import java.util.Optional;

public class ValueTranslationDisambiguation {
	
	
	public static Boolean promptTranslationUpdate(Char_description parent,CharValueTextSuggestion result, String otherText) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		alert.setTitle("Value ambiguity");
		//alert.setHeaderText("There's currently a known translation for '"+result.getSource_value()+"':\n'"+
		//					result.getTarget_value()+"'. Do you wish to update to '"+otherText+"'?");
		
		ButtonType yesButton = new ButtonType(result.getSource_value()+" ("+otherText+")");
        ButtonType noButton = new ButtonType(result.getSource_value()+" ("+result.getTarget_value()+")");
        ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation warning");
		alert.setHeaderText("All current values "+result.getSource_value()+" ("+
							result.getTarget_value()+") will be changed to "+result.getSource_value()+" ("+otherText+").\n"
							+"Do you wish to proceed anyway?");
		
		ButtonType yesButton = new ButtonType("Yes, replace all translations");
        ButtonType noButton = new ButtonType("No, keep current translations");
        ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(yesButton, noButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation values creation");
		//alert.setHeaderText("Do you wish to create the translation link:\n"+
		//					"'"+result.getSource_value()+"' <=> '"+ otherResult.getSource_value()+"'?");
		
		ButtonType yesButton = new ButtonType(result.getSource_value()+" ("+otherResult.getSource_value()+")");
		
		ButtonType noButton;
		if(result.getTarget_value()!=null && result.getTarget_value().length()>0) {
			noButton = new ButtonType(result.getSource_value()+" ("+result.getTarget_value()+")");
		}else {
			noButton = new ButtonType(result.getSource_value());
		}
		ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
        
		alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation values creation");
		//alert.setHeaderText("Do you wish to create the translation link:\n"+
		//					"'"+result.getSource_value()+"' <=> '"+ otherResult.getSource_value()+"'?");
		
		ButtonType yesButton = new ButtonType(result.getSource_value()+" ("+otherText+")");
		ButtonType noButton = new ButtonType(result.getSource_value());
		ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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

	private static void addKeyboardHandler(Alert alert, Button cancelButton) {
		
		EventHandler<KeyEvent> fireOnEnter = event -> {
		    if (KeyCode.ENTER.equals(event.getCode()) 
		            && event.getTarget() instanceof Button) {
		        ((Button) event.getTarget()).fire();
		    }
		};
		
		EventHandler<KeyEvent> fireOnEscape = event -> {
		    if (KeyCode.ESCAPE.equals(event.getCode()) 
		            && event.getTarget() instanceof Button) {
		        cancelButton.fire();
		    }
		};
		
		
		alert.getButtonTypes().forEach(t->{
			alert.getDialogPane().lookupButton(t).addEventHandler(
                    KeyEvent.KEY_PRESSED,
                    fireOnEnter);
			if(!t.equals(cancelButton)){
				((Button) alert.getDialogPane().lookupButton(t)).setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
				((Button) alert.getDialogPane().lookupButton(t)).setMaxSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
				((Button) alert.getDialogPane().lookupButton(t)).setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);
			}

		});
		
		alert.getButtonTypes().forEach(t->{
			alert.getDialogPane().lookupButton(t).addEventHandler(
                    KeyEvent.KEY_PRESSED,
                    fireOnEscape);	  
		});
	}

	public static Boolean promptTranslationDeletion(Char_description parent, CharValueTextSuggestion result) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation values suppression");
		//alert.setHeaderText("Do you wish to create the translation link:\n"+
		//					"'"+result.getSource_value()+"' <=> '"+ otherResult.getSource_value()+"'?");
		
		ButtonType yesButton = new ButtonType(result.getSource_value()+" ("+result.getTarget_value()+")");
		ButtonType noButton = new ButtonType(result.getSource_value());
		ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
		Optional<ButtonType> option = alert.showAndWait();
		 if (option.get() == null) {

	      } else if (option.get() == yesButton) {
	    	  ;
	         return false;
	      } else if (option.get() == noButton) {
	    	  ;
	         return true;
	      } else {
	    	  ;
	    	  return null;
	      }
		return null;
		
	}

	public static Boolean promptTranslationUpdate(String text, String text2) {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation values creation");
		//alert.setHeaderText("Do you wish to create the translation link:\n"+
		//					"'"+result.getSource_value()+"' <=> '"+ otherResult.getSource_value()+"'?");
		
		ButtonType yesButton = new ButtonType(text+" ("+text2+")");
		ButtonType noButton = new ButtonType(text);
		ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(noButton,yesButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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

	public static Boolean promptTranslationWarning(String text, CharValueTextSuggestion otherResult) {

		Alert alert = new Alert(AlertType.WARNING);
		alert.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("customDialog");
		
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
	        @Override
	        public void handle(DialogEvent event) {
	            alert.close();
	        }
	    });
		
		alert.setTitle("Translation warning");
		alert.setHeaderText("All current values "+otherResult.getTarget_value()+" ("+
				otherResult.getSource_value()+") will be changed to "+text+" ("+otherResult.getSource_value()+").\n"
							+"Do you wish to proceed anyway?");
		
		ButtonType yesButton = new ButtonType("Yes, replace all translations");
        ButtonType noButton = new ButtonType("No, keep current translations");
        ButtonType cancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE); 
		
        alert.getButtonTypes().setAll(yesButton, noButton,cancelButton);
        alert.getDialogPane().lookupButton(cancelButton).setVisible(false);
        addKeyboardHandler(alert,(Button)alert.getDialogPane().lookupButton(cancelButton));
        
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
