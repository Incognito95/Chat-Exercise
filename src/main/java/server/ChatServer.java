package server;


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

public class ChatServer {
    private ServerSocket serverSocket;
    public static Map<String, String> users;
    static {
        users = new HashMap<>();
        users.put("Ermin", "Ermin");
        users.put("HH", "HH");
        users.put("Daniel", "Daniel");

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

            switch(token) {
                case "CONNECT":
                    String username = users.get(param);
                    if (username == null) { // if user not found send CLOSE#2 and close connection
                        username = "Der findes ikke en bruger ved det navn";
                    }
                    pw.println(username);
                    send(message, pw);
                    System.out.println("hellooooo");
                    break;
                case "UPPER":
                    pw.println(param.toUpperCase());
                    break;

                default:
                    throw new IllegalArgumentException("Sent request does not obey the protocal");
            }
        }

        return true;

    }


public void send(String message, PrintWriter pw) {
    String[] parts = message.split("#");
    System.out.println("Size: " + parts.length);
    String send = parts[0];
    String param = parts[1];//Værdi efter #-tegne

    switch(send) {
        case "SEND":
            System.out.println("hej");
            pw.println(param.toUpperCase());
            break;

        default:
            throw new IllegalArgumentException("Sent request does not obey the protocal");
    }
}







    private void handleClient(Socket socket) throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); //
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

private void startServer (int port) throws IOException {
    try {
        serverSocket = new ServerSocket(port);
    } catch (IOException e) {
        e.printStackTrace();
    }
    System.out.println("Server started, listening in : "+ port);

    while (true){
        System.out.println("Waiting for a client");
        Socket socket = serverSocket.accept();//Blocking call
        System.out.println("New client connected");
        handleClient(socket);
    }

}


    //Call server with arguments like this: 0.0.0.0 8088 logfile.log
    public static void main(String[] args) throws IOException {
    String ip ="localhost";
        int port = 8088;
        String logFile = "log.txt";  //Do we need this

        try {
            if (args.length == 3) {
                ip = args[0];
                port = Integer.parseInt(args[1]);
                logFile = args[2];
            }
        } catch (NumberFormatException ne) {
            System.out.println("Illegal inputs provided when starting the server!");
            return;
        }

        new ChatServer().startServer(port);
    }


}

/*   else {
    throw new IllegalArgumentException("Server not provided with the right arguments");
} */