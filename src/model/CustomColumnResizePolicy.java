package model;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.ArrayList;

public  class CustomColumnResizePolicy implements Callback<TableView.ResizeFeatures, Boolean>
{
    double mTVWidth;

    @Override
    public Boolean call(TableView.ResizeFeatures arg0)
    {
        TableView tv = arg0.getTable();
        Double tvWidth = tv.widthProperty().getValue();
        if (tvWidth == null || tvWidth <= 0.0)
        {
            return false;
        }

        if (mTVWidth != tvWidth && arg0.getColumn() == null)
        {
            mTVWidth = tvWidth;

            int numColsToSize = 0;
            double fixedColumnsWidths = 0;
            for (TableColumn col : new ArrayList<TableColumn>(tv.getColumns()))
            {
                if (col.isResizable() && col.isVisible())
                {
                    ++numColsToSize;
                }
                else if (col.isVisible())
                {
                    fixedColumnsWidths += col.getWidth();
                }
            }

            if (numColsToSize == 0)
                return TableView.UNCONSTRAINED_RESIZE_POLICY.call(arg0);

            TableColumn lastCol = null;
            for (TableColumn col : new ArrayList<TableColumn>(tv.getColumns()))
            {
                if (col.isResizable() && col.isVisible())
                {
                    double newWidth = (tvWidth - fixedColumnsWidths) / numColsToSize;
                    col.setPrefWidth(newWidth);
                    lastCol = col;
                }
            }
            if (lastCol != null)
            {
                lastCol.setPrefWidth(lastCol.getPrefWidth()-2);
            }

            return true;
        }
        else
        {
            return TableView.UNCONSTRAINED_RESIZE_POLICY.call(arg0);
        }
    }
}