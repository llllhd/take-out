package com.sky.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    /**
     * 自动填充切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知，在通知中自动填充公共字段
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始自动填充公共字段");

        // 获取数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autofill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autofill.value();

        // 获取被拦截方法的参数，即数据库操作的实体对象
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Object entity = args[0];
            // 根据数据库操作类型，自动填充公共字段
            LocalDateTime now = LocalDateTime.now();
            long currentId = BaseContext.getCurrentId();

            if (operationType == OperationType.INSERT) {
                try {
                    Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                    setCreateTime.invoke(entity, now);
                    setCreateUser.invoke(entity, currentId);
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            } else if (operationType == OperationType.UPDATE) {
                try {
                    Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
