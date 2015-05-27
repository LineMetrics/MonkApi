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

public enum TDB {

    Raw, Minute, Hour, Day;

    public long getMilliseconds() {
        switch(this) {
            case Raw:       return        0;
            case Minute:    return    60000;
            case Hour:      return  3600000;
            case Day:       return 86400000;
        }
        return 0;
    }

    public static TDB fromMilliseconds(long millis) throws Exception {
        if(millis == TDB.Raw.getMilliseconds())         return Raw;
        else if(millis == TDB.Minute.getMilliseconds()) return Minute;
        else if(millis == TDB.Hour.getMilliseconds())   return Hour;
        else if(millis == TDB.Day.getMilliseconds())    return Day;
        throw new Exception("Invalid TDB given!");
    }

    public long getQueryLimit() throws Exception {
        switch(this) {
            case Raw:       return (        3 * 60 * 60 * 1000);
            case Minute:    return (       12 * 60 * 60 * 1000);
            case Hour:      return (  31 * 24 * 60 * 60 * 1000);
            case Day:       return ( 372 * 24 * 60 * 60 * 1000);
        }
        throw new Exception("Invalid TDB given!");
    }

    public long getQueryRange() throws Exception {
        switch(this) {
            case Raw:       return (        1 * 60 * 60 * 1000);
            case Minute:    return (        6 * 60 * 60 * 1000);
            case Hour:      return (   7 * 24 * 60 * 60 * 1000);
            case Day:       return (  30 * 24 * 60 * 60 * 1000);
        }
        throw new Exception("Invalid TDB given!");
    }

}
