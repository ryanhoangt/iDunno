package com.ryan.membership;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length < 1)
            throw new IllegalArgumentException("Not enough arguments");

        if (args.length == 1) {
            // starting introducer only needs port number
            new Introducer(Integer.parseInt(args[0])).start();
        } else {
            // starting member needs port number and introducer details
            int port = Integer.parseInt(args[0]);
            String introducerHost = args[1];
            int introducerPort = Integer.parseInt(args[2]);

            new Member(InetAddress.getLocalHost().getHostName(), port, introducerHost, introducerPort).start();
        }
    }
}
