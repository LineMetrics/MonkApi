package com.linemetrics.monk.api;

public class ApiManager {

    static ApiClient apiClient;

    public static void setClient(ApiClient api) {
        ApiManager.apiClient = api;
    }

    public static ApiClient getClient() {
        return ApiManager.apiClient;
    }

}
