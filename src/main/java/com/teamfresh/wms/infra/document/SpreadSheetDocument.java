package com.teamfresh.wms.infra.document;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SpreadSheetDocument implements Iterable<SpreadSheetDocument.Sheet> {
    private final List<Sheet> sheets;

    public SpreadSheetDocument(List<Sheet> sheets) {
        this.sheets = sheets;
    }

    public Sheet get(int index) {
        return sheets.get(index);
    }

    public Sheet get(String sheetName) {
        for (Sheet sheet : sheets) {
            if (Objects.equals(sheet.name(), sheetName)) {
                return sheet;
            }
        }
        throw new SheetNotFoundException(sheetName);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return sheets.iterator();
    }

    public record Sheet(String name, List<Row> rows) implements Iterable<Row> {
        public Row get(int index) {
            return rows.get(index);
        }

        @Override
        public Iterator<Row> iterator() {
            return rows.iterator();
        }
    }

    public record Row(int index, List<Cell> cells) implements Iterable<Cell> {
        public Cell get(int i) {
            if (i < 0 || i >= cells.size()) {
                return new Cell(i, Cell.NULL_VALUE);
            }
            return cells.get(i);
        }

        @Override
        public Iterator<Cell> iterator() {
            return cells.iterator();
        }
    }

    public record Cell(int index, String value) {

        public static final String NULL_VALUE = "NULL";

        public String getOptionalValue() {
            return isNull() ? null : value;
        }

        public boolean isNull() {
            return NULL_VALUE.equals(value) || (value != null && value.isBlank());
        }
    }

    public static class SheetNotFoundException extends RuntimeException {
        public SheetNotFoundException(String sheetName) {
            super("'" + sheetName + "'시트를 찾을 수 없습니다.");
        }
    }
}
