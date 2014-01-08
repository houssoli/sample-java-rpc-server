package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ClientRequestExecutor implements Runnable {
    static Logger log = Logger.getLogger(ClientRequestExecutor.class);
    private final ClientRequestEnvelope requestEnvelope;
    private final ObjectOutputStream outputStream;

    public ClientRequestExecutor(ClientRequestEnvelope requestEnvelope, ObjectOutputStream out) {
        this.requestEnvelope = requestEnvelope;
        this.outputStream = out;
    }

    private ServerResponse execute() {
        ClientRequest request = requestEnvelope.getRequest();
        try {
            Object result;
            if (request instanceof MethodInvocationRequest) {
                MethodInvocationRequest r = (MethodInvocationRequest) request;
                result = ServiceInvoker.getInstance().invoke(r.getService(), r.getMethod(), r.getParameters());
            } else {
                throw new InvalidCommunicationProtocolException("Unknown request type");
            }

            ServerResponseStatus status = ServerResponseStatus.SUCCESSFUL_WITH_CONTENT;

            if (!(result instanceof Serializable)) {
                status = ServerResponseStatus.SUCCESSFUL_WITHOUT_CONTENT;
                result = null;
            }
            return new ServerResponse(request.getId(), status, (Serializable) result);
        } catch (Exception e) {
            log.error(String.format("During execution of request #%d from %s", request.getId(), requestEnvelope.getClientInfo()), e);
            return new ServerResponse(request.getId(), ServerResponseStatus.FAILED, e);
        }
    }

    @Override
    public void run() {
        try {
            if (requestEnvelope.getStatus() == ClientRequestStatus.CANCELLED) return;
            ServerResponse result = execute();
            if (requestEnvelope.getStatus() == ClientRequestStatus.CANCELLED) return;
            synchronized (outputStream) {
                outputStream.writeObject(result);
                outputStream.flush();
            }
            requestEnvelope.setStatus(ClientRequestStatus.PROCESSED);
        } catch (IOException e) {
            log.error(String.format("Cannot send result of request #%d from %s", requestEnvelope.getRequest().getId(), requestEnvelope.getClientInfo()), e);
        }
    }
}