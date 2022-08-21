package transversal.data_exchange_toolbox;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.fxmisc.richtext.StyleClassedTextArea;

public class ScrollTimer extends AnimationTimer {
    private GridPane grid;
    private Node btn;
    private long lastUpdate;
    private String direction;

    /**
     * This method needs to be overridden by extending classes. It is going to
     * be called in every frame while the {@code AnimationTimer} is active.
     *
     * @param now The timestamp of the current frame given in nanoseconds. This
     *            value will be the same for all {@code AnimationTimers} called
     *            during one frame.
     */
    @Override
    public void handle(long now) {
        if(now-lastUpdate>500000000){
            lastUpdate=now;
            if(direction.equals("down")){
                grid.lookupAll("StyleClassedTextArea").forEach(pa -> {
                    Integer paCol = GridPane.getColumnIndex(pa);
                    Integer paColSpan = GridPane.getColumnSpan(pa);
                    Integer paRow = GridPane.getRowIndex(pa);
                    Integer paRowSpan = GridPane.getRowSpan(pa);

                    Integer finalPaColSpan = paColSpan != null ? new Integer(paColSpan - 1) : new Integer(0);
                    Integer finalPaRowSpan = paRowSpan != null ? new Integer(paRowSpan - 1) : new Integer(0);

                    if ((GridPane.getColumnIndex(btn).equals(paCol) && GridPane.getRowIndex(btn).equals(paRow))
                            || (GridPane.getColumnIndex(btn) == paCol + finalPaColSpan && GridPane.getRowIndex(btn) == paRow + finalPaRowSpan)
                    ) {
                        if (pa instanceof StyleClassedTextArea) {
                            //System.out.println(((StyleClassedTextArea) pa).getProperties().);
                            ((StyleClassedTextArea) pa).showParagraphAtTop(
                                    ((StyleClassedTextArea) pa).getParagraphs().indexOf(
                                            ((StyleClassedTextArea) pa).getVisibleParagraphs().get(1)
                                    )
                            );
                        }
                    }
                });
            }else{
                grid.lookupAll("StyleClassedTextArea").forEach(pa->{
                    Integer paCol = GridPane.getColumnIndex(pa);
                    Integer paColSpan = GridPane.getColumnSpan(pa);
                    Integer paRow = GridPane.getRowIndex(pa);
                    Integer paRowSpan = GridPane.getRowSpan(pa);

                    Integer finalPaColSpan = paColSpan!=null?new Integer(paColSpan -1):new Integer(0);
                    Integer finalPaRowSpan = paRowSpan!=null?new Integer(paRowSpan -1):new Integer(0);

                    if(( GridPane.getColumnIndex(btn).equals(paCol) && GridPane.getRowIndex(btn).equals(paRow) )
                            || (GridPane.getColumnIndex(btn) == paCol + finalPaColSpan && GridPane.getRowIndex(btn) == paRow + finalPaRowSpan)
                    ){
                        if(pa instanceof StyleClassedTextArea){
                            try{
                                ((StyleClassedTextArea) pa).showParagraphAtTop(
                                        ((StyleClassedTextArea) pa).getParagraphs().indexOf(
                                                ((StyleClassedTextArea) pa).getVisibleParagraphs().get(0)
                                        ) - 1
                                );
                            }catch (Exception V){

                            }
                        }
                    }
                });
            }
        }
    }

    public void prepareScrollDown(GridPane grid, Node btn) {
        this.direction="down";
        this.lastUpdate = 0;
        this.grid = grid;
        this.btn = btn;
    }

    public void prepareScrollUp(GridPane grid, Node btn) {
        prepareScrollDown(grid,btn);
        this.direction="up";
    }
}
