package com.merchantry.goncharov;

import java.io.Serializable;

public class MethodInvocationRequest extends ClientRequest {
    private final String method;
    private final String service;
    private final Serializable[] parameters;

    public MethodInvocationRequest(long id, String service, String method , Serializable[] parameters) {
        super(id);
        this.method = method;
        this.service = service;
        this.parameters = parameters;
    }

    public String getMethod() {
        return method;
    }

    public String getService() {
        return service;
    }

    public Serializable[] getParameters() {
        return parameters;
    }
}
