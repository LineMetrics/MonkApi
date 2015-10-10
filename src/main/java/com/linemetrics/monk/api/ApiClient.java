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

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import com.linemetrics.monk.api.auth.HashBasedToken;
import com.linemetrics.monk.api.auth.ICredentials;
import com.linemetrics.monk.api.helper.DataItemComparator;
import com.linemetrics.monk.dao.DataItem;
import com.linemetrics.monk.dao.TDB;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

public class ApiClient {

    public static final String DEFAULT_API_REV = "v1";
    public static String apirev = DEFAULT_API_REV;

    public static int MAX_RETRIES = 720;
    public static int WAIT_MILLIS_AFTER_ERROR = 20 * 1000;

    public static int OPERATION_TIME_OUT = 10 * 1000;
    public static int CONNECTION_TIME_OUT = 10 * 1000;
    public static int SOCKET_TIME_OUT = 10 * 1000;
    public static int WAIT_BETWEEN_OPTIMIZE_QUERY = 1 * 1000;

    private RestClient restclient = null;

    public ApiClient(String uri, ICredentials creds) throws RestException {

        RequestConfig requestConfig =
            RequestConfig.custom()
                .setConnectTimeout(OPERATION_TIME_OUT)
                .setConnectionRequestTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT)
                .build();
        HttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        restclient = new RestClient(httpclient, creds, URI.create(uri));

