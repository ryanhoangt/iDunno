package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Thread to receive Ping/Ack and send Ack if needed
 */
public class PingReceiver extends Thread {

    private DatagramSocket udpServer;
    private MembershipEntry selfEntry;

    public PingReceiver(DatagramSocket udpServer, MembershipEntry selfEntry) {
        this.udpServer = udpServer;
        this.selfEntry = selfEntry;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpServer.receive(packet);

                // deserialize and process the packet
                Message message = fromDatagramPacketToMessage(packet);
                if (message == null) continue;

                processMessage(message);
            } catch (IOException e) {
                // handle error
            }
        }
    }

    private void processMessage(Message message) {
        switch (message.getMessageType()) {
            case Ping:
                ack(message.getSubject());
                break;
            case Ack:
                // TODO: Update states to notify the gossip sending thread that the member is still up
            default:
                break;
        }
    }

    private void ack(MembershipEntry member) {
        Message message = new Message(Message.Type.Ack, selfEntry);
        sendMessage(message, member.getHost(), member.getPort());
    }

    private Message fromDatagramPacketToMessage(DatagramPacket packet) {
        byte[] data = packet.getData();
        try (ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Message) oin.readObject();
        } catch (IOException | ClassNotFoundException ignored) {
        }
        return null;
    }

    private void sendMessage(Message message, String host, int port) {
        // serialize the message
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(message);
            oout.flush();

            // construct the packet
            byte[] data = bout.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            udpServer.send(packet);
        } catch (IOException e) {
            // handle error
        }
    }
}
