package model;

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
import transversal.language_toolbox.Unidecode;

import java.util.*;
import java.util.stream.Collectors;

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
	  private static Unidecode unidecode = Unidecode.toAscii();
	  
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
							int av_1 =  ((CharValueTextSuggestion)o1).getSource_value().toUpperCase().startsWith(unidecode.decodeAndTrim(textField.getText()).toUpperCase())?1000000:0;
							int av_2 =  ((CharValueTextSuggestion)o1).getSource_value().toUpperCase().startsWith(unidecode.decodeAndTrim(textField.getText()).toUpperCase())?1000000:0;

							return ret - av_1 + av_2;
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
		            			parent.validateFieldsThenSkipToNext(setValuesOnParent(RESULTMAP.get(0), parent));
		            			
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
						
					}else {
						processEmptyValueOnFocusLost(isDataField);
					}
				}
			}
			
		});
	}



public Boolean processEmptyValueOnFocusLost(boolean isDataField) {
	if(blockFocusLostProcessing || !textfield.getScene().getWindow().isFocused()) {
		return false;
	}
	
	TextField otherField = isDataField?parent.translated_value_field:parent.value_field;
	
	Optional<CharValueTextSuggestion> result = sibling.entries.stream().filter(e -> e.sourceTextEquals(otherField.getText())).findAny();
	if(result.isPresent()) {
		
		if(result.get().getTarget_value()!=null && result.get().getTarget_value().length()>0) {
			
			//The other text has a non null translation
			Boolean deleteMatch = ValueTranslationDisambiguation.promptTranslationDeletion(parent, result.get());
			if(deleteMatch!=null) {
				if(deleteMatch) {
					TranslationServices.removeTranslation(result.get());
				}else {
					//Do nothing
				}
			}else {
				//The user has cancelled
				textfield.selectAll();
				return null;
			}
		}else {
			
		}
		
	}else {
		//The other value is not known, do nothing
		
		/*
		CharValueTextSuggestion tmp = new CharValueTextSuggestion(!isDataField?"DATA":"USER", !isDataField?"USER":"DATA");
		tmp.setSource_value(otherField.getText());
		tmp.setTarget_value(TranslationServices.getEntryTranslation(tmp.getSource_value(),!isDataField));
		sibling.setValuesOnParent(tmp,parent);
		*/
	}
	return true;
}



public Boolean processValueOnFocusLost(boolean isDataField) {
	if(blockFocusLostProcessing || !textfield.getScene().getWindow().isFocused()) {
		return false;
	}
	
	Optional<CharValueTextSuggestion> result = entries.stream().filter(e -> e.sourceTextEquals(textfield.getText())).findAny();
	if(result.isPresent()) {
		//Process the known suggestion
		
		return setValuesOnParent(result.get(),parent);
	}else {
		
		//Create a new suggestion and process
		CharValueTextSuggestion tmp = new CharValueTextSuggestion(isDataField?"DATA":"USER", isDataField?"USER":"DATA");
		tmp.setSource_value(textfield.getText());
		tmp.setTarget_value(TranslationServices.getEntryTranslation(tmp.getSource_value(),isDataField));
		return setValuesOnParent(tmp,parent);
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
        	parent.validateFieldsThenSkipToNext(setValuesOnParent(result,parent));
        }
      });
      RESULTMAP.put(i,result);
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }



