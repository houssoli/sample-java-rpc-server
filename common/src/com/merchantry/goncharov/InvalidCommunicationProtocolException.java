package com.merchantry.goncharov;

/**
 * Indicates that client or server violates protocol
 */
public class InvalidCommunicationProtocolException extends Exception {
    public InvalidCommunicationProtocolException(String message) {
        super(message);
    }
}
