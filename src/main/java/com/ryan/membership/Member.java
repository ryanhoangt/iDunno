package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Member {

    // Member and introducer info
    private String host;
    private int port;
    private Date timestamp;
    private String introducerHost;
    private int introducerPort;

    // Membership list and self entry
    private MembershipList membershipList;
    private MembershipEntry selfEntry;

    // Other
    private boolean joined;

    public Member(String host, int port, String introducerHost, int introducerPort) {
        this.host = host;
        this.port = port;
        this.introducerHost = introducerHost;
        this.introducerPort = introducerPort;
    }

    /**
     * Process command line inputs
     */

    public void start() {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("MemberProcess$ ");
                String command = stdin.readLine();

                switch (command) {
                    case "join":
                        joinGroup();
                        break;
                    case "leave":
                        leaveGroup();
                        break;
                    case "list_mem":
                        // TODO:
                        break;
                    case "list_self":
                        // TODO:
                        break;
                    default:
                        System.out.println("Unrecognized command, type 'join', 'leave', 'list_mem', or 'list_self'");
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private void joinGroup() throws IOException, ClassNotFoundException {
        // do nothing if already joined
        if (joined) return;

        // initialize self-identity
        this.timestamp = new Date();
        this.selfEntry = new MembershipEntry(host, port, timestamp);

        // get info of a running process from introducer
        MembershipEntry runningProcess = getRunningProcess();

        // TODO: Get the membership list from that process. If cannot, get
        // another process from the introducer.

        // TODO: Start a TCP listener thread

        // TODO: Broadcast current join via TCP

        // TODO: Start gossip protocol thread, communicating via UDP

        joined = true;
    }

    private MembershipEntry getRunningProcess() throws IOException, ClassNotFoundException {
        try (Socket introducerConn = new Socket(introducerHost, introducerPort);
            ObjectOutputStream oout = new ObjectOutputStream(introducerConn.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(introducerConn.getInputStream())) {

            // send self entry to the introducer
            oout.writeObject(selfEntry);
            oout.flush();

            return (MembershipEntry) oin.readObject();
        }
    }

    private void leaveGroup() {
        // TODO:
        throw new UnsupportedOperationException();
    }

}
