package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
    static Logger log = Logger.getLogger(Client.class);

    private final String host;
    private final int port;
    private Socket server;
    private ObjectOutputStream outputStream;
    private final AtomicLong requestCounter = new AtomicLong();
    private final ConcurrentHashMap<Long, ManageableFuture<Serializable>> requests = new ConcurrentHashMap<Long, ManageableFuture<Serializable>>();
    private final ServerListener serverListener;

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        connect();
        outputStream = new ObjectOutputStream(server.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(server.getInputStream());
        serverListener = new ServerListener(inputStream, requests);
        new Thread(serverListener).start();
    }

    private void connect() {
        try {
            server = new Socket(host, port);
        } catch (IOException e) {
            log.error(String.format("Cannot start session with %s:%d", host, port), e);
        }
        log.debug(String.format("Connected to %s:%d", host, port));
    }

    public Serializable remoteCall(String service, String method, Serializable [] parameters) throws IOException, InterruptedException, ExecutionException {
        long id = requestCounter.getAndIncrement();
        log.info(String.format("Request #%d: call service %s to invoke %s(%s)", id, service, method, Arrays.toString(parameters)));
        return sendRequestAndWait(new MethodInvocationRequest(id, service, method, parameters));
    }

    public Serializable remoteCall(String service, String method) throws IOException, InterruptedException, ExecutionException {
        return remoteCall(service, method, null);
    }

    private Serializable sendRequestAndWait(ClientRequest request) throws IOException, ExecutionException, InterruptedException {
        ManageableFuture<Serializable> f = new ManageableFuture<Serializable>();
        requests.put(request.getId(), f);
        synchronized (outputStream) {
            outputStream.writeObject(request);
            outputStream.flush();
        }
        log.debug(String.format("Request #%d now waits for server to response", request.getId()));
        if (!serverListener.isActive()) {
            throw new IOException("Server listener is down");
        }
        Serializable result;
        try {
            result = f.get();
        } catch (Exception e) {
            throw new ExecutionException(String.format("Request #%d failed", request.getId()), e);
        }
        requests.remove(request.getId());
        log.debug(String.format("Request #%d competed. Raw content: %s", request.getId(), result));
        return result;
    }

    public void disconnect() throws IOException {
        server.close();
    }


}
