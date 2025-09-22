package com.example.iot_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class DataCompressionService {
    private static final Logger log = LoggerFactory.getLogger(DataCompressionService.class);

    private final ObjectMapper objectMapper;

    public DataCompressionService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Nén dữ liệu JSON thành byte array
     */
    public byte[] compressData(Object data) {
        try {
            // Convert object to JSON string
            String jsonString = objectMapper.writeValueAsString(data);

            // Compress JSON string
            return compressString(jsonString);

        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to compress data", e);
        }
    }

    /**
     * Nén string thành byte array sử dụng GZIP
     */
    public byte[] compressString(String data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {

            gzipOut.write(data.getBytes("UTF-8"));
            gzipOut.finish();

            byte[] compressed = baos.toByteArray();

            log.debug("Compressed {} bytes to {} bytes (ratio: {:.2f})",
                    data.getBytes("UTF-8").length,
                    compressed.length,
                    (double) compressed.length / data.getBytes("UTF-8").length);

            return compressed;

        } catch (IOException e) {
            log.error("Error compressing data: {}", e.getMessage());
            throw new RuntimeException("Failed to compress string", e);
        }
    }

    /**
     * Giải nén byte array thành string
     */
    public String decompressToString(byte[] compressedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                GZIPInputStream gzipIn = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            return baos.toString("UTF-8");

        } catch (IOException e) {
            log.error("Error decompressing data: {}", e.getMessage());
            throw new RuntimeException("Failed to decompress data", e);
        }
    }

    /**
     * Giải nén byte array thành object
     */
    public <T> T decompressToObject(byte[] compressedData, Class<T> targetClass) {
        try {
            String jsonString = decompressToString(compressedData);
            return objectMapper.readValue(jsonString, targetClass);

        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object: {}", e.getMessage());
            throw new RuntimeException("Failed to decompress to object", e);
        }
    }

    /**
     * Giải nén byte array thành List
     */
    public <T> List<T> decompressToList(byte[] compressedData, Class<T> elementClass) {
        try {
            String jsonString = decompressToString(compressedData);
            return objectMapper.readValue(jsonString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));

        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to list: {}", e.getMessage());
            throw new RuntimeException("Failed to decompress to list", e);
        }
    }

    /**
     * Tính tỷ lệ nén
     */
    public double calculateCompressionRatio(int originalSize, int compressedSize) {
        if (originalSize == 0)
            return 0.0;
        return (double) compressedSize / originalSize;
    }

    /**
     * Ước tính dung lượng sau khi nén
     */
    public long estimateCompressedSize(Object data) {
        try {
            String jsonString = objectMapper.writeValueAsString(data);
            byte[] compressed = compressString(jsonString);
            return compressed.length;

        } catch (JsonProcessingException e) {
            log.error("Error estimating compressed size: {}", e.getMessage());
            return 0;
        }
    }
}