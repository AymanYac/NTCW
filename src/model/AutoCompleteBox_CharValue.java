package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.Char_description;
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

public class AutoCompleteBox_CharValue {
	
	  /** The existing autocomplete entries. */
	  /** The popup used to select an entry. */
	  private ContextMenu entriesPopup;
	  private Map<Integer, CharValueTextSuggestion> RESULTMAP;
	  protected boolean PopupIsVisible=false;
	  private TextField textfield;
	  private Char_description parent;
	private ArrayList<CharValueTextSuggestion> entries;
	  
	  public AutoCompleteBox_CharValue(Char_description parent,TextField textField,boolean isDataField, UserAccount account){
		  this.textfield = textField;
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
		          final List<CharValueTextSuggestion> filteredEntries = entries.stream().filter(e -> e.valueTextContains(textfield.getText()))
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
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").setOnKeyPressed(ke ->{
		            		if(ke.getCode().equals(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
		            			setValuesOnParent(RESULTMAP.get(0), parent);
		            		}
		            	});
		            	PopupIsVisible = true;
		            	
		            }catch(Exception V) {
		            	//Empty list
		            	PopupIsVisible = false;
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
	parent.note_field.requestFocus();
	if(result.isDataFieldSuggestion()) {
		parent.value_field.setText(result.getSource_value());
		parent.translated_value_field.setText(result.getTarget_value());
		return;
	}
	
	parent.translated_value_field.setText(result.getSource_value());
	parent.value_field.setText(result.getTarget_value());
	return;
}



}
