package service;

import controllers.Char_description;
import javafx.application.Platform;
import model.CharDescriptionRow;
import model.ClassCaracteristic;
import model.GlobalConstants;
import model.UserAccount;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.dialog_toolbox.FxUtilTest;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;

public class CharAdvancementUpdater {

	private Char_description parentScene;
	public UserAccount account;
	private Integer projectCardinality;

	public void setParentScene(Char_description parent) {
		this.parentScene=parent;
	}

	public void refreshAdvancement(Char_description parent) throws SQLException {

		try {
			Map<String, Long> charadvancementmap = CharItemFetcher.allRowItems.parallelStream().map(CharDescriptionRow::getCompletionStatusString).collect(Collectors.groupingBy(string -> string, Collectors.counting()));
			Map<String, Long> charadvancementmapclass = CharItemFetcher.allRowItems.parallelStream().filter(item->item.getClass_segment_string()!=null && item.getClass_segment_string().split("&&&")[0].equals(parent.classCombo.getValue().getSegmentId())).map(CharDescriptionRow::getCompletionStatusString).collect(Collectors.groupingBy(string -> string, Collectors.counting()));
			Map<String, Long> charadvancementmapDaily = CharItemFetcher.allRowItems.parallelStream().filter(CharDescriptionRow::hasDataFromToday).map(CharDescriptionRow::getCompletionStatusString).collect(Collectors.groupingBy(string -> string, Collectors.counting()));
			Map<String, Long> charadvancementmapclassDaily = CharItemFetcher.allRowItems.parallelStream().filter(CharDescriptionRow::hasDataFromToday).filter(item->item.getClass_segment_string()!=null && item.getClass_segment_string().split("&&&")[0].equals(parent.classCombo.getValue().getSegmentId())).map(CharDescriptionRow::getCompletionStatusString).collect(Collectors.groupingBy(string -> string, Collectors.counting()));
			Platform.runLater(() -> {
				parentScene.charCounterRemaining.setText("Items remaining (class/total): " +
						(charadvancementmapclass.get("Missing critical")!=null?charadvancementmapclass.get("Missing critical"):0)
						+"/"+
						(charadvancementmap.get("Missing critical")!=null?charadvancementmap.get("Missing critical"):0)
				);
				parentScene.charCounterIncluding.setText("Total items completed (inc. unknowns) (class/total): " +
						(charadvancementmapclass.get("Completed critical (inc. Unknown)")!=null?charadvancementmapclass.get("Completed critical (inc. Unknown)"):0)
						+"/"+
						(charadvancementmap.get("Completed critical (inc. Unknown)")!=null?charadvancementmap.get("Completed critical (inc. Unknown)"):0)
				);
				parentScene.charCounterExcluding.setText("Total items completed (exc. unknowns) (class/total): " +
						(charadvancementmapclass.get("Completed critical (exc. Unknown)")!=null?charadvancementmapclass.get("Completed critical (exc. Unknown)"):0)
						+"/"+
						(charadvancementmap.get("Completed critical (exc. Unknown)")!=null?charadvancementmap.get("Completed critical (exc. Unknown)"):0)
				);
				parentScene.charCounterIncludingDaily.setText("Completed today (class/total): " +
						(charadvancementmapclassDaily.get("Completed critical (inc. Unknown)")!=null?charadvancementmapclassDaily.get("Completed critical (inc. Unknown)"):0)
						+"/"+
						(charadvancementmapDaily.get("Completed critical (inc. Unknown)")!=null?charadvancementmapDaily.get("Completed critical (inc. Unknown)"):0)
				);
			});
		}catch (Exception V){

		}
		Connection conn=null;
		try {
			conn = Tools.spawn_connection();
			
			String SUMquery = "select sum(count) from ("
					+QueryFormater.FetchClassifProgresionByDateByUser(account.getActive_project())
					+") progress";
			int sum = 0;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SUMquery);
			while(rs.next()) {
				sum+=rs.getInt(1);
			}
			rs.close();
			
			String PERSONALquery = "select * from ("
					+QueryFormater.FetchClassifProgresionByDateByUser(account.getActive_project())
					+") progress where same_day";
			
			rs = stmt.executeQuery(PERSONALquery);
			
			int dailyPersonal=0;
			int dailyTeam = 0;
			while(rs.next()) {
				if(rs.getString("user_id").equals(account.getUser_id())) {
					dailyPersonal+= rs.getInt("count");
					dailyTeam+=rs.getInt("count");
				}else {
					dailyTeam+=rs.getInt("count");
				}
			}
			
			if(projectCardinality!=null) {
				
			}else {
				projectCardinality = Tools.count_project_cardinality(account.getActive_project());
			}
			rs.close();

			try{
				if (GlobalConstants.DESCRIPTION_RESTORE_PERSISTED_ITEM && !FxUtilTest.getComboBoxValue(parent.classCombo).getSegmentId().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
					CharDescriptionRow row = parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
					int selected_col = Math.floorMod(parent.tableController.selected_col, CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).size());
					ClassCaracteristic active_char = CharValuesLoader.active_characteristics.get(row.getClass_segment_string().split("&&&")[0]).get(selected_col);
					stmt.execute("update users_x_projects set activeCharID = '"+active_char.getCharacteristic_id()+"', activeItemID='"+row.getItem_id()+"' where user_id='"+account.getUser_id()+"' and project_id='"+account.getActive_project()+"'");
				}
			}catch (Exception V){
				//V.printStackTrace(System.err);
			}
			stmt.close();
			final int finalsum = sum;
			final int finaldailyPersonal = dailyPersonal;
			final int finaldailyTeam = dailyTeam;
			
			
			Platform.runLater(()->{
					parentScene.classCounterTotal.setText("Total items classified: "+finalsum);
					parentScene.classCounterRemaining.setText("Items to be classified: "+(projectCardinality-finalsum));
					parentScene.classCounterDaily.setText("Daily personal: "+finaldailyPersonal+" | Daily team : "+finaldailyTeam);
				});

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.close();
		
	}

	protected void updateCounterFields(int sum, Integer projectCardinality2, int dailyPersonal, int dailyTeam) {
		// TODO Auto-generated method stub
		
	}

}
