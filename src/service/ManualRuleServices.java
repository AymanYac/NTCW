package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controllers.Manual_classif;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;
import model.DataInputMethods;
import model.GenericClassRule;
import model.ItemFetcherRow;
import model.RulePaneRow;
import model.UserAccount;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class ManualRuleServices {

	static long tickTime;
	public static double i;
	private static HashMap<String, String> UUID2CID;

	public static void benchmark(GenericClassRule gr, Manual_classif manualClassifController) {
		
		TimeMaster.tick();
		int i=0;
		int k=0;
		
		Pattern p = null;
		if(gr.getComp()!=null) {
			p = Pattern.compile(".*[^\\w]+"+gr.getComp()+"[^\\w].*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			k+=1;
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				i+=1;
			}
			
		}
		TimeMaster.tock("Scanning "+String.valueOf(k)+" items for rule of type '"+gr.getType()+"', found "+String.valueOf(i)+" items matching after");
	}
	
	@SuppressWarnings("unchecked")
	public static void reEvaluateAllActiveRules(boolean reevaluateClassifiedItems, Manual_classif manualClassifController, MenuItem menu) {
		
		String originalMenuText = menu.getText();
		List<ItemFetcherRow> databaseSyncLists = new ArrayList<ItemFetcherRow>();
		
		ArrayList<GenericClassRule> grs = new ArrayList<GenericClassRule>();
		ArrayList<ArrayList<String[]>> itemRuleMaps = new ArrayList<ArrayList<String[]>>();
		ArrayList<Boolean> activeStatuses = new ArrayList<Boolean>();
		ArrayList<String> METHODS = new ArrayList<String>();
		
		
		Task<Void> task = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {

				
				i = 0.0;
				double ruleNumber = ItemFetcherRow.staticRules.values().parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()).size();
				
				StreamRulesOnItemFetcherRows(ruleNumber, ItemFetcherRow.staticRules.values().parallelStream().filter(gr->gr.active&&gr.classif.get(0)!=null).collect(Collectors.toList()),
						(List<ItemFetcherRow>) manualClassifController.tableController.tableGrid.getItems().parallelStream()
						.filter(row -> reevaluateClassifiedItems || !(((ItemFetcherRow) row).getDisplay_segment_id()!=null) ).collect(Collectors.toList())
						,menu,
						originalMenuText+"(XX%)"
						,manualClassifController.account
						,databaseSyncLists
						,grs
						,itemRuleMaps
						,activeStatuses
						,METHODS);
						
				
				
				return null;
			}
			};
		task.setOnSucceeded(e -> {
			;
			
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					menu.setText(originalMenuText);
					manualClassifController.tableController.tableGrid.refresh();
				}
				
			});
			Tools.StoreRules(manualClassifController.account, grs, itemRuleMaps, activeStatuses, METHODS);
			Tools.ItemFetcherRow2ClassEvent(databaseSyncLists,manualClassifController.account,DataInputMethods.USER_CLASSIFICATION_RULE);
			
			});

		task.setOnFailed(e -> {
		    Throwable problem = task.getException();
		    /* code to execute if task throws exception */
		    problem.printStackTrace(System.err);
		    
		    
		});

		task.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
			
			Thread thread = new Thread(task);; thread.setDaemon(true);
			thread.setName("Refreshing rules");
			thread.start();
		
		
		
	}
	
	
	public static void StreamRulesOnItemFetcherRows(double ruleNumber, List<GenericClassRule> ruleStream, List<ItemFetcherRow> itemStream, Object progressUIElement, String progressSyntax, UserAccount account, List<ItemFetcherRow> databaseSyncLists, ArrayList<GenericClassRule> grs, ArrayList<ArrayList<String[]>> itemRuleMaps, ArrayList<Boolean> activeStatuses, ArrayList<String> METHODS) {
		ruleStream.stream().forEach(gr -> {
			i+=1;
			if(i%100==0) {
				Tools.setRuleStreamProgress(Math.floor(100 * i/ruleNumber),progressUIElement,progressSyntax);
			}
			try {
				Pattern p_tmp = null;
				HashMap<ItemFetcherRow, GenericClassRule> itemsToUpdate= new HashMap<ItemFetcherRow,GenericClassRule>();
				ArrayList<ItemFetcherRow> itemsToBlank= new ArrayList<ItemFetcherRow>();
				ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
				
				if(gr.getComp()!=null) {
					p_tmp = Pattern.compile(".*[^\\w]+"+gr.getComp()+"[^\\w].*");
				}
				final Pattern p = p_tmp;
				gr.matched=false;
				
				itemStream.parallelStream().forEach(row->{
					if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
						String[] itemRule = new String [] {null,null};
						itemRule[0]= ((ItemFetcherRow) row).getItem_id();
						itemRule[1]= gr.toString();
						itemRuleMap.add(itemRule);
						
						GenericClassRule finalRule = EvaluateItemRules((ItemFetcherRow) row);
						if(finalRule!=null) {
							itemsToUpdate.put(((ItemFetcherRow) row), finalRule);
						}else {
							itemsToBlank.add(((ItemFetcherRow) row));
						}
						
					}
				});
				
				gr.matched=true;
				
				List<ItemFetcherRow> databaseSyncList = fireRuleClassChange(itemsToUpdate,account);
				databaseSyncList.addAll( fireRuleClassBlank(itemsToBlank) );
				
				databaseSyncLists.addAll(databaseSyncList);
				grs.add(gr);
				itemRuleMaps.add(itemRuleMap);
				activeStatuses.add(true);
				METHODS.add(DataInputMethods.USER_CLASSIFICATION_RULE);	
			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
			
		});
	}

	private static boolean tryRuleOnItem(GenericClassRule gr, ItemFetcherRow row, Pattern compiledCompPattern) {
		if(gr.matched) {
			return row.itemRules.contains(gr.toString());
		}
		try {
			
			if(gr.getMain()!=null) {
				String sd = " "+((ItemFetcherRow) row).getLong_description()+" ";
				String ld = " "+((ItemFetcherRow) row).getShort_description()+" ";
				List<String> sd_array = Arrays.asList(sd.split("[^\\w]+"));
				List<String> ld_array = Arrays.asList(ld.split("[^\\w]+"));
				List<String> main_array = Arrays.asList(gr.getMain().split("[^\\w]+"));
				
				if(WordUtils.startsWithIgnoreCase(main_array, sd_array) || WordUtils.startsWithIgnoreCase(main_array, ld_array)){
					
				}else {
					return false;
				}
			}
			

			if(compiledCompPattern!=null) {
				Matcher matcher = compiledCompPattern.matcher(" "+((ItemFetcherRow) row).getLong_description()+" ".toUpperCase());
				if(matcher.matches()) {
					
				}else {
					matcher = compiledCompPattern.matcher(" "+((ItemFetcherRow) row).getShort_description()+" ".toUpperCase());
					if(matcher.matches()) {
						
					}else {
						return false;
					}
				}
			}
			
			if(gr.getApp()!=null) {
				if( ((ItemFetcherRow) row).getF1().contains(gr.getApp()) ||  ((ItemFetcherRow) row).getF1F2().contains(gr.getApp()) ) {
					
				}else {
					
					return false;
				}
			}
			if(gr.getMg()!=null && ! ((ItemFetcherRow) row).getMaterial_group().toUpperCase().equals(gr.getMg().toUpperCase())) {
				return false;
			}
			if(gr.getPc()!=null && ! ((ItemFetcherRow) row).getPreclassifiation().toUpperCase().equals(gr.getPc().toUpperCase())) {
				return false;
			}
			if(gr.getDwg() && ! ((ItemFetcherRow) row).getDWG()  ) {
				return false;
			}
			
			row.itemRules.add(gr.toString());
			
			
			return true;
		}catch(Exception V) {
			return false;
		}
	}

	public static boolean assignClass2GR(GenericClassRule gr, ItemFetcherRow selectedItem) {
		if(selectedItem.getDisplay_segment_id()!=null && selectedItem.getSource_Display().equals("MANUAL")) {
			gr.classif.set(0, selectedItem.getDisplay_segment_id());
			gr.classif.set(1, selectedItem.getDisplay_segment_number());
			gr.classif.set(2, selectedItem.getDisplay_segment_name());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static void unapplyRule(GenericClassRule gr, Manual_classif manualClassifController) {
		
		try {
			if(! ItemFetcherRow.staticRules.get(gr.toString()).active) {
				//The rule is already inactive return
				return;
			}
		}catch(Exception V) {
			//The rule is unknown leave inactive
			return;
		}
		
		gr.active=false;
		ItemFetcherRow.staticRules.put(gr.toString(), gr);
		
		Pattern tmp_p = null;
		HashMap<ItemFetcherRow,GenericClassRule> itemsToUpdate= new HashMap<ItemFetcherRow,GenericClassRule>();
		ArrayList<ItemFetcherRow> itemsToBlank= new ArrayList<ItemFetcherRow>();
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		if(gr.getComp()!=null) {
			tmp_p = Pattern.compile(".*[^\\w]+"+gr.getComp()+"[^\\w].*");
		}
		
		final Pattern p = tmp_p;
		manualClassifController.tableController.tableGrid.getItems().parallelStream().forEach(row->{
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				GenericClassRule finalRule = EvaluateItemRules((ItemFetcherRow) row);
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				if(finalRule!=null) {
					itemsToUpdate.put(((ItemFetcherRow) row), finalRule);
				}else {
					itemsToBlank.add(((ItemFetcherRow) row));
				}
			}
		});
		
		/*for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				GenericRule finalRule = EvaluateItemRules((ItemFetcherRow) row);
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				if(finalRule!=null) {
					itemsToUpdate.put(((ItemFetcherRow) row).getItem_id(), finalRule);
				}else {
					itemsToBlank.add(((ItemFetcherRow) row).getItem_id());
				}
			}
			
		}*/
		gr.matched=true;
		
		List<ItemFetcherRow> databaseSyncList = fireRuleClassChange(itemsToUpdate,manualClassifController.account);
		databaseSyncList.addAll( fireRuleClassBlank(itemsToBlank) );
		Tools.ItemFetcherRow2ClassEvent(databaseSyncList,manualClassifController.account,DataInputMethods.USER_CLASSIFICATION_RULE);
		
		Tools.StoreRule(manualClassifController.account,gr,itemRuleMap,false,DataInputMethods.USER_CLASSIFICATION_RULE);
	}

	public static void evaluateNewRule(GenericClassRule gr,Manual_classif manualClassifController) {
		
		gr.active=false;
		ItemFetcherRow.staticRules.put(gr.toString(), gr);
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		/*
		Pattern p = null;
		
		
		if(gr.getComp()!=null) {
			p = Pattern.compile(".*[^\\w]+"+gr.getComp()+"[^\\w].*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				
			}
			
		}*/
		Tools.StoreRule(manualClassifController.account, gr,itemRuleMap,false,DataInputMethods.USER_CLASSIFICATION_RULE);
	}
	@SuppressWarnings("unchecked")
	public static void applyRule(GenericClassRule gr, Manual_classif manualClassifController) {
		
		/*
		try {
			if(ItemFetcherRow.staticRules.get(gr.toString()).active) {
				//The rule is already active return
				return;
			}
		}catch(Exception V) {
			//The rule is unknown,proceed
		}
		*/
		
		gr.active=true;
		
		ItemFetcherRow.staticRules.put(gr.toString(), gr);
		
		Pattern p_tmp = null;
		
		HashMap<ItemFetcherRow,GenericClassRule> itemsToUpdate= new HashMap<ItemFetcherRow,GenericClassRule>();
		ArrayList<ItemFetcherRow> itemsToBlank= new ArrayList<ItemFetcherRow>();
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		if(gr.getComp()!=null) {
			p_tmp = Pattern.compile(".*[^\\w]+"+gr.getComp()+"[^\\w].*");
		}
		final Pattern p = p_tmp;
		
		manualClassifController.tableController.tableGrid.getItems().parallelStream().forEach(row->{
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				GenericClassRule finalRule = EvaluateItemRules((ItemFetcherRow) row);
				if(finalRule!=null) {
					itemsToUpdate.put(((ItemFetcherRow) row), finalRule);
				}else {
					itemsToBlank.add(((ItemFetcherRow) row));
				}
				
			}
		});
		/*for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				GenericRule finalRule = EvaluateItemRules((ItemFetcherRow) row);
				if(finalRule!=null) {
					itemsToUpdate.put(((ItemFetcherRow) row).getItem_id(), finalRule);
				}else {
					itemsToBlank.add(((ItemFetcherRow) row).getItem_id());
				}
				
			}
			
		}*/
		gr.matched=true;
		
		List<ItemFetcherRow> databaseSyncList = fireRuleClassChange(itemsToUpdate,manualClassifController.account);
		databaseSyncList.addAll( fireRuleClassBlank(itemsToBlank) );
		Tools.ItemFetcherRow2ClassEvent(databaseSyncList,manualClassifController.account,DataInputMethods.USER_CLASSIFICATION_RULE);
		Tools.StoreRule(manualClassifController.account,gr,itemRuleMap,true,DataInputMethods.USER_CLASSIFICATION_RULE);
		
	}

	

	private static GenericClassRule EvaluateItemRules(ItemFetcherRow row) {
		
		int max_score = Short.MIN_VALUE;
		HashSet<GenericClassRule> bestRules = new HashSet<GenericClassRule>();
		
		for( String loopRuleDesc : row.itemRules) {
			
			GenericClassRule loopRule = ItemFetcherRow.staticRules.get(loopRuleDesc);
			
			Boolean loopRuleActive = loopRule.active;
			
			if(loopRuleActive && loopRule.classif.get(0)!=null) {
				int loopRuleScore = scoreManualRule(loopRule);
				if(loopRuleScore<max_score) {
					continue;
				}else if(loopRuleScore==max_score) {
					bestRules.add(loopRule);
				}else {
					bestRules.clear();
					bestRules.add(loopRule);
					max_score = loopRuleScore;
				}
			}
		}
		GenericClassRule finalRule = manageRuleDisambiguation(bestRules);
		return finalRule;
	}

	public static GenericClassRule manageRuleDisambiguation(HashSet<GenericClassRule> bestRules) {
		//Check for conflict
		HashSet<GenericClassRule> nonSubRules = new HashSet<GenericClassRule>(bestRules.stream().filter(r->!r.isSubRule(bestRules)).collect(Collectors.toList()));
		Set<String> bestClassNumbers = nonSubRules.stream().map(e->e.classif.get(1)).collect(Collectors.toSet());
		if(bestClassNumbers.size()>1) {
			//Conflict, do nothing
			return null;
		}else if(bestClassNumbers.size()==1) {
			//No conflict
			Optional<GenericClassRule> bestRule = nonSubRules.stream().findAny();
			if(bestRule.isPresent()) {
				return bestRule.get();
			}
			return null;
		}else {
			//Possible path: item has no active rules
			return null;
		}
	}

	public static int scoreManualRule(GenericClassRule loopRule) {
		int score = 0;
		score += scoreManualRuleByType(loopRule);
		return score;
	}

	private static int scoreManualRuleByType(GenericClassRule loopRule) {
		return loopRule.getTypeScore();
	}

	public static Boolean getRuleAutoSelect(GenericClassRule nouvelle_regle, boolean manualAdd, boolean previousAdd) {
		
		if(previousAdd) {
			if(nouvelle_regle.active) {
				return (Boolean) null;
			}
			return nouvelle_regle.active;
		}
		//Boolean itemHasClass = assignClass2GR(nouvelle_regle,current_row);
		Boolean autoSelectByType = getRuleAutoSelectByType(nouvelle_regle);
		Boolean autoSelectByScore = getRuleAutoSelectByScore(nouvelle_regle);
		
		return manualAdd || (autoSelectByType || autoSelectByScore);
	}

	private static Boolean getRuleAutoSelectByScore(GenericClassRule nouvelle_regle) {
		// TODO Auto-generated method stub
		return false;
	}

	private static Boolean getRuleAutoSelectByType(GenericClassRule nouvelle_regle) {
		// TODO Auto-generated method stub
		return nouvelle_regle.getTypeScore()>=1100;
	}

	public static void scrollEvaluateRule(ItemFetcherRow current_row, GenericClassRule rule,Manual_classif manual_classif_controller,boolean isSelected) {
		if(!assignClass2GR(rule,current_row)) {
			return;
		}
		if(isSelected) {
			
			applyRule(rule, manual_classif_controller);
		}else {
			
			unapplyRule(rule,manual_classif_controller);
		}
	}

	public static void scrollEvaluateItem(ItemFetcherRow current_row, Manual_classif parent) {

		
		
		Task<Void> task = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				
				for(RulePaneRow row:FXCollections.observableArrayList(parent.rulePaneController.ruleView.getItems())) {
					if(row.getCb().isIndeterminate()) {
						
						continue;
					}
					boolean isSelected = row.getCb().isSelected();
					ManualRuleServices.scrollEvaluateRule(current_row,row.getGr(),parent,isSelected);
				}
				
	    		return null;
		    	}
			};
		task.setOnSucceeded(e -> {
			;
			parent.tableController.tableGrid.refresh();
			
			});

		task.setOnFailed(e -> {
		    Throwable problem = task.getException();
		    /* code to execute if task throws exception */
		    problem.printStackTrace(System.err);
		    
		    
		});

		task.setOnCancelled(e -> {
		    /* task was cancelled */
			;
		});
			
			Thread thread = new Thread(task);; thread.setDaemon(true);
			thread.setName("SE Rules from item "+current_row.getClient_item_number());
			thread.start();
			
		}



	
	

	public static List<ItemFetcherRow> fireRuleClassChange(HashMap<ItemFetcherRow, GenericClassRule> itemsToUpdate,UserAccount account) {
		
		itemsToUpdate.entrySet().stream().forEach( e ->{
			GenericClassRule gr = e.getValue();
			ItemFetcherRow row = e.getKey();
			
			((ItemFetcherRow)row).setReviewer_Rules(account.getUser_name());
			try{
				((ItemFetcherRow)row).setRule_Segment_id(gr.classif.get(0));
				((ItemFetcherRow)row).setRule_Segment_name(gr.classif.get(2));
				if(UUID2CID!=null) {
					
				}else {
					UUID2CID = Tools.UUID2CID(account.getActive_project());
					
				}
				((ItemFetcherRow)row).setRule_Segment_number(UUID2CID.get(gr.classif.get(0)));
				((ItemFetcherRow)row).setRule_description_Rules(gr.toString());
				((ItemFetcherRow)row).setRule_id_Rules((gr.toString()));
			}catch(Exception V) {
				V.printStackTrace(System.err);
				//((ItemFetcherRow)row).setNew_segment_id(null);
				//((ItemFetcherRow)row).setNew_segment_name(null);
				((ItemFetcherRow)row).setRule_Segment_id(null);
				((ItemFetcherRow)row).setRule_Segment_name(null);
				((ItemFetcherRow)row).setRule_Segment_number(null);
				((ItemFetcherRow)row).setRule_description_Rules(null);
				((ItemFetcherRow)row).setRule_id_Rules(null);
				
			}
			((ItemFetcherRow)row).setSource_Rules(DataInputMethods.USER_CLASSIFICATION_RULE);
			
			
			
		});
		
		return new ArrayList<ItemFetcherRow>(itemsToUpdate.keySet());
		
	}
	
	public static List<ItemFetcherRow> fireRuleClassBlank(ArrayList<ItemFetcherRow> itemsToBlank) {
		
		itemsToBlank.stream().forEach(row->{
			((ItemFetcherRow)row).setReviewer_Rules(null);
			//((ItemFetcherRow)row).setNew_segment_id(null);
			//((ItemFetcherRow)row).setNew_segment_name(null);
			((ItemFetcherRow)row).setRule_Segment_id(null);
			((ItemFetcherRow)row).setRule_Segment_name(null);
			((ItemFetcherRow)row).setRule_Segment_number(null);
			((ItemFetcherRow)row).setRule_description_Rules(null);
			((ItemFetcherRow)row).setSource_Rules(null);
			
		});
		
		return itemsToBlank;
		
	}

	
}
