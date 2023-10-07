package com.ryan.membership;

import com.ryan.membership.state.MembershipEntry;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

public class UDPObjectSerDesTest {

    @Test
    public void testSerializeMessageObject_UsingBuiltInMethod() throws IOException, ClassNotFoundException {
        Message message = new Message(Message.Type.Join,
                new MembershipEntry("localhost", 2999, new Date()));

        // serialize
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);
        oOut.writeObject(message);
        oOut.close();

        byte[] serializedMsg = bOut.toByteArray();

        // deserialize
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(serializedMsg));
        Message deserializedMsg = (Message) oin.readObject();
        oin.close();

        assert message.getMessageType().equals(deserializedMsg.getMessageType());
        assert message.getSubject().equals(deserializedMsg.getSubject());
    }

}
