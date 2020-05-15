package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Row;

import javafx.util.Pair;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class ImportTaxoRow {
	public static int projectGranularity;
	private static boolean forceUpdate;
	private static boolean accumulateUoMs;
	private static HashMap<String, Integer> columnMap;
    public static ArrayList<Pair<Row,String>> rejectedRows = new ArrayList<Pair<Row,String>>();
    public static HashMap<String, ClassSegment> sid2Segment;
    public static HashMap<String, ClassCharacteristic> chid2Carac;
    
	ClassSegment segment;
	ClassCharacteristic carac;
	private boolean segmentParseFail;
	private boolean caracParseFail;
	
	
	public ClassSegment getSegment() {
		return segment;
	}
	public ClassCharacteristic getCarac() {
		return carac;
	}
	
	private ClassSegment setSegment(Row current_row) {
		 ClassSegment tmpSegment = parseSegment(current_row);
		
		if(segmentParseHasFailed()) {
			return null;
		}
		if(projectGranularity!=(tmpSegment.getSegmentGranularity())) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Segment granularity ("+tmpSegment.getSegmentGranularity()+") is not the same as the project's("+projectGranularity+")");
			rejectedRows.add(rejectedRow);
			return null;
		}
		Optional<ClassSegment> segmentMatch = sid2Segment.values().stream().filter(s->s.hasSameClassNumbersAsSegment(tmpSegment)).findAny();
		if(segmentMatch.isPresent()) {
			//The segment is already known on all levels
			tmpSegment.setSegmentId(segmentMatch.get().getSegmentId());
		}else {
			//At least an unknown level on this segment with the correct lineage
			tmpSegment.setSegmentId(Tools.generate_uuid());
			sid2Segment.put(tmpSegment.getSegmentId(), tmpSegment);
		}
		return tmpSegment;
	}
	
	
	private ClassCharacteristic setCarac(Row current_row) {
		ClassCharacteristic tmpCarac;
		String row_char_id = current_row.getCell(columnMap.get("charId"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
		ClassCharacteristic knownCarac = chid2Carac.get(row_char_id);
		if(knownCarac!=null) {
			//The char is known
			tmpCarac = parseCarac(current_row);
			if(tmpCarac!=null) {
				//The carac was properly parsed
				if(tmpCarac.getCharacteristic_name().equals(knownCarac.getCharacteristic_name())) {
					
				}else {
					if(forceUpdate) {
						chid2Carac.get(row_char_id).setCharacteristic_name(tmpCarac.getCharacteristic_name());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong name: "+tmpCarac.getCharacteristic_name()+", expected: "+knownCarac.getCharacteristic_name());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				if(tmpCarac.getCharacteristic_name_translated().equals(knownCarac.getCharacteristic_name_translated())) {
					
				}else {
					if(forceUpdate) {
						chid2Carac.get(row_char_id).setCharacteristic_name_translated(tmpCarac.getCharacteristic_name_translated());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong translated name: "+tmpCarac.getCharacteristic_name_translated()+", expected: "+knownCarac.getCharacteristic_name_translated());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				
				if(tmpCarac.getIsCritical().equals(knownCarac.getIsCritical())) {
					
				}else {
					if(forceUpdate) {
						chid2Carac.get(row_char_id).setIsCritical(tmpCarac.getIsCritical());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong criticality: "+tmpCarac.getIsCritical()+", expected: "+knownCarac.getIsCritical());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}

				if(tmpCarac.getSequence() == knownCarac.getSequence()) {
					
				}else {
					if(forceUpdate) {
						chid2Carac.get(row_char_id).setSequence(tmpCarac.getSequence());
					}else {
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong sequnece: "+tmpCarac.getSequence()+", expected: "+knownCarac.getSequence());
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}
				
				if(tmpCarac.getIsNumeric().equals(knownCarac.getIsNumeric())) {
					
				}else {
					Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong type: "+(tmpCarac.getIsNumeric()?"NUM":"TXT")+", expected: "+(knownCarac.getIsNumeric()?"NUM":"TXT"));
					rejectedRows.add(rejectedRow);
					caracParseFail();
					return null;
				}
				
				if(tmpCarac.getIsTranslatable().equals(knownCarac.getIsTranslatable())) {
					
				}else {
					Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong translability: "+(tmpCarac.getIsTranslatable()?"Y":"N")+", expected: "+(knownCarac.getIsTranslatable()?"Y":"N"));
					rejectedRows.add(rejectedRow);
					caracParseFail();
					return null;
				}
				
				if(knownCarac.getAllowedUoms()!=null) {
					//The known carac has uoms
					if(tmpCarac.getAllowedUoms()!=null) {
						//The row carac has uoms
						List<String> undeclaredUomIds = tmpCarac.getAllowedUoms().stream().filter(uid->!knownCarac.getAllowedUoms().contains(uid)).collect(Collectors.toList());
						if(undeclaredUomIds.size()==0) {
							//All row uoms included in the known carac
						}else {
							//There's at least a uom not included in the known carac.
							//Check if the symbol has been misinterpreted and should be a convertible uom to it
							List<Pair<String, String>> reInterpredUoms = undeclaredUomIds.stream().map(uid-> knownCarac.attemptUomSymbolInterpretationCorrection(uid))
							.filter(pair->pair!=null).collect(Collectors.toList());
							tmpCarac.setAllowedUoms(new ArrayList<String> (tmpCarac.getAllowedUoms().stream()
									.map(u->reInterpredUoms.stream().filter(i->i.getKey().equals(u)).findAny().get().getValue()).collect(Collectors.toList())));
							
							if(undeclaredUomIds.size() == reInterpredUoms.size()) {
								//All uoms were misrepresented
								if(forceUpdate) {
									chid2Carac.get(row_char_id).setAllowedUoms(tmpCarac.getAllowedUoms());
								}else {
									if(accumulateUoMs) {
										ArrayList<String> tmpUoms = knownCarac.getAllowedUoms();
										tmpUoms.addAll(tmpCarac.getAllowedUoms());
										chid2Carac.get(row_char_id).setAllowedUoms(new ArrayList<String>(new HashSet<String>(tmpUoms)));
									}else {
										Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong uom(s) :"+
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
								Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with wrong uom(s) :"+
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
						Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+" with undeclared uom(s), excpected: "+
						String.join(",",knownCarac.getAllowedUoms().stream().map(id->UnitOfMeasure.RunTimeUOMS.get(id).toString()).collect(Collectors.toList())));
						rejectedRows.add(rejectedRow);
						caracParseFail();
						return null;
					}
				}else {
					//The char has no declared uoms
					if(tmpCarac.getAllowedUoms()!=null) {
						//The row declares uoms
						if(forceUpdate) {
							ArrayList<String> tmpUoms = tmpCarac.getAllowedUoms();
							chid2Carac.get(row_char_id).setAllowedUoms(new ArrayList<String>(new HashSet<String>(tmpUoms)));
						}else {
							Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Dupplicate char "+row_char_id+". Expected no uom");
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
				chid2Carac.put(tmpCarac.getCharacteristic_id(), tmpCarac);
				return tmpCarac;
			}
			//The carac was not properly parsed
			return null;
		}
		
	}
	private ClassCharacteristic parseCarac(Row current_row) {
		ClassCharacteristic tmpCarac = new ClassCharacteristic();
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
				ArrayList<UnitOfMeasure> tmpUom = WordUtils.parseKnownUoMsFollowingDecimal("1"+tmpUomSymbol);
				if(tmpUom.size()>0) {
					tmpCarac.setAllowedUoms(new ArrayList<String>(tmpUom.stream().map(u->u.getUom_id()).collect(Collectors.toList())));
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
	
	
	public void parseTaxoRow(Row current_row) {
		segment = setSegment(current_row);
		if(!segmentParseHasFailed() && rowHasCharNumber(current_row)) {
			carac = setCarac(current_row);
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
		IntStream.range(0,projectGranularity).forEach(level->{
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
		}catch(NullPointerException V) {
			Pair<Row,String> rejectedRow = new Pair<Row,String>(current_row,"Level name"+String.valueOf(level+1)+" is not a valid character chain");
			rejectedRows.add(rejectedRow);
			segmentParseFail();
			return;
		}
		
		tmpSegment.setSegmentGranularity(level+1);
		
		Optional<Entry<String, ClassSegment>> lineage = sid2Segment.entrySet().stream().filter(e->e.getValue().getLevelNumber(level)!=null).filter(e->e.getValue().getLevelNumber(level).equals(tmpSegment.getLevelNumber(level))).findAny();
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
					sid2Segment.get(lineage.get().getKey()).setLevelName(level, tmpSegment.getLevelName(level));
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
	
	public static void setProjectGranularity(int granularity) {
		projectGranularity = granularity;
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
		
	}
	
    public static void loadTaxoDS() {
    	//Segment id to segment
    	sid2Segment = new HashMap<String,ClassSegment>();
    	forceUpdate = false;
    	accumulateUoMs = true;
    	chid2Carac = new HashMap<String,ClassCharacteristic>();
    	try {
			UnitOfMeasure.RunTimeUOMS = UnitOfMeasure.fetch_units_of_measures("en");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
	
	
}
