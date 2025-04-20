package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TXT 文件解析器实现
 * 支持格式：金额,类型,日期,描述
 * 示例：100.00,Income,2023-01-15,Salary payment
 */
public class TxtFileParser implements FileParser {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int EXPECTED_FIELD_COUNT = 4;
    private static final String INCOME_TYPE = "Income";
    
    private final FinanceController controller;

    public TxtFileParser(FinanceController controller) {
        this.controller = controller;
    }

    @Override
    public List<Transaction> parse(File file) throws IOException {
        List<String> lines = readFileLines(file);
        return parseTransactions(lines);
    }

    private List<String> readFileLines(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    private List<Transaction> parseTransactions(List<String> lines) {
        List<Transaction> transactions = new ArrayList<>();
        
        for (int i = 1; i < lines.size(); i++) { // Skip header line
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                parseTransactionLine(line, i + 1).ifPresent(transactions::add);
            }
        }
        
        return transactions;
    }

    private Optional<Transaction> parseTransactionLine(String line, int lineNumber) {
        try {
            String[] parts = validateAndSplitLine(line, lineNumber);
            return Optional.of(createTransaction(parts, lineNumber));
        } catch (Exception ex) {
            System.err.printf("Error parsing line %d: %s%n", lineNumber, ex.getMessage());
            return Optional.empty();
        }
    }

    private String[] validateAndSplitLine(String line, int lineNumber) {
        String[] parts = line.split(",");
        if (parts.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                String.format("Expected %d fields but found %d", EXPECTED_FIELD_COUNT, parts.length)
            );
        }
        return parts;
    }

    private Transaction createTransaction(String[] parts, int lineNumber) throws ParseException {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setAmount(parseAmount(parts[0].trim(), lineNumber));
        transaction.setType(determineTransactionType(parts[1].trim(), lineNumber));
        transaction.setDate(parseDate(parts[2].trim(), lineNumber));
        transaction.setDescription(parts[3].trim());
        transaction.setCategory(determineCategory(transaction.getDescription()));
        return transaction;
    }

    private BigDecimal parseAmount(String amountStr, int lineNumber) {
        try {
            return new BigDecimal(amountStr);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid amount format", ex);
        }
    }

    private String determineTransactionType(String typeStr, int lineNumber) {
        if (typeStr.equalsIgnoreCase(INCOME_TYPE)) {
            return "INCOME";
        } else if (typeStr.equalsIgnoreCase("Expense")) {
            return "EXPENSE";
        }
        throw new IllegalArgumentException("Invalid transaction type: " + typeStr);
    }

    private Date parseDate(String dateStr, int lineNumber) throws ParseException {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException ex) {
            throw new ParseException("Invalid date format (expected yyyy-MM-dd)", ex.getErrorOffset());
        }
    }

    private String determineCategory(String description) {
        return controller.getAIAnalyzer().matchCategory(description);
    }
}