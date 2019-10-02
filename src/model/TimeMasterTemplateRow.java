package model;

public class TimeMasterTemplateRow {
	
	//Stores the total expected time for this task
	double initial_time;
	//Stores the average speed if this task requires downloads
	double initial_average;
	//Stores this tasks required number of steps
	String card;
	
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	public double getInitial_time() {
		return initial_time;
	}
	public void setInitial_time(double initial_time) {
		this.initial_time = initial_time;
	}
	
	public double getInitial_average() {
		return initial_average;
	}
	public void setInitial_average(double initial_average) {
		this.initial_average = initial_average;
	}
	
	
}
