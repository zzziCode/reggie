package com.zzzi.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author zzzi
 * @date 2023/12/2 9:37
 * 添加一个全局的异常处理器
 * 这就是相当于将项目中的异常全部拦截
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //处理SQL的异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> SQLIntegrityConstraintViolationExceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.error(e.getMessage());
        if (e.getMessage().contains("Duplicate entry")) {
            String[] split = e.getMessage().split(" ");
            String msg = split[2] + "重复";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    //处理删除分类的异常
    @ExceptionHandler(CustomException.class)
    public R<String> CustomExceptionHandler(CustomException e) {
        log.error(e.getMessage());
        if (e.getMessage().contains("当前分类下关联了")) {
            return R.error(e.getMessage());
        }
        if (e.getMessage().contains("起售状态")) {
            return R.error(e.getMessage());
        }
        if (e.getMessage().contains("无法下单")) {
            return R.error(e.getMessage());
        }
        return R.error("未知错误");
    }

}
