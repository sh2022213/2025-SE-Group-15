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
    private static final String DATA_DIR = "data/";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .setPrettyPrinting()
            .create();

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create data directory", e);
        }
    }

    /**
     * 保存对象到JSON文件
     * @param filename 文件名（不需要路径）
     * @param data 要保存的对象
     * @param <T> 对象类型
     */
    public <T> void save(String filename, T data) {
        Path path = Paths.get(DATA_DIR + filename);
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Save failed: " + filename, e);
        }
    }

    /**
     * 从JSON文件加载对象
     * @param filename 文件名（不需要路径）
     * @param type 对象类型
     * @param <T> 返回类型
     * @return 加载的对象，如果文件不存在返回null
     */
    public <T> T load(String filename, Class<T> type) {
        Path path = Paths.get(DATA_DIR + filename);
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Load Fail: " + filename, e);
        }
    }

    /**
     * 从JSON文件加载泛型集合
     * @param filename 文件名（不需要路径）
     * @param typeToken 类型标记，例如 new TypeToken<List<Transaction>>(){}
     * @param <T> 返回类型
     * @return 加载的集合，如果文件不存在返回空集合
     */
    public <T> T loadCollection(String filename, TypeToken<T> typeToken) {
        Path path = Paths.get(DATA_DIR + filename);
        if (!Files.exists(path)) {
            // 检查是否是 List 类型
            if (List.class.isAssignableFrom(typeToken.getRawType())) {
                return (T) new ArrayList<>();
            }
            try {
                return (T) typeToken.getRawType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to create empty collection", e);
            }

        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = typeToken.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Load Fail: " + filename, e);
        }
    }

    /**
     * 检查数据文件是否存在
     * @param filename 文件名
     * @return 是否存在
     */
    public boolean exists(String filename) {
        return Files.exists(Paths.get(DATA_DIR + filename));
    }

    /**
     * 删除数据文件
     * @param filename 文件名
     */
    public void delete(String filename) {
        try {
            Files.deleteIfExists(Paths.get(DATA_DIR + filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }
}
 