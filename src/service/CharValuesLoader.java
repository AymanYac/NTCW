package service;

import controllers.Char_description;
import javafx.util.Pair;
import model.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CharValuesLoader {

	static ArrayList<CaracteristicValue> knownValues;
	static HashMap<String,Integer> indexedKnownValues = new HashMap<String,Integer>();
	public static HashMap<String,ArrayList<ClassCaracteristic>> active_characteristics = new HashMap<String,ArrayList<ClassCaracteristic>>();
	public static void fetchAllKnownValuesAssociated2Items(String active_project,boolean forceUpdate) throws SQLException, ClassNotFoundException {
		if(knownValues!=null && !forceUpdate) {
			return;
		}else {
			fetchDefaultCharValues(active_project);
			knownValues = new ArrayList<CaracteristicValue>();

			Connection conn = Tools.spawn_connection_from_pool();
			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select item_id,characteristic_id,user_id,description_method,description_rule_id,url_link,project_values.value_id,text_value_data_language, text_value_user_language,nominal_value,min_value,max_value,note,uom_id,description_time,manually_reviewed from "
					+ "(select * from "+active_project+".project_items_x_values"
					+ ") data left join "+active_project+".project_values "
					+ "on data.value_id = project_values.value_id");
			rs = stmt.executeQuery();
			HashMap<String, List<String>> charIdArrays = new HashMap<String,List<String>>();
			CharValuesLoader.active_characteristics.forEach((k,v)->{
				charIdArrays.put(k, v.stream().map(c->c.getCharacteristic_id()).collect(Collectors.toList()));
			});
			int i=-1;
			while(rs.next()) {
				if(!CharItemFetcher.classifiedItems.containsKey(rs.getString("item_id"))){
					continue;
				}
				String loop_class_id = CharItemFetcher.classifiedItems.get(rs.getString("item_id")).split("&&&")[4];;
				String item_id = rs.getString("item_id");
				String characteristic_id = rs.getString("characteristic_id");
				String user_id = rs.getString("user_id");
				String description_method = rs.getString("description_method");
				String description_rule_id = rs.getString("description_rule_id");
				if(!charIdArrays.containsKey(loop_class_id)){
					continue;
				}
				if(charIdArrays.get(loop_class_id).indexOf(characteristic_id)==-1){
					continue;
				}
				CaracteristicValue val = new CaracteristicValue();
				val.setValue_id(rs.getString("value_id"));
				val.setDataLanguageValue(rs.getString("text_value_data_language"));
				val.setUserLanguageValue(rs.getString("text_value_user_language"));
				TranslationServices.beAwareOfNewValue(val,CharValuesLoader.active_characteristics.get(loop_class_id).get(charIdArrays.get(loop_class_id).indexOf(characteristic_id)));
				val.setNominal_value(rs.getString("nominal_value"));
				val.setMin_value(rs.getString("min_value"));
				val.setMax_value(rs.getString("max_value"));
				val.setNote(rs.getString("note"));
				val.setUom_id(rs.getString("uom_id"));
				val.setAuthor(user_id);
				val.setSource(description_method);
				val.setRule_id(description_rule_id);
				val.setUrl(rs.getString("url_link"));
				val.setDescriptionTime(rs.getTimestamp("description_time"));
				val.setManually_Reviewed(rs.getBoolean("manually_reviewed"));
				try{
					val.setParentChar(CharValuesLoader.active_characteristics.get(loop_class_id).get(charIdArrays.get(loop_class_id).indexOf(characteristic_id)));
				}catch(Exception V) {
					System.out.println("Couldn't set parent for char "+characteristic_id+" in class "+loop_class_id);
				}
				knownValues.add(val);
				i+=1;
				indexedKnownValues.put(val.getValue_id(), i);

				CharDescriptionRow row = CharItemFetcher.allRowItems.get(CharItemFetcher.indexedRowItems.get(item_id));
				//row.allocateDataField(loop_class_id,tablePane_CharClassif.active_characteristics.get(loop_class_id).size());
				try{
					row.getData(loop_class_id).put(characteristic_id,val);
				}catch(Exception V) {
					System.out.println("Couldn't set data for char "+characteristic_id+" in class "+loop_class_id);
				}


			}

			rs.close();
			stmt.close();
			conn.close();
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



	public static void updateRuntimeDataForItem(CharDescriptionRow r, String classSegment, String charId, CaracteristicValue value) {
		if(r.getClass_segment_string().contains(classSegment)) {
			r.getData(classSegment).put(charId,value);
		}
	}



	public static void storeItemDatafromScreen(int idx, Char_description parent) {
		CharDescriptionRow row = parent.tableController.itemArray.get(idx);
		String class_id = row.getClass_segment_string().split("&&&")[0];
		ArrayList<ClassCaracteristic> chars = CharValuesLoader.active_characteristics.get(class_id);
		ClassCaracteristic active_char = chars.get(parent.tableController.selected_col%chars.size());

		CaracteristicValue tmp = new CaracteristicValue();
		tmp.setParentChar(active_char);
		if(active_char.getIsNumeric()) {
			tmp.setNominal_value(WordUtils.trimTextField(parent.value_field.getText()));
			if(active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
				Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->u.toString().equals(WordUtils.trimTextField(parent.uom_field.getText()))).findAny();
				if(uom.isPresent()) {
					tmp.setUom_id(uom.get().getUom_id());
				}
				tmp.setMin_value(WordUtils.trimTextField(parent.min_field_uom.getText()));
				tmp.setMax_value(WordUtils.trimTextField(parent.max_field_uom.getText()));
			}else {
				tmp.setMin_value(WordUtils.trimTextField(parent.min_field.getText()));
				tmp.setMax_value(WordUtils.trimTextField(parent.max_field.getText()));
			}
		}else {
			if(parent.value_field.getText()!=null && parent.value_field.getText().replace(" ", "").length()>0) {
				tmp.setDataLanguageValue(WordUtils.trimTextField(parent.value_field.getText()));
			}else {
				tmp.setDataLanguageValue(null);
			}

			if(parent.translated_value_field.getText()!=null && parent.translated_value_field.getText().replace(" ", "").length()>0) {
				tmp.setUserLanguageValue(WordUtils.trimTextField(parent.translated_value_field.getText()));
			}else {
				tmp.setUserLanguageValue(null);
			}
		}
		tmp.setNote(WordUtils.trimTextField(parent.note_field_uom.getText()));
		tmp.setSource(DataInputMethods.MANUAL);
		tmp.setAuthor(parent.account.getUser_id());
		tmp.setManually_Reviewed(true);
		parent.assignValueOnSelectedItems(tmp);
		ExternalSearchServices.manualValueInput();
		parent.lastInputValue = tmp.shallowCopy(parent.account);

	}




	public static void updateDefaultCharValue(int selectedRowIndex, Char_description parent) {
		CharDescriptionRow row = parent.tableController.itemArray.get(selectedRowIndex);
		ClassCaracteristic active_char = CharItemFetcher.defaultCharValues.get(selectedRowIndex).getKey();

		CaracteristicValue tmp = new CaracteristicValue();
		tmp.setValue_id(row.getItem_id());
		tmp.setParentChar(active_char);
		if(active_char.getIsNumeric()) {
			tmp.setNominal_value(WordUtils.trimTextField(parent.value_field.getText()));
			if(active_char.getAllowedUoms()!=null && active_char.getAllowedUoms().size()>0) {
				Optional<UnitOfMeasure> uom = UnitOfMeasure.RunTimeUOMS.values().parallelStream().filter(u->u.toString().equals(parent.uom_field.getText())).findAny();
				if(uom.isPresent()) {
					tmp.setUom_id(uom.get().getUom_id());
				}
				tmp.setMin_value(WordUtils.trimTextField(parent.min_field_uom.getText()));
				tmp.setMax_value(WordUtils.trimTextField(parent.max_field_uom.getText()));
			}else {
				tmp.setMin_value(WordUtils.trimTextField(parent.min_field.getText()));
				tmp.setMax_value(WordUtils.trimTextField(parent.max_field.getText()));
			}
		}else {
			if(parent.value_field.getText()!=null && parent.value_field.getText().replace(" ", "").length()>0) {
				tmp.setDataLanguageValue(WordUtils.trimTextField(parent.value_field.getText()));
			}else {
				tmp.setDataLanguageValue(null);
			}

			if(parent.translated_value_field.getText()!=null && parent.translated_value_field.getText().replace(" ", "").length()>0) {
				tmp.setUserLanguageValue(WordUtils.trimTextField(parent.translated_value_field.getText()));
			}else {
				tmp.setUserLanguageValue(null);
			}
		}
		tmp.setNote(WordUtils.trimTextField(parent.note_field_uom.getText()));
		tmp.setSource(DataInputMethods.MANUAL);
		tmp.setAuthor(parent.account.getUser_id());
		tmp.setManually_Reviewed(true);

		row.getData(GlobalConstants.DEFAULT_CHARS_CLASS).put(GlobalConstants.DEFAULT_CHARS_CLASS,tmp);
		row.setLong_desc(tmp.getDisplayValue(parent));
		parent.refresh_ui_display();
		parent.tableController.tableGrid.refresh();

		ArrayList<CaracteristicValue> tmpArray = new ArrayList<CaracteristicValue>();
		tmpArray.add(tmp);
		CharDescriptionExportServices.updateDBCaracValuesInPlace(tmpArray,parent.account.getActive_project());
	}




	public static void fetchDefaultCharValues(String activeProject) throws ClassNotFoundException, SQLException {
		CharItemFetcher.defaultCharValues = new ArrayList<Pair<ClassCaracteristic,CaracteristicValue>>();
		Connection conn = Tools.spawn_connection_from_pool();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select * from (select characteristic_id,user_id,description_method,description_rule_id,"
						+ "project_values.value_id,text_value_data_language, text_value_user_language,note from "
						+ "(select * from "+activeProject+".project_characteristics_x_values) relations left join "
						+ ""+activeProject+".project_values on project_values.value_id = relations.value_id) values "
						+ "left join "+activeProject+".project_characteristics on values.characteristic_id = project_characteristics.characteristic_id");


		while(rs.next()) {

			String user_id = rs.getString("user_id");
			String description_method = rs.getString("description_method");
			String description_rule_id = rs.getString("description_rule_id");
			CaracteristicValue val = new CaracteristicValue();
			val.setValue_id(rs.getString("value_id"));
			val.setDataLanguageValue(rs.getString("text_value_data_language"));
			val.setUserLanguageValue(rs.getString("text_value_user_language"));
			val.setNote(rs.getString("note"));
			val.setAuthor(user_id);
			val.setSource(description_method);
			val.setRule_id(description_rule_id);
			val.setManually_Reviewed(false);

			ClassCaracteristic carac = new ClassCaracteristic();
			carac.setCharacteristic_id(rs.getString("characteristic_id"));
			carac.setCharacteristic_name(rs.getString("characteristic_name"));
			carac.setCharacteristic_name_translated(rs.getString("characteristic_name_translated"));
			carac.setIsNumeric(rs.getBoolean("isNumeric"));
			carac.setIsTranslatable(rs.getBoolean("isTranslatable"));


			try{
				val.setParentChar(carac);
			}catch(Exception V) {
				System.out.println("Couldn't set parent for char "+carac.getCharacteristic_id());
			}
			CharItemFetcher.defaultCharValues.add(new Pair<ClassCaracteristic,CaracteristicValue>(carac,val));
			TranslationServices.beAwareOfNewValue(val,carac);


		}
		rs.close();
		stmt.close();
		conn.close();
	}

	public static ArrayList<ClassCaracteristic> returnSortedCopyOfClassCharacteristic(String classSegment) {

		ArrayList<ClassCaracteristic> tmp = new ArrayList<ClassCaracteristic>(active_characteristics.get(classSegment));
		tmp.sort(new Comparator<ClassCaracteristic>() {
			@Override
			public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
				return o1.getSequence().compareTo(o2.getSequence());
			}
		});
		return  tmp;

	}

}
