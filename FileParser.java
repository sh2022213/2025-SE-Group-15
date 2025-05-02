package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

// 定义文件解析器接口
public interface FileParser {
    // 解析文件的方法   
    List<Transaction> parse(File file) throws IOException;
}
    