package com.chaibytes.bumblebee.data;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by arunesh on 11/4/17.
 */

public class ChartData {
    private List<PointValue> values = new ArrayList<PointValue>();
    private int max = 0;

    public ChartData() {

    }

    public void clear() {
        values.clear();
    }

    public int getSize() {
        return values.size();
    }

    public void add(int value) {
        values.add(new PointValue(values.size(), value));
        max = Math.max(max, value);
    }

    public int getMax() {
        return max;
    }

    public List<PointValue> getValues() {
        return values;
    }
}
