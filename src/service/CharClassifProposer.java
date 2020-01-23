package service;

import java.util.HashMap;

import controllers.Char_description;
import javafx.scene.control.Button;
import javafx.util.Pair;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.UnitOfMeasure;
import transversal.dialog_toolbox.UoMDeclarationDialog;

public class CharClassifProposer {

	
	private static Char_description parent;
	private static int lastestActiveButtonIndex = -1;
	private static HashMap<Integer,Pair<CharacteristicValue,String>> buttonToData = new HashMap<Integer,Pair<CharacteristicValue,String>>();
	
	public CharClassifProposer(Char_description parent) {
		CharClassifProposer.parent = parent;
	}

	public void clearPropButtons() {
		buttonToData.clear();
		lastestActiveButtonIndex=-1;
		for(Button btn:parent.propButtons) {
			btn.setOnAction((event) -> {
				  
			});
			btn.setText("--");
			btn.setOpacity(0.5);
		}
	}

	public void addProposition(String buttonText, CharacteristicValue preparedValue, String preparedRule,
			ClassCharacteristic active_char) {
		
		for(int i=0;i<=lastestActiveButtonIndex;i++) {
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
		lastestActiveButtonIndex = lastestActiveButtonIndex+1;
		int currentLoopButtonIndex = Integer.valueOf(lastestActiveButtonIndex);
		Button btn = parent.propButtons.get(lastestActiveButtonIndex);
		btn.setText(buttonText);
		btn.setOpacity(1.0);
		btn.setOnAction((event) -> {
			if(preparedValue.getUom_id()!=null && !UnitOfMeasure.RunTimeUOMS.containsKey(preparedValue.getUom_id())) {
				//Launch UoM Declaration box
				UoMDeclarationDialog.UomDeclarationPopUpFromPropButton(parent,buttonText.split("\"")[1],currentLoopButtonIndex,active_char);
				
			}else {
				parent.sendPatternRule(getRuleForButton(currentLoopButtonIndex));
				parent.sendPatternValue(getValueForButton(currentLoopButtonIndex));
			}
			
		});
		
		System.out.println("Putting in prop "+lastestActiveButtonIndex+" rule "+preparedRule);
		Pair<CharacteristicValue,String> data = new Pair<CharacteristicValue,String>(preparedValue, preparedRule);
		buttonToData.put(lastestActiveButtonIndex, data);
		
		
	}

	private static void clearRulefromButton(int i) {
		System.out.println("Disabling rule on button "+String.valueOf(i));
		buttonToData.put(i, new Pair<CharacteristicValue,String>(buttonToData.get(i).getKey(),null));
	}

	public static CharacteristicValue getValueForButton(int currentLoopButtonIndex) {
		return buttonToData.get(currentLoopButtonIndex).getKey();
	}

	public static String getRuleForButton(int currentLoopButtonIndex) {
		return buttonToData.get(currentLoopButtonIndex).getValue();
	}


}
