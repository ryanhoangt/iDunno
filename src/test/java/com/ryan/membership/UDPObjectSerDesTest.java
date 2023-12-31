package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.message.MembershipMessage;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Date;

public class UDPObjectSerDesTest {

    @Test
    public void testSerializeMessageObject_UsingBuiltInMethod() throws IOException, ClassNotFoundException {
        MembershipMessage message = new MembershipMessage(MembershipMessage.Type.Join,
                new MembershipEntry("localhost", 2999, new Date()));

        // serialize
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(message);
        oout.close();

        byte[] serializedMsg = bout.toByteArray();

        // deserialize
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(serializedMsg));
        MembershipMessage deserializedMsg = (MembershipMessage) oin.readObject();
        oin.close();

        assert message.getMessageType().equals(deserializedMsg.getMessageType());
        assert message.getSubject().equals(deserializedMsg.getSubject());
    }

}
