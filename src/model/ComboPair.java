package model;

import javafx.util.Pair;

public class ComboPair<T, T1> extends Pair<T,T1> {
    final public static String NEW_ENTRY_LABEL = "(Last)";
    public ComboPair(T key, T1 value) {
        super(key, value);
    }

    @Override
    public String toString() {
        if(getValue().equals(NEW_ENTRY_LABEL)){
            return getKey().toString()+" "+NEW_ENTRY_LABEL;
        }
        return getKey().toString()+" (Current: "+getValue().toString()+")";
    }
}
