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

package com.linemetrics.monk.api;

public class Util {

    /**
     * Gets a string from the given object.
     *
     * @param s a String instance
     *
     * @return a String or null if s isn't a String instance
     */
    public static String getString(Object s) {
        String result = null;

        if (s instanceof String)
            result = (String)s;

        return result;
    }

    /**
     * Gets an floating-point number from the given object.
     *
     * @param i an Double instance
     *
     * @return an floating-point number or null if i isn't a Double instance
     */
    public static Double getDouble(Object i) {
        Double result = null;

        if (i instanceof Double)
            result = (Double)i;

        return result;
    }

    /**
     * Gets an integer from the given object.
     *
     * @param i an Integer instance
     *
     * @return an integer primitive or 0 if i isn't an Integer instance
     */
    public static int getInteger(Object i) {
        int result = 0;

        if (i instanceof Integer)
            result = ((Integer)i).intValue();

        return result;
    }

    /**
     * Gets an integer from the given object.
     *
     * @param i an Long instance
     *
     * @return an long primitive or 0 if i isn't an Long instance
     */
    public static long getLong(Object i) {
        long result = 0;

        if (i instanceof Long)
            result = ((Long)i).longValue();

        return result;
    }

    /**
     * Gets an number from the given object.
     *
     * @param i an Number instance
     *
     * @return an Numbe instance or NULL
     */
    public static Number getNumber(Object i) {
        if (i instanceof Number) {
            return (Number)i;
        }
        return null;
    }

}
