package service;

import controllers.Manual_classif;
import javafx.scene.control.Button;
import model.GlobalConstants;
import model.ItemFetcherRow;
import model.ManualClassProposition;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ManualClassifProposer {

	private HashMap<String,HashMap<String,Integer>> classifiedFW = new HashMap<String,HashMap<String,Integer>> ();
	private HashMap<String,HashMap<String,Integer>> classifiedMG = new HashMap<String,HashMap<String,Integer>> ();
	private HashMap<String,HashMap<String,Integer>> classifiedFor = new HashMap<String,HashMap<String,Integer>> ();
	private HashMap<String,HashMap<String,Integer>> classifiedDW = new HashMap<String,HashMap<String,Integer>> ();
	
	
	private Manual_classif parent_controller;
	private ItemFetcherRow item;
	public HashMap<String,String> segments;
	private HashMap<String,Boolean> register = new HashMap<String,Boolean>();
	public ArrayList<ManualClassProposition> propositions;
	public ManualClassProposition lastClassProp;
	private PriorityQueue<String> MF_FW_results;
	private ManualClassProposition pc;
	private ManualClassProposition lc;
	private ManualClassProposition mg;
	private ManualClassProposition f5;
	public List<String> for_words;
	public List<String> dw_words;
	
	public ManualClassifProposer(String active_project) {

		try {
			Integer gran = Tools.get_project_granularity(active_project);
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			segments = new HashMap<String,String>();
			ResultSet rs = stmt.executeQuery("select segment_id,level_"+gran+"_name,level_"+gran+"_name_translated from "+active_project+".project_segments");
			while(rs.next()) {
				segments.put(rs.getString("segment_id"), rs.getString(2)+"&&&"+rs.getString(3));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@SuppressWarnings("unused")
	public void loadPropositionsFor_OLD(ItemFetcherRow tmp) {
		
		intializeRegister();
		
		this.item = tmp;
		boolean loopAgain = true;
		
		pc = PreClass();
		lc = LastClass();
		mg = MaterialGroup();
		f5 = FieldFive();
		
		while(loopAgain) {
			/*if(register.get("PreClass")){
				ManualClassProposition pc = PreClass();
				if(pc!=null) {
					addProposition(pc);
					register.put("PreClass",pc.getExpectMore());
				}else {
					register.put("PreClass",false);
				}
			}
			loopAgain = register.get("PreClass");
			
			if(register.get("LastClass")){
				ManualClassProposition lc = LastClass();
				if(lc!=null) {
					addProposition(lc);
					register.put("LastClass",lc.getExpectMore());
				}else {
					register.put("LastClass",false);
				}
			}
			loopAgain = loopAgain || register.get("LastClass");
			
			*/
			if(register.get("MostFrequent_FirstWord")){
				ManualClassProposition fw = MostFrequent_FirstWord();
				if(fw!=null) {
					addProposition(fw);
					register.put("MostFrequent_FirstWord",fw.getExpectMore());
				}else {
					register.put("MostFrequent_FirstWord",false);
				}
			}
			loopAgain = register.get("MostFrequent_FirstWord");
			
			
			
			
			
			loopAgain = loopAgain && propositions.size()<GlobalConstants.NUMBER_OF_MANUAL_PROPOSITIONS_OLD;
		}
		
		for(ManualClassProposition prop:propositions) {
			
		};
		
		parent_controller.propose(propositions,pc,lc,mg,f5);
		
	}
	
	
	public void loadPropositionsFor(ItemFetcherRow tmp) {
			
			for(Button butn : parent_controller.propButtons) {
				parent_controller.context.disableButton(butn);
			}
			this.item = tmp;
			
			
			parent_controller.pcProp = PreClass();
			
			parent_controller.lcProp = LastClass();
			
			FWSpecific();
			
			MGSpecific();
			
			ForSpecific();
			
			DwSpecific();
			
			StaticSpecific();
			
			MachineLearning();
}
	
	
	
	private void FWSpecific() {
		String fw = null;
		if(item.getLong_description()!=null) {
			if(item.getShort_description()!=null) {
				fw = item.getLong_description().length()>item.getShort_description().length()?item.getLong_description().split(" ")[0]:item.getShort_description().split(" ")[0];

			}else {
				fw = item.getLong_description().split(" ")[0];
			}
			
		}else if(item.getShort_description()!=null) {
			fw = item.getShort_description();
		}else {
			return;
		}
		
		HashMap<String, Integer> map = classifiedFW.get(fw.toUpperCase());
		if(!(map!=null)) {
			return;
		}
		
		 List<String> ret = map.entrySet().stream()
		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .map(Map.Entry::getKey).collect(Collectors.toList());

		int idx=0;
		int matchNO=0;
		
		while(idx < parent_controller.propButtons.size()) {
			String meth = ManualClassifContext.methods.get(idx);
			Button btn = parent_controller.propButtons.get(idx);
			if(meth!=null && meth.equals("FW")) {
				try {
					String key = ret.get(matchNO);
					matchNO+=1;
					ManualClassProposition tmp = new ManualClassProposition();
					tmp.setSegment_id(key.split("&&&")[0]);
					tmp.setSegment_name(key.split("&&&")[1]);
					tmp.setProposer("FOR_WORD");
					tmp.setExpectMore(false);
					parent_controller.context.assignRecommendation(btn, tmp); 
					
				}
				catch(Exception V) {
					parent_controller.context.disableButton(btn);
				}
			}
			idx+=1;
		}
		
	}

	
	private void MGSpecific() {
		String mg = null;
		if(item.getMaterial_group()!=null) {
			mg = item.getMaterial_group();
		}else {
			return;
		}
		
		HashMap<String, Integer> map = classifiedMG.get(mg.toUpperCase());
		if(!(map!=null)) {
			return;
		}
		List<String> ret = map.entrySet().stream()
		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .map(Map.Entry::getKey).collect(Collectors.toList());
		
		int idx=0;
		int matchNO=0;
		
		while(idx < parent_controller.propButtons.size()) {
			String meth = ManualClassifContext.methods.get(idx);
			Button btn = parent_controller.propButtons.get(idx);
			if(meth!=null && meth.equals("MG")) {
				try {
					String key = ret.get(matchNO);
					matchNO+=1;
					ManualClassProposition tmp = new ManualClassProposition();
					tmp.setSegment_id(key.split("&&&")[0]);
					tmp.setSegment_name(key.split("&&&")[1]);
					tmp.setProposer("FOR_WORD");
					tmp.setExpectMore(false);
					parent_controller.context.assignRecommendation(btn, tmp);
					
				}
				catch(Exception V) {
					parent_controller.context.disableButton(btn);
				}
			}
			idx+=1;
		}
		
	}
	
	
	private void ForSpecific() {
		String desc = item.getShort_description() + item.getLong_description();
		for(String fw:for_words) {
			try {
				String target = desc.toUpperCase().split(fw.toUpperCase()+" ")[1];
				target = WordUtils.getSearchWords(target);
				HashMap<String, Integer> map = classifiedFor.get(target);
				if(!(map!=null)) {
					continue;
				}
				List<String> ret = map.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .map(Map.Entry::getKey).collect(Collectors.toList());
				
				
				int idx=0;
				int matchNO=0;
				
				while(idx < parent_controller.propButtons.size()) {
					String meth = ManualClassifContext.methods.get(idx);
					Button btn = parent_controller.propButtons.get(idx);
					if(meth!=null && meth.equals("FOR")) {
						try {
							String key = ret.get(matchNO);
							
							
							matchNO+=1;
							ManualClassProposition tmp = new ManualClassProposition();
							tmp.setSegment_id(key.split("&&&")[0]);
							tmp.setSegment_name(key.split("&&&")[1]);
							tmp.setProposer("FOR_WORD");
							tmp.setExpectMore(false);
							parent_controller.context.assignRecommendation(btn, tmp);
							
						}
						catch(Exception V) {
							parent_controller.context.disableButton(btn);
						}
					}
					idx+=1;
				}
				
				 
			}catch(Exception V) {
				continue;
			}
		}
	}
	
	
	private void DwSpecific() {
		String desc = item.getShort_description() + item.getLong_description();
		for(String dw:dw_words) {
			if(desc.toUpperCase().contains(dw.toUpperCase())){
				HashMap<String, Integer> map = classifiedDW.get(dw.toUpperCase());
				if(!(map!=null)) {
					continue;
				}
				List<String> ret = map.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .map(Map.Entry::getKey).collect(Collectors.toList());
				
				
				int idx=0;
				int matchNO=0;
				
				while(idx < parent_controller.propButtons.size()) {
					String meth = ManualClassifContext.methods.get(idx);
					Button btn = parent_controller.propButtons.get(idx);
					if(meth!=null && meth.equals("DW")) {
						try {
							String key = ret.get(matchNO);
							matchNO+=1;
							ManualClassProposition tmp = new ManualClassProposition();
							tmp.setSegment_id(key.split("&&&")[0]);
							tmp.setSegment_name(key.split("&&&")[1]);
							tmp.setProposer("FOR_WORD");
							tmp.setExpectMore(false);
							parent_controller.context.assignRecommendation(btn, tmp);
							
						}
						catch(Exception V) {
							parent_controller.context.disableButton(btn);
						}
					}
					idx+=1;
				}
				
			}
		}
	}
	
	private void StaticSpecific() {
		
		int idx=0;
		
		while(idx < parent_controller.propButtons.size()) {
			String meth = ManualClassifContext.methods.get(idx);
			Button btn = parent_controller.propButtons.get(idx);
			if(meth!=null && !meth.equals("FW") && !meth.equals("MG") && !meth.equals("FOR") && !meth.equals("DW") && !meth.equals("ML")) {
				try {
					String key = meth;
					ManualClassProposition tmp = new ManualClassProposition();
					tmp.setSegment_id(key.split("&&&")[0]);
					tmp.setSegment_name(key.split("&&&")[1]);
					tmp.setProposer("FOR_WORD");
					tmp.setExpectMore(false);
					parent_controller.context.assignRecommendation(btn, tmp);
					
				}
				catch(Exception V) {
					parent_controller.context.disableButton(btn);
				}
			}
			idx+=1;
		}
		
	}
	
	private void MachineLearning() {
		
		
		int idx=0;
		while(idx < parent_controller.propButtons.size()) {
			String meth = ManualClassifContext.methods.get(idx);
			Button btn = parent_controller.propButtons.get(idx);
			if(meth!=null && meth.equals("ML")) {
				try {
					String ml = item.getOnline_preclassif();
					Map<String, String> matches = segments.entrySet().stream().filter(
							m -> m.getValue().toUpperCase().split("&&&")[1].equals(ml.toUpperCase())).collect(
						Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
					int numMatches = matches.size();
						if(numMatches==1) {
							ManualClassProposition tmp = new ManualClassProposition();
							tmp.setSegment_id((String) matches.keySet().toArray()[0]);
							try{
								tmp.setSegment_name(matches.get(tmp.getSegment_id()).split("&&&")[1]);
							}catch(Exception V) {
								tmp.setSegment_name(matches.get(tmp.getSegment_id()).split("&&&")[0]);
							}
							tmp.setProposer("ML");
							tmp.setExpectMore(false);
							parent_controller.context.assignRecommendation(btn, tmp);
						}else {
							parent_controller.context.disableButton(btn);
						}
					
				}
				catch(Exception V) {
					parent_controller.context.disableButton(btn);
				}
			}
			idx+=1;
		}
		
	}
	
	
	
	private ManualClassProposition FieldFive() {
		String desc = item.getShort_description() + item.getLong_description();
		for(String fw:for_words) {
			try {
				String target = desc.toUpperCase().split(fw.toUpperCase()+" ")[1];
				target = WordUtils.getSearchWords(target);
				HashMap<String, Integer> map = classifiedFor.get(target);
				if(!(map!=null)) {
					continue;
				}
				List<String> ret = map.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .map(Map.Entry::getKey).collect(Collectors.toList());
				
				String key = ret.get(0);
				
				ManualClassProposition tmp = new ManualClassProposition();
				 tmp.setSegment_id(key.split("&&&")[0]);
				 tmp.setSegment_name(key.split("&&&")[1]);
				 tmp.setProposer("FOR_WORD");
				 tmp.setExpectMore(false);
				 
				 return tmp;
			}catch(Exception V) {
				continue;
			}
		}
		
		for(String dw:dw_words) {
			if(desc.toUpperCase().contains(dw.toUpperCase())){
				HashMap<String, Integer> map = classifiedDW.get(dw.toUpperCase());
				if(!(map!=null)) {
					continue;
				}
				List<String> ret = map.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .map(Map.Entry::getKey).collect(Collectors.toList());
				
				String key = ret.get(0);
				
				ManualClassProposition tmp = new ManualClassProposition();
				 tmp.setSegment_id(key.split("&&&")[0]);
				 tmp.setSegment_name(key.split("&&&")[1]);
				 tmp.setProposer("DW_WORD");
				 tmp.setExpectMore(false);
				 
				 return tmp;
			}
		}
		return null;
	}
	
	

	private ManualClassProposition MaterialGroup() {
		
		String mg = null;
		if(item.getMaterial_group()!=null) {
			mg = item.getMaterial_group();
		}else {
			return null;
		}
		
		HashMap<String, Integer> map = classifiedMG.get(mg.toUpperCase());
		if(!(map!=null)) {
			return null;
		}
		List<String> ret = map.entrySet().stream()
		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .map(Map.Entry::getKey).collect(Collectors.toList());
		
		String key = ret.get(0);
		
		ManualClassProposition tmp = new ManualClassProposition();
		 tmp.setSegment_id(key.split("&&&")[0]);
		 tmp.setSegment_name(key.split("&&&")[1]);
		 tmp.setProposer("Material_Group");
		 tmp.setExpectMore(false);
		 
		 return tmp;
	}

	private void addProposition(ManualClassProposition newProp) {
		for(ManualClassProposition prop:propositions) {
			if(prop.getSegment_id().equals(newProp.getSegment_id())) {
				return;
			}
		}
		propositions.add(newProp);
	}

	private void intializeRegister() {
		
		propositions = new ArrayList<ManualClassProposition>();
		MF_FW_results= null;
		
		register.put("PreClass",true);
		register.put("LastClass",true);
		register.put("MostFrequent_FirstWord",true);
		
	}

	private ManualClassProposition LastClass() {
		
		return lastClassProp;
	}

	private ManualClassProposition PreClass() {
		try {	
			String pcl = item.getPreclassifiation();
			//int numMatches = segments.values().stream().filter(a -> 
			//	a.contains(pcl)).collect(Collectors.toList()).size();
			Map<String, String> matches = segments.entrySet().stream().filter(
					m -> m.getValue().toUpperCase().split("&&&")[1].equals(pcl.toUpperCase())).collect(
				Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
			int numMatches = matches.size();
				if(numMatches==1) {
					ManualClassProposition tmp = new ManualClassProposition();
					tmp.setSegment_id((String) matches.keySet().toArray()[0]);
					try{
						tmp.setSegment_name(matches.get(tmp.getSegment_id()).split("&&&")[1]);
					}catch(Exception V) {
						tmp.setSegment_name(matches.get(tmp.getSegment_id()).split("&&&")[0]);
					}
					tmp.setProposer("PreClass");
					tmp.setExpectMore(false);
					return tmp;
				}
			}catch(Exception V) {
				return null;
			}
		return null;
	}
	
	private ManualClassProposition MostFrequent_FirstWord() {
		
		if(MF_FW_results!=null) {
			 
			 String key = MF_FW_results.poll();
			 ManualClassProposition tmp = new ManualClassProposition();
			 tmp.setSegment_id(key.split("&&&")[0]);
			 tmp.setSegment_name(key.split("&&&")[1]);
			 tmp.setProposer("MostFrequent_FirstWord");
			 tmp.setExpectMore(MF_FW_results.peek()!=null);
			 
			 return tmp;
		}else {
			String fw = null;
			if(item.getLong_description()!=null) {
				if(item.getShort_description()!=null) {
					fw = item.getLong_description().length()>item.getShort_description().length()?item.getLong_description().split(" ")[0]:item.getShort_description().split(" ")[0];

				}else {
					fw = item.getLong_description().split(" ")[0];
				}
				
			}else if(item.getShort_description()!=null) {
				fw = item.getShort_description();
			}else {
				return null;
			}
			
			HashMap<String, Integer> map = classifiedFW.get(fw.toUpperCase());
			if(!(map!=null)) {
				return null;
			}
			MF_FW_results = map.entrySet().stream()
			.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	        .map(Map.Entry::getKey).collect(Collectors.toCollection(PriorityQueue::new));
			
			return MostFrequent_FirstWord();
		}
		
	}
	
	

	public void setParent(Manual_classif parent_controller2) {
		this.parent_controller = parent_controller2;
	}

	public void addClassifiedFW(String key, String val) {
		
		key = key.toUpperCase();
		try {
			classifiedFW.get(key).put(val,classifiedFW.get(key).get(val) +1 );
			
		}catch(Exception V) {
			//No such val
			try {
				classifiedFW.get(key).put(val, 1 );
			}catch(Exception W) {
				//No such key
				HashMap<String, Integer> tmp = new HashMap<String,Integer>();
				tmp.put(val, 1);
				classifiedFW.put(key, tmp);
			}
		}
	}

	public void addClassifiedMG(String key, String val) {
		key = key.toUpperCase();
		try {
			classifiedMG.get(key).put(val,classifiedMG.get(key).get(val) +1 );
			
		}catch(Exception V) {
			//No such val
			try {
				classifiedMG.get(key).put(val, 1 );
			}catch(Exception W) {
				//No such key
				HashMap<String, Integer> tmp = new HashMap<String,Integer>();
				tmp.put(val, 1);
				classifiedMG.put(key, tmp);
			}
		}
	}

	public void addClassifiedFor(String key, String val) {
		key = key.toUpperCase();
		try {
			classifiedFor.get(key).put(val,classifiedFor.get(key).get(val) +1 );
			
		}catch(Exception V) {
			//No such val
			try {
				classifiedFor.get(key).put(val, 1 );
			}catch(Exception W) {
				//No such key
				HashMap<String, Integer> tmp = new HashMap<String,Integer>();
				tmp.put(val, 1);
				classifiedFor.put(key, tmp);
			}
		}
	}

	public void addClassifiedDW(String key, String val) {
		key = key.toUpperCase();
		try {
			classifiedDW.get(key).put(val,classifiedDW.get(key).get(val) +1 );
			
		}catch(Exception V) {
			//No such val
			try {
				classifiedDW.get(key).put(val, 1 );
			}catch(Exception W) {
				//No such key
				HashMap<String, Integer> tmp = new HashMap<String,Integer>();
				tmp.put(val, 1);
				classifiedDW.put(key, tmp);
			}
		}
		
	}

	public void proposeAgain() {
		try {
			loadPropositionsFor(item);
			parent_controller.context.closeLast();
			parent_controller.account.setManualPropositions(ManualClassifContext.methods);
		}catch(Exception V) {
			parent_controller.context.closeLast();
			parent_controller.account.setManualPropositions(ManualClassifContext.methods);
		}
		try {
			System.out.println("Saving propositions");
			transversal.data_exchange_toolbox.ComplexMap2JdbcObject.saveAccountProjectPreference(parent_controller.account);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
