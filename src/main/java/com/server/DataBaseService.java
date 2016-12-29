package com.server;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Created by Юлия on 11.12.2016.
 */
public class DataBaseService {

    private final String path = "src" + File.separator + "main" + File.separator + "java" + File.separator
        + "com" + File.separator + "server" + File.separator + "data" + File.separator;
    private final String from = "FROM";
    Socket socket;
    public DataBaseService(Socket socket){
        this.socket = socket;
    }

    private String getTableNameInsert(String query){
        String tableName = query.substring(12, query.indexOf("("));
        return tableName;
    }

    private String getTableNameSelect(String query){
        String tableName = null;
        String tableStart = query.substring(query.indexOf(from) + 5);
        if(tableStart.contains(" ")){
            tableName = tableStart.substring(0, tableStart.indexOf(" "));
        }else {
            tableName = tableStart;
        }

        return tableName;
    }

    private int getIdValueFromQuery(String query){
        int id = Integer.parseInt(query.substring(query.indexOf("=") + 2));
        return id;
    }

    private List<String> getTableList(String path)  {
        File fileDir = new File(path);
        File[] files = fileDir.listFiles();
        List<String> tableList = new ArrayList<>();

        for (File file : files) {
            String fileName = file.getName();
            String tableName = fileName.substring(0, fileName.lastIndexOf("."));
            tableList.add(tableName);
        }

        return tableList;
    }

    private String findTable(List<String> tableList, String tableName) throws ServerException {
        String exceptionMsg = "No such table " + tableName + " exists in the DataBase!";
        for (String table : tableList) {
            if(tableName.equals(table)){
                return table;
            }
        }
        throw new ServerException(exceptionMsg);
    }

    private List<String> getColumnNamesFromTable(String table, String path)  {
        List<String> columnNameList = new ArrayList<>();

        XSSFWorkbook workbook = null;
        try {
            FileInputStream fi = new FileInputStream(new File(path + table + ".xlsx"));
            workbook = new XSSFWorkbook(fi);
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        Row firstRow = rowIterator.next();
        Iterator<Cell> cellIterator = firstRow.cellIterator();
        while (cellIterator.hasNext()){
            Cell cell = cellIterator.next();
            String stringCellValue = cell.getStringCellValue();
            columnNameList.add(stringCellValue);
        }
        return columnNameList;
    }

    private String getColunmNamesFromInsertQuery(String query){
        String columnList = query.substring(query.indexOf("(") + 1, query.indexOf(")"));

        return columnList;
    }

    private List<String> divideStringIntoList(String columnList){
        List<String> list = new ArrayList<>();
        String[] strings = columnList.split(", ");
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }

    private void isColumnsValid(List<String> columnListFromQuery, List<String> columnListFromTable, String table) throws ServerException {
        String exceptionMsgStart = "No such column ";
        String exceptionMsgEnd = " in the table";

        for (int i = 0; i < columnListFromQuery.size(); i++) {
            if(!columnListFromTable.contains(columnListFromQuery.get(i))){
                throw new ServerException(exceptionMsgStart + columnListFromQuery.get(i)
                        + exceptionMsgEnd + " " + table);
            }
        }

    }

    private List<String> getColumnValuesFromQuery(String query){
        String values = query.substring(query.lastIndexOf("(") + 1, query.lastIndexOf(")"));

        return divideStringIntoList(values);
    }

    private Map<String, String> createMapFromQueryColName2ColValue(List<String> columnNames, List<String> columnValues){
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            map.put(columnNames.get(i), columnValues.get(i));
        }

        return map;
    }

    private void insertDataIntoTable(Map<String, String> map, String table, String path) throws IOException {

        File file = new File(path + table + ".xlsx");
        FileInputStream fi = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fi);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rownum = sheet.getLastRowNum();

        Map<String, Integer> tableMap = new HashMap<>();
        Iterator<Row> rowIterator = sheet.iterator();
        Row firstRow = rowIterator.next();

        int columnIndex = 0;

