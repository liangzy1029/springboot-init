package ${tableInfo.packageName}.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${tableInfo.packageName}.annotation.AuthCheck;
import ${tableInfo.packageName}.common.BaseResponse;
import ${tableInfo.packageName}.common.DeleteRequest;
import ${tableInfo.packageName}.common.ErrorCode;
import ${tableInfo.packageName}.common.ResultUtils;
import ${tableInfo.packageName}.constant.CommonConstant;
import ${tableInfo.packageName}.exception.BusinessException;
import ${tableInfo.packageName}.exception.ThrowUtils;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}AddRequest;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}EditRequest;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}QueryRequest;
import ${tableInfo.packageName}.model.dto.${tableInfo.dtoPackageName}.${tableInfo.className}UpdateRequest;
import ${tableInfo.packageName}.model.entity.${tableInfo.className};
import ${tableInfo.packageName}.model.entity.User;
import ${tableInfo.packageName}.model.enums.UserRoleEnum;
import ${tableInfo.packageName}.model.vo.${tableInfo.className}VO;
import ${tableInfo.packageName}.service.${tableInfo.className}Service;
import ${tableInfo.packageName}.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${tableInfo.tableComment}接口
 */
@Slf4j
@RestController
@RequestMapping("/${tableInfo.tableName}")
@RequiredArgsConstructor
public class ${tableInfo.className}Controller {

    private final ${tableInfo.className}Service ${tableInfo.fieldName}Service;

    private final UserService userService;

    // region 增删改查

