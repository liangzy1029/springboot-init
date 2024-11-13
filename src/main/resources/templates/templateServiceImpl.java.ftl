package ${tableInfo.packageName}.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${tableInfo.packageName}.common.ErrorCode;
import ${tableInfo.packageName}.constant.CommonConstant;
import ${tableInfo.packageName}.exception.ThrowUtils;
import ${tableInfo.packageName}.mapper.${tableInfo.className}Mapper;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}QueryRequest;
import ${tableInfo.packageName}.model.entity.User;
import ${tableInfo.packageName}.model.entity.${tableInfo.className};
import ${tableInfo.packageName}.model.vo.UserVO;
import ${tableInfo.packageName}.model.vo.${tableInfo.className}VO;
import ${tableInfo.packageName}.service.${tableInfo.className}Service;
import ${tableInfo.packageName}.service.UserService;
import ${tableInfo.packageName}.utils.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

<#list importClassSet as importClass>
import ${importClass};
</#list>
import java.util.List;
import java.util.stream.Collectors;

/**
 * ${tableInfo.tableComment}服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ${tableInfo.className}ServiceImpl extends ServiceImpl${"<"}${tableInfo.className}Mapper, ${tableInfo.className}${">"} implements ${tableInfo.className}Service {

    private final UserService userService;

    /**
     * 校验数据
     *
     * @param ${tableInfo.fieldName}
     * @param add  对创建的数据进行校验
     */
    @Override
    public void valid${tableInfo.className}(${tableInfo.className} ${tableInfo.fieldName}, boolean add) {
        ThrowUtils.throwIf(${tableInfo.fieldName} == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        <#list addRequestList as columnInfo>
        ${columnInfo.javaType} ${columnInfo.lowerCamelCaseName} = ${tableInfo.fieldName}.get${columnInfo.upperCamelCaseName}();
        </#list>

        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
    <#list addRequestList as columnInfo>
        <#if columnInfo.javaType == "String">
            ThrowUtils.throwIf(StrUtil.isBlank(${columnInfo.lowerCamelCaseName}), ErrorCode.PARAMS_ERROR, "${columnInfo.comment}不能为空");
        </#if>
        <#if columnInfo.javaType == "Long" || columnInfo.javaType == "Integer" || columnInfo.javaType == "Date">
            ThrowUtils.throwIf(${columnInfo.lowerCamelCaseName} == null, ErrorCode.PARAMS_ERROR, "${columnInfo.comment}不能为空");
        </#if>
    </#list>
        }
        // 修改数据时，有参数则校验
        // 补充校验规则
<#list addRequestList as columnInfo>
    <#if columnInfo.jdbcType == "char" || columnInfo.jdbcType == "varchar">
        if (StrUtil.isNotBlank(${columnInfo.lowerCamelCaseName})) {
            ThrowUtils.throwIf(${columnInfo.lowerCamelCaseName}.length() > ${columnInfo.characterMaximumLength}, ErrorCode.PARAMS_ERROR, "${columnInfo.comment}要小于 ${columnInfo.characterMaximumLength}");
        }
    </#if>
</#list>

    }

    /**
     * 获取查询条件
     *
     * @param ${tableInfo.fieldName}QueryRequest
     * @return
     */
    @Override
    public QueryWrapper${"<"}${tableInfo.className}${">"} getQueryWrapper(${tableInfo.className}QueryRequest ${tableInfo.fieldName}QueryRequest) {
        QueryWrapper${"<"}${tableInfo.className}${">"} queryWrapper = new QueryWrapper${"<"}${">"}();
        if (${tableInfo.fieldName}QueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        <#list queryRequestList as columnInfo>
        ${columnInfo.javaType} ${columnInfo.lowerCamelCaseName} = ${tableInfo.fieldName}QueryRequest.get${columnInfo.upperCamelCaseName}();
        </#list>
        String sortField = ${tableInfo.fieldName}QueryRequest.getSortField();
        String sortOrder = ${tableInfo.fieldName}QueryRequest.getSortOrder();

        // 补充需要的查询条件

        // 模糊查询
<#list queryRequestList as columnInfo>
    <#if columnInfo.javaType == "String">
        queryWrapper.like(StrUtil.isNotBlank(${columnInfo.lowerCamelCaseName}), "${columnInfo.columnName}", ${columnInfo.lowerCamelCaseName});
    </#if>
</#list>

        // 精确查询
<#list queryRequestList as columnInfo>
    <#if columnInfo.javaType == "Long" || columnInfo.javaType == "Integer" || columnInfo.javaType == "Date">
        queryWrapper.eq(ObjUtil.isNotEmpty(${columnInfo.lowerCamelCaseName}), "${columnInfo.columnName}", ${columnInfo.lowerCamelCaseName});
    </#if>
</#list>
        
        // 排序规则
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField
        );
        
        return queryWrapper;
    }

    /**
     * 获取${tableInfo.tableComment}封装
     *
     * @param ${tableInfo.fieldName}
     * @return
     */
    @Override
    public ${tableInfo.className}VO get${tableInfo.className}VO(${tableInfo.className} ${tableInfo.fieldName}) {
        // 对象转封装类
        ${tableInfo.className}VO ${tableInfo.fieldName}VO = ${tableInfo.className}VO.objToVo(${tableInfo.fieldName});
        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
<#list voList as columnInfo>
<#if columnInfo.columnName == "userId">
        // 1. 关联查询用户信息
        Long userId = ${tableInfo.fieldName}.getUserId();
        User user = null;
        if (userId != null && userId ${">"} 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        ${tableInfo.fieldName}VO.setUser(userVO);
<#break>
<#elseif !columnInfo_has_next && columnInfo.columnName != "userId">
        // 1. 关联查询用户信息
        // Long userId = ${tableInfo.fieldName}.getUserId();
        // User user = null;
        // if (userId != null && userId ${">"} 0) {
        //     user = userService.getById(userId);
        // }
        // UserVO userVO = userService.getUserVO(user);
        // ${tableInfo.fieldName}VO.setUser(userVO);
<#break>
</#if>
</#list>
        // endregion

        return ${tableInfo.fieldName}VO;
    }

    /**
     * 分页获取${tableInfo.tableComment}封装
     *
     * @param ${tableInfo.fieldName}Page
     * @return
     */
    @Override
    public Page${"<"}${tableInfo.className}VO${">"} get${tableInfo.className}VOPage(Page${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}Page) {
        List${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}List = ${tableInfo.fieldName}Page.getRecords();
        Page${"<"}${tableInfo.className}VO${">"} ${tableInfo.fieldName}VOPage = new Page${"<"}${">"}(${tableInfo.fieldName}Page.getCurrent(), ${tableInfo.fieldName}Page.getSize(), ${tableInfo.fieldName}Page.getTotal());
        if (CollUtil.isEmpty(${tableInfo.fieldName}List)) {
            return ${tableInfo.fieldName}VOPage;
        }
        // 对象列表 =${">"} 封装对象列表
        List${"<"}${tableInfo.className}VO${">"} ${tableInfo.fieldName}VOList = ${tableInfo.fieldName}List.stream().map(${tableInfo.className}VO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
<#list voList as columnInfo>
<#if columnInfo.columnName == "userId">
        // 1. 关联查询用户信息
        Set${"<"}Long${">"} userIdSet = ${tableInfo.fieldName}List.stream().map(${tableInfo.className}::getUserId).collect(Collectors.toSet());
        Map${"<"}Long, List${"<"}User${">"}${">"} userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        ${tableInfo.fieldName}VOList.forEach(${tableInfo.fieldName}VO -${">"} {
            Long userId = ${tableInfo.fieldName}VO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            ${tableInfo.fieldName}VO.setUser(userService.getUserVO(user));
        });
<#break>
<#elseif !columnInfo_has_next && columnInfo.columnName != "userId">
        // 1. 关联查询用户信息
        // Set${"<"}Long${">"} userIdSet = ${tableInfo.fieldName}List.stream().map(${tableInfo.className}::getUserId).collect(Collectors.toSet());
        // Map${"<"}Long, List${"<"}User${">"}${">"} userIdUserListMap = userService.listByIds(userIdSet).stream()
        //         .collect(Collectors.groupingBy(User::getId));
        // // 填充信息
        // ${tableInfo.fieldName}VOList.forEach(${tableInfo.fieldName}VO -${">"} {
        //     Long userId = ${tableInfo.fieldName}VO.getUserId();
        //     User user = null;
        //     if (userIdUserListMap.containsKey(userId)) {
        //         user = userIdUserListMap.get(userId).get(0);
        //     }
        //     ${tableInfo.fieldName}VO.setUser(userService.getUserVO(user));
        // });
<#break>
</#if>
</#list>
        // endregion

        ${tableInfo.fieldName}VOPage.setRecords(${tableInfo.fieldName}VOList);

        return ${tableInfo.fieldName}VOPage;
    }

}
