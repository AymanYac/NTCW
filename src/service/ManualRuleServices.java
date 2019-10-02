package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controllers.Manual_classif;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import model.AutoCompleteBox_ManualClassification;
import model.ClassificationMethods;
import model.GenericRule;
import model.ItemFetcherRow;
import model.RulePaneRow;
import transversal.generic.Tools;

public class ManualRuleServices {

	static long tickTime;

	public static void benchmark(GenericRule gr, Manual_classif manualClassifController) {
		
		TimeMaster.tick();
		int i=0;
		int k=0;
		
		Pattern p = null;
		if(gr.getComp()!=null) {
			p = Pattern.compile(".* "+gr.getComp()+" .*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			k+=1;
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				i+=1;
			}
			
		}
		TimeMaster.tock("Scanning "+String.valueOf(k)+" items for rule of type '"+gr.getType()+"', found "+String.valueOf(i)+" items matching after");
	}
	
	private static boolean tryRuleOnItem(GenericRule gr, ItemFetcherRow row, Pattern compiledCompPattern) {
		if(gr.matched) {
			return row.itemRules.contains(gr.toString());
		}
		try {
			
			if(gr.getMain()!=null) {
				if( ((ItemFetcherRow) row).getLong_description().toUpperCase().startsWith(gr.getMain()) ||  ((ItemFetcherRow) row).getShort_description().toUpperCase().startsWith(gr.getMain()) ) {
					
				}else {
					return false;
				}
			}
			

			if(compiledCompPattern!=null) {
				Matcher matcher = compiledCompPattern.matcher(((ItemFetcherRow) row).getLong_description().toUpperCase());
				if(matcher.matches()) {
					
				}else {
					matcher = compiledCompPattern.matcher(((ItemFetcherRow) row).getShort_description().toUpperCase());
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

	public static boolean assignClass2GR(GenericRule gr, ItemFetcherRow selectedItem) {
		if(selectedItem.getDisplay_segment_id()!=null && selectedItem.getSource_Display().equals("MANUAL")) {
			gr.classif.set(0, selectedItem.getDisplay_segment_id());
			gr.classif.set(1, selectedItem.getDisplay_segment_number());
			gr.classif.set(2, selectedItem.getDisplay_segment_name());
			return true;
		}
		return false;
	}
	
	public static void unapplyRule(GenericRule gr, Manual_classif manualClassifController) {
		
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
		
		Pattern p = null;
		HashMap<String,GenericRule> itemsToUpdate= new HashMap<String,GenericRule>();
		ArrayList<String> itemsToBlank= new ArrayList<String>();
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		if(gr.getComp()!=null) {
			p = Pattern.compile(".* "+gr.getComp()+" .*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
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
			
		}
		gr.matched=true;
		
		List<ItemFetcherRow> databaseSyncList = manualClassifController.tableController.fireRuleClassChange(itemsToUpdate);
		databaseSyncList.addAll( manualClassifController.tableController.fireRuleClassBlank(itemsToBlank) );
		Tools.ItemFetcherRow2ClassEvent(databaseSyncList,manualClassifController.account,ClassificationMethods.USER_RULE);
		
		Tools.StoreRule(manualClassifController.account,gr,itemRuleMap,false,ClassificationMethods.USER_RULE);
	}

	public static void evaluateNewRule(GenericRule gr,Manual_classif manualClassifController) {
		
		gr.active=false;
		ItemFetcherRow.staticRules.put(gr.toString(), gr);
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		/*
		Pattern p = null;
		
		
		if(gr.getComp()!=null) {
			p = Pattern.compile(".* "+gr.getComp()+" .*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
			if(tryRuleOnItem(gr,(ItemFetcherRow) row,p)) {
				String[] itemRule = new String [] {null,null};
				itemRule[0]= ((ItemFetcherRow) row).getItem_id();
				itemRule[1]= gr.toString();
				itemRuleMap.add(itemRule);
				
				
			}
			
		}*/
		Tools.StoreRule(manualClassifController.account, gr,itemRuleMap,false,ClassificationMethods.USER_RULE);
	}
	public static void applyRule(GenericRule gr, Manual_classif manualClassifController) {
		
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
		
		Pattern p = null;
		
		HashMap<String,GenericRule> itemsToUpdate= new HashMap<String,GenericRule>();
		ArrayList<String> itemsToBlank= new ArrayList<String>();
		ArrayList<String[]> itemRuleMap = new ArrayList<String[]>();
		
		if(gr.getComp()!=null) {
			p = Pattern.compile(".* "+gr.getComp()+" .*");
		}
		for ( Object row:manualClassifController.tableController.tableGrid.getItems()){
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
			
		}
		gr.matched=true;
		
		List<ItemFetcherRow> databaseSyncList = manualClassifController.tableController.fireRuleClassChange(itemsToUpdate);
		databaseSyncList.addAll( manualClassifController.tableController.fireRuleClassBlank(itemsToBlank) );
		Tools.ItemFetcherRow2ClassEvent(databaseSyncList,manualClassifController.account,ClassificationMethods.USER_RULE);
		Tools.StoreRule(manualClassifController.account,gr,itemRuleMap,true,ClassificationMethods.USER_RULE);
		
	}

	

	private static GenericRule EvaluateItemRules(ItemFetcherRow row) {
		
		int max_score = Short.MIN_VALUE;
		HashSet<GenericRule> bestRules = new HashSet<GenericRule>();
		
		for( String loopRuleDesc : row.itemRules) {
			GenericRule loopRule = ItemFetcherRow.staticRules.get(loopRuleDesc);
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
		GenericRule finalRule = manageRuleDisambiguation(bestRules);
		return finalRule;
	}

	private static GenericRule manageRuleDisambiguation(HashSet<GenericRule> bestRules) {
		//Check for conflict
		Set<String> bestClassNumbers = bestRules.stream().map(e->e.classif.get(1)).collect(Collectors.toSet());
		if(bestClassNumbers.size()>1) {
			//Conflict, do nothing
			return null;
		}else if(bestClassNumbers.size()==1) {
			//No conflict
			Optional<GenericRule> bestRule = bestRules.stream().findAny();
			return bestRule.get();
		}else {
			//Possible path: item has no active rules
			return null;
		}
	}

	private static int scoreManualRule(GenericRule loopRule) {
		int score = 0;
		score += scoreManualRuleByType(loopRule);
		return score;
	}

	private static int scoreManualRuleByType(GenericRule loopRule) {
		return loopRule.getTypeScore();
	}

	public static Boolean getRuleAutoSelect(GenericRule nouvelle_regle, boolean manualAdd, boolean previousAdd) {
		
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

	private static Boolean getRuleAutoSelectByScore(GenericRule nouvelle_regle) {
		// TODO Auto-generated method stub
		return false;
	}

	private static Boolean getRuleAutoSelectByType(GenericRule nouvelle_regle) {
		// TODO Auto-generated method stub
		return nouvelle_regle.getTypeScore()>=1100;
	}

	public static void scrollEvaluateRule(ItemFetcherRow current_row, GenericRule rule,Manual_classif manual_classif_controller,boolean isSelected) {
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
						System.out.println("indeterminate rule "+row.getRule_desc());
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

	

	

	
}
