package transversal.dialog_toolbox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import controllers.Project_parameters;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import model.DataInputMethods;
import model.GenericClassRule;
import model.ItemFetcherRow;
import transversal.generic.Tools;

public class ItemUploadDialog {

	private static boolean ruleApplySucess;

	@SuppressWarnings({ "rawtypes" })
	public static void uploadItems(Project_parameters parent) throws SQLException, ClassNotFoundException {
		
		// Create the custom dialog.
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Updating project item list");
		dialog.getDialogPane().setMinHeight(80);
		dialog.getDialogPane().setMinWidth(640);
		dialog.getDialogPane().getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/DialogPane.css").toExternalForm());
		dialog.getDialogPane().getStyleClass().add("customDialog");
		
		Label progressStage = new Label();
		progressStage.setVisible(false);
		ProgressBar progressBar = new ProgressBar();
		progressBar.getStylesheets().clear();
		progressBar.getStylesheets().add(ItemUploadDialog.class.getResource("/Styles/ProgressBarDarkGreenTransparent.css").toExternalForm());
		progressBar.setMinWidth(1000);
		progressBar.setVisible(false);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.add(progressStage, 0, 0);
		grid.add(progressBar, 0, 1);
		dialog.getDialogPane().setContent(grid);

		dialog.show();
		//Upsert the items
		progressStage.setText("Uploading");
		progressStage.setVisible(true);
		progressBar.setProgress(0);
		ArrayList<String> affectedItemIDs = parent.save_data(progressBar);
		if(affectedItemIDs!=null) {
			if(affectedItemIDs.size()>0) {
				progressBar.setProgress(0);
				List<ItemFetcherRow> databaseSyncLists = new ArrayList<ItemFetcherRow>();
				ArrayList<GenericClassRule> grs = new ArrayList<GenericClassRule>();
				ArrayList<ArrayList<String[]>> itemRuleMaps = new ArrayList<ArrayList<String[]>>();
				ArrayList<Boolean> activeStatuses = new ArrayList<Boolean>();
				ArrayList<String> METHODS = new ArrayList<String>();
				ruleApplySucess=false;
				Task<Void> task = new Task<Void>() {
				    
					@Override
				    protected Void call() throws Exception {

						ruleApplySucess = parent.reEvaluateClassifRules(affectedItemIDs,progressStage,progressBar,grs, itemRuleMaps, activeStatuses, METHODS, databaseSyncLists,DataInputMethods.USER_CLASSIFICATION_RULE);
						
						return null;
						
					}
					};
				task.setOnSucceeded(e -> {
					if(ruleApplySucess) {
						int no_classified_items = itemRuleMaps.stream().flatMap(m->m.stream()).filter(m->m!=null).filter(ir -> ir[1]!=null).map(m->m[0]).collect(Collectors.toSet()).size();
						itemRuleMaps.stream().flatMap(m->m.stream()).filter(m->m!=null).filter(ir -> ir[1]!=null);
						
						progressStage.setText(String.valueOf(no_classified_items)+" of "+String.valueOf(affectedItemIDs.size())+" newly uploaded items have been classified. Do you wish to save?");
						progressBar.setProgress(1);
						// Set the button types.
						ButtonType saveClasses = new ButtonType("Save resuts", ButtonData.OK_DONE);
						ButtonType discardClasses = new ButtonType("Discard", ButtonData.CANCEL_CLOSE);
						
						dialog.getDialogPane().getButtonTypes().addAll(saveClasses, discardClasses);
						Button saveButton = (Button)dialog.getDialogPane().lookupButton(saveClasses);
						Button cancelButton = (Button)dialog.getDialogPane().lookupButton(discardClasses);
						saveButton.setOnAction(a->{
							System.out.println(":::OK:::");
							Tools.StoreRules(parent.account, grs, itemRuleMaps, activeStatuses, METHODS);
							Tools.ItemFetcherRow2ClassEvent(databaseSyncLists,parent.account,DataInputMethods.USER_CLASSIFICATION_RULE);
						});
						cancelButton.setOnAction(a->{
							dialog.close();
						});
						
					}else {
						progressBar.setProgress(1);
						ButtonType discardClasses = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
						dialog.getDialogPane().getButtonTypes().addAll(discardClasses);
						Button cancelButton = (Button)dialog.getDialogPane().lookupButton(discardClasses);
						cancelButton.setOnAction(a->{
							dialog.close();
						});
					}
					
					});

				task.setOnFailed(e -> {
				    Throwable problem = task.getException();
				    /* code to execute if task throws exception */
				    problem.printStackTrace(System.err);
				    progressBar.setProgress(1.0);
					progressStage.setText("Error encountred on rule application.");
					ButtonType closeDialog = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
					dialog.getDialogPane().getButtonTypes().clear();
					dialog.getDialogPane().getButtonTypes().addAll(closeDialog);
					Platform.runLater(()->{
						if(affectedItemIDs!=null) {
							parent.ACCORDION.setExpandedPane(parent.RULES);
						}
					});
				    
				});

				task.setOnCancelled(e -> {
				    /* task was cancelled */
					dialog.close();
					;
				});
					
					Thread thread = new Thread(task);; thread.setDaemon(true);
					thread.setName("Reevaluating rules");
					thread.start();
				
				
				
			}else {
				progressBar.setProgress(1.0);
				progressStage.setText("No new items to upload.");
				ButtonType closeDialog = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
				dialog.getDialogPane().getButtonTypes().clear();
				dialog.getDialogPane().getButtonTypes().addAll(closeDialog);
				Platform.runLater(()->{
					if(affectedItemIDs!=null) {
						parent.ACCORDION.setExpandedPane(parent.RULES);
					}
				});
			}
		}else {
			dialog.close();
		}
		
		
	}

}
