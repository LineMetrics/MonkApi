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

package com.linemetrics.monk.api.auth;

import com.linemetrics.monk.api.RestClient;
import com.linemetrics.monk.api.Util;
import org.json.simple.JSONObject;

import java.util.Map;

public class HashBasedToken {

    public String token;
    public Number expires;
    public long generationTime;

    public HashBasedToken(RestClient restclient, JSONObject json) {
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        Map map = json;

        token = Util.getString(map.get("token" ));
        expires = Util.getNumber(map.get("expires" ));
        generationTime = System.currentTimeMillis();
    }

    public boolean valid() {
        return
            (this.generationTime +
                ((expires.intValue() - 60) * 1000)) > System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "HashBasedToken{" +
            "token='" + token + '\'' +
            ", expires=" + expires +
            ", generationTime=" + generationTime +
            '}';
    }

    public String getToken() {
        return token;
    }
}
