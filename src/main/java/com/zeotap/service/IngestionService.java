package com.zeotap.service;

import com.clickhouse.jdbc.ClickHouseConnection;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.zeotap.model.IngestionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class IngestionService {
    
    @Autowired
    private ClickHouseService clickHouseService;
    
    @Autowired
    private CsvService csvService;
    
    public long ingestData(IngestionRequest request, MultipartFile file) throws Exception {
        if ("CLICKHOUSE".equals(request.getSourceType()) && "FLAT_FILE".equals(request.getTargetType())) {
            return clickHouseToCsv(request);
        } else if ("FLAT_FILE".equals(request.getSourceType()) && "CLICKHOUSE".equals(request.getTargetType())) {
            return csvToClickHouse(request, file);
        }
        throw new IllegalArgumentException("Invalid source/target combination");
    }
    
    private long clickHouseToCsv(IngestionRequest request) throws Exception {
        String columns = String.join(", ", request.getSelectedColumns());
        String query = String.format("SELECT %s FROM %s", columns, request.getTableName());
        
        try (ClickHouseConnection conn = getConnection(request.getConnectionConfig());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            List<String> headers = request.getSelectedColumns();
            List<List<String>> data = new ArrayList<>();
            
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= headers.size(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            
            csvService.writeToCsv(data, headers, request.getCsvFilePath(), request.getCsvDelimiter());
            return data.size();
        }
    }
    
    private long csvToClickHouse(IngestionRequest request, MultipartFile file) throws Exception {
        List<String> headers = csvService.getHeaders(file, request.getCsvDelimiter());
        String createTableQuery = buildCreateTableQuery(request.getTableName(), headers);
        
        try (ClickHouseConnection conn = getConnection(request.getConnectionConfig());
             Statement stmt = conn.createStatement()) {
            
            if (!request.isAppendToExisting()) {
                stmt.execute("DROP TABLE IF EXISTS " + request.getTableName());
                stmt.execute(createTableQuery);
            }
            
            String insertQuery = buildInsertQuery(request.getTableName(), headers);
            long count = 0;
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), "UTF-8"));
                 CSVParser parser = CSVFormat.DEFAULT
                     .withDelimiter(request.getCsvDelimiter().charAt(0))
                     .withFirstRecordAsHeader()
                     .parse(reader)) {
                
                for (CSVRecord record : parser) {
                    StringBuilder values = new StringBuilder("(");
                    for (int i = 0; i < headers.size(); i++) {
                        if (i > 0) values.append(", ");
                        values.append("'").append(record.get(i).replace("'", "''")).append("'");
                    }
                    values.append(")");
                    
                    stmt.execute(insertQuery + values);
                    count++;
                }
            }
            
            return count;
        }
    }
    
    private String buildCreateTableQuery(String tableName, List<String> columns) {
        StringBuilder query = new StringBuilder("CREATE TABLE " + tableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) query.append(", ");
            query.append(columns.get(i)).append(" String");
        }
        query.append(") ENGINE = MergeTree() ORDER BY tuple()");
        return query.toString();
    }
    
    private String buildInsertQuery(String tableName, List<String> columns) {
        return "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES ";
    }
    
    private ClickHouseConnection getConnection(ConnectionConfig config) throws Exception {
        String url = String.format("jdbc:clickhouse://%s:%d/%s", 
            config.getHost(), config.getPort(), config.getDatabase());
        
        Properties properties = new Properties();
        properties.setProperty("user", config.getUser());
        properties.setProperty("jwt", config.getJwtToken());
        
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        return dataSource.getConnection();
    }
} 