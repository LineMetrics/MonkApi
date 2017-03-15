package com.linemetrics.monk.api;

public class ApiManager {

    static IApiClient apiClient;

    public static void setClient(IApiClient api) {
        ApiManager.apiClient = api;
    }

    public static IApiClient getClient() {
        return ApiManager.apiClient;
    }

}
