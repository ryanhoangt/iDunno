package com.ryan.membership.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Group membership list.
 */
public class MembershipList implements Iterable<MembershipEntry> {

    private List<MembershipEntry> membershipEntries;

    public MembershipList(MembershipEntry firstMember) {
        this.membershipEntries = new ArrayList<>();
        this.membershipEntries.add(firstMember);
    }

    public boolean addEntry(MembershipEntry newEntry) {
        return membershipEntries.add(newEntry);
    }

    @Override
    public Iterator<MembershipEntry> iterator() {
        return membershipEntries.iterator();
    }
}
