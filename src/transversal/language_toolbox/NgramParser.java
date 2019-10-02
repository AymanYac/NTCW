package transversal.language_toolbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.rowset.CachedRowSet;

import model.DescriptionFetchRow;

public class NgramParser {
	
	public HashSet<String> samples = new HashSet<String>();
	public boolean useUpperCase;
	private HashMap<String, ArrayList<DescriptionFetchRow>> DESCS;
	private boolean AlphabetOnly;
	private boolean decode;
	
	/*public static void main(String[] args){
		NgramParser p = new NgramParser("data/fbistest.xml");
		HashSet<String> x = p.parse();
		;
		;
		;
	}*/

	public NgramParser(HashMap<String, ArrayList<DescriptionFetchRow>> dESCS2,boolean alphabetOnlyParameter, boolean decodeParameter){
		this(dESCS2, true,alphabetOnlyParameter,decodeParameter);
	}
	
    public NgramParser(HashMap<String, ArrayList<DescriptionFetchRow>> dESCS2, boolean useUpperCase,boolean alphabetOnlyParameter, boolean decodeParameter){
        this.useUpperCase = useUpperCase;
        this.DESCS=dESCS2;
        this.AlphabetOnly=alphabetOnlyParameter;
        this.decode=decodeParameter;
    }
	
	public HashSet<String> parse() throws SQLException{
		ArrayList<DescriptionFetchRow>  rs = null;
		for(String pid:DESCS.keySet()){
			rs = DESCS.get(pid);
			for(DescriptionFetchRow row:rs) {
				samples.addAll(Arrays.asList(parseTextNode(row.getSd())));
				samples.addAll(Arrays.asList(parseTextNode(row.getLd())));
				
			}
			
		}
		return samples;
	}

	private String[] parseTextNode(String string){
		String text = null;
		
		text = WordUtils.keepalpha(string, AlphabetOnly, decode);
		
		text = text.replaceAll("\\.\\s", "\\.\\. ");
		text = text.trim();
		if(!useUpperCase){
		    text = text.toLowerCase();
		}
		String[] textArray = text.split("\\.\\s");
		return textArray;
	}
}