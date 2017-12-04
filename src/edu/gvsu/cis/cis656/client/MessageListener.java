package edu.gvsu.cis.cis656.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Accepts messages through the socket and relays them to the edu.gvsu.cis.cis656.client for output.
 */
public class MessageListener implements Runnable {
    private ServerSocket socket;

    public MessageListener(ServerSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        while (true) {
            String line;
            BufferedReader is;
            Socket clientSocket = null;
            try {
                clientSocket = this.socket.accept();
                is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (true) {
                    line = is.readLine();
                    if (line == null) {
                        break;
                    }
                    Client.receiveMessage(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
