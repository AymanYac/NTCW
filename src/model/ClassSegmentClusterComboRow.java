package model;

import transversal.generic.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ClassSegmentClusterComboRow {

    private String rowLabel=null;
    private ArrayList<ClassSegment> rowSegments=new ArrayList<ClassSegment>();

    public ClassSegmentClusterComboRow(int lvl, ClassSegment itemSegment, HashMap<String, ClassSegment> projectSegments) {
        if(lvl==itemSegment.getSegmentGranularity()-1){
            rowLabel = "This category only";
            rowSegments = new ArrayList<ClassSegment>();
            rowSegments.add(itemSegment);
        }else{
            rowLabel = itemSegment.getLevelName(lvl);
            rowSegments = projectSegments.values().stream().filter(s -> s.getLevelNumber(lvl).equals(itemSegment.getLevelNumber(lvl))).collect(Collectors.toCollection(ArrayList::new));
        }

    }

    public ClassSegmentClusterComboRow(HashMap<String, ClassSegment> projectSegments) {
        rowLabel = "All categories";
        rowSegments = new ArrayList<>(projectSegments.values());
    }

    @Override
    public String toString() {
        return rowLabel;
    }
}
