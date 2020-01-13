package transversal.data_exchange_toolbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import controllers.Char_description;
import javafx.stage.FileChooser;
import model.CharDescriptionRow;
import model.CharPaneRow;
import model.ClassCharacteristic;
import model.UnitOfMeasure;
import service.CharItemFetcher;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

public class CharDescriptionExportServices {

	private static int reviewRowIdx;
	private static int baseRowIdx;
	
	public static void ExportItemDataForClass(String targetClass, Char_description parent) throws ClassNotFoundException, SQLException, IOException {
		
		File file = openExportFile(parent);
		
		
        if(file!=null) {
        	
        }else {
        	return;
        }
        System.out.println("Saving...");
        
		SXSSFWorkbook wb = new SXSSFWorkbook(5000); // keep 5000 rows in memory, exceeding rows will be flushed to disk
        Sheet reviewSheet = wb.createSheet("Review format");
        Sheet baseSheet = wb.createSheet("Database format");
        createHeaderRows(wb,new Sheet[] {reviewSheet,baseSheet},parent);
        reviewRowIdx = 0;
        baseRowIdx = 0;
        int reviewCharCardinality=0;
        for(CharDescriptionRow item:CharItemFetcher.allRowItems) {
        	String itemClass = item.getClass_segment().split("&&&")[0];
        	if(targetClass!=null && !targetClass.equals(itemClass)) {
        		continue;
        	}
        	ArrayList<ClassCharacteristic> itemChars = parent.tableController.active_characteristics.get(item.getClass_segment().split("&&&")[0]);
        	if(itemChars.size()>reviewCharCardinality) {
        		reviewCharCardinality = itemChars.size();
        	}
        	appendReviewItem(item,reviewSheet,itemChars,parent);
        	appendBaseItem(item,baseSheet,itemChars);
        }
        
        
        
        setColumnWidths(reviewSheet,baseSheet,parent);
        
        closeExportFile(file,wb);
		
		ConfirmationDialog.show("File saved", "Results successfully saved in\n"+file.getAbsolutePath(), "OK");
		
		
	}
	

	private static void closeExportFile(File file, SXSSFWorkbook wb) throws IOException {
		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
        wb.write(out);
        out.close();

        // dispose of temporary files backing this workbook on disk
        wb.dispose();
        wb.close();
	}

	private static void setColumnWidths(Sheet reviewSheet, Sheet baseSheet, Char_description parent) {
		
		reviewSheet.setColumnWidth(0,256*17);
		reviewSheet.setColumnWidth(1,256*50);
		reviewSheet.setColumnWidth(2,256*50);
		reviewSheet.setColumnWidth(3,256*15);
		reviewSheet.setColumnWidth(4,256*25);
		reviewSheet.setColumnWidth(5,256*35);
		for(int i=0;i<parent.tableController.active_characteristics.values().parallelStream()
				.map(a->a.size()).max(Integer::compare).get();i++) {
			reviewSheet.setColumnWidth(6+2*i,256*20);
			reviewSheet.setColumnWidth(7+2*i,256*20);
		}
		reviewSheet.setZoom(60);
		reviewSheet.createFreezePane(0, 1);
		
		
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

	private static void appendBaseItem(CharDescriptionRow item, Sheet baseSheet, ArrayList<ClassCharacteristic> itemChars) {
		
		for(int i=0;i<itemChars.size();i++) {
			if(item.getData(item.getClass_segment().split("&&&")[0])!=null) {
				baseRowIdx+=1;
				Row row = baseSheet.createRow(baseRowIdx);
				Cell loopCell = row.createCell(0);
				loopCell.setCellValue(item.getClient_item_number());
				
				loopCell = row.createCell(1);
				loopCell.setCellValue(itemChars.get(i).getSequence());
				
				loopCell = row.createCell(2);
				loopCell.setCellValue(itemChars.get(i).getCharacteristic_id());
				
				loopCell = row.createCell(3);
				loopCell.setCellValue(itemChars.get(i).getCharacteristic_name());
				
				loopCell = row.createCell(4);
				loopCell.setCellValue(
					itemChars.get(i).getIsNumeric()?
				((itemChars.get(i).getAllowedUoms()!=null && itemChars.get(i).getAllowedUoms().size()>0)?"NUM with UoM":"NUM w/o Uom")
				:
				(itemChars.get(i).getIsTranslatable()?"TXT translatable":"TXT non translatable")
					);
				
				String itemClass = item.getClass_segment().split("&&&")[0];
				loopCell = row.createCell(5);
				try{
					loopCell.setCellValue(item.getData(itemClass)[i].getDisplayValue());
				}catch(Exception V) {
					
				}
				
				loopCell = row.createCell(6);
				try {
					loopCell.setCellValue(UnitOfMeasure.RunTimeUOMS.get(item.getData(itemClass)[i].getUom_id()).toString());
				}catch(Exception V) {
					
				}
				
				loopCell = row.createCell(7);
				try{
					loopCell.setCellValue(item.getData(itemClass)[i].getUserLanguageValue());
				}catch(Exception V) {
					
				}
				
				loopCell = row.createCell(8);
				try{
					loopCell.setCellValue(item.getData(itemClass)[i].getMin_value());
				}catch(Exception V) {
					
				}
				
				loopCell = row.createCell(9);
				try{
					loopCell.setCellValue(item.getData(itemClass)[i].getMax_value());
				}catch(Exception V) {
					
				}
				
				loopCell = row.createCell(10);
				try{
					loopCell.setCellValue(item.getData(itemClass)[i].getNote());
				}catch(Exception V) {
					
				}
				
				
			}else {
				continue;
			}
		}
		
	}

	private static void appendReviewItem(CharDescriptionRow item, Sheet reviewSheet, ArrayList<ClassCharacteristic> itemChars, Char_description parent) {
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
		loopCell.setCellValue(item.getClass_segment().split("&&&")[2]);
		
		loopCell = row.createCell(5);
		loopCell.setCellValue(item.getClass_segment().split("&&&")[1]);
		
		for(int i=0;i<itemChars.size();i++) {
			loopCell = row.createCell(6+2*i);
			loopCell.setCellValue(itemChars.get(i).getCharacteristic_name());
			loopCell = row.createCell(7+2*i);
			try{
				CharPaneRow tmp = new CharPaneRow(parent);
				tmp.setCarac(itemChars.get(i));
				tmp.setValue(item.getData(item.getClass_segment().split("&&&")[0])[i]);
				loopCell.setCellValue(WordUtils.textFlowToString(tmp.getValue_display()));
				//loopCell.setCellValue(item.getData(item.getClass_segment().split("&&&")[0])[i].getDisplayValue());
			}catch(Exception V) {
				
			}
		}
		
		
		
	}

	private static void createHeaderRows(SXSSFWorkbook wb, Sheet[] sheets, Char_description parent) {
		
		Sheet reviewSheet = sheets[0];
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
				parent.tableController.active_characteristics.values().parallelStream()
				.map(a->a.size()).max(Integer::compare).get());
		
		
		Sheet baseSheet = sheets[1];
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

	
}
