package transversal.pdf_toolbox;

import controllers.paneControllers.Browser_CharClassif;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.GlobalConstants;
import model.UserAccount;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.core.views.DocumentViewModel;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.util.PropertiesManager;
import service.ExternalSearchServices;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.ResourceBundle;

public class PdfCapableBrowser {

    public String latestPDFLink;
    private boolean pdfJsLoaded;

    private ProcessListener processListener;

    public WebView nodeValue;
    private String loadScript;
    private String toExecuteWhenPDFJSLoaded = "";
    private Browser_CharClassif parent;
    public boolean FORCE_PDF_IN_VIEWER=GlobalConstants.FORCE_PDF_IN_VIEWER;


    private boolean urlStartsWithPDFBytes(URL url) {
        try {
            URLConnection uc = url.openConnection();
            uc.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2656.18 Safari/537.36");
            uc.setConnectTimeout(2000); // 2 sec
            uc.setReadTimeout(4000); // 4 sec
            InputStream is = uc.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[6];
            bis.read(buffer);
            String base64 = Base64.getEncoder().encodeToString(buffer);
            //System.out.println("base64 =>"+base64);
            bis.close();
            return base64.startsWith("JVBERi0");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void displayPdf(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        uc.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2656.18 Safari/537.36");

        displayPdf(new BufferedInputStream(uc.getInputStream()));
    }
    public PdfCapableBrowser() {
    }

    public PdfCapableBrowser(File file) throws IOException {
        displayPdf(file);
    }

    public PdfCapableBrowser(InputStream inputStream) throws IOException {
        displayPdf(inputStream);
    }


    public void displayPdf(File file) throws IOException {
        displayPdf(new BufferedInputStream(new FileInputStream(file)));
    }
    

    public void displayPdf(InputStream inputStream) throws IOException {

        if (inputStream == null)
            return;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            String url = getClass().getResource("/scripts/pdfjs/web/viewer.html").toExternalForm();
            nodeValue.getEngine().setJavaScriptEnabled(true);
            nodeValue.getEngine().load(url);
            updateProcessListener(false);
            nodeValue.getEngine().getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    //System.out.println(newValue.intValue());
                    if(newValue.intValue()==100){
                        //System.out.println(loadScript);
                        //System.out.println(toExecuteWhenPDFJSLoaded);
                        try {
                            if (processListener != null) processListener.listen(pdfJsLoaded = true);

                            if (loadScript != null)
                                nodeValue.getEngine().executeScript(loadScript);

                            nodeValue.getEngine().executeScript(toExecuteWhenPDFJSLoaded);
                            toExecuteWhenPDFJSLoaded = "";
                            loadScript=null;
                            observable.removeListener(this);
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            byte[] buffer = new byte[4096];

            int actualByteCount;
            while (true) {
                try {
                    if ((actualByteCount = inputStream.read(buffer)) == -1) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                outputStream.write(buffer, 0, actualByteCount);
            }

            updateProcessListener(true);

            byte[] data = outputStream.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(data);
            // call JS function from Java code
            String js = "openFileFromBase64('" + base64 + "');";

            try {
                executeScript(js);
            } catch (Exception ex) {
                if (!pdfJsLoaded) loadScript = js;
            }

        } finally {
            inputStream.close();
        }

    }


    @SuppressWarnings("all")
    public void setSecondaryToolbarToggleVisibility(boolean value) {
        setVisibilityOf("secondaryToolbarToggle", value);

        String js;
        if (value){
            js = new StringBuilder()
                    .append("var element = document.getElementsByClassName('verticalToolbarSeparator')[0];")
                    .append("element.style.display = 'inherit';")
                    .append("element.style.visibility = 'inherit';")
                    .toString();
        } else {
            js = new StringBuilder()
                    .append("var element = document.getElementsByClassName('verticalToolbarSeparator')[0];")
                    .append("element.style.display = 'none';")
                    .append("element.style.visibility = 'hidden';")
                    .toString();
        }

        try {
            nodeValue.getEngine().executeScript(js);
        } catch (Exception ex){
            if (!pdfJsLoaded) toExecuteWhenPDFJSLoaded += js;
        }
    }

    @SuppressWarnings("all")
    public void setVisibilityOf(String id, boolean value){
        String css;
        if (value) {
            css = new StringBuilder()
                    .append("document.getElementById('" + id + "').style.display = 'inherit';")
                    .append("document.getElementById('" + id + "').style.visibility = 'inherit';")
                    .toString();
        } else {
            css = new StringBuilder()
                    .append("document.getElementById('" + id + "').style.display = 'none';")
                    .append("document.getElementById('" + id + "').style.visibility = 'hidden';")
                    .toString();
        }

        try {
            nodeValue.getEngine().executeScript(css);
        } catch (Exception ex) {
            if (!pdfJsLoaded) this.toExecuteWhenPDFJSLoaded += css;
        }
    }

    public int getActualPageNumber(){
        try {
            return (int) nodeValue.getEngine().executeScript("PDFViewerApplication.page;");
        } catch (Exception e) {
            return 0;
        }
    }

    public int getTotalPageCount(){
        try {
            return (int) nodeValue.getEngine().executeScript("PDFViewerApplication.pagesCount;");
        } catch (Exception e) {
            return 0;
        }
    }

    public void navigateByPage(int pageNum) {
        String jsCommand = "goToPage(" + pageNum + ");";
        try {
            nodeValue.getEngine().executeScript(jsCommand);
        } catch (Exception ex) {
            if (!pdfJsLoaded) toExecuteWhenPDFJSLoaded += jsCommand;
        }
    }

    public void setProcessListener(ProcessListener listener) {
        this.processListener = listener;
    }

    public void executeScript(String js) {
        try {
            this.nodeValue.getEngine().executeScript(js);
        } catch (Exception ex) {
            if (!pdfJsLoaded) toExecuteWhenPDFJSLoaded += String.format("%s;", js);
        }
    }

    private void updateProcessListener(boolean val) {
        if (processListener != null && pdfJsLoaded) processListener.listen(val);
    }

    private WebView createWebView() {
        WebView webView = new WebView();
        webView.setPrefSize(500,500);
        webView.setContextMenuEnabled(false);
        webView.getStylesheets().add(getClass().getResource("/styles/PDFWebView.css").toExternalForm());

        WebEngine engine = webView.getEngine();
        String url = getClass().getResource("/scripts/pdfjs/web/viewer.html").toExternalForm();

        engine.setJavaScriptEnabled(true);
        engine.load(url);

        if (processListener != null) processListener.listen(false);

        engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(parent.showingPdf.getValue()){
                    return;
                }
                System.err.println("Browsing location: "+newValue);
                URI address = null;
                try {
                    address = new URI(observable.getValue());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                try {
                    if (urlStartsWithPDFBytes(address.toURL()) )
                    {
                        parent.showingPdf.setValue(true);
                        latestPDFLink=address.toURL().toString();
                        ExternalSearchServices.externalPdfViewing(address.toURL().toString());
                        if(FORCE_PDF_IN_VIEWER && !GlobalConstants.JAVASCRIPT_PDF_RENDER) {
                            icePdfBench(address.toURL(), parent.iceController);
                        }else if(FORCE_PDF_IN_VIEWER && GlobalConstants.JAVASCRIPT_PDF_RENDER){
                            displayPdf(address.toURL());
                        }else{
                            //parent.parent.urlLink.setText(address.toURL().toString());
                            Desktop d = Desktop.getDesktop();
                            d.browse(address);
                        }
                        //displayPdf(address.toURL());
                        // wv.getEngine().load(oldValue); // 1
                        // wv.getEngine().getLoadWorker().cancel(); // 2
                        // wv.getEngine().executeScript("history.back()"); // 3
                        //d.browse(address);
                        //parent.browserUrlProperty.setValue(address.toURL().toString());
                    }else{
                        ExternalSearchServices.browsingLink(address.toURL().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parent.parent.account.saveBrowserCookies();
            }
        });

        return webView;

    }


    public void icePdfBench(URL url, SwingController iceController) {
        //String filePath = getClass().getResource("/scripts/100RV.pdf").toExternalForm();
        //filePath = getClass().getResource("/scripts/SSRV.pdf").toExternalForm();

        //iceFrame.getContentPane().removeAll();
        //iceFrame.getContentPane().add(viewerComponentPanel);

        // Now that the GUI is all in place, we can try opening a PDF
        iceController.openDocument(url);

        // show the component
        //iceFrame.pack();
        parent.iceFrame.setVisible(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parent.pageField.setText(String.valueOf(iceController.getDocumentViewController().getCurrentPageIndex()+1));
            }
        });
        try{
            parent.pageLabel2.setText("/"+iceController.getDocument().getNumberOfPages());
        }catch (Exception V){

        }
        iceController.setDocumentToolMode(DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION);


    }


    public WebView toNode() {
        if (nodeValue == null)
            return nodeValue = createWebView();
        else
            return nodeValue;
    }

    public void loadPage(String url) {
        nodeValue.getEngine().load(url);
    }

    public void setParent(Browser_CharClassif browser_charClassif) {
        this.parent = browser_charClassif;
    }

    public SwingController loadIceController(JPanel iceFrame, SwingNode iceContainer) {
        SwingController iceController = new SwingController();
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            parent.pageField.setText(String.valueOf(iceController.getDocumentViewController().getCurrentPageIndex()+1));
                        }
                    });
                }
            }
        });

        SwingViewBuilder factory = new SwingViewBuilder(iceController,properties);
        iceFrame = factory.buildViewerPanel();
        iceFrame.setVisible(false);
        iceContainer = new SwingNode();
        iceContainer.setContent(iceFrame);
        iceContainer.visibleProperty().bind(parent.showingPdf);


        return iceController;
    }


    class Bridge {
        public void exit() {
            //Platform.exit();
            System.out.print("Hello!UPCALL");
        }
    }
}
