package server;


import com.google.gson.internal.bind.util.ISO8601Utils;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer implements Runnable {

    public static Map<String, String> users;
    private EchoServerMultithreaded echoServerMain;
    private PrintWriter pw;
    Socket socket;
    //Provides each instance with a unique id. Simulates the unique userid we will need for the chat-server
    private static int id = 0;

    static {
        users = new HashMap<>();
        users.put("Ermin", "Ermin");
        users.put("HH", "HH");
        users.put("Daniel", "Daniel");

    }

    public ChatServer(Socket socket, EchoServerMultithreaded echoServerMain) {
        this.socket = socket;
        this.id++;
        this.echoServerMain = echoServerMain;
    }


    void sendMessage(String msg)
    {
        pw.println("MSG_ALL#" + msg);
    }

        private boolean handleCommand(String message, PrintWriter pw) {
            String[] parts = message.split("#");
            System.out.println("Size: " + parts.length);
            if (parts.length == 1) {
                if (parts[0].equals("CLOSE")) {
                    pw.println("CLOSE#");
                    return false;
                }
                throw new IllegalArgumentException("Sent request does not obey the protocol");
            } else if (parts.length == 2) {
                String token = parts[0];//Indeholder eksempelvis CONECT
                String param = parts[1];//Værdi efter #-tegne

                switch (token) {
                    case "CONNECT":
                        String username = users.get(param);
                        if (username == null) { // if user not found send CLOSE#2 and close connection
                            username = "Der findes ikke en bruger ved det navn";
                        }
                        pw.println(username);
                       // send(message, pw);
                        break;
                    case "UPPER":
                        pw.println(param.toUpperCase());
                        break;
                    case "ALL":
                        echoServerMain.sendToAll(param);
                        break;

                    default:
                        throw new IllegalArgumentException("Sent request does not obey the protocal");
                }
            }else if (parts.length==3) {
                String token1 = parts[0];//Indeholder eksempelvis CONECT
                String param = parts[1];//Værdi efter #-tegne
                String param1 = parts[2];//Værdi efter 2. #-tegne
                switch (token1) {
                    case "SEND":
                        String username = users.get(param);
                        if (username == null) { // if user not found send CLOSE#2 and close connection
                            username = "Der findes ikke en bruger ved det navn";
                        }
                        pw.println(param1);
                        break;
                }
            }

            return true;

        }


        private void handleClient() throws IOException {
            pw = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(socket.getInputStream());
            pw.println("You are connected, send a string for get it upper cased, send 'stop' to stop the server");
            try {
                String message = ""; // scanner.nextLine(); blocking call
                boolean keepRunning = true;
                while (keepRunning) {
                    message = scanner.nextLine();
                    keepRunning = handleCommand(message, pw);
                }
            } catch (Exception e) {
                System.out.println("UPPS" + e.getMessage());
            }
            pw.println("Connection is closing...");
            socket.close(); // close connection

        }
    public int getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class EchoServerMultithreaded {
    public static final int DEFAULT_PORT = 8088;
    ConcurrentHashMap<Integer, ChatServer> allClientHandlers;


    void sendToAll(String msg) {
        allClientHandlers.values().forEach(clientHandler -> {
            clientHandler.sendMessage(msg);
        });
    }


    private void startServer(int port) throws IOException {
        ServerSocket serverSocket;
        allClientHandlers = new ConcurrentHashMap<>();
        serverSocket = new ServerSocket(port);
        System.out.println("Server started, listening on : " + port);

//            try {
//                serverSocket = new ServerSocket(port);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Server started, listening in : " + port);

        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept();//Blocking call
            System.out.println("New client connected");
            ChatServer chatServer = new ChatServer(socket, this);
            allClientHandlers.put(chatServer.getId(),chatServer);
            new Thread(chatServer).start();
        }

    }


    //Call server with arguments like this: 0.0.0.0 8088 logfile.log
    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default port :" + DEFAULT_PORT);
            }
        }
        new EchoServerMultithreaded().startServer(port);

    }

}



/*   else {
    throw new IllegalArgumentException("Server not provided with the right arguments");
} */