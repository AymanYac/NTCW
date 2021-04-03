package service;

import javafx.util.Pair;
import model.CharDescriptionRow;
import model.ClassCaracteristic;
import model.GlobalConstants;
import model.UnitOfMeasure;
import transversal.language_toolbox.Unidecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeduplicationServices {
    private static ArrayList<String> targetSegmentIDS;
    private static HashMap<String, ArrayList<Object>> weightTable;
    private static ArrayList<CharDescriptionRow> targetItems;
    private static HashMap<String, ArrayList<String>> nameSakeCarIDs;
    private static Unidecode unidec;
    private static double lastprogess;

    public static void scoreDuplicatesForClasses(ArrayList<String> targetSegmentIDS, HashMap<String, ArrayList<Object>> weightTable, Integer GLOBAL_MIN_MATCHES, Integer GLOBAL_MAX_MISMATCHES, Double GLOBAL_MISMATCH_RATIO) {
        lastprogess = 0;
        DeduplicationServices.unidec = Unidecode.toAscii();
        DeduplicationServices.targetSegmentIDS = targetSegmentIDS;
        DeduplicationServices.weightTable = weightTable;
        DeduplicationServices.targetItems = CharItemFetcher.allRowItems.parallelStream().filter(r->targetSegmentIDS.contains(r.getClass_segment_string().split("&&&")[0])).collect(Collectors.toCollection(ArrayList::new));
        DeduplicationServices.nameSakeCarIDs = CharValuesLoader.getNameSakeCarIDs();
        IntStream.range(0,targetItems.size()).forEach(idx_A->{
            IntStream.range(idx_A+1,targetItems.size()).parallel().forEach(idx_B->{
                CharDescriptionRow item_A = targetItems.get(idx_A);
                CharDescriptionRow item_B = targetItems.get(idx_B);
                AtomicReference<Double> score = new AtomicReference<>(0.0);
                AtomicReference<Integer> strongMatches = new AtomicReference<>(0);
                AtomicReference<Integer> weakMatches = new AtomicReference<>(0);
                AtomicReference<Integer> descMatches = new AtomicReference<>(0);
                AtomicReference<Integer> mismatches = new AtomicReference<>(0);
                AtomicInteger remainingCars = new AtomicInteger(weightTable.entrySet().size());
                weightTable.entrySet().forEach(e->{
                    if(!itemPairIsViable(strongMatches,weakMatches,descMatches,mismatches,GLOBAL_MIN_MATCHES,GLOBAL_MAX_MISMATCHES,GLOBAL_MISMATCH_RATIO,remainingCars.getAndAdd(-1))){
                        return;
                    }
                    String carID = e.getKey();
                    ClassCaracteristic car = getCarFromWeightTableRowValue(e.getValue());
                    if(car.getIsNumeric()){
                        Pair<String, String> nomA = getField(item_A, "NOMINAL", car);
                        Pair<String, String> nomB = getField(item_B, "NOMINAL", car);
                        if(approachingScalars(nomA,nomB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                            if(equalScalars(nomA,nomB)){
                                //NOM A vs NOM B : EQUAL
                                Pair<String, String> maxA = getField(item_A, "MAX", car);
                                Pair<String, String> maxB = getField(item_B, "MAX", car);
                                Pair<String, String> minA = getField(item_A, "MIN", car);
                                Pair<String, String> minB = getField(item_B, "MIN", car);

                                if( (minA==null || minB==null || equalScalars(minA,minB))
                                       &&
                                    (maxA==null || maxB==null || equalScalars(maxA,maxB))
                                ){
                                    //NOM A vs NOM B:EQUAL
                                    //MIN A vs MIN B:UNKNOWN or EQUAL
                                    //MAX A vs MAX B:UNKNOWN or EQUAL
                                    strongMatch(score,strongMatches,e.getValue());
                                    return;

                                }
                                if(maxA!=null && maxB!=null){
                                    //NOM A vs NOM B:EQUAL
                                    //MAX A vs MAX B:APPROACHING or DIFFERENT
                                    weakMatch(score,weakMatches,e.getValue());
                                    return;
                                }
                                if(minA!=null && minB!=null){
                                    //NOM A vs NOM B:EQUAL
                                    //MIN A vs MIN B:APPROACHING or DIFFERENT
                                    weakMatch(score,weakMatches,e.getValue());
                                    return;
                                }
                            }else{
                                //NOM A vs NOM B : APPROACHING
                                weakMatch(score,weakMatches,e.getValue());
                                return;
                            }

                        }else{
                            if(nomA!=null && nomB!=null){
                                //NOM A vs NOM B:DIFFERENT
                                mismatch(score,mismatches,e.getValue());
                                return;
                            }else{
                                //NOM A vs NOM B:UNKNOWN
                                Pair<String, String> maxA = getField(item_A, "MAX", car);
                                Pair<String, String> maxB = getField(item_B, "MAX", car);
                                Pair<String, String> minA = getField(item_A, "MIN", car);
                                Pair<String, String> minB = getField(item_B, "MIN", car);
                                if(minA==null || minB ==null){
                                    //NOM A vs NOM B:UNKNOWN
                                    //MIN A vs MIN B:UNKNOWN
                                    if(maxA==null || maxB==null){
                                        //NOM A vs NOM B:UNKNOWN
                                        //MIN A vs MIN B:UNKNOWN
                                        //MAX A vs MAX B:UNKNOWN
                                        if(approachingScalars(nomA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)
                                        ||approachingScalars(nomA,minB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)
                                        ||approachingScalars(nomB,maxA,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)
                                        ||approachingScalars(nomB,minA,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                            weakMatch(score,weakMatches,e.getValue());
                                            return;
                                        }
                                        if(itemDescIncludesNum(item_B,maxA)
                                            ||
                                            itemDescIncludesNum(item_B,minA)
                                            ||
                                            itemDescIncludesNum(item_B,nomA)
                                            ||
                                            itemDescIncludesNum(item_A,maxB)
                                            ||
                                            itemDescIncludesNum(item_A,minB)
                                            ||
                                            itemDescIncludesNum(item_A,nomB)
                                        ){
                                            descMatch(score,descMatches,e.getValue());
                                            return;
                                        }
                                    }else{
                                        //NOM A vs NOM B:UNKNOWN
                                        //MIN A vs MIN B:UNKNOWN
                                        if(approachingScalars(maxA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                            if(equalScalars(maxA,maxB)){
                                                //NOM A vs NOM B:UNKNOWN
                                                //MIN A vs MIN B:UNKNOWN
                                                //MAX A vs MAX B:EQUAL
                                                strongMatch(score,strongMatches,e.getValue());
                                                return;
                                            }else{
                                                //NOM A vs NOM B:UNKNOWN
                                                //MIN A vs MIN B:UNKNOWN
                                                //MAX A vs MAX B:APPROACHING
                                                weakMatch(score,weakMatches,e.getValue());
                                                return;
                                            }
                                        }else{
                                            //NOM A vs NOM B:UNKNOWN
                                            //MIN A vs MIN B:UNKNOWN
                                            //MAX A vs MAX B:DIFFERENT
                                            mismatch(score,mismatches,e.getValue());
                                            return;
                                        }
                                    }
                                }else{
                                    //NOM A vs NOM B:UNKNOWN
                                    if(approachingScalars(minA,minB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                        if(equalScalars(minA,minB)){
                                            if(maxA!=null && maxB!=null){
                                                if(approachingScalars(maxA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                                    if(equalScalars(maxA,maxB)){
                                                        //NOM A vs NOM B:UNKNOWN
                                                        //MIN A vs MIN B:EQUAL
                                                        //MAX A vs MAX B:EQUAL
                                                        strongMatch(score,strongMatches,e.getValue());
                                                        return;
                                                    }else{
                                                        //NOM A vs NOM B:UNKNOWN
                                                        //MIN A vs MIN B:EQUAL
                                                        //MAX A vs MAX B:APPROACHING
                                                        weakMatch(score,weakMatches,e.getValue());
                                                        return;
                                                    }
                                                }else{
                                                    //NOM A vs NOM B:UNKNOWN
                                                    //MIN A vs MIN B:EQUAL
                                                    //MAX A vs MAX B:DIFFERENT
                                                    mismatch(score,mismatches,e.getValue());
                                                    return;
                                                }
                                            }else{
                                                //NOM A vs NOM B:UNKNOWN
                                                //MIN A vs MIN B:EQUAL
                                                //MAX A vs MAX B:UNKNOWN
                                                strongMatch(score,strongMatches,e.getValue());
                                                return;
                                            }
                                        }else{
                                            if(maxA==null || maxB==null || approachingScalars(maxA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                                //NOM A vs NOM B:UNKNOWN
                                                //MIN A vs MIN B:APPROACHING
                                                //MAX A vs MAX B:EQUAL, APPROACHING or UNKNOWN
                                                weakMatch(score,weakMatches,e.getValue());
                                                return;
                                            }else{
                                                //NOM A vs NOM B:UNKNOWN
                                                //MIN A vs MIN B:APPROACHING
                                                //MAX A vs MAX B:DIFFERENT
                                                mismatch(score,mismatches,e.getValue());
                                                return;
                                            }
                                        }
                                    }else{
                                        mismatch(score,mismatches,e.getValue());
                                        return;
                                    }
                                }
                            }
                        }
                    }else{
                        //CAR IS TXT
                        Pair<String, String> DLa = getField(item_A, "DL", car);
                        Pair<String, String> DLb = getField(item_A, "DL", car);
                        String valA = null;
                        try{
                            valA = DLa.getKey();
                        }catch (Exception V){

                        }
                        if(valA!=null){
                            valA = unidec.decodeAndTrim(valA).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
                            String valB = unidec.decodeAndTrim(DLb.getKey()).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
                            if(valA.equals(valB)){
                                strongMatch(score,strongMatches,e.getValue());
                                return;
                            }
                            if(valB.contains(valA)){
                                weakMatch(score,weakMatches,e.getValue());
                                return;
                            }
                        }
                        String valB = null;
                        try{
                            valB = DLb.getKey();
                        }catch (Exception V){

                        }
                        if(valB!=null){
                            valB = unidec.decodeAndTrim(valB).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
                            valA = unidec.decodeAndTrim(DLa.getKey()).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
                            if(valB.equals(valA)){
                                strongMatch(score,strongMatches,e.getValue());
                                return;
                            }
                            if(valA.contains(valB)){
                                weakMatch(score,weakMatches,e.getValue());
                                return;
                            }
                        }
                        if(itemDescIncludesTXT(item_A,valA) || itemDescIncludesTXT(item_A,valB)){
                            weakMatch(score,weakMatches,e.getValue());
                            return;
                        }
                    }
                });
                if(itemPairIsViable(strongMatches,weakMatches,descMatches,mismatches,GLOBAL_MIN_MATCHES,GLOBAL_MAX_MISMATCHES,GLOBAL_MISMATCH_RATIO,0)){
                    /*System.out.println("XXXXXXXXXXXX "+item_A.getClient_item_number()+" XXXXXXXXXXXX "+item_B.getClient_item_number()+" XXXXXXXXXXXX");
                    System.out.println(item_A.getClient_item_number() +">"+ item_A.getAccentFreeDescriptionsNoCR());
                    System.out.println(item_B.getClient_item_number() +">"+ item_B.getAccentFreeDescriptionsNoCR());
                    System.out.println("\t"+"strong matches> "+String.valueOf(strongMatches.get()));
                    System.out.println("\t"+"weak matches> "+String.valueOf(weakMatches.get()));
                    System.out.println("\t"+"desc matches> "+String.valueOf(descMatches.get()));
                    System.out.println("\t"+"total matches> "+String.valueOf(strongMatches.get()+weakMatches.get()+descMatches.get()));
                    System.out.println("\t"+"total mismatches> "+String.valueOf(mismatches.get()));*/
                }
            });
            if(Math.random()<0.01){
                double progress = (idx_A * 100.0) / targetItems.size();
                if(progress-lastprogess>2){
                    System.out.println(progress+"%");
                }
                lastprogess = progress;
            }
        });
    }

    private static boolean itemPairIsViable(AtomicReference<Integer> strongMatches, AtomicReference<Integer> weakMatches, AtomicReference<Integer> descMatches, AtomicReference<Integer> mismatches, Integer global_min_matches, Integer global_max_mismatches, Double global_max_ratio, int remainingUncheckedCars) {
        int totalMatches = strongMatches.get() + weakMatches.get() + descMatches.get();
        int hopeMatches = totalMatches + remainingUncheckedCars;
        Integer totalMismatches = mismatches.get();
        double hopeMismatchRatio = (totalMismatches*1.0)/((hopeMatches != 0 ? hopeMatches*1.0 : 1.0));
        if(totalMismatches>global_max_mismatches){
            return false;
        }
        if(hopeMatches<global_min_matches){
            return false;
        }
        if(hopeMismatchRatio>global_max_ratio){
            return false;
        }
        return true;
    }

    private static boolean itemDescIncludesTXT(CharDescriptionRow item_a, String valA) {
        return false;
    }


    private static boolean itemDescIncludesNum(CharDescriptionRow item_b, Pair<String, String> maxA) {
        return false;
    }

    private static void mismatch(AtomicReference<Double> score, AtomicReference<Integer> mismatches, ArrayList<Object> weights) {
        score.updateAndGet(v -> v + getWeightFromTableRowValue("MISMATCH", weights));
        mismatches.updateAndGet(v->v+1);
    }
    private static void descMatch(AtomicReference<Double> score, AtomicReference<Integer> descMatches, ArrayList<Object> weights) {
        score.updateAndGet(v -> v + getWeightFromTableRowValue("INCLUDED", weights));
        descMatches.updateAndGet(v->v+1);
    }

    private static void weakMatch(AtomicReference<Double> score, AtomicReference<Integer> weakMatches, ArrayList<Object> weights) {
        score.updateAndGet(v -> v + getWeightFromTableRowValue("WEAK",weights));
        weakMatches.updateAndGet(v->v+1);
    }

    private static void strongMatch(AtomicReference<Double> score, AtomicReference<Integer> strongMatches, ArrayList<Object> weights) {
        score.updateAndGet(v -> v + getWeightFromTableRowValue("STRONG",weights));
        strongMatches.updateAndGet(v->v+1);
    }

    private static boolean approachingScalars(Pair<String, String> pairA, Pair<String, String> pairB, double threshold) {
        pairB = UnitOfMeasure.convertToUOM(pairB,pairA);
        if(pairA!=null && pairB!=null){
            String scalarA = pairA.getKey();
            String scalarB = pairB.getKey();
            if(scalarA!=null && scalarB!=null){
                try{
                    return Math.abs(Double.parseDouble(scalarA) - Double.parseDouble(scalarB))<=threshold*Math.abs(Math.max(Double.parseDouble(scalarA),Double.parseDouble(scalarB)));
                }catch (Exception V){
                    return false;
                }
            }
            return false;
        }
        return false;
    }
    private static boolean equalScalars(Pair<String, String> pairA, Pair<String, String> pairB) {
        pairB = UnitOfMeasure.convertToUOM(pairB,pairA);
        if(pairA!=null && pairB!=null){
            String scalarA = pairA.getKey();
            String scalarB = pairB.getKey();
            if(scalarA!=null && scalarB!=null){
                try{
                    return Double.parseDouble(scalarA) == Double.parseDouble(scalarB);
                }catch (Exception V){
                    return false;
                }
            }
            return false;
        }
        return false;
    }


    private static Pair<String, String> getField(CharDescriptionRow targetItem, String FIELD_NAME, ClassCaracteristic targetCar) {
        if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
            Optional<Pair<String, String>> tmp = targetItem.getData(targetItem.getClass_segment_string().split("&&&")[0]).entrySet().stream()
                    .filter(e -> nameSakeCarIDs.get(targetCar.getCharacteristic_name()).contains(e.getKey()))
                    .filter(e -> e.getValue() != null && e.getValue().getField(FIELD_NAME) != null && e.getValue().getField(FIELD_NAME).length() > 0)
                    .map(e -> new Pair<String, String>(e.getValue().getField(FIELD_NAME), e.getValue().getField("UOM_ID"))).findAny();
            if(tmp.isPresent()){
                return tmp.get();
            }
        }else{
            Optional<Pair<String, String>> tmp = targetItem.getData(targetItem.getClass_segment_string().split("&&&")[0]).entrySet().stream()
                    .filter(e -> e.getKey().equals(targetCar.getCharacteristic_id()))
                    .filter(e -> e.getValue() != null && e.getValue().getField(FIELD_NAME) != null && e.getValue().getField(FIELD_NAME).length() > 0)
                    .map(e -> new Pair<String, String>(e.getValue().getField(FIELD_NAME), e.getValue().getField("UOM_ID"))).findAny();
            if(tmp.isPresent()){
                return tmp.get();
            }
        }
        return null;
    }

    private static ClassCaracteristic getCarFromWeightTableRowValue(ArrayList<Object> value) {
        return (ClassCaracteristic) value.get(0);
    }
    private static Double getWeightFromTableRowValue(String MATCH_TYPE, ArrayList<Object> value) {
        if(MATCH_TYPE.equals("STRONG")){
            return Double.parseDouble((String) value.get(1));
        }else if(MATCH_TYPE.equals("WEAK")){
            return Double.parseDouble((String) value.get(2));
        }else if(MATCH_TYPE.equals("INCLUDED")){
            return Double.parseDouble((String) value.get(3));
        }else if(MATCH_TYPE.equals("MISMATCH")){
            return Double.parseDouble((String) value.get(4));
        }
        throw new RuntimeException();
    }
}
