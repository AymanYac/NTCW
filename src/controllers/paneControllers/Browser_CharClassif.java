package controllers.paneControllers;

import com.sun.javafx.geom.Rectangle;
import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.CircularArrayList;
import model.GlobalConstants;
import net.miginfocom.layout.Grid;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.core.views.DocumentViewController;
import org.icepdf.core.views.DocumentViewModel;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.util.PropertiesManager;
import org.json.simple.parser.ParseException;
import service.DocumentSearchTask;
import transversal.pdf_toolbox.PdfCapableBrowser;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.Vector;


public class Browser_CharClassif {
	Char_description parent;
	//private WebView browser;
	private PdfCapableBrowser browser;
	public SimpleBooleanProperty showingPdf = new SimpleBooleanProperty();
	public SimpleBooleanProperty nextPageIsPdf = new SimpleBooleanProperty();
	public JPanel iceFrame;
	public SwingController iceController;



	@FXML GridPane toolBar;
	@FXML ToolBar iconBar;

	@FXML Button zoomInButton;
	@FXML Button zoomOutButton;
	@FXML Button pageFitButton;
	@FXML Button pageWidthButton;
	@FXML Label pageLabel1;
	@FXML TextField pageField;
	@FXML Label pageLabel2;

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

	public Stage secondaryStage;
	private DocumentSearchTask.SearchTextTask dtsk;
	private int searchHitIndex=0;
	private SwingNode iceContainer;


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
	

	
	
	@SuppressWarnings("static-access")
	public void search_google_inplace(boolean checkMethodSelect) throws IOException {

		setContainerWindow();

		String selected_text = "";
		if(checkMethodSelect) {
			selected_text = parent.ld.getSelectedText();
			if(selected_text.length()==0) {
				selected_text = parent.ld_translated.getSelectedText();
				if(selected_text.length()==0) {
					selected_text=parent.sd.getSelectedText();
					if(selected_text.length()==0) {
						selected_text=parent.sd_translated.getSelectedText();
					}
				}
			}
		}
		String target = parent.search_text.getText();
		target = (checkMethodSelect && selected_text.length()>0) ?selected_text:target;

		//icePdfBench(new URL("http://www.africau.edu/images/default/sample.pdf"));
		browser.loadPage("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8"));
		//browser.loadPage(getClass().getResource("/scripts/100RV.pdf").toExternalForm());
		//browser.loadPage("http://www.africau.edu/images/default/sample.pdf");
		//browser.displayPdf(new URL("http://www.africau.edu/images/default/sample.pdf"));

		//browser.displayPdf(new URL("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8")));


