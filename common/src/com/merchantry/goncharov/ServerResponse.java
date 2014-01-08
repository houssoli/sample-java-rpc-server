package com.merchantry.goncharov;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private final Serializable content;
    private final ServerResponseStatus status;
    private final long id;

    public ServerResponseStatus getStatus() {
        return status;
    }

    public Serializable getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public ServerResponse(long id, ServerResponseStatus status, Serializable content) {
        this.status = status;
        this.content = content;
        this.id = id;
    }
}
