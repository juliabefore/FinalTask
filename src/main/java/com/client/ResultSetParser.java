package com.client;

import com.client.annotation.Column;
import com.client.annotation.Id;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp-ptcstd-15 on 12/9/2016.
 */
public class ResultSetParser {

    protected boolean parseColumnNames(Socket socket, Class tClass) throws ClientException, IllegalAccessException, InstantiationException, IOException {
        String msgStart = "No such field ";
        String msgEnd = " in the object ";
        QueryGenerator queryGenerator = new QueryGenerator();
        List<String> columnNamesInObject = queryGenerator.getColumnNamesInObject(tClass.newInstance());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String columnName;
        while (!(columnName = bufferedReader.readLine()).equals("Column check finished")){
            if(!columnNamesInObject.contains(columnName)){
                throw new ClientException(msgStart + columnName + msgEnd + tClass);
            }

        }
        return true;
    }

    //Has to be protected but made it public due to UTests
    public  <T> List<T> parseValues2Object(Socket socket, Class tClass) throws IOException, IllegalAccessException, InstantiationException, ClientException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String columnValue;
        int i = 0;
        List<T> generatedObjectList = new ArrayList<>();
        Field[] fields = tClass.getDeclaredFields();
        boolean isValueSet = false;

        T obj = (T) tClass.newInstance();

        columnValue = bufferedReader.readLine();
        if(columnValue.contains("Exception:")){
            throw new ClientException(columnValue.substring(11));
        }else {
            do{
                String colNameFromServer = columnValue.substring(0, columnValue.indexOf("="));
                String colValueFromServer = columnValue.substring(columnValue.indexOf("=") + 1);

                String columnNameFromObject = null;
                for (Field field : fields) {
                    Class<?> type = field.getType();
                    Column annotation = field.getAnnotation(Column.class);
                    Id annotationId = field.getAnnotation(Id.class);
                    if(annotation != null){
                        if("".equals(annotation.name())){
                            columnNameFromObject = field.getName();
                            isValueSet = setValueIntoField(colNameFromServer, columnNameFromObject, field, type, obj, colValueFromServer);
                            if(isValueSet){
                                i++;
                            }

                        }else {
                            String columnNameFromAnnotation  = annotation.name();
                            isValueSet = setValueIntoField(colNameFromServer, columnNameFromAnnotation, field, type, obj, colValueFromServer);
                            if(isValueSet){
                                i++;
                            }
                        }
                    }else if(annotationId != null){
                        columnNameFromObject = field.getName();
                        isValueSet = setValueIntoField(colNameFromServer, columnNameFromObject, field, type, obj, colValueFromServer);
                        if(isValueSet){
                            i++;
                        }
                    }
                }
                if(i==fields.length){
                    i=0;
                    generatedObjectList.add(obj);
                    obj = (T) tClass.newInstance();
                }
            }while ((columnValue = bufferedReader.readLine()) != null);
        }
        return generatedObjectList;
    }

    private boolean setValueIntoField(String colNameFromServer, String columnNameFromObject, Field field,
                                   Class<?> type, Object obj, String colValueFromServer) throws IllegalAccessException {
        if(colNameFromServer.equals(columnNameFromObject)){
            field.setAccessible(true);
            if(type.equals(int.class)){
                field.setInt(obj, Integer.parseInt(colValueFromServer));
            }else if(type.equals(String.class)){
                field.set(obj, colValueFromServer);
            }
            field.setAccessible(false);
            return true;
        }else {return false;}
    }

    //Has to be protected but made it public due to UTests
    public  <T> T parseValues2ObjectById(Socket socket, Class tClass) throws ClientException, IOException, IllegalAccessException, InstantiationException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String columnValue;
        int i = 0;
        Field[] fields = tClass.getDeclaredFields();
        T obj = (T) tClass.newInstance();
        columnValue = bufferedReader.readLine();
        if(columnValue.contains("Exception:")){
            throw new ClientException(columnValue.substring(11));
        }else {
        do{

        String colNameFromServer = columnValue.substring(0, columnValue.indexOf("="));
        String colValueFromServer = columnValue.substring(columnValue.indexOf("=") + 1);

        String columnNameFromObject = null;
        for (Field field : fields) {
            Class<?> type = field.getType();
            Column annotation = field.getAnnotation(Column.class);
            Id annotationId = field.getAnnotation(Id.class);
            if(annotation != null){
                if("".equals(annotation.name())){
                    columnNameFromObject = field.getName();
                    setValueIntoField(colNameFromServer, columnNameFromObject, field, type, obj, colValueFromServer);
                }else {
                    String columnNameFromAnnotation  = annotation.name();
                    setValueIntoField(colNameFromServer, columnNameFromAnnotation, field, type, obj, colValueFromServer);
                }
            }else if(annotationId != null){
                columnNameFromObject = field.getName();
                setValueIntoField(colNameFromServer, columnNameFromObject, field, type, obj, colValueFromServer);
            }
        }
    }while ((columnValue = bufferedReader.readLine()) != null);
}
        return obj;
    }
}
