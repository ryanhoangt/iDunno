package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Message implements Serializable {

    public enum Type {
        Join,
        Leave,
        Ping,
        Ack,
        Crash,
        MembershipListRequest,
        IntroducerProbeAlive
    }

    private Type messageType;
    private MembershipEntry subject;

    public Message(Type messageType, MembershipEntry subject) {
        this.messageType = messageType;
        this.subject = subject;
    }

    public Type getMessageType() {
        return messageType;
    }

    public MembershipEntry getSubject() {
        return subject;
    }

    public void send(DatagramSocket udpServer, String host, int port) {
        // serialize the message
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(this);
            oout.flush();

            // construct the packet
            byte[] data = bout.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            udpServer.send(packet);
        } catch (IOException e) {
            // handle error
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
