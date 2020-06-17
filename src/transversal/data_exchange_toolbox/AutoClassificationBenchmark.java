package transversal.data_exchange_toolbox;

import controllers.Auto_classification_launch;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.BinaryClassificationParameters;
import model.DescriptionFetchRow;
import service.Bigram;
import service.ConfusionMatrixReader;
import service.CorpusReader;
import service.TimeMasterTemplate;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.SpellCorrector;
import transversal.language_toolbox.WordUtils;

import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class AutoClassificationBenchmark {
	
	private HashMap<String, Double> RET = new HashMap<String,Double>();
	private String pid;
	
	public void setUser(String user) {
	}

	public void setRole(String role) {
	}
	public void setPid(String pidx) {
		this.pid=pidx;
	}
	
	
	private HashSet<String> REFERENCE_PROJECTS = new HashSet<String>();
	private HashSet<String> preclass_REFERENCE_PROJECTS = new HashSet<String>();
	
	
	
	private Integer ref_desc_cardinality = 0;
	private Integer preclass_ref_desc_cardinality = 0;
	
	private Integer target_desc_cardinality = 0;
	
	private HashMap<String,String> PROJECT2DATAMAP = new HashMap<String,String>();
	private HashMap<String,String> PROJECT2LANGUAGE = new HashMap<String,String>();
	private HashMap<String,HashMap<String,String>> CLASSIFICATION_RULES = new HashMap<String,HashMap<String,String>>();
	private HashMap<String,HashMap<String,String>> preclass_CLASSIFICATION_RULES = new HashMap<String,HashMap<String,String>>();
	
	public BinaryClassificationParameters binaryClassificationParameters;
	private HashSet<Integer> buildRows;
	private HashSet<Integer> preclass_buildRows;
	
	private HashMap<String,ArrayList<String>> ITEMS_DICO = new HashMap<String,ArrayList<String>>();
	private HashMap<String,ArrayList<String>> preclass_ITEMS_DICO = new HashMap<String,ArrayList<String>>();
	
	private HashMap<String, ArrayList<DescriptionFetchRow>> DESCS = new HashMap<String,ArrayList<DescriptionFetchRow>>();
	private Bigram bigramCount;
	private HashMap<String, HashMap<String,String>> CLEAN_REFERENCES = new HashMap<String,HashMap<String,String>>();
	private HashMap<String, HashMap<String,String>> CLEAN_TARGETS = new HashMap<String,HashMap<String,String>>();
	
	private HashMap<String, HashMap<String,String>> preclass_CLEAN_REFERENCES = new HashMap<String,HashMap<String,String>>();
	private HashMap<String, HashMap<String,String>> preclass_CLEAN_TARGETS = new HashMap<String,HashMap<String,String>>();
	
	private boolean cardinal_counted=false;
	private CorpusReader cr;
	private ConfusionMatrixReader cmr;
	public Auto_classification_launch parent;
	public Task<Void> TargetTask;
	public Task<Void> RuleGenTask;
	public Task<Void> cleanTask;
	public Task<Void> mainTask;
	

	private void launch_preclassification(int MAX_CARD) {
		
		if(this.preclass_ref_desc_cardinality==0) {
			
			return;
		}

		this.TargetTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	preclass_clean_targets();
				return null;
		    }
		};
		TargetTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			
			try {
				preclass_apply_rules();
			} catch (ClassNotFoundException e1) {
				ExceptionDialog.show("CF001 pg_class", "CF001 pg_class", "CF001 pg_class");

			} catch (SQLException e1) {
				ExceptionDialog.show("PG000 db_error", "PG000 db_error", "PG000 db_error");

			}
			
			
		});

		TargetTask.setOnFailed(e -> {
		    TargetTask.getException();
		    

		});

		TargetTask.setOnCancelled(e -> {
		   ;
			
		});
		
		
		
		
		this.RuleGenTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	preclass_generate_rules();
				return null;
		    }

			
		};
		RuleGenTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			
			Thread TargetThread = new Thread(TargetTask);; TargetThread.setDaemon(true);
			TargetThread.start();
			
			
			
		});

		RuleGenTask.setOnFailed(e -> {
		    RuleGenTask.getException();
		    

		});

		RuleGenTask.setOnCancelled(e -> {
		   ;
			
		});
		
		
		
		
		this.cleanTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	
		    	Instant start = Instant.now();
		    	preclass_clean_references();
		    	Instant end = Instant.now();
				Duration timeElapsed = Duration.between(start, end);
				RET.put("preclass clean",(double) timeElapsed.toNanos());
		    	return null;
		    }
		};
		cleanTask.setOnSucceeded(e -> {
			;
			fill_time_master_template(RET);
		    /* code to execute when task completes normally */
			
			
			//Thread Rulethread = new Thread(RuleGenTask);; Rulethread.setDaemon(true);
			//Rulethread.start();
			
		});

		cleanTask.setOnFailed(e -> {
		    cleanTask.getException();
		    

		});

		cleanTask.setOnCancelled(e -> {
		   ;
			
		});
		
		Thread cleanThread = new Thread(cleanTask);; cleanThread.setDaemon(true);
		cleanThread.start();
	}
	

	private void fill_time_master_template(HashMap<String, Double> rET2) {
		// TODO Auto-generated method stub
		TimeMasterTemplate template = new TimeMasterTemplate();
		template.setFactors(rET2);
		template.fill(this.target_desc_cardinality,this.ref_desc_cardinality,this.preclass_ref_desc_cardinality);
		parent.ready2Launch(template,this,rET2);
	}

	private void launch_classification(int MAX_CARD) throws ClassNotFoundException, SQLException {
		
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
			}
			
			});
		
		
		
		this.mainTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	donwload_vocabulary(MAX_CARD);
				return null;
		    }
		};
		mainTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			main_tasks(MAX_CARD);
			
		});

		mainTask.setOnFailed(e -> {
		    mainTask.getException();
		    

		});

		mainTask.setOnCancelled(e -> {
		   ;
			
		});
		
		Thread mainThread = new Thread(mainTask);; mainThread.setDaemon(true);
		mainThread.start();
		
		
		
		
		
	}
	
	private void main_tasks(int MAX_CARD) {
		if(this.ref_desc_cardinality==0) {
			
			launch_preclassification(MAX_CARD);
			return;
		}
		
		this.TargetTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	clean_targets();
				return null;
		    }
		};
		TargetTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			
			
			
		});

		TargetTask.setOnFailed(e -> {
		    TargetTask.getException();
		    

		});

		TargetTask.setOnCancelled(e -> {
		   ;
			
		});
		
		
		
		
		this.RuleGenTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	generate_rules();
				return null;
		    }
		};
		RuleGenTask.setOnSucceeded(e -> {
		    /* code to execute when task completes normally */
			
			Thread TargetThread = new Thread(TargetTask);; TargetThread.setDaemon(true);
			TargetThread.start();
			
			
			
		});

		RuleGenTask.setOnFailed(e -> {
		    RuleGenTask.getException();
		    

		});

		RuleGenTask.setOnCancelled(e -> {
		   ;
			
		});
		
		
		
		
		this.cleanTask = new Task<Void>() {
		    @Override
		    protected Void call() throws Exception {
		    	//count_ref_descs();
		    	
		    	
		    	Instant start = Instant.now();
				clean_references();
				Instant end = Instant.now();
				Duration timeElapsed = Duration.between(start, end);
				RET.put("class clean",(double) timeElapsed.toNanos());
				return null;
		    }
		};
		cleanTask.setOnSucceeded(e -> {
			;
			fill_time_master_template(RET);
		});

		cleanTask.setOnFailed(e -> {
		    cleanTask.getException();
		    

		});

		cleanTask.setOnCancelled(e -> {
		   ;
			
		});
		
		Thread cleanThread = new Thread(cleanTask);; cleanThread.setDaemon(true);
		cleanThread.start();
	}

	private void donwload_vocabulary(int MAX_CARD) throws ClassNotFoundException, SQLException, IOException {
		;
		count_cardinals(MAX_CARD);
		count_ref_descs(MAX_CARD);
		preclass_count_ref_descs(MAX_CARD);
		this.bigramCount = Bigram.trainNGram(DESCS,this.binaryClassificationParameters);
		//boolean alphabetOnlyParameter, boolean decodeParameter
		
		
		cr = new CorpusReader(this.bigramCount);
        cmr = new ConfusionMatrixReader();
        	
	}

	protected void preclass_count_ref_descs(int mAX_CARD) throws ClassNotFoundException, SQLException {

		Instant start = Instant.now();
		for(String pidx:preclass_REFERENCE_PROJECTS) {
			FETCH_DESCRIPTIONS(pidx,true,mAX_CARD,true);
			
		}
		
		Instant end = Instant.now();
		Duration.between(start, end);
		//RET.put("preclass fetch",(double) timeElapsed.toNanos());
		if(this.ref_desc_cardinality==0) {
			FETCH_DESCRIPTIONS(this.pid,true,mAX_CARD,true);
			
		}
		
		
	}

	private void count_ref_descs(int mAX_CARD) throws ClassNotFoundException, SQLException {
		Instant start = Instant.now();
		for(String pidx:REFERENCE_PROJECTS) {
			FETCH_DESCRIPTIONS(pidx,true,mAX_CARD,false);
			
		}
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		//RET.put("class fetch",(double) timeElapsed.toNanos());
		
		start = Instant.now();
		
		SpeedMonitor sm = new SpeedMonitor();
		sm.start();
		FETCH_DESCRIPTIONS(this.pid,false,mAX_CARD,false);
		sm.stop();
		end = Instant.now();
		timeElapsed = Duration.between(start, end);
		RET.put("this fetch",(double) timeElapsed.toNanos());
		RET.put("this speed", sm.getAverage());
	}


	private void count_cardinals(int mAX_CARD) throws ClassNotFoundException, SQLException {
		if(cardinal_counted) {
			return;
		}		

		cardinal_counted=true;
		
		Connection conn = Tools.spawn_connection();
		Connection conn2 = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn2.createStatement();
		ResultSet rs = null;
		ResultSet rs2 = null;
		
		
		for(String pid:this.preclass_REFERENCE_PROJECTS) {
			//#
			rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
			rs.next();
			PROJECT2DATAMAP .put(pid,pid+".project_items");
			PROJECT2LANGUAGE.put(pid,rs.getString(1));
			//#
			rs2 = stmt2.executeQuery("select count( distinct item_id ) from "+pid+".project_items where item_id in (select distinct item_id from "+pid+".project_classification_event where segment_id is not null)");
			rs2.next();
			
			this.preclass_ref_desc_cardinality=this.preclass_ref_desc_cardinality+ rs2.getInt(1);
			
			
			rs2.close();
		}
		
		for(String pid:this.REFERENCE_PROJECTS) {
			
			//#
			rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
			rs.next();
			PROJECT2DATAMAP .put(pid,pid+".project_items");
			PROJECT2LANGUAGE.put(pid,rs.getString(1));
			//#
			rs2 = stmt2.executeQuery("select count( distinct item_id ) from "+pid+".project_items where item_id in (select distinct item_id from "+pid+".project_classification_event where segment_id is not null)");
			rs2.next();
			
			this.ref_desc_cardinality=this.ref_desc_cardinality+ rs2.getInt(1);
			
			
			rs2.close();
		}
		
		//#
		rs = stmt.executeQuery("select data_language from administration.projects where project_id='"+pid+"'");
		rs.next();
		PROJECT2DATAMAP .put(pid,pid+".project_items");
		PROJECT2LANGUAGE.put(pid,rs.getString(1));
		
		rs.close();
		
		stmt.close();
		stmt2.close();
		
		conn.close();
		conn2.close();
		
		this.preclass_ref_desc_cardinality = Math.min(this.preclass_ref_desc_cardinality, mAX_CARD);
		this.ref_desc_cardinality = Math.min(this.ref_desc_cardinality, mAX_CARD);
		this.target_desc_cardinality = Math.min(this.target_desc_cardinality, mAX_CARD);
		
		
	}

	private void FETCH_DESCRIPTIONS(String pid, boolean isREFERENCE, int mAX_CARD,boolean IS_PRECLASSIF) throws ClassNotFoundException, SQLException {
		
		
		
		if(DESCS.containsKey(pid)) {
			
			
		}
		

		Platform.runLater(new Runnable(){

			@Override
			public void run() {
			}
			
			});
		
		
		
		
		Connection conn = Tools.spawn_connection();
		String query=null;
		if(isREFERENCE) {
			query="select client_item_number,short_description,long_description,material_group,level_1_number,level_1_name_translated,level_2_number,level_2_name_translated,level_3_number,level_3_name_translated,level_4_number,level_4_name_translated from (select item_id , level_1_number,level_1_name_translated,level_2_number,level_2_name_translated,level_3_number,level_3_name_translated,level_4_number,level_4_name_translated from  ( select item_id, segment_id from ( select item_id, segment_id, local_rn, max(local_rn) over (partition by  item_id) as max_rn from ( select item_id, segment_id, row_number() over (partition by item_id order by global_rn asc ) as local_rn from  ( SELECT  item_id,segment_id, row_number() over ( order by (select 0) ) as global_rn  FROM "+pid+".project_classification_event where item_id in (select distinct item_id from "+pid+".project_items ) ) as global_rank ) as ranked_events ) as maxed_events where local_rn = max_rn ) as latest_events left join  "+pid+".project_segments on project_segments.segment_id = latest_events.segment_id ) as rich_events left join "+pid+".project_items on rich_events.item_id = project_items.item_id"+" limit "+String.valueOf(mAX_CARD);
		}else {
			query="SELECT client_item_number,short_description,long_description,material_group FROM "+PROJECT2DATAMAP.get(pid)+" limit "+String.valueOf(mAX_CARD);
		}
		
		PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		
		//#
		ResultSet rs = stmt.executeQuery();
		
		int rowcount = 0;
		if (rs.last()) {
			  rowcount = rs.getRow();
			  rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
			}
		ArrayList<DescriptionFetchRow> RESULT = new ArrayList<DescriptionFetchRow>(rowcount);
		
		while(rs.next()) {
			DescriptionFetchRow row = new DescriptionFetchRow();
			row.setAid(rs.getString(1));
			row.setSd(rs.getString(2));
			row.setLd(rs.getString(3));
			try {
				row.setCid(rs.getString("level_"+(IS_PRECLASSIF?this.binaryClassificationParameters.getPreclassif_granularity().toString():this.binaryClassificationParameters.getClassif_granularity().toString())+"_number"));
			}catch(Exception V) {

			}
			RESULT.add(row);
			
		}
		
		DESCS.put(pid, RESULT);
		
		
		
		
		rs.close();
		stmt.close();
		conn.close();
		
		
	}
	
	protected void preclass_clean_targets() throws IOException, ClassNotFoundException, SQLException {
		if(this.binaryClassificationParameters.getPreclassif_targetDescriptionType().toString().startsWith("raw") || this.preclass_ref_desc_cardinality==0) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		
        
		HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
		
		ArrayList<DescriptionFetchRow>  rs = DESCS.get(pid);
		for(DescriptionFetchRow row:rs) {
			
			//
			double char_weight = 1.0;
			double spell_weight = 1.0;
			double abv_weight = 1.0;
				
			double increment_step=
					(this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0) +
					(this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0) + 
					(this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0); 
			increment_step=1.0/increment_step;
			

			//Correction
			ArrayList<String> input = new ArrayList<String>();
			input.add(row.getSd());
			input.add(row.getLd());
			HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,false);
			input =null;
			preclass_CLEAN_TARGETS.put(row.getAid(),output);
			
			
		}
	}

	private void clean_targets() throws ClassNotFoundException, SQLException, IOException {
		
		if(this.binaryClassificationParameters.getClassif_targetDescriptionType().toString().startsWith("raw") || this.ref_desc_cardinality==0) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		
        
		HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
		
		ArrayList<DescriptionFetchRow>  rs = DESCS.get(pid);
		for(DescriptionFetchRow row:rs) {
			
			//
			double char_weight = 1.0;
			double spell_weight = 1.0;
			double abv_weight = 1.0;
				
			double increment_step=
					(this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0) +
					(this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0) + 
					(this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0); 
			increment_step=1.0/increment_step;
			
			//Correction
			ArrayList<String> input = new ArrayList<String>();
			input.add(row.getSd());
			input.add(row.getLd());
			HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,true);
			input =null;
			CLEAN_TARGETS.put(row.getAid(),output);
			
			
		}
	}
	
	private void preclass_generate_rules() {
		
		
		this.preclass_buildRows = Tools.RandomSample(preclass_ref_desc_cardinality,this.binaryClassificationParameters.getPreclassif_buildSampleSize());
		
		int base_desc_count = 0;
		
		String target_desc = this.binaryClassificationParameters.getPreclassif_baseDescriptionType().toString();
		for(String aid:preclass_CLEAN_REFERENCES.keySet()) {
				if( preclass_CLEAN_REFERENCES.get(aid).get("cid").length() <4 || !preclass_buildRows.contains(base_desc_count)) {
					base_desc_count=base_desc_count+1;
					continue;
				}
				base_desc_count=base_desc_count+1;
				String description = preclass_CLEAN_REFERENCES.get(aid).get(target_desc);
				String[] desc = description.split(" ");
				String cid = preclass_CLEAN_REFERENCES.get(aid).get("cid");
				String rule;
				try{
					rule = "MAIN="+desc[0];
				}catch(Exception G) {

					continue;
				}
				preclass_learn_rule(rule,cid);
				try{
					for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
						if(wn.length()<this.binaryClassificationParameters.getPreclassif_minimumTermLength()) {
							continue;
						}
						
						rule = "MAIN="+desc[0]+"|COMP="+wn;
						preclass_learn_rule(rule,cid);
					}
				}catch(Exception V) {

				}
				
				
			}
		HashMap<String,HashMap<String,String>> TMP = new HashMap<String,HashMap<String,String>>();
		
		for(String rule:preclass_CLASSIFICATION_RULES.keySet()) {
			HashMap<String,String> tmp = new HashMap<String,String>();
			int max = 0;
			int total = 0;
			String MF = null;
			for(String cid:preclass_CLASSIFICATION_RULES.get(rule).keySet()) {
				
				total = total + Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid));
				
				if(Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid))>max) {
					max = Integer.valueOf(preclass_CLASSIFICATION_RULES.get(rule).get(cid));
					MF = cid;
				}
				
			}
			if(rule.contains("|COMP=")) {
				tmp.put("Type",String.valueOf(this.binaryClassificationParameters.getPreclassif_typeFactor()));
			}else {
				tmp.put("Type","0");
			}
			tmp.put("Total", String.valueOf(total));
			tmp.put("MF",MF);
			tmp.put("Accuracy", String.valueOf((1.0*max)/total));
			
			TMP.put(rule, tmp);
		}
		preclass_CLASSIFICATION_RULES = null;
		preclass_CLASSIFICATION_RULES = TMP;
		TMP = null;
		
	}

	private void generate_rules() throws ClassNotFoundException, SQLException {
		
		this.buildRows = Tools.RandomSample(ref_desc_cardinality,this.binaryClassificationParameters.getClassif_buildSampleSize());
		
		int base_desc_count = 0;
		
		String target_desc = this.binaryClassificationParameters.getClassif_baseDescriptionType().toString();
		for(String aid:CLEAN_REFERENCES.keySet()) {
			try {
					if( CLEAN_REFERENCES.get(aid).get("cid").length() <4 || !buildRows.contains(base_desc_count)) {
						base_desc_count=base_desc_count+1;
						continue;
					}
					
				}catch(Exception V) {

					base_desc_count=base_desc_count+1;
					continue;
				}
				
				base_desc_count=base_desc_count+1;
				String description = CLEAN_REFERENCES.get(aid).get(target_desc);
				String[] desc = description.split(" ");
				String cid = CLEAN_REFERENCES.get(aid).get("cid");
				
				String rule;
				try{
					rule = "MAIN="+desc[0];
				}catch(Exception G) {

					
					continue;
				}
				learn_rule(rule,cid);
				try{
					for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
						if(wn.length()<this.binaryClassificationParameters.getClassif_minimumTermLength()) {
							continue;
						}
						
						rule = "MAIN="+desc[0]+"|COMP="+wn;
						learn_rule(rule,cid);
					}
				}catch(Exception V) {

				}
					
				
			}
		HashMap<String,HashMap<String,String>> TMP = new HashMap<String,HashMap<String,String>>();
		
		for(String rule:CLASSIFICATION_RULES.keySet()) {
			HashMap<String,String> tmp = new HashMap<String,String>();
			int max = 0;
			int total = 0;
			String MF = null;
			for(String cid:CLASSIFICATION_RULES.get(rule).keySet()) {
				
				total = total + Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid));
				
				if(Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid))>max) {
					max = Integer.valueOf(CLASSIFICATION_RULES.get(rule).get(cid));
					MF = cid;
				}
				
			}
			if(rule.contains("|COMP=")) {
				tmp.put("Type",String.valueOf(this.binaryClassificationParameters.getClassif_typeFactor()));
			}else {
				tmp.put("Type","0");
			}
			tmp.put("Total", String.valueOf(total));
			tmp.put("MF",MF);
			tmp.put("Accuracy", String.valueOf((1.0*max)/total));
			
			TMP.put(rule, tmp);
			
		}
		CLASSIFICATION_RULES = null;
		CLASSIFICATION_RULES = TMP;
		TMP = null;
		
		
		
	}
	
	private void preclass_learn_rule(String rule, String cid) {
		if(preclass_CLASSIFICATION_RULES.containsKey(rule)) {
			if(preclass_CLASSIFICATION_RULES.get(rule).containsKey(cid)) {
				String count = String.valueOf(Integer.parseInt(preclass_CLASSIFICATION_RULES.get(rule).get(cid)) + 1);
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp=preclass_CLASSIFICATION_RULES.get(rule);
				tmp.put(cid, count);
				preclass_CLASSIFICATION_RULES.put(rule, tmp);
			}else {
				String count = "1";
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp=preclass_CLASSIFICATION_RULES.get(rule);
				tmp.put(cid, count);
				preclass_CLASSIFICATION_RULES.put(rule, tmp);
			}
		}else {
			String count = "1";
			HashMap<String,String> tmp = new HashMap<String,String>();
			tmp.put(cid, count);
			preclass_CLASSIFICATION_RULES.put(rule, tmp);
		}
	}

	private void learn_rule(String rule, String cid) {
		if(CLASSIFICATION_RULES.containsKey(rule)) {
			if(CLASSIFICATION_RULES.get(rule).containsKey(cid)) {
				String count = String.valueOf(Integer.parseInt(CLASSIFICATION_RULES.get(rule).get(cid)) + 1);
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp=CLASSIFICATION_RULES.get(rule);
				tmp.put(cid, count);
				CLASSIFICATION_RULES.put(rule, tmp);
			}else {
				String count = "1";
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp=CLASSIFICATION_RULES.get(rule);
				tmp.put(cid, count);
				CLASSIFICATION_RULES.put(rule, tmp);
			}
		}else {
			String count = "1";
			HashMap<String,String> tmp = new HashMap<String,String>();
			tmp.put(cid, count);
			CLASSIFICATION_RULES.put(rule, tmp);
		}
	}
	
	protected void preclass_clean_references() throws IOException, ClassNotFoundException, SQLException {
		if(this.binaryClassificationParameters.getPreclassif_baseDescriptionType().toString().startsWith("raw")) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
        
		
		for(String pid:preclass_REFERENCE_PROJECTS) {
			
			HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
			
			ArrayList<DescriptionFetchRow> rs = DESCS.get(pid);
			for(DescriptionFetchRow row:rs) {
				
				//
				double char_weight = 1.0;
				double spell_weight = 1.0;
				double abv_weight = 1.0;
					
				double increment_step=
						(this.binaryClassificationParameters.getPreclassif_cleanChar()?char_weight:0) +
						(this.binaryClassificationParameters.getPreclassif_cleanAbv()?abv_weight:0) + 
						(this.binaryClassificationParameters.getPreclassif_cleanSpell()?spell_weight:0); 
				increment_step=1.0/increment_step;
				
				//Correction
				ArrayList<String> input = new ArrayList<String>();
				input.add(row.getSd());
				input.add(row.getLd());
				HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,false);
				output.put("cid", row.getCid());
				input =null;
				preclass_CLEAN_REFERENCES.put(row.getAid(),output);
				
				
				
			}
		}
	}

	private void clean_references() throws ClassNotFoundException, SQLException, IOException {
		
		
		if(this.binaryClassificationParameters.getClassif_baseDescriptionType().toString().startsWith("raw")) {
			return;
		}
		
		SpellCorrector sc = new SpellCorrector(cr, cmr);
		
        
		for(String pid:REFERENCE_PROJECTS) {
			
			HashMap<String,String> CORRECTIONS  = WordUtils.getCorrections(PROJECT2LANGUAGE.get(pid).replace(".", ""));
			
			ArrayList<DescriptionFetchRow> rs = DESCS.get(pid);
			for(DescriptionFetchRow row:rs) {
				
				//
				double char_weight = 1.0;
				double spell_weight = 1.0;
				double abv_weight = 1.0;
					
				double increment_step=
						(this.binaryClassificationParameters.getClassif_cleanChar()?char_weight:0) +
						(this.binaryClassificationParameters.getClassif_cleanAbv()?abv_weight:0) + 
						(this.binaryClassificationParameters.getClassif_cleanSpell()?spell_weight:0); 
				increment_step=1.0/increment_step;
				
				
				//Correction
				ArrayList<String> input = new ArrayList<String>();
				input.add(row.getSd());
				input.add(row.getLd());
				HashMap<String,String> output = WordUtils.CLEAN_DESC(input,CORRECTIONS,this.binaryClassificationParameters,sc,true);
				output.put("cid", row.getCid());
				input =null;
				CLEAN_REFERENCES.put(row.getAid(),output);
				
				
				
			}
		}
	}

	public void setRefenceProjectsSIMULATION(HashSet<String> rEFERENCE_PROJECTS, HashSet<String> rEFERENCE_PROJECTS2) {
		this.REFERENCE_PROJECTS = rEFERENCE_PROJECTS;
		this.preclass_REFERENCE_PROJECTS = rEFERENCE_PROJECTS2;
		
		
		
		
		try {
			launch_classification(5000);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block

		}
		;
		
		
	}

	public void setTarget_desc_cardinality(Integer Target_desc_cardinality) {
		this.target_desc_cardinality = Target_desc_cardinality;
	}

	public void setConfig(BinaryClassificationParameters binaryClassificationParameters) {
		
		this.binaryClassificationParameters = binaryClassificationParameters;
	}
	

	private void preclass_apply_rules() throws ClassNotFoundException, SQLException {
		String target_desc = this.binaryClassificationParameters.getPreclassif_targetDescriptionType().toString();

		for(String aid:preclass_CLEAN_TARGETS.keySet()) {
			String description = preclass_CLEAN_TARGETS.get(aid).get(target_desc);
			String[] desc = description.split(" ");
			String rule;
			try{
				rule = "MAIN="+desc[0];
			}catch(Exception G) {

				
				continue;
			}
			
			preclass_apply_rule(aid,rule);
			try{
				for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
					rule = "MAIN="+desc[0]+"|COMP="+wn;
					preclass_apply_rule(aid,rule);
				}
			}catch(Exception V) {

			}
			
		}
		
		
	}
	
	
	public void apply_rules() throws ClassNotFoundException, SQLException {
		String target_desc = this.binaryClassificationParameters.getClassif_targetDescriptionType().toString();

		for(String aid:CLEAN_TARGETS.keySet()) {
			String description = CLEAN_TARGETS.get(aid).get(target_desc);
			String[] desc = description.split(" ");
			String rule;
			try{
				rule = "MAIN="+desc[0];
			}catch(Exception G) {

				continue;
			}
			
			apply_rule(aid,rule);
			try{
				for(String wn:Arrays.copyOfRange(desc, 1,desc.length)){
					rule = "MAIN="+desc[0]+"|COMP="+wn;
					apply_rule(aid,rule);
				}
			}catch(Exception V) {

			}
			
			
		}
		
		
	}
	
	private void preclass_apply_rule(String aid, String rule) {
		if(!preclass_CLASSIFICATION_RULES.containsKey(rule)) {
			
			return;
		}
		Double score = Tools.score_rule(preclass_CLASSIFICATION_RULES.get(rule),this.binaryClassificationParameters,false);
		if(score==null) {
			return;
		}
		if(preclass_ITEMS_DICO.containsKey(aid)) {
			if(score>Double.parseDouble(preclass_ITEMS_DICO.get(aid).get(0))) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(preclass_CLASSIFICATION_RULES.get(rule).get("MF"));
				tmp.add(String.valueOf(score));
				preclass_ITEMS_DICO.put(aid, tmp);
			}
		}else {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(preclass_CLASSIFICATION_RULES.get(rule).get("MF"));
			tmp.add(String.valueOf(score));
			preclass_ITEMS_DICO.put(aid, tmp);
			
			
		}
		
	}

	private void apply_rule(String aid, String rule) {
		if(!CLASSIFICATION_RULES.containsKey(rule)) {
			return;
		}
		Double score = Tools.score_rule(CLASSIFICATION_RULES.get(rule),this.binaryClassificationParameters,true);
		if(score==null) {
			return;
		}
		if(ITEMS_DICO.containsKey(aid)) {
			if(score>Double.parseDouble(ITEMS_DICO.get(aid).get(0))) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(CLASSIFICATION_RULES.get(rule).get("MF"));
				tmp.add(String.valueOf(score));
				ITEMS_DICO.put(aid, tmp);
			}
		}else {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(CLASSIFICATION_RULES.get(rule).get("MF"));
			tmp.add(String.valueOf(score));
			ITEMS_DICO.put(aid, tmp);
		}
	}
	
}
