package model;

import controllers.Char_description;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import transversal.dialog_toolbox.CaracDeclarationDialog;

import java.sql.SQLException;

public final class mouseHoverTableCell<S, T> extends TextFieldTableCell<S, T> {

    private static TableView table;

	public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> list) {
                final TextFieldTableCell<S, T> result = new TextFieldTableCell<S, T>(converter);
                final Popup popup = new Popup();
                popup.setAutoHide(true);

                final EventHandler<MouseEvent> hoverListener = new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        final Label popupContent = new Label(result.getText());
                        popupContent.setStyle("-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;");

                        popup.getContent().clear();
                        popup.getContent().addAll(popupContent);

                        if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                            popup.hide();
                        } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED && result!=null && result.getText()!=null && result.getText().length()>0) {
                            popup.show(result, event.getScreenX() + 10, event.getScreenY());                
                        }
                    }
                };

                result.setOnMouseEntered(hoverListener);
                result.setOnMouseExited(hoverListener);
                return result;
            }
        };
    }

    public static Callback forCharEdition(TableView tableGrid,Char_description parent) {
        table = tableGrid;
        return forCharEdition(new DefaultStringConverter(),parent);
    }
    private static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forCharEdition(final StringConverter<T> converter,Char_description parent) {
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
                            //new Label(((CharPaneRow)table.getItems().get(result.getIndex())).getChar_name_translated());
                            final Label ligne1 = new Label("Edit characteristic");
                            final Label ligne2 = new Label("Delete characteristic");

                            ligne1.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    try {
                                        CaracDeclarationDialog.CaracEditionPopUp(((CharPaneRow)table.getItems().get(result.getIndex())).getCarac(),parent.account,parent.tableController.tableGrid);
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
                                }
                            });

                            ligne1.setStyle("-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;");
                            ligne2.setStyle("-fx-background-color:#ACB9CA; -fx-border-color:#ACB9CA; -fx-border-width: 1px; -fx-padding: 5px; -fx-text-fill:#212934;");

                            popup.getContent().clear();
                            popup.getContent().add(ligne1);
                            //popup.getContent().add(ligne2);

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