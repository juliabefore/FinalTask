package com.server;

import java.io.*;
import java.net.Socket;

/**
 * Created by Юлия on 28.12.2016.
 */
public class ServerSocket implements Runnable{
    Socket socket;
    private final String insert = "insert";
    private final String where = "where";

    public ServerSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        DataBaseService dataBaseService = new DataBaseService(socket);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String query = bufferedReader.readLine();
            System.out.println("Server read a query: " + query);

            if (insert.equalsIgnoreCase(query.substring(0, 6))){
                dataBaseService.save(query);
            }else if(!(query.toLowerCase().contains(where))){
                dataBaseService.getAll(query);
            }else{
                dataBaseService.getById(query);
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bufferedWriter.write("Exception: " + e.getMessage());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
