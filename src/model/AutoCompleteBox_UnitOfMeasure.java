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
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


public class AutoCompleteBox_UnitOfMeasure extends TextField
{
  /** The existing autocomplete entries. */
  private final List<UnitOfMeasure> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;
  private Map<Integer, UnitOfMeasure> RESULTMAP;
  private String populateSearchMethod;
  protected boolean PopupIsVisible=false;
  public UnitOfMeasure selectedUom;
  public javafx.beans.property.BooleanProperty incompleteProperty = new javafx.beans.property.SimpleBooleanProperty();

    /** Construct a new AutoCompleteTextField.
 *  */
  public AutoCompleteBox_UnitOfMeasure(String SearchMethod) {
    super();
    entries = new ArrayList<UnitOfMeasure>();
    entriesPopup = new ContextMenu();
    incompleteProperty.setValue(true);
    populateSearchMethod = SearchMethod;
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

            selectedUom=null;
            incompleteProperty.setValue(true);
          LinkedList<UnitOfMeasure> searchResult = new LinkedList<>();
          final List<UnitOfMeasure> filteredEntries = entries.stream().filter(e ->
                  (populateSearchMethod.equals("NAME") && e.getUom_name().contains(getText()) )
                  ||( populateSearchMethod.equals("SYMBOL") && e.HasPartialUomSymbol(getText()) )
                  ||( populateSearchMethod.equals("NAME_AND_SYMBOL") && (e.getUom_name().contains(getText()) || e.HasPartialUomSymbol(getText())) )

          )
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
                  int av_1 =  ((UnitOfMeasure) o1).HasPartialUomSymbol(getText())?1000000:0;
                  int av_2 =  ((UnitOfMeasure) o2).HasPartialUomSymbol(getText())?1000000:0;

                  return ret - av_1 + av_2;
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
            		if(ke.getCode().equals(KeyCode.ENTER) ) {
            			print_uom_in_parent(RESULTMAP.get(0));
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
    int maxEntries = 6;
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
        	print_uom_in_parent(result);
        }
      });
      RESULTMAP.put(i,result);
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }


protected void print_uom_in_parent(UnitOfMeasure result) {
      setText(result.toString());
      this.selectedUom = result;
      incompleteProperty.setValue(false);

}




public void send_uom_to_parent(UnitOfMeasure unitOfMeasure) {
	/*CharDescriptionRow row = parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
	String selectedRowClass = row.getClass_segment().split("&&&")[0];
	int active_char_index = Math.floorMod(parent.tableController.selected_col,parent.tableController.active_characteristics.get(selectedRowClass).size());
	CharacteristicValue currentValue = row.getData(selectedRowClass)[active_char_index];
	currentValue=(currentValue!=null)?currentValue:new CharacteristicValue();
	currentValue.setUom_id(unitOfMeasure.getUom_id());
	parent.sendPatternValue(currentValue);
	*/
}




public void setUom(UnitOfMeasure unitOfMeasure) {
	setText(unitOfMeasure.getUom_symbol()+" ("+unitOfMeasure.getUom_name()+")");
}


}