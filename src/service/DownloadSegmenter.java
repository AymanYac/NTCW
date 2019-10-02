package service;

import java.util.ArrayList;
import java.util.HashMap;

import model.GlobalConstants;

public class DownloadSegmenter {
	
	private HashMap<String,Integer> PROJECT2CARD = new HashMap<String,Integer>();
	private HashMap<String,Integer> PROJECT2SEGMENT = new HashMap<String,Integer>();
	private Integer segment_no = 0;
	private Integer remaining_segments;
	
	
	public Integer getSegment_no() {
		return segment_no;
	}

	public Integer getRemaining_segments() {
		return remaining_segments;
	}
	
	public void setRemaining_segments(Integer remaining_segments) {
		this.remaining_segments = remaining_segments;
	}

	public void initialize_count(String project_id,int cardinality) {
		PROJECT2CARD.put(project_id, cardinality);
		PROJECT2SEGMENT.put(project_id, 0);
		segment_no = segment_no + cardinality/GlobalConstants.AUTO_SEGMENT_SIZE +((cardinality%GlobalConstants.AUTO_SEGMENT_SIZE>0)?1:0);
		remaining_segments = segment_no;
	}
	
	public ArrayList<Integer> get_next_range(String project_id) {
		ArrayList<Integer> ret = new ArrayList<Integer>(2);
		
		if(PROJECT2SEGMENT.get(project_id)*GlobalConstants.AUTO_SEGMENT_SIZE>PROJECT2CARD.get(project_id)) {
			ret.add(0);
			ret.add(0);
			return ret;
		}
		ret.add(PROJECT2SEGMENT.get(project_id)*GlobalConstants.AUTO_SEGMENT_SIZE);
		PROJECT2SEGMENT.put(project_id,PROJECT2SEGMENT.get(project_id)+1);
		ret.add(PROJECT2SEGMENT.get(project_id)*GlobalConstants.AUTO_SEGMENT_SIZE);
		
		return ret;
	}
	
}
