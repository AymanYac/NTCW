package service;

import controllers.Char_description;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import model.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.dialog_toolbox.UoMDeclarationDialog;
import transversal.generic.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class CharClassifProposer {

	
	private static Char_description parent;
	private static int lastestActiveSAIndex = -1;
	private static int lastestActiveCRIndex = -1;
	
	private static HashMap<Integer,Pair<CaracteristicValue,String>> buttonToData = new HashMap<Integer,Pair<CaracteristicValue,String>>();
	private static HashMap<String,ArrayList<CaracteristicValue>> customValues = new HashMap<String,ArrayList<CaracteristicValue>>();
	public boolean selectionFromBrowser =false;

	public CharClassifProposer(Char_description parent) {
		CharClassifProposer.parent = parent;
	}

	public void clearPropButtons() {
		buttonToData.clear();
		lastestActiveSAIndex=-1;
		for(Button btn:parent.propButtons) {
			btn.setOnAction((event) -> {
				  
			});
			btn.setText("--");
			btn.setTooltip(null);
			btn.setOpacity(0.5);
		}
	}

	public void

	addSemiAutoProposition(String buttonText, CaracteristicValue preparedValue, String preparedRule,
			ClassCaracteristic active_char,String selectedText) {
		if(parent.draftingRule){
			CharDescriptionRow activeRow = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
			String activeClass = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0];
			int activeCharIndex = parent.tableController.selected_col;
			ArrayList<ClassCaracteristic> activeChars = CharValuesLoader.active_characteristics.get(activeClass);
			ClassCaracteristic activeChar = activeChars.get(activeCharIndex%activeChars.size());
			GenericCharRule newRule = new GenericCharRule(preparedRule, activeChar);
			newRule.setRegexMarker();
			if(newRule.parseSuccess()) {
				newRule.storeGenericCharRule();
				CharRuleResult draft = new CharRuleResult(newRule, selectedText, parent.account);
				draft.setStatus("Draft");
				draft.setActionValue(preparedValue);
				activeRow.addRuleResult2Row(draft);
			}
			return;
		}
		for(int i=0;i<=lastestActiveSAIndex;i++) {
			Button loopBtn = parent.propButtons.get(i);
			/*System.out.println("Checking for button "+loopBtn.getText());*/
			if(loopBtn.getText().equals(buttonText)) {
				clearRulefromButton(i);
				return;
			}else {
				try{
					
				}catch(Exception V) {
					if(preparedValue.getMax_value().equals(preparedValue.getMin_value())) {
						return;
					}
				}
			}
		}
		lastestActiveSAIndex = lastestActiveSAIndex+1;
		int currentLoopButtonIndex = Integer.valueOf(lastestActiveSAIndex);
		Button btn = parent.propButtons.get(lastestActiveSAIndex);
		btn.setText(buttonText);
		btn.setOpacity(1.0);
		btn.setOnAction((event) -> {
			if(preparedValue.getUom_id()!=null && !UnitOfMeasure.RunTimeUOMS.containsKey(preparedValue.getUom_id())) {
				//Launch UoM Declaration box
				UoMDeclarationDialog.UomDeclarationPopUpFromPropButton(parent,buttonText.split("\"")[1],currentLoopButtonIndex,active_char);
				
			}else {
				CaracteristicValue val = getValueForButton(currentLoopButtonIndex);
				String rule = getRuleForButton(currentLoopButtonIndex);
				parent.sendSemiAutoPattern(val, rule, null);
				
			}
			
		});
		
		/*System.out.println("Putting in prop "+lastestActiveSAIndex+" rule "+preparedRule);
		*/
		Pair<CaracteristicValue,String> data = new Pair<CaracteristicValue,String>(preparedValue, preparedRule);
		buttonToData.put(lastestActiveSAIndex, data);
		
		
	}

	private static void clearRulefromButton(int i) {
		/*System.out.println("Disabling rule on button "+String.valueOf(i));*/
		buttonToData.put(i, new Pair<CaracteristicValue,String>(buttonToData.get(i).getKey(),null));
	}

	public static CaracteristicValue getValueForButton(int currentLoopButtonIndex) {
		/*System.out.println("*GETTING VALUE*");
		System.out.println(currentLoopButtonIndex);
		System.out.println(buttonToData.get(currentLoopButtonIndex).getKey());
		System.out.println(buttonToData.get(currentLoopButtonIndex).getValue());*/
		return buttonToData.get(currentLoopButtonIndex).getKey();
	}

	public static String getRuleForButton(int currentLoopButtonIndex) {
		/*System.out.println("*GETTING RULE*");
		System.out.println(currentLoopButtonIndex);
		System.out.println(buttonToData.get(currentLoopButtonIndex).getKey());
		System.out.println(buttonToData.get(currentLoopButtonIndex).getValue());*/
		return buttonToData.get(currentLoopButtonIndex).getValue();
	}

	public void loadCharRuleProps(CharDescriptionRow row,String segment, String charId) {
		try {
			lastestActiveCRIndex=0;
			row.getRulePropositions(charId).stream().forEach(result->{
				Button btn = parent.propButtons.get(lastestActiveCRIndex);
				btn.setText(result.getMatchedBlock()+" >>> "+result.getActionValue().getDisplayValue(false,false));
				btn.setTooltip(new Tooltip(btn.getText()));
				btn.setOpacity(1.0);
				btn.setOnAction((event) -> {
					//result.setStatus("Applied");
					CaracteristicValue sc = result.getActionValue().shallowCopy(parent.account);
					sc.setSource(DataInputMethods.SEMI_CHAR_DESC);
					CharValuesLoader.updateRuntimeDataForItem(row,segment,result.getSourceChar().getCharacteristic_id(),sc);
					CharDescriptionExportServices.addItemCharDataToPush(row, segment, charId);
					CharDescriptionExportServices.flushItemDataToDBThreaded(parent.account, null);
					clearPropButtons();
					if(!parent.charsVisible.get()){
						try {
							int idx = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedIndex();
							parent.tableController.charDescriptionTable.getSelectionModel().clearAndSelect(idx + 1);
						}catch (Exception V){

						}
					}
					parent.refresh_ui_display();
					parent.tableController.charDescriptionTable.refresh();
				});
				lastestActiveCRIndex+=1;
			});	
		}catch(Exception V) {
			
		}
		
	}


	public void loadCustomValues(ClassCaracteristic activeChar) {
		ArrayList<CaracteristicValue> values = customValues.get(activeChar.getCharacteristic_id());
		if(values!=null){
			try {
				lastestActiveCRIndex=0;
				values.forEach(v->{
					CaracteristicValue copy = v.shallowCopy(parent.account);
					copy.setSource(DataInputMethods.MANUAL);
					Button btn = parent.propButtons.get(lastestActiveCRIndex);
					btn.setText(copy.getDisplayValue(parent));
					btn.setOpacity(1.0);
					btn.setOnAction((event) -> {
						parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItems().forEach(row->{
							CharValuesLoader.updateRuntimeDataForItem(row,row.getClass_segment_string().split("&&&")[0],activeChar.getCharacteristic_id(),copy.shallowCopy(parent.account));
							CharDescriptionExportServices.addItemCharDataToPush(row, row.getClass_segment_string().split("&&&")[0],activeChar.getCharacteristic_id());
						});
						if(!parent.charsVisible.get()){
							int idx = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedIndex();
							parent.tableController.jumpNext();
							//parent.tableController.tableGrid.getSelectionModel().clearAndSelect(idx+1);
						}
						parent.tableController.charDescriptionTable.refresh();
						CharDescriptionExportServices.flushItemDataToDBThreaded(parent.account, null);
					});
					ContextMenu cm = new ContextMenu();
					MenuItem m = new MenuItem("Remove this value from custom suggestions");
					m.setOnAction(event -> {
						customValues.get(activeChar.getCharacteristic_id()).remove(v);
						parent.refresh_ui_display();
					});
					cm.getItems().add(m);
					btn.setContextMenu(cm);
					lastestActiveCRIndex+=1;
				});
			}catch(Exception V) {

			}
		}
	}

	public void addCustomValue(String activeCharId, CaracteristicValue copy, UserAccount account) {
		if(copy!=null){
			CaracteristicValue shallowCopy = copy.shallowCopy(account);
			try{
				customValues.get(activeCharId).add(shallowCopy);
			}catch (Exception V){
				customValues.put(activeCharId,new ArrayList<CaracteristicValue>());
				customValues.get(activeCharId).add(shallowCopy);
			}
		}
		
	}

	public void clearCustomValues() {
		try{
			customValues.clear();
		}catch (Exception V){

		}
	}

	public String getUserSelectedText() {
		selectionFromBrowser = false;
		String selectedText = "";
		selectedText = TextUtils.getSelectedText(parent.ld);
		if(selectedText.length()==0) {
			selectedText = TextUtils.getSelectedText(parent.ld_translated);
			if(selectedText.length()==0) {
				selectedText = TextUtils.getSelectedText(parent.sd);
				if(selectedText.length()==0) {
					selectedText = TextUtils.getSelectedText(parent.sd_translated);
				}
			}
		}
		try{
			String browserSelection = (String) parent.browserController.browser.nodeValue.getEngine().executeScript("window.getSelection().toString()");
			if (browserSelection != null && browserSelection.length() > 0) {
				selectedText = browserSelection;
				selectionFromBrowser = true;
			}
		}catch (Exception V){

		}
		try{
			if(parent.browserController.showingPdf.getValue()) {
				String pdfSelection = parent.browserController.iceController.getDocumentViewController().getSelectedText();
				if (pdfSelection != null && pdfSelection.length() > 0) {
					selectedText = pdfSelection;
					selectionFromBrowser = true;
				}
			}

		}catch (Exception V){

		}
		return selectedText;
	}
}
