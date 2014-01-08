package com.merchantry.goncharov;

public class ClientRequestEnvelope {
    private final ClientRequest request;
    private final String clientInfo;
    private volatile ClientRequestStatus status;

    public ClientRequestEnvelope(ClientRequest request, String clientInfo) {
        this.request = request;
        this.clientInfo = clientInfo;
        this.status = ClientRequestStatus.UNKNOWN;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public ClientRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ClientRequestStatus status) {
        this.status = status;
    }
}
