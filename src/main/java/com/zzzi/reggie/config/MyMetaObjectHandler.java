package com.zzzi.reggie.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zzzi.reggie.common.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author zzzi
 * @date 2023/12/2 15:23
 * 所有实体的四个公共字段在这里统一进行填充，就不用在之后的新增和修改时每次都手动编写填充的代码了
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    //插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入时自动填充属性");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    //更新时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新时自动填充属性");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
