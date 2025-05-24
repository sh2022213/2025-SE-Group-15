package com.test;

import com.personalfinance.model.User;
import com.personalfinance.model.Transaction;
import com.personalfinance.controller.FinanceController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CoreFunctionalityTest {

    private FinanceController controller;

    @BeforeEach
    public void setup() {
        controller = new FinanceController();

        // Simulate user login
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        controller.registerUser(user.getUsername(), "123456");  // Registration initialization
        controller.loginUser(user.getUsername(), "123456");     // Login initialization
    }

    // 1. Test User password hashing and validation
    @Test
    public void testUserPasswordHashing() {
        User user = new User();
        user.setPassword("secure123");
        assertTrue(user.validatePassword("secure123"));
        assertFalse(user.validatePassword("wrongpass"));
    }

    // 2. Test Transaction.getSignedAmount method (expenses should be negative)
    @Test
    public void testTransactionSignedAmount() {
        Transaction expense = new Transaction(new BigDecimal("100"), "Food", "EXPENSE", new Date(), "Dinner");
        Transaction income = new Transaction(new BigDecimal("500"), "Salary", "INCOME", new Date(), "Bonus");

        assertEquals(new BigDecimal("-100"), expense.getSignedAmount());
        assertEquals(new BigDecimal("500"), income.getSignedAmount());
    }

    // 3. Test Transaction type check methods
    @Test
    public void testTransactionTypeChecks() {
        Transaction t = new Transaction(new BigDecimal("300"), "Salary", "INCOME", new Date(), "Payday");
        assertTrue(t.isIncome());
        assertFalse(t.isExpense());
    }

    // 4. Test FinanceController.addTransaction
    @Test
    public void testAddTransaction() {
        Transaction t = new Transaction(new BigDecimal("800"), "Salary", "INCOME", new Date(), "Salary");
        controller.addTransaction(t);
        assertEquals(1, controller.getTransactions().size());
    }

    // 5. Test FinanceController.getTotalIncome
    @Test
    public void testGetTotalIncome() {
        controller.addTransaction(new Transaction(new BigDecimal("1000"), "Salary", "INCOME", new Date(), "A"));
        controller.addTransaction(new Transaction(new BigDecimal("500"), "Gift", "INCOME", new Date(), "B"));
        assertEquals(new BigDecimal("1500"), controller.getTotalIncome());
    }

    // 6. Test FinanceController.getCategorySpending
    @Test
    public void testGetCategorySpending() {
        controller.addTransaction(new Transaction(new BigDecimal("200"), "Food", "EXPENSE", new Date(), "Lunch"));
        controller.addTransaction(new Transaction(new BigDecimal("300"), "Food", "EXPENSE", new Date(), "Dinner"));
        controller.addTransaction(new Transaction(new BigDecimal("100"), "Transportation", "EXPENSE", new Date(), "Bus"));

        Map<String, BigDecimal> spending = controller.getCategorySpending();
        assertEquals(new BigDecimal("500"), spending.get("Food"));
        assertEquals(new BigDecimal("100"), spending.get("Transportation"));
    }
}
