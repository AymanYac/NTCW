package transversal.data_exchange_toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;

import model.ImportTaxoRow;
import transversal.generic.Tools;

public class CharDescriptionImportServices {

	private static String active_project;
	private static PreparedStatement stmt;

	public static void upsertTaxoAndChar(String filePath,String taxoSheetName,String dataSheetName, String active_pid) throws FileNotFoundException, ClassNotFoundException, SQLException {
		active_project = active_pid;
		InputStream is = new FileInputStream(new File(filePath));
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)
		
		
        Sheet taxoSheet = workbook.getSheet(taxoSheetName);
		Iterator<Row> rows = taxoSheet.rowIterator();
		//Skip the header row
		if(rows.hasNext()) {
			rows.next();
		}
		while(rows.hasNext()) {
			processTaxoRow(rows.next());
		}
		storeTaxo();
		storeChars();
		printRejectedTaxoRows();
	}


	private static void printRejectedTaxoRows() {
		ImportTaxoRow.rejectedRows.forEach(p->{
			System.out.println("Error on row "+String.valueOf(p.getKey().getRowNum()+1)+">"+p.getValue());
		});;
	}

	private static void storeChars() throws ClassNotFoundException, SQLException {
		Connection conn = Tools.spawn_connection();
		stmt = conn.prepareStatement("insert into "+active_project+".project_characteristics values(?,?,?,?,?)");
		//characteristic_id , characteristic_name , characteristic_name_translated , isNumeric , isTranslatable 
		ImportTaxoRow.chid2Carac.values().stream().forEach(car->{
			try {
				System.out.println(car.getCharacteristic_id()+">"+car.getCharacteristic_name());
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
		//stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
	}

	private static void storeTaxo() throws SQLException, ClassNotFoundException {
		Connection conn = Tools.spawn_connection();
		stmt = conn.prepareStatement("delete from "+active_project+".project_segments");
		stmt.execute();
		stmt.close();
		String tmp_insertColumns = "segment_id, ";
  	    String tmp_preparedStatementClause = "?, ";
  	    for(int i = 0; i < ImportTaxoRow.projectGranularity; i++){
  	      tmp_insertColumns += "level_"+String.valueOf(i+1)+"_number,"+"level_"+String.valueOf(i+1)+"_name,"+"level_"+String.valueOf(i+1)+"_name_translated";
  	    tmp_preparedStatementClause += "?,?,?";
  	      if(i <= (ImportTaxoRow.projectGranularity - 2)){
  	        tmp_insertColumns += ", ";
  	        tmp_preparedStatementClause += ", ";
  	      }
  	    }
      
		stmt = conn.prepareStatement(String.format("Insert into "+active_project+".project_segments"+"(%s) values(%s)" , tmp_insertColumns, tmp_preparedStatementClause));
		ImportTaxoRow.sid2Segment.values().forEach(s->{
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
		//stmt.executeBatch();
		stmt.clearBatch();
		stmt.close();
		conn.close();
	}

	private static void processTaxoRow(Row current_row) {
		ImportTaxoRow parsedRow = new ImportTaxoRow();
		parsedRow.parseTaxoRow(current_row);
	}

}