protected Boolean setValuesOnParent(CharValueTextSuggestion result, Char_description parent) {
	
	
	
	if(result.isDataFieldSuggestion()) {
		TextField otherfield = parent.translated_value_field;
		if(otherfield.getText()!=null && otherfield.getText().length()>0) {
			//The other field is not empty
			String otherText = otherfield.getText();
			if(result.getTarget_value()!=null && result.getTarget_value().length()>0) {
				//We know what value the other field is supposed to have
				if(otherText.equals(result.getTarget_value())) {
					//The value equals the present value, just update the source value
					
					parent.value_field.setText(result.getSource_value());
				}else {
					//Conflict, resolve
					parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
					Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(parent, result, otherText);
					if(updateValues!=null) {
						if(updateValues) {
							parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
							updateValues = ValueTranslationDisambiguation.promptTranslationWarning(parent, result, otherText);
							if(updateValues!=null) {
								if(updateValues) {
									
									TranslationServices.updateTranslation(result,true,otherText);
									parent.value_field.setText(result.getSource_value());
								}else {
									
									//The user chose to keep old value
									parent.value_field.setText(result.getSource_value());
									parent.translated_value_field.setText(result.getTarget_value());
								}
							}else {
								//The user cancelled, do nothing
								return null;
								
							}
						}else {
							
							//The user chose to keep old value
							parent.value_field.setText(result.getSource_value());
							parent.translated_value_field.setText(result.getTarget_value());
						}
					}else {
						//The user cancelled, do nothing
						return null;
						
					}
				}
			}else {
				//We don't know what the other field is supposed to have
				
				Optional<CharValueTextSuggestion> otherResult = sibling.entries.stream().filter(e -> e.sourceTextEquals(otherfield.getText())).findAny();
				if(otherResult.isPresent()) {
					//If the other field has no known translation
					if(!(otherResult.get().getTarget_value()!=null) || otherResult.get().getTarget_value().length()==0) {
						
						parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
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
							//The user cancelled
							
							parent.value_field.setText(result.getSource_value());
							return null;
						}
					}else {
						//The other field has a known translation
						//Launch this function with the otherResult as input
						
						String knownTranslation = otherResult.get().getTarget_value();
						if(unidecode.decodeAndTrim(knownTranslation.toLowerCase()).equals(
								unidecode.decodeAndTrim(textfield.getText().toLowerCase()))){
							
							
						}else {
							
							//Textfield / Textfield, otherText
							Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(textfield.getText(), otherfield.getText());
							if(updateValues!=null) {
								if(updateValues) {
									parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
									updateValues = ValueTranslationDisambiguation.promptTranslationWarning(textfield.getText(), otherResult.get());
									if(updateValues!=null) {
										if(updateValues) {
											//Update the values
											TranslationServices.updateTranslation(otherResult.get(),false,textfield.getText());
										}else {
											//Clear the other field
											otherfield.setText("");
										}
									}else {
										//The user cancelled, do nothing
										return null;
									}
									
								}else {
									//Clear the other field
									otherfield.setText("");
								}
							}else {
								//The user cancelled, do nothing
								return null;
							}
						}
					}
					
				}else {
					//The other word is not known
					
					parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
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
						//The user cancelled
						parent.value_field.setText(result.getSource_value());
						return null;
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
					
					parent.translated_value_field.setText(result.getSource_value());
				}else {
					//Conflict, resolve
					parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
					Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(parent, result, otherText);
					if(updateValues!=null) {
						if(updateValues) {
							parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
							updateValues = ValueTranslationDisambiguation.promptTranslationWarning(parent, result, otherText);
							if(updateValues!=null) {
								if(updateValues) {
									
									TranslationServices.updateTranslation(result,false,otherText);
									parent.translated_value_field.setText(result.getSource_value());
								}else {
									
									//The user chose to keep old value
									parent.translated_value_field.setText(result.getSource_value());
									parent.value_field.setText(result.getTarget_value());
								}
							}else {
								//The user cancelled, do nothing
								return null;
								
							}
						}else {
							
							//The user chose to keep old value
							parent.translated_value_field.setText(result.getSource_value());
							parent.value_field.setText(result.getTarget_value());
						}
					}else {
						//The user cancelled, do nothing
						return null;
						
					}
				}
			}else {
				
				//We don't know what the other field is supposed to have
				
				Optional<CharValueTextSuggestion> otherResult = sibling.entries.stream().filter(e -> e.sourceTextEquals(otherfield.getText())).findAny();
				if(otherResult.isPresent()) {
					//If the other field has no known translation
					if(!(otherResult.get().getTarget_value()!=null) || otherResult.get().getTarget_value().length()==0) {
						
						parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
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
							//The user cancelled
							
							parent.translated_value_field.setText(result.getSource_value());
							return null;
						}
					}else {

						//The other field has a known translation
						
						String knownTranslation = otherResult.get().getTarget_value();

						if(unidecode.decodeAndTrim(knownTranslation.toLowerCase()).equals(
								unidecode.decodeAndTrim(textfield.getText().toLowerCase()))){
							
							
						}else {
							
							//Textfield / Textfield, otherText
							Boolean updateValues = ValueTranslationDisambiguation.promptTranslationUpdate(textfield.getText(), otherfield.getText());
							if(updateValues!=null) {
								if(updateValues) {
									parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
									updateValues = ValueTranslationDisambiguation.promptTranslationWarning(textfield.getText(), otherResult.get());
									if(updateValues!=null) {
										if(updateValues) {
											//Update the values
											TranslationServices.updateTranslation(otherResult.get(),true,textfield.getText());
											
										}else {
											//Clear the other field
											otherfield.setText("");
										}
									}else {
										//The user cancelled, do nothing
										return null;
									}
									
								}else {
									//Clear the other field
									otherfield.setText("");
								}
							}else {
								//The user cancelled, do nothing
								return null;
							}
						}						
					}
					
				}else {
					//The other word is not known
					parent.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
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
						//The user cancelled
						
						parent.translated_value_field.setText(result.getSource_value());
						return null;
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
	
	return true;
}



public void hidePopUp() {
	this.entriesPopup.hide();
}



public void setSibling(AutoCompleteBox_CharValue sibling) {
	this.sibling = sibling;
}



}
