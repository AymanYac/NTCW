package model;

public class AdvancedResultRow {
    ClassSegment segment;
    ClassCaracteristic carac;

    public AdvancedResultRow(ClassSegment segment, ClassCaracteristic carac) {
        this.segment = segment;
        this.carac = carac;
    }

    public ClassSegment getSegment() {
        return segment;
    }

    public ClassCaracteristic getCarac() {
        return carac;
    }
}
