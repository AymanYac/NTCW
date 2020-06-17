package controllers.paneControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import service.ManualClassifContext;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class PropositionContext_ManualClassif {
	
	@FXML public  RadioButton prop1;
	@FXML public  RadioButton prop2;
	@FXML public  RadioButton prop3;
	@FXML public  RadioButton prop4;
	@FXML public  TextField prop5;
	@FXML public  RadioButton prop6;
	
	private ManualClassifContext parent;
	private int idx;
	protected boolean keepSelected;
	public HashMap<String, String> CID2NAME;
	public Stage Stage;
	
	
	@FXML void initialize() {
		
		
		
		
		
		prop1.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
		        if (isNowSelected) { 
		        	
		        	keepSelected=false;
		            ManualClassifContext.methods.set(idx, "FW");
		            parent.parent.proposer.proposeAgain();
		        	prop2.setSelected(false);
		        	prop3.setSelected(false);
		        	prop4.setSelected(false);
		        	prop5.setText("");
		        	prop6.setSelected(false);
		        	keepSelected=true;
		        	
		        } else {
		            if(wasPreviouslySelected && keepSelected) {
		            	prop1.setSelected(true);
		            }
		        }
		    }
		});
		
		prop2.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
		        if (isNowSelected) { 
		        	
		        	keepSelected=false;
		            ManualClassifContext.methods.set(idx, "MG");
		            parent.parent.proposer.proposeAgain();
		        	prop1.setSelected(false);
		        	prop3.setSelected(false);
		        	prop4.setSelected(false);
		        	prop5.setText("");
		        	prop6.setSelected(false);
		        	keepSelected=true;
		        	
		        } else {
		            if(wasPreviouslySelected && keepSelected) {
		            	prop2.setSelected(true);
		            }
		        }
		    }
		});
		
		prop3.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
		        if (isNowSelected) { 
		        	
		        	keepSelected=false;
		            ManualClassifContext.methods.set(idx, "FOR");
		            parent.parent.proposer.proposeAgain();
		        	prop2.setSelected(false);
		        	prop1.setSelected(false);
		        	prop4.setSelected(false);
		        	prop5.setText("");
		        	prop6.setSelected(false);
		        	keepSelected=true;
		        	
		        } else {
		            if(wasPreviouslySelected && keepSelected) {
		            	prop3.setSelected(true);
		            }
		        }
		    }
		});
		
		prop4.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
		        if (isNowSelected) { 
		        	
		        	keepSelected=false;
		            ManualClassifContext.methods.set(idx, "DW");
		            parent.parent.proposer.proposeAgain();
		        	prop2.setSelected(false);
		        	prop3.setSelected(false);
		        	prop1.setSelected(false);
		        	prop5.setText("");
		        	prop6.setSelected(false);
		        	keepSelected=true;
		        	
		        } else {
		            if(wasPreviouslySelected && keepSelected) {
		            	prop4.setSelected(true);
		            }
		        }
		    }
		});
		
		prop5.textProperty().addListener(new ChangeListener<String>() {

		    @Override
		    public void changed(ObservableValue<? extends String> observable, 
		                                    String oldValue, String newValue) {
		    	
		    if(newValue.length()>0) {
		    	
		    	
		    	try {	
					String pcl = newValue;
					
					Map<String, String> matches = parent.parent.proposer.segments.entrySet().stream().filter(
							m -> m.getValue().toUpperCase().split("&&&")[1].equals(pcl.toUpperCase())).collect(
						Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
					int numMatches = matches.size();
						if(numMatches==1) {
							String key = (String) matches.keySet().toArray()[0];
							ManualClassifContext.methods.set(idx,key+"&&&"+newValue);
							enableFixedProp();
					    	
						}else {
							ManualClassifContext.methods.set(idx, null);
						}
				}catch(Exception V) {
					ManualClassifContext.methods.set(idx, null);
				}
		    	
		    }
		    
		    	
		    	
		    }

			private void enableFixedProp() {
				keepSelected=false;
		    	prop1.setSelected(false);
		    	prop2.setSelected(false);
		    	prop3.setSelected(false);
		    	prop4.setSelected(false);
		    	prop6.setSelected(false);
		    	keepSelected=true;
		    	parent.parent.proposer.proposeAgain();
			}
		});
		
		prop6.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
		        if (isNowSelected) { 
		        	
		        	keepSelected=false;
		            ManualClassifContext.methods.set(idx, "ML");
		            parent.parent.proposer.proposeAgain();
		            prop1.setSelected(false);
		            prop2.setSelected(false);
		        	prop3.setSelected(false);
		        	prop4.setSelected(false);
		        	prop5.setText("");
		        	keepSelected=true;
		        	
		        } else {
		            if(wasPreviouslySelected && keepSelected) {
		            	prop6.setSelected(true);
		            }
		        }
		    }
		});
		
	}
	


	public void setParent(ManualClassifContext manualClassifContext) {
		this.parent=manualClassifContext;
		
	}


	public void setIdx(int idx) {
		this.idx=idx;
	}
	
	public void selectPreviousItem() {
		if(ManualClassifContext.methods.get(idx)!=null) {
			switch(ManualClassifContext.methods.get(idx)){
			case "FW":
				prop1.setSelected(true);
				break;
			case "MG":
				prop2.setSelected(true);
				break;
			case "FOR":
				prop3.setSelected(true);
				break;
			case "DW":
				prop4.setSelected(true);
				break;
			case "ML":
				prop6.setSelected(true);
				break;
			default:
				try{
					prop5.setText(ManualClassifContext.methods.get(idx).split("&&&")[1]);
				}catch(Exception V) {
					prop5.setText("");
				}
			
		}
		}
	}



	public void setClasses(HashMap<String, String> CID2NAME) {
		this.CID2NAME = CID2NAME;
		TextFields.bindAutoCompletion(prop5, CID2NAME.values());
		
		
	}



	public void setStage(Stage stage) {
		this.Stage=stage;
		Stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() 
        {
			
            public void handle(final KeyEvent keyEvent) 
            {
            	if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            		parent.closeLast();
            		parent.parent.classification.requestFocus();
        			
        		}
            }
        });
	}
	
}
