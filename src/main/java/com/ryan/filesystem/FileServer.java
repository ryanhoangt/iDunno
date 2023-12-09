package com.ryan.filesystem;

import com.ryan.message.FileMessage;

import java.io.File;

public class FileServer {
    private String baseDirectoryStr;

    public FileServer(String dirName) {
        this.baseDirectoryStr = System.getProperty("user.home") + "/Desktop/SDFS/" + dirName + "/";
    }

    /**
     * Handle command line arguments from main thread
     */
    public void processFileCommand(String command, String localFileName, String sdfsFilename) {
        switch (command) {
            case "put":
                File file = new File(localFileName);
                if (!file.exists()) {
                    System.out.println("Put operation failed: File does not exist");
                    return;
                }

                FileMessage putMessage = new FileMessage(FileMessage.Type.Put, sdfsFilename);
                FileMessage response = sendToCoordinator(putMessage);

                // TODO: replicate files to other servers

                break;

            // TODO: implement other commands
            default:
                break;
        }
    }

    private FileMessage sendToCoordinator(FileMessage message) {
        // TODO:
        return null;
    }
}
