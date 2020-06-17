package service;

import controllers.Auto_classification_progress;
import javafx.application.Platform;
import javafx.scene.control.Label;
import model.TimeMasterRow;
import model.TimeMasterTemplateRow;
import transversal.generic.Tools;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TimeMaster {
	
	public ArrayList<TimeMasterRow> grid = new ArrayList<TimeMasterRow>(29);
	HashMap<String,Integer> CARDS = new HashMap<String,Integer>();
	ArrayList<Boolean> onoff = new ArrayList<Boolean>(29);
	
	public void initiate_grid(Integer target_desc_cardinality, Integer ref_desc_cardinality, Integer preclass_ref_desc_cardinality) {
		grid = new ArrayList<TimeMasterRow>(29);
		CARDS.put("target_desc_cardinality",target_desc_cardinality);
		CARDS.put("ref_desc_cardinality",ref_desc_cardinality);
		CARDS.put("preclass_ref_desc_cardinality",preclass_ref_desc_cardinality);
		CARDS.put("classif_download", target_desc_cardinality+ref_desc_cardinality);
		CARDS.put("preclassif_download", target_desc_cardinality+preclass_ref_desc_cardinality);
		
		for(int i=0;i<29;i++) {
			grid.add(null);
			onoff.add(false);
		}
	}
	
	public void fillSummaryRow(int row,Label first,Label second,Label third,HashSet<Integer> DEPENDENCIES_INDEX) {
		grid.set(row-1,new TimeMasterRow());
		grid.get(row-1).first=first;
		grid.get(row-1).second=second;
		grid.get(row-1).third=third;
		grid.get(row-1).DEPENDENCIES.addAll(DEPENDENCIES_INDEX);
		Platform.runLater(new Runnable (){

			@Override
			public void run() {
				double sum = 0;
				for(int i:DEPENDENCIES_INDEX) {
					sum = sum + grid.get(i-1).remaining_time;
				}
				
				second.setText(Tools.formatDuration(Duration.ofNanos((long) sum )));
				third.setText(Tools.formatDuration(Duration.ofNanos((long) sum )));
				grid.get(row-1).remaining_time=sum;
			}
			
		});
		
	}
	
	public void fillRow(Auto_classification_progress parent, int row, Label first,Label second,Label third, TimeMasterTemplateRow template, double mult_factor, boolean isLaunched) {
		isLaunched = false;
		grid.set(row-1, new TimeMasterRow());
		grid.get(row-1).first=first;
		grid.get(row-1).second=second;
		grid.get(row-1).third=third;
		grid.get(row-1).template = template;
		grid.get(row-1).remaining_time=!isLaunched?mult_factor * ( CARDS.get(template.getCard()) * template.getInitial_time() ):template.getInitial_time();
		if(mult_factor==0 || CARDS.get(template.getCard())==0) {
			parent.set_semi_opaque(first);
		}
		
		
		
		
		
		template.setInitial_time(!isLaunched?mult_factor * ( CARDS.get(template.getCard()) * template.getInitial_time() ):template.getInitial_time());
		
		Platform.runLater(new Runnable (){

			@Override
			public void run() {
				second.setText(Tools.formatDuration(Duration.ofNanos((long) template.getInitial_time() )));
				third.setText(Tools.formatDuration(Duration.ofNanos((long) template.getInitial_time() )));
				
			}
			
		});
		
	}
	
	
	public void startSummaryRow(int row) {
		HashSet<TimeMasterRow> tmp = new HashSet<TimeMasterRow>();
		for(int i:grid.get(row-1).DEPENDENCIES) {
			tmp.add(grid.get(i-1));
		}
		grid.get(row-1).start_summary(tmp);
	}
	
	public void startRow(int row,double remaining_steps) {
		if(onoff.get(row-1)) {
			return;
		}
		onoff.set(row-1, true);
		grid.get(row-1).start_cpu(remaining_steps);
	}
	public void startRow(int row) {
		grid.get(row-1).start_network();
	}
	
	public void stopRow(int row) {
		try{
			grid.get(row-1).stop();
		}catch(Exception V) {
			
		}
	}
	
	public void increment(int row) {
		grid.get(row-1).increment();
	}
	
	public void increment_data(int row,double current_speed) {
		grid.get(row-1).increment_data(current_speed);
	}
	
	public void increment_data_row(int row,DownloadSegmenter downloadSegmenter) {
		grid.get(row-1).increment_data(downloadSegmenter);
	}

	public void startRow(int row, DownloadSegmenter downloadSegmenter) {
		grid.get(row-1).start_network(downloadSegmenter);
	}

	// called at end of operation, prints message and time since tick().
	static void tock (String action) {
	    long mstime = (System.nanoTime() - ManualRuleServices.tickTime) / 1000000;
	    System.out.println(action + ": " + mstime + "ms");
	}

	// called at start of operation, for timing
	static void tick () {
	    ManualRuleServices.tickTime = System.nanoTime();
	}
}
