package model;

public class DescriptionFetchRow {
	String auid;
	String aid;
	String sd;
	String ld;
	String cid;
	private String cname;
	private String MG;
	
	
	public String getAuid() {
		return auid;
	}
	public void setAuid(String auid) {
		this.auid = auid;
	}
	
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getSd() {
		return sd;
	}
	public void setSd(String sd) {
		this.sd = sd;
	}
	public String getLd() {
		return ld;
	}
	
	public void setLd(String ld) {
		this.ld = ld;
	}
	public void setCname(String string) {
		this.cname = string;
	}
	public String getCname() {
		return cname;
	}
	public void setMG(String string) {
		this.MG=string;
	}
	public String getMG() {
		return MG;
	}
	
	

}
