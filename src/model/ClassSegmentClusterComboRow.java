package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ClassSegmentClusterComboRow {

    private String rowLabel=null;
    private ArrayList<Pair<ClassSegment, SimpleBooleanProperty>> rowSegments=new ArrayList<Pair<ClassSegment,SimpleBooleanProperty>>();

    public ClassSegmentClusterComboRow(int lvl, ClassSegment itemSegment, HashMap<String, ClassSegment> projectSegments) {
        if(lvl==itemSegment.getSegmentGranularity()-1){
            rowLabel = "This category only";
            rowSegments = new ArrayList<Pair<ClassSegment,SimpleBooleanProperty>>();
            rowSegments.add(new Pair<ClassSegment,SimpleBooleanProperty>(itemSegment,new SimpleBooleanProperty(true)));
        }else{
            rowLabel = itemSegment.getLevelNumber(lvl)+" - "+itemSegment.getLevelName(lvl);
            rowSegments = projectSegments.values().stream().filter(s -> s.getLevelNumber(lvl).equals(itemSegment.getLevelNumber(lvl))).map(s ->new Pair<ClassSegment,SimpleBooleanProperty>(s,new SimpleBooleanProperty(true))).collect(Collectors.toCollection(ArrayList::new));
        }

    }

    public ClassSegmentClusterComboRow(HashMap<String, ClassSegment> projectSegments) {
        rowLabel = "All categories";
        rowSegments = new ArrayList<>(projectSegments.values().stream().map(s->new Pair<ClassSegment,SimpleBooleanProperty>(s,new SimpleBooleanProperty(true))).collect(Collectors.toCollection(ArrayList::new)));
    }

    public ClassSegmentClusterComboRow(HashMap<String, HashSet<ClassSegment>> templateMaps, Pair<ClassSegment, ClassCaracteristic> selectedEntry) {
        ArrayList<Pair<ClassSegment, SimpleBooleanProperty>> templateMappedSegments = templateMaps.get(selectedEntry.getValue().getTemplateSignature()).stream().map(s -> new Pair<ClassSegment, SimpleBooleanProperty>(s, new SimpleBooleanProperty(true))).collect(Collectors.toCollection(ArrayList::new));
        rowSegments = templateMappedSegments;
        rowLabel = "This class"+(templateMappedSegments.size()>1?" and "+String.valueOf(templateMappedSegments.size()-1)+" other category(ies)":"");
    }

    public ClassSegmentClusterComboRow(String label, ArrayList<Pair<ClassSegment, SimpleBooleanProperty>> rowSegments) {
        this.rowLabel = label;
        this.rowSegments = rowSegments;
    }

    @Override
    public String toString() {
        return rowLabel;
    }

    public ArrayList<Pair<ClassSegment,SimpleBooleanProperty>> getRowSegments() {
        return rowSegments;
    }
}
