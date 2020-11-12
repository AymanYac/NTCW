package service;

import controllers.Char_description;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import model.CaracteristicValue;
import model.CharDescriptionRow;
import model.ClassCaracteristic;
import model.UnitOfMeasure;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.dialog_toolbox.UoMDeclarationDialog;

import java.util.HashMap;

public class CharClassifProposer {

	
	private static Char_description parent;
	private static int lastestActiveSAIndex = -1;
	private static int lastestActiveCRIndex = -1;
	
	private static HashMap<Integer,Pair<CaracteristicValue,String>> buttonToData = new HashMap<Integer,Pair<CaracteristicValue,String>>();
	
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

	public void addSemiAutoProposition(String buttonText, CaracteristicValue preparedValue, String preparedRule,
			ClassCaracteristic active_char) {
		
		for(int i=0;i<=lastestActiveSAIndex;i++) {
			Button loopBtn = parent.propButtons.get(i);
			System.out.println("Checking for button "+loopBtn.getText());
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
				parent.sendSemiAutoPattern(val, rule);
				
			}
			
		});
		
		System.out.println("Putting in prop "+lastestActiveSAIndex+" rule "+preparedRule);
		Pair<CaracteristicValue,String> data = new Pair<CaracteristicValue,String>(preparedValue, preparedRule);
		buttonToData.put(lastestActiveSAIndex, data);
		
		
	}

	private static void clearRulefromButton(int i) {
		System.out.println("Disabling rule on button "+String.valueOf(i));
		buttonToData.put(i, new Pair<CaracteristicValue,String>(buttonToData.get(i).getKey(),null));
	}

	public static CaracteristicValue getValueForButton(int currentLoopButtonIndex) {
		System.out.println("*GETTING VALUE*");
		System.out.println(currentLoopButtonIndex);
		System.out.println(buttonToData.get(currentLoopButtonIndex).getKey());
		System.out.println(buttonToData.get(currentLoopButtonIndex).getValue());
		return buttonToData.get(currentLoopButtonIndex).getKey();
	}

	public static String getRuleForButton(int currentLoopButtonIndex) {
		System.out.println("*GETTING RULE*");
		System.out.println(currentLoopButtonIndex);
		System.out.println(buttonToData.get(currentLoopButtonIndex).getKey());
		System.out.println(buttonToData.get(currentLoopButtonIndex).getValue());
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
					result.setStatus("Applied");
					CharValuesLoader.updateRuntimeDataForItem(row,segment,result.getSourceChar().getCharacteristic_id(),result.getActionValue());
					CharDescriptionExportServices.addItemCharDataToPush(row, segment, charId);
					CharDescriptionExportServices.flushItemDataToDB(parent.account, null);
					clearPropButtons();
				});
				lastestActiveCRIndex+=1;
			});	
		}catch(Exception V) {
			
		}
		
	}


}
