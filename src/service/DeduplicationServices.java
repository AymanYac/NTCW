package service;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;
import model.*;
import transversal.data_exchange_toolbox.CharDescriptionExportServices;
import transversal.dialog_toolbox.ConfirmationDialog;
import transversal.dialog_toolbox.DedupLaunchDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.Unidecode;
import transversal.language_toolbox.WordUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeduplicationServices {
    private static ArrayList<String> sourceSegmentIDS;
    private static ArrayList<String> targetSegmentIDS;
    private static HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weightTable;
    private static ArrayList<CharDescriptionRow> computeItems;
    private static ArrayList<Pair<CharDescriptionRow,CharDescriptionRow>> computeItemPairs;
    private static HashMap<String, ArrayList<String>> nameSakeCarIDs;
    private static Unidecode unidec;
    private static double lastprogess;
    private static ConcurrentHashMap<String, HashMap<String, ComparisonResult>> fullCompResults;

    public static void scoreDuplicatesForClassesFull(ComboBox<ClassSegmentClusterComboRow> sourceCharClassLink, ComboBox<ClassSegmentClusterComboRow> targetCharClassLink, HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weightTable, Integer GLOBAL_MIN_MATCHES, Integer GLOBAL_MAX_MISMATCHES, double GLOBAL_MISMATCH_RATIO, Double topCouplesPercentage, int topCouplesNumber, Char_description parent, ProgressBar progressBar) throws SQLException, ClassNotFoundException, IOException {
        DeduplicationServices.sourceSegmentIDS = sourceCharClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
        DeduplicationServices.targetSegmentIDS = targetCharClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
        lastprogess = 0;
        DeduplicationServices.unidec = Unidecode.toAscii();
        DeduplicationServices.weightTable = weightTable;
        DeduplicationServices.nameSakeCarIDs = CharValuesLoader.getNameSakeCarIDs();
        DeduplicationServices.fullCompResults = new ConcurrentHashMap<String, HashMap<String, ComparisonResult>>();
        DeduplicationServices.computeItemPairs = new ArrayList<>();
        ArrayList<CharDescriptionRow> inClass = CharItemFetcher.allRowItems.parallelStream().filter(item -> isInSourceClass(item)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<CharDescriptionRow> outClass = CharItemFetcher.allRowItems.parallelStream().filter(item -> !isInSourceClass(item) && isInTargetClass(item)).collect(Collectors.toCollection(ArrayList::new));
        IntStream.range(0, inClass.size()).forEach(idx_A -> {
            CharDescriptionRow item_A = inClass.get(idx_A);
            IntStream.range(idx_A+1,inClass.size()).forEach(idx_B->{
                computeItemPairs.add(new Pair<>(item_A,inClass.get(idx_B)));
            });
            outClass.forEach(item_B->{
                computeItemPairs.add(new Pair<>(item_A,item_B));
            });
        });
        Collections.shuffle(computeItemPairs);
        System.out.println("::::::::::::::::::::::::::::: TARGET ITEMS : "+inClass.size()+" :::::::::::::::::::::::::::::");
        System.out.println("::::::::::::::::::::::::::::: ITEMS IN PROJECT : "+CharItemFetcher.allRowItems.size()+" :::::::::::::::::::::::::::::");
        System.out.println("::::::::::::::::::::::::::::: COMPARISONS TO BE MADE : "+ computeItemPairs.size()+" :::::::::::::::::::::::::::::");
        Instant start = Instant.now();
        AtomicInteger doneComparisons = new AtomicInteger(0);
        computeItemPairs.parallelStream().forEach(e -> {
            HashMap<String, ComparisonResult> localCompStorage = new HashMap<String, ComparisonResult>();
            CharDescriptionRow item_A = e.getKey();
            CharDescriptionRow item_B = e.getValue();
            CaracteristicValue data_a = new CaracteristicValue();
            try {
                data_a.setDataLanguageValue(Tools.get_project_segments(parent.account).get(item_A.getClass_segment_string().split("&&&")[0]).toString());
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
            CaracteristicValue data_b = new CaracteristicValue();
            try {
                data_b.setDataLanguageValue(Tools.get_project_segments(parent.account).get(item_B.getClass_segment_string().split("&&&")[0]).toString());
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
            localCompStorage.put("CLASS_ID",new ComparisonResult(item_A,item_B,null,null,data_a,data_b,item_A.getClass_segment_string().equals(item_B.getClass_segment_string())?"STRONG_MATCH":"MISMATCH"));
            HashSet<ClassCaracteristic> checkedCarBInDescA = new HashSet<ClassCaracteristic>();
            String class_A = item_A.getClass_segment_string().split("&&&")[0];
            String class_B = item_B.getClass_segment_string().split("&&&")[0];
            ArrayList<ClassCaracteristic> cars_A = CharValuesLoader.active_characteristics.get(class_A);
            ArrayList<ClassCaracteristic> cars_B = CharValuesLoader.active_characteristics.get(class_B);
            IntStream.range(0,cars_A.size()).forEach(car_idx_A->{
                ClassCaracteristic car_A = cars_A.get(car_idx_A);
                final DedupLaunchDialog.DedupLaunchDialogRow car_A_params = getCarParams(weightTable,car_A);
                if(!car_A_params.isSameCarac()){
                    return;
                }
                CaracteristicValue data_A = item_A.getData(class_A).get(car_A.getCharacteristic_id());
                if(!hasCounterPart(car_A,cars_B) && (data_A==null || data_A.getRawDisplay().length()==0) && car_A_params.isSameCarac()){
                    ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,null,data_A,null,"UNKNOWN_MATCH");
                    hardStore(localCompStorage, car_A,result);
                    return;
                }
                IntStream.range(0, cars_B.size()).forEach(car_idx_B->{
                    ClassCaracteristic car_B = cars_B.get(car_idx_B);
                    final DedupLaunchDialog.DedupLaunchDialogRow car_B_params = getCarParams(weightTable,car_B);
                    CaracteristicValue data_B = item_B.getData(class_B).get(car_B.getCharacteristic_id());
                    if(!hasCounterPart(car_B,cars_A) && (data_B==null || data_B.getRawDisplay().length()==0) && car_B_params.isSameCarac()){
                        ComparisonResult result = new ComparisonResult(item_A,item_B,null,car_B,null,data_B,"UNKNOWN_MATCH");
                        hardStore(localCompStorage, car_B,result);
                        return;
                    }
                    if(!itemPairIsViable(localCompStorage.values(),GLOBAL_MIN_MATCHES,GLOBAL_MAX_MISMATCHES,GLOBAL_MISMATCH_RATIO,(cars_A.size()-car_idx_A-1)*cars_B.size() + (cars_B.size()-car_idx_B))){
                        return;
                    }
                    if(!(car_B.getIsNumeric().equals(car_A.getIsNumeric()))){
                        return;
                    }
                    if(isSameCar(car_A,car_B)){
                        String valCompare = compareValues(item_A, item_B, car_A, car_B,false);
                        if(data_A == null && data_B == null){
                            ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"UNKNOWN_MATCH");
                            hardStore(localCompStorage, car_A,result);
                            return;
                        }else{
                            if(valCompare.equals("STRONG_MATCH") || valCompare.equals("WEAK_MATCH")){
                                ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,car_B,data_A,data_B,valCompare);
                                hardStore(localCompStorage, car_A,result);
                                return;
                            }
                        }
                    }else if(car_A_params.isAllCarac() || car_B_params.isAllCarac()){
                        String valCompare = compareValues(item_A, item_B, car_A, car_B,false);
                        if(valCompare.equals("WEAK_MATCH") || valCompare.equals("STRONG_MATCH")){
                            ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"ALTERNATIVE_MATCH");
                            softStore(localCompStorage, car_A,result);
                        }
                    }
                    if(car_A_params.isAllCarac() && !hasStored(localCompStorage, car_A)){
                        item_B.addDedupRulesForCar(car_A);
                        if(checkDescContainsVal(item_B,car_A,data_A,item_A)){
                            ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,null,data_A,null,"DESCRIPTION_MATCH");
                            softStore(localCompStorage, car_A,result);
                        }
                    }
                    if(car_B_params.isAllCarac() && !hasStored(localCompStorage, car_B)){
                        if(checkedCarBInDescA.add(car_B)){
                            item_A.addDedupRulesForCar(car_B);
                            if(checkDescContainsVal(item_A,car_B,data_B,item_B)){
                                ComparisonResult result = new ComparisonResult(item_A,item_B,null,car_B,null,data_B,"DESCRIPTION_MATCH");
                                softStore(localCompStorage, car_B,result);
                            }
                        }
                    }
                    if(car_A_params.isSameCarac() && isSameCar(car_A,car_B) && !hasStored(localCompStorage, car_A) && !hasStored(localCompStorage, car_B)){
                        if(data_A!=null && data_B!=null && data_A.getRawDisplay().length()>0 && data_B.getRawDisplay().length()>0){
                            ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"MISMATCH");
                            softStore(localCompStorage, car_B,result);
                        }else{
                            ComparisonResult result = new ComparisonResult(item_A,item_B,car_A,car_B,data_A,data_B,"UNKNOWN_MATCH");
                            softStore(localCompStorage, car_B,result);
                        }
                    }
                });
            });
            doneComparisons.addAndGet(1);
            if(itemPairIsViable(localCompStorage.values(),GLOBAL_MIN_MATCHES,GLOBAL_MAX_MISMATCHES,GLOBAL_MISMATCH_RATIO,0)){
                aggregatePairScore(localCompStorage);
                fullCompResults.put(item_A.getItem_id()+"<=>"+item_B.getItem_id(),localCompStorage);
            }
            if(Math.random()<0.001){
                double progress = (doneComparisons.get() * 100.0) / computeItemPairs.size();
                if(progress-lastprogess>2){
                    lastprogess = progress;
                    System.out.println(LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+" > "+ WordUtils.Rewriter.DecimalUtils.round(progress)+"%");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progress/100.0);
                        }
                    });
                    if(fullCompResults.size()>GlobalConstants.MAX_DEDUP_PAIRS_IN_MEMORY){
                        AtomicInteger numberOfRemovals = new AtomicInteger(0);
                        AtomicInteger minScoreOfRemoval = new AtomicInteger(0);
                        fullCompResults.values().stream().map(p->p.get("PAIR_SCORE").getScore()).collect(Collectors.toCollection(ArrayList::new)).stream().sorted().forEach(lowestScoreFirst->{
                            if(numberOfRemovals.get()+GlobalConstants.MAX_DEDUP_PAIRS_IN_MEMORY < fullCompResults.size()){
                                numberOfRemovals.addAndGet(1);
                                minScoreOfRemoval.set((int) Math.floor(lowestScoreFirst));
                            }
                        });
                        fullCompResults.entrySet().removeIf(p->p.getValue().get("PAIR_SCORE").getScore()<=minScoreOfRemoval.get());
                    }
                }
                //printConsoleSummary(item_A,item_B, localCompStorage);
            }
        });
        System.out.println("::::::::::::::::::::::::::::: RETAINED ITEMS : "+fullCompResults.size()+" ::::::::::::::::::::::::::::: in "+Duration.between(start,Instant.now()).getSeconds() +" seconds");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ConfirmationDialog.show("Done", "RETAINED ITEMS : "+fullCompResults.size(), "OK");
                try {
                    CharDescriptionExportServices.exportDedupReport(fullCompResults,weightTable,GLOBAL_MIN_MATCHES,GLOBAL_MAX_MISMATCHES,GLOBAL_MISMATCH_RATIO,sourceCharClassLink,targetCharClassLink,topCouplesPercentage,topCouplesNumber, parent);
                } catch (SQLException | ClassNotFoundException | IOException throwables) {
                    throwables.printStackTrace();
                }
                ConfirmationDialog.show("Done", "Results saved", "OK");
            }
        });

    }

    private static void aggregatePairScore(HashMap<String, ComparisonResult> e) {
        AtomicReference<Double> score = new AtomicReference<>(0.0);
        e.values().forEach(r->{
            score.updateAndGet(v -> v + r.getScore());
        });
        e.put("PAIR_SCORE",new ComparisonResult(score.get()));
    }

    private static DedupLaunchDialog.DedupLaunchDialogRow getCarParams(HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weightTable, ClassCaracteristic car) {
        DedupLaunchDialog.DedupLaunchDialogRow ret = weightTable.get(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID ? car.getCharacteristic_name() : car.getCharacteristic_id());
        if(ret!=null){
            return ret;
        }else{
            DedupLaunchDialog.DedupLaunchDialogRow tmp = new DedupLaunchDialog.DedupLaunchDialogRow(car,true);
            tmp.setAllCarac(false);
            tmp.setSameCarac(false);
            return tmp;
        }
    }

    private static boolean hasCounterPart(ClassCaracteristic car_a, ArrayList<ClassCaracteristic> cars_b) {
        return cars_b.stream().map(car->
                    GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID?car.getCharacteristic_name():car.getCharacteristic_id())
                .collect(Collectors.toList())
                .contains(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID?car_a.getCharacteristic_name():car_a.getCharacteristic_id());
    }

    private static void printConsoleSummary(CharDescriptionRow item_A, CharDescriptionRow item_B, HashMap<String, ComparisonResult> localCompStorage) {
        AtomicReference<String> pairComparison = new AtomicReference<>("XXXXXXXX\n"
                + item_A.getClient_item_number() + "\n"
                + item_A.getAccentFreeDescriptionsNoCR(false) + "\n"
                + item_B.getClient_item_number() + "\n"
                + item_B.getAccentFreeDescriptionsNoCR(false));
        AtomicReference<String> carComparison = new AtomicReference<>();
        localCompStorage.entrySet().forEach(e -> {
            String carID = e.getKey();
            ComparisonResult comparaisonResult = e.getValue();
            if(e.getKey().equals("CLASS_ID")){
                pairComparison.getAndSet(pairComparison.get() + "\n\t>" + "ITEM_CLASS<=>ITEM_CLASS:"+comparaisonResult.getItem_A().getClass_segment(true).getClassNumber()+"-"+comparaisonResult.getItem_A().getClass_segment(true).getClassName()+"<=>"+comparaisonResult.getItem_B().getClass_segment(true).getClassNumber()+"-"+comparaisonResult.getItem_B().getClass_segment(true).getClassName()+":"+comparaisonResult.getResultType());
                return;
            }
            carComparison.set("");
            try {
                carComparison.getAndSet(carComparison.get()+ comparaisonResult.getCar_A().getCharacteristic_name());
            } catch (Exception V) {

            }
            try {
                carComparison.getAndSet(carComparison.get()+ "<=>" + comparaisonResult.getCar_B().getCharacteristic_name());
            } catch (Exception V) {

            }
            carComparison.getAndSet(carComparison.get()+ ":");
            try {
                carComparison.getAndSet(carComparison.get()+ comparaisonResult.getVal_A().getRawDisplay());
            } catch (Exception V) {

            }
            try {
                carComparison.getAndSet(carComparison.get()+ "<=>" + comparaisonResult.getVal_B().getRawDisplay());
            } catch (Exception V) {

            }
            carComparison.getAndSet(carComparison.get()+ ":");
            carComparison.getAndSet(carComparison.get()+ comparaisonResult.getResultType());
            pairComparison.getAndSet(pairComparison.get() + "\n\t>" + carComparison.get());
        });
        System.out.println(pairComparison.get());
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


    private static boolean hasStored(HashMap<String, ComparisonResult> localComp, ClassCaracteristic car_a) {
        try {
            localComp.get(car_a.getCharacteristic_id()).getResultType();
            return true;
        }catch (Exception V){
            return false;
        }
    }

    private static void hardStore(HashMap<String, ComparisonResult> localComp, ClassCaracteristic car_a, ComparisonResult result) {
        localComp.put(car_a.getCharacteristic_id(),result);
    }
    private static void softStore(HashMap<String, ComparisonResult> localComp, ClassCaracteristic car_a, ComparisonResult result) {
        try {
            localComp.get(car_a.getCharacteristic_id()).getResultType();
        }catch (Exception V){
            hardStore(localComp, car_a,result);
        }
    }

    public static class ComparisonResult {
        CharDescriptionRow item_A;
        CharDescriptionRow item_B;
        ClassCaracteristic car_A;
        ClassCaracteristic car_B;
        CaracteristicValue val_A;
        CaracteristicValue val_B;
        String ResultType;
        Double score = 0.0;


        public ComparisonResult(CharDescriptionRow item_a, CharDescriptionRow item_b, ClassCaracteristic car_a, ClassCaracteristic car_b, CaracteristicValue data_a, CaracteristicValue data_b, String match_type) {
            super();
            setItem_A(item_a);
            setItem_B(item_b);
            setCar_A(car_a);
            setCar_B(car_b);
            setVal_A(data_a);
            setVal_B(data_b);
            setResultType(match_type);
            setScore();
        }

        public ComparisonResult(Double score) {
            super();
            setScore(score);
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

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
        private void setScore(){
            if(getCar_A()!=null){
                setScore(getWeightFromWeightTableRowValue(getResultType(),getCarParams(weightTable,car_A)));
            }else if(getCar_B()!=null){
                setScore(getWeightFromWeightTableRowValue(getResultType(),getCarParams(weightTable,car_B)));
            }else{
                setScore(getWeightFromWeightTableRowValue(getResultType(),weightTable.get(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID?"Item Class":"CLASS_ID")));
            }

        }
    }

    private static boolean isSameCar(ClassCaracteristic car_A, ClassCaracteristic car_B) {
        if(GlobalConstants.DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID){
            return car_A.getCharacteristic_name().equals(car_B.getCharacteristic_name());
        }
        return car_A.getCharacteristic_id().equals(car_B.getCharacteristic_id());
    }


    private static boolean isInSourceClass(CharDescriptionRow item) {
        return DeduplicationServices.sourceSegmentIDS.contains(item.getClass_segment_string().split("&&&")[0]);
    }
    private static boolean isInTargetClass(CharDescriptionRow item) {
        return DeduplicationServices.targetSegmentIDS.contains(item.getClass_segment_string().split("&&&")[0]);
    }

    public static void scoreDuplicatesForClassesPairWise(ComboBox<ClassSegmentClusterComboRow> sourceCharClassLink, HashMap<String, DedupLaunchDialog.DedupLaunchDialogRow> weightTable, Integer GLOBAL_MIN_MATCHES, Integer GLOBAL_MAX_MISMATCHES, Double GLOBAL_MISMATCH_RATIO) {
        DeduplicationServices.sourceSegmentIDS = sourceCharClassLink.getValue().getRowSegments().stream().filter(p -> p.getValue().getValue()).map(p -> p.getKey().getSegmentId()).collect(Collectors.toCollection(ArrayList::new));
        lastprogess = 0;
        DeduplicationServices.unidec = Unidecode.toAscii();
        DeduplicationServices.weightTable = weightTable;
        DeduplicationServices.computeItems = CharItemFetcher.allRowItems.parallelStream().filter(DeduplicationServices::isInSourceClass).collect(Collectors.toCollection(ArrayList::new));
        DeduplicationServices.nameSakeCarIDs = CharValuesLoader.getNameSakeCarIDs();
        IntStream.range(0, computeItems.size()).forEach(idx_A->{
            IntStream.range(idx_A+1, computeItems.size()).parallel().forEach(idx_B->{
                CharDescriptionRow item_A = computeItems.get(idx_A);
                CharDescriptionRow item_B = computeItems.get(idx_B);
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
                            case "DESCRIPTION_MATCH":
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
                            case "DESCRIPTION_MATCH":
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
                double progress = (idx_A * 100.0) / computeItems.size();
                if(progress-lastprogess>2){
                    lastprogess = progress;
                    System.out.println(LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+" > "+WordUtils.Rewriter.DecimalUtils.round(progress)+"%");
                }
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
            return "DESCRIPTION_MATCH";

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
                            return "DESCRIPTION_MATCH";

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


    private static boolean itemPairIsViable(Collection<ComparisonResult> stringComparaisonResultHashMap, Integer global_min_matches, Integer global_max_mismatches, Double global_max_ratio, int remainingUncheckedCars) {
        List<String> matchResultWordings = Arrays.asList("STRONG_MATCH", "WEAK_MATCH", "ALTERNATIVE_MATCH", "DESCRIPTION_MATCH");
        int totalMatches = stringComparaisonResultHashMap.stream().filter(r->r.getResultType()!=null && matchResultWordings.contains(r.getResultType())).collect(Collectors.toList()).size();
        List<String> NomatchResultWordings = Arrays.asList("MISMATCH");
        int totalMismatches = stringComparaisonResultHashMap.stream().filter(r->r.getResultType()!=null && NomatchResultWordings.contains(r.getResultType())).collect(Collectors.toList()).size();
        return itemPairIsViable(totalMatches,totalMismatches,global_min_matches,global_max_mismatches,global_max_ratio,remainingUncheckedCars);
    }
    private static boolean itemPairIsViable(AtomicReference<Integer> strongMatches, AtomicReference<Integer> weakMatches, AtomicReference<Integer> descMatches, AtomicReference<Integer> mismatches, Integer global_min_matches, Integer global_max_mismatches, Double global_max_ratio, int remainingUncheckedCars) {
        int totalMatches = strongMatches.get() + weakMatches.get() + descMatches.get();
        int totalMismatches = mismatches.get();
        return itemPairIsViable(totalMatches,totalMismatches,global_min_matches,global_max_mismatches,global_max_ratio,remainingUncheckedCars);
    }

    private static boolean itemPairIsViable(int totalMatches, int totalMismatches, Integer global_min_matches, Integer global_max_mismatches, Double global_max_ratio, int remainingUncheckedCars) {
        int hopeMatches = totalMatches + remainingUncheckedCars;
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

    private static void mismatch(AtomicReference<Double> score, AtomicReference<Integer> mismatches, DedupLaunchDialog.DedupLaunchDialogRow weights) {
        score.updateAndGet(v -> v + getWeightFromWeightTableRowValue("MISMATCH", weights));
        mismatches.updateAndGet(v->v+1);
    }
    private static void descMatch(AtomicReference<Double> score, AtomicReference<Integer> descMatches, DedupLaunchDialog.DedupLaunchDialogRow weights) {
        score.updateAndGet(v -> v + getWeightFromWeightTableRowValue("DESCRIPTION_MATCH", weights));
        descMatches.updateAndGet(v->v+1);
    }

    private static void weakMatch(AtomicReference<Double> score, AtomicReference<Integer> weakMatches, DedupLaunchDialog.DedupLaunchDialogRow weights) {
        score.updateAndGet(v -> v + getWeightFromWeightTableRowValue("WEAK_MATCH",weights));
        weakMatches.updateAndGet(v->v+1);
    }

    private static void strongMatch(AtomicReference<Double> score, AtomicReference<Integer> strongMatches, DedupLaunchDialog.DedupLaunchDialogRow weights) {
        score.updateAndGet(v -> v + getWeightFromWeightTableRowValue("STRONG_MATCH",weights));
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

    public static ClassCaracteristic getCarFromWeightTableRowValue(DedupLaunchDialog.DedupLaunchDialogRow value) {
        return value.getCarac();
    }
    public static Double getWeightFromWeightTableRowValue(String MATCH_TYPE, DedupLaunchDialog.DedupLaunchDialogRow value) {
        switch (MATCH_TYPE) {
            case "STRONG_MATCH":
                return Double.parseDouble((String) value.getStrongWeight());
            case "WEAK_MATCH":
                return Double.parseDouble((String) value.getWeakWeight());
            case "DESCRIPTION_MATCH":
                return Double.parseDouble((String) value.getIncludedWeight());
            case "ALTERNATIVE_MATCH":
                return Double.parseDouble((String) value.getAlternativeWeight());
            case "UNKNOWN_MATCH":
                return Double.parseDouble((String) value.getUnknownWeight());
            case "MISMATCH":
                return Double.parseDouble((String) value.getMismatchWeight());
        }
        System.out.println(MATCH_TYPE);
        throw new RuntimeException();
    }

}
