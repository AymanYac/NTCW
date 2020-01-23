package model;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import controllers.Char_description;


public class AutoCompleteBox_UnitOfMeasure extends TextField
{
  /** The existing autocomplete entries. */
  private final List<UnitOfMeasure> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;
  private Map<Integer, UnitOfMeasure> RESULTMAP;
  protected boolean PopupIsVisible=false;
private Char_description parent;
  /** Construct a new AutoCompleteTextField. 
 * @param char_description 
 * @param style 
 * @param account 
 * @param rowIndex */
  public AutoCompleteBox_UnitOfMeasure( Char_description char_description, String style, UserAccount account) {
    super();
    entries = new ArrayList<UnitOfMeasure>();
    entriesPopup = new ContextMenu();
    parent = char_description;
    
    focusedProperty().addListener(new ChangeListener<Boolean>()
    {
        @Override
        public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
        {
            if (newPropertyValue) //On focus
            {
                check_revert_value_in_allowed_uoms();
            }
            else
            {
                
            }
        }
    });
    
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
        	
        	
          LinkedList<UnitOfMeasure> searchResult = new LinkedList<>();
          final List<UnitOfMeasure> filteredEntries = entries.stream().filter(e -> e.HasPartialUomSymbol(getText()))
        		  .collect(Collectors.toList());
          searchResult.addAll(filteredEntries);
          //searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
          if (entries.size() > 0)
          {
        	
        	  Collections.sort(searchResult,new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					int ret = (new Integer(
							((UnitOfMeasure)o1).getUom_name().compareTo(
							((UnitOfMeasure)o2).getUom_name())
							));
					
        	  		return ret ;
				}
                 
        		});
        	
        	
            populatePopup(searchResult);
            if (!entriesPopup.isShowing())
            {
              entriesPopup.show(AutoCompleteBox_UnitOfMeasure.this, Side.BOTTOM, 0, 0);
              
            }
            
            try{
            	entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
            	entriesPopup.getSkin().getNode().lookup(".menu-item").setOnKeyPressed(ke ->{
            		if(ke.getCode().equals(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
            			send_uom_to_parent(RESULTMAP.get(0));
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




protected void check_revert_value_in_allowed_uoms() {
	// TODO Auto-generated method stub
	
}








/**
   * Get the existing set of autocomplete entries.
   * @return The existing autocomplete entries.
   */
  public List<UnitOfMeasure> getEntries() { return entries; }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(LinkedList<UnitOfMeasure> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    int maxEntries = 20;
    int count = Math.min(searchResult.size(), maxEntries);
    
    RESULTMAP = new HashMap<Integer,UnitOfMeasure>();
    for (int i = 0; i < count; i++)
    {
      final UnitOfMeasure result = searchResult.get(i);
      Label entryLabel = new Label(result.toString());
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent) {
        	send_uom_to_parent(result);
        }
      });
      RESULTMAP.put(i,result);
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }


public void send_uom_to_parent(UnitOfMeasure unitOfMeasure) {
	CharDescriptionRow row = parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
	String selectedRowClass = row.getClass_segment().split("&&&")[0];
	int active_char_index = Math.floorMod(parent.tableController.selected_col,parent.tableController.active_characteristics.get(selectedRowClass).size());
	CharacteristicValue currentValue = row.getData(selectedRowClass)[active_char_index];
	currentValue=(currentValue!=null)?currentValue:new CharacteristicValue();
	currentValue.setUom_id(unitOfMeasure.getUom_id());
	parent.sendPatternValue(currentValue);
	
}




public void setUom(UnitOfMeasure unitOfMeasure) {
	setText(unitOfMeasure.getUom_symbol()+" ("+unitOfMeasure.getUom_name()+")");
}


}