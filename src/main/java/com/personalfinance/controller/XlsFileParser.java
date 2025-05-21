package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserve an interface for parsing xls files
 */
public class XlsFileParser implements FileParser {
    @Override
    public List<Transaction> parse(File file) throws IOException {
        // This is the specific implementation for parsing xls files.
        // Currently, it is just a placeholder. 
		// The logic needs to be written based on the actual situation.
        return new ArrayList<>();
    }
}
