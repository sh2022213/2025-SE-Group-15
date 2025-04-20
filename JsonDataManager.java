package com.personalfinance.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonDataManager {
    // 使用固定的数据存储目录
    private static final String DATA_DIR = "data/";
    // Gson实例配置了日期格式和美化输出
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")  // 统一日期序列化格式
            .setPrettyPrinting()         // 生成易读的JSON
            .create();

    static {
        try {
            // 初始化时创建必要目录
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize data directory", e);
        }
    }

    /**
     * 检查指定数据文件是否存在
     * @param filename 目标文件名
     * @return 存在返回true，否则false
     */
    public boolean exists(String filename) {
        return Files.exists(Paths.get(DATA_DIR + filename));
    }

    /**
     * 永久删除数据文件
     * @param filename 要删除的文件名
     * @throws RuntimeException 当删除操作失败时抛出
     */
    public void delete(String filename) {
        try {
            Files.deleteIfExists(Paths.get(DATA_DIR + filename));
        } catch (IOException e) {
            throw new RuntimeException("File deletion error: " + filename, e);
        }
    }

    /**
     * 将数据对象序列化到JSON文件
     * @param filename 目标存储文件名
     * @param data 需要持久化的数据对象
     */
    public <T> void save(String filename, T data) {
        Path path = Paths.get(DATA_DIR + filename);
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Data serialization failed: " + filename, e);
        }
    }

    /**
     * 从JSON文件反序列化单个对象
     * @param filename 源数据文件名
     * @param type 目标类类型
     * @return 解析后的对象，文件不存在时返回null
     */
    public <T> T load(String filename, Class<T> type) {
        Path path = Paths.get(DATA_DIR + filename);
        if (!Files.exists(path)) {
            return null;  // 文件不存在时静默返回
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Data parsing error: " + filename, e);
        }
    }

    /**
     * 加载复杂数据结构（如集合）
     * @param filename 数据源文件
     * @param typeToken 类型标记对象
     * @return 当文件不存在时返回对应类型的空对象
     */
    public <T> T loadCollection(String filename, TypeToken<T> typeToken) {
        Path path = Paths.get(DATA_DIR + filename);
        if (!Files.exists(path)) {
            // 处理集合类型的空值返回
            if (List.class.isAssignableFrom(typeToken.getRawType())) {
                return (T) new ArrayList<>();
            }
            try {
                return (T) typeToken.getRawType().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Empty instance creation failed", e);
            }
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, typeToken.getType());
        } catch (IOException e) {
            throw new RuntimeException("Collection loading error", e);
        }
    }
}