		//icePdfBench(new URL("https://www.nexans.gm/Morocco/family/doc/en/U_1000_R2V.pdf"));
		//icePdfBench(new URL("https://www.nexans.fr/France/2018/Referentiel_Cables_Liste_Prix.pdf"));
		//icePdfBench(new URL("https://assets.new.siemens.com/siemens/assets/api/uuid:5644c620-1ca1-4f77-807d-d72aeac5cc23/version:1560766252/produits-d-alimentation.pdf"));





	}

	public void icePdfBench(URL url) {
		//String filePath = getClass().getResource("/scripts/100RV.pdf").toExternalForm();
		//filePath = getClass().getResource("/scripts/SSRV.pdf").toExternalForm();

		//iceFrame.getContentPane().removeAll();
		//iceFrame.getContentPane().add(viewerComponentPanel);

		// Now that the GUI is all in place, we can try opening a PDF
		iceController.openDocument(url);

		// show the component
		//iceFrame.pack();
		showingPdf.setValue(true);
		iceFrame.setVisible(true);
		updateLayoutAfterPageChange();
		try{
			pageLabel2.setText("/"+iceController.getDocument().getNumberOfPages());
		}catch (Exception V){

		}
		iceController.setDocumentToolMode(DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION);


	}

	private void setContainerWindow() {
		if(!(iceController!=null)) {
			//iceFrame = new JFrame();
			//iceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// build a component controller
			iceController = new SwingController();
			PropertiesManager properties =
					new PropertiesManager(System.getProperties(),
							ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));

			// Change the value of a couple default viewer Properties.
			// Note: this should be done before the factory is initialized.
			properties.setBoolean(PropertiesManager.PROPERTY_VIEWPREF_HIDETOOLBAR,Boolean.TRUE);
			properties.setBoolean(PropertiesManager.PROPERTY_VIEWPREF_HIDEMENUBAR,Boolean.TRUE);
			//properties.setBoolean(PropertiesManager.PROPERTY_SHOW_STATUSBAR,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_KEYBOARD_SHORTCUTS,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_STATUSBAR,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_STATUSBAR_STATUSLABEL,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_PAGENAV,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_UTILITY,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ZOOM,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_OPEN,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_PRINT,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_SAVE,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_SEARCH,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_UPANE,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_BOOKMARKS,Boolean.FALSE);
			properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_SEARCH,Boolean.FALSE);

			// add interactive mouse link annotation support via callback
			iceController.getDocumentViewController().setAnnotationCallback(
					new org.icepdf.ri.common.MyAnnotationCallback(
							iceController.getDocumentViewController()));
			//Add page change listener handler
			((DocumentViewControllerImpl) iceController.getDocumentViewController()).addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (PropertyConstants.DOCUMENT_CURRENT_PAGE.equals(evt.getPropertyName())) {
						updateLayoutAfterPageChange();
					}
				}
			});

			SwingViewBuilder factory = new SwingViewBuilder(iceController,properties);

			iceFrame = factory.buildViewerPanel();



			iceFrame.setVisible(false);
			iceContainer = new SwingNode();
			iceContainer.setContent(iceFrame);
			iceContainer.visibleProperty().bind(showingPdf);
			pageFitButton.disableProperty().bind(showingPdf.not());
			pageWidthButton.disableProperty().bind(showingPdf.not());
			zoomInButton.disableProperty().bind(showingPdf.not());
			zoomOutButton.disableProperty().bind(showingPdf.not());
			pageLabel1.visibleProperty().bind(showingPdf);
			pageField.visibleProperty().bind(showingPdf);
			pageLabel2.visibleProperty().bind(showingPdf);
			browser.toNode().visibleProperty().bind(showingPdf.not());
			searchLabel.visibleProperty().bind(searchField.textProperty().length().greaterThan(0));
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

			showingPdf.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(newValue){
						browsePreviousPageButton.setDisable(false);
						browseNextPageButton.setDisable(true);
					}else{
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

			toolBar.add(browser.toNode(),0,1,GridPane.REMAINING,GridPane.REMAINING);
			toolBar.add(iceContainer,0,1,GridPane.REMAINING,GridPane.REMAINING);


			parent.leftAnchor.getChildren().add(toolBar);
			parent.leftAnchor.setLeftAnchor(toolBar, 0.0);
			parent.leftAnchor.setTopAnchor(toolBar, 0.0);
			parent.leftAnchor.setRightAnchor(toolBar,0.0);
			parent.leftAnchor.setBottomAnchor(toolBar,0.0);

			toolBar.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@SuppressWarnings("incomplete-switch")
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
						case ESCAPE:   try {
							parent.show_table();
						} catch (IOException | ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} break;
					}
				}
			});

		}
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
		switch_pane_hide_browser(false);
	}



	@FXML void hide_browser() throws IOException, ParseException {
		parent.show_table();
	}

	public void switch_pane_hide_browser(boolean bool) {
		toolBar.setVisible(!bool);
		parent.tableController.tableGrid.setVisible(bool);
	}


	@FXML void zoomIn(){
		iceController.getDocumentViewController().setZoom((float) (iceController.getUserZoom()*1.1));
	}
	@FXML void zoomOut(){
		iceController.getDocumentViewController().setZoom((float) (iceController.getUserZoom()*0.9));

	}
	@FXML void pageFit(){
		iceController.getDocumentViewController().setFitMode(DocumentViewController.PAGE_FIT_WINDOW_HEIGHT);
	}
	@FXML void pageWidth(){
		iceController.getDocumentViewController().setFitMode(DocumentViewController.PAGE_FIT_WINDOW_WIDTH);
	}
	@FXML void goToUserPage(KeyEvent event){
		if(event.getCode().equals(KeyCode.ENTER)){
			iceController.getDocumentViewController().setCurrentPageIndex(Integer.valueOf(pageField.getText())-1);
		}
	}
	private void updateLayoutAfterPageChange() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				pageField.setText(String.valueOf(iceController.getDocumentViewController().getCurrentPageIndex()+1));
			}
		});
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
		searchHitIndex+=1;
		scrollToSearchHit(dtsk.hitResults.get(searchHitIndex));
	}
	@FXML void searchPrevious(){
		searchHitIndex-=1;
		scrollToSearchHit(dtsk.hitResults.get(searchHitIndex));
	}

	@FXML void paneSmall(){
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
		close.getStylesheets().add(Browser_CharClassif.class.getResource("/Styles/CloseButtonRed.css").toExternalForm());
		close.setAlignment(Pos.CENTER_RIGHT);
		close.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				paneSmall();
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
		headerBar.getStylesheets().add(Browser_CharClassif.class.getResource("/Styles/Main.css").toExternalForm());
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
					secondaryStage.close();
				}
			}
		});
		secondaryStage.show();
		parent.tableController.tableGrid.setVisible(true);
	}
	public void setParent(Char_description char_description) {
		this.parent=char_description;
	}

	@FXML void externalBrowser() throws IOException, URISyntaxException, ParseException {
		Desktop.getDesktop().browse(new URL(browser.toNode().getEngine().getLocation()+(showingPdf.get()?"#page="+pageField.getText():"")).toURI());
		parent.externalBrowserUrlProperty.setValue(browser.toNode().getEngine().getLocation()+(showingPdf.get()?"#page="+pageField.getText():""));
		//hide_browser();

	}

}
