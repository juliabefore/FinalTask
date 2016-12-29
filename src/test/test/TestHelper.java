import com.server.ServerException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Created by Юлия on 28.12.2016.
 */
public class TestHelper {

    protected void cleanDataInFile(String table, String path) throws IOException, InvalidFormatException {
        File file = new File(path + table);
        FileInputStream fi = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fi);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int index = sheet.getLastRowNum(); index > sheet.getFirstRowNum(); index--) {
            sheet.removeRow( sheet.getRow(index));
        }

        FileOutputStream fo = new FileOutputStream(file);
        workbook.write(fo);
        fo.close();
    }

    protected Map<String, String> readDataFromTableById(String table, String path, int id, String idNameFromQuery) throws ServerException, IOException {
        File file = new File(path + table);
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
}
