package com.migrator.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Consts {
    public static final String COMMA_DELIMITER = ",";
    public static final List<String> MY_SQL_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "group",
            "primary"
    ));
}
