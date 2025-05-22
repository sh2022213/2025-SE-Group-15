package com.personalfinance.view.panel;

import com.personalfinance.controller.FileParser;
import com.personalfinance.controller.FinanceController;
import com.personalfinance.controller.TxtFileParser;
import com.personalfinance.model.Transaction;
import com.personalfinance.view.MainFrame;
import com.personalfinance.view.component.CategoryComboBox;
import com.personalfinance.view.component.CurrencyTextField;
import com.personalfinance.view.component.MyDatePicker;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TransactionPanel extends JPanel {
    private final FinanceController controller;
    private JTable transactionTable;
    private TransactionTableModel tableModel;
    private CategoryComboBox categoryCombo;

    public TransactionPanel(FinanceController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        // 顶部 - 添加交易表单
        add(createTransactionForm(), BorderLayout.NORTH);

        // 中部 - 交易表格
        tableModel = new TransactionTableModel(controller);
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(30);
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateFormWithSelectedTransaction();
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部 - 操作按钮
        add(createActionButtons(), BorderLayout.SOUTH);
    }

    private JPanel createTransactionForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Transaction"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 类型选择
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Type:"), gbc);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(typeCombo, gbc);

        // 金额输入
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Amount:"), gbc);
        CurrencyTextField amountField = new CurrencyTextField();
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(amountField, gbc);

        // 分类选择
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        categoryCombo = new CategoryComboBox(controller.getUser().getCategories());
        categoryCombo.setVisible(false);

        //gbc.gridx = 1; gbc.gridy = 2;
        //formPanel.add(categoryCombo, gbc);

        // 日期选择
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date:"), gbc);
        MyDatePicker datePicker = new MyDatePicker();
        datePicker.setFormats("yyyy-MM-dd");
        datePicker.setDate(new Date());

        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(datePicker, gbc);

        // 描述输入
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        JTextField descField = new JTextField(20);
        // 添加描述字段的监听器，用于智能匹配分类
        descField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                matchCategory();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                matchCategory();
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                matchCategory();
            }

            private void matchCategory() {
                String description = descField.getText().trim();
                if (!description.isEmpty()) {
                    // 使用AI分析器智能匹配分类
                    String matchedCategory = controller.getAIAnalyzer().matchCategory(description);
                    if (matchedCategory != null) {
                        categoryCombo.setSelectedItem(matchedCategory);
                    }
                }
            }
        });

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(descField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // 1. 下载模板按钮
        JButton templateButton = new JButton("Download Template");
        templateButton.addActionListener(this::downloadTemplate);
        buttonPanel.add(templateButton);

        // 2. 导入按钮
        JButton importButton = new JButton("Import");
        importButton.addActionListener(this::importTransactions);
        buttonPanel.add(importButton);

        // 3. 保存按钮
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                Transaction transaction = new Transaction();
                transaction.setAmount(amountField.getAmount());
                transaction.setCategory((String) categoryCombo.getSelectedItem());
                transaction.setType(typeCombo.getSelectedIndex() == 0 ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE);
                transaction.setDate(datePicker.getDate());
                transaction.setDescription(descField.getText());

                int selectedRow = transactionTable.getSelectedRow();
                if (selectedRow != -1) {
                    String transactionId = tableModel.getTransactionAt(selectedRow).getId();
                    transaction.setId(transactionId);
                    controller.updateTransaction(transaction);
                } else {
                    controller.addTransaction(transaction);
                }

                resetForm();
                tableModel.refresh();
                // 获取父窗口并刷新所有面板
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null && window instanceof MainFrame) {
                    MainFrame mainFrame = (MainFrame) window;
                    mainFrame.refreshAll();
                }

            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Amount format error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);


        // 将按钮面板添加到表单
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);
        formPanel.setPreferredSize(new Dimension(1000, 250));
        return formPanel;
    }

    private JPanel createActionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedTransaction());


        // "修改分类"按钮
        JButton modifyCategoryButton = new JButton("Modify Category");
        modifyCategoryButton.addActionListener(e -> showModifyCategoryDialog());
        buttonPanel.add(modifyCategoryButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> tableModel.refresh());

        buttonPanel.add(deleteButton);
        //buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private void showModifyCategoryDialog() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction record to modify first",
                    "Prompt",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction selectedTransaction = tableModel.getTransactionAt(selectedRow);

        // 创建修改分类的对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modify Category", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 显示交易信息
        JLabel infoLabel = new JLabel("Transaction: " + selectedTransaction.getDescription() +
                " (" + new SimpleDateFormat("yyyy-MM-dd").format(selectedTransaction.getDate()) + ")");
        panel.add(infoLabel);

        // 分类选择
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Category:"));
        CategoryComboBox dialogCategoryCombo = new CategoryComboBox(controller.getUser().getCategories());
        dialogCategoryCombo.setSelectedItem(selectedTransaction.getCategory());
        categoryPanel.add(dialogCategoryCombo);
        panel.add(categoryPanel);

        // 确认和取消按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            String newCategory = (String) dialogCategoryCombo.getSelectedItem();
            selectedTransaction.setCategory(newCategory);
            controller.updateTransaction(selectedTransaction);
            tableModel.refresh();
            dialog.dispose();
        });
        buttonPanel.add(confirmButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction record to delete first",
                    "Prompt",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this transaction record?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 获取选中交易的ID
            String transactionId = tableModel.getTransactionAt(selectedRow).getId();
            // 从控制器删除数据
            boolean success = controller.deleteTransaction(transactionId);

            if (success) {
                // 刷新表格数据
                tableModel.refresh();
                resetForm();
                // 通知主窗口刷新其他面板
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    if (window instanceof MainFrame) {
                        ((MainFrame) window).refreshAll();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Deletion failed, please try again",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //下载横版
    // 下载模板方法实现
    private void downloadTemplate(ActionEvent e) {
        String templateContent = "Amount,Type(Income/Expense),Date(YYYY-MM-DD),Description\n" +
                "100.00,Expense,2023-01-01,Sample data buy book\n" +
                "5000.00,Income,2023-01-01,Monthly salary";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Template File");
        fileChooser.setSelectedFile(new File("Transaction Record Template.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // 确保文件有.txt后缀
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(templateContent);
                JOptionPane.showMessageDialog(this,
                        "Template file has been saved to: " + fileToSave.getAbsolutePath(),
                        "Download Completed",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save template: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //导入
    private void importTransactions(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Text files (*.txt)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                FileParser parser = new TxtFileParser(this.controller);
                List<Transaction> imported = parser.parse(selectedFile);
                if (!imported.isEmpty()) {
                    int count = processImportedTransactions(imported);
                    JOptionPane.showMessageDialog(this,
                            "Successfully imported " + count + " transaction records",
                            "Import Completed",
                            JOptionPane.INFORMATION_MESSAGE);

                    // 刷新界面
                    tableModel.refresh();
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window != null) {
                        if (window instanceof MainFrame) {
                            ((MainFrame) window).refreshAll();
                        }
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "File reading error: " + ex.getMessage(),
                        "Import Failed",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "File format error: " + ex.getMessage(),
                        "Import Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private int processImportedTransactions(List<Transaction> imported) {
        int count = 0;
        for (Transaction t : imported) {
            try {
                controller.addTransaction(t);
                count++;
            } catch (Exception e) {
                System.err.println("Import failed: " + e.getMessage());
            }
        }
        return count;
    }

    private void updateFormWithSelectedTransaction() {
        // 实现从表格填充表单的逻辑
    }

    private void resetForm() {
        // 实现表单重置逻辑
    }

    private static class TransactionTableModel extends AbstractTableModel {
        private final FinanceController controller;
        private List<Transaction> transactions;

        public TransactionTableModel(FinanceController controller) {
            this.controller = controller;
            this.transactions = controller.getTransactions();
        }

        public Transaction getTransactionAt(int row) {
            return transactions.get(row);
        }

        @Override
        public int getRowCount() {
            return transactions.size();
        }

        @Override
        public int getColumnCount() {
            return 5; // 日期、分类、类型、金额、描述
        }

        @Override
        public Object getValueAt(int row, int col) {
            Transaction t = transactions.get(row);
            switch (col) {
                case 0:
                    return new SimpleDateFormat("yyyy-MM-dd").format(t.getDate());
                case 1:
                    return t.getCategory();
                case 2:
                    return t.getType().equals(Transaction.TYPE_INCOME) ? "Income" : "Expense";
                case 3:
                    return t.getAmount();
                case 4:
                    return t.getDescription();
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Date";
                case 1:
                    return "Category";
                case 2:
                    return "Type";
                case 3:
                    return "Amount";
                case 4:
                    return "Description";
                default:
                    return "";
            }
        }

        public void refresh() {
            this.transactions = controller.getTransactions();
            fireTableDataChanged();
        }

    }
}
