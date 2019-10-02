package model;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnitOfMeasure {
	private String uom_id;
	private ArrayList<String> uom_symbols;
	private String uom_multiplier;
	private String uom_base_id;
	private String uom_name;
	
	
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
	public void setUom_symbols(Array uom_symbols) throws SQLException {
		try{
			this.uom_symbols = new ArrayList<String> (Arrays.asList((String[])uom_symbols.getArray()));
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
		return this.getUom_name()+" ("+this.getUom_symbol()+") ";
		
	}
}
