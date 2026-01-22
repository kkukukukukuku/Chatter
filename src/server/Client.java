package server;

import gui.Gui;
import gui.TextInput;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class Client {

    private static PrintWriter out;

    public static void main(String[] args) {
        Gui.display();
        Thread main = new Thread(() -> {
            try {//connection to server
                System.out.println("connecting...");
                Socket socket = new Socket("your ip..", 1234);//
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                System.out.println("connected");
                TextInput.setOut(out);
                while (!Gui.isNicknameSet()) {
                    Thread.sleep(1000);
                }
                String senderName = Gui.getName();
                out.println(senderName);
                out.flush();
                new Thread(() -> {//message handling
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            final String msg = serverMessage;
                            System.out.println("got: " + msg);

                            SwingUtilities.invokeLater(() -> {

                                String messageText = msg;
                                String sender = "";
                                if (messageText.contains("CMD_CLOSE_WINDOW")) {
                                    SwingUtilities.invokeLater(() -> {
                                        if (Gui.getFrame() != null) {
                                            Gui.getFrame().dispose();
                                        }
                                    });
                                }
                                if (messageText.contains("/setname")){
                                    String username = messageText.substring(senderName.length() + 13);
                                    Gui.setName(username);
                                }
                                if (msg.startsWith("[") && msg.contains("]: ")) {
                                    int firstBracketEnd = msg.indexOf("]: ");
                                    sender = msg.substring(1, firstBracketEnd);
                                    if (firstBracketEnd != -1) {
                                        int secondBracketStart = msg.indexOf("]: ", firstBracketEnd + 3);
                                        if (secondBracketStart != -1) {
                                            messageText = msg.substring(secondBracketStart + 3);
                                        }
                                    }
                                }
                                TextInput.appendResponse(sender, messageText);
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
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        main.start();
    }
}