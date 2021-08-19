package transversal.data_exchange_toolbox;

import com.google.gson.Gson;
import model.ClassSegmentClusterComboRow;
import model.UserAccount;
import org.hildan.fxgson.FxGson;
import org.postgresql.util.PGobject;
import transversal.dialog_toolbox.DedupLaunchDialog;
import transversal.generic.Tools;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class ComplexMap2JdbcObject {

	private static final Gson gson = new Gson();

	public static Object getObject(HashMap<?, ?> tmp) throws SQLException {
		 PGobject jsonObject = new PGobject();
		 jsonObject.setType("json");
		 Gson gson = new Gson(); 
		 String json = gson.toJson(tmp); 
		 
		 jsonObject.setValue(json);
		 
		 return jsonObject;
	}

	public static void saveAccountProjectPreferenceForClassification(UserAccount account) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection_from_pool();
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

	public static void saveAccountProjectPreferenceForDescription(UserAccount account) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection_from_pool();
		PreparedStatement stmt = conn.prepareStatement("update administration.users_x_projects set "
				+ "user_description_sorting_columns=?,"
				+ "user_description_sorting_order=? where user_id = ? and project_id = ?");

		stmt.setArray(1, conn.createArrayOf("text", account.getDescriptionSortColumnsForJDBC()) );
		stmt.setArray(2, conn.createArrayOf("text", account.getDescriptionSortDirsForJDBC()) );

		stmt.setString(3, account.getUser_id());
		stmt.setString(4, account.getActive_project());


		stmt.execute();
		stmt.close();
		conn.close();

	}

	public static String serialize(Object obj) {
		return gson.toJson(obj);
	}

	public static String serializeFX(Object obj) {
		// to handle only Properties and Observable collections
		Gson fxGson = FxGson.coreBuilder().setPrettyPrinting().create();
		return fxGson.toJson(obj);
	}

	public static Object deserialize(String string, Type type) {
		return gson.fromJson(string,type);
	}

	public static Object deserializeFX(String string, Type type) {
		// to handle only Properties and Observable collections
		Gson fxGson = FxGson.create();
		return fxGson.fromJson(string,type);
	}

	public static class DedupSettings {
		String match;
		String mismatch;
		String ratio;
		ClassSegmentClusterComboRow source;
		ClassSegmentClusterComboRow target;
		HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weights;

		public DedupSettings() {
		}

		public String getMatch() {
			return match;
		}

		public void setMatch(String match) {
			this.match = match;
		}

		public String getMismatch() {
			return mismatch;
		}

		public void setMismatch(String mismatch) {
			this.mismatch = mismatch;
		}

		public String getRatio() {
			return ratio;
		}

		public void setRatio(String ratio) {
			this.ratio = ratio;
		}

		public ClassSegmentClusterComboRow getSource() {
			return source;
		}

		public void setSource(ClassSegmentClusterComboRow source) {
			this.source = source;
		}

		public ClassSegmentClusterComboRow getTarget() {
			return target;
		}

		public void setTarget(ClassSegmentClusterComboRow target) {
			this.target = target;
		}

		public HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> getWeights() {
			return weights;
		}

		public void setWeights(HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weights) {
			this.weights = weights;
		}
	}

}
