package transversal.data_exchange_toolbox;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import service.*;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.DedupLaunchDialog;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.dialog_toolbox.FxUtilTest;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CharDescriptionExportServices {

	private static int reviewRowIdx;
	private static int baseRowIdx;
	private static int taxoRowIdx;
	private static int knownValueRowIdx;
	private static int knownRulesSheetIdx;

	public static ConcurrentLinkedQueue<Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>> itemDataBuffer = new ConcurrentLinkedQueue<Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>>();
	//public static ConcurrentHashMap<String,CharDescriptionRow> itemRuleBuffer = new ConcurrentHashMap<String,CharDescriptionRow>();
	public static ConcurrentLinkedQueue<CharDescriptionRow> itemRuleBuffer = new ConcurrentLinkedQueue<CharDescriptionRow>();
	public static ConcurrentLinkedQueue<Pair<CaracteristicValue,ClassCaracteristic>> caracDataBuffer = new ConcurrentLinkedQueue<Pair<CaracteristicValue,ClassCaracteristic>>();
	public static ConcurrentLinkedQueue<Pair<ClassCaracteristic,ClassSegment>> caracDefBuffer = new ConcurrentLinkedQueue<Pair<ClassCaracteristic,ClassSegment>>();
	public static ConcurrentLinkedQueue<Pair<ClassCaracteristic,String>> caracDeleteBuffer = new ConcurrentLinkedQueue<Pair<ClassCaracteristic,String>>();

	private static PreparedStatement stmt;
	private static ArrayList<ArrayList<Object>> miscellanousQueue;
	private static AtomicBoolean threadedDBFlushActive = new AtomicBoolean(false);

	public static void ExportItemDataForClass(String targetClass, Char_description parent, boolean exportReview, boolean exportBase, boolean exportTaxo, boolean exportKV, boolean exportRules) throws ClassNotFoundException, SQLException, IOException {
		
		File file = openExportFile(parent);
		
		
        if(file!=null) {
        	
        }else {
        	return;
        }
        
        
		SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
        Sheet reviewSheet = null;
        if(exportReview){
        	reviewSheet = wb.createSheet("Review format");
			createReviewHeader(wb,reviewSheet);
		}
        Sheet baseSheet = null;
        if(exportBase) {
        	baseSheet = wb.createSheet("Database format");
        	createBaseHeader(wb,baseSheet);

		}

        reviewRowIdx = 0;
        baseRowIdx = 0;
        int reviewCharCardinality=0;

        for(CharDescriptionRow item:CharItemFetcher.allRowItems) {
        	String itemClass = item.getClass_segment_string().split("&&&")[0];
        	if(targetClass!=null && !targetClass.equals(itemClass)) {
        		continue;
        	}
        	ArrayList<ClassCaracteristic> itemChars = CharValuesLoader.active_characteristics.get(item.getClass_segment_string().split("&&&")[0]);
        	if(itemChars.size()>reviewCharCardinality) {
        		reviewCharCardinality = itemChars.size();
        	}
        	if(exportReview) appendReviewItem(item,reviewSheet,itemChars,parent);
        	if(exportBase) appendBaseItem(item,baseSheet,itemChars);
        }

		HashMap<String, ClassSegment> sid2Segment = Tools.get_project_segments(parent.account);
		ArrayList<Pair<String, ClassSegment>> ks = CharValuesLoader.active_characteristics.keySet().stream().map(s -> new Pair<String, ClassSegment>(s, sid2Segment.get(s))).collect(Collectors.toCollection(ArrayList::new));
		ks.sort(new Comparator<Pair<String, ClassSegment>>() {
			@Override
			public int compare(Pair<String, ClassSegment> o1, Pair<String, ClassSegment> o2) {
				return o1.getValue().getClassNumber().compareTo(o2.getValue().getClassNumber());
			}
		});

		if(exportTaxo) {
			Sheet taxoSheet = wb.createSheet("Taxonomy");
			createTaxoHeader(taxoSheet,wb);
			taxoRowIdx=0;
			ks.stream().forEach(kse->{
				CharValuesLoader.returnSortedCopyOfClassCharacteristic(kse.getKey()).forEach(carac->{
					appendTaxoRow(kse.getValue(),carac,taxoSheet);
				});
			});
		}

		if(exportKV){
			Sheet knownValueSheet = wb.createSheet("Known values");
			createKnownValuesHeader(knownValueSheet,wb);
			knownValueRowIdx=0;
			HashSet<ClassCaracteristic> caracSet = new HashSet<ClassCaracteristic>();
			CharValuesLoader.active_characteristics.values().stream().flatMap(a->a.stream()).forEach(c->caracSet.add(c));
			ArrayList<ClassCaracteristic> caracArray = new ArrayList<ClassCaracteristic>(caracSet);
			caracArray.sort(new Comparator<ClassCaracteristic>() {
				@Override
				public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
					return (o1.getCharacteristic_name()+o1.getCharacteristic_id()).compareTo(o2.getCharacteristic_name()+o2.getCharacteristic_id());
				}
			});
			caracArray.forEach(c->{
				TranslationServices.getTextEntriesForCharOnLanguages(c,true).forEach(t->{
					appendKnownValueRow(c, t,knownValueSheet);
				});

			});

		}
		Sheet knownRulesSheet = null;
		if(exportRules){
			knownRulesSheet = wb.createSheet("Known rules");
			createKnownRulesHeader(knownRulesSheet,wb);
			knownRulesSheetIdx = 0;
			Sheet finalKnownRulesSheet = knownRulesSheet;
			CharValuesLoader.active_characteristics.values().stream().flatMap(a -> a.stream()).collect(Collectors.toCollection(HashSet::new)).forEach(carac->{
				ArrayList<GenericCharRule> caracRules = CharPatternServices.descriptionRules.values().parallelStream().filter(rule->rule.getParentChar()!=null && rule.getParentChar().getCharacteristic_id().equals(carac.getCharacteristic_id())).collect(Collectors.toCollection(ArrayList::new));
				caracRules.sort(new Comparator<GenericCharRule>() {
					@Override
					public int compare(GenericCharRule o1, GenericCharRule o2) {
						return o1.getRuleSyntax().compareTo(o2.getRuleSyntax());
					}
				});
				caracRules.forEach(rule->{
					appendKnownRuleRow(carac,rule, finalKnownRulesSheet);
				});
			});
		}


        
        setColumnWidths(reviewSheet,baseSheet,knownRulesSheet, exportBase,exportReview,exportTaxo,exportKV,exportRules);
        
        closeExportFile(file,wb);
		
		ConfirmationDialog.show("File saved", "Results successfully saved in\n"+file.getAbsolutePath(), "OK");
		
		
	}


	private static void createKnownRulesHeader(Sheet knownRulesSheet, SXSSFWorkbook wb) {
		Row reviewHeader = createHeaderRow(wb,knownRulesSheet, 0, new String[] {
				"Characteristic ID",
				"Characteristic Name",
				"Characteristic Type",
				"Rule Syntax",
				"Rule Marker",
				"Rule action 1",
				"Rule action 2",
				"Rule action 3",
				"Rule action 4"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT
		}, new Double[]{}, 100);
	}

	private static void createReviewHeader(SXSSFWorkbook wb, Sheet reviewSheet) {
		Row reviewHeader = createHeaderRow(wb,reviewSheet, 0, new String[] {
				"Completion Status",
				"Client Item Number",
				"Short description",
				"Long description",
				"Material Group",
				"Classification number",
				"Classification name",
				"Reference URL"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.GREY_80_PERCENT
		}, new Double[]{}, 100);
		completeReviewHeaderRow(wb,reviewHeader,
				CharValuesLoader.active_characteristics.values().parallelStream()
						.map(a->a.size()).max(Integer::compare).get());
	}


	private static void createBaseHeader(SXSSFWorkbook wb, Sheet baseSheet) {
		createHeaderRow(wb,baseSheet, 0, new String[] {
				"Client Item Number",
				"Characteristic Sequence",
				"Characteristic ID",
				"Characteristic Name",
				"Characteristic Type",
				"Value",
				"Unit of Measure",
				"Value (translation)",
				"Value (Min)",
				"Value (Max)",
				"Note",
				"Source",
				"Rule",
				"Author",
				"URL"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
		}, new Double[]{}, 100);
	}


	private static void closeExportFile(File file, SXSSFWorkbook wb) throws IOException {
		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
        wb.write(out);
        out.close();

        // dispose of temporary files backing this workbook on disk
        wb.dispose();
        wb.close();
	}

	private static void setColumnWidths(Sheet reviewSheet, Sheet baseSheet, Sheet ruleSheet, boolean exportBase, boolean exportReview, boolean exportTaxo, boolean exportKV, boolean exportRules) {

		if(exportRules){
			ruleSheet.setColumnWidth(0,256*15);
			ruleSheet.setColumnWidth(1,256*30);
			ruleSheet.setColumnWidth(2,256*18);
			ruleSheet.setColumnWidth(3,256*104);
			ruleSheet.setColumnWidth(4,256*85);
			ruleSheet.setColumnWidth(5,256*42);
			ruleSheet.setColumnWidth(6,256*12);
			ruleSheet.setColumnWidth(7,256*12);
			ruleSheet.setColumnWidth(8,256*12);
			ruleSheet.setZoom(80);
			ruleSheet.createFreezePane(0,1);
		}
		if(exportReview){
			reviewSheet.setColumnWidth(0,256*17);
			reviewSheet.setColumnWidth(1,256*17);
			reviewSheet.setColumnWidth(2,256*50);
			reviewSheet.setColumnWidth(3,256*50);
			reviewSheet.setColumnWidth(4,256*15);
			reviewSheet.setColumnWidth(5,256*25);
			reviewSheet.setColumnWidth(6,256*35);
			reviewSheet.setColumnWidth(7,256*35);
			for(int i=0;i<CharValuesLoader.active_characteristics.values().parallelStream()
					.map(a->a.size()).max(Integer::compare).get();i++) {
				reviewSheet.setColumnWidth(8+2*i,256*20);
				reviewSheet.setColumnWidth(9+2*i,256*20);
			}
			reviewSheet.setZoom(60);
			reviewSheet.createFreezePane(0, 1);
		}

		if(exportBase){
			baseSheet.setColumnWidth(0, 256*16);
			baseSheet.setColumnWidth(1, 256*8);
			baseSheet.setColumnWidth(2, 256*22);
			baseSheet.setColumnWidth(3, 256*22);
			baseSheet.setColumnWidth(4, 256*22);
			baseSheet.setColumnWidth(5, 256*14);
			baseSheet.setColumnWidth(6, 256*14);
			baseSheet.setColumnWidth(7, 256*14);
			baseSheet.setColumnWidth(8, 256*14);
			baseSheet.setColumnWidth(9, 256*14);
			baseSheet.setColumnWidth(10, 256*14);
			baseSheet.setColumnWidth(11, 256*14);
			baseSheet.setColumnWidth(12, 256*14);
			baseSheet.setColumnWidth(13, 256*14);
			baseSheet.setColumnWidth(14, 256*14);
			baseSheet.setZoom(90);
			baseSheet.createFreezePane(0, 1);
		}

	}

	private static void appendKnownRuleRow(ClassCaracteristic carac, GenericCharRule rule, Sheet knownRulesSheet) {
		knownRulesSheetIdx+=1;
		Row row = knownRulesSheet.createRow(knownRulesSheetIdx);
		Cell loopCell = row.createCell(0);
		loopCell.setCellValue(carac.getCharacteristic_id());

		loopCell = row.createCell(1);
		loopCell.setCellValue(carac.getCharacteristic_name());

		loopCell = row.createCell(2);
		loopCell.setCellValue(carac.getIsNumeric()?"NUM":"TXT");

		loopCell = row.createCell(3);
		loopCell.setCellValue(rule.getRuleSyntax());

		loopCell = row.createCell(4);
		loopCell.setCellValue(rule.getRuleMarker());

		AtomicInteger actionIdx= new AtomicInteger(0);
		rule.getRuleActions().forEach(action->{
			actionIdx.addAndGet(1);
			Cell actionCell = row.createCell(4 + actionIdx.get());
			actionCell.setCellValue(action);
		});
	}

	private static void appendKnownValueRow(ClassCaracteristic carac, CharValueTextSuggestion sugg, Sheet knownValueSheet) {
		knownValueRowIdx+=1;
		Row row = knownValueSheet.createRow(knownValueRowIdx);
		Cell loopCell = row.createCell(0);
		loopCell.setCellValue(carac.getCharacteristic_id());

		loopCell = row.createCell(1);
		loopCell.setCellValue(carac.getCharacteristic_name());
		try{
			loopCell = row.createCell(2);
			loopCell.setCellValue(sugg.getSource_value());
		}catch (Exception V){

		}
		try{
			loopCell = row.createCell(3);
			loopCell.setCellValue(sugg.getTarget_value());
		}catch (Exception V){

		}


	}

	private static void appendTaxoRow(ClassSegment segment, ClassCaracteristic carac, Sheet taxoSheet) {
		taxoRowIdx+=1;
		Row row = taxoSheet.createRow(taxoRowIdx);
		Cell loopCell = row.createCell(0);
		loopCell.setCellValue(segment.getClassNumber());

		loopCell = row.createCell(1);
		loopCell.setCellValue(carac.getSequence());

		loopCell = row.createCell(2);
		loopCell.setCellValue(carac.getCharacteristic_id());

		loopCell = row.createCell(3);
		loopCell.setCellValue(carac.getCharacteristic_name());

		loopCell = row.createCell(4);
		loopCell.setCellValue(
				carac.getIsNumeric()?
						((carac.getAllowedUoms()!=null && carac.getAllowedUoms().size()>0)?"NUM with UoM":"NUM w/o Uom")
						:
						(carac.getIsTranslatable()?"TXT translatable":"TXT non translatable")
		);

		loopCell = row.createCell(5);
		loopCell.setCellValue(carac.getIsCritical()?"Critical":"Not critical");

		loopCell = row.createCell(6);
		try {
			loopCell.setCellValue(String.join("--",carac.getAllowedUoms().stream().map(uid->UnitOfMeasure.RunTimeUOMS.get(uid).toString()).collect(Collectors.toCollection(ArrayList::new))));
		}catch(Exception V) {

		}


	}

	private static void appendBaseItem(CharDescriptionRow item, Sheet baseSheet, ArrayList<ClassCaracteristic> itemChars) {
		String itemClass = item.getClass_segment_string().split("&&&")[0];
		itemChars.forEach(carac->{

			if(item.getData(itemClass)!=null && item.getData(itemClass).get(carac.getCharacteristic_id())!=null) {
				baseRowIdx+=1;
				Row row = baseSheet.createRow(baseRowIdx);
				Cell loopCell = row.createCell(0);
				loopCell.setCellValue(item.getClient_item_number());

				loopCell = row.createCell(1);
				loopCell.setCellValue(carac.getSequence());

				loopCell = row.createCell(2);
				loopCell.setCellValue(carac.getCharacteristic_id());

				loopCell = row.createCell(3);
				loopCell.setCellValue(carac.getCharacteristic_name());

				loopCell = row.createCell(4);
				loopCell.setCellValue(
						carac.getIsNumeric()?
								((carac.getAllowedUoms()!=null && carac.getAllowedUoms().size()>0)?"NUM with UoM":"NUM w/o Uom")
								:
								(carac.getIsTranslatable()?"TXT translatable":"TXT non translatable")
				);

				loopCell = row.createCell(5);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getStdValueRounded());
				}catch(Exception V) {

				}

				loopCell = row.createCell(6);
				try {
					loopCell.setCellValue(UnitOfMeasure.RunTimeUOMS.get(item.getData(itemClass).get(carac.getCharacteristic_id()).getUom_id()).toString());
				}catch(Exception V) {

				}

				loopCell = row.createCell(7);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getUserLanguageValue());
				}catch(Exception V) {

				}

				loopCell = row.createCell(8);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getMin_value());
				}catch(Exception V) {

				}

				loopCell = row.createCell(9);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getMax_value());
				}catch(Exception V) {

				}

				loopCell = row.createCell(10);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getNote());
				}catch(Exception V) {

				}

				loopCell = row.createCell(11);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getSource());
				}catch(Exception V) {

				}

				loopCell = row.createCell(12);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getRule_id());
				}catch(Exception V) {

				}

				loopCell = row.createCell(13);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getAuthorName());
				}catch(Exception V) {

				}

				loopCell = row.createCell(14);
				try{
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getUrl());
				}catch(Exception V) {

				}
			}
		});
		
	}

	private static void appendReviewItem(CharDescriptionRow item, Sheet reviewSheet, ArrayList<ClassCaracteristic> itemChars, Char_description parent) {
		reviewRowIdx+=1;
		Row row = reviewSheet.createRow(reviewRowIdx);
		Cell loopCell;

		loopCell = row.createCell(0);
		loopCell.setCellValue(item.getCompletionStatusString());

		loopCell = row.createCell(1);
		loopCell.setCellValue(item.getClient_item_number());
		
		loopCell = row.createCell(2);
		try{
			loopCell.setCellValue(item.getShort_desc());
		}catch (Exception V){
			loopCell.setCellValue(item.getShort_desc().substring(0,32766));
		}

		loopCell = row.createCell(3);
		try{
			loopCell.setCellValue(item.getLong_desc());
		}catch (Exception V){
			loopCell.setCellValue(item.getLong_desc().substring(0,32766));
		}
		
		loopCell = row.createCell(4);
		loopCell.setCellValue(item.getMaterial_group());
		
		loopCell = row.createCell(5);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[2]);
		
		loopCell = row.createCell(6);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[1]);

		loopCell = row.createCell(7);
		String urlList = item.getItemURLListForClass(item.getClass_segment_string().split("&&&")[0]);
		if(urlList.length()>0){
			loopCell.setCellValue(urlList);
		}

		for(int i=0;i<itemChars.size();i++) {
			loopCell = row.createCell(8+2*i);
			loopCell.setCellValue(itemChars.get(i).getCharacteristic_name());
			loopCell = row.createCell(9+2*i);
			try{
				loopCell.setCellValue(item.getData(item.getClass_segment_string().split("&&&")[0]).get(itemChars.get(i).getCharacteristic_id()).getDisplayValue(parent/*,carac*/));
			}catch(Exception V) {

			}
		}



	}

	private static void createKnownValuesHeader(Sheet knownValueSheet, SXSSFWorkbook wb) {
		createHeaderRow(wb,knownValueSheet, 0, new String[] {
				"Characteristic ID",
				"Characteristic Name",
				"Known value in DL",
				"Known value in UL"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT}, new Double[]{}, 100);
	}
	private static void createTaxoHeader(Sheet taxoSheet, SXSSFWorkbook wb){
		createHeaderRow(wb,taxoSheet, 0, new String[] {
				"Class Number",
				"Characteristic Sequence",
				"Characteristic ID",
				"Characteristic Name",
				"Characteristic Type",
				"Characteristic Criticality",
				"Unit of Measure"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT}, new Double[]{}, 100);
	}

	
	private static void completeReviewHeaderRow(SXSSFWorkbook wb, Row headerRow, int reviewCharCardinality) {
		
		//#445469 Dark blue 68,84,105
		//#8496AE Light blue 132,150,174
		
		byte[] byteColor = new byte[]{68,84,105};
		XSSFColor darkBlue = new XSSFColor(byteColor, null);
		byteColor = new byte[]{(byte) 132,(byte) 150,(byte) 174};
		XSSFColor lightBlue = new XSSFColor(byteColor, null);
		
		XSSFColor[] colorArr = new XSSFColor[]{darkBlue,lightBlue};
		
		for(int i=0;i<reviewCharCardinality;i++) {
			Cell cell = headerRow.createCell(8+2*i);
			cell.setCellValue("Characteristic name "+String.valueOf(i+1));
    		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(colorArr[Math.floorMod(i, 2)]);
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 Font font = wb.createFont();
    		 font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
    		 font.setBold(true);
    		 style.setFont(font);
    		 cell.setCellStyle(style);
    		 
    		 cell = headerRow.createCell(9+2*i);
 			 cell.setCellValue("Characteristic value "+String.valueOf(i+1));
     		 style = (XSSFCellStyle) wb.createCellStyle();
     		 style.setFillForegroundColor(colorArr[Math.floorMod(i, 2)]);
     		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
     		 font = wb.createFont();
     		 font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
     		 font.setBold(true);
     		 style.setFont(font);
     		 cell.setCellStyle(style);
    		 
		}
	}


	private static File openExportFile(Char_description parent) throws ClassNotFoundException, SQLException {
		String PROJECT_NAME;
		Connection conn = Tools.spawn_connection_from_pool();
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("select project_name from projects where project_id='"+parent.account.getActive_project()+"'");
	    rs.next();
	    PROJECT_NAME = rs.getString("project_name");
	    rs.close();
	    stmt.close();
	    conn.close();
	    
	    Date instant = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat( "MMMMM_dd" );
	    String time = sdf.format( instant );
	    
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(PROJECT_NAME+"_"+time);
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        return fileChooser.showSaveDialog(new Stage());
		
	}

	private static Row createHeaderRow(SXSSFWorkbook wb, Sheet targetSheet, Integer headerRowOffset, String[] columnTitles, IndexedColors[] columnColors, Double[] columnWidths, int zoomLevel) {
		targetSheet.createFreezePane(0,headerRowOffset+1);
		Row headerRow = targetSheet.createRow(headerRowOffset);
		for(int colIdx=0;colIdx<columnTitles.length;colIdx++) {
			Cell cell = headerRow.createCell(colIdx);
			cell.setCellValue(columnTitles[colIdx]);
    		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
    		 try{
    			 style.setFillForegroundColor(columnColors[colIdx].getIndex());
    		 }catch(Exception V) {
    			 byte[] byteColor = new byte[]{68,84,105};
    			 XSSFColor darkBlue = new XSSFColor(byteColor, null);	
    			 style.setFillForegroundColor(darkBlue);
    		 }
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 Font font = wb.createFont();
    		 font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
    		 font.setBold(true);
    		 style.setFont(font);
    		 cell.setCellStyle(style);
    		 targetSheet.setColumnWidth(colIdx, (int) (columnWidths.length==columnTitles.length?Math.ceil(columnWidths[colIdx])*256:Math.floor(190.0/columnTitles.length)*256));
		}
		targetSheet.setZoom(zoomLevel);
		
		return headerRow;
	}

	public static void addItemCharDataToPush(CharDescriptionRow row) {
		String itemClass = row.getClass_segment_string().split("&&&")[0];
		CharValuesLoader.active_characteristics.get(itemClass).forEach(c->{
			addItemCharDataToPush(row,itemClass,c);
		});
	}
	public static void addItemCharDataToPush(CharDescriptionRow row, CaracteristicValue val, ClassCaracteristic carac) {
		if(val!=null){

		}else{
			val = new CaracteristicValue();
			val.setParentChar(carac);
		}
		Pair<CaracteristicValue,ClassCaracteristic> valCaracPair = new Pair<CaracteristicValue,ClassCaracteristic>(val,carac);
		Pair<String, Pair<CaracteristicValue,ClassCaracteristic>> queueItem = new Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>(row.getItem_id(),valCaracPair);
		itemDataBuffer.add(queueItem);
		itemRuleBuffer.add(row);
	}

	public static void addItemCharDataToPush(CharDescriptionRow row, String segment, String charId) {
		CaracteristicValue val = null;
		try{
			val = row.getData(segment).get(charId);
		}catch (Exception V){

		}
		Optional<ClassCaracteristic> carac = CharValuesLoader.active_characteristics.get(segment).stream().filter(car->car.getCharacteristic_id().equals(charId)).findAny();
		if(carac.isPresent()){
			addItemCharDataToPush(row,val,carac.get());
		}

	}
	public static void addItemCharDataToPush(CharDescriptionRow row, String segment, ClassCaracteristic carac) {
		CaracteristicValue val = null;
		try{
			val = row.getData(segment).get(carac.getCharacteristic_id());
		}catch (Exception V){

		}
		addItemCharDataToPush(row,val,carac);

	}

	public static void addCaracDefinitionToPush(ClassCaracteristic template,ClassSegment segment){
		Pair<ClassCaracteristic,ClassSegment> carSegPair = new Pair<ClassCaracteristic,ClassSegment>(template,segment);
		caracDefBuffer.add(carSegPair);
	}
	public static void addCaracDefinitionToDisable(ClassCaracteristic template,String segmentId){
		Pair<ClassCaracteristic,String> carSegPair = new Pair<ClassCaracteristic,String>(template,segmentId);
		caracDeleteBuffer.add(carSegPair);
	}

	public static void flushCaracDefinitionToDB(UserAccount account) throws SQLException, ClassNotFoundException {
		Connection conn = Tools.spawn_connection_from_pool();
		stmt = conn.prepareStatement("insert into " + account.getActive_project() + ".project_characteristics values(?,?,?,?,?) on conflict(characteristic_id) do update set characteristic_name=excluded.characteristic_name, characteristic_name_translated=excluded.characteristic_name_translated, isNumeric=excluded.isNumeric, isTranslatable=excluded.isTranslatable");
		//characteristic_id , characteristic_name , characteristic_name_translated , isNumeric , isTranslatable 
		caracDefBuffer.stream().map(Pair::getKey).collect(Collectors.toCollection(HashSet::new)).forEach(car->{
			try {
				stmt.setString(1, car.getCharacteristic_id());
				stmt.setString(2, car.getCharacteristic_name());
				stmt.setString(3, car.getCharacteristic_name_translated());
				stmt.setBoolean(4, car.getIsNumeric());
				stmt.setBoolean(5, car.getIsTranslatable());

				stmt.addBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();

		stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_characteristics_x_segments values(?,?,?,?,?,?,?) on conflict(characteristic_id,segment_id) do update set sequence=excluded.sequence, isCritical=excluded.isCritical, allowedValues=excluded.allowedValues, allowedUoMs=excluded.allowedUoMs, isActive=excluded.isActive");
		//characteristic_id,segment_id,sequence,isCritical,allowedValues[],allowedUoMs[],isActive

		caracDefBuffer.forEach(p->{
				ClassCaracteristic template = p.getKey();
				ClassSegment segment = p.getValue();
				try {
					stmt.setString(1, template.getCharacteristic_id());
					stmt.setString(2, segment.getSegmentId());
					stmt.setInt(3, template.getSequence());
					stmt.setBoolean(4, template.getIsCritical());
					stmt.setArray(5, null);
					try{
						stmt.setArray(6, conn.createArrayOf("VARCHAR", template.getAllowedUoms().toArray(new String[0])));
					}catch(Exception V) {
						stmt.setArray(6, null);
					}
					stmt.setBoolean(7, true);

					stmt.addBatch();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
		caracDefBuffer.clear();
	}

	public static void flushCaracDeleteToDB(UserAccount account) throws SQLException, ClassNotFoundException {
		Connection conn = Tools.spawn_connection_from_pool();
		stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_characteristics_x_segments values(?,?,?,?,?,?,?) on conflict(characteristic_id,segment_id) do update set isActive=excluded.isActive");
		//characteristic_id,segment_id,sequence,isCritical,allowedValues[],allowedUoMs[],isActive

		caracDeleteBuffer.forEach(p->{
			ClassCaracteristic template = p.getKey();
			String segmentID = p.getValue();
			try {
				stmt.setString(1, template.getCharacteristic_id());
				stmt.setString(2, segmentID);
				stmt.setInt(3, template.getSequence());
				stmt.setBoolean(4, template.getIsCritical());
				stmt.setArray(5, null);
				try{
					stmt.setArray(6, conn.createArrayOf("VARCHAR", template.getAllowedUoms().toArray(new String[0])));
				}catch(Exception V) {
					stmt.setArray(6, null);
				}
				stmt.setBoolean(7, false);

				stmt.addBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
		caracDeleteBuffer.clear();
	}
	
	public static void addCaracDefaultDataToPush(String segment, ClassCaracteristic carac, CaracteristicValue val) {
		Pair<CaracteristicValue,ClassCaracteristic> valCaracPair = new Pair<CaracteristicValue,ClassCaracteristic>(val,carac);
		caracDataBuffer.add(valCaracPair);
	}
	
	public static void updateDBCaracValuesInPlace(ArrayList<CaracteristicValue> valuesToUpdate,String active_project) {
		Task<Void> dbFlushTask = new Task<Void>() {
		    
			@Override
		    protected Void call() throws Exception {
				Connection conn = Tools.spawn_connection_from_pool();
				//value_id,text_value_data_language,text_value_user_language,nominal_value,min_value,max_value,note,uom_id
				PreparedStatement stmt = conn.prepareStatement("update "+active_project+".project_values set "
						+ "text_value_data_language = ?,"
						+ "text_value_user_language = ?,"
						+ "nominal_value = ?,"
						+ "min_value = ?,"
						+ "max_value = ?,"
						+ "note = ?,"
						+ "uom_id = ?,manually_reviewed = ? where value_id = ?");
				valuesToUpdate.stream().forEach(val->{	
					try {
						stmt.setString(1, val.getDataLanguageValue());
						stmt.setString(2, val.getUserLanguageValue());
						stmt.setString(3, val.getNominal_value());
						stmt.setString(4, val.getMin_value());
						stmt.setString(5, val.getMax_value());
						stmt.setString(6, val.getNote());
						stmt.setString(7, val.getUom_id());
						stmt.setBoolean(8,val.getManually_Reviewed());
						stmt.setString(9, val.getValue_id());
						
						
						stmt.addBatch();
						
					}catch(Exception V) {
						V.printStackTrace(System.err);
					}
				});
				stmt.execute();
				stmt.clearBatch();
				stmt.close();
				conn.close();
				
				return null;
		    }
		};
		dbFlushTask.setOnSucceeded(e -> {
			
			
			});
		dbFlushTask.setOnFailed(e -> {
		    Throwable problem = dbFlushTask.getException();
		    /* code to execute if task throws exception */
		    problem.printStackTrace(System.err);
		});

		dbFlushTask.setOnCancelled(e -> {
		    /* task was cancelled */
			
		});
		
		Thread dbFlushThread = new Thread(dbFlushTask);; dbFlushThread.setDaemon(true);
		dbFlushThread.setName("CharacDBUpdate");
		dbFlushThread.start();
			
	}
	
	public static void flushCaracDefaultValuesToDB(UserAccount account, String active_project) {
		if(caracDataBuffer.peek()!=null) {
			Task<Void> dbFlushTask = new Task<Void>() {
			    
				@Override
			    protected Void call() throws Exception {
					Connection conn = Tools.spawn_connection_from_pool();
					Connection conn2 = Tools.spawn_connection_from_pool();
					PreparedStatement stmt = conn.prepareStatement("delete from "+active_project+".project_values");
					PreparedStatement stmt2 = conn.prepareStatement("delete from "+active_project+".project_characteristics_x_values");
					stmt.execute();
					stmt2.execute();
					stmt.close();
					stmt2.close();
					
					stmt = conn.prepareStatement("insert into "+active_project+".project_values values(?,?,?,?,?,?,?,?,?)");
					stmt2 = conn2.prepareStatement("insert into "+active_project+".project_characteristics_x_values values (?,?,?,clock_timestamp(),?,?,?)");
					while(caracDataBuffer.peek()!=null) {
						try {
							Pair<CaracteristicValue, ClassCaracteristic> elem = caracDataBuffer.poll();
							CaracteristicValue val = elem.getKey();
							ClassCaracteristic carac = elem.getValue();

							stmt.setString(1, val.getValue_id());
							stmt.setString(2, val.getDataLanguageValue());
							stmt.setString(3, val.getUserLanguageValue());
							stmt.setString(4, val.getNominal_value());
							stmt.setString(5, val.getMin_value());
							stmt.setString(6, val.getMax_value());
							stmt.setString(7, val.getNote());
							stmt.setString(8, val.getUom_id());
							stmt.setBoolean(9,val.getManually_Reviewed());

							stmt.addBatch();
							
							stmt2.setString(1, carac.getCharacteristic_id());
							stmt2.setString(2, account.getUser_id());
							stmt2.setString(3, val.getSource());
							stmt2.setString(4, val.getValue_id());
							stmt2.setString(5, val.getRule_id());
							stmt2.setString(6, val.getUrl());
							
							stmt2.addBatch();
							
							
						}catch(Exception V) {
							V.printStackTrace(System.err);
						}
					}
					stmt.executeBatch();
					stmt2.executeBatch();
					
					stmt.clearBatch();
					stmt.close();
					stmt2.clearBatch();
					stmt2.close();
					
					conn.close();
					conn2.close();
					
					return null;
			    }
			};
			dbFlushTask.setOnSucceeded(e -> {
				
				
				});
			dbFlushTask.setOnFailed(e -> {
			    Throwable problem = dbFlushTask.getException();
			    /* code to execute if task throws exception */
			    problem.printStackTrace(System.err);
			});

			dbFlushTask.setOnCancelled(e -> {
			    /* task was cancelled */
				
			});
			
			Thread dbFlushThread = new Thread(dbFlushTask);; dbFlushThread.setDaemon(true);
			dbFlushThread.setName("CharacDBFlush");
			dbFlushThread.start();
			
		}
		return;
	}

	public static void flushItemDataToDBNoThread(UserAccount account) throws SQLException, ClassNotFoundException {
		Connection conn = Tools.spawn_connection_from_pool();
		Connection conn2 = Tools.spawn_connection_from_pool();
		Connection conn3 = Tools.spawn_connection_from_pool();
		PreparedStatement stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_values values(?,?,?,?,?,?,?,?,?) on conflict(value_id) do update set manually_reviewed = excluded.manually_reviewed");
		PreparedStatement stmt2 = conn2.prepareStatement("insert into "+account.getActive_project()+".project_items_x_values values (?,?,?,?,clock_timestamp(),?,?,?) on conflict (item_id,characteristic_id) do update set user_id = excluded.user_id, description_method = excluded.description_method, description_time=excluded.description_time, value_id = excluded.value_id, description_rule_id=excluded.description_rule_id,url_link = excluded.url_link");
		PreparedStatement stmt3 = conn3.prepareStatement("insert into "+account.getActive_project()+".project_items_x_values_history values (?,?,?,?,clock_timestamp(),?,?,?)");
		while(itemDataBuffer.peek()!=null) {
			try {
				Pair<String, Pair<CaracteristicValue, ClassCaracteristic>> elem = itemDataBuffer.poll();
				String item_id = elem.getKey();
				CaracteristicValue val = elem.getValue().getKey();
				ClassCaracteristic carac = elem.getValue().getValue();

				stmt.setString(1, val.getValue_id());
				stmt.setString(2, val.getDataLanguageValue());
				stmt.setString(3, val.getUserLanguageValue());
				stmt.setString(4, val.getNominal_value());
				stmt.setString(5, val.getMin_value());
				stmt.setString(6, val.getMax_value());
				stmt.setString(7, val.getNote());
				stmt.setString(8, val.getUom_id());
				stmt.setBoolean(9,val.getManually_Reviewed());
				stmt.addBatch();

				stmt2.setString(1, item_id);
				stmt2.setString(2, carac.getCharacteristic_id());
				stmt2.setString(3, account.getUser_id());
				stmt2.setString(4, val.getSource());
				stmt2.setString(5, val.getValue_id());
				stmt2.setString(6, val.getRule_id());
				stmt2.setString(7, val.getUrl());

				stmt2.addBatch();

				stmt3.setString(1, item_id);
				stmt3.setString(2, carac.getCharacteristic_id());
				stmt3.setString(3, account.getUser_id());
				stmt3.setString(4, val.getSource());
				stmt3.setString(5, val.getValue_id());
				stmt3.setString(6, val.getRule_id());
				stmt3.setString(7, val.getUrl());
				stmt3.addBatch();

			}catch(Exception V) {
				V.printStackTrace(System.err);
			}
		}
		try{
			stmt.executeBatch();
			stmt2.executeBatch();
			stmt3.executeBatch();
		}catch (Exception V){
			V.printStackTrace(System.err);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					ExceptionDialog.show("Connection Error","Could not reach server","Item values could not be saved. Please restart");
				}
			});
			throw new RuntimeException(V.getCause());
		}


		stmt.clearBatch();
		stmt.close();
		stmt2.clearBatch();
		stmt2.close();
		stmt3.clearBatch();
		stmt3.close();

		conn.close();
		conn2.close();
		conn3.close();

		conn = Tools.spawn_connection_from_pool();
		stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_items_x_pattern_results values(?,?,?,?) on conflict(item_id,characteristic_id) do update set char_rule_results_json = excluded.char_rule_results_json");
		PreparedStatement finalStmt = stmt;
		CharDescriptionExportServices.itemRuleBuffer = CharDescriptionExportServices.itemRuleBuffer.stream().collect(Collectors.toCollection(HashSet::new)).stream().collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
		int initRuleBufferSize = CharDescriptionExportServices.itemRuleBuffer.size();
		if(GlobalConstants.PUSH_RULE_BUFFER_BY_BLOCK){
			IntStream.range(0,20).forEach(loop->{
				System.out.println("\t Buffer Block "+String.valueOf(loop+1)+"/20");
				int bufferBlockIdx =0;
				while (CharDescriptionExportServices.itemRuleBuffer.peek()!=null && bufferBlockIdx<(1.0*initRuleBufferSize)/20.0){
					CharDescriptionRow row = CharDescriptionExportServices.itemRuleBuffer.poll();
					bufferBlockIdx +=1;
					addItemRuleBufferElement2StatementBatch(row,finalStmt);
				}
				try {
					finalStmt.executeBatch();
					finalStmt.clearBatch();
				}catch (Exception V){
					V.printStackTrace(System.err);
					ExceptionDialog.show("Connection Error","Could not reach server","Rule results could not be saved. Please restart");
				}
			});
		}
		while (CharDescriptionExportServices.itemRuleBuffer.peek()!=null){
			CharDescriptionRow row = CharDescriptionExportServices.itemRuleBuffer.poll();
			addItemRuleBufferElement2StatementBatch(row,stmt);
		}

		stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
	}

	public static void flushItemDataToDBThreaded(UserAccount account, Runnable runTaskOnFailed) {
		if(itemDataBuffer.peek()!=null) {
			Task<Void> dbFlushTask = new Task<Void>() {
			    
				@Override
			    protected Void call() throws Exception {
					flushItemDataToDBNoThread(account);
			    	return null;
			    }
			};
			dbFlushTask.setOnSucceeded(e -> {
				threadedDBFlushActive.set(false);
				});
			dbFlushTask.setOnFailed(e -> {
				threadedDBFlushActive.set(false);
			    Throwable problem = dbFlushTask.getException();
			    /* code to execute if task throws exception */
				problem.printStackTrace(System.err);
				if(runTaskOnFailed!=null){
					System.out.println("PERFORMING SESSION DUMP ON TO LOCAL DISK");
					runTaskOnFailed.run();
				}
				throw new RuntimeException(dbFlushTask.getException());
			});

			dbFlushTask.setOnCancelled(e -> {
			    /* task was cancelled */
				threadedDBFlushActive.set(false);
			});
			
			Thread dbFlushThread = new Thread(dbFlushTask);; dbFlushThread.setDaemon(true);
			dbFlushThread.setName("CharacDBFlush");
			if(!threadedDBFlushActive.getAndSet(true)){
				dbFlushThread.start();
			}else{
				System.out.println("******* QUEUING CARAC DB FLUSH *******");
			}
			
		}
		return;
	}

	private static void addItemRuleBufferElement2StatementBatch(CharDescriptionRow row, PreparedStatement finalStmt) {
		row.getRuleResultsFull().entrySet().forEach(e->{
			try {
				String charId = e.getKey();
				finalStmt.setString(1,row.getItem_id());
				finalStmt.setString(2,charId);
				//finalStmt.setArray(3,finalConn.createArrayOf("bytea",e.getValue().stream().map(CharRuleResult::serialize).toArray(byte[][]::new)));
				finalStmt.setArray(3,null);
				finalStmt.setString(4,ComplexMap2JdbcObject.serialize(e.getValue()));
				//System.out.println(finalStmt.toString());
				finalStmt.addBatch();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		});
	}


	public static void exportDedupReport(Char_description parent, ConcurrentHashMap<String, HashMap<String, DeduplicationServices.ComparisonResult>> fullCompResults, ConcurrentHashMap<String, DeduplicationServices.ComparisonResult> clearedCompResults, HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weightTable, Integer global_min_matches, Integer global_max_mismatches, Double global_mismatch_ratio, ComboBox<ClassSegmentClusterComboRow> sourceCharClassLink, ComboBox<ClassSegmentClusterComboRow> targetCharClassLink, Double topCouplesPercentage, int topCouplesNumber, Duration processingTime, int comparisonsMade, int retained, int ignored, Optional<Double> detailLimitedScore, Optional<Double> coupleLimitedScore) throws SQLException, ClassNotFoundException, IOException {
		File file = openExportFile(parent);
		if(file==null){
			return;
		}

		SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
		Sheet paramSheet = null;
		paramSheet = wb.createSheet("Matching parameters");
		createDudupParamHeader(parent,wb,paramSheet,global_min_matches,global_max_mismatches,global_mismatch_ratio,sourceCharClassLink.getValue().toString(),targetCharClassLink.getValue().toString(),topCouplesNumber,topCouplesPercentage, processingTime, comparisonsMade, retained, ignored, detailLimitedScore, coupleLimitedScore);
		Sheet classSheet = null;
		classSheet = wb.createSheet("Matched class detail");
		fillClassSheet(wb,classSheet,sourceCharClassLink.getValue(),targetCharClassLink.getValue());
		Sheet couplesSheet = null;
		couplesSheet = wb.createSheet("Matching couples");
		createDedupCoupleHeader(wb,couplesSheet);
		Sheet dedupDetailSheet = null;
		dedupDetailSheet = wb.createSheet("Detailed matching results");
		createDedupDetailsHeader(wb,dedupDetailSheet);
		Sheet itemClassSheet = null;
		itemClassSheet = wb.createSheet("Project items");
		createDedupItemClassHeader(wb,itemClassSheet);
		reviewRowIdx = 0;
		for(CharDescriptionRow item:CharItemFetcher.allRowItems) {
			String itemClass = item.getClass_segment_string().split("&&&")[0];
			ArrayList<ClassCaracteristic> itemChars = CharValuesLoader.active_characteristics.get(item.getClass_segment_string().split("&&&")[0]);
			appendDedupItemClassLine(item,itemClassSheet,itemChars,parent);
		}


		fillDedupParamSheet(wb,paramSheet,weightTable.values());
		Sheet finalCouplesSheet = couplesSheet;
		Sheet finalDedupDetailSheet = dedupDetailSheet;
		CharDescriptionExportServices.miscellanousQueue = new ArrayList<>();
		fullCompResults.values().forEach(e->{
			AtomicReference<CharDescriptionRow> itemA = new AtomicReference<CharDescriptionRow>();
			AtomicReference<CharDescriptionRow> itemB = new AtomicReference<CharDescriptionRow>();
			AtomicInteger strongMatches = new AtomicInteger();
			AtomicInteger weakMatches = new AtomicInteger();
			AtomicInteger includedMatches = new AtomicInteger();
			AtomicInteger alternativeMatches = new AtomicInteger();
			AtomicInteger unknownMatches = new AtomicInteger();
			AtomicInteger mismatches = new AtomicInteger();
			AtomicReference<Double> score = new AtomicReference<>(0.0);
			e.values().stream().filter(r->r.getResultType()!=null).forEach(r->{
				itemA.set(r.getItem_A());
				itemB.set(r.getItem_B());
				switch (r.getResultType()){
					case "STRONG_MATCH":
						strongMatches.addAndGet(1);
						break;
					case "WEAK_MATCH":
						weakMatches.addAndGet(1);
						break;
					case "DESCRIPTION_MATCH":
						includedMatches.addAndGet(1);
						break;
					case "ALTERNATIVE_MATCH":
						alternativeMatches.addAndGet(1);
						break;
					case "UNKNOWN_MATCH":
						unknownMatches.addAndGet(1);
						break;
					case "MISMATCH":
						mismatches.addAndGet(1);
						break;
				}
				score.updateAndGet(v -> v + r.getScore());
				
			});
			if(!score.get().equals(e.get("PAIR_SCORE").getScore())){
				throw new RuntimeException();
			}
			queueDedupCouple(itemA.get(),itemB.get(),strongMatches.get(),weakMatches.get(),includedMatches.get(),alternativeMatches.get(),unknownMatches.get(),mismatches.get(),score.get());
		});
		CharDescriptionExportServices.miscellanousQueue.sort(new Comparator<ArrayList<Object>>() {
			@Override
			public int compare(ArrayList<Object> o1, ArrayList<Object> o2) {
				return Double.compare((Double) o2.get(8),(Double) o1.get(8));
			}
		});
		AtomicInteger coupleRank = new AtomicInteger(0);
		AtomicInteger detailsIndx = new AtomicInteger(0);
		CharDescriptionExportServices.miscellanousQueue.forEach(e->{
			if( coupleRank.get()>Math.floor(miscellanousQueue.size()*(topCouplesPercentage/100.0)) ){
				return;
			}
			CharDescriptionRow itemA = (CharDescriptionRow) e.get(0);
			CharDescriptionRow itemB = (CharDescriptionRow) e.get(1);
			int strongMatches = (int) e.get(2);
			int weakMatches = (int) e.get(3);
			int includedMatches = (int) e.get(4);
			int alternativeMatches = (int) e.get(5);
			int unknownMatches = (int) e.get(6);
			int mismatches = (int) e.get(7);
			Double score = (Double) e.get(8);
			appendDedupCouple(finalCouplesSheet, coupleRank.addAndGet(1),itemA,itemB,strongMatches,weakMatches,includedMatches,alternativeMatches,unknownMatches,mismatches,score);
			ArrayList<DeduplicationServices.ComparisonResult> details = fullCompResults.get(itemA.getItem_id() + "<=>" + itemB.getItem_id()).values().stream().collect(Collectors.toCollection(ArrayList::new));
			details.sort(new Comparator<DeduplicationServices.ComparisonResult>() {
				@Override
				public int compare(DeduplicationServices.ComparisonResult o1, DeduplicationServices.ComparisonResult o2) {
					Integer a1=99;
					Integer a2=99;
					Integer b1=99;
					Integer b2=99;
					try{
						a1=o1.getCar_A().getSequence();
					}catch (Exception V){
						if(o1.getVal_A()!=null) a1=0;
					}
					try{
						a2=o2.getCar_A().getSequence();
					}catch (Exception V){
						if(o2.getVal_A()!=null) a2=0;
					}
					try{
						b1=o1.getCar_B().getSequence();
					}catch (Exception V){
						if(o1.getVal_B()!=null) b1=0;
					}
					try{
						b2=o2.getCar_B().getSequence();
					}catch (Exception V){
						if(o2.getVal_B()!=null) b2=0;
					}
					int ret_a = Integer.compare(a1, a2);
					int ret_b = Integer.compare(b1, b2);
					return ret_a*100 + ret_b;
				}
			});
			details.forEach(r->{
				appendDedupDetailLine(finalDedupDetailSheet,detailsIndx.addAndGet(1),r,coupleRank.get());
			});
		});
		miscellanousQueue.clear();
		clearedCompResults.values().forEach(result->{
			result.setResultType("CLEARED_FOR_MEM_MANAGEMENT");
			queueDedupCouple(result, result.getScore());
		});
		CharDescriptionExportServices.miscellanousQueue.sort(new Comparator<ArrayList<Object>>() {
			@Override
			public int compare(ArrayList<Object> o1, ArrayList<Object> o2) {
				return Double.compare((Double) o2.get(8),(Double) o1.get(8));
			}
		});

		miscellanousQueue.forEach(e->{
			CharDescriptionRow itemA = (CharDescriptionRow) e.get(0);
			CharDescriptionRow itemB = (CharDescriptionRow) e.get(1);
			int strongMatches = (int) e.get(2);
			int weakMatches = (int) e.get(3);
			int includedMatches = (int) e.get(4);
			int alternativeMatches = (int) e.get(5);
			int unknownMatches = (int) e.get(6);
			int mismatches = (int) e.get(7);
			Double score = (Double) e.get(8);
			appendDedupCouple(finalCouplesSheet, coupleRank.addAndGet(1),itemA,itemB,strongMatches,weakMatches,includedMatches,alternativeMatches,unknownMatches,mismatches,score);
		});
		//appendDedupDetailLine(finalDedupDetailSheet,detailsIndx.addAndGet(1),r);
		//appendDedupCouple(finalCouplesSheet, couplesIndx.addAndGet(1),itemA.get(),itemB.get(),strongMatches.get(),weakMatches.get(),includedMatches.get(),alternativeMatches.get(),unknownMatches.get(),mismatches.get(),score.get());
		try{
			closeExportFile(file,wb);
		}catch (Exception V){
			ConfirmationDialog.show("Failed", "Results not saved. Close the file you wish to overwrite", "OK");
			exportDedupReport(parent, fullCompResults, clearedCompResults, weightTable, global_min_matches, global_max_mismatches, global_mismatch_ratio, sourceCharClassLink, targetCharClassLink, topCouplesPercentage, topCouplesNumber, processingTime, comparisonsMade, retained, ignored, detailLimitedScore,  coupleLimitedScore);
			//throw new RuntimeException();
		}
	}

	private static void appendDedupItemClassLine(CharDescriptionRow item, Sheet itemClassSheet, ArrayList<ClassCaracteristic> itemChars, Char_description parent) {
		reviewRowIdx+=1;
		Row row = itemClassSheet.createRow(reviewRowIdx);
		Cell loopCell;

		loopCell = row.createCell(0);
		loopCell.setCellValue(item.getClient_item_number());

		loopCell = row.createCell(1);
		try{
			loopCell.setCellValue(item.getShort_desc());
		}catch (Exception V){
			loopCell.setCellValue(item.getShort_desc().substring(0,32766));
		}

		loopCell = row.createCell(2);
		try{
			loopCell.setCellValue(item.getLong_desc());
		}catch (Exception V){
			loopCell.setCellValue(item.getLong_desc().substring(0,32766));
		}

		loopCell = row.createCell(3);
		loopCell.setCellValue(item.getMaterial_group());

		loopCell = row.createCell(4);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[2]);

		loopCell = row.createCell(5);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[1]);

	}

	private static void queueDedupCouple(CharDescriptionRow itemA, CharDescriptionRow itemB, int strong, int weak, int included, int alternative, int unknown, int mismatch, Double score) {
		ArrayList<Object> tmp = new ArrayList<Object>();
		tmp.add(itemA);
		tmp.add(itemB);
		tmp.add(strong);
		tmp.add(weak);
		tmp.add(included);
		tmp.add(alternative);
		tmp.add(unknown);
		tmp.add(mismatch);
		tmp.add(score);
		CharDescriptionExportServices.miscellanousQueue.add(tmp);
	}
	private static void queueDedupCouple(DeduplicationServices.ComparisonResult result, Double score) {
		ArrayList<Object> tmp = new ArrayList<Object>();
		tmp.add(result.getItem_A());
		tmp.add(result.getItem_B());
		tmp.add(result.getAdhocArray().get(0));
		tmp.add(result.getAdhocArray().get(1));
		tmp.add(result.getAdhocArray().get(2));
		tmp.add(result.getAdhocArray().get(3));
		tmp.add(result.getAdhocArray().get(4));
		tmp.add(result.getAdhocArray().get(5));
		tmp.add(score);
		CharDescriptionExportServices.miscellanousQueue.add(tmp);
	}

	private static void fillClassSheet(SXSSFWorkbook wb, Sheet classSheet, ClassSegmentClusterComboRow source, ClassSegmentClusterComboRow target) {
		Row firstRow = classSheet.createRow(0);
		firstRow.createCell(0).setCellValue("Compare items of:");
		firstRow.createCell(3).setCellValue("Compare with items of:");
		createHeaderRow(wb,classSheet, 1, new String[] {
				"Class ID",
				"Class Name",
				"",
				"Class ID",
				"Class Name"}, new IndexedColors[] {
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.WHITE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
		}, new Double[]{}, 100);
		AtomicInteger sourceIdx = new AtomicInteger(1);
		Comparator<Pair<ClassSegment, SimpleBooleanProperty>> classSegmentComparator = new Comparator<Pair<ClassSegment, SimpleBooleanProperty>>() {
			@Override
			public int compare(Pair<ClassSegment, SimpleBooleanProperty> o1, Pair<ClassSegment, SimpleBooleanProperty> o2) {
				return o1.getKey().getClassName().compareTo(o2.getKey().getClassName());
			}
		};
		source.getRowSegments().stream().filter(r->r.getValue().getValue()).sorted(classSegmentComparator).forEach(r->{
			String classID = r.getKey().getClassNumber();
			String className = r.getKey().getClassName();
			try{
				classSheet.getRow(sourceIdx.addAndGet(1)).createCell(0).setCellValue(classID);
			}catch (Exception V){
				classSheet.createRow(sourceIdx.get()).createCell(0).setCellValue(classID);
			}
			classSheet.getRow(sourceIdx.get()).createCell(1).setCellValue(className);
		});
		AtomicInteger targetIdx = new AtomicInteger(1);
		target.getRowSegments().stream().filter(r->r.getValue().getValue()).sorted(classSegmentComparator).forEach(r->{
			String classID = r.getKey().getClassNumber();
			String className = r.getKey().getClassName();
			try{
				classSheet.getRow(targetIdx.addAndGet(1)).createCell(3).setCellValue(classID);
			}catch (Exception V){
				classSheet.createRow(targetIdx.get()).createCell(3).setCellValue(classID);
			}
			classSheet.getRow(targetIdx.get()).createCell(4).setCellValue(className);
		});

	}

	private static void appendDedupDetailLine(Sheet finalDedupDetailSheet, int detailsIndx, DeduplicationServices.ComparisonResult result, int coupleRank) {
		int headerOffset=1;
		if(detailsIndx>=GlobalConstants.EXCEL_MAX_ROW_COUNT){
			return;
		}
		Row row = finalDedupDetailSheet.createRow(detailsIndx+headerOffset);
		int currentCell = 0;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(coupleRank);
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getItem_A().getClient_item_number()+"|"+result.getItem_B().getClient_item_number());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getItem_A().getClient_item_number());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_A().getSequence());
		}catch (Exception V){
			if(result.getVal_A()!=null){
				Cell cell = row.createCell(currentCell);
				cell.setCellValue(0);
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_A().getCharacteristic_id());
		}catch (Exception V){
			if(result.getVal_A()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("CLASS_ID");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_A().getCharacteristic_name());
		}catch (Exception V){
			if(result.getVal_A()!=null){
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("Item Class");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_A().getIsNumeric()?"NUM":"TXT");
		}catch (Exception V){
			if(result.getVal_A()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("TXT");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_A().getStdValue());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(UnitOfMeasure.RunTimeUOMS.get(result.getVal_A().getUom_id()).toString());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_A().getUserLanguageValue());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_A().getMin_value());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_A().getMax_value());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getItem_B().getClient_item_number());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_B().getSequence());
		}catch (Exception V){
			if(result.getVal_B()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue(0);
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_B().getCharacteristic_id());
		}catch (Exception V){
			if(result.getVal_B()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("CLASS_ID");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_B().getCharacteristic_name());
		}catch (Exception V){
			if(result.getVal_B()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("Item Class");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getCar_B().getIsNumeric()?"NUM":"TXT");
		}catch (Exception V){
			if(result.getVal_B()!=null) {
				Cell cell = row.createCell(currentCell);
				cell.setCellValue("TXT");
			}
		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_B().getStdValue());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(UnitOfMeasure.RunTimeUOMS.get(result.getVal_B().getUom_id()).toString());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_B().getUserLanguageValue());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_B().getMin_value());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(result.getVal_B().getMax_value());
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue("");
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(StringUtils.capitalize(result.getResultType().toLowerCase()).replace("_"," "));
		}catch (Exception V){

		}
		currentCell+=1;
		try{
			Cell cell = row.createCell(currentCell);
			cell.setCellValue(Double.valueOf(WordUtils.rewriteNumeric(String.valueOf(result.getScore()))));
		}catch (Exception V){

		}
	}

	private static void appendDedupCouple(Sheet couplesSheet, int couplesRank, CharDescriptionRow itemA, CharDescriptionRow itemB, int strongMatches, int weakMatches, int includedMatches, int alternativeMatches, int unknownMatches, int mismatches, Double score) {
		int headerOffset=0;
		if(couplesRank+headerOffset>=GlobalConstants.EXCEL_MAX_ROW_COUNT){
			return;
		}
		Row row = couplesSheet.createRow(couplesRank+headerOffset);
		Cell cell = row.createCell(0);
		cell.setCellValue(couplesRank);
		cell = row.createCell(1);
		cell.setCellValue(itemA.getClient_item_number()+"|"+itemB.getClient_item_number());
		cell = row.createCell(2);
		cell.setCellValue(itemA.getClient_item_number());
		cell = row.createCell(3);
		cell.setCellValue(itemA.getShort_desc());
		cell = row.createCell(4);
		cell.setCellValue(itemA.getClass_segment(true).getClassNumber());
		cell = row.createCell(5);
		cell.setCellValue(itemA.getClass_segment(true).getClassName());
		cell = row.createCell(6);
		cell.setCellValue(itemB.getClient_item_number());
		cell = row.createCell(7);
		cell.setCellValue(itemB.getShort_desc());
		cell = row.createCell(8);
		cell.setCellValue(itemB.getClass_segment(true).getClassNumber());
		cell = row.createCell(9);
		cell.setCellValue(itemB.getClass_segment(true).getClassName());
		cell = row.createCell(10);
		cell.setCellValue(strongMatches);
		cell = row.createCell(11);
		cell.setCellValue(weakMatches);
		cell = row.createCell(12);
		cell.setCellValue(includedMatches);
		cell = row.createCell(13);
		cell.setCellValue(alternativeMatches);
		cell = row.createCell(14);
		cell.setCellValue(unknownMatches);
		cell = row.createCell(15);
		cell.setCellValue(mismatches);
		cell = row.createCell(16);
		cell.setCellValue(score);
	}

	private static void fillDedupParamSheet(SXSSFWorkbook wb, Sheet paramSheet, Collection<DedupLaunchDialog.DedupLaunchDialogRow> values) {
		AtomicInteger rowIndex = new AtomicInteger(14);
		values.stream().sorted(new Comparator<DedupLaunchDialog.DedupLaunchDialogRow>() {
			@Override
			public int compare(DedupLaunchDialog.DedupLaunchDialogRow o1, DedupLaunchDialog.DedupLaunchDialogRow o2) {
				return Integer.compare(o1.getCarac().getSequence(),o2.getCarac().getSequence());
			}
		}).forEach(p->{
			rowIndex.addAndGet(1);
			Row row = paramSheet.createRow(rowIndex.get());
			Cell cell = row.createCell(0);
			cell.setCellValue(DeduplicationServices.getCarFromWeightTableRowValue(p).getSequence());
			cell = row.createCell(1);
			cell.setCellValue(DeduplicationServices.getCarFromWeightTableRowValue(p).getCharacteristic_id());
			cell = row.createCell(2);
			cell.setCellValue(DeduplicationServices.getCarFromWeightTableRowValue(p).getCharacteristic_name());
			cell = row.createCell(3);
			try{
				cell.setCellValue(DeduplicationServices.getCarFromWeightTableRowValue(p).getIsNumeric()?"NUM":"TXT");
			}catch (Exception V){

			}
			cell = row.createCell(4);
			try{
				cell.setCellValue(DedupLaunchDialog.getUomDisplay(p));
			}catch (Exception V){

			}
			cell = row.createCell(5);
			cell.setCellValue(p.isSameCarac()?"TRUE":"FALSE");
			cell = row.createCell(6);
			cell.setCellValue(p.isAllCarac()?"TRUE":"FALSE");
			cell = row.createCell(7);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("STRONG_MATCH",p));
			cell = row.createCell(8);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("WEAK_MATCH",p));
			cell = row.createCell(9);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("DESCRIPTION_MATCH",p));
			cell = row.createCell(10);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("ALTERNATIVE_MATCH",p));
			cell = row.createCell(11);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("UNKNOWN_MATCH",p));
			cell = row.createCell(12);
			cell.setCellValue(DeduplicationServices.getWeightFromWeightTableRowValue("MISMATCH",p));
		});
	}

	private static void createDedupItemClassHeader(SXSSFWorkbook wb, Sheet itemClassSheet){
		Row reviewHeader = createHeaderRow(wb,itemClassSheet, 0, new String[] {
				"Client Item Number",
				"Short description",
				"Long description",
				"Material Group",
				"Classification number",
				"Classification name"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN
		}, new Double[]{}, 100);
	}

	private static void createDedupDetailsHeader(SXSSFWorkbook wb, Sheet dedupDetailSheet) {
		Row row = dedupDetailSheet.createRow(0);
		Cell cell = row.createCell(2);
		cell.setCellValue("Item 1");
		XSSFCellStyle item1Style = (XSSFCellStyle) wb.createCellStyle();
		item1Style.setAlignment(HorizontalAlignment.LEFT);
		Font item1Font = wb.createFont();
		item1Font.setColor(HSSFColor.HSSFColorPredefined.SEA_GREEN.getIndex());
		item1Font.setBold(true);
		item1Style.setFont(item1Font);
		cell.setCellStyle(item1Style);

		cell = row.createCell(15);
		cell.setCellValue("Item 2");
		XSSFCellStyle item2Style = (XSSFCellStyle) wb.createCellStyle();
		item2Style.setAlignment(HorizontalAlignment.LEFT);
		Font item2Font = wb.createFont();
		item2Font.setColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
		item2Font.setBold(true);
		item2Style.setFont(item2Font);
		cell.setCellStyle(item2Style);

		createHeaderRow(wb,dedupDetailSheet, 1, new String[] {
				"Rank",
				"Couple ID",
				"Client Item Number",
				"Characteristic Sequence",
				"Characteristic ID",
				"Characteristic Name",
				"Characteristic Type",
				"Value",
				"Unit of Measure",
				"Value (translation)",
				"Value (Min)",
				"Value (Max)",
				"Source",
				"Rule",
				"Value status",
				"Client Item Number",
				"Characteristic Sequence",
				"Characteristic ID",
				"Characteristic Name",
				"Characteristic Type",
				"Value",
				"Unit of Measure",
				"Value (translation)",
				"Value (Min)",
				"Value (Max)",
				"Source",
				"Rule",
				"Value status",
				"Characteristic matching result",
				"Characteristic matching score"}, new IndexedColors[] {
				IndexedColors.RED,
				IndexedColors.RED,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.DARK_BLUE,
				IndexedColors.RED,
				IndexedColors.RED
		}, new Double[]{}, 100);

	}

	private static void createDedupCoupleHeader(SXSSFWorkbook wb, Sheet couplesSheet) {
		createHeaderRow(wb,couplesSheet, 0, new String[] {
				"Rank",
				"Couple ID",
				"Client Item Number 1",
				"Short description",
				"Class ID",
				"Class Name",
				"Client Item Number 2",
				"Short description",
				"Class ID",
				"Class Name",
				"# of strong matches",
				"# of weak matches",
				"# of included matches",
				"# of alternative matches",
				"# of unknown matches",
				"# of mismatches",
				"Matching score"}, new IndexedColors[] {
				IndexedColors.RED,
				IndexedColors.RED,
				IndexedColors.SEA_GREEN,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.DARK_BLUE,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.RED,
				IndexedColors.RED,
				IndexedColors.RED,
				IndexedColors.RED,
				IndexedColors.GREY_80_PERCENT
		}, new Double[]{}, 100);
	}

	private static void createDudupParamHeader(Char_description parent, SXSSFWorkbook wb, Sheet paramSheet, Integer global_min_matches, Integer global_max_mismatches, Double global_mismatch_ratio, String sourceString, String targetString, int topCouplesNumber, Double topCouplesPercentage, Duration processingTime, int comparisonsMade, int retained, int ignored, Optional<Double> detailLimitedScore, Optional<Double> coupleLimitedScore) {

		XSSFCellStyle separatorRowStyle = (XSSFCellStyle) wb.createCellStyle();
		separatorRowStyle.setAlignment(HorizontalAlignment.LEFT);
		Font topRowFont = wb.createFont();
		topRowFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		topRowFont.setBold(true);
		separatorRowStyle.setFont(topRowFont);
		separatorRowStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		separatorRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFCellStyle paramNameStyle = (XSSFCellStyle) wb.createCellStyle();
		paramNameStyle.setAlignment(HorizontalAlignment.RIGHT);
		Font parameNameFont = wb.createFont();
		parameNameFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		parameNameFont.setBold(true);
		paramNameStyle.setFont(parameNameFont);

		XSSFCellStyle paramValueStyle = (XSSFCellStyle) wb.createCellStyle();
		paramValueStyle.setAlignment(HorizontalAlignment.LEFT);
		Font paramValuefont = wb.createFont();
		paramValuefont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		paramValuefont.setBold(true);
		paramValueStyle.setFont(paramValuefont);


		paramSheet.createRow(0);
		IntStream.range(0,13).forEach(colIdx->{
			Cell cell = paramSheet.getRow(0).createCell(colIdx);
			cell.setCellStyle((CellStyle) separatorRowStyle.clone());
			switch (colIdx){
				case 0:
					cell.setCellValue(DateTimeFormatter.ofPattern("dd/MM HH:mm")
							.withZone(ZoneId.systemDefault())
							.format(Instant.now()));
					break;
				case 1:
					cell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
					cell.setCellValue("Tool Version");
					break;
				case 2:
					cell.setCellValue(GlobalConstants.TOOL_VERSION);
					break;
				case 6:
					cell.setCellValue(FxUtilTest.getComboBoxValue(parent.classCombo).getClassNumber());
					break;
				case 7:
					cell.setCellValue(FxUtilTest.getComboBoxValue(parent.classCombo).getClassName());
					break;
			}
		});

		paramSheet.createRow(2);
		IntStream.range(0,13).forEach(colIdx->{
			Cell cell = paramSheet.getRow(2).createCell(colIdx);
			cell.setCellStyle((CellStyle) separatorRowStyle.clone());
			if(colIdx<5){
				cell.getCellStyle().setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
			}else{
				cell.getCellStyle().setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
			}
			switch (colIdx){
				case 0:
					cell.setCellValue("General Settings");
					break;
				case 5:
					cell.setCellStyle((CellStyle) separatorRowStyle.clone());
					cell.getCellStyle().setFillPattern(FillPatternType.NO_FILL);
					break;
				case 6:
					cell.setCellValue("Results");
					break;
			}
		});

		int paramSplitRightAtIndx = 7;
		ArrayList<String> params = Arrays.stream((new String[]{
				"Minimum number of matches",
				"Maximum number of mismatches",
				"Maximum mismatch/match ratio",
				"Compare item of:",
				"Compare with items of:",
				"Number of top couples to export",
				"Percent of top couple to export",
				"Processing time",//paramSplitRightAtIndx
				"Total number of comparisons",
				"Total pre-filtered couples",
				"Number of couples visible in \"Matching couples\" tab",
				"Minimum score",
				"Number of couples visible in \"Detailed matching results\" tab",
				"Minimum score"})).collect(Collectors.toCollection(ArrayList::new));
		ArrayList<Object> values = Arrays.stream((new Object[]{
				Double.valueOf(global_min_matches),
				Double.valueOf(global_max_mismatches),
				global_mismatch_ratio,
				sourceString,
				targetString,
				Double.valueOf(topCouplesNumber),
				topCouplesPercentage,
				DurationFormatUtils.formatDuration(processingTime.toMillis(), "HH'h'mm'min'ss's'"),
				Double.valueOf(comparisonsMade),
				Double.valueOf(retained+ignored),
				Math.min(Double.valueOf(retained+ignored),Double.valueOf(GlobalConstants.EXCEL_MAX_ROW_COUNT)),
				coupleLimitedScore.isPresent()?Double.valueOf(coupleLimitedScore.get()):"N/A",
				Double.valueOf(retained),
				detailLimitedScore.isPresent()?Double.valueOf(detailLimitedScore.get()):"N/A"
		})).collect(Collectors.toCollection(ArrayList::new));

		IntStream.range(4,11).forEach(rwIdx->{
			paramSheet.createRow(rwIdx);
			IntStream.range(0,13).forEach(colIdx->{
				Cell cell = paramSheet.getRow(rwIdx).createCell(colIdx);
				if(colIdx%2==0){
					cell.setCellStyle((CellStyle) paramValueStyle.clone());
				}else{
					cell.setCellStyle((CellStyle) paramNameStyle.clone());
				}
				switch (colIdx){
					case 1:
						cell.setCellValue(params.get(rwIdx-4));
						break;
					case 2:
						try{
							cell.setCellValue((String)values.get(rwIdx-4));
						}catch (Exception V){
							cell.setCellValue((Double)values.get(rwIdx-4));
						}
						break;
					case 9:
						cell.setCellValue(params.get(rwIdx-4+paramSplitRightAtIndx));
						break;
					case 10:
						try{
							cell.setCellValue((String)values.get(rwIdx-4+paramSplitRightAtIndx));
						}catch (Exception V){
							cell.setCellValue((Double)values.get(rwIdx-4+paramSplitRightAtIndx));
						}
				}
			});
		});

		paramSheet.createRow(12);
		IntStream.range(0,13).forEach(colIdx->{
			Cell cell = paramSheet.getRow(12).createCell(colIdx);
			cell.setCellStyle((CellStyle) separatorRowStyle.clone());
			cell.getCellStyle().setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			if(colIdx==0){
				cell.setCellValue("Detailed Settings");
			}
		});

		/*
		Row row = paramSheet.createRow(1);
		Cell cell = row.createCell(3);
		cell.setCellValue("Minimum number of matches");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(4);
		cell.setCellValue(global_min_matches);
		cell.setCellStyle(paramValueStyle);
		cell = row.createCell(9);
		cell.setCellValue("Number of retained couples");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(10);
		cell.setCellValue(retained);
		cell.setCellStyle(paramValueStyle);

		row = paramSheet.createRow(2);
		cell = row.createCell(3);
		cell.setCellValue("Maximum number of mismatches");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(4);
		cell.setCellValue(global_max_mismatches);
		cell.setCellStyle(paramValueStyle);
		cell = row.createCell(9);
		cell.setCellValue("Number of ignored couples");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(10);
		cell.setCellValue(ignored);
		cell.setCellStyle(paramValueStyle);

		row = paramSheet.createRow(3);
		cell = row.createCell(3);
		cell.setCellValue("Maximum mismatch/match ratio");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(4);
		cell.setCellValue(global_mismatch_ratio);
		cell.setCellStyle(paramValueStyle);

		row = paramSheet.createRow(4);
		cell = row.createCell(3);
		cell.setCellValue("Compare item of:");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(4);
		cell.setCellValue(sourceString);
		cell.setCellStyle(paramValueStyle);
		cell = row.createCell(9);
		cell.setCellValue("Keep number of retained couples below");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(10);
		cell.setCellValue(GlobalConstants.MAX_DEDUP_PAIRS_IN_MEMORY);
		cell.setCellStyle(paramValueStyle);

		row = paramSheet.createRow(5);
		cell = row.createCell(3);
		cell.setCellValue("Compare with items of:");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(4);
		cell.setCellValue(targetString);
		cell.setCellStyle(paramValueStyle);
		cell = row.createCell(9);
		cell.setCellValue("Tool version");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(10);
		cell.setCellValue(GlobalConstants.TOOL_VERSION);
		cell.setCellStyle(paramValueStyle);

		row = paramSheet.createRow(6);
		cell = row.createCell(3);
		cell.setCellValue("Total number of comparisons:");
		cell.setCellStyle(paramNameStyle);
		cell=row.createCell(4);
		cell.setCellValue(comparisonsMade);
		cell.setCellStyle(paramValueStyle);
		cell = row.createCell(9);
		cell.setCellValue("Date and time");
		cell.setCellStyle(paramNameStyle);
		cell = row.createCell(10);
		DateFormat df = new SimpleDateFormat("dd/MM HH:mm");
		Date dateobj = new Date();
		cell.setCellValue(df.format(dateobj));
		cell.setCellStyle(paramValueStyle);
		*/
		Double[] columnWidths = new Double[]{10.36,16.45,16.45,16.45,8.27,11.09,11.09,17.55,17.55,17.55,17.55,17.55,17.55};
		int zoomLevel = 90;
		createHeaderRow(wb,paramSheet, 14, new String[] {
				"Sequence",
				"Characteristic ID",
				"Characteristic name",
				"Characteristic type",
				"UoM",
				"Same charac",
				"All data",
				"Strong match weight",
				"Weak match weight",
				"Included weight",
				"Alternative weight",
				"Unknown weight",
				"Mismatch weight"}, new IndexedColors[] {
				IndexedColors.BLACK,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.SEA_GREEN,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
		}, columnWidths, zoomLevel);
	}
}
