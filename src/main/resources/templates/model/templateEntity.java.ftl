package ${tableInfo.packageName}.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
<#list importClassSet as importClass>
import ${importClass};
</#list>

/**
* ${tableInfo.tableComment}
*/
@Data
@TableName(value = "${tableInfo.tableName}")
public class ${tableInfo.className} implements Serializable {

<#list columnInfoList as columnInfo>
    /**
    * ${columnInfo.comment}
    */
<#if (columnInfo.annotationSet)?? && (columnInfo.annotationSet?size > 0) >
    <#list columnInfo.annotationSet as annotation>
    ${annotation}
    </#list>
</#if>
    private ${columnInfo.javaType} ${columnInfo.columnName};

</#list>
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
