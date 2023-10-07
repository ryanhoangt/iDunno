package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;

import java.io.Serializable;

public class Message implements Serializable {

    public enum Type {
        Join,
        Ping,
        Ack
    }

    private Type messageType;
    private MembershipEntry subject;

    public Message(Type messageType, MembershipEntry subject) {
        this.messageType = messageType;
        this.subject = subject;
    }

    public Type getMessageType() {
        return messageType;
    }

    public MembershipEntry getSubject() {
        return subject;
    }
}
