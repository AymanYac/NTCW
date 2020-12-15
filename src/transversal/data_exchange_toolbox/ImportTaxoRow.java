package transversal.data_exchange_toolbox;

import javafx.util.Pair;
import model.*;
import org.apache.poi.ss.usermodel.Row;
import service.TranslationServices;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImportTaxoRow {

	ClassSegment segment;
	ClassCaracteristic carac;
	private boolean segmentParseFail;
	private boolean caracParseFail;
	public static HashMap<String, Integer> columnMap;
    
	public static ArrayList<Pair<Row,String>> rejectedRows = new ArrayList<Pair<Row,String>>();
    
	private static boolean forceUpdate;
	private static boolean accumulateUoMs;
	
	
	public ClassSegment getSegment() {
		return segment;
	}
	public ClassCaracteristic getCarac() {
		return carac;
	}
	
	private ClassSegment setSegment(Row current_row) {
		 ClassSegment tmpSegment = parseSegment(current_row);
		
		if(segmentParseHasFailed()) {
			return null;
		}
		if(CharDescriptionImportServices.projectGranularity!=(tmpSegment.getSegmentGranularity())) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Segment granularity ("+tmpSegment.getSegmentGranularity()+") is not the same as the project's("+CharDescriptionImportServices.projectGranularity+")");
			rejectedRows.add(rejectedRow);
			return null;
		}
		Optional<ClassSegment> segmentMatch = CharDescriptionImportServices.sid2Segment.values().stream().filter(s->s.hasSameClassNumbersAsSegment(tmpSegment)).findAny();
		if(segmentMatch.isPresent()) {
			//The segment is already known on all levels
			tmpSegment.setSegmentId(segmentMatch.get().getSegmentId());
		}else {
			//At least an unknown level on this segment with the correct lineage
			tmpSegment.setSegmentId(Tools.generate_uuid());
			CharDescriptionImportServices.sid2Segment.put(tmpSegment.getSegmentId(), tmpSegment);
		}
		return tmpSegment;
	}
	
	
	private ClassCaracteristic setCarac(Row current_row, String current_segment_id) {
		ClassCaracteristic tmpCarac;
		String row_char_id = current_row.getCell(columnMap.get("charId"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
		ClassCaracteristic knownCarac = CharDescriptionImportServices.chid2Carac.get(row_char_id);
		if(knownCarac!=null) {
			//The char is known
			tmpCarac = parseCarac(current_row);
			if(tmpCarac!=null) {
				//The carac was properly parsed
				if(tmpCarac.getCharacteristic_name().equals(knownCarac.getCharacteristic_name())) {
					
				}else {
					if(forceUpdate) {
						CharDescriptionImportServices.chid2Carac.get(row_char_id).setCharacteristic_name(tmpCarac.getCharacteristic_name());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong name: "+tmpCarac.getCharacteristic_name()+", expected: "+knownCarac.getCharacteristic_name());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				if(tmpCarac.getCharacteristic_name_translated().equals(knownCarac.getCharacteristic_name_translated())) {
					
				}else {
					if(forceUpdate) {
						CharDescriptionImportServices.chid2Carac.get(row_char_id).setCharacteristic_name_translated(tmpCarac.getCharacteristic_name_translated());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong translated name: "+tmpCarac.getCharacteristic_name_translated()+", expected: "+knownCarac.getCharacteristic_name_translated());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				ClassCaracteristic knownTemplate = CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id);
				boolean creatingNewTemplate=false;
				if(!(knownTemplate!=null)) {
					//No known template
					creatingNewTemplate=true;
					CharDescriptionImportServices.classSpecificFields.get(row_char_id).put(current_segment_id, new ClassCaracteristic());
					CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setIsCritical(tmpCarac.getIsCritical());
				}else if(tmpCarac.getIsCritical().equals(knownTemplate.getIsCritical())) {
					//Known template with no conflict
				}else {
					//Known template with conflict
					if(forceUpdate) {
						CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setIsCritical(tmpCarac.getIsCritical());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong criticality: "+tmpCarac.getIsCritical()+", expected: "+knownTemplate.getIsCritical());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				if(creatingNewTemplate) {
					//No known template
					CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setSequence(tmpCarac.getSequence());
				}else if(tmpCarac.getSequence() == knownTemplate.getSequence()) {
					//Known template with no conflict
				}else {
					//Known template with conflicts
					if(forceUpdate) {
						CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setSequence(tmpCarac.getSequence());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong sequence: "+tmpCarac.getSequence()+", expected: "+knownTemplate.getSequence());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				
				if(tmpCarac.getIsNumeric().equals(knownCarac.getIsNumeric())) {
					
				}else {
					Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong type: "+(tmpCarac.getIsNumeric()?"NUM":"TXT")+", expected: "+(knownCarac.getIsNumeric()?"NUM":"TXT"));
					rejectedRows.add(rejectedRow);
					caracParseFail();
					return null;
				}
				
				if(tmpCarac.getIsTranslatable().equals(knownCarac.getIsTranslatable())) {
					
				}else {
					Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong translability: "+(tmpCarac.getIsTranslatable()?"Y":"N")+", expected: "+(knownCarac.getIsTranslatable()?"Y":"N"));
					rejectedRows.add(rejectedRow);
					caracParseFail();
					return null;
				}
				
				if(creatingNewTemplate) {
					//No known template
					CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setAllowedUoms(tmpCarac.getAllowedUoms());
				}else if(knownTemplate.getAllowedUoms()!=null) {
					//The known template has uoms
					if(tmpCarac.getAllowedUoms()!=null) {
						//The row carac has uoms
						List<String> undeclaredUomIds = tmpCarac.getAllowedUoms().stream().filter(uid->!knownTemplate.getAllowedUoms().contains(uid)).collect(Collectors.toList());
						if(undeclaredUomIds.size()==0) {
							//All row uoms included in the known template
						}else {
							//There's at least a uom not included in the known template.
							//Check if the symbol has been misinterpreted and should be a convertible uom to it
							List<Pair<String, String>> reInterpredUoms = undeclaredUomIds.stream().map(uid-> knownTemplate.attemptUomSymbolInterpretationCorrection(uid))
							.filter(pair->pair!=null).collect(Collectors.toList());
							tmpCarac.setAllowedUoms(new ArrayList<String> (tmpCarac.getAllowedUoms().stream()
									.map(u->reInterpredUoms.stream().filter(i->i.getKey().equals(u)).findAny().map(e->e.getValue()).orElse(null)).filter(t->t!=null).collect(Collectors.toList())));
							
							if(undeclaredUomIds.size() == reInterpredUoms.size()) {
								//All uoms were misrepresented
								if(forceUpdate) {
									CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setAllowedUoms(tmpCarac.getAllowedUoms());
								}else {
									if(accumulateUoMs) {
										ArrayList<String> tmpUoms = knownTemplate.getAllowedUoms();
										tmpUoms.addAll(tmpCarac.getAllowedUoms());
										CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setAllowedUoms(new ArrayList<String>(new HashSet<String>(tmpUoms)));
									}else {
										Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong uom(s) :"+
										String.join(",",reInterpredUoms.stream().map(
												u->UnitOfMeasure.RunTimeUOMS.get(u.getValue()).toString()).collect(Collectors.toList()))
										+". uom(s) convertible");
										rejectedRows.add(rejectedRow);
										caracParseFail();
										return null;
									}
									
								}
							}else {
								//Some uoms are not convertible
								Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with wrong uom(s) :"+
								String.join(",",undeclaredUomIds.stream().filter(id->!reInterpredUoms.stream().map(r->r.getKey()).collect(Collectors.toList()).contains(id)).map(
										u->UnitOfMeasure.RunTimeUOMS.get(u).toString()).collect(Collectors.toList()))
								+". uom(s) not convertible");
								rejectedRows.add(rejectedRow);
								caracParseFail();
								return null;
							}
							
							
						}
							
					}else {
						//The row lacks declared uoms
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+" with undeclared uom(s), excpected: "+
						String.join(",",knownTemplate.getAllowedUoms().stream().map(id->UnitOfMeasure.RunTimeUOMS.get(id).toString()).collect(Collectors.toList())));
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}else {
					//The template has no declared uoms
					if(tmpCarac.getAllowedUoms()!=null) {
						//The row declares uoms
						if(forceUpdate || accumulateUoMs) {
							ArrayList<String> tmpUoms = tmpCarac.getAllowedUoms();
							CharDescriptionImportServices.classSpecificFields.get(row_char_id).get(current_segment_id).setAllowedUoms(new ArrayList<String>(new HashSet<String>(tmpUoms)));
						}else {
							Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Duplicate characteristic "+row_char_id+". Expected no uom");
							rejectedRows.add(rejectedRow);
							caracParseFail();
							return null;
						}
					}else {
						//The row doesn't declare uoms
						//Pass
					}
				}
				
				return knownCarac;
			}else {
				//The carac was not properly parsed
				return null;
			}
			
		}else {
			//The char is unknown
			tmpCarac = parseCarac(current_row);
			if(tmpCarac!=null) {
				//The carac was properly parsed
				tmpCarac.setCharacteristic_id(row_char_id);
				CharDescriptionImportServices.chid2Carac.put(tmpCarac.getCharacteristic_id(), tmpCarac);
				setClassSpecificCharFields(tmpCarac,current_segment_id);
				return tmpCarac;
			}
			//The carac was not properly parsed
			return null;
		}
		
	}
	private void setClassSpecificCharFields(ClassCaracteristic tmpCarac, String current_segment_id) {
		ClassCaracteristic classCaracTemplate = new ClassCaracteristic();
		classCaracTemplate.setSequence(tmpCarac.getSequence());
		classCaracTemplate.setIsCritical(tmpCarac.getIsCritical());
		classCaracTemplate.setAllowedUoms(tmpCarac.getAllowedUoms());
		try {
			CharDescriptionImportServices.classSpecificFields.get(tmpCarac.getCharacteristic_id()).put(current_segment_id, classCaracTemplate);
		}catch(Exception V) {
			CharDescriptionImportServices.classSpecificFields.put(tmpCarac.getCharacteristic_id(), new HashMap<String,ClassCaracteristic>());
			CharDescriptionImportServices.classSpecificFields.get(tmpCarac.getCharacteristic_id()).put(current_segment_id, classCaracTemplate);
		}
		
		
		
		
		
	}
	private ClassCaracteristic parseCarac(Row current_row) {
		ClassCaracteristic tmpCarac = new ClassCaracteristic();
		try{
			tmpCarac.setCharacteristic_name(current_row.getCell(columnMap.get("charName"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic name is not a valid character chain");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			tmpCarac.setCharacteristic_name_translated(current_row.getCell(columnMap.get("charNameTranslated"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic name translated is not a valid character chain");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			tmpCarac.setSequence((int)Math.floor(current_row.getCell(columnMap.get("charSequence"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue()));
		}catch(Exception V) {
			try {
				tmpCarac.setSequence(Integer.valueOf(current_row.getCell(columnMap.get("charSequence"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
			}catch(Exception E) {
				Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic sequence is not a valid number");
				rejectedRows.add(rejectedRow);
				caracParseFail();
				return null;
			}
			
		}
		if(tmpCarac.getSequence()==0) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic sequence is not a valid number");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			String tmpType = current_row.getCell(columnMap.get("charType"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(tmpType.equals("TXT")) {
				tmpCarac.setIsNumeric(false);
			}else if(tmpType.equals("NUM")) {
				tmpCarac.setIsNumeric(true);
			}else {
				throw new NullPointerException();
			}
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic type is not valid");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			String tmpTranslatable = current_row.getCell(columnMap.get("charIsTranslatable"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(tmpTranslatable.equals("N")) {
				tmpCarac.setIsTranslatable(false);
			}else if(tmpTranslatable.equals("Y")) {
				tmpCarac.setIsTranslatable(true);
			}else {
				if(!tmpCarac.getIsNumeric()) {
					throw new NullPointerException();
				}else {
					tmpCarac.setIsTranslatable(false);
				}
			}
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic translability is not valid");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			String tmpMandatoriness = current_row.getCell(columnMap.get("charIsMandatory"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(tmpMandatoriness.equals("N")) {
				tmpCarac.setIsCritical(false);
			}else if(tmpMandatoriness.equals("Y")) {
				tmpCarac.setIsCritical(true);
			}else {
				throw new NullPointerException();
			}
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic mandatoriness is not valid");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		try{
			String tmpUomSymbol = current_row.getCell(columnMap.get("charUoM"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(tmpUomSymbol!=null && tmpUomSymbol.length()>0) {
				UnitOfMeasure tmpUom = UnitOfMeasure.lookUpUomInText_SymbolPriority(tmpUomSymbol);
				if(tmpUom!=null) {
					ArrayList<String> tmp = new ArrayList<String>();
					tmp.add(tmpUom.getUom_id());
					tmpCarac.setAllowedUoms(tmp);
				}else {
					if(tmpCarac.getIsNumeric()) {
						throw new NullPointerException();
					}
				}
			}
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Characteristic uom is unknown");
			rejectedRows.add(rejectedRow);
			caracParseFail();
			return null;
		}
		
		
		return tmpCarac;
	}
	
	
	public Pair<GenericCharRule, ClassCaracteristic> parseTaxoRow(Row current_row, UserAccount account) {
		segment = setSegment(current_row);
		if(!segmentParseHasFailed() && rowHasCharNumber(current_row)) {
			carac = setCarac(current_row,segment.getClassNumber());
		}
		if(!caracParseHasFailed()) {
			parseValue(current_row,account);
			
		}
		if(!segmentParseHasFailed()){
			return parseRule(current_row,account);
		}
		return null;
	}

	private Pair<GenericCharRule,ClassCaracteristic> parseRule(Row current_row, UserAccount account) {
		String row_rule_id=null;
		String rowCharId=null;
		try{
			row_rule_id = current_row.getCell(columnMap.get("descriptionRule")).getStringCellValue();
			rowCharId = current_row.getCell(columnMap.get("charId")).getStringCellValue();
		}catch (Exception V){
			return null;
		}
		GenericCharRule newRule = new GenericCharRule(WordUtils.correctDescriptionRuleSyntax(row_rule_id));
		if(!newRule.parseSuccess()){
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Description Rule could not be parsed. Check syntax");
			rejectedRows.add(rejectedRow);
			return null;
		}
		if(rowCharId!=null && CharDescriptionImportServices.chid2Carac.get(rowCharId)!=null){
			ClassCaracteristic rowCarac = CharDescriptionImportServices.chid2Carac.get(rowCharId);
			newRule.setRegexMarker(rowCarac);
			if(newRule.parseSuccess()) {
				newRule.storeGenericCharRule();
				return new Pair<GenericCharRule,ClassCaracteristic>(newRule,rowCarac);
			}
		}
		Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Description Rule could not be evaluated using characteristic '"+carac.getCharacteristic_name()+". Check semantics");
		rejectedRows.add(rejectedRow);
		return null;
	}

	private void parseValue(Row current_row, UserAccount account) {
		if(carac!=null && !carac.getIsNumeric()) {
			String dl=null;
			String ul=null;
			try {
				dl = current_row.getCell(columnMap.get("value_DL")).getStringCellValue();
			}catch(Exception V) {
				
			}
			try {
				ul = current_row.getCell(columnMap.get("value_UL")).getStringCellValue();
				
			}catch(Exception V) {
				
			}
			
			CaracteristicValue val = new CaracteristicValue();
			val.setValue_id(Tools.generate_uuid());
			val.setParentChar(carac);
			val.setDataLanguageValue(dl);
			val.setUserLanguageValue(ul);
			val.setSource(DataInputMethods.PROJECT_SETUP_UPLOAD);
			val.setAuthor(account.getUser_id());
			
			TranslationServices.beAwareOfNewValue(val,carac);
			CharDescriptionExportServices.addCaracDefaultDataToPush(segment.getSegmentId(), carac, val);
			
		}
	}
	private boolean rowHasCharNumber(Row current_row) {
		try{
			return current_row.getCell(columnMap.get("charId"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().length()>0;
		}catch(NullPointerException V) {
			return false;
		}
	}
	
	private ClassSegment parseSegment(Row current_row) {
		ClassSegment tmpSegment = new ClassSegment();
		IntStream.range(0,CharDescriptionImportServices.projectGranularity).forEach(level->{
			parseSegmentLevel(level,current_row,tmpSegment);
		});
		return tmpSegment;
	}
	private void parseSegmentLevel(int level, Row current_row, ClassSegment tmpSegment) {

		try{
			tmpSegment.setLevelNumber(level,  current_row.getCell(columnMap.get("number_"+String.valueOf(level)),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Level number"+String.valueOf(level+1)+" is not a valid character chain");
			rejectedRows.add(rejectedRow);
			segmentParseFail();
			return;
		}
		try{
			tmpSegment.setLevelName(level,  current_row.getCell(columnMap.get("name_"+String.valueOf(level)),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
			tmpSegment.setLevelNameTranslated(level, tmpSegment.getLevelName(level));
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Level name"+String.valueOf(level+1)+" is not a valid character chain");
			rejectedRows.add(rejectedRow);
			segmentParseFail();
			return;
		}
		
		tmpSegment.setSegmentGranularity(level+1);
		
		Optional<Entry<String, ClassSegment>> lineage = CharDescriptionImportServices.sid2Segment.entrySet().stream().filter(e->e.getValue().getLevelNumber(level)!=null).filter(e->e.getValue().getLevelNumber(level).equals(tmpSegment.getLevelNumber(level))).findAny();
		if(lineage.isPresent()) {
			//The level number is known
			if(level==0 || lineage.get().getValue().getLevelNumber(level-1).equals(tmpSegment.getLevelNumber(level-1))) {
				//The level preceding hierarchy is the same declared
			}else {
				Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate level number: "+tmpSegment.getLevelNumber(level));
				rejectedRows.add(rejectedRow);
				segmentParseFail();
				return;
			}
			
			if(lineage.get().getValue().getLevelName(level).equals(tmpSegment.getLevelName(level))) {
				//The level name is the same declared
			}else {
				String oldName = lineage.get().getValue().getLevelName(level);
				if(forceUpdate) {
					CharDescriptionImportServices.sid2Segment.get(lineage.get().getKey()).setLevelName(level, tmpSegment.getLevelName(level));
					lineage.get().getValue().setLevelName(level, tmpSegment.getLevelName(level));
				}else {
					Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Level name"+String.valueOf(level+1)+" is wrong, expected: "+oldName);
					rejectedRows.add(rejectedRow);
					segmentParseFail();
					return;
				}
			}
		}
		
	}
	public boolean segmentParseHasFailed() {
		return this.segmentParseFail;
	}
	private void segmentParseFail() {
		this.segmentParseFail = true;
	}
	public boolean caracParseHasFailed() {
		return this.caracParseFail;
	}
	private void caracParseFail() {
		this.caracParseFail = true;
	}
	
	public static void setColumnMap() {
		columnMap = new HashMap<String,Integer>();
		columnMap.put("number_0", 0);
		columnMap.put("name_0", 1);
		columnMap.put("number_1", 2);
		columnMap.put("name_1", 3);
		columnMap.put("number_2", 4);
		columnMap.put("name_2", 5);
		columnMap.put("number_3", 6);
		columnMap.put("name_3", 7);
		columnMap.put("charId", 8);
		columnMap.put("charName", 9);
		columnMap.put("charNameTranslated", 10);
		columnMap.put("charSequence", 11);
		columnMap.put("charType", 12);
		columnMap.put("charIsTranslatable", 13);
		columnMap.put("charIsMandatory", 14);
		columnMap.put("charUoM", 15);
		columnMap.put("value_DL", 16);
		columnMap.put("value_UL", 17);
		columnMap.put("descriptionRule",18);
		
	}
	
    public static void loadTaxoDS(String active_pid) {
    	forceUpdate = false;
    	accumulateUoMs = true;
    	
    	CharDescriptionImportServices.sid2Segment = new HashMap<String,ClassSegment>();
    	CharDescriptionImportServices.chid2Carac = new HashMap<String,ClassCaracteristic>();
    	CharDescriptionImportServices.classSpecificFields = new HashMap<String, HashMap<String,ClassCaracteristic>>();
    	
    	try {
			UnitOfMeasure.RunTimeUOMS = UnitOfMeasure.fetch_units_of_measures(Tools.get_project_user_language_code(active_pid));
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
	
	
}
