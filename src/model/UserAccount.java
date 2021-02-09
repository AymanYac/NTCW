package model;

import javafx.scene.input.KeyCode;
import service.CharValuesLoader;
import transversal.data_exchange_toolbox.ComplexMap2JdbcObject;
import transversal.generic.CustomKeyboardListener;
import transversal.generic.Tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class UserAccount {
	String user_id;
	String user_name;
	String user_password;
	String user_profil;
	HashMap<String,String> user_projects;
	String Active_project=null;
	String Active_project_name = "";
	
	String user_desc_class;
	String[] user_desc_classes;
	static ArrayList<ArrayList<String>> searchPreferences = new ArrayList<ArrayList<String>>();
	
	
	public String getUser_desc_class(String defaultSegment) {
		if(user_desc_class!=null) {
			return user_desc_class;
		}
		return defaultSegment;
	}
	
	public String getUser_desc_class() {
		return user_desc_class;
	}

	public void setUser_desc_class(String user_desc_class) {
		this.user_desc_class = user_desc_class;
	}

	public String[] getUser_desc_classes() {
		return user_desc_classes;
	}

	public void setUser_desc_classes(String[] user_desc_classes) {
		this.user_desc_classes = user_desc_classes;
	}

	public String getActive_project_name() {
		return Active_project_name;
	}

	public void setActive_project_name(String active_project_name) {
		Active_project_name = active_project_name;
	}

	public CustomKeyboardListener<KeyCode, Boolean> PRESSED_KEYBOARD = new CustomKeyboardListener<KeyCode,Boolean>(new HashMap<KeyCode,Boolean>());
	private ArrayList<String> ManualSortColumns = new ArrayList<String>();
	private ArrayList<String> DescriptionSortColumns = new ArrayList<String>();
	private ArrayList<String> ManualSortDirs = new ArrayList<String>();
	private ArrayList<String> DescriptionSortDirs = new ArrayList<String>();
	private ArrayList<String> ManualPropositions = new ArrayList<String>();
	private Integer DescriptionActiveIdx;
	
	
	public UserAccount(){
		PRESSED_KEYBOARD.put(KeyCode.COLORED_KEY_3, false);
	}
	
	public String getUser_profil() {
		return user_profil;
	}
	public void setUser_profil(String user_profil) {
		this.user_profil = user_profil;
	}
	public String getActive_project() {
		return this.Active_project;
	}
	public void setActive_project(String active_project) {
		;
		this.Active_project = active_project;
		try {
			this.setActive_project_name(Tools.get_project_name(active_project));
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
		}
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getUser_password() {
		return user_password;
	}
	public void setUser_password(String user_password) {
		this.user_password = user_password;
	}
	
	public HashMap<String, String> getUser_projects() {
		return user_projects;
	}
	public void setUser_projects(HashMap<String, String> user_projects) {
		this.user_projects = user_projects;
	}

	public ArrayList<String> getManualSortColumns() {
		return this.ManualSortColumns;
	}
	
	public ArrayList<String> getManualSortDirs() {
		return this.ManualSortDirs;
	}

	public void setManualPropositions(ArrayList<String> methods) {
		this.ManualPropositions=methods;
	}
	
	public ArrayList<String> getManualPropositions() {
		return this.ManualPropositions;
	}

	public Object[] getManualPropositionsForJDBC() {
		if(getManualPropositions()!=null) {
			return getManualPropositions().toArray();
		}
		return new String [0];
		
	}

	public Object[] getManualSortColumnsForJDBC() {
		if(getManualSortColumns()!=null) {
			return getManualSortColumns().toArray();
		}
		return new String [0];
	}

	public Object[] getDescriptionSortColumnsForJDBC() {
		if(getDescriptionSortColumns()!=null) {
			return getDescriptionSortColumns().toArray();
		}
		return new String [0];
	}

	public Object[] getManualSortDirsForJDBC() {
		if(getManualSortDirs()!=null) {
			return getManualSortDirs().toArray();
		}
		return new String [0];
	}

	public Object[] getDescriptionSortDirsForJDBC() {
		if(getDescriptionSortDirs()!=null) {
			return getDescriptionSortDirs().toArray();
		}
		return new String [0];
	}

	public void setManualSortColumns(ArrayList arrayList) {
		this.ManualSortColumns=arrayList;
	}

	public void setManualSortDirs(ArrayList arrayList) {
		this.ManualSortDirs=arrayList;
	}

	public void setManualPropositions(Array array) throws SQLException {
		// TODO Auto-generated method stub
		this.ManualPropositions = new ArrayList<> ( Arrays.asList( ( (String[])array.getArray() )) );

	}
	public void setDescriptionActiveIdx(Integer intIndx) {
		// TODO Auto-generated method stub
		this.DescriptionActiveIdx = intIndx;

	}
	public Integer getDescriptionActiveIdx() {
		return this.DescriptionActiveIdx;

	}

	public void setManualSortColumns(Array array) throws SQLException {
		this.ManualSortColumns =  new ArrayList<> ( Arrays.asList( ( (String[])array.getArray() )) );
	}
	public void setDescriptionSortColumns(Array array) throws SQLException {
		this.DescriptionSortColumns =  new ArrayList<> ( Arrays.asList( ( (String[])array.getArray() )) );
	}

	public void setManualSortDirs(Array array) throws SQLException {
		 this.ManualSortDirs = new ArrayList<> ( Arrays.asList( ( (String[])array.getArray() )) );
		
	}
	public void setDescriptionSortDirs(Array array) throws SQLException {
		this.DescriptionSortDirs = new ArrayList<> ( Arrays.asList( ( (String[])array.getArray() )) );

	}

	public ArrayList<String> getDescriptionSortColumns() {
		return this.DescriptionSortColumns;
	}

	public ArrayList<String> getDescriptionSortDirs() {
		return this.DescriptionSortDirs;

	}

    public ArrayList<ArrayList<String>> getSearchSettings(String sourceSegment) {
		if(searchPreferences!=null){
			return FilterSearchSettingsForSegment(searchPreferences,sourceSegment);
		}
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		ArrayList<String> elem = new ArrayList<String>();
		elem.add(CustomSearchElements.DESCRIPTION_OVERVIEW);
		elem.add(CustomSearchElements.LONG_OR_SHORT);
		elem.add(CustomSearchElements.DL_OR_UL);
		ret.add(elem);
		return ret;
	}

	private ArrayList<ArrayList<String>> FilterSearchSettingsForSegment(ArrayList<ArrayList<String>> searchPreferences, String sourceSegment) {
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		searchPreferences.forEach(elem->{
			if(elem.get(0).matches("[1-9]+-.*")){
				String caracName = elem.get(0).split("[1-9]+" + "-")[1];
				Optional<ClassCaracteristic> matchingCarac = CharValuesLoader.active_characteristics.get(sourceSegment).stream().filter(car -> car.getCharacteristic_name().equals(caracName)).findAny();
				if(matchingCarac.isPresent()){
					elem.set(0,(String.valueOf(matchingCarac.get().getSequence()) + "-" + matchingCarac.get().getCharacteristic_name()));
					ret.add(elem);
				}
			}else{
				ret.add(elem);
			}
		});
		return ret;
	}

	public void saveSearchSettings(ArrayList<ArrayList<String>> concatElems) {
		searchPreferences = concatElems;
		new Thread (()->{
			Connection conn = null;
			PreparedStatement stmt=null;
			try {
				conn = Tools.spawn_connection_from_pool();
				stmt = conn.prepareStatement("update users_x_projects set search_preferences = ? where project_id = ? and user_id = ?");
				stmt.setString(1, ComplexMap2JdbcObject.serialize(searchPreferences));
				stmt.setString(2,getActive_project());
				stmt.setString(3,getUser_id());
				stmt.execute();
				//System.out.println(stmt.toString());
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			try {
				stmt.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}).start();
	}

	public void setSearchSettings(Object search_preferences) {
		searchPreferences = (ArrayList<ArrayList<String>>) search_preferences;
	}
}
