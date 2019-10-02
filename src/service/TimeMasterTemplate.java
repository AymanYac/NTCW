package service;

import java.util.ArrayList;
import java.util.HashMap;

import model.TimeMasterTemplateRow;

public class TimeMasterTemplate {
	//Array of <TimeMasterTemplateRow> stores for every row in the classification progress screen,
	//the type of the row (data processing / data downloading,
	//it's unit speed and its mulitplying factor)
	public ArrayList<TimeMasterTemplateRow> grid = new ArrayList<TimeMasterTemplateRow>(29);
	//Stores the unit download speed
	public Double network_speed;
	//Stores the unit download time
	public Double fetch_time;
	//Stores the unit cleansing time
	public double clean_speed;
	
	//Takes the target and reference cardinalities and sets the grid field
	public void fill(Integer target_desc_cardinality, Integer ref_desc_cardinality,
			Integer preclass_ref_desc_cardinality) {
		for(int i=0;i<29;i++) {
			grid.add(i,null);
			if(i==1 || i==15) {//	Row 2 and 16 are data download rows, we set the corresponding TimeMasterTemplateRow network speed, fetch time and cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setInitial_average(network_speed);
				tmp.setInitial_time(fetch_time);
				tmp.setCard(i==1?"classif_download":"preclassif_download");
				grid.set(i, tmp);
			}
			if(i>2 && i<6) {//	Row 4,5,6 are data cleansing rows, we set the corresponding TimeMasterTemplateRow cleansing speed and cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("ref_desc_cardinality");
				tmp.setInitial_time(clean_speed);
				grid.set(i, tmp);
			}
			if(i==7) { //	Row 8 is a rule row, we set the corresponding cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("ref_desc_cardinality");
				tmp.setInitial_time(0.0000001);
				grid.set(i, tmp);
			}
			if(i==8) { //	Row 9 is a rule row, we set the corresponding cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("ref_desc_cardinality");
				tmp.setInitial_time(0.00001);
				grid.set(i, tmp);
			}
			if(i>9 && i<13) { //	Row 11,12,13 are data cleansing rows, we set the corresponding TimeMasterTemplateRow cleansing speed and cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("target_desc_cardinality");
				tmp.setInitial_time(clean_speed);
				grid.set(i, tmp);
			}
			if(i==13) { //	Row 14 is a rule row, we set the corresponding cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("target_desc_cardinality");
				tmp.setInitial_time(0.0000001);
				grid.set(i, tmp);
			}
			
			if(i>16 && i<20) { //	Row 18,19,20 are data cleansing rows, we set the corresponding TimeMasterTemplateRow cleansing speed and cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("preclass_ref_desc_cardinality");
				tmp.setInitial_time(clean_speed);
				grid.set(i, tmp);
			}
			if(i==21) { //	Row 22 is a rule row, we set the corresponding cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("preclass_ref_desc_cardinality");
				tmp.setInitial_time(0.0000001);
				grid.set(i, tmp);
			}
			if(i==22) { //	Row 23 is a rule row, we set the corresponding cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("preclass_ref_desc_cardinality");
				tmp.setInitial_time(0.00001);
				grid.set(i, tmp);
			}
			
			if(i>23 && i<27) { //	Row 25,26,27 are data cleansing rows, we set the corresponding TimeMasterTemplateRow cleansing speed and cardinality
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("target_desc_cardinality");
				tmp.setInitial_time(clean_speed);
				grid.set(i, tmp);
			}
			if(i==27) { //	Row 28 is data upload row
				TimeMasterTemplateRow tmp = new TimeMasterTemplateRow();
				tmp.setCard("target_desc_cardinality");
				tmp.setInitial_time(0.0000001);
				grid.set(i, tmp);
			}
		}
		
	}
	//Sets the global download speed, download time and cleansing speed based on the result of the ran benchmark
	public void setFactors(HashMap<String, Double> RET) {
		// TODO Auto-generated method stub
		this.network_speed=RET.get("this speed");
		this.fetch_time=(RET.get("this fetch"))/5000.0;
		for(String key:RET.keySet()) {
			if(key.contains("clean")) {
				this.clean_speed=(RET.get(key))/5000.0;
			}
		}
	}
	
}
