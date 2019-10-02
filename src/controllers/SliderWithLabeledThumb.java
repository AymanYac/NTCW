package controllers;


import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SliderWithLabeledThumb extends Application {
    private Slider s;

	public void start(Stage ps) {
        s = new Slider();
        s.getStylesheets().add(".slider .thumb{\r\n" + 
        		"	-fx-background-color:#445469;\r\n" + 
        		"	-fx-border-color:lightgrey;\r\n" + 
        		"	-fx-border-radius:10;\r\n" + 
        		"}\r\n" + 
        		"\r\n" + 
        		".slider .axis { -fx-tick-label-fill: white; }");
        StackPane root = new StackPane(s);
        root.setPadding(new Insets(5));
        s.setOrientation(Orientation.VERTICAL);
        s.setMin(49);
        s.setMax(99);
        s.setValue(51);
        s.setMinorTickCount(0);
        s.setMajorTickUnit(1);

        Scene scene = new Scene(root);

        s.applyCss();
        s.layout();
        Pane p = (Pane) s.lookup(".thumb");
        Label l = new Label();
        l.textProperty().bind(s.valueProperty().asString("%.1f").concat(" °"));
        l.translateYProperty().set(-20);
        p.getChildren().add(l);

        ps.setScene(scene);
        ps.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

	public void setMin(int i) {
		s.setMin(i);
	}

	public void setMax(int i) {
		s.setMax(i);
	}

	public void setMajorTickUnit(int i) {
		s.setMajorTickUnit(i);
	}

	public void setMinorTickCount(int i) {
		s.setMinorTickCount(i);
	}

	public void setSnapToTicks(boolean b) {
		s.setSnapToTicks(b);
	}

	public double getValue() {
		return s.getValue();
	}
}