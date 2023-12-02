package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;

import java.io.*;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread to receive Ping/Ack and send Ack if needed
 */
public class PingReceiver extends Thread {
    private final Member curMember;
    private final AtomicBoolean ackSignal;
    private MembershipEntry acker;; // can be changed over time as the membership list changes

    public PingReceiver(Member curMember, AtomicBoolean ackSignal) {
        this.curMember = curMember;
        this.ackSignal = ackSignal;
    }

    public void updateAcker(MembershipEntry newAcker) {
        synchronized (acker) {
            this.acker = newAcker;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                curMember.getGossipServer().receive(packet);

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
                synchronized (ackSignal) {
                    ackSignal.set(true);
                    ackSignal.notify();
                }
                break;
            default:
                break;
        }
    }

    private void ack(MembershipEntry member) {
        Message message = new Message(Message.Type.Ack, curMember.getSelfEntry());
        message.send(curMember.getGossipServer(), member.getHost(), member.getPort());
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
