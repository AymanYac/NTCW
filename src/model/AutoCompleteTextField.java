package model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import transversal.language_toolbox.Unidecode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.<p>
 *
 * If the entered text matches a part of any of the supplied entries these are
 * going to be displayed in a popup. Further the matching part of the entry is
 * going to be displayed in a special style, defined by
 * {@link #textOccurenceStyle textOccurenceStyle}. The maximum number of
 * displayed entries in the popup is defined by
 * {@link #maxEntries maxEntries}.<br>
 * By default the pattern matching is not case-sensitive. This behaviour is
 * defined by the {@link #caseSensitive caseSensitive}
 * .<p>
 *
 * The AutoCompleteTextField also has a List of
 * {@link #filteredEntries filteredEntries} that is equal to the search results
 * if search results are not empty, or {@link #filteredEntries filteredEntries}
 * is equal to {@link #entries entries} otherwise. If
 * {@link #popupHidden popupHidden} is set to true no popup is going to be
 * shown. This list can be used to bind all entries to another node (a ListView
 * for example) in the following way:
 * <pre>
 * <code>
 * AutoCompleteTextField auto = new AutoCompleteTextField(entries);
 * auto.setPopupHidden(true);
 * SimpleListProperty filteredEntries = new SimpleListProperty(auto.getFilteredEntries());
 * listView.itemsProperty().bind(filteredEntries);
 * </code>
 * </pre>
 *
 * @author Caleb Brinkman
 * @author Fabian Ochmann
 */
public class AutoCompleteTextField {

    /**
     * The existing autocomplete entries.
     */
    private final ArrayList<Object> entries;

    /**
     * The set of filtered entries:<br>
     * Equal to the search results if search results are not empty, equal to
     * {@link #entries entries} otherwise.
     */
    private ObservableList<Object> filteredEntries
            = FXCollections.observableArrayList();

    /**
     * The popup used to select an entry.
     */
    private ContextMenu entriesPopup;

    /**
     * Indicates whether the search is case sensitive or not. <br>
     * Default: false
     */
    private boolean caseSensitive = false;

    /**
     * Indicates whether the Popup should be hidden or displayed. Use this if
     * you want to filter an existing list/set (for example values of a
     * {@link javafx.scene.control.ListView ListView}). Do this by binding
     * {@link #getFilteredEntries() getFilteredEntries()} to the list/set.
     */
    private boolean popupHidden = false;

    /**
     * The CSS style that should be applied on the parts in the popup that match
     * the entered text. <br>
     * Default: "-fx-font-weight: bold; -fx-fill: red;"
     * <p>
     * Note: This style is going to be applied on an
     * {@link javafx.scene.text.Text Text} instance. See the <i>JavaFX CSS
     * Reference Guide</i> for available CSS Propeties.
     */
    private String textOccurenceStyle = "-fx-font-weight: bold; "
            + "-fx-fill: red;";

    /**
     * The maximum Number of entries displayed in the popup.<br>
     * Default: 10
     */
    private int maxEntries = 10;
    private static Unidecode unidecode = Unidecode.toAscii();
    private TextField parentField;

    /**
     * Construct a new AutoCompleteTextField.
     */
    public AutoCompleteTextField(TextField parentField, ArrayList<Object> entrySet) {
        this.parentField = parentField;
        this.entries = (entrySet == null ? new ArrayList<Object>() : entrySet);
        this.filteredEntries.addAll(entries);

        entriesPopup = new ContextMenu();
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if (getText().length() == 0) {
                    filteredEntries.clear();
                    filteredEntries.addAll(entries);
                    entriesPopup.hide();
                } else {
                    LinkedList<Object> searchResult = new LinkedList<>();

                    //Check if the entered Text is part of some entry
                    String text = getText();
                    Pattern pattern;
                    if (isCaseSensitive()) {
                        pattern = Pattern.compile(".*" + Pattern.quote(unidecode.decode(text)) + ".*");
                    } else {
                        pattern = Pattern.compile(".*" + Pattern.quote(unidecode.decode(text)) + ".*",
                                Pattern.CASE_INSENSITIVE);
                    }

                    for (Object entry : entries) {
                        Matcher matcher = pattern.matcher(getEntrySearchableText(entry));
                        if (matcher.matches()) {
                            searchResult.add(entry);
                        }
                    }

                    if (entrySet.size() > 0) {
                        Collections.sort(searchResult,new Comparator() {

                            @Override
                            public int compare(Object o1, Object o2) {
                                int ret = getEntrySearchableText(o1).compareTo(
                                        getEntrySearchableText(o2));
                                int av_1 =  getEntrySearchableText(o1).toUpperCase().startsWith(unidecode.decodeAndTrim(getText()).toUpperCase())?1000000:0;
                                int av_2 =  getEntrySearchableText(o2).toUpperCase().startsWith(unidecode.decodeAndTrim(getText()).toUpperCase())?1000000:0;

                                return ret - av_1 + av_2;
                            }

                        });
                        filteredEntries.clear();
                        filteredEntries.addAll(searchResult);

                        //Only show popup if not in filter mode
                        if (!isPopupHidden()) {
                            populatePopup(searchResult, text);
                            if (!entriesPopup.isShowing()) {
                                entriesPopup.show(parentField, Side.BOTTOM, 0, 0);
                            }
                        }
                    } else {
                        entriesPopup.hide();
                    }
                }
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                entriesPopup.hide();
            }
        });

    }

    private ObservableValue<Boolean> focusedProperty() {
        return parentField.focusedProperty();
    }

    private String getText() {
        if(parentField!=null && parentField.getText()!=null){
            return parentField.getText();
        }
        return new TextField().getText();
    }

    private ObservableValue<String> textProperty() {
        return parentField.textProperty();
    }

    private String getEntrySearchableText(Object entry) {
        if(entry instanceof CharValueTextSuggestion){
            return unidecode.decodeAndTrim(((CharValueTextSuggestion) entry).getSource_value());
        }
        if(entry instanceof String){
            return unidecode.decodeAndTrim((String) entry);
        }
        return unidecode.decodeAndTrim(entry.toString());
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public ArrayList<Object> getEntries() {
        return entries;
    }

    /**
     * Populate the entry set with the given search results. Display is limited
     * to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<Object> searchResult, String text) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int count = Math.min(searchResult.size(), getMaxEntries());
        for (int i = 0; i < count; i++) {
            final String result = getEntryDisplayText(searchResult.get(i));
            int occurence;

            if (isCaseSensitive()) {
                occurence = result.indexOf(text);
            } else {
                occurence = result.toLowerCase().indexOf(text.toLowerCase());
            }

            //Part before occurence (might be empty)
            Text pre = new Text(result.substring(0, occurence));
            //Part of (first) occurence
            Text in = new Text(result.substring(occurence,
                    occurence + text.length()));
            in.setStyle(getTextOccurenceStyle());
            //Part after occurence
            Text post = new Text(result.substring(occurence + text.length(),
                    result.length()));

            TextFlow entryFlow = new TextFlow(pre, in, post);

            CustomMenuItem item = new CustomMenuItem(entryFlow, true);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    setText(result);
                    entriesPopup.hide();
                }
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }

    private void setText(String result) {
        parentField.setText(result);
    }

    private String getEntryDisplayText(Object entry) {
        if(entry instanceof CharValueTextSuggestion){
            return ((CharValueTextSuggestion) entry).getDisplay_value();
        }
        if(entry instanceof String){
            return (String) entry;
        }
        return entry.toString();
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public String getTextOccurenceStyle() {
        return textOccurenceStyle;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setTextOccurenceStyle(String textOccurenceStyle) {
        this.textOccurenceStyle = textOccurenceStyle;
    }

    public boolean isPopupHidden() {
        return popupHidden;
    }

    public void setPopupHidden(boolean popupHidden) {
        this.popupHidden = popupHidden;
    }

    public ObservableList<Object> getFilteredEntries() {
        return filteredEntries;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

}