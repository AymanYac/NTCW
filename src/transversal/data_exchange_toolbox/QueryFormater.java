package transversal.data_exchange_toolbox;

import model.ClassSegment;
import model.DataInputMethods;
import model.ItemFetcherRow;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

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
		
		return "select EVENTS.item_id,level_"+projectGranularity+"_number,level_"+projectGranularity+"_name_translated,"
				+ " EVENTS.classification_method,EVENTS.user_id,EVENTS.segment_id,EVENTS.rule_id from ( "
				+ "SELECT item_id,classification_method,user_id,segment_id,rule_id FROM ( "
				+ "SELECT DISTINCT ON (item_id) * FROM "+projectID+".project_classification_event"
				+ " "+conditionStatement+" ORDER BY item_id, classification_time DESC ) tmp ORDER BY classification_time DESC) "
				+ " as EVENTS inner join "+projectID+".project_segments on"
						+ " project_segments.segment_id = EVENTS.segment_id";
		
		
	}
	
	
	
	public static String FetchItemsClassifiedBlanks(String active_project) {
		return "select item_id from (SELECT item_id,classification_method,user_id,segment_id FROM ( SELECT DISTINCT ON (item_id) * FROM "+active_project+".project_classification_event  ORDER BY item_id, classification_time DESC ) tmp ORDER BY classification_time DESC) as latest_events where segment_id is null or length(segment_id) = 0";
		
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

	public static HashMap<String, ItemClassificationData> FETCH_ITEM_CLASSES_NO_UPLOAD_PRIORITY(String joinStatement, Integer projectGranularity, String active_project) throws SQLException, ClassNotFoundException {

		HashMap<String, ClassSegment> sid2Segments = Tools.get_project_segments(active_project);

		Connection conn = Tools.spawn_connection_from_pool();
		Statement st = conn.createStatement();
		
		ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,null,null)
				+ joinStatement);
		
		HashMap<String, ItemClassificationData> classifiedItems = new HashMap<>();
		while(rs.next()) {
			
				if(rs.getString(2)!=null) {
					//2 : level_granularity_number
					//3 : level_granularity_name_translated
					ItemClassificationData tmp = new ItemClassificationData();
					tmp.setClassSegment(sid2Segments.get(rs.getString("segment_id")));
					tmp.setClassificationMethod(rs.getString("classification_method"));
					tmp.setUserID(rs.getString("user_id"));
					classifiedItems.put(rs.getString("item_id"), tmp);
				}
				
		}
		rs.close();
		st.close();
		conn.close();
		
		return classifiedItems;
	}

	public static HashMap<String, ItemClassificationData> FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY(String joinStatement,
			Integer projectGranularity, String active_project) throws ClassNotFoundException, SQLException {

		HashMap<String, ClassSegment> sid2Segments = Tools.get_project_segments(active_project);
		// Load items with classification : upload or manual
		ArrayList<String> methods = new ArrayList<String>();
		methods.add(DataInputMethods.MANUAL);
		methods.add(DataInputMethods.PROJECT_SETUP_UPLOAD);
		ArrayList<Boolean> acceptBlanks = new ArrayList<Boolean>();
		acceptBlanks.add(true);
		acceptBlanks.add(true);
		
		Connection conn = Tools.spawn_connection_from_pool();
		Statement st = conn.createStatement();
		
		ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks)
				+ joinStatement);
		
		HashMap<String, ItemClassificationData> classifiedItems = new HashMap<String,ItemClassificationData>();
		while(rs.next()) {
			
				if(rs.getString(2)!=null && rs.getString("classification_method").equals(DataInputMethods.MANUAL)) {
					//2 : level_granularity_number
					//3 : level_granularity_name_translated
					ItemClassificationData tmp = new ItemClassificationData();
					tmp.setClassSegment(sid2Segments.get(rs.getString("segment_id")));
					tmp.setClassificationMethod(rs.getString("classification_method"));
					tmp.setUserID(rs.getString("user_id"));
					classifiedItems.put(rs.getString("item_id"), tmp);
				}
				
		}
		
		
		methods = new ArrayList<String>();
		methods.add(DataInputMethods.PROJECT_SETUP_UPLOAD);
		methods.add(DataInputMethods.USER_CLASSIFICATION_RULE);
		methods.add(DataInputMethods.BINARY_CLASSIFICATION);
		acceptBlanks = new ArrayList<Boolean>();
		acceptBlanks.add(true);
		acceptBlanks.add(false);
		acceptBlanks.add(false);
		
		rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks)
				+ joinStatement);
		
		while(rs.next()) {
			
				if(rs.getString(2)!=null && !classifiedItems.containsKey( rs.getString("item_id") )) {
					ItemClassificationData tmp = new ItemClassificationData();
					tmp.setClassSegment(sid2Segments.get(rs.getString("segment_id")));
					tmp.setClassificationMethod(rs.getString("classification_method"));
					tmp.setUserID(rs.getString("user_id"));
					tmp.setRuleID(rs.getString("rule_id"));
					classifiedItems.put(rs.getString("item_id"), tmp);
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
			methods.add(DataInputMethods.PROJECT_SETUP_UPLOAD);
			ArrayList<Boolean> acceptBlanks = new ArrayList<Boolean>();
			acceptBlanks.add(true);
			Connection conn = Tools.spawn_connection_from_pool();
			Statement st = conn.createStatement();
			
			ResultSet rs =st.executeQuery(QueryFormater.ManualFetchLatestClassifiedItems(projectGranularity, active_project,methods,acceptBlanks));
			
			while(rs.next()) {
				
					if(rs.getString(2)!=null) {
						//2 : level_granularity_number
						//3 : level_granularity_name_translated
						uploadClasses.put(rs.getString("item_id"), rs.getString(2)+"&&&"+rs.getString(3)+"&&&"+rs.getString("classification_method")+"&&&"+rs.getString("user_id")+"&&&"+rs.getString("segment_id"));
					}
					
			}
			rs.close();
			st.close();
			conn.close();
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

	public static class ItemClassificationData {
		ClassSegment classSegment;
		String classificationMethod;
		String UserID;
		String RuleID;

		public ClassSegment getClassSegment() {
			return classSegment;
		}

		public void setClassSegment(ClassSegment classSegment) {
			this.classSegment = classSegment;
		}

		public String getClassificationMethod() {
			return classificationMethod;
		}

		public void setClassificationMethod(String classificationMethod) {
			this.classificationMethod = classificationMethod;
		}

		public String getUserID() {
			return UserID;
		}

		public void setUserID(String userID) {
			UserID = userID;
		}

		public String getRuleID(){
			return RuleID;
		}
		public void setRuleID(String rule_id) {
			RuleID = rule_id;
		}
	}
}
