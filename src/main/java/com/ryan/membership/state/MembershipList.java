package com.ryan.membership.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Group membership list.
 */
public class MembershipList implements Iterable<MembershipEntry>,
                                        Serializable {

    /**
     * Use {@link java.util.TreeSet} for quicker lookup, insertion, and removal.
     */
    private final TreeSet<MembershipEntry> membershipEntries;
    private MembershipEntry owner;

    public MembershipList(MembershipEntry owner) {
        this.owner = owner;
        this.membershipEntries = new TreeSet<>();
        this.membershipEntries.add(owner);
    }

    public synchronized boolean addEntry(MembershipEntry newEntry) {
        return membershipEntries.add(newEntry);
    }

    @Override
    public Iterator<MembershipEntry> iterator() {
        return membershipEntries.iterator();
    }

    public MembershipEntry getSuccessor() {
        // get the successor member node in the list, ordered by timestamp joined
        MembershipEntry successor = membershipEntries.higher(owner);
        if (successor == null)
            successor = membershipEntries.first();
        return successor == owner ? null : successor;
    }

    public synchronized boolean remove(MembershipEntry entry) {
        return membershipEntries.remove(entry);
    }

    public int size() {
        return this.membershipEntries.size();
    }
}
