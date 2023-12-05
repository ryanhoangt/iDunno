package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Member {
    // Protocol config
    private static final long PING_INTERVAL_MS = 1500;
    public static final long PING_ACK_TIMEOUT_MS = 1000;

    // Logger
    public static final Logger logger = Logger.getLogger("MemberLogger");

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
        Handler fh = new FileHandler("log/dev/membership/member.log");
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
                System.out.print("MemberProcess$ ");
                String command = stdin.readLine();

                switch (command) {
                    case "join":
                        logger.info("Member process received 'join' command");
                        joinGroup();
                        break;
                    case "leave":
                        leaveGroup(true);
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

    public MembershipEntry getSelfEntry() {
        return selfEntry;
    }

    public MembershipList getMembershipList() {
        return membershipList;
    }

    public DatagramSocket getGossipServer() {
        return gossipServer;
    }

    // Broadcast the message to all members via TCP
    public void disseminateMessage(Message message) {
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
            logger.info("Requesting membership list from: " + runningProcess);
            this.membershipList = requestMembershipList(runningProcess);
            this.membershipList.addEntryAsOwner(selfEntry); // add self entry to the list
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

    // Fetch membership details from a member already in group
    private MembershipList requestMembershipList(MembershipEntry runningProcess) throws IOException, ClassNotFoundException {
        try (Socket reqConn = new Socket(runningProcess.getHost(), runningProcess.getPort())) {
            ObjectOutputStream oout = new ObjectOutputStream(reqConn.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(reqConn.getInputStream());

            Message message = new Message(Message.Type.MembershipListRequest, selfEntry);
            oout.writeObject(message);
            oout.flush();
            logger.info("Sent membership list request to: " + runningProcess);

            MembershipList list = (MembershipList) oin.readObject();
            oout.close();
            oin.close();
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private MembershipEntry getRunningProcess() throws IOException, ClassNotFoundException {
        try (Socket introducerConn = new Socket(introducerHost, introducerPort)) {
            ObjectOutputStream oout = new ObjectOutputStream(introducerConn.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(introducerConn.getInputStream());
            logger.info("Connected to introducer: " + introducerConn);

            // send self entry to the introducer
            oout.writeObject(selfEntry);
            oout.flush();
            logger.info("Sent message to introducer");

            MembershipEntry runningProcess = (MembershipEntry) oin.readObject();
            oout.close();
            oin.close();

            return runningProcess;
        } finally {
            logger.info("Connection to introducer closed");
        }
    }

    private void leaveGroup(boolean notifyOthers) {
        // do nothing if not joined
        if (!joined) return;

        logger.info("Leave command received");

        if (notifyOthers) {
            disseminateMessage(new Message(Message.Type.Leave, selfEntry));
            logger.info("Leave message disseminated");
        }

        // TODO: close resources
    }

    private void TCPListener() {
        while (true) {
            try {
                Socket reqConn = tcpServer.accept();
                logger.info("TCP connection established from: " + reqConn.toString());

                // spawn a new thread to process the request
                new Thread(() -> this.processTCPMessage(reqConn)).start();
            } catch (Exception e) {
                // do nothing
                e.printStackTrace();
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
                case Leave:
                    logger.info("Received message for process leaving group: " + message.getSubject());
                    membershipList.remove(message.getSubject());
                    logger.info("Process removed from membership list: " + message.getSubject());
                    break;
                case Crash:
                    logger.info("Received message for process failure: " + message.getSubject());
                    if (selfEntry.equals(message.getSubject())) {
                        // false crash of this node detected
                        System.out.println("False positive crash of this node detected. Stopping execution");
                        logger.warning("False positive crash of this node detected. Stopping execution");

                        // leave group silently without notifying others
                        leaveGroup(false);
                        break;
                    }
                    membershipList.remove(message.getSubject());
                    logger.info("Process removed from membership list: " + message.getSubject());
                    break;
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
            AtomicBoolean ackSignal = new AtomicBoolean(); // monitoring 1 member
            // start the ping receiver thread
            PingReceiver receiver = new PingReceiver(this, ackSignal);
            receiver.start();
            logger.info("UDP Socket opened");

            while (true) {
                // get the successor member from membership list
                MembershipEntry successor;
                synchronized (membershipList) {
                    successor = membershipList.getSuccessor();
                }
                receiver.updateAcker(successor);

                if (successor != null) {
                    logger.info("Successor: " + successor);
                    new PingSender(this, successor, ackSignal).start();
                } else
                    logger.info("No successor found");

                Thread.sleep(PING_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            System.out.println("Exit the gossip protocol thread: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            gossipServer.close();
            logger.info("UDP Socket closed");
        }
    }
}
