package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;

public class PingSender extends Thread {
    private MembershipEntry toMember;
    private long timeoutMs;
    private final MembershipList membershipList;

    public PingSender(MembershipEntry toMember, long timeoutMs, MembershipList membershipList) {
        this.toMember = toMember;
        this.timeoutMs = timeoutMs;
        this.membershipList = membershipList;
    }

    @Override
    public void run() {
        // TODO: Send the ping message to the successor & Wait

        // TODO: After timout, disseminate a CRASH message

        synchronized (membershipList) {
            membershipList.remove(toMember);
        }
    }
}
