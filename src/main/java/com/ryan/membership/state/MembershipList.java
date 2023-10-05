package com.ryan.membership.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Group membership list.
 */
public class MembershipList {

    private List<MembershipEntry> membershipEntries;

    public MembershipList(MembershipEntry firstMember) {
        this.membershipEntries = new ArrayList<>();
        this.membershipEntries.add(firstMember);
    }

    public boolean addEntry(MembershipEntry newEntry) {
        return membershipEntries.add(newEntry);
    }
}
