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

public class CharAdvancementUpdater {

	private Char_description parentScene;
	public UserAccount account;
	private Integer projectCardinality;

	public void setParentScene(Char_description parent) {
		this.parentScene=parent;
	}

	public void refresh(Char_description parent) throws SQLException {
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
				if (GlobalConstants.DESCRIPTION_RESTORE_PERSISTED_ITEM && !FxUtilTest.getComboBoxValue(parent.classCombo).getClassSegment().equals(GlobalConstants.DEFAULT_CHARS_CLASS)) {
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
			
			
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					
					parentScene.counterTotal.setText("Total items classified: "+finalsum);
					parentScene.counterRemaining.setText("Items to be classified: "+(projectCardinality-finalsum));
					parentScene.counterDaily.setText("Daily personal: "+finaldailyPersonal+" | Daily team : "+finaldailyTeam);
					
				}
				
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
