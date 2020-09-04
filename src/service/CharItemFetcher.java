package service;

import controllers.paneControllers.TablePane_CharClassif;
import javafx.util.Pair;
import model.CaracteristicValue;
import model.CharDescriptionRow;
import model.ClassCaracteristic;
import model.GlobalConstants;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CharItemFetcher {
	
	public static ArrayList<CharDescriptionRow> allRowItems;
	public static HashMap<String,Integer> indexedRowItems = new HashMap<String,Integer>();
	public static HashMap<String, String> classifiedItems;
	private static int fakeItemIdx;
	public static ArrayList<Pair<ClassCaracteristic,CaracteristicValue>> defaultCharValues;

	
	public static void fetchAllItems(String active_project, TablePane_CharClassif tablePane_CharClassif) throws ClassNotFoundException, SQLException {
		if(allRowItems!=null) {
			
		}else {
			System.out.println("Fetching all items");
			Tools.userID2Author = Tools.get_user_names();
			allRowItems = new ArrayList<CharDescriptionRow>();
			Connection conn = Tools.spawn_connection();
			PreparedStatement stmt;
			ResultSet rs;
			
			stmt = conn.prepareStatement("select item_id, client_item_number, short_description,short_description_translated, long_description,long_description_translated,material_group,pre_classification from "+active_project+".project_items");
			rs = stmt.executeQuery();
			
			int i=-1;
			while(rs.next()) {
				try {
					//We don't have the associated char data length yet
					//String loop_class_id = tablePane_CharClassif.classifiedItems.get(rs.getString("item_id")).split("&&&")[4];
					//CharDescriptionRow tmp = new CharDescriptionRow(loop_class_id,tablePane_CharClassif.active_characteristics.get(loop_class_id).size());
					CharDescriptionRow tmp = new CharDescriptionRow();
					tmp.setParent(tablePane_CharClassif.Parent);
					tmp.setItem_id(rs.getString("item_id"));
					i+=1;
					indexedRowItems.put(tmp.getItem_id(), i);
					tmp.setClient_item_number(rs.getString("client_item_number"));
					tmp.setShort_desc(rs.getString("short_description"));
					tmp.setShort_desc_translated(rs.getString("short_description_translated"));
					tmp.setLong_desc(rs.getString("long_description"));
					tmp.setLong_desc_translated(rs.getString("long_description_translated"));
					tmp.setMaterial_group(rs.getString("material_group"));
					tmp.setPreclassif(rs.getString("pre_classification"));
					
					String loop_class_segment = CharItemFetcher.classifiedItems.get(rs.getString("item_id"));
					String loop_class_id = loop_class_segment.split("&&&")[4];
					String loop_class_name = loop_class_segment.split("&&&")[1];
					String loop_class_number = loop_class_segment.split("&&&")[0];
					tmp.setClass_segment_string(loop_class_id+"&&&"+loop_class_name+"&&&"+loop_class_number);
					
					allRowItems.add(tmp);
				}catch(Exception V) {
					V.printStackTrace(System.err);
				}
				
			}
			rs.close();
			stmt.close();
			conn.close();
		}
			
	}
	
	public static void generateDefaultCharEditingItems(TablePane_CharClassif tableController) throws ClassNotFoundException, SQLException {
		if(CharItemFetcher.defaultCharValues!=null) {
			
		}else{
			CharValuesLoader.fetchDefaultCharValues(tableController.Parent.account.getActive_project());
		}
		System.out.println("Generating fake items ");
		try {
			tableController.itemArray.clear();
			System.gc();
		}catch(Exception V) {
			tableController.itemArray = new ArrayList<CharDescriptionRow>();
			System.gc();
		}
		fakeItemIdx = 0;
		CharItemFetcher.defaultCharValues.forEach(p->{
			fakeItemIdx+=1;
			CharDescriptionRow fakeItem = new CharDescriptionRow();
			fakeItem.setItem_id(p.getValue().getValue_id());
			fakeItem.setClient_item_number("Dummy_item_"+String.valueOf(fakeItemIdx));
			fakeItem.setClass_segment_string(GlobalConstants.DEFAULT_CHARS_CLASS+"&&&"+GlobalConstants.DEFAULT_CHARS_CLASS+"&&&"+GlobalConstants.DEFAULT_CHARS_CLASS);
			fakeItem.setShort_desc(p.getKey().getCharacteristic_name());
			fakeItem.setLong_desc(p.getValue().getDisplayValue(tableController.Parent));
			fakeItem.setParent(tableController.Parent);
			fakeItem.allocateDataField(GlobalConstants.DEFAULT_CHARS_CLASS);
			fakeItem.getData(GlobalConstants.DEFAULT_CHARS_CLASS).put(GlobalConstants.DEFAULT_CHARS_CLASS,p.getValue());
			tableController.itemArray.add(fakeItem);
			
		});
		
	}
	public static void generateItemArray(List<String> classItemIDs, TablePane_CharClassif tablePane_CharClassif) {

		try {
			tablePane_CharClassif.itemArray.clear();
			indexedRowItems.entrySet().parallelStream().filter(e->classItemIDs.contains(e.getKey()))
			.forEach(e->tablePane_CharClassif.itemArray.add(allRowItems.get(e.getValue())));
			System.gc();
		}catch(Exception V) {
			tablePane_CharClassif.itemArray = new ArrayList<CharDescriptionRow>();
			indexedRowItems.entrySet().parallelStream().filter(e->classItemIDs.contains(e.getKey()))
			.forEach(e->tablePane_CharClassif.itemArray.add(allRowItems.get(e.getValue())));
			System.gc();
		}
		
	}

	public static void allocateDataFieldForClassOnItems(String target_class_id, Set<CharDescriptionRow> itemList,TablePane_CharClassif tablePane_CharClassif) {
		List<String> itemListIDs = itemList.stream().
				map(i -> i.getItem_id()).collect(Collectors.toList());
		
		indexedRowItems.entrySet().parallelStream().filter(e->itemListIDs.contains(e.getKey()))
		.forEach(e->{
			allRowItems.get(e.getValue()).allocateDataField(target_class_id);
		});
		
		System.out.println("Class change :: allocated data fields for "+itemList.size()+" items");
	}

	public static void initClassDataFields(TablePane_CharClassif tablePane_CharClassif) {
		allRowItems.forEach(r->{
			String item_class_id = classifiedItems.get(r.getItem_id()).split("&&&")[4];
			r.allocateDataField(item_class_id);
			
		});
	}

	
	
}
