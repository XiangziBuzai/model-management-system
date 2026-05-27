package com.model.management.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultVO {
    private int successCount;
    private int failCount;
    private List<ErrorRow> errorList = new ArrayList<>();

    @Data
    public static class ErrorRow {
        private int rowIndex;
        private String reason;

        public ErrorRow(int rowIndex, String reason) {
            this.rowIndex = rowIndex;
            this.reason = reason;
        }
    }

    public void addError(int rowIndex, String reason) {
        this.failCount++;
        this.errorList.add(new ErrorRow(rowIndex, reason));
    }
}
