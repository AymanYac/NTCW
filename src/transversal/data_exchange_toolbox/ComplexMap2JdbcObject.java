package transversal.data_exchange_toolbox;

import com.google.gson.Gson;
import model.UserAccount;
import org.postgresql.util.PGobject;
import transversal.generic.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class ComplexMap2JdbcObject {

	public static Object getObject(HashMap<?, ?> tmp) throws SQLException {
		 PGobject jsonObject = new PGobject();
		 jsonObject.setType("json");
		 Gson gson = new Gson(); 
		 String json = gson.toJson(tmp); 
		 
		 jsonObject.setValue(json);
		 
		 return jsonObject;
	}

	public static void saveAccountProjectPreference(UserAccount account) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("update administration.users_x_projects set "
				+ "user_manual_propositions=?,"
				+ "user_manual_sorting_columns=?,"
				+ "user_manual_sorting_order=? where user_id = ? and project_id = ?");
		
		stmt.setArray(1, conn.createArrayOf("text", account.getManualPropositionsForJDBC()) );
		stmt.setArray(2, conn.createArrayOf("text", account.getManualSortColumnsForJDBC()) );
		stmt.setArray(3, conn.createArrayOf("text", account.getManualSortDirsForJDBC()) );
		
		stmt.setString(4, account.getUser_id());
		stmt.setString(5, account.getActive_project());
		
		
		stmt.execute();
		stmt.close();
		conn.close();
		
	}

}
