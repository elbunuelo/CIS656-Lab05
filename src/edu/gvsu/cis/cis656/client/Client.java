package edu.gvsu.cis.cis656.client;

import edu.gvsu.cis.cis656.peer.Peer;
import edu.gvsu.cis.cis656.peer.PresenceService;
import edu.gvsu.cis.cis656.peer.RegistrationInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;


public class Client {

    public static final int FRIENDS_LIST = 0;
    public static final int TALK = 1;
    public static final int BROADCAST = 2;
    public static final int BUSY = 3;
    public static final int AVAILABLE = 4;
    public static final int EXIT = 5;
    public static final int HELP = 6;
    private static final String LOCALHOST = "localhost";
    public static int DEFAULT_PORT = 8080;


    private static final String COLOR_PURPLE = "[38;5;165m";
    public static final String COLOR_BLUE = "[38;5;63m";
    public static final String COLOR_YELLOW = "[38;5;226m";


    private static PresenceService peer;
    private static RegistrationInfo userInfo;
    private static ServerSocket serverSocket;
    private static HashMap<String, Integer> commands;
    private static OutputWriter writer;
    private static CommandExecutor commandExecutor;

    /**
     * Prints out the help for executing the program
     */
    public static void printHelp() {
        System.out.println("Invalid parameters specified. The correct invocation is: ");
        System.out.println("user [host [port]]");
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        String username = args[0];

        /*
        String backendHost = LOCALHOST;
        if (args.length > 1) {
            backendHost = args[1];
        }

        int backendPort = DEFAULT_PORT;
        if (args.length > 2) {
            backendPort = Integer.parseInt(args[2]);
        }*/

        try {
            initServer();
            initUser(username);
            startIO();
            welcomeUser();
        } catch (Exception e) {
            System.err.println("Computing exception:");
            e.printStackTrace();
        }
    }


    /**
     * Initializes the peer
     */
    private static void initServer() {
        peer = new Peer();
    }

    /**
     * Initializes the user info and tries to register it with the presence peer.
     * <p>
     * If the username is already registered with the peer, the program prints an error message and exits
     *
     * @param username Chosen nickname for the user
     * @throws IOException
     */
    private static void initUser(String username) throws Exception {
        initSockets();

        int port = serverSocket.getLocalPort();
        String host = InetAddress.getLocalHost().getHostAddress();

        userInfo = new RegistrationInfo(username, host, port, true);
        peer.register(userInfo);
    }

    /**
     * Initializes the peer socket for receiving messages and starts the message listener thread.
     *
     * @throws IOException
     */
    private static void initSockets() throws IOException {
        serverSocket = new ServerSocket(0);
        Thread messageListener = new Thread(new MessageListener(serverSocket));
        messageListener.start();
    }

    /**
     * Prints a welcome message for the user and the commands help.
     */
    private static void welcomeUser() {
        output("Welcome to the chat. you may find these commands useful:");
        commandsHelp();
    }

    /**
     * Starts both the input and the output threads for receiving commands and displaying their results to the console.
     */
    private static void startIO() {
        writer = new OutputWriter(userInfo);

        Thread inputThread = new Thread(new InputReader());
        inputThread.start();

        initCommands();
        commandExecutor = new CommandExecutor(peer, userInfo);
    }

    /**
     * Takes the input received though the input reader and tries to execute it as a command.
     * <p>
     * This method just checks that the first word of the input is a valid command. If it is valid, it sends it to
     * the CommandExecutor class which can make further validations on the command parameters and executes them on
     * the presence service.
     *
     * @param input The full input entered by the user.
     * @throws IOException
     */
    public static void executeInput(String input) throws Exception {
        String[] inputParts = input.split("\\s");
        String command = inputParts[0];
        int commandCode = getCommandCode(command);

        String message;
        switch (commandCode) {
            case TALK:
                String user = inputParts[1];
                message = joinInputParts(inputParts, 2);
                commandExecutor.talk(user, message);
                break;
            case FRIENDS_LIST:
                commandExecutor.friends();
                break;
            case BROADCAST:
                message = joinInputParts(inputParts, 1);
                commandExecutor.broadcast(message);
                break;
            case BUSY:
                commandExecutor.busy();
                break;
            case AVAILABLE:
                commandExecutor.available();
                break;
            case EXIT:
                commandExecutor.exit();
                break;
            case HELP:
                commandsHelp();
                break;
            default:
                output("The command you entered is not valid");
                break;
        }
    }

    /**
     * Prints the list of commands with their parameters and an explanation of what they do
     */
    private static void commandsHelp() {
        //  output("- friends: Prints the friends list", COLOR_YELLOW);
        output("- talk <user> <message>: Sends a message to the specified user", COLOR_YELLOW);
        //  output("- broadcast <message>: Sends a message to all available users", COLOR_YELLOW);
        output("- busy: Sets your status as 'busy'", COLOR_YELLOW);
        output("- available: Sets your status as available", COLOR_YELLOW);
        output("- exit: terminates the program", COLOR_YELLOW);
        output("- ?: prints this help message", COLOR_YELLOW);
    }

    /**
     * This utility method joins the message parts of a command into a single string.
     *
     * @param inputParts Array of strings that contains all the words input by the user.
     * @param start      Index of the array from which the string should be constructed.
     * @return
     */
    private static String joinInputParts(String[] inputParts, int start) {
        String result = "";
        for (int i = start; i < inputParts.length; i++) {
            result += " " + inputParts[i];
        }
        return result;
    }

    /**
     * Default output method, prints the content of the message passed in purple.
     *
     * @param message Message to be printed to the console.
     */
    public static void output(String message) {
        output(message, COLOR_PURPLE);
    }

    /**
     * Specific output method, prints the content of the message in the specified color.
     *
     * @param message Message to be printed to the console.
     * @param color   String that determines the color of the output.
     */
    public static void output(String message, String color) {
        writer.addEvent((char) 27 + color + message + (char) 27 + "[0m");
    }

    /**
     * Initializes the commands hash that maps strings to the integer constants for the commands.
     * <p>
     * Using a hash for the commands allows us to simplify the command lookup and easy mapping of multiple string commands
     * to the same integer constant.
     */
    public static void initCommands() {
        commands = new HashMap<>();
        commands.put("talk", TALK);
        commands.put("busy", BUSY);
        commands.put("available", AVAILABLE);
        commands.put("exit", EXIT);
        commands.put("?", HELP);

    }

    /**
     * Looks up the command string in the commands hash and returns the integer constant for the command.
     *
     * @param command String command input by the user.
     * @return
     */
    public static int getCommandCode(String command) {
        int commandCode = -1;
        if (commands.containsKey(command)) {
            commandCode = commands.get(command);
        }

        return commandCode;
    }

    /**
     * Receives a message from the messageListener and outputs it to the terminal in blue if the user is available.
     *
     * @param message Message received through the socket
     */
    public static void receiveMessage(String message) {
        if (userInfo.getStatus()) {
            output(message, COLOR_BLUE);
        }
    }
}