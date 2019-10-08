package controllers;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.GlobalConstants;
import model.ImageResult;
import model.ItemDispatcherRow;
import model.UserAccount;
import service.ItemDispatcher;
import service.Translator;
import transversal.generic.Tools;

public class Rule_definition {

	@FXML MenuBar menubar;
	@FXML TextArea sd;
	@FXML TextArea sd_translated;
	@FXML TextArea ld;
	@FXML TextArea ld_translated;
	@FXML TextField material_group;
	@FXML TextField preclassification;
	
	
	@FXML TextField search_text;
	@FXML Button search_google;
	@FXML Button search_image;
	
	@FXML Region image_1;
	@FXML Region image_2;
	@FXML Region image_3;
	@FXML Region image_4;
	
	@FXML Region image_1_down;
	@FXML Region image_2_down;
	@FXML Region image_3_down;
	@FXML Region image_4_down;
	
	@FXML Region image_1_up;
	@FXML Region image_2_up;
	@FXML Region image_3_up;
	@FXML Region image_4_up;
	
	
	private UserAccount account;
	private ItemDispatcher dsp;
	private final WebView browser = new WebView();
	private final WebEngine webEngine = browser.getEngine();
	private Stage search_stage;
	private VBox search_window;
	private Scene search_scene;
	private String edge_style;
	private String image_style;
	private String user_language_gcode;
		
	//load the projects that the current user is able to select
	@FXML void initialize(){
		sd.setText("");
		sd_translated.setText("");
		ld.setText("");
		ld_translated.setText("");
		material_group.setText("");
		preclassification.setText("");
		
		edge_style = image_1_down.getStyle();
		image_style = image_1.getStyle();
		
		;
		;
		;
		
		
		
		
	}
	
