package com.migrator.core.entity.sqllite;

import com.migrator.core.entity.DatabaseInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
public class MigrationTask {
    private Long id;
    private String sourceHost;
    private Integer sourcePort;
    private String sourceUserName;
    private String sourcePassword;
    private String sourceDbName;

    private String targetHost;
    private Integer targetPort;
    private String targetUserName;
    private String targetPassword;
    private String targetDbName;

    private Integer status;
    private Date createTime;
    private Date updateTime;

    public MigrationTask(DatabaseInfo from, DatabaseInfo to){
        this.sourceHost = from.getHost();
        this.sourcePort = from.getPort();
        this.sourceUserName = from.getUsername();
        this.sourcePassword = from.getPassword();
        this.sourceDbName = from.getDbName();

        this.targetHost = to.getHost();
        this.targetPort = to.getPort();
        this.targetUserName = to.getUsername();
        this.targetPassword = to.getPassword();
        this.targetDbName = to.getDbName();
    }
}
