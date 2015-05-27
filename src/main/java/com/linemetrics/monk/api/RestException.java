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

public class RestException extends Exception {

    private int status;
    private String result;

    public RestException(String msg, int status) {
        super(msg);

        this.status = status;
    }

    public RestException(String msg, int status, String result) {
        this(msg, status);

        this.result = result;
    }

    public int getHttpStatusCode() {
        return status;
    }

    public String getHttpResult() {
        return result;
    }

    public String getMessage() {
        if (result == null) {
            return String.format("%s %s", Integer.toString(status), super.getMessage());
        } else {
            return String.format("%s %s: %s", Integer.toString(status), super.getMessage(), result);
        }
    }
}