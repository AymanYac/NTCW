package transversal.data_exchange_toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;

import model.CharDescriptionRow;
import model.ClassCaracteristic;
import model.ClassSegment;
import model.DataInputMethods;
import model.UserAccount;
import service.CharValuesLoader;
import service.TranslationServices;
import transversal.generic.Tools;

public class CharDescriptionImportServices {

	private static String active_project;
	private static PreparedStatement stmt;
	static int projectGranularity;
	public static HashMap<String, ClassSegment> sid2Segment;
	public static HashMap<String, ClassCaracteristic> chid2Carac;
	public static HashMap<String, HashMap<String,ClassCaracteristic>> classSpecificFields;
	public static HashMap<String, CharDescriptionRow> client2Item;
	
	public static void upsertTaxoAndChar(String filePath,String taxoSheetName,String itemDataSheetName, String active_pid, int projectGranularity, UserAccount account) throws FileNotFoundException, ClassNotFoundException, SQLException {
		active_project = active_pid;
		account.setActive_project(active_pid);
		
		InputStream is = new FileInputStream(new File(filePath));
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)
		
		CharDescriptionImportServices.projectGranularity=projectGranularity;
		CharValuesLoader.fetchAllKnownValuesAssociated2Items(active_project);
		
		System.out.println(">>>> Known translation link data language perspective");
		try {
			TranslationServices.Data2UserTermsDico.entrySet().forEach(e->{
				System.out.println(e.getKey()+"<=>"+e.getValue());
			});
		}catch(Exception V) {
			
		}
		
		ImportTaxoRow.setColumnMap();
		ImportTaxoRow.loadTaxoDS();
        Sheet taxoSheet = workbook.getSheet(taxoSheetName);
		Iterator<Row> rows = taxoSheet.rowIterator();
		//Skip the header row
		if(rows.hasNext()) {
			rows.next();
		}
		while(rows.hasNext()) {
			processTaxoRow(rows.next(),account);
		}
		
		System.out.println(">>>> Known translation link data language perspective");
		
		try {
			TranslationServices.Data2UserTermsDico.entrySet().forEach(e->{
				System.out.println(e.getKey()+"<=>"+e.getValue());
			});
		}catch(Exception V) {
			
		}
		
		ImportItemRow.setColumnMap();
		ImportItemRow.loadItemDS();
		Sheet itemDataSheet = workbook.getSheet(itemDataSheetName);
		rows = itemDataSheet.rowIterator();
		//Skip the header row
		if(rows.hasNext()) {
			rows.next();
		}
		while(rows.hasNext()) {
			processItemRow(rows.next(),account);
		}
		
		System.out.println(">>>> Known translation link data language perspective");
		
		try {
			TranslationServices.Data2UserTermsDico.entrySet().forEach(e->{
				System.out.println(e.getKey()+"<=>"+e.getValue());
			});
		}catch(Exception V) {
			
		}
		
		storeTaxo();
		storeChars();
		storeDefaultValues(account);
		
		storeItemsWithClass(account);
		storeItemsData(account);
		
		
		printRejectedTaxoRows();
		
		
		printRejectedItemRows();
		
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
		CharDescriptionExportServices.flushDefaultCaracDataToDB(account,active_project);
	}

	private static void printRejectedItemRows() {
		ImportItemRow.rejectedRows.forEach(p->{
			System.out.println("Error on row "+String.valueOf(p.getKey().getRowNum()+1)+">"+p.getValue());
		});;
	}

	private static void printRejectedTaxoRows() {
		ImportTaxoRow.rejectedRows.forEach(p->{
			System.out.println("Error on row "+String.valueOf(p.getKey().getRowNum()+1)+">"+p.getValue());
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
		Connection conn = Tools.spawn_connection();
		stmt = conn.prepareStatement("delete from "+active_project+".project_segments");
		stmt.execute();
		stmt.close();
		
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

	private static void processTaxoRow(Row current_row, UserAccount account) {
		ImportTaxoRow parsedRow = new ImportTaxoRow();
		parsedRow.parseTaxoRow(current_row,account);
	}
	
	private static void processItemRow(Row current_row, UserAccount account) {
		ImportItemRow parsedRow = new ImportItemRow();
		parsedRow.parseItemRow(current_row,account);
		
	}

	
	

}
