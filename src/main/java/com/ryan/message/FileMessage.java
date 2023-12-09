package com.ryan.message;

import java.io.Serializable;

public class FileMessage implements Serializable {

    public enum Type {
        Put,
    }

    private Type messageType;

    private String fileName;

    public FileMessage(Type messageType, String fileName) {
        this.messageType = messageType;
        this.fileName = fileName;
    }
}
