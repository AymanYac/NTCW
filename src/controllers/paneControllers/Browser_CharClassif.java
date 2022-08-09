package controllers.paneControllers;

import com.sun.javafx.geom.Rectangle;
import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.GlobalConstants;
import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.views.DocumentViewController;
import org.icepdf.ri.common.SwingController;
import org.json.simple.parser.ParseException;
import service.CharPatternServices;
import service.CharValuesLoader;
import service.ExternalSearchServices;
import service.DocumentSearchTask;
import transversal.generic.TextUtils;
import transversal.generic.Tools;
import transversal.pdf_toolbox.PdfCapableBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Vector;


public class Browser_CharClassif {
	public Char_description parent;
	//private WebView browser;
	public PdfCapableBrowser browser;
	public SimpleBooleanProperty showingPdf = new SimpleBooleanProperty();
	public SimpleBooleanProperty nextPageIsPdf = new SimpleBooleanProperty();
	public JPanel iceFrame;
	public SwingController iceController;



	@FXML public GridPane toolBar;
	@FXML ToolBar iconBar;

	@FXML Button zoomInButton;
	@FXML Button zoomOutButton;
	@FXML Button pageFitButton;
	@FXML Button pageWidthButton;
	@FXML Label pageLabel1;
	@FXML public TextField pageField;
	@FXML public Label pageLabel2;

	@FXML TextField searchField;
	@FXML Label searchLabel;
	@FXML Button searchNextButton;
	@FXML Button searchPreviousButton;


	@FXML Button paneSmallButton;
	@FXML Button paneBigButton;
	@FXML Button paneNewButton;
	@FXML Button externalBrowserButton;

	@FXML Button browsePreviousPageButton;
	@FXML Button browseNextPageButton;
	@FXML Button refreshPageButton;
	@FXML Button closeButton;

	public Stage secondaryStage;
	private DocumentSearchTask.SearchTextTask dtsk;
	private int searchHitIndex=0;
	private SwingNode iceContainer;
	private String lastPaneLayout;
	private String beforeExternalPaneLayout;


	@FXML void initialize() {
		//browser = new WebView();
		showingPdf.setValue(false);
		browser = new PdfCapableBrowser();//browser.getEngine();
		browser.setParent(this);


	}
	
	@FXML void browse_previous_page() {
		if(showingPdf.getValue()){
			showingPdf.setValue(false);
			nextPageIsPdf.setValue(true);
			browser.nodeValue.getEngine().executeScript("history.back()");
		}else{
			nextPageIsPdf.setValue(false);
			browser.nodeValue.getEngine().executeScript("history.back()");
		}

	}
	
	@FXML void browse_next_page() {
		if(nextPageIsPdf.getValue()){
			showingPdf.setValue(true);
		}
		browser.nodeValue.getEngine().executeScript("history.forward()");
	}
	@FXML void refresh_page() {
		ExternalSearchServices.refreshingBrowser(browser.nodeValue.getEngine().getLocation());
		browser.nodeValue.getEngine().reload();
	}
	

	
	
