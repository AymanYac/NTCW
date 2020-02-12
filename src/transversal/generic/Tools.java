package transversal.generic;

import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.poi.util.IOUtils;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.GlobalConstants;
import model.BinaryClassificationParameters;
import model.CharDescriptionRow;
import model.ClassificationMethods;
import model.GenericRule;
import model.ItemFetcherRow;
import model.UserAccount;
import transversal.dialog_toolbox.ExceptionDialog;


public class Tools {
	
	
	public static String RunVBS(File file) throws IOException {
		
		//new ProcessBuilder().inheritIO().command("cscript",file.getCanonicalPath()).start();
		ProcessBuilder pb = new ProcessBuilder().command("cscript",file.getCanonicalPath());
		String result = null;
		try {
	        Process p = pb.start();
	        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

	        StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
	        reader.lines().iterator().forEachRemaining(sj::add);
	        result = sj.toString();

	        p.waitFor();
	        p.destroy();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	    
	}
	
	
	
	public static String get_project_user_language_code(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select google_language_code from administration.languages where language_id in (select classifier_language from projects where project_id ='"+active_project+"')");
		rs.next();
		String ret = rs.getString("google_language_code");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}

	public static String get_project_data_language_code(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select google_language_code from administration.languages where language_id in (select data_language from projects where project_id ='"+active_project+"')");
		rs.next();
		String ret = rs.getString("google_language_code");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}
	//Creates a connection, the LOGIC section uses the connection to check
	//the credentials and returns the user's administration profile


	public static UserAccount checkpass(String login, String pass) throws ClassNotFoundException, SQLException {
		//#
		
		//Creates a connection
		Connection conn = spawn_connection();
		
		//LOGIC
		//Get the password and profile associated to the login
		PreparedStatement st = conn.prepareStatement("select * from administration.users where user_name = ?");
	    st.setString(1, login);
	    //#
	    ResultSet rs = st.executeQuery();
	    
	    
	    //Intialize the return value of the profile to null
	    UserAccount account = null;
	    
	    //If the query contains results (the login exists in the database)
	    //return the associated password
	    if (rs.next())
	    {
	    	
	    	//check the associated password with the input password
	    	//if the passwords match , set the return value to the associated profile
	    	
	    	if(Boolean.valueOf(pass.equals(rs.getString("user_password")))) {
	    		
	    		
	    		account = new UserAccount();
		    	account.setUser_id(rs.getString("user_id"));
		    	account.setUser_name(rs.getString("user_name"));
		    	account.setUser_password(rs.getString("user_password"));
		    	account.setUser_profil("user_profile");
		    	Connection conn2 = Tools.spawn_connection();
		    	PreparedStatement st2 = conn.prepareStatement("select * from administration.users_x_projects where user_id = ?");
		    	st2.setString(1, account.getUser_id());
		    	//#
		    	ResultSet rs2 = st2.executeQuery();
		    	HashMap<String,String> tmp = new HashMap<String,String>();
		    	while(rs2.next()) {
		    		if(rs2.getString("user_project_profile")!=null && rs2.getString("user_project_profile").length()>5) {
		    			tmp.put(rs2.getString("project_id"), rs2.getString("user_project_profile"));
		    		}
		    		if(rs2.getBoolean("active_project_status")) {
		    			account.setActive_project(rs2.getString("project_id"));
		    		}
		    	}
		    	account.setUser_projects(tmp);
		    	
		    	rs2.close();
		    	st2.close();
		    	conn2.close();
		    	
	    	};
	    }
	    
	    
	    //Closes the database connection
	    rs.close();
	    st.close();
    	conn.close();
    	
    	//return the return value 
		return account;
		
	}
	
	//Creates an independent connection session to the database

	public static Connection spawn_connection() throws ClassNotFoundException, SQLException {
		 //load postgresql driver
		 Class.forName("org.postgresql.Driver");
		 //load database address, port and database name
	     String url = "jdbc:postgresql://" + load_ip() + ":"+load_port()+"/" + getDatabaseName();
	     //set connection user, password, login timeout, connection timeout, socket timeout
	     Properties props = new Properties();
	     props.setProperty("user", getUserName());
	     props.setProperty("password", getUserPassword());
	     props.setProperty("loginTimeout", "20");
	     props.setProperty("connectTimeout", "0");
	     props.setProperty("socketTimeout", "0");
	     
	     //create connection to the dabase server
	     Connection conn = DriverManager.getConnection(url, props);
	     
	     //return the connection
	     return conn;
	}

	public static String load_ip() {
		return "localhost";
		//return "88.190.148.154";
		//return "192.168.0.25";
	}
	public static String load_port() {
		return "5432";
	}
	

	public static String getDatabaseName() {

		return "Classif_Test2";
	}
	
	public static String getUserName() {
		return "postgres";
		
	}
	public static String getUserPassword() {
		return "EternalPhoenix";
	}

	public static HashSet<Integer> RandomSample(Integer ref_desc_cardinality, Double classif_buildSampleSize) {
		
		HashSet<Integer> ret = new HashSet<Integer>();
		Random randomNum = new Random();
		
		IntStream.range(0, ref_desc_cardinality).forEach(
		        n -> {
		        	if(randomNum.nextInt(100)<classif_buildSampleSize) {
		        		ret.add(n);
		        	}
		        }
		    );
		
		
		return ret;
	}

	public static Double score_rule(HashMap<String, String> rule, BinaryClassificationParameters binaryClassificationParameters, boolean isClassif) {
		try {	
			if(isClassif) {
				if((100.0 * Double.parseDouble(rule.get("Accuracy")))<binaryClassificationParameters.getClassif_Ta()) {
					return null;
				}
				if( Double.parseDouble(rule.get("Total"))*Double.parseDouble(rule.get("Accuracy")) < binaryClassificationParameters.getClassif_Tb()){
					return null;
				}
				Double eps = binaryClassificationParameters.getClassif_epsilon();
				Double firstTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getClassif_rank().get(0)));
				Double secondTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getClassif_rank().get(1)));
				Double thirdTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getClassif_rank().get(2)));
				
				return firstTerm+eps*secondTerm+eps*eps*thirdTerm;
			}else {
				if((100.0 * Double.parseDouble(rule.get("Accuracy")))<binaryClassificationParameters.getPreclassif_Ta()) {
					return null;
				}
				if( Double.parseDouble(rule.get("Total"))*Double.parseDouble(rule.get("Accuracy")) < binaryClassificationParameters.getPreclassif_Tb()){
					return null;
				}
				Double eps = binaryClassificationParameters.getPreclassif_epsilon();
				Double firstTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getPreclassif_rank().get(0)));
				Double secondTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getPreclassif_rank().get(1)));
				Double thirdTerm = Double.parseDouble(rule.get(binaryClassificationParameters.getPreclassif_rank().get(2)));
				
				return firstTerm+eps*secondTerm+eps*eps*thirdTerm;
			}
		}catch(Exception V) {
			return null;
		}
		
		
	}

	public static String formatDuration(Duration duration) {
		 long seconds = duration.getSeconds();
		    long absSeconds = Math.abs(seconds);
		    String positive = String.format(
		        "%d:%02d:%02d",
		        absSeconds / 3600,
		        (absSeconds % 3600) / 60,
		        absSeconds % 60);
		    return seconds < 0 ? "-" + positive : positive;
	}
	
	public static String formatThounsands(Integer number) {
		
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);
		return formatter.format(number.longValue());
		
	}

	public void create_project_schema(String pid) {
		try {
			String schema_creation_query = readStringFile("/scripts/project_schema_creation.sql",Charset.defaultCharset());
			Connection conn = spawn_connection();
			Statement stmt = conn.createStatement();
			//#
			stmt.execute(schema_creation_query);
			//#
			stmt.execute("select administration.create_project('"+pid+"')");
			stmt.close();
			conn.close();
			
			
		} catch (IOException | ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String readStringFile(String path, Charset encoding) throws IOException {
		InputStream in = getClass().getResourceAsStream(path);
		byte[] encoded = IOUtils.toByteArray(in);
		return new String(encoded, encoding);
		
	}
	public String readFile(String path, Charset encoding) 
			  throws IOException, URISyntaxException 
	{
		;
	  byte[] encoded = Files.readAllBytes(Paths.get(getClass().getResource(path).toURI()));
	  return new String(encoded, encoding);
	}

	public static String generate_uuid() {
		UUID uuid = java.util.UUID.randomUUID();
		return "u"+uuid.toString().replaceAll("[^A-Za-z0-9]", "");
	}

	public static void decorate_menubar(MenuBar menubar, UserAccount account) {
		
		Label homeLabel = new Label("Home");
		homeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    	try {
					  
				 	//Loads next screen's controller, loading cursor
					Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Front_page.fxml"));
					AnchorPane root = fxmlLoader.load();

				    controllers.Front_page controller = fxmlLoader.getController();
					
					Scene scene = new Scene(root,400,400);
					controller.scene = scene;
					scene.setCursor(Cursor.WAIT);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					
					//Sets up next screen size
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();
					
					//Sets up next screen's login and role, default cursor
					controller.setUserAccount(account);
				    //close the current window
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 front_page", "FX001 front_page", "FX001 front_page");
					e.printStackTrace();
				}
		    }
		});
		
		Menu home = new Menu();
		home.setGraphic(homeLabel);
		
		
		Menu project_selection = menubar.getMenus().get(1);
		MenuItem activate_project = new MenuItem("Project selection");
		activate_project.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
					;
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Project_selection.fxml"));
					AnchorPane root = fxmlLoader.load();

				    controllers.Project_selection controller = fxmlLoader.getController();
					
					Scene scene = new Scene(root,400,400);
					controller.scene = scene;
					scene.setCursor(Cursor.WAIT);
				    
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();
					
					
					controller.setUserAccount(account);
				    controller.load_projects_list();
					scene.setCursor(Cursor.DEFAULT);
					//close the current window
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
					
				} catch(Exception e) {
					ExceptionDialog.show("FX001 project_selection", "FX001 project_selection", "FX001 project_selection");
					e.printStackTrace();
				}
		    }
		});
		
		MenuItem new_project = new MenuItem("New project creation");
		project_selection.getItems().clear();
		project_selection.getItems().addAll(activate_project,new_project);
		
		new_project.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	  
			    try {
				    Stage primaryStage = new Stage();
				    
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Project_parameters.fxml"));
					AnchorPane root = fxmlLoader.load();

					Scene scene = new Scene(root,400,400);
					
					primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
					primaryStage.setScene(scene);
					//primaryStage.setMinHeight(768);
					//primaryStage.setMinWidth(1024);
					primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
					primaryStage.show();

					controllers.Project_parameters controller = fxmlLoader.getController();
					
					controller.setUserAccount(account);
					
					//close the current window
				    ((Stage) menubar.getScene().getWindow()).close();
				    scene.setCursor(Cursor.DEFAULT);
					
					
				    
					
				} catch(Exception e) {
					e.printStackTrace(System.err);
					ExceptionDialog.show("FX001 project_parameters", "FX001 project_parameters", "FX001 project_parameters");
				}
		    }});
		
		
		
		
		Menu auto_classif = new Menu();
		Label auto_classif_label = new Label("Automated classification");
		auto_classif_label.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
			//if there's currently an actif project, proceed to the classification
			String activated_pid = account.getActive_project();
			Stage stage = (Stage) menubar.getScene().getWindow();
					if(activated_pid!=null) {
						
					    try {
					    	Connection conn = Tools.spawn_connection();
					    	Statement stmt = conn.createStatement();
					    	ResultSet rs = stmt.executeQuery("select target_quality from administration.projects where project_id='"+activated_pid+"'");
					    	rs.next();
					    	double target_desc_accuracy = rs.getDouble(1);
					    	
					    	rs = stmt.executeQuery("select count(distinct item_id) from "+activated_pid+".project_items");
					    	rs.next();
					    	int target_desc_cardinality = rs.getInt(1);
					    	
					    	rs.close();
					    	stmt.close();
					    	conn.close();
					    	
					    	
					    	
						    Stage primaryStage = new Stage();
						    
						    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Auto_classification_launch.fxml"));
							AnchorPane root = fxmlLoader.load();

							Scene scene = new Scene(root,400,400);
							
							primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
							primaryStage.setScene(scene);
							//primaryStage.setMinHeight(768);
							//primaryStage.setMinWidth(1024);
							primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
							primaryStage.show();

							controllers.Auto_classification_launch controller = fxmlLoader.getController();
							
							controller.setUserAccount(account);
							
							controller.setPid(activated_pid);
							controller.setTarget_desc_cardinality(target_desc_cardinality);
							controller.setTarget_desc_accuracy(target_desc_accuracy);
							
							stage.close();
							
						} catch(Exception e) {
							ExceptionDialog.show("FX001 auto_classification_launch", "FX001 auto_classification_launch", "FX001 auto_classification_launch");
							e.printStackTrace(System.err);
						}
					}else {
						//Else raise an alert message dialog
						ExceptionDialog.no_selected_project();
					}
		}});
		
		auto_classif.setGraphic(auto_classif_label);
		
		
		
		Menu manual_classif = new Menu();
		Label manual_classif_label = new Label("Semi-manual classification");
		
		manual_classif.setDisable(false);
		manual_classif_label.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    	//if there's currently an actif project, proceed to the classification
				String activated_pid = account.getActive_project();
				Stage stage = (Stage) menubar.getScene().getWindow();

					if(activated_pid!=null) {
						
					    try {
					    	
						    Stage primaryStage = new Stage();
						    
						    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Manual_classif.fxml"));
							AnchorPane root = fxmlLoader.load();

							Scene scene = new Scene(root,400,400);
							
							primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
							primaryStage.setScene(scene);
							//primaryStage.setMinHeight(768);
							//primaryStage.setMinWidth(1024);
							primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
							primaryStage.show();

							controllers.Manual_classif controller = fxmlLoader.getController();
							controller.setUserAccount(account);
							
							
							stage.close();
							
						} catch(Exception e) {
							ExceptionDialog.show("FX001 Rule_definition", "FX001 Rule_definition", "FX001 Rule_definition");
							e.printStackTrace(System.err);
						}
					}else {
						//Else raise an alert message dialog
						ExceptionDialog.no_selected_project();
					}
		    }});
		
		manual_classif.setGraphic(manual_classif_label);
		
		
		
		Menu item_desc = new Menu();
		Label item_desc_label = new Label("Item description");
		
		item_desc.setDisable(false);
		item_desc_label.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    	
		    	//if there's currently an actif project, proceed to the classification
				String activated_pid = account.getActive_project();
				Stage stage = (Stage) menubar.getScene().getWindow();

					if(activated_pid!=null) {
						
					    try {
					    	
						    Stage primaryStage = new Stage();
						    
						    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Scenes/Char_description.fxml"));
							AnchorPane root = fxmlLoader.load();

							Scene scene = new Scene(root,400,400);
							
							primaryStage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
							primaryStage.setScene(scene);
							//primaryStage.setMinHeight(768);
							//primaryStage.setMinWidth(1024);
							primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
							primaryStage.show();

							controllers.Char_description controller = fxmlLoader.getController();
							controller.setUserAccount(account);
							
							
							stage.close();
							
						} catch(Exception e) {
							ExceptionDialog.show("FX001 Char_description", "FX001 Char_description", "FX001 Char_description");
							e.printStackTrace(System.err);
						}
					}else {
						//Else raise an alert message dialog
						ExceptionDialog.no_selected_project();
					}
		    	
		    	
		    }});
		
		item_desc.setGraphic(item_desc_label);
		
		
		
		
		
		
		
		
		
		
		
		Menu review = new Menu("Classification review");
		review.setDisable(true);
		Menu dashboard = new Menu("Project Dashboard");
		dashboard.setDisable(true);
		Menu help = new Menu("Help");
		help.setDisable(true);
		
		
		
		menubar.getMenus().clear();
		menubar.getMenus().addAll(home,project_selection,auto_classif,manual_classif,item_desc,review,dashboard,help);
	}



	public static Integer count_project_cardinality(String project_id) throws ClassNotFoundException, SQLException {

		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("select count(*) from "+project_id+".project_items");
		rs.next();
		int ret = rs.getInt(1);
		rs.close();
		st.close();
		conn.close();
		return ret;
	}



	public static Integer get_project_granularity(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("select number_of_levels from administration.projects where project_id = '"+active_project+"'");
		rs.next();
		int ret = rs.getInt(1);
		rs.close();
		st.close();
		conn.close();
		return ret;
	}



	public static ScrollBar getVerticalScrollbar(TableView table) {
		 for (Node n : table.lookupAll(".scroll-bar")) {
	            if (n instanceof ScrollBar) {
	                ScrollBar bar = (ScrollBar) n;
	                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
	                }
	            }
	        }        
	        //return result;
	        return (ScrollBar) table.lookup(".scroll-bar:vertical");
	}



	public static HashMap<String,String> get_user_names() throws ClassNotFoundException, SQLException {
		HashMap<String, String> tmp = new HashMap<String,String>();
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from administration.users");
		while(rs.next()) {
			tmp.put(rs.getString("user_id"), rs.getString("user_name"));
		}
		rs.close();
		stmt.close();
		conn.close();
		return tmp;
	}



	public static boolean isCapsLockOn() {
		java.awt.Robot robot;
		try {
			robot = new java.awt.Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
		    robot.keyRelease(KeyEvent.VK_CONTROL);
		    return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK); 
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     return false;
	}

	public static HashMap<String, String> UUID2CID(String PID) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("select segment_id,level_"+Tools.get_project_granularity(PID)+"_number from "+PID+".project_segments");
	    HashMap<String, String> UUID2CID = new HashMap<String,String>();
	    while(rs.next()) {
	    	UUID2CID.put(rs.getString("segment_id"),rs.getString(2));
	    }
	    UUID2CID.put("","");
	    rs.close();
	    stmt.close();
	    conn.close();
	    
	    return UUID2CID;
	}

	
	
	//Update after upload method introduced as back-up class for rule blanked items:
	//This method SHOULD NOT print to the database whatever state the item is in
	//This method should only print to the database the state of an item if its current classification
	//is indeed made using the calling method (rules or manual)
	public static void ItemFetcherRow2ClassEvent( List<ItemFetcherRow> rows, UserAccount account, String METHOD) {

		Task<Void> task = new Task<Void>() {
		    
		@Override
	    protected Void call() throws Exception {
		Connection conn = Tools.spawn_connection();
		//Connection conn2 = Tools.spawn_connection();
		
		PreparedStatement stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_classification_event("
				+ "classification_event_id,item_id,segment_id,classification_method,rule_id,user_id,classification_date,classification_time) values("
				+ "?,?,?,?,?,?,?,clock_timestamp())");
		
		//PreparedStatement stmt2 = conn2.prepareStatement("insert into administration.wal values (?, clock_timestamp())");
		
		
		for(ItemFetcherRow tmp:rows) {
			
			
			try {
				//Don't update rule matching items that are assigned to a manual input
				//This is optional now as on screen-load no longer uses the latest event
				//The latest manual class is always loaded
				if( tmp.getSource_Display().equals(ClassificationMethods.MANUAL) && !METHOD.equals(ClassificationMethods.MANUAL)) {
					continue;
				}
			}catch(Exception V) {
				//The item has no source (blanking rule on blank item)
				
			}
			
			
			String eventid = Tools.generate_uuid();
			stmt.setString(1, eventid);
			stmt.setString(2, tmp.getItem_id());
			//stmt.setString(3, tmp.getDisplay_segment_id());//WRONG ! see update above
			if(METHOD.equals(ClassificationMethods.USER_RULE)) {
				stmt.setString(3,tmp.getRule_Segment_id());
				stmt.setString(5, tmp.getRule_id_Display());
				
			}else {
				//Only possible branch : the user changed the manual classification
				stmt.setString(3, tmp.getManual_segment_id());
			}
			stmt.setString(4, METHOD);
			stmt.setString(5, tmp.getRule_id_Display());
			stmt.setString(6, account.getUser_id());
			stmt.setDate(7, new java.sql.Date((new java.util.Date()).getTime()));
			;
			stmt.addBatch();
			
			
			//stmt2.setString(1, eventid);
			//stmt2.addBatch();
		}
			stmt.executeBatch();
			stmt.clearBatch();
			stmt.close();
			
			//stmt2.executeBatch();
			//stmt2.clearBatch();
			//stmt2.close();
			
			conn.close();
			//conn2.close();
			
    		return null;
	    }
		};
		task.setOnSucceeded(e -> {
			;

			
			});

		task.setOnFailed(e -> {
		    Throwable problem = task.getException();
		    /* code to execute if task throws exception */
		    System.err.println("failed to sync items ");
		    //System.err.println(ExceptionUtils.getRootCauseMessage( problem ))	;
		    problem.printStackTrace(System.err);
		    
		    
		});

		task.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
		
		Thread thread = new Thread(task);; thread.setDaemon(true);
		thread.setName("Sync DB for method "+METHOD);
		thread.start();
		
	}
	
	


	public static List<String> get_project_dw_words(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct term_name from "+active_project+".project_terms where drawing_term_status ");
		ArrayList<String> tmp = new ArrayList<String>();
		while(rs.next()) {
			tmp.add(rs.getString(1));
		}
		rs.close();
		stmt.close();
		conn.close();
		return tmp;
	}



	public static List<String> get_project_for_words(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct term_name from "+active_project+".project_terms where application_term_status");
		ArrayList<String> tmp = new ArrayList<String>();
		while(rs.next()) {
			tmp.add(rs.getString(1));
		}
		rs.close();
		stmt.close();
		conn.close();
		return tmp;
	}



	public static String get_project_last_classified_item_id(String active_project) throws ClassNotFoundException, SQLException {
		
		try{
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select item_id from "+active_project+".project_classification_event where classification_method = 'MANUAL' and classification_time =  (select max (classification_time) from "+active_project+".project_classification_event where classification_method = 'MANUAL')");
			rs.next();
			String ret = rs.getString("item_id");
			rs.close();
			stmt.close();
			conn.close();
			return ret;
		}catch(Exception V) {
			return null;
		}
		
	}
	
	public static String get_project_name(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select project_name from administration.projects where project_id = '"+active_project+"'");
		rs.next();
		String ret = rs.getString("project_name");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}



	public static void StoreRule(UserAccount account, GenericRule gr, ArrayList<String[]> itemRuleMap, boolean active, String METHOD) {
		Task<Void> task = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				
				Connection conn = Tools.spawn_connection();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO "+account.getActive_project()+".project_rules(" + 
						"            rule_id, main, application, complement, material_group," + 
						"            pre_classification, drawing, class_id, rule_source, rule_type," + 
						"            user_id, rule_date, active_status)" + 
						"    VALUES (?, ?, ?, ?, ?," + 
						"            ?, ?, ?, ?, ?," + 
						"            ?, clock_timestamp() ,?) on conflict(rule_id) do update set active_status = EXCLUDED.active_status, class_id = EXCLUDED.class_id;");
				ps.setString(1, gr.toString());
				ps.setString(2, gr.getMain());
				ps.setString(3, gr.getApp());
				ps.setString(4, gr.getComp());
				ps.setString(5, gr.getMg());
				
				ps.setString(6, gr.getPc());
				ps.setBoolean(7, gr.getDwg());
				ps.setString(8, String.join("&&&", gr.classif));
				ps.setString(9, METHOD);
				ps.setString(10, gr.getType());
				
				ps.setString(11, account.getUser_id());
				//ps.setDate(12, x);
				ps.setBoolean(12, active);
				ps.execute();
				ps.close();
				
				final PreparedStatement ps2 = conn.prepareStatement("INSERT INTO "+account.getActive_project()+".project_items_x_rules(" + 
						"            item_id, rule_id)" + 
						"    VALUES (?, ?) on conflict(item_id,rule_id) do nothing;");
				itemRuleMap.forEach((A)->{
					String item_id = A[0];
					String rule_id = A[1];
					try {
						ps2.setString(1, item_id);
						ps2.setString(2, rule_id);
						ps2.addBatch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				
				ps2.executeBatch();
				ps2.clearBatch();
				ps2.close();
				
				conn.close();
				
				
	    		return null;
		    	}
			};
		task.setOnSucceeded(e -> {
			;

			
			});

		task.setOnFailed(e -> {
		    Throwable problem = task.getException();
		    /* code to execute if task throws exception */
		    System.err.println("failed to sync rules ");
		    //System.err.println(ExceptionUtils.getRootCauseMessage( problem ))	;
		    problem.printStackTrace(System.err);
		    
		    
		});

		task.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
			
			Thread thread = new Thread(task);; thread.setDaemon(true);
			thread.setName("Sync db for rule "+gr.toString());
			thread.start();
			
		}

	public static void StoreAutoRules(UserAccount account, ArrayList<GenericRule> grs, ArrayList<String[]> itemRuleMap, boolean ISCLASSIF, String METHOD) {
		/*
		 * Set classif rule to active if conflict
		 * Do nothing for preclassif rule if conflict
		 */
		Task<Void> task = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				
				Connection conn = Tools.spawn_connection();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO "+account.getActive_project()+".project_rules(" + 
						"            rule_id, main, application, complement, material_group," + 
						"            pre_classification, drawing, class_id, rule_source, rule_type," + 
						"            user_id, rule_date, active_status)" + 
						"    VALUES (?, ?, ?, ?, ?," + 
						"            ?, ?, ?, ?, ?," + 
						"            ?, clock_timestamp() ,?) on conflict(rule_id) do "
						+ (ISCLASSIF?"update set active_status = EXCLUDED.active_status, class_id = EXCLUDED.class_id;":"nothing"));
				
				for(GenericRule gr:grs) {
					ps.setString(1, gr.toString());
					ps.setString(2, gr.getMain());
					ps.setString(3, gr.getApp());
					ps.setString(4, gr.getComp());
					ps.setString(5, gr.getMg());
					
					ps.setString(6, gr.getPc());
					ps.setBoolean(7, gr.getDwg());
					ps.setString(8, String.join("&&&", gr.classif));
					ps.setString(9, METHOD);
					ps.setString(10, gr.getType());
					
					ps.setString(11, account.getUser_id());
					//ps.setDate(12, x);
					ps.setBoolean(12, ISCLASSIF);
					ps.addBatch();
				}
					ps.executeBatch();
					ps.clearBatch();
					ps.close();
				
				final PreparedStatement ps2 = conn.prepareStatement("INSERT INTO "+account.getActive_project()+".project_items_x_rules(" + 
						"            item_id, rule_id,rule_application_description_form)" + 
						"    VALUES (?,?,?) on conflict(item_id,rule_id) do update set rule_application_description_form = EXCLUDED.rule_application_description_form;");
				itemRuleMap.forEach((A)->{
					String item_id = A[0];
					String rule_id = A[1];
					try {
						ps2.setString(1, item_id);
						ps2.setString(2, rule_id);
						ps2.setString(3, "["+METHOD+"] "+rule_id);
						ps2.addBatch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				
				ps2.executeBatch();
				ps2.clearBatch();
				ps2.close();
				
				conn.close();
				
				
	    		return null;
		    	}
			};
		task.setOnSucceeded(e -> {
			;

			
			});

		task.setOnFailed(e -> {
		    Throwable problem = task.getException();
		    /* code to execute if task throws exception */
		    System.err.println("failed to sync rules ");
		    //System.err.println(ExceptionUtils.getRootCauseMessage( problem ))	;
		    problem.printStackTrace(System.err);
		    
		    
		});

		task.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
			
			Thread thread = new Thread(task);; thread.setDaemon(true);
			thread.setName("Sync db for rules ");
			thread.start();
			
		}

	public static String[] get_desc_classes(UserAccount account, MenuBar menubar) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("select user_description_classes from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String[] ret = null;
		try{
			ret = (String[]) rs.getArray("user_description_classes").getArray();
			
		}catch(Exception V) {
			//User has no assigned classes, load all available classes
			rs.close();
			stmt.close();
			stmt = conn.prepareStatement("select distinct segment_id from "+account.getActive_project()+".project_classification_event where segment_id is not null");
			rs = stmt.executeQuery();
			ArrayList<String> tmp = new ArrayList<String>();
			while(rs.next()) {
				tmp.add(rs.getString("segment_id"));
			}
			ret = new String[tmp.size()];
			for(int i=0;i<tmp.size();i++) {
				ret[i] = tmp.get(i);
				
			}
			
		}
		
		rs.close();
		stmt.close();
		conn.close();
		return ret;
	}



	public static String get_desc_class(UserAccount account) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		PreparedStatement stmt = conn.prepareStatement("select user_description_class from administration.users_x_projects where project_id = ? and user_id = ?");
		stmt.setString(1, account.getActive_project());
		stmt.setString(2, account.getUser_id());
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String ret = rs.getString("user_description_class");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}



	public static ArrayList<String> SET_PROJECT_CLASSES_ARRAY( UserAccount account) throws ClassNotFoundException, SQLException {
		ArrayList<String> CNAME_CID = new ArrayList<String>();
		Connection conn = Tools.spawn_connection();
		Statement st = conn.createStatement();
		ResultSet rs;
		int granularity = Tools.get_project_granularity(account.getActive_project());
		rs = st.executeQuery("select segment_id,level_"+granularity+"_name_translated,level_"+granularity+"_number from "+account.getActive_project()+".project_segments");
		while(rs.next()) {
			CNAME_CID.add(rs.getString(1)+"&&&"+rs.getString(2)+"&&&"+rs.getString(3));
		}
		rs.close();
		st.close();
		conn.close();
		return CNAME_CID;
	}



	//Method is always manual
	public static void CharDescriptionRow2ClassEvent(List<CharDescriptionRow> rows, UserAccount account,
			String METHOD) {
		Task<Void> task = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
			Connection conn = Tools.spawn_connection();
			//Connection conn2 = Tools.spawn_connection();
			
			PreparedStatement stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_classification_event("
					+ "classification_event_id,item_id,segment_id,classification_method,rule_id,user_id,classification_date,classification_time) values("
					+ "?,?,?,?,?,?,?,clock_timestamp())");
			
			//PreparedStatement stmt2 = conn2.prepareStatement("insert into administration.wal values (?, clock_timestamp())");
			
			
			for(CharDescriptionRow tmp:rows) {
				
				String eventid = Tools.generate_uuid();
				stmt.setString(1, eventid);
				stmt.setString(2, tmp.getItem_id());
				//Only possible branch : the user changed the manual classification
				stmt.setString(3, tmp.getClass_segment().split("&&&")[0]);
				stmt.setString(4, METHOD);
				stmt.setString(5, null);
				stmt.setString(6, account.getUser_id());
				stmt.setDate(7, new java.sql.Date((new java.util.Date()).getTime()));
				;
				stmt.addBatch();
				
				
				//stmt2.setString(1, eventid);
				//stmt2.addBatch();
			}
				stmt.executeBatch();
				stmt.clearBatch();
				stmt.close();
				
				//stmt2.executeBatch();
				//stmt2.clearBatch();
				//stmt2.close();
				
				conn.close();
				//conn2.close();
				
	    		return null;
		    }
			};
			task.setOnSucceeded(e -> {
				;

				
				});

			task.setOnFailed(e -> {
			    Throwable problem = task.getException();
			    /* code to execute if task throws exception */
			    System.err.println("failed to sync items ");
			    //System.err.println(ExceptionUtils.getRootCauseMessage( problem ))	;
			    problem.printStackTrace(System.err);
			    
			    
			});

			task.setOnCancelled(e -> {
			    /* task was cancelled */
				;
			});
			
			Thread thread = new Thread(task);; thread.setDaemon(true);
			thread.setName("Sync DB for method "+METHOD);
			thread.start();
	}



	
	public static String get_project_data_language(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select data_language from projects where project_id ='"+active_project+"'");
		rs.next();
		String ret = rs.getString("data_language");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}


	public static String get_project_user_language(String active_project) throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select classifier_language from projects where project_id ='"+active_project+"'");
		rs.next();
		String ret = rs.getString("classifier_language");
		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}



	public static void moveItemInCollection(int sourceIndex, int targetIndex, ObservableList<Node> list) {
		if (sourceIndex <= targetIndex) {
	        Collections.rotate(list.subList(sourceIndex, targetIndex + 1), -1);
	    } else {
	        Collections.rotate(list.subList(targetIndex, sourceIndex + 1), 1);
	    }
	}
}
