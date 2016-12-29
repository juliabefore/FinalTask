package com.server;

import java.io.*;
import java.net.Socket;

/**
 * Created by Юлия on 11.12.2016.
 */
public class Session {

    public static void main(String[] args) throws IOException {
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3000);

        while (true){
            Socket socket = serverSocket.accept();
            Thread thread = new Thread(new ServerSocket(socket));
            thread.start();
        }
    }
}

