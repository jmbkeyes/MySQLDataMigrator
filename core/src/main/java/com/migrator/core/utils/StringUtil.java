package com.migrator.core.utils;

import java.util.List;
import java.util.StringJoiner;

public class StringUtil {
    public static String join(List<String> strs, String delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        for(String str : strs){
            joiner.add(str);
        }

        return joiner.toString();
    }

    public static String repeatStr(String str, int times, String delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        for(int i = 0; i<times; i++){
            joiner.add(str);
        }
        return joiner.toString();
    }
}
