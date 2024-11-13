package com.lzy.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.lzy.springbootinit.annotation.AuthCheck;
import com.lzy.springbootinit.common.BaseResponse;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.common.ResultUtils;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.manager.CosManager;
import com.lzy.springbootinit.model.dto.file.UploadFileRequest;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.FileUploadBizEnum;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UserService userService;

    private final CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<String> uploadFile(UploadFileRequest uploadFileRequest, @RequestPart("file") MultipartFile multipartFile) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验文件
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser();
        // 文件目录：根据业务、用户来划分
        String filename = UUID.randomUUID().toString(true) + "." + FileUtil.getSuffix(multipartFile.getOriginalFilename());
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

}
