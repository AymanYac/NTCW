package model;

import controllers.Manual_classif;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.text.TextFlow;
import service.ManualRuleServices;

public class RulePaneRow {
	
	GenericRule gr;
	CheckBox cb;
	private static Manual_classif manualClassifController;
	private boolean engageUncheckAction=true;
	
	
	
	public RulePaneRow(Manual_classif parent,GenericRule gr, Boolean isSelected) {
		this.manualClassifController = parent;
		this.gr = gr;
		
		cb = new CheckBox();
		cb.setPrefHeight(0);
		cb.setAlignment(Pos.CENTER);
		
		if(isSelected!=null) {
			cb.setSelected(isSelected);
		}else {
			cb.setIndeterminate(true);
			cb.setAllowIndeterminate(true);
		}
		
		
		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				/*
				if(newValue) {
					if(ManualRuleServices.assignClass2GR(gr,(ItemFetcherRow) manualClassifController.tableController.tableGrid.getSelectionModel().getSelectedItem())) {
						//ManualRuleServices.benchmark(gr,manualClassifController);
						ManualRuleServices.applyRule(gr,manualClassifController);
					}else{
						engageUncheckAction=false;
						cb.setSelected(false);
						engageUncheckAction=true;
						return;
					};
					
				}else if(engageUncheckAction) {
					ManualRuleServices.unapplyRule(gr,manualClassifController);
					
				}
				
			*/
				if(newValue) {
					/*
					if(ManualRuleServices.assignClass2GR(gr,(ItemFetcherRow) manualClassifController.tableController.tableGrid.getSelectionModel().getSelectedItem())) {
						//ManualRuleServices.benchmark(gr,manualClassifController);
						ManualRuleServices.applyRule(gr,manualClassifController);
					}*/
					manualClassifController.classification.requestFocus();
				}else {
					/*
					try {
						ItemFetcherRow row = (ItemFetcherRow) manualClassifController.tableController.tableGrid.getSelectionModel().getSelectedItem();
						if(row.getSource_Display().equals("MANUAL")) {
							ManualRuleServices.unapplyRule(gr,manualClassifController);
							
						}
					}catch(Exception V) {
						
					}*/
					manualClassifController.classification.requestFocus();
					
				}
			}
		});
		
	}
	public GenericRule getGr() {
		return gr;
	}
	public void setGr(GenericRule gr) {
		this.gr = gr;
	}
	public String getType() {
		return gr.getType();
	}
	
	public String getRule_desc() {
		return gr.toString();
	}
	
	public TextFlow getRule_display() {
		return gr.toDisplay();
	}
	
	public CheckBox getCb() {
		return cb;
	}
	public void setCb(CheckBox cb) {
		this.cb = cb;
	}
	
	
	
}
