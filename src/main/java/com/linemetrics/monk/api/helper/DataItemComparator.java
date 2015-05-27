package com.linemetrics.monk.api.helper;

import com.linemetrics.monk.dao.DataItem;

import java.util.Comparator;

public class DataItemComparator implements Comparator<DataItem> {

    static DataItemComparator instance = null;

    public static DataItemComparator getInstance() {
        if(instance == null) {
            instance = new DataItemComparator();
        }
        return instance;
    }

    @Override
    public int compare(DataItem o1, DataItem o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    }

}