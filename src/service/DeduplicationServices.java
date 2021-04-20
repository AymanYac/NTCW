package service;

import javafx.util.Pair;
import model.*;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.language_toolbox.Unidecode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeduplicationServices {
    private static ArrayList<String> targetSegmentIDS;
    private static HashMap<String, ArrayList<Object>> weightTable;
    private static ArrayList<CharDescriptionRow> targetItems;
    private static ArrayList<Pair<CharDescriptionRow,CharDescriptionRow>> targetItemPairs = new ArrayList<>();
    private static HashMap<String, ArrayList<String>> nameSakeCarIDs;
    private static Unidecode unidec;
    private static double lastprogess;
    private static HashMap<String, ConcurrentHashMap<String, HashMap<String, ComparaisonResult>>> fullCompResults;
    private static int compteur;

    public static void scoreDuplicatesForClassesFull(ArrayList<String> targetSegmentIDS, HashMap<String, ArrayList<Object>> weightTable, Integer GLOBAL_MIN_MATCHES, Integer GLOBAL_MAX_MISMATCHES, Double GLOBAL_MISMATCH_RATIO) {
        lastprogess = 0;
        DeduplicationServices.unidec = Unidecode.toAscii();
        DeduplicationServices.targetSegmentIDS = targetSegmentIDS;
        DeduplicationServices.weightTable = weightTable;
        DeduplicationServices.nameSakeCarIDs = CharValuesLoader.getNameSakeCarIDs();
        DeduplicationServices.fullCompResults = new HashMap<String, ConcurrentHashMap<String, HashMap<String, ComparaisonResult>>>();
        ArrayList<CharDescriptionRow> inClass = CharItemFetcher.allRowItems.parallelStream().filter(item -> isInTargetClass(item)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<CharDescriptionRow> outClass = CharItemFetcher.allRowItems.parallelStream().filter(item -> !isInTargetClass(item)).collect(Collectors.toCollection(ArrayList::new));
        IntStream.range(0, inClass.size()).forEach(idx_A -> {
            CharDescriptionRow item_A = inClass.get(idx_A);
            IntStream.range(idx_A+1,inClass.size()).forEach(idx_B->{
                targetItemPairs.add(new Pair<>(item_A,inClass.get(idx_B)));
            });
            outClass.forEach(item_B->{
                targetItemPairs.add(new Pair<>(item_A,item_B));
            });
        });
        System.out.println("::::::::::::::::::::::::::::: COMPARISONS TO BE MADE : "+targetItemPairs.size()+" :::::::::::::::::::::::::::::");
        targetItemPairs.forEach(e -> {
            CharDescriptionRow item_A = e.getKey();
            CharDescriptionRow item_B = e.getValue();
            fullCompResults.put(item_A.getItem_id(),new ConcurrentHashMap<String,HashMap<String,ComparaisonResult>>());
            fullCompResults.get(item_A.getItem_id()).put(item_B.getItem_id(),new HashMap<String,ComparaisonResult>());
            HashSet<ClassCaracteristic> checkedCarBInDescA = new HashSet<ClassCaracteristic>();
            System.out.println("XXXXXXXX");
            System.out.println(item_A.getClient_item_number()+">"+item_A.getAccentFreeDescriptionsNoCR(false));
            System.out.println(item_B.getClient_item_number()+">"+item_B.getAccentFreeDescriptionsNoCR(false));

            String class_A = item_A.getClass_segment_string().split("&&&")[0];
            String class_B = item_B.getClass_segment_string().split("&&&")[0];
            ArrayList<ClassCaracteristic> cars_A = CharValuesLoader.active_characteristics.get(class_A);
            ArrayList<ClassCaracteristic> cars_B = CharValuesLoader.active_characteristics.get(class_B);
            cars_A.forEach(car_A->{
                cars_B.forEach(car_B->{
                    if(!car_B.getIsNumeric().equals(car_A.getIsNumeric())){
                        return;
                    }
                    System.out.print("Comparing "+car_A.getCharacteristic_name()+ "<=>"+car_B.getCharacteristic_name());
                    Instant then = Instant.now();
                    CaracteristicValue data_A = item_A.getData(class_A).get(car_A.getCharacteristic_id());
                    CaracteristicValue data_B = item_B.getData(class_B).get(car_B.getCharacteristic_id());
                    String valCompare = compareValues(item_A, item_B, car_A, car_B,false);
                    if(isSameCar(car_A,car_B)){
                        if(data_A == null && data_B == null){
                            ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"UNKNOWN_MATCH");
                            hardStore(item_A,item_B,car_A,result);
                            System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                            return;
                        }else{
                            if(valCompare.equals("STRONG_MATCH") || valCompare.equals("WEAK_MATCH")){
                                ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,car_B,data_A,data_B,valCompare);
                                hardStore(item_A,item_B,car_A,result);
                                System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                                return;
                            }
                        }
                    }else{
                        if(valCompare.equals("WEAK_MATCH") || valCompare.equals("STRONG_MATCH")){
                            ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"ALTERNATIVE_MATCH");
                            System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                            softStore(item_A,item_B,car_A,result);
                        }
                    }
                    if(!hasStored(item_A,item_B,car_A)){
                        item_B.addDedupRulesForCar(car_A);
                        if(checkDescContainsVal(item_B,car_A,data_A,item_A)){
                            ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,null,data_A,null,"DESCRIPTION_MATCH");
                            System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                            softStore(item_A,item_B,car_A,result);
                        }
                    }
                    if(!hasStored(item_A,item_B,car_B)){
                        if(checkedCarBInDescA.add(car_B)){
                            item_A.addDedupRulesForCar(car_B);
                            if(checkDescContainsVal(item_A,car_B,data_B,item_B)){
                                ComparaisonResult result = new ComparaisonResult(item_A,item_B,null,car_B,null,data_B,"DESCRIPTION_MATCH");
                                System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                                softStore(item_A,item_B,car_B,result);
                            }
                        }
                    }
                    if(isSameCar(car_A,car_B) && !hasStored(item_A,item_B,car_A) && !hasStored(item_A,item_B,car_B)){
                        if(data_A!=null && data_B!=null && data_A.getRawDisplay().length()>0 && data_B.getRawDisplay().length()>0){
                            ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"MISMATCH");
                            System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                            softStore(item_A,item_B,car_B,result);
                        }else{
                            ComparaisonResult result = new ComparaisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"UNKNOWN_MATCH");
                            System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                            softStore(item_A,item_B,car_B,result);
                        }
                    }
                    System.out.println(" took "+ ChronoUnit.NANOS.between(then,Instant.now()));
                });
            });
            if(Math.random()<0){
                System.out.println("XXXXXXXX");
                System.out.println(item_A.getClient_item_number());
                System.out.println(item_A.getAccentFreeDescriptionsNoCR(false));
                System.out.println(item_B.getClient_item_number());
                System.out.println(item_B.getAccentFreeDescriptionsNoCR(false));
                fullCompResults.get(item_A.getItem_id()).get(item_B.getItem_id()).values().forEach(comparaisonResult -> {
                    String ret = "";
                    try{
                        ret+=comparaisonResult.getCar_A().getCharacteristic_name();
                    }catch (Exception V){

                    }
                    try{
                        ret+="<=>"+comparaisonResult.getCar_B().getCharacteristic_name();
                    }catch (Exception V){

                    }
                    ret+=":";
                    try{
                        ret+=comparaisonResult.getVal_A().getRawDisplay();
                    }catch (Exception V){

                    }
                    try{
                        ret+="<=>"+comparaisonResult.getVal_B().getRawDisplay();
                    }catch (Exception V){

                    }
                    ret+=":";
                    ret+=comparaisonResult.getResultType();
                    System.out.println("\t>"+ret);
                });
            }
            fullCompResults.get(item_A.getItem_id()).get(item_B.getItem_id()).values().forEach(comparaisonResult -> {
                String ret = "";
                try{
                    ret+=comparaisonResult.getCar_A().getCharacteristic_name();
                }catch (Exception V){

                }
                try{
                    ret+="<=>"+comparaisonResult.getCar_B().getCharacteristic_name();
                }catch (Exception V){

                }
                ret+=":";
                try{
                    ret+=comparaisonResult.getVal_A().getRawDisplay();
                }catch (Exception V){

                }
                try{
                    ret+="<=>"+comparaisonResult.getVal_B().getRawDisplay();
                }catch (Exception V){

                }
                ret+=":";
                ret+=comparaisonResult.getResultType();
                System.out.println("\t>"+ret);
            });
            //System.out.println(fullCompResults.get(item_A.getItem_id()));
            //System.out.println(item_A.getClient_item_number());
        });
        System.out.println("::::::::::::::::::::::::::::: DONE COMPARING : "+targetItemPairs.size()+" :::::::::::::::::::::::::::::");
        ConfirmationDialog.show("Done", "DONE COMPARING : "+targetItemPairs.size(), "OK");


    }

    private static boolean checkDescContainsVal(CharDescriptionRow targetItem, ClassCaracteristic sourceCarac, CaracteristicValue sourceValue, CharDescriptionRow sourceItem) {
        if(sourceValue == null){
            return false;
        }
        if(sourceCarac.getIsNumeric()){
            Optional<CaracteristicValue> match = targetItem.addDedupCandidateForCar(sourceCarac).stream().filter(cand ->
                    equalScalars(new Pair<String, String>(cand.getField("NOMINAL"), cand.getField("UOM_ID")), new Pair<String, String>(sourceValue.getField("NOMINAL"), sourceValue.getField("UOM_ID")))
            ).findAny();
            return match.isPresent();
        }else{
            String noPunctDesc = unidec.decodeAndTrim(targetItem.getAccentFreeDescriptionsNoCR(true)).toLowerCase().replaceAll("\\p{Punct}", " ").trim().replaceAll(" +", "");
            String noPunctVal = unidec.decodeAndTrim(sourceValue.getDataLanguageValue()).toLowerCase().replaceAll("\\p{Punct}", " ").trim().replaceAll(" +", "");
            return noPunctVal.length()>2 && noPunctDesc.contains(noPunctVal);
        }
    }


    private static boolean hasStored(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a) {
        try {
            fullCompResults.get(item_a.getItem_id()).get(item_b.getItem_id()).get(car_a.getCharacteristic_id()).getResultType();
            return true;
        }catch (Exception V){
            return false;
        }
    }

    private static void hardStore(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a, ComparaisonResult result) {
        fullCompResults.get(item_a.getItem_id()).get(item_b.getItem_id()).put(car_a.getCharacteristic_id(),result);
    }
    private static void softStore(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a, ComparaisonResult result) {
        try {
            fullCompResults.get(item_a.getItem_id()).get(item_b.getItem_id()).get(car_a.getCharacteristic_id()).getResultType();
        }catch (Exception V){
            hardStore(item_a,item_b,car_a,result);
        }
    }

    static class ComparaisonResult {
        CharDescriptionRow item_A;
        CharDescriptionRow item_B;
        ClassCaracteristic car_A;
        ClassCaracteristic car_B;
        CaracteristicValue val_A;
        CaracteristicValue val_B;
        String ResultType;


        public ComparaisonResult(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a, ClassCaracteristic car_b, CaracteristicValue data_a, CaracteristicValue data_b, String match_type) {
            super();
            setItem_A(item_a);
            setItem_B(item_b);
            setCar_A(car_a);
            setCar_B(car_b);
            setVal_A(data_a);
            setVal_B(data_b);
            setResultType(match_type);
        }

        public CharDescriptionRow getItem_A() {
            return item_A;
        }

        public void setItem_A(CharDescriptionRow item_A) {
            this.item_A = item_A;
        }

        public CharDescriptionRow getItem_B() {
            return item_B;
        }

        public void setItem_B(CharDescriptionRow item_B) {
            this.item_B = item_B;
        }

        public ClassCaracteristic getCar_A() {
            return car_A;
        }

        public void setCar_A(ClassCaracteristic car_A) {
            this.car_A = car_A;
        }

        public ClassCaracteristic getCar_B() {
            return car_B;
        }

        public void setCar_B(ClassCaracteristic car_B) {
            this.car_B = car_B;
        }

        public CaracteristicValue getVal_A() {
            return val_A;
        }

        public void setVal_A(CaracteristicValue val_A) {
            this.val_A = val_A;
        }

        public CaracteristicValue getVal_B() {
            return val_B;
        }

        public void setVal_B(CaracteristicValue val_B) {
            this.val_B = val_B;
        }

        public String getResultType() {
            return ResultType;
        }

        public void setResultType(String resultType) {
            ResultType = resultType;
        }
    }

    private static boolean isSameCar(ClassCaracteristic car_A, ClassCaracteristic car_B) {
        return !GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID && car_A.getCharacteristic_id().equals(car_B.getCharacteristic_id())
                ||
                GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID && car_A.getCharacteristic_name().equals(car_B.getCharacteristic_name());
    }


    private static boolean isInTargetClass(CharDescriptionRow item) {
        return DeduplicationServices.targetSegmentIDS.contains(item.getClass_segment_string().split("&&&")[0]);
    }

    public static void scoreDuplicatesForClassesPairWise(ArrayList<String> targetSegmentIDS, HashMap<String, ArrayList<Object>> weightTable, Integer GLOBAL_MIN_MATCHES, Integer GLOBAL_MAX_MISMATCHES, Double GLOBAL_MISMATCH_RATIO) {
        lastprogess = 0;
        DeduplicationServices.unidec = Unidecode.toAscii();
        DeduplicationServices.targetSegmentIDS = targetSegmentIDS;
        DeduplicationServices.weightTable = weightTable;
        DeduplicationServices.targetItems = CharItemFetcher.allRowItems.parallelStream().filter(DeduplicationServices::isInTargetClass).collect(Collectors.toCollection(ArrayList::new));
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
                    ClassCaracteristic car = getCarFromWeightTableRowValue(e.getValue());
                    if(car.getIsNumeric()){
                        String numCompare = compareNumValues(item_A,item_B,car,car,true);
                        switch (numCompare) {
                            case "STRONG_MATCH":
                                strongMatch(score, strongMatches, e.getValue());
                                break;
                            case "WEAK_MATCH":
                                weakMatch(score, weakMatches, e.getValue());
                                break;
                            case "MISMATCH":
                                mismatch(score, mismatches, e.getValue());
                                break;
                            case "DESC_MATCH":
                                descMatch(score, descMatches, e.getValue());
                                break;
                        }
                    }else{
                        String txtCompare = compareTxtValues(item_A,item_B,car,car,true);
                        switch (txtCompare) {
                            case "STRONG_MATCH":
                                strongMatch(score, strongMatches, e.getValue());
                                break;
                            case "WEAK_MATCH":
                                weakMatch(score, weakMatches, e.getValue());
                                break;
                            case "MISMATCH":
                                mismatch(score, mismatches, e.getValue());
                                break;
                            case "DESC_MATCH":
                                descMatch(score, descMatches, e.getValue());
                                break;
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
    private static String compareValues(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a, ClassCaracteristic car_b, boolean checkDescs) {
        if(!car_a.getIsNumeric().equals(car_b.getIsNumeric())){
            return "";
        }
        if(car_a.getIsNumeric()){
            return compareNumValues(item_a,item_b,car_a,car_b,checkDescs);
        }else{
            return compareTxtValues(item_a,item_b,car_a,car_b,checkDescs);
        }
    }

    private static String compareTxtValues(CharDescriptionRow item_A,CharDescriptionRow item_B, ClassCaracteristic carA, ClassCaracteristic carB,boolean checkDescs){
        Pair<String, String> DLa = getField(item_A, "DL", carA);
        Pair<String, String> DLb = getField(item_B, "DL", carB);
        String valA = null;
        try{
            valA = DLa.getKey();
        }catch (Exception V){

        }
        if(valA!=null && DLb!=null){
            valA = unidec.decodeAndTrim(valA).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
            String valB = "";
            valB = unidec.decodeAndTrim(DLb.getKey()).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
            if(valA.equals(valB)){
                return "STRONG_MATCH";

            }
            if(valB.contains(valA)){
                return "WEAK_MATCH";

            }
        }
        String valB = null;
        try{
            valB = DLb.getKey();
        }catch (Exception V){

        }
        if(valB!=null && DLa!=null){
            valB = unidec.decodeAndTrim(valB).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
            valA = unidec.decodeAndTrim(DLa.getKey()).toLowerCase().replaceAll("\\p{Punct}"," ").trim().replaceAll(" +", " ");
            if(valB.equals(valA)){
                return "STRONG_MATCH";

            }
            if(valA.contains(valB)){
                return "WEAK_MATCH";

            }
        }
        if(checkDescs && (itemDescIncludesTXT(item_A,valA) || itemDescIncludesTXT(item_A,valB))){
            return "DESC_MATCH";

        }
        return "";
    }
    private static String compareNumValues(CharDescriptionRow item_A, CharDescriptionRow item_B, ClassCaracteristic carA, ClassCaracteristic carB,boolean checkDescs) {
        Pair<String, String> nomA = getField(item_A, "NOMINAL", carA);
        Pair<String, String> nomB = getField(item_B, "NOMINAL", carB);
        if(approachingScalars(nomA,nomB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
            if(equalScalars(nomA,nomB)){
                //NOM A vs NOM B : EQUAL
                Pair<String, String> maxA = getField(item_A, "MAX", carA);
                Pair<String, String> maxB = getField(item_B, "MAX", carB);
                Pair<String, String> minA = getField(item_A, "MIN", carA);
                Pair<String, String> minB = getField(item_B, "MIN", carB);

                if( (minA==null || minB==null || equalScalars(minA,minB))
                        &&
                        (maxA==null || maxB==null || equalScalars(maxA,maxB))
                ){
                    //NOM A vs NOM B:EQUAL
                    //MIN A vs MIN B:UNKNOWN or EQUAL
                    //MAX A vs MAX B:UNKNOWN or EQUAL
                    return "STRONG_MATCH";


                }
                if(maxA!=null && maxB!=null){
                    //NOM A vs NOM B:EQUAL
                    //MAX A vs MAX B:APPROACHING or DIFFERENT
                    return "WEAK_MATCH";

                }
                if(minA!=null && minB!=null){
                    //NOM A vs NOM B:EQUAL
                    //MIN A vs MIN B:APPROACHING or DIFFERENT
                    return "WEAK_MATCH";

                }
            }else{
                //NOM A vs NOM B : APPROACHING
                return "WEAK_MATCH";

            }

        }else{
            if(nomA!=null && nomB!=null){
                //NOM A vs NOM B:DIFFERENT
                return "MISMATCH";

            }else{
                //NOM A vs NOM B:UNKNOWN
                Pair<String, String> maxA = getField(item_A, "MAX", carA);
                Pair<String, String> maxB = getField(item_B, "MAX", carB);
                Pair<String, String> minA = getField(item_A, "MIN", carA);
                Pair<String, String> minB = getField(item_B, "MIN", carB);
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
                            return "WEAK_MATCH";

                        }
                        if(checkDescs && (itemDescIncludesNum(item_B,maxA)
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
                        )){
                            return "DESC_MATCH";

                        }
                    }else{
                        //NOM A vs NOM B:UNKNOWN
                        //MIN A vs MIN B:UNKNOWN
                        if(approachingScalars(maxA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                            if(equalScalars(maxA,maxB)){
                                //NOM A vs NOM B:UNKNOWN
                                //MIN A vs MIN B:UNKNOWN
                                //MAX A vs MAX B:EQUAL
                                return "STRONG_MATCH";

                            }else{
                                //NOM A vs NOM B:UNKNOWN
                                //MIN A vs MIN B:UNKNOWN
                                //MAX A vs MAX B:APPROACHING
                                return "WEAK_MATCH";

                            }
                        }else{
                            //NOM A vs NOM B:UNKNOWN
                            //MIN A vs MIN B:UNKNOWN
                            //MAX A vs MAX B:DIFFERENT
                            return "MISMATCH";

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
                                        return "STRONG_MATCH";

                                    }else{
                                        //NOM A vs NOM B:UNKNOWN
                                        //MIN A vs MIN B:EQUAL
                                        //MAX A vs MAX B:APPROACHING
                                        return "WEAK_MATCH";

                                    }
                                }else{
                                    //NOM A vs NOM B:UNKNOWN
                                    //MIN A vs MIN B:EQUAL
                                    //MAX A vs MAX B:DIFFERENT
                                    return "MISMATCH";

                                }
                            }else{
                                //NOM A vs NOM B:UNKNOWN
                                //MIN A vs MIN B:EQUAL
                                //MAX A vs MAX B:UNKNOWN
                                return "STRONG_MATCH";

                            }
                        }else{
                            if(maxA==null || maxB==null || approachingScalars(maxA,maxB,GlobalConstants.DEDUP_NUM_APPROACHING_THRESH)){
                                //NOM A vs NOM B:UNKNOWN
                                //MIN A vs MIN B:APPROACHING
                                //MAX A vs MAX B:EQUAL, APPROACHING or UNKNOWN
                                return "WEAK_MATCH";

                            }else{
                                //NOM A vs NOM B:UNKNOWN
                                //MIN A vs MIN B:APPROACHING
                                //MAX A vs MAX B:DIFFERENT
                                return "MISMATCH";

                            }
                        }
                    }else{
                        return "MISMATCH";

                    }
                }
            }
        }
        return "";
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
        return !(hopeMismatchRatio > global_max_ratio);
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
            String uomA = pairA.getValue();
            String uomB = pairB.getValue();
            if(scalarA!=null && scalarB!=null){
                try{
                    return ( (uomA==null && uomB==null) || (uomA!=null && uomB!=null && uomA.equals(uomB)) )
                            &&  Math.abs(Double.parseDouble(scalarA) - Double.parseDouble(scalarB))<=threshold*Math.abs(Math.max(Double.parseDouble(scalarA),Double.parseDouble(scalarB)));
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
            String uomA = pairA.getValue();
            String uomB = pairB.getValue();
            if(scalarA!=null && scalarB!=null){
                try{
                    return ( (uomA==null && uomB==null) || (uomA!=null && uomB!=null && uomA.equals(uomB)) )
                    && Double.parseDouble(scalarA) == Double.parseDouble(scalarB);
                }catch (Exception V){
                    return false;
                }
            }
            return false;
        }
        return false;
    }


    private static Pair<String, String> getField(CharDescriptionRow targetItem, String FIELD_NAME, ClassCaracteristic targetCar) {
        Optional<Pair<String, String>> tmp;
        if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
            tmp = targetItem.getData(targetItem.getClass_segment_string().split("&&&")[0]).entrySet().stream()
                    .filter(e -> nameSakeCarIDs.get(targetCar.getCharacteristic_name()).contains(e.getKey()))
                    .filter(e -> e.getValue() != null && e.getValue().getField(FIELD_NAME) != null && e.getValue().getField(FIELD_NAME).length() > 0)
                    .map(e -> new Pair<String, String>(e.getValue().getField(FIELD_NAME), e.getValue().getField("UOM_ID"))).findAny();
        }else{
            tmp = targetItem.getData(targetItem.getClass_segment_string().split("&&&")[0]).entrySet().stream()
                    .filter(e -> e.getKey().equals(targetCar.getCharacteristic_id()))
                    .filter(e -> e.getValue() != null && e.getValue().getField(FIELD_NAME) != null && e.getValue().getField(FIELD_NAME).length() > 0)
                    .map(e -> new Pair<String, String>(e.getValue().getField(FIELD_NAME), e.getValue().getField("UOM_ID"))).findAny();
        }
        return tmp.orElse(null);
    }

    private static ClassCaracteristic getCarFromWeightTableRowValue(ArrayList<Object> value) {
        return (ClassCaracteristic) value.get(0);
    }
    private static Double getWeightFromTableRowValue(String MATCH_TYPE, ArrayList<Object> value) {
        switch (MATCH_TYPE) {
            case "STRONG":
                return Double.parseDouble((String) value.get(1));
            case "WEAK":
                return Double.parseDouble((String) value.get(2));
            case "INCLUDED":
                return Double.parseDouble((String) value.get(3));
            case "MISMATCH":
                return Double.parseDouble((String) value.get(4));
        }
        throw new RuntimeException();
    }

}
