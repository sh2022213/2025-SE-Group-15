package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;    

/**
 * 预留 xls 文件解析器接口   
 */
public class XlsFileParser implements FileParser {
    @Override    
    public List<Transaction> parse(File file) throws IOException {
        // 这里是解析 xls 文件的具体实现
        // 目前只是占位，需要根据实际情况编写逻辑
        return new ArrayList<>();
    }
}
