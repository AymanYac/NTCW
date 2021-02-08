package model;


import org.apache.commons.lang3.StringUtils;
import transversal.data_exchange_toolbox.QueryFormater;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnitOfMeasure {
	private String uom_id;
	private ArrayList<String> uom_symbols;
	private String uom_multiplier;
	private String uom_base_id;
	private String uom_name;
	public static HashMap<String, UnitOfMeasure> RunTimeUOMS;
	private static int uom_lookup_max_found_length;
	private static UnitOfMeasure uom_lookup_best_candidate;
	
	
	public String getUom_id() {
		return uom_id;
	}
	public void setUom_id(String uom_id) {
		this.uom_id = uom_id;
	}
	public ArrayList<String> getUom_symbols() {
		return uom_symbols;
	}
	public String getUom_symbol() {
		try {
			return uom_symbols.get(0);
		}catch(Exception V) {
			return null;
		}
		
	}
	public void setUom_symbols(Array uom_symbols) {
		try{
			this.uom_symbols = new ArrayList<String> (Arrays.asList((String[])uom_symbols.getArray()));
		}catch(Exception V) {
			V.printStackTrace(System.err);
			this.uom_symbols=null;
		}
	}
	public void setUom_symbols(String[] uom_symbols) {
		try{
			this.uom_symbols = new ArrayList<String> (Arrays.asList((String[])uom_symbols));
		}catch(Exception V) {
			V.printStackTrace(System.err);
			this.uom_symbols=null;
		}
	}
	
	public BigDecimal getUom_multiplier() {
		return new BigDecimal(uom_multiplier);
	}
	public void setUom_multiplier(String uom_multiplier) {
		this.uom_multiplier = uom_multiplier;
	}
	public String getUom_base_id() {
		return uom_base_id;
	}
	public void setUom_base_id(String uom_base_id) {
		this.uom_base_id = uom_base_id;
	}
	public String getUom_name() {
		return uom_name;
	}
	public void setUom_name(String uom_name) {
		this.uom_name = uom_name;
	}
	
	public boolean HasUomSymbol(String symbol) {
		return getUom_symbols().stream().filter(s->s.trim().toLowerCase().equals(symbol.trim().toLowerCase()))
      		  .collect(Collectors.toList()).size()>0;
	}
	
	public boolean HasPartialUomSymbol(String symbol) {
		return getUom_symbols().stream().filter(s->s.trim().toLowerCase().contains(symbol.trim().toLowerCase()))
      		  .collect(Collectors.toList()).size()>0;
	}
	
	public String toString() {
		return this.getUom_symbol()+" ("+this.getUom_name()+")";
		
	}
	public static  HashMap<String, UnitOfMeasure> fetch_units_of_measures(String language_code) throws SQLException, ClassNotFoundException {
		HashMap<String,UnitOfMeasure> ret = new HashMap<String,UnitOfMeasure>();
		Connection conn = Tools.spawn_connection_from_pool();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		try{
			rs = stmt.executeQuery("select uom_id, uom_symbols, "+QueryFormater.UOM_MULTIPLIER_DOUBLE2CHAR_QUERY("uom_multiplier")+", uom_base_id, uom_name_"+language_code.toLowerCase()+" from public_ressources.units_of_measure");
			while(rs.next()) {
				UnitOfMeasure tmp = new UnitOfMeasure();
				tmp.setUom_id(rs.getString("uom_id"));
				tmp.setUom_name(rs.getString("uom_name_"+language_code.toLowerCase()));
				tmp.setUom_symbols(rs.getArray("uom_symbols"));
				tmp.setUom_multiplier(rs.getString("uom_multiplier"));
				tmp.setUom_base_id(rs.getString("uom_base_id"));
				ret.put(tmp.getUom_id(), tmp);

			}
		} catch (SQLException throwables) {
			rs = stmt.executeQuery("select uom_id, uom_symbols, "+QueryFormater.UOM_MULTIPLIER_DOUBLE2CHAR_QUERY("uom_multiplier")+", uom_base_id, uom_name_"+"en".toLowerCase()+" from public_ressources.units_of_measure");
			while(rs.next()) {
				UnitOfMeasure tmp = new UnitOfMeasure();
				tmp.setUom_id(rs.getString("uom_id"));
				tmp.setUom_name(rs.getString("uom_name_"+"en".toLowerCase()));
				tmp.setUom_symbols(rs.getArray("uom_symbols"));
				tmp.setUom_multiplier(rs.getString("uom_multiplier"));
				tmp.setUom_base_id(rs.getString("uom_base_id"));
				ret.put(tmp.getUom_id(), tmp);

			}
		}

		rs.close();
		stmt.close();
		conn.close();
		
		return ret;
	}
	public static UnitOfMeasure lookUpUomInTextStart(String text) {
		if(text!=null) {
			text=WordUtils.replacePunctuationSplit(text,true);
			if(text.length()==0) {
				return null;
			}
			Unidecode unidecode = Unidecode.toAscii();
			for(String uom_id:RunTimeUOMS.keySet()) {
				UnitOfMeasure uom = RunTimeUOMS.get(uom_id);
				if(text.startsWith(uom.getUom_name())){
					return uom;
				}
				if(unidecode.decodeAndTrim(text).startsWith(unidecode.decodeAndTrim(uom.getUom_name()))) {
					return uom;
				}
				if(unidecode.decodeAndTrim(text).toLowerCase().startsWith(unidecode.decodeAndTrim(uom.getUom_name()).toLowerCase())) {
					return uom;
				}
				
				if(text.startsWith(uom.getUom_symbol())){
					return uom;
				}
				if(unidecode.decodeAndTrim(text).startsWith(unidecode.decodeAndTrim(uom.getUom_symbol()))) {
					return uom;
				}
				if(unidecode.decodeAndTrim(text).toLowerCase().startsWith(unidecode.decodeAndTrim(uom.getUom_symbol()).toLowerCase())) {
					return uom;
				}
				
				
			}
		}
		return null;
	}

	public static UnitOfMeasure lookUpUomInText_SymbolPriority(String text) {
		if(text!=null) {

			Unidecode unidecode = Unidecode.toAscii();
			uom_lookup_max_found_length = 0;
			uom_lookup_best_candidate=null;

			UnitOfMeasure.RunTimeUOMS.entrySet().stream().forEach(e -> {
						if(e.getValue().getUom_symbol().equals(text)){
							uom_lookup_max_found_length = Integer.MAX_VALUE;
							uom_lookup_best_candidate = e.getValue();
						}
						for(String symbol:e.getValue().getUom_symbols()) {
							if(symbol.length()>uom_lookup_max_found_length){
								if(StringUtils.equalsIgnoreCase(unidecode.decodeAndTrim(symbol),unidecode.decodeAndTrim(text))){
									uom_lookup_max_found_length = symbol.length();
									uom_lookup_best_candidate = e.getValue();
								}
							}
						}

					});

			return uom_lookup_best_candidate;

		}
		return null;
	}
	
	public static UnitOfMeasure lookUpUomInText_V2(String text, ArrayList<String> allowedUoms) {
		if(text!=null) {
			
			Unidecode unidecode = Unidecode.toAscii();
			uom_lookup_max_found_length = 0;
			uom_lookup_best_candidate=null;
			
			UnitOfMeasure.RunTimeUOMS.entrySet().stream().filter(
					e->(!(allowedUoms!=null))||ConversionPathExists(e.getValue(),allowedUoms))
					.forEach(e -> {
						String searchedName = unidecode.decodeAndTrim(e.getValue().getUom_name()).toLowerCase();
						if(searchedName.length()>uom_lookup_max_found_length) {
							if(unidecode.decodeAndTrim(text).toLowerCase().startsWith(searchedName)) {
								uom_lookup_max_found_length = searchedName.length();
								uom_lookup_best_candidate = e.getValue();
							}
						}
						for(String symbol:e.getValue().getUom_symbols()) {
							String searchedSymbol = unidecode.decodeAndTrim(symbol).toLowerCase();
							if(searchedSymbol.length()>uom_lookup_max_found_length) {
								if(unidecode.decodeAndTrim(text).toLowerCase().startsWith(searchedSymbol)) {
									uom_lookup_max_found_length = searchedSymbol.length();
									uom_lookup_best_candidate = e.getValue();
								}
							}
						}
						
					});
			
			return uom_lookup_best_candidate;
					
		}
		return null;
	}
	
	public static UnitOfMeasure lookUpUomInText(String text, ArrayList<String> arrayList) {
		if(text!=null) {
			text=WordUtils.replacePunctuationSplit(text,true);
			if(text.length()==0) {
				return null;
			}
			Unidecode unidecode = Unidecode.toAscii();
			for(String uom_id:RunTimeUOMS.keySet()) {
				UnitOfMeasure uom = RunTimeUOMS.get(uom_id);
				if(text.equals(uom.getUom_name())){
					return uom;
				}
				if(unidecode.decodeAndTrim(text).equals(unidecode.decodeAndTrim(uom.getUom_name()))) {
					return uom;
				}
				if(unidecode.decodeAndTrim(text).toLowerCase().equals(unidecode.decodeAndTrim(uom.getUom_name()).toLowerCase())) {
					return uom;
				}
				
				if(text.equals(uom.getUom_symbol())){
					return uom;
				}
				if(unidecode.decodeAndTrim(text).equals(unidecode.decodeAndTrim(uom.getUom_symbol()))) {
					return uom;
				}
				if(unidecode.decodeAndTrim(text).toLowerCase().equals(unidecode.decodeAndTrim(uom.getUom_symbol()).toLowerCase())) {
					return uom;
				}
				
				
			}
		}
		return null;
	}
	
	
	
	public static boolean ConversionPathExists(UnitOfMeasure following_uom, List<String> allowedUoms) {
		if(allowedUoms!=null){
			for(String allowed:allowedUoms) {
				if(UnitOfMeasure.RunTimeUOMS.get(allowed).getUom_base_id().equals(following_uom.getUom_base_id())
						||
						UnitOfMeasure.RunTimeUOMS.get(allowed).getUom_id().equals(following_uom.getUom_base_id())
						||
						UnitOfMeasure.RunTimeUOMS.get(allowed).getUom_base_id().equals(following_uom.getUom_id())
				) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
	public static UnitOfMeasure CheckIfMultiplierIsKnown(double inputMultiplierToBase, String uom_base_id) {
		double min_found = Double.MAX_VALUE;
		UnitOfMeasure candidate = null;
		for(UnitOfMeasure loop_uom:UnitOfMeasure.RunTimeUOMS.values()) {
			if(loop_uom.getUom_base_id().equals(uom_base_id)) {
				if(loop_uom.getUom_multiplier().doubleValue()-inputMultiplierToBase< min_found) {
					min_found = loop_uom.getUom_multiplier().doubleValue()-inputMultiplierToBase;
					
					if( (Math.abs(min_found)/loop_uom.getUom_multiplier().doubleValue())
							<
							GlobalConstants.MAX_UOM_MULTIPLIER_TOLERANCE) {
						
						
						candidate = loop_uom;
					}
					
				}
			}
		}
		return candidate;
	}
	
	@Override
    public boolean equals(Object o) { 
  
        // If the object is compared with itself then return true   
        if (o == this) { 
            return true; 
        } 
  
        /* Check if o is an instance of UnitOfMeasure or not 
          "null instanceof [type]" also returns false */
        if (!(o instanceof UnitOfMeasure)) { 
            return false; 
        } 
          
        // typecast o to UnitOfMeasure so that we can compare data members  
        UnitOfMeasure c = (UnitOfMeasure) o; 
          
        // Compare the data members and return accordingly  
        return this.getUom_id().equals(c.getUom_id());
    }
	
	@Override
	public int hashCode() {
		return getUom_id().hashCode();
	}
	public static void storeNewUom(UnitOfMeasure newUom) {
		RunTimeUOMS.put(newUom.getUom_id(), newUom);
		try{
			Connection conn = Tools.spawn_connection_from_pool();
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO public_ressources.units_of_measure(\n" +
					"\tuom_id, uom_multiplier, uom_base_id, uom_name_en, uom_symbols)\n" +
					"\tVALUES (?, ?, ?, ?, ?);");
			stmt.setString(1,newUom.getUom_id());
			stmt.setDouble(2,newUom.getUom_multiplier().doubleValue());
			stmt.setString(3,newUom.getUom_base_id());
			stmt.setString(4,newUom.getUom_name());
			stmt.setArray(5,conn.createArrayOf("VARCHAR", newUom.getUom_symbols().toArray(new String [0])));
			stmt.execute();
			stmt.close();
			conn.close();
		}catch (Exception V){

		}
	}
}
