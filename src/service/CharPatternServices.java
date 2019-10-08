package service;

import controllers.Char_description;
import model.ClassCharacteristic;

public class CharPatternServices {

	public static void scanSelectionForRuleCreation(Char_description parent, ClassCharacteristic activeChar) {
		
		String selected_text = "";
		selected_text = parent.ld.getSelectedText();
		if(selected_text.length()==0) {
			selected_text = parent.ld_translated.getSelectedText();
			if(selected_text.length()==0) {
				selected_text=parent.sd.getSelectedText();
				if(selected_text.length()==0) {
					selected_text=parent.sd_translated.getSelectedText();
				}
			}
		}
		System.out.println("Creating rule for ::: "+selected_text);
	}

}
