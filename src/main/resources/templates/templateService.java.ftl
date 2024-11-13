package ${tableInfo.packageName}.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}QueryRequest;
import ${tableInfo.packageName}.model.entity.${tableInfo.className};
import ${tableInfo.packageName}.model.vo.${tableInfo.className}VO;

/**
 * ${tableInfo.tableComment}服务
 */
public interface ${tableInfo.className}Service extends IService${"<"}${tableInfo.className}${">"} {

    /**
     * 校验数据
     *
     * @param ${tableInfo.fieldName}
     * @param add  对创建的数据进行校验
     */
    void valid${tableInfo.className}(${tableInfo.className} ${tableInfo.fieldName}, boolean add);

    /**
     * 获取查询条件
     *
     * @param ${tableInfo.fieldName}QueryRequest
     * @return
     */
    QueryWrapper${"<"}${tableInfo.className}${">"} getQueryWrapper(${tableInfo.className}QueryRequest ${tableInfo.fieldName}QueryRequest);

    /**
     * 获取应用封装
     *
     * @param ${tableInfo.fieldName}
     * @return
     */
    ${tableInfo.className}VO get${tableInfo.className}VO(${tableInfo.className} ${tableInfo.fieldName});

    /**
     * 分页获取应用封装
     *
     * @param ${tableInfo.fieldName}Page
     * @return
     */
    Page${"<"}${tableInfo.className}VO${">"} get${tableInfo.className}VOPage(Page${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}Page);

}
