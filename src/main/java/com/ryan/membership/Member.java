package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Member {
    // Protocol config
    private static final long PING_INTERVAL_MS = 1500;
    private static final long PING_ACK_TIMEOUT_MS = 1000;

    // Logger
    private static final Logger logger = Logger.getLogger("MemberLogger");

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

    public Member(String host, int port, String introducerHost, int introducerPort) throws IOException {
        this.host = host;
        this.port = port;
        this.introducerHost = introducerHost;
        this.introducerPort = introducerPort;

        // setup logger
        Handler fh = new FileHandler("/var/log/iDunno/dev/membership/member.log");
        fh.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
    }

    /**
     * Process command line inputs
     */

    public void start() throws ClassNotFoundException {
        logger.info("Member process started");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("MemberProcess$ ");
                String command = stdin.readLine();

                switch (command) {
                    case "join":
                        logger.info("Member process received 'join' command");
                        joinGroup();
                        break;
                    case "leave":
                        leaveGroup();
                        break;
                    case "list_mem":
                        if (joined)
                            System.out.println(membershipList);
                        else
                            System.out.println("Not joined.");
                        break;
                    case "list_self":
                        if (joined)
                            System.out.println(selfEntry);
                        else
                            System.out.println("Not joined.");
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
        logger.info("Created new entry: " + selfEntry);

        // get info of a running process from introducer
        MembershipEntry runningProcess = getRunningProcess();

        // get the membership list from that process.
        // TODO: If cannot, get another process from the introducer.
        if (runningProcess == null) {
            logger.info("First member of the group");
            this.membershipList = new MembershipList(selfEntry);
        } else {
            this.membershipList = requestMembershipList(runningProcess);
            this.membershipList.addEntry(selfEntry); // add self entry to the list
            logger.info("Received membership list. Currently having: " + membershipList.size() + " member(s)");
        }

        // start a TCP listener thread
        this.tcpServer = new ServerSocket(port);
        this.tcpListener = new Thread(this::TCPListener);
        this.tcpListener.start();
        logger.info("TCP listener started");

        // broadcast current join via TCP
        disseminateMessage(new Message(Message.Type.Join, selfEntry));
        logger.info("Broadcasted messages - Joined the group");

        // start gossip protocol thread, communicating via UDP
        this.gossipServer = new DatagramSocket(port);
        this.gossipProtocolThread = new Thread(this::GossipProtocol);
        this.gossipProtocolThread.start();
        logger.info("Gossip protocol started");

        joined = true;
    }

    // Broadcast the message to all members via TCP
    private void disseminateMessage(Message message) {
        for (MembershipEntry member: this.membershipList) {
            if (member.equals(selfEntry)) continue;

            try (Socket introducerConn = new Socket(member.getHost(), member.getPort());
                 ObjectOutputStream oout = new ObjectOutputStream(introducerConn.getOutputStream())) {

                // send self entry to the introducer
                oout.writeObject(message);
                oout.flush();
                logger.info("Disseminated " + message.getMessageType().toString() + " message");
            } catch (IOException ignored) {
            }
        }
    }

    // Fetch membership details from a member already in group
    private MembershipList requestMembershipList(MembershipEntry runningProcess) throws IOException, ClassNotFoundException {
        try (Socket reqConn = new Socket(runningProcess.getHost(), runningProcess.getPort());
             ObjectInputStream oin = (ObjectInputStream) reqConn.getInputStream();
             ObjectOutputStream oout = (ObjectOutputStream) reqConn.getOutputStream()) {
            Message message = new Message(Message.Type.MembershipListRequest, selfEntry);
            oout.writeObject(message);
            oout.flush();
            logger.info("Sent membership list request to: " + runningProcess);

            return (MembershipList) oin.readObject();
        }
    }

    private MembershipEntry getRunningProcess() throws IOException, ClassNotFoundException {
        try (Socket introducerConn = new Socket(introducerHost, introducerPort);
            ObjectOutputStream oout = new ObjectOutputStream(introducerConn.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(introducerConn.getInputStream())) {
            logger.info("Connected to introducer: " + introducerConn);

            // send self entry to the introducer
            oout.writeObject(selfEntry);
            oout.flush();
            logger.info("Sent message to introducer");

            return (MembershipEntry) oin.readObject();
        } finally {
            logger.info("Connection to introducer closed");
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
                logger.info("TCP connection established from: " + reqConn.toString());

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

            // read the message
            Message message = (Message) oin.readObject();
            switch (message.getMessageType()) {
                case Join:
                    logger.info("Received message for process joining group: " + message.getSubject());
                    membershipList.addEntry(message.getSubject());
                    logger.info("Process added to membership list: " + message.getSubject());
                    break;
                // TODO: handle other types of message
                case MembershipListRequest:
                    logger.info("Received request for membership list from: " + message.getSubject());
                    oout.writeObject(this.membershipList);
                    oout.flush();
                    logger.info("Response membership list back");
                    break;
                case IntroducerProbeAlive:
                    logger.info("Received introducer alive probing message from: " + message.getSubject());
                    // do nothing
                default:
                    break;
            }

            reqConn.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error processing TCP request: " + e.getMessage());
        }
    }

    // Sending pings and waiting for acks
    private void GossipProtocol() {
        try {
            // start the ping receiver thread
            new PingReceiver(gossipServer).start();
            logger.info("UDP Socket opened");

            while (true) {
                // get the successor member from membership list
                MembershipEntry successor = membershipList.getSuccessor();

                // send ping messages periodically
                new PingSender(successor, PING_ACK_TIMEOUT_MS, this.membershipList).start();

                Thread.sleep(PING_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            System.out.println("Exit the gossip protocol thread: " + e.getMessage());
        } finally {
            gossipServer.close();
            logger.info("UDP Socket closed");
        }
    }
}
