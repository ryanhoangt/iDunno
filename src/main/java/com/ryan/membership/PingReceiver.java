package com.ryan.membership;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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

                // TODO: Deserialize and process the packet
            } catch (IOException e) {
                // TODO: Handle error
            }
        }
    }
}
