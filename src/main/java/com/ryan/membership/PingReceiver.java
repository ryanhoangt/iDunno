package com.ryan.membership;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Thread to receive Ping/Ack and send Ack if needed
 */
public class PingReceiver extends Thread {

    private DatagramSocket udpServer;

    public PingReceiver(DatagramSocket udpServer) {
        this.udpServer = udpServer;
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
                // TODO: Send acks
            case Ack:
                // TODO: Update states to notify the gossip sending thread that the member is still up
            default:
        }
    }

    private Message fromDatagramPacketToMessage(DatagramPacket packet) {
        byte[] data = packet.getData();
        try (ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Message) oin.readObject();
        } catch (IOException | ClassNotFoundException ignored) {
        }
        return null;
    }

}
