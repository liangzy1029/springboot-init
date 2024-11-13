package com.lzy.springbootinit.generate;

import lombok.Data;

import java.util.Set;

@Data
public class ColumnInfo {

    /**
     * 真实字段名
     */
    private String columnName;

    /**
     * 大驼峰
     */
    private String upperCamelCaseName;

    /**
     * 小驼峰
     */
    private String lowerCamelCaseName;

    /**
     * 字段注释
     */
    private String comment;

    /**
     * java类型
     */
    private String javaType;

    /**
     * mysql类型
     */
    private String jdbcType;

    /**
     * 字符最大长度
     */
    private String characterMaximumLength;

    /**
     * xml mysql类型
     */
    private String xmlJdbcType;

    /**
     * 是否是主键 PRI
     */
    private String columnKey;

    /**
     * 字段的注解
     */
    private Set<String> annotationSet;

    /**
     * 是否是逻辑删除字段
     */
    private Boolean TableLogic;

}
