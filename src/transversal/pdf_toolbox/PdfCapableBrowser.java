package transversal.pdf_toolbox;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import transversal.dialog_toolbox.ItemUploadDialog;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfCapableBrowser {

    private boolean pdfJsLoaded;

    private ProcessListener processListener;

    public WebView nodeValue;
    private String loadScript;
    private String toExecuteWhenPDFJSLoaded = "";



    public PdfCapableBrowser() {
    }

    public PdfCapableBrowser(File file) throws IOException {
        displayPdf(file);
    }

    public PdfCapableBrowser(URL url) throws IOException {
        displayPdf(url);
    }

    public PdfCapableBrowser(InputStream inputStream) throws IOException {
        displayPdf(inputStream);
    }


    public void displayPdf(File file) throws IOException {
        displayPdf(new BufferedInputStream(new FileInputStream(file)));
    }

    public void displayPdf(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        /*uc.addRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");*/
        uc.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2656.18 Safari/537.36");

        displayPdf(new BufferedInputStream(uc.getInputStream()));
    }

    public void displayPdf(InputStream inputStream) throws IOException {

        if (inputStream == null)
            return;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            String url = getClass().getResource("/scripts/pdfjs/web/viewer.html").toExternalForm();

            nodeValue.getEngine().setJavaScriptEnabled(true);
            nodeValue.getEngine().load(url);
            updateProcessListener(false);

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
            /*String editedString = outputStream.toString();
            Pattern ptn = Pattern.compile(Pattern.quote("(")+" .+ "+Pattern.quote(")"));
            Matcher m = ptn.matcher(editedString);
            while (m.find()){
                System.out.println(m.group(0));
                editedString = editedString.replace(m.group(0),"( "+new String(new char[m.group(0).length()-4]).replace("\0", "$")+" )");
            }
            data = editedString.getBytes();*/
            String base64 = Base64.getEncoder().encodeToString(data);
            // call JS function from Java code
            String js = "openFileFromBase64('" + base64 + "');";

            try {
                nodeValue.getEngine().executeScript(js);
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
        webView.setContextMenuEnabled(false);
        webView.getStylesheets().add(getClass().getResource("/Styles/PDFWebView.css").toExternalForm());

        WebEngine engine = webView.getEngine();
        String url = getClass().getResource("/scripts/pdfjs/web/viewer.html").toExternalForm();

        engine.setJavaScriptEnabled(true);
        engine.load(url);

        if (processListener != null) processListener.listen(false);

        engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("%"+String.valueOf(newValue.intValue()));
                if(newValue.intValue()==100){
                    try {
                        if (processListener != null) processListener.listen(pdfJsLoaded = true);

                        if (loadScript != null)
                            engine.executeScript(loadScript);

                        engine.executeScript(toExecuteWhenPDFJSLoaded);
                        toExecuteWhenPDFJSLoaded = null;
                        loadScript=null;
                        observable.removeListener(this);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        engine.getLoadWorker()
                .stateProperty()
                .addListener(
                        new ChangeListener<Worker.State>() {
                            @Override
                            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                                JSObject window = (JSObject) engine.executeScript("window");
                                window.setMember("java", new JSLogListener());
                                engine.executeScript("console.log = function(message){ try {java.log(message);} catch(e) {} };");

                                if (newValue == Worker.State.SUCCEEDED) {
                                    System.out.println("Worker Success");
                                    try {
                                        if (processListener != null) processListener.listen(pdfJsLoaded = true);

                                        if (loadScript != null)
                                            engine.executeScript(loadScript);

                                        engine.executeScript(toExecuteWhenPDFJSLoaded);
                                        toExecuteWhenPDFJSLoaded = null;
                                        loadScript=null;
                                        observable.removeListener(this);
                                    } catch (Exception e) {
                                        //throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
        engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Desktop d = Desktop.getDesktop();
                URI address = null;
                try {
                    address = new URI(observable.getValue());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                try {
                    if ((address.toURL() + "").indexOf(".pdf") > -1)
                    {
                        System.out.println("XXXXX PDF LINK DETECTED XXXXX");
                        displayPdf(address.toURL());
                        // wv.getEngine().load(oldValue); // 1
                        // wv.getEngine().getLoadWorker().cancel(); // 2
                        // wv.getEngine().executeScript("history.back()"); // 3
                        //d.browse(address);
                        //parent.browserUrlProperty.setValue(address.toURL().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return webView;

    }



    public Parent toNode() {
        if (nodeValue == null)
            return nodeValue = createWebView();
        else
            return nodeValue;
    }

    public void loadPage(String url) {
        nodeValue.getEngine().load(url);
    }
}
