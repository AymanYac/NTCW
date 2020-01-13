package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import controllers.paneControllers.TablePane_CharClassif;
import model.CharDescriptionRow;
import transversal.generic.Tools;

public class CharItemFetcher {
	
	public static ArrayList<CharDescriptionRow> allRowItems;
	public static HashMap<String,Integer> indexedRowItems = new HashMap<String,Integer>();
	public static HashMap<String, String> classifiedItems;
	
	
	public static void fetchAllItems(String active_project, TablePane_CharClassif tablePane_CharClassif) throws ClassNotFoundException, SQLException {
		if(allRowItems!=null) {
			
		}else {
			System.out.println("Fetching all items");
			allRowItems = new ArrayList<CharDescriptionRow>();
			Connection conn = Tools.spawn_connection();
			PreparedStatement stmt;
			ResultSet rs;
			
			stmt = conn.prepareStatement("select item_id, client_item_number, short_description,short_description_translated, long_description,long_description_translated,material_group from "+active_project+".project_items");
			rs = stmt.executeQuery();
			
			int i=-1;
			while(rs.next()) {
				//We don't have the associated char data length yet
				//String loop_class_id = tablePane_CharClassif.classifiedItems.get(rs.getString("item_id")).split("&&&")[4];
				//CharDescriptionRow tmp = new CharDescriptionRow(loop_class_id,tablePane_CharClassif.active_characteristics.get(loop_class_id).size());
				CharDescriptionRow tmp = new CharDescriptionRow();
				tmp.setItem_id(rs.getString("item_id"));
				i+=1;
				indexedRowItems.put(tmp.getItem_id(), i);
				tmp.setClient_item_number(rs.getString("client_item_number"));
				tmp.setShort_desc(rs.getString("short_description"));
				tmp.setShort_desc_translated(rs.getString("short_description_translated"));
				tmp.setLong_desc(rs.getString("long_description"));
				tmp.setLong_desc_translated(rs.getString("long_description_translated"));
				tmp.setMaterial_group(rs.getString("material_group"));
				
				String loop_class_segment = CharItemFetcher.classifiedItems.get(rs.getString("item_id"));
				String loop_class_id = loop_class_segment.split("&&&")[4];
				String loop_class_name = loop_class_segment.split("&&&")[1];
				String loop_class_number = loop_class_segment.split("&&&")[0];
				tmp.setClass_segment(loop_class_id+"&&&"+loop_class_name+"&&&"+loop_class_number);
				
				allRowItems.add(tmp);
			}
			rs.close();
			stmt.close();
			conn.close();
		}
			
	}

	public static void loadItemArray(List<String> classItemIDs, TablePane_CharClassif tablePane_CharClassif) {
		System.out.println("Retrieving "+classItemIDs.size());
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
			allRowItems.get(e.getValue()).allocateDataField(target_class_id,tablePane_CharClassif.active_characteristics.get(target_class_id).size());
		});
		
		System.out.println("Class change :: allocated data fields for "+itemList.size()+" items");
	}

	public static void initClassDataFields(TablePane_CharClassif tablePane_CharClassif) {
		System.out.println("Initializing all data fields");
		allRowItems.forEach(r->{
			String item_class_id = classifiedItems.get(r.getItem_id()).split("&&&")[4];
			r.allocateDataField(item_class_id,tablePane_CharClassif.active_characteristics.get(item_class_id).size());
			
		});
	}

	
}
