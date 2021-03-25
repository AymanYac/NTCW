package transversal.data_exchange_toolbox;

import javafx.util.Pair;
import model.*;
import org.apache.poi.ss.usermodel.Row;
import service.TranslationServices;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImportItemRow {

	private CharDescriptionRow item;
	private CaracteristicValue value;
	private boolean itemParseHasFailed=false;
	private boolean valueParseHasFailed=false;
	public static HashMap<String, Integer> columnMap;
	
	public static ArrayList<Pair<Row,String>> rejectedRows = new ArrayList<Pair<Row,String>>();
	
	private static boolean forceUpdate;
	private static Unidecode unidecode;
    
	public void parseItemRow(Row current_row, UserAccount account) {
		item = setItem(current_row);
		if(!itemParseHasFailed && item.getClass_segment()!=null && rowGetCharNumber(current_row)!=null) {
			
			value = setCaracValue(current_row,item,rowGetCharNumber(current_row));
		}
		if(!valueParseHasFailed && value!=null) {
			
			CharDescriptionExportServices.addItemCharDataToPush(item, value, CharDescriptionImportServices.chid2Carac.get(rowGetCharNumber(current_row)));
		}
	}

	
	private String rowGetCharNumber(Row current_row) {
		try{
			String ret = current_row.getCell(columnMap.get("charId")).getStringCellValue();
			if(ret.length()>0){
				return ret;
			}
			return null;
		}catch(Exception V) {
			return null;
		}
	}

	private CaracteristicValue setCaracValue(Row current_row, CharDescriptionRow item, String charID) {
		
		CaracteristicValue current_value = new CaracteristicValue();
		
		ClassCaracteristic knownCarac = CharDescriptionImportServices.chid2Carac.get(charID);
		if(knownCarac!=null) {
			//The carac is known
			
			ClassCaracteristic knownTemplate = CharDescriptionImportServices.classSpecificFields.get(charID).get(item.getClass_segment().getClassNumber());
			if(knownTemplate!=null) {
				if(knownCarac.getIsNumeric()) {
					//The carac is numeric
					
					try {
						String num = current_row.getCell(columnMap.get("value_nominal"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						if(num.length()>0) {
							current_value.setNominal_value(String.valueOf(Double.valueOf(num.replace(",", "."))));
						}
					}catch(Exception V) {
						try {
							Double num = current_row.getCell(columnMap.get("value_nominal"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();
							current_value.setNominal_value(String.valueOf(num));
						}catch(Exception E) {
							valueParseHasFailed = true;
							rejectedRows.add(new Pair<Row,String>(current_row,"Nominal value is not a valid number"));
							return null;
						}
					}
					
					try {
						String num = current_row.getCell(columnMap.get("value_min"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						if(num.length()>0) {
							current_value.setMin_value(String.valueOf(Double.valueOf(num.replace(",", "."))));
						}
					}catch(Exception V) {
						try {
							Double num = current_row.getCell(columnMap.get("value_min"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();
							current_value.setMin_value(String.valueOf(num));
						}catch(Exception E) {
							valueParseHasFailed = true;
							rejectedRows.add(new Pair<Row,String>(current_row,"Minimum value is not a valid number"));
							return null;
						}
					}
					
					try {
						String num = current_row.getCell(columnMap.get("value_max"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						if(num.length()>0) {
							current_value.setMax_value(String.valueOf(Double.valueOf(num.replace(",", "."))));
						}
					}catch(Exception V) {
						try {
							Double num = current_row.getCell(columnMap.get("value_max"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();
							current_value.setMax_value(String.valueOf(num));
						}catch(Exception E) {
							valueParseHasFailed = true;
							rejectedRows.add(new Pair<Row,String>(current_row,"Maximum value is not a valid number"));
							return null;
						}
					}
					
					
					if(knownTemplate.getAllowedUoms()!=null && knownTemplate.getAllowedUoms().size()>0) {
						
						//The carac requires uom, check for uom symbol exists in row
						String tmpUomSymbol = current_row.getCell(columnMap.get("value_uom"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						if(tmpUomSymbol!=null && tmpUomSymbol.length()>0) {
							
							//The uom symbol exists in row
							UnitOfMeasure tmpUom = UnitOfMeasure.lookUpUomInText_SymbolPriority(tmpUomSymbol);
							if(tmpUom!=null) {
								
								//The uom symbol is known
								if(knownTemplate.getAllowedUoms().contains(tmpUom.getUom_id())){
									current_value.setUom_id(tmpUom.getUom_id());
								}else {
									
									//The row uom is not known in the template
									
									//Check if the symbol has been misinterpreted and should be a convertible uom to it
									Pair<String, String> reInterpredUom = knownTemplate.attemptUomSymbolInterpretationCorrection(tmpUom.getUom_id());
									if(reInterpredUom!=null) {
										//The uom has been corrected
										current_value.setUom_id(reInterpredUom.getValue());
									}else {
										
										//The uom can not be corrected
										rejectedRows.add(new Pair<Row,String>(current_row,UnitOfMeasure.RunTimeUOMS.get(tmpUom).toString()+" can not be converted to "+String.join(",", knownTemplate.getAllowedUoms().stream().map(uid->UnitOfMeasure.RunTimeUOMS.get(uid).getUom_name()).collect(Collectors.toList()))));
										valueParseHasFailed=true;
										return null;
									}
								}
							}else {
								
								//The uom symbol is not known
								rejectedRows.add(new Pair<Row,String>(current_row,"Uom symbol: "+tmpUomSymbol+" can not be matched to any known unit of measure"));
								valueParseHasFailed=true;
								return null;
							}
						}else {
							
							//The row has no uom symbol
							if(
								(current_value.getNominal_value()!=null && current_value.getNominal_value().length()>0)||
								(current_value.getMin_value()!=null && current_value.getMin_value().length()>0)||
								(current_value.getMax_value()!=null && current_value.getMax_value().length()>0)
							) {
								//And yet there's a nominal, min or max value in the row, reject
								rejectedRows.add(new Pair<Row, String>(current_row, "Uom is required for characteristic: " + charID));
								valueParseHasFailed = true;
								return null;
							}else{
								//No num field in the row, allow *UNKNOWN*
								try{
									current_value.setUom_id(knownTemplate.getAllowedUoms().get(0));
								}catch (Exception V){

								}
							}
						}
					}
				}else {
					
					//The carac is not numeric
					String dataVal = current_row.getCell(columnMap.get("value_data"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					dataVal = (dataVal.length()>0)?dataVal:null;
					String userVal = current_row.getCell(columnMap.get("value_user"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					userVal = (userVal.length()>0)?userVal:null;
					
					if(knownCarac.getIsTranslatable()) {
						
						
						Pair<String, String> conflictingLinks = TranslationServices.createUnverifiedDicoTranslationLink(dataVal,userVal);
						
						if(conflictingLinks!=null) {
							
							//Inconsistent translation
							rejectedRows.add(new Pair<Row,String>(current_row,"Value "+dataVal+"/"+userVal+" was not created (inconsistent translation). Expected: "+conflictingLinks.getKey()+"/"+conflictingLinks.getValue()));
							valueParseHasFailed=true;
							return null;
							
						}else {
							current_value.setDataLanguageValue(dataVal);
							current_value.setUserLanguageValue(userVal);
						}

						
					}else {
						current_value.setDataLanguageValue(dataVal);
					}
				}
				
			}else {
				
				//The carac is not declared in this segment (unknown template)
				rejectedRows.add(new Pair<Row,String>(current_row,"Non blocking Warning: Characteristic: "+charID+" is not declared for the item's category: "+item.getClass_segment().getClassNumber()));
				//valueParseHasFailed = true;
				//return null;
			}
		}else {
			
			//The carac is not known
			rejectedRows.add(new Pair<Row,String>(current_row,"Characteristic "+charID+" is not declared"));
			valueParseHasFailed = true;
			return null;
		}
		current_value.setSource(DataInputMethods.PROJECT_SETUP_UPLOAD);
		current_value.setManually_Reviewed(true);
		try{
			String url = current_row.getCell(columnMap.get("value_url"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(url!=null && url.replace(" ","").length()>0){
				current_value.setUrl(url);
			}
		}catch (Exception V){

		}

		return current_value;
	}


	private CharDescriptionRow setItem(Row current_row) {
		try {
			String row_item_number = current_row.getCell(columnMap.get("client_number"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_item_number = unidecode.decodeAndTrim(row_item_number);
			String row_sd_data = current_row.getCell(columnMap.get("sd_data"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_sd_data = unidecode.decodeAndTrim(row_sd_data);
			String row_sd_user = current_row.getCell(columnMap.get("sd_user"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_sd_user = unidecode.decodeAndTrim(row_sd_user);
			String row_ld_data = current_row.getCell(columnMap.get("ld_data"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_ld_data = unidecode.decodeAndTrim(row_ld_data);
			String row_ld_user = current_row.getCell(columnMap.get("ld_user"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_ld_user = unidecode.decodeAndTrim(row_ld_user);
			String row_mg = current_row.getCell(columnMap.get("mg"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_mg = unidecode.decodeAndTrim(row_mg);
			String row_pc = current_row.getCell(columnMap.get("preclass"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			row_pc = unidecode.decodeAndTrim(row_pc);

			String row_class_number = current_row.getCell(columnMap.get("class_number"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			Optional<ClassSegment> row_segment = CharDescriptionImportServices.sid2Segment.values().stream().filter(s->s.getClassNumber().equals(row_class_number)).findAny();

			CharDescriptionRow row_item = new CharDescriptionRow();
			if(row_item_number.replace(" ", "").length()==0) {
				rejectedRows.add(new Pair<Row,String>(current_row,"Row has no item ID"));
				itemParseHasFailed = true;
				return null;
			}

			if(row_segment.isPresent()) {
				row_item.setClass_segment(row_segment.get());
			}else {
				if(row_class_number.length()>0) {
					rejectedRows.add(new Pair<Row,String>(current_row,"Class number unknown: "+row_class_number+". Item created but not classified"));
				}
				row_item.setClass_segment(null);
			}

			row_item.setClient_item_number(row_item_number);
			row_item.setShort_desc(row_sd_data.length()>0?row_sd_data:null);
			row_item.setLong_desc(row_ld_data.length()>0?row_ld_data:null);
			row_item.setShort_desc_translated(row_sd_user.length()>0?row_sd_user:null);
			row_item.setLong_desc_translated(row_ld_user.length()>0?row_ld_user:null);
			row_item.setMaterial_group(row_mg.length()>0?row_mg:null);
			row_item.setPreclassif(row_pc.length()>0?row_pc:null);

			CharDescriptionRow known_item = CharDescriptionImportServices.client2Item.get(row_item.getClient_item_number().toLowerCase());
			if(known_item!=null) {
				//The item is known
				known_item.setShort_desc(forceUpdate?row_item.getShort_desc():known_item.getShort_desc());
				known_item.setLong_desc(forceUpdate?row_item.getLong_desc():known_item.getLong_desc());
				known_item.setShort_desc_translated(forceUpdate?row_item.getShort_desc_translated():known_item.getShort_desc_translated());
				known_item.setLong_desc_translated(forceUpdate?row_item.getLong_desc_translated():known_item.getLong_desc_translated());
				known_item.setMaterial_group(forceUpdate?row_item.getMaterial_group():known_item.getMaterial_group());
				known_item.setPreclassif(forceUpdate?row_item.getPreclassif():known_item.getPreclassif());
				known_item.setClass_segment((forceUpdate && row_item.getClass_segment()!=null)?row_item.getClass_segment():known_item.getClass_segment());

				CharDescriptionImportServices.client2Item.put(known_item.getClient_item_number().toLowerCase(), known_item);
				return known_item;
			}else {
				//The item is not known
				row_item.setItem_id(Tools.generate_uuid());
				CharDescriptionImportServices.client2Item.put(row_item.getClient_item_number().toLowerCase(), row_item);
				return row_item;
			}
		}catch(Exception V) {
			rejectedRows.add(new Pair<Row,String>(current_row,"Line could not be read"));
			itemParseHasFailed = true;
			return null;
		}


	}

	public static void setColumnMap() {
		columnMap = new HashMap<String,Integer>();
		columnMap.put("client_number", 0);
		columnMap.put("sd_data", 1);
		columnMap.put("sd_user", 2);
		columnMap.put("ld_data", 3);
		columnMap.put("ld_user", 4);
		columnMap.put("mg", 5);
		columnMap.put("preclass", 6);
		columnMap.put("class_number", 7);
		columnMap.put("charId", 8);
		columnMap.put("value_data", 9);
		columnMap.put("value_user", 10);
		columnMap.put("value_nominal", 11);
		columnMap.put("value_min", 12);
		columnMap.put("value_max", 13);
		columnMap.put("value_uom", 14);
		columnMap.put("value_note", 15);
		columnMap.put("valute_rule", 16);
		columnMap.put("value_url",17);
		
	}

	public static void loadItemDS() {
		forceUpdate = false;
		CharDescriptionImportServices.client2Item = new HashMap<String,CharDescriptionRow>();
		unidecode = Unidecode.toAscii();
		
	}

}
