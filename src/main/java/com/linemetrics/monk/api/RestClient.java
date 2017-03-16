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

import com.linemetrics.monk.api.auth.ICredentials;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class RestClient {

    private HttpClient httpClient = null;
    private ICredentials creds = null;
    private URI uri = null;

    public RestClient(HttpClient httpclient, URI uri) {
        this(httpclient, null, uri);
    }

    public RestClient(HttpClient httpclient, ICredentials creds, URI uri) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
    }

    public URI buildURI(String path) throws URISyntaxException {
        return buildURI(path, null);
    }

    public URI buildURI(String path, Map<String, String> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder(uri);
        ub.setPath(ub.getPath() + path);

        if (params != null) {
            for (Map.Entry<String, String> ent : params.entrySet())
                ub.addParameter(ent.getKey(), ent.getValue());
        }

        return ub.build();
    }

    private JSONObject request(HttpRequestBase req) throws RestException, IOException {
        return request(req, true);
    }

    private JSONObject request(HttpRequestBase req, Boolean attachAuthHeader) throws RestException, IOException {
        req.addHeader("Accept", "application/json");

        if (creds != null && attachAuthHeader) {
            creds.authenticate(req);
        }


        StringBuilder result = new StringBuilder();

        try {
            HttpResponse resp = httpClient.execute(req);

            HttpEntity ent = resp.getEntity();

            if (ent != null) {
                String encoding = null;
                if (ent.getContentEncoding() != null) {
                    encoding = ent.getContentEncoding().getValue();
                }

                if (encoding == null) {
                    Header contentTypeHeader = resp.getFirstHeader("content-type");
                    HeaderElement[] contentTypeElements = contentTypeHeader.getElements();
                    for (HeaderElement he : contentTypeElements) {
                        NameValuePair nvp = he.getParameterByName("charset");
                        if (nvp != null) {
                            encoding = nvp.getValue();
                        }
                    }
                }

                InputStreamReader isr = encoding != null ?
                    new InputStreamReader(ent.getContent(), encoding) :
                    new InputStreamReader(ent.getContent());
                BufferedReader br = new BufferedReader(isr);
                String line = "";

                while ((line = br.readLine()) != null)
                    result.append(line);
            }

            StatusLine sl = resp.getStatusLine();

            if (sl.getStatusCode() >= 300) {
                throw new RestException(sl.getReasonPhrase(), sl.getStatusCode(), result.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new RestException(e.getMessage(), -1, "");
        } finally {
            req.releaseConnection();
        }

        return (JSONObject)JSONValue.parse(! result.toString().isEmpty() ? result.toString() : "{\"data\":[]}");
    }

    public JSONObject get(URI uri) throws RestException, IOException {
        return get(uri, true);
    }

    public JSONObject get(URI uri, boolean attachAuthHeader) throws RestException, IOException {
        return request(new HttpGet(uri), attachAuthHeader);
    }
}

