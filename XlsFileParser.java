package com.personalfinance.controller;

import com.personalfinance.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Excel 文件解析器实现
 * 支持格式：金额 | 类型 | 日期 | 描述 | 分类(可选)
 * 日期格式：yyyy-MM-dd
 */
public class XlsFileParser implements FileParser {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int MIN_COLUMNS = 4; // 最小必需列数
    private static final String INCOME_TYPE = "Income";
    
    private final FinanceController financeController;

    public XlsFileParser(FinanceController financeController) {
        this.financeController = financeController;
    }

    @Override
    public List<Transaction> parse(File file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 从第2行开始(跳过标题)
                Row row = sheet.getRow(i);
                if (row != null) {
                    parseRow(row).ifPresent(transactions::add);
                }
            }
        }
        
        return transactions;
    }

    private Optional<Transaction> parseRow(Row row) {
        try {
            // 检查最小列数
            if (row.getLastCellNum() < MIN_COLUMNS) {
                throw new IllegalArgumentException("行 " + (row.getRowNum() + 1) + " 缺少必要列");
            }

            Transaction transaction = new Transaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setAmount(parseAmount(getCellValue(row.getCell(0))));
            transaction.setType(determineType(getCellValue(row.getCell(1))));
            transaction.setDate(parseDate(getCellValue(row.getCell(2))));
            transaction.setDescription(getCellValue(row.getCell(3)));
            
            // 如果第5列存在，尝试获取分类
            if (row.getLastCellNum() >= 5) {
                String category = getCellValue(row.getCell(4));
                transaction.setCategory(category.isEmpty() ? 
                    determineCategory(transaction.getDescription()) : category);
            } else {
                transaction.setCategory(determineCategory(transaction.getDescription()));
            }
            
            return Optional.of(transaction);
        } catch (Exception e) {
            System.err.printf("解析行 %d 时出错: %s%n", row.getRowNum() + 1, e.getMessage());
            return Optional.empty();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).toString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的金额格式: " + value);
        }
    }

    private String determineType(String typeStr) {
        if (typeStr.equalsIgnoreCase(INCOME_TYPE)) {
            return "INCOME";
        } else if (typeStr.equalsIgnoreCase("Expense")) {
            return "EXPENSE";
        }
        throw new IllegalArgumentException("无效的交易类型: " + typeStr);
    }

    private Date parseDate(String dateStr) throws java.text.ParseException {
        return DATE_FORMAT.parse(dateStr);
    }

    private String determineCategory(String description) {
        return financeController.getAIAnalyzer().matchCategory(description);
    }
}