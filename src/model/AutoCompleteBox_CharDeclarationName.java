package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public class AutoCompleteBox_CharDeclarationName extends TextField{

	 /** The existing autocomplete entries. */
	  private final List<ClassCaracteristic> entries;
	  /** The popup used to select an entry. */
	  private ContextMenu entriesPopup;
	  private Map<Integer, ClassCaracteristic> RESULTMAP;
	  protected boolean PopupIsVisible=false;
	 
	  public AutoCompleteBox_CharDeclarationName(String style) {
		    super();
		    entries = new ArrayList<ClassCaracteristic>();
		    entriesPopup = new ContextMenu();
		    
		    
		    
		    textProperty().addListener(new ChangeListener<String>()
		    {
		      @SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
		      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
		        if (getText().length() == 0 || !isFocused())
		        {

		          entriesPopup.hide();
		        } else
		        {
		        	
		        	
		          LinkedList<ClassCaracteristic> searchResult = new LinkedList<>();
		          final List<ClassCaracteristic> filteredEntries = entries.stream().filter(e -> e.getCharacteristic_name().contains(getText()))
		        		  .collect(Collectors.toList());
		          searchResult.addAll(filteredEntries);
		          //searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
		          if (entries.size() > 0)
		          {
		        	
		        	  Collections.sort(searchResult,new Comparator() {

						@Override
						public int compare(Object o1, Object o2) {
							int ret = (new Integer(
									((ClassCaracteristic)o1).getCharacteristic_name().compareTo(
									((ClassCaracteristic)o2).getCharacteristic_name())
									));
							
		        	  		return ret ;
						}
		                 
		        		});
		        	
		        	
		            populatePopup(searchResult);
		            if (!entriesPopup.isShowing())
		            {
		              entriesPopup.show(AutoCompleteBox_CharDeclarationName.this, Side.BOTTOM, 0, 0);
		              
		            }
		            
		            try{
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
		            	entriesPopup.getSkin().getNode().lookup(".menu-item").setOnKeyPressed(ke ->{
		            		if(ke.getCode().equals(KeyCode.ENTER) ) {
		            			processSelectedCarac(RESULTMAP.get(0));
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
		    

	    focusedProperty().addListener(new ChangeListener<Boolean>() {
	      @Override
	      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
	        entriesPopup.hide();
	      }
	    });
	    //this.setStyle("-fx-control-inner-background: #445469");
	    this.setStyle(style);
	  }
	  
	  
	  
	  




	private void populatePopup(LinkedList<ClassCaracteristic> searchResult) {
		    List<CustomMenuItem> menuItems = new LinkedList<>();
		    int maxEntries = 20;
		    int count = Math.min(searchResult.size(), maxEntries);
		    
		    RESULTMAP = new HashMap<Integer,ClassCaracteristic>();
		    for (int i = 0; i < count; i++)
		    {
		      final ClassCaracteristic result = searchResult.get(i);
		      Label entryLabel = new Label(result.toString());
		      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
		      item.setOnAction(new EventHandler<ActionEvent>()
		      {
		        @Override
		        public void handle(ActionEvent actionEvent) {
		        	processSelectedCarac(result);
		        }
		      });
		      RESULTMAP.put(i,result);
		      menuItems.add(item);
		    }
		    entriesPopup.getItems().clear();
		    entriesPopup.getItems().addAll(menuItems);

		  }

	protected void processSelectedCarac(ClassCaracteristic classCaracteristic) {
		// TODO Auto-generated method stub
		
	}


	  
}
