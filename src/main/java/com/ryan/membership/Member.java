package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    // process command line inputs
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

    private void joinGroup() {
        // Do nothing if already joined
        if (joined) return;

        // Initialize self-identity
        this.selfEntry = new MembershipEntry(host, port, timestamp);

        // TODO: Get info of a running process from introducer

        // TODO: Get the membership list from that process. If cannot, get
        // another process from the introducer.

        // TODO: Start a TCP listener thread

        // TODO: Broadcast current join via TCP

        // TODO: Start gossip protocol thread, communicating via UDP

        joined = true;
    }

    private void leaveGroup() {
    }

}
