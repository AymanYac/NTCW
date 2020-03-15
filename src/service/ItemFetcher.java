package service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import controllers.Manual_classif;
import javafx.scene.control.TableView;
import model.GlobalConstants;
import model.DataInputMethods;
import model.GenericClassRule;
import model.ItemFetcherRow;
import model.ObservableDeque;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class ItemFetcher {
	
	private String active_project;
	public ArrayList<ItemFetcherRow> topList;
	public ArrayList<ItemFetcherRow> bottomList;
	public ArrayList<ItemFetcherRow> currentList_STATIC;
	public Deque<ItemFetcherRow> currentList_DYNAMIC;
	public Deque<ItemFetcherRow> nextList;
	
	public ArrayList<String> orderedItems;
	private Integer projectCardinality;
	private Integer projectGranularity;
	public HashMap<String, String> classifiedItems;
	public HashMap<String, String> CID2NAME = new HashMap<String,String>();
	public HashMap<String, String> ONLINE_LABELS = new HashMap<String,String>();
	private List<String> dw_words;
	private List<String> for_words;
	private HashMap<String, ArrayList<String>> items_x_rules;
	
	
	
	public ItemFetcher(String active_project, Manual_classif parent_controller) throws ClassNotFoundException, SQLException {
		super();
		this.active_project = active_project;
		projectCardinality = Tools.count_project_cardinality(active_project);
		projectGranularity = Tools.get_project_granularity(active_project);
		Tools.userID2Author = Tools.get_user_names();
		
		if(GlobalConstants.MANUAL_PREORDER) {
			orderedItems = new ArrayList<String>(projectCardinality);
		}
		classifiedItems = new HashMap<String,String>();
		
		
		
		refresh_item_order_and_class(null);
		
		if(GlobalConstants.MANUAL_FETCH_ALL ) {
			
			currentList_STATIC = new ArrayList<ItemFetcherRow>(projectCardinality);
			fill_array( currentList_STATIC,parent_controller);
			
		}else if(GlobalConstants.MANUAL_PREORDER) {
			topList = new ArrayList<ItemFetcherRow>(GlobalConstants.MANUAL_SEGMENT_SIZE);
			bottomList = new ArrayList<ItemFetcherRow>(GlobalConstants.MANUAL_SEGMENT_SIZE);
			currentList_DYNAMIC = new LinkedList<ItemFetcherRow>();
			nextList = new LinkedList<ItemFetcherRow>();
			
			//initialize_array(null,topList);
			//initialize_array(orderedItems.get(orderedItems.size()-1),bottomList);
			//initialize_array(null,(List<ItemFetcherRow>) nextList);
			initialize_array(orderedItems.get(orderedItems.size()-1888),(List<ItemFetcherRow>) currentList_DYNAMIC);
			//initialize_array(null,(List<ItemFetcherRow>) currentList);
			
		}
		
		
		
	}
	
	
	private void fill_array(ArrayList<ItemFetcherRow> targetList, Manual_classif parent_controller) throws ClassNotFoundException, SQLException {
		
		for_words = Tools.get_project_for_words(active_project);
		dw_words = Tools.get_project_dw_words(active_project);
		
		parent_controller.proposer.for_words = for_words;
		parent_controller.proposer.dw_words = dw_words;
		
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		
		if(GlobalConstants.MANUAL_PREORDER) {
			;
			int i=0;
			String joinStatement = "JOIN (VALUES";
			for(String aid:orderedItems) {
				i+=1;
				if(i==1) {
					joinStatement+=" ('"+aid+"',"+i+")";
				}else {
					joinStatement+=", ('"+aid+"',"+i+")";
				}
				if(i%10000==0) {
					;
				}
			}
			joinStatement +=") as x(aid,display_order)";

			rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
					+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
			
		}else {
			
			//Legacy rule query : Rules are associated uniquely to an item
			/*rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification "
					+ "from (select * from "+active_project+".project_items) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id");
			;*/
			//Now rules are all fetched in different query in order to fill the rules pane
			rs = stmt.executeQuery("select item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification "
					+ "from "+active_project+".project_items");
			;
			
		}
		
		
		while(rs.next()) {
			
			ItemFetcherRow tmp = new ItemFetcherRow();
			tmp.setItem_id(rs.getString("item_id"));
			tmp.setClient_item_number(rs.getString("client_item_number"));
			tmp.setShort_description(rs.getString("short_description"));
			tmp.setShort_description_translated(rs.getString("short_description_translated"));
			tmp.setLong_description(rs.getString("long_description"));
			tmp.setLong_description_translated(rs.getString("long_description_translated"));
			tmp.setMaterial_group(rs.getString("material_group"));
			tmp.setPreclassifiation(rs.getString("pre_classification"));
			tmp.setOnline_preclassif(ONLINE_LABELS.get( rs.getString("item_id")) );
			//tmp.setRule_id(rs.getString("rule_id"));
			//tmp.setRule_description(rs.getString("rule_application_description_form"));
			try{
				//tmp.setSegment_number(classifiedItems.get(tmp.getItem_id()).split("&&&")[0]);
				//tmp.setSegment_id(classifiedItems.get(tmp.getItem_id()).split("&&&")[4]);
				//tmp.setSegment_name(classifiedItems.get(tmp.getItem_id()).split("&&&")[1]);
				if(classifiedItems.get(tmp.getItem_id()).split("&&&")[2].equals(DataInputMethods.MANUAL)){
					tmp.setManual_segment_number( classifiedItems.get(tmp.getItem_id()).split("&&&")[0] );
					tmp.setManual_segment_id( classifiedItems.get(tmp.getItem_id()).split("&&&")[4] );
					tmp.setManual_segment_name( classifiedItems.get(tmp.getItem_id()).split("&&&")[1] );
					tmp.setSource_Manual(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
					tmp.setAuthor_Manual(Tools.userID2Author.get(classifiedItems.get(tmp.getItem_id()).split("&&&")[3]));
					
				}
				if(classifiedItems.get(tmp.getItem_id()).split("&&&")[2].equals(DataInputMethods.PROJECT_SETUP_UPLOAD)){
					tmp.setUpload_segment_number( classifiedItems.get(tmp.getItem_id()).split("&&&")[0] );
					tmp.setUpload_segment_id( classifiedItems.get(tmp.getItem_id()).split("&&&")[4] );
					tmp.setUpload_segment_name( classifiedItems.get(tmp.getItem_id()).split("&&&")[1] );
					tmp.setSource_Upload(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
					tmp.setAuthor_Upload(Tools.userID2Author.get(classifiedItems.get(tmp.getItem_id()).split("&&&")[3]));
					
				}
				if(classifiedItems.get(tmp.getItem_id()).split("&&&")[2].equals(DataInputMethods.USER_CLASSIFICATION_RULE)
						||
						classifiedItems.get(tmp.getItem_id()).split("&&&")[2].equals(DataInputMethods.BINARY_CLASSIFICATION)){
					tmp.setRule_Segment_number( classifiedItems.get(tmp.getItem_id()).split("&&&")[0] );
					tmp.setRule_Segment_id( classifiedItems.get(tmp.getItem_id()).split("&&&")[4] );
					tmp.setRule_Segment_name( classifiedItems.get(tmp.getItem_id()).split("&&&")[1] );
					tmp.setSource_Rules(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
					tmp.setAuthor_Rules(Tools.userID2Author.get(classifiedItems.get(tmp.getItem_id()).split("&&&")[3]));
					tmp.setRule_id_Rules(classifiedItems.get(tmp.getItem_id()).split("&&&")[5]);
					tmp.setRule_description_Rules(classifiedItems.get(tmp.getItem_id()).split("&&&")[5]);
					QueryFormater.ADD_UPLOAD_ITEM_CLASS(tmp, projectGranularity, active_project);
				}
				
				
				String key;
				if(tmp.getLong_description()!=null) {
					key = tmp.getLong_description().split(" ")[0];
				}else {
					key = tmp.getShort_description().split(" ")[0];
				}
				String val = classifiedItems.get(tmp.getItem_id()).split("&&&")[4]+"&&&"+tmp.getDisplay_segment_name();
				parent_controller.proposer.addClassifiedFW(key,val);
				
				
				if(tmp.getMaterial_group()!=null) {
					key = tmp.getMaterial_group();
					val = classifiedItems.get(tmp.getItem_id()).split("&&&")[4]+"&&&"+tmp.getDisplay_segment_name();
				parent_controller.proposer.addClassifiedMG(key,val);
				}
				
				
				
				
			}catch(Exception V) {

			}
			
			if(items_x_rules.containsKey(tmp.getItem_id())) {
				tmp.itemRules = items_x_rules.get(tmp.getItem_id());
			}
			
			
			
			String desc;
			String key;
			String val;
			if(tmp.getLong_description()!=null) {
				desc = tmp.getLong_description();
				
			}else {
				desc = tmp.getShort_description();
			}
			
			for(String fw:for_words) {
				try {
					key = desc.toUpperCase().split(fw.toUpperCase()+" ")[1];
					key = WordUtils.getSearchWords(key);
					tmp.addF1(key.split(" ")[0].toUpperCase());//#
					tmp.addF1F2(key.toUpperCase());//#
					val = classifiedItems.get(tmp.getItem_id()).split("&&&")[4]+"&&&"+tmp.getDisplay_segment_name();
					parent_controller.proposer.addClassifiedFor(key,val);
					
				}catch(Exception V) {
					continue;
				}
			}
			
			for(String dw:dw_words) {
				try {
						if(desc.toUpperCase().contains(dw.toUpperCase())){
							tmp.setDWG(true);//#
							val = classifiedItems.get(tmp.getItem_id()).split("&&&")[4]+"&&&"+tmp.getDisplay_segment_name();
							parent_controller.proposer.addClassifiedDW(dw,val);
							
						}else {
							continue;
						}
					}catch(Exception V) {
					continue;
				}
			}
			
			
			
			targetList.add(tmp);
			tmp = null;
		}
		rs.close();
		stmt.close();
		conn.close();
	}


	public void add_bottom(int numLines, TableViewExtra tvX, ObservableDeque oq, TableView tableGrid) throws SQLException, ClassNotFoundException {
		
		//Legacy scroll code
		/*
		;
		
		int start = current_end;
		int end = current_end + numLines +1;
		
		List<String> Items = orderedItems.subList(start, end);
		
		int i=0;
		String joinStatement = "JOIN (VALUES";
		for(String aid:Items) {
			i+=1;
			if(i==1) {
				joinStatement+=" ('"+aid+"',"+i+")";
			}else {
				joinStatement+=", ('"+aid+"',"+i+")";
			}
		}
		joinStatement +=") as x(aid,display_order)";
		
		ResultSet rs;
		Statement stmt;
		try {
			stmt = live_connection.createStatement();
			rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
					+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
			
		}catch(Exception V) {
			live_connection = Tools.spawn_connection();
			stmt = live_connection.createStatement();
			rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
					+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
			
		}
		
		while(rs.next()) {
			ItemFetcherRow tmp = new ItemFetcherRow();
			tmp.setItem_id(rs.getString("item_id"));
			tmp.setClient_item_number(rs.getString("client_item_number")+"::"+oq.size());
			
			tmp.setShort_description(rs.getString("short_description"));
			tmp.setShort_description_translated(rs.getString("short_description_translated"));
			tmp.setLong_description(rs.getString("long_description"));
			tmp.setLong_description_translated(rs.getString("long_description_translated"));
			tmp.setMaterial_group(rs.getString("material_group"));
			tmp.setPreclassifiation(rs.getString("pre_classification"));
			tmp.setRule_id(rs.getString("rule_id"));
			tmp.setRule_description(rs.getString("rule_application_description_form"));
			try{
				
				tmp.setSegment_id(classifiedItems.get(tmp.getItem_id()).split("&&&")[0]);
				tmp.setSegment_name(classifiedItems.get(tmp.getItem_id()).split("&&&")[1]);
				tmp.setSource(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
			}catch(Exception V) {
				
			}
			oq.addLast(tmp);
			oq.removeFirst();
			
		}
		
		//for(int j=0;j<numLines;j++) {
		//	oq.removeFirst();
		//}
		
		current_start = start;
		current_end = end;
		//tableGrid.getSelectionModel().select( tableGrid.getSelectionModel().getSelectedIndex() - numLines);
		rs.close();
		stmt.close();
		*/
	}

	public void add_top(int numLines, TableViewExtra tvX, ObservableDeque oq, TableView tableGrid) throws ClassNotFoundException, SQLException {
		//Legacy scroll code
		/*
		;

		int start = current_start - numLines - 1;
		int end = current_start;
		
		if(end<=start) {
			return;
		}
		
		List<String> Items = orderedItems.subList(start, end);
		Collections.reverse(Items);
		int i=0;
		String joinStatement = "JOIN (VALUES";
		for(String aid:Items) {
			i+=1;
			if(i==1) {
				joinStatement+=" ('"+aid+"',"+i+")";
			}else {
				joinStatement+=", ('"+aid+"',"+i+")";
			}
		}
		joinStatement +=") as x(aid,display_order)";
		
		ResultSet rs;
		Statement stmt;
		try {
			stmt = live_connection.createStatement();
			rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
					+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
			
		}catch(Exception V) {
			live_connection = Tools.spawn_connection();
			
			stmt = live_connection.createStatement();
			rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
					+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
					+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
			
		}
		
		while(rs.next()) {
			ItemFetcherRow tmp = new ItemFetcherRow();
			tmp.setItem_id(rs.getString("item_id"));
			tmp.setClient_item_number(rs.getString("client_item_number")+"::"+oq.size());
			
			tmp.setShort_description(rs.getString("short_description"));
			tmp.setShort_description_translated(rs.getString("short_description_translated"));
			tmp.setLong_description(rs.getString("long_description"));
			tmp.setLong_description_translated(rs.getString("long_description_translated"));
			tmp.setMaterial_group(rs.getString("material_group"));
			tmp.setPreclassifiation(rs.getString("pre_classification"));
			tmp.setRule_id(rs.getString("rule_id"));
			tmp.setRule_description(rs.getString("rule_application_description_form"));
			try{
				tmp.setSegment_id(classifiedItems.get(tmp.getItem_id()).split("&&&")[0]);
				tmp.setSegment_name(classifiedItems.get(tmp.getItem_id()).split("&&&")[1]);
				tmp.setSource(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
			}catch(Exception V) {
				
			}
			oq.addFirst(tmp);
			oq.removeLast();
			
		}
		
		//for(int j=0;j<numLines;j++) {
		//	oq.removeFirst();
		//}
		
		current_start = start;
		current_end = end;
		//tableGrid.getSelectionModel().select( tableGrid.getSelectionModel().getSelectedIndex() - numLines);
		rs.close();
		stmt.close();
		*/
	}


	private void initialize_array(String central_id,List<ItemFetcherRow> targetList) throws ClassNotFoundException, SQLException {
		//Legacy scroll code
		/*
		int central_index = 0;
		if(central_id!=null) {
			central_index = orderedItems.indexOf(central_id);
			
		}
		int gap_to_left = central_index - GlobalConstants.MANUAL_SEGMENT_SIZE/2;
		;
		int start = 0;
		int end = 0;
		if(gap_to_left<0) {
			start = 0;
			end = Math.min(orderedItems.size(), GlobalConstants.MANUAL_SEGMENT_SIZE);
		}
		if(gap_to_left>0) {
			start = gap_to_left;
			end = gap_to_left + GlobalConstants.MANUAL_SEGMENT_SIZE;
			if(GlobalConstants.MANUAL_SEGMENT_SIZE + gap_to_left>orderedItems.size()) {
				end = orderedItems.size();
				start = Math.max(0, end - GlobalConstants.MANUAL_SEGMENT_SIZE);
			}
		}
		
		current_start = start;
		current_end = end;
		
		List<String> Items = orderedItems.subList(start, end);
		Connection conn = Tools.spawn_connection();
		int i=0;
		String joinStatement = "JOIN (VALUES";
		for(String aid:Items) {
			i+=1;
			if(i==1) {
				joinStatement+=" ('"+aid+"',"+i+")";
			}else {
				joinStatement+=", ('"+aid+"',"+i+")";
			}
		}
		joinStatement +=") as x(aid,display_order)";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select items.item_id,client_item_number,short_description,short_description_translated,long_description,"
				+ "long_description_translated,material_group,pre_classification,rule_id,rule_application_description_form "
				+ "from (select * from "+active_project+".project_items "+joinStatement+" on x.aid = project_items.item_id) as items left join "+active_project+".project_items_x_rules on items.item_id = project_items_x_rules.item_id order by display_order");
		
		while(rs.next()) {
			ItemFetcherRow tmp = new ItemFetcherRow();
			tmp.setItem_id(rs.getString("item_id"));
			tmp.setClient_item_number(rs.getString("client_item_number"));
			tmp.setShort_description(rs.getString("short_description"));
			tmp.setShort_description_translated(rs.getString("short_description_translated"));
			tmp.setLong_description(rs.getString("long_description"));
			tmp.setLong_description_translated(rs.getString("long_description_translated"));
			tmp.setMaterial_group(rs.getString("material_group"));
			tmp.setPreclassifiation(rs.getString("pre_classification"));
			tmp.setRule_id(rs.getString("rule_id"));
			tmp.setRule_description(rs.getString("rule_application_description_form"));
			try{
				tmp.setSegment_id(classifiedItems.get(tmp.getItem_id()).split("&&&")[0]);
				tmp.setSegment_name(classifiedItems.get(tmp.getItem_id()).split("&&&")[1]);
				tmp.setSource(classifiedItems.get(tmp.getItem_id()).split("&&&")[2]);
			}catch(Exception V) {
				
			}
			targetList.add(tmp);
			
		}
		rs.close();
		stmt.close();
		conn.close();
		*/
	}



	private void refresh_item_order_and_class(ArrayList<String> aid_sequence) throws ClassNotFoundException, SQLException {
		
		String joinStatement = "";
		try {
			int i=0;
			joinStatement = " JOIN (VALUES";
			for(String aid:aid_sequence) {
				i+=1;
				if(i==1) {
					joinStatement+=" ('"+aid+"',"+i+")";
				}else {
					joinStatement+=", ('"+aid+"',"+i+")";
				}
			}
			joinStatement +=") as x(aid,display_order) on extract.item_id = x.aid order by display_order";
		}catch(Exception V) {
			joinStatement = "";
		}
		
		if(GlobalConstants.MANUAL_CLASSIF_LOAD_LATEST_WITH_UPLOAD_PRIORITY) {
			classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(joinStatement,this.projectGranularity,this.active_project);
		}else {
			classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_NO_UPLOAD_PRIORITY(joinStatement,this.projectGranularity,this.active_project);
		}
		
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		ResultSet rs;
		
		rs = st.executeQuery("select segment_id,level_"+projectGranularity+"_name_translated from "+active_project+".project_segments");
		while(rs.next()) {
			CID2NAME.put(rs.getString(1),rs.getString(2));
		}
		rs.close();
		
		rs = st.executeQuery("select item_id,online_label from public_ressources.online_models where project_id ='"+active_project+"' order by label_time asc");
		while(rs.next()) {
			ONLINE_LABELS.put(rs.getString("item_id"),rs.getString("online_label"));
		}
		rs.close();
		
		rs = st.executeQuery("select rule_id, main, application, complement, material_group," + 
				"pre_classification, drawing, class_id, active_status from "+active_project+".project_rules");
		System.out.println("select rule_id, main, application, complement, material_group," + 
				"pre_classification, drawing, class_id, active_status from "+active_project+".project_rules");
		while(rs.next()) {
			GenericClassRule gr = new GenericClassRule();
			gr.setMain(rs.getString("main"));
			gr.setApp(rs.getString("application"));
			gr.setComp(rs.getString("complement"));
			gr.setMg(rs.getString("material_group"));
			gr.setPc(rs.getString("pre_classification"));
			gr.setDwg(rs.getBoolean("drawing"));
			gr.classif=new ArrayList<> ( Arrays.asList( rs.getString("class_id").split("&&&") ) );
			gr.active=rs.getBoolean("active_status");
			ItemFetcherRow.staticRules.put(gr.toString(), gr);
			if(rs.getString("rule_id").equals("CLAPET (PILOTE)")) {
				System.out.println(rs.getString("item_id"));
				System.out.println(gr.toString());
				System.out.println(gr.toString().equals(rs.getString("rule_id")));
			}
		}
		rs.close();
		
		items_x_rules = new HashMap<String,ArrayList<String>>();
		rs = st.executeQuery("select item_id,rule_id from "+active_project+".project_items_x_rules");
		System.out.println("select item_id,rule_id from "+active_project+".project_items_x_rules");
		while(rs.next()) {
			if(rs.getString("rule_id").equals("CLAPET (PILOTE)")) {
				System.out.println(rs.getString("item_id"));
			}
			String item = rs.getString("item_id");
			String rule = rs.getString("rule_id");
			try {
				ArrayList<String> tmp = items_x_rules.get(item);
				tmp.add(rule);
				items_x_rules.put(item, tmp);
			}catch(Exception V) {
				//Item has no entry
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(rule);
				items_x_rules.put(item, tmp);
			}
		}
		
		rs.close();
		st.close();
		conn.close();
	}
	
	
	
}
