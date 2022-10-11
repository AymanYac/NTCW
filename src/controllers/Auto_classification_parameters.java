package controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.*;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class Auto_classification_parameters {
	
	private HashSet<String> REFERENCE_PROJECTS;
	private Integer target_desc_cardinality;
	
	private String pid;
	
	
	public void setPid(String pid) {
		this.pid=pid;
	}
	
	private ArrayList<AbaqueRow> ABAQUE = new ArrayList<AbaqueRow>(1024) ;
	
	@FXML
	public Label estimated;
	
	@FXML private Label classifLabel;
	@FXML private Label preclassifLabel;
	
	@FXML private TextFlow classifTextFlow;
	@FXML private TextFlow preclassifTextFlow;
	

	@FXML private Slider build_sample_slider;
	@FXML private Slider term_slider;
	@FXML RadioButton alpha_Y;
	@FXML private void alpha_Y(){
		alpha_Y.setSelected(true);
		alpha_N.setSelected(false);
		update_graphs();
	}
	@FXML RadioButton alpha_N;
	@FXML private void alpha_N(){
		alpha_Y.setSelected(false);
		alpha_N.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton base_sd;
	@FXML private void base_sd() {
		base_ld.setSelected(false);
		base_pref.setSelected(false);
		base_sd.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton base_ld;
	@FXML private void base_ld() {
		base_sd.setSelected(false);
		base_pref.setSelected(false);
		base_ld.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton base_pref;
	@FXML private void base_pref() {
		base_sd.setSelected(false);
		base_ld.setSelected(false);
		base_pref.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton base_raw;
	@FXML private void base_raw() {
		base_raw.setSelected(true);
		base_simple.setSelected(false);
		base_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton base_simple;
	@FXML private void base_simple() {
		base_raw.setSelected(false);
		base_simple.setSelected(true);
		base_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton base_clean;
	@FXML private void base_clean() {
		base_raw.setSelected(false);
		base_simple.setSelected(false);
		base_clean.setSelected(true);
		update_graphs();
	}
	
	@FXML private RadioButton target_sd;
	@FXML private void target_sd() {
		target_sd.setSelected(true);
		target_ld.setSelected(false);
		target_pref.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton target_ld;
	@FXML private void target_ld() {
		target_sd.setSelected(false);
		target_ld.setSelected(true);
		target_pref.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton target_pref;
	@FXML private void target_pref() {
		target_sd.setSelected(false);
		target_ld.setSelected(false);
		target_pref.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton target_raw;
	@FXML private void target_raw() {
		target_raw.setSelected(true);
		target_simple.setSelected(false);
		target_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton target_simple;
	@FXML private void target_simple() {
		target_raw.setSelected(false);
		target_simple.setSelected(true);
		target_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton target_clean;
	@FXML private void target_clean() {
		target_raw.setSelected(false);
		target_simple.setSelected(false);
		target_clean.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton char_Y;
	@FXML private void char_Y() {
		char_Y.setSelected(true);
		char_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton char_N;
	@FXML private void char_N() {
		char_N.setSelected(true);
		char_Y.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton abv_Y;
	@FXML private void abv_Y() {
		abv_Y.setSelected(true);
		abv_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton abv_N;
	@FXML private void abv_N() {
		abv_N.setSelected(true);
		abv_Y.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton spell_Y;
	@FXML private void spell_Y() {
		spell_Y.setSelected(true);
		spell_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton spell_N;
	@FXML private void spell_N() {
		spell_N.setSelected(true);
		spell_Y.setSelected(false);
		update_graphs();
	}
	@FXML private Slider ta_slider;
	@FXML private Slider tb_slider;
	@FXML private Spinner<String> rank1;
	@FXML private Spinner<String> rank2;
	@FXML private Spinner<String> rank3;
	@FXML private Slider eps_slider;
	@FXML private Slider factor_slider;
	

	@FXML private Slider preclass_build_sample_slider;
	@FXML private Slider preclass_term_slider;
	@FXML RadioButton preclass_alpha_Y;
	@FXML private void preclass_alpha_Y(){
		preclass_alpha_Y.setSelected(true);
		preclass_alpha_N.setSelected(false);
		update_graphs();
	}
	@FXML RadioButton preclass_alpha_N;
	@FXML private void preclass_alpha_N(){
		preclass_alpha_Y.setSelected(false);
		preclass_alpha_N.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_sd;
	@FXML private void preclass_base_sd() {
		preclass_base_ld.setSelected(false);
		preclass_base_pref.setSelected(false);
		preclass_base_sd.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_ld;
	@FXML private void preclass_base_ld() {
		preclass_base_sd.setSelected(false);
		preclass_base_pref.setSelected(false);
		preclass_base_ld.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_pref;
	@FXML private void preclass_base_pref() {
		preclass_base_sd.setSelected(false);
		preclass_base_ld.setSelected(false);
		preclass_base_pref.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_raw;
	@FXML private void preclass_base_raw() {
		preclass_base_raw.setSelected(true);
		preclass_base_simple.setSelected(false);
		preclass_base_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_simple;
	@FXML private void preclass_base_simple() {
		preclass_base_raw.setSelected(false);
		preclass_base_simple.setSelected(true);
		preclass_base_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_base_clean;
	@FXML private void preclass_base_clean() {
		preclass_base_raw.setSelected(false);
		preclass_base_simple.setSelected(false);
		preclass_base_clean.setSelected(true);
		update_graphs();
	}
	
	@FXML private RadioButton preclass_target_sd;
	@FXML private void preclass_target_sd() {
		preclass_target_sd.setSelected(true);
		preclass_target_ld.setSelected(false);
		preclass_target_pref.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_target_ld;
	@FXML private void preclass_target_ld() {
		preclass_target_sd.setSelected(false);
		preclass_target_ld.setSelected(true);
		preclass_target_pref.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_target_pref;
	@FXML private void preclass_target_pref() {
		preclass_target_sd.setSelected(false);
		preclass_target_ld.setSelected(false);
		preclass_target_pref.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_target_raw;
	@FXML private void preclass_target_raw() {
		preclass_target_raw.setSelected(true);
		preclass_target_simple.setSelected(false);
		preclass_target_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_target_simple;
	@FXML private void preclass_target_simple() {
		preclass_target_raw.setSelected(false);
		preclass_target_simple.setSelected(true);
		preclass_target_clean.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_target_clean;
	@FXML private void preclass_target_clean() {
		preclass_target_raw.setSelected(false);
		preclass_target_simple.setSelected(false);
		preclass_target_clean.setSelected(true);
		update_graphs();
	}
	@FXML private RadioButton preclass_char_Y;
	@FXML private void preclass_char_Y() {
		preclass_char_Y.setSelected(true);
		preclass_char_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_char_N;
	@FXML private void preclass_char_N() {
		preclass_char_N.setSelected(true);
		preclass_char_Y.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_abv_Y;
	@FXML private void preclass_abv_Y() {
		preclass_abv_Y.setSelected(true);
		preclass_abv_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_abv_N;
	@FXML private void preclass_abv_N() {
		preclass_abv_N.setSelected(true);
		preclass_abv_Y.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_spell_Y;
	@FXML private void preclass_spell_Y() {
		preclass_spell_Y.setSelected(true);
		preclass_spell_N.setSelected(false);
		update_graphs();
	}
	@FXML private RadioButton preclass_spell_N;
	@FXML private void preclass_spell_N() {
		preclass_spell_N.setSelected(true);
		preclass_spell_Y.setSelected(false);
		update_graphs();
	}
	@FXML private Slider preclass_ta_slider;
	@FXML private Slider preclass_tb_slider;
	@FXML private Spinner<String> preclass_rank1;
	@FXML private Spinner<String> preclass_rank2;
	@FXML private Spinner<String> preclass_rank3;
	@FXML private Slider preclass_eps_slider;
	@FXML private Slider preclass_factor_slider;
	
	
	
	@FXML private Slider granularity;
	@FXML private Slider preclass_granularity;
	
	@FXML private Label granularity_label;
	@FXML private Label preclass_granularity_label;
	
	
	
	@FXML
	private StackedBarChart<String, Number> graph;
	@FXML
	private CategoryAxis xAxe;
	@FXML
	private NumberAxis yAxe;
	
	
	
	
	
	
	@FXML private Button def_button;
	@FXML private void load_def() {
		refresh_dynamic_fields(false);
	}
	@FXML private Button save;
	private Integer CLASSIFICATION_GRANULARITY;
	private Integer PRECLASSIFICATION_GRANULARITY;
	private Integer preClassification_tb;
	private Integer classification_tb;
	private Double preClassification_ta;
	private Double classification_ta;
	private Double classificationBS;
	private Double preClassificationBS;
	private double classification_graph_coverage;
	private double classification_graph_accuracy;
	private double preclassification_graph_coverage;
	private double preclassification_graph_accuracy;
	public Auto_classification_launch parent;
	private UserAccount account;
	@FXML MenuBar menubar;
	@FXML GridPane grille;
	private ArrayList<RowConstraints> rc = new ArrayList<RowConstraints> ();
	
	
	@FXML void initialize() {
		
		grille.getRowConstraints().clear();
		load_constraints();
		grille.getRowConstraints().addAll(rc);
		
		
		Tools.decorate_menubar(menubar, account);
		
		graph.setAnimated(false);
		graph.setLegendVisible(false);
		graph.setHorizontalGridLinesVisible(false);
		graph.setVerticalGridLinesVisible(false);
		graph.setVerticalZeroLineVisible(false);
		graph.setHorizontalZeroLineVisible(false);
		graph.getYAxis().setTickMarkVisible(false);
		graph.getXAxis().setTickMarkVisible(false);
		graph.getYAxis().setTickLabelsVisible(false);
		graph.getXAxis().setTickLabelsVisible(false);
		classifLabel.setVisible(false);
		preclassifLabel.setVisible(false);
		
		
		
		build_sample_slider.setMin(0);
		build_sample_slider.setMax(100);
		//build_sample_slider.setShowTickLabels(true);
		//build_sample_slider.setShowTickMarks(true);
		build_sample_slider.setMajorTickUnit(25);
		build_sample_slider.setMinorTickCount(1);
		//build_sample_slider.setSnapToTicks(true);
		
		eps_slider.setMin(-5);
		eps_slider.setMax(-1);
		//eps_slider.setShowTickLabels(true);
		//eps_slider.setShowTickMarks(true);
		eps_slider.setMajorTickUnit(0.5);
		eps_slider.setMinorTickCount(1);
		//eps_slider.setSnapToTicks(true);
		
		
		factor_slider.setMin(1);
		factor_slider.setMax(10);
		//factor_slider.setShowTickLabels(true);
		//factor_slider.setShowTickMarks(true);
		factor_slider.setMajorTickUnit(1);
		factor_slider.setMinorTickCount(0);
		factor_slider.setSnapToTicks(true);
		
		
		ta_slider.setMin(GlobalConstants.MIN_TA);
		ta_slider.setMax(100);
		//ta_slider.setShowTickLabels(true);
		//ta_slider.setShowTickMarks(true);
		ta_slider.setMajorTickUnit(4);
		ta_slider.setMinorTickCount(0);
		//ta_slider.setSnapToTicks(true);
		
		
		tb_slider.setMin(1);
		tb_slider.setMax(10);
		//tb_slider.setShowTickLabels(true);
		//tb_slider.setShowTickMarks(true);
		tb_slider.setMajorTickUnit(1);
		tb_slider.setMinorTickCount(0);
		tb_slider.setSnapToTicks(true);
		
		term_slider.setMin(1);
		term_slider.setMax(5);
		//term_slider.setShowTickLabels(true);
		//term_slider.setShowTickMarks(true);
		term_slider.setMajorTickUnit(1);
		term_slider.setMinorTickCount(0);
		term_slider.setSnapToTicks(true);
		
		
		
		
		preclass_build_sample_slider.setMin(0);
		preclass_build_sample_slider.setMax(100);
		//preclass_build_sample_slider.setShowTickLabels(true);
		//preclass_build_sample_slider.setShowTickMarks(true);
		preclass_build_sample_slider.setMajorTickUnit(25);
		preclass_build_sample_slider.setMinorTickCount(1);
		//preclass_build_sample_slider.setSnapToTicks(true);

		preclass_eps_slider.setMin(-5);
		preclass_eps_slider.setMax(-1);
		//preclass_eps_slider.setShowTickLabels(true);
		//preclass_eps_slider.setShowTickMarks(true);
		preclass_eps_slider.setMajorTickUnit(0.5);
		preclass_eps_slider.setMinorTickCount(1);
		//preclass_eps_slider.setSnapToTicks(true);

		
		preclass_factor_slider.setMin(1);
		preclass_factor_slider.setMax(10);
		//preclass_factor_slider.setShowTickLabels(true);
		//preclass_factor_slider.setShowTickMarks(true);
		preclass_factor_slider.setMajorTickUnit(1);
		preclass_factor_slider.setMinorTickCount(0);
		preclass_factor_slider.setSnapToTicks(true);

		
		preclass_ta_slider.setMin(GlobalConstants.MIN_TA);
		preclass_ta_slider.setMax(100);
		//preclass_ta_slider.setShowTickLabels(true);
		//preclass_ta_slider.setShowTickMarks(true);
		preclass_ta_slider.setMajorTickUnit(4);
		preclass_ta_slider.setMinorTickCount(0);
		//preclass_ta_slider.setSnapToTicks(true);

		
		preclass_tb_slider.setMin(1);
		preclass_tb_slider.setMax(10);
		//preclass_tb_slider.setShowTickLabels(true);
		//preclass_tb_slider.setShowTickMarks(true);
		preclass_tb_slider.setMajorTickUnit(1);
		preclass_tb_slider.setMinorTickCount(0);
		preclass_tb_slider.setSnapToTicks(true);

		preclass_term_slider.setMin(1);
		preclass_term_slider.setMax(5);
		//preclass_term_slider.setShowTickLabels(true);
		//preclass_term_slider.setShowTickMarks(true);
		preclass_term_slider.setMajorTickUnit(1);
		preclass_term_slider.setMinorTickCount(0);
		preclass_term_slider.setSnapToTicks(true);
		
		granularity.setMin(1);
		//granularity.setMax(this.CLASSIFICATION_GRANULARITY);
		granularity.setValue(granularity.getMax());
		//granularity.setShowTickLabels(true);
		//granularity.setShowTickMarks(true);
		granularity.setMajorTickUnit(1);
		granularity.setMinorTickCount(0);
		granularity.setSnapToTicks(true);
		
		preclass_granularity.setMin(1);
		//preclass_granularity.setMax(this.PRECLASSIFICATION_GRANULARITY);
		preclass_granularity.setValue(preclass_granularity.getMax());
		//preclass_granularity.setShowTickLabels(true);
		//preclass_granularity.setShowTickMarks(true);
		preclass_granularity.setMajorTickUnit(1);
		preclass_granularity.setMinorTickCount(0);
		preclass_granularity.setSnapToTicks(true);
		
		
		try {
			Connection conn = Tools.spawn_connection_from_pool();
			Statement ps = conn.createStatement();
			ResultSet rs;
			rs = ps.executeQuery("select base_sample_size, class_accuracy,  rule_baseline_threshold, rule_accuracy_threshold,coverage from public_ressources.abacus_values");
			while(rs.next()) {
				AbaqueRow rw = new AbaqueRow();
				rw.setBs(rs.getInt(1));
				rw.setClass_accuracy(rs.getDouble(2));
				rw.setTc(rs.getInt(3));
				rw.setTa(rs.getDouble(4));
				rw.setCoverage(rs.getDouble(5));
				rw.setFamily_accuracy(0.0);
				ABAQUE.add(rw);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		Platform.runLater(new Runnable() {
		    public void run() {
		    	refresh_dynamic_fields(true);
		    }
		});
		
		
		
		
		 ObservableList<String> ranks = FXCollections.observableArrayList("Accuracy","Type","Total");
		 SpinnerValueFactory<String> r1Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(ranks);
		 r1Factory.setValue("Accuracy");
		 rank1.setValueFactory(r1Factory);
		 SpinnerValueFactory<String> r2Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(ranks);
		 r1Factory.setValue("Type");
		 rank2.setValueFactory(r2Factory);
		 SpinnerValueFactory<String> r3Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(ranks);
		 r1Factory.setValue("Total");
		 rank3.setValueFactory(r3Factory);
		 
		 
		 rank1.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(r2Factory.getValue().equals(newValue)) {
		    	r2Factory.setValue(oldValue);
		    	}else {
		    		r3Factory.setValue(oldValue);
		    	}
		    }
		    );
		 
		 rank2.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(r1Factory.getValue().equals(newValue)) {
		    	r1Factory.setValue(oldValue);
		    	}else {
		    		r3Factory.setValue(oldValue);
		    	}
		    }
		    );
		 
		 rank3.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(r1Factory.getValue().equals(newValue)) {
		    	r1Factory.setValue(oldValue);
		    	}else {
		    		r2Factory.setValue(oldValue);
		    	}
		    }
		    );
		 
		 
		 
		 
		 ObservableList<String> preclass_ranks = FXCollections.observableArrayList("Accuracy","Type","Total");
		 SpinnerValueFactory<String> preclass_r1Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(preclass_ranks);
		 preclass_r1Factory.setValue("Accuracy");
		 preclass_rank1.setValueFactory(preclass_r1Factory);
		 SpinnerValueFactory<String> preclass_r2Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(preclass_ranks);
		 preclass_r1Factory.setValue("Type");
		 preclass_rank2.setValueFactory(preclass_r2Factory);
		 SpinnerValueFactory<String> preclass_r3Factory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(preclass_ranks);
		 preclass_r1Factory.setValue("Total");
		 preclass_rank3.setValueFactory(preclass_r3Factory);
		 
		 
		 preclass_rank1.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(preclass_r2Factory.getValue().equals(newValue)) {
		    	preclass_r2Factory.setValue(oldValue);
		    	}else {
		    		preclass_r3Factory.setValue(oldValue);
		    	}
		    }
		    );
		 
		 preclass_rank2.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(preclass_r1Factory.getValue().equals(newValue)) {
		    	preclass_r1Factory.setValue(oldValue);
		    	}else {
		    		preclass_r3Factory.setValue(oldValue);
		    	}
		    }
		    );
		 
		 preclass_rank3.valueProperty().addListener((obs, oldValue, newValue) -> 
		    {if(preclass_r1Factory.getValue().equals(newValue)) {
		    	preclass_r1Factory.setValue(oldValue);
		    	}else {
		    		preclass_r2Factory.setValue(oldValue);
		    	}
		    }
		    );
	     
	}

	private void load_constraints() {
		for(int i = 0;i<58;i++) {
			RowConstraints tmp = new RowConstraints();
			tmp.setMinHeight(0);
			tmp.setMaxHeight(1000);
			tmp.setPercentHeight(0);
			
			if(i==2){tmp.setPercentHeight(3.75);}
			if(i==3){tmp.setPercentHeight(1.25);}
			if(i==4){tmp.setPercentHeight(0.625);}
			if(i==5){tmp.setPercentHeight(2.5);}
			if(i==8){tmp.setPercentHeight(2.5);}
			if(i==9){tmp.setPercentHeight(1.25);}
			if(i==10){tmp.setPercentHeight(2.5);}
			if(i==11){tmp.setPercentHeight(2.5);}
			if(i==12){tmp.setPercentHeight(2.5);}
			if(i==13){tmp.setPercentHeight(2.5);}
			if(i==14){tmp.setPercentHeight(2.5);}
			if(i==15){tmp.setPercentHeight(2.5);}
			if(i==16){tmp.setPercentHeight(2.5);}
			if(i==17){tmp.setPercentHeight(2.5);}
			if(i==18){tmp.setPercentHeight(2.5);}
			if(i==19){tmp.setPercentHeight(2.5);}
			if(i==20){tmp.setPercentHeight(2.5);}
			if(i==21){tmp.setPercentHeight(2.5);}
			if(i==22){tmp.setPercentHeight(2.5);}
			if(i==23){tmp.setPercentHeight(2.5);}
			if(i==24){tmp.setPercentHeight(1.25);}
			if(i==25){tmp.setPercentHeight(1.25);}
			if(i==26){tmp.setPercentHeight(1.25);}
			if(i==27){tmp.setPercentHeight(1.25);}
			if(i==28){tmp.setPercentHeight(1.25);}
			if(i==30){tmp.setPercentHeight(2.5);}
			if(i==31){tmp.setPercentHeight(1.25);}
			if(i==32){tmp.setPercentHeight(2.5);}
			if(i==33){tmp.setPercentHeight(2.5);}
			if(i==34){tmp.setPercentHeight(2.5);}
			if(i==35){tmp.setPercentHeight(2.5);}
			if(i==36){tmp.setPercentHeight(2.5);}
			if(i==37){tmp.setPercentHeight(2.5);}
			if(i==38){tmp.setPercentHeight(2.5);}
			if(i==39){tmp.setPercentHeight(2.5);}
			if(i==40){tmp.setPercentHeight(2.5);}
			if(i==41){tmp.setPercentHeight(2.5);}
			if(i==42){tmp.setPercentHeight(2.5);}
			if(i==43){tmp.setPercentHeight(2.5);}
			if(i==44){tmp.setPercentHeight(3.75);}
			if(i==45){tmp.setPercentHeight(2.5);}
			if(i==46){tmp.setPercentHeight(2.5);}
			if(i==47){tmp.setPercentHeight(2.5);}
			if(i==48){tmp.setPercentHeight(1.875);}




			rc.add(tmp);
		}
		
	}

	@SuppressWarnings("unused")
	private void refresh_dynamic_fields(boolean sleep) {
		
		
		
		
		try {
			if(false && sleep) {
				TimeUnit.SECONDS.sleep(2);
			}

			try{
				granularity.setMax(this.CLASSIFICATION_GRANULARITY);
				granularity_label.setText(String.valueOf(this.CLASSIFICATION_GRANULARITY));
			}catch(Exception V) {
				granularity.setMax(4);
				granularity_label.setText(String.valueOf(4));
			}
			try{
				preclass_granularity.setMax(this.PRECLASSIFICATION_GRANULARITY);
				preclass_granularity_label.setText(String.valueOf(this.PRECLASSIFICATION_GRANULARITY));
				
			}catch(Exception W) {
				preclass_granularity.setMax(4);
				preclass_granularity_label.setText(String.valueOf(4));
			}
			
			try {
				preclass_ta_slider.setValue(this.preClassification_ta);
			}
			catch(Exception L) {
				
			}
			try {
				preclass_tb_slider.setValue(this.preClassification_tb);
			}catch(Exception M) {
				
			}try {
				ta_slider.setValue(this.classification_ta);
			}catch(Exception N) {
				
			}try {
				tb_slider.setValue(this.classification_tb);
			}catch(Exception O) {
				
			}try {
				build_sample_slider.setValue(100);
			}catch(Exception P) {
				
			}try {
				preclass_build_sample_slider.setValue(100);
			}catch(Exception Q) {
				
			}
			
			
			
			
			
			decorate_slider(build_sample_slider);
			decorate_slider(term_slider);
			decorate_slider(ta_slider);
			decorate_slider(tb_slider);
			decorate_slider(eps_slider);
			decorate_slider(factor_slider);
			decorate_slider(preclass_build_sample_slider);
			decorate_slider(preclass_term_slider);
			decorate_slider(preclass_ta_slider);
			decorate_slider(preclass_tb_slider);
			decorate_slider(preclass_eps_slider);
			decorate_slider(preclass_factor_slider);
			decorate_slider(granularity);
			decorate_slider(preclass_granularity);
			
			   
		     alpha_N();
		     target_pref();
		     char_Y();
		     abv_N();
		     spell_N();
			preclass_alpha_N();
			preclass_target_pref();
			preclass_char_Y();
			preclass_abv_N();
			preclass_spell_N();
			
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			
			granularity.setMax(4);
			preclass_granularity.setMax(4);
			
		}
		
	}

	private void decorate_slider(Slider slider) {
		Pane thumb = (Pane) slider.lookup(".thumb");
		thumb.setMinWidth(18);
		thumb.setMaxWidth(18);
		
		thumb.setMinHeight(18);
		thumb.setMaxHeight(18);
		
		thumb.setPrefWidth(18);
		thumb.setPrefHeight(18);
		
		
		Label label = new Label();
        label.setFont(new Font("Colibri", 12));
        label.textProperty().bind(slider.valueProperty().asString("%.1f"));
        label.translateYProperty().set(-18);
        label.setEllipsisString(label.getText());
        thumb.getChildren().add(label);
		
		
	}

	@SuppressWarnings({ "unused", "unchecked" })
	@FXML private void update_graphs() {
		
		//Launch Benchmark
		
		BinaryClassificationParameters tmp = new BinaryClassificationParameters();

		tmp.setClassif_granularity((int) Math.ceil(granularity.getValue()));
		tmp.setPreclassif_granularity((int) Math.ceil(preclass_granularity.getValue()));
		
		
		tmp.setClassif_buildSampleSize(build_sample_slider.getValue());
		tmp.setClassif_minimumTermLength((int) Math.ceil(term_slider.getValue()));
		tmp.setClassif_keepAlpha(alpha_Y.isSelected());
		/*if(base_sd.isSelected()) {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(base_ld.isSelected()) {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_LONG);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}*/
		if(target_sd.isSelected()) {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(target_ld.isSelected()) {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_LONG);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_LONG);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_PREFERED);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}
		tmp.setClassif_cleanAbv(abv_Y.isSelected());
		tmp.setClassif_cleanChar(char_Y.isSelected());
		tmp.setClassif_cleanSpell(spell_Y.isSelected());
		tmp.setClassif_Ta(ta_slider.getValue());
		tmp.setClassif_Tb((int) Math.ceil(tb_slider.getValue()));
		ArrayList<String> t = new ArrayList<String>();
		t.add(rank1.getValue());
		t.add(rank2.getValue());
		t.add(rank3.getValue());
		tmp.setClassif_rank(t);
		tmp.setClassif_epsilon(Math.pow(10, eps_slider.getValue()));
		tmp.setClassif_typeFactor(factor_slider.getValue());
		
		tmp.setPreclassif_buildSampleSize(preclass_build_sample_slider.getValue());
		tmp.setPreclassif_minimumTermLength((int) Math.ceil(preclass_term_slider.getValue()));
		tmp.setPreclassif_keepAlpha(preclass_alpha_Y.isSelected());
		/*if(preclass_base_sd.isSelected()) {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(preclass_base_ld.isSelected()) {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_LONG);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}*/
		if(preclass_target_sd.isSelected()) {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(preclass_target_ld.isSelected()) {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_LONG);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_LONG);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_PREFERED);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}
		tmp.setPreclassif_cleanAbv(preclass_abv_Y.isSelected());
		tmp.setPreclassif_cleanChar(preclass_char_Y.isSelected());
		tmp.setPreclassif_cleanSpell(preclass_spell_Y.isSelected());
		tmp.setPreclassif_Ta(preclass_ta_slider.getValue());
		tmp.setPreclassif_Tb((int) Math.ceil(preclass_tb_slider.getValue()));
		t = new ArrayList<String>();
		t.add(preclass_rank1.getValue());
		t.add(preclass_rank2.getValue());
		t.add(preclass_rank3.getValue());
		tmp.setPreclassif_rank(t);
		tmp.setPreclassif_epsilon(Math.pow(10, preclass_eps_slider.getValue()));
		tmp.setPreclassif_typeFactor(preclass_factor_slider.getValue());
		
		try {
			parent.setConfig(tmp);
			parent.simulate();
		}catch(Exception V) {
			
		}
		
		
		
		
		
		
		//UPDATE GRAPH
		if(this.classificationBS!=null) {
			
		}else {
			this.classificationBS=(double) 0;
		}
		if(this.preClassificationBS!=null) {
		}else {
			this.preClassificationBS=(double) 0;
		}
		
		//find closest coverage and accuracy for classification 
		Integer closest_bs=null;
		for(AbaqueRow rw:ABAQUE) {
			Integer rwbs = rw.getBs();
			if(!(closest_bs!=null) && Math.abs(rwbs-this.classificationBS*build_sample_slider.getValue())<=10) {
				
				
				
				
				closest_bs=rwbs;
				break;
			}
		}
		if(!(closest_bs!=null)) {
			closest_bs = GlobalConstants.MAX_BS;
		}
		
		try {
			Connection conn = Tools.spawn_connection_from_pool();
			PreparedStatement stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs);
			stmt.setDouble(2, Math.min(Math.ceil(ta_slider.getValue()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.ceil(tb_slider.getValue()), GlobalConstants.MAX_TC));
			ResultSet rs = stmt.executeQuery();
			rs.next();
			;
			double new_accuracy = rs.getDouble(1);
			double new_coverage = rs.getDouble(2);
			
			stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs+10);
			stmt.setDouble(2, Math.min(Math.ceil(ta_slider.getValue()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.ceil(tb_slider.getValue()), GlobalConstants.MAX_TC));
			rs = stmt.executeQuery();
			rs.next();
			Double second_coverage=null;
			try {
				second_coverage = rs.getDouble(2);
			}catch (Exception V){
				second_coverage = null;
			}
			rs.close();
			stmt.close();
			conn.close();
			
			if(build_sample_slider.getValue()==0) {
				new_coverage = 0;
			}
			if(build_sample_slider.getValue()<10) {
				new_coverage = new_coverage*(build_sample_slider.getValue()/10);
			}
			if(second_coverage!=null &&!(build_sample_slider.getValue()<10)) {
				new_coverage = new_coverage + (second_coverage-new_coverage)*((build_sample_slider.getValue()-closest_bs)/10);
			}
			
			this.classification_graph_coverage = this.classificationBS>0?new_coverage:0;
			parent.classification_coverage = this.classificationBS>0?new_coverage:0;
			
			this.classification_graph_accuracy = (this.classification_graph_coverage!=0)?new_accuracy:0;
			parent.classification_quality = (this.classification_graph_coverage!=0)?new_accuracy:0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		//find closest coverage and accuracy for preclassification 
		
		closest_bs=null;
		for(AbaqueRow rw:ABAQUE) {
			Integer rwbs = rw.getBs();
			if(!(closest_bs!=null) && Math.abs(rwbs-(this.preClassificationBS*preclass_build_sample_slider.getValue()))<=10) {
				closest_bs=rwbs;
				
				break;
			}
		}
		if(!(closest_bs!=null)) {
			closest_bs = GlobalConstants.MAX_BS;
		}
		try {
			Connection conn = Tools.spawn_connection_from_pool();
			PreparedStatement stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs);
			stmt.setDouble(2, Math.min(Math.ceil(preclass_ta_slider.getValue()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.ceil(preclass_tb_slider.getValue()), GlobalConstants.MAX_TC));
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			double new_accuracy = rs.getDouble(1);
			double new_coverage = rs.getDouble(2);
			
			stmt = conn.prepareStatement("select class_accuracy,coverage from public_ressources.abacus_values where base_sample_size = ? and rule_accuracy_threshold = ? and rule_baseline_threshold = ?");
			stmt.setInt(1, closest_bs+10);
			stmt.setDouble(2, Math.min(Math.ceil(preclass_ta_slider.getValue()), GlobalConstants.MAX_TA));
			stmt.setDouble(3, Math.min(Math.ceil(preclass_tb_slider.getValue()), GlobalConstants.MAX_TC));
			rs = stmt.executeQuery();
			rs.next();
			Double second_coverage=null;
			try {
				second_coverage = rs.getDouble(2);
			}catch (Exception V){
				second_coverage = null;
			}
			rs.close();
			stmt.close();
			conn.close();
			
			if(preclass_build_sample_slider.getValue()==0) {
				new_coverage = 0;
			}
			if(preclass_build_sample_slider.getValue()<10) {
				new_coverage = new_coverage*(preclass_build_sample_slider.getValue()/10);
			}
			if(second_coverage!=null &&!(preclass_build_sample_slider.getValue()<10)) {
				new_coverage = new_coverage + (second_coverage-new_coverage)*((preclass_build_sample_slider.getValue()-closest_bs)/10);
				}
			
			
			this.preclassification_graph_coverage = this.preClassificationBS>0?new_coverage:0;
			parent.preclassification_coverage = this.preClassificationBS>0?new_coverage:0;
			
			this.preclassification_graph_accuracy = (preclassification_graph_coverage!=0)?new_accuracy:0;
			parent.preclassification_quality = (preclassification_graph_coverage!=0)?new_accuracy:0;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		
		
		//Defining the y axis
		xAxe.setCategories(FXCollections.<String>observableArrayList(Arrays.asList
				   ("Estimated coverage using reference data")));
		//Prepare XYChart.Series objects by setting data 
		XYChart.Series<String, Number> series1 = new XYChart.Series<>(); 
		series1.setName("Classification quality: "+Math.ceil(this.classification_graph_accuracy)+"%");
		series1.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.classification_graph_coverage)); 
		XYChart.Series<String, Number> series2 = new XYChart.Series<>(); 
		series2.setName("Pre-classification quality: "+Math.ceil(this.preclassification_graph_accuracy)+"%");
		series2.getData().add(new XYChart.Data<>("Estimated coverage using reference data", this.preclassification_graph_coverage-this.classification_graph_coverage));
		graph.getData().clear();
		graph.getData().addAll(series1,series2);
		yAxe.setAutoRanging(false);
		yAxe.setUpperBound(100);
		
		//Decorated the graph
		decorate_graph();
		
		parent.update_chart();
		parent.simulate();
		
	}

	private void decorate_graph() {
		classifLabel.setTranslateY((1.03*graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_graph_coverage));
		classifTextFlow.setTranslateY((1.03*graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.classification_graph_coverage));
		
		classifLabel.setVisible(false);
		classifTextFlow.setVisible(this.classification_graph_coverage>10);
		
		classifLabel.setText("Coverage: "+(int)Math.ceil(this.classification_graph_coverage)+"%\nQuality "+(int)Math.ceil(this.classification_graph_accuracy)+"%");
		Text text1 = new Text();
		text1.setText("Coverage: "+(int)Math.ceil(this.classification_graph_coverage)+"%");
		Text text2 = new Text();
		text2.setText("\nEst. quality: "+(int)Math.ceil(this.classification_graph_accuracy)+"%");
		
		classifTextFlow.toFront();
		//classifLabel.toFront();
		DoubleProperty fontSize = new SimpleDoubleProperty(10);
		fontSize.bind(yAxe.widthProperty().add(yAxe.heightProperty()).divide(60));
		classifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		classifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		preclassifLabel.setTranslateY((1.03*graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.preclassification_graph_coverage));
		preclassifTextFlow.setTranslateY((1.03*graph.getHeight()-yAxe.getHeight()) + yAxe.getHeight()*0.01*(100-this.preclassification_graph_coverage));
		
		preclassifLabel.setVisible(false);
		preclassifTextFlow.setVisible((this.preclassification_graph_coverage-this.classification_graph_coverage)>10);
		
		preclassifLabel.setText("Coverage: "+(int)Math.ceil(this.preclassification_graph_coverage-this.classification_graph_coverage)+"%\nQuality "+(int)Math.ceil(this.preclassification_graph_accuracy)+"%");
		Text text3 = new Text();
		text3.setText("Coverage: "+(int)Math.ceil(this.preclassification_graph_coverage-this.classification_graph_coverage)+"%");
		Text text4 = new Text();
		text4.setText("\nEst. quality: "+(int)Math.ceil(this.preclassification_graph_accuracy)+"%");
		
		
		preclassifTextFlow.toFront();
		//preclassifLabel.toFront();
		
		preclassifLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		preclassifTextFlow.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
		
		text1.setStyle("-fx-font-weight	: bold");
		text1.setFill(classifLabel.getTextFill());
		
		text2.setFill(classifLabel.getTextFill());
		text2.setStyle("-fx-font-style: italic");
		classifTextFlow.getChildren().clear();
		classifTextFlow.getChildren().add(text1);
		classifTextFlow.getChildren().add(text2);
		
		
		
		
		text3.setStyle("-fx-font-weight	: bold");
		text3.setFill(preclassifLabel.getTextFill());
		
		text4.setFill(preclassifLabel.getTextFill());
		text4.setStyle("-fx-font-style: italic");
		preclassifTextFlow.getChildren().clear();
		preclassifTextFlow.getChildren().add(text3);
		preclassifTextFlow.getChildren().add(text4);
		
		
		
		
	}

	@SuppressWarnings("unused")
	@FXML private void save() {
		BinaryClassificationParameters tmp = new BinaryClassificationParameters();
		
		tmp.setClassif_granularity((int) Math.ceil(granularity.getValue()));
		tmp.setPreclassif_granularity((int) Math.ceil(preclass_granularity.getValue()));
		
		
		tmp.setClassif_buildSampleSize(build_sample_slider.getValue());
		tmp.setClassif_minimumTermLength((int) Math.ceil(term_slider.getValue()));
		tmp.setClassif_keepAlpha(alpha_Y.isSelected());
		/*if(base_sd.isSelected()) {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(base_ld.isSelected()) {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_LONG);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(base_raw.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(base_simple.isSelected()) {
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}*/
		if(target_sd.isSelected()) {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_SHORT);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(target_ld.isSelected()) {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_LONG);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_LONG);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(false && target_raw.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(false && target_simple.isSelected()) {
				tmp.setClassif_targetDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setClassif_targetDescriptionType(DescriptionType.CLEAN_PREFERED);
				tmp.setClassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}
		tmp.setClassif_cleanAbv(abv_Y.isSelected());
		tmp.setClassif_cleanChar(char_Y.isSelected());
		tmp.setClassif_cleanSpell(spell_Y.isSelected());
		tmp.setClassif_Ta(ta_slider.getValue());
		tmp.setClassif_Tb((int) Math.ceil(tb_slider.getValue()));
		ArrayList<String> t = new ArrayList<String>();
		t.add(rank1.getValue());
		t.add(rank2.getValue());
		t.add(rank3.getValue());
		tmp.setClassif_rank(t);
		tmp.setClassif_epsilon(Math.pow(10, eps_slider.getValue()));
		tmp.setClassif_typeFactor(factor_slider.getValue());
		
		tmp.setPreclassif_buildSampleSize(preclass_build_sample_slider.getValue());
		tmp.setPreclassif_minimumTermLength((int) Math.ceil(preclass_term_slider.getValue()));
		tmp.setPreclassif_keepAlpha(preclass_alpha_Y.isSelected());
		/*if(preclass_base_sd.isSelected()) {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(preclass_base_ld.isSelected()) {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_LONG);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(preclass_base_raw.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(preclass_base_simple.isSelected()) {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}*/
		if(preclass_target_sd.isSelected()) {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.RAW_SHORT);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.SIMPLE_SHORT);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_SHORT);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_SHORT);
			}
		}else if(preclass_target_ld.isSelected()) {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_LONG);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_LONG);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_LONG);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_LONG);
			}
		}else {
			if(false && preclass_target_raw.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.RAW_PREFERED);
			}else if(false && preclass_target_simple.isSelected()) {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.SIMPLE_PREFERED);
			}else {
				tmp.setPreclassif_targetDescriptionType(DescriptionType.CLEAN_PREFERED);
				tmp.setPreclassif_baseDescriptionType(DescriptionType.CLEAN_PREFERED);
			}
		}
		tmp.setPreclassif_cleanAbv(preclass_abv_Y.isSelected());
		tmp.setPreclassif_cleanChar(preclass_char_Y.isSelected());
		tmp.setPreclassif_cleanSpell(preclass_spell_Y.isSelected());
		tmp.setPreclassif_Ta(preclass_ta_slider.getValue());
		tmp.setPreclassif_Tb((int) Math.ceil(preclass_tb_slider.getValue()));
		t = new ArrayList<String>();
		t.add(preclass_rank1.getValue());
		t.add(preclass_rank2.getValue());
		t.add(preclass_rank3.getValue());
		tmp.setPreclassif_rank(t);
		tmp.setPreclassif_epsilon(Math.pow(10, preclass_eps_slider.getValue()));
		tmp.setPreclassif_typeFactor(preclass_factor_slider.getValue());
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		Stage stage = (Stage) save.getScene().getWindow();
	    stage.close();
	    parent.setConfig(tmp);
	    if(1==1) {
	    	parent.parametersScene.hide();
	    	return;
	    }
	    
	    try {
		    Stage primaryStage = new Stage();
		    
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/Auto_classification_launch.fxml"));
			AnchorPane root = fxmlLoader.load();

			Scene scene = new Scene(root,400,400);
			
			ToolHeaderController.titleProperty.setValue("Neonec classification wizard - V"+GlobalConstants.TOOL_VERSION+" ["+account.getActive_project_name()+"]");
			primaryStage.setScene(scene);
			//primaryStage.setMinHeight(768);
			//primaryStage.setMinWidth(1024);
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			primaryStage.setMinHeight(768);primaryStage.setMinWidth(1024);primaryStage.setMaximized(true);primaryStage.setResizable(false);primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/NEONEC_Logo_Blue.png")));
			primaryStage.show();

			controllers.Auto_classification_launch controller = fxmlLoader.getController();

			controller.setUserAccount(account);
			
		    controller.setPid(this.pid);
			controller.setRefenceProjects(REFERENCE_PROJECTS);
			controller.setTarget_desc_cardinality(this.target_desc_cardinality);
			controller.setConfig(tmp);
			
			
		} catch(Exception e) {
			ExceptionDialog.show("FX001 Auto_class_launch", "FX001 Auto_class_launch", "FX001 Auto_class_launch");
			e.printStackTrace();
		}
	}

	public void setRefenceProjects(HashSet<String> rEFERENCE_PROJECTS) {
		this.REFERENCE_PROJECTS = rEFERENCE_PROJECTS;
	}

	public void setTarget_desc_cardinality(Integer Target_desc_cardinality) {
		this.target_desc_cardinality = Target_desc_cardinality;
	}

	public void setClassificationGranularity(Integer min) {
		
		this.CLASSIFICATION_GRANULARITY = min;
	}
	
	public void setPreClassificationGranularity(Integer min) {
		this.PRECLASSIFICATION_GRANULARITY = min;
	}

	public void setPreClassificationTb(Integer preClassification_tb) {
		this.preClassification_tb = preClassification_tb;
	}

	public void setClassficationTb(Integer classification_tb) {
		this.classification_tb = classification_tb;
	}

	public void setPreClassificationTa(Double preClassification_ta) {
		this.preClassification_ta=preClassification_ta;
	}

	public void setClassificationTa(Double classification_ta) {
		this.classification_ta=classification_ta;
	}

	public void setClassificationBS(double classificationBS2) {
		
		
		this.classificationBS = classificationBS2;
	}

	public void setPreClassificationBS(double preClassificationBS2) {
		;
		;
		this.preClassificationBS = preClassificationBS2;
	}

	public void setUserAccount(UserAccount account) {
		Tools.decorate_menubar(menubar, account);
		this.account=account;
	}
	
}
