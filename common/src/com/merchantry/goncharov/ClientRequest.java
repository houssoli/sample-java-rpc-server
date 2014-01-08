package com.merchantry.goncharov;

import java.io.Serializable;

public class ClientRequest implements Serializable {
    private final long id;

    public long getId() {
        return id;
    }

    public ClientRequest(long id) {
        this.id = id;
    }
}