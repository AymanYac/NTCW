package model;

import java.util.ArrayList;

public class CircularArrayList<T> extends ArrayList {
    @Override
    public T get(int index)
    {
        return (T) super.get(getCircularizedIndex(index));
    }

    public int getCircularizedIndex(int index){
        if(index < 0)
            while (index<0)
                index = index + size();
        else if(index>=size())
            while(index>=size())
                index = index - size();
        return index;
    }
}
