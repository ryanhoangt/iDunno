package com.ryan.filesystem;

import com.ryan.membership.state.MembershipEntry;
import com.ryan.membership.state.MembershipList;
import com.ryan.message.FileMessage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileServer {
    private String baseDirectoryStr;
    private final MembershipList membershipList;

    public FileServer(String dirName, MembershipList membershipList) {
        this.baseDirectoryStr = System.getProperty("user.home") + "/Desktop/SDFS/" + dirName + "/";
        this.membershipList = membershipList;
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
        // get the coordinator from the membership list
        MembershipEntry coordinator = membershipList.getCoordinator();

        try (Socket coordinatorSocket = new Socket(coordinator.getHost(), coordinator.getPort())) {
            ObjectOutputStream oout = new ObjectOutputStream(coordinatorSocket.getOutputStream());
            ObjectInputStream oin = new ObjectInputStream(coordinatorSocket.getInputStream());

            oout.writeObject(message);
            oout.flush();

            FileMessage response = (FileMessage) oin.readObject();

            oout.close();
            oin.close();

            return response;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error sending message to coordinator:");
            e.printStackTrace();
            return null;
        }
    }
}
