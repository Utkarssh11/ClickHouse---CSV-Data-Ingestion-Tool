package com.zeotap.model;

import lombok.Data;
import java.util.List;

@Data
public class IngestionRequest {
    private String sourceType; // "CLICKHOUSE" or "FLAT_FILE"
    private String targetType; // "CLICKHOUSE" or "FLAT_FILE"
    private ConnectionConfig connectionConfig;
    private String tableName;
    private List<String> selectedColumns;
    private String csvDelimiter;
    private String csvFilePath;
    private boolean appendToExisting;
} 