package transversal.file_import;

import com.monitorjbl.xlsx.StreamingReader;
import model.GlobalConstants;
import model.UnitOfMeasure;
import model.UserAccount;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

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

        private static class TaxoRow {

            public TaxoRow(Row current_row) {

            }
        }
    }
}
