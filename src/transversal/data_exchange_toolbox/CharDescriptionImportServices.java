package transversal.data_exchange_toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;

import model.ImportTaxoRow;

public class CharDescriptionImportServices {

	public static void upsertTaxoAndChar(String filePath,String taxoSheetName,String dataSheetName) throws FileNotFoundException {
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
	}

	private static void processTaxoRow(Row current_row) {
		ImportTaxoRow parsedRow = new ImportTaxoRow();
		parsedRow.parseTaxoRow(current_row);
	}

}
