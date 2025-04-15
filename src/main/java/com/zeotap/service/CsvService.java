package com.zeotap.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvService {
    
    public List<String> getHeaders(MultipartFile file, String delimiter) throws IOException {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                 .withDelimiter(delimiter.charAt(0))
                 .withFirstRecordAsHeader()
                 .parse(reader)) {
            return new ArrayList<>(parser.getHeaderMap().keySet());
        }
    }
    
    public List<List<String>> previewData(MultipartFile file, String delimiter, int limit) throws IOException {
        List<List<String>> preview = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                 .withDelimiter(delimiter.charAt(0))
                 .withFirstRecordAsHeader()
                 .parse(reader)) {
            
            int count = 0;
            for (CSVRecord record : parser) {
                if (count >= limit) break;
                List<String> row = new ArrayList<>();
                record.forEach(row::add);
                preview.add(row);
                count++;
            }
        }
        return preview;
    }
    
    public void writeToCsv(List<List<String>> data, List<String> headers, String filePath, String delimiter) throws IOException {
        try (Writer writer = new FileWriter(filePath);
             CSVPrinter printer = new CSVPrinter(writer, 
                 CSVFormat.DEFAULT
                     .withDelimiter(delimiter.charAt(0))
                     .withHeader(headers.toArray(new String[0])))) {
            
            for (List<String> row : data) {
                printer.printRecord(row);
            }
        }
    }
} 