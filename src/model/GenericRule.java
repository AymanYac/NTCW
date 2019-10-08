package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GenericRule {
	
	   Boolean dwg=false;
	   String main=null;
	   String comp=null;
	   String app=null;
	   String mg=null;
	   String pc=null;
	   
	   public ArrayList<String> classif = new ArrayList<String>(3);
	   public static HashMap<String,Integer> typeScoreSchema;
	   public Boolean active;
	   public Boolean matched=false;

	public GenericRule() {
		classif.add(null);
		classif.add(null);
		classif.add(null);
		
		
		if(typeScoreSchema!=null) {
			return;
		}
		typeScoreSchema  = new HashMap<String,Integer>();
		typeScoreSchema.put("MAIN", 100);
		typeScoreSchema.put("PC", 100);
		typeScoreSchema.put("FOR", 10);
		typeScoreSchema.put("MG", 10);
		typeScoreSchema.put("COMP", 10);
		typeScoreSchema.put("DWG", 1);
	}


	public String toString() {
		String ret = "";
		if(dwg) {
			ret += "*";
		}
		if(main!=null) {
			ret+=main;
		}else {
			ret+="\"ANY\"";
		}
		if(comp!=null) {
			ret+=" ("+comp+")";
		}
		if(app!=null) {
			ret+=" for "+app;
		}
		if(mg!=null || pc!=null) {
			ret+=" [";
		}
		if(mg!=null) {
			ret+=mg+" ";
		}
		if(pc!=null) {
			ret+="PC: "+pc;
		}
		if(mg!=null || pc!=null) {
			ret+="]";
		}
			
		return ret.toString().toUpperCase();
		
		
	}
	
	
	public TextFlow toDisplay() {
		Text textes []= new Text[] {new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text(""),new Text("")};
		int i = 0;
		
		if(dwg) {
			Text texte = new Text("*");
			texte.setFill(GlobalConstants.RULE_DISPLAY_DWG_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_DWG_FONT,GlobalConstants.RULE_DISPLAY_DWG_WEIGHT,GlobalConstants.RULE_DISPLAY_DWG_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(main!=null) {
			Text texte = new Text(main);
			texte.setFill(GlobalConstants.RULE_DISPLAY_MAIN_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_MAIN_FONT,GlobalConstants.RULE_DISPLAY_MAIN_WEIGHT,GlobalConstants.RULE_DISPLAY_MAIN_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}else {
			Text texte = new Text("\"ANY\"");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(comp!=null) {
			Text texte = new Text("(");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
			
			texte = new Text(comp);
			texte.setFill(GlobalConstants.RULE_DISPLAY_COMP_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_COMP_FONT,GlobalConstants.RULE_DISPLAY_COMP_WEIGHT,GlobalConstants.RULE_DISPLAY_COMP_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
			
			texte = new Text(")");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(app!=null) {
			Text texte = new Text(" for ");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
			
			texte = new Text(app);
			texte.setFill(GlobalConstants.RULE_DISPLAY_FOR_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_FOR_FONT,GlobalConstants.RULE_DISPLAY_FOR_WEIGHT,GlobalConstants.RULE_DISPLAY_FOR_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(mg!=null || pc!=null) {
			Text texte = new Text("[");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(mg!=null) {
			Text texte = new Text(mg);
			texte.setFill(GlobalConstants.RULE_DISPLAY_MG_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_MG_FONT,GlobalConstants.RULE_DISPLAY_MG_WEIGHT,GlobalConstants.RULE_DISPLAY_MG_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(pc!=null) {
			Text texte = new Text("PC: "+pc);
			texte.setFill(GlobalConstants.RULE_DISPLAY_PC_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_PC_FONT,GlobalConstants.RULE_DISPLAY_PC_WEIGHT,GlobalConstants.RULE_DISPLAY_PC_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
		if(mg!=null || pc!=null) {
			Text texte = new Text("]");
			texte.setFill(GlobalConstants.RULE_DISPLAY_SYNTAX_COLOR);
			texte.setFont(Font.font(GlobalConstants.RULE_DISPLAY_SYNTAX_FONT,GlobalConstants.RULE_DISPLAY_SYNTAX_WEIGHT,GlobalConstants.RULE_DISPLAY_SYNTAX_POSTURE,GlobalConstants.RULE_DISPLAY_FONT_SIZE));
			textes[i] = texte;i+=1;
		}
			
		TextFlow ret = new TextFlow(textes);
		ret.setMinHeight(0);
		ret.setPrefHeight(0);
		return ret;
		
		
	}
	
	
	public String getType() {
		String ret = "";
		
		if(main!=null) {
			ret+="MAIN";
		}
		if(comp!=null) {
			ret+="+COMP";
		}
		if(app!=null) {
			ret+="+FOR";
		}
		if(mg!=null) {
			ret+="+MG";
		}
		if(pc!=null) {
			ret+="+PC";
		}
		
		if(dwg) {
			ret += "+DWG";
		}
		
		
		
		return ret;
		
		
	}
	
	
	
	
	
	public Boolean getDwg() {
		return dwg;
	}
	public void setDwg(Boolean dwg) {
		if(dwg!=null) {
			this.dwg = dwg;
		}else {
			this.dwg=false;
		}
	}
	public String getMain() {
		return main;
	}
	public void setMain(String main) {
		try{
			this.main = main.toUpperCase();
		}catch(Exception V) {
			this.main = null;
		}
	}
	public String getComp() {
		return comp;
	}
	public void setComp(String comp) {
		try{
			this.comp = comp.toUpperCase();
		}catch(Exception V) {
			this.comp = null;
		}
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		try{
			this.app = app.toUpperCase();
		}catch(Exception V) {
			this.app=null;
		}
	}
	public String getMg() {
		return mg;
	}
	public void setMg(String mg) {
		this.mg = mg;
	}
	public String getPc() {
		return pc;
	}
	public void setPc(String pc) {
		this.pc = pc;
	}


	public int getTypeScore() {
		int score = 0;
		for(String componenent: this.getType().split(Pattern.quote("+"))) {
			if(componenent.equals("MAIN")) {

				score+=1000;
				
				for(String word : Arrays.asList( this.getMain().split(" ") ).subList(1, this.getMain().split(" ").length) ) {
					if(word.length()>3) {
						score+=100;
					}
				}
			}
			if(componenent.equals("PC") || componenent.equals("FOR")) {
				score+=100;
			}
			if(componenent.equals("MG") || componenent.equals("COMP")) {
				score+=10;
			}
			if(componenent.equals("DWG")) {
				score+=1;
			}
			
		}
		return score;
	}
	
	
	

}
