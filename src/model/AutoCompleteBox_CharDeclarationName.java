package model;

import java.awt.*;
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
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;

public class AutoCompleteBox_CharDeclarationName extends TextField{

	 /** The existing autocomplete entries. */
	  public final ArrayList<Pair<ClassSegment,ClassCaracteristic>> entries;
	  /** The popup used to select an entry. */
	  private final ContextMenu entriesPopup;
	  private Map<Integer, Pair<ClassSegment, ClassCaracteristic>> RESULTMAP;
	  protected boolean PopupIsVisible=false;
	  public Pair<ClassSegment,ClassCaracteristic> selectedEntry;
	  public javafx.beans.property.BooleanProperty incompleteProperty = new javafx.beans.property.SimpleBooleanProperty();

	public AutoCompleteBox_CharDeclarationName() {
		super();
		entries = new ArrayList<Pair<ClassSegment,ClassCaracteristic>>();
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

					selectedEntry=null;
					incompleteProperty.setValue(true);
		          LinkedList<Pair<ClassSegment,ClassCaracteristic>> searchResult = new LinkedList<>();
		          final List<Pair<ClassSegment,ClassCaracteristic>> filteredEntries = entries.stream().filter(e -> StringUtils.containsIgnoreCase(e.getValue().getCharacteristic_name(),getText()))
		        		  .collect(Collectors.toList());
		          searchResult.addAll(filteredEntries);
		          //searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
		          if (entries.size() > 0)
		          {
		        	
		        	  Collections.sort(searchResult,new Comparator() {

						@Override
						public int compare(Object o1, Object o2) {

							return (((Pair<ClassSegment, ClassCaracteristic>) o1).getValue().getCharacteristic_name().compareTo(
									((Pair<ClassSegment, ClassCaracteristic>) o2).getValue().getCharacteristic_name()));
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

	  }
	  
	  
	  
	  




	private void populatePopup(LinkedList<Pair<ClassSegment, ClassCaracteristic>> searchResult) {
		    List<CustomMenuItem> menuItems = new LinkedList<>();
		    int maxEntries = 20;
		    int count = Math.min(searchResult.size(), maxEntries);
		    
		    RESULTMAP = new HashMap<Integer, Pair<ClassSegment, ClassCaracteristic>>();
		    for (int i = 0; i < count; i++)
		    {
		      final Pair<ClassSegment,ClassCaracteristic> result = searchResult.get(i);
		      Label entryLabel = new Label(result.getValue().getCharacteristic_name()+"["+result.getKey().getClassName()+"]");
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

	protected void processSelectedCarac(Pair<ClassSegment, ClassCaracteristic> result) {
		setText(result.getValue().getCharacteristic_name()+"["+result.getKey().getClassName()+"]");
		this.selectedEntry = result;
		incompleteProperty.setValue(false);
	}


	  
}