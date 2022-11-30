package transversal.file_import;

import com.monitorjbl.xlsx.StreamingReader;
import model.GlobalConstants;
import model.UnitOfMeasure;
import model.UserAccount;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import transversal.generic.CustomKeyboardListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImportPipeLine {

    public static UserAccount account;
    public static Workbook importFile;
    public static HashMap<String, UnitOfMeasure> ImportedUoMs;
    public static ExecutorService pl = Executors.newFixedThreadPool(GlobalConstants.MAX_THREADS);

    public void streamFromFile(String filePath, UserAccount account) throws FileNotFoundException, SQLException, ClassNotFoundException, InterruptedException {
        ImportPipeLine.account=account;
        InputStream is = new FileInputStream(new File(filePath));
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(1000)    // number of rows to keep in memory (defaults to 10)
                .bufferSize(2048)     // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(is);            // InputStream or File for XLSX file (required)*/
        ImportPipeLine.importFile=workbook;
        importUnitsOfMeasure();
        importTaxonomy();
        importArticles();
        importValues();
        importRules();
    }

    private void importUnitsOfMeasure() throws SQLException, ClassNotFoundException {
        ImportPipeLine.ImportedUoMs=UnitOfMeasure.fetch_units_of_measures("en");
    }

    private boolean importTaxonomy() throws InterruptedException {
        Iterator<Row> iterator = ImportPipeLine.importFile.getSheet("Taxonomy").rowIterator();
        TaxonomyPipeline.parseHeader(iterator.next());
        while (iterator.hasNext()){
            Row currentRow = iterator.next();
            ImportPipeLine.pl.execute(TaxonomyPipeline.process(currentRow));
        }
        return pl.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
    }

    private void importArticles() {
    }

    private void importValues() {
    }

    private void importRules() {
    }

    private static class TaxonomyPipeline {

        private static HashMap<String,Integer> columnMap = new HashMap<>();

        public static Runnable process(Row currentRow) {
            return new Runnable() {
                @Override
                public void run() {
                    TaxoRow row = TaxonomyPipeline.parseRow(currentRow);
                }
            };
        }

        private static TaxoRow parseRow(Row currentRow) {
            return new TaxoRow(currentRow);
        }

        public static void parseHeader(Row taxoHeader) {
            taxoHeader.cellIterator().forEachRemaining(cell->{
                switch (cell.getStringCellValue().toLowerCase()){
                    case "domain id":
                        columnMap.put("number_0",cell.getColumnIndex()-1);
                        break;
                    case "domain name":
                        columnMap.put("name_0",cell.getColumnIndex()-1);
                        break;
                    case "group id":
                        columnMap.put("number_1",cell.getColumnIndex()-1);
                        break;
                    case "group name":
                        columnMap.put("name_1",cell.getColumnIndex()-1);
                        break;
                    case "family id":
                        columnMap.put("number_2",cell.getColumnIndex()-1);
                        break;
                    case "family name":
                        columnMap.put("name_2",cell.getColumnIndex()-1);
                        break;
                    case "class id":
                        columnMap.put("number_3",cell.getColumnIndex()-1);
                        break;
                    case "class name":
                        columnMap.put("name_3",cell.getColumnIndex()-1);
                        break;
                    case "Characteristic ID":
                        columnMap.put("charId",cell.getColumnIndex()-1);
                        break;
                    case "Characteristic name":
                        columnMap.put("charName",cell.getColumnIndex()-1);
                        break;
                    case "Characteristic name translation":
                        columnMap.put("charNameTranslated",cell.getColumnIndex()-1);
                        break;
                    case "Sequence":
                        columnMap.put("charSequence",cell.getColumnIndex()-1);
                        break;
                    case "Characteristic type (TXT/NUM)":
                        columnMap.put("charType",cell.getColumnIndex()-1);
                        break;
                    case "Translatable? (Y/N)":
                        columnMap.put("charIsTranslatable",cell.getColumnIndex()-1);
                        break;
                    case "Mandatory? (Y/N)":
                        columnMap.put("charIsMandatory",cell.getColumnIndex()-1);
                        break;
                    case "Unit of measure":
                        columnMap.put("charUoM",cell.getColumnIndex()-1);
                        break;
                    case "Class command":
                        columnMap.put("classCommand",cell.getColumnIndex()-1);
                        break;
                    case "Characteristic command":
                        columnMap.put("charCommand",cell.getColumnIndex()-1);
                        break;
                    case "Class x Characteristic command":
                        columnMap.put("classCharCommand",cell.getColumnIndex()-1);
                        break;
                }
            });
        }

        private static class TaxoRow {

            public TaxoRow(Row current_row) {

            }
        }
    }
}
