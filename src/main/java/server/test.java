//package chat;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.*;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//class ClientHandler implements Runnable{
//    private Socket socket;
//    private  PrintWriter pw;
//    ChatServer chatServer;
//    private String userName = "";
//    private int closeStatus = 0;
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public ClientHandler(Socket socket, ChatServer chatServer) {
//        this.socket = socket;
//        this.chatServer = chatServer;
//    }
//
//    public void sendToThisClient(String msg){
//        pw.println(msg);
//    }
//
//    private boolean handleCommand(String msg, PrintWriter pw,Scanner scanner) {
//        //System.out.println("Command: "+msg);
//        String[] parts = msg.split("#");
//        if (parts.length == 1) {
//            if (parts[0].equals("CLOSE")) {
//                return false;
//            }
//            throw new IllegalArgumentException("Sent request does not obey the protocol");
//        } else if (parts.length == 3) {
//            //Only one command to handle here, since Connect is taken care of in it's own method
//            String token = parts[0];
//            String receivers = parts[1];
//            String message = parts[2];
//            switch (token) {
//                case "SEND":
//                    //TODO
//                    break;
//                default:
//                    throw new IllegalArgumentException("Sent request does not obey protocol");
//            }
//        }
//        return true;
//    }
//
//    private boolean handleConnectCommand(String msg, PrintWriter pw,Scanner scanner){
//        String[] parts = msg.split("#");
//        if(parts.length != 2 || !parts[0].equals("CONNECT")){
//            closeStatus = 1;
//            throw new IllegalArgumentException("Sent request does not obey protocol");
//        }
//        userName = parts[1];
//        if(ChatServer.doesUserExist(userName)) {
//            chatServer.addClientToList(userName, this);
//        } else{
//            closeStatus = 2;
//            throw new IllegalArgumentException();
//        }
//        return true;
//    }
//
//    private void handleClient() throws IOException {
//        pw = new PrintWriter(socket.getOutputStream(), true);
//        Scanner scanner = new Scanner(socket.getInputStream());
//        try {
//            String message = "";
//            //This is taken care of here, since it can ONLY HAPPEN HERE and ONLY ONCE
//            //Read the CONNECT#user  message
//            String connectMsg = scanner.nextLine();
//            handleConnectCommand(connectMsg,pw,scanner);
//
//            boolean keepRunning = true;
//            while (keepRunning) {
//                message = scanner.nextLine();  //Blocking call
//                keepRunning = handleCommand(message, pw,scanner);
//            }
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } finally {
//            pw.println("CLOSE#"+closeStatus);
//            socket.close();
//        }
//
//    }
//
//    @Override
//    public void run() {
//        try {
//            handleClient();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//public class ChatServer {
//    public static final int DEFAULT_PORT = 2345;
//    private ServerSocket serverSocket;
//    private ConcurrentHashMap<String, ClientHandler> allClientHandlers = new ConcurrentHashMap<>();
//
//    //Value not used, but this gives experience with Map
//    private static Map<String,String> allChatUsers = new HashMap<>();
//    static {
//        allChatUsers.put("Peter","Peter");
//        allChatUsers.put("Sandra","Sandra");
//        allChatUsers.put("Jan","Jan");
//        allChatUsers.put("Ida","Ida");
//    }
//
//    public static boolean doesUserExist(String user){
//        return allChatUsers.get(user) != null ? true : false;
//    }
//
//
//    public void addClientToList(String user, ClientHandler ch){
//        allClientHandlers.put(user,ch);
//        sendOnlineMessageToAll();
//    }
//
//    public void removeClientFromList(String user, ClientHandler ch){
//        //TODO  Complete this
//    }
//
////    public void sendOnlineMessageToAll(){
////        Set<String> allUserNames = allClientHandlers.keySet();
////        final String usersCommaSeparated = allUserNames.stream().collect(Collectors.joining(","));
////        allClientHandlers.values().forEach((clientHandler -> {
////            clientHandler.sendToThisClient("ONLINE#"+usersCommaSeparated);
////        }));
////    }
//
//    //If you find this CLUMSY replace with the stream-version above
//    public void sendOnlineMessageToAll(){
//        Set<String> allUserNames = allClientHandlers.keySet();
//        String onlineString="ONLINE#";
//        //Streams and join will make the following a lot simpler :-)
//        for(String user: allUserNames){
//            onlineString += user +",";
//        };
//        //Remove the last comma
//        final String onlineStringWithUsers = onlineString.substring(0,onlineString.length()-1);
//        allClientHandlers.values().forEach((clientHandler -> {
//            clientHandler.sendToThisClient(onlineStringWithUsers);
//        }));
//    }
//
//    public void sendToAll(String msg) throws IOException {
//        //You might need this
//    }
//
//    public void startServer(int port) throws IOException {
//        serverSocket = new ServerSocket(port);
//        System.out.println("Server started, listening on : " + port);
//        while (true) {
//            System.out.println("Waiting for a client");
//            Socket socket = serverSocket.accept();
//            System.out.println("New client connected");
//            ClientHandler clientHandler = new ClientHandler(socket,this);
//            Thread t2 = new Thread(clientHandler);
//            t2.start();
//        }
//
//    }
//
//    public static void main(String[] args) throws IOException {
//        int port = DEFAULT_PORT;
//        if (args.length == 1) {
//            try {
//                port = Integer.parseInt(args[0]);
//            } catch (NumberFormatException e) {
//                System.out.println("Invalid port number, using default port :" + DEFAULT_PORT);
//            }
//        }
//        ChatServer server = new ChatServer();
//        server.startServer(port);
//    }
//}