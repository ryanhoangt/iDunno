package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.message.MembershipMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Introducer to facilitate node/machine joins. If the introducer fails,
 * the already running group functions as normal, but without any new joins.
 */
public class Introducer {
    // Logger
    private static final Logger logger = Logger.getLogger("IntroducerLogger");

    private Queue<MembershipEntry> recentJoins;
    private int port;

    public Introducer(int port) throws IOException {
        this.port = port;
        this.recentJoins = new LinkedList<>();

        // setup logger
        Handler fh = new FileHandler("log/dev/membership/introducer.log");
        fh.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
    }

    public void start() {
        logger.info("Introducer started");
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                logger.info("Waiting for request at: " + server);
                Socket reqConn = server.accept();
                logger.info("Connection established on local port: " + reqConn.getLocalPort());

                ObjectOutputStream oout = new ObjectOutputStream(reqConn.getOutputStream());
                ObjectInputStream oin = new ObjectInputStream(reqConn.getInputStream());

                MembershipEntry newEntry;
                try {
                    newEntry = (MembershipEntry) oin.readObject();
                    logger.info("Member joining: " + newEntry);
                } catch (ClassNotFoundException e) {
//                    System.err.println("Error deserializing request: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                // connect to a running process
                MembershipEntry member = recentJoins.peek();
                while (member != null) {
                    try (Socket probConn = new Socket(member.getHost(), member.getPort())) {
                        ObjectOutputStream probOout = new ObjectOutputStream(probConn.getOutputStream());
                        logger.info("Found running process: " + member.getHost() + ":" + member.getPort());
                        MembershipMessage probeAlive = new MembershipMessage(MembershipMessage.Type.IntroducerProbeAlive, null);

                        probOout.writeObject(probeAlive);
                        probOout.flush();

                        probOout.close(); // manually close the connection
                        break;
                    } catch (Exception ex) {
                        logger.info("Process no longer joined: " + member.getHost() + ":" + member.getPort());
                        ex.printStackTrace();
                        recentJoins.poll();
                        member = recentJoins.peek();
                    }
                }
//                assert Objects.nonNull(member);

                // write the process info to the response
                oout.writeObject(member);
                oout.flush();

                if (member != null)
                    logger.info("Running process sent to newly joined process");
                else
                    logger.info("Newly joined process is the first group member");

                // add the new entry to recent join list & close the request
                recentJoins.add(newEntry);
                oout.close();
                oin.close();
                reqConn.close(); // in turn close in/out stream
            }
        } catch (IOException ex) {
            System.err.println("Error when starting server at port " + port + ": " + ex.getMessage());
        }
    }
}
