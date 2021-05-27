package com.migrator.core.entity.sqllite;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
public class MigrationSubTask {
    private Long id;
    private Long taskId;
    private Long minId;
    private Long maxId;
    private Integer rows;

    private Date createTime;
    private Date updateTime;
}
