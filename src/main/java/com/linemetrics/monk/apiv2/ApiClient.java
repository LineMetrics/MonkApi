package com.linemetrics.monk.apiv2;

import com.linemetrics.api.ILMService;
import com.linemetrics.api.LineMetricsService;
import com.linemetrics.api.datatypes.Base;
import com.linemetrics.api.datatypes.DoubleAverage;
import com.linemetrics.api.exceptions.AuthorizationException;
import com.linemetrics.api.exceptions.ServiceException;
import com.linemetrics.api.returntypes.DataReadResponse;
import com.linemetrics.api.returntypes.DataStream;
import com.linemetrics.api.returntypes.RawDataReadResponse;
import com.linemetrics.api.types.FunctionType;
import com.linemetrics.monk.api.ApiException;
import com.linemetrics.monk.api.IApiClient;
import com.linemetrics.monk.api.RestClient;
import com.linemetrics.monk.api.RestException;
import com.linemetrics.monk.api.auth.HashBasedToken;
import com.linemetrics.monk.api.auth.ICredentials;
import com.linemetrics.monk.api.auth.SecretBasedCredential;
import com.linemetrics.monk.dao.DataItem;
import com.linemetrics.monk.dao.TDB;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Klemens on 13.03.2017.
 */
public class ApiClient implements IApiClient {

    private ICredentials credentials;
    private ILMService api;

    public ApiClient(String uri, ICredentials creds) throws RestException {
        this.credentials = creds;
        try {
            api = new LineMetricsService(((SecretBasedCredential)credentials).getClientId(), ((SecretBasedCredential)credentials).getClientSecret());
        } catch(ServiceException e){
            throw new RestException(e.getMessage(), 500);
        }
    }

    @Override
    public List<DataItem> getRangeOptimized(Number dataStreamId, long time_from, long time_to, TDB tdb, TimeZone tz) throws ApiException {
        final List<DataItem> result = new ArrayList<>();
        if(api != null) {
            int authErrorCounter = 0;
            boolean run = true;

            while(run){
                run = false;
                try {
                    DataStream ds = (DataStream) api.loadObject("2c7c94eb76df497d90e33cdf9f97c5f4"); //TODO replace with dataStreamId

                    System.out.println(String.format("Call API with params time_from: %s, " +
                                    "time_to: %s, timezone: %s, granularity: %s, type: %s"
                        , new Date(time_from), new Date(time_to), tz.getID(), "PT1M", "raw"));

                    List<DataReadResponse> list = ds.loadData(new Date(time_from), new Date(time_to), tz.getID(), "PT1M", FunctionType.RAW);
                    System.out.println("Resultlist size: "+list.size());
                    if (list != null) {
                        for (DataReadResponse entry : list) {
                            if (entry instanceof RawDataReadResponse) {
                                DataItem item = mapItem((RawDataReadResponse) entry, null);
                                if (item != null) {
                                    result.add(item);
                                }
                            }
                        }
                    }
                } catch(AuthorizationException e){
                    e.printStackTrace(); //TODO remove
                    //if token is expired run again
                    if(e.getMessage() != null && "Access denied. Auth Token expired?".equalsIgnoreCase(e.getMessage())){
                        authErrorCounter++;
                        if(authErrorCounter <= 1){
                            run = true;
                            try {
                                api.refreshToken();
                            } catch(ServiceException se){
                                throw new ApiException(se.getMessage(), se);
                            }
                        }
                    }
                } catch(ServiceException e){
                    throw new ApiException(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    private DataItem mapItem(final RawDataReadResponse entry, Base type){
        if(entry != null && entry.getData() instanceof DoubleAverage){
            DoubleAverage da = ((DoubleAverage)entry.getData());
            return new DataItem(da.getMinimum(), da.getMaximum(), da.getValue(), da.getTimestamp().getTime());
        }
        return null;
    }

    @Override
    public HashBasedToken getToken(final String hash) throws ApiException {
        return null;
    }
}