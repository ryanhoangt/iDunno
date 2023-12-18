package com.ryan.membership.state;

import java.io.Serializable;
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
    private MembershipEntry coordinator; // for passing to other nodes

    public MembershipList(MembershipEntry owner) {
        this.owner = owner;
        this.membershipEntries = new TreeSet<>();
        this.membershipEntries.add(owner);
    }

    public synchronized boolean addEntry(MembershipEntry newEntry) {
        return membershipEntries.add(newEntry);
    }

    public synchronized boolean addEntryAsOwner(MembershipEntry newEntry) {
        this.owner = newEntry;
        return membershipEntries.add(newEntry);
    }

    public MembershipEntry getCoordinator() {
        return coordinator;
    }

    @Override
    public Iterator<MembershipEntry> iterator() {
        return membershipEntries.iterator();
    }

    public MembershipEntry getSuccessor() {
        // get the successor member node in the list, ordered by timestamp joined
        MembershipEntry successor = membershipEntries.higher(owner);
        if (successor == null)
            successor = membershipEntries.first(); // maintain a ring topology
        return successor == owner ? null : successor;
    }

    public synchronized boolean remove(MembershipEntry entry) {
        return membershipEntries.remove(entry);
    }

    public int size() {
        return this.membershipEntries.size();
    }

    @Override
    public String toString() {
        String stringMemberList = "Hostname\tPort\tTimestamp (Join)\n";
        stringMemberList += "----------------------------------------";

        for (MembershipEntry entry: membershipEntries) {
            stringMemberList += "\n";
            if (entry.equals(owner)) {
                stringMemberList += "Self: ";
            }
            stringMemberList += entry.toString();
        }

        return stringMemberList;
    }
}
