package edu.gvsu.cis.cis656.client;


import edu.gvsu.cis.cis656.peer.PresenceService;
import edu.gvsu.cis.cis656.peer.RegistrationInfo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.rmi.RemoteException;

/**
 * This auxiliary class execute the commands to the peer and calls the Client output method to print
 * resulting messages to the console
 */
public class CommandExecutor {
    RegistrationInfo userInfo;
    PresenceService server;

    public CommandExecutor(PresenceService server, RegistrationInfo userInfo) {
        this.server = server;
        this.userInfo = userInfo;
    }

    /**
     * Executes the 'friends' command on the peer, reads the peer response and prints the friends list to the console.
     *
     * @throws RemoteException
     */
    public void friends() throws Exception {
        RegistrationInfo[] friendsList = server.listRegisteredUsers();
        Client.output("These are the users that are currently on the peer:");
        Client.output("");
        for (RegistrationInfo info : friendsList) {
            Client.output("- " + info.getUserName() + " (" + (info.getStatus() ? "Active" : "Busy") + ")");
        }
        Client.output("");
        Client.output("To message one of them use the 'talk' command");
        Client.output("To message all of them use the 'broadcast' command");
    }

    /**
     * Gets the recipient user information from the peer and sends the message to the recipient.
     * <p>
     * Before sending the message, it checks that the message recipient is not the sender user, that the recipient
     * is registered on the peer and that they are not busy.
     *
     * @param user    UserName of the recipient of the message
     * @param message Message to be sent
     * @throws IOException
     */
    public void talk(String user, String message) throws Exception {
        if (user.equals(userInfo.getUserName())) {
            Client.output("You can't talk to yourself, think instead.");
            return;
        }

        RegistrationInfo receiverInfo = server.lookup(user);

        if (receiverInfo == null) {
            Client.output("The user " + user + " is not currently connected to the network");
            Client.output("You can see a list of the connected users by using the 'friends command");
            return;
        }

        if (!receiverInfo.getStatus()) {
            Client.output("The user " + user + " is busy right now and will not receive your message");
            return;
        }

        Client.output("You: " + message);

        sendMessage(buildMessageToSend(message), receiverInfo);
    }

    /**
     * Gets the users list from the peer and then it sends the message to all of the active users.
     * <p>
     * It skips message sending for the sender and all of the busy users.
     *
     * @param message The message to be sent to all the users.
     * @throws RemoteException
     * @throws IOException
     */
    public void broadcast(String message) throws Exception {
        RegistrationInfo[] friendsList = server.listRegisteredUsers();
        for (RegistrationInfo user : friendsList) {
            if (!user.getStatus() || user.getUserName().equals(userInfo.getUserName())) {
                continue;
            }

            sendMessage(buildMessageToSend(message), user);
        }

        Client.output("You (Broadcast): " + message);
    }

    /**
     * Builds the message by prepending the user name to the message
     *
     * @param message Message that is going to be sent
     * @return
     */
    private String buildMessageToSend(String message) {
        String messageToSend = userInfo.getUserName() + ": " + message;
        return messageToSend;
    }

    /**
     * Opens a socket using the information returned by the peer and sends the message through it.
     *
     * @param message Message that is going to be sent through the socket
     * @param user    Information about hte recipient of the message.
     */
    private void sendMessage(String message, RegistrationInfo user) {
        try {
            Socket clientSocket = new Socket(user.getHost(), user.getPort());
            PrintStream os = new PrintStream(clientSocket.getOutputStream());
            os.println(message);
            clientSocket.close();
        } catch (Exception e) {
            Client.output("There was an error delivering the message to " + user.getUserName());
        }
    }

    /**
     * Changes the status of the user on the peer so that no messages will be sent to them.
     *
     * @throws RemoteException
     */
    public void busy() throws Exception {
        userInfo.setStatus(false);
        server.setStatus(userInfo.getUserName(), false);
        Client.output("Your status is now set to busy. You'll not receive any messages until you set it back to available with the 'available' command");
    }

    /**
     * Changes the status of the user on the peer so that the user will resume receiving messages.
     *
     * @throws RemoteException
     */
    public void available() throws Exception {
        userInfo.setStatus(true);
        server.setStatus(userInfo.getUserName(), true);
        Client.output("Your status is now set to available. You'll receive messages until you set it back to available with the 'busy' command");
    }

    /**
     * Unregisters the user on the peer and exits the program.
     *
     * @throws RemoteException
     */
    public void exit() throws Exception {
        server.unregister(userInfo.getUserName());
        Client.output("You have been logged out from the peer. Bye!");
        System.exit(0);
    }
}
