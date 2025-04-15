package com.zeotap.model;

import lombok.Data;

@Data
public class ConnectionConfig {
    private String host;
    private int port;
    private String user;
    private String database;
    private String jwtToken;
} 