    /**
     * 创建${tableInfo.tableComment}
     *
     * @param ${tableInfo.fieldName}AddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse${"<"}Long${">"} add${tableInfo.className}(@RequestBody ${tableInfo.className}AddRequest ${tableInfo.fieldName}AddRequest) {
        // 在此处将实体类和 DTO 进行转换
        ${tableInfo.className} ${tableInfo.fieldName} = new ${tableInfo.className}();
        BeanUtils.copyProperties(${tableInfo.fieldName}AddRequest, ${tableInfo.fieldName});
        // 数据校验
        ${tableInfo.fieldName}Service.valid${tableInfo.className}(${tableInfo.fieldName}, true);
        // 填充默认值
<#list queryRequestList as columnInfo>
<#if columnInfo.columnName == "userId">
        User loginUser = userService.getLoginUser();
        ${tableInfo.fieldName}.setUserId(loginUser.getId());
<#break>
</#if>
</#list>
        // 写入数据库
        boolean result = ${tableInfo.fieldName}Service.save(${tableInfo.fieldName});
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long new${tableInfo.className}Id = ${tableInfo.fieldName}.getId();
        return ResultUtils.success(new${tableInfo.className}Id);
    }

    /**
     * 删除${tableInfo.tableComment}（仅本人或管理员可用）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
<#list queryRequestList as columnInfo>
<#if columnInfo.columnName == "userId">
    @AuthCheck(mustRole = UserRoleEnum.USER)
<#break>
<#elseif !columnInfo_has_next && columnInfo.columnName != "userId">
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
<#break>
</#if>
</#list>
    public BaseResponse${"<"}Boolean${">"} delete${tableInfo.className}(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id ${"<"}= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 判断是否存在
        ${tableInfo.className} old${tableInfo.className} = ${tableInfo.fieldName}Service.getById(id);
        ThrowUtils.throwIf(old${tableInfo.className} == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (<#list queryRequestList as columnInfo><#if columnInfo.columnName == "userId">!old${tableInfo.className}.getUserId().equals(loginUser.getId()) &&<#break></#if></#list>!userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = ${tableInfo.fieldName}Service.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新${tableInfo.tableComment}（仅管理员可用）
     *
     * @param ${tableInfo.fieldName}UpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse${"<"}Boolean${">"} update${tableInfo.className}(@RequestBody ${tableInfo.className}UpdateRequest ${tableInfo.fieldName}UpdateRequest) {
        Long id = ${tableInfo.fieldName}UpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id ${"<"}= 0, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        ${tableInfo.className} ${tableInfo.fieldName} = new ${tableInfo.className}();
        BeanUtils.copyProperties(${tableInfo.fieldName}UpdateRequest, ${tableInfo.fieldName});
        // 数据校验
        ${tableInfo.fieldName}Service.valid${tableInfo.className}(${tableInfo.fieldName}, false);
        // 判断是否存在
        ${tableInfo.className} old${tableInfo.className} = ${tableInfo.fieldName}Service.getById(id);
        ThrowUtils.throwIf(old${tableInfo.className} == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = ${tableInfo.fieldName}Service.updateById(${tableInfo.fieldName});
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新${tableInfo.tableComment}（给用户使用）
     *
     * @param ${tableInfo.fieldName}EditRequest
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse${"<"}Boolean${">"} edit${tableInfo.className}(@RequestBody ${tableInfo.className}EditRequest ${tableInfo.fieldName}EditRequest) {
        Long id = ${tableInfo.fieldName}EditRequest.getId();
        ThrowUtils.throwIf(id == null || id ${"<"}= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 在此处将实体类和 DTO 进行转换
        ${tableInfo.className} ${tableInfo.fieldName} = new ${tableInfo.className}();
        BeanUtils.copyProperties(${tableInfo.fieldName}EditRequest, ${tableInfo.fieldName});
        // 数据校验
        ${tableInfo.fieldName}Service.valid${tableInfo.className}(${tableInfo.fieldName}, false);
        // 判断是否存在
        ${tableInfo.className} old${tableInfo.className} = ${tableInfo.fieldName}Service.getById(id);
        ThrowUtils.throwIf(old${tableInfo.className} == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!old${tableInfo.className}.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = ${tableInfo.fieldName}Service.updateById(${tableInfo.fieldName});
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取${tableInfo.tableComment}
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse${"<"}${tableInfo.className}${">"} get${tableInfo.className}ById(long id) {
        ThrowUtils.throwIf(id ${"<"}= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        ${tableInfo.className} ${tableInfo.fieldName} = ${tableInfo.fieldName}Service.getById(id);
        ThrowUtils.throwIf(${tableInfo.fieldName} == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(${tableInfo.fieldName});
    }

    /**
     * 根据 id 获取${tableInfo.tableComment}（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse${"<"}${tableInfo.className}VO${">"} get${tableInfo.className}VOById(long id) {
        ThrowUtils.throwIf(id ${"<"}= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        ${tableInfo.className} ${tableInfo.fieldName} = ${tableInfo.fieldName}Service.getById(id);
        ThrowUtils.throwIf(${tableInfo.fieldName} == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(${tableInfo.fieldName}Service.get${tableInfo.className}VO(${tableInfo.fieldName}));
    }

    /**
     * 分页获取${tableInfo.tableComment}列表（仅管理员可用）
     *
     * @param ${tableInfo.fieldName}QueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse${"<"}Page${"<"}${tableInfo.className}${">"}${">"} list${tableInfo.className}ByPage(@RequestBody ${tableInfo.className}QueryRequest ${tableInfo.fieldName}QueryRequest) {
        long current = ${tableInfo.fieldName}QueryRequest.getCurrent();
        long size = ${tableInfo.fieldName}QueryRequest.getPageSize();
        // 查询数据库
        Page${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}Page = ${tableInfo.fieldName}Service.page(new Page${"<"}${">"}(current, size), ${tableInfo.fieldName}Service.getQueryWrapper(${tableInfo.fieldName}QueryRequest));
        return ResultUtils.success(${tableInfo.fieldName}Page);
    }

    /**
     * 分页获取${tableInfo.tableComment}列表（封装类）
     *
     * @param ${tableInfo.fieldName}QueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse${"<"}Page${"<"}${tableInfo.className}VO${">"}${">"} list${tableInfo.className}VOByPage(@RequestBody ${tableInfo.className}QueryRequest ${tableInfo.fieldName}QueryRequest) {
        long current = ${tableInfo.fieldName}QueryRequest.getCurrent();
        long size = ${tableInfo.fieldName}QueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size ${">"} CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}Page = ${tableInfo.fieldName}Service.page(new Page${"<"}${">"}(current, size), ${tableInfo.fieldName}Service.getQueryWrapper(${tableInfo.fieldName}QueryRequest));
        // 获取封装类
        return ResultUtils.success(${tableInfo.fieldName}Service.get${tableInfo.className}VOPage(${tableInfo.fieldName}Page));
    }

<#list queryRequestList as columnInfo>
<#if columnInfo.columnName == "userId">
    /**
     * 分页获取${tableInfo.tableComment}列表（用户）
     *
     * @param ${tableInfo.fieldName}QueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse${"<"}Page${"<"}${tableInfo.className}VO${">"}${">"} listMy${tableInfo.className}VOByPage(@RequestBody ${tableInfo.className}QueryRequest ${tableInfo.fieldName}QueryRequest) {
        long current = ${tableInfo.fieldName}QueryRequest.getCurrent();
        long size = ${tableInfo.fieldName}QueryRequest.getPageSize();
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser();
        ${tableInfo.fieldName}QueryRequest.setUserId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size ${">"} CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page${"<"}${tableInfo.className}${">"} ${tableInfo.fieldName}Page = ${tableInfo.fieldName}Service.page(new Page${"<"}${">"}(current, size), ${tableInfo.fieldName}Service.getQueryWrapper(${tableInfo.fieldName}QueryRequest));
        // 获取封装类
        return ResultUtils.success(${tableInfo.fieldName}Service.get${tableInfo.className}VOPage(${tableInfo.fieldName}Page));
    }
<#break>
</#if>
</#list>

    // endregion
}