	public void setUserAccount(UserAccount account2) throws ClassNotFoundException, SQLException, IOException {
		;
		this.account = account2;
		Tools.decorate_menubar(menubar,account);
		this.user_language_gcode = Tools.get_project_user_language_code(account.getActive_project());
		this.dsp = new ItemDispatcher(account.getActive_project());
		load_next_item();
		
		
	}

	
	@FXML void load_next_item() throws IOException {
		ItemDispatcherRow tmp = dsp.getNextItem();
		if(tmp!=null) {
			sd.setText(tmp.getShort_description());
			ld.setText(tmp.getLong_description());
			material_group.setText(tmp.getMaterial_group());
			preclassification.setText(tmp.getPreclassifiation());
			
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					try {
						sd_translated.setText(tmp.getShort_description_translated().replace(" ", "").length()>0?tmp.getShort_description_translated():translate2UserLanguage(tmp.getShort_description()));
						ld_translated.setText(tmp.getLong_description_translated().replace(" ", "").length()>0?tmp.getLong_description_translated():translate2UserLanguage(tmp.getLong_description()));
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				});
			
		}else {
			sd.setText("");
			sd_translated.setText("");
			ld.setText("");
			ld_translated.setText("");
			material_group.setText("");
			preclassification.setText("");
		}
		
		
	}
	private String translate2UserLanguage(String description) throws IOException {
		if(description.replace(" ", "").length()==0) {
			return "";
		}
		return Translator.translate("", this.user_language_gcode, description);
	}

	@FXML void search_google() throws MalformedURLException, UnsupportedEncodingException, IOException, URISyntaxException {
		
		if(search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}
		
		
		Desktop.getDesktop().browse(new URL("https://www.google.com/search?q="+URLEncoder.encode(search_text.getText(),"UTF-8")).toURI());
		
	}
	@FXML void search_google_inplace() throws UnsupportedEncodingException {
		
		if(search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}
		
		webEngine.load("https://www.google.com/search?q="+URLEncoder.encode(search_text.getText(),"UTF-8"));
		
		try {
			search_stage.close();
		}catch(Exception V) {
			
		}
	 	search_window = new VBox();
	 	search_window.setPadding(new Insets(5));
	 	search_window.setSpacing(5);
	 	browser.setPrefSize(ld_translated.getScene().getWidth()*0.95, ld_translated.getScene().getHeight());
		
	 	search_window.getChildren().addAll(browser);
	 	
	 	
        search_scene = new Scene(search_window);
        search_scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @SuppressWarnings("incomplete-switch")
			@Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:   search_stage.close(); break;
                }
            }
        });
        
        search_stage = new Stage();
        search_stage.setTitle("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
        search_stage.setScene(search_scene);
        search_stage.setMaximized(true);
        //stage.setWidth(search_text.getScene().getWidth());
        //stage.setHeight(search_text.getScene().getHeight());
 
        search_stage.show();
        
        
	
	}
	
	@FXML void search_image() throws IOException, ParseException {
		
		if(search_text.getText().replaceAll(" ", "").length()==0) {
			return;
		}
		
		// can only grab first 100 results
		//String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String url = "https://www.google.com/search?site=imghp&tbm=isch&source=hp&q="+URLEncoder.encode(search_text.getText(),"UTF-8")+"&gws_rd=cr&tbs=imgo:1,itp:photo";
		//add &tbs=ic:trans,imgo:1 to url for now 
		/*
		 * Large images: tbs=isz:l
			Medium images: tbs=isz:m
			Icon sized images: tba=isz:i
			Image size larger than 400×300: tbs=isz:lt,islt:qsvga
			Image size larger than 640×480: tbs=isz:lt,islt:vga
			Image size larger than 800×600: tbs=isz:lt,islt:svga
			Image size larger than 1024×768: tbs=isz:lt,islt:xga
			Image size larger than 1600×1200: tbs=isz:lt,islt:2mp
			Image size larger than 2272×1704: tbs=isz:lt,islt:4mp
			Image sized exactly 1000×1000: tbs=isz:ex,iszw:1000,iszh:1000
			 Images in full color: tbs=ic:color
			Images in black and white: tbs=ic:gray
			Images that are red: tbs=ic:specific,isc:red [orange, yellow, green, teal, blue, purple, pink, white, gray, black, brown]
			Image type Face: tbs=itp:face
			Image type Photo: tbs=itp:photo
			Image type Clipart: tbs=itp:clipart
			Image type Line drawing: tbs=itp:lineart
			Image type Animated (gif): tbs=itp:animated (thanks Dan)
			Group images by subject: tbs=isg:to
			Show image sizes in search results: tbs=imgo:1
			Show only transparent images tbs=ic:trans
		 */
		
		 Document doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();
		 List<ImageResult> resultImages = new ArrayList<ImageResult>();
		 	
		    Elements elements = doc.select("div.rg_meta");
		    
		    
		    JSONObject jsonObject;
		    for (Element element : elements) {
		        if (element.childNodeSize() > 0) {

		        	jsonObject = (JSONObject) new JSONParser().parse(element.childNode(0).toString());		            
		            String image_url = (String) jsonObject.get("ou");
		            long image_height = (long) jsonObject.get("oh");
		            long image_width = (long) jsonObject.get("ow");
		            
		            ImageResult tmp = new ImageResult();
		            tmp.setUrl(image_url);
		            tmp.setHeight(image_height);
		            tmp.setWidth(image_width);
		            
		            resultImages.add(tmp);
		            
		            
		        }
		    }

		    ;
		    HashMap<Integer, Region> centrals = new HashMap<Integer,Region>();
		    centrals.put(1, image_1);
		    centrals.put(2,image_2);
		    centrals.put(3,image_3);
		    centrals.put(4,image_4);
		    
		    HashMap<Integer, Region> downs = new HashMap<Integer,Region>();
		    downs.put(1, image_1_down);
		    downs.put(2, image_2_down);
		    downs.put(3, image_3_down);
		    downs.put(4, image_4_down);
		    
		    
		    HashMap<Integer, Region> ups = new HashMap<Integer,Region>();
		    ups.put(1, image_1_up);
		    ups.put(2, image_2_up);
		    ups.put(3, image_3_up);
		    ups.put(4, image_4_up);
		    
		    
		    
		    
		    int i = 1;
		    for (ImageResult image : resultImages) {
		    	
		    	String imageUrl = image.getUrl();
		    	if(imageUrl.contains("http:")) {
		    		continue;
		    	}
		    	try{
		    		
		    		
		    		
		    		

	    		URL img_url = new URL(image.getUrl());
	    		BufferedImage img = ImageIO.read(img_url);
	    		
	    		boolean is_white_image = is_white_image(img);
	    		
	    		
	    		
		    		
		    		
		    		BoxBlur bb = new BoxBlur();
	    	        bb.setWidth(10);
	    	        bb.setHeight(10);
	    	        bb.setIterations(1);
	    	        
	    	        if(!is_white_image) {
	    	        	
	    	        	long ih = image.getHeight();
			    		long iw = image.getWidth();
			    		
			    		double region_ratio = centrals.get(i).getWidth() *1.0  / centrals.get(i).getHeight();
			    		
			    		if(iw > ih * region_ratio) {
			    			double ratio = centrals.get(i).getWidth()*1.0 / iw;
			    			double y = ih * ratio;
			    			
			    			centrals.get(i).setStyle(image_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		downs.get(i).setStyle(edge_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		ups.get(i).setStyle(edge_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		
				    		
				    		
				    		double central_position = (centrals.get(i).getHeight() - y)*0.5 ;
				    		double down_position = (-10*y + central_position);
				    		double up_position = (centrals.get(i).getHeight() + y)*0.5 ;
				    		
				    		
				    		
			    			centrals.get(i).setStyle(centrals.get(i).getStyle()+"; "+"-fx-background-position: 0 "+central_position+";");
			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-background-position: 0 "+down_position+";");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-background-position: 0 "+up_position+";");
			    			
			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-scale-y: -1;");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-scale-y: -1;");
			    			
			    			
			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-background-size: "+downs.get(i).getWidth()+" "+10*y+";");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-background-size: "+ups.get(i).getWidth()+" "+10*y+";");
			    			
			    			//downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-background-position: 0 "+(-10*y + translate_to_center)+";");
			    			//ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-background-position: 0 "+(shift_from_center)+";");
			    			
			    			
			    			downs.get(i).toFront();
			    			ups.get(i).toFront();
			    			centrals.get(i).toFront();
			    			
			    			
			    		}else {
			    			
			    			double ratio = centrals.get(i).getHeight()*1.0 / ih;
			    			double x = iw * ratio;
			    			
			    			centrals.get(i).setStyle(image_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		downs.get(i).setStyle(edge_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		ups.get(i).setStyle(edge_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
				    		

			                double central_position = (centrals.get(i).getWidth() - x)*0.5 ;
			                double right_position = (-10*x + central_position);
			                double left_position = (centrals.get(i).getWidth() + x)*0.5 ;
			                
				    		
				    		
			    			centrals.get(i).setStyle(centrals.get(i).getStyle()+"; "+"-fx-background-position: "+central_position+" 0;");
			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-background-position: "+right_position+" 0;");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-background-position: "+left_position+" 0;");
			    			
			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-scale-x: -1;");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-scale-x: -1;");
			    			

			    			downs.get(i).setStyle(downs.get(i).getStyle()+"; "+"-fx-background-size: "+10*x+" "+downs.get(i).getHeight()+";");
			    			ups.get(i).setStyle(ups.get(i).getStyle()+"; "+"-fx-background-size: "+10*x+" "+ups.get(i).getHeight()+";");
			    			
			    			
			    			
			    			
			    			
			    			
			    			downs.get(i).toFront();
			    			ups.get(i).toFront();
			    			centrals.get(i).toFront();
			    			
			    		
			    		}
	    	        	
	    	        	
	    	        }else {
	    	        	centrals.get(i).setStyle(image_style+"; "+"-fx-background-image: url(\""+imageUrl+"\");");
			    		downs.get(i).setStyle(edge_style);
			    		ups.get(i).setStyle(edge_style);
	    	        }
	    	        
	    	       
		    		
		    		
		    		
		    		
		    		
	    	
		    		 	
		    		;
		    	}catch(Exception V) {
		    		break;
		    	}
		    	i+=1;
		    	
		    }
	
	}

	private boolean is_white_image(BufferedImage img) {
		return ( is_valid_border_pixel(img, 0,0) 					&& is_valid_border_pixel(img, img.getWidth()-1,img.getHeight()-1) )
				||
				( is_valid_border_pixel(img, img.getWidth()-1,0)	&& is_valid_border_pixel(img, 0,img.getHeight()-1) );
		
	}

	private boolean is_valid_border_pixel(BufferedImage img, int i, int j) {

		  int clr=  img.getRGB(i,j); 
		  int  red   = (clr & 0x00ff0000) >> 16;
		  int  green = (clr & 0x0000ff00) >> 8;
		  int  blue  =  clr & 0x000000ff;
		 
		  if(red<GlobalConstants.MAX_BORDER_LUMIN && red!=0) {
			  return false;
		  }
		  if(green<GlobalConstants.MAX_BORDER_LUMIN && red!=0) {
			  return false;
		  }
		  if(blue<GlobalConstants.MAX_BORDER_LUMIN && red!=0) {
			  return false;
		  }
		  
		return true;
		  
	}
	
}
