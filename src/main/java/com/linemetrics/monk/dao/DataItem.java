/**
 * Copyright (c) 2015 by LineMetrics GmbH
 * Author: Thomas Pillmayr <t.pillmayr@linemetrics.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.linemetrics.monk.dao;

import com.linemetrics.monk.api.Util;

import java.util.List;
import java.util.Map;

public class DataItem {

    private Double min          = null;
    private Double max          = null;
    private Double value        = null;
    private Long timestampStart = null;
    private Long timestampEnd   = null;

    public DataItem(Object json) {
        if (json != null) {
            if (json instanceof Map)
                deserialiseMap((Map) json);
            else if (json instanceof List)
                deserialiseList((List) json);
        }
    }

    public DataItem(Double min, Double max, Double value, Long timestampStart){
        this.min = min;
        this.max = max;
        this.value = value;
        this.timestampStart = timestampStart;
    }

    private DataItem() {}

    public static DataItem empty() {
        return new DataItem();
    }

    private void deserialiseMap(Map map) {
        if (map.containsKey(map.get("max")) &&
                map.containsKey(map.get("max"))) {
            min = Util.getDouble(map.get("min" ));
            max = Util.getDouble(map.get("max" ));
        }

        value = Util.getDouble(map.get("value" ));
        timestampStart = Util.getLong(map.get("timestamp" ));
    }

    private void deserialiseList(List list) {
        value = Util.getDouble(list.get(1));
        timestampStart = Util.getLong(list.get(0));

        if(list.size() == 4) {
            min = Util.getDouble(list.get(2));
            max = Util.getDouble(list.get(3));
        }
    }


    @Override
    public String toString() {
        if (issetMin() && issetMax()) {
            return "DataItem{" +
                "min=" + min +
                ", max=" + max +
                ", value=" + value +
                ", timestampStart=" + timestampStart +
                ", timestampEnd=" + timestampEnd +
                '}';
        } else {
            return "DataItem{" +
                "value=" + value +
                ", timestampStart=" + timestampStart +
                ", timestampEnd=" + timestampEnd +
                '}';
        }
    }

    public boolean issetMin() {
        return min != null;
    }

    public boolean issetMax() {
        return max != null;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestampStart;
    }

    public void setTimestamp(Long timestamp) {
        this.timestampStart = timestampStart;
    }

    public Long getTimestampStart() {
        return timestampStart;
    }

    public void setTimestampStart(Long timestampStart) {
        this.timestampStart = timestampStart;
    }

    public Long getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(Long timestampEnd) {
        this.timestampEnd = timestampEnd;
    }
}
