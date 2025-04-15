package com.zeotap.service;

import com.clickhouse.jdbc.ClickHouseConnection;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.zeotap.model.ConnectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class ClickHouseService {
    
    public List<String> getTables(ConnectionConfig config) throws Exception {
        try (ClickHouseConnection conn = getConnection(config)) {
            List<String> tables = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            return tables;
        }
    }
    
    public List<String> getColumns(ConnectionConfig config, String tableName) throws Exception {
        try (ClickHouseConnection conn = getConnection(config)) {
            List<String> columns = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("DESCRIBE " + tableName)) {
                while (rs.next()) {
                    columns.add(rs.getString(1));
                }
            }
            return columns;
        }
    }
    
    public long getRowCount(ConnectionConfig config, String tableName) throws Exception {
        try (ClickHouseConnection conn = getConnection(config)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count() FROM " + tableName)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        }
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