package com.client;

import com.client.data.Person;
import com.client.data.Phone;

import java.io.*;
import java.net.Socket;

/**
 * Created by dp-ptcstd-15 on 12/9/2016.
 */
public class DataBaseConnector {
    public static void main(String[] args) throws ClientException, IOException, InstantiationException, IllegalAccessException {

        Class<Person> tClass = Person.class;
        Class<Phone> zClass = Phone.class;

        Socket socket = new Socket("localhost", 3000);
        SessionFactory sessionFactory = new SessionFactory(socket);

        //sessionFactory.save(new Phone(1, 111, 1111111));
        sessionFactory.getAll(zClass);
        //sessionFactory.getById(zClass, new Phone(7));

        socket.close();

    }
}
