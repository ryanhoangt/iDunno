package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.message.MembershipMessage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static com.ryan.membership.Member.PING_ACK_TIMEOUT_MS;

public class PingSender extends Thread {
    private final Member curMember;
    private final MembershipEntry toMember;
    private final AtomicBoolean ackSignal;

    private static final Logger logger = Member.logger;

    public PingSender(Member curMember,
                      MembershipEntry toMember,
                      AtomicBoolean ackSignal) throws IOException {
        this.curMember = curMember;
        this.toMember = toMember;
        this.ackSignal = ackSignal;
    }

    @Override
    public void run() {
        try {
            synchronized (ackSignal) {
                // reset the ack signal
                ackSignal.set(false);
                logger.info("Sending ping to: " + toMember);
                ping(toMember, curMember.getSelfEntry());

                // wait for ack
                ackSignal.wait(PING_ACK_TIMEOUT_MS);
            }

            // after waking up, check if ack was received
            if (!ackSignal.get()) {
                // ack not received, remove the member from the membership list
                logger.warning("Ack not received from: " + toMember);
                logger.warning("Process failure detected: " + toMember);
                curMember.disseminateMessage(new MembershipMessage(MembershipMessage.Type.Crash, toMember));

                curMember.getMembershipList().remove(toMember);
                logger.info("Process removed from the membership list: " + toMember);
            } else {
                logger.info("Ack received from: " + toMember);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void ping(MembershipEntry toMember, MembershipEntry sender) {
        MembershipMessage pingMsg = new MembershipMessage(MembershipMessage.Type.Ping, sender);
        pingMsg.udpSend(curMember.getGossipServer(), toMember.getHost(), toMember.getPort());
    }
}
