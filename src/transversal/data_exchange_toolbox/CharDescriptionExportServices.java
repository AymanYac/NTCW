package transversal.data_exchange_toolbox;

import controllers.Char_description;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import model.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import service.CharItemFetcher;
import service.CharValuesLoader;
import service.TranslationServices;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CharDescriptionExportServices {

	private static int reviewRowIdx;
	private static int baseRowIdx;
	private static int taxoRowIdx;
	private static int knownValueRowIdx;

	public static ConcurrentLinkedQueue<Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>> itemDataBuffer = new ConcurrentLinkedQueue<Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>>();
	//public static ConcurrentHashMap<String,CharDescriptionRow> itemRuleBuffer = new ConcurrentHashMap<String,CharDescriptionRow>();
	public static ConcurrentLinkedQueue<CharDescriptionRow> itemRuleBuffer = new ConcurrentLinkedQueue<CharDescriptionRow>();
	public static ConcurrentLinkedQueue<Pair<CaracteristicValue,ClassCaracteristic>> caracDataBuffer = new ConcurrentLinkedQueue<Pair<CaracteristicValue,ClassCaracteristic>>();
	public static ConcurrentLinkedQueue<Pair<ClassCaracteristic,ClassSegment>> caracDefBuffer = new ConcurrentLinkedQueue<Pair<ClassCaracteristic,ClassSegment>>();
	public static ConcurrentLinkedQueue<Pair<ClassCaracteristic,String>> caracDeleteBuffer = new ConcurrentLinkedQueue<Pair<ClassCaracteristic,String>>();

	private static PreparedStatement stmt;

	public static void ExportItemDataForClass(String targetClass, Char_description parent,boolean exportReview, boolean exportBase, boolean exportTaxo, boolean exportKV) throws ClassNotFoundException, SQLException, IOException {
		
		File file = openExportFile(parent);
		
		
        if(file!=null) {
        	
        }else {
        	return;
        }
        
        
		SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
        Sheet reviewSheet = null;
        if(exportReview){
        	reviewSheet = wb.createSheet("Review format");
			createReviewHeader(parent,wb,reviewSheet);
		}
        Sheet baseSheet = null;
        if(exportBase) {
        	baseSheet = wb.createSheet("Database format");
        	createBaseHeader(parent,wb,baseSheet);

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
        	if(exportBase) appendBaseItem(item,baseSheet,itemChars,parent);
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
			createTaxoHeader(taxoSheet,wb,parent);
			taxoRowIdx=0;
			ks.stream().forEach(kse->{
				CharValuesLoader.returnSortedCopyOfClassCharacteristic(kse.getKey()).forEach(carac->{
					appendTaxoRow(kse.getValue(),carac,taxoSheet);
				});
			});
		}

		if(exportKV){
			Sheet knownValueSheet = wb.createSheet("Known values");
			createKnownValuesHeader(knownValueSheet,wb,parent);
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




        
        setColumnWidths(reviewSheet,baseSheet,parent,exportBase,exportReview,exportTaxo,exportKV);
        
        closeExportFile(file,wb);
		
		ConfirmationDialog.show("File saved", "Results successfully saved in\n"+file.getAbsolutePath(), "OK");
		
		
	}

	private static void createReviewHeader(Char_description parent, SXSSFWorkbook wb, Sheet reviewSheet) {
		Row reviewHeader = createHeaderRow(parent,wb,reviewSheet,new String[] {
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
		});
		completeReviewHeaderRow(wb,reviewHeader,
				CharValuesLoader.active_characteristics.values().parallelStream()
						.map(a->a.size()).max(Integer::compare).get());
	}


	private static void createBaseHeader(Char_description parent, SXSSFWorkbook wb, Sheet baseSheet) {
		createHeaderRow(parent,wb,baseSheet,new String[] {
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
		});
	}


	private static void closeExportFile(File file, SXSSFWorkbook wb) throws IOException {
		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
        wb.write(out);
        out.close();

        // dispose of temporary files backing this workbook on disk
        wb.dispose();
        wb.close();
	}

	private static void setColumnWidths(Sheet reviewSheet, Sheet baseSheet, Char_description parent, boolean exportBase, boolean exportReview, boolean exportTaxo, boolean exportKV) {

		if(exportReview){
			reviewSheet.setColumnWidth(0,256*17);
			reviewSheet.setColumnWidth(1,256*50);
			reviewSheet.setColumnWidth(2,256*50);
			reviewSheet.setColumnWidth(3,256*15);
			reviewSheet.setColumnWidth(4,256*25);
			reviewSheet.setColumnWidth(5,256*35);
			for(int i=0;i<CharValuesLoader.active_characteristics.values().parallelStream()
					.map(a->a.size()).max(Integer::compare).get();i++) {
				reviewSheet.setColumnWidth(6+2*i,256*20);
				reviewSheet.setColumnWidth(7+2*i,256*20);
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

	private static void appendBaseItem(CharDescriptionRow item, Sheet baseSheet, ArrayList<ClassCaracteristic> itemChars, Char_description parent) {
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
					loopCell.setCellValue(item.getData(itemClass).get(carac.getCharacteristic_id()).getStdValue());
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
		loopCell.setCellValue(item.getClient_item_number());
		
		loopCell = row.createCell(1);
		loopCell.setCellValue(item.getShort_desc());

		loopCell = row.createCell(2);
		loopCell.setCellValue(item.getLong_desc());
		
		loopCell = row.createCell(3);
		loopCell.setCellValue(item.getMaterial_group());
		
		loopCell = row.createCell(4);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[2]);
		
		loopCell = row.createCell(5);
		loopCell.setCellValue(item.getClass_segment_string().split("&&&")[1]);


		for(int i=0;i<itemChars.size();i++) {
			loopCell = row.createCell(6+2*i);
			loopCell.setCellValue(itemChars.get(i).getCharacteristic_name());
			loopCell = row.createCell(7+2*i);
			try{
				loopCell.setCellValue(item.getData(item.getClass_segment_string().split("&&&")[0]).get(itemChars.get(i).getCharacteristic_id()).getDisplayValue(parent/*,carac*/));
			}catch(Exception V) {

			}
		}



	}

	private static void createKnownValuesHeader(Sheet knownValueSheet, SXSSFWorkbook wb, Char_description parent) {
		createHeaderRow(parent,wb,knownValueSheet,new String[] {
				"Characteristic ID",
				"Characteristic Name",
				"Known value in DL",
				"Known value in UL"}, new IndexedColors[] {
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT,
				IndexedColors.GREY_80_PERCENT});
	}
	private static void createTaxoHeader(Sheet taxoSheet, SXSSFWorkbook wb, Char_description parent){
		createHeaderRow(parent,wb,taxoSheet,new String[] {
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
				IndexedColors.GREY_80_PERCENT});
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
			Cell cell = headerRow.createCell(6+2*i);
			cell.setCellValue("Characteristic name "+String.valueOf(i+1));
    		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
    		 style.setFillForegroundColor(colorArr[Math.floorMod(i, 2)]);
    		 style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    		 Font font = wb.createFont();
    		 font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
    		 font.setBold(true);
    		 style.setFont(font);
    		 cell.setCellStyle(style);
    		 
    		 cell = headerRow.createCell(7+2*i);
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
		Connection conn = Tools.spawn_connection();
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
        return fileChooser.showSaveDialog(parent.exportButton.getScene().getWindow());
		
	}

	private static Row createHeaderRow(Char_description parent, SXSSFWorkbook wb, Sheet targetSheet, String[] columnTitles, IndexedColors[] columnColors) {
		Row headerRow = targetSheet.createRow(0);
		for(int i=0;i<columnTitles.length;i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columnTitles[i]);
    		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
    		 try{
    			 style.setFillForegroundColor(columnColors[i].getIndex());
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
    		 
		}
		
		
		return headerRow;
	}

	public static void addItemCharDataToPush(CharDescriptionRow row, CaracteristicValue val, ClassCaracteristic carac) {
		Pair<CaracteristicValue,ClassCaracteristic> valCaracPair = new Pair<CaracteristicValue,ClassCaracteristic>(val,carac);
		Pair<String, Pair<CaracteristicValue,ClassCaracteristic>> queueItem = new Pair<String, Pair<CaracteristicValue,ClassCaracteristic>>(row.getItem_id(),valCaracPair);
		itemDataBuffer.add(queueItem);
		itemRuleBuffer.add(row);
	}

	public static void addItemCharDataToPush(CharDescriptionRow row, String segment, String charId) {
		CaracteristicValue val = row.getData(segment).get(charId);
		if(val!=null){
			Optional<ClassCaracteristic> carac = CharValuesLoader.active_characteristics.get(segment).stream().filter(car->car.getCharacteristic_id().equals(charId)).findAny();
			if(carac.isPresent()){
				addItemCharDataToPush(row,val,carac.get());
			}
		}

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
		Connection conn = Tools.spawn_connection();
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
		Connection conn = Tools.spawn_connection();
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
				Connection conn = Tools.spawn_connection();
				//value_id,text_value_data_language,text_value_user_language,nominal_value,min_value,max_value,note,uom_id
				PreparedStatement stmt = conn.prepareStatement("update "+active_project+".project_values set "
						+ "text_value_data_language = ?,"
						+ "text_value_user_language = ?,"
						+ "nominal_value = ?,"
						+ "min_value = ?,"
						+ "max_value = ?,"
						+ "note = ?,"
						+ "uom_id = ? where value_id = ?");
				valuesToUpdate.stream().forEach(val->{	
					try {
						stmt.setString(1, val.getDataLanguageValue());
						stmt.setString(2, val.getUserLanguageValue());
						stmt.setString(3, val.getNominal_value());
						stmt.setString(4, val.getMin_value());
						stmt.setString(5, val.getMax_value());
						stmt.setString(6, val.getNote());
						stmt.setString(7, val.getUom_id());
						stmt.setString(8, val.getValue_id());
						
						
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
					Connection conn = Tools.spawn_connection();
					Connection conn2 = Tools.spawn_connection();
					PreparedStatement stmt = conn.prepareStatement("delete from "+active_project+".project_values");
					PreparedStatement stmt2 = conn.prepareStatement("delete from "+active_project+".project_characteristics_x_values");
					stmt.execute();
					stmt2.execute();
					stmt.close();
					stmt2.close();
					
					stmt = conn.prepareStatement("insert into "+active_project+".project_values values(?,?,?,?,?,?,?,?)");
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

	public static void flushItemDataToDB(UserAccount account) {
		if(itemDataBuffer.peek()!=null) {
			Task<Void> dbFlushTask = new Task<Void>() {
			    
				@Override
			    protected Void call() throws Exception {
					Connection conn = Tools.spawn_connection();
					Connection conn2 = Tools.spawn_connection();
					Connection conn3 = Tools.spawn_connection();
					PreparedStatement stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_values values(?,?,?,?,?,?,?,?) on conflict(value_id) do nothing");
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
					stmt.executeBatch();
					stmt2.executeBatch();
					stmt3.executeBatch();
					
					stmt.clearBatch();
					stmt.close();
					stmt2.clearBatch();
					stmt2.close();
					stmt3.clearBatch();
					stmt3.close();
					
					conn.close();
					conn2.close();
					conn3.close();

					conn = Tools.spawn_connection();
					stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_items_x_pattern_results values(?,?,?,?) on conflict(item_id,characteristic_id) do update set char_rule_results = excluded.char_rule_results");
					Connection finalConn = conn;
					PreparedStatement finalStmt = stmt;
					itemRuleBuffer = itemRuleBuffer.stream().collect(Collectors.toCollection(HashSet::new)).stream().collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
					int initRuleBufferSize = itemRuleBuffer.size();
					System.out.println("Rule Buffer contains "+String.valueOf(initRuleBufferSize)+" items");
					IntStream.range(0,20).forEach(loop->{
						System.out.println("\t Buffer Block "+String.valueOf(loop+1)+"/20");
						int bufferBlockIdx =0;
						while (itemRuleBuffer.peek()!=null && bufferBlockIdx<(1.0*initRuleBufferSize)/20.0){
							CharDescriptionRow row = itemRuleBuffer.poll();
							bufferBlockIdx +=1;
							addItemRuleBufferElement2StatementBatch(row,finalStmt);
						}
						try {
							finalStmt.executeBatch();
							finalStmt.clearBatch();
						} catch (SQLException throwables) {

						}
					});
					while (itemRuleBuffer.peek()!=null){
						CharDescriptionRow row = itemRuleBuffer.poll();
						addItemRuleBufferElement2StatementBatch(row,finalStmt);
					}

					stmt.executeBatch();
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
			dbFlushThread.setName("CharacDBFlush");
			dbFlushThread.start();
			
		}
		return;
	}

	private static void addItemRuleBufferElement2StatementBatch(CharDescriptionRow row, PreparedStatement finalStmt) {
		row.getRuleResults().entrySet().forEach(e->{
			try {
				String charId = e.getKey();
				finalStmt.setString(1,row.getItem_id());
				finalStmt.setString(2,charId);
				//finalStmt.setArray(3,finalConn.createArrayOf("bytea",e.getValue().stream().map(CharRuleResult::serialize).toArray(byte[][]::new)));
				finalStmt.setArray(3,null);
				finalStmt.setString(4,ComplexMap2JdbcObject.serialize(e.getValue()));
				finalStmt.addBatch();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		});
	}


}