        creds.initialize(this);
    }

    public static String getApiRev() {
        return apirev;
    }

    public static void setApiRev(String apirev) {
        ApiClient.apirev = apirev;
    }

    public static String getBaseUri() {
        return String.format("/%s", apirev);
    }

    public DataItem getLastValue(
            Number dataStreamId,
            final TDB tdb,
            final TimeZone tz)
        throws ApiException {

        try {
            URI uri = restclient.buildURI(
                getBaseUri() + "/lastvalue/" + dataStreamId,
                new HashMap<String, String>() {{
                    put("tdb", "" + tdb.getMilliseconds());
                    put("time_offset", "" + (tz.getRawOffset() + tz.getDSTSavings()));
                }});

            JSONObject result = restclient.get(uri);
            if(result.containsKey("data")) {
                return new DataItem(result.get("data"));
            }
        } catch (Exception ex) {
            throw new ApiException(
                "Unable to retrieve last value of dataStream (" + dataStreamId + "): " + ex.getMessage(), ex
            );
        }

        return null;
    }

    public List<DataItem> getRangeOptimized(
            final Number dataStreamId,
            long time_from,
            long time_to,
            TDB tdb,
            TimeZone tz)
        throws ApiException {

        try {
            long timeDiff     = time_to - time_from;
            long maxTimeRange = tdb.getQueryLimit();
            long queryRange   = tdb.getQueryRange();

            if(timeDiff < maxTimeRange) {
                return this.getRange(dataStreamId, time_from, time_to, tdb, tz);
            }

            long millis = System.currentTimeMillis();

            ExecutorService executorService = Executors.newSingleThreadExecutor();

            Set<Future<List<DataItem>>> callables = new HashSet<Future<List<DataItem>>>();

            long queryStart = time_from;
            long queryEnd   = time_from + queryRange;

            while(queryStart < time_to) {

                callables.add(
                    executorService.submit(
                        new CallableRangeQuery(
                        dataStreamId,
                        queryStart,
                        queryEnd,
                        tdb,
                        tz)
                    ));

                queryStart += queryRange;
                queryEnd   += queryRange;
            }

            executorService.shutdown();

            List<DataItem> list = new ArrayList<>();
            for(Future<List<DataItem>> future : callables){
                List<DataItem> slice = future.get();
                if(slice == null) {
                    throw new ApiException("Error while retrieving slice :(");
                } else {
                    list.addAll(slice);
                }
            }

            executorService.awaitTermination(60 * 60 * 1000L, TimeUnit.MILLISECONDS);

            System.out.print("Optimized Range Query takes: " + (System.currentTimeMillis() - millis) + "ms ");

//            System.out.println(list.size());
//
            Collections.sort(list, DataItemComparator.getInstance());

            DataItem prevItem = null, currItem;
            DataItem beginSlice = null;

            Iterator<DataItem> itemIterator = list.iterator();
            while(itemIterator.hasNext()) {
                currItem = itemIterator.next();

                if(prevItem != null) {
                    if(prevItem.getTimestamp().equals(currItem.getTimestamp())) {
                        itemIterator.remove();
                        continue;
                    }

                    if(beginSlice == null) {
                        if(currItem.getTimestamp() - prevItem.getTimestamp() > tdb.getMilliseconds()) {
                            beginSlice = prevItem;
                        }
                    } else {
                        if(currItem.getTimestamp() - prevItem.getTimestamp() == tdb.getMilliseconds()) {
                            System.out.println("TimeRange " + beginSlice.getTimestamp() + " - " + prevItem.getTimestamp()+ " " + (prevItem.getTimestamp() - beginSlice.getTimestamp()) + " ms missing!");
                            beginSlice = null;
                        }
                    }
                }
                prevItem = currItem;
            }

            if(beginSlice != null) {
                System.out.println("TimeRange " + beginSlice.getTimestamp() + " - " + prevItem.getTimestamp() + " " + (prevItem.getTimestamp() - beginSlice.getTimestamp()) + " ms missing!");
            }

            long expectedItems = ((time_to - time_from) / tdb.getMilliseconds()) - 1;
            System.out.println(" (" + (list.size() - expectedItems) + ")");

            return list;

        } catch(Exception e) {
            throw new ApiException(e.getMessage());
        }
    }



    public List<DataItem> getRange(
            Number dataStreamId,
            final long time_from,
            final long time_to,
            final TDB tdb,
            final TimeZone tz)
        throws ApiException {

        List<DataItem> list = new ArrayList<>();

        Map<String, String> reqParameters =
            new HashMap<String, String>() {{
                put("tdb", "" + tdb.getMilliseconds());
                put("time_to", "" + time_to);
                put("time_from", "" + time_from);
                put("time_offset", "" + (tz.getRawOffset() + tz.getDSTSavings()));
            }};
        String reqURl = getBaseUri() + "/data/" + dataStreamId;

        System.out.print("URL: " +  reqURl + " -> " + reqParameters);

        int retries = MAX_RETRIES;
        boolean isError = false;

        while(retries-- >= 0) {

            if(isError) {
                System.out.println("WAIT CAUSE OF ERROR AND RETRY (" + (MAX_RETRIES - retries) + ")...");
                try {
                    Thread.sleep(WAIT_MILLIS_AFTER_ERROR);
                } catch(InterruptedException iexp) {
//                    iexp.printStackTrace();
                }
            }

            long millis = System.currentTimeMillis();
            isError = false;

            try {
                URI uri = restclient.buildURI(reqURl, reqParameters);

                JSONObject result = restclient.get(uri);

                if (result.containsKey("data") && result.get("data") instanceof List) {
                    for (Object dataItem : (List) result.get("data")) {
                        DataItem di = new DataItem(dataItem);
                        di.setTimestampEnd(di.getTimestampStart() + tdb.getMilliseconds());
                        list.add(di);
                    }

                    if (list.isEmpty()) {
                        System.out.print(" DATA BUT EMPTY ");
                    }
                    break;
                } else {
                    System.out.print(" NO DATA ");
                    isError = true;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.print(" UNABLE: " + ex.getMessage()); isError = true;
            } finally {
                System.out.println(": " + (System.currentTimeMillis() - millis) + "ms ");
            }
        }

        if(isError) {
            throw new ApiException("Unable to grab data");
        }

        Collections.sort(list, DataItemComparator.getInstance());

        DataItem currItem;

        Iterator<DataItem> itemIterator = list.iterator();
        while(itemIterator.hasNext()) {
            currItem = itemIterator.next();

            if(currItem.getTimestamp() < time_from ||
                currItem.getTimestamp() > time_to) {

                itemIterator.remove();
            }
        }

        return list;
    }

    public HashBasedToken getToken(final String hash)
        throws ApiException {

        try {
            URI uri = restclient.buildURI(
                getBaseUri() + "/auth",
                new HashMap<String, String>() {{
                    put("basic", hash);
                }});
            JSONObject result = restclient.get(uri, false);
            return new HashBasedToken(this.restclient, result);
        } catch (Exception ex) {
            System.out.println("Unable to retrieve token!");
            throw new ApiException(
                "Unable to retrieve auth token: " + ex.getMessage(), ex
            );
        }
    }

    public class CallableRangeQuery implements Callable<List<DataItem>> {

        long queryStart;
        long queryEnd;

        Number dataStreamId;
        TDB tdb;
        TimeZone tz;

        public CallableRangeQuery(Number dataStreamId,
                                  long queryStart,
                                  long queryEnd,
                                  TDB tdb,
                                  TimeZone tz) {

            this.queryStart = queryStart;
            this.queryEnd = queryEnd;

            this.tz       = tz;
            this.tdb      = tdb;
            this.dataStreamId = dataStreamId;
        }

        public List<DataItem> call() throws Exception {
            try {
                Thread.sleep(WAIT_BETWEEN_OPTIMIZE_QUERY);
                return getRange(dataStreamId, queryStart, queryEnd, tdb, tz);
            } catch (ApiException apiExp) {
//                apiExp.printStackTrace();
                return null;
            }
        }
    }
}
