package server;

import gui.Gui;
import gui.TextInput;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class Server {
    private static final Object lock = new Object();
    static boolean isDone = false;
    private static PrintWriter out;
    public static void main(String[] args) {
        Thread gui = new Thread(() -> {
            Gui.display();
            synchronized(lock) {
                isDone = true;
                lock.notifyAll();
            }
        });

        Thread main = new Thread(() -> {
            synchronized(lock) {
                while (!isDone) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                int port = 1234;
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("working on port " + port);

                Socket clientSocket = serverSocket.accept();
                System.out.println("connected");
                OutputStream outputStream = clientSocket.getOutputStream();
                out = new PrintWriter(outputStream, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                TextInput.setOut(out);
                out.println("[System]: Connected successfully");
                out.flush();
                String username = TextInput.getUserName();
                new Thread(() -> {
                    try {
                        String clientMessage;
                        while ((clientMessage = in.readLine()) != null) {
                            final String msg = clientMessage;
                            System.out.println("got: " + msg);
                            SwingUtilities.invokeLater(() -> {
                                String senderName = "Client";
                                String messageText = msg;

                                if (msg.startsWith("[") && msg.contains("]: ")) {
                                    int endIndex = msg.indexOf("]: ");
                                    senderName = msg.substring(1, endIndex);
                                    messageText = msg.substring(endIndex + 3);
                                }
                                if (!messageText.equals(senderName)){
                                    TextInput.appendResponse(senderName, messageText);
                                }
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("disconnected");
                        SwingUtilities.invokeLater(() -> {
                            TextInput.appendResponse("System", "client disconnected");
                        });
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        gui.start();
        main.start();
    }
}
