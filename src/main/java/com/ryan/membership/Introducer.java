package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Introducer to facilitate node/machine joins. If the introducer fails,
 * the already running group functions as normal, but without any new joins.
 */
public class Introducer {

    private Queue<MembershipEntry> recentJoins;
    private int port;

    public Introducer(int port) {
        this.port = port;
        this.recentJoins = new LinkedList<>();
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket reqConn = server.accept();

                ObjectOutputStream oout = new ObjectOutputStream(reqConn.getOutputStream());
                ObjectInputStream oin = new ObjectInputStream(reqConn.getInputStream());

                MembershipEntry newEntry;
                try {
                    newEntry = (MembershipEntry) oin.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println("Error deserializing request: " + e.getMessage());
                    continue;
                }

                // connect to a running process
                MembershipEntry member = recentJoins.peek();
                while (member != null) {
                    try (Socket probConn = new Socket(member.getHost(), member.getPort());
                         ObjectInputStream probOin = (ObjectInputStream) probConn.getInputStream();
                         ObjectOutputStream probOout = (ObjectOutputStream) probConn.getOutputStream()) {
                        Message probeAlive = new Message(Message.Type.IntroducerProbeAlive, null);

                        probOout.writeObject(probeAlive);
                        probOout.flush();

                        break;
                    } catch (Exception ex) {
                        recentJoins.poll();
                        member = recentJoins.peek();
                    }
                }
//                assert Objects.nonNull(member);

                // write the process info to the response
                oout.writeObject(member);
                oout.flush();

                // add the new entry to recent join list & close the request
                recentJoins.add(newEntry);
                reqConn.close(); // in turn close in/out stream
            }
        } catch (IOException ex) {
            System.err.println("Error when starting server at port " + port + ": " + ex.getMessage());
        }
    }
}
