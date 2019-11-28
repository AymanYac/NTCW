package transversal.data_exchange_toolbox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import model.ClassificationMethods;
import model.ItemFetcherRow;
import transversal.generic.Tools;

public class QueryFormater {

	private static HashMap<String, String> uploadClasses;
	private static HashMap<String, String> userID2Author;

	public static String ManualFetchLatestClassifiedItems(Integer projectGranularity,String projectID, ArrayList<String> methods, ArrayList<Boolean> acceptBlanks) {
		
		String conditionStatement="";
		if(methods!=null) {
			conditionStatement = "where (";
			for(int i=0;i<methods.size();i++) {
				if(i>0) {
					conditionStatement+=" or ";
				}
				conditionStatement+="( classification_method = '"+methods.get(i)+"' and "+(acceptBlanks.get(i)?"true )":"segment_id is not null )");
			}
			conditionStatement+=")";
		}
		System.out.println("!!");
		System.out.println("select EVENTS.item_id,level_"+projectGranularity+"_number,level_"+projectGranularity+"_name_translated,"
				+ " EVENTS.classification_method,EVENTS.user_id,EVENTS.segment_id,EVENTS.rule_id from ( "
				+ "SELECT item_id,classification_method,user_id,segment_id,rule_id FROM ( "
				+ "SELECT DISTINCT ON (item_id) * FROM "+projectID+".project_classification_event"
				+ " "+conditionStatement+" ORDER BY item_id, classification_time DESC ) tmp ORDER BY classification_time DESC) "
				+ " as EVENTS inner join "+projectID+".project_segments on"
						+ " project_segments.segment_id = EVENTS.segment_id");
		
		return "select EVENTS.item_id,level_"+projectGranularity+"_number,level_"+projectGranularity+"_name_translated,"
				+ " EVENTS.classification_method,EVENTS.user_id,EVENTS.segment_id,EVENTS.rule_id from ( "
				+ "SELECT item_id,classification_method,user_id,segment_id,rule_id FROM ( "
				+ "SELECT DISTINCT ON (item_id) * FROM "+projectID+".project_classification_event"
				+ " "+conditionStatement+" ORDER BY item_id, classification_time DESC ) tmp ORDER BY classification_time DESC) "
				+ " as EVENTS inner join "+projectID+".project_segments on"
						+ " project_segments.segment_id = EVENTS.segment_id";
		
		
	}
	
	public static String ManualFetchClassifiedItems_OLD(Integer projectGranularity,String active_project) {
		
		return "select extract.item_id, level_"+projectGranularity+"_number, level_"+projectGranularity+"_name_translated, classification_method, user_id,segment_id from (select project_items.item_id,"
				+ "level_"+projectGranularity+"_number,level_"+projectGranularity+"_name_translated, segment_id, classification_method, user_id from"
				+ " (select item_id, level_"+projectGranularity+"_number, level_"+projectGranularity+"_name_translated, latest_events.segment_id, classification_method, user_id from "
				+ "( select item_id, segment_id, classification_method, user_id from ( "
				+ "select item_id, segment_id, classification_method, user_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from "
				+ "( select item_id, segment_id, classification_method, user_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  "
				+ "( SELECT  item_id,segment_id, classification_method, user_id, row_number() over ( order by (select 0) ) as global_rn  from"
				+ " "+active_project+".project_classification_event where item_id in (select distinct item_id from "
				+ ""+active_project+".project_items ) ) as global_rank ) as ranked_events ) as maxed_events "
				+ "where local_rn = max_rn ) as latest_events left join  "+active_project+".project_segments "
				+ "on project_segments.segment_id = latest_events.segment_id ) as rich_events right join"
				+ " "+active_project+".project_items on rich_events.item_id = project_items.item_id ) as extract";
		
	}
	
	public static String FetchItemsClassifiedBlanks(String active_project) {
		return "select item_id from (SELECT item_id,classification_method,user_id,segment_id FROM ( SELECT DISTINCT ON (item_id) * FROM "+active_project+".project_classification_event  ORDER BY item_id, classification_time DESC ) tmp ORDER BY classification_time DESC) as latest_events where length(segment_id) = 0 or segment_id is null";
		
	}
	
	public static String FetchItemsClassifiedEarliest(String active_project) {
		return "SELECT item_id,user_id,classification_date FROM ( SELECT DISTINCT ON (item_id) * FROM "+active_project+".project_classification_event  ORDER BY item_id, classification_time ) tmp ORDER BY classification_time";
	}
	
	public static String FetchItemsLatestClassificationDate(String active_project) {
		return "SELECT item_id,user_id,classification_date FROM ( SELECT DISTINCT ON (item_id) * FROM "+active_project+".project_classification_event  ORDER BY item_id, classification_time desc) tmp ORDER BY classification_time desc";
		
	}
	
	public static String FetchItemsClassifiedNonBlanks_DATE(String active_project) {
		
		return "select item_id,user_id,classification_date from ( "+
		QueryFormater.FetchItemsClassifiedEarliest(active_project)
		+ ") early where item_id not in ("+
		QueryFormater.FetchItemsClassifiedBlanks(active_project)
		+")";
	}
	
	public static String FetchClassifProgresionByDateByUser (String active_project) {
		return "select user_id,classification_date, "
				+ "(to_char(classification_date, 'IYYY-MM')=to_char(current_date, 'IYYY-MM')) as same_month"
				+ ",(to_char(classification_date, 'IYYY-IW')=to_char(current_date, 'IYYY-IW')) as same_week,"
				+ "(classification_date = current_date) as same_day ,count(*) from ("+
				QueryFormater.FetchItemsClassifiedNonBlanks_DATE(active_project)+
				") performance group by grouping sets ((user_id,classification_date))";
	}

