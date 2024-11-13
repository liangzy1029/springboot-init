package ${tableInfo.packageName}.model.vo;

import ${tableInfo.packageName}.model.entity.${tableInfo.className};
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
<#list importClassSet as importClass>
import ${importClass};
</#list>

/**
* ${tableInfo.tableComment}视图
*/
@Data
public class ${tableInfo.className}VO implements Serializable {

    private static final long serialVersionUID = 1L;

<#list voList as columnInfo>
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
<#list voList as columnInfo>
<#if columnInfo.columnName == "userId">
    /**
    * 创建用户信息
    */
    private UserVO user;
    
<#break>
</#if>
</#list>
    /**
    * 封装类转对象
    *
    * @param ${tableInfo.fieldName}VO
    * @return
    */
    public static ${tableInfo.className} voToObj(${tableInfo.className}VO ${tableInfo.fieldName}VO) {
        if (${tableInfo.fieldName}VO == null) {
            return null;
        }
        ${tableInfo.className} ${tableInfo.fieldName} = new ${tableInfo.className}();
        BeanUtils.copyProperties(${tableInfo.fieldName}VO, ${tableInfo.fieldName});
        return ${tableInfo.fieldName};
    }

    /**
    * 对象转封装类
    *
    * @param ${tableInfo.fieldName}
    * @return
    */
    public static ${tableInfo.className}VO objToVo(${tableInfo.className} ${tableInfo.fieldName}) {
        if (${tableInfo.fieldName} == null) {
            return null;
        }
        ${tableInfo.className}VO ${tableInfo.fieldName}VO = new ${tableInfo.className}VO();
        BeanUtils.copyProperties(${tableInfo.fieldName}, ${tableInfo.fieldName}VO);
        return ${tableInfo.fieldName}VO;
    }

}