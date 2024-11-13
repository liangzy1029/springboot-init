package com.lzy.springbootinit.common;

/**
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     * @return 通用返回
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    // region 失败

    /**
     * 失败
     *
     * @param errorCode 自定义错误
     * @return 通用返回
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 失败
     *
     * @param errorCode 自定义错误
     * @param message   失败信息
     * @return 通用返回
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }

    /**
     * 失败
     *
     * @param code    失败状态码
     * @param message 失败信息
     * @return 通用返回
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    // endregion
}
