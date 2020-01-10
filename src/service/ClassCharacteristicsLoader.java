package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import controllers.paneControllers.TablePane_CharClassif;
import model.ClassCharacteristic;
import transversal.generic.Tools;

public class ClassCharacteristicsLoader {

	public static void loadKnownValuesAssociated2Items(TablePane_CharClassif tablePane_CharClassif, String active_project) throws ClassNotFoundException, SQLException {
		
		
		//CharValuesLoader.loadAllKnownValues(active_project);
		CharValuesLoader.loadAllKnownValuesAssociated2Items(active_project, tablePane_CharClassif);
		/*HashSet<ClassCharacteristic> knownChars = new HashSet<ClassCharacteristic>();
		tablePane_CharClassif.active_characteristics.forEach((k,v)->{
			v.forEach(e -> knownChars.add(e));
		});
		knownChars.forEach(c->CharValuesLoader.loadAllowedValuesAsKnownValuesForCharacteristic(c));
		knownChars.clear();*/
		CharValuesLoader.clearKnownValues();
		System.gc();
		
	}
	
	public static void loadAllClassCharacteristic(TablePane_CharClassif tablePane_CharClassif, String active_project) throws ClassNotFoundException, SQLException {
		System.out.println("Loading all known characteristics");
		if(CharValuesLoader.knownValues!=null) {
			return;
		}
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement(
				"select * from ("
				+ "select segment_id,characteristic_id,sequence,isCritical,allowedValues,allowedUoms,isActive from "
						+active_project+".project_characteristics_x_segments ) chars inner join "
				+active_project+".project_characteristics on chars.characteristic_id = project_characteristics.characteristic_id order by sequence asc");
		System.out.println("Loading all chars");
		System.out.println(stmt.toString());
		ResultSet rs=stmt.executeQuery();
		
		while(rs.next()) {
			String loop_class_id = rs.getString("segment_id");
			if(tablePane_CharClassif.active_characteristics.containsKey(loop_class_id)) {
				
			}else {
				tablePane_CharClassif.active_characteristics.put(loop_class_id, new ArrayList<ClassCharacteristic>());
			}
			ClassCharacteristic tmp = new ClassCharacteristic();
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
			
			tablePane_CharClassif.active_characteristics.get(loop_class_id).add(tmp);
			
			
			
		}
		rs.close();
		stmt.close();
		conn.close();
		
	}

}