	@SuppressWarnings("static-access")
	public void search_google_inplace(boolean checkMethodSelect) throws IOException {

		setContainerWindow();

		String selected_text = "";
		if(checkMethodSelect) {
			selected_text = TextUtils.getSelectedText(parent.ld);
			if(selected_text.length()==0) {
				selected_text = TextUtils.getSelectedText(parent.ld_translated);
				if(selected_text.length()==0) {
					selected_text = TextUtils.getSelectedText(parent.sd);
					if(selected_text.length()==0) {
						selected_text = TextUtils.getSelectedText(parent.sd_translated);
					}
				}
			}
		}
		String target = parent.search_text.getText();
		target = (checkMethodSelect && selected_text.length()>0) ?selected_text:target;

		ExternalSearchServices.launchingSearch("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8"));
		browser.loadPage("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8"));

	}


	public void setContainerWindow() {

		nextPageIsPdf.setValue(false);
		showingPdf.setValue(false);

		try{
			Tools.deleteRow(toolBar,1);
		}catch (Exception V){

		}
		if(!GlobalConstants.JAVASCRIPT_PDF_RENDER) {
			if(!(iceController!=null)){
				iceController = browser.loadIceController(iceFrame,iceContainer);
				browser.toNode().visibleProperty().bind(showingPdf.not());
			}
			toolBar.add(iceContainer,0,1,GridPane.REMAINING,GridPane.REMAINING);

		}else{
			toolBar.add(browser.toNode(),0,1,GridPane.REMAINING,GridPane.REMAINING);
		}

		pageFitButton.disableProperty().bind(showingPdf.not());
		pageWidthButton.disableProperty().bind(showingPdf.not());
		zoomInButton.disableProperty().bind(showingPdf.not());
		zoomOutButton.disableProperty().bind(showingPdf.not());
		pageLabel1.visibleProperty().bind(showingPdf);
		pageField.visibleProperty().bind(showingPdf);
		pageLabel2.visibleProperty().bind(showingPdf);
		parent.leftAnchor.getChildren().add(toolBar);
		parent.leftAnchor.setLeftAnchor(toolBar, 0.0);
		parent.leftAnchor.setTopAnchor(toolBar, 0.0);
		parent.leftAnchor.setRightAnchor(toolBar,0.0);
		parent.leftAnchor.setBottomAnchor(toolBar,0.0);
		searchLabel.visibleProperty().bind(searchField.textProperty().length().greaterThan(0));

		try{
			secondaryStage.close();
		}catch (Exception V){

		}
		switch_pane_hide_browser(false);
		try {
			loadLastPaneLayout();
		} catch (ParseException | IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		parent.ruleButton.setSelected(false);
		parent.charButton.setSelected(false);
		parent.imageButton.setSelected(false);
		if(lastPaneLayout!=null && lastPaneLayout.equals("BIG")){
			return;
		}
		if(parent.lastRightPane.equals("RULES")){
			try {
				parent.view_rules();
				parent.ruleButton.setSelected(true);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
		if(parent.lastRightPane.equals("CHARS")){
			try {
				parent.view_chars();
				parent.charButton.setSelected(true);
			} catch (IOException | ParseException | ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		if(parent.lastRightPane.equals("IMAGES")){
			try {
				parent.search_image();
				parent.imageButton.setSelected(true);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}

	}

	private void setSearchFieldListener() {
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			searchField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					browser.toNode().getEngine().executeScript("findSentence('" + newValue + "');");
				}});
		}else{
			searchField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					try {
						dtsk.stop();
					}catch (Exception ignored){
					}
					searchHitIndex=0;
					dtsk = new DocumentSearchTask.SearchTextTask(iceController,newValue,false,false,false);
					dtsk.done.addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							if(newValue){
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										iceController.getDocumentViewController().getViewContainer().repaint();
										if(dtsk.hitResults.size()==0){
											searchNextButton.setDisable(true);
											searchPreviousButton.setDisable(true);
											searchLabel.setText("0/0");
										}else{
											searchNextButton.setDisable(false);
											searchPreviousButton.setDisable(false);
											scrollToSearchHit(dtsk.hitResults.get(0));
										}
									}
								});
							}
						}
					});
					dtsk.totalHitCount.addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									searchLabel.setText(String.valueOf(dtsk.hitResults.getCircularizedIndex(searchHitIndex)+1)+"/"+newValue.toString());
								}
							});
						}
					});
					dtsk.go();
				}
			});
		}
	}


	@FXML void hide_browser() throws IOException, ParseException {
		ExternalSearchServices.closingBrowser();
		String copyLastLayout = lastPaneLayout;
		paneSmall();
		lastPaneLayout = copyLastLayout;
		boolean closingBrowser = false;
		if(toolBar.isVisible()){
			closingBrowser=true;
		}
		System.out.println("hide_browser()");
		System.out.println("Closing browser >"+String.valueOf(closingBrowser));
		switch_pane_hide_browser(true);
		parent.ruleButton.setSelected(false);
		parent.charButton.setSelected(false);
		parent.imageButton.setSelected(false);
		if(closingBrowser){
			if(parent.lastRightPane.equals("RULES")){
				try {
					parent.view_rules();
					parent.ruleButton.setSelected(true);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
			if(parent.lastRightPane.equals("CHARS")){
				try {
					parent.view_chars();
					parent.charButton.setSelected(true);
				} catch (IOException | ParseException | ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
			}
			if(parent.lastRightPane.equals("IMAGES")){
				try {
					parent.search_image();
					parent.imageButton.setSelected(true);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
		}else{
			parent.lastRightPane="";
		}
	}

	public void switch_pane_hide_browser(boolean bool) {
		toolBar.setVisible(!bool);
		parent.tableController.charDescriptionTable.setVisible(bool);
	}

	private void loadLastPaneLayout() throws ParseException, IOException, URISyntaxException {

		if(GlobalConstants.SEARCH_PANE_LAYOUT_FORCE!=null){
			lastPaneLayout = GlobalConstants.SEARCH_PANE_LAYOUT_FORCE;
		}
		if(lastPaneLayout!=null){
			if(lastPaneLayout.equals("NEW")){
				paneNew();
			}else if(lastPaneLayout.equals("SMALL")){
				paneSmall();
			}else if(lastPaneLayout.equals("BIG")){
				paneBig();
			}else if(lastPaneLayout.equals("EXTERNAL")){
				if(beforeExternalPaneLayout !=null){
					if(beforeExternalPaneLayout.equals("NEW")){
						paneNew();
					}else if(beforeExternalPaneLayout.equals("SMALL")){
						paneSmall();
					}else if(beforeExternalPaneLayout.equals("BIG")){
						paneBig();
					}
				}
				externalBrowser();
			}
		}
	}


	@FXML void zoomIn(){
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			browser.toNode().getEngine().executeScript("zoomIn()");
		}else{
			iceController.getDocumentViewController().setZoom((float) (iceController.getUserZoom()*1.1));
		}
	}
	@FXML void zoomOut(){
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			browser.toNode().getEngine().executeScript("zoomOut()");
		}else{
			iceController.getDocumentViewController().setZoom((float) (iceController.getUserZoom()*0.9));
		}

	}
	@FXML void pageFit(){
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			browser.toNode().getEngine().executeScript("pageFit()");
		}else{
			iceController.getDocumentViewController().setFitMode(DocumentViewController.PAGE_FIT_WINDOW_HEIGHT);
		}
	}
	@FXML void pageWidth(){
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			browser.toNode().getEngine().executeScript("pageWidth()");
		}else{
			iceController.getDocumentViewController().setFitMode(DocumentViewController.PAGE_FIT_WINDOW_WIDTH);
		}
	}
	@FXML void goToUserPage(KeyEvent event){
		if(event.getCode().equals(KeyCode.ENTER)){
			if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
				browser.toNode().getEngine().executeScript("goToPage("+Integer.valueOf(pageField.getText())+")");
			}else{
				iceController.getDocumentViewController().setCurrentPageIndex(Integer.valueOf(pageField.getText())-1);
			}
		}
	}


	private void scrollToSearchHit(Pair<Integer, LineText> integerLineTextPair) {
		Vector<Object> v = new Vector<Object>();
		v.add(iceController.getDocument().getPageTree().getPageReference(integerLineTextPair.getKey()));
		v.add(Destination.TYPE_XYZ);
		v.add(integerLineTextPair.getValue().getBounds().x);
		v.add(integerLineTextPair.getValue().getBounds().y);
		v.add(null);
		Destination azd = new Destination(iceController.getDocument().getCatalog().getLibrary(),v);
		iceController.getDocumentViewController().setDestinationTarget(azd);
		searchLabel.setText(String.valueOf(dtsk.hitResults.getCircularizedIndex(searchHitIndex)+1)+"/"+String.valueOf(dtsk.totalHitCount.get()));
	}

	@FXML void searchNext(){
		if(searchNextButton.isDisabled()){
			return;
		}
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
			browser.toNode().getEngine().executeScript("findNext();");
		}else{
			searchHitIndex+=1;
			scrollToSearchHit(dtsk.hitResults.get(searchHitIndex));
		}
	}
	@FXML void searchPrevious(){
		if(GlobalConstants.JAVASCRIPT_PDF_RENDER){

		}else{
			searchHitIndex-=1;
			scrollToSearchHit(dtsk.hitResults.get(searchHitIndex));
		}

	}

	@FXML void paneSmall(){
		lastPaneLayout="SMALL";
		if(!parent.leftAnchor.getChildren().stream().anyMatch(e->e.equals(toolBar))){
			parent.leftAnchor.getChildren().add(toolBar);
			parent.leftAnchor.setLeftAnchor(toolBar, 0.0);
			parent.leftAnchor.setTopAnchor(toolBar, 0.0);
			parent.leftAnchor.setRightAnchor(toolBar,0.0);
			parent.leftAnchor.setBottomAnchor(toolBar,0.0);

		}
		try{
			secondaryStage.close();
		}catch (Exception V){

		}
		parent.setBottomRegionColumnSpans(false);
	}
	@FXML void paneBig(){
		lastPaneLayout="BIG";
		if(!parent.leftAnchor.getChildren().stream().anyMatch(e->e.equals(toolBar))){
			parent.leftAnchor.getChildren().add(toolBar);
			parent.leftAnchor.setLeftAnchor(toolBar, 0.0);
			parent.leftAnchor.setTopAnchor(toolBar, 0.0);
			parent.leftAnchor.setRightAnchor(toolBar,0.0);
			parent.leftAnchor.setBottomAnchor(toolBar,0.0);

		}
		try{
			secondaryStage.close();
		}catch (Exception V){

		}
		parent.setBrowserFullScreen();
	}
	@FXML void paneNew(){
		paneSmall();
		lastPaneLayout="NEW";
		GridPane secondaryLayout = new GridPane();
		secondaryLayout.setMinWidth(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.setMaxWidth(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.setMinHeight(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.setMaxHeight(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
		secondaryLayout.add(toolBar,0,1);
		ToolBar headerBar = new ToolBar();
		headerBar.setMinWidth(GridPane.USE_COMPUTED_SIZE);
		headerBar.setMaxWidth(GridPane.USE_COMPUTED_SIZE);
		headerBar.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
		headerBar.setMinHeight(GridPane.USE_COMPUTED_SIZE);
		headerBar.setMaxHeight(GridPane.USE_COMPUTED_SIZE);
		headerBar.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
		Button close = new Button("X");
		close.getStylesheets().add(Browser_CharClassif.class.getResource("/styles/CloseButtonRed.css").toExternalForm());
		close.setAlignment(Pos.CENTER_RIGHT);
		close.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				ExternalSearchServices.closingBrowser();
				paneSmall();
				lastPaneLayout="NEW";
				secondaryStage.close();
				switch_pane_hide_browser(true);
			}
		});
		//Button icon = new Button();
		//ImageView iconImage = new ImageView(new Image(getClass().getResourceAsStream("/pictures/NEONEC_FAV.ico")));
		//iconImage.setFitHeight(close.getHeight());
		//iconImage.setFitWidth(close.getWidth());
		//icon.setGraphic(iconImage);
		//icon.setAlignment(Pos.CENTER_LEFT);

		Label title = new Label("Neonec classification wizard - V"+ GlobalConstants.TOOL_VERSION+" - Web Browser");
		title.setAlignment(Pos.CENTER_LEFT);
		Pane verticalSpace = new Pane();
		HBox.setHgrow(verticalSpace, Priority.ALWAYS);


		headerBar.getItems().setAll(title,verticalSpace,close);
		headerBar.getStylesheets().add(Browser_CharClassif.class.getResource("/styles/Main.css").toExternalForm());
		GridPane.setValignment(headerBar, VPos.TOP);
		secondaryLayout.add(headerBar,0,0);

		Rectangle dragDelta = new Rectangle();
		headerBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = (int) Math.floor(secondaryLayout.getScene().getWindow().getX() - mouseEvent.getScreenX());
				dragDelta.y = (int) Math.floor(secondaryLayout.getScene().getWindow().getY() - mouseEvent.getScreenY());
			}
		});
		headerBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				secondaryLayout.getScene().getWindow().setX(mouseEvent.getScreenX() + dragDelta.x);
				secondaryLayout.getScene().getWindow().setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});
		RowConstraints R1 = new RowConstraints();
		R1.setPercentHeight(5);
		RowConstraints R2 = new RowConstraints();
		R2.setPercentHeight(95);
		secondaryLayout.getRowConstraints().setAll(R1,R2);
		ColumnConstraints C0 = new ColumnConstraints();
		C0.setPercentWidth(100);
		secondaryLayout.getColumnConstraints().setAll(C0);

		//secondaryLayout.getChildren().add(toolBar);
		Scene secondScene = new Scene(secondaryLayout);
		secondaryStage = new Stage();
		secondaryStage.setScene(secondScene);
		secondaryStage.initStyle(StageStyle.UNDECORATED);
		secondaryStage.setMaximized(true);

		secondScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ESCAPE)){
					try {
						hide_browser();
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
				}
				if(event.getCode().equals(KeyCode.ENTER)){
					if(event.isControlDown()){
						System.out.println("CTRL+ENTER");
						((Stage)parent.charButton.getScene().getWindow()).toFront();
						parent.value_field.requestFocus();
						int active_char_index = Math.floorMod(parent.tableController.selected_col, CharValuesLoader.active_characteristics.get(parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0]).size());
						try{
							parent.proposer.clearPropButtons();
							String selectedText=parent.proposer.getUserSelectedText();
							CharPatternServices.scanSelectionForPatternDetection(parent,
									CharValuesLoader.active_characteristics.get(parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getClass_segment_string().split("&&&")[0])
											.get(active_char_index),selectedText);
						}catch (Exception V){
							V.printStackTrace(System.err);
						}
					}
				}
			}
		});
		secondaryStage.show();
		parent.tableController.charDescriptionTable.setVisible(true);
	}
	public void setParent(Char_description char_description) {
		this.parent=char_description;
		setSearchFieldListener();
		pageField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String numbersOnly = newValue.replaceAll("[^\\d]", "");
				if(numbersOnly.equals(oldValue)){
					return;
				}
				pageField.setText(numbersOnly);
			}
		});
		showingPdf.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue){
					System.out.println("now showing pdf");
					browsePreviousPageButton.setDisable(false);
					browseNextPageButton.setDisable(true);
				}else{
					System.out.println("closed pdf");
					browsePreviousPageButton.setDisable( browser.toNode().getEngine().getHistory().currentIndexProperty().getValue() == 0 );
					browseNextPageButton.setDisable(false);
				}
			}
		});
		browser.toNode().getEngine().getHistory().currentIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				browseNextPageButton.setDisable(newValue.intValue() == browser.toNode().getEngine().getHistory().getEntries().size()-1 );
				browsePreviousPageButton.setDisable(!showingPdf.getValue() && newValue.intValue() == 0 );
			}
		});

	}

	@FXML void externalBrowser() throws IOException, URISyntaxException, ParseException {
		this.beforeExternalPaneLayout =lastPaneLayout;
		lastPaneLayout="EXTERNAL";
		if(showingPdf.getValue()){
			if(GlobalConstants.JAVASCRIPT_PDF_RENDER){
				Desktop.getDesktop().browse(new URL(browser.latestPDFLink).toURI());
			}else{
				Desktop.getDesktop().browse(new URL(browser.toNode().getEngine().getLocation()+"#page="+pageField.getText()).toURI());
			}
		}else{
			Desktop.getDesktop().browse(new URL(browser.toNode().getEngine().getLocation()).toURI());
		}
		//hide_browser();
	}

}
