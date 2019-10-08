package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.GlobalConstants;
import model.Project;
import model.ProjectTemplate;
import model.SelectionButton;
import model.UserAccount;
import service.ConcurentTask;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class Project_selection {

	private ArrayList<Project> VIEWTABLE = new ArrayList<Project>();
	
	private String selected_pid;
	private String activated_pid;
	private UserAccount account;
	
	@FXML MenuBar menubar;
	
	@FXML
	private MenuItem Proceed;
	@FXML
	private Button new_project;
	
	@FXML
	public TableView<Project> projectlist;
	@FXML
	private TableColumn<?, ?> colonne1;
	@FXML
	private TableColumn<?, ?> colonne2;
	@FXML
	private TableColumn<?, ?> colonne3;
	@FXML
	private TableColumn<?, ?> colonne4;
	@FXML
	private TableColumn<?, ?> colonne5;
	@FXML
	private TableColumn<?, ?> colonne6;
	@FXML
	private TableColumn<?, ?> colonne7;
	
	@FXML
	private Button suppress_project;
	@FXML
	private Button edit_project;
	
	private Integer target_desc_cardinality;
	private double target_desc_accuracy;
	public Scene scene;

	private Project activated_project;

	private Project selected_project;

	private ConcurentTask triple_dots_task;
	
	
	
	
	
	@FXML
	private void create_new_project() {
		Stage stage = (Stage) new_project.getScene().getWindow();
	    
	    
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
			
		    stage.close();
			
		    
			
		} catch(Exception e) {
			e.printStackTrace(System.err);
			ExceptionDialog.show("FX001 project_parameters", "FX001 project_parameters", "FX001 project_parameters");
		}
	    
	}
	
	@FXML
	private void edit_project() {
		
		//If the user isn't the project manager, quit
		try {
			if(!account.getUser_projects().get(selected_pid).equals("Project Manager")) {
				ExceptionDialog.show("Project setting edition denied", "You can only edit projects for which you assume a project manager role", "");
				return;
			}
		}catch (Exception G) {
			ExceptionDialog.show("Project setting edition denied", "You can only edit projects for which you assume a project manager role", "");
			return;
		}
		
		Stage stage = (Stage) new_project.getScene().getWindow();
	    
	    
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
			controller.setTargetProject(this.selected_project);
			controller.prj = new ProjectTemplate();
		    controller.prj.setPid(selected_pid);
		    controller.setPid(selected_pid);
		    ;
		    ;
		    
		    stage.close();
		    
			
		} catch(Exception e) {
			e.printStackTrace(System.err);
			ExceptionDialog.show("FX001 project_parameters", "FX001 project_parameters", "FX001 project_parameters");
		}
	    
	}

	
	//load the projects that the current user is able to select
	@FXML void initialize(){
		
		Tools.decorate_menubar(menubar,account);
		
		
		//activateButton.setDisable(true);
	}
	
	//Set the activated_project_id to the input project id
	
	void activate_project(String pid) {
		
		//set the selected_pid, activated_pid variables to the input project id
		this.selected_pid= pid;
		this.activated_pid = selected_pid;
		this.account.setActive_project(activated_pid);
		//empty the cardinality of the activated project
		this.target_desc_cardinality = null;

		//loop through the projects present in the selection table
		this.activated_project=null;
		for(Project prj:VIEWTABLE) {
			//if the loop project has the same id of the input project_id
			if(prj.getPid().equals(selected_pid)) {
				this.activated_project = prj;
				//set the cardinality of the activated project to the cardinality of the loop project
				this.target_desc_cardinality=1000*Integer.parseInt(prj.getNoItems().split(" ")[0])+Integer.parseInt(prj.getNoItems().split(" ")[1]);
				//set the desired quality of the activated project to the desired qualit of the loop project
				this.target_desc_accuracy=Double.valueOf(prj.getDesiredQuality());
				
			}else {
				//if the loop projects hasn't the same id of the input project_id, set the corresponding radio button to unselected
				prj.getCircle().setSelected(false);
			}
		}
		
		//Persists the active project in for the user in the database
		try {
			Connection conn = Tools.spawn_connection();
			PreparedStatement stmt = conn.prepareStatement("update administration.users_x_projects set active_project_status = false where user_id = ?");
			stmt.setString(1, account.getUser_id());
			stmt.execute();
			stmt.close();
			
			
			stmt = conn.prepareStatement("insert into administration.users_x_projects(user_id,project_id,active_project_status) values(?,?,?) on conflict(user_id,project_id) do update set active_project_status = true ");
			stmt.setString(1, account.getUser_id());
			stmt.setString(2, this.activated_pid);
			stmt.setBoolean(3, true);
			
			stmt.execute();
			stmt.close();
			conn.close();
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//launch the project_click procedure
		project_click();
	
	}
	
	
	//Triggered when the user clicks the "Suppress project" Button.
		@FXML void suppress_project() {
			//If the user isn't the project manager, quit
			try {
				if(!account.getUser_projects().get(selected_pid).equals("Project Manager")) {
					ExceptionDialog.show("Project setting edition denied", "You can only suppress projects for which you assume a project manager role", "");
					return;
				}
			}catch (Exception G) {
				ExceptionDialog.show("Project setting edition denied", "You can only suppress projects for which you assume a project manager role", "");
				return;
			}
			
			
			
			
			
			
			//If the project has classified items (known by calling the hasCoverage routine)
			if(hasCoverage()) {
				//	Call transversal.dialog_toolbox.ConfirmationDialog
				ConfirmationDialog.show("Confirm action", "Do you whish to supress this project ?", "Delete the project", "Cancel",this,false);
			}else {
				//	Else call directly the suppress() routine
				confirm_supress();
			}
			
		}
	
	

	//updates the selected_pid variable to the project_id of the currently clicked on project
	@FXML void project_click() {
		int i = 0;
		try{
			String selected_pid = projectlist.getSelectionModel().getSelectedItem().getPid();
			this.selected_pid=selected_pid;
			this.selected_project=null;
			for(i=0;i<VIEWTABLE.size();i++) {
				Project prj = VIEWTABLE.get(i);
				//prj.setName(prj.getName().split(" - \\[")[0]);
				if(prj.getPid().equals(selected_pid)) {
					this.selected_project = prj;
					break;
					//prj.setName(prj.getName()+" - [SELECTED]");
				}
			}
			//activateButton.setDisable(false);
			edit_project.setDisable(false);
			suppress_project.setDisable(false);
			//update_projects_list();
		}catch(Exception v) {
			//activateButton.setDisable(true);
		}
		
		/*projectlist.requestFocus();
		projectlist.getSelectionModel().select(i);
		projectlist.getFocusModel().focus(i);*/
		
	}
	//
	@FXML void proceed_to_classif() {
		//if there's currently an actif project, proceed to the classification
		if(this.activated_pid!=null) {
			Stage stage = (Stage) new_project.getScene().getWindow();
		    
		    
		    try {
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
				controller.setTargetProject(this.activated_project);
			    controller.setTarget_desc_cardinality(this.target_desc_cardinality);
				controller.setTarget_desc_accuracy(this.target_desc_accuracy);
			    
				stage.close();
				
			} catch(Exception e) {
				ExceptionDialog.show("FX001 auto_classification_launch", "FX001 auto_classification_launch", "FX001 auto_classification_launch");
				e.printStackTrace(System.err);
			}
		}else {
			//Else raise an alert message dialog
			ExceptionDialog.no_selected_project();
		}
	}
	

	//Fills the screen table view
	@SuppressWarnings("unchecked")
	private void update_projects_list() {
		
		try {
			triple_dots_task.stop();
		}catch(Exception V) {
			
		}
		
		//Remove all the rows already contained in the table
		projectlist.getItems().clear();
		//Set the columns 1 to 7 equivalent name in the Project data-structure
		colonne1.setCellValueFactory(new PropertyValueFactory<>("name"));
		colonne2.setCellValueFactory(new PropertyValueFactory<>("Circle"));
		colonne3.setCellValueFactory(new PropertyValueFactory<>("dataLanguage"));
		colonne4.setCellValueFactory(new PropertyValueFactory<>("noItems"));
		colonne5.setCellValueFactory(new PropertyValueFactory<>("Coverage"));
		colonne6.setCellValueFactory(new PropertyValueFactory<>("Quality"));
		colonne7.setCellValueFactory(new PropertyValueFactory<>("leDate"));
		//Transform all the Project data-structure instances to rows in the table
		projectlist.getItems().addAll(VIEWTABLE);
		//Set column 1 as the default sorted view
		projectlist.getSortOrder().add((TableColumn<Project, ?>) colonne1);
		/*projectlist.getSortOrder().add(colonne2);
		projectlist.getSortOrder().add(colonne3);
		projectlist.getSortOrder().add(colonne4);
		projectlist.getSortOrder().add(colonne5);
		projectlist.getSortOrder().add(colonne6);
		projectlist.getSortOrder().add(colonne7);*/
		
		//TableFilter<Project> tableFilter = new TableFilter<>(projectlist);
		//tableFilter.filterColumn(colonne3);
		//tableFilter.filterColumn(colonne2);
		
		
		
		
	}

	
	//Fills the project list data structure
	@SuppressWarnings("rawtypes")
	public void load_projects_list() {
		
		ArrayList<TableView> tmp = new ArrayList<TableView>(1);
		tmp.add(projectlist);
		;
		try {
			triple_dots_task.stop();
			triple_dots_task = new ConcurentTask(tmp);
		}catch(Exception V) {
			triple_dots_task = new ConcurentTask(tmp);
		}
		
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					
					
					
					
					//Create a first connection to the list the administration schema projects
					Connection conn = Tools.spawn_connection();
					//Get the list of the projects the current user is able to see ( the public projects or the private projects where the user has a role defined and unsupressed)
					Statement st = conn.createStatement();
					String eligible_pids = null;
					ResultSet rs0 = null;
					try{
						eligible_pids = String.join("','", account.getUser_projects().keySet());
						;
						;
						rs0= st.executeQuery("select * from administration.projects where project_name IS NOT NULL and (suppression_status IS NULL or suppression_status is false) and ( project_id in ('"+eligible_pids+"') or (privacy_status IS NOT NULL and privacy_status is FALSE) ) order by last_edit_date ");
					}catch(Exception V) {
						V.printStackTrace();
						;
						rs0= st.executeQuery("select * from administration.projects where project_name IS NOT NULL and (suppression_status IS NULL or suppression_status is false) and ( privacy_status IS NOT NULL and privacy_status is FALSE ) order by last_edit_date ");
					}
					//#
					VIEWTABLE.clear();
					Connection connX = Tools.spawn_connection();
					Statement stX = connX.createStatement();
					ResultSet rsX;
					//Stores the known mappings language_id <-> language_name
					HashMap<String, String> LANGUAGES = WordUtils.load_languages();
					
					while(rs0.next()) {
						
						//For each project
						Project tmp = new Project();
						//Fill the project_id
						tmp.setPid(rs0.getString("project_id"));
						//Fill the project_name
						tmp.setName(rs0.getString("project_name"));
						//Fill the project_data_language
						tmp.setDataLanguage(LANGUAGES.get(rs0.getString("data_language")));
						//Fill the project_classifier_language
						tmp.setClassifierLanguage(LANGUAGES.get(rs0.getString("classifier_language")));
						//Fill the target quality
						tmp.setDesiredQuality(rs0.getDouble("target_quality"));
						//Fill the current quality
						tmp.setQuality(String.valueOf(rs0.getDouble("current_quality")));
						//Fill the latest date of edition
						Date date = rs0.getDate("last_edit_date");
						String stringDate;
						try {
							stringDate = date.toString();
						}catch(Exception E){
							stringDate="";
						}
						tmp.setLeDate(stringDate);
						//Fill the classification_system_id
						tmp.setClassificationSystemId(rs0.getString("classification_system_id"));
						boolean hasData = tmp.getClassificationSystemId()!=null?tmp.getClassificationSystemId().length()>0:false;
						//Fill the privacy status
						tmp.setPrivacyStatus(rs0.getBoolean("privacy_status"));
						//Fill the project's number of levels
						tmp.setNumberOfLevels(rs0.getInt("number_of_levels"));
						//Fill the suppression status
						tmp.setSuppressionStatus(rs0.getBoolean("suppression_status"));
						
						
						try {
							
							//Try to count the number of items
							//#
							
							rsX = stX.executeQuery("select count(*) from "+tmp.getPid()+".project_items");
							rsX.next();
							int no_items = rsX.getInt(1);
							if(no_items>0) {
								tmp.setNoItems(Tools.formatThounsands(no_items));
								
								
								//Try to count the number of classified items (for every classification event get the latest event for each item and count the number of items)
								//select distinct a.item_id, b.segment_id from"
								//#
								//example query select count(*) from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM u95a5fb608e6a47548432695ae0f78968.project_classification_event where item_id in (select distinct item_id from u95a5fb608e6a47548432695ae0f78968.project_items) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  u95a5fb608e6a47548432695ae0f78968.project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join u95a5fb608e6a47548432695ae0f78968.project_items on rich_events.item_id = project_items.item_id
								//rsX = stX.executeQuery("select count(*) from (select item_id , level_4_number,level_1_name_translated,level_2_name_translated,level_3_name_translated,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM "+tmp.getPid()+".project_classification_event where item_id in (select distinct item_id from "+tmp.getPid()+".project_items) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  "+tmp.getPid()+".project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join "+tmp.getPid()+".project_items on rich_events.item_id = project_items.item_id");
								
								rsX = stX.executeQuery("select count(distinct item_id) from "+tmp.getPid()+".project_classification_event where item_id in (select distinct item_id from "+tmp.getPid()+".project_items)"); 
								//Try to set the coverage
								NumberFormat formatter = new DecimalFormat("#0");
								rsX.next();
								tmp.setCoverage(formatter.format((rsX.getInt(1))*100.0/no_items) +" %");
								tmp.setCoveredItems( rsX.getInt(1));
								
								//If the tries succeed set the correct values
								
							}else {
								hasData = false;
								tmp.setNoItems("N/A");
								tmp.setCoverage("N/A");
								tmp.setCoveredItems(0);
							}
							rsX.close();
							
							
							
							
							
						}catch(Exception V) {
							V.printStackTrace(System.err);
							//Otherwise, project has no items associated, set the values to "N/A"
							tmp.setNoItems("N/A");
							tmp.setCoverage("N/A");
						}
						
						
					    //Fill the project activation Button
						SelectionButton circle = new SelectionButton(tmp.getPid());
						//If the item count tries succeed, set the button to activate the project
						if(hasData) {
							circle.selectedProperty().addListener(new ChangeListener<Boolean>() {
							    

								@Override
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
										Boolean newValue) {
									 if (circle.isSelected()) { 
								            //circle.setSelected(false);
								            activate_project(circle.getPid());
								        } else {
								            // ...
								        }
								}
							});
						//Else set the button to show an exception dialog
						}else {
							circle.selectedProperty().addListener(new ChangeListener<Boolean>() {
							    

								@Override
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
										Boolean newValue) {
									 if (circle.isSelected()) { 
								            //circle.setSelected(false);
								            ExceptionDialog.empty_project_selected();
								            circle.setSelected(false);
								        } else {
								            // ...
								        }
								}
							});
						}
						
						tmp.setCircle(circle);
						VIEWTABLE.add(tmp);
						
					}
					//Close the first connection
					rs0.close();
					st.close();
					conn.close();
					//Close the second connection
					stX.close();
					connX.close();
					
							
				} catch (ClassNotFoundException e) {
					ExceptionDialog.show("CF001 pg_class", "CF001 pg_class", "CF001 pg_class");
					e.printStackTrace();
				} catch (SQLException e) {
					ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");
					e.printStackTrace();
				}
				
				//Activate the project previously activated
						if(account.getActive_project()!=null) {
							for(Project row:VIEWTABLE) {
								if(row.getPid().equals(account.getActive_project())) {
									row.getCircle().setSelected(true);
								}
							}
						}
				
				return null;
			}

			
		};

		jfxTask.setOnSucceeded(event -> {
			update_projects_list();
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.setName("LoadingProjectList");
		task.start();
	}

	public void setUserAccount(UserAccount account2) {
		;
		this.account = account2;
		Tools.decorate_menubar(menubar,account);
		
		
		
		
	}
	
	
	
	private boolean hasCoverage() {
		for(Project prj:VIEWTABLE) {
			if(prj.getPid().equals(this.selected_pid)) {
				return prj.getCoveredItems()!=0;
			}
		}
		return false;
	}
	

	public void confirm_supress() {
		;
		ConfirmationDialog.show("Confirm action", "Project suppression can't be undone, proceed ?", "Procede", "Cancel",this,true);
	}
	//Sets this project's suppressed status to True in the database
	public void supress() {
		Connection conn;
		//Create a connection
		try {
			conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			//Update the project by setting its suppressed status column to True
			//#
			stmt.executeUpdate("update administration.projects set suppression_status=true where project_id='"+this.selected_pid+"'");
			//Close the connection
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Refresh the listed projects
		load_projects_list();
	}
	
	
}
