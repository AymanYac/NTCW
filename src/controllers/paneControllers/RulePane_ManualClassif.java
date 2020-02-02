package controllers.paneControllers;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import controllers.Manual_classif;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.util.Callback;
import model.GenericRule;
import model.GlobalConstants;
import model.ItemFetcherRow;
import model.RulePaneRow;
import service.ManualRuleServices;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class RulePane_ManualClassif {

	@FXML Button rulePaneClose;
	@FXML Button addRule;
	
	
	
	@FXML public TableView<RulePaneRow> ruleView;
	@SuppressWarnings("rawtypes")
	@FXML TableColumn ruleColumn;
	@SuppressWarnings("rawtypes")
	@FXML TableColumn typeColumn;
	@SuppressWarnings("rawtypes")
	@FXML TableColumn cbColumn;
	
	@FXML TextField customMAIN;
	@FXML TextField customFOR;
	@FXML TextField customCOMP;
	
	@FXML CheckBox sameMG;
	@FXML CheckBox samePC;
	@FXML CheckBox sameDWG;
	
	
	
	public Manual_classif parent;

	private List<String> for_words;

	private List<String> dw_words;
	private ItemFetcherRow current_row;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setParent(Manual_classif manual_classif) throws ClassNotFoundException, SQLException {
		this.parent = manual_classif;
		for_words = Tools.get_project_for_words(parent.account.getActive_project());
		dw_words = Tools.get_project_dw_words(parent.account.getActive_project());
		
		cbColumn.setMinWidth(0);
		typeColumn.setMinWidth(0);
		ruleColumn.setMinWidth(0);
		
		
		
		ruleColumn.setCellValueFactory(new PropertyValueFactory<>("Rule_display"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
		typeColumn.setCellFactory(new Callback<TableColumn, TableCell>() {

            @Override
            public TableCell call(TableColumn param) 
            {
                return new TableCell<RulePaneRow, String>() 
                {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {

                            setTextFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
                            setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
                            setText(item);
                        }
                    }
                };
            }
        });
		cbColumn.setCellValueFactory(new PropertyValueFactory<>("Cb"));
		
		
	}

	private void setColumnWidth() {
		

		
		double verticalScrollBarWidth = 0;
		for (Node n : ruleView.lookupAll(".scroll-bar")) {
	        if (n instanceof ScrollBar) {
	            ScrollBar bar = (ScrollBar) n;
	            if (bar.getOrientation().equals(Orientation.VERTICAL)) {
	                verticalScrollBarWidth = bar.getWidth();
	                break; 
	            }
	        }
	    }
		
		cbColumn.prefWidthProperty().bind(ruleView.widthProperty().add(-verticalScrollBarWidth).multiply(0.1));
		typeColumn.prefWidthProperty().bind(ruleView.widthProperty().add(-verticalScrollBarWidth).multiply(0.15));
		ruleColumn.prefWidthProperty().bind(ruleView.widthProperty().add(-verticalScrollBarWidth).multiply(0.73));
		
	}

	public void load_item_rules() {
		
		ItemFetcherRow row = null;
		try {
			row=(ItemFetcherRow) parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
		}catch(Exception V) {
			
		}
		this.current_row = row;
		sameMG.setDisable(true);
		samePC.setDisable(true);
		sameMG.setSelected(false);
		samePC.setSelected(false);
		sameDWG.setSelected(false);
		customMAIN.setText("");
		customCOMP.setText("");
		customFOR.setText("");
		cleanRuleList();
		
		if(row!=null) {
			
			load_previous_rules(row);
			load_default_rules(row);
			
			setColumnWidth();
			
		}else {
			//empty row selection
			
		}
}
	
	private void load_previous_rules(ItemFetcherRow row) {
		row.itemRules.forEach((k)->{
			
			GenericRule gr = ItemFetcherRow.staticRules.get(k);
			addGR2List(gr,false,true);
		});
	}

	private void load_default_rules(ItemFetcherRow row) {
		String desc = row.getLong_description().length()>0 ? row.getLong_description() : row.getShort_description();
		desc = desc.toUpperCase();
		String w1w2 = WordUtils.getSearchWords(desc);
		String w1 = desc.split(" ")[0];
		String mg = row.getMaterial_group();
		String pc = row.getPreclassifiation();
		String f1f2 = null;
		String f1 =null;
		boolean dwg = false;
		
		for(String fw:for_words) {
			try{
				f1f2 = WordUtils.getSearchWords(desc.split(fw.toUpperCase()+" ")[1]);
				f1 = desc.split(fw.toUpperCase()+" ")[1].split(" ")[0];
				break;
			}catch(Exception V) {
				
			}
		}
		for(String dw:dw_words) {
			if(desc.contains(dw.toUpperCase())){
				dwg=true;
				break;
			}
		}
		
		
		GenericRule r1 = new GenericRule();
		r1.setMain(w1w2);
		addGR2List(r1,false,false);
		
		if(f1f2!=null) {
			GenericRule r2 = new GenericRule();
			r2.setMain(w1);
			r2.setApp(f1f2);
			addGR2List(r2,false,false);
			
			GenericRule r3 = new GenericRule();
			r3.setMain(w1);
			r3.setApp(f1);
			addGR2List(r3,false,false);
			
		}
		
		if(mg!=null) {
			GenericRule r4 = new GenericRule();
			r4.setMain(w1);
			r4.setMg(mg);
			addGR2List(r4,false,false);
			sameMG.setDisable(false);
		}
		if(pc!=null) {
			GenericRule r5 = new GenericRule();
			r5.setMain(w1);
			r5.setPc(pc);
			addGR2List(r5,false,false);
			samePC.setDisable(false);
		}
		if(dwg) {
			GenericRule r6 = new GenericRule();
			r6.setMain(w1);
			r6.setDwg(true);
			addGR2List(r6,false,false);
		}
		
		GenericRule r7 = new GenericRule();
		r7.setMain(w1);
		this.customMAIN.setText(w1);
		addGR2List(r7,false,false);
		
		
		if(f1f2!=null) {
			GenericRule r8 = new GenericRule();
			r8.setApp(f1f2);
			addGR2List(r8,false,false);
			
			GenericRule r9 = new GenericRule();
			r9.setApp(f1);
			addGR2List(r9,false,false);
			
		}
		
		if(mg!=null) {
			GenericRule r10 = new GenericRule();
			r10.setMg(mg);
			addGR2List(r10,false,false);
		}
		
		if(pc!=null) {
			GenericRule r11 = new GenericRule();
			r11.setPc(pc);
			addGR2List(r11,false,false);
		}
		
		
	}

	private void cleanRuleList() {
		ruleView.getItems().clear();
	}

	@FXML public void PaneClose() {
		parent.rulesButton.setSelected(false);
		parent.setBottomRegionColumnSpans(false);
		parent.classification.requestFocus();
	}
	@FXML public void addRuleButtonAction() {
		GenericRule nouvelle = new GenericRule();
		if(customMAIN.getText().replace(" ", "").length()>0) {
			nouvelle.setMain(customMAIN.getText().toUpperCase().trim());
		}
		if(customFOR.getText().replace(" ", "").length()>0) {
			nouvelle.setApp(customFOR.getText().toUpperCase().trim());
		}
		if(customCOMP.getText().replace(" ", "").length()>0) {
			nouvelle.setComp(customCOMP.getText().toUpperCase().trim());
		}
		if(sameMG.isSelected()) {
			nouvelle.setMg(current_row.getMaterial_group());
		}
		if(samePC.isSelected()) {
			nouvelle.setPc(current_row.getPreclassifiation());
		}
		
		nouvelle.setDwg(sameDWG.isSelected());
		
		
		sameMG.setSelected(false);
		samePC.setSelected(false);
		sameDWG.setSelected(false);
		customMAIN.setText("");
		customCOMP.setText("");
		customFOR.setText("");
		
		if(nouvelle.getTypeScore()>0) {
			addGR2List(nouvelle,true,false);
		}
		
		this.parent.classification.requestFocus();
		
	}

	private void addGR2List(GenericRule nouvelle_regle,boolean manualAdd, boolean previousAdd) {
		
		for( RulePaneRow rrw : ruleView.getItems()) {
			if(rrw.getRule_desc().equals(nouvelle_regle.toString())) {
				return;
			}
		}
		if(!ItemFetcherRow.staticRules.containsKey(nouvelle_regle.toString())) {
			//The user/default loader just inputed a an unknown rule before
			//Not present within the staticRules
			ManualRuleServices.evaluateNewRule(nouvelle_regle, parent);
		}
		if(!current_row.itemRules.contains(nouvelle_regle.toString())) {
			current_row.itemRules.add(nouvelle_regle.toString());
		}
		RulePaneRow nouvelle_ligne = new RulePaneRow(this.parent,nouvelle_regle,ManualRuleServices.getRuleAutoSelect(nouvelle_regle,manualAdd,previousAdd));
		ruleView.getItems().add(nouvelle_ligne);
		ruleView.getItems().sort(new Comparator<RulePaneRow>() {

				@Override
				public int compare(RulePaneRow o1, RulePaneRow o2) {
					//Desc sort
					int ret = o2.getGr().getTypeScore() -
							o1.getGr().getTypeScore();
					if(ret==0) {
						ret = o2.getGr().toString().split(Pattern.quote("+")).length -
								o1.getGr().toString().split(Pattern.quote("+")).length;
					}
					if(ret==0 && o1.getGr().getType().contains("FOR") && o2.getGr().getType().contains("FOR")) {
						if(o1.getGr().getApp().startsWith(o2.getGr().getApp())) {
							return o2.getGr().getApp().length()-
									o1.getGr().getApp().length();
						}
						if(o2.getGr().getApp().startsWith(o1.getGr().getApp())) {
							return o2.getGr().getApp().length()-
									o1.getGr().getApp().length();
						}
						
					}
					return ret;
				}
                 
        		});
		
	}
	
	
	@FXML public void addRule(KeyEvent event) {
		 if(event.getCode().equals(KeyCode.ENTER)) {
			 addRuleButtonAction();
		 }
	 }

}
