package com.linemetrics.monk.apiv2;

import com.linemetrics.api.ILMService;
import com.linemetrics.api.LineMetricsService;
import com.linemetrics.api.datatypes.Base;
import com.linemetrics.api.datatypes.Double;
import com.linemetrics.api.datatypes.DoubleAverage;
import com.linemetrics.api.exceptions.AuthorizationException;
import com.linemetrics.api.exceptions.ServiceException;
import com.linemetrics.api.returntypes.DataReadResponse;
import com.linemetrics.api.returntypes.DataStream;
import com.linemetrics.api.returntypes.RawDataReadResponse;
import com.linemetrics.api.types.FunctionType;
import com.linemetrics.monk.api.ApiException;
import com.linemetrics.monk.api.IApiClient;
import com.linemetrics.monk.api.RestException;
import com.linemetrics.monk.api.auth.HashBasedToken;
import com.linemetrics.monk.api.auth.ICredentials;
import com.linemetrics.monk.api.auth.SecretBasedCredential;
import com.linemetrics.monk.dao.DataItem;
import com.linemetrics.monk.dao.TDB;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
    private JSONObject jobProperties;

    public static int MAX_RETRIES = 5;
    public static int WAIT_MILLIS_AFTER_ERROR = 10 * 1000;

    public ApiClient(String uri, ICredentials creds) throws RestException {
        this.credentials = creds;
        try {
            if(credentials == null || !(credentials instanceof  SecretBasedCredential)){
                throw new RestException("Invalid credential settings", 500);
            }
            api = new LineMetricsService(((SecretBasedCredential)credentials).getClientId(), ((SecretBasedCredential)credentials).getClientSecret());
        } catch(ServiceException e){
            throw new RestException(e.getMessage(), 500);
        }
    }

    @Override
    public void setJobProperties(String properties) {
        this.jobProperties = (JSONObject) JSONValue.parse(properties);
    }

    private String extractFieldFromJson(JSONObject object, String key){
        return object!=null&&object.containsKey(key)?(String)object.get(key):null;
    }

    @Override
    public List<DataItem> getRangeOptimized(final String dataStreamProperties, long time_from, long time_to, TDB tdb, TimeZone tz) throws ApiException {
        final List<DataItem> result = new ArrayList<>();
        JSONObject datastream = (JSONObject)JSONValue.parse(dataStreamProperties);

        String object_id = extractFieldFromJson(datastream, "id");
        String customKey = extractFieldFromJson(datastream, "customkey");
        String alias = extractFieldFromJson(datastream, "alias");
        String granularity = extractFieldFromJson(this.jobProperties, "batch_size");

        if(api != null) {
            int retries = MAX_RETRIES;
            boolean isError = false;

            while(retries-- >= 0){

                if(isError) {
                    System.out.println("WAIT CAUSE OF ERROR AND RETRY (" + (MAX_RETRIES - retries) + ")...");
                    try {
                        Thread.sleep(WAIT_MILLIS_AFTER_ERROR);
                    } catch(InterruptedException iexp) { }
                }
                isError = false;

                try {
                    DataStream ds = null;
                    if(StringUtils.isNotEmpty(customKey) && StringUtils.isNotEmpty(alias)){
                        ds = (DataStream) api.loadObject(customKey, alias);
                    } else if(StringUtils.isNotEmpty(object_id)){
                        ds = (DataStream) api.loadObject(object_id);
                    } else {
                        System.err.println(String.format("Invalid properties for datastream"));
                        return result;
                    }

                    List<DataReadResponse> list = ds.loadData(new Date(time_from), new Date(time_to), tz.getID(), StringUtils.isNotEmpty(granularity)?
                        granularity.toUpperCase():"PT1M", FunctionType.RAW);

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

                    //succesfully loaded data
                    break;

                } catch(AuthorizationException e){
                    //check if token is expired - if yes -> refresh
                    try {
                        api.refreshToken();
                    } catch(ServiceException se){
                        throw new ApiException(se.getMessage(), se);
                    }
                } catch(ServiceException e){
                    e.printStackTrace();
                    isError = true;
                } catch(Exception e){
                    e.printStackTrace();
                    throw new ApiException(e.getMessage(), e);
                }
            }

            if(isError){
                throw new ApiException("Unable to grab data");
            }
        }
        return result;
    }

    private DataItem mapItem(final RawDataReadResponse entry, Base type){
        if(entry != null && entry.getData() instanceof DoubleAverage){
            DoubleAverage da = ((DoubleAverage)entry.getData());
            return new DataItem(da.getMinimum(), da.getMaximum(), da.getValue(), da.getTimestamp().getTime(), da.getTimestamp().getTime());
        } else if(entry != null && entry.getData() instanceof Double){
            Double d = ((Double)entry.getData());
            return new DataItem(d.getValue(), d.getValue(), d.getValue(), d.getTimestamp().getTime(), d.getTimestamp().getTime());
        } else {
            System.err.println(String.format("Unknown Datatype: %", type.toString()));
        }
        return null;
    }

    @Override
    public HashBasedToken getToken(final String hash) throws ApiException {
        return null;
    }
}