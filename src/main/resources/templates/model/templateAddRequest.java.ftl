package ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName};

import lombok.Data;

import java.io.Serializable;
<#list importClassSet as importClass>
import ${importClass};
</#list>

/**
* 创建${tableInfo.tableComment}请求
*/
@Data
public class ${tableInfo.className}AddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

<#list addRequestList as columnInfo>
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
}
