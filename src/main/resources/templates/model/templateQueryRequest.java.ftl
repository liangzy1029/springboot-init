package ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName};

import ${tableInfo.packageName}.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
<#list importClassSet as importClass>
import ${importClass};
</#list>

/**
* 查询${tableInfo.tableComment}请求
*/
@Data
@EqualsAndHashCode(callSuper = true)
public class ${tableInfo.className}QueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

<#list queryRequestList as columnInfo>
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
