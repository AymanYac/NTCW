package transversal.generic;
import javafx.collections.ListChangeListener.Change;
import model.ObservableDeque;



public class ObservableQueueTest {
	
	
	public static void main(String[] args) {
        ObservableDeque<String> oq = new ObservableDeque<>();
        oq.addListener((Change<? extends String> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    ;
                }
                if (change.wasRemoved()) {
                    ;
                }
                if (change.wasUpdated()) {
                    ;
                }
                if (change.wasReplaced()) {
                    ;
                }
            }
        });

        oq.addFirst("One");
        oq.addFirst("Two");
        oq.addLast("Three");

        ;
        ;
        ;

        ;
        ;

        ;

        ;
        ;

        ;
        ;

        ;
        ;

        ;
        ;
    }

	
	
}
