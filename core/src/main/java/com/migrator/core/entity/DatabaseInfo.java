package com.migrator.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.migrator.core.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Properties;
import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfo implements Serializable {
    private String host;
    private Integer port;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String dbName;
    private String url;
    private Boolean ignoreCharacterCompare = false;

    public DatabaseInfo(Properties properties, String prefix){
        this(properties.getProperty(prefix+".host"), Integer.parseInt(properties.getProperty(prefix+".port")),
                properties.getProperty(prefix+".username"), properties.getProperty(prefix+".password"),
                properties.getProperty(prefix+".dbname"));
    }

    public DatabaseInfo(String host, Integer port, String username, String password, String dbName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbName = dbName;
        this.url = getUrl();
    }

    public String getUrl() {
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + dbName;
        url += "?useUnicode=true&characterEncoding=utf8&&characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true&useSSL=false";
        String timeZone = TimeZone.getDefault().getID();
        if (!ObjectUtils.isEmpty(timeZone)) {
            url += "&serverTimezone=" + timeZone;
        }
        return url;
    }
}
