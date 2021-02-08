package service;

import model.GlobalConstants;
import model.ItemDispatcherRow;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;

import java.sql.*;
import java.util.ArrayList;

public class ItemDispatcher {

	private ArrayList<ItemDispatcherRow> itemList;
	private Integer last_row_number = 0;
	private Integer last_batch_number = -1;
	private String activeProject;
	private String orderClause; 
	
	public ItemDispatcher(String active_project) throws ClassNotFoundException, SQLException {
		
		this.activeProject = active_project;
		this.orderClause = "";
		
		
		Connection conn = Tools.spawn_connection_from_pool();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select count(item_id) from "+active_project+".project_items where item_id not in (select distinct item_id from "+active_project+".project_classification_event)");
		rs.next();
		rs.getInt(1);
		load_next_batch();
		rs.close();
		stmt.close();
		
		
	}

	private void load_next_batch() throws SQLException, ClassNotFoundException {
		Connection conn = Tools.spawn_connection_from_pool();
		
		PreparedStatement ps = conn.prepareStatement("select * from "+activeProject+".project_items where row_number>"+this.last_row_number.toString()+" and item_id not in (select distinct item_id from "+activeProject+".project_classification_event) "+(orderClause.length()>0?orderClause:""+"limit "+GlobalConstants.MANUAL_SEGMENT_SIZE.toString()));

		;
		ResultSet rs = ps.executeQuery();
		try {
			this.itemList.clear();
			this.itemList = null;
		}catch(Exception V) {
			
		}
		this.itemList = new ArrayList<ItemDispatcherRow>(GlobalConstants.MANUAL_SEGMENT_SIZE);
		while(rs.next()) {
			ItemDispatcherRow dsrow = new ItemDispatcherRow();
			dsrow.setItem_id(rs.getString("item_id"));
			dsrow.setClient_item_number(rs.getString("client_item_number"));
			dsrow.setShort_description(rs.getString("short_description"));
			dsrow.setLong_description(rs.getString("long_description"));
			dsrow.setShort_description_translated(rs.getString("short_description_translated"));
			dsrow.setLong_description_translated(rs.getString("long_description_translated"));
			dsrow.setMaterial_group(rs.getString("material_group"));
			dsrow.setPreclassifiation(rs.getString("pre_classification"));
			dsrow.setRow_number(rs.getInt("row_number"));
			itemList.add(dsrow);
		}
		;
		;
		rs.close();
		ps.close();
		conn.close();
		
	}

	public ItemDispatcherRow getNextItem() {
		if(last_batch_number==GlobalConstants.MANUAL_SEGMENT_SIZE-1) {
			try {
				load_next_batch();
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			last_batch_number = 0;
			last_row_number = itemList.get(last_batch_number).getRow_number();
			return itemList.get(last_batch_number);
			
		}else {
			last_batch_number += 1;
			ItemDispatcherRow ret = null;
			try{
				last_row_number = itemList.get(last_batch_number).getRow_number();
				ret = itemList.get(last_batch_number);
			}catch(Exception V) {
				ConfirmationDialog.show("Classification complete", "All items contained within this project have successfully been classified", "OK");
	    		return null;
			}
			return ret;
		}
	}

}
