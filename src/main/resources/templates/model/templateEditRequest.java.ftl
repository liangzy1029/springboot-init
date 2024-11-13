package ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName};

import lombok.Data;

import java.io.Serializable;
<#list importClassSet as importClass>
import ${importClass};
</#list>

/**
* 编辑${tableInfo.tableComment}请求
*/
@Data
public class ${tableInfo.className}EditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

<#list editRequestList as columnInfo>
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
