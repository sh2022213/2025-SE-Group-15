package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

// Define the file parser interface
public interface FileParser {
    // The method of parsing files
    List<Transaction> parse(File file) throws IOException;
}
