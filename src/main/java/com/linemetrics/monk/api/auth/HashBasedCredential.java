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

import com.linemetrics.monk.api.ApiException;
import com.linemetrics.monk.api.IApiClient;
import org.apache.http.HttpRequest;

public class HashBasedCredential
        implements ICredentials {

    private String hash;

    private IApiClient client;

    private HashBasedToken token;

    public HashBasedCredential(String hash) {
        this.hash = hash;
    }

    public void initialize(IApiClient client) {
        this.client = client;
    }

    @Override
    public void authenticate(HttpRequest req) {

        if(token == null || ! token.valid()) {
            try {
                token = this.client.getToken(hash);
            } catch(ApiException exp) {
                exp.printStackTrace();
            }
        }

        req.addHeader("Authorisation", token.getToken());
    }
}
