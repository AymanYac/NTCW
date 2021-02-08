package service;

import model.ClassCaracteristic;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClassCharacteristicsLoader {

	public static void loadKnownValuesAssociated2Items(String active_project,boolean forceUpdate) throws ClassNotFoundException, SQLException {
		
		
		//CharValuesLoader.loadAllKnownValues(active_project);
		CharValuesLoader.fetchAllKnownValuesAssociated2Items(active_project,forceUpdate);
		/*HashSet<ClassCharacteristic> knownChars = new HashSet<ClassCharacteristic>();
		tablePane_CharClassif.active_characteristics.forEach((k,v)->{
			v.forEach(e -> knownChars.add(e));
		});
		knownChars.forEach(c->CharValuesLoader.loadAllowedValuesAsKnownValuesForCharacteristic(c));
		knownChars.clear();*/
		CharValuesLoader.clearKnownValues();
		System.gc();
		
	}
	
	public static void loadAllClassCharacteristic(String active_project, boolean forceUpdate) throws ClassNotFoundException, SQLException {
		if(CharValuesLoader.knownValues!=null && !forceUpdate) {
			return;
		}
		Connection conn = Tools.spawn_connection_from_pool();
		PreparedStatement stmt = conn.prepareStatement(
				"select * from ("
				+ "select segment_id,characteristic_id,sequence,isCritical,allowedValues,allowedUoms,isActive from "
						+active_project+".project_characteristics_x_segments where isActive ) chars inner join "
				+active_project+".project_characteristics on chars.characteristic_id = project_characteristics.characteristic_id order by sequence asc");
		System.out.println("Loading all chars");
		ResultSet rs=stmt.executeQuery();
		
		while(rs.next()) {
			String loop_class_id = rs.getString("segment_id");
			if(CharValuesLoader.active_characteristics.containsKey(loop_class_id)) {
				
			}else {
				CharValuesLoader.active_characteristics.put(loop_class_id, new ArrayList<ClassCaracteristic>());
			}
			ClassCaracteristic tmp = new ClassCaracteristic();
			tmp.setCharacteristic_id(rs.getString("characteristic_id"));
			tmp.setSequence(rs.getInt("sequence"));
			tmp.setIsCritical(rs.getBoolean("isCritical"));
			try{
				tmp.setAllowedValues(((String[]) rs.getArray("allowedValues").getArray()));
			}catch(Exception V) {
			}
			try {
				tmp.setAllowedUoms(((String[]) rs.getArray("allowedUoms").getArray()));
			}catch(Exception V) {
			}
			tmp.setIsActive(rs.getBoolean("isActive"));
			
			tmp.setCharacteristic_name(rs.getString("characteristic_name"));
			tmp.setCharacteristic_name_translated(rs.getString("characteristic_name_translated"));
			tmp.setIsNumeric(rs.getBoolean("isNumeric"));
			tmp.setIsTranslatable(rs.getBoolean("isTranslatable"));
			
			CharValuesLoader.active_characteristics.get(loop_class_id).add(tmp);
			
			
			
		}
		rs.close();
		stmt.close();
		conn.close();
		
	}

}
