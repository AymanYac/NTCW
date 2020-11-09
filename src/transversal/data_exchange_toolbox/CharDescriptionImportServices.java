package transversal.data_exchange_toolbox;

import com.monitorjbl.xlsx.StreamingReader;
import controllers.paneControllers.TablePane_CharClassif;
import javafx.util.Pair;
import model.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import service.CharItemFetcher;
import service.CharPatternServices;
import service.CharValuesLoader;
import service.ClassCharacteristicsLoader;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.generic.Tools;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CharDescriptionImportServices {

	private static String active_project;
	private static PreparedStatement stmt;
	static int projectGranularity;
	public static HashMap<String, ClassSegment> sid2Segment;
	public static HashMap<String, ClassCaracteristic> chid2Carac;
	public static HashMap<String, HashMap<String,ClassCaracteristic>> classSpecificFields;
	public static HashMap<String, CharDescriptionRow> client2Item;
	private static int rejected_row_num;
	private static SXSSFWorkbook ReportWorkbook;
	private static ArrayList<Pair<GenericCharRule, ClassCaracteristic>> Patterns2Apply;

	public static void upsertTaxoAndChar(String filePath,String taxoSheetName,String itemDataSheetName, String active_pid, int projectGranularity, UserAccount account) throws IOException, ClassNotFoundException, SQLException, InvalidFormatException {
		active_project = active_pid;
		account.setActive_project(active_pid);
		Row taxoHeader = null;
		Row itemHeader = null;

		InputStream is = new FileInputStream(new File(filePath));
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(1000)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(2048)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)*/
		/*
		File excel = new File(filePath);
		FileInputStream fis = new FileInputStream(excel);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
*/

		CharDescriptionImportServices.projectGranularity=projectGranularity;
		CharValuesLoader.fetchAllKnownValuesAssociated2Items(active_project,true);
		


		ImportTaxoRow.setColumnMap();
		ImportTaxoRow.loadTaxoDS(active_pid);
        Sheet taxoSheet = workbook.getSheet(taxoSheetName);
		Iterator<Row> rows = taxoSheet.rowIterator();
		//Skip the header row
		if(rows.hasNext()) {
			taxoHeader = rows.next();
		}
		Patterns2Apply = new ArrayList<Pair<GenericCharRule,ClassCaracteristic>>();
		while(rows.hasNext()) {
			Pair<GenericCharRule, ClassCaracteristic> pair = processTaxoRow(rows.next(), account);
			if(pair!=null){
				Patterns2Apply.add(pair);
			}
		}

		
		ImportItemRow.setColumnMap();
		ImportItemRow.loadItemDS();
		Sheet itemDataSheet = workbook.getSheet(itemDataSheetName);
		rows = itemDataSheet.rowIterator();
		//Skip the header row
		if(rows.hasNext()) {
			itemHeader = rows.next();
		}
		while(rows.hasNext()) {
			processItemRow(rows.next(),account);
		}

		storeTaxo();
		storeChars();
		storeDefaultValues(account);
		
		storeItemsWithClass(account);
		storeItemsData(account);

		Date instant = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat( "MMMMM_dd" );
		String time = sdf.format( instant );
		String reportFilePath = filePath.replace(".xlsx","_"+time+"_REJECTED.xlsx");
		ReportWorkbook  = new SXSSFWorkbook(5000);
		FileOutputStream out = new FileOutputStream(reportFilePath);

		try{

			printRejectedTaxoRows(ReportWorkbook,taxoSheetName,taxoHeader);
			printRejectedItemRows(ReportWorkbook,itemDataSheetName,itemHeader);
			ReportWorkbook.write(out);
			out.close();

			// dispose of temporary files backing this workbook on disk
			ReportWorkbook.dispose();

			workbook.close();
			is.close();
			ConfirmationDialog.show("Upload success", "Data uploaded. See potential rejected rows in\n"+reportFilePath, "OK", null);

		}catch (Exception V){
			V.printStackTrace(System.err);
			try{
				out.close();
				// dispose of temporary files backing this workbook on disk
				ReportWorkbook.dispose();
			}catch (Exception E){

			}
			workbook.close();
			is.close();
			ConfirmationDialog.show("File saving failed", "Data uploaded but rejected rows could not be saved in\n"+reportFilePath, "OK", null);

		}

		ArrayList<String> patterns2Store = Patterns2Apply.stream().map(p -> p.getKey()).collect(Collectors.toCollection(HashSet::new)).stream().map(r -> r.getCharRuleId()).collect(Collectors.toCollection(ArrayList::new));
		CharPatternServices.suppressGenericRuleInDB(null,account.getActive_project(),patterns2Store,false);

		try{
			CharItemFetcher.classifiedItems = QueryFormater.FETCH_ITEM_CLASSES_WITH_UPLOAD_PRIORITY("",Tools.get_project_granularity(account.getActive_project()),account.getActive_project());
			Tools.get_project_segments(account);
			CharPatternServices.loadDescriptionRules(account.getActive_project());
			CharItemFetcher.fetchAllItems(account.getActive_project(),true);
			ClassCharacteristicsLoader.loadAllClassCharacteristic(account.getActive_project(),true);
			CharItemFetcher.initClassDataFields();
			ClassCharacteristicsLoader.loadKnownValuesAssociated2Items(account.getActive_project(),true);
			HashSet<String> items2Update = new HashSet<String>();
			Patterns2Apply.forEach(p->{
				System.out.println("Applying rule "+p.getKey().getRuleMarker()+" for carac "+p.getValue().getCharacteristic_name());
				items2Update.addAll(CharPatternServices.applyRule(p.getKey(),p.getValue(),account));
			});
			CharItemFetcher.allRowItems.parallelStream().filter(r-> items2Update.contains(r.getItem_id())).forEach(r->{
				r.reEvaluateCharRules();
				String itemClass = r.getClass_segment_string().split("&&&")[0];
				CharValuesLoader.active_characteristics.get(itemClass).forEach(loopCarac->{
					CharDescriptionExportServices.addItemCharDataToPush(r,itemClass,loopCarac.getCharacteristic_id());
				});

			});
			if(items2Update.size()>0){
				ConfirmationDialog.show("Description rules applied", String.valueOf(items2Update.size())+" rule(s) have been applied", "OK", null);
			}
		}catch (Exception V){
			V.printStackTrace(System.err);
		}



	}



	private static void storeItemsData(UserAccount account) {
		CharDescriptionExportServices.flushItemDataToDB(account);
	}




	private static void storeItemsWithClass(UserAccount account) throws SQLException, ClassNotFoundException {
		//Uploading items
		Connection conn = Tools.spawn_connection();
		stmt = conn.prepareStatement("delete from "+account.getActive_project()+".project_items");
		stmt.execute();
		stmt.close();
		//item_id,client_item_number,short_description,long_description,short_description_translated,long_description_translated,material_group,pre_classification
		stmt = conn.prepareStatement("insert into "+account.getActive_project()+".project_items(item_id,client_item_number,short_description,long_description,short_description_translated,long_description_translated,material_group,pre_classification) values (?,?,?,?,?,?,?,?)");
		client2Item.values().stream().forEach(r->{
			try {
				stmt.setString(1, r.getItem_id());
				stmt.setString(2, r.getClient_item_number());
				stmt.setString(3, r.getShort_desc());
				stmt.setString(4, r.getLong_desc());
				stmt.setString(5, r.getShort_desc_translated());
				stmt.setString(6, r.getLong_desc_translated());
				stmt.setString(7, r.getMaterial_group());
				stmt.setString(8, r.getPreclassif());
				
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
		
		//Uploading classes
		Tools.CharDescriptionRow2ClassEvent(client2Item.values().stream().filter(i->i.getClass_segment()!=null).collect(Collectors.toList()), account, DataInputMethods.PROJECT_SETUP_UPLOAD);
	}




	private static void storeDefaultValues(UserAccount account) {
		CharDescriptionExportServices.flushCaracDefaultValuesToDB(account,active_project);
	}

	private static void printRejectedItemRows(Workbook workbook, String sourceSheetName, Row headerRow) {
		rejected_row_num = 0;
		Sheet sheet = workbook.createSheet(sourceSheetName + "_Rejected");
		Row hr = sheet.createRow(rejected_row_num);
		hr.createCell(0).setCellValue("Row number");
		IntStream.range(1,ImportItemRow.columnMap.size()).forEach(idx->{
			hr.createCell(idx).setCellValue(headerRow.getCell(idx-1).getStringCellValue());
		});
		hr.createCell(ImportItemRow.columnMap.size()).setCellValue("Comment");

		ImportItemRow.rejectedRows.forEach(p->{
			rejected_row_num+=1;
			Row reject_row = sheet.createRow(rejected_row_num);
			reject_row.createCell(0).setCellValue(p.getKey().getRowNum());
			IntStream.range(1,ImportItemRow.columnMap.size()).forEach(idx->{
				try{
					reject_row.createCell(idx).setCellValue(p.getKey().getCell(idx-1).getStringCellValue());
				}catch (Exception V){
				}
			});
			reject_row.createCell(ImportItemRow.columnMap.size()).setCellValue(p.getValue());
			//System.out.println("Error on row "+String.valueOf(p.getKey().getRowNum()+1)+">"+p.getValue());
		});;
	}

	private static void printRejectedTaxoRows(Workbook workbook, String sourceSheetName, Row headerRow) {
		rejected_row_num = 0;
		Sheet sheet = workbook.createSheet(sourceSheetName + "_Rejected");
		Row hr = sheet.createRow(rejected_row_num);
		hr.createCell(0).setCellValue("Row number");
		IntStream.range(1,ImportTaxoRow.columnMap.size()).forEach(idx->{
			hr.createCell(idx).setCellValue(headerRow.getCell(idx-1).getStringCellValue());
		});
		hr.createCell(ImportTaxoRow.columnMap.size()).setCellValue("Comment");
		ImportTaxoRow.rejectedRows.forEach(p->{
			rejected_row_num+=1;
			Row reject_row = sheet.createRow(rejected_row_num);
			reject_row.createCell(0).setCellValue(p.getKey().getRowNum());
			IntStream.range(1,ImportTaxoRow.columnMap.size()).forEach(idx->{
				try{
					reject_row.createCell(idx).setCellValue(p.getKey().getCell(idx-1).getStringCellValue());
				}catch (Exception V){
				}
			});
			reject_row.createCell(ImportTaxoRow.columnMap.size()).setCellValue(p.getValue());
			//System.out.println("Error on row "+String.valueOf(p.getKey().getRowNum()+1)+">"+p.getValue());
		});;
	}

	private static void storeChars() throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		
		stmt = conn.prepareStatement("delete from "+active_project+".project_characteristics");
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement("insert into "+active_project+".project_characteristics values(?,?,?,?,?)");
		//characteristic_id , characteristic_name , characteristic_name_translated , isNumeric , isTranslatable 
		CharDescriptionImportServices.chid2Carac.values().stream().forEach(car->{
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
		
		stmt = conn.prepareStatement("delete from "+active_project+".project_characteristics_x_segments");
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement("insert into "+active_project+".project_characteristics_x_segments values(?,?,?,?,?,?,?)");
		//characteristic_id,segment_id,sequence,isCritical,allowedValues[],allowedUoMs[],isActive

		CharDescriptionImportServices.classSpecificFields.entrySet().stream().forEach(chid->{
			chid.getValue().entrySet().stream().forEach(sF->{
				String caracID = chid.getKey();
				String classNumber = sF.getKey();
				try {
					stmt.setString(1, caracID);
					stmt.setString(2, CharDescriptionImportServices.sid2Segment.entrySet().stream().filter(s->s.getValue().getClassNumber().equals(classNumber)).findAny().get().getValue().getSegmentId());
					stmt.setInt(3, sF.getValue().getSequence());
					stmt.setBoolean(4, sF.getValue().getIsCritical());
					stmt.setArray(5, null);
					try{
						stmt.setArray(6, conn.createArrayOf("VARCHAR", sF.getValue().getAllowedUoms().toArray(new String[0])));
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
		});
		stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
		conn.close();
	}

	private static void storeTaxo() throws SQLException, ClassNotFoundException {


		String tmp_insertColumns = "segment_id, ";
  	    String tmp_preparedStatementClause = "?, ";
  	    for(int i = 0; i < CharDescriptionImportServices.projectGranularity; i++){
  	      tmp_insertColumns += "level_"+String.valueOf(i+1)+"_number,"+"level_"+String.valueOf(i+1)+"_name,"+"level_"+String.valueOf(i+1)+"_name_translated";
  	    tmp_preparedStatementClause += "?,?,?";
  	      if(i <= (CharDescriptionImportServices.projectGranularity - 2)){
  	        tmp_insertColumns += ", ";
  	        tmp_preparedStatementClause += ", ";
  	      }
  	    }
		Connection conn = Tools.spawn_connection();
		stmt = conn.prepareStatement("delete from "+active_project+".project_segments");
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement(String.format("Insert into "+active_project+".project_segments"+"(%s) values(%s)" , tmp_insertColumns, tmp_preparedStatementClause));
		CharDescriptionImportServices.sid2Segment.values().forEach(s->{
			try {
				stmt.setString(1, s.getSegmentId());
				IntStream.range(0,s.getSegmentGranularity()).forEach(level->{
					try {
						stmt.setString(1+level*3+1, s.getLevelNumber(level));
						stmt.setString(1+level*3+2, s.getLevelName(level));
						stmt.setString(1+level*3+3, s.getLevelNameTranslated(level));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				
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
	}

	private static Pair<GenericCharRule, ClassCaracteristic> processTaxoRow(Row current_row, UserAccount account) {
		ImportTaxoRow parsedRow = new ImportTaxoRow();
		return parsedRow.parseTaxoRow(current_row,account);
	}
	
	private static void processItemRow(Row current_row, UserAccount account) {
		ImportItemRow parsedRow = new ImportItemRow();
		parsedRow.parseItemRow(current_row,account);
		
	}

	
	

}