	public static HashMap<String, String> FETCH_ITEM_CLASSES_NO_UPLOAD_PRIORITY( String joinStatement, Integer projectGranularity, String active_project) throws SQLException, ClassNotFoundException {
		
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		
		ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,null,null)
				+ joinStatement);
		
		HashMap<String, String> classifiedItems = new HashMap<String,String>();
		while(rs.next()) {
			
				if(rs.getString(2)!=null) {
					//2 : level_granularity_number
					//3 : level_granularity_name_translated
					classifiedItems.put(rs.getString("item_id"), rs.getString(2)+"&&&"+rs.getString(3)+"&&&"+rs.getString("classification_method")+"&&&"+rs.getString("user_id")+"&&&"+rs.getString("segment_id"));
				}
				
		}
		rs.close();
		st.close();
		conn.close();
		
		return classifiedItems;
	}

	public static HashMap<String, String> FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(String joinStatement,
			Integer projectGranularity, String active_project) throws ClassNotFoundException, SQLException {
		// Load items with classification : upload or manual
		ArrayList<String> methods = new ArrayList<String>();
		methods.add(ClassificationMethods.MANUAL);
		methods.add(ClassificationMethods.PROJECT_SETUP_UPLOAD);
		ArrayList<Boolean> acceptBlanks = new ArrayList<Boolean>();
		acceptBlanks.add(true);
		acceptBlanks.add(true);
		
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		
		ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks)
				+ joinStatement);
		
		HashMap<String, String> classifiedItems = new HashMap<String,String>();
		while(rs.next()) {
			
				if(rs.getString(2)!=null && rs.getString("classification_method").equals(ClassificationMethods.MANUAL)) {
					//2 : level_granularity_number
					//3 : level_granularity_name_translated
					classifiedItems.put(rs.getString("item_id"), rs.getString(2)+"&&&"+rs.getString(3)+"&&&"+rs.getString("classification_method")+"&&&"+rs.getString("user_id")+"&&&"+rs.getString("segment_id"));
				}
				
		}
		
		
		methods = new ArrayList<String>();
		methods.add(ClassificationMethods.PROJECT_SETUP_UPLOAD);
		methods.add(ClassificationMethods.USER_RULE);
		methods.add(ClassificationMethods.BINARY_CLASSIFICATION);
		acceptBlanks = new ArrayList<Boolean>();
		acceptBlanks.add(true);
		acceptBlanks.add(false);
		acceptBlanks.add(false);
		
		rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks)
				+ joinStatement);
		
		while(rs.next()) {
			
				if(rs.getString(2)!=null && !classifiedItems.containsKey( rs.getString("item_id") )) {
					//2 : level_granularity_number
					//3 : level_granularity_name_translated
					classifiedItems.put(rs.getString("item_id"), rs.getString(2)+"&&&"+rs.getString(3)+"&&&"+rs.getString("classification_method")+"&&&"+rs.getString("user_id")+"&&&"+rs.getString("segment_id")+"&&&"+rs.getString("rule_id"));
				}
				
		}
		
		rs.close();
		st.close();
		conn.close();
		return classifiedItems;
	}

	public static void ADD_UPLOAD_ITEM_CLASS(ItemFetcherRow tmp,Integer projectGranularity, String active_project) throws ClassNotFoundException, SQLException {
		
		if(uploadClasses!=null) {
			
		}
		else {
			userID2Author = Tools.get_user_names();
			uploadClasses = new HashMap<String,String>();
			ArrayList<String> methods = new ArrayList<String>();
			methods.add(ClassificationMethods.PROJECT_SETUP_UPLOAD);
			ArrayList<Boolean> acceptBlanks = new ArrayList<Boolean>();
			acceptBlanks.add(true);
			Connection conn = Tools.spawn_connection();
			Statement st = conn.createStatement();
			
			ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks));
			
			while(rs.next()) {
				
					if(rs.getString(2)!=null) {
						//2 : level_granularity_number
						//3 : level_granularity_name_translated
						uploadClasses.put(rs.getString("item_id"), rs.getString(2)+"&&&"+rs.getString(3)+"&&&"+rs.getString("classification_method")+"&&&"+rs.getString("user_id")+"&&&"+rs.getString("segment_id"));
					}
					
			}
		}
		
		
		
		if(uploadClasses.containsKey(tmp.getItem_id())) {
			tmp.setUpload_segment_number( uploadClasses.get(tmp.getItem_id()).split("&&&")[0] );
			tmp.setUpload_segment_id( uploadClasses.get(tmp.getItem_id()).split("&&&")[4] );
			tmp.setUpload_segment_name( uploadClasses.get(tmp.getItem_id()).split("&&&")[1] );
			tmp.setSource_Upload(uploadClasses.get(tmp.getItem_id()).split("&&&")[2]);
			tmp.setAuthor_Upload(userID2Author.get(uploadClasses.get(tmp.getItem_id()).split("&&&")[3]));
			return;
		}
		
		
	}

	public static String UOM_MULTIPLIER_DOUBLE2CHAR_QUERY(String colName) {
		// TODO Auto-generated method stub
		return "to_char(uom_multiplier, 'FM999999999999999999999999999999999999999999999990.09999999999999999999999999999999999999999FM') as "+colName;
	}
	
}
