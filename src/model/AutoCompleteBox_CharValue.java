package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import service.TranslationServices;
import transversal.dialog_toolbox.ValueTranslationDisambiguation;

public class AutoCompleteBox_CharValue {
	
	  /** The existing autocomplete entries. */
	  /** The popup used to select an entry. */
	  private ContextMenu entriesPopup;
	  private Map<Integer, CharValueTextSuggestion> RESULTMAP;
	  private TextField textfield;
	  private Char_description parent;
	  private ArrayList<CharValueTextSuggestion> entries;
	  private AutoCompleteBox_CharValue sibling;
	  private static boolean blockFocusLostProcessing=false;
	  
	  public AutoCompleteBox_CharValue(Char_description parent,TextField textField,boolean isDataField, UserAccount account){
		  this.textfield = textField;
		  setOnFocusLostProcessValue(isDataField);
		  this.parent = parent;
		  refresh_entries(isDataField);
		  entriesPopup = new ContextMenu();
		    
		  
		    textfield.textProperty().addListener(new ChangeListener<String>()
		    {
		      @SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
		      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
		        if (!textfield.isFocused() || !(textfield.getText()!=null) ||  textfield.getText().length() == 0)
		        {

		          entriesPopup.hide();
		        } else
		        {
		        	
		        	
		          LinkedList<CharValueTextSuggestion> searchResult = new LinkedList<>();
		          final List<CharValueTextSuggestion> filteredEntries = entries.stream().filter(e -> e.sourceTextContains(textfield.getText()))
		        		  .collect(Collectors.toList());
		          searchResult.addAll(filteredEntries);
		          //searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
		          if (entries.size() > 0)
		          {
		        	
		        	  Collections.sort(searchResult,new Comparator() {

						@Override
						public int compare(Object o1, Object o2) {
							int ret = (new Integer(
									((CharValueTextSuggestion)o1).getSource_value().compareTo(
									((CharValueTextSuggestion)o2).getSource_value())
									));
							
		        	  		return ret ;
						}
		                 
		        		});
		        	
		        	
		            populatePopup(searchResult);
		            if (!entriesPopup.isShowing())
		            {
		              entriesPopup.show(textfield, Side.BOTTOM, 0, 0);
		              
		            }
		            
		            try{
		            	blockFocusLostProcessing = true;
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").setOnKeyPressed(ke ->{
		            		if(ke.getCode().equals(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
		            			setValuesOnParent(RESULTMAP.get(0), parent);
		            		}
		            	});
		            	blockFocusLostProcessing = false;
		            	
		            }catch(Exception V) {
		            	//Empty list
		            	blockFocusLostProcessing = false;
		            }
		          } else
		          {
		            entriesPopup.hide();
		          }
		        }
		        
		      }
		    });

		    textfield.focusedProperty().addListener(new ChangeListener<Boolean>() {
		      @Override
		      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
		        entriesPopup.hide();
		      }
		    });
		    textfield.setStyle(parent.classification.getStyle());
	}

	  

  private void setOnFocusLostProcessValue(boolean isDataField) {
		this.textfield.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(oldValue && !newValue && parent.translated_value_field.isVisible()) {
					if(textfield.getText()!=null && textfield.getText().length()>0) {
						
						processValueOnFocusLost(isDataField);
						
					}
				}
			}
			
		});
	}



public void processValueOnFocusLost(boolean isDataField) {
	if(blockFocusLostProcessing || !textfield.getScene().getWindow().isFocused()) {
		return;
	}
	System.out.println("Focus lost on autocomplete field");
	Optional<CharValueTextSuggestion> result = entries.stream().filter(e -> e.sourceTextEquals(textfield.getText())).findAny();
	if(result.isPresent()) {
		//Process the known suggestion
		setValuesOnParent(result.get(),parent);
	}else {
		//Create a new suggestion and process
		CharValueTextSuggestion tmp = new CharValueTextSuggestion(isDataField?"DATA":"USER", isDataField?"USER":"DATA");
		tmp.setSource_value(textfield.getText());
		tmp.setTarget_value(TranslationServices.getEntryTranslation(tmp.getSource_value(),isDataField));
		setValuesOnParent(tmp,parent);
	}	
}



