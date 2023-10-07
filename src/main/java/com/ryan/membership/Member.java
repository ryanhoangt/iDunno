package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Member {

    // Member and introducer info
    private String host;
    private int port;
    private Date timestamp;
    private String introducerHost;
    private int introducerPort;

    // Sockets
    private ServerSocket tcpServer;
    private DatagramSocket gossipServer;

    // Threads
    private Thread tcpListener;
    private Thread gossipProtocolThread;

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

    public void start() throws ClassNotFoundException {
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

        // get the membership list from that process. If cannot, get
        // another process from the introducer.
        if (runningProcess == null) // first member in group
            this.membershipList = new MembershipList(selfEntry);
        else {
            this.membershipList = requestMembershipList(runningProcess);
            this.membershipList.addEntry(selfEntry); // add self entry to the list
        }

        // start a TCP listener thread
        this.tcpServer = new ServerSocket(port);
        this.tcpListener = new Thread(this::TCPListener);
        this.tcpListener.start();

        // TODO: Broadcast current join via TCP

        // start gossip protocol thread, communicating via UDP
        this.gossipServer = new DatagramSocket(port);
        this.gossipProtocolThread = new Thread(this::GossipProtocol);
        this.gossipProtocolThread.start();

        joined = true;
    }

    // Fetch membership details from a member already in group
    private MembershipList requestMembershipList(MembershipEntry runningProcess) {
        // TODO:
        throw new UnsupportedOperationException();
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

    private void TCPListener() {
        while (true) {
            try {
                Socket reqConn = tcpServer.accept();

                // spawn a new thread to process the request
                new Thread(() -> this.processTCPMessage(reqConn));
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private void processTCPMessage(Socket reqConn) {
        try {
            ObjectOutputStream oout = new ObjectOutputStream(reqConn.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(reqConn.getInputStream());

            // TODO: Read the message

            reqConn.close();
        } catch (IOException e) {
            System.err.println("Error processing TCP request: " + e.getMessage());
        }
    }

    // Sending pings and waiting for acks
    private void GossipProtocol() {
        // start the ping receiver thread
        new PingReceiver(gossipServer).start();

        // TODO: Send ping messages periodically

    }

}
