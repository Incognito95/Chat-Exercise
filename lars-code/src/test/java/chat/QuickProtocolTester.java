package chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class QuickProtocolTester {

    Socket socket ;
    Scanner scanner;
    PrintWriter pw;

    private void initializeAll() throws IOException {
        socket = new Socket("localhost",2345);
        scanner = new Scanner(socket.getInputStream());
        pw = new PrintWriter(socket.getOutputStream(),true);
    }

    private void testConnectOK() throws IOException {
        initializeAll();
        System.out.println("TEST1 (Connecting with an existing user)");
        pw.println("CONNECT#Peter");
        String response = scanner.nextLine();
        System.out.println(response);
        pw.println("CLOSE#");
        socket.close();
    }

    private void testConnectWrongUser() throws IOException {
        initializeAll();
        System.out.println("TEST2 (Connecting with a NON-existing user)");
        pw.println("CONNECT#xxxxxx");
        String response = scanner.nextLine();
        System.out.println(response);
        socket.close();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new QuickProtocolTester().testConnectOK();
        new QuickProtocolTester().testConnectWrongUser();
    }
}