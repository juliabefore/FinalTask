package com.client;

import com.client.annotation.Column;
import com.client.annotation.Id;
import com.client.annotation.Table;
import com.client.data.Person;
import com.client.data.Phone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp-ptcstd-15 on 12/9/2016.
 */
public class QueryGenerator {

    protected String generateSave(Object value) throws ClientException, IllegalAccessException {

        String start = "INSERT INTO ";
        String table_name = getTableName(value);
        String columnNameList = "(" + getColumnNames(value) + ")";
        String center = " VALUES ";
        String columnValueList = "(" + getColumnValues(value) + ")";

        String queryString = start + table_name + columnNameList + center + columnValueList;

        return queryString;
    }

    public String generateGetAll(Class tClass) throws IllegalAccessException, InstantiationException, ClientException {

        String select = "SELECT ";
        Object value = tClass.newInstance();
        List<String> columnNamesInObject = getColumnNamesInObject(value);
        String separatedColumns = generateSeparatedString(columnNamesInObject);
        String from = " FROM ";
        String table_name = getTableName(value);
        String query = select + separatedColumns + from + table_name;

        return query;
    }

    public <T> String generateGetById(Class<T> tClass, T object) throws ClientException, InstantiationException, IllegalAccessException {
        String msg = "No Id column in the object ";
        String getAll = generateGetAll(tClass);

        List<String> columnNamesInObject = getColumnNamesInObject(object);
        String idName = null;
        int idValue = 0;
        String idColumn = getIdColumn(object);
        if(idColumn != null){
            idName = idColumn;
            idValue = getIdColumnValue(object);
        }else if(idColumn == null){
            for (String column : columnNamesInObject) {
                if(column.toLowerCase().contains("id")){
                    idName = column;
                    idValue = getIdColumnValue(object, idName);
                    break;
                }
            }
        }else{
            throw new ClientException(msg + object);
        }

        String condition = " where " + idName + " = " + idValue;

        return getAll + condition;
    }

    private String getTableName(Object value) throws ClientException {
        Class clazz = value.getClass();
        Table annotation = (Table) clazz.getAnnotation(Table.class);

        if(annotation != null){
            return annotation.name();
        }else {
        throw new ClientException("No table mapping for the object: " + value);
        }
    }

    private String getColumnNames(Object value) throws ClientException {

        String errorMsg = "No fields with @Column annotation were found for object ";
        String columns = "";

        List<String> columnList = getColumnNamesInObject(value);
        if(columnList.size() == 0){
            throw new ClientException(errorMsg + value);
        }

        columns = generateSeparatedString(columnList);

        return columns;
    }

    protected List<String> getColumnNamesInObject(Object value) {

        Field[] annotatedFields = getAnnotatedFields(value);
        String columnNameFromObject = null;
        List<String> columnList = new ArrayList<>();
        for (Field field : annotatedFields) {
            Column annotation = field.getAnnotation(Column.class);
            Id annotationId = field.getAnnotation(Id.class);
            if(annotation != null){
                if("".equals(annotation.name())){
                    columnNameFromObject = field.getName();
                    columnList.add(columnNameFromObject);
                }else {
                    String columnNameFromAnnotation  = annotation.name();
                    columnList.add(columnNameFromAnnotation);
                }
            }else if(annotationId != null){
                columnNameFromObject = field.getName();
                columnList.add(columnNameFromObject);
            }
        }
        return columnList;
    }

    private <T> String getIdColumn(T value){
        Field[] annotatedFields = getAnnotatedFields(value);
        String columnId = null;
        for (Field field : annotatedFields) {
            Id annotation = field.getAnnotation(Id.class);
            if(annotation != null){
                columnId  = field.getName();
            }
        }
        return columnId;
    }

    private <T> int getIdColumnValue(T value, String columnId) throws IllegalAccessException {
        Field[] annotatedFields = getAnnotatedFields(value);
        int columnIdValue = 0;
        for (Field field : annotatedFields) {
            Column annotation = field.getAnnotation(Column.class);
            if(annotation != null && columnId.equals(annotation.name())) {
                field.setAccessible(true);
                columnIdValue = field.getInt(value);
                field.setAccessible(false);
            }
        }
        return columnIdValue;
    }

    private <T> int getIdColumnValue(T value) throws IllegalAccessException {
        Field[] annotatedFields = getAnnotatedFields(value);
        int columnIdValue = 0;
        for (Field field : annotatedFields) {
            Id annotation = field.getAnnotation(Id.class);
            if(annotation != null) {
                field.setAccessible(true);
                columnIdValue = field.getInt(value);
                field.setAccessible(false);
            }
        }
        return columnIdValue;
    }

    private String getColumnValues(Object value) throws IllegalAccessException {
        Field[] annotatedFields = getAnnotatedFields(value);
        List<String> columnValueList = new ArrayList<>();
        String columnValues = "";
        String columnValue = null;
        for (Field field : annotatedFields) {
            Column annotation = field.getAnnotation(Column.class);
            Id annotationId = field.getAnnotation(Id.class);
            if(annotation != null){
                field.setAccessible(true);
                columnValue = String.valueOf(field.get(value));
                columnValueList.add(columnValue);
                field.setAccessible(false);
            }else if(annotationId != null){
                field.setAccessible(true);
                columnValue = String.valueOf(field.get(value));
                columnValueList.add(columnValue);
                field.setAccessible(false);
            }
        }

        columnValues = generateSeparatedString(columnValueList);
        return columnValues;
    }

    private Field[] getAnnotatedFields(Object value){
        Class clazz = value.getClass();
        Field [] fields = clazz.getDeclaredFields();
        return fields;
    }

    private String generateSeparatedString(List<String> list){
        String separatedString = "";
        for (int i = 0; i < list.size(); i++) {
            if(i <list.size() - 1){
                separatedString += list.get(i) + ", ";
            }else {
                separatedString += list.get(i);
            }
        }
        return separatedString;
    }
}