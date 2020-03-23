package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import controllers.Char_description;
import controllers.paneControllers.TablePane_CharClassif;
import model.CharDescriptionRow;
import model.CharacteristicValue;
import model.ClassCharacteristic;
import model.DataInputMethods;
import model.UnitOfMeasure;
import transversal.generic.Tools;

public class CharValuesLoader {
	
	static ArrayList<CharacteristicValue> knownValues;
	static HashMap<String,Integer> indexedKnownValues = new HashMap<String,Integer>();
	public static void loadAllKnownValuesAssociated2Items(String active_project,TablePane_CharClassif tablePane_CharClassif) throws SQLException, ClassNotFoundException {
		if(knownValues!=null) {
			return;
		}else {
			knownValues = new ArrayList<CharacteristicValue>();
			System.out.println("Loading all values known to items");
			
			Connection conn = Tools.spawn_connection();
			PreparedStatement stmt;
			ResultSet rs;
			
			stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,url_link,project_values.value_id,text_value_data_language, text_value_user_language,nominal_value,min_value,max_value,note,uom_id from "
					+ "(select * from "+active_project+".project_items_x_values"
									+ ") data left join "+active_project+".project_values "
											+ "on data.value_id = project_values.value_id");
			System.out.println(stmt.toString());
			rs = stmt.executeQuery();
			HashMap<String, List<String>> charIdArrays = new HashMap<String,List<String>>();
			tablePane_CharClassif.active_characteristics.forEach((k,v)->{
			charIdArrays.put(k, v.stream().map(c->c.getCharacteristic_id()).collect(Collectors.toList()));	
			});
			int i=-1;
			while(rs.next()) {
				String loop_class_id = CharItemFetcher.classifiedItems.get(rs.getString("item_id")).split("&&&")[4];
				String item_id = rs.getString("item_id");
				String characteristic_id = rs.getString("characteristic_id");
				String user_id = rs.getString("user_id");
				String description_method = rs.getString("description_method");
				String description_rule_id = rs.getString("description_rule_id");
				CharacteristicValue val = new CharacteristicValue();
				val.setValue_id(rs.getString("value_id"));
				val.setDataLanguageValue(rs.getString("text_value_data_language"));
				val.setUserLanguageValue(rs.getString("text_value_user_language"));
				TranslationServices.beAwareOfNewValue(val,tablePane_CharClassif.active_characteristics.get(loop_class_id).get(charIdArrays.get(loop_class_id).indexOf(characteristic_id)));
				val.setNominal_value(rs.getString("nominal_value"));
				val.setMin_value(rs.getString("min_value"));
				val.setMax_value(rs.getString("max_value"));
				val.setNote(rs.getString("note"));
				val.setUom_id(rs.getString("uom_id"));
				val.setAuthor(user_id);
				val.setSource(description_method);
				val.setRule_id(description_rule_id);
				val.setUrl(rs.getString("url_link"));
				
				try{
					val.setParentChar(tablePane_CharClassif.active_characteristics.get(loop_class_id).get(charIdArrays.get(loop_class_id).indexOf(characteristic_id)));
				}catch(Exception V) {
					System.out.println("Couldn't set parent for char "+characteristic_id+" in class "+loop_class_id);
				}
				knownValues.add(val);
				i+=1;
				indexedKnownValues.put(val.getValue_id(), i);
				
				CharDescriptionRow row = CharItemFetcher.allRowItems.get(CharItemFetcher.indexedRowItems.get(item_id));
				//row.allocateDataField(loop_class_id,tablePane_CharClassif.active_characteristics.get(loop_class_id).size());
				try{
					row.getData(loop_class_id)[charIdArrays.get(loop_class_id).indexOf(characteristic_id)]=val;
				}catch(Exception V) {
					System.out.println("Couldn't set data for char "+characteristic_id+" in class "+loop_class_id);
				}
				
				
			}
			
			rs.close();
			stmt.close();
			conn.close();
		}
		
	}
	
	

	public static void loadAllowedValuesAsKnownValuesForCharacteristic(ClassCharacteristic tmp) {
		if(Math.random()<0.01) {
			System.out.println("loading allowed values for char "+tmp.getCharacteristic_name());
		}
		/*for(String value_id:tmp.getAllowedValues()) {
			for(CharacteristicValue knownVal:knownValues) {
				if(value_id.equals(knownVal.getValue_id())) {
					knownVal.addThisValuetoKnownValuesForCharacteristic(tmp);
					break;
				}
			}
		}*/
		try {
			if(tmp.getAllowedValues().size()==0) {
				return;
			}
			indexedKnownValues.entrySet().parallelStream().filter(e->tmp.getAllowedValues().contains(e.getKey()))
			.forEach(e->{
				knownValues.get(e.getValue()).addThisValuetoKnownValuesForCharacteristic(tmp);
			});
			
		}catch(Exception V) {
			
		}
		
	}

	public static void clearKnownValues() {
		try {
			knownValues.clear();
			indexedKnownValues.clear();
			//knownValues = null;
		}catch(Exception V) {
			V.printStackTrace(System.err);
		}
	}



	public static void updateRuntimeDataForItem(CharDescriptionRow r, String classSegment, int active_char_index, CharacteristicValue value) {
		if(r.getClass_segment().contains(classSegment)) {
			r.getData(classSegment)[active_char_index]=value;
		}
	}



	public static void storeItemDatafromScreen(int idx, Char_description parent) {
		CharDescriptionRow row = parent.tableController.itemArray.get(idx);
		String class_id = row.getClass_segment().split("&&&")[0];
		ArrayList<ClassCharacteristic> chars = parent.tableController.active_characteristics.get(class_id);
		ClassCharacteristic active_char = chars.get(parent.tableController.selected_col%chars.size());
		
		CharacteristicValue tmp = new CharacteristicValue();
		tmp.setParentChar(active_char);
		if(active_char.getIsNumeric()) {
			tmp.setNominal_value(parent.value_field.getText());
			if(active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
				Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->u.toString().equals(parent.uom_field.getText())).findAny();
				if(uom.isPresent()) {
					tmp.setUom_id(uom.get().getUom_id());
				}
				tmp.setMin_value(parent.min_field_uom.getText());
				tmp.setMax_value(parent.max_field_uom.getText());
			}else {
				tmp.setMin_value(parent.min_field.getText());
				tmp.setMax_value(parent.max_field.getText());
			}
		}else {
			if(parent.value_field.getText()!=null && parent.value_field.getText().replace(" ", "").length()>0) {
				tmp.setDataLanguageValue(parent.value_field.getText());
			}else {
				tmp.setDataLanguageValue(null);
			}
			
			if(parent.translated_value_field.getText()!=null && parent.translated_value_field.getText().replace(" ", "").length()>0) {
				tmp.setUserLanguageValue(parent.translated_value_field.getText());
			}else {
				tmp.setUserLanguageValue(null);
			}
		}
		tmp.setNote(parent.note_field_uom.getText());
		tmp.setSource(DataInputMethods.MANUAL);
		tmp.setAuthor(parent.account.getUser_id());
		tmp.ManualValueReviewed=true;
		parent.AssignValueOnSelectedItems(tmp);
		
	}
}
