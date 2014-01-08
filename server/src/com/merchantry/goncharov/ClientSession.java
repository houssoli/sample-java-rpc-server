package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * Handle connection with single client.
 * Utilises ExecutorService thread to do actual work
 */
public class ClientSession implements Runnable {
    Logger log = Logger.getLogger(ClientSession.class);

    private final Socket client;
    private final ExecutorService executor;
    private final Collection<ClientRequestEnvelope> requestEnvelopes = new LinkedList<ClientRequestEnvelope>();

    public ClientSession(Socket client, ExecutorService executor) {
        this.client = client;
        this.executor = executor;
    }

    @Override
    public void run() {
        String clientInfo = String.format("%s:%d", client.getInetAddress(), client.getPort());
        try {
            try {
                log.debug(String.format("Started session with %s", clientInfo));
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ClientRequest request;
                while (true) {
                    try {
                        request = (ClientRequest) in.readObject();
                        if (request == null) break;
                        ClientRequestEnvelope envelope = new ClientRequestEnvelope(request, clientInfo);
                        log.debug(String.format("Put request #%d from %s to queue", request.getId(), envelope.getClientInfo()));
                        envelope.setStatus(ClientRequestStatus.QUEUED);
                        requestEnvelopes.add(envelope);
                        executor.submit(new ClientRequestExecutor(envelope, out));
                    } catch (ClassNotFoundException e) {
                        log.error(String.format("Cannot understand request from %s", clientInfo), e);
                    }
                }
            } catch (SocketException e) {
                log.debug(String.format("No longer listening for %s. Cause is", clientInfo), e);
            } catch (IOException e) {
                log.debug(String.format("No longer listening for %s. Cause is", clientInfo), e);
            }
        } finally {
            for (ClientRequestEnvelope r : requestEnvelopes) {
                if (r.getStatus() != ClientRequestStatus.PROCESSED) {
                    r.setStatus(ClientRequestStatus.CANCELLED);
                    log.warn(String.format("Cancelling client %s request #%s", r.getClientInfo(), r.getRequest().getId()));
                }
            }
        }
    }
}
