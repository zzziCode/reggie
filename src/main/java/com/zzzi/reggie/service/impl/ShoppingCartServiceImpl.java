package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.entity.ShoppingCart;
import com.zzzi.reggie.mapper.ShoppingCartMapper;
import com.zzzi.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    //清空购物车的代码写在这里，便于别人调用
    @Override
    public void removeByUserId(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        remove(queryWrapper);
    }
}
