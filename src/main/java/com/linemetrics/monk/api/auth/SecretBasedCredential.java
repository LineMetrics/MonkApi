package com.linemetrics.monk.api.auth;

import com.linemetrics.monk.api.ApiClient;
import com.linemetrics.monk.api.IApiClient;
import com.linemetrics.monk.api.RestException;
import org.apache.http.HttpRequest;

/**
 * Created by Klemens on 14.03.2017.
 */
public class SecretBasedCredential implements ICredentials {

    private IApiClient client;

    private String clientId;
    private String clientSecret;

    public SecretBasedCredential(String clientId, String clientSecret){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public void initialize(IApiClient client) throws RestException {
        this.client = client;
    }

    @Override
    public void authenticate(HttpRequest req) {

    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
