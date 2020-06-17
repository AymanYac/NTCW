package model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import service.ConcurentTask;
import service.DownloadSegmenter;
import transversal.generic.Tools;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Queue;

public class TimeMasterRow {
	
	public Instant start;
	public Label first;
	public Label second;
	public Label third;
	public TimeMasterTemplateRow template;
	
	Instant last_increment;
	Queue<Duration> INTERVALS = new CircularFifoQueue<Duration>(5000);
	Queue<Double> BANDWIDTH = new CircularFifoQueue<Double>(500);
	Queue<Double> SEGMENT_DURATIONS = new CircularFifoQueue<Double>(3);
	double remaining_steps;
	public double remaining_time=0;
	public HashSet<Integer> DEPENDENCIES = new HashSet<Integer>();
	
	private ConcurentTask task;
	private double initial_time;
	private double initial_average;
	public long elapsed_time;

	
	public void start_cpu(double remaining_steps2) {
		start = Instant.now();
		last_increment = Instant.now();
		task = new ConcurentTask(this,false);
		this.remaining_steps = remaining_steps2;
		//third.setText(Tools.formatDuration(Duration.ofNanos((long) template.getInitial_time())));
	}
	
	public void start_network() {
		start = Instant.now();
		last_increment = Instant.now();
		task = new ConcurentTask(this,true);
		this.initial_time=template.getInitial_time();
		this.initial_average=template.getInitial_average();
		//third.setText(Tools.formatDuration(Duration.ofNanos((long) template.getInitial_time())));
		
	}
	
	public void stop() {
		task.stop();
		Platform.runLater(new Runnable() {
		    public void run() {
		    	
		    	second.setText(Tools.formatDuration(Duration.ofNanos(0)));
		    }
		});
		
	}

	public void increment() {
		Instant now = Instant.now();
		INTERVALS.add(Duration.between(last_increment, now));
		last_increment = now;
		remaining_steps=remaining_steps-1;
		
		long sum = Duration.ofNanos(0).toNanos();
		
		int denom = 1;
		for(Duration interval:INTERVALS) {
			sum=sum+interval.toNanos();
			denom+=1;
		}
		
		
		
		sum = sum / denom;
		
		sum=(long) (remaining_steps*sum);
		
		Duration remaining = Duration.ofNanos(sum);
		Platform.runLater(new Runnable() {
		    public void run() {
		    	second.setText(Tools.formatDuration(remaining));
		    }});
		this.remaining_time=sum;
	}

	public void increment_data(double current_speed) {
		Duration spent_time = Duration.between(start, Instant.now());
		double sum = 0.0;
		for(double speed:BANDWIDTH) {
			sum = sum+speed;
		}
		double current_average = sum/BANDWIDTH.size();
		
		
		double remaining_time = (this.initial_average*this.initial_time - spent_time.toNanos()*current_average)/current_average;
		if(remaining_time<=Duration.ofNanos(1000).toNanos()) {
			remaining_time = Duration.ofNanos(1000).toNanos();
		}
		
		if(current_speed>0) {
			BANDWIDTH.add(current_speed);
		}
		second.setText(Tools.formatDuration(Duration.ofNanos((long) remaining_time)));
		this.remaining_time=remaining_time;
		
	}

	public void start_summary(HashSet<TimeMasterRow> tmp) {
		start = Instant.now();
		task = new ConcurentTask(this,tmp);	
	}

	public void start_network(DownloadSegmenter downloadSegmenter) {
		start = Instant.now();
		last_increment = Instant.now();
		SEGMENT_DURATIONS.add(template.getInitial_time()/downloadSegmenter.getSegment_no());
		remaining_time=template.getInitial_time();
		task = new ConcurentTask(this,downloadSegmenter);
	}
	public void increment_data(DownloadSegmenter downloadSegmenter) {
		SEGMENT_DURATIONS.add((double) Duration.between(last_increment, Instant.now()).toNanos());
		last_increment = Instant.now();
		double sum = 0;
		for(Double interval:SEGMENT_DURATIONS) {
			sum+=interval;
		}
		sum = sum/(1.0*SEGMENT_DURATIONS.size());
		remaining_time = downloadSegmenter.getRemaining_segments()*sum;
		downloadSegmenter.setRemaining_segments(downloadSegmenter.getRemaining_segments()-1);
	}
	
	
}
