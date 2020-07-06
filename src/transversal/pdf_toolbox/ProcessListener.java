package transversal.pdf_toolbox;

@FunctionalInterface
public interface ProcessListener {
    void listen(boolean finished);
}
