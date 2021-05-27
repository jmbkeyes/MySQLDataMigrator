package com.migrator.core.utils;

import com.migrator.core.entity.schema.annotations.DbProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MigrateBeanTools {
    public static <T> Map<String, String> customColumn(Class<T> clazz) {
        Map<String, String> map = new HashMap<>(0);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DbProperty.class)) {
                DbProperty annotation = field.getAnnotation(DbProperty.class);
                String name = annotation.value();
                if (ObjectUtils.isEmpty(name)) {
                    name = field.getName();
                }
                String value = field.getName();
                map.put(name, value);
            }
        }
        return map;
    }
}
