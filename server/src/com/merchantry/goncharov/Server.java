package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sample TCP server
 * Right now only supports invocation of other classes so please add them via {@code addServices}.
 * Uses shared pool of threads across all the clients to prevent possible DoS.
 */
public class Server {
    static Logger log = Logger.getLogger(Server.class);

    private final ExecutorService requestExecutorService;
    public Server(int executionThreadsCount) {
        requestExecutorService = Executors.newFixedThreadPool(executionThreadsCount);
    }

    public Server() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Add new definitions of services to be invoked with {@link MethodInvocationRequest}.
     * Only one instance of each service will be created.
     * @param services entry key corresponds to service name, value contains class name
     * @see MethodInvocationRequest
     */
    public void addServices(Set<Map.Entry<Object, Object>> services) {
        ServiceInvoker.getInstance().addServices(services);
    }

    /**
     * Open socket on specified port to accept clients.
     * Runs in current thread thus blocking execution
     * @param port port number
     */
    public void ListenTo(int port) {
        ServerSocket serverSocket;


        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error(String.format("Could not open port %d to listen.", port), e);
            return;
        }

        log.info(String.format("Listening to %d", port));

        ClientSession session;
        Socket client;

        while(!serverSocket.isClosed()) {
            log.debug("Waiting for a new connection");
            try {
                client = serverSocket.accept();
                log.debug(String.format("Client %s:%d accepted", client.getInetAddress(), client.getPort()));
            } catch (IOException e) {
                log.error(String.format("Could not accept client request at :%d", port), e);
                continue;
            }
            session = new ClientSession(client, requestExecutorService);
            new Thread(session).start();
        }
    }
}