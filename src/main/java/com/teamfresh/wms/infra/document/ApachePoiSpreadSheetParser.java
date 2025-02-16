package com.teamfresh.wms.infra.document;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
class ApachePoiSpreadSheetParser implements SpreadSheetParser {
    private static final DataFormatter cellDataFormatter = createDataFormatter();

    @Override
    public SpreadSheetDocument parse(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            List<SpreadSheetDocument.Sheet> sheetList = new ArrayList<>();

            Iterator<org.apache.poi.ss.usermodel.Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                org.apache.poi.ss.usermodel.Sheet sheet = sheetIterator.next();

                List<SpreadSheetDocument.Row> rows = rows(sheet);
                sheetList.add(new SpreadSheetDocument.Sheet(sheet.getSheetName(), rows));
            }

            return new SpreadSheetDocument(sheetList);

        } catch (IOException e) {
            throw new RuntimeException("엑셀/스프레드시트를 여는 중 IO 예외가 발생했습니다.", e);
        }
    }

    private List<SpreadSheetDocument.Row> rows(org.apache.poi.ss.usermodel.Sheet sheet) {
        try {
            Row firstRow = sheet.getRow(sheet.getFirstRowNum());
            if (firstRow == null) {
                return new ArrayList<>();
            }

            short firstCellNum = firstRow.getFirstCellNum();
            short lastCellNum = firstRow.getLastCellNum();

            List<SpreadSheetDocument.Row> rowList = new ArrayList<>();
            Iterator<Row> rowIterator = sheet.rowIterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (!isEmpty(row)) {
                    List<SpreadSheetDocument.Cell> cells = cells(row, firstCellNum, lastCellNum);
                    rowList.add(new SpreadSheetDocument.Row(row.getRowNum(), cells));
                }
            }
            return rowList;

        } catch (Exception e) {
            throw new InvalidSheetException(sheet.getSheetName(), e);
        }
    }

    private boolean isEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null && cell.getCellType() != CellType.BLANK && cell.getCellType() != CellType._NONE) {
                return false;
            }
        }
        return true;
    }

    private List<SpreadSheetDocument.Cell> cells(Row row, short firstCellNum, short lastCellNum) {
        List<SpreadSheetDocument.Cell> cellList = new ArrayList<>();

        for (int i = firstCellNum; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);

            if (cell == null) {
                cell = emptyCell(row, i);
            }

            String parsedValue = switch (cell.getCellType()) {
                case _NONE, BLANK -> SpreadSheetDocument.Cell.NULL_VALUE;
                case STRING, NUMERIC, BOOLEAN, FORMULA -> cellDataFormatter.formatCellValue(cell);
                case ERROR ->
                    throw new InvalidCellValueException(row.getRowNum() + 1, i + 1);
            };

            cellList.add(new SpreadSheetDocument.Cell(i, parsedValue));
        }

        return cellList;
    }

    private Cell emptyCell(Row row, int cellNum) {
        Cell newCell = row.createCell(cellNum);
        newCell.setCellValue("");
        return newCell;
    }

    private static DataFormatter createDataFormatter() {
        DataFormatter formatter = new DataFormatter();
        formatter.setUseCachedValuesForFormulaCells(true);
        return formatter;
    }

    public static class InvalidCellValueException extends RuntimeException {
        public InvalidCellValueException(int row, int col) {
            super("셀 값이 유효하지 않습니다.[row=" + row + ", col=" + col + "]");
        }
    }

    public static class InvalidSheetException extends RuntimeException {
        public InvalidSheetException(String sheetName, Throwable cause) {
            super("시트를 읽는 중 문제가 발생했습니다.[sheetName=" + sheetName + "]", cause);
        }
    }
}
