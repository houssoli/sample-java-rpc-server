package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerListener implements Runnable {
    static Logger log = Logger.getLogger(Client.class);

    private final ObjectInputStream inputStream;
    private final ConcurrentHashMap<Long, ManageableFuture<Serializable>> requests;
    private volatile boolean active = false;

    public ServerListener(ObjectInputStream inputStream, ConcurrentHashMap<Long, ManageableFuture<Serializable>> requests) {
        this.inputStream = inputStream;
        this.requests = requests;
    }

    @Override
    public void run() {
        active = true;
        try {
            ServerResponse response;
            while (true) {
                try {
                    response = (ServerResponse) inputStream.readObject();
                    if (response == null) break;
                    long id = response.getId();
                    if (requests.containsKey(id)) {
                        switch (response.getStatus()) {
                            case SUCCESSFUL_WITH_CONTENT:
                                requests.get(id).setResult(response.getContent());
                                break;
                            case SUCCESSFUL_WITHOUT_CONTENT:
                                requests.get(id).setResult(null);
                                break;
                            case FAILED:
                                requests.get(id).setException((Exception) response.getContent());
                                break;
                        }
                    } else {
                        log.warn(String.format("Received response with unknown id %d. Ignoring.", id));
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Cannot understand server response", e);
                }
            }
        } catch (SocketException e) {
            log.debug("Disconnected from server. Cause is", e);
        } catch (IOException e) {
            log.error("Disconnected from server. Cause is", e);
        } finally {
            active = false;
            for (Map.Entry<Long, ManageableFuture<Serializable>> entry : requests.entrySet()) {
                entry.getValue().cancel(false);
            }
        }
    }

    public boolean isActive() {
        return active;
    }
}
