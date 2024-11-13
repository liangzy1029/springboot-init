package com.lzy.springbootinit.aop;

import com.lzy.springbootinit.annotation.AuthCheck;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuthInterceptor {

    private final UserService userService;

    // 定义一个切点，匹配Controller包下所有类的所有方法
    @Pointcut("execution(public * com.lzy.springbootinit.controller..*.*(..))")
    public void controllerPackageMethods() {
    }

    @Around("controllerPackageMethods()")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuthCheck authCheck = method.getAnnotation(AuthCheck.class);
        // RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 不需要权限，放行
        if (authCheck != null && !authCheck.mustLogin()) {
            return joinPoint.proceed();
        }
        // 必须有权限才通过
        // 当前登录用户
        User loginUser = userService.getLoginUser();
        UserRoleEnum loginUserRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (loginUserRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果被封号，直接拒绝
        if (loginUserRoleEnum == UserRoleEnum.BAN) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 接口需要的登录权限
        UserRoleEnum mustRoleEnum = authCheck == null ? UserRoleEnum.USER : authCheck.mustRole();
        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            // 用户没有管理员权限，拒绝
            if (!UserRoleEnum.ADMIN.equals(loginUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
