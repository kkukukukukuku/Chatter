package server;

import gui.Gui;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            this.username = reader.readLine();
            clientHandlers.add(this);
            broadcastToAll("[SERVER] " + username + " connected!");

            System.out.println(username + " connected");
        } catch (IOException e) {
            closeAll();
            System.out.println("connection error");
        }
    }

    public void broadcastToAll(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (!ch.username.equals(this.username)) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    public void sendMessageToClient(String targetUsername, String message) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.username.equals(targetUsername)) {
                    ch.sendMessage("private from " + username + message.substring(targetUsername.length()));
                    break;
                }
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void removeClientHandler() {
        synchronized (clientHandlers) {
            clientHandlers.remove(this);
            broadcastToAll("[SERVER] " + username + " left");
        }
        System.out.println(username + " disconnected");
    }

    private void closeAll() {
        try {
            removeClientHandler();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                message = reader.readLine();
                if (message == null) {
                    break;
                }

                System.out.println("SERVER from " + username + ": '" + message + "'");

                if (message.contains("/exit")) {
                    writer.println("CMD_CLOSE_WINDOW");
                    writer.flush();

                    System.out.println(username + " is exiting");
                    break;
                }else if (message.contains("/setname")){
                    writer.println(message);
                    writer.flush();
                    Thread.sleep(400);
                    username = message.substring(username.length() + 13);
                }else if (message.contains("/private")){
                    String[] arr = message.split(" ");
                    if (arr.length >= 4) {
                        String name = arr[2];
                        StringBuilder msg = new StringBuilder();
                        for (int i = 2; i < arr.length; i++){
                            System.out.println(arr[i]);
                            msg.append(arr[i]).append(" ");
                        }
                        sendMessageToClient(name, msg.toString().trim());
                    }else{

                    }
                }else {
                    String formattedMessage = "[" + username + "]: " + message;
                    System.out.println("Broadcasting: " + formattedMessage);
                    broadcastToAll(formattedMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            closeAll();
        }
    }
}