package model;

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
import javafx.scene.input.KeyEvent;
import transversal.language_toolbox.Unidecode;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class AutoCompleteBox_CharClassification extends TextField
{
  /** The existing autocomplete entries. */
  private final SortedSet<String> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;
  private Char_description parent_controller;
  private Robot keyboardRobot;
  private Map<Integer, String> RESULTMAP;
  private UserAccount account;
  protected boolean PopupIsVisible=false;
  
  /** Construct a new AutoCompleteTextField. 
 * @param char_description 
 * @param style 
 * @param account
   * */
  public AutoCompleteBox_CharClassification( Char_description char_description, String style, UserAccount account) {
    super();
    try {
		keyboardRobot = new Robot();
	} catch (AWTException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    this.parent_controller = char_description;
    this.account = account; 
    
    this.account.PRESSED_KEYBOARD.put(KeyCode.ESCAPE, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.D, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.U, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.DOWN, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.UP, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.PAGE_UP, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.PAGE_DOWN, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.RIGHT, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.LEFT, false);
	
	
	this.account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5), false);
	
	Unidecode unidecode = Unidecode.toAscii();
	
    
    
    
    entries = new TreeSet<>();
    entriesPopup = new ContextMenu();
    focusedProperty().addListener(new ChangeListener<Boolean>()
    {
        @Override
        public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
        {
            if (newPropertyValue) //On focus
            {
                //parent_controller.fireClassSelection(null);
            }
            else
            {
                
            }
        }
    });
    this.setOnKeyPressed(new EventHandler<KeyEvent>() 
    {
        public void handle(final KeyEvent keyEvent) 
        {
            handleReclassifFieldKeyBoardEvent(keyEvent,true);
        }
    });
    this.setOnKeyReleased(new EventHandler<KeyEvent>() 
    {
        public void handle(final KeyEvent keyEvent) 
        {
            handleReclassifFieldKeyBoardEvent(keyEvent,false);
        }
    });
    
    
    textProperty().addListener(new ChangeListener<String>()
    {
      @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
        if (getText().length() == 0 || char_description.CHANGING_CLASS)
        {
          entriesPopup.hide();
        } else
        {
          LinkedList<String> searchResult = new LinkedList<>();
          final List<String> filteredEntries = entries.stream().filter(e -> unidecode.decodeAndTrim(e).toLowerCase().contains(unidecode.decodeAndTrim(getText()).toLowerCase())).collect(Collectors.toList());
          searchResult.addAll(filteredEntries);
          //searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
          if (entries.size() > 0)
          {
        	
        	  Collections.sort(searchResult,new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					int ret = (new Integer(
							((String)o1).split("&&&")[1].compareTo(
							((String)o2).split("&&&")[1])
							));
					int av_1 =  unidecode.decodeAndTrim(((String)o1).split("&&&")[1]).toUpperCase().startsWith(unidecode.decodeAndTrim(getText()).toUpperCase())?1000000:0;
        	  		int av_2 =  unidecode.decodeAndTrim(((String)o2).split("&&&")[1]).toUpperCase().startsWith(unidecode.decodeAndTrim(getText()).toUpperCase())?1000000:0;
        	  		
        	  		return ret - av_1 + av_2;
				}
                 
        		});
        	
        	
            populatePopup(searchResult);
            if (!entriesPopup.isShowing())
            {
              entriesPopup.show(AutoCompleteBox_CharClassification.this, Side.BOTTOM, 0, 0);
              
            }
            
            try{
            	entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
            	entriesPopup.getSkin().getNode().lookup(".menu-item").setOnKeyPressed(ke ->{
            		if(ke.getCode().equals(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
            			send_classification_to_parent(RESULTMAP.get(0));
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

  protected void handleReclassifFieldKeyBoardEvent(KeyEvent keyEvent, boolean pressed) {


	if (this.account.PRESSED_KEYBOARD.get(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
		
			if(entriesPopup.isShowing()) {
				send_classification_to_parent(RESULTMAP.get(0));
			}else {
				//parent_controller.tableController.skipClassification();
			}
			parent_controller.tableController.charDescriptionTable.requestFocus();
		}
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.ESCAPE)) {
			parent_controller.value_field.requestFocus();
			parent_controller.hideAutoCompletePopups();
			parent_controller.value_field.end();
			parent_controller.value_field.selectAll();
		}
	
}

public void duplicateKeyEvent(KeyCode key) {
	if(!GlobalConstants.AUTO_TEXT_FIELD_DUPLICATE_ACTION) {
		return;
	}
	try {
		
	    keyboardRobot = new Robot();
	    //there are other methods such as positioning mouse and mouseclicks etc.
	    if(key.equals(KeyCode.DOWN)){
	    	keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_DOWN);
	    }
	    if(key.equals(KeyCode.UP)){
	    	keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_UP);
	    }
	    if(key.equals(KeyCode.PAGE_UP)){
	    	keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_PAGE_UP);
	    }
	    if(key.equals(KeyCode.PAGE_DOWN)){
	    	keyboardRobot.keyPress(java.awt.event.KeyEvent.VK_PAGE_DOWN);
	    }
	    
	 } catch (AWTException e) {
	    
	 }
}

/**
   * Get the existing set of autocomplete entries.
   * @return The existing autocomplete entries.
   */
  public SortedSet<String> getEntries() { return entries; }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<String> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    int maxEntries = 20;
    int count = Math.min(searchResult.size(), maxEntries);
    
    RESULTMAP = new HashMap<Integer,String>();
    for (int i = 0; i < count; i++)
    {
      final String result = searchResult.get(i);
      Label entryLabel = new Label(result.split("&&&")[1]);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent) {
        	send_classification_to_parent(result);
        }
      });
      RESULTMAP.put(i,result);
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }

protected void send_classification_to_parent(String result)  {
	if(result!=null) {
		parent_controller.fireClassChange(result);
		/*System.out.print("Class change result string ::");*/
		
		//setText(result);
		entriesPopup.hide();
		parent_controller.tableController.charDescriptionTable.requestFocus();
	}else {
		
		
		parent_controller.classification.setText("");
		parent_controller.tableController.charDescriptionTable.getSelectionModel().clearAndSelect(1+ (int) Collections.max(parent_controller.tableController.charDescriptionTable.getSelectionModel().getSelectedIndices()));
	
	}
	
	
}
}