package server;

import gui.Gui;
import gui.TextInput;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final Object lock = new Object();
    static boolean isDone = false;
    private static PrintWriter out;

    public static void main(String[] args) {
        Thread gui = new Thread(() -> {//popjsodivjlskvj
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
                System.out.println("connecting...");
                Socket socket = new Socket("your ip...", 1234);//
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                System.out.println("connected");
                TextInput.setOut(out);
                out.println("New client connected!");
                out.flush();

                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            final String msg = serverMessage;
                            System.out.println("got from server: " + msg);

                            SwingUtilities.invokeLater(() -> {
                                String senderName = "System";
                                String messageText = msg;

                                if (msg.startsWith("[") && msg.contains("]: ")) {
                                    int endIndex = msg.indexOf("]: ");
                                    senderName = msg.substring(1, endIndex);
                                    messageText = msg.substring(endIndex + 3);
                                }

                                TextInput.appendResponse(senderName, messageText);
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("server disconnected");
                        SwingUtilities.invokeLater(() -> {
                            TextInput.appendResponse("System", "server disconnected");
                        });
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "failed to connect: " + e.getMessage(),
                            "error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });

        gui.start();
        main.start();
    }
}