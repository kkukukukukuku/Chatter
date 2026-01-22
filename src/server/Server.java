    package server;

    import javax.swing.*;
    import java.io.*;
    import java.net.*;

    public class Server {
        public static void main(String[] args) {
                int port = 1234;
                try(ServerSocket serverSocket = new ServerSocket(port)){
                System.out.println("working on port " + port);
                while (true){
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("connected");
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                }

            } catch (IOException e) {
                System.out.println();
            }
        }
    }
