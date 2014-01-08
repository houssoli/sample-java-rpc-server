package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class ClientApplication {
    static Logger log = Logger.getLogger(ClientApplication.class);
    static Client client;
    private final static Random random = new Random();

    public static void main(String[] args) {
        //defaults
        String host = "localhost";
        int port = 1337;
        int threadCount = 20;

        if (args.length > 0) {
            host = args[0];
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            if (args.length > 2) {
                try {
                    threadCount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        final CountDownLatch latch = new CountDownLatch(threadCount);

        try {
            client = new Client(host, port);
            for (int i = 0; i < threadCount; i++) {
                (new Thread() {
                    @Override
                    public void run() {
                        try {
                            callRandomMethod();
                        } catch (Exception e) {
                            log.error("Client failed to execute request", e);
                        } finally {
                            latch.countDown();
                        }
                    }
                }).start();
            }
            latch.await();
            client.disconnect();
        } catch (IOException e) {
            log.error("", e);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    private static void callRandomMethod() throws InterruptedException, ExecutionException, IOException, InvalidCommunicationProtocolException {
        int c = random.nextInt(9); // http://stackoverflow.com/questions/5819638/is-random-class-thread-safe
        switch (c) {
            case 0:
                Object[] rows = (Object[]) client.remoteCall("sample", "databaseQueryMock", new Serializable[]{"SELECT * FROM USERS", new Long(5000)});
                StringBuilder message = new StringBuilder();
                message.append("databaseQueryMock returned:");
                for (int i = 0; i < rows.length; i++) {
                    Object[] row = (Object[]) rows[i];
            message.append(String.format("\n(%s, %s)", row[0], row[1]));
        }
        log.info(message.toString());
        break;
        case 1:
        Integer[] values = new Integer[]{random.nextInt(10), random.nextInt(10), random.nextInt(10)};
        Integer sum = (Integer) client.remoteCall("sample", "calculateSum", new Serializable[]{values});
        log.info(String.format("Sum of %s = %d", Arrays.toString(values), sum));
        break;
            case 2:
                String echo = (String) client.remoteCall("sample", "echoString", new String[]{"hello"});
                log.info(String.format("echoed: %s", echo));
                break;
            case 3:
                Date d = (Date) client.remoteCall("sample", "getCurrentDate");
                log.info(String.format("Today is %s", d));
                break;
            case 4:
                client.remoteCall("lazy", "sleep", new Serializable[]{(long) 2000}); //should block other clients of that service
                log.info("just awake");
                break;
            case 5:
                client.remoteCall("lazy", "calculateSum", new Serializable[]{new Integer[]{1, 2, 3}}); //throws exception
                break;
            case 6:
                Object r = client.remoteCall("lazy", "tellMeTheTruth", null); // returns unserializable
                if (r != null) {
                    throw new InvalidCommunicationProtocolException("Client should return null for unserializable result" + r.toString());
                }
                log.info("The truth is out there");
                break;
            case 7:
                client.remoteCall("lazy", "doNothing", new Serializable[]{null});
                throw new InvalidCommunicationProtocolException("Right now server should not able to find method by null parameters");
            case 8:
                client.remoteCall("non-existing service", "call", null);
                throw new InvalidCommunicationProtocolException("Client should not find that service");
        }
    }
}
