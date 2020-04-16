package transversal.data_exchange_toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.monitorjbl.xlsx.StreamingReader;

import javafx.scene.control.ProgressBar;
import model.DataInputMethods;
import model.GenericClassRule;
import model.UserAccount;
import transversal.dialog_toolbox.ExceptionDialog;
import transversal.generic.Tools;

public class SpreadsheetUpload {
	
	
	@SuppressWarnings("resource")
	public static void loadSheetInDatabase(String tableName, String inputFile, LinkedHashMap<String,String> columnMap) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
	        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(inputFile));

	        // Get first sheet from the workbook
	        XSSFSheet sheet = wb.getSheetAt(0);

	        Row row;
	        Cell cell;

	        // Iterate through each rows from first sheet
	        Iterator < Row > rowIterator = sheet.iterator();

	        Boolean parsedHeaders = false;
	        List < String > headers = new ArrayList < String > ();
	        List < String > rowValues = null;
	        
	        
	        Connection conn = Tools.spawn_connection();
	        String query = "";
	        PreparedStatement ps = conn.prepareStatement(query);
	        
	        
	        /*String query = String.format( "CREATE TABLE IF NOT EXISTS %s",tableName);
    	    PreparedStatement ps = conn.prepareStatement(query);
    	    ;
    	    ps.execute();
    	    
    	    ps.close();*/
    	    
    	    
    	    
    	    new ArrayList<ArrayList<String>>();
    	    
	        while (rowIterator.hasNext()) {
	            row = rowIterator.next();
	            if (parsedHeaders) {
	                rowValues = new ArrayList <String> ();
	            }
	            // 
	            // For each row, iterate through each columns
	            Iterator < Cell > cellIterator = row.cellIterator();
	            
	            
	            
	            
	    	    
	    	    
	            while (cellIterator.hasNext()) {
	                cell = cellIterator.next();
	                
	                if(!columnMap.containsKey(CellReference.convertNumToColString(cell.getColumnIndex()))) {
	                	continue;
	                }
	                
	                
	                if (!parsedHeaders) {
	                    headers.add(columnMap.get(CellReference.convertNumToColString(cell.getColumnIndex())));
	                    
	                    
	                    
	                    
	                } else {
	                  try{
	                	  rowValues.add(cell.getStringCellValue());
	                  }catch(Exception e) {
	                	  ExceptionDialog.show("XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex(), "XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex(), "XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex());
	                	  rowValues.add("#ERROR");
	                  }
	                  
	                }
	            }
	            if(!parsedHeaders){
	              parsedHeaders = true;
	              
	                String tmp_insertColumns = tableName.contains("segment")?"segment_id, ":"item_id, ";
		      	    String tmp_preparedStatementClause = "?, ";
		      	    for(int i = 0; i < headers.size(); i++){
		      	      String fieldValue = headers.get(i);
		      	      tmp_insertColumns += fieldValue;
		      	    tmp_preparedStatementClause += "?";
		      	      if(i <= (headers.size() - 2)){
		      	        tmp_insertColumns += ", ";
		      	      tmp_preparedStatementClause += ", ";
		      	      }
		      	    }
	              

	      	    	query = String.format( "Insert into %s(%s) values(%s)" , tableName, tmp_insertColumns, tmp_preparedStatementClause);
	        	    ps = conn.prepareStatement(query);
	        	    
	              
	              
	            }else{
	              
	            ps.setString(1, Tools.generate_uuid());
	            int i;
	      	    for(i =2; i <= headers.size(); i++){
	      	      try{
	      	    	  ps.setString(i, rowValues.get(i-2));
	      	      }catch(Exception V) {
	      	    	  ps.setString(i, "");
	      	      }
	      	    }

	      	    try{
	      	    	  ps.setString(i, rowValues.get(i-2));
	      	      }catch(Exception V) {
	      	    	  ps.setString(i, "");
	      	      }
		      	    ps.addBatch();
		              
	            }
	        }
	        
	        
	            
      	    
	      //#
	    ps.executeBatch();
	    ps.close();
	    conn.close();
	    ;
	}

	
	public static ArrayList<String> streamSheetInDatabase(String tableName, String inputFile, LinkedHashMap<String,String> columnMap, boolean replace, String CIDColumn, Integer granularity, HashSet<String> failedcid, UserAccount account, ProgressBar progressBar) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        
		ArrayList<String> affectedItemIDs = new ArrayList<String>();
		InputStream is = new FileInputStream(new File(inputFile));
		Workbook workbook = StreamingReader.builder()
		        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
		        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
		        .open(is);            // InputStream or File for XLSX file (required)
		
		
        // Get first sheet from the workbook
        Sheet sheet = workbook.getSheetAt(0);
        int number_of_rows = sheet.getLastRowNum();
        Row row;
        Cell cell;

        // Iterate through each rows from first sheet
        Iterator < Row > rowIterator = sheet.iterator();

        Boolean parsedHeaders = false;
        List < String > headers = new ArrayList < String > ();
        List < String > rowValues = null;
        String client_item_column = null;
        for(String colref:columnMap.keySet()) {
        	if(columnMap.get(colref).equals("client_item_number")) {
        		client_item_column = colref;
        	}
        }
        
        Connection conn = Tools.spawn_connection();
        String query = "";
        PreparedStatement ps = null ;
        
        Connection conn2 = Tools.spawn_connection();
	    HashMap<String,String> CID2SEGMENT = new HashMap<String,String>();
	    HashMap<String,String> AID2CAID = new HashMap<String,String>();
	    String taxo_table = tableName.split("\\.")[0]+".project_segments";
	    PreparedStatement ps2 = conn2.prepareStatement("select segment_id,level_"+granularity+"_number from "+taxo_table);
	    ResultSet rs2;
	    if(granularity!=null && granularity>0) {
	    	rs2 = ps2.executeQuery();
	 	    while(rs2.next()) {
	 	    	CID2SEGMENT.put(rs2.getString(2), rs2.getString(1));
	 	    }
	    }
	   
	    
	    String item_table = tableName.split("\\.")[0]+".project_items";
   		ps2 = conn2.prepareStatement("select item_id,client_item_number from "+item_table);
   		rs2 = ps2.executeQuery();
   		while(rs2.next()) {
	    	AID2CAID.put(rs2.getString(2), rs2.getString(1));
	    }
	    
   		rs2.close();
   		
   		
        String classif_table = tableName.split("\\.")[0];
   		ps2 = conn2.prepareStatement("insert into "+classif_table+".project_classification_event"+" (classification_event_id,item_id,segment_id,classification_method,user_id,classification_date,classification_time) values (?,?,?,?,?,?,clock_timestamp())");
   		
   		/*String query = String.format( "CREATE TABLE IF NOT EXISTS %s",tableName);
	    PreparedStatement ps = conn.prepareStatement(query);
	    ;
	    ps.execute();
	    
	    ps.close();*/
	    
	    
	    
	    int current_row = -1;
	    System.out.println("Number of rows in excel "+number_of_rows);
        while (rowIterator.hasNext()) {
        	current_row+=1;
        	try{
        		if(Math.floorMod(current_row, Math.floorDiv(number_of_rows, 20))==0) {
        			progressBar.setProgress(current_row*1.0/number_of_rows*1.0);
        			System.out.println("current row "+current_row);
        			System.out.println(progressBar.getProgress());
        		}
        	}catch(Exception V) {
        		
        	}
            row = rowIterator.next();
            if (parsedHeaders) {
                rowValues = new ArrayList <String> ();
            }
            // 
            // For each row, iterate through each columns
            Iterator < Cell > cellIterator = row.cellIterator();
            
            
            String row_id = null;
            String cid = null;
    	    Boolean replace_cid = false;
    	    
    	    
            while (cellIterator.hasNext()) {
                cell = cellIterator.next();
                
                try {
                	if(client_item_column.equals(CellReference.convertNumToColString(cell.getColumnIndex()))) {
                    	row_id = cell.getStringCellValue();
                    	if(AID2CAID.containsKey(row_id)) {
                    		
                    		;
                        	;
                        	row_id = AID2CAID.get(row_id);
                        	replace_cid = replace?true:false;
                        	;
                        	
                    	}else {
                    		replace_cid = true;
                    		row_id = Tools.generate_uuid();
                    	}
                    }
                	
                }catch(Exception V) {
                	row_id = Tools.generate_uuid();
                }
                
                if(CellReference.convertNumToColString(cell.getColumnIndex()).equals(CIDColumn)) {
                	cid = cell.getStringCellValue();
                	
                }
                
                if(!columnMap.containsKey(CellReference.convertNumToColString(cell.getColumnIndex()))) {
                	continue;
                }
                
                
                if (!parsedHeaders) {
                    headers.add(columnMap.get(CellReference.convertNumToColString(cell.getColumnIndex())));
                    
                    
                    
                    
                } else {
                  try{
                	  rowValues.add(cell.getStringCellValue());
                  }catch(Exception e) {
                	  ExceptionDialog.show("XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex(), "XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex(), "XL001 cell_error "+cell.getColumnIndex()+":"+cell.getRowIndex());
                	  rowValues.add("#ERROR");
                  }
                  
                }
            }
            if(!parsedHeaders){
              parsedHeaders = true;
              
                String tmp_insertColumns = tableName.contains("segment")?"segment_id, ":"item_id, ";
	      	    String tmp_preparedStatementClause = "?, ";
	      	    //String tmp_catchClause = tmp_insertColumns.split(",")[0]+" = "+tmp_preparedStatementClause;
	      	    String tmp_catchClause = "";
	      	    
	      	    for(int i = 0; i < headers.size(); i++){
	      	      String fieldValue = headers.get(i);
	      	      tmp_insertColumns += fieldValue;
	      	      tmp_catchClause +=fieldValue+" = ?";
	      	    tmp_preparedStatementClause += "?";
	      	      if(i <= (headers.size() - 2)){
	      	        tmp_insertColumns += ", ";
	      	        tmp_catchClause+=", ";
	      	      tmp_preparedStatementClause += ", ";
	      	      }
	      	    }
              

      	    	//query = String.format( "Insert into %s(%s) values(%s)" , tableName, tmp_insertColumns, tmp_preparedStatementClause);
	      	    String key = tableName.contains("segment")?"segment_id ":"client_item_number ";
	      	    if(replace) {
	      	    	query = String.format( "Insert into %s(%s) values(%s) on conflict("+key+") do update set %s" , tableName, tmp_insertColumns, tmp_preparedStatementClause,tmp_catchClause);
	      	    	;
	      	    }else {
	      	    	query = String.format( "Insert into %s(%s) values(%s) on conflict("+key+") do nothing" , tableName, tmp_insertColumns, tmp_preparedStatementClause);
	      	    }
      	    	
      	    	ps = conn.prepareStatement(query);
        	    
      	    	
              
            }else{
            
            	
           
           	
            	
            ps.setString(1,row_id);
            affectedItemIDs.add(row_id);
            int i;
      	    for(i =2; i <= headers.size(); i++){
      	      try{
      	    	  ps.setString(i, rowValues.get(i-2));
      	    	  if(replace) {
      	    		  ps.setString(i+headers.size(), rowValues.get(i-2));
      	    	  }
      	      }catch(Exception V) {
      	    	  ps.setString(i, "");
      	    	  if(replace) {
      	    		  ps.setString(i+headers.size(), "");
      	    	  }
      	      }
      	    }

      	    try{
      	    	  ps.setString(i, rowValues.get(i-2));
      	    	  if(replace) {
      	    		  ps.setString(i+headers.size(), rowValues.get(i-2));
      	    	  }
      	      }catch(Exception V) {
      	    	  ps.setString(i, "");
      	    	  if(replace) {
      	    		  ps.setString(i+headers.size(), "");
      	    	  }
      	      }
      	    
      	    
      	    
      	    try {
           		if(replace_cid) {
           			if(CID2SEGMENT.containsKey(cid)) {
           				ps2.setString(1, Tools.generate_uuid());
           				ps2.setString(2, row_id);
           				ps2.setString(3, CID2SEGMENT.get(cid));
           				ps2.setString(4, DataInputMethods.PROJECT_SETUP_UPLOAD);
           				ps2.setString(5, account.getUser_id());
           				ps2.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
           				
           				ps2.addBatch();
           			}else {
           				failedcid.add(cid);
           				//;
           			}
           		}
           	}catch(Exception V) {
           		
           	}
      	    
      	    ps.addBatch();
	              
            }
        }
        
        
            
  	    
      //#
    ps.executeBatch();
    ps2.executeBatch();
    ps.close();
    ps2.close();
    conn.close();
    conn2.close();
    System.out.println("Upserted "+String.valueOf(affectedItemIDs.size())+" items");
    return affectedItemIDs;
}
	
	public static void CopySheetFromDatabase(String source, String target) {
		try {
			Connection conn = Tools.spawn_connection();
			Statement stmt = conn.createStatement();
			//#
			;
			;
			stmt.execute("delete from "+target);
			stmt.execute("insert into "+target+" select * from "+source+"");
			stmt.close();
			conn.close();
			;
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public static ArrayList<GenericClassRule> importClassRules(String source, String target) throws ClassNotFoundException, SQLException {
		
		ArrayList<GenericClassRule> newImportedRules = new ArrayList<GenericClassRule>();
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		stmt.execute("CREATE TEMP table IF NOT EXISTS rules_temp_view as select * from "+source+".project_rules limit 0");
		stmt.execute("delete from rules_temp_view");
		stmt.execute("insert into rules_temp_view select * from "+source+".project_rules where active_status");
		stmt.execute("update rules_temp_view set source_project_id = '"+source+"'");
		ResultSet rs = stmt.executeQuery("insert into "+target+".project_rules select * from rules_temp_view on conflict(rule_id) do nothing returning *;");
		while(rs.next()) {
			GenericClassRule gr = new GenericClassRule();
			gr.setMain(rs.getString("main"));
			gr.setApp(rs.getString("application"));
			gr.setComp(rs.getString("complement"));
			gr.setMg(rs.getString("material_group"));
			gr.setPc(rs.getString("pre_classification"));
			gr.setDwg(rs.getBoolean("drawing"));
			gr.classif=new ArrayList<> ( Arrays.asList( rs.getString("class_id").split("&&&") ) );
			gr.active=rs.getBoolean("active_status");
			gr.setSource_project_id(rs.getString("source_project_id"));
			newImportedRules.add(gr);
		}
		rs.close();
		stmt.execute("drop table rules_temp_view");
		stmt.close();
		conn.close();
		return newImportedRules;
	}


	public static ArrayList<GenericClassRule> getKnownClassificationRules(String pid) throws ClassNotFoundException, SQLException {
		ArrayList<GenericClassRule> knownRules = new ArrayList<GenericClassRule>();
		Connection conn = Tools.spawn_connection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from "+pid+".project_rules where active_status");
		while(rs.next()) {
			GenericClassRule gr = new GenericClassRule();
			gr.setMain(rs.getString("main"));
			gr.setApp(rs.getString("application"));
			gr.setComp(rs.getString("complement"));
			gr.setMg(rs.getString("material_group"));
			gr.setPc(rs.getString("pre_classification"));
			gr.setDwg(rs.getBoolean("drawing"));
			gr.classif=new ArrayList<> ( Arrays.asList( rs.getString("class_id").split("&&&") ) );
			gr.active=rs.getBoolean("active_status");
			gr.setSource_project_id(rs.getString("source_project_id"));
			knownRules.add(gr);
		}
		rs.close();
		stmt.close();
		conn.close();
		return knownRules;
	}
	
}