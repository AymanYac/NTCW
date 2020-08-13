package model;

import javafx.util.Pair;

public class ComboPair<T, T1> extends Pair<T,T1> {
    public ComboPair(T key, T1 value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return getKey().toString()+" (Current: "+getValue().toString()+")";
    }
}
