package controllers.paneControllers;

import controllers.Char_description;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import model.GlobalConstants;
import model.ImageResult;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import service.BoxBlurFilter;
import transversal.generic.PixelUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

	public class ImagePane_CharClassif {
		@FXML GridPane imageList;
		@FXML Button imagePaneClose;
		private Char_description parent;
		String image_style = "-fx-background-size: contain; -fx-background-repeat: no-repeat; -fx-background-color: transparent; -fx-background-position: center center; -fx-border-color: #445469;";
		String noImageBG = this.getClass().getResource("/pictures/No_picture.png").toExternalForm();
		String edge_style = "-fx-background-size: contain; -fx-background-repeat: no-repeat; -fx-background-position: center center; -fx-border-color: white;";
		private Thread imageDownloadThread;
		private Task<Void> imageDownloadTask;


		@FXML public void imagePaneClose() {
		parent.imagesVisible.set(false);
		parent.setBottomRegionColumnSpans(false);
		parent.value_field.requestFocus();
		parent.hideAutoCompletePopups();
		parent.value_field.end();
		parent.value_field.selectAll();
	}
	
	
	@SuppressWarnings("static-access")
	private void download_images_bing(boolean checkMethodSelect) throws IOException, ParseException{
		/*System.out.println(LocalDateTime.now()+" start");*/
		String selected_text="";
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
		
		
		// can only grab first 100 results
		//String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String url;
		if(GlobalConstants.IMAGE_SEARCH_THUMBNAIL){
			url = "https://www.bing.com/images/search?q="+URLEncoder.encode(target,"UTF-8")+"&form=HDRSC3&first=1&tsc=ImageHoverTitle&scope=images";
		}else{
			url = "https://www.bing.com/images/search?q="+URLEncoder.encode(target,"UTF-8")+"&scope=images";
		}

		
		 Document doc = Jsoup.connect(url).get();
		 List<ImageResult> resultImages = new ArrayList<ImageResult>();
		 	
		    //Elements elements = doc.select("a.iusc");
			Elements elements = doc.select("img.mimg");
		    JSONObject jsonObject;
			String image_url;
			long image_height;
			long image_width;
		    for (Element element : elements) {

				if(GlobalConstants.IMAGE_SEARCH_THUMBNAIL){
					image_url = element.attr("src");
					if(!image_url.startsWith("http")){
						/*System.out.println("rejected "+image_url);*/
						continue;
					}else{
						/*System.out.println("in "+image_url);*/
					}
					image_height = Long.valueOf(element.attr("height"));
					image_width =  Long.valueOf(element.attr("width"));
				}else{
					jsonObject = (JSONObject) new JSONParser().parse(element.attr("m"));
					image_url = (String) jsonObject.get("murl");
					image_height = Long.valueOf(element.attr("href").split("exph=")[1].split("&")[0]);
					image_width =  Long.valueOf(element.attr("href").split("expw=")[1].split("&")[0]);
				}

				ImageResult tmp = new ImageResult();
	            tmp.setUrl(image_url);
	            tmp.setHeight(image_height);
	            tmp.setWidth(image_width);
	            
	            resultImages.add(tmp);
				if(resultImages.size()==6){
					break;
				}
		    }

		    
		    
		    HashMap<Integer, Region> centrals = new HashMap<Integer,Region>();
		    HashMap<Integer, Region> downs = new HashMap<Integer,Region>();
		    HashMap<Integer, Region> ups = new HashMap<Integer,Region>();
		    
		    for(Node node : imageList.getChildren()) {
		    	if(node instanceof Region) {
		    		Integer row = imageList.getRowIndex(node);
		    		Integer col = imageList.getColumnIndex(node);
		    		if(row!=null) {
		    			
		    		}else {
		    			row=0;
		    		}
		    		if(col!=null) {
		    			
		    		}else {
		    			col=0;
		    		}
		    		
		    		int mapIndex = row + (col!=0?1:0);
		    		
		    		
		    		if(centrals.containsKey(mapIndex)) {
		    			if(downs.containsKey(mapIndex)) {
		    				node.setStyle(edge_style);
		    				ups.put(mapIndex,(Region) node);
		    				/*Platform.runLater(new Runnable() {
			    	            @Override public void run() {
			    	            	node.toFront();
			    	            }
			    	        });*/
			    			continue;
		    			}else {
		    				node.setStyle(edge_style);
		    				downs.put(mapIndex,(Region) node);
		    				/*Platform.runLater(new Runnable() {
			    	            @Override public void run() {
			    	            	node.toFront();
			    	            }
			    	        });*/
			    			continue;
		    			}
		    		}else {
		    			node.setStyle(image_style);
		    			centrals.put(mapIndex,(Region) node);
		    			/*Platform.runLater(new Runnable() {
		    	            @Override public void run() {
		    	            	node.toFront();
		    	            }
		    	        });*/
		    			
		    			continue;
		    		}
		    	}
		    }
		    
		    for(Region node:centrals.values()) {
		    	node.setStyle(node.getStyle()+"-fx-background-image: url('" + noImageBG + "');");
		    }

		/*System.out.println(LocalDateTime.now()+" got all pics");*/




		int i = 0;
		    for (ImageResult image : resultImages) {
		    	/*System.out.println("image "+LocalDateTime.now());*/
		    	String imageUrl = image.getUrl();
		    	if(imageUrl.contains("http:")) {
		    		//continue;
		    	}
		    	try{

	    		URL img_url = new URL(image.getUrl());
				BufferedImage img = ImageIO.read(img_url);

	    		boolean is_white_image = PixelUtils.is_white_image(img);
	    		
	    		    if(!is_white_image) {
	    	        	
	    	        	long ih = image.getHeight();
			    		long iw = image.getWidth();
			    		
			    		double region_ratio = centrals.get(i).getWidth() *1.0  / centrals.get(i).getHeight();
			    		BoxBlurFilter blur = new BoxBlurFilter();
			    		//GaussianFilter blur = new GaussianFilter();
			    		
			    		if(iw > ih * region_ratio) {
			    			
			    			double ratio = centrals.get(i).getWidth()*1.0 / iw;
			    			double y = ih * ratio;
			    			
			    			Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
			    			BufferedImage src = PixelUtils.BufferedImageDeepCopy(img);
			    			BufferedImage dst = PixelUtils.BufferedImageDeepCopy(img);
			    			blur.filter(src, dst);
			    			Image bg_photo = SwingFXUtils.toFXImage(dst, null);
			    			
			    			double central_position = (centrals.get(i).getHeight() - y)*0.5 ;
				    		BackgroundPosition centralPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,central_position,false);
				    		BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
				    		BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, centralPosition, centralSize);
		                    Background central_bg = new Background(centralImange);
		                  
		                    double down_position = (-y + central_position);
				    		BackgroundPosition downPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,down_position,false);
				    		BackgroundSize downSize = new BackgroundSize(downs.get(i).getWidth(),10*y,false,false,true,false);
				    		BackgroundImage downImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, downPosition, downSize);
				    		Background down_bg = new Background(downImange);
		                    
		                    double up_position = (centrals.get(i).getHeight() + y)*0.5 ;
				    		BackgroundPosition upPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,up_position,false);
				    		BackgroundSize upSize = new BackgroundSize(ups.get(i).getWidth(),10*y,false,false,true,false);
				    		BackgroundImage upImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, upPosition, upSize);
		                    Background up_bg = new Background(upImange);
		                    
		                    
		                    
		                    centrals.get(i).setBackground(central_bg);
		                    downs.get(i).setBackground(down_bg);
		                    ups.get(i).setBackground(up_bg);
		                     
		                    downs.get(i).setScaleY(-1);
		                    ups.get(i).setScaleY(-1);

			    		}else {
			    			double ratio = centrals.get(i).getHeight()*1.0 / ih;
			    			double x = iw * ratio;

			    			Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
			    			BufferedImage src = PixelUtils.BufferedImageDeepCopy(img);
			    			BufferedImage dst = PixelUtils.BufferedImageDeepCopy(img);
			    			blur.filter(src, dst);
			    			Image bg_photo = SwingFXUtils.toFXImage(dst, null);

			    			double central_position = (centrals.get(i).getWidth() - x)*0.5 ;
			                BackgroundPosition centralPosition = new BackgroundPosition(Side.LEFT,central_position,false,Side.TOP,0,false);
				    		BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
				    		BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, centralPosition, centralSize);
		                    Background central_bg = new Background(centralImange);

		                    double right_position = (-x + central_position);
		                    BackgroundPosition rightPosition = new BackgroundPosition(Side.LEFT,right_position,false,Side.TOP,0,false);
				    		BackgroundSize rightSize = new BackgroundSize(10*x,downs.get(i).getHeight(),false,false,true,false);
				    		BackgroundImage rightImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, rightPosition, rightSize);
				    		Background right_bg = new Background(rightImange);
		                    
			                double left_position = (centrals.get(i).getWidth() + x)*0.5 ;
			                BackgroundPosition leftPosition = new BackgroundPosition(Side.LEFT,left_position,false,Side.TOP,0,false);
				    		BackgroundSize leftSize = new BackgroundSize(10*x,ups.get(i).getHeight(),false,false,true,false);
				    		BackgroundImage leftImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, leftPosition, leftSize);
		                    Background left_bg = new Background(leftImange);
		                    
		                    
		                    centrals.get(i).setBackground(central_bg);
		                    downs.get(i).setBackground(left_bg);
		                    ups.get(i).setBackground(right_bg);
		                     
		                    downs.get(i).setScaleX(-1);
		                    ups.get(i).setScaleX(-1);

			    		}
	    	        	
	    	        	
	    	        }else {
	    	        	
	    	        	Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
		    			BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
			    		BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, centralSize);
	                    Background central_bg = new Background(centralImange);
	                  
			    		
	                    downs.get(i).setBackground(central_bg);
	                    centrals.get(i).setStyle(edge_style+";-fx-background-color: white");
			    		ups.get(i).setStyle(edge_style);
	    	        }

		    	}catch(Exception V) {
		    		continue;
		    	}
		    	i+=1;
		    	if(i==centrals.size()) {
		    		break;
		    	}
		    }
	}
		
		
	
	@SuppressWarnings({ "static-access", "unused" })
	private void download_images_google_OLD(boolean checkMethodSelect) throws IOException, ParseException {
		
		
		
		
		String selected_text="";
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
		
		
		// can only grab first 100 results
		//String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
		String url = "https://www.google.com/search?site=imghp&tbm=isch&source=hp&q="+URLEncoder.encode(target,"UTF-8")+"&gws_rd=cr&tbs=imgo:1,itp:photo";
		
		//add &tbs=ic:trans,imgo:1 to url for now 
		/*
		 * Large images: tbs=isz:l
			Medium images: tbs=isz:m
			Icon sized images: tba=isz:i
			Image size larger than 400�300: tbs=isz:lt,islt:qsvga
			Image size larger than 640�480: tbs=isz:lt,islt:vga
			Image size larger than 800�600: tbs=isz:lt,islt:svga
			Image size larger than 1024�768: tbs=isz:lt,islt:xga
			Image size larger than 1600�1200: tbs=isz:lt,islt:2mp
			Image size larger than 2272�1704: tbs=isz:lt,islt:4mp
			Image sized exactly 1000�1000: tbs=isz:ex,iszw:1000,iszh:1000
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

		    
		    
		    HashMap<Integer, Region> centrals = new HashMap<Integer,Region>();
		    HashMap<Integer, Region> downs = new HashMap<Integer,Region>();
		    HashMap<Integer, Region> ups = new HashMap<Integer,Region>();
		    
		    for(Node node : imageList.getChildren()) {
		    	if(node instanceof Region) {
		    		Integer row = imageList.getRowIndex(node);
		    		Integer col = imageList.getColumnIndex(node);
		    		if(row!=null) {
		    			
		    		}else {
		    			row=0;
		    		}
		    		if(col!=null) {
		    			
		    		}else {
		    			col=0;
		    		}
		    		
		    		int mapIndex = row + (col!=0?1:0);
		    		
		    		
		    		if(centrals.containsKey(mapIndex)) {
		    			if(downs.containsKey(mapIndex)) {
		    				node.setStyle(edge_style);
		    				ups.put(mapIndex,(Region) node);
		    				/*Platform.runLater(new Runnable() {
			    	            @Override public void run() {
			    	            	node.toFront();
			    	            }
			    	        });*/
			    			continue;
		    			}else {
		    				node.setStyle(edge_style);
		    				downs.put(mapIndex,(Region) node);
		    				/*Platform.runLater(new Runnable() {
			    	            @Override public void run() {
			    	            	node.toFront();
			    	            }
			    	        });*/
			    			continue;
		    			}
		    		}else {
		    			node.setStyle(image_style);
		    			centrals.put(mapIndex,(Region) node);
		    			/*Platform.runLater(new Runnable() {
		    	            @Override public void run() {
		    	            	node.toFront();
		    	            }
		    	        });*/
		    			
		    			continue;
		    		}
		    	}
		    }
		    
		    for(Region node:centrals.values()) {
		    	node.setStyle(node.getStyle()+"-fx-background-image: url('" + noImageBG + "');");
		    }
		    
		    
		    
		    
		    
		    int i = 0;
		    for (ImageResult image : resultImages) {

		    	String imageUrl = image.getUrl();
		    	if(imageUrl.contains("http:")) {
		    		//continue;
		    	}
		    	try{

					URL img_url = new URL(image.getUrl());
					BufferedImage img = ImageIO.read(img_url);
					
					boolean is_white_image = PixelUtils.is_white_image(img);

						if(!is_white_image) {

							long ih = image.getHeight();
							long iw = image.getWidth();

							double region_ratio = centrals.get(i).getWidth() *1.0  / centrals.get(i).getHeight();
							BoxBlurFilter blur = new BoxBlurFilter();
							//GaussianFilter blur = new GaussianFilter();

							if(iw > ih * region_ratio) {

								double ratio = centrals.get(i).getWidth()*1.0 / iw;
								double y = ih * ratio;

								Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
								BufferedImage src = PixelUtils.BufferedImageDeepCopy(img);
								BufferedImage dst = PixelUtils.BufferedImageDeepCopy(img);
								blur.filter(src, dst);
								Image bg_photo = SwingFXUtils.toFXImage(dst, null);

								double central_position = (centrals.get(i).getHeight() - y)*0.5 ;
								BackgroundPosition centralPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,central_position,false);
								BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
								BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, centralPosition, centralSize);
								Background central_bg = new Background(centralImange);

								double down_position = (-y + central_position);
								BackgroundPosition downPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,down_position,false);
								BackgroundSize downSize = new BackgroundSize(downs.get(i).getWidth(),10*y,false,false,true,false);
								BackgroundImage downImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, downPosition, downSize);
								Background down_bg = new Background(downImange);

								double up_position = (centrals.get(i).getHeight() + y)*0.5 ;
								BackgroundPosition upPosition = new BackgroundPosition(Side.LEFT,0,false,Side.TOP,up_position,false);
								BackgroundSize upSize = new BackgroundSize(ups.get(i).getWidth(),10*y,false,false,true,false);
								BackgroundImage upImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, upPosition, upSize);
								Background up_bg = new Background(upImange);

								centrals.get(i).setBackground(central_bg);
								downs.get(i).setBackground(down_bg);
								ups.get(i).setBackground(up_bg);

								downs.get(i).setScaleY(-1);
								ups.get(i).setScaleY(-1);

							}else {
								double ratio = centrals.get(i).getHeight()*1.0 / ih;
								double x = iw * ratio;

								Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
								BufferedImage src = PixelUtils.BufferedImageDeepCopy(img);
								BufferedImage dst = PixelUtils.BufferedImageDeepCopy(img);
								blur.filter(src, dst);
								Image bg_photo = SwingFXUtils.toFXImage(dst, null);



								double central_position = (centrals.get(i).getWidth() - x)*0.5 ;
								BackgroundPosition centralPosition = new BackgroundPosition(Side.LEFT,central_position,false,Side.TOP,0,false);
								BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
								BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, centralPosition, centralSize);
								Background central_bg = new Background(centralImange);


								double right_position = (-x + central_position);
								BackgroundPosition rightPosition = new BackgroundPosition(Side.LEFT,right_position,false,Side.TOP,0,false);
								BackgroundSize rightSize = new BackgroundSize(10*x,downs.get(i).getHeight(),false,false,true,false);
								BackgroundImage rightImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, rightPosition, rightSize);
								Background right_bg = new Background(rightImange);

								double left_position = (centrals.get(i).getWidth() + x)*0.5 ;
								BackgroundPosition leftPosition = new BackgroundPosition(Side.LEFT,left_position,false,Side.TOP,0,false);
								BackgroundSize leftSize = new BackgroundSize(10*x,ups.get(i).getHeight(),false,false,true,false);
								BackgroundImage leftImange = new BackgroundImage(bg_photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, leftPosition, leftSize);
								Background left_bg = new Background(leftImange);


								centrals.get(i).setBackground(central_bg);
								downs.get(i).setBackground(left_bg);
								ups.get(i).setBackground(right_bg);

								downs.get(i).setScaleX(-1);
								ups.get(i).setScaleX(-1);

							}


						}else {

							Image photo = SwingFXUtils.toFXImage(PixelUtils.BufferedImageDeepCopy(img),null);
							BackgroundSize centralSize = new BackgroundSize(centrals.get(i).getWidth(),centrals.get(i).getHeight(),false,false,true,false);
							BackgroundImage centralImange = new BackgroundImage(photo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, centralSize);
							Background central_bg = new Background(centralImange);


							downs.get(i).setBackground(central_bg);
							centrals.get(i).setStyle(edge_style+";-fx-background-color: white");
							ups.get(i).setStyle(edge_style);
						}

		    	}catch(Exception V) {
		    		continue;
		    	}
		    	i+=1;
		    	if(i==centrals.size()) {
		    		break;
		    	}
		    }
	}
	@SuppressWarnings("deprecation")
	public void search_image(boolean checkMethodSelect) throws IOException, ParseException {
		
		try {
			imageDownloadTask.cancel();
			imageDownloadThread.stop();
		}catch(Exception V) {
			
		}
		imageDownloadTask = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				download_images_bing(checkMethodSelect);
				/*System.out.println("Done");*/
		    	return null;
		    }
		};
		imageDownloadTask.setOnSucceeded(e -> {
			
			
			});
		imageDownloadTask.setOnFailed(e -> {
		    Throwable problem = imageDownloadTask.getException();
		    /* code to execute if task throws exception */
		    problem.printStackTrace(System.err);
		});

		imageDownloadTask.setOnCancelled(e -> {
		    /* task was cancelled */
			
		});
		
		imageDownloadThread = new Thread(imageDownloadTask);; imageDownloadThread.setDaemon(true);
		imageDownloadThread.setName("ImgDwnld");
		imageDownloadThread.start();
		
		
		
		
	
	}

	

	public void setParent(Char_description char_description) {
		this.parent = char_description;
	}




	public void prepare_images() {
		for(Node node : imageList.getChildren()) {
	    	if(node instanceof Region) {
	    		((Region)node).setStyle(image_style+"-fx-background-image: url('" + noImageBG + "');");
	    	}	
	    }
	    
	}



}
