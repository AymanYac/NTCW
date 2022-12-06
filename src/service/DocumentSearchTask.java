package service;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import model.CircularArrayList;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingWorker;

import java.util.List;

public class DocumentSearchTask {




    public static class SearchTextTask {

        //Document controller
        private final SwingController controller;
        public final CircularArrayList<Pair<Integer, LineText>> hitResults = new CircularArrayList<Pair<Integer, LineText>>();
        // total length of task (total page count), used for progress bar
        private int lengthOfTask;

        // current progress, used for the progress bar
        private int current = 0;

        // flags for threading
        public SimpleBooleanProperty done = new SimpleBooleanProperty();
        private boolean canceled = false;

        // keep track of total hits
        public SimpleIntegerProperty totalHitCount = new SimpleIntegerProperty();

        // String to search for and parameters from gui
        private String pattern = "";
        private boolean wholeWord;
        private boolean caseSensitive;
        private boolean cumulative;
        private boolean currentlySearching;


        /**
         * Creates a new instance of the SearchTextTask.
         *
         * @param controller    root controller object
         * @param pattern       pattern to search for
         * @param wholeWord     true inticates whole word search
         * @param caseSensitive case sensitive indicates cases sensitive search
         * @param cumulative    if false, clears previous results and starts new search.
         */
        public SearchTextTask(SwingController controller,
                              String pattern,
                              boolean wholeWord,
                              boolean caseSensitive,
                              boolean cumulative) {
            this.controller = controller;
            this.pattern = pattern;
            lengthOfTask = controller.getDocument().getNumberOfPages();
            this.wholeWord = wholeWord;
            this.caseSensitive = caseSensitive;
            this.cumulative = cumulative;


        }

        /**
         * Start the task, start searching the document for the pattern.
         */
        public void go() {
            final SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    current = 0;
                    done.set(false);
                    canceled = false;
                    return new ActualTask();
                }
            };
            worker.setThreadPriority(Thread.NORM_PRIORITY);
            worker.start();
        }

        /**
         * Number pages that search task has to iterate over.
         *
         * @return returns max number of pages in document being search.
         */
        public int getLengthOfTask() {
            return lengthOfTask;
        }

        /**
         * Gets the page that is currently being searched by this task.
         *
         * @return current page being processed.
         */
        public int getCurrent() {
            return current;
        }

        /**
         * Stop the task.
         */
        public void stop() {
            canceled = true;
        }

        /**
         * Find out if the task has completed.
         *
         * @return true if task is done, false otherwise.
         */
        public boolean isDone() {
            return done.get();
        }

        public boolean isCurrentlySearching() {
            return currentlySearching;
        }


        /**
         * The actual long running task.  This runs in a SwingWorker thread.
         */
        class ActualTask {
            ActualTask() {

                // break on bad input
                if ("".equals(pattern) || " ".equals(pattern)) {
                    return;
                }

                try {
                    currentlySearching = true;
                    // Extraction of text from pdf procedure
                    totalHitCount.set(0);
                    current = 0;

                    // get instance of the search controller
                    DocumentSearchController searchController =
                            controller.getDocumentSearchController();
                    if (!cumulative) {
                        searchController.clearAllSearchHighlight();
                        hitResults.clear();
                    }
                    searchController.addSearchTerm(pattern,
                            caseSensitive, wholeWord);

                    Document document = controller.getDocument();
                    // iterate over each page in the document
                    for (int i = 0; i < document.getNumberOfPages(); i++) {
                        // break if needed
                        if (canceled || done.get()) {
                            break;
                        }
                        // Update task information
                        current = i;

                        // hits per page count
                        final List<LineText> lineItems =
                                searchController.searchHighlightPage(current, 6);
                        int hitCount = lineItems.size();
                        lineItems.stream().forEach(l->{
                            Pair<Integer,LineText> hitResult = new Pair<Integer, LineText>(current,l);
                            hitResults.add(hitResult);
                        });
                        // update total hit count
                        totalHitCount.set(totalHitCount.getValue()+ hitCount);
                        if (hitCount > 0) {
                            // update search dialog
                            final int currentPage = i;
                            // add the node to the search panel tree but on the
                            // awt thread.
                        }
                        Thread.yield();
                    }
                    done.set(true);
                } finally {
                    currentlySearching = false;
                }

            }
        }
    }
}
