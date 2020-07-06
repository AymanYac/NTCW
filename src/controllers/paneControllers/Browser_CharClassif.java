package controllers.paneControllers;

import controllers.Char_description;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.json.simple.parser.ParseException;
import transversal.pdf_toolbox.PdfCapableBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;


public class Browser_CharClassif {
	Char_description parent;
	//private WebView browser;
	private PdfCapableBrowser browser;
	private VBox search_window;
	private AnchorPane search_anchor;
	PdfCapableBrowser pdfdsp;
	
	@FXML GridPane toolBar;
	@FXML Button frontButton;
	@FXML Button backButton;
	@FXML Button favButton;
	@FXML Button unfavButton;
	@FXML public Button closeButton;
	
	@FXML void initialize() {
		//browser = new WebView();
		browser = new PdfCapableBrowser();//browser.getEngine();
		unfavButton.setVisible(false);
	}
	
	@FXML void browse_previous_page() {
		browser.nodeValue.getEngine().executeScript("history.back()");
	}
	
	@FXML void browse_next_page() {
		browser.nodeValue.getEngine().executeScript("history.forward()");
	}
	
	@FXML void unFavPage() {
		unfavButton.setVisible(false);
	}
	
	@FXML void FavPage() {
		unfavButton.setVisible(true);
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
		
		browser.loadPage("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8"));
		//browser.loadPage(getClass().getResource("/scripts/100RV.pdf").toExternalForm());
		//browser.loadPage("http://www.africau.edu/images/default/sample.pdf");
		//browser.displayPdf(new URL("http://www.africau.edu/images/default/sample.pdf"));

		//browser.displayPdf(new URL("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8")));

		//icePdfBench(new URL("http://www.africau.edu/images/default/sample.pdf"));
		//icePdfBench(new URL("https://www.nexans.gm/Morocco/family/doc/en/U_1000_R2V.pdf"));
		//icePdfBench(new URL("https://www.nexans.fr/France/2018/Referentiel_Cables_Liste_Prix.pdf"));
		//icePdfBench(new URL("https://assets.new.siemens.com/siemens/assets/api/uuid:5644c620-1ca1-4f77-807d-d72aeac5cc23/version:1560766252/produits-d-alimentation.pdf"));





	}

	private void icePdfBench(URL url) {
		//String filePath = getClass().getResource("/scripts/100RV.pdf").toExternalForm();
		//filePath = getClass().getResource("/scripts/SSRV.pdf").toExternalForm();

		// build a component controller
		SwingController controller = new SwingController();

		SwingViewBuilder factory = new SwingViewBuilder(controller);

		JPanel viewerComponentPanel = factory.buildViewerPanel();

		// add interactive mouse link annotation support via callback
		controller.getDocumentViewController().setAnnotationCallback(
				new org.icepdf.ri.common.MyAnnotationCallback(
						controller.getDocumentViewController()));

		JFrame applicationFrame = new JFrame();
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		applicationFrame.getContentPane().add(viewerComponentPanel);

		// Now that the GUI is all in place, we can try opening a PDF
		controller.openDocument(url);

		// show the component
		applicationFrame.pack();
		applicationFrame.setVisible(true);
	}

	private void setContainerWindow() {
		if(!(search_window!=null)) {
			search_window = new VBox();
			//search_window.setPadding(new Insets(5));
			//search_window.setSpacing(5);
			search_window.getChildren().add(browser.toNode());

			search_anchor = new AnchorPane();
			search_anchor.getChildren().add(search_window);
			search_anchor.setBottomAnchor(search_window, (double) 0);
			search_anchor.setTopAnchor(search_window, (double) 0);
			search_anchor.setLeftAnchor(search_window, (double) 0);
			search_anchor.setRightAnchor(search_window, (double) 0);

			parent.leftAnchor.getChildren().add(search_anchor);
			parent.leftAnchor.setBottomAnchor(search_anchor, (double) 0);
			parent.leftAnchor.setTopAnchor(search_anchor, (double) 0);
			parent.leftAnchor.setLeftAnchor(search_anchor, (double) 0);
			parent.leftAnchor.setRightAnchor(search_anchor, (double) 0);

			parent.leftAnchor.getChildren().add(toolBar);
			parent.leftAnchor.setRightAnchor(toolBar, 5.0);
			parent.leftAnchor.setTopAnchor(toolBar, 5.0);

			search_anchor.setOnKeyPressed(new EventHandler<KeyEvent>() {
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

		switch_pane_hide_browser(false);
	}


	@FXML void hide_browser() throws IOException, ParseException {
		parent.show_table();
	}

	public void switch_pane_hide_browser(boolean bool) {
		toolBar.setVisible(!bool);
		search_anchor.setVisible(!bool);
		parent.tableController.tableGrid.setVisible(bool);
	}





	public void setParent(Char_description char_description) {
		this.parent=char_description;
	}
}
