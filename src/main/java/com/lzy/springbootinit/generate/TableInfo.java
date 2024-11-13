package com.lzy.springbootinit.generate;

import lombok.Data;

@Data
public class TableInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表注释
     */
    private String tableComment;

    /**
     * 实体类名
     */
    private String className;

    /**
     * 实体类字段名
     */
    private String fieldName;

    /**
     * 包名（前缀）
     */
    private String packageName;

    /**
     * DTO子包名
     */
    private String dtoPackageName;
}
