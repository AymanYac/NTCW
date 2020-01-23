package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import controllers.paneControllers.TablePane_CharClassif;
import model.CharDescriptionRow;
import model.CharacteristicValue;
import model.ClassCharacteristic;
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
			
			stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,project_values.value_id,text_value_data_language, text_value_user_language,nominal_value,min_value,max_value,note,uom_id from "
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
				//TranslationServices.addTextEntry(rs.getString("text_value"),characteristic_id);
				val.setNominal_value(rs.getString("nominal_value"));
				val.setMin_value(rs.getString("min_value"));
				val.setMax_value(rs.getString("max_value"));
				val.setNote(rs.getString("note"));
				val.setUom_id(rs.getString("uom_id"));
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
				row.setAuthor(user_id);
				row.setSource(description_method);
				row.setRule_id(description_rule_id);
				
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



	public static void fillData(String classSegment, int active_char_index, CharacteristicValue value, CharDescriptionRow i) {
		if(i.getClass_segment().contains(classSegment)) {
			i.getData(classSegment)[active_char_index]=value;
		}
	}
}
