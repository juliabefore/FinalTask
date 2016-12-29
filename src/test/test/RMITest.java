import com.client.ClientException;
import com.client.QueryGenerator;
import com.client.ResultSetParser;
import com.client.SessionFactory;
import com.client.data.Phone;
import com.server.ServerSocket;
import com.server.ServerException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.testng.annotations.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Created by Юлия on 28.12.2016.
 */
public class RMITest {

    private final String path = "src" + File.separator + "main" + File.separator + "java" + File.separator
        + "com" + File.separator + "server" + File.separator + "data" + File.separator;
    private final String table = "phones.xlsx";

    private final String id = "id";
    private final String code = "code";
    private final String number = "number";

    @BeforeClass
    public void beforeTestCleanFile() throws IOException, InvalidFormatException {
        TestHelper testHelper = new TestHelper();
        testHelper.cleanDataInFile(table, path);
    }

    @Test(priority = 1)
    public void testSave() throws IOException, ClientException, IllegalAccessException, ServerException {
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3000);

        Socket clientSocket = new Socket("localhost", 3000);
        Socket socket = serverSocket.accept();

        SessionFactory sessionFactory = new SessionFactory(clientSocket);
        sessionFactory.save(new Phone(3, 333, 3333333));
        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        TestHelper testHelper = new TestHelper();
        Map<String, String> map = testHelper.readDataFromTableById(table, path, 3, id);

        for(Map.Entry<String, String> entry : map.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(id.equals(key)){
                assertEquals(value, "3");
            }else if(code.equals(key)){
                assertEquals(value, "333");
            }else if(number.equals(key)){
                assertEquals(value, "3333333");
            }
        }
        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 2, expectedExceptions = {ClientException.class}, expectedExceptionsMessageRegExp = "No table mapping for the object: not mapped object")
    public void testSaveForNotMappedObject() throws IOException, ClientException, IllegalAccessException, ServerException, InterruptedException {
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3001);

        Socket clientSocket = new Socket("localhost", 3001);

        SessionFactory sessionFactory = new SessionFactory(clientSocket);
        sessionFactory.save(new String("not mapped object"));
        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 3, expectedExceptions = {ClientException.class}, expectedExceptionsMessageRegExp = "No such table phones exists in the DataBase!")
    public void testSaveForNotExistingTable() throws IOException, ClientException, IllegalAccessException, ServerException, InterruptedException {
        File file = new File(path + table);
        File file2 = new File(path + "phones1.xlsx");
        file.renameTo(file2);

        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3002);

        Socket clientSocket = new Socket("localhost", 3002);
        Socket socket = serverSocket.accept();

        SessionFactory sessionFactory = new SessionFactory(clientSocket);
        sessionFactory.save(new Phone(3, 333, 3333333));
        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String exception = bufferedReader.readLine();
        if(exception.contains("Exception:")){
            throw new ClientException(exception.substring(11));
        }

        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 4)
    public void testGetById() throws IOException, ClientException, IllegalAccessException, InstantiationException, InterruptedException {
        Class<Phone> zClass = Phone.class;
        Phone expected = new Phone(3, 333, 3333333);
        Phone phone = new Phone(3);
        String query;
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3003);

        Socket clientSocket = new Socket("localhost", 3003);
        Socket socket = serverSocket.accept();

        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        QueryGenerator queryGenerator = new QueryGenerator();
        query = queryGenerator.generateGetById(zClass, phone);
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        ResultSetParser resultSetParser = new ResultSetParser();
        Object objectById = resultSetParser.parseValues2ObjectById(clientSocket, zClass);
        System.out.println(objectById);
        assertEquals(objectById, expected);

        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 5, expectedExceptions = {ClientException.class}, expectedExceptionsMessageRegExp = "No data in the table phones with id value = 1")
    public void testGetByIdWhichNotExists() throws IOException, ClientException, IllegalAccessException, InstantiationException, InterruptedException {
        Class<Phone> zClass = Phone.class;
        Phone phone = new Phone(1);
        String query;
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3004);

        Socket clientSocket = new Socket("localhost", 3004);
        Socket socket = serverSocket.accept();

        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        QueryGenerator queryGenerator = new QueryGenerator();
        query = queryGenerator.generateGetById(zClass, phone);
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        ResultSetParser resultSetParser = new ResultSetParser();
        Object objectById = resultSetParser.parseValues2ObjectById(clientSocket, zClass);
        System.out.println(objectById);

        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 6)
    public void testGetAll() throws IOException, ClientException, IllegalAccessException, InstantiationException, InterruptedException {
        Class<Phone> zClass = Phone.class;
        List<Phone> expected = new ArrayList<>();
        expected.add(new Phone(3, 333, 3333333));
        String query;
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3005);

        Socket clientSocket = new Socket("localhost", 3005);
        Socket socket = serverSocket.accept();

        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        QueryGenerator queryGenerator = new QueryGenerator();
        query = queryGenerator.generateGetAll(zClass);
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        ResultSetParser resultSetParser = new ResultSetParser();
        List<Object> objects = resultSetParser.parseValues2Object(clientSocket, zClass);
        System.out.println(objects);
        assertEquals(objects, expected);

        serverSocket.close();
        clientSocket.close();
    }

    @Test(priority = 7, expectedExceptions = {ClientException.class}, expectedExceptionsMessageRegExp = "No data in the table phones")
    public void testGetAllEmptyTable() throws IOException, ClientException, IllegalAccessException, InstantiationException, InvalidFormatException, InterruptedException {
        TestHelper testHelper = new TestHelper();
        testHelper.cleanDataInFile(table, path);

        Class<Phone> zClass = Phone.class;
        String query;
        System.out.println("Server started...");
        java.net.ServerSocket serverSocket = new java.net.ServerSocket(3006);

        Socket clientSocket = new Socket("localhost", 3006);
        Socket socket = serverSocket.accept();

        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        QueryGenerator queryGenerator = new QueryGenerator();
        query = queryGenerator.generateGetAll(zClass);
        bufferedWriter.write(query);
        System.out.println("Query was sent by client to server: " + query);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        ServerSocket internalSocket = new ServerSocket(socket);
        internalSocket.run();

        ResultSetParser resultSetParser = new ResultSetParser();
        resultSetParser.parseValues2Object(clientSocket, zClass);


        serverSocket.close();
        clientSocket.close();
    }

    @AfterMethod
    public void afterRenameFile(Method method){
        File file = new File(path + "phones1.xlsx");
        File file2 = new File(path + table);
        if(file.exists()){
            file.renameTo(file2);
        }

    }
}
