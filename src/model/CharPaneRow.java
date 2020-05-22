package model;

import java.util.ArrayList;

import controllers.Char_description;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;

public class CharPaneRow {

	private int char_index;
	private CaracteristicValue value;
	private ClassCaracteristic carac;
	private static Char_description parent;
	
	public StackPane getCriticality() {
		Circle tmp = new Circle(8,8,8);
		
		Stop[] stops = new Stop[] { new Stop(0, Color.web("#DE827A")), new Stop(1, Color.web("#BD392F"))};
		LinearGradient empty = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
		stops = new Stop[] { new Stop(0, Color.web("#ACB9CA")), new Stop(1, Color.web("#8496AE"))};
		LinearGradient full = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
		
		
		tmp.setFill(( value!=null && value.isNonEmpty() )?full:empty);
		tmp.setStrokeWidth(1);
        tmp.setStroke(Color.WHITE);
        
		Text text = new Text("C");
		text.setFill(Color.WHITE);
		text.setBoundsType(TextBoundsType.VISUAL); 
		text.setFont(Font.font(GlobalConstants.CHAR_UOM_FONT,GlobalConstants.CHAR_UOM_WEIGHT,GlobalConstants.CHAR_UOM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
		
		StackPane stack = new StackPane();
		stack.getChildren().addAll(tmp, text);
		stack.setVisible(carac.getIsCritical());
		
		return stack;
	}
	public CharPaneRow(Char_description parent) {
		CharPaneRow.parent = parent;
	}
	public ClassCaracteristic getCarac() {
		return carac;
	}
	public void setCarac(ClassCaracteristic carac) {
		this.carac = carac;
	}
	public int getChar_index() {
		return char_index;
	}
	public void setChar_index(int char_index) {
		this.char_index = char_index;
	}
	public int getChar_sequence() {
		return carac.getSequence();
	}
	public String getChar_name() {
		return carac.getCharacteristic_name();
	}
	public String getChar_name_translated() {
		return carac.getCharacteristic_name_translated();
	}
	public ArrayList<String> getAllowed_uom() {
		return carac.getAllowedUoms();
	}
	public CaracteristicValue getValue() {
		return value;
	}
	public void setValue(CaracteristicValue value) {
		this.value = value;
	}
	
	public TextFlow getUom_display() {
		if(!(this.getAllowed_uom()!=null)) {
			return null;
		}
		ArrayList<Text> textes = new ArrayList<Text>();
		
		boolean same_uom_family=true;
		String base_uom_family=null;
		for(String uom:this.getAllowed_uom()) {
			if(base_uom_family!=null) {
				UnitOfMeasure loopUom = UnitOfMeasure.RunTimeUOMS.get(uom);
				if(!loopUom.getUom_base_id().equals(base_uom_family)) {
					same_uom_family = false;
					break;
				}
			}else {
				UnitOfMeasure loopUom = UnitOfMeasure.RunTimeUOMS.get(uom);
				base_uom_family = loopUom.getUom_base_id();
			}
		}
		for(int i=0;i<this.getAllowed_uom().size();i++) {
			Text tmp = new Text(UnitOfMeasure.RunTimeUOMS.get(this.getAllowed_uom().get(i)).getUom_symbol());
			tmp.setFill(GlobalConstants.CHAR_UOM_COLOR);
			tmp.setFont(Font.font(GlobalConstants.CHAR_UOM_FONT,GlobalConstants.CHAR_UOM_WEIGHT,GlobalConstants.CHAR_UOM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
			textes.add(tmp);
			if(i!=this.getAllowed_uom().size()-1) {
				tmp = new Text(same_uom_family?" or ":" or ");
				tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
				tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
				textes.add(tmp);
			}
		}
		TextFlow ret = new TextFlow(textes.toArray(new Text[textes.size()]));
		ret.setMinHeight(0);
		ret.setPrefHeight(0);
		return ret;
	}
	
	public TextFlow getValue_display(){
		if(!(this.getValue()!=null)) {
			return null;
		}
		return this.getValue().getFormatedDisplayAndUomPair(parent, carac).getValue();
	}
	/*public TextFlow getValue_display(){
		if(!(this.getValue()!=null)) {
			return null;
		}
		ArrayList<Text> textes = new ArrayList<Text>();
		if(!this.carac.getIsNumeric()) {
			Text tmp = new Text(this.getValue().getDataLanguageValue());
			tmp.setFill(GlobalConstants.CHAR_TXT_COLOR);
			tmp.setFont(Font.font(GlobalConstants.CHAR_TXT_FONT,GlobalConstants.CHAR_TXT_WEIGHT,GlobalConstants.CHAR_TXT_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
			textes.add(tmp);
			if(this.carac.getIsTranslatable() && !parent.user_language.equals(parent.data_language)) {
				//tmp = new Text(" ("+translateValue(this.getValue()).getNominal_value()+")");
				tmp = new Text(" ("+this.getValue().getUserLanguageValue()+")");
				tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
				tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
				textes.add(tmp);
				
			}
			
		}else {
			String local_uom="";
			String local_Nominal_value=null;
			String local_Min_value=null;
			String local_Max_value=null;
			try{
				
				UnitOfMeasure current_uom = UnitOfMeasure.RunTimeUOMS.get(this.getValue().getUom_id());
				if((!(current_uom!=null)) || carac.getAllowedUoms().contains(current_uom.getUom_id())) {
					//Either there's no uom or the uom is included in the allowed uoms
					//No conversion and show the input value
					try{
						local_uom = current_uom.getUom_symbol();
					}catch(Exception V) {
						//V.printStackTrace(System.err);
					}
					local_Nominal_value = this.value.getNominal_value();
					local_Max_value = this.value.getMax_value();
					local_Min_value = this.value.getMin_value();
				}else {
					//Converting to base uom
					UnitOfMeasure base_uom = UnitOfMeasure.RunTimeUOMS.get(current_uom.getUom_base_id());
					try{
						local_uom = base_uom.getUom_symbol();
					}catch(Exception V) {
						V.printStackTrace(System.err);
					}
					try{
						local_Nominal_value = String.valueOf( new BigDecimal( this.value.getNominal_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					try{
						local_Max_value = String.valueOf( new BigDecimal( this.value.getMax_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					try{
						local_Min_value = String.valueOf( new BigDecimal( this.value.getMin_value().replace(",", ".").replace(" ", "") ).multiply(current_uom.getUom_multiplier())).replace(".", ",");
					}catch(Exception V) {
						
					}
					for(String uom:this.getAllowed_uom()) {
						UnitOfMeasure loopUom = UnitOfMeasure.RunTimeUOMS.get(uom);
						if(loopUom.getUom_base_id().equals(base_uom.getUom_id())) {
							
							try{
								local_uom = loopUom.getUom_symbol();
							}catch(Exception V) {
								V.printStackTrace(System.err);
							}
							try{
								local_Nominal_value = String.valueOf( new BigDecimal( local_Nominal_value.replace(",", ".").replace(" ", "") ).divide(loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							try{
								local_Max_value = String.valueOf( new BigDecimal( local_Max_value.replace(",", ".").replace(" ", "") ).divide(loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							try{
								local_Min_value = String.valueOf( new BigDecimal( local_Min_value.replace(",", ".").replace(" ", "") ).divide( loopUom.getUom_multiplier())).replace(".", ",");
							}catch(Exception V) {
								
							}
							break;
							
						}
					}
					
					
				}
			}catch(Exception V) {
				V.printStackTrace(System.err);
				local_uom="";
			}
			
			if(local_Nominal_value!=null && local_Nominal_value.replace(" ","").length() > 0) {
				//Has nominal value
				@SuppressWarnings("static-access")
				Text tmp = new Text(local_Nominal_value+" "+local_uom);
				tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
				tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
				textes.add(tmp);
				if(local_Min_value!=null && local_Min_value.replace(" ","").length() > 0) {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text(" ("+local_Min_value+" to "+local_Max_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}else {
						tmp = new Text(" (Min:"+local_Min_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}
				}else {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text(" (Max:"+local_Max_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
						tmp.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.ITALIC_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						}
				}
			}else {
				//No nominal
				Text tmp;
				if(local_Min_value!=null && local_Min_value.replace(" ","").length() > 0) {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text("("+local_Min_value+" to "+local_Max_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}else {
						tmp = new Text("(Min:"+local_Min_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						
					}
				}else {
					if(local_Max_value!=null && local_Max_value.replace(" ","").length() > 0) {
						tmp = new Text("(Max:"+local_Max_value+" "+local_uom+") ");
						tmp.setFill(GlobalConstants.CHAR_NUM_COLOR);
						tmp.setFont(Font.font(GlobalConstants.CHAR_NUM_FONT,GlobalConstants.CHAR_NUM_WEIGHT,GlobalConstants.CHAR_NUM_POSTURE,GlobalConstants.CHAR_DISPLAY_FONT_SIZE));
						textes.add(tmp);
						}
			}
		}
		}
		TextFlow ret = new TextFlow(textes.toArray(new Text[textes.size()]));
		ret.setMinHeight(0);
		ret.setPrefHeight(0);
		return ret;
		
	}*/
	
}
