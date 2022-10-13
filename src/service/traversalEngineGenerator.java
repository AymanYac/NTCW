package service;

import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


//ParentHelper.setTraversalEngine(grid, traversalEngineGenerator.CharTraversalEngine(grid,new TextField[] {value_field,uom_field,min_field_uom,max_field_uom,note_field_uom,rule_field}));

@SuppressWarnings("restriction")
public class traversalEngineGenerator {

	public static ParentTraversalEngine CharTraversalEngine(GridPane parent, TextField[] textFields) {
		Algorithm algo = new Algorithm() {

            @Override
            public Node select(Node node, Direction dir,
                    TraversalContext context) {
                Node next = trav(node, dir);
                return next;
            }

            /**
             * Just for fun: implemented to invers reaction
             */
            @SuppressWarnings("incomplete-switch")
			private Node trav(Node node, Direction drctn) {
            	int index;
                for(index=0;index<textFields.length;index++) {
                	if(textFields[index].isFocused()) {
                		/*System.out.println(String.valueOf(index)+" is focused!");*/
                	}
                }
                switch (drctn) {
                    //case DOWN:
                    case RIGHT:
                    case NEXT:
                    case NEXT_IN_LINE:    
                        index++;
                        break;
                    case LEFT:
                    case PREVIOUS:
                    //case UP:
                        index--;
                }

                if (index < 0) {
                    index = textFields.length - 1;
                }
                index %= textFields.length;

                /*System.out.println("Select <" + index + ">");*/

                return textFields[index];
            }

            @Override
            public Node selectFirst(TraversalContext context) {
                return textFields[0];
            }

            @Override
            public Node selectLast(TraversalContext context) {
                return textFields[textFields.length - 1];
            }

        };
		
		return new ParentTraversalEngine(parent, algo);
	}

}
