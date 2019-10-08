package controllers.paneControllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.simple.parser.ParseException;

import controllers.Char_description;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Browser_CharClassif {
	Char_description parent;
	private WebView browser;
	private WebEngine webEngine;
	private VBox search_window;
	private AnchorPane search_anchor;
	
	@FXML GridPane toolBar;
	@FXML Button frontButton;
	@FXML Button backButton;
	@FXML Button favButton;
	@FXML Button unfavButton;
	@FXML public Button closeButton;
	
	@FXML void initialize() {
		browser = new WebView();
		webEngine = browser.getEngine();
		unfavButton.setVisible(false);
	}
	
	@FXML void browse_previous_page() {
		webEngine.executeScript("history.back()");
	}
	
	@FXML void browse_next_page() {
		webEngine.executeScript("history.forward()");
	}
	
	@FXML void unFavPage() {
		unfavButton.setVisible(false);
	}
	
	@FXML void FavPage() {
		unfavButton.setVisible(true);
		}

	
	
	
	@SuppressWarnings("static-access")
	public void search_google_inplace(boolean checkMethodSelect) throws UnsupportedEncodingException {
		
		/*if(parent.search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}*/
		
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
		
		webEngine.load("https://www.google.com/search?q="+URLEncoder.encode(target,"UTF-8"));
		
		
		
		if(!(search_window!=null)) {
			search_window = new VBox();
		 	//search_window.setPadding(new Insets(5));
		 	//search_window.setSpacing(5);
		 	search_window.getChildren().add(browser);
		 	
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
