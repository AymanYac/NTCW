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
import javafx.scene.input.KeyEvent;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import controllers.Manual_classif;


public class AutoCompleteBox_ManualClassification extends TextField
{
  /** The existing autocomplete entries. */
  private final SortedSet<String> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;
  private Manual_classif parent_controller;
  private Robot keyboardRobot;
  private Map<Integer, String> RESULTMAP;
  private UserAccount account;
  protected boolean PopupIsVisible=false;
  
  /** Construct a new AutoCompleteTextField. 
 * @param parent_controller 
 * @param style 
 * @param account 
 * @param rowIndex */
  public AutoCompleteBox_ManualClassification( Manual_classif parent_controller, String style, UserAccount account) {
    super();
    try {
		keyboardRobot = new Robot();
	} catch (AWTException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    this.parent_controller = parent_controller;
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
	
	this.account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5), false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.W, false);
	this.account.PRESSED_KEYBOARD.put(KeyCode.Q, false);
	
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
            handleKeyBoardEvent(keyEvent,true);
        }
    });
    this.setOnKeyReleased(new EventHandler<KeyEvent>() 
    {
        public void handle(final KeyEvent keyEvent) 
        {
            handleKeyBoardEvent(keyEvent,false);
        }
    });
    
    
    textProperty().addListener(new ChangeListener<String>()
    {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
        if (getText().length() == 0 || parent_controller.CHANGING_CLASS)
        {
          entriesPopup.hide();
        } else
        {
          LinkedList<String> searchResult = new LinkedList<>();
          final List<String> filteredEntries = entries.stream().filter(e->WordUtils.filterClassNameAutoCompelete(e,getText())).collect(Collectors.toList());
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
              entriesPopup.show(AutoCompleteBox_ManualClassification.this, Side.BOTTOM, 0, 0);
              
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

  protected void handleKeyBoardEvent(KeyEvent keyEvent, boolean pressed) {
	
	
	if(keyEvent.getCode().equals(KeyCode.CONTROL)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.UP, false);
		this.account.PRESSED_KEYBOARD.put(KeyCode.DOWN, false);
		
		this.account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.SHIFT)) {
		if(!pressed) {
			this.account.PRESSED_KEYBOARD.put(KeyCode.UP, false);
			this.account.PRESSED_KEYBOARD.put(KeyCode.DOWN, false);
		}
		this.account.PRESSED_KEYBOARD.put(KeyCode.SHIFT, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.D)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.D, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.U)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.U, pressed);
	}
	
	if(keyEvent.getCode().equals(KeyCode.PAGE_UP)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.PAGE_UP, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.PAGE_DOWN)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.PAGE_DOWN, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.DOWN)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.DOWN, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.UP)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.UP, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.ENTER)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.ENTER, pressed);
	}
	
	/*
	if(keyEvent.getCode().equals(KeyCode.CONTROL)) {
		keyEvent.consume();
		this.account.PRESSED_KEYBOARD.put(KeyCode.CONTROL, pressed);
	}*/
	if(keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1))) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1), pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2))) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2), pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3))) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3), pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4))) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4), pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5))) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5), pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.W)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.W, pressed);
	}
	if(keyEvent.getCode().equals(KeyCode.Q)) {
		this.account.PRESSED_KEYBOARD.put(KeyCode.Q, pressed);
	}
	
	
	
	
	
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_1))) {
		parent_controller.fireProposition(1);
	}
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_2))) {
		parent_controller.fireProposition(2);
	}
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_3))) {
		parent_controller.fireProposition(3);
	}
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_4))) {
		parent_controller.fireProposition(4);
	}
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.getKeyCode(GlobalConstants.MANUAL_PROPS_5))) {
		parent_controller.fireProposition(5);
	}
	
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.Q)) {
		parent_controller.firePropositionPreclass();
	}
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.W)) {
		parent_controller.firePropositionPreviousclass();
	}
	
	
	
	
	
	
	if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.D)) {
		parent_controller.tableController.fireClassDown();
	}
	
	else if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.U)) {
		parent_controller.tableController.fireClassUp();
	}
	
	else if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
		parent_controller.tableController.fireScrollNBDown();
		entriesPopup.hide();
	}
	
	else if(this.account.PRESSED_KEYBOARD.get(KeyCode.CONTROL) && this.account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
		parent_controller.tableController.fireScrollNBUp();
		entriesPopup.hide();
	}

	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.ENTER) && !account.PRESSED_KEYBOARD.get(KeyCode.SHIFT)) {
		
			if(entriesPopup.isShowing()) {
				send_classification_to_parent(RESULTMAP.get(0));
			}else {
				parent_controller.tableController.skipClassification();
			}
			parent_controller.tableController.tableGrid.requestFocus();
		}
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.ESCAPE)) {
			parent_controller.classification.requestFocus();
		}
	
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.DOWN)) {
		parent_controller.tableController.tableGrid.requestFocus();
		duplicateKeyEvent(KeyCode.DOWN);
		//parent_controller.fireClassScroll(rowIndex+1,KeyCode.DOWN);
	}
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.UP)) {
		parent_controller.tableController.tableGrid.requestFocus();
		duplicateKeyEvent(KeyCode.UP);
		//parent_controller.fireClassScroll(rowIndex-1,KeyCode.UP);
	}
	
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.PAGE_DOWN)) {
		parent_controller.tableController.tableGrid.requestFocus();
		duplicateKeyEvent(KeyCode.PAGE_DOWN);
		//parent_controller.fireClassScroll(rowIndex+1,KeyCode.DOWN);
	}
	else if (this.account.PRESSED_KEYBOARD.get(KeyCode.PAGE_UP)) {
		parent_controller.tableController.tableGrid.requestFocus();
		duplicateKeyEvent(KeyCode.PAGE_UP);
		//parent_controller.fireClassScroll(rowIndex-1,KeyCode.UP);
	}
	
	
	
}

private void duplicateKeyEvent(KeyCode key) {
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
		//setText(result);
		entriesPopup.hide();
		parent_controller.tableController.tableGrid.requestFocus();
	}else {
		
		
		parent_controller.classification.setText("");
		parent_controller.tableController.tableGrid.getSelectionModel().clearAndSelect(1+ (int) Collections.max(parent_controller.tableController.tableGrid.getSelectionModel().getSelectedIndices()));
	
	}
	
	
}
}