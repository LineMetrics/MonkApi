package com.linemetrics.monk.api;

import com.linemetrics.monk.api.auth.HashBasedToken;
import com.linemetrics.monk.dao.DataItem;
import com.linemetrics.monk.dao.TDB;

import java.util.List;
import java.util.TimeZone;

/**
 * Created by Klemens on 13.03.2017.
 */
public interface IApiClient {

    public void setJobProperties(String properties);

    public List<DataItem> getRangeOptimized(
            final String dataStreamProperties,
            long time_from,
            long time_to,
            TDB tdb,
            TimeZone tz)
            throws ApiException;

    public HashBasedToken getToken(final String hash) throws ApiException;
}
