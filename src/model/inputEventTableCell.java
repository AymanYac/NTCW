package model;

import controllers.Char_description;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import transversal.dialog_toolbox.CaracDeclarationDialog;

import java.sql.SQLException;

public final class inputEventTableCell<S, T> extends TextFieldTableCell<S, T> {

    private static TableView table;

    public static Callback forCharEdition(TableView tableGrid,Char_description parent) {
        table = tableGrid;
        return forCharEdition(new DefaultStringConverter(),parent);
    }
    private static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forCharEdition(final StringConverter<T> converter,Char_description parent) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            private static final String HOVERED_BUTTON_STYLE = "-fx-background-color:#212934; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#ACB9CA;";
            private static final String STANDARD_BUTTON_STYLE="-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;";
            @Override
            public TableCell<S, T> call(TableColumn<S, T> list) {
                final TextFieldTableCell<S, T> result = new TextFieldTableCell<S, T>(converter);
                final Popup popup = new Popup();
                popup.setAutoHide(true);

                final EventHandler<Event> eventHandler = new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        try{
                            //new Label(((CharPaneRow)table.getItems().get(result.getIndex())).getChar_name_translated());
                            final Label ligne1 = new Label("Edit characteristic");
                            final Label ligne2 = new Label("Delete characteristic");



                            ligne1.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    try {
                                        CaracDeclarationDialog.CaracEditionPopUp(((CharPaneRow)table.getItems().get(result.getIndex())).getCarac(),parent.account,parent.tableController.tableGrid);
                                        popup.hide();
                                        parent.refresh_ui_display();
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            ligne2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    CaracDeclarationDialog.CaracDeletion(((CharPaneRow)table.getItems().get(result.getIndex())).getCarac());
                                    popup.hide();
                                    parent.refresh_ui_display();
                                }
                            });

                            setStyle(ligne1);
                            setStyle(ligne2);


                            GridPane contentGrid = new GridPane();
                            contentGrid.add(ligne1,0,0);
                            contentGrid.add(ligne2,0,1);
                            contentGrid.setHgrow(ligne1, Priority.ALWAYS);
                            ligne1.setMaxWidth(Integer.MAX_VALUE);
                            contentGrid.setHgrow(ligne2, Priority.ALWAYS);
                            ligne2.setMaxWidth(Integer.MAX_VALUE);
                            contentGrid.setGridLinesVisible(true);

                            popup.getContent().clear();
                            popup.getContent().add(contentGrid);

                            if ( event instanceof KeyEvent && ((KeyEvent)event).getCode() == KeyCode.ESCAPE) {
                                popup.hide();
                            }else if (event instanceof MouseEvent && ((MouseEvent)event).getButton() == MouseButton.SECONDARY ) {
                                popup.show(result, ((MouseEvent)event).getScreenX() + 10, ((MouseEvent)event).getScreenY());
                            }
                            /*else if (((MouseEvent)event).getEventType() == MouseEvent.MOUSE_ENTERED && result!=null && result.getText()!=null && result.getText().length()>0) {
                                popup.show(result, ((MouseEvent)event).getScreenX() + 10, ((MouseEvent)event).getScreenY());
                            }*/

                        }catch (Exception V){

                        }

                    }

                    private void setStyle(Node ligne) {
                        ligne.styleProperty().bind(
                                Bindings
                                        .when(ligne.hoverProperty())
                                        .then(
                                                new SimpleStringProperty(HOVERED_BUTTON_STYLE)
                                        )
                                        .otherwise(
                                                new SimpleStringProperty(STANDARD_BUTTON_STYLE)
                                        )
                        );
                    }
                };

                //result.setOnMouseEntered(hoverListener);
                result.setOnKeyPressed(eventHandler);
                result.setOnMouseClicked(eventHandler);
                return result;
            }
        };
    }
	public static Callback forCharNameTranslation(TableView tableGrid) {
		table = tableGrid;
		return forCharNameTranslation(new DefaultStringConverter());
	}

	private static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forCharNameTranslation(final StringConverter<T> converter) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> list) {
                final TextFieldTableCell<S, T> result = new TextFieldTableCell<S, T>(converter);
                final Popup popup = new Popup();
                popup.setAutoHide(true);

                final EventHandler<MouseEvent> hoverListener = new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        try{
                            final Label popupContent = new Label(((CharPaneRow)table.getItems().get(result.getIndex())).getChar_name_translated());
                            popupContent.setStyle("-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;");

                            popup.getContent().clear();
                            popup.getContent().addAll(popupContent);

                            if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                                popup.hide();
                            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED && result!=null && result.getText()!=null && result.getText().length()>0) {
                                popup.show(result, event.getScreenX() + 10, event.getScreenY());
                            }
                        }catch (Exception V){

                        }

                    }
                };

                result.setOnMouseEntered(hoverListener);
                result.setOnMouseExited(hoverListener);
                return result;
            }
        };
    }
}