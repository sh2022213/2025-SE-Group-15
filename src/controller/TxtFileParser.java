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

// 实现 txt 文件解析器  
public class TxtFileParser implements FileParser {  
    private final FinanceController controller;

    public TxtFileParser(FinanceController controller) {
        this.controller = controller;
    }

    @Override
    public List<Transaction> parse(File file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        List<String> lines = Files.readAllLines(file.toPath());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Incorrect number of fields in line " + (i + 1));
                }

                Transaction t = new Transaction();
                t.setAmount(new BigDecimal(parts[0].trim()));
                t.setType(parts[1].trim().equalsIgnoreCase("Income") ? "INCOME" : "EXPENSE");
                t.setDate(dateFormat.parse(parts[2].trim()));
                t.setDescription(parts[3].trim());
                String matchedCategory = controller.getAIAnalyzer().matchCategory(t.getDescription());
                t.setCategory(matchedCategory);
                t.setId(UUID.randomUUID().toString());

                transactions.add(t);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalArgumentException("Data format error in line " + (i + 1) + ": " + ex.getMessage());
            }
        }
        return transactions;
    }
}
