package com.ryan.filesystem;

public class FileServer {
    private String baseDirectoryStr;

    public FileServer(String dirName) {
        this.baseDirectoryStr = System.getProperty("user.home") + "/Desktop/SDFS/" + dirName + "/";
    }
}