        for(Map.Entry<String, String> entry: map.entrySet()){
            String key = entry.getKey();

            Iterator<Cell> cellIterator = firstRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String stringCellValue = cell.getStringCellValue();
                if(key.equals(stringCellValue)){
                    columnIndex = cell.getColumnIndex();
                    break;
                }

            }
            tableMap.put(key, columnIndex);
        }

        int newRowIndex = rownum + 1;
        Row row = sheet.createRow(newRowIndex);

        for(Map.Entry<String, String> entry: map.entrySet()){
            String keyQuery = entry.getKey();

            for(Map.Entry<String, Integer> tableEntry: tableMap.entrySet()){
                String keyTable = tableEntry.getKey();
                if(keyQuery.equals(keyTable)){
                    Integer columnPosition = tableEntry.getValue();
                    Cell newCell = row.createCell(columnPosition);
                    if(keyTable.equals("p_id") || keyTable.equals("age") || keyTable.equals("id")
                            || keyTable.equals("code") || keyTable.equals("number")){
                        newCell.setCellValue(new Double(entry.getValue()));
                    }else if(keyTable.equals("p_name")){
                        newCell.setCellValue(entry.getValue());
                    }
                }
            }

        }

        FileOutputStream fo = new FileOutputStream(file);
        workbook.write(fo);
        fo.close();
    }

    private List<List<String>> readDataFromTable(String table, String path) throws IOException {
        File file = new File(path + table + ".xlsx");
        FileInputStream fi = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fi);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getLastRowNum();
        List<List<String>> rowList = new ArrayList<>();
        String stringCellValue;
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            Row row = sheet.getRow(rowNum);
            short cellCount = row.getLastCellNum();
            List<String> cellList = new ArrayList<>();
            for (int cellNum = 0; cellNum < cellCount; cellNum++) {
                Cell cell = row.getCell(cellNum);

                switch (cell.getCellType()){
                    case Cell.CELL_TYPE_STRING:
                        stringCellValue = cell.getStringCellValue();
                        cellList.add(stringCellValue);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        int numericCellValue = (int) cell.getNumericCellValue();
                        cellList.add(String.valueOf(numericCellValue));
                        break;
                }

            }
            rowList.add(cellList);
        }
        return rowList;
    }

    private int findCellNumByName(XSSFSheet sheet, String idNameFromQuery){
        Row firstRow = sheet.getRow(0);
        int cellNum = 0;
        short cellCountFirstRow = firstRow.getLastCellNum();
        for (int i = 0; i < cellCountFirstRow; i++) {
            Cell cell = firstRow.getCell(i);
            if(idNameFromQuery.equals(cell)){
                cellNum = i;
                break;
            }
        }
        return cellNum;
    }

    private int findRowIdByCellValue(XSSFSheet sheet, int rowCount, int cellNumByName, int id, String table) throws ServerException {
        String msgStart = "No data in the table ";
        String msgEnd = " with id value = ";
        int rowNumForId = 0;
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            Row row = sheet.getRow(rowNum);
            Cell cell = row.getCell(cellNumByName);
            int numericCellValue = (int) cell.getNumericCellValue();

            if (id == numericCellValue) {
                rowNumForId = rowNum;
                break;
            }
        }
        if(rowNumForId == 0){
            throw new ServerException(msgStart + table + msgEnd + id);
        }else {
            return rowNumForId;
        }
    }

    private Map<String, String> readDataFromTableById(String table,
                                                      String path, int id, String idNameFromQuery) throws ServerException, IOException {
        File file = new File(path + table + ".xlsx");
        FileInputStream fi = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fi);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getLastRowNum();
        String stringCellValue;

        int cellNumByName = findCellNumByName(sheet, idNameFromQuery);

        int rowIdByCellValue = findRowIdByCellValue(sheet, rowCount, cellNumByName, id, table);

        //Get row for column names
        Row firstRow = sheet.getRow(0);
        //Get row for column values
        Row row = sheet.getRow(rowIdByCellValue);
        short cellCount = row.getLastCellNum();
        Map<String, String> columnName2ColumnValue = new HashMap<>();
        for (int cellNum = 0; cellNum < cellCount; cellNum++) {
            Cell columnName = firstRow.getCell(cellNum);
            Cell cell = row.getCell(cellNum);

            switch (cell.getCellType()){
                case Cell.CELL_TYPE_STRING:
                    stringCellValue = cell.getStringCellValue();
                    columnName2ColumnValue.put(columnName.getStringCellValue(), stringCellValue);
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    int numericCellValue = (int) cell.getNumericCellValue();
                    columnName2ColumnValue.put(columnName.getStringCellValue(), String.valueOf(numericCellValue));
                    break;
            }
        }
        return columnName2ColumnValue;
    }

    private List<Map<String, String>> readAllDataFromTable(String table, String path) throws IOException {
        File file = new File(path + table + ".xlsx");
        FileInputStream fi = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fi);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getLastRowNum();
        String stringCellValue;
        List<Map<String, String>> dataMapList = new ArrayList<>();

        //Get row for column names
        Row firstRow = sheet.getRow(0);
        //Get row for column values
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            Row row = sheet.getRow(rowNum);

            short cellCount = row.getLastCellNum();
            Map<String, String> columnName2ColumnValue = new HashMap<>();
            for (int cellNum = 0; cellNum < cellCount; cellNum++) {
                Cell columnName = firstRow.getCell(cellNum);
                Cell cell = row.getCell(cellNum);
                switch (cell.getCellType()){
                    case Cell.CELL_TYPE_STRING:
                        stringCellValue = cell.getStringCellValue();
                        columnName2ColumnValue.put(columnName.getStringCellValue(), stringCellValue);
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        int numericCellValue = (int) cell.getNumericCellValue();
                        columnName2ColumnValue.put(columnName.getStringCellValue(), String.valueOf(numericCellValue));
                        break;
                }
            }
            dataMapList.add(columnName2ColumnValue);
        }
        return dataMapList;
    }

    public void save(String query) throws ServerException, IOException {
        String tableName = getTableNameInsert(query);
        List<String> tableList = getTableList(path);
        findTable(tableList, tableName);
        List<String> columnNamesFromTable = getColumnNamesFromTable(tableName, path);
        String colNamesFromQueryIntoString = getColunmNamesFromInsertQuery(query);
        List<String> colNamesFromQueryIntoArray = divideStringIntoList(colNamesFromQueryIntoString);
        isColumnsValid(colNamesFromQueryIntoArray, columnNamesFromTable, tableName);
        List<String> columnValuesFromQuery = getColumnValuesFromQuery(query);
        Map<String, String> mapFromQueryColName2ColValue = createMapFromQueryColName2ColValue(colNamesFromQueryIntoArray, columnValuesFromQuery);
        insertDataIntoTable(mapFromQueryColName2ColValue, tableName, path);
        System.out.println("Object was saved in the file " + tableName);
    }

    public void checkColumns(String query) throws ServerException {
        String msgStart = "No such column '";
        String msgEnd = "' in the table ";
        //Get table from query
        //Check if exists
        String tableName = getTableNameSelect(query);
        List<String> tableList = getTableList(path);
        findTable(tableList, tableName);
        //Get columns from query
        List<String> colunmNamesFromSelectQuery = getColunmNamesFromSelectQuery(query);
        //Get columns from found table
        List<String> columnNamesFromTable = getColumnNamesFromTable(tableName, path);
        //Check if columns from query exist in found table
        for (String colFromQuery : colunmNamesFromSelectQuery) {
            for (int i = 0; i < columnNamesFromTable.size();) {
                if(colFromQuery.equals(columnNamesFromTable.get(i))){
                    break;
                }else {
                    i++;
                    if(i == columnNamesFromTable.size()){
                        throw new ServerException(msgStart + colFromQuery + msgEnd + tableName);
                    }
                }
            }
        }
    }

    private List<String> getColunmNamesFromSelectQuery(String query) {
        String columnsFromSelectQuery = query.substring(7, query.indexOf("FROM") - 1);
        return divideStringIntoList(columnsFromSelectQuery);
    }

    public List<List<String>> getValues(String query) throws IOException {
        String tableName = getTableNameSelect(query);
        List<List<String>> dataFromTable = readDataFromTable(tableName, path);
        System.out.println("Column values from DB: " + dataFromTable);
        return dataFromTable;
    }

    protected Map<String, String> getValuesById(String query) throws IOException, ServerException {
        String tableName = getTableNameSelect(query);
        int idValueFromQuery = getIdValueFromQuery(query);
        String idNameFromQuery = getIdNameFromQuery(query);
        Map<String, String> dataFromTable = readDataFromTableById(tableName, path, idValueFromQuery, idNameFromQuery);

        return dataFromTable;
    }
    protected List<Map<String, String>> getAllValues(String query) throws ServerException, IOException {
        String msg = "No data in the table ";
        String tableName = getTableNameSelect(query);
        List<Map<String, String>> allDataFromTable = readAllDataFromTable(tableName, path);

        if(allDataFromTable.size() == 0){
            throw new ServerException(msg + tableName);
        }

        return allDataFromTable;
    }

    private String getIdNameFromQuery(String query) {
        String idNameStart = query.substring(query.indexOf("where") + 6);
        String idName = idNameStart.substring(0, idNameStart.indexOf(" "));
        return idName;
    }

    protected void getAll(String query) throws ServerException, IOException {
        checkColumns(query);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        List<Map<String, String>> allValues = getAllValues(query);
            for (Map<String, String> valuesForObject : allValues) {

                for (Map.Entry<String, String> entry : valuesForObject.entrySet()){
                    bufferedWriter.write(entry.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        System.out.println("Server finished with response...");
    }

    public void getById(String query) throws ServerException, IOException {
        checkColumns(query);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Map<String, String> valuesFromDBById = getValuesById(query);
        for (Map.Entry<String, String> entry : valuesFromDBById.entrySet()){
            bufferedWriter.write(entry.toString());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        System.out.println("Server finished with response...");
    }
}
