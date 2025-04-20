package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for parsing financial transaction files into Transaction objects.
 * Implementations should handle specific file formats (CSV, Excel, OFX, etc.).
 */
public interface FileParser {
    
    /**
     * Parses a financial transaction file and converts it into a list of Transaction objects.
     * 
     * @param file The file to be parsed (CSV, Excel, OFX, etc.)
     * @return List of parsed Transaction objects
     * @throws IOException If there's an error reading the file
     * @throws FileParseException If the file format is invalid or unsupported
     * @throws IllegalArgumentException If the file is null or doesn't exist
     */
    List<Transaction> parse(File file) throws IOException, FileParseException;
    
    /**
     * Checks if this parser supports the given file format.
     * 
     * @param file The file to check
     * @return true if this parser can handle the file format, false otherwise 
     */
    default boolean supports(File file) {
        if (file == null) {
            return false;
        }
        return getSupportedExtensions().stream()
            .anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }
    
    /**
     * Gets the file extensions supported by this parser.
     * 
     * @return List of supported file extensions (e.g., ".csv", ".xlsx")
     */
    List<String> getSupportedExtensions();
}

/**
 * Custom exception for file parsing errors.
 */
class FileParseException extends Exception {
    public FileParseException(String message) {
        super(message);
    }
    
    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}