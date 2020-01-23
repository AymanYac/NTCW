package model;

public class UomClassComboRow {

	private UnitOfMeasure uom;

	public UomClassComboRow(UnitOfMeasure uom) {
		this.uom = uom;
	}
	
	public UnitOfMeasure getUnitOfMeasure() {
		return uom;
	}

	@Override
    public String toString() {
        return this.uom.getUom_symbol()+" ("+this.uom.getUom_name()+")";
    }
	
}
