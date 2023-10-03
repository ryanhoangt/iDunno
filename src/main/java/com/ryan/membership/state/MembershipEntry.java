package com.ryan.membership.state;

import org.xml.sax.helpers.AttributesImpl;

import java.util.Date;
import java.util.Objects;

/**
 * Individual member details
 */
public class MembershipEntry {

    private final String host;
    private final int port;
    private final Date timestamp; // for distinguishing successive versions of the same machine

    public MembershipEntry(String host, int port, Date timestamp) {
        this.host = host;
        this.port = port;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembershipEntry that = (MembershipEntry) o;
        return port == that.port && Objects.equals(host, that.host) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, timestamp);
    }

    @Override
    public String toString() {
        return host + '\t' + port + '\t' + timestamp.toString();
    }
}
