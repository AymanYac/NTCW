package transversal.pdf_toolbox;

import java.io.IOException;
import java.io.OutputStream;

public class JSLogListener {
    private static OutputStream out = System.out;

    public JSLogListener(){
    }

    public void log(String message) throws IOException {
        if (message != null && out != null)
           out.write((message + "\n").getBytes());
    }

    public static void setOutputStream(OutputStream outputStream){
        out = outputStream;
    }
}
