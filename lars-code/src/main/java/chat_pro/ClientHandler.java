package chat_pro;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class ClientHandler implements Runnable{
    private Socket socket;
    private  PrintWriter pw;
    ChatServer chatServer;
    private String userName = "";
    private int closeStatus = 0;

    public String getUserName() {
        return userName;
    }

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    public void sendToThisClient(String msg){
        pw.println(msg);
    }

    private boolean handleCommand(String msg, PrintWriter pw,Scanner scanner) {
        //System.out.println("Command: "+msg);
        String[] parts = msg.split("#");
        if (parts.length == 1) {
            if (parts[0].equals("CLOSE")) {
                return false;
            }
            throw new IllegalArgumentException("Sent request does not obey the protocol");
        } else if (parts.length == 2) {
            String token = parts[0];
            String argument = parts[1];
            switch (token) {
                case "UPPER":
                    pw.println(argument.toUpperCase());
                    break;
                default:
                    throw new IllegalArgumentException("Sent request does not obey protocol");
            }
        }

        return true;
    }

    private boolean handleConnectCommand(String msg, PrintWriter pw,Scanner scanner){
        String[] parts = msg.split("#");
        if(parts.length != 2 || !parts[0].equals("CONNECT")){
            closeStatus = 1;
            throw new IllegalArgumentException("Sent request does not obey protocol");
        }
        userName = parts[1];
        if(ChatServer.doesUserExist(userName)) {
            chatServer.addClientToList(userName, this);
        } else{
            closeStatus = 2;
            throw new IllegalArgumentException();
        }
        return true;
    }

    private void handleClient() throws IOException {
        pw = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        try {
            String message = "";
            //This is taken care of here, since it can ONLY HAPPEN HERE and ONLY ONCE
            String connectMsg = scanner.nextLine(); //Read the CONNECT#user message
            handleConnectCommand(connectMsg,pw,scanner);

            boolean keepRunning = true;
            while (keepRunning) {
                message = scanner.nextLine();  //Blocking call
                keepRunning = handleCommand(message, pw,scanner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pw.println("CLOSE#"+closeStatus);
            socket.close();
        }

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

/*
Only purpose of this Runnable is to handle all OUTGOING communicaton to all Clients
 */
class ClientCommunicator implements Runnable {
    ChatServer chatServer;
    BlockingQueue<String> sendQueue;

    public ClientCommunicator(ChatServer chatServer, BlockingQueue<String> sendQueue) {
        this.chatServer = chatServer;
        this.sendQueue = sendQueue;
    }

    @Override
    public void run() {
        while(true){
            try {
                String message = sendQueue.take();
                switch(message) {
                    case  "ONLINE" : chatServer.sendOnlineMessageToAll();
                    default:
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

class ChatServer {
    public static final int DEFAULT_PORT = 2345;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> allClientHandlers = new ConcurrentHashMap<>();
    private BlockingQueue<String> sendQueue = new ArrayBlockingQueue<>(8);

    //Value not used, but this gives experience with Map
    private static Map<String,String> allChatUsers = new HashMap<>();
    static {
        allChatUsers.put("Peter","Peter");
        allChatUsers.put("Sandra","Sandra");
        allChatUsers.put("Jan","Jan");
        allChatUsers.put("Ida","Ida");
    }

    public static boolean doesUserExist(String user) {
        return allChatUsers.get(user) != null ? true : false;
    }

    public void dispatchToClientCommunicator(String msg){
        try {
            sendQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addClientToList(String user, ClientHandler ch){
        allClientHandlers.put(user,ch);
        dispatchToClientCommunicator("ONLINE");
    }

    public void removeClientFromList(String user, ClientHandler ch){
        //TODO
    }

    public void sendOnlineMessageToAll(){
        Set<String> allUserNames = allClientHandlers.keySet();
        final String usersCommaSeparated = allUserNames.stream().collect(Collectors.joining(","));
        allClientHandlers.values().forEach((clientHandler -> {
            clientHandler.sendToThisClient("ONLINE#"+usersCommaSeparated);
        }));
    }

    public void sendToAll(String msg) throws IOException {
        //allClientHandlers.values().forEach((handleClient -> handleClient.msgToAll(msg)));
    }

    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started, listening on : " + port);
        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept(); //Blocking call
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(socket,this);

            ClientCommunicator stc = new ClientCommunicator(this,sendQueue);
            Thread t1 = new Thread(stc);
            t1.start();
            Thread t2 = new Thread(clientHandler);
            t2.start();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default port :" + DEFAULT_PORT);
            }
        }
        ChatServer server = new ChatServer();
        server.startServer(port);
    }
}