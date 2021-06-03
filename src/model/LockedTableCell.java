package model;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;

public abstract class LockedTableCell<T, S> extends TableCell<T, S> {

    {

        Platform.runLater(() -> {

            ScrollBar sc = (ScrollBar) getTableView().queryAccessibleAttribute(AccessibleAttribute.HORIZONTAL_SCROLLBAR);
            sc.valueProperty().addListener((ob, o, n) -> {

                double doubleValue = n.doubleValue();
                this.setTranslateX(doubleValue);
                this.toFront();

            });

        });

    }

}