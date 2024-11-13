<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${tableInfo.packageName}.mapper.${tableInfo.className}Mapper">

    <resultMap id="BaseResultMap" type="${tableInfo.packageName}.model.entity.${tableInfo.className}">
<#list columnInfoList as columnInfo>
    <#if columnInfo.columnKey?? && columnInfo.columnKey == "PRI">
        <id property="${columnInfo.lowerCamelCaseName}" column="${columnInfo.columnName}" jdbcType="${columnInfo.xmlJdbcType}"/>
    <#else>
        <result property="${columnInfo.lowerCamelCaseName}" column="${columnInfo.columnName}" jdbcType="${columnInfo.xmlJdbcType}"/>
    </#if>
</#list>
    </resultMap>

    <sql id="Base_Column_List">
        <#list columnInfoList as columnInfo><#if (columnInfo_index + 1) == 1 || (columnInfo_index + 1) % 3 != 1>${columnInfo.columnName}<#else>        ${columnInfo.columnName}</#if><#if (columnInfo_index + 1) % 3 != 0 && columnInfo_has_next>,</#if><#if (columnInfo_index + 1) % 3 == 0 && columnInfo_has_next>
        </#if></#list>

    </sql>

</mapper>
