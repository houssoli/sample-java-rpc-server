package com.merchantry.goncharov;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerApplication {
    static Logger log = Logger.getLogger(ServerApplication.class);

    public static void main (String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        int portNumber;
        try {
            portNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printHelp();
            return;
        }

        Server server = new Server();

        Properties p = new Properties();

        try {
            p.load(new FileInputStream("server.properties"));
        } catch (IOException e) {
            log.error("Cannot load server.properties file:", e);
            return;
        }

        server.addServices(p.entrySet());
        server.ListenTo(portNumber);
    }

    private static void printHelp() {
        System.out.println("Use following command-line format to start server:\n");
        System.out.println("...ServerApplication <port>\n");
        System.out.println("<port> - port number to listen");
    }
}