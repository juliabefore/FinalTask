package com.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

/**
 * Created by dp-ptcstd-15 on 12/9/2016.
 */
public class SessionFactory {

    String query;
    Socket socket;

    public SessionFactory(Socket socket){
        this.socket = socket;
    }

    public void save(Object object) throws ClientException, IllegalAccessException, IOException {
        QueryGenerator queryGenerator = new QueryGenerator();
        this.query = queryGenerator.generateSave(object);
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public <T> List<T> getAll(Class<T> tClass) throws ClientException, IOException, InstantiationException, IllegalAccessException {
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        QueryGenerator queryGenerator = new QueryGenerator();
        this.query = queryGenerator.generateGetAll(tClass);
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        ResultSetParser resultSetParser = new ResultSetParser();
        List<T> objList = resultSetParser.parseValues2Object(socket, tClass);
        System.out.println(objList);

        return objList;
    }

    public <T> T getById(Class<T> tClass, T object) throws IOException, IllegalAccessException, InstantiationException {


        T value = null;
        try {

            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            QueryGenerator queryGenerator = new QueryGenerator();
            this.query = queryGenerator.generateGetById(tClass, object);
            bufferedWriter.write(query);
            System.out.println("Query was sent by client to server: " + query);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            ResultSetParser resultSetParser = new ResultSetParser();
            value = resultSetParser.parseValues2ObjectById(socket, tClass);

        } catch (ClientException e) {
            e.printStackTrace();
        }
        return value;
    }
}