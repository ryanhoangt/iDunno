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

    public MembershipEntry getSuccessor() {
        // TODO: get the successor member node in the list, ordered by timestamp joined
        throw new UnsupportedOperationException();
    }

    public void remove(MembershipEntry toMember) {
        // TODO: remove the entry from the membership list
        throw new UnsupportedOperationException();
    }

    public int size() {
        return this.membershipEntries.size();
    }
}
