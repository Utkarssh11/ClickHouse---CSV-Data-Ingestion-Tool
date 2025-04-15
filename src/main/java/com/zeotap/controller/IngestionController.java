package com.zeotap.controller;

import com.zeotap.model.ConnectionConfig;
import com.zeotap.model.IngestionRequest;
import com.zeotap.service.ClickHouseService;
import com.zeotap.service.CsvService;
import com.zeotap.service.IngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
public class IngestionController {
    
    @Autowired
    private ClickHouseService clickHouseService;
    
    @Autowired
    private CsvService csvService;
    
    @Autowired
    private IngestionService ingestionService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("connectionConfig", new ConnectionConfig());
        return "index";
    }
    
    @PostMapping("/connect")
    @ResponseBody
    public ResponseEntity<List<String>> connect(@RequestBody ConnectionConfig config) {
        try {
            List<String> tables = clickHouseService.getTables(config);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error connecting to ClickHouse", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/columns")
    @ResponseBody
    public ResponseEntity<List<String>> getColumns(
            @RequestBody ConnectionConfig config,
            @RequestParam String tableName) {
        try {
            List<String> columns = clickHouseService.getColumns(config, tableName);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            log.error("Error getting columns", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/preview")
    @ResponseBody
    public ResponseEntity<List<List<String>>> previewCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam String delimiter) {
        try {
            List<List<String>> preview = csvService.previewData(file, delimiter, 100);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Error previewing CSV", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/ingest")
    @ResponseBody
    public ResponseEntity<Long> ingestData(
            @ModelAttribute IngestionRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            long count = ingestionService.ingestData(request, file);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error during ingestion", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 