public void refresh_entries(boolean isDataField) {
	  entries = TranslationServices.getTextEntriesForActiveCharOnLanguages(parent,isDataField);
	}



/**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(LinkedList<CharValueTextSuggestion> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    int maxEntries = 20;
    int count = Math.min(searchResult.size(), maxEntries);
    
    RESULTMAP = new HashMap<Integer,CharValueTextSuggestion>();
    for (int i = 0; i < count; i++)
    {
      final CharValueTextSuggestion result = searchResult.get(i);
      Label entryLabel = new Label(result.getDisplay_value());
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent) {
        	setValuesOnParent(result,parent);
        }
      });
      RESULTMAP.put(i,result);
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }



protected void setValuesOnParent(CharValueTextSuggestion result, Char_description parent) {
	
	System.out.println("Processing suggestion: "+result.getDisplay_value());
	CharValueTextSuggestion otherResultCopy;
	
	if(result.isDataFieldSuggestion()) {
		TextField otherfield = parent.translated_value_field;
		if(otherfield.getText()!=null && otherfield.getText().length()>0) {
			//The other field is not empty
			String otherText = otherfield.getText();
			if(result.getTarget_value()!=null && result.getTarget_value().length()>0) {
				//We know what value the other field is supposed to have
				if(otherText.equals(result.getTarget_value())) {
					//The value equals the present value, just update the source value
					System.out.println("Matching links");
					parent.value_field.setText(result.getSource_value());
				}else {
					//Conflict, resolve
					Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(parent, result, otherText);
					if(updateValues!=null) {
						if(updateValues) {
							updateValues = ValueTranslationDisambiguation.promptTranslationWarning(parent, result, otherText);
							if(updateValues!=null) {
								if(updateValues) {
									System.out.println("Updating links and values");
									TranslationServices.updateTranslation(result,true,otherText);
									parent.value_field.setText(result.getSource_value());
								}else {
									System.out.println("Keeping links and values");
									//The user chose to keep old value
									parent.value_field.setText(result.getSource_value());
									parent.translated_value_field.setText(result.getTarget_value());
								}
							}else {
								//The user cancelled, do nothing
								
							}
						}else {
							System.out.println("Keeping links and values");
							//The user chose to keep old value
							parent.value_field.setText(result.getSource_value());
							parent.translated_value_field.setText(result.getTarget_value());
						}
					}else {
						//The user cancelled, do nothing
						
					}
				}
			}else {
				//We don't know what the other field is supposed to have
				System.out.println("We don't know what the other field is supposed to have");
				Optional<CharValueTextSuggestion> otherResult = sibling.entries.stream().filter(e -> e.sourceTextEquals(otherfield.getText())).findAny();
				if(otherResult.isPresent()) {
					//If the other field has no known translation
					if(!(otherResult.get().getTarget_value()!=null) || otherResult.get().getTarget_value().length()==0) {
						Boolean linkResults = ValueTranslationDisambiguation.promptTranslationCreation(parent, result, otherResult.get());
						if(linkResults!=null) {
							if(linkResults) {
								TranslationServices.updateTranslation(result,true,otherText);
								parent.value_field.setText(result.getSource_value());
							}else {
								parent.value_field.setText(result.getSource_value());
								parent.translated_value_field.setText(null);
							}
						}else {
							parent.value_field.setText(result.getSource_value());
						}
					}else {
						//The other field has a known translation
						//Launch this function with the otherResult as input
						parent.value_field.setText(result.getSource_value());
						otherResultCopy = otherResult.get().flipIsDataFieldSuggestion();
						setValuesOnParent(otherResultCopy, parent);
					}
					
				}else {
					//The other word is not known
					Boolean linkResults = ValueTranslationDisambiguation.promptTranslationCreation(parent, result, otherText);
					if(linkResults!=null) {
						if(linkResults) {
							TranslationServices.updateTranslation(result,true,otherText);
							parent.value_field.setText(result.getSource_value());
						}else {
							parent.value_field.setText(result.getSource_value());
							parent.translated_value_field.setText(null);
						}
					}else {
						parent.value_field.setText(result.getSource_value());
					}
				}
				
			}
			
		}else {
			//The other field is empty
			parent.value_field.setText(result.getSource_value());
			parent.translated_value_field.setText(result.getTarget_value());
		}

	}else {
		TextField otherfield = parent.value_field;
		if(otherfield.getText()!=null && otherfield.getText().length()>0) {
			//The other field is not empty
			String otherText = otherfield.getText();
			if(result.getTarget_value()!=null && result.getTarget_value().length()>0) {
				//We know what value the other field is supposed to have
				if(otherText.equals(result.getTarget_value())) {
					//The value equals the present value, just update the source value
					System.out.println("Matching links");
					parent.translated_value_field.setText(result.getSource_value());
				}else {
					//Conflict, resolve
					Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(parent, result, otherText);
					if(updateValues!=null) {
						if(updateValues) {
							updateValues = ValueTranslationDisambiguation.promptTranslationWarning(parent, result, otherText);
							if(updateValues!=null) {
								if(updateValues) {
									System.out.println("Updating links and values");
									TranslationServices.updateTranslation(result,false,otherText);
									parent.translated_value_field.setText(result.getSource_value());
								}else {
									System.out.println("Keeping links and values");
									//The user chose to keep old value
									parent.translated_value_field.setText(result.getSource_value());
									parent.value_field.setText(result.getTarget_value());
								}
							}else {
								//The user cancelled, do nothing
								
							}
						}else {
							System.out.println("Keeping links and values");
							//The user chose to keep old value
							parent.translated_value_field.setText(result.getSource_value());
							parent.value_field.setText(result.getTarget_value());
						}
					}else {
						//The user cancelled, do nothing
						
					}
				}
			}else {
				//We don't know what the other field is supposed to have
				System.out.println("We don't know what the other field is supposed to have");
				Optional<CharValueTextSuggestion> otherResult = sibling.entries.stream().filter(e -> e.sourceTextEquals(otherfield.getText())).findAny();
				if(otherResult.isPresent()) {
					//If the other field has no known translation
					if(!(otherResult.get().getTarget_value()!=null) || otherResult.get().getTarget_value().length()==0) {
						Boolean linkResults = ValueTranslationDisambiguation.promptTranslationCreation(parent, result, otherResult.get());
						if(linkResults!=null) {
							if(linkResults) {
								TranslationServices.updateTranslation(result,false,otherText);
								parent.translated_value_field.setText(result.getSource_value());
							}else {
								parent.translated_value_field.setText(result.getSource_value());
								parent.value_field.setText(null);
							}
						}else {
							parent.translated_value_field.setText(result.getSource_value());
						}
					}else {
						//The other field has a known translation
						//Launch this function with the otherResult as input
						parent.value_field.setText(result.getSource_value());
						otherResultCopy = otherResult.get().flipIsDataFieldSuggestion();
						setValuesOnParent(otherResultCopy, parent);
					}
					
				}else {
					//The other word is not known
					Boolean linkResults = ValueTranslationDisambiguation.promptTranslationCreation(parent, result, otherText);
					if(linkResults!=null) {
						if(linkResults) {
							TranslationServices.updateTranslation(result,false,otherText);
							parent.translated_value_field.setText(result.getSource_value());
						}else {
							parent.translated_value_field.setText(result.getSource_value());
							parent.value_field.setText(null);
						}
					}else {
						parent.translated_value_field.setText(result.getSource_value());
					}
				}
				
			}
			
		}else {
			//The other field is empty
			parent.translated_value_field.setText(result.getSource_value());
			parent.value_field.setText(result.getTarget_value());
		}
	}
	Platform.runLater(new Runnable() {

		@Override
		public void run() {
			hidePopUp();
			
		}
		
	});
}



public void hidePopUp() {
	this.entriesPopup.hide();
}



public void setSibling(AutoCompleteBox_CharValue sibling) {
	this.sibling = sibling;
}



}
